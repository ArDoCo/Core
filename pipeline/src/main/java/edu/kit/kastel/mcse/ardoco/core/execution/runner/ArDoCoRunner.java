/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;

public abstract class ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunner.class);

    private final ArDoCo arDoCo;

    private File outputDirectory;
    protected boolean isSetUp = false;

    protected ArDoCoRunner(String projectName) {
        this.arDoCo = new ArDoCo(projectName);
        outputDirectory = null;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public final ArDoCoResult run() {
        if (this.isSetUp() && outputDirectory != null) {
            return this.getArDoCo().runAndSave(outputDirectory);
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up.");
            return null;
        }
    }

    protected ArDoCo getArDoCo() {
        return this.arDoCo;
    }

    protected void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
