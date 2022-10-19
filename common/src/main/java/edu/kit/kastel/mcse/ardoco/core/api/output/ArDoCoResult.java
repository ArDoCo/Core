/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

/**
 * This record represents the result of running ArDoCo. It is backed by a {@link DataRepository} and grabs data from it.
 * Besides accessing all data from the calculation steps, this record also provides some convenience methods to directly
 * access results such as found trace links and detected inconsistencies.
 * 
 */
public record ArDoCoResult(DataRepository dataRepository) {

    /**
     * Returns the name of the project the results are based on.
     * 
     * @return the name of the project the results are based on.
     */
    public String getProjectName() {
        return DataRepositoryHelper.getProjectPipelineData(dataRepository).getProjectName();
    }

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
    public ImmutableList<TraceLink> getAllTraceLinks() {
        MutableSet<TraceLink> traceLinks = Sets.mutable.empty();

        for (var modelId : getModelIds()) {
            traceLinks.addAll(getTraceLinksForModel(modelId).castToCollection());
        }
        return traceLinks.toImmutableList();
    }

    /**
     * Returns the set of {@link TraceLink}s as strings. The strings are beautified to have a human-readable format
     * 
     * @return Trace links as Strings
     */
    public List<String> getAllTraceLinksAsBeautifiedStrings() {
        return getAllTraceLinks().toSortedList(TraceLink::compareTo).collect(ArDoCoResult::formatTraceLinksHumanReadable);
    }

    private static String formatTraceLinksHumanReadable(TraceLink traceLink) {
        InstanceLink instanceLink = traceLink.getInstanceLink();
        String modelElementName = instanceLink.getModelInstance().getFullName();
        String modelElementUid = traceLink.getModelElementUid();
        String modelInfo = String.format("%s (%s)", modelElementName, modelElementUid);

        var sentence = traceLink.getSentence();
        int sentenceNumber = sentence.getSentenceNumberForOutput();
        String sentenceInfo = String.format("S%3d: \"%s\"", sentenceNumber, sentence.getText());

        return String.format("%-42s <--> %s", modelInfo, sentenceInfo);
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
     * Returns a list of all {@link Inconsistency inconsistencies} that were found.
     * 
     * @return all found inconsistencies
     */
    public ImmutableList<Inconsistency> getAllInconsistencies() {
        MutableList<Inconsistency> inconsistencies = Lists.mutable.empty();
        for (var model : getModelIds()) {
            inconsistencies.addAll(getAllInconsistenciesForModel(model).castToCollection());
        }
        return inconsistencies.toImmutable();
    }

    /**
     * Returns all {@link TextInconsistency TextInconsistencies} that were found.
     * 
     * @return all found TextInconsistencies
     */
    public ImmutableList<TextInconsistency> getAllTextInconsistencies() {
        var inconsistencies = getAllInconsistencies();
        return inconsistencies.select(i -> TextInconsistency.class.isAssignableFrom(i.getClass())).collect(TextInconsistency.class::cast);
    }

    /**
     * Returns all {@link ModelInconsistency ModelInconsistencies} that were found.
     *
     * @return all found ModelInconsistencies
     */
    public ImmutableList<ModelInconsistency> getAllModelInconsistencies() {
        var inconsistencies = getAllInconsistencies();
        return inconsistencies.select(i -> ModelInconsistency.class.isAssignableFrom(i.getClass())).collect(ModelInconsistency.class::cast);
    }

    /**
     * Returns a list of {@link InconsistentSentence InconsistentSentences}.
     * 
     * @return all InconsistentSentences
     */
    public ImmutableList<InconsistentSentence> getInconsistentSentences() {
        Map<Integer, InconsistentSentence> incSentenceMap = new HashMap<>();

        var inconsistencies = getAllTextInconsistencies();
        for (var inconsistency : inconsistencies) {
            int sentenceNo = inconsistency.getSentenceNumber();
            var incSentence = incSentenceMap.get(sentenceNo);
            if (incSentence != null) {
                incSentence.addInconsistency(inconsistency);
            } else {
                var sentence = getSentence(sentenceNo);
                incSentence = new InconsistentSentence(sentence, inconsistency);
                incSentenceMap.put(sentenceNo, incSentence);
            }
        }

        var sortedInconsistentSentences = Lists.mutable.withAll(incSentenceMap.values()).sortThisByInt(i -> i.sentence().getSentenceNumberForOutput());
        return sortedInconsistentSentences.toImmutable();
    }

    /**
     * Returns the {@link Sentence} with the given sentence number.
     * 
     * @param sentenceNo the sentence number
     * @return Sentence with the given number
     */
    public Sentence getSentence(int sentenceNo) {
        return getText().getSentences().detect(s -> s.getSentenceNumberForOutput() == sentenceNo);
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
     * Returns the internal {@link TextState}.
     *
     * @return the TextState
     */
    public TextState getTextState() {
        return DataRepositoryHelper.getTextState(dataRepository);
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

    public PreprocessingData getPreprocessingData() {
        return dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
    }

    /**
     * Returns the {@link Text}
     * 
     * @return the Text
     */
    public Text getText() {
        var preprocessingData = getPreprocessingData();
        return preprocessingData.getText();
    }
}
