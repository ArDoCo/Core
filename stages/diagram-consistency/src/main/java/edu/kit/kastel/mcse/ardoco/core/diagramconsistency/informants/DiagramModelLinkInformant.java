/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.jgrapht.graph.DirectedMultigraph;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelLinkState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.WeightedTextSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.FixpointFormula;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.OrderedMatchingFilter;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.PropagationCoefficientFormula;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.SimilarityFloodingAlgorithm;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.similarityflooding.SimilarityMapping;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Searches for links between the model and the selected diagram.
 */
public class DiagramModelLinkInformant extends Informant {
    /**
     * The default epsilon stop threshold for the similarity flooding algorithm.
     */
    /*package-private*/ static final double DEFAULT_EPSILON = 1.0;
    /**
     * The default maximum number of iterations for the similarity flooding algorithm.
     */
    /*package-private*/ static final int DEFAULT_MAX_ITERATIONS = 100;
    /**
     * The default threshold for the levenshtein similarity, under which the similarity is considered to be zero.
     */
    /*package-private*/ static final double DEFAULT_TEXT_SIMILARITY_THRESHOLD = 0.68;
    /**
     * The default threshold for the similarity, under which no link is created.
     */
    /*package-private*/ static final double DEFAULT_SIMILARITY_THRESHOLD = 0.06;
    @Configurable
    private double epsilon = DEFAULT_EPSILON;
    @Configurable
    private int maxIterations = DEFAULT_MAX_ITERATIONS;
    @Configurable
    private double textSimilarityThreshold = DEFAULT_TEXT_SIMILARITY_THRESHOLD;
    @Configurable
    private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    @Configurable
    private boolean skip = false;

    /**
     * Creates a new DiagramModelLinkInformant.
     *
     * @param data
     *             The DataRepository.
     */
    public DiagramModelLinkInformant(DataRepository data) {
        super(DiagramModelLinkInformant.class.getSimpleName(), data);
    }

    @Override
    public void process() {
        if (this.skip) {
            return;
        }

        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramState diagram = data.getData(DiagramState.ID, DiagramStateImpl.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElse(null);
        DiagramModelLinkState matching = data.getData(DiagramModelLinkState.ID, DiagramModelLinkState.class).orElse(null);

        if (models == null || diagram == null || selection == null || matching == null) {
            this.logger.error("DiagramModelLinkInformant: Could not find all required data.");
            return;
        }

        for (var selectedModelType : selection.getSelection()) {
            Model model = models.getModel(selectedModelType.getModelId());

            if (model instanceof ArchitectureModel architectureModel) {
                this.match(diagram.getDiagram(), architectureModel, selectedModelType, matching, selection.getSimilarityFunction());
            } else if (model instanceof CodeModel codeModel) {
                this.match(diagram.getDiagram(), codeModel, selectedModelType, matching, selection.getSimilarityFunction());
            } else {
                this.logger.error("DiagramModelLinkInformant: Unknown model type: {}", model.getClass().getSimpleName());
            }
        }
    }

    private void match(Diagram diagram, ArchitectureModel model, ModelType type, DiagramModelLinkState matching, WeightedTextSimilarity similarityFunction) {
        DirectedMultigraph<Vertex<Box>, Edge> diagramAsGraph = Transformations.toGraph(diagram);
        DirectedMultigraph<Vertex<ArchitectureItem>, Edge> modelAsGraph = Transformations.toGraph(model);

        this.match(type, matching, diagramAsGraph, modelAsGraph, ArchitectureItem::getId, similarityFunction);
    }

    private void match(Diagram diagram, CodeModel model, ModelType type, DiagramModelLinkState matching, WeightedTextSimilarity similarityFunction) {
        DirectedMultigraph<Vertex<Box>, Edge> diagramAsGraph = Transformations.toGraph(diagram);
        DirectedMultigraph<Vertex<CodeItem>, Edge> modelAsGraph = Transformations.toGraph(model);

        this.match(type, matching, diagramAsGraph, modelAsGraph, Extractions::getPath, similarityFunction);
    }

    private <M> void match(ModelType modelType, DiagramModelLinkState matching, DirectedMultigraph<Vertex<Box>, Edge> diagramAsGraph,
            DirectedMultigraph<Vertex<M>, Edge> modelAsGraph, Function<M, String> idExtractor, WeightedTextSimilarity similarityFunction) {
        SimilarityFloodingAlgorithm<Vertex<Box>, Vertex<M>, Label> algorithm = new SimilarityFloodingAlgorithm<>(this.epsilon, this.maxIterations,
                PropagationCoefficientFormula.getInverseAverageFormula(), FixpointFormula.getCFormula());

        SimilarityMapping<Vertex<Box>, Vertex<M>> initialSimilarity = new SimilarityMapping<>(pair -> {
            double similarity = similarityFunction.apply(pair.getFirst().getName(), pair.getSecond().getName());
            return similarity < this.textSimilarityThreshold ? 0.0 : similarity;
        });
        initialSimilarity.prepareCartesian(diagramAsGraph.vertexSet(), modelAsGraph.vertexSet());

        SimilarityMapping<Vertex<Box>, Vertex<M>> mapping = algorithm.match(diagramAsGraph, modelAsGraph, initialSimilarity);

        OrderedMatchingFilter<Vertex<Box>, Vertex<M>> filter = new OrderedMatchingFilter<>(this.similarityThreshold, this.textSimilarityThreshold);
        MutableBiMap<Vertex<Box>, Vertex<M>> filteredMapping = filter.filter(mapping, initialSimilarity);
        for (Map.Entry<Vertex<Box>, Vertex<M>> entry : filteredMapping.entrySet()) {
            Box box = entry.getKey().getRepresented();
            M item = entry.getValue().getRepresented();

            if (box != null && item != null) {
                matching.addLink(modelType, box.getUUID(), idExtractor.apply(item));
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
