/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeUtils {
    private static final Logger logger = LoggerFactory.getLogger(CodeUtils.class);

    private CodeUtils() {
        super();
    }

    public static boolean cloneRepository(String repositoryLink, String desiredCodeLocation) {
        File codeLocation;
        try {
            codeLocation = Files.createDirectories(Paths.get(desiredCodeLocation)).toFile();
        } catch (IOException e) {
            logger.warn("An exception occurred when trying to create/locate the desired code location.", e);
            return false;
        }

        try {
            Git.cloneRepository().setURI(repositoryLink).setDirectory(codeLocation).setDepth(1).call();
            return true;
        } catch (GitAPIException e) {
            logger.warn("An error occurred when cloning the repository.", e);
            return false;
        }
    }

    public static boolean removeCodeFolder(String codeLocation) {
        try {
            FileUtils.deleteDirectory(new File(codeLocation));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
