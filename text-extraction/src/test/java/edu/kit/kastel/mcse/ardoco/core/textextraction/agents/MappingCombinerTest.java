package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.IPhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.Phrase;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextState;

class MappingCombinerTest implements IClaimant {

    private MappingCombiner agent;

    private ITextState preTextState;
    private ITextState textState;

    private IWord fox0;
    private IWord dog1;
    private IWord fox2;
    private IWord dog3;
    private IWord hut3;
    private IWord turtle4;

    private IWord doggy5;

    private IWord dog6;

    private IPhrase dogPhrase0;

    private IPhrase dogPhrase1;

    private IPhrase turtlePhrase2;

    private IPhrase dogPhrase3;

    private IPhrase dogPhrase4;

    private IAgent claimant;
    private DataStructure data;

    @BeforeEach
    void setup() {
        this.claimant = new MyAgent();
        this.agent = new MappingCombiner();
        preTextState = new TextState(Map.of());

        IWord a0 = Mockito.mock(IWord.class);
        IWord fast0 = Mockito.mock(IWord.class);
        IWord fox0 = Mockito.mock(IWord.class);
        IWord the1 = Mockito.mock(IWord.class);
        IWord brown1 = Mockito.mock(IWord.class);
        IWord dog1 = Mockito.mock(IWord.class);

        Phrase foxPhrase0 = Mockito.mock(Phrase.class);
        Phrase dogPhrase0 = Mockito.mock(Phrase.class);

        mockPhrase(foxPhrase0, "A fast fox", 0, Lists.immutable.with(a0, fast0, fox0));
        mockPhrase(dogPhrase0, "the brown dog", 0, Lists.immutable.with(the1, brown1, dog1));

        mockWord(a0, "a", "a", 0, foxPhrase0, 0);
        mockWord(fast0, "fast", "fast", 0, foxPhrase0, 1);
        mockWord(fox0, "fox", "fox", 0, foxPhrase0, 2);

        mockWord(the1, "the", "the", 0, dogPhrase0, 6);
        mockWord(brown1, "brown", "brown", 0, dogPhrase0, 7);
        mockWord(dog1, "dog", "dog", 0, dogPhrase0, 8);

        IWord the2 = Mockito.mock(IWord.class);
        IWord lazy2 = Mockito.mock(IWord.class);
        IWord fox2 = Mockito.mock(IWord.class);
        IWord the3 = Mockito.mock(IWord.class);
        IWord brown3 = Mockito.mock(IWord.class);
        IWord dog3 = Mockito.mock(IWord.class);
        IWord hut3 = Mockito.mock(IWord.class);

        Phrase foxPhrase1 = Mockito.mock(Phrase.class);
        Phrase dogPhrase1 = Mockito.mock(Phrase.class);

        mockPhrase(foxPhrase1, "The lazy fox", 1, Lists.immutable.with(the2, lazy2, fox2));
        mockPhrase(dogPhrase1, "the brown dog hut", 1, Lists.immutable.with(the3, brown3, dog3, hut3));

        mockWord(the2, "the", "the", 1, foxPhrase1, 0);
        mockWord(lazy2, "lazy", "lazy", 1, foxPhrase1, 1);
        mockWord(fox2, "fox", "fox", 1, foxPhrase1, 2);

        mockWord(the3, "the", "the", 1, dogPhrase1, 5);
        mockWord(brown3, "brown", "brown", 1, dogPhrase1, 6);
        mockWord(dog3, "dog", "dog", 1, dogPhrase1, 7);
        mockWord(hut3, "hut", "hut", 1, dogPhrase1, 8);

        IWord i4 = Mockito.mock(IWord.class);
        IWord green4 = Mockito.mock(IWord.class);
        IWord turtle4 = Mockito.mock(IWord.class);
        IWord hats4 = Mockito.mock(IWord.class);

        Phrase iPhrase2 = Mockito.mock(Phrase.class);
        Phrase turtlePhrase2 = Mockito.mock(Phrase.class);

        mockPhrase(iPhrase2, "I", 2, Lists.immutable.with(i4));
        mockPhrase(turtlePhrase2, "green turtle hats", 2, Lists.immutable.with(green4, turtle4, hats4));

        mockWord(i4, "I", "i", 2, iPhrase2, 0);
        mockWord(green4, "green", "green", 2, turtlePhrase2, 2);
        mockWord(turtle4, "turtles", "turtles", 2, turtlePhrase2, 3);
        mockWord(hats4, "hats", "hat", 2, turtlePhrase2, 4);

        IWord a5 = Mockito.mock(IWord.class);
        IWord brown5 = Mockito.mock(IWord.class);
        IWord doggy5 = Mockito.mock(IWord.class);
        IWord hut5 = Mockito.mock(IWord.class);
        Phrase dogPhrase3 = Mockito.mock(Phrase.class);

        mockPhrase(dogPhrase3, "A brown doggy hut", 3, Lists.immutable.with(a5, brown5, doggy5, hut5));

        mockWord(a5, "a", "a", 3, dogPhrase3, 0);
        mockWord(brown5, "brown", "brown", 3, dogPhrase3, 1);
        mockWord(doggy5, "doggy", "dog", 3, dogPhrase3, 2);
        mockWord(hut5, "hut", "hut", 3, dogPhrase3, 3);

        IWord green6 = Mockito.mock(IWord.class);
        IWord dog6 = Mockito.mock(IWord.class);
        IWord hats6 = Mockito.mock(IWord.class);
        Phrase dogPhrase4 = Mockito.mock(Phrase.class);

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

        this.data = new DataStructure(null, null);

    }

