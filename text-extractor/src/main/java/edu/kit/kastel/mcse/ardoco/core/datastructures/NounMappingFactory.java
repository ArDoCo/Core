package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.MappingKind;

public final class NounMappingFactory {

	private NounMappingFactory() {
		throw new IllegalAccessError();
	}

	/**
	 * Creates a mapping dependent on the kind and a single node
	 *
	 * @param n           node for the mapping
	 * @param probability probability of being a mapping of the kind
	 * @param kind        the kind
	 * @param reference   the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created mapping
	 */
	public static INounMapping createMappingTypeNode(IWord n, String reference, MappingKind kind, double probability, List<String> occurrences) {
		return switch (kind) {
		case NAME -> createNameNode(List.of(n), probability, reference, occurrences);
		case TYPE -> createTypeNode(List.of(n), probability, reference, occurrences);
		default -> createNortNode(List.of(n), probability, reference, occurrences);
		};
	}

	/**
	 * Creates a name mapping
	 *
	 * @param nodes       nodes for the mapping
	 * @param probability probability of being a name mapping
	 * @param name        the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created name mapping
	 */
	public static INounMapping createNameNode(List<IWord> nodes, double probability, String name, List<String> occurrences) {
		return new NounMappingWithoutDistribution(nodes, probability, MappingKind.NAME, name, occurrences);
	}

	/**
	 * Creates a name mapping, based on a single node
	 *
	 * @param node        node for the mapping
	 * @param probability probability of being a name mapping
	 * @param type        the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created name mapping
	 */
	public static INounMapping createNameMapping(IWord node, double probability, String type, List<String> occurrences) {
		return new NounMappingWithoutDistribution(List.of(node), probability, MappingKind.NAME, type, occurrences);
	}

	/**
	 * Creates a type mapping
	 *
	 * @param node        node for the mapping
	 * @param probability probability of being a type mapping
	 * @param type        the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created type mapping
	 */
	public static INounMapping createTypeMapping(IWord node, double probability, String type, List<String> occurrences) {
		return new NounMappingWithoutDistribution(List.of(node), probability, MappingKind.TYPE, type, occurrences);
	}

	/**
	 * Creates a type mapping, based on a single node
	 *
	 * @param nodes       nodes for the mapping
	 * @param probability probability of being a type mapping
	 * @param type        the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created type mapping
	 */
	public static INounMapping createTypeNode(List<IWord> nodes, double probability, String type, List<String> occurrences) {
		return new NounMappingWithoutDistribution(nodes, probability, MappingKind.TYPE, type, occurrences);
	}

	/**
	 * Creates a name or type mapping, based on a single node
	 *
	 * @param node        node for the mapping
	 * @param probability probability of being a name or type mapping
	 * @param type        the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created name or type mapping
	 */
	public static INounMapping createNortMapping(IWord node, double probability, String type, List<String> occurrences) {
		return new NounMappingWithoutDistribution(List.of(node), probability, MappingKind.NAME_OR_TYPE, type, occurrences);
	}

	/**
	 * Creates a name or type mapping
	 *
	 * @param nodes       nodes for the mapping
	 * @param probability probability of being a name or type mapping
	 * @param ref         the reference for this mapping
	 * @param occurrences the appearances of the mapping
	 * @return the created name or type mapping
	 */
	public static INounMapping createNortNode(List<IWord> nodes, double probability, String ref, List<String> occurrences) {
		return new NounMappingWithoutDistribution(nodes, probability, MappingKind.NAME_OR_TYPE, ref, occurrences);
	}

}
