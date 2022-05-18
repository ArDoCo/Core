/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;

class JSONTextProviderGeneratorTest {
    @Test
    @Disabled("Only a generator")
    void generateJSONText() throws Exception {
        ParseProvider pp = new ParseProvider(JSONTextProviderGeneratorTest.class.getResourceAsStream("/teastore.txt"));
        var text = pp.getAnnotatedText();
        JsonTextProvider jtp = new JsonTextProvider();
        var jsonText = jtp.addNewText("teastore", text);
        Assertions.assertNotNull(jsonText);
        jtp.saveTexts(new File("./src/test/resources/teastore.json"));
    }
}
