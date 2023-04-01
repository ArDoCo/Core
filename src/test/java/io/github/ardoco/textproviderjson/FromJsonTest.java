/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.dto.*;

class FromJsonTest {

    @Test
    void testFromJson() throws IOException {
        TextDTO generatedText = JsonConverter.fromJsonString(Files.readString(Path.of("./src/test/resources/valid-example-text.json")));

        WordDTO expectedWord = new WordDTO();
        expectedWord.setId(1);
        expectedWord.setSentenceNo(1);
        expectedWord.setLemma("hello");
        expectedWord.setText("Hello");
        expectedWord.setPosTag(PosTag.forValue("UH"));

        OutgoingDependencyDTO expectedOutDep = new OutgoingDependencyDTO();
        expectedOutDep.setTargetWordId(1);
        expectedOutDep.setDependencyType(DependencyType.APPOS);
        List<OutgoingDependencyDTO> expectedOutList = new ArrayList<>();
        expectedOutList.add(expectedOutDep);
        expectedWord.setOutgoingDependencies(expectedOutList);

        IncomingDependencyDTO expectedInDep = new IncomingDependencyDTO();
        expectedInDep.setSourceWordId(1);
        expectedInDep.setDependencyType(DependencyType.APPOS);
        List<IncomingDependencyDTO> expectedInList = new ArrayList<>();
        expectedInList.add(expectedInDep);
        expectedWord.setIncomingDependencies(expectedInList);

        List<WordDTO> expectedWords = new ArrayList<>();
        expectedWords.add(expectedWord);

        SentenceDTO expectedSentence = new SentenceDTO();
        expectedSentence.setSentenceNo(1);
        expectedSentence.setText("Hello World!");
        expectedSentence.setConstituencyTree("(ROOT (FRAG (INTJ (UH Hello)) (NP (NNP World)) (. !)))");
        expectedSentence.setWords(expectedWords);

        List<SentenceDTO> expectedSentences = new ArrayList<>();
        expectedSentences.add(expectedSentence);

        TextDTO expectedText = new TextDTO();
        expectedText.setSentences(expectedSentences);
        Assertions.assertEquals(expectedText, generatedText);
    }

}
