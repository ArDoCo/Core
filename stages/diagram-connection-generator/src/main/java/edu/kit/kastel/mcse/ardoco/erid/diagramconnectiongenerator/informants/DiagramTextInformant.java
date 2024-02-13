/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants;

import java.util.Locale;

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

/**
 * This informant searches for text in diagram elements that is an initialism of a recommended instances name and creates a
 * {@link edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi} between them.
 */
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
    public void process() {
        var dataRepository = getDataRepository();
        this.projectName = dataRepository.getData(ProjectPipelineData.ID, ProjectPipelineData.class).orElseThrow().getProjectName();
        var diagramState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        for (var mm : Metamodel.values()) {
            var recommendationState = recommendationStates.getRecommendationState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            createLinksBasedOnDiagramElements(diagramState, recommendationState, diagramConnectionState);
        }
    }

    /**
     * Uses {@link AbbreviationDisambiguationHelper#isInitialismOf(String, String, double)} to determine if the text in a
     * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement} {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox} is
     * an initialism of a recommended instances name. For example, consider a diagram element with the text box "SSM" and a recommended instance with "Secure
     * Storage Manager". The function identifies "SSM" as an initialism of the recommended instance name and thus creates a trace link between them.
     *
     * @param diagramState           the diagram recognition state
     * @param recommendationState    the recommendation state
     * @param diagramConnectionState the diagram connection state
     */
    private void createLinksBasedOnDiagramElements(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var boxes = diagram.getBoxes();
            for (var box : boxes) {
                var texts = box.getTexts();
                for (var tBox : texts) {
                    var ris = recommendationState.getRecommendedInstances();
                    for (var recommendedInstance : ris) {
                        if (AbbreviationDisambiguationHelper.isInitialismOf(recommendedInstance.getName().toLowerCase(Locale.ENGLISH), tBox.getText()
                                .toLowerCase(Locale.ENGLISH), initialismThreshold)) {
                            diagramConnectionState.addToLinksBetweenDeAndRi(recommendedInstance, box, projectName, this, DiagramUtil.calculateSimilarityMap(
                                    getMetaData().getWordSimUtils(), box, recommendedInstance));
                        }
                    }
                }
            }
        }
    }
}
