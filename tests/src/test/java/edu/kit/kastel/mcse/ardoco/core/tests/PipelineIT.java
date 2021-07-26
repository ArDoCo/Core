package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

@RunWith(JUnitPlatform.class)
class PipelineIT {

    private static final String OUTPUT = "src/test/resources/testout";
    private static final String TEXT = "src/test/resources/teastore.txt";
    private static final String MODEL = "src/test/resources/teastore.owl";
    private static final String MODEL_W_TEXT = "src/test/resources/teastore_w_text.owl";
    private static final String NAME = "test_teastore";

    @Test
    @DisplayName("Integration Test with provided text file")
    void pipelineWithTextIT() {
        String[] args = { "-n", NAME, "-m", MODEL, "-t", TEXT, "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test without provided text file but with text in the ontology")
    void pipelineWithProvidedTextOntologyIT() {
        String[] args = { "-n", NAME, "-m", MODEL_W_TEXT, "-p", "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test without provided text file and with wrong ontology")
    void pipelineWithProvidedWrongTextOntologyIT() {
        String[] args = { "-n", NAME, "-m", MODEL, "-p", "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test with wrong text")
    void pipelineWithNonexistentTextIT() {
        String[] args = { "-n", NAME, "-m", MODEL, "-t", "NONEXISTENT", "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Test
    @DisplayName("Integration Test with wrong text")
    void pipelineWithNonexistentModelIT() {
        String[] args = { "-n", NAME, "-m", "NONEXISTENT", "-t", TEXT, "-o", OUTPUT };
        Assertions.assertNotNull(args);
        Pipeline.main(args);
    }

    @Disabled("Disabled for now. Enable, if you need to check if the Ontology provider and INDIRECT are returning the same")
    @Test
    @DisplayName("Compare INDIRECT and Ontology providers")
    void compareIT() {
        var inputText = new File(TEXT);
        IText indirectText = null;
        try {
            ITextConnector textConnector = new ParseProvider(new FileInputStream(inputText));
            indirectText = textConnector.getAnnotatedText();
        } catch (IOException | LunaRunException | LunaInitException e) {
            Assertions.fail("Exception caught while executing ParseProvider.");
        }

        var ontoConnector = new OntologyConnector(new File(MODEL).getAbsolutePath());
        var ontologyTextProvider = OntologyTextProvider.get(ontoConnector);
        ontologyTextProvider.addText(indirectText);

        var ontologyText = ontologyTextProvider.getAnnotatedText();

        var indirectWords = indirectText.getWords();
        var ontologyWords = ontologyText.getWords();
        Assertions.assertEquals(indirectWords.size(), ontologyWords.size());
        Assertions.assertEquals(indirectText.getFirstWord().getText(), ontologyText.getFirstWord().getText());

        for (var i = 0; i < indirectWords.size(); i++) {
            var indirectWord = indirectWords.get(i);
            var ontologyWord = ontologyWords.get(i);

            Assertions.assertEquals(indirectWord.getText(), ontologyWord.getText());
            Assertions.assertEquals(indirectWord.getPosition(), ontologyWord.getPosition());
            Assertions.assertEquals(indirectWord.getSentenceNo(), ontologyWord.getSentenceNo());
            Assertions.assertEquals(indirectWord.getLemma(), ontologyWord.getLemma());
            Assertions.assertEquals(indirectWord.getPosTag(), ontologyWord.getPosTag());
        }
    }

}
