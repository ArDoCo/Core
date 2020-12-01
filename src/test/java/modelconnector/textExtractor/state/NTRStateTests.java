package modelconnector.textExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;

/**
 * This test class tests the functionality of the ntr state.
 *
 * @author Sophie
 *
 */
public class NTRStateTests {

	private TextExtractionState ntrState;
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
	private static List<NounMapping> typeMappings = new ArrayList<>();
	private static List<NounMapping> nameMappings = new ArrayList<>();
	private static List<NounMapping> nortMappings = new ArrayList<>();
	private static List<INode> separatedNortNodes = new ArrayList<>();
	private static List<INode> separatedNameNodes = new ArrayList<>();
	private static List<INode> altSeparatedNortNodes = new ArrayList<>();

	/**
	 * Before the tests can run the graph mus be initilized. Moreover, different
	 * type, nort and name nodes and mappings have to be created. Also separated
	 * nodes should be created.
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
				typeMappings.add(NounMapping.createTypeMapping(n, 0.5, type, List.of(type)));
			} else if (nodeValue.contentEquals(nort)) {
				nortNodes.add(n);
				nortMappings.add(NounMapping.createNortMapping(n, 0.5, nort, List.of(nort)));
			} else if (nodeValue.contentEquals(name)) {
				nameNodes.add(n);
				nameMappings.add(NounMapping.createNameMapping(n, 0.5, name, List.of(name)));
			} else if (nodeValue.contentEquals(separatedNort)) {
				separatedNortNodes.add(n);
			} else if (nodeValue.contentEquals(separatedName)) {
				separatedNameNodes.add(n);
			} else if (nodeValue.contentEquals(altSeparatedNort)) {
				altSeparatedNortNodes.add(n);
			}
		}

		assertFalse(typeNodes.isEmpty());
		assertFalse(nameNodes.isEmpty());
		assertFalse(nortNodes.isEmpty());
		assertEquals(2, separatedNortNodes.size());
	}

	/**
	 * The ntrState has to be created newly before each test.
	 */
	@Before
	public void createNtrState() {
		ntrState = new TextExtractionState();

	}

	/**
	 * Tests the creation of a ntr state. It is created successfully, if its not
	 * null.
	 */
	public void creation() {
		assertNotNull(ntrState);
	}

	/**
	 * Tests the functionality of containing requests in the ntr state. Moreover,
	 * the functionality of the contains check with nodes of specific mapping types
	 * is tested. This test asserts, that in a freshly created ntrState no nodes are
	 * contained. By adding nodes, all nodes, no matter what mapping type, should be
	 * contained by noun mappings, as they should be found by their specific mapping
	 * type.
	 */
	@Test
	public void containedInNodes() {

		assertFalse(ntrState.isNodeContainedByNounMappings(typeNodes.get(0)));
		assertFalse(ntrState.isNodeContainedByNounMappings(nameNodes.get(0)));
		assertFalse(ntrState.isNodeContainedByNounMappings(nortNodes.get(0)));

		assertFalse(ntrState.isNodeContainedByTypeNodes(typeNodes.get(0)));
		assertFalse(ntrState.isNodeContainedByNameNodes(nameNodes.get(0)));
		assertFalse(ntrState.isNodeContainedByNameOrTypeNodes(nortNodes.get(0)));

		ntrState.addType(typeNodes.get(0), type, 1.0);
		ntrState.addName(nameNodes.get(0), name, 1.0);
		ntrState.addNort(nortNodes.get(0), nort, 1.0);

		assertTrue(ntrState.isNodeContainedByTypeNodes(typeNodes.get(0)));
		assertTrue(ntrState.isNodeContainedByNameNodes(nameNodes.get(0)));
		assertTrue(ntrState.isNodeContainedByNameOrTypeNodes(nortNodes.get(0)));

		assertTrue(ntrState.isNodeContainedByNounMappings(typeNodes.get(0)));
		assertTrue(ntrState.isNodeContainedByNounMappings(nameNodes.get(0)));
		assertTrue(ntrState.isNodeContainedByNounMappings(nortNodes.get(0)));
	}

