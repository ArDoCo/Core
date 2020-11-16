package modelconnector.textExtractor.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.ipd.parse.luna.graph.INode;
import modelconnector.helpers.GraphUtils;
import modelconnector.helpers.ModelConnectorConfiguration;
import modelconnector.helpers.SimilarityUtils;

/**
 * The text extraction state holds instance, relation mappings and terms
 * extracted from the textual information.
 *
 * @author Sophie
 *
 */
public class TextExtractionState {

	private List<NounMapping> nounMappings;
	private List<RelationMapping> relationMappings;
	private List<TermMapping> terms;

	/**
	 * Creates a new name type relation state
	 */
	public TextExtractionState() {
		nounMappings = new ArrayList<>();
		relationMappings = new ArrayList<>();
		terms = new ArrayList<>();
	}

	// --- add section --->

	/**
	 * Adds a node with its ref to the state. If the node and the reference is not
	 * already contained by the mappings the mapping is created. If then similar
	 * occurrences are contained the matching entries are copied to the new mapping.
	 * If the node is not contained, but the reference, the matching mapping is
	 * updated. If the node is already contained but its mapping doesn't contains a
	 * separated input it is extended. This possibly changes the kind. If the node
	 * is already contained and the mappings contain its occurrences are checked for
	 * similarities. If they are similar to the new occurrences the found mapping is
	 * updated. Elsewhere dependent on the occurrence of the reference a new is
	 * created or an existing one is extended.
	 *
	 * Only use for occurrences with separator in it!
	 *
	 * @param n           node to add to the mappings
	 * @param ref         reference to add to the mappings
	 * @param occurrences occurrences to add to the mappings
	 * @param kind        kind of the mapping
	 */
	private void hardAdd(INode n, String ref, List<String> occurrences, double probability, MappingKind kind) {

		double hardAddProbability = ModelConnectorConfiguration.textExtractionState_hardAddProbability * probability;
		List<NounMapping> nounMappingsWithNode = this.getNounMappingsByNode(n);

		if (nounMappingsWithNode.isEmpty()) {
			List<NounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(ref, nounMappings);

			addOrUpdate(nounMappingsWithRef, n, ref, occurrences, hardAddProbability, kind);
		} else {

			for (NounMapping nounMapping : nounMappingsWithNode) {

				if (occurrences.stream().filter(SimilarityUtils::containsSeparator).findAny().isPresent() && !SimilarityUtils.areWordsSimilar(nounMapping.getReference(), ref)) {

					List<NounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(ref, nounMappings);

					addOrUpdate(nounMappingsWithRef, n, ref, occurrences, hardAddProbability, kind);

				} else {
					updateKindProbOccOfMapping(nounMapping, occurrences, kind, probability);
				}
			}
		}
	}

	private void addOrUpdate(List<NounMapping> nounMappingsWithRef, INode n, String ref, List<String> occurrences, double hardAddProbability, MappingKind kind) {
		if (nounMappingsWithRef.isEmpty()) {

			List<NounMapping> nounMappingsWithOcc = this.nounMappings.stream().filter(//
					nsn -> SimilarityUtils.areWordsOfListsSimilar(nsn.getRepresentativeComparables(), occurrences)).collect(Collectors.toList());

			NounMapping createdMapping = createMappingTypeDependentMapping(kind, n, hardAddProbability, ref, occurrences);

			extendNounMappingsWithOccurrences(nounMappingsWithOcc, ref, createdMapping);

		} else {

			for (NounMapping nm : nounMappingsWithRef) {

				extendMappingBy(nm, kind, n, hardAddProbability, occurrences);
			}

		}
	}

	/**
	 * Searches for noun mappings with similar occurrences as the given ref. If some
	 * are found their occurrences and nodes are copied to the given mapping.
	 *
	 * @param nounMappingsWithOccurrences
	 * @param ref
	 * @param mapping
	 */
	private void extendNounMappingsWithOccurrences(List<NounMapping> nounMappingsWithOccurrences, String ref, NounMapping mapping) {

		for (NounMapping nnm : nounMappingsWithOccurrences) {
			List<String> occSimilar = nnm.getOccurrences().stream().filter(occ -> SimilarityUtils.areWordsSimilar(ref, occ)).collect(Collectors.toList());
			occSimilar.stream().forEach(occ -> nnm.copyOccurrencesAndNodesTo(occ, mapping));
		}
	}

