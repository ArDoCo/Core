package modelconnector.textExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

/**
 * Tests the functionality of the noun mappings.
 *
 * @author Sophie
 *
 */
public class NounMappingTest {

	private static IGraph graph;
	private static String type = "component";
	private static String name = "common";
	private static String nort = "architecture";
	private static String separatedNort = "test.driver";
	private static String separatedName = "logic.api";
	private static String altSeparatedNort = "test.epic";
	private static List<INode> typeNodes = new ArrayList<>();
	private static List<INode> nameNodes = new ArrayList<>();
	private static List<INode> nortNodes = new ArrayList<>();
	private static List<INode> separatedNortNodes = new ArrayList<>();
	private static List<INode> separatedNameNodes = new ArrayList<>();
	private static List<INode> altSeparatedNortNodes = new ArrayList<>();

	/**
	 * Before this class the PARSE graph has to be created. The nodes have to be
	 * collected.
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

		for (INode n : graph.getNodesOfType(graph.getNodeType("token"))) {
			String nodeValue = GraphUtils.getNodeValue(n);
			if (nodeValue.contentEquals(type)) {
				typeNodes.add(n);
			} else if (nodeValue.contentEquals(nort)) {
				nortNodes.add(n);
			} else if (nodeValue.contentEquals(name)) {
				nameNodes.add(n);
			} else if (nodeValue.contentEquals(separatedNort)) {
				separatedNortNodes.add(n);
			} else if (nodeValue.contentEquals(separatedName)) {
				separatedNameNodes.add(n);
			} else if (nodeValue.contentEquals(altSeparatedNort)) {
				altSeparatedNortNodes.add(n);
			}
		}
	}

	/**
	 * Tests the creation of noun mappings. Secures that the created noun mappings
	 * aren't null. Asserts that similar created noun mappings are equal. Asserts
	 * that noun mappings created by different functions equal.
	 */
	@Test
	public void creation() {

		NounMapping nameMapping = NounMapping.createNameMapping(nameNodes.get(0), 0.5, name, List.of(name));
		NounMapping typeMapping = NounMapping.createTypeMapping(typeNodes.get(0), 0.5, type, List.of(type));
		NounMapping nortMapping = NounMapping.createNortMapping(nortNodes.get(0), 0.5, nort, List.of(nort));

		assertNotNull(nameMapping);
		assertNotNull(typeMapping);
		assertNotNull(nortMapping);

		NounMapping nameMapping2 = NounMapping.createNameNode(List.of(nameNodes.get(0)), 0.5, name, List.of(name));
		NounMapping typeMapping2 = NounMapping.createTypeNode(List.of(typeNodes.get(0)), 0.5, type, List.of(type));
		NounMapping nortMapping2 = NounMapping.createNortNode(List.of(nortNodes.get(0)), 0.5, nort, List.of(nort));

		NounMapping nameMapping3 = NounMapping.createMappingTypeNode(nameNodes.get(0), name, MappingKind.NAME, 0.5, List.of(name));
		NounMapping typeMapping3 = NounMapping.createMappingTypeNode(typeNodes.get(0), type, MappingKind.TYPE, 0.5, List.of(type));
		NounMapping nortMapping3 = NounMapping.createMappingTypeNode(nortNodes.get(0), nort, MappingKind.NAME_OR_TYPE, 0.5, List.of(nort));

		assertEquals(nameMapping2, nameMapping);
		assertEquals(typeMapping2, typeMapping);
		assertEquals(nortMapping2, nortMapping);
		assertEquals(nameMapping3, nameMapping);
		assertEquals(typeMapping3, typeMapping);
		assertEquals(nortMapping3, nortMapping);

		assertEquals(List.of(nameNodes.get(0)), nameMapping.getNodes());

	}

	/**
	 * Tests the general getters of noun mappings. The getter of mapping type should
	 * return the specified mapping type. The occurrences, probability, reference
	 * and SentenceNo should be as defined in the creation.
	 */
	@Test
	public void getter() {

		NounMapping nameMapping = NounMapping.createNameMapping(nameNodes.get(0), 0.5, name, List.of(name));
		NounMapping typeMapping = NounMapping.createTypeMapping(typeNodes.get(0), 0.5, type, List.of(type));
		NounMapping nortMapping = NounMapping.createNortMapping(nortNodes.get(0), 0.5, nort, List.of(nort));

		assertEquals(MappingKind.NAME_OR_TYPE, nortMapping.getKind());
		assertEquals(MappingKind.NAME, nameMapping.getKind());
		assertEquals(MappingKind.TYPE, typeMapping.getKind());

		assertEquals(List.of(nort), nortMapping.getOccurrences());
		assertEquals(0.5, nortMapping.getProbability(), 0.001);
		assertEquals(nort, nortMapping.getReference());
		assertEquals(1.0, nortMapping.getMappingSentenceNo().get(0), 0.001);

	}

