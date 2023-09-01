package edu.kit.kastel.mcse.ardoco.core.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbbreviationDisambiguationHelperTest {
  private final String dbJSON = """
          {
              "abbreviation": "DB",
              "meanings": [
                  "Database",
                  "Decibel"
              ]
          }""";

  @Test
  void get() {
    var abbreviation = AbbreviationDisambiguationHelper.disambiguate("DB");
    assertNotNull(abbreviation);
    assertEquals(abbreviation.size(), Math.min(AbbreviationDisambiguationHelper.LIMIT, 3));
  }

  @Test
  void deserialize() throws JsonProcessingException {
    var abbr = new ObjectMapper().readValue(dbJSON, Disambiguation.class);
    assertEquals("DB", abbr.getAbbreviation());
    assertTrue(abbr.getMeanings().containsAll(List.of("Database", "Decibel")));
    assertEquals(abbr.getMeanings().size(), 2);
  }

  @Test
  void load() {
    var abbreviations = AbbreviationDisambiguationHelper.getInstance().getOrRead();
    assertNotNull(abbreviations);
  }

  @Test
  void crawlAbbreviationsCom() {
    var meanings = AbbreviationDisambiguationHelper.crawlAbbreviationsCom("DB");
    assertNotNull(meanings);
    assertEquals(AbbreviationDisambiguationHelper.LIMIT, meanings.size());
  }

  @Test
  void crawlAcronymFinderCom() {
    var meanings = AbbreviationDisambiguationHelper.crawlAcronymFinderCom("GAE");
    assertNotNull(meanings);
    assertEquals(AbbreviationDisambiguationHelper.LIMIT, meanings.size());
  }

  @Test
  void crawl() {
    //Let's hope no one ever comes up with a sensible meaning for this non-sense. Until then, it
    // will remain as our way to test how we handle no search results.
    var nonSens = AbbreviationDisambiguationHelper.crawl("8DAS8UDZGU23HG1U");
    assertNotNull(nonSens);
    assertTrue(nonSens.getMeanings().isEmpty());
  }

  @Test
  void inOrderTest() {
    assertEquals(3, AbbreviationDisambiguationHelper.containsInOrder("some text", "stt"));
    assertTrue(AbbreviationDisambiguationHelper.containsAllInOrder("some text", "stt"));
    assertEquals(4, AbbreviationDisambiguationHelper.containsInOrder("some text", "smex"));
    assertTrue(AbbreviationDisambiguationHelper.containsAllInOrder("some text", "smex"));
    assertEquals(3, AbbreviationDisambiguationHelper.containsInOrder("some text", "semx"));
    assertFalse(AbbreviationDisambiguationHelper.containsAllInOrder("some text", "semx"));
  }

  @Test
  void shareInitialTest() {
    assertTrue(AbbreviationDisambiguationHelper.shareInitial("ahjkdsshds ousidh bndwb",
            "adfgbodiowdiowhi sdhsdshg"));
    assertFalse(AbbreviationDisambiguationHelper.shareInitial(null, "sth"));
    assertFalse(AbbreviationDisambiguationHelper.shareInitial("sth", null));
    assertFalse(AbbreviationDisambiguationHelper.shareInitial(null, null));
    assertFalse(AbbreviationDisambiguationHelper.shareInitial("abcde", "bcdea"));
  }

  @Test
  void maximumAbbreviationScoreTest() {
    assertEquals(1, AbbreviationDisambiguationHelper.maximumAbbreviationScore("abcd", "a", 1,
            0.5, 0, 0));
    assertEquals(1, AbbreviationDisambiguationHelper.maximumAbbreviationScore("abad", "a", 1,
            0.5, 0, 0));
    assertEquals(1.5, AbbreviationDisambiguationHelper.maximumAbbreviationScore("abcd", "ac", 1,
            0.5, 0, 0));
    assertEquals(2.5, AbbreviationDisambiguationHelper.maximumAbbreviationScore("abcd cd", "acd", 1,
            0.5, 0, 0));
  }
}
