/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import io.github.ardoco.textproviderjson.converter.JsonConverter;
import io.github.ardoco.textproviderjson.dto.*;

class ToJsonTest {

    @Test
    void testToJson() throws IOException {
        WordDTO word1 = new WordDTO();
        word1.setId(1);
        word1.setSentenceNo(1);
        word1.setLemma("hello");
        word1.setText("Hello");
        word1.setPosTag(PosTag.forValue("UH"));

        OutgoingDependencyDTO outDep1 = new OutgoingDependencyDTO();
        outDep1.setTargetWordId(1);
        outDep1.setDependencyTag(DependencyTag.APPOS);
        List<OutgoingDependencyDTO> outList1 = new ArrayList<>();
        outList1.add(outDep1);
        word1.setOutgoingDependencies(outList1);

        IncomingDependencyDTO inDep1 = new IncomingDependencyDTO();
        inDep1.setSourceWordId(1);
        inDep1.setDependencyTag(DependencyTag.APPOS);
        List<IncomingDependencyDTO> inList1 = new ArrayList<>();
        inList1.add(inDep1);
        word1.setIncomingDependencies(inList1);

        List<WordDTO> words = new ArrayList<>();
        words.add(word1);

        SentenceDTO sentence1 = new SentenceDTO();
        sentence1.setSentenceNo(1);
        sentence1.setText("Hello World!");
        sentence1.setConstituencyTree("(ROOT (FRAG (INTJ (UH Hello)) (NP (NNP World)) (. !)))");
        sentence1.setWords(words);

        List<SentenceDTO> sentences = new ArrayList<>();
        sentences.add(sentence1);

        TextDTO text = new TextDTO();
        text.setSentences(sentences);

        String generatedJson = JsonConverter.toJsonString(text);
        Assertions.assertEquals(text, JsonConverter.fromJsonString(generatedJson));
    }
}
