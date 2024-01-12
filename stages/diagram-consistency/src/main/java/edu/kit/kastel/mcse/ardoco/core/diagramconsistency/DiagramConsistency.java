/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents.DiagramModelInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents.DiagramModelMatchingAgent;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents.DiagramModelSelectionAgent;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents.DiagramProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.ArDoCoRunner;
import edu.kit.kastel.mcse.ardoco.core.models.agents.ArCoTLModelProviderAgent;

/**
 * A runner that checks the consistency of a diagram and the represented architecture/code model.
 */
public class DiagramConsistency extends ArDoCoRunner {
    private static final ArchitectureModelType ARCHITECTURE_MODEL_TYPE = ArchitectureModelType.UML;
    private static final Set<ModelType> AVAILABLE_MODEL_TYPES = new LinkedHashSet<>(List.of(ARCHITECTURE_MODEL_TYPE, CodeModelType.CODE_MODEL));

    /**
     * Creates a new runner.
     *
     * @param projectName
     *                    The name of the project that is analysed.
     */
    public DiagramConsistency(String projectName) {
        super(projectName);
    }

    /**
     * Accesses the data repository.
     *
     * @return The data repository.
     */
    public DataRepository getDataRepository() {
        return this.getArDoCo().getDataRepository();
    }

    /**
     * Sets up the pipeline.
     *
     * @param inputArchitectureModel
     *                               The architecture model file, as defined by ArCoTL.
     * @param inputCodeModel
     *                               The code model file, as defined by ArCoTL.
     * @param inputDiagram
     *                               The diagram, following the format defined by {@link Diagram}.
     * @param outputDirectory
     *                               The output directory.
     * @param config
     *                               The configuration.
     */
    public void setUp(File inputArchitectureModel, File inputCodeModel, File inputDiagram, File outputDirectory, SortedMap<String, String> config) {
        this.definePipeline(inputArchitectureModel, inputCodeModel, inputDiagram);
        this.setOutputDirectory(outputDirectory);
        this.getArDoCo().applyConfiguration(config);
        this.isSetUp = true;
    }

    private void definePipeline(File inputArchitectureModel, File inputCodeModel, File inputDiagram) {
        ArDoCo arDoCo = this.getArDoCo();
        DataRepository dataRepository = arDoCo.getDataRepository();

        arDoCo.addPipelineStep(ArCoTLModelProviderAgent.get(inputArchitectureModel, ARCHITECTURE_MODEL_TYPE, inputCodeModel, new TreeMap<>(), dataRepository));

        arDoCo.addPipelineStep(DiagramProviderAgent.get(inputDiagram, dataRepository));

        arDoCo.addPipelineStep(DiagramModelSelectionAgent.get(AVAILABLE_MODEL_TYPES, dataRepository));
        arDoCo.addPipelineStep(DiagramModelMatchingAgent.get(dataRepository));
        arDoCo.addPipelineStep(DiagramModelInconsistencyAgent.get(dataRepository));
    }
}
