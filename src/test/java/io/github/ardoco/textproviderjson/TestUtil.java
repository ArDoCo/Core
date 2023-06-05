/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import io.github.ardoco.textproviderjson.dto.PosTag;
import io.github.ardoco.textproviderjson.dto.SentenceDTO;
import io.github.ardoco.textproviderjson.dto.TextDTO;
import io.github.ardoco.textproviderjson.dto.WordDTO;
import io.github.ardoco.textproviderjson.textobject.PhraseImpl;
import io.github.ardoco.textproviderjson.textobject.SentenceImpl;
import io.github.ardoco.textproviderjson.textobject.TextImpl;
import io.github.ardoco.textproviderjson.textobject.WordImpl;

/**
 * This utility class provides methods to generate test data
 */
public final class TestUtil {

    private TestUtil() {
    }

    /**
     * generates a default textDTO without dependencies between the words
     * 
     * @return a default textDTO
     */
    public static TextDTO generateDefaultDTO() throws IOException {
        WordDTO word1 = new WordDTO();
        word1.setId(1);
        word1.setSentenceNo(1);
        word1.setLemma("this");
        word1.setText("This");
        word1.setPosTag(PosTag.forValue("DT"));

        WordDTO word2 = new WordDTO();
        word2.setId(2);
        word2.setSentenceNo(1);
        word2.setLemma("be");
        word2.setText("is");
        word2.setPosTag(PosTag.forValue("VBZ"));

        WordDTO word3 = new WordDTO();
        word3.setId(3);
        word3.setSentenceNo(1);
        word3.setLemma("I");
        word3.setText("me");
        word3.setPosTag(PosTag.forValue("PRP"));

        WordDTO word4 = new WordDTO();
        word4.setId(4);
        word4.setSentenceNo(1);
        word4.setLemma(".");
        word4.setText(".");
        word4.setPosTag(PosTag.forValue("."));

        List<WordDTO> words = new ArrayList<>(List.of(word1, word2, word3, word4));

        SentenceDTO sentence1 = new SentenceDTO();
        sentence1.setSentenceNo(1);
        sentence1.setText("This is me.");
        sentence1.setConstituencyTree("(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (PRP me))) (. .)))");
        sentence1.setWords(words);

        List<SentenceDTO> sentences = new ArrayList<>();
        sentences.add(sentence1);

        TextDTO text = new TextDTO();
        text.setSentences(sentences);

        return text;
    }

    /**
     * generates a default text object without dependencies between the words
     * 
     * @return the default text object
     */
    public static Text generateDefaultText() {
        TextImpl text = new TextImpl();
        List<WordImpl> words = new ArrayList<>(List.of(new WordImpl(text, 0, 0, "This", POSTag.DETERMINER, "this", new ArrayList<>(), new ArrayList<>()),
                new WordImpl(text, 1, 0, "is", POSTag.VERB_SINGULAR_PRESENT_THIRD_PERSON, "be", new ArrayList<>(), new ArrayList<>()), new WordImpl(text, 2, 0,
                        "me", POSTag.PRONOUN_PERSONAL, "I", new ArrayList<>(), new ArrayList<>()), new WordImpl(text, 3, 0, ".", POSTag.CLOSER, ".",
                                new ArrayList<>(), new ArrayList<>())));

        SentenceImpl sentence1 = new SentenceImpl(0, "This is me.", Lists.immutable.ofAll(words));

        Phrase subsubphrase1 = new PhraseImpl(Lists.immutable.of(words.get(2)), PhraseType.NP, new ArrayList<>());
        List<Phrase> subsubphrases = new ArrayList<>(List.of(subsubphrase1));
        Phrase subphrase1 = new PhraseImpl(Lists.immutable.of(words.get(0)), PhraseType.NP, new ArrayList<>());
        Phrase subphrase2 = new PhraseImpl(Lists.immutable.of(words.get(1)), PhraseType.VP, new ArrayList<>(subsubphrases));
        List<Phrase> subphrases = new ArrayList<>(List.of(subphrase1, subphrase2));
        Phrase phrase1 = new PhraseImpl(Lists.immutable.of(words.get(3)), PhraseType.S, subphrases);
        List<Phrase> phrases = new ArrayList<>(List.of(phrase1));
        Phrase rootPhrase = new PhraseImpl(Lists.immutable.empty(), PhraseType.ROOT, phrases);
        sentence1.setPhrases(Lists.immutable.of(rootPhrase));

        List<Sentence> sentences = new ArrayList<>();
        sentences.add(sentence1);
        text.setSentences(Lists.immutable.ofAll(sentences));
        return text;
    }

}
