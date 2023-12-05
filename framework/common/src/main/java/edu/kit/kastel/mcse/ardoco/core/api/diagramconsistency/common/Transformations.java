package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
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
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
            Vertex<Box> vertex = new Vertex<>(box, DiagramUtility.getBoxText(box));

            graph.addVertex(vertex);
            boxToVertex.put(box, vertex);
        }

        SortedMap<String, Box> boxes = DiagramUtility.getBoxes(diagram);
        for (Vertex<Box> vertex : graph.vertexSet()) {
            Box represented = Objects.requireNonNull(vertex.getRepresented());

            for (Connector connector : DiagramUtility.getOutgoingConnectors(diagram, represented)) {
                for (Box target : DiagramUtility.getTargets(connector, boxes)) {
                    graph.addEdge(vertex, boxToVertex.get(target), new Edge(Edge.Label.DEFAULT));
                }
            }

            for (Box contained : DiagramUtility.getContainedBoxes(represented, boxes)) {
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
                    if (component.equals(provider)) {
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
                if (codePackage.equals(otherPackage)) {
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
}
