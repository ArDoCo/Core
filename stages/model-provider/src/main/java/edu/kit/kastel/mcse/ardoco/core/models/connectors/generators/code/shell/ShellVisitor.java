/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.shell;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.python.util.PythonInterpreter;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;

public class ShellVisitor implements FileVisitor<Path> {

    private final Path startingDir;
    private final Set<CodeItem> codeEndpoints;

    public ShellVisitor(Path startingDir) {
        this.startingDir = startingDir;
        codeEndpoints = new HashSet<>();
    }

    public CodeModel getCodeModel() {
        return new CodeModel(codeEndpoints);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String fileName = path.getFileName().toString();
        String code = "";
        try (FileReader reader = new FileReader(path.toFile())) {
            code = IOUtils.toString(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!isShellFile(fileName, code)) {
            return FileVisitResult.CONTINUE;
        }

        String extension = FilenameUtils.getExtension(fileName);
        String fileNameWithoutExtension = FilenameUtils.removeExtension(fileName);
        List<String> pathElements = new ArrayList<>();

        // relativize path
        URI sourceFileUri = path.toUri();
        String relativePathString = startingDir.toUri().relativize(sourceFileUri).toString();
        Path relativePath = Path.of(relativePathString);

        for (int i = 0; i < relativePath.getNameCount() - 1; i++) {
            pathElements.add(relativePath.getName(i).toString());
        }
        CodeCompilationUnit sourceFile = new CodeCompilationUnit(fileNameWithoutExtension, new HashSet<>(), pathElements, extension, ProgrammingLanguage.SHELL);
        codeEndpoints.add(sourceFile);
        return FileVisitResult.CONTINUE;
    }

    private static boolean isShellFile(String fileName, String code) {
        try (PythonInterpreter interpreter = new PythonInterpreter()) {
            // Set variables
            interpreter.set("filename", fileName);
            interpreter.set("code", code);

            // Use Pygments as in Python
            interpreter.exec(
                    "from pygments.lexers import guess_lexer_for_filename\n" + "from pygments.lexers import guess_lexer\n" + "from pygments.lexers import BashLexer\n" + "from pygments.util import ClassNotFound\n" + "lexer_name = ''\n" + "try:\n" + "  lexer = guess_lexer_for_filename(filename, code)\n" + "  lexer_name = lexer.name\n" + "except ClassNotFound:\n" + "  try:\n" + "    lexer = guess_lexer(code)\n" + "    lexer_name = lexer.name\n" + "  except:\n" + "    pass\n" + "except:\n" + "  pass\n");

            // Get the lexer's name that has been set in the variable
            String lexerName = interpreter.get("lexer_name", String.class);
            return lexerName.equals("Bash");
        }
    }
}
