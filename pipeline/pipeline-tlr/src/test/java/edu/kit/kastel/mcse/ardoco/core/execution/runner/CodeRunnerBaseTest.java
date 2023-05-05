/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import edu.kit.kastel.mcse.ardoco.core.common.CodeUtils;

// TODO improve this so this does not have to be in the src/main/java
class CodeRunnerBaseTest extends RunnerBaseTest {
    protected static final String inputCodeRepository = "https://github.com/ArDoCo/TeaStore.git";

    // If you change the folder, make sure to also update the CleanupTest in the module "report"
    protected static final String inputCode = "../../temp/code/teastore";

    @BeforeAll
    static void setup() {
        File codeLocation = new File(inputCode);

        if (!codeLocation.exists()) {
            var successfulClone = CodeUtils.cloneRepository(inputCodeRepository, inputCode);
            if (!successfulClone) {
                Assertions.fail("Could not clone repository.");
            }
        }
    }

}
