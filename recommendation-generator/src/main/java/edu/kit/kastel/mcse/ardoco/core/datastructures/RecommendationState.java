package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.common.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

/**
 * The recommendation state encapsulates all recommended instances and
 * relations. These recommendations should be contained by the model by their
 * probability.
 *
 * @author Sophie
 *
 */
public class RecommendationState implements IRecommendationState {

	private List<IRecommendedInstance> recommendedInstances;
	private List<IRecommendedRelation> recommendedRelations;

	private int calledAddedInstances = 0;
	private int calledAddedRelations = 0;
	private int addedInstances = 0;
	private int addedRelations = 0;

	@Override
	public IRecommendationState createCopy() {
		RecommendationState recommendationState = new RecommendationState();
		recommendationState.recommendedInstances = recommendedInstances.stream().map(IRecommendedInstance::createCopy).collect(Collectors.toList());
		recommendationState.recommendedRelations = recommendedRelations.stream().map(IRecommendedRelation::createCopy).collect(Collectors.toList());
		return recommendationState;
	}

	/**
	 * Creates a new recommendation state.
	 */
	public RecommendationState() {
		recommendedInstances = new ArrayList<>();
		recommendedRelations = new ArrayList<>();
	}

	/**
	 * Returns all recommended instances.
	 *
	 * @return all recommended instances as list
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstances() {
		return new ArrayList<>(recommendedInstances);
	}

	/**
	 * Returns all recommended relations.
	 *
	 * @return all recommended relations as list
	 */
	@Override
	public List<IRecommendedRelation> getRecommendedRelations() {
		return recommendedRelations;
	}

	/**
	 * Adds a new recommended relation.
	 *
	 * @param name           name of that recommended relation
	 * @param probability    probability of being in the model
	 * @param ri1            first end point of the relation as recommended instance
	 * @param ri2            second end point of the relation as recommended
	 *                       instance
	 * @param otherInstances other involved recommended instances
	 * @param occurrences    nodes representing the relation
	 */
	@Override
	public void addRecommendedRelation(String name, IRecommendedInstance ri1, IRecommendedInstance ri2, List<IRecommendedInstance> otherInstances,
			double probability, List<IWord> occurrences) {

		IRecommendedRelation rrel = new RecommendedRelation(name, ri1, ri2, otherInstances, probability, occurrences);
		List<IRecommendedInstance> ris = new ArrayList<>(List.of(ri1, ri2));
		ris.addAll(otherInstances);
		Optional<IRecommendedRelation> recr = recommendedRelations.stream().filter(//
				r -> r.getRelationInstances().containsAll(ris) && ris.containsAll(r.getRelationInstances())).findAny();

		if (recr.isPresent()) {
			updateRecommendedRelation(recr.get(), occurrences, probability);
		} else {
			recommendedRelations.add(rrel);
		}
	}

	private void updateRecommendedRelation(IRecommendedRelation recr, List<IWord> occurrences, double probability) {
		recr.addOccurrences(occurrences);
		recr.updateProbability(probability);
	}

	/**
	 * Adds a recommended instance without a type.
	 *
	 * @param name         name of that recommended instance
	 * @param probability  probability of being in the model
	 * @param nameMappings name mappings representing that recommended instance
	 */
	@Override
	public void addRecommendedInstanceJustName(String name, double probability, List<INounMapping> nameMappings) {

		this.addRecommendedInstance(name, "", probability, nameMappings, new ArrayList<>());

	}

	/**
	 * Adds a recommended instance.
	 *
	 * @param name         name of that recommended instance
	 * @param type         type of that recommended instance
	 * @param probability  probability of being in the model
	 * @param nameMappings name mappings representing the name of the recommended
	 *                     instance
	 * @param typeMappings type mappings representing the type of the recommended
	 *                     instance
	 * @return the added recommended instance
	 */
	@Override
	public IRecommendedInstance addRecommendedInstance(String name, String type, double probability, List<INounMapping> nameMappings,
			List<INounMapping> typeMappings) {

		RecommendedInstance ri = new RecommendedInstance(name, type, probability, new ArrayList<>(new HashSet<>(nameMappings)),
				new ArrayList<>(new HashSet<>(typeMappings)));
		this.addRecommendedInstance(ri);

		return ri;
	}

