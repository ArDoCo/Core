/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstanceImpl;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi;

/**
 * This informant creates temporary {@link ModelInstance ModelInstances} from each diagram element and uses ArDoCo's SAD SAM TLR approach, but instead of
 * creating a SAD SAM trace link for a temporary model instance and recommended instance, a {@link LinkBetweenDeAndRi} is created for the source of the
 * temporary model instance. The temporary model instances are created using the
 * {@link edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramReferenceAgent diagram element references}.
 */
public class DiagramAsModelInformant extends Informant {
    @Configurable
    private int minSimilarSurfaceWords = 1;
    private String projectName;

    public DiagramAsModelInformant(DataRepository dataRepository) {
        super(DiagramAsModelInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = getDataRepository();
        this.projectName = dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow().getProjectName();
        var diagramState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        for (var mm : Metamodel.values()) {
            var recommendationState = recommendationStates.getRecommendationState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            findTextOfDiagramInstancesInSupposedMappings(diagramState, recommendationState, diagramConnectionState);
            createLinksForEqualOrSimilarRecommendedInstances(diagramState, recommendationState, diagramConnectionState);
        }
    }

    private void findTextOfDiagramInstancesInSupposedMappings(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var basedOnIncreasingMinimalProportionalThreshold = new ArrayList<LinkBetweenDeAndRi>();
        var recommendedInstances = recommendationState.getRecommendedInstances();
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            for (Pair<Box, ModelInstance> pair : diagramModelInstances) {
                var mostLikelyRi = getMetaData().getSimilarityUtils().getMostRecommendedInstancesToInstanceByReferences(pair.second(), recommendedInstances);
                for (var recommendedInstance : mostLikelyRi) {
                    basedOnIncreasingMinimalProportionalThreshold.add(new LinkBetweenDeAndRi(recommendedInstance, pair.first(), projectName, this, DiagramUtil
                            .calculateSimilarityMap(getMetaData().getWordSimUtils(), pair.first(), recommendedInstance)));
                }
            }
        }
        logger.debug("Found {} diagram links based on increasing minimal proportional threshold", basedOnIncreasingMinimalProportionalThreshold.size());
        basedOnIncreasingMinimalProportionalThreshold.forEach(diagramConnectionState::addToLinksBetweenDeAndRi);
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var basedOnOverallSimilarity = new ArrayList<LinkBetweenDeAndRi>();
        var basedOnSurfaceWords = new ArrayList<LinkBetweenDeAndRi>();
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            var ris = recommendationState.getRecommendedInstances();
            for (var recommendedInstance : ris) {
                for (Pair<Box, ModelInstance> pair : diagramModelInstances) {
                    //Add based on overall similarity
                    if (getMetaData().getSimilarityUtils().isRecommendedInstanceSimilarToModelInstance(recommendedInstance, pair.second())) {
                        basedOnOverallSimilarity.add(new LinkBetweenDeAndRi(recommendedInstance, pair.first(), projectName, this, DiagramUtil
                                .calculateSimilarityMap(getMetaData().getWordSimUtils(), pair.first(), recommendedInstance)));
                    }
                    //Add based on surface words
                    var similarSurfaceWords = getMetaData().getSimilarityUtils().getSimilarSurfaceWords(recommendedInstance, pair.second());
                    if (similarSurfaceWords.size() >= minSimilarSurfaceWords) {
                        basedOnSurfaceWords.add(new LinkBetweenDeAndRi(recommendedInstance, pair.first(), projectName, this, DiagramUtil.calculateSimilarityMap(
                                getMetaData().getWordSimUtils(), pair.first(), recommendedInstance)));
                    }
                }
            }
        }
        logger.debug("Found {} diagram links based on overall similarity", basedOnOverallSimilarity.size());
        logger.debug("Found {} diagram links based on surface words", basedOnSurfaceWords.size());
        basedOnOverallSimilarity.forEach(diagramConnectionState::addToLinksBetweenDeAndRi);
        basedOnSurfaceWords.forEach(diagramConnectionState::addToLinksBetweenDeAndRi);
    }

    private List<Pair<Box, ModelInstance>> diagramToModelInstances(Diagram diagram) {
        //Create model instances so we can reuse a lot of code
        var instances = new ArrayList<Pair<Box, ModelInstance>>();

        var boxes = diagram.getBoxes();
        for (Box box : boxes) {
            var names = box.getReferences();

            names.forEach(name -> instances.add(new Pair<>(box, new ModelInstanceImpl(name, "", Integer.toString(name.hashCode())))));
        }

        return instances;
    }
}
