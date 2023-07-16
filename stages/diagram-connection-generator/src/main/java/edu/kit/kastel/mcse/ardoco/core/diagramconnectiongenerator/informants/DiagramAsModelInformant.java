package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.util.DiagramConnectionGeneratorUtil;
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class DiagramAsModelInformant extends Informant {
    @Configurable
    private int minSimilarSurfaceWords = 1;

    public DiagramAsModelInformant(DataRepository dataRepository) {
        super(DiagramAsModelInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        //Empty
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var diagramState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).get();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            findTextOfDiagramInstancesInSupposedMappings(diagramState, recommendationState, diagramConnectionState);
            createLinksForEqualOrSimilarRecommendedInstances(diagramState, recommendationState, diagramConnectionState);
        }
    }

    private void findTextOfDiagramInstancesInSupposedMappings(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var basedOnIncreasingMinimalProportionalThreshold = new ArrayList<DiagramLink>();
        var recommendedInstances = recommendationState.getRecommendedInstances();
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            for (Pair<Box, ModelInstance> pair : diagramModelInstances) {
                var mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(pair.second(), recommendedInstances);
                for (var recommendedInstance : mostLikelyRi) {
                    basedOnIncreasingMinimalProportionalThreshold.add(new DiagramLink(recommendedInstance, pair.first(), this,
                            WordSimUtils.getSimilarity(recommendedInstance.getName(), pair.second().getName()),
                            DiagramConnectionGeneratorUtil.calculateHighestSimilarity(pair.first(), recommendedInstance)));
                }
            }
        }
        logger.info("Found {} diagram links based on increasing minimal proportional threshold", basedOnIncreasingMinimalProportionalThreshold.size());
        basedOnIncreasingMinimalProportionalThreshold.forEach(diagramConnectionState::addToDiagramLinks);
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var basedOnOverallSimilarity = new ArrayList<DiagramLink>();
        var basedOnSurfaceWords = new ArrayList<DiagramLink>();
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            var ris = recommendationState.getRecommendedInstances();
            for (var recommendedInstance : ris) {
                for (Pair<Box, ModelInstance> pair : diagramModelInstances) {
                    //Add based on overall similarity
                    if (SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, pair.second())) {
                        basedOnOverallSimilarity.add(new DiagramLink(recommendedInstance, pair.first(), this,
                                WordSimUtils.getSimilarity(recommendedInstance.getName(), pair.second().getName()),
                                DiagramConnectionGeneratorUtil.calculateHighestSimilarity(pair.first(), recommendedInstance)));
                    }
                    //Add based on surface words
                    var similarSurfaceWords = SimilarityUtils.getSimilarSurfaceWords(recommendedInstance, pair.second());
                    if (similarSurfaceWords.size() >= minSimilarSurfaceWords) {
                        for (var similar : similarSurfaceWords) {
                            basedOnSurfaceWords.add(
                                    new DiagramLink(recommendedInstance, pair.first(), this, WordSimUtils.getSimilarity(similar, pair.second().getName()),
                                            DiagramConnectionGeneratorUtil.calculateHighestSimilarity(pair.first(), recommendedInstance)));
                        }
                    }
                }
            }
        }
        logger.info("Found {} diagram links based on overall similarity", basedOnOverallSimilarity.size());
        logger.info("Found {} diagram links based on surface words", basedOnSurfaceWords.size());
        basedOnOverallSimilarity.forEach(diagramConnectionState::addToDiagramLinks);
        basedOnSurfaceWords.forEach(diagramConnectionState::addToDiagramLinks);
    }

    private List<Pair<Box, ModelInstance>> diagramToModelInstances(Diagram diagram) {
        //Create model instances so we can reuse a lot of code
        var instances = new ArrayList<Pair<Box, ModelInstance>>();

        var boxes = diagram.getBoxes();
        for (Box box : boxes) {
            var names = DiagramConnectionGeneratorUtil.getPossibleNames(box);
            names.forEach(name -> instances.add(new Pair<>(box, new ModelInstanceImpl(name, "", Integer.toString(name.hashCode())))));
        }

        return instances;
    }
}
