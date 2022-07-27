/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import static org.mockito.ArgumentMatchers.any;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.Phrase;

public class AddNounMappingsToTextStateTest {

    private ITextState preTextState;
    private ITextState textState;

    private IWord fox0;
    private IWord dog1;
    private IWord fox2;
    private IWord dog3;
    private IWord hut3;
    private IWord turtle4;

    private IWord doggy5;

    private IWord hut5;

    private IWord dog6;

    private IPhrase dogPhrase0;
    private IPhrase foxPhrase0;

    private IPhrase dogPhrase1;
    private IPhrase foxPhrase1;

    private IPhrase turtlePhrase2;

    private IPhrase dogPhrase3;

    private IPhrase dogPhrase4;

    private ISentence sentence0;
    private IAgent claimant;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        this.claimant = new MyAgent();
        preTextState = new TextState(Map.of());

        IWord a0 = Mockito.mock(IWord.class);
        IWord fast0 = Mockito.mock(IWord.class);
        IWord fox0 = Mockito.mock(IWord.class);
        IWord the1 = Mockito.mock(IWord.class);
        IWord brown1 = Mockito.mock(IWord.class);
        IWord dog1 = Mockito.mock(IWord.class);

        Phrase foxPhrase0 = Mockito.mock(Phrase.class);
        Phrase dogPhrase0 = Mockito.mock(Phrase.class);
        ISentence sentence0 = Mockito.mock(ISentence.class);

        mockSentence(sentence0, 0, "A fast fox is hunted by the brown dog", Lists.immutable.with(foxPhrase0, dogPhrase0));

        mockPhrase(foxPhrase0, "A fast fox", PhraseType.NP, sentence0, 0, Lists.immutable.with(a0, fast0, fox0));
        mockPhrase(dogPhrase0, "the brown dog", PhraseType.NP, sentence0, 0, Lists.immutable.with(the1, brown1, dog1));

        mockWord(a0, "a", "a", sentence0, 0, foxPhrase0, 0);
        mockWord(fast0, "fast", "fast", sentence0, 0, foxPhrase0, 1);
        mockWord(fox0, "fox", "fox", sentence0, 0, foxPhrase0, 2);

        mockWord(the1, "the", "the", sentence0, 0, dogPhrase0, 6);
        mockWord(brown1, "brown", "brown", sentence0, 0, dogPhrase0, 7);
        mockWord(dog1, "dog", "dog", sentence0, 0, dogPhrase0, 8);

        IWord the2 = Mockito.mock(IWord.class);
        IWord lazy2 = Mockito.mock(IWord.class);
        IWord fox2 = Mockito.mock(IWord.class);
        IWord the3 = Mockito.mock(IWord.class);
        IWord brown3 = Mockito.mock(IWord.class);
        IWord dog3 = Mockito.mock(IWord.class);
        IWord hut3 = Mockito.mock(IWord.class);

        Phrase foxPhrase1 = Mockito.mock(Phrase.class);
        Phrase dogPhrase1 = Mockito.mock(Phrase.class);
        ISentence sentence1 = Mockito.mock(ISentence.class);

        mockSentence(sentence1, 1, "The lazy fox jumps over the brown dog hut", Lists.immutable.with(foxPhrase1, dogPhrase1));

        mockPhrase(foxPhrase1, "The lazy fox", PhraseType.NP, sentence1, 1, Lists.immutable.with(the2, lazy2, fox2));
        mockPhrase(dogPhrase1, "the brown dog hut", PhraseType.NP, sentence1, 1, Lists.immutable.with(the3, brown3, dog3, hut3));

        mockWord(the2, "the", "the", sentence1, 1, foxPhrase1, 0);
        mockWord(lazy2, "lazy", "lazy", sentence1, 1, foxPhrase1, 1);
        mockWord(fox2, "fox", "fox", sentence1, 1, foxPhrase1, 2);

        mockWord(the3, "the", "the", sentence1, 1, dogPhrase1, 5);
        mockWord(brown3, "brown", "brown", sentence1, 1, dogPhrase1, 6);
        mockWord(dog3, "dog", "dog", sentence1, 1, dogPhrase1, 7);
        mockWord(hut3, "hut", "hut", sentence0, 1, dogPhrase1, 8);

