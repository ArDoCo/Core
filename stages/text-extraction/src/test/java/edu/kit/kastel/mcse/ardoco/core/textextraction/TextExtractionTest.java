package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.execution.runner.AnonymousRunner;
import edu.kit.kastel.mcse.ardoco.core.models.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;
import edu.kit.kastel.mcse.ardoco.tests.eval.StageTest;

public class TextExtractionTest extends StageTest<TextExtraction, TextExtractionTest.TextExtractionResult> {
    public TextExtractionTest() {
        super(new TextExtraction(null));
    }

    @Override
    protected TextExtractionResult runComparable(DiagramProject project, Map<String, String> additionalConfigurations, boolean cachePreRun) {
        return null;
    }

    @Override
    protected DataRepository runPreTestRunner(DiagramProject project) {
        return new AnonymousRunner(project.getProjectName()) {
            @Override
            public List<AbstractPipelineStep> initializePipelineSteps() throws IOException {
                var pipelineSteps = new ArrayList<AbstractPipelineStep>();

                ArDoCo arDoCo = getArDoCo();
                var dataRepository = arDoCo.getDataRepository();

                var text = CommonUtilities.readInputText(project.getTextFile());
                if (text.isBlank()) {
                    throw new IllegalArgumentException("Cannot deal with empty input text. Maybe there was an error reading the file.");
                }
                DataRepositoryHelper.putInputText(dataRepository, text);
                pipelineSteps.add(TextPreprocessingAgent.get(project.getAdditionalConfigurations(), dataRepository));

                pipelineSteps.add(ModelProviderAgent.get(project.getModelFile(), project.getArchitectureModelType(), dataRepository));

                pipelineSteps.add(TextExtraction.get(project.getAdditionalConfigurations(), dataRepository));

                return pipelineSteps;
            }
        }.runWithoutSaving();
    }

    @Override
    protected DataRepository runTestRunner(DiagramProject project, Map<String, String> additionalConfigurations, DataRepository dataRepository) {
        return null;
    }

    public record TextExtractionResult() {
    }
}