	/**
	 * Tests the method for removing noun mappings in the ntr state. It asserts,
	 * that when a type is added the node can be found by the general, as well by
	 * the specific mapping type request. Moreover, it asserts, that the reference
	 * of the mapping type is contained by the specific mapping type list. After
	 * removing the noun mapping. The noun mapping shouldn't be found by the general
	 * or specialized contained function, or listed as mapping type with its
	 * reference.
	 */
	@Test
	public void removeNounMapping() {

		assertFalse(ntrState.getTypeList().contains(type));

		ntrState.addType(typeNodes.get(0), type, 1.0);

		assertTrue(ntrState.isNodeContainedByTypeNodes(typeNodes.get(0)));
		assertTrue(ntrState.isNodeContainedByNounMappings(typeNodes.get(0)));

		List<NounMapping> typeMaps = ntrState.getNounMappingsByNode(typeNodes.get(0));
		assertEquals(1, typeMaps.size());
		assertTrue(ntrState.getTypeList().contains(type));

		ntrState.removeNounNode(typeMaps.get(0));

		assertFalse(ntrState.isNodeContainedByNounMappings(typeNodes.get(0)));
		assertFalse(ntrState.isNodeContainedByTypeNodes(typeNodes.get(0)));
		assertFalse(ntrState.getTypeList().contains(type));
	}

	/**
	 * Tests if a type is added if its not already contained by the ntr state. It
	 * asserts that after adding the noun mapping the specific noun mapping getter
	 * returns the correct node. Moreover, it asserts that the noun mapping is
	 * returned by the getter of all noun mappings of its mapping type, and its
	 * reference is returned as one of them.
	 */
	@Test
	public void addTypeIfTypeIsNew() {
		INode typeNode = typeNodes.get(0);
		ntrState.addType(typeNode, type, 1.0);

		NounMapping typeMapping = ntrState.getTypeNodesByNode(typeNode).get(0);

		assertEquals(typeMapping.getNodes().get(0), typeNode);
		assertTrue(ntrState.getTypes().contains(typeMapping));
		assertTrue(ntrState.getTypeList().contains(type));
	}

	/**
	 * Tests the functionality of adding a type if the node is already contained as
	 * a name or type (nort mapping type). After adding a nort it should be returned
	 * by all getters for nort nodes and mappings. When adding the same node as
	 * type, instead of the nort getters the type getters should return the node and
	 * mapping. The mapping type of the mapping should have been changed.
	 */
	@Test
	public void addTypeIfTypeIsNort() {
		INode typeNode = typeNodes.get(0);

		ntrState.addNort(typeNode, type, 1.0);

		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());
		List<String> nortList = ntrState.getNortList();
		assertTrue(nortList.contains(type));

		ntrState.addType(typeNode, type, 1.0);

