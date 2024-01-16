/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.DiagramImpl;

/**
 * A diagram annotated with links to a model and inconsistencies between the diagram and the model.
 *
 * @param diagram
 *                        The diagram.
 * @param links
 *                        The links between elements in the diagram and elements in the model.
 * @param inconsistencies
 *                        The inconsistencies between the diagram and the model.
 * @param <M>
 *                        The element type of the model.
 */
public record AnnotatedDiagram<M>(Diagram diagram, Map<Box, M> links, Set<Inconsistency<Box, M>> inconsistencies) {
    /**
     * Create an annotated diagram from a diagram.
     *
     * @param diagram
     *                The diagram.
     * @return The annotated diagram.
     */
    public static AnnotatedDiagram<Box> createFrom(Diagram diagram) {
        MutableBiMap<Box, Box> links = new HashBiMap<>();
        for (Box box : diagram.getBoxes()) {
            links.put(box, box);
        }

        return new AnnotatedDiagram<>(diagram, links, new LinkedHashSet<>());
    }

    /**
     * Creates a diagram from an architecture model.
     *
     * @param source The source of the diagram.
     * @param model  The architecture model.
     * @return The diagram.
     */
    public static AnnotatedDiagram<ArchitectureItem> createFrom(String source, ArchitectureModel model) {
        Diagram diagram = new DiagramImpl(source, new File(source));
        MutableBiMap<ArchitectureItem, Box> links = new HashBiMap<>();

        Transformations.transform(model, (item) -> {
            Box box = DiagramUtility.addBox(diagram, item.getName());
            links.put(item, box);
            return box;
        }, (from, to) -> DiagramUtility.addConnector(diagram, from, to), (child, parent) -> parent.addContainedBox(child));

        return new AnnotatedDiagram<>(diagram, links.inverse(), new LinkedHashSet<>());
    }

    /**
     * Creates a diagram from a code model.
     *
     * @param source The source of the diagram.
     * @param model  The code model.
     * @return The diagram.
     */
    public static AnnotatedDiagram<CodeItem> createFrom(String source, CodeModel model) {
        Diagram diagram = new DiagramImpl(source, new File(source));
        SortedMap<CodeItem, Box> links = new TreeMap<>();

        Transformations.transform(model, (item) -> {
            Box box = DiagramUtility.addBox(diagram, item.getName());
            links.put(item, box);
            return box;
        }, (from, to) -> DiagramUtility.addConnector(diagram, from, to), (child, parent) -> parent.addContainedBox(child));

        Map<Box, CodeItem> identityInverse = new IdentityHashMap<>();
        for (Map.Entry<CodeItem, Box> entry : links.entrySet()) {
            identityInverse.put(entry.getValue(), entry.getKey());
        }

        return new AnnotatedDiagram<>(diagram, identityInverse, new LinkedHashSet<>());
    }

    /**
     * Creates a diagram from a graph.
     *
     * @param <T>    The element type of the graph.
     * @param source The source of the diagram.
     * @param graph  The graph.
     * @return The diagram.
     */
    public static <T> AnnotatedDiagram<T> createFrom(String source, AnnotatedGraph<Box, T> graph) {
        Diagram diagram = new DiagramImpl(source, new File(source));
        Map<Vertex<Box>, Box> vertexToBox = new LinkedHashMap<>();

        for (Vertex<Box> vertex : graph.graph().vertexSet()) {
            Box box = DiagramUtility.addBox(diagram, vertex.getName());
            vertexToBox.put(vertex, box);
        }

        for (Vertex<Box> vertex : graph.graph().vertexSet()) {
            Box box = vertexToBox.get(vertex);

            for (Edge edge : graph.graph().outgoingEdgesOf(vertex)) {
                switch (edge.getLabel()) {
                case DEFAULT -> {
                    Box targetBox = vertexToBox.get(graph.graph().getEdgeTarget(edge));
                    DiagramUtility.addConnector(diagram, box, targetBox);
                }
                case HIERARCHY -> {
                    Box parentBox = vertexToBox.get(graph.graph().getEdgeTarget(edge));
                    parentBox.addContainedBox(box);
                }
                }
            }
        }

        MutableBiMap<Box, T> links = new HashBiMap<>();
        for (Map.Entry<Vertex<Box>, T> entry : graph.links().entrySet()) {
            links.put(vertexToBox.get(entry.getKey()), entry.getValue());
        }

        Set<Inconsistency<Box, T>> inconsistencies = new LinkedHashSet<>();
        for (Inconsistency<Vertex<Box>, T> inconsistency : graph.inconsistencies()) {
            inconsistencies.add(inconsistency.map(vertexToBox::get, Function.identity()));
        }

        return new AnnotatedDiagram<>(diagram, links, inconsistencies);
    }

    /**
     * Get all links but instead of references to the elements, their ids are used.
     *
     * @param idProvider
     *                   A function that provides the id of a model element.
     * @return The links.
     */
    public MutableBiMap<String, String> getIdBasedLinks(Function<M, String> idProvider) {
        MutableBiMap<String, String> idBasedLinks = new HashBiMap<>();
        for (Map.Entry<Box, M> entry : this.links.entrySet()) {
            idBasedLinks.put(entry.getKey().getUUID(), idProvider.apply(entry.getValue()));
        }
        return idBasedLinks;
    }
}
