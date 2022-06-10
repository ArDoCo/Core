/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IClaimant;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextState;

class ComputerScienceWordsAgentTest implements IClaimant {
    private ComputerScienceWordsAgent agent;
    private ImmutableList<String> data;
    private double modifier;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        this.agent = new ComputerScienceWordsAgent();
        setData();
    }

    @Test
    void testSetProbability() {

        var validWord = wordToListOfIWord(data.get(0));
        var nounMapping = new NounMapping(Lists.immutable.withAll(validWord), MappingKind.NAME, this, 1.0, List.copyOf(validWord),
                Lists.immutable.withAll(Arrays.stream(data.get(0).split("\\s+")).toList()));
        var invalidWord = new MyWord("ASDFWJ", validWord.size());
        MyText text = new MyText(Lists.immutable.withAll(Stream.concat(validWord.stream(), Stream.of(invalidWord)).toList()));
        TextState ts = new TextState(Map.of());

        ts.addNounMapping(nounMapping, this);
        ts.addNounMapping(invalidWord, MappingKind.NAME, this, 1.0);

        TextAgentData tad = new TextAgentData() {
            @Override
            public IText getText() {
                return text;
            }

            @Override
            public void setTextState(ITextState state) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ITextState getTextState() {
                return ts;
            }

            @Override
            public IData createCopy() {
                throw new UnsupportedOperationException();
            }
        };

        this.agent.execute(tad);

        Assertions.assertEquals((1.0 + this.modifier) / 2, ts.getNounMappingsByWord(validWord.get(0)).get(0).getProbability());
        Assertions.assertEquals(1.0, ts.getNounMappingsByWord(invalidWord).get(0).getProbability());

    }

    private List<IWord> wordToListOfIWord(String word) {
        var words = word.split("\\s+");
        List<IWord> wordsList = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            wordsList.add(new MyWord(words[i], i));
        }
        return wordsList;
    }

    @SuppressWarnings("unchecked")
    private void setData() throws NoSuchFieldException, IllegalAccessException {
        // FORCE ENABLE
        var enabledField = this.agent.getClass().getDeclaredField("enabled");
        enabledField.setAccessible(true);
        enabledField.set(this.agent, true);

        var wordsField = this.agent.getClass().getDeclaredField("commonCSWords");
        wordsField.setAccessible(true);
        this.data = (ImmutableList<String>) wordsField.get(this.agent);

        var probField = this.agent.getClass().getDeclaredField("probabilityOfFoundWords");
        probField.setAccessible(true);
        this.modifier = (double) probField.get(this.agent);
    }

    private static class MyText implements IText {
        private final ImmutableList<IWord> words;

        private MyText(ImmutableList<IWord> words) {
            this.words = words;
        }

        @Override
        public IWord getFirstWord() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<IWord> getWords() {
            return words;
        }

        @Override
        public ImmutableList<ISentence> getSentences() {
            throw new UnsupportedOperationException();
        }
    }

    private static class MyWord implements IWord {
        private final String word;
        private final int pos;

        private MyWord(String word, int pos) {
            this.word = word;
            this.pos = pos;
        }

        @Override
        public int getSentenceNo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IPhrase getPhrase() {
            return new Phrase(this);
        }

        @Override
        public ISentence getSentence() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText() {
            return word;
        }

        @Override
        public POSTag getPosTag() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IWord getPreWord() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IWord getNextWord() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public String getLemma() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<IWord> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<IWord> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }
    }

    private static class Phrase implements IPhrase {
        private final IWord word;

        Phrase(IWord text) {
            this.word = text;
        }

        @Override
        public int getSentenceNo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ISentence getSentence() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getText() {
            return word.getText();
        }

        @Override
        public PhraseType getPhraseType() {
            return PhraseType.X;
        }

        @Override
        public ImmutableList<IWord> getContainedWords() {
            return Lists.immutable.with(word);
        }

        @Override
        public ImmutableList<IPhrase> getSubPhrases() {
            return Lists.immutable.empty();
        }

        @Override
        public boolean isSuperPhraseOf(IPhrase other) {
            return false;
        }

        @Override
        public boolean isSubPhraseOf(IPhrase other) {
            return false;
        }
    }
}
