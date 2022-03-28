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

import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextState;

public class ComputerScienceWordsAgentTest {
    private ComputerScienceWordsAgent agent;
    private ImmutableList<String> data;
    private double modifier;

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        this.agent = new ComputerScienceWordsAgent();
        setData();
    }

    @Test
    public void testSetProbability() {

        var validWord = wordToListOfIWord(data.get(0));
        var nounMapping = new NounMapping(Lists.immutable.withAll(validWord), MappingKind.NAME, null, 1.0, List.copyOf(validWord),
                Lists.immutable.withAll(Arrays.stream(data.get(0).split("\\s+")).toList()));
        var invalidWord = new MyWord("ASDFWJ", validWord.size());
        MyText text = new MyText(Lists.immutable.withAll(Stream.concat(validWord.stream(), Stream.of(invalidWord)).toList()));
        TextState ts = new TextState(Map.of());

        ts.addNounMapping(nounMapping, null);
        ts.addName(invalidWord, null, 1.0);

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

        Assertions.assertEquals((1.0 + this.modifier) / 2, nounMapping.getProbability());
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

    private void setData() throws NoSuchFieldException, IllegalAccessException {
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
        public ImmutableList<ICorefCluster> getCorefClusters() {
            throw new UnsupportedOperationException();
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
        public ImmutableList<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }
    }
}