	/**
	 * If the given kind is not a nort, the type of the noun mapping is changed to
	 * the given kind if the probability is high enough. The occurrences are
	 * extended and the probability is set in any case.
	 *
	 * @param nounMapping the mapping to update
	 * @param occurrences the occurrences to update with
	 * @param kind        the kind of the updating mapping
	 * @param probability the probability of the updating mapping
	 */
	private void updateKindProbOccOfMapping(NounMapping nounMapping, List<String> occurrences, MappingKind kind, Double probability) {
		if (kind != null && probability != null && !kind.equals(MappingKind.NAME_OR_TYPE)) {
			updateKindOfNounMapping(nounMapping, kind, probability);
		}
		nounMapping.addOccurrence(occurrences);
		if (probability != null) {
			nounMapping.hardSetProbability(probability);
		}
	}

	private void updateKindOfNounMapping(NounMapping nounMapping, MappingKind kind, double probability) {
		MappingKind preKind = nounMapping.getKind();
		if (preKind == kind) {
			nounMapping.changeMappingTypeTo(kind, probability);
			return;
		}

		List<TermMapping> termsWithMapping = this.terms.stream().filter(//
				t -> t.getMappings().contains(nounMapping)).collect(Collectors.toList());

		nounMapping.changeMappingTypeTo(kind, probability);

		for (TermMapping termWithMapping : termsWithMapping) {
			if (!termWithMapping.getKind().equals(kind)) {
				if (termWithMapping.getMappings().stream().anyMatch(mapping -> //
				!mapping.getKind().equals(kind) && !mapping.getKind().equals(MappingKind.NAME_OR_TYPE))) {
					removeTerm(termWithMapping);
				}
			}
		}

	}

	/**
	 * Extends the given mapping by the node if it is not already contained. After
	 * that, the kind, probability, and occurrences are updated.
	 *
	 * @param nounMapping the noun mapping to update
	 * @param mt          the kind to update with
	 * @param n           the node to update with
	 * @param probability the probability to update with
	 * @param occurrences the occurrences to update with
	 */
	private void extendMappingBy(NounMapping nounMapping, MappingKind mt, INode n, Double probability, List<String> occurrences) {

		if (!nounMapping.getNodes().contains(n)) {
			nounMapping.addNode(n);
		}

		updateKindProbOccOfMapping(nounMapping, occurrences, mt, probability);

	}

	/**
	 * Dependent on the kind a new node is created and added to the mappings of the
	 * state.
	 *
	 * @param kind        the given kind
	 * @param n           the given node
	 * @param probability the given probability
	 * @param ref         the reference of the mapping
	 * @param occurrences the occurrences for the mapping
	 * @return the created noun mapping
	 */
	private NounMapping createMappingTypeDependentMapping(MappingKind kind, INode n, double probability, String ref, List<String> occurrences) {
		NounMapping createdMapping;
		if (kind.equals(MappingKind.NAME)) {
			createdMapping = NounMapping.createNameMapping(n, probability, ref, occurrences);
			this.nounMappings.add(createdMapping);
		} else if (kind.equals(MappingKind.TYPE)) {
			createdMapping = NounMapping.createTypeMapping(n, probability, ref, occurrences);
			this.nounMappings.add(createdMapping);
		} else {
			createdMapping = NounMapping.createNortMapping(n, probability, ref, occurrences);
			this.nounMappings.add(createdMapping);
		}
		return createdMapping;
	}

	/**
	 * Adds a mapping of a certain kind to the mappings of this state. If the
	 * reference contains a separator it is removed and the method
	 * {@link #hardAdd(INode, String, List, MappingKind)} is called and returned. If
	 * neither the node nor the reference is contained in the mappings a new mapping
	 * is created. Elsewhere if a mapping with the node can be found it is updated.
	 * If the reference can be found the mapping with it is updated. The method
	 * {@link #updateMapping(NounMapping, MappingKind, double, List)} is used for
	 * updating the mappings.
	 *
	 *
	 * @param n           node to add
	 * @param reference   reference to add
	 * @param kind        kind to add
	 * @param probability probability for kind
	 * @param occurrences appearances of the mapping to add
	 */
	private void addNounMapping(INode node, String reference, MappingKind kind, double probability, List<String> occurrences) {

		if (SimilarityUtils.containsSeparator(reference)) {
			addNounMappingWithSeparator(node, reference, probability, kind);
		}

		List<NounMapping> nounMappingsWithNode = this.getNounMappingsByNode(node);
		List<NounMapping> nounMappingsWithRef = SimilarityUtils.getMostLikelyNMappingsByReference(reference, nounMappings);

		if (nounMappingsWithNode.isEmpty() && nounMappingsWithRef.isEmpty()) {
			NounMapping createdMapping = NounMapping.createMappingTypeNode(node, reference, kind, probability, occurrences);
			this.nounMappings.add(createdMapping);

		} else if (nounMappingsWithNode.isEmpty()) {
			if (nounMappingsWithRef.size() == 1) {
				NounMapping nounMapping = nounMappingsWithRef.get(0);
				nounMapping.addNode(node);
				updateMapping(nounMapping, kind, probability, occurrences);
			}
		} else {
			for (NounMapping nounMapping : nounMappingsWithNode) {
				updateMapping(nounMapping, kind, probability, occurrences);
			}
		}

	}

