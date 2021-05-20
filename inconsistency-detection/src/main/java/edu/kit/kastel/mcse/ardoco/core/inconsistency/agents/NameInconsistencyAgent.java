package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures.NameInconsistency;

@MetaInfServices(InconsistencyAgent.class)
public class NameInconsistencyAgent extends InconsistencyAgent {
    private static final String REGEX_DIVIDER = "[\\.:]";

    public NameInconsistencyAgent() {
        super(InconsistencyConfig.class);
    }

    private NameInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, InconsistencyConfig inconsistencyConfig) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, InconsistencyConfig.class, text, textState, modelState, recommendationState,
                connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new NameInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (InconsistencyConfig) config);

    }

    @Override
    public void exec() {
        List<IInstanceLink> tracelinks = connectionState.getInstanceLinks();
        for (IInstanceLink tracelink : tracelinks) {
            IInstance modelInstance = tracelink.getModelInstance();
            IRecommendedInstance recommendationInstance = tracelink.getTextualInstance();
            List<INounMapping> nameMappings = recommendationInstance.getNameMappings();
            for (INounMapping nameMapping : nameMappings) {
                List<IWord> words = nameMapping.getWords();
                for (IWord word : words) {
                    analyseWord(modelInstance, word);
                }
            }
        }
    }

    private void analyseWord(IInstance modelInstance, IWord word) {
        if (!equalTextOrLemma(modelInstance, word)) {
            if (partOfDividerEqualsModelInstance(modelInstance, word)) {
                return;
            }
            var nameInconsistency = new NameInconsistency(modelInstance, word);
            inconsistencyState.addInconsistency(nameInconsistency);
        }
    }

    /**
     * Return if text or lemma of provided word matches to the model instance
     *
     * @param modelInstance the model instance
     * @param word          the word
     * @return <code>true</code>, if there is a match, otherwise <code>false</code>
     */
    private boolean equalTextOrLemma(IInstance modelInstance, IWord word) {
        String modelName = modelInstance.getLongestName();
        String text = word.getText();
        String lemma = word.getLemma();

        // TODO employ other/better String comparison instead of exact equals?
        return modelName.equalsIgnoreCase(text) && modelName.equalsIgnoreCase(lemma);
    }

    private boolean partOfDividerEqualsModelInstance(IInstance modelInstance, IWord word) {
        String text = word.getText();
        String modelName = modelInstance.getLongestName();
        for (String part : text.split(REGEX_DIVIDER)) {
            // TODO employ other/better String comparison instead of exact equals?
            if (part.equalsIgnoreCase(modelName)) {
                return true;
            }
        }
        return false;
    }
}
