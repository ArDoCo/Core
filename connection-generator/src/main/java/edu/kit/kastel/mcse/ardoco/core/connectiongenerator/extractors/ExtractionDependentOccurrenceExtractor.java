/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.extractors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionExtractor;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.GenericConnectionConfig;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * This analyzer searches for the occurrence of instance names and types of the extraction state and adds them as names
 * and types to the text extraction state.
 *
 * @author Sophie
 *
 */
@MetaInfServices(ConnectionExtractor.class)
public class ExtractionDependentOccurrenceExtractor extends ConnectionExtractor {

    private double probability;

    /**
     * Creates a new extraction dependent occurrence marker.
     *
     * @param textExtractionState  the state that contains all information from the text
     * @param modelExtractionState the state that contains all information from the architecture model
     * @param recommendationState  the state that contains all recommended instances and relations
     * @param connectionState      the state that contains all information on possible trace links
     */
    public ExtractionDependentOccurrenceExtractor(//
            ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState) {
        this(textExtractionState, modelExtractionState, recommendationState, connectionState, GenericConnectionConfig.DEFAULT_CONFIG);
    }

    /**
     * Creates a new extraction dependent occurrence marker.
     *
     * @param textExtractionState  the state that contains all information from the text
     * @param modelExtractionState the state that contains all information from the architecture model
     * @param recommendationState  the state that contains all recommended instances and relations
     * @param connectionState      the state that contains all information on possible trace links
     * @param config               the configuration to be used
     */
    public ExtractionDependentOccurrenceExtractor(//
            ITextState textExtractionState, IModelState modelExtractionState, IRecommendationState recommendationState, IConnectionState connectionState,
            GenericConnectionConfig config) {
        super(textExtractionState, modelExtractionState, recommendationState, connectionState);
        probability = config.extractionDependentOccurrenceAnalyzerProbability;
    }

    /**
     * For deserialization.
     */
    public ExtractionDependentOccurrenceExtractor() {
        this(null, null, null, null);
    }

    @Override
    public ConnectionExtractor create(ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config) {
        return new ExtractionDependentOccurrenceExtractor(textState, modelExtractionState, recommendationState, connectionState,
                (GenericConnectionConfig) config);
    }

    @Override
    public void exec(IWord n) {
        // TODO revisit and check if we want to check something different than only words as well
        searchForName(n);
        searchForType(n);
    }

    /**
     * This method checks whether a given node is a name of an instance given in the model extraction state. If it
     * appears to be a name this is stored in the text extraction state.
     *
     * @param word the node to check
     */
    private void searchForName(IWord word) {
        if (posTagIsUndesired(word) && !wordStartsWithCapitalLetter(word)) {
            return;
        }
        var instanceNameIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstance(word, i));
        if (instanceNameIsSimilar) {
            textState.addName(word, probability);
        }
    }

    private boolean wordStartsWithCapitalLetter(IWord word) {
        return Character.isUpperCase(word.getText().charAt(0));
    }

    private boolean posTagIsUndesired(IWord word) {
        return !word.getPosTag().getTag().startsWith("NN");
    }

    /**
     * This method checks whether a given node is a type of an instance given in the model extraction state. If it
     * appears to be a type this is stored in the text extraction state. If multiple options are available the node
     * value is taken as reference.
     *
     * @param word the node to check
     */
    private void searchForType(IWord word) {
        var instanceTypeIsSimilar = modelState.getInstances().anySatisfy(i -> SimilarityUtils.isWordSimilarToModelInstanceType(word, i));
        if (instanceTypeIsSimilar) {
            textState.addType(word, probability);
        }
    }

}
