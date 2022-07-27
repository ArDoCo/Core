/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.*;

import org.eclipse.collections.api.block.predicate.Predicate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The Class TextState defines the basic implementation of a {@link ITextState}.
 *
 * @author Sophie Schulz
 * @author Jan Keim
 */
public class TextState extends AbstractState implements ITextState {

    private MutableSet<ElementWrapper<INounMapping>> nounMappings;
    private MutableSet<IPhraseMapping> phraseMappings;

    private static final double MIN_COSINE_SIMILARITY = 0.4;

    /**
     * Creates a new name type relation state
     *
     * @param configs any additional configuration
     */
    public TextState(Map<String, String> configs) {
        super(configs);
        nounMappings = Sets.mutable.empty();
        phraseMappings = Sets.mutable.empty();
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
        addNounMappingOrExtendExistingNounMapping(words, kind, claimant, probability, occurrences);

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

    @Override
    public final ImmutableList<INounMapping> getNounMappings() {

        MutableList<INounMapping> nounMappings = Lists.mutable.empty();
        for (ElementWrapper<INounMapping> nm : this.nounMappings) {
            nounMappings.add(nm.getElement());
        }
        return nounMappings.toImmutable();

    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappings() {

        return Sets.mutable.withAll(getNounMappings().flatCollect(this::getPhraseMappingsByNounMapping)).toImmutableList();
    }

    public ImmutableList<IPhraseMapping> getPhraseMappingsByNounMapping(INounMapping nm) {

        var result = phraseMappings.select(pm -> pm.getPhrases().containsAllIterable(nm.getPhrases())).toImmutableList();

        if (result.size() != 1) {
            int i = 0;
        }
        /*
         * if (nm.getPhrases().size() != result.get(0).getPhrases().size() || nm.getPhrases().select(p ->
         * !result.get(0).getPhrases().contains(p)).size() > 0) { int i = 0; }
         */

        return result;
    }

    public IPhraseMapping getPhraseMappingByNounMapping(INounMapping nounMapping) {
        ImmutableList<IPhraseMapping> phraseMappingsByNounMapping = getPhraseMappingsByNounMapping(nounMapping);

        assert (phraseMappingsByNounMapping.size() >= 1) : "We currently support only noun mappings with just one phrase mapping";
        return phraseMappingsByNounMapping.get(0);

    }

    public ImmutableList<INounMapping> getNounMappingsByPhraseMapping(IPhraseMapping phraseMapping) {
        return getNounMappings().select(nm -> phraseMapping.getPhrases().equals(nm.getPhrases())).toImmutableList();
    }

    /**
     * Returns all type mappings.
     *
     * @param kind searched mappingKind
     * @return all type mappings as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsOfKind(MappingKind kind) {
        return getNounMappings().select(nounMappingIsOfKind(kind)).toImmutable();
    }

    /**
     * Returns all mappings containing the given word.
     *
     * TODO: For Phi: Will it make problems that this is not compared just by reference?
     * 
     * @param word the given word
     * @return all mappings containing the given node as list
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsByWord(IWord word) {
        return getNounMappings().select(nMapping -> nMapping.getWords().contains(word)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhrase(IPhrase phrase) {
        return getPhraseMappings().select(pm -> pm.getPhrases().contains(phrase));
    }

    /**
     * Returns all mappings with a similar reference as given.
     *
     * @param ref the reference to search for
     * @return a list of noun mappings with the given reference.
     */
    @Override
    public final ImmutableList<INounMapping> getNounMappingsWithSimilarReference(String ref) {

        return getNounMappings().select(nm -> SimilarityUtils.areWordsSimilar(ref, nm.getReference())).toImmutable();
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
    public final ImmutableList<INounMapping> getNounMappingsByWordTextAndKind(IWord word, MappingKind kind) {
        return getNounMappings().select(n -> n.getWords().anySatisfy(w -> w.getText().equals(word.getText()))).select(nounMappingIsOfKind(kind)).toImmutable();
    }

    @Override
    public ImmutableList<IPhraseMapping> getPhraseMappingsByPhraseType(PhraseType phraseType) {
        return getPhraseMappings().select(p -> p.getPhraseType().equals(phraseType)).toImmutable();
    }

    /**
     * Returns if a node is contained by the mappings.
     *
     * @param word node to check
     * @return true if the node is contained by mappings.
     */
    @Override
    public final boolean isWordContainedByNounMappings(IWord word) {
        return getNounMappings().anySatisfy(n -> n.getWords().contains(word));
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
        return getNounMappings().select(n -> n.getWords().contains(word)).anySatisfy(nounMappingIsOfKind(mappingKind));
    }

    private Predicate<? super INounMapping> nounMappingIsOfKind(MappingKind mappingKind) {
        return n -> n.getKind() == mappingKind;
    }

    @Override
    public ITextState createCopy() {
        var textExtractionState = new TextState(this.configs);
        textExtractionState.nounMappings = Sets.mutable.withAll(nounMappings);
        textExtractionState.phraseMappings = Sets.mutable.withAll(phraseMappings);
        return textExtractionState;
    }

    @Override
    public void addNounMapping(INounMapping nounMapping, IClaimant claimant) {
        addNounMappingOrExtendExistingNounMapping(nounMapping.getWords(), nounMapping.getKind(), claimant, nounMapping.getProbability(),
                nounMapping.getSurfaceForms());
    }

    private IPhraseMapping getPhraseMappingByPhrase(IPhrase phrase) {
        ImmutableList<IPhraseMapping> phraseMappingsWithPhrase = getPhraseMappings().select(pm -> pm.getPhrases().contains(phrase));
        assert (phraseMappingsWithPhrase.size() == 1) : "There is an overlap of phraseMappings!";
        return phraseMappingsWithPhrase.get(0);
    }

    /**
     * Extracts a phrase mapping that has the same phrase as the given phrase mapping. Currently, we only allow one
     * phrase per phrase mapping to search for the same phrase.
     * 
     * @param phraseMapping that holds the phrase that is searched for.
     * @return phraseMapping that has the same phrases as the given phrase mapping
     */
    private IPhraseMapping getPhraseMappingContainingTheSamePhrase(IPhraseMapping phraseMapping) {
        Map<IPhrase, IPhraseMapping> exactPhrases = new HashMap<>();

        for (IPhraseMapping existingPhraseMapping : getPhraseMappings()) {
            for (IPhrase existingPhrase : existingPhraseMapping.getPhrases()) {
                if (phraseMapping.getPhrases().anySatisfy(phrase -> phrase.equals(existingPhrase))) {
                    exactPhrases.put(existingPhrase, existingPhraseMapping);
                }
            }
        }

        assert exactPhrases.isEmpty() || (exactPhrases.keySet().size() == 1) : "There should be only one phrase in the initial phrase mapping";

        ImmutableList<IPhraseMapping> phraseMappingsWithEqualPhrases = Lists.immutable.withAll(exactPhrases.values());

        if (phraseMappingsWithEqualPhrases.isEmpty())
            return null;

        assert (phraseMappingsWithEqualPhrases.size() == 1) : "There should be only one equal phrase mapping";
        return phraseMappingsWithEqualPhrases.get(0);
    }

    private INounMapping addNounMappingOrExtendExistingNounMapping(ImmutableList<IWord> words, MappingKind kind, IClaimant claimant, double probability,
            ImmutableList<String> occurrences) {

        ImmutableList<INounMapping> currentNounMappings = getNounMappings();
        ImmutableList<IPhraseMapping> currentPhraseMappings = getPhraseMappings();

        INounMapping nounMapping = new NounMapping(words, kind, claimant, probability, Lists.immutable.withAll(words), occurrences);
        IPhraseMapping phraseMapping = new PhraseMapping(words.collect(IWord::getPhrase));

        ImmutableList<INounMapping> equalNounMappings = currentNounMappings.select(nm -> nm.containsSameWordsAs(nounMapping));
        ImmutableList<INounMapping> textualEqualNounMappings = currentNounMappings.select(nm -> nm.sharesTextualWordRepresentation(nounMapping));
        ImmutableList<INounMapping> similarNounMappings = this.getNounMappings().select(nm -> SimilarityUtils.areNounMappingsSimilar(nm, nounMapping));

        IPhraseMapping phraseMappingWithEqualPhrase = getPhraseMappingContainingTheSamePhrase(phraseMapping);
        ImmutableList<IPhraseMapping> similarPhraseMappings = currentPhraseMappings.select(pm -> SimilarityUtils.getPhraseMappingSimilarity(this, pm,
                phraseMapping, SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MIN_COSINE_SIMILARITY);

        if (!equalNounMappings.isEmpty()) {
            // all words are equal -> equal phrases -> do nothing
            return null;

        } else if (!textualEqualNounMappings.isEmpty()) {
            // all new words have the same text as one noun mapping
            // however - semantically they can differ -> dependent on phrases!

            assert (words.size() == 1) : "We currently only support the input of one word per new noun mapping!";
            return addTextualEqualNounMapping(textualEqualNounMappings, phraseMappingWithEqualPhrase, nounMapping);

        } else if (!similarNounMappings.isEmpty()) {

            return addSimilarNounMapping(phraseMappingWithEqualPhrase, similarNounMappings, similarPhraseMappings, nounMapping);

        } else {

            return addDifferentNounMapping(phraseMappingWithEqualPhrase, similarPhraseMappings, nounMapping);
        }
    }

    private INounMapping addNounMappingAddPhraseMapping(INounMapping nounMapping) {

        nounMappings.add(wrap(nounMapping));
        phraseMappings.add(new PhraseMapping(nounMapping.getPhrases()));

        if (invalidState()) {
            int i = 0;
        }
        return nounMapping;
    }

    private void mergeAllNounMappings(ImmutableList<INounMapping> nounMappingsToMerge, INounMapping newNounMapping) {

        var phraseMappingsOfExistingNounMappings = nounMappingsToMerge.flatCollect(this::getPhraseMappingsByNounMapping);

        for (int i = 0; i < nounMappingsToMerge.size(); i++) {
            newNounMapping.merge(nounMappingsToMerge.get(i));
            nounMappings.remove(wrap(nounMappingsToMerge.get(i)));
        }

        IPhraseMapping newPhraseMapping = new PhraseMapping(newNounMapping.getPhrases());
        phraseMappingsOfExistingNounMappings.forEach(newPhraseMapping::merge);
        phraseMappings.removeAllIterable(phraseMappingsOfExistingNounMappings);

        nounMappings.add(wrap(newNounMapping));
        phraseMappings.add(newPhraseMapping);
    }

    private void mergeAllNounMappingsOld(ImmutableList<INounMapping> nounMappingsToMerge, INounMapping nounMappingToMergeInto) {

        for (int i = 0; i < nounMappingsToMerge.size(); i++) {
            nounMappingToMergeInto.merge(nounMappingsToMerge.get(i));
            nounMappings.remove(wrap(nounMappingsToMerge.get(i)));
        }

        ImmutableList<IPhraseMapping> phraseMappingsOfMergedNounMapping = getPhraseMappingsByNounMapping(nounMappingToMergeInto);

        if (phraseMappingsOfMergedNounMapping.isEmpty()) {
            IPhraseMapping newPhraseMapping = new PhraseMapping(nounMappingToMergeInto.getPhrases());
            phraseMappings.add(newPhraseMapping);
            phraseMappingsOfMergedNounMapping = Lists.immutable.with(newPhraseMapping);
        }

        IPhraseMapping equalPhraseMapping = phraseMappingsOfMergedNounMapping.get(0);
        for (int i = 1; i < phraseMappingsOfMergedNounMapping.size(); i++) {
            equalPhraseMapping.merge(phraseMappingsOfMergedNounMapping.get(i));
            phraseMappings.remove(phraseMappingsOfMergedNounMapping.get(i));
        }

        if (invalidState()) {
            int i = 0;
        }

    }

    private INounMapping addTextualEqualNounMapping(ImmutableList<INounMapping> textualEqualNounMappings, IPhraseMapping phraseMappingWithEqualPhrase,
            INounMapping nounMapping) {

        if (phraseMappingWithEqualPhrase != null) {

            // contains does not work here
            var equalNounMappingsWithEqualPhrase = textualEqualNounMappings
                    .select(nm -> getNounMappingsByPhraseMapping(phraseMappingWithEqualPhrase).contains(nm));

            if (equalNounMappingsWithEqualPhrase.isEmpty()) {
                // there is a textual equal noun mapping but not in that phrase

                var nounMappingsWithEqualPhrase = getNounMappingsByPhraseMapping(phraseMappingWithEqualPhrase);
                if (!nounMappingsWithEqualPhrase.isEmpty()) {
                    // case 1: if phrase is contained by other noun mappings -> add noun mapping
                    // (phrase mapping already exists)

                    nounMappings.add(wrap(nounMapping));

                    // TODO: Remove
                    if (invalidState()) {
                        int i = 0;
                    }
                    return nounMapping;

                } else {
                    // case 2: phrase is not contained by other noun mappings -> throw exception
                    // TODO: equal phrase but no noun mappings of that!
                    // TODO: add to invalid State
                    // throw new IllegalStateException("It should not be possible that a noun and
                    // phrase mappings are equal but not connected!");
                    nounMappings.add(wrap(nounMapping));
                    return nounMapping;
                }

            } else {
                return addTextualEqualNounMappingWithEqualPhrase(nounMapping, equalNounMappingsWithEqualPhrase);

            }

        } else {

            ImmutableList<INounMapping> nounMappingsWithSimilarPhrases = textualEqualNounMappings.select(
                    nm -> SimilarityUtils.getPhraseMappingSimilarity(this, getPhraseMappingByNounMapping(nm), new PhraseMapping(nounMapping.getPhrases()),
                            SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MIN_COSINE_SIMILARITY);

            if (!nounMappingsWithSimilarPhrases.isEmpty()) {
                return addTextualEqualNounMappingWithSimilarPhrase(nounMapping, nounMappingsWithSimilarPhrases);

            } else {
                return addTextualEqualNounMappingWithDifferentPhrase(nounMapping);
            }

        }
    }

    private INounMapping addSimilarNounMapping(IPhraseMapping phraseMappingWithEqualPhrase, ImmutableList<INounMapping> similarMappings,
            ImmutableList<IPhraseMapping> similarPhraseMappings, INounMapping nounMapping) {

        if (phraseMappingWithEqualPhrase != null) {

            ImmutableList<INounMapping> similarNounMappingsWithEqualPhrase = similarMappings
                    .select(nm -> getPhraseMappingByNounMapping(nm).equals(phraseMappingWithEqualPhrase));

            return addSimilarNounMappingWithEqualPhrase(nounMapping, similarNounMappingsWithEqualPhrase);

        } else {

            if (!similarPhraseMappings.isEmpty()) {
                return addSimilarNounMappingWithSimilarPhrase(similarMappings, nounMapping);

            } else {
                return addSimilarNounMappingWithDifferentPhrase(nounMapping);
            }
        }
    }

    private INounMapping addDifferentNounMapping(IPhraseMapping phraseMappingWithEqualPhrase, ImmutableList<IPhraseMapping> similarPhraseMappings,
            INounMapping nounMapping) {

        if (phraseMappingWithEqualPhrase != null) {
            return addDifferentNounMappingWithEqualPhrases(nounMapping, phraseMappingWithEqualPhrase);
        } else if (!similarPhraseMappings.isEmpty()) {
            return addDifferentNounMappingWithSimilarPhrase(nounMapping);

        } else {
            return addDifferentNounMappingWithDifferentPhrase(nounMapping);
        }

    }

    private INounMapping addTextualEqualNounMappingWithEqualPhrase(INounMapping nounMapping,
            ImmutableList<INounMapping> textualEqualNounMappingsWithEqualPhrase) {

        mergeAllNounMappings(textualEqualNounMappingsWithEqualPhrase, nounMapping);
        return nounMapping;
    }

    private INounMapping addTextualEqualNounMappingWithSimilarPhrase(INounMapping nounMapping,
            ImmutableList<INounMapping> textualEqualNounMappingsWithSimilarPhrase) {

        // System.out.println(this.toString());

        mergeAllNounMappings(textualEqualNounMappingsWithSimilarPhrase, nounMapping);

        // System.out.println("after addTextEqNM W SimPhrase:");
        // System.out.println(this.toString());
        return nounMapping;
    }

    private INounMapping addTextualEqualNounMappingWithDifferentPhrase(INounMapping nounMapping) {
        // DO: Do not merge. Create a new noun mapping with new phrase mapping
        addNounMappingAddPhraseMapping(nounMapping);
        return nounMapping;
    }

    private INounMapping addSimilarNounMappingWithEqualPhrase(INounMapping nounMapping, ImmutableList<INounMapping> similarNounMappingsWithEqualPhrase) {
        // TODO: Think about this scenario: Occurs in TEAMMATES - check & checks in same
        // phrase (one is wrongly
        // recognized - but what is a useful handling?)
        // For sentence-based TLR this won't make a big difference.
        // (However, it could decrease the similarity between noun mappings)
        mergeAllNounMappings(similarNounMappingsWithEqualPhrase, nounMapping);
        return nounMapping;
    }

    private INounMapping addSimilarNounMappingWithSimilarPhrase(ImmutableList<INounMapping> similarMappings, INounMapping nounMapping) {
        // DO: Merge all to one noun mapping and one phrase mapping

        mergeAllNounMappings(similarMappings, nounMapping);
        return nounMapping;
    }

    private INounMapping addSimilarNounMappingWithDifferentPhrase(INounMapping nounMapping) {
        // DO: add a new noun mapping and a new phrase mapping.
        return addNounMappingAddPhraseMapping(nounMapping);
    }

    private INounMapping addDifferentNounMappingWithEqualPhrases(INounMapping nounMapping, IPhraseMapping phraseMappingWithEqualPhrase) {

        // phraseMappingWithEqualPhrase.merge(new PhraseMapping(nounMapping.getPhrases()));
        // nounMappings.add(wrap(nounMapping));

        System.out.println(this.toString());

        var sharedPhrases = nounMapping.getPhrases().select(p -> phraseMappingWithEqualPhrase.getPhrases().contains(p));
        MutableList<INounMapping> nounMappingsOfSharedPhrases = Lists.mutable.empty();

        for (IPhrase sharedPhrase : sharedPhrases) {
            nounMappingsOfSharedPhrases.addAll(sharedPhrase.getContainedWords().flatCollect(this::getNounMappingsByWord).toList());
        }

        var wordsOfSharedPhrases = sharedPhrases.flatCollect(IPhrase::getContainedWords);

        MutableList<INounMapping> splittedNounMappings = Lists.mutable.empty();

        for (INounMapping nounMappingWithSharedPhrase : nounMappingsOfSharedPhrases) {
            splittedNounMappings.add(nounMappingWithSharedPhrase.split(wordsOfSharedPhrases));
        }

        var phraseMappingsOfExistingNounMappings = splittedNounMappings.flatCollect(this::getPhraseMappingsByNounMapping);

        for (int i = 0; i < splittedNounMappings.size(); i++) {
            nounMapping.merge(splittedNounMappings.get(i));
            nounMappings.remove(wrap(splittedNounMappings.get(i)));
        }

        IPhraseMapping newPhraseMapping = new PhraseMapping(nounMapping.getPhrases());
        phraseMappingsOfExistingNounMappings.forEach(newPhraseMapping::merge);
        phraseMappings.removeAllIterable(phraseMappingsOfExistingNounMappings);

        nounMappings.add(wrap(nounMapping));
        phraseMappings.add(newPhraseMapping);

        System.out.println(this.toString());

        if (invalidState()) {
            int i = 0;
        }

        return nounMapping;

    }

    /**
     * Adds a new noun mapping that has a similar phrase mapping as an already existing noun mapping. However, both
     * phrase mappings do not share same phrases. Therefore, both can be added freshly to the state.
     * 
     * @param nounMapping to be added
     * @return the added noun mapping
     */
    private INounMapping addDifferentNounMappingWithSimilarPhrase(INounMapping nounMapping) {

        return addNounMappingAddPhraseMapping(nounMapping);
    }

    /**
     * Adds the given noun mapping and the phrase mapping to the state.
     * 
     * @param nounMapping to be added
     * @return the added noun mapping
     */
    private INounMapping addDifferentNounMappingWithDifferentPhrase(INounMapping nounMapping) {
        // DO: add noun mapping and phrase mapping
        return addNounMappingAddPhraseMapping(nounMapping);
    }

    public void removeNounMapping(INounMapping nounMapping) {
        ImmutableList<IPhraseMapping> phraseMappingsOfNounMapping = getPhraseMappingsByNounMapping(nounMapping);
        phraseMappingsOfNounMapping.select(pm -> getNounMappingsByPhraseMapping(pm).size() == 1);
    }

    private boolean invalidState() {

        // the size of phraseMappings and nounMappings is equal
        if (nounMappings.flatCollect(nm -> getPhraseMappingsByNounMapping(nm.getElement())).size() > getNounMappings().size()) {
            return true;
        }

        if (nounMappings.size() != getNounMappings().size()) {
            return true;
        }
        if (phraseMappings.size() != getPhraseMappings().size()) {
            return true;
        }

        // every noun mapping should have exactly one phrase mapping
        ImmutableList<INounMapping> currentNounMappings = getNounMappings();

        for (int i = 0; i < currentNounMappings.size(); i++) {
            if (getPhraseMappingsByNounMapping(currentNounMappings.get(i)).size() != 1) {
                return true;
            }

        }

        return false;

    }

    private ElementWrapper<INounMapping> wrap(INounMapping nounMapping) {

        return new ElementWrapper<>(INounMapping.class, nounMapping, nm -> Objects.hash(nm.getReference(), nm.getPhrases()),
                (nm1, nm2) -> (Objects.equals(nm1.getPhrases(), nm2.getPhrases()) && Objects.equals(nm1.getReference(), nm2.getReference())));
    }

    @Override
    public String toString() {
        return "TextExtractionState [NounMappings: \n" + getNounMappings() + "\n PhraseMappings: \n" + getPhraseMappings() + "]";
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional configuration
    }
}
