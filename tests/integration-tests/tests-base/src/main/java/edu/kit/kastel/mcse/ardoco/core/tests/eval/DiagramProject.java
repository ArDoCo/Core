/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.InjectableValues;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramsGS;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.diagrams.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum DiagramProject implements GoldStandardDiagramsWithTLR {
    MEDIASTORE(//
            Project.MEDIASTORE, //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_id_2016.json", //
            new ExpectedResults(.87, .93, .9, .99, .89, .99), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.81, .93, .87, .98, .86, .98), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(.89, .72, .68, .94, .72, .96), //Expected MME results (Mock)
            new ExpectedResults(.89, .72, .68, .94, .72, .96), //Expected MME results (No-Mock)
            new ExpectedResults(.76, .79, .77, .97, .76, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(.76, .79, .77, .97, .76, .99), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/mediastore/diagrams_2016/ArchitectureWithCache.png") //
    ), //
    TEASTORE( //
            Project.TEASTORE, //
            "/benchmark/teastore/goldstandards/goldstandard_sad_id_2018.json", //
            new ExpectedResults(.95, .74, .83, .97, .82, 1), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.65, .74, .69, .93, .65, .95), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(1, .70, .80, .96, .83, 1), //Expected MME results (Mock)
            new ExpectedResults(.96, .70, .78, .95, .80, 1), //Expected MME results (No-Mock)
            new ExpectedResults(1, .74, .85, .98, .85, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(1, .74, .85, .98, .85, 1), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/teastore/diagrams_2018/Overview.jpg") //
    ), //
    TEAMMATES( //
            Project.TEAMMATES, //
            "/benchmark/teammates/goldstandards/goldstandard_sad_id_2023.json", //
            new ExpectedResults(.60, .67, .63, .98, .62, .99), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.37, .25, .30, .97, .29, .99), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(.61, .70, .53, .96, .58, .97), //Expected MME results (Mock)
            new ExpectedResults(.61, .70, .53, .96, .58, .97), //Expected MME results (No-Mock)
            new ExpectedResults(.73, .88, .80, .98, .79, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(.73, .88, .80, .98, .79, .99), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/teammates/diagrams_2023/highlevelArchitecture.png", "/benchmark/teammates/diagrams_2023/packageDiagram.png") //
    ), //
    BIGBLUEBUTTON( //
            Project.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_id_2021.json", //
            new ExpectedResults(.79, .72, .75, .97, .74, .99), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.76, .61, .67, .97, .66, .99), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(.93, .38, .39, .96, .55, .99), //Expected MME results (Mock)
            new ExpectedResults(.93, .38, .39, .96, .55, .99), //Expected MME results (No-Mock)
            new ExpectedResults(.87, .82, .85, .98, .84, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(.87, .82, .85, .98, .84, .99), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/bigbluebutton/diagrams_2021/bbb-arch-overview.png") //
    ), //
    TEASTORE_HISTORICAL( //
            Project.TEASTORE_HISTORICAL, //
            "/benchmark/teastore/goldstandards/goldstandard_sad_id_2018.json", //
            new ExpectedResults(1, .92, .96, .99, .95, 1), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.77, .92, .84, .96, .82, .96), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(1, .91, .91, .99, .95, 1), //Expected MME results (Mock)
            new ExpectedResults(.84, .91, .87, .98, .86, .99), //Expected MME results (No-Mock)
            new ExpectedResults(1, .93, .96, .99, .96, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(1, .93, .96, .99, .96, 1), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/teastore/diagrams_2018/Overview.jpg") //
    ), //
    TEAMMATES_HISTORICAL( //
            Project.TEAMMATES_HISTORICAL, //
            "/benchmark/teammates/goldstandards/goldstandard_sad_id_2015.json", //
            new ExpectedResults(.62, .71, .66, .98, .66, .99), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.51, .39, .44, .98, .43, .99), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(.31, .69, .42, .93, .44, .94), //Expected MME results (Mock)
            new ExpectedResults(.45, .69, .49, .95, .53, .96), //Expected MME results (No-Mock)
            new ExpectedResults(.72, .76, .74, .98, .73, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(.68, .76, .72, .98, .71, .99), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/teammates/diagrams_2015/highlevelArchitecture.png", "/benchmark/teammates/diagrams_2015/packageDiagram.png") //
    ), //
    BIGBLUEBUTTON_HISTORICAL( //
            Project.BIGBLUEBUTTON_HISTORICAL, //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_id_2015.json", //
            new ExpectedResults(.73, .91, .81, .98, .8, .98), //Expected Diagram-Sentence TLR results (Mock)
            new ExpectedResults(.69, .77, .73, .97, .71, .98), //Expected Diagram-Sentence TLR results (No-Mock)
            new ExpectedResults(.07, .20, .10, .73, -0.01, .79), //Expected MME results (Mock)
            new ExpectedResults(.07, .20, .10, .73, -0.01, .79), //Expected MME results (No-Mock)
            new ExpectedResults(.77, .61, .68, .97, .68, 1), //Expected SAD-SAM TLR results (Mock),
            new ExpectedResults(.77, .61, .68, .97, .68, .99), //Expected SAD-SAM TLR results (No-Mock),
            List.of("/benchmark/bigbluebutton/diagrams_2015/bbb-arch-overview.png") //
    );

    private final Project baseProject;
    private final String goldStandardDiagrams;

    private final ExpectedResults expectedDiagramSentenceTlrResultsMock;
    private final ExpectedResults expectedDiagramSentenceTlrResultsNoMock;
    private final ExpectedResults expectedMMEResultsMock;
    private final ExpectedResults expectedMMEResultsNoMock;
    private final ExpectedResults expectedSadSamTlrResultsMock;
    private final ExpectedResults expectedSadSamTlrResultsNoMock;

    private final SortedSet<String> diagramResourceNames;

    private final ArchitectureModelType architectureModelType;
    private final SortedSet<String> resourceNames;

    /**
     * Sole constructor for a project with diagrams.
     *
     * @param project                                 the base {@link Project} that is extended
     * @param goldStandardDiagrams                    the name of the JSON file containing the combined gold standard
     * @param expectedDiagramSentenceTlrResultsMock   the {@link ExpectedResults} for the Diagram-Sentence TLR using gold standard diagrams
     * @param expectedDiagramSentenceTlrResultsNoMock the {@link ExpectedResults} for the Diagram-Sentence TLR
     * @param expectedMMEResultsMock                  the {@link ExpectedResults} for the MME inconsistency detection using gold standard diagrams
     * @param expectedMMEResultsNoMock                the {@link ExpectedResults} for the MME inconsistency detection using the diagram recognition
     * @param expectedSadSamTlrResultsMock            the {@link ExpectedResults} for the SAD SAM TLR using gold standard diagrams
     * @param expectedSadSamTlrResultsNoMock          the {@link ExpectedResults} for the SAD SAM TLR
     * @param diagramResourceNames                    a set of diagram-related resources
     */
    DiagramProject(Project project, String goldStandardDiagrams, ExpectedResults expectedDiagramSentenceTlrResultsMock,
            ExpectedResults expectedDiagramSentenceTlrResultsNoMock, ExpectedResults expectedMMEResultsMock, ExpectedResults expectedMMEResultsNoMock,
            ExpectedResults expectedSadSamTlrResultsMock, ExpectedResults expectedSadSamTlrResultsNoMock, List<String> diagramResourceNames) {
        //We need to keep the paths as well, because the actual files are just temporary at this point due to jar packaging
        this.goldStandardDiagrams = goldStandardDiagrams;
        this.baseProject = project;
        this.expectedDiagramSentenceTlrResultsMock = expectedDiagramSentenceTlrResultsMock;
        this.expectedDiagramSentenceTlrResultsNoMock = expectedDiagramSentenceTlrResultsNoMock;
        this.expectedMMEResultsMock = expectedMMEResultsMock;
        this.expectedMMEResultsNoMock = expectedMMEResultsNoMock;
        this.expectedSadSamTlrResultsMock = expectedSadSamTlrResultsMock;
        this.expectedSadSamTlrResultsNoMock = expectedSadSamTlrResultsNoMock;
        this.diagramResourceNames = new TreeSet<>(diagramResourceNames);
        this.architectureModelType = setupArchitectureModelType();
        var set = new TreeSet<>(project.getResourceNames());
        set.add(goldStandardDiagrams);
        set.addAll(diagramResourceNames);
        resourceNames = set;
    }

    @Override
    public String getProjectName() {
        return this.name();
    }

    @Override
    public SortedSet<String> getResourceNames() {
        return new TreeSet<>(resourceNames);
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
                    "The model file could not be resolved to a known ArchitectureModelType. Please comply with the mandatory folder structure!");
        }
    }

    /**
     * Returns an {@link Optional} containing the project that has a name that equals the given name, ignoring case.
     *
     * @param name the name of the project
     * @return the Optional containing the project with the given name or is empty if no such is found.
     */
    public static Optional<DiagramProject> getFromName(String name) {
        for (DiagramProject project : DiagramProject.values()) {
            if (project.name().equalsIgnoreCase(name)) {
                return Optional.of(project);
            }
        }
        return Optional.empty();
    }

    /**
     * {@return the list of historical diagram projects}
     */
    public static List<DiagramProject> getHistoricalProjects() {
        return filterForHistoricalProjects(List.of(values()));
    }

    /**
     * {@return the list of non-historical diagram projects}
     */
    public static List<DiagramProject> getNonHistoricalProjects() {
        return filterForNonHistoricalProjects(List.of(values()));
    }

    private static <T extends Enum<T>> List<T> filterForHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForNonHistoricalProjects(Collection<T> unfilteredProjects) {
        return filterForProjects(unfilteredProjects, p -> !p.name().endsWith("HISTORICAL"));
    }

    private static <T extends Enum<T>> List<T> filterForProjects(Collection<T> unfilteredProjects, Predicate<T> filter) {
        return unfilteredProjects.stream().filter(filter).toList();
    }

    @Override
    public String getDiagramsGoldStandardResourceName() {
        return goldStandardDiagrams;
    }

    @Override
    public File getDiagramsGoldStandardFile() {
        return ProjectHelper.loadFileFromResources(goldStandardDiagrams);
    }

    @Override
    public ExpectedResults getExpectedDiagramSentenceTlrResultsWithMock() {
        return expectedDiagramSentenceTlrResultsMock;
    }

    @Override
    public ExpectedResults getExpectedDiagramSentenceTlrResults() {
        return expectedDiagramSentenceTlrResultsNoMock;
    }

    @Override
    public ExpectedResults getExpectedMMEResults() {
        return expectedMMEResultsNoMock;
    }

    @Override
    public ExpectedResults getExpectedMMEResultsWithMock() {
        return expectedMMEResultsMock;
    }

    @Override
    public ExpectedResults getExpectedSadSamResults() {
        return expectedSadSamTlrResultsNoMock;
    }

    @Override
    public ExpectedResults getExpectedSadSamResultsWithMock() {
        return expectedSadSamTlrResultsMock;
    }

    @Override
    public Set<DiagramGoldStandardTraceLink> getDiagramTraceLinks(List<Sentence> sentences) {
        return getDiagramTraceLinks(sentences, baseProject.getTextResourceName());
    }

    @Override
    public Map<TraceType, List<DiagramGoldStandardTraceLink>> getDiagramTraceLinksAsMap(List<Sentence> sentences) {
        var traceLinks = getDiagramTraceLinks(sentences);
        return traceLinks.stream().collect(Collectors.groupingBy(DiagramGoldStandardTraceLink::getTraceType));
    }

    private Set<DiagramGoldStandardTraceLink> getDiagramTraceLinks(List<Sentence> sentences, String textGoldstandard) {
        return getDiagramsGoldStandard().stream().flatMap(d -> d.getTraceLinks(sentences, textGoldstandard).stream()).collect(Collectors.toSet());
    }

    @Override
    public Set<DiagramGS> getDiagramsGoldStandard() {
        try {
            var objectMapper = createObjectMapper();
            var file = getDiagramsGoldStandardFile();
            objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramProject.class, this));
            return new LinkedHashSet<>(List.of(objectMapper.readValue(file, DiagramsGS.class).diagrams));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public SortedSet<String> getDiagramResourceNames() {
        return new TreeSet<>(diagramResourceNames);
    }

    @Override
    public List<Pair<String, File>> getDiagramData() {
        return getDiagramResourceNames().stream().map(rn -> new Pair<>(rn, ProjectHelper.loadFileFromResources(rn))).toList();
    }
}
