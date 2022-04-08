/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;

class GenerateTextModels {
    @Disabled
    @ParameterizedTest(name = "Generating {0} (Text)")
    @EnumSource(value = Project.class)
    void generateTextModels(Project project) throws Exception {
        var textFile = project.getTextFile();
        ParseProvider pp = new ParseProvider(new FileInputStream(textFile));
        var text = pp.getAnnotatedText();
        Assertions.assertNotNull(text);

        JsonTextProvider jtp = new JsonTextProvider();
        jtp.addNewText(project.name(), text);
        jtp.saveTexts(new File(textFile.getAbsolutePath() + ".json"));
    }

}
