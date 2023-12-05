package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedDiagram;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.AnnotatedGraph;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.DiagramProject;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.refactoring.Refactoring;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.UmlExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.AllLanguagesExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;

/**
 * Base class for all evaluation tests that require diagrams.
 */
public class EvaluationBase {
    protected static final double PARTIAL_SELECTION_MIN = 0.05;
    protected static final double PARTIAL_SELECTION_MAX = 0.25;
    protected static final double REFACTORING_RATIO = 0.25;

    protected static final String PIPELINE_OUTPUT = "src/test/resources/pipeline_out";
    protected static final String TEST_OUTPUT = "src/test/resources/test_out";
    protected static final Logger logger = LoggerFactory.getLogger(EvaluationBase.class);

    protected FileWriter writer;


    @BeforeAll
    static void init() {
        File output = new File(TEST_OUTPUT);

        boolean created = output.mkdirs();
        if (created) {
            logger.info("Created output directory: {}", output.getAbsolutePath());
        }
    }

    @BeforeEach
    void init(TestInfo testInfo) throws IOException {
        File output = new File(TEST_OUTPUT + "/" + testInfo.getTestMethod()
                .map(Method::getName)
                .orElse("unknown"));
        boolean created = output.mkdirs();
        if (created) {
            logger.info("Created output directory: {}", output.getAbsolutePath());
        }

        this.writer = new FileWriter(output.getAbsolutePath() + "/" + testInfo.getDisplayName() + ".txt", StandardCharsets.UTF_8);
    }

    @AfterEach
    void close() throws IOException {
        this.writer.close();
    }

    protected static List<DiagramProject> getDiagrams() {
        return List.of(DiagramProject.values());
    }

    protected static List<DiagramProject> getDistinctDiagrams() {
        Set<CodeProject> projects = new LinkedHashSet<>();
        return getDiagrams().stream()
                .filter(d -> projects.add(d.getSourceProject()))
                .toList();
    }

    protected static ArchitectureModel getArchitectureModel(DiagramProject project) {
        File model = project.getSourceProject()
                .getProject()
                .getModelFile(ArchitectureModelType.UML);
        UmlExtractor extractor = new UmlExtractor(model.getAbsolutePath());

        ArchitectureModel architectureModel = extractor.extractModel();
        assertNotNull(architectureModel);
        return architectureModel;
    }

    protected static CodeModel getCodeModel(DiagramProject project) {
        File model = new File(project.getSourceProject()
                .getCodeModelDirectory());
        CodeExtractor extractor = new AllLanguagesExtractor(new CodeItemRepository(), model.getAbsolutePath());

        CodeModel codeModel = extractor.readInCodeModel();
        assertNotNull(codeModel);
        return codeModel;
    }

    protected static <M> @Nullable AnnotatedDiagram<M> applyRefactoring(AnnotatedDiagram<M> diagram,
            Refactoring<Box, M> refactoring) {
        AnnotatedGraph<Box, M> graph = AnnotatedGraph.createFrom(diagram);

        boolean successful = refactoring.applyTo(graph);

        return successful ? AnnotatedDiagram.createFrom(diagram.diagram().getLocation().toString(), graph) : null;
    }

    protected static AnnotatedDiagram<ArchitectureItem> getAnnotatedArchitectureDiagram(DiagramProject project) {
        AnnotatedDiagram<ArchitectureItem> diagram = AnnotatedDiagram.createFrom(project.toString(), getArchitectureModel(project));
        assertNotNull(diagram);
        return diagram;
    }

    protected static AnnotatedDiagram<CodeItem> getAnnotatedCodeDiagram(DiagramProject project) {
        AnnotatedDiagram<CodeItem> diagram = AnnotatedDiagram.createFrom(project.toString(), getCodeModel(project));
        assertNotNull(diagram);
        return diagram;
    }

    protected static <B, E> MutableBiMap<Inconsistency<B, E>, Inconsistency<B, E>> getMapBasedInconsistencySet(
            List<Inconsistency<B, E>> inconsistencies) {
        MutableBiMap<Inconsistency<B, E>, Inconsistency<B, E>> map = new HashBiMap<>();
        for (Inconsistency<B, E> inconsistency : inconsistencies) {
            map.put(inconsistency, inconsistency);
        }
        return map;
    }
}
