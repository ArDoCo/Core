/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.*;
import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.*;

/***
 * this class converts a DTO text into an ArDoCo text object
 */
public class DtoToObjectConverter {

    private static final String CONSTITUENCY_TREE_SEPARATOR = " ";
    private static final char CONSTITUENCY_TREE_OPEN_BRACKET = '(';
    private static final char CONSTITUENCY_TREE_CLOSE_BRACKET = ')';

    /**
     * converts the given text DTO into an ArDoCo text object
     * 
     * @param textDTO the text DTO
     * @return the ArDoCo text
     */
    public Text convertText(TextDTO textDTO) {
        TextImpl text = new TextImpl();
        ImmutableList<Sentence> sentences = generateSentences(textDTO, text);
        text.setSentences(sentences);
        return text;
    }

    private ImmutableList<Sentence> generateSentences(TextDTO textDTO, Text parentText) {
        List<SentenceDTO> sentenceDTOs = textDTO.getSentences();
        return Lists.immutable.fromStream(sentenceDTOs.stream().map(sentenceDto -> convertToSentence(sentenceDto, parentText)));
    }

    private Sentence convertToSentence(SentenceDTO sentenceDTO, Text parentText) {
        List<Word> words = sentenceDTO.getWords().stream().map(wordDto -> convertToWord(wordDto, parentText)).toList();
        String constituencyTree = sentenceDTO.getConstituencyTree();
        SentenceImpl sentence = new SentenceImpl((int) sentenceDTO.getSentenceNo() - 1, sentenceDTO.getText(), Lists.immutable.ofAll(words));
        Phrase phrases = parseConstituencyTree(constituencyTree, new ArrayList<>(words));
        sentence.setPhrases(Lists.immutable.of(phrases));
        return sentence;
    }

    public Phrase parseConstituencyTree(String constituencyTree, List<Word> wordsOfSentence) {
        // remove outer brackets
        String tree = constituencyTree.substring(1, constituencyTree.length() - 1);
        PhraseType phraseType = PhraseType.get(tree.split(CONSTITUENCY_TREE_SEPARATOR, 2)[0]);
        // remove phrase type
        String treeWithoutType = tree.split(CONSTITUENCY_TREE_SEPARATOR, 2)[1];

        List<String> subTrees = getSubtrees(treeWithoutType);

        List<Phrase> subPhrases = new ArrayList<>();
        List<Word> words = new ArrayList<>();
        for (String subtree : subTrees) {
            if (isWord(subtree)) {
                words.add(wordsOfSentence.remove(0));
            } else {
                subPhrases.add(parseConstituencyTree(subtree, wordsOfSentence));
            }
        }
        return new PhraseImpl(Lists.immutable.ofAll(words), phraseType, subPhrases);
    }

    private List<String> getSubtrees(String treeWithoutType) {
        List<String> subTrees = new ArrayList<>();
        // iterate through tree to find all subtrees
        while (treeWithoutType.length() > 0) {
            // find next subtree
            int index = 1;
            while (treeWithoutType.substring(0, index).chars().filter(ch -> ch == CONSTITUENCY_TREE_OPEN_BRACKET).count() != treeWithoutType.substring(0, index)
                    .chars()
                    .filter(ch -> ch == CONSTITUENCY_TREE_CLOSE_BRACKET)
                    .count()) {
                index++;
            }
            // number of '(' and ')' is equal -> new subphrase tree found
            subTrees.add(treeWithoutType.substring(0, index));
            if (index == treeWithoutType.length()) {
                treeWithoutType = "";
            } else {
                treeWithoutType = treeWithoutType.substring(index + 1);
            }
        }
        return subTrees;
    }

    private boolean isWord(String tree) {
        return tree.chars().filter(character -> character == CONSTITUENCY_TREE_OPEN_BRACKET).count() == 1;
    }

    private Word convertToWord(WordDTO wordDTO, Text parent) {
        List<DependencyImpl> incomingDep = wordDTO.getIncomingDependencies().stream().map(this::convertIncomingDependency).toList();
        List<DependencyImpl> outgoingDep = wordDTO.getOutgoingDependencies().stream().map(this::convertOutgoingDependency).toList();
        return new WordImpl(parent, (int) wordDTO.getId() - 1, (int) wordDTO.getSentenceNo() - 1, wordDTO.getText(), POSTag.get(wordDTO.getPosTag().toString()),
                wordDTO.getLemma(), incomingDep, outgoingDep);
    }

    private DependencyImpl convertIncomingDependency(IncomingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyTag(), dependencyDTO.getSourceWordId() - 1L);
    }

    private DependencyImpl convertOutgoingDependency(OutgoingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyTag(), dependencyDTO.getTargetWordId() - 1L);
    }
}
