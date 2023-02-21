package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.Converter;
import io.github.ardoco.textproviderjson.dto.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class FromJsonTest {

    @Test
    void testFromJson() throws IOException {
        JsonText generatedText = Converter.fromJsonString(Files.readString(Path.of("./src/test/resources/valid-example-text.json")));

        Word expectedWord = new Word();
        expectedWord.setId(1);
        expectedWord.setSentenceNo(1);
        expectedWord.setLemma("hello");
        expectedWord.setText("Hello");
        expectedWord.setPosTag(PosTag.forValue("UH"));

        OutgoingDependency expectedOutDep = new OutgoingDependency();
        expectedOutDep.setTargetWordId(1);
        expectedOutDep.setDependencyType(DependencyType.APPOS);
        List<OutgoingDependency> expectedOutList = new ArrayList<>();
        expectedOutList.add(expectedOutDep);
        expectedWord.setOutgoingDependencies(expectedOutList);

        IncomingDependency expectedInDep = new IncomingDependency();
        expectedInDep.setSourceWordId(1);
        expectedInDep.setDependencyType(DependencyType.APPOS);
        List<IncomingDependency> expectedInList = new ArrayList<>();
        expectedInList.add(expectedInDep);
        expectedWord.setIncomingDependencies(expectedInList);

        List<Word> expectedWords = new ArrayList<>();
        expectedWords.add(expectedWord);

        Sentence expectedSentence = new Sentence();
        expectedSentence.setSentenceNo(1);
        expectedSentence.setText("Hello World!");
        expectedSentence.setConstituencyTree("(ROOT (FRAG (INTJ (UH Hello)) (NP (NNP World)) (. !)))");
        expectedSentence.setWords(expectedWords);

        List<Sentence> expectedSentences = new ArrayList<>();
        expectedSentences.add(expectedSentence);

        JsonText expectedText = new JsonText();
        expectedText.setSentences(expectedSentences);
        Assertions.assertEquals(expectedText, generatedText);
    }

}
