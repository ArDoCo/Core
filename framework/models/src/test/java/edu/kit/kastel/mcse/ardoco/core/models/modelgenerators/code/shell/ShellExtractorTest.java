package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code.shell;

import edu.kit.kastel.mcse.ardoco.core.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.models.cmtl.CodeModel;

public class ShellExtractorTest {

    public static void main(String[] args) {
        CodeModel model = ShellExtractor.getExtractor().extractModel("../evaluation/casestudies/bigbluebutton/code");
        for (Entity endpoint : model.getContent()) {
            System.out.println(endpoint);
        }
    }
}
