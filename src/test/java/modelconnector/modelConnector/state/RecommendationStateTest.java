package modelconnector.modelConnector.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.recommendationGenerator.state.RecommendationState;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class test the recommendation state.
 *
 * @author Sophie
 *
 */
public class RecommendationStateTest {

	private static String type = "component";
	private static String type2 = "typi";
	private static String name = "test.driver";
	private static String name2 = "test diver";
	private static RecommendedRelation rrl0;
	private static RecommendedInstance ri0;
	private static RecommendedInstance ri1;
	private static RecommendedInstance ri2;
	private static RecommendedInstance ri3;
	private static RecommendedInstance ri4;
	private static List<INode> relationNodes = new ArrayList<>();
	private static List<NounMapping> typeMappings = new ArrayList<>();
	private static List<NounMapping> nameMappings = new ArrayList<>();

	/**
	 * For testing the recommendation state some recommended instances and
	 * recommended relations are needed. These are created in this method.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {

		String content = ""//
				+ "The architecture contains ui component, Logic component, storage component, common component, " //
				+ "test driver component, e2e component, client component. The common component is in style of a beautiful component architecture. "//
				+ "The test.driver is a very important component. The logic.api, test.driver, and test.epic components are important, too.";

		IGraph graph = UtilsForTesting.getGraph(content);

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

		ri0 = new RecommendedInstance(name, type, 0.5, List.of(nameMappings.get(0)), List.of(typeMappings.get(0)));
		ri1 = new RecommendedInstance(name, "", 0.5, nameMappings, List.of());
		ri2 = new RecommendedInstance(name, type2, 0.5, nameMappings, typeMappings);
		ri3 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		ri4 = new RecommendedInstance(name2, type2, 0.5, List.of(), List.of());

		rrl0 = new RecommendedRelation("in", ri1, ri0, List.of(ri2), 0.5, relationNodes);
	}

	/**
	 * Tests if the recommendation is successfully created. This is the case if it's
	 * not null.
	 */
	@Test
	public void creation() {
		RecommendationState state = new RecommendationState();
		assertNotNull(state);

	}

	/**
	 * Tests the add and get functionality for relations. If a recommended relation
	 * is not already contained it should be added. If an equal recommended relation
	 * is contained, it should be extended by the occurrences of the recommended
	 * relations to add. The probability should change, too.
	 */
	@Test
	public void addAndGetRecommendedRelations() {
		RecommendationState state = new RecommendationState();

		state.addRecommendedRelation("in", ri0, ri1, List.of(ri2), 0.5, List.of(relationNodes.get(0)));
		assertEquals(List.of(rrl0), state.getRecommendedRelations());

		state.addRecommendedRelation("in", ri0, ri1, List.of(ri2), 0.8, relationNodes);
		assertEquals(List.of(rrl0), state.getRecommendedRelations());

		RecommendedRelation stateRrl = state.getRecommendedRelations().get(0);
		assertNotEquals(rrl0.getProbability(), stateRrl.getProbability());
		assertTrue(stateRrl.getNodes().containsAll(rrl0.getNodes()));
		assertEquals(rrl0.getNodes().size(), stateRrl.getNodes().size());

	}

	/**
	 * Tests the add functionality for recommended instances with parameters. If the
	 * recommended instance is not already contained it should be added. If a
	 * recommended instance with an equal name is already contained its
	 * nameMappings, and typeMappings should by extended. If the name or the type
	 * differs a new recommended instance should be created and added to the state.
	 */
	@Test
	public void addAndRecommendedInstanceWithParameters() {

		RecommendationState state = new RecommendationState();

		state.addRecommendedInstance(name, type, 0.5, List.of(nameMappings.get(0)), List.of(typeMappings.get(0)));
		List<RecommendedInstance> currentInstances = state.getRecommendedInstances();
		assertTrue(currentInstances.contains(ri0));
		assertEquals(1, currentInstances.size());

		RecommendedInstance ci = currentInstances.get(0);
		RecommendedInstance prevStateRI = new RecommendedInstance(ci.getName(), ci.getType(), ci.getProbability(), ci.getNameMappings(), ci.getTypeMappings());

		state.addRecommendedInstance(name, type, 0.5, List.of(nameMappings.get(0)), List.of(typeMappings.get(0)));
		currentInstances = state.getRecommendedInstances();
		assertTrue(currentInstances.contains(prevStateRI));
		assertEquals(1, currentInstances.size());
		assertEquals(prevStateRI.getNameMappings().size(), currentInstances.get(0).getNameMappings().size());
		assertEquals(prevStateRI.getTypeMappings().size(), currentInstances.get(0).getTypeMappings().size());

		state.addRecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		currentInstances = state.getRecommendedInstances();
		assertTrue(currentInstances.contains(prevStateRI));
		assertEquals(1, currentInstances.size());
		assertEquals(prevStateRI.getNameMappings().size(), currentInstances.get(0).getNameMappings().size());
		assertEquals(prevStateRI.getTypeMappings().size(), currentInstances.get(0).getTypeMappings().size());

		state.addRecommendedInstance(name, type2, 0.5, nameMappings, typeMappings);
		currentInstances = state.getRecommendedInstances();
		assertEquals(2, currentInstances.size());
		assertTrue(currentInstances.contains(ri2));

		state.addRecommendedInstance(name2, type, 0.5, nameMappings, typeMappings);
		currentInstances = state.getRecommendedInstances();
		assertEquals(3, currentInstances.size());
	}

