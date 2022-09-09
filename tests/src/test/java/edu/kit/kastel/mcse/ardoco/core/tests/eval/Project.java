/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/pcm/ms.repository", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            new ExpectedResults(.999, .620, .765, .978, .778), //
            new ExpectedResults(.000, .000, .256, .534, .178) //
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/pcm/teammates.repository", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            new ExpectedResults(.913, .880, .896, .988, .890), //
            new ExpectedResults(.000, .000, .222, .606, .227) //
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/pcm/teastore.repository", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            new ExpectedResults(.999, .713, .832, .982, .837), //
            new ExpectedResults(.000, .000, .250, .502, .103) //
    ), //
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/pcm/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            new ExpectedResults(.877, .826, .850, .984, .844), //
            new ExpectedResults(.000, .000, .272, .738, .190) //
    );

    private final String model;
    private final String textFile;
    private final String goldStandard;
    private final ExpectedResults expectedTraceLinkResults;
    private final ExpectedResults expectedInconsistencyResults;

    Project(String model, String textFile, String goldStandard, ExpectedResults expectedTraceLinkResults, ExpectedResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    /**
     * Returns the File that represents the model for this project.
     * 
     * @return the File that represents the model for this project
     */
    public File getModelFile() {
        return new File(model);
    }

    /**
     * Returns the File that represents the model for this project with the given model type.
     * 
     * @param modelType the model type
     * @return the File that represents the model for this project
     */
    public File getModelFile(ArchitectureModelType modelType) {
        return switch (modelType) {
        case PCM -> getModelFile();
        case UML -> new File(model.replace("/pcm/", "/uml/").replace(".repository", ".uml"));
        };
    }

    /**
     * Returns the File that represents the text for this project.
     * 
     * @return the File that represents the text for this project
     */
    public File getTextFile() {
        return new File(textFile);
    }

    /**
     * Returns the {@link GoldStandard} for this project.
     * 
     * @return the File that represents the gold standard for this project
     */
    public File getGoldStandardFile() {
        return new File(goldStandard);
    }

    /**
     * Returns the {@link GoldStandard} for this project for the given model connector.
     * 
     * @param pcmModel the model connector (pcm)
     * @return the {@link GoldStandard} for this project
     */
    public GoldStandard getGoldStandard(ModelConnector pcmModel) {
        return new GoldStandard(getGoldStandardFile(), pcmModel);
    }

    /**
     * Returns the expected results for Traceability Link Recovery.
     * 
     * @return the expectedTraceLinkResults
     */
    public ExpectedResults getExpectedTraceLinkResults() {
        return expectedTraceLinkResults;
    }

    /**
     * Returns the expected results for Inconsistency Detection.
     * 
     * @return the expectedInconsistencyResults
     */
    public ExpectedResults getExpectedInconsistencyResults() {
        return expectedInconsistencyResults;
    }
}
