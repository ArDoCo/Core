package modelconnector;

/**
 * A solver works on the base of its knowledge (its holding states).
 *
 * @author Sophie
 *
 */
public abstract class Solver {

	protected DependencyType type;

	/**
	 * An analyzer can be exectued without additional arguments
	 *
	 */
	public void exec() {
	}

	/**
	 * Creates a new analyzer of the specified type.
	 *
	 * @param type the analyzer type
	 */
	public Solver(DependencyType type) {
		this.type = type;
	}

	/**
	 * Returns the dependency type of the current solver.
	 *
	 * @return the dependency type of the current solver
	 */
	public DependencyType getDependencyType() {
		return this.type;
	}
}
