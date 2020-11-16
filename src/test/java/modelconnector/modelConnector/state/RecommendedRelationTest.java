package modelconnector.modelConnector.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class tests the recommended relation.
 *
 * @author Sophie
 *
 */
public class RecommendedRelationTest {

	private static String type = "component";
	private static String name = "test.driver";
	private static String name2 = "scndName";
	private static String type2 = "scndType";
	private static RecommendedInstance ri0;
	private static RecommendedInstance ri1;
	private static RecommendedInstance ri2;
	private static List<INode> relationNodes = new ArrayList<>();

	/**
	 * Initializes the test class by creating recommended instances.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {

		String content = "" + //
				"The architecture contains ui component, Logic component, storage component, common component, test driver component,"//
				+ " e2e component, client component. The common component is in style of a beautiful component architecture. "//
				+ "The test.driver is a very important component. The logic.api, test.driver, and test.epic components are important, too.";

		IGraph graph = UtilsForTesting.getGraph(content);

		List<NounMapping> typeMappings = new ArrayList<>();
		List<NounMapping> nameMappings = new ArrayList<>();

		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			String nodeValue = GraphUtils.getNodeValue(n);
			if (nodeValue.contentEquals(type)) {
				typeMappings.add(NounMapping.createTypeMapping(n, 0.5, type, List.of(type)));

			} else if (nodeValue.contentEquals(name)) {
				nameMappings.add(NounMapping.createNameMapping(n, 0.5, name, List.of(name)));
			}

			if (SimilarityUtils.containsSeparator(nodeValue)) {
				relationNodes.add(n);
			}
		}

		ri0 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		ri1 = new RecommendedInstance(name2, type, 0.5, nameMappings, typeMappings);
		ri2 = new RecommendedInstance(name, type2, 0.5, nameMappings, typeMappings);

		assertNotEquals(ri0, ri1);
		assertNotEquals(ri0, ri2);
		assertNotEquals(ri1, ri2);

	}

	/**
	 * Tests the creation of a recommended relation. It is successfully created if
	 * it's not null. Recommended relation equality depends on the relation
	 * type/name, and the encapsulated recommended instances.
	 */
	@Test
	public void creation() {
		RecommendedRelation rrl0 = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, relationNodes);
		assertNotNull(rrl0);

		RecommendedRelation rrl1 = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, relationNodes);
		assertEquals(rrl0, rrl1);

		RecommendedRelation rrl2 = new RecommendedRelation("provide", ri0, ri1, List.of(ri2), 0.5, relationNodes);
		assertNotEquals(rrl0, rrl2);

		RecommendedRelation rrl3 = new RecommendedRelation("provide", ri0, ri1, List.of(), 0.5, relationNodes);
		assertNotEquals(rrl0, rrl3);

	}

	/**
	 * Tests the getter of a recommended relation. The getter should return the
	 * name, nodes, and probability of the recommended relation. The getter for the
	 * relation instances should return all encapsulated recommended instances.
	 */
	@Test
	public void getter() {

		RecommendedRelation rrl = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, relationNodes);

		assertEquals(rrl.getName(), "in");
		assertTrue(rrl.getNodes().containsAll(relationNodes));
		assertTrue(rrl.getNodes().size() == relationNodes.size());
		assertTrue(rrl.getProbability() == 0.5);

		Set<RecommendedInstance> rrlris = new HashSet<>();
		rrlris.addAll(rrl.getRelationInstances());

		Set<RecommendedInstance> relationInstances = new HashSet<>();
		relationInstances.addAll(List.of(ri0, ri1, ri2));

		assertEquals(rrlris, relationInstances);

	}

	/**
	 * Tests the add occurrences functionality of a recommended relation.
	 * Occurrences should be added, if they aren't already contained.
	 */
	@Test
	public void addOccurrences() {
		RecommendedRelation rrl = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, List.of());

		rrl.addOccurrences(relationNodes);

		assertTrue(rrl.getNodes().containsAll(relationNodes));
		assertTrue(rrl.getNodes().size() == relationNodes.size());

		rrl.addOccurrences(relationNodes);

		assertTrue(rrl.getNodes().containsAll(relationNodes));
		assertTrue(rrl.getNodes().size() == relationNodes.size());
	}

	/**
	 * Tests the update of the probability. If the probability is updated it should
	 * change.
	 */
	@Test
	public void updateProbability() {
		RecommendedRelation rrl = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, List.of());

		rrl.updateProbability(0.9);
		assertTrue(rrl.getProbability() != 0.5);
	}

	/**
	 * Tests the setter of a recommended relation. If the probability is set to a
	 * given value it should be set to that value.
	 */
	@Test
	public void setter() {
		RecommendedRelation rrl = new RecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, List.of());

		rrl.setProbability(0.9);
		assertTrue(rrl.getProbability() == 0.9);
	}

}
