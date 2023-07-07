package edu.kit.kastel.mcse.ardoco.core.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        var abbreviation = AbbreviationDisambiguationHelper.get("DB");
        assertNotNull(abbreviation);
        assertEquals(abbreviation.size(), Math.min(AbbreviationDisambiguationHelper.limit, 3));
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var abbr = new ObjectMapper().readValue(dbJSON, AbbreviationDisambiguationHelper.Abbreviation.class);
        assertEquals("DB", abbr.abbreviation());
        assertTrue(abbr.meanings().containsAll(List.of("Database", "Decibel")));
        assertEquals(abbr.meanings().size(), 2);
    }

    @Test
    void read() {
        var abbreviations = AbbreviationDisambiguationHelper.read();
        assertNotNull(abbreviations);
    }

    @Test
    void crawl() {
        var abbreviation = AbbreviationDisambiguationHelper.crawl("DB");
        assertNotNull(abbreviation);
        assertEquals(abbreviation.abbreviation(), "DB");
        assertEquals(abbreviation.meanings().size(), Math.min(AbbreviationDisambiguationHelper.limit, 3));

        //Let's hope no one ever comes up with a sensible meaning for this non-sense. Until then, it will remain as our way to test how we handle no search results.
        var nonSens = AbbreviationDisambiguationHelper.crawl("8DAS8UDZGU23HG1U");
        assertNotNull(nonSens);
        assertTrue(nonSens.meanings().isEmpty());
    }
}
