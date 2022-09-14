/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;
import edu.kit.kastel.mcse.ardoco.core.textextraction.informants.ComputerScienceWordsInformant;

class ComputerScienceWordsAgentTest implements Claimant {
    private ComputerScienceWordsAgent agent;
    private ComputerScienceWordsInformant informant;
    private ImmutableList<String> data;
    private double modifier;

    private NounMapping nounMapping;
    private MyWord invalidWord;
    private TextStateImpl textState;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        var dataRepository = new DataRepository();
        this.agent = new ComputerScienceWordsAgent(dataRepository);
        this.informant = (ComputerScienceWordsInformant) agent.getEnabledPipelineSteps().get(0);

        setData();

        textState = new TextStateImpl();
        var validWord = wordToListOfWord(data.get(0));
        nounMapping = textState.addNounMapping(Sets.immutable.withAll(validWord), MappingKind.NAME, this, 1.0, Lists.immutable.withAll(validWord),
                Lists.immutable.withAll(Arrays.stream(data.get(0).split("\\s+")).toList()), validWord.get(0).getText());
        invalidWord = new MyWord("ASDFWJ", validWord.size());
        MyText text = new MyText(Lists.immutable.withAll(Stream.concat(validWord.stream(), Stream.of(invalidWord)).toList()));
        var preprocessingData = new PreprocessingData(text);
        dataRepository.addData(PreprocessingData.ID, preprocessingData);

        textState.addNounMapping(invalidWord, MappingKind.NAME, this, 1.0);

        dataRepository.addData(TextStateImpl.ID, textState);
    }

    @Test
    void testSetProbability() {
        this.agent.run();
        var nounMappingProbability = nounMapping.getProbability();
        var invalidNounMappingProbability = textState.getNounMappingByWord(invalidWord).getProbability();

        Assertions.assertEquals((1.0 + this.modifier) / 2, nounMappingProbability);
        Assertions.assertEquals(1.0, invalidNounMappingProbability);

    }

    private List<Word> wordToListOfWord(String word) {
        var words = word.split("\\s+", -1);
        List<Word> wordsList = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            wordsList.add(new MyWord(words[i], i));
        }
        return wordsList;
    }

    private void setData() throws NoSuchFieldException, IllegalAccessException {
        // FORCE ENABLE
        var enabledField = this.informant.getClass().getDeclaredField("enabled");
        enabledField.setAccessible(true);
        enabledField.set(this.informant, true);

        var wordsField = this.informant.getClass().getDeclaredField("commonCSWords");
        wordsField.setAccessible(true);
        this.data = (ImmutableList<String>) wordsField.get(this.informant);

        var probField = this.informant.getClass().getDeclaredField("probabilityOfFoundWords");
        probField.setAccessible(true);
        this.modifier = (double) probField.get(this.informant);
    }

    private record MyText(ImmutableList<Word> words) implements Text {

        @Override
        public ImmutableList<Sentence> getSentences() {
            throw new UnsupportedOperationException();
        }
    }

    private record MyWord(String word, int position) implements Word {

        @Override
        public int getSentenceNo() {
            return 0;
        }

        @Override
        public Sentence getSentence() {
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
        public Word getPreWord() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Word getNextWord() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public String getLemma() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Phrase getPhrase() {
            // TODO @Phi: Phrases are now needed for the TextState
            return Mockito.mock(Phrase.class);
        }
    }
}
