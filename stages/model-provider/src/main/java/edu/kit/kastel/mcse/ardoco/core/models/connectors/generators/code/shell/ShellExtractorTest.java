/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;

public class ShellExtractorTest {

    public static void main(String[] args) {
        CodeModel model = ShellExtractor.getExtractor().extractModel("../evaluation/casestudies/bigbluebutton/code");
        for (Entity endpoint : model.getContent()) {
            System.out.println(endpoint);
        }
    }
}
