package modelconnector.modelConnector.state;

import static org.junit.Assert.assertEquals;
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
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class tests the recommended instances.
 *
 * @author Sophie
 *
 */
public class RecommendedInstanceTest {

	private static String type = "component";
	private static String name = "test.driver";
	private static String nort = "architecture";
	private static List<NounMapping> typeMappings = new ArrayList<>();
	private static List<NounMapping> nameMappings = new ArrayList<>();
	private static List<NounMapping> nortMappings = new ArrayList<>();

	/**
	 * For the following tests noun mappings are needed. These are created in this
	 * initialization.
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

		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			String nodeValue = GraphUtils.getNodeValue(n);
			if (nodeValue.contentEquals(type)) {
				typeMappings.add(NounMapping.createTypeMapping(n, 0.5, type, List.of(type)));

			} else if (nodeValue.contentEquals(name)) {
				nameMappings.add(NounMapping.createNameMapping(n, 0.5, name, List.of(name)));

			} else if (nodeValue.contentEquals(nort)) {
				nortMappings.add(NounMapping.createNortMapping(n, 0.5, nort, List.of(nort)));
			}
		}

		assertTrue(nameMappings.size() >= 2);
		assertTrue(typeMappings.size() >= 2);
	}

	/**
	 * Tests the creation of a recommended instance. It is successfully created if
	 * it's not null. The equality of two recommended instances depends on the name
	 * of the instance, and the type of the instance.
	 */
	@Test
	public void creation() {
		RecommendedInstance ri0 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		RecommendedInstance ri1 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);

		assertNotNull(ri0);
		assertEquals(ri0, ri1);

		RecommendedInstance ri2 = new RecommendedInstance(name, type, 0.5, nortMappings, nortMappings);
		assertEquals(ri0, ri2);

		RecommendedInstance ri3 = new RecommendedInstance(nort, type, 0.5, nameMappings, typeMappings);
		assertNotEquals(ri0, ri3);

		RecommendedInstance ri4 = new RecommendedInstance(name, nort, 0.5, nameMappings, typeMappings);
		assertNotEquals(ri0, ri4);
	}

	/**
	 * Tests the getters of a recommended instance. They should return the
	 * attributes of it (name, name mappings, probability, type, type mappings).
	 */
	@Test
	public void getter() {
		RecommendedInstance ri = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);

		assertEquals(name, ri.getName());
		assertEquals(nameMappings, ri.getNameMappings());
		assertEquals(0.5, ri.getProbability(), 0.001);
		assertEquals(type, ri.getType());
		assertEquals(typeMappings, ri.getTypeMappings());
	}

	/**
	 * Tests the functionality of adding mappings to a recommended instance. If
	 * mappings are not already contained by the recommended instance they are added
	 * to it.
	 */
	@Test
	public void addMappings() {

		NounMapping typeMapping0 = typeMappings.get(0);
		NounMapping typeMapping1 = typeMappings.get(1);
		NounMapping nameMapping0 = nameMappings.get(0);
		NounMapping nameMapping1 = nameMappings.get(1);

		RecommendedInstance ri = new RecommendedInstance(name, type, 0.5, List.of(nameMapping0), List.of(typeMapping0));

		ri.addMappings(nortMappings, nortMappings);

		ArrayList<NounMapping> newTypeMappings = new ArrayList<>();
		newTypeMappings.add(typeMapping0);
		newTypeMappings.addAll(nortMappings);

		ArrayList<NounMapping> newNameMappings = new ArrayList<>();
		newNameMappings.add(nameMapping0);
		newNameMappings.addAll(nortMappings);

		assertEquals(newNameMappings, ri.getNameMappings());
		assertEquals(newTypeMappings, ri.getTypeMappings());

		ri.addMappings(nameMapping1, typeMapping1);

		newNameMappings.add(nameMapping1);
		newTypeMappings.add(typeMapping1);

		assertEquals(newNameMappings, ri.getNameMappings());
		assertEquals(newTypeMappings, ri.getTypeMappings());

		ri.addMappings(nortMappings, nortMappings);
		ri.addMappings(nameMapping1, typeMapping1);
		assertEquals(newNameMappings, ri.getNameMappings());
		assertEquals(newTypeMappings, ri.getTypeMappings());
	}

	/**
	 * Tests the functionality of adding a name or type (nort). If a nort mapping is
	 * added to the to the names they are extended. If its added to the types these
	 * are extended to.
	 */
	@Test
	public void addNameOrType() {

		NounMapping typeMapping = typeMappings.get(0);
		NounMapping nortMapping = nortMappings.get(0);
		NounMapping nameMapping = nameMappings.get(0);

		RecommendedInstance ri = new RecommendedInstance(name, type, 0.5, List.of(nameMapping), List.of(typeMapping));

		ri.addName(nortMapping);
		assertTrue(ri.getNameMappings().containsAll(List.of(nameMapping, nortMapping)));
		assertEquals(2, ri.getNameMappings().size());

		ri.addType(nortMapping);
		assertTrue(ri.getTypeMappings().containsAll(List.of(typeMapping, nortMapping)));
		assertEquals(2, ri.getNameMappings().size());
	}

	/**
	 * Tests the removal of a mapping from the recommended instance. After the
	 * removal the mapping should not be contained in the mappings of the
	 * recommended isntance.
	 */
	@Test
	public void remove() {

		NounMapping typeMapping = typeMappings.get(0);
		NounMapping nameMapping = nameMappings.get(0);

		RecommendedInstance ri = new RecommendedInstance(name, type, 0.5, nameMappings, List.of(typeMapping));

		ri.removeNounNodeMappingsFromName(List.of(nameMapping));

		List<NounMapping> newNameMappings = new ArrayList<>(nameMappings);
		newNameMappings.remove(nameMapping);

		assertTrue(ri.getNameMappings().containsAll(newNameMappings));
		assertEquals(newNameMappings.size(), ri.getNameMappings().size());

	}

	/**
	 * Tests the setters of a recommended instance. The probability should be set to
	 * the given value. The type and name should be set, too.
	 */
	@Test
	public void setter() {
		RecommendedInstance ri = new RecommendedInstance(name, type, 0.5, List.of(nameMappings.get(0)), List.of(typeMappings.get(0)));

		ri.setProbability(0.8);
		assertEquals(0.8, ri.getProbability(), 0.001);

		ri.setType(nort);
		assertEquals(nort, ri.getType());

		ri.setName(nort);
		assertEquals(nort, ri.getName());

	}

}