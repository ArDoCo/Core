/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;

@Disabled("Disabled for CI, only enable if you need to generate text models.")
class GenerateTextModelsTest {

    @ParameterizedTest(name = "Generating {0} (Text)")
    @EnumSource(value = Project.class)
    void generateTextModels(Project project) throws Exception {
        var textFile = project.getTextFile();
        var coreNLPProvider = new CoreNLPProvider(new FileInputStream(textFile));
        var text = coreNLPProvider.getAnnotatedText();
        Assertions.assertNotNull(text);

        var jtp = new JsonTextProvider();
        jtp.addNewText(project.name(), text);
        jtp.saveTexts(new File(textFile.getAbsolutePath() + ".json"));
    }

}
