/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions;

import static edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions.ArchitecturalDesignDecision.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;

public class DesignDecisionKindClassifierOracle implements DesignDecisionKindClassifier {
    private static final Logger logger = LoggerFactory.getLogger(DesignDecisionKindClassifierOracle.class);

    private Map<String, String> nameToResource = Map.of(//
            "bigbluebutton", "ClassificationOracleData/bigbluebutton.csv", //
            "jabref", "ClassificationOracleData/jabref.csv", //
            "mediastore", "ClassificationOracleData/mediastore.csv", //
            "teammates", "ClassificationOracleData/teammates.csv", //
            "teastore", "ClassificationOracleData/teastore.csv");

    private Map<Integer, ArchitecturalDesignDecision> cache = new HashMap<>();

    public DesignDecisionKindClassifierOracle(String projectName) {
        loadClassifications(nameToResource.get(projectName));
    }

    private void loadClassifications(String classificationOracleResource) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(classificationOracleResource)));) {
            for (var line : reader.lines().toList()) {
                StringTokenizer tokens = new StringTokenizer(line, ";");
                int sentence = -1;
                try {
                    sentence = Integer.parseInt(tokens.nextToken());
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse sentence number", e);
                }
                boolean hasNoDesignDecision = tokens.nextToken().equals("0");
                if (hasNoDesignDecision) {
                    cache.put(sentence, ArchitecturalDesignDecision.NO_DESIGN_DECISION);
                } else {
                    String primaryClassification = tokens.nextToken();
                    ArchitecturalDesignDecision architecturalDesignDecision = processClassification(primaryClassification);
                    cache.put(sentence, architecturalDesignDecision);
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to load classifications from file. ", e);
        }
    }

    private ArchitecturalDesignDecision processClassification(String classification) {
        return switch (classification) {
        case "data file" -> DATA_FILE;
        case "integration" -> INTEGRATION;
        case "interface" -> INTERFACE;
        case "component" -> COMPONENT;
        case "association" -> ASSOCIATION;
        case "class" -> CLASS;
        case "inheritance" -> INHERITANCE;
        case "architectural style" -> ARCHITECTURAL_STYLE;
        case "architectural pattern" -> ARCHITECTURAL_PATTERN;
        case "reference architecture" -> REFERENCE_ARCHITECTURE;
        case "relation" -> RELATION;
        case "function" -> FUNCTION;
        case "algorithm" -> ALGORITHM;
        case "messaging" -> MESSAGING;
        case "guideline" -> GUIDELINE;
        case "design rule" -> DESIGN_RULE;
        case "organizational/process-related" -> ORGANIZATIONAL_PROCESS_RELATED;
        case "tool" -> TOOL;
        case "data base" -> DATA_BASE;
        case "platform" -> PLATFORM;
        case "user interface" -> USER_INTERFACE;
        case "api" -> API;
        case "programming language" -> PROGRAMMING_LANGUAGE;
        case "framework" -> FRAMEWORK;
        default -> NO_DESIGN_DECISION;
        };
    }

    /**
     * Returns if a sentence has a given {@link ArchitecturalDesignDecision} based on the classification. The results also incorporates that the sentence can be
     * classified as a children of the given class. In this case, it still returns true as children are more specific and thus also decisions of the given kind.
     * 
     * The classification uses an oracle, i.e., it looks at a manually created gold standard. This is to assess the best possible results without classification
     * errors on the side of the automated classifier.
     * 
     * @param sentence the sentence that should be checked
     * @param kind     the kind
     * @return if the sentence is classified as the kind (or a children of that kind)
     */
    @Override
    public boolean sentenceHasKind(Sentence sentence, ArchitecturalDesignDecision kind) {
        ArchitecturalDesignDecision sentenceKind = classifySentence(sentence);
        return sentenceKind.isContainedIn(kind);
    }

    @Override
    public ArchitecturalDesignDecision classifySentence(Sentence sentence) {
        int sentenceNumber = sentence.getSentenceNumberForOutput();
        if (cache.containsKey(sentenceNumber)) {
            return cache.get(sentenceNumber);
        }
        ArchitecturalDesignDecision designDecision = ArchitecturalDesignDecision.NO_DESIGN_DECISION;
        cache.put(sentenceNumber, designDecision);
        return designDecision;
    }

    /**
     * Returns if a sentence contains a design decision. This method is similar to {@link #sentenceHasKind(Sentence, ArchitecturalDesignDecision)} but uses a
     * fixed {@link ArchitecturalDesignDecision}.
     * 
     * @param sentence the sentence that should be checked
     * @return if the sentence contains a design decision.
     */
    @Override
    public boolean sentenceHasDesignDecision(Sentence sentence) {
        return sentenceHasKind(sentence, DESIGN_DECISION);
    }

}
