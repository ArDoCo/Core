/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class TextState extends AbstractState implements ITextState {

    private MutableList<INounMapping> nounMappings;

    private MutableList<IPhraseMapping> phraseMappings;

    private static final double MAX_COSINE_PHRASE_DISTANCE = 0.5;

    /**
     * Creates a new name type relation state
     *
     * @param configs any additional configuration
     */
    public TextState(Map<String, String> configs) {
        super(configs);
        nounMappings = Lists.mutable.empty();
        phraseMappings = Lists.mutable.empty();
    }

    /***
     * Adds a name mapping to the state
     *
     * @param kind        mapping kind of the future noun mapping
     * @param probability probability to be a name mapping
     * @param occurrences list of the appearances of the mapping
     * @param word        node of the mapping
     */
    @Override
    public final void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability, ImmutableList<String> occurrences) {
        var words = Lists.immutable.with(word);
        addNounMappingOrAppendToSimilarNounMapping(words, kind, claimant, probability, occurrences);

    }

    /***
     * Adds a name mapping to the state
     *
     * @param word        word of the mapping
     * @param kind        mapping kind of the future noun mapping
     * @param probability probability to be a name mapping
     */
    @Override
    public final void addNounMapping(IWord word, MappingKind kind, IClaimant claimant, double probability) {

        addNounMapping(word, kind, claimant, probability, Lists.immutable.with(word.getText()));
    }

    private void addPhraseMapping(IPhraseMapping phraseMapping) {
        this.phraseMappings.add(phraseMapping);
    }

    @Override
    public final ImmutableList<INounMapping> getNounMappings() {
        return nounMappings.toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappings() {
        return phraseMappings.toImmutable();
    }

    /**
     * Returns all type mappings.
     *
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind kind) {
        return nounMappings.select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns all mappings containing the given node.
     *
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWord(IWord word) {
        return nounMappings.select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByNounMapping(INounMapping nounMapping) {
        return this.phraseMappings.select(p -> p.getNounMappings().contains(nounMapping)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhrase(IPhrase phrase) {
        return phraseMappings.select(p -> p.equals(phrase)).toImmutable();
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref) {
        return nounMappings.select(nm -> SimilarityUtils.areWordsSimilar(ref, nm.getReference())).toImmutable();
    }

    /**
     * Returns a list of all references of kind mappings.
     *
     * @return all references of type mappings as list.
     */
    @Override
    public final ImmutableList<String> getListOfReferences(MappingKind kind) {
        Set<String> referencesOfKind = new HashSet<>();
        var kindMappings = getNounMappingsOfKind(kind);
        for (INounMapping nnm : kindMappings) {
            referencesOfKind.add(nnm.getReference());
        }
        return Lists.immutable.withAll(referencesOfKind);
    }

    /**
     * Returns all type mappings containing the given node.
     *
     * @param word word to filter for
     * @return a list of alltype mappings containing the given node
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWordAndKind(IWord word, MappingKind kind) {
        return nounMappings.select(n -> n.getWords().contains(word)).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhraseType(PhraseType phraseType) {
        return this.phraseMappings.select(p -> p.getPhraseType().equals(phraseType)).toImmutable();
    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param word node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public final boolean isWordContainedByNounMappings(IWord word) {
        return nounMappings.anySatisfy(n -> n.getWords().contains(word));
    }

    /**
     * Returns if a node is contained by the name mappings.
     *
     * @param word        node to check
     * @param mappingKind mappingKind to check for
     * @return true if the node is contained by name mappings.
     */
    @Override
    public final boolean isWordContainedByMappingKind(IWord word, MappingKind mappingKind) {
        return nounMappings.select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
    }

    private Predicate<? super INounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(this.configs);
        textExtractionState.nounMappings = Lists.mutable.ofAll(nounMappings);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IClaimant claimant) {
        addNounMappingOrAppendToSimilarNounMapping(nounMapping.getWords(), nounMapping.getKind(), claimant, nounMapping.getProbability(),
                nounMapping.getSurfaceForms());
    }

    private IPhraseMapping getPhraseMappingByPhrase(IPhrase phrase) {
        MutableList<IPhraseMapping> phraseMappingsWithPhrase = this.phraseMappings.select(pm -> pm.getPhrases().contains(phrase));
        assert (phraseMappingsWithPhrase.size() == 1) : "There is an overlap of phraseMappings!";
        return phraseMappingsWithPhrase.get(0);
    }

    private INounMapping addNounMappingOrAppendToSimilarNounMapping(ImmutableList<IWord> words, MappingKind kind, IClaimant claimant, double probability,
            ImmutableList<String> occurrences) {

        INounMapping nounMapping = new NounMapping(words, kind, claimant, probability, words.castToList(), occurrences);
        IPhraseMapping phraseMapping = new PhraseMapping(nounMapping.getPhrases(), Lists.immutable.with(nounMapping), claimant, 1.0);

        ImmutableList<INounMapping> equalNounMappings = this.getNounMappings().select(nm -> nm.equals(nounMapping));

        if (equalNounMappings.size() > 0) {
            assert (equalNounMappings.size() == 1) : "There should only be one noun mapping with this reference";
            INounMapping equalNounMapping = equalNounMappings.get(0);

            return addEqualNounMapping(equalNounMapping, nounMapping, phraseMapping, claimant);
        }

        ImmutableList<INounMapping> similarNounMappings = this.getNounMappings().select(nm -> SimilarityUtils.areNounMappingsSimilar(nm, nounMapping));
        ImmutableList<IPhraseMapping> equalPhraseMappings = this.getPhraseMappings().select(pm -> pm.equals(phraseMapping));

        ImmutableList<IPhraseMapping> similarPhraseMappings = this.getPhraseMappings()
                .select(pm -> SimilarityUtils.arePhraseMappingsSimilar(pm, phraseMapping, MAX_COSINE_PHRASE_DISTANCE));

        Map<INounMapping, IPhraseMapping> similarMappings = new HashMap<>();
        for (INounMapping similarMapping : similarNounMappings) {
            // each nounMapping has only one similar phrase mapping
            similarMappings.put(similarMapping, similarPhraseMappings.select(pm -> pm.getNounMappings().contains(similarMapping)).get(0));
        }

        if (similarNounMappings.size() > 0) {

            return addSimilarNounMapping(equalPhraseMappings, similarMappings, nounMapping, phraseMapping, claimant);

        } else {
            return addDifferentNounMapping(equalPhraseMappings, similarMappings, nounMapping, phraseMapping);
        }
    }

    private INounMapping appendNounMappingToExistingNounMapping(INounMapping existingNounMapping, INounMapping nounMapping, IPhraseMapping phraseMapping,
            IClaimant claimant) {
        existingNounMapping.addKindWithProbability(nounMapping.getKind(), claimant, nounMapping.getProbability());
        existingNounMapping.addOccurrence(nounMapping.getSurfaceForms());
        existingNounMapping.addWord(nounMapping.getReferenceWords().get(0));

        ImmutableList<IPhraseMapping> phraseMappingsOfCorrectType = this.getPhraseMappingsByNounMapping(existingNounMapping)
                .select(pm -> pm.getPhraseType().equals(phraseMapping.getPhraseType()));
        assert (phraseMappingsOfCorrectType.size() <= 1) : "There should be at max one phrase mapping with the correct type";

        if (phraseMappingsOfCorrectType.size() == 0) {
            this.phraseMappings
                    .add(new PhraseMapping(phraseMapping.getPhrases(), Lists.immutable.with(existingNounMapping), claimant, phraseMapping.getProbability()));
        } else {
            phraseMappingsOfCorrectType.get(0).merge(phraseMapping);
        }
        return existingNounMapping;
    }

    private INounMapping addNounMappingAddPhraseMapping(INounMapping nounMapping, IPhraseMapping phraseMapping) {
        this.nounMappings.add(nounMapping);
        assert (phraseMapping.getNounMappings().contains(nounMapping)) : "When adding a noun mapping and a phrase mapping, both should be connected";

        this.phraseMappings.add(phraseMapping);

        return nounMapping;
    }

    private INounMapping addEqualNounMapping(INounMapping equalNounMapping, INounMapping nounMapping, IPhraseMapping phraseMapping, IClaimant claimant) {
        ImmutableList<IPhraseMapping> equalPhraseMappings = this.getPhraseMappings().select(pm -> pm.equals(phraseMapping));
        if (equalPhraseMappings.size() > 0) {
            assert (equalPhraseMappings.size() == 1) : "There should be only one equal phrase mapping";
            IPhraseMapping equalPhraseMapping = equalPhraseMappings.get(0);
            assert (equalPhraseMapping.getNounMappings().contains(equalNounMapping)) : "An identical phrase mapping should contain the identical noun mapping";

            return addEqualNounMappingWithEqualPhrase(equalNounMapping, nounMapping, phraseMapping, claimant);

        } else {

            ImmutableList<IPhraseMapping> similarPhraseMappingsOfEqualNounMapping = this.getPhraseMappingsByNounMapping(equalNounMapping)
                    .select(pm -> SimilarityUtils.arePhraseMappingsSimilar(pm, phraseMapping, MAX_COSINE_PHRASE_DISTANCE));

            if (similarPhraseMappingsOfEqualNounMapping.size() > 0) {
                return this.addEqualNounMappingWithSimilarPhrase(equalNounMapping, nounMapping, phraseMapping, claimant);
            } else {
                return addEqualNounMappingWithDifferentPhrase(equalNounMapping, nounMapping, phraseMapping);
            }

        }
    }

    private INounMapping addSimilarNounMapping(ImmutableList<IPhraseMapping> equalPhraseMappings, Map<INounMapping, IPhraseMapping> similarMappings,
            INounMapping nounMapping, IPhraseMapping phraseMapping, IClaimant claimant) {

        if (equalPhraseMappings.size() > 0) {
            return addSimilarNounMappingWithEqualPhrase();

        } else {

            if (similarMappings.keySet().size() > 0) {
                return addSimilarNounMappingWithSimilarPhrase(similarMappings, nounMapping, phraseMapping, claimant);

            } else {
                return addSimilarNounMappingWithDifferentPhrase(nounMapping, phraseMapping);
            }
        }
    }

    private INounMapping addDifferentNounMapping(ImmutableList<IPhraseMapping> equalPhraseMappings, Map<INounMapping, IPhraseMapping> similarMappings,
            INounMapping nounMapping, IPhraseMapping phraseMapping) {

        if (equalPhraseMappings.size() > 0) {
            assert (equalPhraseMappings.size() == 1) : "There should only be one equal phrase mapping";

            return addDifferentNounMappingWithEqualPhrase(equalPhraseMappings.get(0), nounMapping, phraseMapping);
        } else {

            if (similarMappings.keySet().size() > 0) {
                return addDifferentNounMappingWithSimilarPhrase(nounMapping, phraseMapping);

            } else {
                return addDifferentNounMappingWithDifferentPhrase(nounMapping, phraseMapping);
            }
        }
    }

    private INounMapping addEqualNounMappingWithEqualPhrase(INounMapping existingNounMapping, INounMapping nounMapping, IPhraseMapping phraseMapping,
            IClaimant claimant) {
        // DO: Extend existing noun mapping and phrase mapping with occurrences
        appendNounMappingToExistingNounMapping(existingNounMapping, nounMapping, phraseMapping, claimant);
        return existingNounMapping;
    }

    private INounMapping addEqualNounMappingWithSimilarPhrase(INounMapping existingNounMapping, INounMapping nounMapping, IPhraseMapping phraseMapping,
            IClaimant claimant) {
        // DO: Extend existing noun mapping and phrase mapping with occurrences
        appendNounMappingToExistingNounMapping(existingNounMapping, nounMapping, phraseMapping, claimant);
        return existingNounMapping;
    }

    private INounMapping addEqualNounMappingWithDifferentPhrase(INounMapping existingNounMapping, INounMapping nounMapping, IPhraseMapping phraseMapping) {
        // DO: Do not merge. Create a new noun mapping with new phrase mapping
        addNounMappingAddPhraseMapping(nounMapping, phraseMapping);
        return nounMapping;
    }

    private INounMapping addSimilarNounMappingWithEqualPhrase() {
        // DO: This state should not be possible and should throw an IllegalStateException.
        throw new IllegalStateException("If the phrases are the same the containing noun mappings can not be just similar - but must be equal!");
    }

    private INounMapping addSimilarNounMappingWithSimilarPhrase(Map<INounMapping, IPhraseMapping> similarMappings, INounMapping nounMapping,
            IPhraseMapping phraseMapping, IClaimant claimant) {
        // DO: Merge all to one noun mapping and one phrase mapping

        var similarMappingsCopy = Lists.mutable.withAll(similarMappings.keySet());

        if (similarMappings.keySet().size() > 1) {

            for (int i = 1; i < similarMappings.keySet().size(); i++) {

                INounMapping nounMappingToMerge = similarMappingsCopy.get(i);

                nounMapping.addKindWithProbability(nounMappingToMerge.getKind(), claimant, nounMapping.getProbability());
                nounMapping.addOccurrence(nounMapping.getSurfaceForms());
                nounMapping.addWord(nounMapping.getReferenceWords().get(0));

                phraseMapping.merge(similarMappings.get(nounMappingToMerge));
            }

            for (INounMapping similarMapping : similarMappingsCopy) {
                this.removeNounMapping(similarMapping);
            }

            this.addNounMappingAddPhraseMapping(nounMapping, phraseMapping);
            return nounMapping;

        } else {
            return appendNounMappingToExistingNounMapping(similarMappingsCopy.get(0), nounMapping, phraseMapping, claimant);
        }

    }

    private INounMapping addSimilarNounMappingWithDifferentPhrase(INounMapping nounMapping, IPhraseMapping phraseMapping) {
        // DO: add a new noun mapping and a new phrase mapping.
        return addNounMappingAddPhraseMapping(nounMapping, phraseMapping);
    }

    private INounMapping addDifferentNounMappingWithEqualPhrase(IPhraseMapping equalPhraseMapping, INounMapping nounMapping, IPhraseMapping phraseMapping) {
        // DO: Extend phrase mapping, add noun mapping, add new noun mapping to existing phrase mapping
        this.nounMappings.add(nounMapping);
        assert (phraseMapping.getNounMappings().contains(nounMapping)) : "When adding a noun mapping and a phrase mapping, both should be connected";

        equalPhraseMapping.merge(phraseMapping);

        return nounMapping;
    }

    private INounMapping addDifferentNounMappingWithSimilarPhrase(INounMapping nounMapping, IPhraseMapping phraseMapping) {
        // DO: add noun mapping and phrase mapping
        return addNounMappingAddPhraseMapping(nounMapping, phraseMapping);
    }

    private INounMapping addDifferentNounMappingWithDifferentPhrase(INounMapping nounMapping, IPhraseMapping phraseMapping) {
        // DO: add noun mapping and phrase mapping
        return addNounMappingAddPhraseMapping(nounMapping, phraseMapping);
    }

    @Override
    public void removeNounMapping(INounMapping nounMapping) {
        nounMappings.remove(nounMapping);
        ImmutableList<IPhraseMapping> phraseMappings = this.getPhraseMappingsByNounMapping(nounMapping);
        for (IPhraseMapping phraseMapping : phraseMappings) {
            if (phraseMapping.getNounMappings().size() == 1) {
                this.phraseMappings.remove(phraseMapping);
            } else if (phraseMapping.getNounMappings().size() > 1) {
                phraseMapping.removeNounMapping(nounMapping);
            } else {
                throw new IllegalStateException("There should be no phrase mapping without any noun mappings");
            }
        }
    }

    private void removePhraseMapping(IPhraseMapping phraseMapping) {
        this.phraseMappings.remove(phraseMapping);
    }

    @Override
    public String toString() {
        return "TextExtractionState [nounMappings=" + String.join("\n", nounMappings.toString()) + ", relationNodes=" + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
