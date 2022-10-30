package edu.kit.kastel.mcse.ardoco.core.text.providers;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.TextProvider;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp.OpenNLPProvider;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ComparisonTest {

    protected static String inputText1 = "src/test/resources/teastore.txt";
    protected static String inputText2 = "src/test/resources/teammates.txt";
    protected static String inputText3 = "src/test/resources/mediastore.txt";
    protected static String inputText4 = "src/test/resources/jabref.txt";
    protected static String inputText5 = "src/test/resources/bigbluebutton.txt";

    @Test
    void posTagTest(){
        TextProvider coreNLPProvider;
        TextProvider openNLPProvider;
        try {
            coreNLPProvider = new CoreNLPProvider(new DataRepository(), new FileInputStream(inputText5));
            openNLPProvider = new OpenNLPProvider(new DataRepository(), new FileInputStream(inputText5));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ImmutableList<Sentence> openSentences = openNLPProvider.getAnnotatedText().getSentences();
        ImmutableList<Sentence> coreSentences = coreNLPProvider.getAnnotatedText().getSentences();
        Assertions.assertEquals(coreSentences.size(), openSentences.size());

        System.out.println("Big Blue Button \n");
        for (int i = 0; i < openSentences.size(); i++) {
            List<POSTag> openPOSTags = openSentences.get(i).getWords().stream().map(Word::getPosTag).toList();
            List<POSTag> corePOSTags = coreSentences.get(i).getWords().stream().map(Word::getPosTag).toList();
            for (int k = 0; k < openPOSTags.size() && k < corePOSTags.size(); k++) {
                if (!openPOSTags.get(k).equals(corePOSTags.get(k))) {
                    System.out.println("Word " + (k + 1) + " in sentence " + (i + 1)
                            + "\n  Word OpenNLP: " + openSentences.get(i).getWords().get(k).getText() + "\t OpenNLP: " + openPOSTags.get(k)
                            + "\n  Word CoreNLP: " + coreSentences.get(i).getWords().get(k).getText() + "\t CoreNLP: " + corePOSTags.get(k) + "\n");
                }
                if (!openSentences.get(i).getWords().get(k).getText().equals(coreSentences.get(i).getWords().get(k).getText())) {
                    break;
                }
            }
        }

    }
}
