/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import io.github.ardoco.textproviderjson.PhraseType;
import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.*;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;

/***
 * this class converts a DTO text into an ArDoCo text object
 */
public class DtoToObjectConverter {

    private static final String CONSTITUENCY_TREE_ROOT = "ROOT";
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
        List<Sentence> sentences = sentenceDTOs.stream().map(x -> convertToSentence(x, parentText)).toList();
        return Lists.immutable.ofAll(sentences);
    }

    private Sentence convertToSentence(SentenceDTO sentenceDTO, Text parentText) {
        List<Word> words = sentenceDTO.getWords().stream().map(x -> convertToWord(x, parentText)).toList();
        String constituencyTree = sentenceDTO.getConstituencyTree();
        Sentence sentence = new SentenceImpl(parentText, (int) sentenceDTO.getSentenceNo(), sentenceDTO.getText(), Lists.immutable.ofAll(words));
        Phrase phrases = parseConstituencyTree(constituencyTree, new ArrayList<>(words), sentence);
        sentence.setPhrases(Lists.immutable.of(phrases));
        return sentence;
    }

    public Phrase parseConstituencyTree(String constituencyTree, List<Word> wordsOfSentence, Sentence parent) {
        // cut of root
        String treeWithoutRoot = constituencyTree.substring(2 + CONSTITUENCY_TREE_ROOT.length(), constituencyTree.length() - 1);
        return findSubphrases(treeWithoutRoot, wordsOfSentence, parent);
    }

    private Phrase findSubphrases(String constituencyTree, List<Word> wordsOfSentence, Sentence parent) {
        // cut off outer brackets
        String tree = constituencyTree.substring(1, constituencyTree.length() - 1);
        // extract phrase type
        PhraseType phraseType = PhraseType.get(tree.split(CONSTITUENCY_TREE_SEPARATOR, 2)[0]);
        // cut off phrase type
        String treeWithoutType = tree.split(CONSTITUENCY_TREE_SEPARATOR, 2)[1];

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
        List<Phrase> subPhrases = new ArrayList<>();
        List<Word> words = new ArrayList<>();
        for (String subtree : subTrees) {
            if (isWord(subtree)) {
                words.add(wordsOfSentence.remove(0));
            } else {
                subPhrases.add(findSubphrases(subtree, wordsOfSentence, parent));
            }
        }
        return new PhraseImpl(Lists.immutable.ofAll(words), parent, phraseType, subPhrases);
    }

    private boolean isWord(String tree) {
        return tree.chars().filter(ch -> ch == CONSTITUENCY_TREE_OPEN_BRACKET).count() == 1;
    }

    private Word convertToWord(WordDTO wordDTO, Text parent) {
        List<DependencyImpl> incomingDep = wordDTO.getIncomingDependencies().stream().map(this::convertIncomingDependency).toList();
        List<DependencyImpl> outgoingDep = wordDTO.getOutgoingDependencies().stream().map(this::convertOutgoingDependency).toList();
        return new WordImpl(parent, (int) wordDTO.getId(), (int) wordDTO.getSentenceNo(), wordDTO.getText(), wordDTO.getPosTag(), wordDTO.getLemma(),
                incomingDep, outgoingDep);
    }

    private DependencyImpl convertIncomingDependency(IncomingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyType(), dependencyDTO.getSourceWordId());
    }

    private DependencyImpl convertOutgoingDependency(OutgoingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyType(), dependencyDTO.getTargetWordId());
    }
}
