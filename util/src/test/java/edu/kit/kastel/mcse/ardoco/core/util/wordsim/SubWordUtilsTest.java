package edu.kit.kastel.mcse.ardoco.core.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.SubWordUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubWordUtilsTest {

    record Pair(String word, String... subWords) {
        List<String> subWordList() { return Arrays.stream(subWords).toList(); }
    }

    private static final List<Pair> KNOWN_SUB_WORDS = List.of(
            new Pair("MediaAccess", "Media", "Access"),
            new Pair("MainComponent", "Main", "Component"),
            new Pair("FileStorage", "File", "Storage"),
            new Pair("BigBlueButton API", "Big", "Blue", "Button", "API"), // maybe this should be ["BigBlueButton", "API"]
            new Pair("AutomatedActionFactory", "Automated", "Action", "Factory"),
            new Pair("WebRTC-SFU", "Web", "RTC", "SFU"),
            new Pair("WebUi", "Web", "Ui"),
            new Pair("x.datatransfer", "x", "datatransfer"),
            new Pair("E2E tests", "E2E", "tests"),
            new Pair("E2E tests", "E2E", "tests"),
            new Pair("GAE Datastore", "GAE", "Datastore")
    );

    private static final List<String> NON_SUBWORD_WORDS = List.of(
            "Tree",
            "Apple",
            "dog",
            ".",
            "",
            "-",
            " ",
            ". ",
            " -",
            "E2E",
            "IO"
    );

    @Test
    public void testGoldStandard() {
        for (Pair pair : KNOWN_SUB_WORDS) {
            assertTrue(SubWordUtils.hasSubWords(pair.word), pair.word);
            assertEquals(pair.subWordList(), SubWordUtils.getSubWords(pair.word));
        }
    }

    @Test
    public void testNonSubwords() {
        for (String nonSubwordWord : NON_SUBWORD_WORDS) {
            assertFalse(SubWordUtils.hasSubWords(nonSubwordWord), nonSubwordWord);
            assertEquals(List.of(nonSubwordWord), SubWordUtils.getSubWords(nonSubwordWord));
        }
    }

    @Test
    @Disabled
    public void manualTest() {
        String word = "E2E";
        System.out.printf("Has sub words: %s%n", SubWordUtils.hasSubWords(word));
        System.out.printf("Sub words: %s%n", SubWordUtils.getSubWords(word));
    }

}