	/**
	 * Adds a recommended instance to the state. If the in the stored instance an
	 * instance with the same name and type is contained it is extended. If an rec.
	 * istance with the same name can be found it is extended. Elsewhere a new
	 * recommended instance is created.
	 *
	 * @param ri
	 */
	private void addRecommendedInstance(IRecommendedInstance ri) {

		List<IRecommendedInstance> risWithExactName = recommendedInstances.stream().filter(r -> r.getName().contentEquals(ri.getName()))
				.collect(Collectors.toList());
		List<IRecommendedInstance> risWithExactNameAndType = risWithExactName.stream().filter(r -> r.getType().contentEquals(ri.getType()))
				.collect(Collectors.toList());

		if (recommendedInstances.contains(ri)) {
			return;
		}

		if (risWithExactNameAndType.isEmpty()) {
			processRecommendedInstancesWithNoExactNameAndType(ri, risWithExactName);
		} else {
			risWithExactNameAndType.get(0).addMappings(ri.getNameMappings(), ri.getTypeMappings());

		}

	}

	private void processRecommendedInstancesWithNoExactNameAndType(IRecommendedInstance ri, List<IRecommendedInstance> risWithExactName) {
		if (risWithExactName.isEmpty()) {
			recommendedInstances.add(ri);
		} else {

			boolean added = false;

			for (IRecommendedInstance riWithExactName : risWithExactName) {

				boolean areWordsSimilar = SimilarityUtils.areWordsSimilar(riWithExactName.getType(), ri.getType(), 0.85);
				if (areWordsSimilar || recommendedInstancesHasEmptyType(ri, riWithExactName)) {
					riWithExactName.addMappings(ri.getNameMappings(), ri.getTypeMappings());
					added = true;
					break;
				}
			}

			if (!added && !ri.getType().contentEquals("")) {
				recommendedInstances.add(ri);
			}
		}
	}

	private boolean recommendedInstancesHasEmptyType(IRecommendedInstance ri, IRecommendedInstance riWithExactName) {
		return riWithExactName.getType().contentEquals("") && !ri.getType().contentEquals("");
	}

	/**
	 * Returns all recommended instances that contain a given mapping as type.
	 *
	 * @param mapping given mapping to search for in types
	 * @return the list of recommended instances with the mapping as type.
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstancesByTypeMapping(INounMapping mapping) {
		return recommendedInstances.stream().filter(sinstance -> sinstance.getTypeMappings().contains(mapping)).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a given mapping.
	 *
	 * @param mapping given mapping to search for
	 * @return the list of recommended instances with the mapping.
	 */
	@Override
	public List<IRecommendedInstance> getAnyRecommendedInstancesByMapping(INounMapping mapping) {
		return recommendedInstances.stream().filter(sinstance -> sinstance.getTypeMappings().contains(mapping) || sinstance.getNameMappings().contains(mapping))
				.collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a given name.
	 *
	 * @param name given name to search for in names
	 * @return the list of recommended instances with that name.
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstancesByName(String name) {
		return recommendedInstances.stream().filter(ri -> ri.getName().toLowerCase().contentEquals(name.toLowerCase())).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a similar name.
	 *
	 * @param name given name to search for in names
	 * @return the list of recommended instances with a similar name.
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstancesBySimilarName(String name) {
		List<IRecommendedInstance> ris = new ArrayList<>();
		for (IRecommendedInstance ri : recommendedInstances) {
			if (SimilarityUtils.areWordsSimilar(ri.getName(), name)) {
				ris.add(ri);
			}
		}

		return ris;
	}

	/**
	 * Returns all recommended instances that contain a given name and type.
	 *
	 * @param type given type to search for in types
	 * @return the list of recommended instances with that name and type
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstancesByType(String type) {
		return recommendedInstances.stream().filter(ri -> ri.getType().toLowerCase().contentEquals(type.toLowerCase())).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a similar type.
	 *
	 * @param type given type to search for in types
	 * @return the list of recommended instances with a similar type.
	 */
	@Override
	public List<IRecommendedInstance> getRecommendedInstancesBySimilarType(String type) {
		return recommendedInstances.stream().filter(ri -> SimilarityUtils.areWordsSimilar(ri.getType(), type)).collect(Collectors.toList());
	}

}
