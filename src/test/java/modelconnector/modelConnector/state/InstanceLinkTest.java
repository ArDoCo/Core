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
import modelconnector.connectionGenerator.state.InstanceLink;
import modelconnector.helpers.GraphUtils;
import modelconnector.modelExtractor.state.Instance;
import modelconnector.recommendationGenerator.state.RecommendedInstance;
import modelconnector.textExtractor.state.NounMapping;

/**
 * This class tests the instance link class.
 *
 * @author Sophie
 *
 */
public class InstanceLinkTest {

	private static String type = "component";
	private static String type2 = "comp";
	private static String name = "test.driver";
	private static String name2 = "test diver";
	private static RecommendedInstance ri0;
	private static RecommendedInstance ri1;
	private static Instance i0;
	private static Instance i1;

	/**
	 * Before instance links can be tested some recommended instances, as well as
	 * instances have to be created.
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
		}
		ri0 = new RecommendedInstance(name, type, 0.5, nameMappings, typeMappings);
		ri1 = new RecommendedInstance(name2, type2, 0.5, nameMappings, typeMappings);

		i0 = new Instance(name, type, 0);
		i1 = new Instance(name2, type2, 1);

	}

	/**
	 * Tests the creation of an instance link. An instance link is successfully
	 * created if it's not null. It is equal to another instance link if, and only
	 * if both, the recommended instance and the instance are equal.
	 */
	@Test
	public void creation() {
		InstanceLink il0 = new InstanceLink(ri0, i0, 0.5);

		assertNotNull(il0);

		InstanceLink il1 = new InstanceLink(ri0, i0, 0.6);
		assertEquals(il0, il1);

		InstanceLink il2 = new InstanceLink(ri1, i0, 0.5);
		assertNotEquals(il0, il2);

		InstanceLink il3 = new InstanceLink(ri0, i1, 0.5);
		assertNotEquals(il0, il3);
	}

	/**
	 * Tests the getters of an instance link. The getters should return the
	 * attributes of an instance link. These are the instance, the recommended
	 * instance and the probability.
	 */
	@Test
	public void getter() {
		InstanceLink il = new InstanceLink(ri0, i0, 0.5);

		assertEquals(i0, il.getModelInstance());
		assertEquals(ri0, il.getTextualInstance());
		assertEquals(0.5, il.getProbability(), 0.001);
	}

	/**
	 * Tests the setters of an instance link. The setter for the probability should
	 * set the probability to a given value.
	 */
	@Test
	public void setter() {
		InstanceLink il = new InstanceLink(ri0, i0, 0.5);

		il.setProbability(0.8);
		assertEquals(0.8, il.getProbability(), 0.001);
	}

}
