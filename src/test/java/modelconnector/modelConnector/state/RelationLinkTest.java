package modelconnector.modelConnector.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.UtilsForTesting;
import modelconnector.connectionGenerator.state.RelationLink;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.modelExtractor.state.Relation;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.recommendationGenerator.state.RecommendedRelation;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class tests the relation links.
 *
 * @author Sophie
 *
 */
public class RelationLinkTest {

	private static String type = "component";
	private static String type2 = "comp";
	private static String name = "test.driver";
	private static String name2 = "test diver";
	private static RecommendedRelation rrl0;
	private static RecommendedRelation rrl1;
	private static Relation r0;
	private static Relation r1;

	/**
	 * Initializes the tests before the class by creating recommended relations and
	 * relations to use in the tests.
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

		RecommendedInstance ri0 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		RecommendedInstance ri1 = new RecommendedInstance(name2, type, 0.5, nameMappings, typeMappings);
		RecommendedInstance ri2 = new RecommendedInstance(name, type2, 0.5, nameMappings, typeMappings);

		rrl0 = new RecommendedRelation("in", ri1, ri0, List.of(ri2), 0.5, relationNodes);
		rrl1 = new RecommendedRelation("in", ri1, ri0, List.of(), 0.5, relationNodes);

		Instance i0 = new Instance(name, type, 0);
		Instance i1 = new Instance(name2, type2, 1);

		r0 = new Relation(i0, i1, "in", 0);
		r1 = new Relation(i0, i1, "provide", 1);

	}

	/**
	 * Tests the creation of a relation link. It is successfully created if it's not
	 * null. Its equality is dependent on its recommended relation and relation.
	 */
	@Test
	public void creation() {
		RelationLink rl0 = new RelationLink(rrl0, r0, 0.5);
		assertNotNull(rl0);

		RelationLink rl1 = new RelationLink(rrl0, r0, 0.6);
		assertEquals(rl0, rl1);

		RelationLink rl2 = new RelationLink(rrl1, r0, 0.5);
		assertNotEquals(rl0, rl2);

		RelationLink rl3 = new RelationLink(rrl0, r1, 0.5);
		assertNotEquals(rl0, rl3);
	}

	/**
	 * Tests the getters of a relation link. The getters should return the
	 * attributes probability, relation, and recommended relation of the relation
	 * link.
	 */
	@Test
	public void getter() {

		RelationLink rl = new RelationLink(rrl0, r0, 0.5);

		assertEquals(0.5, rl.getProbability(), 0.001);
		assertEquals(r0, rl.getModelRelation());
		assertEquals(rrl0, rl.getTextualRelation());
	}

	/**
	 * Tests the setters of a relation link. If the probability is set to a new
	 * value it should update.
	 */
	@Test
	public void setter() {
		RelationLink rl = new RelationLink(rrl0, r0, 0.5);
		rl.setProbability(0.8);
		assertEquals(0.8, rl.getProbability(), 0.001);
	}

}