        IWord i4 = Mockito.mock(IWord.class);
        IWord green4 = Mockito.mock(IWord.class);
        IWord turtle4 = Mockito.mock(IWord.class);
        IWord hats4 = Mockito.mock(IWord.class);

        Phrase iPhrase2 = Mockito.mock(Phrase.class);
        Phrase turtlePhrase2 = Mockito.mock(Phrase.class);
        ISentence sentence2 = Mockito.mock(ISentence.class);

        mockSentence(sentence2, 2, "I like green turtle hats", Lists.immutable.with(iPhrase2, turtlePhrase2));

        mockPhrase(iPhrase2, "I", PhraseType.NP, sentence2, 2, Lists.immutable.with(i4));
        mockPhrase(turtlePhrase2, "green turtle hats", PhraseType.NP, sentence2, 2, Lists.immutable.with(green4, turtle4, hats4));

        mockWord(i4, "I", "i", sentence2, 2, iPhrase2, 0);
        mockWord(green4, "green", "green", sentence2, 2, turtlePhrase2, 2);
        mockWord(turtle4, "turtles", "turtles", sentence2, 2, turtlePhrase2, 3);
        mockWord(hats4, "hats", "hat", sentence2, 2, turtlePhrase2, 4);

        IWord a5 = Mockito.mock(IWord.class);
        IWord brown5 = Mockito.mock(IWord.class);
        IWord doggy5 = Mockito.mock(IWord.class);
        IWord hut5 = Mockito.mock(IWord.class);
        Phrase dogPhrase3 = Mockito.mock(Phrase.class);
        ISentence sentence3 = Mockito.mock(ISentence.class);

        mockSentence(sentence3, 3, "A brown doggy hut", Lists.immutable.with(dogPhrase3));

        mockPhrase(dogPhrase3, "A brown doggy hut", PhraseType.NP, sentence3, 3, Lists.immutable.with(a5, brown5, doggy5, hut5));

        mockWord(a5, "a", "a", sentence3, 3, dogPhrase3, 0);
        mockWord(brown5, "brown", "brown", sentence3, 3, dogPhrase3, 1);
        mockWord(doggy5, "doggy", "dog", sentence3, 3, dogPhrase3, 2);
        mockWord(hut5, "hut", "hut", sentence3, 3, dogPhrase3, 3);

        IWord i6 = Mockito.mock(IWord.class);
        IWord green6 = Mockito.mock(IWord.class);
        IWord dog6 = Mockito.mock(IWord.class);
        IWord hats6 = Mockito.mock(IWord.class);
        Phrase dogPhrase4 = Mockito.mock(Phrase.class);
        ISentence sentence4 = Mockito.mock(ISentence.class);

        mockSentence(sentence4, 4, "I like green dog hats", Lists.immutable.with(dogPhrase4));

        mockPhrase(dogPhrase4, "green dog hats", PhraseType.NP, sentence4, 4, Lists.immutable.with(green6, dog6, hats6));

        mockWord(green6, "green", "green", sentence4, 4, dogPhrase4, 2);
        mockWord(dog6, "dog", "dog", sentence4, 4, dogPhrase4, 3);
        mockWord(hats6, "hats", "hat", sentence4, 4, dogPhrase4, 4);

        this.fox0 = fox0;
        this.dog1 = dog1;
        this.fox2 = fox2;
        this.dog3 = dog3;
        this.hut3 = hut3;
        this.turtle4 = turtle4;
        this.doggy5 = doggy5;
        this.hut5 = hut5;
        this.dog6 = dog6;
        this.dogPhrase0 = dogPhrase0;
        this.foxPhrase0 = foxPhrase0;
        this.dogPhrase1 = dogPhrase1;
        this.foxPhrase1 = foxPhrase1;
        this.turtlePhrase2 = turtlePhrase2;
        this.dogPhrase3 = dogPhrase3;
        this.dogPhrase4 = dogPhrase4;
        this.sentence0 = sentence0;

