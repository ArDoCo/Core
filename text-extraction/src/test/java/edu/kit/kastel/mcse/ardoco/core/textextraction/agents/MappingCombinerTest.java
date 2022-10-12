/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.ElementWrapper;
import edu.kit.kastel.mcse.ardoco.core.common.util.PhraseMappingAggregatorStrategy;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.PhraseImpl;
import edu.kit.kastel.mcse.ardoco.core.textextraction.PhraseConcerningTextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;

class MappingCombinerTest implements Claimant {

    // Has to be aligned to MappingCombinerInformant
    private static final double MIN_COSINE_SIMILARITY = 0.4;

    private MappingCombiner agent;

    private TextState preTextState;
    private TextState textState;

    private Word fox0;
    private Word dog1;
    private Word fox2;
    private Word dog3;
    private Word hut3;
    private Word turtle4;

    private Word doggy5;

    private Word dog6;

    private PhraseImpl dogPhrase0;

    private PhraseImpl dogPhrase1;

    private PhraseImpl turtlePhrase2;

    private PhraseImpl dogPhrase3;

    private PhraseImpl dogPhrase4;
    private DataRepository data;

    @BeforeEach
    void setup() {
        this.data = new DataRepository();
        this.agent = new MappingCombiner(data);
        preTextState = new TextStateImpl(PhraseConcerningTextStateStrategy::new);

        Word a0 = Mockito.mock(Word.class);
        Word fast0 = Mockito.mock(Word.class);
        Word fox0 = Mockito.mock(Word.class);
        Word the1 = Mockito.mock(Word.class);
        Word brown1 = Mockito.mock(Word.class);
        Word dog1 = Mockito.mock(Word.class);

        PhraseImpl foxPhrase0 = Mockito.mock(PhraseImpl.class);
        PhraseImpl dogPhrase0 = Mockito.mock(PhraseImpl.class);

        mockPhrase(foxPhrase0, "A fast fox", 0, Lists.immutable.with(a0, fast0, fox0));
        mockPhrase(dogPhrase0, "the brown dog", 0, Lists.immutable.with(the1, brown1, dog1));

        mockWord(a0, "a", "a", 0, foxPhrase0, 0);
        mockWord(fast0, "fast", "fast", 0, foxPhrase0, 1);
        mockWord(fox0, "fox", "fox", 0, foxPhrase0, 2);

        mockWord(the1, "the", "the", 0, dogPhrase0, 6);
        mockWord(brown1, "brown", "brown", 0, dogPhrase0, 7);
        mockWord(dog1, "dog", "dog", 0, dogPhrase0, 8);

        Word the2 = Mockito.mock(Word.class);
        Word lazy2 = Mockito.mock(Word.class);
        Word fox2 = Mockito.mock(Word.class);
        Word the3 = Mockito.mock(Word.class);
        Word brown3 = Mockito.mock(Word.class);
        Word dog3 = Mockito.mock(Word.class);
        Word hut3 = Mockito.mock(Word.class);

        PhraseImpl foxPhrase1 = Mockito.mock(PhraseImpl.class);
        PhraseImpl dogPhrase1 = Mockito.mock(PhraseImpl.class);

        mockPhrase(foxPhrase1, "The lazy fox", 1, Lists.immutable.with(the2, lazy2, fox2));
        mockPhrase(dogPhrase1, "the brown dog hut", 1, Lists.immutable.with(the3, brown3, dog3, hut3));

        mockWord(the2, "the", "the", 1, foxPhrase1, 0);
        mockWord(lazy2, "lazy", "lazy", 1, foxPhrase1, 1);
        mockWord(fox2, "fox", "fox", 1, foxPhrase1, 2);

        mockWord(the3, "the", "the", 1, dogPhrase1, 5);
        mockWord(brown3, "brown", "brown", 1, dogPhrase1, 6);
        mockWord(dog3, "dog", "dog", 1, dogPhrase1, 7);
        mockWord(hut3, "hut", "hut", 1, dogPhrase1, 8);

        Word i4 = Mockito.mock(Word.class);
        Word green4 = Mockito.mock(Word.class);
        Word turtle4 = Mockito.mock(Word.class);
        Word hats4 = Mockito.mock(Word.class);

        PhraseImpl iPhrase2 = Mockito.mock(PhraseImpl.class);
        PhraseImpl turtlePhrase2 = Mockito.mock(PhraseImpl.class);

        mockPhrase(iPhrase2, "I", 2, Lists.immutable.with(i4));
        mockPhrase(turtlePhrase2, "green turtle hats", 2, Lists.immutable.with(green4, turtle4, hats4));

        mockWord(i4, "I", "i", 2, iPhrase2, 0);
        mockWord(green4, "green", "green", 2, turtlePhrase2, 2);
        mockWord(turtle4, "turtles", "turtles", 2, turtlePhrase2, 3);
        mockWord(hats4, "hats", "hat", 2, turtlePhrase2, 4);

        Word a5 = Mockito.mock(Word.class);
        Word brown5 = Mockito.mock(Word.class);
        Word doggy5 = Mockito.mock(Word.class);
        Word hut5 = Mockito.mock(Word.class);
        PhraseImpl dogPhrase3 = Mockito.mock(PhraseImpl.class);

        mockPhrase(dogPhrase3, "A brown doggy hut", 3, Lists.immutable.with(a5, brown5, doggy5, hut5));

        mockWord(a5, "a", "a", 3, dogPhrase3, 0);
        mockWord(brown5, "brown", "brown", 3, dogPhrase3, 1);
        mockWord(doggy5, "doggy", "dog", 3, dogPhrase3, 2);
        mockWord(hut5, "hut", "hut", 3, dogPhrase3, 3);

        Word green6 = Mockito.mock(Word.class);
        Word dog6 = Mockito.mock(Word.class);
        Word hats6 = Mockito.mock(Word.class);
        PhraseImpl dogPhrase4 = Mockito.mock(PhraseImpl.class);

        mockPhrase(dogPhrase4, "green dog hats", 4, Lists.immutable.with(green6, dog6, hats6));

        mockWord(green6, "green", "green", 4, dogPhrase4, 2);
        mockWord(dog6, "dog", "dog", 4, dogPhrase4, 3);
        mockWord(hats6, "hats", "hat", 4, dogPhrase4, 4);

        this.fox0 = fox0;
        this.dog1 = dog1;
        this.fox2 = fox2;
        this.dog3 = dog3;
        this.hut3 = hut3;
        this.turtle4 = turtle4;
        this.doggy5 = doggy5;
        this.dog6 = dog6;
        this.dogPhrase0 = dogPhrase0;
        this.dogPhrase1 = dogPhrase1;
        this.turtlePhrase2 = turtlePhrase2;
        this.dogPhrase3 = dogPhrase3;
        this.dogPhrase4 = dogPhrase4;
    }

