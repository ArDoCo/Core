/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;

class JSONTextProviderGeneratorTest {
    @Test
    @Disabled("Only a generator")
    void generateJSONText() throws Exception {
        CoreNLPProvider textProvider = new CoreNLPProvider(JSONTextProviderGeneratorTest.class.getResourceAsStream("/teastore.txt"));
        var text = textProvider.getAnnotatedText();
        JsonTextProvider jsonTextProvider = new JsonTextProvider();
        var jsonText = jsonTextProvider.addNewText("teastore", text);
        Assertions.assertNotNull(jsonText);
        jsonTextProvider.saveTexts(new File("./src/test/resources/teastore.json"));
    }
}
