/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.informants;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * This Filter checks for common computer science words and filters them. Unfortunately, this is very rigorous and filters a lot of words that we actually want
 * to cover.
 * Examples include "facade" and "database". This is because inconsistencies (missing elements) that we want to find share wording with common CS words.
 * We have to analyze how we want to deal with this. One idea is to not fully filter them but take care in another way.
 */
public class ComputerScienceWordsFilter extends Filter {

    private static final String WIKI = "WIKI";
    private static final String ISO24765 = "ISO24765";
    private static final String STANDARD_GLOSSARY = "STANDARD_GLOSSARY";

    @Configurable
    private List<String> sources = List.of(STANDARD_GLOSSARY);

    @Configurable
    private List<String> additionalWords = List.of();

    private final ImmutableList<String> commonCSWords;

    public ComputerScienceWordsFilter(DataRepository data) {
        super(ComputerScienceWordsFilter.class.getSimpleName(), data);
        this.commonCSWords = loadWords();
    }

    @Override
    protected void filterRecommendedInstances(InconsistencyState inconsistencyState) {
        var originalRecommendedInstances = inconsistencyState.getRecommendedInstances();
        var recommendedInstancesToKeep = Lists.mutable.<RecommendedInstance>empty();
        var recommendedInstancesToDismiss = Lists.mutable.empty();

        for (var recommendedInstance : originalRecommendedInstances) {
            boolean shallBeFiltered = checkRecommendedInstance(recommendedInstance);
            if (!shallBeFiltered)
                recommendedInstancesToKeep.add(recommendedInstance);
            else
                recommendedInstancesToDismiss.add(recommendedInstance);
        }

        inconsistencyState.setRecommendedInstances(recommendedInstancesToKeep);
    }

    private boolean checkRecommendedInstance(RecommendedInstance recommendedInstance) {
        for (var nounMapping : recommendedInstance.getNameMappings()) {
            if (checkNounMapping(nounMapping))
                return true;
        }
        return false;
    }

    private boolean checkNounMapping(NounMapping nounMapping) {
        return this.commonCSWords.stream().anyMatch(commonWord -> match(nounMapping, commonWord));
    }

    private boolean match(NounMapping nounMapping, String csWord) {
        // this uses a simple version to match NounMapping and a common CS word by just comparing the reference to the cs word
        // There is another implementation in the class ComputerScienceWordsInformant that checks more thoroughly but also has more "false positives"
        var reference = nounMapping.getReference();
        return SimilarityUtils.areWordsSimilar(reference.toLowerCase(), csWord.toLowerCase());
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

        for (String name : List.of("design", "engineering")) {
            JsonNode tree;
            try (InputStream data = this.getClass().getResourceAsStream("/dbpedia/" + name + ".json")) {
                ObjectMapper oom = new ObjectMapper();
                tree = oom.readTree(data);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            tree = tree.get("results").get("bindings");
            tree.spliterator().forEachRemaining(n -> result.add(n.get("alabel").get("value").textValue()));
        }
        result.removeIf(Objects::isNull);
        // Check that word has a type
        result.removeIf(w -> !w.contains("("));
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
        // empty
    }

}
