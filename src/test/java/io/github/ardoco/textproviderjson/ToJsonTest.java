package io.github.ardoco.textproviderjson;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class ToJsonTest {

    @Test
    void testToJson() throws IOException {
        Word word1 = new Word();
        word1.setId(1);
        word1.setSentenceNo(1);
        word1.setLemma("hello");
        word1.setText("Hello");
        word1.setPosTag(PosTag.forValue("UH"));

        OutgoingDependency outDep1 = new OutgoingDependency();
        outDep1.setTargetWordId(1);
        outDep1.setDependencyType(DependencyType.APPOS);
        List<OutgoingDependency> outList1 = new ArrayList<>();
        outList1.add(outDep1);
        word1.setOutgoingDependencies(outList1);

        IncomingDependency inDep1 = new IncomingDependency();
        inDep1.setSourceWordId(1);
        inDep1.setDependencyType(DependencyType.APPOS);
        List<IncomingDependency> inList1 = new ArrayList<>();
        inList1.add(inDep1);
        word1.setIncomingDependencies(inList1);

        List<Word> words = new ArrayList<>();
        words.add(word1);

        Sentence sentence1 = new Sentence();
        sentence1.setSentenceNo(1);
        sentence1.setText("Hello World!");
        sentence1.setConstituencyTree("(ROOT (FRAG (INTJ (UH Hello)) (NP (NNP World)) (. !)))");
        sentence1.setWords(words);

        List<Sentence> sentences = new ArrayList<>();
        sentences.add(sentence1);

        JsonText text = new JsonText();
        text.setSentences(sentences);

        Assertions.assertEquals(text, Converter.fromJsonString(Converter.toJsonString(text)));
    }
}
