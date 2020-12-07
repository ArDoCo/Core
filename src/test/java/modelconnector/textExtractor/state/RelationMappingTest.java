package modelconnector.textExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.helpers.GraphUtils;

/**
 * Test for the RelationMapping.
 *
 * @author Sophie
 *
 */
public class RelationMappingTest {

	private static IGraph graph;
	private String type = "component";
	private String name = "common";
	private String nort = "architecture";
	private List<NounMapping> typeMappings = new ArrayList<>();
	private List<NounMapping> nameMappings = new ArrayList<>();
	private List<NounMapping> nortMappings = new ArrayList<>();

	/**
	 * Before running all relation mapping tests a graph to work on has to be
	 * generated.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {

		String content = "" + //
				"The architecture contains ui component, Logic component, storage component, common component, test driver component,"//
				+ " e2e component, client component. The common component is in style of a beautiful component architecture. "//
				+ "The test.driver is a very important component. The logic.api, test.driver, and test.epic components are important, too.";
		graph = UtilsForTesting.getGraph(content);
	}

	/**
	 * Before each test the typeMappings, nortMappings and nameMappings have to be
	 * filled.
	 */
	@Before
	public void initializeTest() {

		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			String nodeValue = GraphUtils.getNodeValue(n);
			if (nodeValue.contentEquals(type)) {
				typeMappings.add(NounMapping.createTypeMapping(n, 0.5, type, List.of(type)));
			} else if (nodeValue.contentEquals(nort)) {
				nortMappings.add(NounMapping.createNortMapping(n, 0.5, nort, List.of(nort)));
			} else if (nodeValue.contentEquals(name)) {
				nameMappings.add(NounMapping.createNameMapping(n, 0.5, name, List.of(name)));
			}
		}
	}

	/**
	 * Tests the creation of a relation mapping. A relation mapping is successfully
	 * created, if its not null and equals to a similar created relation mapping.
	 */
	@Test
	public void creation() {
		RelationMapping rm1 = new RelationMapping(nortMappings.get(0), nameMappings.get(0), 0.5);
		assertNotNull(rm1);
		RelationMapping rm2 = new RelationMapping(nortMappings.get(0), nameMappings.get(0), 0.5);
		assertEquals(rm2, rm1);
	}

	/**
	 * Tests the getter of a relation mapping: Asserts that the occurrences of the
	 * get and the ones of the creation are equals. Asserts that the probability is
	 * equals. Asserts that, if no preposition was set, null is returned.
	 */
	@Test
	public void getter() {

		RelationMapping rm = new RelationMapping(nortMappings.get(0), nameMappings.get(0), 0.5);

		assertEquals(List.of(nortMappings.get(0), nameMappings.get(0)), rm.getOccurrenceNodes());
		assertEquals(0.5, rm.getProbability(), 0.001);
		assertNull(rm.getPreposition());

	}

	/**
	 * Tests the setters of the relation mapping. If the preposition is set, it
	 * should be returned by the getter, tested in {@link #getter()}.
	 */
	@Test
	public void setter() {
		RelationMapping rm = new RelationMapping(nortMappings.get(0), nameMappings.get(0), 0.5);

		assertNull(rm.getPreposition());
		INode prePosition = typeMappings.get(0).getNodes().get(0);
		rm.setPreposition(prePosition);
		assertEquals(prePosition, rm.getPreposition());
	}

	/**
	 * Tests if nodes can be added to the relation mapping. When adding a node that
	 * is already contained by the relation mapping, the add should not be executed.
	 * When adding a new node, it should be executed.
	 */
	@Test
	public void addNodes() {
		RelationMapping rm = new RelationMapping(nortMappings.get(0), nameMappings.get(0), 0.5);

		List<NounMapping> mappings = new ArrayList<>();
		mappings.add(nortMappings.get(0));

		List<NounMapping> currentRMNortMappings = rm.getOccurrenceNodes().stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
		assertEquals(mappings, currentRMNortMappings);

		rm.addMappingsToRelation(List.of(nortMappings.get(0)));
		currentRMNortMappings = rm.getOccurrenceNodes().stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
		assertEquals(mappings, currentRMNortMappings);

		rm.addMappingsToRelation(List.of(nortMappings.get(1)));
		mappings.addAll(List.of(nortMappings.get(1)));
		currentRMNortMappings = rm.getOccurrenceNodes().stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
		assertEquals(mappings, currentRMNortMappings);

	}

}