		assertTrue(ntrState.getNameOrTypeMappings().isEmpty());
		assertFalse(ntrState.getNortList().contains(nort));
		List<NounMapping> typeMapping = ntrState.getTypeNodesByNode(typeNode);
		assertFalse(typeMapping.isEmpty());
		assertTrue(ntrState.getTypes().contains(typeMapping.get(0)));
		assertTrue(ntrState.getTypeList().contains(type));

	}

	/**
	 * Tests the functionality of adding a type if it is already contained as name.
	 * In this case the probability of the mapping for being a type is significant
	 * higher than the probability of being a name. First, the node is added as a
	 * name. This is secured by the check of the getters for names. Then, the node
	 * is added as a type with higher probability. After that, not the names, but
	 * the types should contain the the node as mapping. The mapping type should be
	 * changed to type.
	 */
	@Test
	public void addTypeIfMoreLikelyTypeIsName() {

		INode typeNode = typeNodes.get(0);

		ntrState.addName(typeNode, type, 0.5);

		assertFalse(ntrState.getNames().isEmpty());
		assertTrue(ntrState.getNameList().contains(type));

		ntrState.addType(typeNode, type, 1.0);

		assertTrue(ntrState.getNames().isEmpty());
		assertFalse(ntrState.getNameList().contains(type));

		List<NounMapping> typeMapping = ntrState.getTypeNodesByNode(typeNode);
		assertFalse(typeMapping.isEmpty());
		assertTrue(ntrState.getTypes().contains(typeMapping.get(0)));
		assertTrue(ntrState.getTypeList().contains(type));

	}

	/**
	 * Tests the functionality of adding a type if it is already contained as name.
	 * In this case the probability of the mapping for being a type is more less
	 * than the probability of being a name. First, the node is added as a name.
	 * This is secured by the check of the getters for names. Then, the node is
	 * added as a type with less probability. After that, the node should be still
	 * contained by the names and not by the types. Moreover, it is secured that the
	 * mapping type of the noun mapping hasn't changed.
	 */
	@Test
	public void addTypeIfLessLikelyTypeIsName() {

		INode typeNode = typeNodes.get(0);

		ntrState.addName(typeNode, type, 1.0);
		assertFalse(ntrState.getNames().isEmpty());
		assertTrue(ntrState.getNameList().contains(type));

		ntrState.addType(typeNode, type, 0.5);

		assertFalse(ntrState.getNames().isEmpty());
		assertTrue(ntrState.getNameList().contains(type));

		List<NounMapping> typeMapping = ntrState.getTypeNodesByNode(typeNode);
		assertTrue(typeMapping.isEmpty());
		assertFalse(ntrState.getTypeList().contains(type));
	}

	/**
	 * Tests the functionality of adding a type if it is already contained as type.
	 * If the node is already contained the add should not change the noun mapping
	 * entries of before. By adding an already contained noun mapping of the similar
	 * mapping type the probability of the existing mapping should be increased.
	 */
	@Test
	public void addTypeIfTypeIsType() {
		INode typeNode = typeNodes.get(0);

		ntrState.addType(typeNode, type, 0.5);
		assertFalse(ntrState.getTypes().isEmpty());
		assertTrue(ntrState.getTypeList().contains(type));

		List<NounMapping> before2nd = ntrState.getTypes();

		ntrState.addType(typeNode, type, 1.0);

		List<NounMapping> typeMapping = ntrState.getTypeNodesByNode(typeNode);
		assertFalse(typeMapping.isEmpty());
		assertEquals(ntrState.getTypes(), before2nd);
		assertTrue(ntrState.getTypeList().contains(type));

		assertTrue(typeMapping.get(0).getProbability() > 0.5);
	}

	/**
	 * Tests if a name is added, if its not already contained by the ntr state. It
	 * asserts that after adding the noun mapping the specific noun mapping getter
	 * returns the correct node. Moreover, it asserts that the noun mapping is
	 * returned by the getter of all noun mappings of its mapping type, and its
	 * reference is returned as one of them.
	 */
	@Test
	public void addNameIfNameIsNew() {
		INode nameNode = nameNodes.get(0);

		assertFalse(nameNodes.isEmpty());
		assertTrue(ntrState.getNameNodesByNode(nameNode).isEmpty());

		ntrState.addName(nameNode, name, 1.0);

		List<NounMapping> nameMapping = ntrState.getNameNodesByNode(nameNode);
		assertFalse(nameMapping.isEmpty());
		assertTrue(ntrState.getNames().contains(nameMapping.get(0)));
		assertTrue(ntrState.getNameList().contains(name));
	}

	/**
	 * Tests the functionality of adding a name if the node is already contained as
	 * a name or type (nort mapping type). After adding a nort it should be returned
	 * by all getters for nort nodes and mappings. When adding the same node as
	 * name, instead of the nort getters the name getters should return the node and
	 * mapping. The mapping type of the mapping should have been changed.
	 */
	@Test
	public void addNameIfNameIsNort() {
		INode nameNode = nameNodes.get(0);

		ntrState.addNort(nameNode, name, 1.0);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());
		assertTrue(ntrState.getNortList().contains(name));

		ntrState.addName(nameNode, name, 1.0);

		assertFalse(ntrState.getNortList().contains(name));
		assertTrue(ntrState.getNameOrTypeMappings().isEmpty());

		List<NounMapping> nameMapping = ntrState.getNameNodesByNode(nameNode);
		assertFalse(nameMapping.isEmpty());
		assertTrue(ntrState.getNames().contains(nameMapping.get(0)));
		assertTrue(ntrState.getNameList().contains(name));

	}

	/**
	 * Tests the functionality of adding a name if it is already contained as type.
	 * In this case the probability of the mapping for being a name is significant
	 * higher than the probability of being a type. First, the node is added as a
	 * type. This is secured by the check of the getters for types. Then, the node
	 * is added as a name with higher probability. After that, not the types, but
	 * the names should contain the the node as mapping. The mapping type should be
	 * changed to name.
	 */
	@Test
	public void addNameIfMoreLikelyNameIsType() {

		INode nameNode = nameNodes.get(0);

		ntrState.addType(nameNode, name, 0.5);
		assertFalse(ntrState.getTypes().isEmpty());
		assertTrue(ntrState.getTypeList().contains(name));

		ntrState.addName(nameNode, name, 1.0);

		assertFalse(ntrState.getTypeList().contains(name));
		assertTrue(ntrState.getTypes().isEmpty());

		List<NounMapping> nameMapping = ntrState.getNameNodesByNode(nameNode);
		assertFalse(nameMapping.isEmpty());
		assertTrue(ntrState.getNames().contains(nameMapping.get(0)));
		assertTrue(ntrState.getNameList().contains(name));

	}

	/**
	 * Tests the functionality of adding a name if it is already contained as type.
	 * In this case the probability of the mapping for being a name is more less
	 * than the probability of being a type. First, the node is added as a type.
	 * This is secured by the check of the getters for type. Then, the node is added
	 * as a name with less probability. After that, the node should be still
	 * contained by the types and not by the names. Moreover, it is secured that the
	 * mapping type of the noun mapping hasn't changed.
	 */
	@Test
	public void addNameIfLessLikelyNameIsType() {

		INode nameNode = nameNodes.get(0);

		ntrState.addType(nameNode, name, 1.0);
		assertFalse(ntrState.getTypes().isEmpty());
		assertTrue(ntrState.getTypeList().contains(name));

		ntrState.addName(nameNode, name, 0.5);

		assertTrue(ntrState.getTypeList().contains(name));
		assertFalse(ntrState.getTypes().isEmpty());

		List<NounMapping> nameMapping = ntrState.getNameNodesByNode(nameNode);
		assertTrue(nameMapping.isEmpty());
		assertFalse(ntrState.getNameList().contains(name));
	}

	/**
	 * Tests the functionality of adding a name if it is already contained as name.
	 * If the node is already contained the add should not change the noun mapping
	 * entries of before. By adding an already contained noun mapping of the similar
	 * mapping type the probability of the existing mapping should be increased.
	 */
	@Test
	public void addNameIfNameIsName() {

		INode nameNode = nameNodes.get(0);

		assertTrue(ntrState.getNames().isEmpty());
		assertFalse(ntrState.getNameList().contains(name));

		ntrState.addName(nameNode, name, 0.5);
		assertFalse(ntrState.getNames().isEmpty());
		assertTrue(ntrState.getNameList().contains(name));

		List<NounMapping> before2nd = ntrState.getNames();

		ntrState.addName(nameNode, name, 1.0);

		List<NounMapping> nameMapping = ntrState.getNameNodesByNode(nameNode);
		assertFalse(nameMapping.isEmpty());
		assertTrue(ntrState.getNames().contains(nameMapping.get(0)));
		assertTrue(ntrState.getNameList().contains(name));

		assertEquals(before2nd, ntrState.getNames());
		assertTrue(ntrState.getNameNodesByNode(nameNode).get(0).getProbability() > 0.5);
	}

	/**
	 * Tests if a nort is added, if its not already contained by the ntr state. It
	 * asserts that after adding the noun mapping the specific noun mapping getter
	 * returns the correct node. Moreover, it asserts that the noun mapping is
	 * returned by the getter of all noun mappings of its mapping type, and its
	 * reference is returned as one of them.
	 */
	@Test
	public void addNortIfNortIsNew() {

		INode nortNode = nortNodes.get(0);

		ntrState.addNort(nortNode, nort, 1.0);

		List<NounMapping> nortMapping = ntrState.getNortNodesByNode(nortNode);
		assertFalse(nortMapping.isEmpty());
		assertTrue(ntrState.getNameOrTypeMappings().contains(nortMapping.get(0)));
		assertTrue(ntrState.getNortList().contains(nort));
	}

	/**
	 * Tests the functionality of adding a name or type (nort) if it is already
	 * contained as type. First, the node is added as a type. This is secured by the
	 * check of the getters for type. Then, the node is added as a nort. After that,
	 * the node should be still contained by the types and not by the norts.
	 * Moreover, it is secured that the mapping type of the noun mapping hasn't
	 * changed.
	 */
	@Test
	public void addNortIfNortIsType() {

		INode nortNode = nortNodes.get(0);

		ntrState.addType(nortNode, nort, 1.0);
		assertTrue(ntrState.getTypeList().contains(nort));
		assertFalse(ntrState.getTypeNodesByNode(nortNode).isEmpty());

		ntrState.addNort(nortNode, nort, 1.0);

		assertTrue(ntrState.getTypeList().contains(nort));
		List<NounMapping> nortMapping = ntrState.getNortNodesByNode(nortNode);
		assertTrue(nortMapping.isEmpty());
		assertFalse(ntrState.getNortList().contains(nort));

	}

	/**
	 * Tests the functionality of adding a name or type (nort) if it is already
	 * contained as name. First, the node is added as a name. This is secured by the
	 * check of the getters for name. Then, the node is added as a nort. After that,
	 * the node should be still contained by the names and not by the norts.
	 * Moreover, it is secured that the mapping type of the noun mapping hasn't
	 * changed.
	 */
	@Test
	public void addNortIfNortIsName() {

		INode nortNode = nortNodes.get(0);

		assertFalse(nortNodes.isEmpty());
		assertTrue(ntrState.getNortNodesByNode(nortNode).isEmpty());

		ntrState.addName(nortNodes.get(0), nort, 1.0);
		assertTrue(ntrState.getNameList().contains(nort));
		assertFalse(ntrState.getNameNodesByNode(nortNode).isEmpty());

		ntrState.addNort(nortNode, nort, 1.0);

		assertTrue(ntrState.getNameList().contains(nort));
		List<NounMapping> nortMapping = ntrState.getNortNodesByNode(nortNode);
		assertTrue(nortMapping.isEmpty());
		assertFalse(ntrState.getNortList().contains(nort));
	}

	/**
	 * Tests the functionality of adding a name or type (nort) if it is already
	 * contained as nort. If the node is already contained the add should not change
	 * the noun mapping entries of before. By adding an already contained noun
	 * mapping of the similar mapping type the probability of the existing mapping
	 * should be increased.
	 */
	@Test
	public void addNortIfNortIsNort() {

		INode nortNode = nortNodes.get(0);

		ntrState.addNort(nortNode, nort, 0.5);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());
		assertTrue(ntrState.getNortList().contains(nort));

		List<NounMapping> before2nd = ntrState.getNameOrTypeMappings();

		ntrState.addNort(nortNode, nort, 1.0);

		List<NounMapping> nortMapping = ntrState.getNortNodesByNode(nortNode);
		assertFalse(nortMapping.isEmpty());
		assertTrue(ntrState.getNameOrTypeMappings().contains(nortMapping.get(0)));
		assertTrue(ntrState.getNortList().contains(nort));

		assertEquals(before2nd, ntrState.getNameOrTypeMappings());
		assertTrue(ntrState.getNortNodesByNode(nortNode).get(0).getProbability() > 0.5);

	}

	/**
	 * Tests the adding functionality if the ref, but not the node is contained by
	 * the noun mappings of the ntr state. In this case, the existing noun mapping
	 * with the reference should be extended by the node to add. It is secured, that
	 * the noun mapping can be addressed by the newly added node, too.
	 */
	@Test
	public void addNodeIfRefButNotNodeIsContained() {

		INode nortNode = nortNodes.get(0);
		INode typeNode = typeNodes.get(0);
		List<String> occurrences = List.of("blub");

		ntrState.addNort(nortNode, nort, 0.5);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());
		assertTrue(ntrState.getNortList().contains(nort));

		ntrState.addNort(typeNode, ntrState.getNounMappingsByNode(nortNode).get(0).getReference(), 1.0, occurrences);

		List<NounMapping> nms = ntrState.getNortNodesByNode(nortNode);
		assertEquals(1, nms.size());

		NounMapping nm = nms.get(0);
		assertTrue(nm.getOccurrences().containsAll(occurrences));
		assertEquals(nms, ntrState.getNortNodesByNode(typeNode));
	}

	/**
	 * This method tests if separated nort nodes can be added. It is tested, that
	 * for each part a nort mapping is created, with the part as reference.
	 */
	@Test
	public void addSeparatedNortNode() {

		INode separatedNortNode = separatedNortNodes.get(0);

		ntrState.addNort(separatedNortNode, separatedNort, 0.5);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());
		List<String> separatedNortParts = List.of(SimilarityUtils.splitAtSeparators(separatedNort).split(" "));

		assertEquals(2, separatedNortParts.size());
		String separatedNortPart0 = separatedNortParts.get(0);
		String separatedNortPart1 = separatedNortParts.get(1);

		assertTrue(ntrState.getNortList().contains(separatedNortPart0));
		assertTrue(ntrState.getNortList().contains(separatedNortPart1));

		List<NounMapping> nms = ntrState.getNortNodesByNode(separatedNortNode);

		assertEquals(2, nms.size());
		NounMapping nm0 = nms.get(0);
		NounMapping nm1 = nms.get(1);

		assertTrue(nm0.getReference().contentEquals(separatedNortPart0));
		assertEquals(nm1.getReference(), separatedNortPart1);

		assertEquals(nm0.getOccurrences(), nm1.getOccurrences());

	}

	/**
	 * This method tests the functionality of adding a separated name or type (nort)
	 * if a similar ref is already contained by the ntr state. In this case, the
	 * mappings with the reference should be updated. The reference should stay,
	 * while the nodes should extend.
	 */
	@Test
	public void addSeparatedNortIfRefAlreadyContained() {

		INode separatedNortNode = separatedNortNodes.get(0);
		INode altSeparatedNortNode = altSeparatedNortNodes.get(0);

		ntrState.addNort(separatedNortNode, separatedNort, 0.5);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());

		int preNodesSize = ntrState.getNounMappingsByNode(separatedNortNode).get(0).getNodes().size();
		List<String> separatedNortParts = List.of(SimilarityUtils.splitAtSeparators(separatedNort).split(" "));

		List<NounMapping> nms0 = ntrState.getNounMappingsByNode(separatedNortNode);
		assertTrue(nms0.size() == 2);
		NounMapping nm00 = nms0.get(0);

		ntrState.addNort(altSeparatedNortNode, altSeparatedNort, 0.8);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());

		List<NounMapping> nms1 = ntrState.getNounMappingsByNode(altSeparatedNortNode);
		assertTrue(nms1.size() == 2);
		NounMapping nm10 = nms1.get(0);

		assertTrue(nm00.getReference().contentEquals(separatedNortParts.get(0)));
		assertEquals(nm10, nm00);
		assertNotEquals(nms1.get(1), nms0.get(1));
		assertTrue(nm00.getNodes().size() > preNodesSize);

	}

	/**
	 * Tests the functionality of adding a separated name or type (nort) if the
	 * occurrence is already contained by the ntr state. In this case the already
	 * existing mappings should be updated. The node to add should be added to the
	 * mapping.
	 */
	@Test
	public void addSeparatedNortIfOccAlreadyContained() {

		INode separatedNortNode0 = separatedNortNodes.get(0);
		INode separatedNortNode1 = separatedNortNodes.get(1);

		ntrState.addNort(separatedNortNode0, separatedNort, 0.5);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());

		int preNodesSize = ntrState.getNounMappingsByNode(separatedNortNode0).get(0).getNodes().size();
		List<String> separatedNortParts = List.of(SimilarityUtils.splitAtSeparators(separatedNort).split(" "));

		List<NounMapping> nms0 = ntrState.getNounMappingsByNode(separatedNortNode0);
		assertEquals(2, nms0.size());
		NounMapping nm00 = nms0.get(0);

		ntrState.addNort(separatedNortNode1, separatedNort, 0.8);
		assertFalse(ntrState.getNameOrTypeMappings().isEmpty());

		List<NounMapping> nms1 = ntrState.getNounMappingsByNode(separatedNortNode1);
		assertEquals(2, nms1.size());
		NounMapping nm10 = nms1.get(0);

		assertTrue(nm00.getReference().contentEquals(separatedNortParts.get(0)));
		assertEquals(nm00, nm10);
		assertEquals(nms1.get(1), nms0.get(1));
		assertTrue(nm00.getNodes().size() > preNodesSize);

	}

	/**
	 * This test secures the functionality of adding a relation to the ntr state. If
	 * a relation is not already contained it should be added. When already
	 * contained the state should not change. If a relation with already contained
	 * occurrences should be added the state should not change. If a relation with
	 * another instances are added they should be added, even if the instances are
	 * partially contained.
	 */
	@Test
	public void addRelation() {
		NounMapping nameMapping = nameMappings.get(0);
		NounMapping typeMapping = typeMappings.get(0);
		NounMapping nortMapping = nortMappings.get(0);

		assertEquals(0, ntrState.getRelations().size());
		RelationMapping rm = new RelationMapping(nortMapping, nameMapping, 0.5);

		ntrState.addRelation(rm);
		assertEquals(1, ntrState.getRelations().size());
		assertEquals(rm, ntrState.getRelations().get(0));

		ntrState.addRelation(rm);
		assertEquals(1, ntrState.getRelations().size());

		ntrState.addRelation(rm.getOccurrenceNodes().get(0), rm.getOccurrenceNodes().get(1), 0.6);
		assertEquals(1, ntrState.getRelations().size());

		ntrState.addRelation(nameMapping, typeMapping, 0.6);
		assertEquals(2, ntrState.getRelations().size());

		ntrState.addRelation(nameMapping, nortMapping, 0.6);
		assertEquals(3, ntrState.getRelations().size());

	}

	/**
	 * Tests if a relation can be removed from the ntr state.
	 */
	@Test
	public void removeRelation() {

		NounMapping nameMapping = nameMappings.get(0);
		NounMapping nortMapping = nortMappings.get(0);

		assertTrue(ntrState.getRelations().isEmpty());
		RelationMapping rm = new RelationMapping(nortMapping, nameMapping, 0.5);

		ntrState.addRelation(rm);
		assertFalse(ntrState.getRelations().isEmpty());

		ntrState.removeRelation(rm);
		assertTrue(ntrState.getRelations().isEmpty());

	}

}