	/**
	 * Test the add recommended instance functionality when using just a name. If a
	 * recommended instance is not already contained it is added to the state. If a
	 * recommended instance with the same name is already contained, the existing
	 * one is extended by the nameMappings. If a similar recommended instance with a
	 * type is added it extends the existing recommended instance without a type, by
	 * its attributes. If a recommended instance with another name, but the same
	 * mappings is added the add is executed.
	 *
	 */
	@Test
	public void addAndRecommendedInstancesJustName() {
		RecommendationState state = new RecommendationState();

		state.addRecommendedInstanceJustName(name, 0.5, List.of(nameMappings.get(0)));
		List<RecommendedInstance> currentInstances = state.getRecommendedInstances();
		assertTrue(currentInstances.contains(ri1));
		assertEquals(1, currentInstances.size());
		assertEquals(currentInstances.get(0).getNameMappings(), ri0.getNameMappings());

		RecommendedInstance ci = currentInstances.get(0);
		RecommendedInstance prevStateRI = new RecommendedInstance(ci.getName(), ci.getType(), ci.getProbability(), ci.getNameMappings(), ci.getTypeMappings());

		state.addRecommendedInstanceJustName(name, 0.5, List.of(nameMappings.get(0)));
		assertTrue(currentInstances.contains(prevStateRI));
		assertEquals(1, currentInstances.size());
		assertEquals(currentInstances.get(0).getNameMappings().size(), prevStateRI.getNameMappings().size());

		state.addRecommendedInstanceJustName(name, 0.5, nameMappings);
		assertTrue(currentInstances.contains(prevStateRI));
		assertEquals(1, currentInstances.size());
		assertTrue(2 <= nameMappings.size());
		assertEquals(currentInstances.get(0).getNameMappings().size(), prevStateRI.getNameMappings().size());

		state.addRecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		assertTrue(currentInstances.contains(ri1));
		assertFalse(currentInstances.contains(ri3));
		assertEquals(1, currentInstances.size());
		assertEquals(currentInstances.get(0).getNameMappings(), ri3.getNameMappings());
		assertNotEquals(ri3.getTypeMappings(), currentInstances.get(0).getTypeMappings());

		state.addRecommendedInstanceJustName(name2, 0.5, nameMappings);
		assertEquals(1, currentInstances.size());
	}

	/**
	 * Tests the getters of a recommendation state. The getters should return the
	 * stored information of recommended instances. These are: The recommended
	 * instances itself, the recommended instances by name, similar name, similar
	 * type, and type, as well as by the type mapping.
	 */
	@Test
	public void getterRecommendedInstances() {
		RecommendationState state = new RecommendationState();
		state.addRecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		state.addRecommendedInstance(name2, type2, 0.5, List.of(), List.of());

		assertEquals(state.getRecommendedInstances(), List.of(ri3, ri4));
		assertEquals(state.getRecommendedInstancesByName(name), List.of(ri3));
		assertEquals(state.getRecommendedInstancesBySimilarName(name), List.of(ri3));
		assertEquals(state.getRecommendedInstancesBySimilarType(type), List.of(ri3));
		assertEquals(state.getRecommendedInstancesByType(type), List.of(ri3));
		assertEquals(state.getRecommendedInstancesByTypeMapping(typeMappings.get(0)), List.of(ri3));
	}

}
