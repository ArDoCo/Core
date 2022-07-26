/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This record represents the result of running ArDoCo. It is backed by a {@link DataRepository} and grabs data from it.
 * Besides accessing all data from the calculation steps, this record also provides some convenience methods to directly
 * access results such as found trace links and detected inconsistencies.
 * 
 * @param dataRepository the repository that backs the results
 */
public record ArDoCoResult(DataRepository dataRepository) {

    /**
     * Returns the set of {@link TraceLink}s that were found for the Model with the given ID.
     * 
     * @param modelId the ID of the model that should be traced
     * @return Trace links for the model with the given id
     */
    public ImmutableSet<TraceLink> getTraceLinksForModel(String modelId) {
        return getConnectionState(modelId).getTraceLinks();
    }

    /**
     * Returns the set of {@link TraceLink}s that were found for the Model with the given ID as strings in the format
     * "ModelElementId,SentenceNo".
     * 
     * @param modelId the ID of the model that should be traced
     * @return Trace links for the model with the given id as Strings
     */
    public ImmutableSet<String> getTraceLinksForModelAsStrings(String modelId) {
        var formatString = "%s,%d";
        return getTraceLinksForModel(modelId).collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1));
    }

    /**
     * Returns the set of {@link TraceLink}s
     * 
     * @return set of Trace links
     */
    public ImmutableSet<TraceLink> getAllTraceLinks() {
        MutableSet<TraceLink> traceLinks = Sets.mutable.empty();

        for (var modelId : getModelIds()) {
            traceLinks.addAll(getTraceLinksForModel(modelId).castToCollection());
        }
        return traceLinks.toImmutableSet();
    }

    /**
     * Returns the set of {@link TraceLink}s as strings in the format "ModelElementId,SentenceNo".
     * 
     * @return Trace links as Strings
     */
    public ImmutableSet<String> getAllTraceLinksAsStrings() {
        var formatString = "%s,%d";
        return getAllTraceLinks().collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1));
    }

    /**
     * Returns all {@link Inconsistency inconsistencies} that were found for the model with the given ID.
     * 
     * @param modelId the ID of the model
     * @return Inconsistencies for the model
     */
    public ImmutableList<Inconsistency> getAllInconsistenciesForModel(String modelId) {
        return getInconsistencyState(modelId).getInconsistencies();

    }

    /**
     * Returns a list of {@link Inconsistency inconsistencies} that were found for the model with the given ID and that
     * are of the given Inconsistency class.
     * 
     * @param modelId           the ID of the model
     * @param inconsistencyType type of the Inconsistency that should be returned
     * @return Inconsistencies for the model with the given type
     * @param <T> Type-parameter of the inconsistency
     */
    public <T extends Inconsistency> ImmutableList<T> getInconsistenciesOfTypeForModel(String modelId, Class<T> inconsistencyType) {
        return getAllInconsistenciesForModel(modelId).select(i -> inconsistencyType.isAssignableFrom(i.getClass())).collect(inconsistencyType::cast);
    }

    /**
     * Returns the internal {@link ConnectionState} for the modelId with the given ID.
     * 
     * @param modelId the ID of the model
     * @return the connection state
     */
    public ConnectionState getConnectionState(String modelId) {
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        var modelState = getModelState(modelId);
        return connectionStates.getConnectionState(modelState.getMetamodel());
    }

    /**
     * Returns the internal {@link InconsistencyState} for the modelId with the given ID.
     * 
     * @param modelId the ID of the model
     * @return the inconsistency state
     */
    public InconsistencyState getInconsistencyState(String modelId) {
        var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
        var modelState = getModelState(modelId);
        return inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
    }

    /**
     * Returns the internal {@link ModelStates}
     *
     * @return the ModelStates
     */
    private ModelStates getModelStates() {
        return DataRepositoryHelper.getModelStatesData(dataRepository);
    }

    /**
     * Returns a list of all IDs for all the models that were loaded in.
     * 
     * @return list of all model IDs
     */
    public List<String> getModelIds() {
        ModelStates modelStates = getModelStates();
        return Lists.mutable.ofAll(modelStates.modelIds());
    }

    /**
     * Returns the internal {@link ModelExtractionState} for the modelId with the given ID.
     *
     * @param modelId the ID of the model
     * @return the ModelExtractionState
     */
    public ModelExtractionState getModelState(String modelId) {
        ModelStates modelStates = getModelStates();
        return modelStates.getModelState(modelId);
    }

    /**
     * Returns the internal {@link RecommendationState} for the given {@link Metamodel}.
     *
     * @param metamodel the metamodel
     * @return the recommendation state
     */
    public RecommendationState getRecommendationState(Metamodel metamodel) {
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        return recommendationStates.getRecommendationState(metamodel);
    }

    /**
     * Returns the {@link Text}
     * 
     * @return the Text
     */
    public Text getText() {
        var preprocessingData = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
        return preprocessingData.getText();
    }
}
