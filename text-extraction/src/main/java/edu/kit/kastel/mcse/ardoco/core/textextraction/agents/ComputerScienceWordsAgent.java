/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;

/**
 * This agent uses data from DBPedia to mark default words in computer science.
 *
 * @author Dominik Fuchss
 */
public class ComputerScienceWordsAgent extends TextAgent {

    /**
     * This is the probability that will be assigned to found words.
     */
    @Configurable
    private double probabilityOfFoundWords = 0.1;

    private final ImmutableList<String> commonCSWords;

    public ComputerScienceWordsAgent() {
        this.commonCSWords = loadWordsFromJSON();
    }

    @Override
    public void execute(TextAgentData data) {
        var text = data.getText();
        var textState = data.getTextState();

        Set<INounMapping> processed = new HashSet<>();
        for (var word : text.getWords()) {
            var nounMappings = textState.getNounMappingsByWord(word);
            for (var nounMapping : nounMappings)
                processNounMapping(word, nounMapping, processed);
        }
    }

    private void processNounMapping(IWord word, INounMapping nounMapping, Set<INounMapping> processed) {
        if (processed.contains(nounMapping))
            return;
        processed.add(nounMapping);
        // TODO Handle Phrases
        if (this.commonCSWords.stream().anyMatch(w -> w.contains(word.getText().toLowerCase()))) {
            nounMapping.addKindWithProbability(MappingKind.NAME, this, probabilityOfFoundWords);
            nounMapping.addKindWithProbability(MappingKind.TYPE, this, probabilityOfFoundWords);
            nounMapping.addKindWithProbability(MappingKind.NAME_OR_TYPE, this, probabilityOfFoundWords);
        }
    }

    private ImmutableList<String> loadWordsFromJSON() {
        Set<String> result = new HashSet<>();
        JsonNode tree;
        try (InputStream data = this.getClass().getResourceAsStream("/dbpedia/common-cs.json")) {
            ObjectMapper oom = new ObjectMapper();
            tree = oom.readTree(data);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }

        tree = tree.get("results").get("bindings");
        tree.spliterator().forEachRemaining(n -> result.add(n.get("alabel").get("value").textValue()));

        result.removeIf(Objects::isNull);
        logger.debug("Found {} words from DBPedia", result.size());
        return Lists.immutable.withAll(result.stream().map(w -> w.trim().toLowerCase()).toList());
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
