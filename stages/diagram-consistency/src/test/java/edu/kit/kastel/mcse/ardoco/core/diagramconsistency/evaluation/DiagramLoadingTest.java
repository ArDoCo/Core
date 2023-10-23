package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.JsonMapping;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1.Element;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1.ElementIdentification;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage1.Occurrence;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage2.ElementLinks;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage3.DiagramInconsistencies;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;

import static org.junit.jupiter.api.Assertions.*;

class DiagramLoadingTest extends EvaluationBase {
    private static <E extends Entity> Set<String> getAllIds(List<E> start, Function<E, List<E>> contentProvider,
            Function<E, String> idProvider) {
        Set<E> visited = new LinkedHashSet<>();
        Queue<E> queue = new java.util.ArrayDeque<>(start);

        Set<String> ids = new LinkedHashSet<>();

        while (!queue.isEmpty()) {
            E element = queue.poll();
            if (visited.contains(element)) {
                continue;
            }
            visited.add(element);
            queue.addAll(contentProvider.apply(element));

            ids.add(idProvider.apply(element));
        }

        return ids;
    }

    @DisplayName("Load diagrams")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void loadDiagram(DiagramProject project) throws IOException {
        String text = project.getDiagram();

        assertDoesNotThrow(() -> {
            var diagram = JsonMapping.OBJECT_MAPPER.readValue(text, Diagram.class);
            assertNotNull(diagram);
        });
    }

    @DisplayName("Load stage one")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void loadStageOne(DiagramProject project) throws IOException {
        String text = project.getIdentificationStage();

        AtomicReference<ElementIdentification> identification = new AtomicReference<>();

        assertDoesNotThrow(() -> {
            identification.set(JsonMapping.OBJECT_MAPPER.readValue(text, ElementIdentification.class));
            assertNotNull(identification.get());
        });

        Set<String> ids = new LinkedHashSet<>();
        ids.addAll(getAllIds(getArchitectureModel(project).getContent(), item -> {
            if (item instanceof ArchitectureComponent component) {
                List<ArchitectureItem> content = new ArrayList<>();
                content.addAll(component.getSubcomponents());
                content.addAll(component.getProvidedInterfaces());
                content.addAll(component.getRequiredInterfaces());
                return content;
            }
            return List.of();
        }, Entity::getId));
        ids.addAll(getAllIds(getCodeModel(project).getContent()
                .stream()
                .map(item -> (CodeItem) item)
                .toList(), CodeItem::getContent, Extractions::getPath));

        for (Element element : identification.get()
                .elements()) {
            for (Occurrence occurrence : element.occurrences()) {
                assertTrue(ids.contains(occurrence.modelElementId()), occurrence.modelElementId());
            }
        }
    }

    @DisplayName("Load stage two")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void loadStageTwo(DiagramProject project) throws IOException {
        String text = project.getLinkingStage();

        assertDoesNotThrow(() -> {
            var object = JsonMapping.OBJECT_MAPPER.readValue(text, ElementLinks.class);
            assertNotNull(object);
        });
    }

    @DisplayName("Load stage three")
    @ParameterizedTest(name = "{0}")
    @MethodSource("getDiagrams")
    void loadStageThree(DiagramProject project) throws IOException {
        String text = project.getValidationStage();

        assertDoesNotThrow(() -> {
            var object = JsonMapping.OBJECT_MAPPER.readValue(text, DiagramInconsistencies.class);
            assertNotNull(object);
        });
    }
}
