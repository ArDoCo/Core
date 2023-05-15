/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import edu.kit.kastel.mcse.ardoco.core.common.CodeUtils;

class CodeRunnerBaseTest extends RunnerBaseTest {
    protected static final String inputCodeRepository = "https://github.com/ArDoCo/TeaStore.git";

    protected static String inputCode = "../pipeline-core/src/test/resources/code/teastore";

    @BeforeAll
    static void setup() {
        File codeLocation = new File(inputCode);

        if (System.getenv("testCodeFull") != null) {
            inputCode = "../../temp/code/teastore";
        }

        if (!codeLocation.exists()) {
            var successfulClone = CodeUtils.shallowCloneRepository(inputCodeRepository, inputCode);
            if (!successfulClone) {
                Assertions.fail("Could not clone repository.");
            }
        }
    }

}