    @Test
    void copy() {
        preTextState.addNounMapping(fox0, MappingKind.NAME, this, 0.5);

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());

        preTextState.addNounMapping(dog3, MappingKind.NAME, this, 0.5);

        Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());
    }

    @Test
    void addEqualNounMappingWithEqualPhrase() {

        preTextState.addNounMapping(fox0, MappingKind.NAME, this, 0.5);

        var fox0NM = preTextState.getNounMappingByWord(fox0);
        Assertions.assertNotNull(preTextState.getPhraseMappingByNounMapping(fox0NM));

        preTextState.addNounMapping(fox0, MappingKind.NAME, this, 0.5);
        Assertions.assertEquals(1, preTextState.getNounMappings().size());
        Assertions.assertEquals(1, preTextState.getPhraseMappings().size());

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());
    }

    @Test
    void addTextualEqualNounMappingWithSimilarPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.NAME, this, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));
        Assertions.assertTrue(SimilarityUtils.areNounMappingsSimilar(preTextState.getNounMappingByWord(dog1), preTextState.getNounMappingByWord(dog3)));

        Assertions.assertEquals(2, preTextState.getNounMappings().size());
        Assertions.assertEquals(2, preTextState.getPhraseMappings().size());

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, dog3, textState));
        Assertions.assertTrue(phraseMappingsWereMerged(preTextState, dog1, dog3, textState));
    }

    @Test
    void addTextualEqualNounMappingWithDifferentPhrase() {

        preTextState.addNounMapping(fox0, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(fox2, MappingKind.NAME, this, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, fox0, fox2));

        Assertions.assertEquals(2, preTextState.getNounMappings().size());
        Assertions.assertEquals(2, preTextState.getPhraseMappings().size());

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertEquals(2, textState.getNounMappings().size());
        Assertions.assertEquals(2, textState.getPhraseMappings().size());
    }

    @Test
    void addSimilarNounMappingWithEqualPhrase() {

        Word alternativeDog = Mockito.mock(Word.class);
        mockWord(alternativeDog, "doggy", "doggy", 0, dogPhrase0, 0);

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(alternativeDog, MappingKind.NAME, this, 0.5);

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertNotEquals(null, textState.getNounMappingByWord(dog1));
        Assertions.assertEquals(textState.getNounMappingByWord(dog1), textState.getNounMappingByWord(alternativeDog));

        NounMapping doggyNounMapping = textState.getNounMappingByWord(alternativeDog);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(alternativeDog)), //
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(dog1)), //
                () -> Assertions.assertEquals(1, doggyNounMapping.getPhrases().size())//
        );

        var doggyPhraseMapping = textState.getPhraseMappingByNounMapping(doggyNounMapping);
        Assertions.assertEquals(1, doggyPhraseMapping.getPhrases().size());//

    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingSimilarNounMapping() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, dog3, textState));
        Assertions.assertEquals(1, textState.getNounMappings().size());
    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingNoSimilarNounMapping() {

        preTextState.addNounMapping(turtle4, MappingKind.TYPE, this, 0.5);
        preTextState.addNounMapping(dog6, MappingKind.NAME, this, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, turtle4, dog6));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()), () -> Assertions.assertEquals(preTextState
                        .getPhraseMappings(), textState.getPhraseMappings()));
    }

    @Test
    void addSimilarNounMappingWithDifferentPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(doggy5, MappingKind.TYPE, this, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, dog1, doggy5));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()), () -> Assertions.assertEquals(preTextState
                        .getPhraseMappings(), textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithEqualPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(hut3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));
        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, hut3));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        PhraseMapping dog1PhraseMapping = textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).get(0);

        Assertions.assertAll(//

                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()), () -> Assertions.assertEquals(1, textState
                        .getNounMappings()
                        .select(nm -> nm.getWords().contains(hut3))
                        .size()), () -> Assertions.assertEquals(1, textState.getNounMappings().select(nm -> nm.getWords().contains(dog3)).size()),

                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()), () -> Assertions.assertEquals(1, textState
                        .getPhraseMappings()
                        .select(pm -> pm.getPhrases().contains(dogPhrase1))
                        .size()), () -> Assertions.assertTrue(textState.getNounMappingsByPhraseMapping(textState.getPhraseMappings()
                                .select(pm -> pm.getPhrases().contains(dogPhrase0))
                                .get(0)).select(nm -> nm.getWords().contains(dog3)).isEmpty()),

                () -> Assertions.assertEquals(1, textState.getNounMappingsByPhraseMapping(dog1PhraseMapping).select(nm -> nm.getWords().contains(dog3)).size()),
                () -> Assertions.assertEquals(1, textState.getNounMappingsByPhraseMapping(dog1PhraseMapping)
                        .select(nm -> nm.getWords().contains(hut3))
                        .size()));
    }

    @Test
    void addDifferentNounMappingWithSimilarPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(hut3, MappingKind.TYPE, this, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, hut3));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()), () -> Assertions.assertEquals(preTextState
                        .getPhraseMappings(), textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithDifferentPhrase() {
        preTextState.addNounMapping(dog1, MappingKind.NAME, this, 0.5);
        preTextState.addNounMapping(turtle4, MappingKind.NAME, this, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, dog1, turtle4));

        textState = createCopy(preTextState);
        this.data.addData(TextState.ID, textState);
        agent.run();

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()), () -> Assertions.assertEquals(preTextState
                        .getPhraseMappings(), textState.getPhraseMappings()));
    }

    private boolean phraseMappingsAreSimilar(TextState textState, Word word1, Word word2) {
        var nm0 = textState.getNounMappingByWord(word1);
        var nm1 = textState.getNounMappingByWord(word2);

        return SimilarityUtils.getPhraseMappingSimilarity(textState, textState.getPhraseMappingByNounMapping(nm0), textState.getPhraseMappingByNounMapping(nm1),
                PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MappingCombinerTest.MIN_COSINE_SIMILARITY;
    }

    private boolean nounMappingsWereMerged(TextState preTextState, Word word1, Word word2, TextState afterTextState) {

        Assertions.assertNotEquals(preTextState.getNounMappings().size(), afterTextState.getNounMappings().size());
        Assertions.assertNotNull(textState.getNounMappingByWord(word1));
        Assertions.assertNotNull(textState.getNounMappingByWord(word2));
        Assertions.assertEquals(textState.getNounMappingByWord(word1), textState.getNounMappingByWord(word2));

        var nounMapping = textState.getNounMappingByWord(word1);

        Assertions.assertTrue(nounMapping.getWords().contains(word1));
        Assertions.assertTrue(nounMapping.getWords().contains(word2));

        return true;
    }

    private boolean phraseMappingsWereMerged(TextState preTextState, Word word1, Word word2, TextState afterTextState) {

        var nounMapping1 = afterTextState.getNounMappingByWord(word1);
        var nounMapping2 = afterTextState.getNounMappingByWord(word2);

        Assertions.assertNotEquals(preTextState.getPhraseMappings().size(), afterTextState.getPhraseMappings().size());
        Assertions.assertEquals(textState.getPhraseMappingByNounMapping(nounMapping1), textState.getPhraseMappingByNounMapping(nounMapping2));

        var phraseMapping = textState.getPhraseMappingByNounMapping(nounMapping1);

        Assertions.assertTrue(phraseMapping.getPhrases().contains(word1.getPhrase()));
        Assertions.assertTrue(phraseMapping.getPhrases().contains(word2.getPhrase()));

        return true;
    }

    private void mockPhrase(PhraseImpl phrase, String text, int sentenceNumber, ImmutableList<Word> containedWords) {
        Mockito.when(phrase.getText()).thenReturn(text);
        Mockito.when(phrase.getPhraseType()).thenReturn(PhraseType.NP);
        Mockito.when(phrase.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(phrase.getContainedWords()).thenReturn(containedWords);
        Mockito.when(phrase.getPhraseVector()).thenCallRealMethod();
        Mockito.when(phrase.toString()).thenCallRealMethod();
    }

    private void mockWord(Word word, String text, String lemma, int sentenceNumber, Phrase phrase, int position) {
        Mockito.when(word.getText()).thenReturn(text);
        Mockito.when(word.getPosition()).thenReturn(position);
        Mockito.when(word.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(word.getLemma()).thenReturn(lemma);
        Mockito.when(word.getPhrase()).thenReturn(phrase);
        Mockito.when(word.compareTo(Mockito.any())).thenCallRealMethod();
    }

    private TextState createCopy(TextState textState) {
        TextStateImpl newTextState = new TextStateImpl();

        MutableList<ElementWrapper<NounMapping>> nounMappings = getField(textState, "nounMappings");
        MutableSet<PhraseMapping> phraseMappings = getField(textState, "phraseMappings");

        MutableList<ElementWrapper<NounMapping>> newNounMappings = getField(newTextState, "nounMappings");
        MutableSet<PhraseMapping> newPhraseMappings = getField(newTextState, "phraseMappings");

        newNounMappings.addAll(nounMappings);
        newPhraseMappings.addAll(phraseMappings);
        return newTextState;
    }

    private <T> T getField(Object data, String fieldName) {
        try {
            var field = data.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(data);
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
            throw new Error("Unreachable code!");
        }
    }

}
