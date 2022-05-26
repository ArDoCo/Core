/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.informalin.framework.configuration.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * The Interface ITextState.
 */
public interface ITextState extends ICopyable<ITextState>, IConfigurable {

	/**
	 * Minimum difference that need to shall not be reached to identify a
	 * NounMapping as NameOrType.
	 * 
	 * @see #getMappingsThatCouldBeNameOrType(IWord)
	 * @see #isWordContainedByNameOrTypeMapping(IWord)
	 */
	double MAPPINGKIND_MAX_DIFF = 0.1;

	/**
	 * * Adds a name mapping to the state.
	 *
	 * @param n           node of the mapping
	 * @param probability probability to be a name mapping
	 * @param occurrences list of the appearances of the mapping
	 */
	void addName(IWord n, IClaimant claimant, double probability, ImmutableList<String> occurrences);

	/**
	 * * Adds a name mapping to the state.
	 *
	 * @param word        word of the mapping
	 * @param probability probability to be a name mapping
	 */
	void addName(IWord word, IClaimant claimant, double probability);

	/**
	 * * Adds a type mapping to the state.
	 *
	 * @param word        node of the mapping
	 * @param probability probability to be a type mapping
	 */
	void addType(IWord word, IClaimant claimant, double probability);

	/**
	 * * Adds a type mapping to the state.
	 *
	 * @param word        node of the mapping
	 * @param probability probability to be a type mapping
	 * @param occurrences list of the appearances of the mapping
	 */
	void addType(IWord word, IClaimant claimant, double probability, ImmutableList<String> occurrences);

	/**
	 * Creates a new relation mapping and adds it to the state. More end points, as
	 * well as a preposition can be added afterwards.
	 *
	 * @param node1       first relation end point
	 * @param node2       second relation end point
	 * @param probability probability of being a relation
	 * @return the added relation mapping
	 */
	IRelationMapping addRelation(INounMapping node1, INounMapping node2, IClaimant claimant, double probability);

	// --- remove section --->

	/**
	 * Removes a noun mapping from the state.
	 *
	 * @param n noun mapping to remove
	 */
	void removeNounMapping(INounMapping n);

	/**
	 * Returns all mappings containing the given node.
	 *
	 * @param n the given node
	 * @return all mappings containing the given node as list
	 */
	ImmutableList<INounMapping> getNounMappingsByWord(IWord n);

	/**
	 * Returns a list of all references of name mappings.
	 *
	 * @return all references of name mappings as list.
	 */
	ImmutableList<String> getNameList();

	/**
	 * Returns a list of all references of type mappings.
	 *
	 * @return all references of type mappings as list.
	 */
	ImmutableList<String> getTypeList();

	/**
	 * Returns all type mappings containing the given node.
	 *
	 * @param word node to filter for
	 * @return a list of alltype mappings containing the given node
	 */
	ImmutableList<INounMapping> getTypeMappingsByWord(IWord word);

	/**
	 * Returns if a node is contained by the name mappings.
	 *
	 * @param node        node to check
	 * @param mappingKind mappingKind to check
	 * @return true if the node is contained by name mappings.
	 */
	boolean isWordContainedByMappingKind(IWord node, MappingKind mappingKind);

	/**
	 * Returns if a node is contained by the mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by mappings.
	 */
	boolean isWordContainedByNounMappings(IWord node);

	/**
	 * Gets the all noun mappings.
	 *
	 * @return the all mappings
	 */
	ImmutableList<INounMapping> getNounMappings();

	/**
	 * Adds the noun mapping.
	 *
	 * @param nounMapping the noun mapping.
	 */
	void addNounMapping(INounMapping nounMapping, IClaimant claimant);

	/**
	 * Gets the mappings that could be A type.
	 *
	 * @param word        the word
	 * @param mappingKind the mapping Kind that
	 * @return the mappings that could be A type
	 */
	default ImmutableList<INounMapping> getMappingsThatCouldBeOfKind(IWord word, MappingKind mappingKind) {
		return getNounMappingsByWord(word).select(mapping -> mapping.getProbabilityForKind(mappingKind) > 0);
	}

	/**
	 * Returns all mappings with a similar reference as given.
	 *
	 * @param ref the reference to search for
	 * @return a list of noun mappings with the given reference.
	 */
	ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref);

	/**
	 * Gets the mappings that could be a Name or Type.
	 *
	 * @param word  the word
	 * @param kinds the required mappingKinds
	 * @return the mappings that could be a Name or Type
	 */
	default ImmutableList<INounMapping> getMappingsThatCouldBeMultipleKinds(IWord word, List<MappingKind> kinds) {

		if (kinds.size() < 2) {
			return getNounMappingsOfKind(kinds.get(0));
		}

		return getNounMappingsByWord(word).select(n -> {

			Stream<Double> probabilities = kinds.stream().map(n::getProbabilityForKind);
			return probabilities.noneMatch(d -> d <= 0.0)
					&& probabilities.allMatch(a -> probabilities.allMatch(b -> Math.abs(a - b) < MAPPINGKIND_MAX_DIFF));
		});
	}

	ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind mappingKind);

}
