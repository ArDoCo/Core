/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import edu.kit.kastel.mcse.ardoco.core.common.RepositoryHandler;

public class CodeRunnerBaseTest extends RunnerBaseTest {
    protected static final String inputCodeRepository = "https://github.com/ArDoCo/TeaStore.git";

    protected static String inputCode = "../pipeline-core/src/test/resources/code/teastore";

    protected static String commitHash = "bdc49020a55cfa97eaabbb25744fefbc2697defa";

    @BeforeAll
    static void setup() {
        if (System.getenv("testCodeFull") != null) {
            inputCode = "../../temp/code/teastore";
        }

        File codeLocation = new File(inputCode);

        if (!codeLocation.exists()) {
            var successfulClone = RepositoryHandler.shallowCloneRepository(inputCodeRepository, inputCode, commitHash);
            if (!successfulClone) {
                Assertions.fail("Could not clone repository.");
            }
        }
    }

}