	private void addNounMappingWithSeparator(INode n, String reference, double probability, MappingKind kind) {

		String wholeName = SimilarityUtils.splitAtSeparators(reference);
		List<String> parts = List.of(wholeName.split(" "));
		parts = parts.stream().filter(part -> part.length() > 1).collect(Collectors.toList());
		for (String part : parts) {
			this.hardAdd(n, part, List.of(reference), probability, kind);
		}
		return;

	}

	/**
	 * The update of a mapping depends on its kind. Name or type mappings can be
	 * changed always if something more specific is proposed. For updating name or
	 * types the probability has to be greater than the current probability. In
	 * every case the mapping is extended by the occurrences and the probability is
	 * updated.
	 *
	 * @param nnm         the existing mapping
	 * @param kind        the proposed kind
	 * @param probability the probability for the kind
	 * @param occurrences the occurrences to add
	 */
	private void updateMapping(NounMapping nnm, MappingKind kind, double probability, List<String> occurrences) {

		if (kind.equals(MappingKind.NAME_OR_TYPE)) {
			nnm.addOccurrence(occurrences);
			nnm.updateProbability(probability);

		} else if (kind.equals(MappingKind.TYPE)) {
			if (nnm.getKind().equals(MappingKind.NAME_OR_TYPE)) {
				nnm.changeMappingTypeTo(MappingKind.TYPE, probability);
			} else if (probability >= nnm.getProbability()) {
				nnm.changeMappingTypeTo(MappingKind.TYPE, probability);
			}
		} else if (kind.equals(MappingKind.NAME)) {
			if (nnm.getKind().equals(MappingKind.NAME_OR_TYPE)) {
				nnm.changeMappingTypeTo(MappingKind.NAME, probability);
			} else if (probability >= nnm.getProbability()) {
				nnm.changeMappingTypeTo(MappingKind.NAME, probability);

			}
		}

		nnm.addOccurrence(occurrences);
		nnm.updateProbability(probability); // Hier evtl nicht ganz richtig, wenn nicht gleicher MappingType... verstärkt
											// in falsche Richtung!
	}

	/***
	 * Adds a name mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param name        reference of the mapping
	 * @param probability probability to be a name mapping
	 * @param occurrences list of the appearances of the mapping
	 */
	public void addName(INode n, String name, double probability, List<String> occurrences) {
		addNounMapping(n, name, MappingKind.NAME, probability, occurrences);
	}

	/***
	 * Adds a name mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param name        reference of the mapping
	 * @param probability probability to be a name mapping
	 */
	public void addName(INode n, String name, double probability) {
		addName(n, name, probability, List.of(GraphUtils.getNodeValue(n)));
	}

	/***
	 * Adds a name or type mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param ref         reference of the mapping
	 * @param probability probability to be a name or type mapping
	 */
	public void addNort(INode n, String ref, double probability) {
		addNort(n, ref, probability, List.of(GraphUtils.getNodeValue(n)));
	}

	/***
	 * Adds a name or type mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param ref         reference of the mapping
	 * @param probability probability to be a name or type mapping
	 * @param occurrences list of the appearances of the mapping
	 */
	public void addNort(INode n, String ref, double probability, List<String> occurrences) {
		addNounMapping(n, ref, MappingKind.NAME_OR_TYPE, probability, occurrences);
	}

	/***
	 * Adds a type mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param type        reference of the mapping
	 * @param probability probability to be a type mapping
	 */
	public void addType(INode n, String type, double probability) {
		addType(n, type, probability, List.of(GraphUtils.getNodeValue(n)));
	}

	/***
	 * Adds a type mapping to the state
	 *
	 * @param n           node of the mapping
	 * @param type        reference of the mapping
	 * @param probability probability to be a type mapping
	 * @param occurrences list of the appearances of the mapping
	 */
	public void addType(INode n, String type, double probability, List<String> occurrences) {
		addNounMapping(n, type, MappingKind.TYPE, probability, occurrences);
	}

