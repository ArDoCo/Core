/* Licensed under MIT 2022-2023. */
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadSamTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * This record represents the result of running ArDoCo. It is backed by a {@link DataRepository} and grabs data from it. Besides accessing all data from the
 * calculation steps, this record also provides some convenience methods to directly access results such as found trace links and detected inconsistencies.
 */
public record ArDoCoResult(DataRepository dataRepository) {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoResult.class);

    /**
     * Returns the name of the project the results are based on.
     *
     * @return the name of the project the results are based on.
     */
    public String getProjectName() {
        return DataRepositoryHelper.getProjectPipelineData(dataRepository).getProjectName();
    }

    /**
     * Returns the set of {@link SadSamTraceLink}s that were found for the Model with the given ID.
     *
     * @param modelId the ID of the model that should be traced
     * @return Trace links for the model with the given id
     */
    public ImmutableSet<SadSamTraceLink> getTraceLinksForModel(String modelId) {
        ConnectionState connectionState = getConnectionState(modelId);
        if (connectionState != null) {
            return connectionState.getTraceLinks();
        }
        return Sets.immutable.empty();
    }

    /**
     * Returns the set of {@link SadSamTraceLink}s that were found for the Model with the given ID as strings in the format "ModelElementId,SentenceNo".
     *
     * @param modelId the ID of the model that should be traced
     * @return Trace links for the model with the given id as Strings
     */
    public ImmutableSet<String> getTraceLinksForModelAsStrings(String modelId) {
        var formatString = "%s,%d";
        return getTraceLinksForModel(modelId).collect(tl -> String.format(formatString, tl.getModelElementUid(), tl.getSentenceNumber() + 1));
    }

    /**
     * Returns the set of {@link SadSamTraceLink}s
     *
     * @return set of Trace links
     */
    public ImmutableList<SadSamTraceLink> getAllTraceLinks() {
        MutableSet<SadSamTraceLink> traceLinks = Sets.mutable.empty();

        for (var modelId : getModelIds()) {
            traceLinks.addAll(getTraceLinksForModel(modelId).castToCollection());
        }
        return traceLinks.toImmutableList();
    }

    /**
     * Returns the set of {@link SadSamTraceLink}s as strings. The strings are beautified to have a human-readable format
     *
     * @return Trace links as Strings
     */
    public List<String> getAllTraceLinksAsBeautifiedStrings() {
        return getAllTraceLinks().toSortedList(SadSamTraceLink::compareTo).collect(ArDoCoResult::formatTraceLinksHumanReadable);
    }

    private static String formatTraceLinksHumanReadable(SadSamTraceLink traceLink) {
        String modelElementName = traceLink.getInstanceLink().getModelInstance().getFullName();
        String modelElementUid = traceLink.getModelElementUid();
        String modelInfo = String.format("%s (%s)", modelElementName, modelElementUid);

        var sentence = traceLink.getSentence();
        int sentenceNumber = sentence.getSentenceNumberForOutput();
        String sentenceInfo = String.format("S%3d: \"%s\"", sentenceNumber, sentence.getText());

        return String.format("%-42s <--> %s", modelInfo, sentenceInfo);
    }

    /**
     * Return the list of {@link SamCodeTraceLink SamCodeTraceLinks}. If there are none, it will return an empty list.
     *
     * @return the list of {@link SamCodeTraceLink SamCodeTraceLinks}.
     */
    public List<SamCodeTraceLink> getSamCodeTraceLinks() {
        var samCodeTraceabilityState = getCodeTraceabilityState();
        if (samCodeTraceabilityState != null)
            return samCodeTraceabilityState.getSamCodeTraceLinks().toList();
        return List.of();
    }

    /**
     * Return the list of {@link TransitiveTraceLink TransitiveTraceLinks}. If there are none, it will return an empty list.
     *
     * @return the list of {@link TransitiveTraceLink TransitiveTraceLinks}.
     */
    public List<SadCodeTraceLink> getSadCodeTraceLinks() {
        var samCodeTraceabilityState = getCodeTraceabilityState();
        if (samCodeTraceabilityState != null)
            return samCodeTraceabilityState.getSadCodeTraceLinks().toList();
        return List.of();
    }

    /**
     * Returns all {@link Inconsistency inconsistencies} that were found for the model with the given ID.
     *
     * @param modelId the ID of the model
     * @return Inconsistencies for the model
     */
    public ImmutableList<Inconsistency> getAllInconsistenciesForModel(String modelId) {
        InconsistencyState inconsistencyState = getInconsistencyState(modelId);
        if (inconsistencyState != null) {
            return inconsistencyState.getInconsistencies();
        }
        return Lists.immutable.empty();
    }

    /**
     * Returns a list of {@link Inconsistency inconsistencies} that were found for the model with the given ID and that are of the given Inconsistency class.
     *
     * @param modelId           the ID of the model
     * @param inconsistencyType type of the Inconsistency that should be returned
     * @param <T>               Type-parameter of the inconsistency
     * @return Inconsistencies for the model with the given type
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
     * Returns the internal {@link ConnectionState} for the modelId with the given ID or null, if there is none.
     *
     * @param modelId the ID of the model
     * @return the connection state or null, if there is no {@link ConnectionState} for the given model ID
     */
    public ConnectionState getConnectionState(String modelId) {
        if (DataRepositoryHelper.hasConnectionStates(dataRepository)) {
            var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
            var modelState = getModelState(modelId);
            return connectionStates.getConnectionState(modelState.getMetamodel());
        }
        logger.warn("No ConnectionState found.");
        return null;
    }

    /**
     * Returns the internal {@link InconsistencyState} for the modelId with the given ID or null, if there is none.
     *
     * @param modelId the ID of the model
     * @return the inconsistency state or null, if there is no {@link InconsistencyState} for the given model ID
     */
    public InconsistencyState getInconsistencyState(String modelId) {
        if (DataRepositoryHelper.hasInconsistencyStates(dataRepository)) {
            var inconsistencyStates = DataRepositoryHelper.getInconsistencyStates(dataRepository);
            var modelState = getModelState(modelId);
            return inconsistencyStates.getInconsistencyState(modelState.getMetamodel());
        }
        logger.warn("No InconsistencyState found.");
        return null;
    }

    /**
     * Returns the internal {@link CodeTraceabilityState} or null, if there is none.
     *
     * @return the {@link CodeTraceabilityState} state or null, if there is no {@link CodeTraceabilityState} for the given model ID
     */
    public CodeTraceabilityState getCodeTraceabilityState() {
        if (DataRepositoryHelper.hasCodeTraceabilityState(dataRepository)) {
            return DataRepositoryHelper.getCodeTraceabilityState(dataRepository);
        }
        logger.warn("No SamCodeTraceabilityState found.");
        return null;
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
        return Lists.mutable.ofAll(modelStates.extractionModelIds());
    }

    /**
     * Returns the internal {@link ModelExtractionState} for the modelId with the given ID.
     *
     * @param modelId the ID of the model
     * @return the ModelExtractionState
     */
    public ModelExtractionState getModelState(String modelId) {
        ModelStates modelStates = getModelStates();
        return modelStates.getModelExtractionState(modelId);
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
     * Returns the internal {@link RecommendationState} for the given {@link Metamodel} or null, if there is none.
     *
     * @param metamodel the metamodel
     * @return the recommendation state or null, if there is none
     */
    public RecommendationState getRecommendationState(Metamodel metamodel) {
        if (DataRepositoryHelper.hasRecommendationStates(dataRepository)) {
            var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
            return recommendationStates.getRecommendationState(metamodel);
        }
        logger.warn("No RecommendationState found");
        return null;
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
