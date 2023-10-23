package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram.Line;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility to transform diagrams and models to graphs.
 */
@Deterministic public final class Transformations {
    private Transformations() {

    }

    /**
     * Transforms a diagram to a graph.
     *
     * @param diagram
     *         The diagram to transform.
     * @return The graph.
     */
    public static DirectedMultigraph<Vertex<Box>, Edge> toGraph(Diagram diagram) {
        DirectedMultigraph<Vertex<Box>, Edge> graph = new DirectedMultigraph<>(null, null, false);
        Map<Box, Vertex<Box>> boxToVertex = new LinkedHashMap<>();

        for (Box box : diagram.getBoxes()) {
            Vertex<Box> vertex = new Vertex<>(box, box.getText());

            graph.addVertex(vertex);
            boxToVertex.put(box, vertex);
        }

        for (Vertex<Box> vertex : graph.vertexSet()) {
            for (Line line : Objects.requireNonNull(vertex.getRepresented())
                    .getOutgoingLines()) {
                graph.addEdge(vertex, boxToVertex.get(line.target()), new Edge(Edge.Label.DEFAULT));
            }

            for (Box contained : Objects.requireNonNull(vertex.getRepresented())
                    .getContainedBoxes()) {
                graph.addEdge(boxToVertex.get(contained), vertex, new Edge(Edge.Label.HIERARCHY));
            }
        }

        return graph;
    }

    /**
     * Transforms an architecture model to a graph.
     *
     * @param model
     *         The architecture model to transform.
     * @return The graph.
     */
    public static DirectedMultigraph<Vertex<ArchitectureItem>, Edge> toGraph(ArchitectureModel model) {
        DirectedMultigraph<Vertex<ArchitectureItem>, Edge> graph = new DirectedMultigraph<>(null, null, false);

        transform(model, item -> {
                    Vertex<ArchitectureItem> vertex = new Vertex<>(item, item.getName());
                    graph.addVertex(vertex);
                    return vertex;
                }, (source, target) -> graph.addEdge(source, target, new Edge(Edge.Label.DEFAULT)),
                (child, parent) -> graph.addEdge(child, parent, new Edge(Edge.Label.HIERARCHY)));

        return graph;
    }

    /**
     * Transforms an architecture model to a graph.
     *
     * @param model
     *         The architecture model to transform.
     * @param vertexSupplier
     *         A function that creates a vertex from an architecture item.
     * @param edgeConsumer
     *         A function that adds a default edge to the graph.
     * @param hierarchyConsumer
     *         A function that adds a hierarchy edge to the graph.
     * @param <V>
     *         The type of the vertices.
     */
    public static <V> void transform(ArchitectureModel model, Function<ArchitectureItem, V> vertexSupplier,
            BiConsumer<V, V> edgeConsumer, BiConsumer<V, V> hierarchyConsumer) {
        Map<ArchitectureInterface, List<ArchitectureComponent>> providers = new LinkedHashMap<>();
        Map<ArchitectureComponent, V> components = new LinkedHashMap<>();

        for (ArchitectureItem item : model.getEndpoints()) {
            if (item instanceof ArchitectureComponent component) {
                V vertex = vertexSupplier.apply(component);
                components.put(component, vertex);

                for (ArchitectureInterface provided : component.getProvidedInterfaces()) {
                    providers.computeIfAbsent(provided, k -> new ArrayList<>())
                            .add(component);
                }
            }
        }

        for (var entry : components.entrySet()) {
            ArchitectureComponent component = entry.getKey();
            V componentVertex = entry.getValue();

            for (ArchitectureInterface required : component.getRequiredInterfaces()) {
                for (ArchitectureComponent provider : providers.getOrDefault(required, List.of())) {
                    if (component == provider) {
                        continue;
                    }

                    V providerVertex = components.get(provider);
                    edgeConsumer.accept(componentVertex, providerVertex);
                }
            }

            for (ArchitectureComponent subcomponent : component.getSubcomponents()) {
                V subcomponentVertex = components.get(subcomponent);
                hierarchyConsumer.accept(subcomponentVertex, componentVertex);
            }
        }
    }

    /**
     * Transforms a code model to a graph.
     *
     * @param model
     *         The code model to transform.
     * @return The graph.
     */
    public static DirectedMultigraph<Vertex<CodeItem>, Edge> toGraph(CodeModel model) {
        DirectedMultigraph<Vertex<CodeItem>, Edge> graph = new DirectedMultigraph<>(null, null, false);

        transform(model, item -> {
                    Vertex<CodeItem> vertex = new Vertex<>(item, item.getName());
                    graph.addVertex(vertex);
                    return vertex;
                }, (source, target) -> graph.addEdge(source, target, new Edge(Edge.Label.DEFAULT)),
                (child, parent) -> graph.addEdge(child, parent, new Edge(Edge.Label.HIERARCHY)));

        return graph;
    }

