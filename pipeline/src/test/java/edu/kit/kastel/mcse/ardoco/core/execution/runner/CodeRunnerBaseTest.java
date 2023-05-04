/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import edu.kit.kastel.mcse.ardoco.core.common.CodeUtils;

public class CodeRunnerBaseTest extends RunnerBaseTest {
    protected static final String inputCodeRepository = "https://github.com/ArDoCo/TeaStore.git";
    protected static final String inputCode = "src/test/resources/code/teastore";

    @BeforeAll
    static void setup() {
        File codeLocation = new File(inputCode);

        if (codeLocation.exists()) {
            CodeUtils.removeCodeFolder(inputCode);
        }

        var successfulClone = CodeUtils.cloneRepository(inputCodeRepository, inputCode);
        if (!successfulClone) {
            Assertions.fail("Could not clone repository.");
        }
    }

    @AfterAll
    static void tearDown() {
        CodeUtils.removeCodeFolder(inputCode);
    }

}
