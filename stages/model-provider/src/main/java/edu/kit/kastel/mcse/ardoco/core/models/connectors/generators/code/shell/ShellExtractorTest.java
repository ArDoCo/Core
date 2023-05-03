/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;

public class ShellExtractorTest {

    public static void main(String[] args) {
        var extractor = new ShellExtractor("../evaluation/casestudies/bigbluebutton/code");
        CodeModel model = extractor.extractModel();
        for (Entity endpoint : model.getContent()) {
            System.out.println(endpoint);
        }
    }
}
