package modelconnector.modelExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the relations.
 *
 * @author Sophie
 *
 */
public class RelationTest {

	private static Instance i0;
	private static Instance i1;
	private static Instance i2;
	private String type = "in";

	/**
	 * Before creating relation the instances for it have to be created.
	 */
	@BeforeClass
	public static void init() {
		String longestType = "type otype";
		String longestName = "name oname";
		i0 = new Instance(longestName, longestType, 0);
		i1 = new Instance(longestName, longestType, 1);
		i2 = new Instance(longestName, longestType, 2);

	}

	/**
	 * Tests the creation of relations. After createion they should not be null. Two
	 * relations should equal, if they have the same attributes.
	 */
	@Test
	public void creation() {
		Relation r0 = new Relation(i0, i1, type, 0);
		Relation r1 = new Relation(i0, i1, type, 0);

		assertEquals(r0, r1);
		assertNotNull(r0);

		Relation r2 = new Relation(i0, i1, type, 1);
		assertNotEquals(r0, r2);
	}

	/**
	 * Tests the getters of a relation. By the getters the attributes (instances,
	 * type, and uid) should be returned.
	 */
	@Test
	public void getter() {

		Relation r0 = new Relation(i0, i1, type, 0);

		assertEquals(List.of(i0, i1), r0.getInstances());
		assertEquals(type, r0.getType());
		assertEquals(0, r0.getUid());
	}

	/**
	 * Test the functionality of adding more instances to an existing relation. If
	 * instances are added, that are already contained the add should not be
	 * executed. Otherwise the instances should be extended
	 */
	@Test
	public void addOtherInstances() {

		Relation r0 = new Relation(i0, i1, type, 0);

		assertTrue(r0.getInstances().containsAll(List.of(i0, i1)));
		assertEquals(2, r0.getInstances().size());

		r0.addOtherInstances(List.of(i0));

		assertTrue(r0.getInstances().containsAll(List.of(i0, i1)));
		assertEquals(2, r0.getInstances().size());

		r0.addOtherInstances(List.of(i2));

		assertTrue(r0.getInstances().containsAll(List.of(i0, i1, i2)));
		assertEquals(3, r0.getInstances().size());
	}

}
