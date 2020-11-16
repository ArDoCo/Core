package modelconnector;

import edu.kit.ipd.parse.luna.graph.INode;

/**
 * Parent class of all analyzers. Analyzer are executed on a graph node. They
 * are able to get the current state of the textual analysis. Analyzers owe a
 * type that classifies them.
 *
 * @author Sophie
 *
 */
public abstract class Analyzer {

	protected DependencyType type;

	/**
	 * An analyzer is executed on the current node.
	 *
	 * @param node the current node
	 */
	public void exec(INode node) {
	}

	/**
	 * Creates a new analyzer of the specified type.
	 *
	 * @param type the analyzer type
	 */
	public Analyzer(DependencyType type) {
		this.type = type;
	}

	/**
	 * Returns the dependency type of the current analyzer.
	 *
	 * @return the dependency type of the current analyzer
	 */
	public DependencyType getDependencyType() {
		return this.type;
	}

}
