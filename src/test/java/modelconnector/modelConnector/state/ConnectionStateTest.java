package modelconnector.modelConnector.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.connectionGenerator.state.ConnectionState;
import modelconnector.connectionGenerator.state.InstanceLink;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class tests the connection state.
 *
 * @author Sophie
 *
 */
public class ConnectionStateTest {

	private static String type = "component";
	private static String type2 = "comp";
	private static String name = "test.driver";
	private static String name2 = "test diver";
	private static RecommendedInstance ri0;
	private static RecommendedInstance ri1;
	private static RecommendedInstance ri2;
	private static RecommendedRelation rrl0;
	private static RecommendedRelation rrl1;
	private static Instance i0;
	private static Instance i1;
	private static Relation r0;
	private static Relation r1;
	private static InstanceLink il0;
	private static InstanceLink il1;
	private static RelationLink rl0;
	private static RelationLink rl1;

	/**
	 * To be able to test the connection state recommended instances, instances,
	 * relations, recommended relations, instance links and relation links have to
	 * be created.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {

		String content = "" + //
				"The architecture contains ui component, Logic component, storage component, common component, "//
				+ "test driver component, e2e component, client component. The common component is in style of a beautiful component architecture. "//
				+ "The test.driver is a very important component. The logic.api, test.driver, and test.epic components are important, too.";

		IGraph graph = UtilsForTesting.getGraph(content);

		List<NounMapping> typeMappings = new ArrayList<>();
		List<NounMapping> nameMappings = new ArrayList<>();

		List<INode> relationNodes = new ArrayList<>();

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
		ri1 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		ri2 = new RecommendedInstance(name2, type2, 0.5, nameMappings, typeMappings);

		rrl0 = new RecommendedRelation("in", ri1, ri0, List.of(ri2), 0.5, List.of(relationNodes.get(0)));
		rrl1 = new RecommendedRelation("in", ri1, ri0, List.of(), 0.5, relationNodes);

		i0 = new Instance(name, type, 0);
		i1 = new Instance(name2, type2, 1);

		r0 = new Relation(i0, i1, "in", 0);
		r1 = new Relation(i0, i1, "provide", 1);

		il0 = new InstanceLink(ri0, i0, 0.5);
		il1 = new InstanceLink(ri1, i1, 0.5);

		rl0 = new RelationLink(rrl0, r0, 0.5);
		rl1 = new RelationLink(rrl1, r1, 0.5);
	}

	/**
	 * Tests the creation of a connection state. The state is created successfully
	 * if it's not null.
	 */
	@Test
	public void creation() {

		ConnectionState state = new ConnectionState();
		assertNotNull(state);
	}

	/**
	 * Test the add functionality for instance links. If an instance link is added,
	 * that is not already contained in the state, the add is executed. If an
	 * instance link is added that refers to the same instance The recommended
	 * instance of the existing instance link is extended by the mappings of the
	 * recommended instance of the instance link to add. An instance link can be
	 * added by transferring the parameters, but as an instance link, too.
	 */
	@Test
	public void addtoInstanceLinks() {
		ConnectionState state = new ConnectionState();

		state.addToLinks(ri0, i0, 0.5);

		List<InstanceLink> currentInstanceLinks = state.getInstanceLinks();
		RecommendedInstance ri = currentInstanceLinks.get(0).getTextualInstance();

		assertTrue(state.getInstanceLinks().contains(il0));
		assertTrue(state.getInstanceLinks().size() == 1);
		assertTrue(ri.getNameMappings().containsAll(ri0.getNameMappings()));
		assertTrue(ri.getNameMappings().size() == ri0.getNameMappings().size());
		assertTrue(ri.getTypeMappings().containsAll(ri0.getTypeMappings()));
		assertTrue(ri.getTypeMappings().size() == ri0.getTypeMappings().size());

		state.addToLinks(ri0, i0, 0.5);
		currentInstanceLinks = state.getInstanceLinks();
		assertTrue(currentInstanceLinks.contains(il0));
		assertTrue(currentInstanceLinks.size() == 1);

		state.addToLinks(ri1, i0, 0.5);
		currentInstanceLinks = state.getInstanceLinks();
		ri = currentInstanceLinks.get(0).getTextualInstance();
		assertTrue(currentInstanceLinks.contains(il0));
		assertTrue(currentInstanceLinks.size() == 1);
		assertTrue(ri.getNameMappings().containsAll(ri1.getNameMappings()));
		assertTrue(ri.getNameMappings().size() == ri1.getNameMappings().size());
		assertTrue(ri.getTypeMappings().containsAll(ri1.getTypeMappings()));
		assertTrue(ri.getTypeMappings().size() == ri1.getTypeMappings().size());

		state.addToLinks(il1);
		currentInstanceLinks = state.getInstanceLinks();
		assertTrue(currentInstanceLinks.containsAll(List.of(il1, il0)));
		assertTrue(currentInstanceLinks.size() == 2);

		state.addToLinks(il1);
		currentInstanceLinks = state.getInstanceLinks();
		assertTrue(currentInstanceLinks.containsAll(List.of(il1, il0)));
		assertTrue(currentInstanceLinks.size() == 2);
	}

