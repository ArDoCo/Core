package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

class TextIT {

    private static final String TEXT_OWL = "src/test/resources/teammates/teammates_w_text.owl";
    private static final String TEXT = "src/test/resources/teammates/teammates.txt";
    private static final String MODEL_OWL = "src/test/resources/teammates/teammates.owl";
    private String tmpOwlFile;

    @Disabled("Only for local testing")
    @Test
    @DisplayName("compare text and ontology")
    void textIT() {

        var inputText = new File(TEXT);
        var textWords = getTextWords(inputText);

        var inputModel = new File(tmpOwlFile);
        var ontologyWords = getOntologyWords(inputModel);

        for (var i = 0; i < textWords.size(); i++) {
            var textWord = textWords.get(i);
            var ontologyWord = ontologyWords.get(i);
            compare(textWord, ontologyWord);
        }
    }

    private ImmutableList<IWord> getTextWords(File inputText) {
        ITextConnector textConnector;
        try {
            textConnector = new ParseProvider(new FileInputStream(inputText));
        } catch (FileNotFoundException | LunaRunException | LunaInitException e) {
            e.printStackTrace();
            Assertions.fail();
            return Lists.immutable.empty();
        }

        var annotatedText = textConnector.getAnnotatedText();

        var ontologyFile = new File(MODEL_OWL);
        var ontoConnector = new OntologyConnector(ontologyFile.getAbsolutePath());
        var ontologyTextProvider = OntologyTextProvider.get(ontoConnector);
        ontologyTextProvider.addText(annotatedText, inputText.getName());
        tmpOwlFile = inputText.getAbsoluteFile().getParent() + File.separator + "tmp.owl";
        ontoConnector.save(tmpOwlFile);

        return annotatedText.getWords();
    }

    private ImmutableList<IWord> getOntologyWords(File ontologyFile) {

        var ontoConnector = new OntologyConnector(ontologyFile.getAbsolutePath());
        var ontologyTextProvider = OntologyTextProvider.get(ontoConnector);
        var ontologyText = ontologyTextProvider.getAnnotatedText();
        var ontologyWords = ontologyText.getWords();
        return ontologyWords;
    }

    private static void compare(IWord word1, IWord word2) {
        if (Objects.equals(word1, word2)) {
            return;
        }

        if (word1 == null || word2 == null) {
            throw new Error(word1 + " - " + word2);
        }

        if (!Objects.equals(word1.getText(), word2.getText())) {
            throw new Error(word1 + " - " + word2);
        }
        if (!Objects.equals(word1.getLemma(), word2.getLemma())) {
            throw new Error(word1 + " - " + word2);
        }

        if (!Objects.equals(word1.getPosition(), word2.getPosition())) {
            throw new Error(word1 + " - " + word2);
        }

        if (!Objects.equals(word1.getPosTag(), word2.getPosTag())) {
            throw new Error(word1 + " - " + word2);
        }

        if (!Objects.equals(word1.getSentenceNo(), word2.getSentenceNo())) {
            throw new Error(word1 + " - " + word2);
        }

        if ((word1.getNextWord() == null && word2.getNextWord() != null) || (word1.getNextWord() != null && word2.getNextWord() == null)) {
            throw new Error(word1 + " - " + word2);
        }
        if ((word1.getPreWord() == null && word2.getPreWord() != null) || (word1.getPreWord() != null && word2.getPreWord() == null)) {
            throw new Error(word1 + " - " + word2);
        }

        for (var type : DependencyTag.values()) {
            var w1d = word1.getWordsThatAreDependencyOfThis(type);
            var w2d = word2.getWordsThatAreDependencyOfThis(type);

            if (!Objects.equals(w1d.size(), w2d.size())) {
                throw new Error(word1 + " - " + word2);
            }

            for (int j = 0; j < w1d.size(); j++) {
                var a = w1d.get(j);
                var b = w2d.get(j);
                if (a.getText() == null || b.getText() == null) {
                    throw new Error(word1 + " - " + word2);
                } else if (!a.getText().equalsIgnoreCase(b.getText())) {
                    throw new Error(word1 + " - " + word2);
                }
            }
        }

        for (var type : DependencyTag.values()) {
            var w1d = word1.getWordsThatAreDependentOnThis(type);
            var w2d = word2.getWordsThatAreDependentOnThis(type);

            if (!Objects.equals(w1d.size(), w2d.size())) {
                throw new Error(word1 + " - " + word2);
            }

            for (int j = 0; j < w1d.size(); j++) {
                var a = w1d.get(j);
                var b = w2d.get(j);
                if (a.getText() == null || b.getText() == null) {
                    throw new Error(word1 + " - " + word2);
                } else if (!a.getText().equalsIgnoreCase(b.getText())) {
                    throw new Error(word1 + " - " + word2);
                }
            }
        }
    }
}
