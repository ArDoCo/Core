/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tests.eval;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramsGS;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This enum captures the different case studies that are used for evaluation in the integration
 * tests.
 */
public enum DiagramProject implements GoldStandardDiagramsWithTLR {
    MEDIASTORE(//
            Project.MEDIASTORE, //
            "/benchmark/mediastore/diagrams_2016/goldstandard.json", //
            new ExpectedResults(.87, .93, .9, .99, .89, .99), //
            Set.of(
                    "/benchmark/mediastore/diagrams_2016/ArchitectureWithCache.png"
            ) //
    ), //
    TEASTORE( //
            Project.TEASTORE, //
            "/benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(1, .74, .85, .97, .85, 1), //
            Set.of(
                    "/benchmark/teastore/diagrams_2018/Overview.jpg"
            ) //
    ), //
    TEAMMATES( //
            Project.TEAMMATES, //
            "/benchmark/teammates/diagrams_2023/goldstandard.json", //
            new ExpectedResults(.54, .67, .6, .98, .59, .99), //
            Set.of(
                    "/benchmark/teammates/diagrams_2023/highlevelArchitecture.png",
                    "/benchmark/teammates/diagrams_2023/packageDiagram.png"
            ) //
    ), //
    BIGBLUEBUTTON( //
            Project.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/diagrams_2021/goldstandard.json", //
            new ExpectedResults(.79, .72, .75, .97, .74, .99), //
            Set.of(
                    "/benchmark/bigbluebutton/diagrams_2021/bbb-arch-overview.png"
            ) //
    ), //
    TEASTORE_HISTORICAL( //
            Project.TEASTORE_HISTORICAL, //
            "/benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(1, .92, .96, .99, 0.95, 1), //
            Set.of(
                    "/benchmark/teastore/diagrams_2018/Overview.jpg"
            ) //
    ), //
    TEAMMATES_HISTORICAL( //
            Project.TEAMMATES_HISTORICAL, //
            "/benchmark/teammates/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.56, .71, .63, .98, .62, .99), //
            Set.of(
                    "/benchmark/teammates/diagrams_2015/highlevelArchitecture.png",
                    "/benchmark/teammates/diagrams_2015/packageDiagram.png"
            ) //
    ), //
    BIGBLUEBUTTON_HISTORICAL( //
            Project.BIGBLUEBUTTON_HISTORICAL, //
            "/benchmark/bigbluebutton/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.73, .91, .81, .98, .8, .98), //
            Set.of(
                    "/benchmark/bigbluebutton/diagrams_2015/bbb-arch-overview.png"
            ) //
    );

    private final Project baseProject;
    private final String goldStandardDiagrams;

    private final ExpectedResults expectedDiagramTraceLinkResults;

    private final Set<String> diagramResourceNames;

    private final ArchitectureModelType architectureModelType;
    private final Set<String> resourceNames;

    DiagramProject(Project project, String goldStandardDiagrams,
                   ExpectedResults expectedDiagramTraceLinkResults,
                   Set<String> diagramResourceNames) {
        //We need to keep the paths as well, because the actual files are just temporary at this
        // point due to jar packaging
        this.goldStandardDiagrams = goldStandardDiagrams;
        this.baseProject = project;
        this.expectedDiagramTraceLinkResults = expectedDiagramTraceLinkResults;
        this.diagramResourceNames = diagramResourceNames;
        this.architectureModelType = setupArchitectureModelType();
        var set = new HashSet<>(project.getResourceNames());
        set.add(goldStandardDiagrams);
        set.addAll(diagramResourceNames);
        resourceNames = set;
    }

    @Override
    public String getProjectName() {
        return this.name();
    }

    @Override
    public Set<String> getResourceNames() {
        return resourceNames;
    }

    /**
     * TODO This should probably be part of {@link ArchitectureModelType}
     */
    public Metamodel getMetamodel() {
        return switch (architectureModelType) {
            case PCM, UML -> Metamodel.ARCHITECTURE;
        };
    }

    public ArchitectureModelType getArchitectureModelType() {
        return architectureModelType;
    }

    private ArchitectureModelType setupArchitectureModelType() {
        if (baseProject.getModelResourceName().contains("/pcm/")) {
            return ArchitectureModelType.PCM;
        } else if (baseProject.getModelResourceName().contains("/uml/")) {
            return ArchitectureModelType.UML;
        } else {
            throw new IllegalArgumentException(
                    "The model file could not be resolved to a known ArchitectureModelType. " +
                            "Please " +
                            "comply with the mandatory folder structure!");
        }
    }

    /**
     * Returns an {@link Optional} containing the project that has a name that equals the given
     * name, ignoring case.
     *
     * @param name the name of the project
     * @return the Optional containing the project with the given name or is empty if no such is
     * found.
     */
    public static Optional<DiagramProject> getFromName(String name) {
        for (DiagramProject project : DiagramProject.values()) {
            if (project.name().equalsIgnoreCase(name)) {
                return Optional.of(project);
            }
        }
        return Optional.empty();
    }

    public static List<DiagramProject> getHistoricalProjects() {
        return filterForHistoricalProjects(List.of(values()));
    }

    public static List<DiagramProject> getNonHistoricalProjects() {
        return filterForNonHistoricalProjects(List.of(values()));
    }

    private static <T extends Enum<T>> List<T> filterForHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForNonHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> !p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForProjects(Collection<T> unfilteredProjects,
                                                                 Predicate<T> filter) {
        return unfilteredProjects.stream().filter(filter).toList();
    }

    /**
     * {@return the resource name that represents the diagrams gold standard for this project}
     */
    public String getDiagramsGoldStandardResourceName() {
        return goldStandardDiagrams;
    }

    /**
     * Returns the {@link GoldStandard} for this project.
     *
     * @return the File that represents the gold standard for this project
     */
    public File getDiagramsGoldStandardFile() {
        return ProjectHelper.loadFileFromResources(goldStandardDiagrams);
    }

    /**
     * Returns the expected results from the diagram trace link recovery
     *
     * @return the expectedDiagramTraceLinkResults
     */
    public ExpectedResults getExpectedDiagramTraceLinkResults() {
        return expectedDiagramTraceLinkResults;
    }

    public Set<DiaGSTraceLink> getDiagramTraceLinks(@NotNull List<Sentence> sentences) {
        return getDiagramTraceLinks(sentences, baseProject.getTextResourceName());
    }

    public Map<TraceType, List<DiaGSTraceLink>> getDiagramTraceLinksAsMap(@NotNull List<Sentence> sentences) {
        var traceLinks = getDiagramTraceLinks(sentences);
        return traceLinks.stream().collect(Collectors.groupingBy(DiaGSTraceLink::getTraceType));
    }

    private Set<DiaGSTraceLink> getDiagramTraceLinks(@NotNull List<Sentence> sentences,
                                                     @Nullable String textGoldstandard) {
        return getDiagramsGoldStandard().stream().flatMap(d -> d.getTraceLinks(sentences,
                textGoldstandard).stream()).collect(Collectors.toSet());
    }

    public Set<DiagramGS> getDiagramsGoldStandard() {
        try {
            var objectMapper = new ObjectMapper();
            var file = getDiagramsGoldStandardFile();
            objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramProject.class,
                    this));
            return Set.of(objectMapper.readValue(file, DiagramsGS.class).diagrams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getDiagramResourceNames() {
        return diagramResourceNames;
    }

    public List<Pair<String, File>> getDiagramData() {
        return getDiagramResourceNames().stream().map(rn -> new Pair<>(rn,
                ProjectHelper.loadFileFromResources(rn))).toList();
    }
}

