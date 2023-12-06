/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.JsonMapping;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.DiagramImpl;

/**
 * Loads a diagram from a file.
 * The file must follow the JSON format of the {@link Diagram} class.
 */
public class DiagramProviderInformant extends Informant {
    private final File diagramFile;

    /**
     * Creates a new DiagramProviderInformant.
     *
     * @param data
     *                    The DataRepository.
     * @param diagramFile
     *                    The file from which the diagram is loaded.
     */
    public DiagramProviderInformant(DataRepository data, File diagramFile) {
        super(DiagramProviderInformant.class.getSimpleName(), data);
        this.diagramFile = diagramFile;
    }

    /**
     * Loads a diagram from a file.
     *
     * @param file
     *             The file from which the diagram is loaded.
     * @return The loaded diagram.
     * @throws IOException
     *                     If the file could not be read.
     */
    public static Diagram load(File file) throws IOException {
        Diagram diagram;

        try (InputStream stream = new FileInputStream(file)) {
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            diagram = JsonMapping.OBJECT_MAPPER.readValue(text, DiagramImpl.class);
        }

        return diagram;
    }

    @Override
    public void run() {
        Diagram diagram = null;

        try {
            diagram = load(this.diagramFile);
        } catch (IOException e) {
            this.logger.error("Could not read file " + this.diagramFile, e);
        }

        if (diagram != null) {
            this.addDiagramToState(diagram);
        }
    }

    private void addDiagramToState(Diagram diagram) {
        DataRepository data = this.getDataRepository();
        Optional<DiagramState> optionalDiagramState = data.getData(DiagramState.ID, DiagramState.class);
        DiagramState state = optionalDiagramState.orElseGet(DiagramStateImpl::new);

        state.setDiagram(diagram);

        if (optionalDiagramState.isEmpty()) {
            data.addData(DiagramState.ID, state);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