	/**
	 * Test the getter of representative comparables. The comparables of a mapping
	 * with a part of a separated occurrence and a mapping with the separated
	 * occurrence itself should be equal.
	 */
	@Test
	public void getRepresentativeComparables() {
		NounMapping nameMapping = NounMapping.createNameMapping(nameNodes.get(0), 0.5, name, List.of(name));

		assertEquals(nameMapping.getOccurrences(), nameMapping.getRepresentativeComparables());

		List<String> occParts = new ArrayList<>(List.of(SimilarityUtils.splitAtSeparators(separatedNort).split(" ")));

		NounMapping nortMapping = NounMapping.createNortMapping(separatedNortNodes.get(0), 0.5, occParts.get(0), List.of(separatedNort));

		assertEquals(List.of(occParts.get(0), separatedNort), nortMapping.getRepresentativeComparables());
	}

	/**
	 * Test the changes of a noun mapping. A created mapping should be able to
	 * change its mapping type. By this the probability should change, too. The
	 * probability of a mapping can be hard set. By this the probability should be
	 * set on the new probability. The reference can be updated, too. If the
	 * probability for this change is to low, the change isn't executed. If its
	 * significantly higher than the current reference it should be changed. The
	 * probability can be updated, too. Then the probability should change.
	 * Occurrences can be added. If they aren't already contained this update is
	 * executed.
	 */
	@Test
	public void change() {

		NounMapping nameMapping = NounMapping.createNameMapping(nameNodes.get(0), 0.5, name, List.of(name));

		nameMapping.changeMappingTypeTo(MappingKind.TYPE, 0.9);

		assertEquals(MappingKind.TYPE, nameMapping.getKind());
		assertNotEquals(0.5, nameMapping.getProbability());

		nameMapping.hardSetProbability(0.1);
		assertEquals(0.1, nameMapping.getProbability(), 0.001);

		nameMapping.updateReference(type, 0.15);
		assertNotEquals(type, nameMapping.getReference());
		assertEquals(0.1, nameMapping.getProbability(), 0.001);

		nameMapping.updateReference(type, 0.05);
		assertNotEquals(type, nameMapping.getReference());

		nameMapping.updateReference(type, 0.9);
		assertEquals(type, nameMapping.getReference());

		assertEquals(0.1, nameMapping.getProbability(), 0.001);
		nameMapping.updateProbability(0.75);
		assertNotEquals(0.1, nameMapping.getProbability());

		assertFalse(nameMapping.getOccurrences().contains(nort));
		nameMapping.addOccurrence(List.of(nort));
		assertTrue(nameMapping.getOccurrences().contains(nort));

	}

	/**
	 * This test secures the functionality of copying occurrences and nodes of a
	 * noun mapping to another, given noun mapping. It is secured, that after the
	 * execution, the occurrences of the given noun mapping contain the occurrences
	 * of the original mapping. Moreover it is secured that the nodes are contained.
	 */
	@Test
	public void copyOccurrencesAndNodes() {
		INode nameNode = nameNodes.get(0);
		INode typeNode = typeNodes.get(0);

		NounMapping nameMapping = NounMapping.createNameMapping(nameNode, 0.5, name, List.of(name));
		NounMapping typeMapping = NounMapping.createTypeMapping(typeNode, 0.5, type, List.of(type));

		assertFalse(typeMapping.getOccurrences().contains(name));
		assertFalse(typeMapping.getNodes().contains(nameNode));

		nameMapping.copyOccurrencesAndNodesTo(name, typeMapping);

		assertTrue(typeMapping.getOccurrences().contains(name));
		assertTrue(typeMapping.getNodes().contains(nameNode));

	}

	/**
	 * Test the functionality of adding nodes to a noun mapping. If a node is added
	 * that is not already contained the nodes should be extended. If more than one
	 * node is added all should be contained by the nodes of the mapping, after the
	 * execution. If nodes are added, that are already contained, the size should
	 * not change.
	 */
	@Test
	public void addNodes() {

		INode typeNode = typeNodes.get(0);

		NounMapping nortMapping = NounMapping.createNortMapping(nortNodes.get(0), 0.5, nort, List.of(nort));

		nortMapping.addNode(typeNode);

		assertTrue(nortMapping.getNodes().contains(typeNode));

		nortMapping.addNodes(nameNodes);

		assertTrue(nortMapping.getNodes().containsAll(nameNodes));

		nortMapping.addNodes(nortNodes);

		Set<INode> nodesAsSet = new HashSet<>();
		nodesAsSet.addAll(nameNodes);
		nodesAsSet.addAll(nortNodes);
		nodesAsSet.add(typeNode);

		assertEquals(nortMapping.getNodes().size(), nodesAsSet.size());
	}

}
