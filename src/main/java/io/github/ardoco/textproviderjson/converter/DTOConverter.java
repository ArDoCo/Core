package io.github.ardoco.textproviderjson.converter;

import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.DependencyImpl;
import io.github.ardoco.textproviderjson.textobject.SentenceImpl;
import io.github.ardoco.textproviderjson.textobject.TextImpl;
import io.github.ardoco.textproviderjson.textobject.WordImpl;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import io.github.ardoco.textproviderjson.textobject.text.Word;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

/***
 * this class converts a dto text into ardoco text
 */
public class DTOConverter {

    public Text convertJsonText(TextDTO textDTO) {
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
        List<Phrase> phrases = parseConstituencyTree(constituencyTree, words);
        return new SentenceImpl(Lists.immutable.ofAll(phrases), parentText, (int) sentenceDTO.getSentenceNo(), sentenceDTO.getText(), Lists.immutable.ofAll(words));
    }

    private List<Phrase> parseConstituencyTree(String constituencyTree, List<Word> wordsOfSentence) {
        return null;
    }

    private Word convertToWord(WordDTO wordDTO, Text parent) {
        List<DependencyImpl> incomingDep = wordDTO.getIncomingDependencies().stream().map(this::convertIncomingDependency).toList();
        List<DependencyImpl> outgoingDep = wordDTO.getOutgoingDependencies().stream().map(this::convertOutgoingDependency).toList();
        return new WordImpl(parent, (int) wordDTO.getId(), (int) wordDTO.getSentenceNo(), wordDTO.getText(), wordDTO.getPosTag(), wordDTO.getLemma(), incomingDep, outgoingDep);
    }

    private DependencyImpl convertIncomingDependency(IncomingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyType(), dependencyDTO.getSourceWordId());
    }

    private DependencyImpl convertOutgoingDependency(OutgoingDependencyDTO dependencyDTO) {
        return new DependencyImpl(dependencyDTO.getDependencyType(), dependencyDTO.getTargetWordId());
    }
}
