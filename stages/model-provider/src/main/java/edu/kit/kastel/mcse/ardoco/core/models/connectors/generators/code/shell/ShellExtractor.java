package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.CodeExtractor;

public final class ShellExtractor extends CodeExtractor {

    private static final ShellExtractor extractor = new ShellExtractor();

    private ShellExtractor() {
    }

    public static ShellExtractor getExtractor() {
        return extractor;
    }

    @Override
    public CodeModel extractModel(String path) {
        CodeModel codeModel = parseCode(new File(path));
        return codeModel;
    }

    private static CodeModel parseCode(File file) {
        Path startingDir = Paths.get(file.toURI());
        ShellVisitor shellScriptVisitor = new ShellVisitor(startingDir);
        // walk all files and run the ShellScriptVisitor
        try {
            Files.walkFileTree(startingDir, shellScriptVisitor);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return shellScriptVisitor.getCodeModel();
    }
}
