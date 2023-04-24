/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.java;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.CodeExtractor;

/**
 * An extractor for Java. Extracts a CMTL instance.
 */
public final class JavaExtractor extends CodeExtractor {

    private static final JavaExtractor extractor = new JavaExtractor();

    private JavaExtractor() {
    }

    public static JavaExtractor getExtractor() {
        return extractor;
    }

    /**
     * Extracts a code model, i.e. an CMTL instance, from Java code.
     *
     * @param path the path of the Java project's root directory
     * @return the extracted code model
     */
    @Override
    public CodeModel extractModel(String path) {
        Path directoryPath = Path.of(path);
        Map<String, CompilationUnit> compUnitMap = parseDirectory(directoryPath);
        JavaModel javaModel = new JavaModel(compUnitMap);
        CodeModel codeModel = javaModel.getCodeModel();
        return codeModel;
    }

    private static Map<String, CompilationUnit> parseDirectory(Path dir) {
        ASTParser parser = getJavaParser();
        final String[] sources = getEntries(dir, ".java");
        final String[] encodings = new String[sources.length];
        Arrays.fill(encodings, StandardCharsets.UTF_8.toString());
        final Map<String, CompilationUnit> compilationUnits = new HashMap<>();
        parser.setEnvironment(new String[0], new String[0], new String[0], false);
        parser.createASTs(sources, encodings, new String[0], new FileASTRequestor() {
            @Override
            public void acceptAST(final String sourceFilePath, final CompilationUnit ast) {
                URI sourceFileUri = Path.of(sourceFilePath).toUri();
                String relativeSourceFilePath = dir.toUri().relativize(sourceFileUri).toString();
                compilationUnits.put(relativeSourceFilePath, ast);
            }
        }, new NullProgressMonitor());
        return compilationUnits;
    }

    private static ASTParser getJavaParser() {
        String javaCoreVersion = JavaCore.latestSupportedJavaVersion();
        final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setCompilerOptions(Map.of(JavaCore.COMPILER_SOURCE, javaCoreVersion, JavaCore.COMPILER_COMPLIANCE, javaCoreVersion,
                JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, javaCoreVersion));
        return parser;
    }

    private static String[] getEntries(Path dir, String suffix) {
        try (Stream<Path> paths = Files.walk(dir)) {
            return paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(suffix))
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .map(Path::toString)
                    .toArray(i -> new String[i]);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
