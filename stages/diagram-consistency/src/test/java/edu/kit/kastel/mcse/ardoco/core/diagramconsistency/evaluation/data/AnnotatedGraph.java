/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.jgrapht.graph.DirectedMultigraph;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Edge;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Label;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Vertex;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;

/**
 * A graph with additional information about inconsistencies between the graph and the represented model.
 *
 * @param graph
 *                        The graph.
 * @param links
 *                        The links between the graph and the represented model.
 * @param inconsistencies
 *                        The inconsistencies between the graph and the represented model.
 * @param <R>
 *                        The element type in the representation. Could be Box or the same as M.
 * @param <M>
 *                        The element type of the model.
 */
public record AnnotatedGraph<R, M>(DirectedMultigraph<Vertex<R>, Edge> graph, MutableBiMap<Vertex<R>, M> links,
                                   Set<Inconsistency<Vertex<R>, M>> inconsistencies) {
    /**
     * Creates a graph from an architecture model.
     *
     * @param model
     *              The architecture model.
     * @return The graph.
     */
    public static AnnotatedGraph<ArchitectureItem, ArchitectureItem> createFrom(ArchitectureModel model) {
        DirectedMultigraph<Vertex<ArchitectureItem>, Edge> graph = new DirectedMultigraph<>(null, null, false);
        MutableBiMap<ArchitectureItem, Vertex<ArchitectureItem>> links = new HashBiMap<>();

        Transformations.transform(model, (item) -> {
            Vertex<ArchitectureItem> vertex = new Vertex<>(item, item.getName());
            graph.addVertex(vertex);
            links.put(item, vertex);
            return vertex;
        }, (source, target) -> graph.addEdge(source, target, new Edge(Label.DEFAULT)), (child, parent) -> graph.addEdge(child, parent, new Edge(
                Label.HIERARCHY)));

        return new AnnotatedGraph<>(graph, links.inverse(), new LinkedHashSet<>());
    }

    /**
     * Creates a graph from a code model.
     *
     * @param model
     *              The code model.
     * @return The graph.
     */
    public static AnnotatedGraph<CodeItem, CodeItem> createFrom(CodeModel model) {
        DirectedMultigraph<Vertex<CodeItem>, Edge> graph = new DirectedMultigraph<>(null, null, false);
        MutableBiMap<CodeItem, Vertex<CodeItem>> links = new HashBiMap<>();

        Transformations.transform(model, (item) -> {
            Vertex<CodeItem> vertex = new Vertex<>(item, item.getName());
            graph.addVertex(vertex);
            links.put(item, vertex);
            return vertex;
        }, (source, target) -> graph.addEdge(source, target, new Edge(Label.DEFAULT)), (child, parent) -> graph.addEdge(child, parent, new Edge(
                Label.HIERARCHY)));

        return new AnnotatedGraph<>(graph, links.inverse(), new LinkedHashSet<>());
    }

    /**
     * Create an annotated graph from an annotated diagram.
     *
     * @param diagram
     *                The annotated diagram.
     * @param <T>
     *                The element type of the model that is represented by the diagram.
     * @return The annotated graph.
     */
    public static <T> AnnotatedGraph<Box, T> createFrom(AnnotatedDiagram<T> diagram) {
        DirectedMultigraph<Vertex<Box>, Edge> graph = Transformations.toGraph(diagram.diagram());

        Map<Box, Vertex<Box>> boxToVertex = new LinkedHashMap<>();
        for (Vertex<Box> vertex : graph.vertexSet()) {
            boxToVertex.put(vertex.getRepresented(), vertex);
        }

        MutableBiMap<Vertex<Box>, T> links = new HashBiMap<>();
        for (Map.Entry<Box, T> entry : diagram.links().entrySet()) {
            links.put(boxToVertex.get(entry.getKey()), entry.getValue());
        }

        Set<Inconsistency<Vertex<Box>, T>> inconsistencies = new LinkedHashSet<>();
        for (Inconsistency<Box, T> inconsistency : diagram.inconsistencies()) {
            inconsistencies.add(inconsistency.map(boxToVertex::get, Function.identity()));
        }

        return new AnnotatedGraph<>(graph, links, inconsistencies);
    }

    /**
     * Adds a new inconsistency to the graph.
     *
     * @param inconsistency
     *                      The inconsistency.
     */
    public void addInconsistency(Inconsistency<Vertex<R>, M> inconsistency) {
        this.inconsistencies.add(inconsistency);
    }

    /**
     * Perform a deep copy of the graph.
     *
     * @return The copy.
     */
    public AnnotatedGraph<R, M> copy() {
        DirectedMultigraph<Vertex<R>, Edge> graph = new DirectedMultigraph<>(null, null, false);
        Map<Vertex<R>, Vertex<R>> oldToNew = new LinkedHashMap<>();

        for (Vertex<R> vertex : this.graph.vertexSet()) {
            Vertex<R> copy = vertex.copy();
            graph.addVertex(copy);
            oldToNew.put(vertex, copy);
        }

        for (Edge edge : this.graph.edgeSet()) {
            Vertex<R> source = oldToNew.get(this.graph.getEdgeSource(edge));
            Vertex<R> target = oldToNew.get(this.graph.getEdgeTarget(edge));
            graph.addEdge(source, target, edge.copy());
        }

        MutableBiMap<Vertex<R>, M> links = new HashBiMap<>();
        for (Map.Entry<Vertex<R>, M> entry : this.links.entrySet()) {
            links.put(oldToNew.get(entry.getKey()), entry.getValue());
        }

        Set<Inconsistency<Vertex<R>, M>> inconsistencies = new LinkedHashSet<>();
        for (Inconsistency<Vertex<R>, M> inconsistency : this.inconsistencies) {
            inconsistencies.add(inconsistency.map(oldToNew::get, Function.identity()));
        }

        return new AnnotatedGraph<>(graph, links, inconsistencies);
    }
}
