/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.*;
import org.eclipse.collections.api.list.ImmutableList;

import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.DependencyImpl;

public class ObjectToDtoConverter {

    private static final String TREE_ROOT = "ROOT";
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
        return new ArrayList<>(sentences.stream().map(this::convertToSentenceDTO).toList());
    }

    private SentenceDTO convertToSentenceDTO(Sentence sentence) {
        SentenceDTO sentenceDTO = new SentenceDTO();
        sentenceDTO.setSentenceNo(sentence.getSentenceNumber());
        sentenceDTO.setText(sentence.getText());
        List<WordDTO> words = generateWordDTOs(sentence.getWords());
        sentenceDTO.setWords(words);
        String tree = convertToConstituencyTree(sentence.getPhrases());
        sentenceDTO.setConstituencyTree(tree);
        return sentenceDTO;
    }

    private List<WordDTO> generateWordDTOs(ImmutableList<Word> words) {
        return new ArrayList<>(words.stream().map(this::convertToWordDTO).toList());
    }

    private WordDTO convertToWordDTO(Word word) {
        WordDTO wordDTO = new WordDTO();
        wordDTO.setId(word.getPosition());
        wordDTO.setText(word.getText());
        wordDTO.setLemma(word.getLemma());
        try {
            wordDTO.setPosTag(PosTag.forValue(word.getPosTag().toString()));
        } catch (IOException e) {
            return null;
        }
        wordDTO.setSentenceNo(word.getSentenceNo());
        List<DependencyImpl> inDep = new ArrayList<>();
        List<DependencyImpl> outDep = new ArrayList<>();
        for (DependencyTag depType : DependencyTag.values()) {
            ImmutableList<Word> inDepWords = word.getIncomingDependencyWordsWithType(depType);
            inDep.addAll(inDepWords.stream().map(x -> new DependencyImpl(depType, x.getPosition())).toList());
            ImmutableList<Word> outDepWords = word.getOutgoingDependencyWordsWithType(depType);
            outDep.addAll(outDepWords.stream().map(x -> new DependencyImpl(depType, x.getPosition())).toList());
        }
        List<IncomingDependencyDTO> inDepDTO = generateDepInDTOs(inDep);
        List<OutgoingDependencyDTO> outDepDTO = generateDepOutDTOs(outDep);
        wordDTO.setIncomingDependencies(inDepDTO);
        wordDTO.setOutgoingDependencies(outDepDTO);
        return wordDTO;
    }

    private String convertToConstituencyTree(ImmutableList<Phrase> phrases) {
        List<String> trees = phrases.stream().map(this::convertToSubtree).toList();
        StringBuilder constituencyTree = new StringBuilder(TREE_OPEN_BRACKET + TREE_ROOT);
        for (String tree : trees) {
            constituencyTree.append(TREE_SEPARATOR).append(tree);
        }
        constituencyTree.append(TREE_CLOSE_BRACKET);
        return constituencyTree.toString();
    }

    private String convertToSubtree(Phrase phrase) {
        StringBuilder constituencyTree = new StringBuilder().append(TREE_OPEN_BRACKET);
        constituencyTree.append(phrase.getPhraseType().toString());
        List<Phrase> subphrases = new ArrayList<>(phrase.getSubPhrases().castToList());
        List<Word> words = new ArrayList<>(phrase.getContainedWords().castToList());
        // since we don't know the order of words and subphrases we have to reconstruct the order by comparing the word index
        while (!subphrases.isEmpty() || !words.isEmpty()) {
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
                List<Integer> phraseWordIndices = subphrases.get(0).getContainedWords().stream().map(Word::getPosition).toList();
                if (wordIndex < Collections.max(phraseWordIndices)) {
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
        dependencyDTO.setSourceWordId(dependency.getWordId());
        return dependencyDTO;
    }

    private OutgoingDependencyDTO convertToDepOutDTO(DependencyImpl dependency) {
        OutgoingDependencyDTO dependencyDTO = new OutgoingDependencyDTO();
        dependencyDTO.setDependencyTag(dependency.getDependencyTag());
        dependencyDTO.setTargetWordId(dependency.getWordId());
        return dependencyDTO;
    }
}
