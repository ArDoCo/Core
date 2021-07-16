package edu.kit.kastel.mcse.ardoco.core.connectiongenerator.agents_extractors;

import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.ConnectionExtractor;

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
     * For deserialization
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
    public void setProbability(List<Double> probabilities) {
        if (probabilities.size() > 1) {
            throw new IllegalArgumentException(getId() + ": The given probabilities are more than needed!");
        } else if (probabilities.isEmpty()) {
            throw new IllegalArgumentException(getId() + ": The given probabilities are empty!");
        } else {
            probability = probabilities.get(0);
        }
    }

    @Override
    public void exec(IWord n) {

        searchForName(n);
        searchForType(n);
    }

    /**
     * This method checks whether a given node is a name of an instance given in the model extraction state. If it
     * appears to be a name this is stored in the text extraction state. If multiple options are available the node
     * value is taken as reference.
     *
     * @param n the node to check
     */
    private void searchForName(IWord n) {
        List<IModelInstance> instances = modelState.getInstances()
                .stream()
                .filter(i -> SimilarityUtils.areWordsOfListsSimilar(i.getNames(), List.of(n.getText())))
                .collect(Collectors.toList());
        if (instances.size() == 1) {
            textState.addName(n, instances.get(0).getLongestName(), probability);

        } else if (instances.size() > 1) {
            textState.addName(n, n.getText(), probability);
        }
    }

    /**
     * This method checks whether a given node is a type of an instance given in the model extraction state. If it
     * appears to be a type this is stored in the text extraction state. If multiple options are available the node
     * value is taken as reference.
     *
     * @param n the node to check
     */
    private void searchForType(IWord n) {
        List<IModelInstance> instances = modelState.getInstances()
                .stream()
                .filter(i -> SimilarityUtils.areWordsOfListsSimilar(i.getTypes(), List.of(n.getText())))
                .collect(Collectors.toList());
        if (instances.size() == 1) {
            textState.addType(n, instances.get(0).getLongestType(), probability);

        } else if (instances.size() > 1) {
            textState.addType(n, n.getText(), probability);
        }
    }

}
