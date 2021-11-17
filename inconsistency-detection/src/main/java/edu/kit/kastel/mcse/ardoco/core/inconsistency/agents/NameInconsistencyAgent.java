package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.NameInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class NameInconsistencyAgent extends InconsistencyAgent {
    private static final String REGEX_DIVIDER = "[\\.:]";

    public NameInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private NameInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        super(GenericInconsistencyConfig.class, text, textState, modelState, recommendationState, connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new NameInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState);

    }

    @Override
    public void exec() {
        ImmutableList<IInstanceLink> tracelinks = connectionState.getInstanceLinks();
        for (IInstanceLink tracelink : tracelinks) {
            IModelInstance modelInstance = tracelink.getModelInstance();
            var recommendationInstance = tracelink.getTextualInstance();
            var nameMappings = recommendationInstance.getNameMappings();
            for (INounMapping nameMapping : nameMappings) {
                var words = nameMapping.getWords();
                for (IWord word : words) {
                    analyseWord(modelInstance, word);
                }
            }
        }
    }

    private void analyseWord(IModelInstance modelInstance, IWord word) {
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
    private static boolean equalTextOrLemma(IModelInstance modelInstance, IWord word) {
        String modelName = modelInstance.getLongestName();
        String text = word.getText();
        String lemma = word.getLemma();

        // TODO employ other/better String comparison instead of exact equals?
        return modelName.equalsIgnoreCase(text) && modelName.equalsIgnoreCase(lemma);
    }

    private static boolean partOfDividerEqualsModelInstance(IModelInstance modelInstance, IWord word) {
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
