package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;

public class DiagramTextInformant extends Informant {
    /**
     * The percentage of characters in a word that need to be uppercase for a word to be considered an initialism candidate.
     */
    @Configurable
    private double initialismThreshold = 0.5;
    private String projectName;

    public DiagramTextInformant(DataRepository dataRepository) {
        super(DiagramTextInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        this.projectName = dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow().getProjectName();
        var diagramState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            createLinksBasedOnDiagramElements(diagramState, recommendationState, diagramConnectionState);
        }
    }

    private void createLinksBasedOnDiagramElements(@NotNull DiagramRecognitionState diagramState, @NotNull RecommendationState recommendationState,
            @NotNull DiagramConnectionState diagramConnectionState) {
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var boxes = diagram.getBoxes();
            for (var box : boxes) {
                var texts = box.getTexts();
                for (var tBox : texts) {
                    var ris = recommendationState.getRecommendedInstances();
                    for (var recommendedInstance : ris) {
                        if (AbbreviationDisambiguationHelper.isInitialismOf(recommendedInstance.getName().toLowerCase(Locale.US),
                                tBox.getText().toLowerCase(Locale.US), initialismThreshold)) {
                            diagramConnectionState.addToLinksBetweenDeAndRi(recommendedInstance, box, projectName, this,
                                    DiagramUtil.calculateHighestSimilarity(box, recommendedInstance));
                        }
                    }
                }
            }
        }
    }
}
