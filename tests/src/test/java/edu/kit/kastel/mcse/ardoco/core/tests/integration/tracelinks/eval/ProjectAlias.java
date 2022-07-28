/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.tests.Project;

public class ProjectAlias {

    public static String getAlias(Project project) {
        return switch (project) {
        case BIGBLUEBUTTON -> "BBB";
        case TEAMMATES -> "TM";
        case TEASTORE -> "TS";
        case MEDIASTORE -> "MS";
        };
    }

    private ProjectAlias() {
    }

}
