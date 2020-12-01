package modelconnector.modelExtractor.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

/**
 * This class test the instance class.
 *
 * @author Sophie
 *
 */
public class InstanceTest {

	private List<String> names = List.of("name", "oname", "name oname");
	private List<String> types = List.of("type", "otype", "type otype");
	private String longestType = "type otype";
	private String longestName = "name oname";

	/**
	 * This test secures that the created instances are not null and equal, if they
	 * are created with the same parameters. If the name or type or uid is
	 * different, they should not equal.
	 */
	@Test
	public void creation() {

		Instance i0 = new Instance(longestName, longestType, 0);
		Instance i1 = new Instance(longestName, longestType, 0);

		assertNotNull(i0);
		assertEquals(i0, i1);

		Instance i2 = new Instance(names.get(0), longestType, 0);
		assertNotEquals(i0, i2);

		Instance i3 = new Instance(longestName, types.get(0), 0);
		assertNotEquals(i0, i3);

		Instance i4 = new Instance(longestName, longestType, 2);
		assertNotEquals(i0, i4);
	}

	/**
	 * Tests the getter of an instance. They should return the attributes of the
	 * created instance.
	 */
	@Test
	public void getter() {
		Instance i0 = new Instance(longestName, longestType, 0);

		assertEquals(longestName, i0.getLongestName());
		assertEquals(longestType, i0.getLongestType());

		assertEquals(names, i0.getNames());
		assertEquals(types, i0.getTypes());
		assertEquals(0, i0.getUid());
	}

}
