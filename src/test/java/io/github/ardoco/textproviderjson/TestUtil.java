package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.dto.*;
import io.github.ardoco.textproviderjson.textobject.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {

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

        List<WordDTO> words = new ArrayList<>();
        words.add(word1);

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

//    public static Text generateDefaultText() {
//
//    }
//
//    public static String generateDefaultJson() {
//
//    }
}
