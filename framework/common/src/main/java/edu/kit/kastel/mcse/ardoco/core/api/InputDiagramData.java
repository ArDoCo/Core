/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * This {@link PipelineStepData} gives access to diagrams associated with the project to analyze.
 */
public class InputDiagramData implements PipelineStepData {
    public static final String ID = "InputDiagramData";
    private static final List<String> ALLOWED_FILE_TYPES = List.of("jpg", "png", "jpeg");

    private String pathToDiagrams;

    private List<Pair<String, File>> diagramFiles;

    /**
     * Create the data by a directory of image files.
     *
     * @param pathToDiagrams the path to a directory containing diagrams and sketches
     */
    public InputDiagramData(String pathToDiagrams) {
        this.pathToDiagrams = Objects.requireNonNull(pathToDiagrams);
    }

    /**
     * Create the data by a directory of image files.
     *
     * @param diagramDirectory the directory containing diagrams and sketches
     */
    public InputDiagramData(File diagramDirectory) {
        this(Objects.requireNonNull(diagramDirectory).getAbsolutePath());
    }

    /**
     * Create the data by a list of image files.
     *
     * @param diagramFiles the diagram files
     */
    public InputDiagramData(List<Pair<String, File>> diagramFiles) {
        Objects.requireNonNull(diagramFiles);
        this.diagramFiles = diagramFiles;
    }

    /**
     * Get all image files of the given directory (not recursive).
     *
     * @return a list of image files ordered by name
     */
    public List<Pair<String, File>> getDiagramData() {
        if (diagramFiles != null)
            return diagramFiles;
        if (pathToDiagrams == null)
            return List.of();
        File directoryOfDiagrams = new File(pathToDiagrams);
        if (!directoryOfDiagrams.exists() || !directoryOfDiagrams.isDirectory()) {
            return List.of();
        }
        File[] allFiles = directoryOfDiagrams.listFiles();
        if (allFiles == null)
            return List.of();
        List<Pair<String, File>> diagrams = Arrays.stream(allFiles)
                .filter(File::isFile)
                .filter(f -> ALLOWED_FILE_TYPES.stream().anyMatch(t -> f.getName().toLowerCase().endsWith("." + t)))
                .sorted(Comparator.comparing(File::getName))
                .map(f -> new Pair<>(f.getName(), f))
                .toList();
        logger.info("Found {} diagrams to consider.", diagrams.size());
        return diagrams;
    }

}