	/**
	 * Creates a new relation mapping and adds it to the state. More end points, as
	 * well as a preposition can be added afterwards.
	 *
	 * @param node1       first relation end point
	 * @param node2       second relation end point
	 * @param probability probability of being a relation
	 * @return the added relation mapping
	 */
	public RelationMapping addRelation(NounMapping node1, NounMapping node2, double probability) {
		RelationMapping relationMapping = new RelationMapping(node1, node2, probability);
		if (!relationMappings.contains(relationMapping)) {
			relationMappings.add(relationMapping);
		}
		return relationMapping;
	}

	/**
	 * Adds a relation mapping to the state.
	 *
	 * @param n the relation mapping to add.
	 */
	public void addRelation(RelationMapping n) {
		if (!relationMappings.contains(n)) {
			relationMappings.add(n);
		}
	}

	/**
	 * Adds a term to the state.
	 *
	 * @param reference   the reference of the term
	 * @param mapping1    the first mapping of the term
	 * @param mapping2    the second mapping of the term
	 * @param kind        the kind of the term
	 * @param probability the probability that this term is from that kind
	 */
	public void addTerm(String reference, NounMapping mapping1, NounMapping mapping2, MappingKind kind, double probability) {
		addTerm(reference, List.of(mapping1, mapping2), kind, probability);
	}

	/**
	 * Adds a term to the state.
	 *
	 * @param reference     the reference of the term
	 * @param mapping1      the first mapping of the term
	 * @param mapping2      the second mapping of the term
	 * @param otherMappings other mappings of the term
	 * @param kind          the kind of the term
	 * @param probability   the probability that this term is from that kind
	 */
	public void addTerm(String reference, NounMapping mapping1, NounMapping mapping2, List<NounMapping> otherMappings, MappingKind kind, double probability) {

		List<NounMapping> mappings = new ArrayList<>();
		mappings.add(mapping1);
		mappings.add(mapping2);
		mappings.addAll(otherMappings);
		addTerm(reference, mappings, kind, probability);
	}

	/**
	 * Adds a term as a name to the state.
	 *
	 * @param reference   the reference of the term
	 * @param mapping1    the first mapping of the term
	 * @param mapping2    the second mapping of the term
	 * @param probability the probability that this term is a name
	 */
	public void addNameTerm(String reference, NounMapping mapping1, NounMapping mapping2, double probability) {
		addTerm(reference, List.of(mapping1, mapping2), MappingKind.NAME, probability);
	}

	/**
	 * Adds a term as a type to the state.
	 *
	 * @param reference   the reference of the term
	 * @param mapping1    the first mapping of the term
	 * @param mapping2    the second mapping of the term
	 * @param probability the probability that this term is a type
	 */
	public void addTypeTerm(String reference, NounMapping mapping1, NounMapping mapping2, double probability) {
		addTerm(reference, List.of(mapping1, mapping2), MappingKind.TYPE, probability);
	}

	/**
	 * Creates a new term if the term is not yet included by the state, and adds it
	 * it. If terms with the same mappings and of the same kind can be found their
	 * probability is updated.
	 *
	 * @param reference   the reference of the term
	 * @param mappings    mappings of the term
	 * @param kind        the kind of the term
	 * @param probability the probability that this term is from that kind
	 */
	private void addTerm(String reference, List<NounMapping> mappings, MappingKind kind, double probability) {

		List<TermMapping> includedTerms = getTermsByMappingsAndKind(mappings, kind);

		if (includedTerms.size() >= 1) {
			for (TermMapping includedTerm : includedTerms) {
				includedTerm.updateProbability(probability);
			}
		} else {
			TermMapping term;
			if (mappings.size() <= 2) {
				term = new TermMapping(reference, mappings.get(0), mappings.get(1), List.of(), kind, probability);

			} else {
				term = new TermMapping(reference, mappings.get(0), mappings.get(1), mappings.subList(2, mappings.size() - 1), kind, probability);
			}
			terms.add(term);
		}
	}

	// --- remove section --->
	/**
	 * Removes a noun mapping from the state.
	 *
	 * @param n noun mapping to remove
	 */
	public void removeNounNode(NounMapping n) {
		nounMappings.remove(n);
	}

	/**
	 * Removes a relation mapping from the state.
	 *
	 * @param n relation mapping to remove
	 */
	public void removeRelation(RelationMapping n) {
		relationMappings.remove(n);
	}

