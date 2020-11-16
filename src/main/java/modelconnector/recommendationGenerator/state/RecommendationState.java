package modelconnector.recommendationGenerator.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.helpers.SimilarityUtils;
import modelconnector.textExtractor.state.NounMapping;

/**
 * The recommendation state encapsulates all recommended instances and
 * relations. These recommendations should be contained by the model by their
 * probability.
 *
 * @author Sophie
 *
 */
public class RecommendationState {

	private List<RecommendedInstance> recommendedInstances;
	private List<RecommendedRelation> recommendedRelations;

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
	public List<RecommendedInstance> getRecommendedInstances() {
		return new ArrayList<>(recommendedInstances);
	}

	/**
	 * Returns all recommended relations.
	 *
	 * @return all recommended relations as list
	 */
	public List<RecommendedRelation> getRecommendedRelations() {
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
	public void addRecommendedRelation(String name, RecommendedInstance ri1, RecommendedInstance ri2, List<RecommendedInstance> otherInstances, double probability, List<INode> occurrences) {

		RecommendedRelation rrel = new RecommendedRelation(name, ri1, ri2, otherInstances, probability, occurrences);
		List<RecommendedInstance> ris = new ArrayList<>(List.of(ri1, ri2));
		ris.addAll(otherInstances);
		Optional<RecommendedRelation> recr = this.recommendedRelations.stream().filter(//
				r -> r.getRelationInstances().containsAll(ris) && ris.containsAll(r.getRelationInstances())).findAny();

		if (recr.isPresent()) {
			updateRecommendedRelation(recr.get(), occurrences, probability);
		} else {
			this.recommendedRelations.add(rrel);
		}
	}

	private void updateRecommendedRelation(RecommendedRelation recr, List<INode> occurrences, double probability) {
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
	public void addRecommendedInstanceJustName(String name, double probability, List<NounMapping> nameMappings) {

		this.addRecommendedInstance(name, "", probability, nameMappings, new ArrayList<NounMapping>());

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
	public RecommendedInstance addRecommendedInstance(String name, String type, double probability, List<NounMapping> nameMappings, List<NounMapping> typeMappings) {

		RecommendedInstance ri = new RecommendedInstance(name, type, probability, new ArrayList<>(new HashSet<>(nameMappings)), new ArrayList<>(new HashSet<>(typeMappings)));
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
	private void addRecommendedInstance(RecommendedInstance ri) {

		List<RecommendedInstance> risWithExactName = recommendedInstances.stream().filter(r -> r.getName().contentEquals(ri.getName())).collect(Collectors.toList());
		List<RecommendedInstance> risWithExactNameAndType = risWithExactName.stream().filter(r -> r.getType().contentEquals(ri.getType())).collect(Collectors.toList());

		if (recommendedInstances.contains(ri)) {
			return;
		}

		if (risWithExactNameAndType.isEmpty()) {

			if (risWithExactName.isEmpty()) {
				this.recommendedInstances.add(ri);
			} else {

				boolean added = false;

				for (RecommendedInstance riWithExactName : risWithExactName) {

					if (SimilarityUtils.areWordsSimilar(riWithExactName.getType(), ri.getType(), 0.85)) {
						riWithExactName.addMappings(ri.getNameMappings(), ri.getTypeMappings());
						added = true;
						// System.out.println(riWithExactName + " <- " + ri.getTypeMappings);
						break;
					} else if (riWithExactName.getType().contentEquals("") && !ri.getType().contentEquals("")) {
						riWithExactName.addMappings(ri.getNameMappings(), ri.getTypeMappings());
						added = true;
						break;
					}
				}

				if (!added && !ri.getType().contentEquals("")) {
					this.recommendedInstances.add(ri);
				}
			}

		} else {
			risWithExactNameAndType.get(0).addMappings(ri.getNameMappings(), ri.getTypeMappings());

		}

	}

	/**
	 * Returns all recommended instances that contain a given mapping as type.
	 *
	 * @param mapping given mapping to search for in types
	 * @return the list of recommended instances with the mapping as type.
	 */
	public List<RecommendedInstance> getRecommendedInstancesByTypeMapping(NounMapping mapping) {
		return recommendedInstances.stream().filter(sinstance -> sinstance.getTypeMappings().contains(mapping)).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a given mapping.
	 *
	 * @param mapping given mapping to search for
	 * @return the list of recommended instances with the mapping.
	 */
	public List<RecommendedInstance> getAnyRecommendedInstancesByMapping(NounMapping mapping) {
		return recommendedInstances.stream().filter(sinstance -> sinstance.getTypeMappings().contains(mapping) || sinstance.getNameMappings().contains(mapping)).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a given name.
	 *
	 * @param name given name to search for in names
	 * @return the list of recommended instances with that name.
	 */
	public List<RecommendedInstance> getRecommendedInstancesByName(String name) {
		return recommendedInstances.stream().filter(ri -> ri.getName().toLowerCase().contentEquals(name.toLowerCase())).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a similar name.
	 *
	 * @param name given name to search for in names
	 * @return the list of recommended instances with a similar name.
	 */
	public List<RecommendedInstance> getRecommendedInstancesBySimilarName(String name) {
		List<RecommendedInstance> ris = new ArrayList<>();
		for (RecommendedInstance ri : recommendedInstances) {
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
	public List<RecommendedInstance> getRecommendedInstancesByType(String type) {
		return recommendedInstances.stream().filter(ri -> ri.getType().toLowerCase().contentEquals(type.toLowerCase())).collect(Collectors.toList());
	}

	/**
	 * Returns all recommended instances that contain a similar type.
	 *
	 * @param type given type to search for in types
	 * @return the list of recommended instances with a similar type.
	 */
	public List<RecommendedInstance> getRecommendedInstancesBySimilarType(String type) {
		return recommendedInstances.stream().filter(ri -> SimilarityUtils.areWordsSimilar(ri.getType(), type)).collect(Collectors.toList());
	}

}