        preTextState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);

        textState = preTextState.createCopy();

    }

    @Test
    void addEqualNounMappingWithEqualPhrase() {
        // DO: Extend existing noun mapping and phrase mapping with occurrences
        //
        // Assertions: Only one equal noun mapping with same pharse mapping

        textState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);
        Assertions.assertAll(//
                () -> Assertions.assertIterableEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertIterableEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));

        preTextState.addNounMapping(dog1, MappingKind.TYPE, claimant, 0.5);
        Assertions.assertAll(//

                () -> Assertions.assertIterableEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertIterableEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));
    }

    @Test
    void addTextualEqualNounMappingWithSimilarPhrase() {
        // DO: Extend existing noun mapping and phrase mapping with occurrences
        //
        // Assertions: Only one equal noun mapping with similar phrase mapping

        ImmutableList<INounMapping> previousNounMappings = preTextState.getNounMappingsByWord(dog1);
        Assertions.assertEquals(1, previousNounMappings.size());
        INounMapping previousNounMapping = previousNounMappings.get(0);

        ImmutableList<IPhraseMapping> previousPhraseMappings = textState.getPhraseMappingsByNounMapping(previousNounMapping);
        Assertions.assertEquals(1, previousPhraseMappings.size());
        IPhraseMapping previousPhraseMapping = previousPhraseMappings.get(0);

        textState.addNounMapping(dog3, MappingKind.NAME, claimant, 0.5);

        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, textState, dog3));
    }

    @Test
    void addEqualNounMappingWithDifferentPhrase() {
        // DO: Do not merge. Create a new noun mapping with new phrase mapping
        //
        // Options: Merging would also be a possibility here.
        //
        // Argumentation: We want to follow a more conservative strategy.

        textState.addNounMapping(fox2, MappingKind.NAME, claimant, 0.5);

        ImmutableList<INounMapping> fox2NounMappings = textState.getNounMappingsByWord(fox2);
        Assertions.assertEquals(1, fox2NounMappings.size());
        INounMapping fox2NounMapping = fox2NounMappings.get(0);

        ImmutableList<IPhraseMapping> fox2PhraseMappings = textState.getPhraseMappingsByNounMapping(fox2NounMapping);

        Assertions.assertEquals(1, fox2PhraseMappings.size());
        IPhraseMapping fox2PhraseMapping = fox2PhraseMappings.get(0);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(fox2PhraseMapping.getPhrases().contains(foxPhrase1)),
                () -> Assertions.assertEquals(1, fox2PhraseMapping.getPhrases().size()));
    }

    @Test
    void addSimilarNounMappingWithEqualPhrase() {
        // DO: This state should not be possible and should throw an
        // IllegalStateException.
        //
        // Argumentation: Since same phrase mappings are currently defined for the same
        // vector.
        //
        // Options: Extending both, noun mapping and phrase mapping, would be an idea
        // here, if it would not be an
        // illegal state.

        IWord alternativeDog = Mockito.mock(IWord.class);

        mockWord(alternativeDog, "doggy", "doggy", sentence0, 0, dogPhrase0, 0);

        // TODO: Is not illegal - but it should be decided what should be done with it!

        textState.addNounMapping(alternativeDog, MappingKind.NAME, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(!textState.getNounMappingsByWord(dog1).isEmpty()),
                () -> Assertions.assertTrue(textState.getNounMappingsByWord(alternativeDog).size() == 1),
                () -> Assertions.assertIterableEquals(textState.getNounMappingsByWord(dog1), textState.getNounMappingsByWord(alternativeDog))//
        );

        INounMapping dogNounMapping = textState.getNounMappingsByWord(dog1).get(0);
        INounMapping doggyNounMapping = textState.getNounMappingsByWord(alternativeDog).get(0);

        Assertions.assertAll(//
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(alternativeDog)), //
                () -> Assertions.assertTrue(doggyNounMapping.getWords().contains(dog1)), //
                () -> Assertions.assertEquals(1, doggyNounMapping.getPhrases().size())//
        );

        var doggyPhraseMappings = textState.getPhraseMappingsByNounMapping(doggyNounMapping);

        Assertions.assertAll(//
                () -> Assertions.assertEquals(1, doggyPhraseMappings.size()), //
                () -> Assertions.assertEquals(1, doggyPhraseMappings.flatCollect(pm -> pm.getPhrases()).size())//
        );

    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingSimilarNounMapping() {
        // Attention: n possible similar noun mappings and phrases
        //
        // DO: Merge all to one noun mapping and one phrase mapping
        //
        // Options: An option would also to create additional noun mappings for extended
        // versions.
        //
        // Argumentation: We follow currently a more conservative strategy.
        //
        // Attention: Adding additional noun mappings would change the conditions for
        // the other situations.

        textState.addNounMapping(dog3, MappingKind.TYPE, claimant, 0.5);
        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, textState, dog3));

        ITextState pre2TextState = textState.createCopy();

        textState.addNounMapping(doggy5, MappingKind.TYPE, claimant, 0.5);
        Assertions.assertTrue(nounMappingsWereMerged(pre2TextState, dog1, textState, doggy5));
        Assertions.assertTrue(nounMappingsWereMerged(pre2TextState, dog3, textState, doggy5));

    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingNoSimilarNounMapping() {
        // Scenario: State [("big brown fox","fox"), ("small blue bear", "bear")] +
        // ("small blue foxgloves","foxgloves")
        //
        // DO: add a new noun mapping and a new phrase mapping.
        //
        // Options: It could become interesting when lowering the similarity of noun
        // mappings in this case

        // the brown dog -> dog
        // I like green turtle hats -> turtle
        // I like green dog hats -> dog

        textState.addNounMapping(turtle4, MappingKind.TYPE, claimant, 0.5);
        textState.addNounMapping(dog6, MappingKind.NAME, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(dog6)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase4)).size() == 1));

    }

    @Test
    void addSimilarNounMappingWithDifferentPhrase() {
        // DO: Add new noun mapping and new phrase mapping
        //
        // Options: Merging would also be a possibility here.
        //
        // Argumentation: Following a conservative way analogue to previous cases

        textState.addNounMapping(doggy5, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(doggy5)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase3)).size() == 1));

    }

    @Test
    void addDifferentNounMappingWithEqualPhrase() {
        // DO: split (if necessary) already existing phrase with shared noun mapping
        // Do: add noun mapping and phrase mapping - extend with shared noun mapping
        //
        // Argumentation: Detecting a combination of noun mappings

        textState.addNounMapping(dog3, MappingKind.NAME, claimant, 0.5);
        textState.addNounMapping(hut3, MappingKind.TYPE, claimant, 0.5);

        IPhraseMapping dog1PhraseMapping = textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).get(0);

        Assertions.assertAll(//

                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(hut3)).size() == 1),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(dog3)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).size() == 1),
                () -> Assertions.assertFalse(
                        !textState.getNounMappingsByPhraseMapping(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase0)).get(0))
                                .select(nm -> nm.getWords().contains(dog3))
                                .isEmpty()),

                () -> Assertions.assertTrue(textState.getNounMappingsByPhraseMapping(dog1PhraseMapping).select(nm -> nm.getWords().contains(dog3)).size() == 1),
                () -> Assertions
                        .assertTrue(textState.getNounMappingsByPhraseMapping(dog1PhraseMapping).select(nm -> nm.getWords().contains(hut3)).size() == 1));
    }

    @Test
    void addDifferentNounMappingWithSimilarPhraseContainingNounMapping() {
        // DO: add noun mapping and phrase mapping
        //
        // Options: Merging would be a possibility here. Could also be done dependent on
        // a lowering of similarity.
        //
        // Argumentation: Follow a conservative way analogue to the previous cases
        textState.addNounMapping(hut3, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(hut3)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).size() == 1));

    }

    @Test
    void addDifferentNounMappingWithSimilarPhraseContainingNoNounMapping() {
        // DO: add noun mapping and phrase mapping
        //
        // Argumentation: Phrase Mapping should contain the noun mapping or at least
        // some similar one.

        textState.addNounMapping(hut3, MappingKind.NAME, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(hut3)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).size() == 1));

    }

    @Test
    void addDifferentNounMappingWithDifferentPhrase() {
        // DO: add noun mapping and phrase mapping
        //
        // Argumentation: There are no respective mappings in the state.

        textState.addNounMapping(turtle4, MappingKind.NAME, claimant, 0.5);

        Assertions.assertAll(//
                () -> Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertTrue(textState.getNounMappings().select(nm -> nm.getWords().contains(turtle4)).size() == 1),

                () -> Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertTrue(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(turtlePhrase2)).size() == 1));

    }

    private boolean nounMappingsWereMerged(ITextState preTextState, IWord preWord, ITextState afterTextState, IWord addedWord) {

        Assertions.assertEquals(preTextState.getNounMappings().size(), afterTextState.getNounMappings().size());

        Assertions.assertEquals(1, textState.getNounMappingsByWord(preWord).size());
        INounMapping originNounMapping = textState.getNounMappingsByWord(preWord).get(0);
        Assertions.assertEquals(1, preTextState.getNounMappingsByWord(preWord).size());
        INounMapping previousNounMapping = preTextState.getNounMappingsByWord(preWord).get(0);

        Assertions.assertEquals(previousNounMapping.getWords().size() + 1, originNounMapping.getWords().size());

        ImmutableList<INounMapping> extendedNounMappings = textState.getNounMappingsByWord(addedWord);
        Assertions.assertEquals(1, extendedNounMappings.size());
        INounMapping extendedNounMapping = extendedNounMappings.get(0);

        Assertions.assertEquals(originNounMapping, extendedNounMapping);

        phraseMappingsWereMerged(preTextState, previousNounMapping, afterTextState, originNounMapping, extendedNounMapping);

        return true;
    }

    private boolean phraseMappingsWereMerged(ITextState preTextState, INounMapping preNounMapping, ITextState afterTextState, INounMapping originNounMapping,
            INounMapping extendedNounMapping) {

        Assertions.assertEquals(preTextState.getPhraseMappings().size(), afterTextState.getPhraseMappings().size());

        ImmutableList<IPhraseMapping> originPhraseMappings = textState.getPhraseMappingsByNounMapping(originNounMapping);
        Assertions.assertEquals(1, originPhraseMappings.size());
        IPhraseMapping originPhraseMapping = originPhraseMappings.get(0);
        Assertions.assertEquals(1, preTextState.getPhraseMappingsByNounMapping(preNounMapping).size());
        IPhraseMapping previousNounMapping = preTextState.getPhraseMappingsByNounMapping(preNounMapping).get(0);

        Assertions.assertEquals(previousNounMapping.getPhrases().size() + 1, originPhraseMapping.getPhrases().size());

        ImmutableList<IPhraseMapping> extendedPhraseMappings = textState.getPhraseMappingsByNounMapping(extendedNounMapping);
        Assertions.assertEquals(1, extendedPhraseMappings.size());
        IPhraseMapping extendedPhraseMapping = extendedPhraseMappings.get(0);

        Assertions.assertEquals(originPhraseMapping, extendedPhraseMapping);

        return true;
    }

    private static class MyAgent extends TextAgent {

        @Override
        protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(TextAgentData data) {
            throw new UnsupportedOperationException();
        }
    }

    private void mockSentence(ISentence sentence, int sentenceNumber, String text, ImmutableList<IPhrase> phrases) {
        Mockito.when(sentence.getSentenceNumber()).thenReturn(sentenceNumber);
        Mockito.when(sentence.getText()).thenReturn(text);
        Mockito.when(sentence.getPhrases()).thenReturn(phrases);
        Mockito.when(sentence.isEqualTo(any())).thenCallRealMethod();
    }

    private void mockPhrase(Phrase phrase, String text, PhraseType phraseType, ISentence sentence, int sentenceNumber, ImmutableList<IWord> containedWords) {
        Mockito.when(phrase.getText()).thenReturn(text);
        Mockito.when(phrase.getSentence()).thenReturn(sentence);
        Mockito.when(phrase.getPhraseType()).thenReturn(phraseType);
        Mockito.when(phrase.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(phrase.getContainedWords()).thenReturn(containedWords);
        Mockito.when(phrase.getPhraseVector()).thenCallRealMethod();
        Mockito.when(phrase.toString()).thenCallRealMethod();
    }

    private void mockWord(IWord word, String text, String lemma, ISentence sentence, int sentenceNumber, IPhrase phrase, int position) {
        Mockito.when(word.getText()).thenReturn(text);
        Mockito.when(word.getPosition()).thenReturn(position);
        Mockito.when(word.getSentence()).thenReturn(sentence);
        Mockito.when(word.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(word.getLemma()).thenReturn(lemma);
        Mockito.when(word.getPhrase()).thenReturn(phrase);
    }

}
