/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class ComputerScienceWordsInformant extends Informant {

    private static final int MAX_WIKI_LEVEL = 3;

    private static final String WIKI = "WIKI";
    private static final String ISO24765 = "ISO24765";
    private static final String STANDARD_GLOSSARY = "STANDARD_GLOSSARY";

    /**
     * This is the probability that will be assigned to found words.
     */
    @Configurable
    private double probabilityOfFoundWords = 1E-8;

    @Configurable
    private CSWAgentMode mode = CSWAgentMode.ADD_PROBABILITY;

    @Configurable
    private List<String> sources = List.of(WIKI, ISO24765, STANDARD_GLOSSARY);

    @Configurable
    private int maxWikiLevels = MAX_WIKI_LEVEL;

    @Configurable
    private double wordSimilarityThreshold = 0.99;
    @Configurable
    private double wordMinLengthForSimilarity = 4;

    @Configurable
    private List<String> additionalWords = List.of();

    @Configurable
    private boolean enabled = false;

    private final ImmutableList<String> commonCSWords;

    public ComputerScienceWordsInformant(DataRepository data) {
        super(ComputerScienceWordsInformant.class.getSimpleName(), data);
        this.commonCSWords = loadWords();
    }

    @Override
    public void run() {
        var text = DataRepositoryHelper.getAnnotatedText(getDataRepository());
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        execute(text, textState);
    }

    public void execute(Text text, TextState textState) {
        if (!enabled)
            return;

        Set<NounMapping> processed = new HashSet<>();
        for (var word : text.words()) {
            var nounMappings = textState.getNounMappingsByWord(word);
            for (var nounMapping : nounMappings) {
                processNounMapping(textState, word, nounMapping, processed);
            }
        }
    }

    private void processNounMapping(TextState textState, Word word, NounMapping nounMapping, Set<NounMapping> processed) {
        if (processed.contains(nounMapping))
            return;
        processed.add(nounMapping);
        // TODO Handle Phrases
        Predicate<String> predicate = commonWord -> match(nounMapping, commonWord);
        if (this.commonCSWords.stream().anyMatch(predicate)) {
            if (logger.isTraceEnabled()) {
                var occurrence = this.commonCSWords.stream().filter(predicate).findFirst().orElseThrow();
                logger.trace("Found {} for {}", occurrence, word);
            }

            if (mode == ComputerScienceWordsInformant.CSWAgentMode.ADD_PROBABILITY) {
                addProbability(nounMapping);
            } else if (mode == ComputerScienceWordsInformant.CSWAgentMode.DELETE_OCCURRENCE) {
                deleteOccurrence(textState, nounMapping);
            }

        }
    }

    private void addProbability(NounMapping nounMapping) {
        nounMapping.addKindWithProbability(MappingKind.NAME, this, probabilityOfFoundWords);
        nounMapping.addKindWithProbability(MappingKind.TYPE, this, probabilityOfFoundWords);
    }

    private void deleteOccurrence(TextState textState, NounMapping nounMapping) {
        textState.removeNounMapping(nounMapping, null);
    }

    private boolean match(NounMapping nounMapping, String csWord) {
        String[] csParts = csWord.split("\\s+");
        Word[] nmWords = nounMapping.getWords().toArray(new Word[0]);

        for (int start = 0; start < nmWords.length; start++) {
            Word[] remaining = Arrays.copyOfRange(nmWords, start, nmWords.length);
            if (remaining.length < csParts.length)
                return false;
            // Try to match ..
            Word[] wordsToMatch = Arrays.copyOfRange(remaining, 0, csParts.length);
            boolean match = matchIt(csParts, wordsToMatch);
            if (match)
                return true;
        }

        return false;
    }

    private boolean matchIt(String[] csParts, Word[] wordsToMatch) {
        assert csParts.length == wordsToMatch.length;

        for (int i = 0; i < csParts.length; i++) {
            // TODO Maybe Lemma etc ..
            String csWord = csParts[i];
            String word = wordsToMatch[i].getText();

            if (Math.min(csWord.length(), word.length()) < wordMinLengthForSimilarity && !csWord.equalsIgnoreCase(word)) {
                return false;
            }
            if (!SimilarityUtils.areWordsSimilar(csWord, word)) { // TODO: Check how to use wordSimilarityThreshold here
                return false;
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("Matched CS Word [{}] with Words in Text [{}] ", String.join(" ", csParts), String.join(" ", Arrays.stream(wordsToMatch)
                    .map(Word::getText)
                    .toList()));
        return true;
    }

    private ImmutableList<String> loadWords() {
        Set<String> result = new HashSet<>();
        loadDBPedia(result);
        loadISO24765(result);
        loadStandardGlossary(result);
        result.addAll(additionalWords);
        // Remove after bracket (
        result = result.stream().map(e -> e.split("\\(", -1)[0].trim()).collect(Collectors.toSet());
        return Lists.immutable.withAll(result.stream().map(w -> w.trim().toLowerCase()).toList());
    }

    private void loadDBPedia(Set<String> result) {
        if (!this.sources.contains(WIKI))
            return;

        int before = result.size();
        logger.debug("Loading words from DBPedia");

        for (int level = 0; level <= maxWikiLevels; level++) {
            JsonNode tree;
            try (InputStream data = this.getClass().getResourceAsStream("/dbpedia/common-cs-" + level + ".json")) {
                ObjectMapper oom = new ObjectMapper();
                tree = oom.readTree(data);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            tree = tree.get("results").get("bindings");
            tree.spliterator().forEachRemaining(n -> result.add(n.get("alabel").get("value").textValue()));
        }
        result.removeIf(Objects::isNull);
        logger.debug("Found {} words by adding DBPedia", result.size() - before);
    }

    private void loadISO24765(Set<String> result) {
        if (!this.sources.contains(ISO24765))
            return;

        int before = result.size();
        logger.debug("Loading words from ISO24765");
        List<String> words = loadWordsFromResource("/pdfs/24765-2017.pdf.words.txt");
        result.addAll(words);
        logger.debug("Found {} words by adding ISO24765", result.size() - before);
    }

    private void loadStandardGlossary(Set<String> result) {
        if (!this.sources.contains(STANDARD_GLOSSARY))
            return;

        int before = result.size();
        logger.debug("Loading words from STANDARD_GLOSSARY");
        List<String> words = loadWordsFromResource("/pdfs/Standard_glossary_of_terms_used_in_Software_Engineering_1.0.pdf.words.txt");
        result.addAll(words);
        logger.debug("Found {} words by adding STANDARD_GLOSSARY", result.size() - before);
    }

    private List<String> loadWordsFromResource(String path) {
        try (InputStream data = this.getClass().getResourceAsStream(path)) {
            ObjectMapper oom = new ObjectMapper();
            return oom.readValue(data, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        //None
    }

    private enum CSWAgentMode {
        ADD_PROBABILITY, DELETE_OCCURRENCE
    }
}
