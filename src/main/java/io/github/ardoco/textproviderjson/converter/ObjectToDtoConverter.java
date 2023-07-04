/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;
import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.DependencyImpl;

public class ObjectToDtoConverter {

    private static Logger logger = LoggerFactory.getLogger(ObjectToDtoConverter.class);
    private static final String TREE_SEPARATOR = " ";
    private static final char TREE_OPEN_BRACKET = '(';
    private static final char TREE_CLOSE_BRACKET = ')';

    /**
     * converts an ArDoCo text into a text DTO
     * 
     * @param text the ArDoCo text
     * @return the text DTO
     */
    public TextDTO convertTextToDTO(Text text) {
        TextDTO textDTO = new TextDTO();
        List<SentenceDTO> sentences = generateSentenceDTOs(text.getSentences());
        textDTO.setSentences(sentences);
        return textDTO;
    }

    private List<SentenceDTO> generateSentenceDTOs(ImmutableList<Sentence> sentences) {
        return new ArrayList<>(sentences.toList().stream().map(this::convertToSentenceDTO).toList());
    }

    private SentenceDTO convertToSentenceDTO(Sentence sentence) {
        SentenceDTO sentenceDTO = new SentenceDTO();
        sentenceDTO.setSentenceNo(sentence.getSentenceNumber() + (long) 1);
        sentenceDTO.setText(sentence.getText());
        List<WordDTO> words = generateWordDTOs(sentence.getWords());
        sentenceDTO.setWords(words);
        String tree = convertToConstituencyTrees(sentence);
        sentenceDTO.setConstituencyTree(tree);
        return sentenceDTO;
    }

    private List<WordDTO> generateWordDTOs(ImmutableList<Word> words) {
        return new ArrayList<>(words.toList().stream().map(this::convertToWordDTO).toList());
    }

    private WordDTO convertToWordDTO(Word word) {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setId(word.getPosition() + (long) 1);
        wordDTO.setText(word.getText());
        wordDTO.setLemma(word.getLemma());
        try {
            wordDTO.setPosTag(PosTag.forValue(word.getPosTag().toString()));
        } catch (IOException e) {
            logger.warn("IOException when converting to WordDto.", e);
            return null;
        }
        wordDTO.setSentenceNo(word.getSentenceNo() + (long) 1);
        List<DependencyImpl> inDep = new ArrayList<>();
        List<DependencyImpl> outDep = new ArrayList<>();
        for (DependencyTag depType : DependencyTag.values()) {
            ImmutableList<Word> inDepWords = word.getIncomingDependencyWordsWithType(depType);
            inDep.addAll(inDepWords.toList().stream().map(currentWord -> new DependencyImpl(depType, currentWord.getPosition())).toList());
            ImmutableList<Word> outDepWords = word.getOutgoingDependencyWordsWithType(depType);
            outDep.addAll(outDepWords.toList().stream().map(currentWord -> new DependencyImpl(depType, currentWord.getPosition())).toList());
        }
        List<IncomingDependencyDTO> inDepDTO = generateDepInDTOs(inDep);
        List<OutgoingDependencyDTO> outDepDTO = generateDepOutDTOs(outDep);
        wordDTO.setIncomingDependencies(inDepDTO);
        wordDTO.setOutgoingDependencies(outDepDTO);
        return wordDTO;
    }

    private String convertToConstituencyTrees(Sentence sentence) {
        List<Phrase> rootPhrases = ConverterUtil.getChildPhrases(sentence);
        StringBuilder constituencyTree = new StringBuilder();
        for (Phrase rootPhrase : rootPhrases) {
            constituencyTree.append(convertToSubtree(rootPhrase));
        }
        return constituencyTree.toString();
    }

    private String convertToSubtree(Phrase phrase) {
        List<Word> words = phrase.getContainedWords().toList().stream().filter(x -> x.getPhrase().equals(phrase)).collect(Collectors.toList());
        StringBuilder constituencyTree = new StringBuilder().append(TREE_OPEN_BRACKET);
        constituencyTree.append(phrase.getPhraseType().toString());
        List<Phrase> subphrases = new ArrayList<>(ConverterUtil.getChildPhrases(phrase));
        // since we don't know the order of words and subphrases we have to reconstruct the order by comparing the word index
        while (!words.isEmpty() || !subphrases.isEmpty()) {
            if (subphrases.isEmpty()) {
                // word next
                Word word = words.remove(0);
                constituencyTree.append(TREE_SEPARATOR).append(convertWordToTree(word));
            } else if (words.isEmpty()) {
                // phrase next
                Phrase subphrase = subphrases.remove(0);
                constituencyTree.append(TREE_SEPARATOR).append(convertToSubtree(subphrase));
            } else {
                int wordIndex = words.get(0).getPosition();
                List<Integer> phraseWordIndices = subphrases.get(0).getContainedWords().toList().stream().map(Word::getPosition).toList();
                if (wordIndex < Collections.min(phraseWordIndices)) {
                    // word next
                    Word word = words.remove(0);
                    constituencyTree.append(TREE_SEPARATOR).append(convertWordToTree(word));
                } else {
                    // phrase next
                    Phrase subphrase = subphrases.remove(0);
                    constituencyTree.append(TREE_SEPARATOR).append(convertToSubtree(subphrase));
                }
            }
        }
        constituencyTree.append(TREE_CLOSE_BRACKET);
        return constituencyTree.toString();
    }

    private String convertWordToTree(Word word) {
        return TREE_OPEN_BRACKET + word.getPosTag().toString() + TREE_SEPARATOR + word.getText() + TREE_CLOSE_BRACKET;
    }

    private List<IncomingDependencyDTO> generateDepInDTOs(List<DependencyImpl> dependencies) {
        return new ArrayList<>(dependencies.stream().map(this::convertToDepInDTO).toList());
    }

    private List<OutgoingDependencyDTO> generateDepOutDTOs(List<DependencyImpl> dependencies) {
        return new ArrayList<>(dependencies.stream().map(this::convertToDepOutDTO).toList());
    }

    private IncomingDependencyDTO convertToDepInDTO(DependencyImpl dependency) {
        IncomingDependencyDTO dependencyDTO = new IncomingDependencyDTO();
        dependencyDTO.setDependencyTag(dependency.getDependencyTag());
        dependencyDTO.setSourceWordId(dependency.getWordId() + 1L);
        return dependencyDTO;
    }

    private OutgoingDependencyDTO convertToDepOutDTO(DependencyImpl dependency) {
        OutgoingDependencyDTO dependencyDTO = new OutgoingDependencyDTO();
        dependencyDTO.setDependencyTag(dependency.getDependencyTag());
        dependencyDTO.setTargetWordId(dependency.getWordId() + 1L);
        return dependencyDTO;
    }
}
