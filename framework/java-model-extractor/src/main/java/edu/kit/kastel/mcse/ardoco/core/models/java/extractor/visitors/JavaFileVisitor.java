/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.java.extractor.visitors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.javaparser.StaticJavaParser;

import edu.kit.kastel.mcse.ardoco.core.models.java.JavaProject;

public class JavaFileVisitor implements FileVisitor<Path> {
    private static final String JAVA_FILE_ENDING = ".java";

    private final JavaProject project;
    private final List<String> filenames = new ArrayList<>();

    public JavaFileVisitor() {
        this.project = new JavaProject();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var fileName = file.getFileName().toString();
        if (fileName.endsWith(JAVA_FILE_ENDING)) {
            filenames.add(fileName);
            new ClassOrInterfaceVisitor().visit(StaticJavaParser.parse(file), project);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    /**
     * @return the project
     */
    public JavaProject getProject() {
        // Calculate Id for Project
        String id = calculateId();
        project.setId(id);
        return project;
    }

    private String calculateId() {
        var data = filenames.stream().sorted().toList().toString();
        UUID uuid = UUID.nameUUIDFromBytes(data.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

}