    /**
     * Transform a code model using provided functions.
     *
     * @param model
     *         The code model to transform.
     * @param vertexSupplier
     *         A function that creates a vertex from a code item.
     * @param edgeConsumer
     *         A function that adds a default edge to the graph.
     * @param hierarchyConsumer
     *         A function that adds a hierarchy edge to the graph.
     * @param <V>
     *         The type of the vertices.
     */
    public static <V> void transform(CodeModel model, Function<CodeItem, V> vertexSupplier,
            BiConsumer<V, V> edgeConsumer, BiConsumer<V, V> hierarchyConsumer) {
        Map<CodeItem, V> vertices = new LinkedHashMap<>();
        Graph<CodePackage, DefaultEdge> packages = new DirectedMultigraph<>(DefaultEdge.class);

        for (CodeItem item : model.getContent()) {
            if (item instanceof CodePackage codePackage) {
                packages.addVertex(codePackage);
                for (CodePackage subpackage : codePackage.getAllPackages()) {
                    packages.addVertex(subpackage);
                }
            }
        }

        Map<Datatype, CodePackage> types = new LinkedHashMap<>();

        for (CodePackage codePackage : packages.vertexSet()) {
            V packageVertex = vertexSupplier.apply(codePackage);
            vertices.put(codePackage, packageVertex);

            for (CodeCompilationUnit compilationUnit : codePackage.getCompilationUnits()) {
                for (Datatype datatype : compilationUnit.getAllDataTypes()) {
                    types.put(datatype, codePackage);

                    V datatypeVertex = vertexSupplier.apply(datatype);
                    vertices.put(datatype, datatypeVertex);
                }

                for (Datatype datatype : compilationUnit.getAllDataTypes()) {
                    Datatype parentDatatype = datatype.getParentDatatype();
                    V parentDatatypeVertex = vertices.get(parentDatatype);
                    V datatypeVertex = vertices.get(datatype);

                    if (parentDatatype != null) {
                        hierarchyConsumer.accept(datatypeVertex, parentDatatypeVertex);
                    } else {
                        hierarchyConsumer.accept(datatypeVertex, packageVertex);
                    }
                }
            }
        }

        for (Map.Entry<Datatype, CodePackage> entry : types.entrySet()) {
            Datatype datatype = entry.getKey();
            V datatypeVertex = vertices.get(datatype);
            CodePackage datatypePackage = entry.getValue();

            for (Datatype extended : datatype.getExtendedTypes()) {
                V extendedVertex = vertices.get(extended);
                edgeConsumer.accept(datatypeVertex, extendedVertex);
            }

            for (Datatype implemented : datatype.getImplementedTypes()) {
                V implementedVertex = vertices.get(implemented);
                edgeConsumer.accept(datatypeVertex, implementedVertex);
            }

            for (Datatype reference : datatype.getDatatypeReferences()) {
                V referenceVertex = vertices.get(reference);
                if (Objects.equals(datatype, reference) || referenceVertex == null) {
                    continue;
                }
                edgeConsumer.accept(datatypeVertex, referenceVertex);

                CodePackage referencePackage = types.get(reference);
                if (!Objects.equals(datatypePackage, referencePackage)) {
                    packages.addEdge(datatypePackage, referencePackage);
                }
            }
        }

        for (CodePackage codePackage : packages.vertexSet()) {
            for (CodePackage otherPackage : packages.vertexSet()) {
                if (codePackage == otherPackage) {
                    continue;
                }

                if (packages.containsEdge(codePackage, otherPackage)) {
                    edgeConsumer.accept(vertices.get(codePackage), vertices.get(otherPackage));
                }
            }
        }

        for (CodePackage codePackage : packages.vertexSet()) {
            if (codePackage.hasParent()) {
                V parentPackageVertex = vertices.get(codePackage.getParent());
                V packageVertex = vertices.get(codePackage);
                hierarchyConsumer.accept(packageVertex, parentPackageVertex);
            }
        }
    }

    /**
     * Transform any known model type using provided functions.
     *
     * @param model
     *         The model to transform.
     * @param vertexSupplier
     *         A function that creates a vertex from an entity.
     * @param edgeConsumer
     *         A function that adds a default edge to the graph.
     * @param hierarchyConsumer
     *         A function that adds a hierarchy edge to the graph.
     * @param <V>
     *         The type of the vertices.
     */
    public static <V> void transformAny(Model model, Function<Entity, V> vertexSupplier, BiConsumer<V, V> edgeConsumer,
            BiConsumer<V, V> hierarchyConsumer) {
        if (model instanceof ArchitectureModel architectureModel) {
            transform(architectureModel, vertexSupplier::apply, edgeConsumer, hierarchyConsumer);
        } else if (model instanceof CodeModel codeModel) {
            transform(codeModel, vertexSupplier::apply, edgeConsumer, hierarchyConsumer);
        } else {
            throw new IllegalArgumentException("Unknown model type: " + model.getClass());
        }
    }

    public static Diagram transform(edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram diagram) {
        Diagram newDiagram = new Diagram("", diagram.getLocation().getAbsolutePath());

        Map<String, Box> boxes = new LinkedHashMap<>();
        for (edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box box : diagram.getBoxes()) {
            String text = box.getTexts().stream().map(edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox::getText).collect(Collectors.joining(" "));

            Box newBox = newDiagram.addBox(text);
            boxes.put(box.getUUID(), newBox);
        }

        for (Connector connector : diagram.getConnectors()) {
            List<String> connected = connector.getConnectedBoxes();
            Box source = boxes.get(connected.get(0));

            for (int i = 1; i < connected.size(); i++) {
                Box target = boxes.get(connector.getConnectedBoxes().get(i));
                source.addLineTo(target);
            }
        }

        for (edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box box : diagram.getBoxes()) {
            edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box parent = null;
            int overlap = 0;

            for (edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box other : diagram.getBoxes()) {
                if (box == other) {
                    continue;
                }

                if (box.getBox()[0] >= other.getBox()[0] && box.getBox()[1] >= other.getBox()[1] && box.getBox()[2] <= other.getBox()[2] && box.getBox()[3] <= other.getBox()[3]) {
                    int newOverlap = (box.getBox()[2] - box.getBox()[0]) * (box.getBox()[3] - box.getBox()[1]);
                    if (newOverlap > overlap) {
                        parent = other;
                        overlap = newOverlap;
                    }
                }
            }

            if (parent != null) {
                Box newBox = boxes.get(box.getUUID());
                Box newParent = boxes.get(parent.getUUID());

                newParent.addContainedBox(newBox);
            }
        }

        return newDiagram;
    }
}