    @Test
    void copy() {
        preTextState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());

        preTextState.addNounMapping(dog3, MappingKind.NAME, claimant, 0.5);

        Assertions.assertNotEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertNotEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());
    }

    @Test
    void addEqualNounMappingWithEqualPhrase() {

        preTextState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);

        Assertions.assertEquals(1, preTextState.getNounMappingsByWord(fox0).size());
        var fox0NM = preTextState.getNounMappingsByWord(fox0).get(0);
        Assertions.assertNotNull(preTextState.getPhraseMappingByNounMapping(fox0NM));

        preTextState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);
        Assertions.assertEquals(1, preTextState.getNounMappings().size());
        Assertions.assertEquals(1, preTextState.getPhraseMappings().size());

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings());
        Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings());
    }

    @Test
    void addTextualEqualNounMappingWithSimilarPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.NAME, claimant, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));
        Assertions.assertTrue(
                SimilarityUtils.areNounMappingsSimilar(preTextState.getNounMappingsByWord(dog1).get(0), preTextState.getNounMappingsByWord(dog3).get(0)));

        Assertions.assertEquals(2, preTextState.getNounMappings().size());
        Assertions.assertEquals(2, preTextState.getPhraseMappings().size());

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, dog3, textState));
        Assertions.assertTrue(phraseMappingsWereMerged(preTextState, dog1, dog3, textState));
    }

    @Test
    void addTextualEqualNounMappingWithDifferentPhrase() {

        preTextState.addNounMapping(fox0, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(fox2, MappingKind.NAME, claimant, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, fox0, fox2));

        Assertions.assertEquals(2, preTextState.getNounMappings().size());
        Assertions.assertEquals(2, preTextState.getPhraseMappings().size());

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertEquals(2, textState.getNounMappings().size());
        Assertions.assertEquals(2, textState.getPhraseMappings().size());
        Assertions.assertEquals(1, textState.getNounMappingsByWord(fox0).size());
        Assertions.assertEquals(1, textState.getNounMappingsByWord(fox2).size());
    }

    @Test
    void addSimilarNounMappingWithEqualPhrase() {

        IWord alternativeDog = Mockito.mock(IWord.class);
        mockWord(alternativeDog, "doggy", "doggy", 0, dogPhrase0, 0);

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(alternativeDog, MappingKind.NAME, claimant, 0.5);

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertFalse(textState.getNounMappingsByWord(dog1).isEmpty());
        Assertions.assertEquals(1, textState.getNounMappingsByWord(alternativeDog).size());
        Assertions.assertEquals(textState.getNounMappingsByWord(dog1), textState.getNounMappingsByWord(alternativeDog));

        INounMapping doggyNounMapping = textState.getNounMappingsByWord(alternativeDog).get(0);

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

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertTrue(nounMappingsWereMerged(preTextState, dog1, dog3, textState));

        textState.addNounMapping(doggy5, MappingKind.TYPE, claimant, 0.5);
        ITextState textState2 = textState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertTrue(nounMappingsWereMerged(textState, dog1, doggy5, textState2));
        Assertions.assertTrue(nounMappingsWereMerged(textState, dog3, doggy5, textState2));
    }

    @Test
    void addSimilarNounMappingWithSimilarPhraseContainingNoSimilarNounMapping() {

        preTextState.addNounMapping(turtle4, MappingKind.TYPE, claimant, 0.5);
        preTextState.addNounMapping(dog6, MappingKind.NAME, claimant, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, turtle4, dog6));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));
    }

    @Test
    void addSimilarNounMappingWithDifferentPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(doggy5, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, dog1, doggy5));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithEqualPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(dog3, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(hut3, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, dog3));
        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, hut3));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        IPhraseMapping dog1PhraseMapping = textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).get(0);

        Assertions.assertAll(//

                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertEquals(1, textState.getNounMappings().select(nm -> nm.getWords().contains(hut3)).size()),
                () -> Assertions.assertEquals(1, textState.getNounMappings().select(nm -> nm.getWords().contains(dog3)).size()),

                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()),
                () -> Assertions.assertEquals(1, textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase1)).size()),
                () -> Assertions.assertTrue(
                        textState.getNounMappingsByPhraseMapping(textState.getPhraseMappings().select(pm -> pm.getPhrases().contains(dogPhrase0)).get(0))
                                .select(nm -> nm.getWords().contains(dog3))
                                .isEmpty()),

                () -> Assertions.assertEquals(1, textState.getNounMappingsByPhraseMapping(dog1PhraseMapping).select(nm -> nm.getWords().contains(dog3)).size()),
                () -> Assertions.assertEquals(1,
                        textState.getNounMappingsByPhraseMapping(dog1PhraseMapping).select(nm -> nm.getWords().contains(hut3)).size()));
    }

    @Test
    void addDifferentNounMappingWithSimilarPhrase() {

        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(hut3, MappingKind.TYPE, claimant, 0.5);

        Assertions.assertTrue(phraseMappingsAreSimilar(preTextState, dog1, hut3));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));

    }

    @Test
    void addDifferentNounMappingWithDifferentPhrase() {
        preTextState.addNounMapping(dog1, MappingKind.NAME, claimant, 0.5);
        preTextState.addNounMapping(turtle4, MappingKind.NAME, claimant, 0.5);

        Assertions.assertFalse(phraseMappingsAreSimilar(preTextState, dog1, turtle4));

        textState = preTextState.createCopy();
        this.data.setTextState(textState);
        agent.execute(data);

        Assertions.assertAll(//
                () -> Assertions.assertEquals(preTextState.getNounMappings(), textState.getNounMappings()),
                () -> Assertions.assertEquals(preTextState.getPhraseMappings(), textState.getPhraseMappings()));
    }

    private boolean phraseMappingsAreSimilar(ITextState textState, IWord word1, IWord word2) {
        var nm0 = textState.getNounMappingsByWord(word1).get(0);
        var nm1 = textState.getNounMappingsByWord(word2).get(0);

        if (SimilarityUtils.getPhraseMappingSimilarity(textState, textState.getPhraseMappingByNounMapping(nm0), textState.getPhraseMappingByNounMapping(nm1),
                SimilarityUtils.PhraseMappingAggregatorStrategy.MAX_SIMILARITY) > MappingCombiner.MIN_COSINE_SIMILARITY) {
            return true;
        }
        return false;
    }

    private boolean nounMappingsWereMerged(ITextState preTextState, IWord word1, IWord word2, ITextState afterTextState) {

        Assertions.assertNotEquals(preTextState.getNounMappings().size(), afterTextState.getNounMappings().size());
        Assertions.assertFalse(textState.getNounMappingsByWord(word1).isEmpty());
        Assertions.assertFalse(textState.getNounMappingsByWord(word2).isEmpty());
        Assertions.assertEquals(textState.getNounMappingsByWord(word1), textState.getNounMappingsByWord(word2));

        var nounMappings = textState.getNounMappingsByWord(word1);
        Assertions.assertEquals(1, nounMappings.size());
        var nounMapping = nounMappings.get(0);

        Assertions.assertTrue(nounMapping.getWords().contains(word1));
        Assertions.assertTrue(nounMapping.getWords().contains(word2));

        return true;
    }

    private boolean phraseMappingsWereMerged(ITextState preTextState, IWord word1, IWord word2, ITextState afterTextState) {

        var nounMappings1 = afterTextState.getNounMappingsByWord(word1);
        var nounMappings2 = afterTextState.getNounMappingsByWord(word2);
        Assertions.assertEquals(1, nounMappings1.size());
        Assertions.assertEquals(1, nounMappings2.size());
        var nounMapping1 = nounMappings1.get(0);
        var nounMapping2 = nounMappings2.get(0);

        Assertions.assertNotEquals(preTextState.getPhraseMappings().size(), afterTextState.getPhraseMappings().size());
        Assertions.assertEquals(textState.getPhraseMappingByNounMapping(nounMapping1), textState.getPhraseMappingByNounMapping(nounMapping2));

        var phraseMapping = textState.getPhraseMappingByNounMapping(nounMapping1);

        Assertions.assertTrue(phraseMapping.getPhrases().contains(word1.getPhrase()));
        Assertions.assertTrue(phraseMapping.getPhrases().contains(word2.getPhrase()));

        return true;
    }

    private void mockPhrase(Phrase phrase, String text, int sentenceNumber, ImmutableList<IWord> containedWords) {
        Mockito.when(phrase.getText()).thenReturn(text);
        Mockito.when(phrase.getPhraseType()).thenReturn(PhraseType.NP);
        Mockito.when(phrase.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(phrase.getContainedWords()).thenReturn(containedWords);
        Mockito.when(phrase.getPhraseVector()).thenCallRealMethod();
        Mockito.when(phrase.toString()).thenCallRealMethod();
    }

    private void mockWord(IWord word, String text, String lemma, int sentenceNumber, IPhrase phrase, int position) {
        Mockito.when(word.getText()).thenReturn(text);
        Mockito.when(word.getPosition()).thenReturn(position);
        Mockito.when(word.getSentenceNo()).thenReturn(sentenceNumber);
        Mockito.when(word.getLemma()).thenReturn(lemma);
        Mockito.when(word.getPhrase()).thenReturn(phrase);
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

}
