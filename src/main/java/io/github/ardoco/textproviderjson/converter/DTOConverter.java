package io.github.ardoco.textproviderjson.converter;

import io.github.ardoco.textproviderjson.dto.SentenceDTO;
import io.github.ardoco.textproviderjson.dto.TextDTO;
import io.github.ardoco.textproviderjson.textobject.SentenceImpl;
import io.github.ardoco.textproviderjson.textobject.TextImpl;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Sentence;
import io.github.ardoco.textproviderjson.textobject.text.Text;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

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
        ImmutableList<Phrase> phrases = Lists.immutable.empty();
        // todo phrases
        return new SentenceImpl(phrases, parentText, (int) sentenceDTO.getSentenceNo(), sentenceDTO.getText());
    }
}