	/**
	 * Removes the given term from the state.
	 *
	 * @param term the term to remove.
	 */
	public void removeTerm(TermMapping term) {
		terms.remove(term);
	}

	// --- get section --->

	/**
	 * Getter for the terms of this state.
	 *
	 * @return the list of found terms
	 */
	public List<TermMapping> getTerms() {
		return terms;
	}

	/**
	 * Getter for the terms of this state, that have exactly the same nounMappings.
	 *
	 * @param nounMappings the nounMappings to search for
	 * @return a list of terms with that nounMappings
	 */
	public List<TermMapping> getTermsByMappings(List<NounMapping> nounMappings) {
		return terms.stream().filter(//
				t -> t.getMappings().containsAll(nounMappings) && nounMappings.containsAll(t.getMappings())).//
				collect(Collectors.toList());
	}

	/**
	 * Getter for the terms of this state, that have a similar reference.
	 *
	 * @param reference the given reference
	 * @return a list of terms with a reference that is similar to the given
	 */
	public List<TermMapping> getTermsBySimilarReference(String reference) {
		return terms.stream().filter(t -> SimilarityUtils.areWordsSimilar(reference, t.getReference())).collect(Collectors.toList());
	}

	/**
	 * Getter for the terms of this state, that have exactly the same nounMappings
	 * and the same kind.
	 *
	 * @param nounMappings the nounMappings to search for
	 * @param kind         the kind of the term mappings to search for
	 * @return a list of terms with that nounMappings and the same kind
	 */
	public List<TermMapping> getTermsByMappingsAndKind(List<NounMapping> nounMappings, MappingKind kind) {
		List<TermMapping> terms = getTermsByMappings(nounMappings);
		return terms.stream().filter(t -> t.getKind().equals(kind)).collect(Collectors.toList());
	}

	/**
	 * Returns all type mappings.
	 *
	 * @return all type mappings as list
	 */
	public List<NounMapping> getTypes() {
		return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
	}

