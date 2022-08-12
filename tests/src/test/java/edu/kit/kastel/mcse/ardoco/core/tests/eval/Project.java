/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/original_model/ms.repository", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            null, //
            new EvaluationResults(.999, .620, .765), //
            new EvaluationResults(.0, .0, .246)//
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/original_model/teammates.repository", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            "src/test/resources/diagrams/teammates", //
            new EvaluationResults(.913, .880, .896), //
            new EvaluationResults(.0, .0, .222)//
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/original_model/teastore.repository", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            null, //
            new EvaluationResults(.999, .713, .832), //
            new EvaluationResults(.0, .0, .250)), //
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/original_model/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            "src/test/resources/diagrams/bbb", //
            new EvaluationResults(.877, .826, .850), //
            new EvaluationResults(.0, .0, .272));

    private final String model;
    private final String textFile;
    private final String goldStandard;
    private final String diagramDir;
    private final EvaluationResults expectedTraceLinkResults;
    private final EvaluationResults expectedInconsistencyResults;

    Project(String model, String textFile, String goldStandard, String diagramDir, EvaluationResults expectedTraceLinkResults,
            EvaluationResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
        this.diagramDir = diagramDir;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    /**
     * @return the File that represents the model for this project
     */
    public File getModelFile() {
        return new File(model);
    }

    /**
     * @return the File that represents the text for this project
     */
    public File getTextFile() {
        return new File(textFile);
    }

    /**
     * @return the File that represents the gold standard for this project
     */
    public File getGoldStandardFile() {
        return new File(goldStandard);
    }

    /**
     * @param pcmModel the model connector (pcm)
     * @return the {@link GoldStandard} for this project
     */
    public GoldStandard getGoldStandard(ModelConnector pcmModel) {
        return new GoldStandard(getGoldStandardFile(), pcmModel);
    }

    public File getDiagramDir() {
        if (diagramDir == null)
            return null;
        File file = new File(diagramDir);
        if (file.exists() && file.isDirectory())
            return file;
        return null;
    }

    /**
     * @return the expectedTraceLinkResults
     */
    public EvaluationResults getExpectedTraceLinkResults() {
        return expectedTraceLinkResults;
    }

    /**
     * @return the expectedInconsistencyResults
     */
    public EvaluationResults getExpectedInconsistencyResults() {
        return expectedInconsistencyResults;
    }
}
