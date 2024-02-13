/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.SortedMap;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
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
            var oom = JsonHandling.createObjectMapper();
            oom.setInjectableValues(new InjectableValues() {
                @Override
                public Object findInjectableValue(Object o, DeserializationContext deserializationContext, BeanProperty beanProperty, Object o1)
                        throws JsonMappingException {
                    if (beanProperty.getType().getRawClass() != Diagram.class)
                        throw new JsonMappingException(deserializationContext.getParser(), "Could not inject value into " + beanProperty.getName());
                    Object parent = deserializationContext.getParser().getParsingContext().getParent().getCurrentValue();
                    if (!(parent instanceof DiagramImpl parentDiagram))
                        throw new JsonMappingException(deserializationContext.getParser(), "Could not inject value into " + beanProperty.getName());
                    return parentDiagram;
                }
            });

            diagram = oom.readValue(text, DiagramImpl.class);
        }

        return diagram;
    }

    @Override
    public void process() {
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
