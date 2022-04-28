/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This agent uses data from DBPedia to mark default words in computer science.
 *
 * @author Dominik Fuchss
 */
public class ComputerScienceWordsAgent extends TextAgent {

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

    private final ImmutableList<String> commonCSWords;

    public ComputerScienceWordsAgent() {
        this.commonCSWords = loadWords();
    }

    @Override
    public void execute(TextAgentData data) {
        var text = data.getText();
        var textState = data.getTextState();

        Set<INounMapping> processed = new HashSet<>();
        for (var word : text.getWords()) {
            var nounMappings = textState.getNounMappingsByWord(word);
            for (var nounMapping : nounMappings)
                processNounMapping(textState, word, nounMapping, processed);
        }
    }

    private void processNounMapping(ITextState textState, IWord word, INounMapping nounMapping, Set<INounMapping> processed) {
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

            switch (mode) {
            case ADD_PROBABILITY -> addProbability(nounMapping);
            case DELETE_OCCURRENCE -> deleteOccurrence(textState, nounMapping);
            }

        }
    }

    private void addProbability(INounMapping nounMapping) {
        nounMapping.addKindWithProbability(MappingKind.NAME, this, probabilityOfFoundWords);
        nounMapping.addKindWithProbability(MappingKind.TYPE, this, probabilityOfFoundWords);
    }

    private void deleteOccurrence(ITextState textState, INounMapping nounMapping) {
        textState.removeNounMapping(nounMapping);
    }

    private boolean match(INounMapping nounMapping, String csWord) {
        String[] csParts = csWord.split("\\s+");
        IWord[] nmWords = nounMapping.getWords().toArray(new IWord[0]);

        for (int start = 0; start < nmWords.length; start++) {
            IWord[] remaining = Arrays.copyOfRange(nmWords, start, nmWords.length);
            if (remaining.length < csParts.length)
                return false;
            // Try to match ..
            IWord[] wordsToMatch = Arrays.copyOfRange(remaining, 0, csParts.length);
            boolean match = matchIt(csParts, wordsToMatch);
            if (match)
                return true;
        }

        return false;
    }

    private boolean matchIt(String[] csParts, IWord[] wordsToMatch) {
        assert csParts.length == wordsToMatch.length;

        for (int i = 0; i < csParts.length; i++) {
            // TODO Maybe Lemma etc ..
            String csWord = csParts[i];
            String word = wordsToMatch[i].getText();

            if (Math.min(csWord.length(), word.length()) < wordMinLengthForSimilarity && !csWord.equalsIgnoreCase(word)) {
                return false;
            }
            if (!SimilarityUtils.areWordsSimilar(csWord, word, wordSimilarityThreshold)) {
                return false;
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("Matched CS Word [{}] with Words in Text [{}] ", String.join(" ", csParts),
                    String.join(" ", Arrays.stream(wordsToMatch).map(IWord::getText).toList()));
        return true;
    }

    private ImmutableList<String> loadWords() {
        Set<String> result = new HashSet<>();
        loadDBPedia(result);
        loadISO24765(result);
        loadStandardGlossary(result);
        result.addAll(additionalWords);
        // Remove after bracket (
        result = result.stream().map(e -> e.split("\\(")[0].trim()).collect(Collectors.toSet());
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
            return oom.readValue(data, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // No Delegates
    }

    private enum CSWAgentMode {
        ADD_PROBABILITY, DELETE_OCCURRENCE
    }
}
