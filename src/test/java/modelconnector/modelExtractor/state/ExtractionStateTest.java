package modelconnector.modelExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the extraction state.
 *
 * @author Sophie
 *
 */
public class ExtractionStateTest {

	private List<String> names = List.of("name", "oname", "name oname");
	private List<String> types = List.of("type", "otype", "type otype");
	private static String longestType = "type otype";
	private static String typi = "typi";
	private static String nami = "nami";
	private static Instance i0;
	private static Instance i1;
	private static Relation r0;
	private static Relation r1;
	private static String rtype0 = "in";
	private static String rtype1 = "provide";

	/**
	 * Before the extraction state can be tested some instances and relations have
	 * to be created.
	 */
	@BeforeClass
	public static void init() {
		String longestName = "name oname";
		i0 = new Instance(longestName, longestType, 0);
		i1 = new Instance(nami, typi, 1);
		r0 = new Relation(i0, i1, rtype0, 0);
		r1 = new Relation(i0, i1, rtype1, 1);
	}

	/**
	 * Tests the creation of an extraction state. It is created successfully if it's
	 * not null.
	 */
	@Test
	public void creation() {
		ModelExtractionState state = new ModelExtractionState(List.of(i0, i1), List.of(r0));

		assertNotNull(state);
	}

	/**
	 * Tests the getter of an extraction state. If the extraction state is created
	 * with some instances and relations their attributes (names, instance types,
	 * relation types, relations) should be returned by the getters. The relations
	 * of a specific type should be returned by getRelationsOfType(...).
	 */
	@Test
	public void getter() {
		ModelExtractionState state = new ModelExtractionState(List.of(i0, i1), List.of(r0, r1));

		assertEquals(List.of(i0, i1), state.getInstances());
		assertEquals(List.of(i0), state.getInstancesOfType(longestType));
		Set<String> currentNames = new HashSet<>();
		currentNames.addAll(names);
		currentNames.add(nami);

		Set<String> currentInstanceTypes = new HashSet<>();
		currentInstanceTypes.addAll(types);
		currentInstanceTypes.add(typi);

		Set<String> currentRelationTypes = new HashSet<>();
		currentRelationTypes.add(r0.getType());
		currentRelationTypes.add(r1.getType());

		assertEquals(currentNames, state.getNames());
		assertEquals(currentInstanceTypes, state.getInstanceTypes());
		assertEquals(currentRelationTypes, state.getRelationTypes());
		assertEquals(List.of(r0, r1), state.getRelations());
		assertEquals(List.of(r0), state.getRelationsOfType(r0.getType()));
	}
}
