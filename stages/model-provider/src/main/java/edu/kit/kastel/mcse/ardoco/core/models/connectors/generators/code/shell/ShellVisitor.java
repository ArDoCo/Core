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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;

public class ShellVisitor implements FileVisitor<Path> {
    private static final Logger logger = LoggerFactory.getLogger(ShellVisitor.class);

    private final Path startingDir;
    private final Set<CodeItem> codeEndpoints;
    private final CodeItemRepository codeItemRepository;

    public ShellVisitor(CodeItemRepository codeItemRepository, Path startingDir) {
        this.codeItemRepository = codeItemRepository;
        this.startingDir = startingDir;
        codeEndpoints = new HashSet<>();
    }

    public CodeModel getCodeModel() {
        return new CodeModel(codeItemRepository, codeEndpoints);
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
            logger.warn("Exception when reading file", e);
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
        CodeCompilationUnit sourceFile = new CodeCompilationUnit(codeItemRepository, fileNameWithoutExtension, new HashSet<>(), pathElements, extension,
                ProgrammingLanguage.SHELL);
        codeEndpoints.add(sourceFile);
        return FileVisitResult.CONTINUE;
    }

    private static boolean isShellFile(String fileName, String code) {
        return fileName.endsWith(".sh") || code.startsWith("#!/bin/bash") || code.startsWith("#!/bin/sh") || code.startsWith("#!/usr/bin/env bash");
    }
}