	/**
	 * Test the add relation link functionality of the connection state. If the
	 * relation links is not already contained it is added. Both ways of adding are
	 * tested: With transferring parameters or an already created relation link.
	 */
	@Test
	public void addToRelationLinks() {

		ConnectionState state = new ConnectionState();

		state.addToLinks(rrl0, r0, 0.5);
		List<RelationLink> currentRelationLinks = state.getRelationLinks();
		assertTrue(currentRelationLinks.contains(rl0));
		assertTrue(currentRelationLinks.size() == 1);
		assertTrue(currentRelationLinks.get(0).getTextualRelation().getNodes().containsAll(rrl0.getNodes()));
		assertTrue(currentRelationLinks.get(0).getTextualRelation().getNodes().size() == rrl0.getNodes().size());

		state.addToLinks(rrl0, r0, 0.5);
		currentRelationLinks = state.getRelationLinks();
		assertTrue(currentRelationLinks.contains(rl0));
		assertTrue(currentRelationLinks.size() == 1);

		state.addToLinks(rl1);
		currentRelationLinks = state.getRelationLinks();
		assertTrue(currentRelationLinks.containsAll(List.of(rl1, rl0)));
		assertTrue(currentRelationLinks.size() == 2);
		assertTrue(currentRelationLinks.get(0).getTextualRelation().getNodes().containsAll(rrl1.getNodes()));
		assertTrue(currentRelationLinks.get(0).getTextualRelation().getNodes().size() == rrl1.getNodes().size());

		state.addToLinks(rl1);
		currentRelationLinks = state.getRelationLinks();
		assertTrue(currentRelationLinks.containsAll(List.of(rl1, rl0)));
		assertTrue(currentRelationLinks.size() == 2);
	}

	/**
	 * Tests the getters of the connection state. The getters should return the
	 * stored values of the connection state. This test includes getters for
	 * instance links with given name and/or type and relation links.
	 */
	@Test
	public void getter() {
		ConnectionState state = new ConnectionState();

		state.addToLinks(il0);
		state.addToLinks(il1);
		state.addToLinks(rl0);
		state.addToLinks(rl1);

		assertEquals(state.getInstanceLinks(), List.of(il1, il0));
		assertEquals(state.getInstanceLinks(name, type), List.of(il0));
		assertEquals(state.getInstanceLinksByType(type), List.of(il0));
		assertEquals(state.getInstanceLinksByName(name), List.of(il0));
		assertEquals(state.getRelationLinks(), List.of(rl1, rl0));
	}

	/**
	 * Tests the is contained functionality for instance and relation links in the
	 * connection state. It should return true if the link is contained and false if
	 * it's not.
	 */
	@Test
	public void isContained() {
		ConnectionState state = new ConnectionState();

		state.addToLinks(il0);
		state.addToLinks(rl0);

		assertTrue(state.isContainedByInstanceLinks(il0));
		assertFalse(state.isContainedByInstanceLinks(il1));
		assertTrue(state.isContainedByRelationLinks(rl0));
		assertFalse(state.isContainedByRelationLinks(rl1));
	}

	/**
	 * Tests the removal of instance and relation links of the connection state.
	 * After removeAllInstanceLinksWith(..) all links with this link should be
	 * removed. After removeAllMappingsWith(..) all relation links with this mapping
	 * should be removed. After removing a link from the connection state mappings,
	 * the given link should be removed.
	 */
	@Test
	public void remove() {
		ConnectionState state = new ConnectionState();

		state.addToLinks(il0);
		state.addToLinks(il1);
		state.addToLinks(rl0);
		state.addToLinks(rl1);

		state.removeAllInstanceLinksWith(i0);
		assertEquals(state.getInstanceLinks(), List.of(il1));

		state.removeAllInstanceLinksWith(ri0);
		assertEquals(state.getInstanceLinks(), List.of());

		state.removeAllMappingsWith(r0);
		assertEquals(state.getRelationLinks(), List.of(rl1));

		state.removeAllMappingsWith(rrl1);
		assertEquals(state.getRelationLinks(), List.of());

		state.addToLinks(il0);
		state.addToLinks(il1);
		state.addToLinks(rl0);
		state.addToLinks(rl1);

		state.removeFromMappings(il0);
		assertEquals(state.getInstanceLinks(), List.of(il1));

		state.removeFromMappings(rl0);
		assertEquals(state.getRelationLinks(), List.of(rl1));

	}

}