	/**
	 * Returns all type term mappings.
	 *
	 * @return all type term mappings as list
	 */
	public List<TermMapping> getTypeTerms() {
		return terms.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).collect(Collectors.toList());
	}

	/**
	 * Returns all mappings containing the given node.
	 *
	 * @param n the given node
	 * @return all mappings containing the given node as list
	 */
	public List<NounMapping> getNounMappingsByNode(INode n) {
		return nounMappings.stream().filter(nMapping -> nMapping.getNodes().contains(n)).collect(Collectors.toList());
	}

	/**
	 * Returns all mappings with the exact same reference as given.
	 *
	 * @param ref the reference to search for
	 * @return a list of noun mappings with the given reference.
	 */
	public List<NounMapping> getNounMappingsWithSameReference(String ref) {
		return nounMappings.stream().filter(nMapping -> nMapping.getReference().contentEquals(ref)).collect(Collectors.toList());
	}

	/**
	 * Returns a list of all references of name mappings.
	 *
	 * @return all references of name mappings as list.
	 */
	public List<String> getNameList() {

		Set<String> names = new HashSet<>();
		List<NounMapping> nameMappings = this.getNames();
		for (NounMapping nnm : nameMappings) {
			names.add(nnm.getReference());
		}
		return new ArrayList<>(names);
	}

	/**
	 * Returns a list of all references of name term mappings.
	 *
	 * @return all references of name term mappings as list.
	 */
	public List<String> getNameTermList() {
		Set<String> names = new HashSet<>();
		List<TermMapping> nameMappings = this.getNameTerms();
		for (TermMapping nnm : nameMappings) {
			names.add(nnm.getReference());
		}
		return new ArrayList<>(names);
	}

	/**
	 * Returns a list of all references of name or type mappings.
	 *
	 * @return all references of name or type mappings as list.
	 */
	public List<String> getNortList() {
		Set<String> norts = new HashSet<>();
		List<NounMapping> nortMappings = this.getNameOrTypeMappings();
		for (NounMapping nnm : nortMappings) {
			norts.add(nnm.getReference());
		}
		return new ArrayList<>(norts);
	}

	/**
	 * Returns a list of all references of type mappings.
	 *
	 * @return all references of type mappings as list.
	 */
	public List<String> getTypeList() {

		Set<String> types = new HashSet<>();
		List<NounMapping> typeMappings = this.getTypes();
		for (NounMapping nnm : typeMappings) {
			types.add(nnm.getReference());
		}
		return new ArrayList<>(types);
	}

	/**
	 * Returns a list of all references of type term mappings.
	 *
	 * @return all references of type term mappings as list.
	 */
	public List<String> getTypeTermList() {

		Set<String> types = new HashSet<>();
		List<TermMapping> typeMappings = this.getTypeTerms();
		for (TermMapping nnm : typeMappings) {
			types.add(nnm.getReference());
		}
		return new ArrayList<>(types);
	}

	/**
	 * Returns all name mappings
	 *
	 * @return a list of all name mappings
	 */
	public List<NounMapping> getNames() {
		return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
	}

	/**
	 * Returns all name term mappings
	 *
	 * @return a list of all name term mappings
	 */
	public List<TermMapping> getNameTerms() {
		return terms.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).collect(Collectors.toList());
	}

	/**
	 * Returns all name or type mappings
	 *
	 * @return a list of all name or type mappings
	 */
	public List<NounMapping> getNameOrTypeMappings() {
		return nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).collect(Collectors.toList());
	}

	/**
	 * Returns alltype mappings containing the given node.
	 *
	 * @param node node to filter for
	 * @return a list of alltype mappings containing the given node
	 */
	public List<NounMapping> getTypeNodesByNode(INode node) {
		return this.nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.TYPE).collect(Collectors.toList());
	}

	/**
	 * Returns all name mappings containing the given node.
	 *
	 * @param node node to filter for
	 * @return a list of all name mappings containing the given node
	 */
	public List<NounMapping> getNameNodesByNode(INode node) {
		return this.nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.NAME).collect(Collectors.toList());
	}

	/**
	 * Returns all name or type mappings containing the given node.
	 *
	 * @param node node to filter for
	 * @return a list of all name or type mappings containing the given node
	 */
	public List<NounMapping> getNortNodesByNode(INode node) {
		return this.nounMappings.stream().filter(n -> n.getNodes().contains(node)).filter(n -> n.getKind() == MappingKind.NAME_OR_TYPE).collect(Collectors.toList());
	}

	/**
	 * Returns all relation mappings.
	 *
	 * @return relation mappings as list
	 */
	public List<RelationMapping> getRelations() {
		return relationMappings;
	}

	/**
	 * Returns all term mappings that contain the given noun mapping.
	 *
	 * @param nounMapping the noun mapping that should be contained.
	 * @return all term mappings that contain the noun mapping.
	 */
	public List<TermMapping> getTermsByContainedMapping(NounMapping nounMapping) {

		List<TermMapping> filteredTerms = new ArrayList<>();

		for (TermMapping term : terms) {
			if (term.getMappings().contains(nounMapping)) {
				filteredTerms.add(term);
			}
		}
		return filteredTerms;
	}

	// --- isContained section --->
	/**
	 * Returns if a node is contained by the name or type mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by name or type mappings.
	 */
	public boolean isNodeContainedByNameOrTypeNodes(INode node) {
		return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME_OR_TYPE)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
	}

	/**
	 * Returns if a node is contained by the name mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by name mappings.
	 */
	public boolean isNodeContainedByNameNodes(INode node) {
		return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.NAME)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
	}

	/**
	 * Returns if a node is contained by the term mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by term mappings.
	 */
	public boolean isNodeContainedByTermMappings(INode node) {

		for (TermMapping term : terms) {
			if (term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns all term mappings that contain noun mappings containing the given
	 * node.
	 *
	 * @param node the node to search for
	 * @return a list of term mappings that contain that node.
	 */
	public List<TermMapping> getTermMappingsByNode(INode node) {

		return terms.stream().filter(//
				term -> term.getMappings().stream().anyMatch(n -> n.getNodes().contains(node))).collect(Collectors.toList());

	}

	/**
	 * Returns if a node is contained by the mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by mappings.
	 */
	public boolean isNodeContainedByNounMappings(INode node) {
		return !nounMappings.stream().filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
	}

	/**
	 * Returns if a node is contained by the type mappings.
	 *
	 * @param node node to check
	 * @return true if the node is contained by type mappings.
	 */
	public boolean isNodeContainedByTypeNodes(INode node) {
		return !nounMappings.stream().filter(n -> n.getKind().equals(MappingKind.TYPE)).filter(n -> n.getNodes().contains(node)).findAny().isEmpty();
	}

	@Override
	/**
	 * Prints the name type relation state: The noun mappings as well as the
	 * relation mappings.
	 */
	public String toString() {
		return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes=" + String.join("\n", relationMappings.toString()) + "]";
	}

}
