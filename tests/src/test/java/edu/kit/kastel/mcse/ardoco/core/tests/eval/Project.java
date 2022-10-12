/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/pcm/ms.repository", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            "src/test/resources/configurations/mediastore.txt", //
            new EvaluationResults(.999, .620, .765), //
            new EvaluationResults(.000, .000, .315) //
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/pcm/teammates.repository", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            "src/test/resources/configurations/teammates.txt", //
            new EvaluationResults(.913, .880, .896), //
            new EvaluationResults(.000, .000, .293) //
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/pcm/teastore.repository", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            "src/test/resources/configurations/teastore.txt", //
            new EvaluationResults(.999, .713, .832), //
            new EvaluationResults(.000, .000, .418) //
    ), //
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/pcm/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            "src/test/resources/configurations/bigbluebutton.txt", //
            new EvaluationResults(.877, .826, .850), //
            new EvaluationResults(.000, .000, .287) //
    ), //
    JABREF( //
            "src/test/resources/benchmark/jabref/pcm/jabref.repository", //
            "src/test/resources/benchmark/jabref/jabref.txt", //
            "src/test/resources/benchmark/jabref/goldstandard.csv", //
            "src/test/resources/configurations/jabref.txt", //
            new EvaluationResults(.849, .999, .918), //
            new EvaluationResults(.000, .000, .355) //
    );

    private final String model;
    private final String textFile;
    private final String configurationsFile;
    private final String goldStandard;
    private final EvaluationResults expectedTraceLinkResults;
    private final EvaluationResults expectedInconsistencyResults;

    Project(String model, String textFile, String goldStandard, String configurationsFile, EvaluationResults expectedTraceLinkResults,
            EvaluationResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.configurationsFile = configurationsFile;
        this.goldStandard = goldStandard;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    /**
     * Returns an {@link Optional} containing the project that has a name that equals the given name, ignoring case.
     * 
     * @param name the name of the project
     * @return the Optional containing the project with the given name or is empty if no such is found.
     */
    public static Optional<Project> getFromName(String name) {
        for (Project project : Project.values()) {
            if (project.name().equalsIgnoreCase(name)) {
                return Optional.of(project);
            }
        }
        return Optional.empty();
    }

    /**
     * @return the File that represents the model for this project
     */
    public File getModelFile() {
        return new File(model);
    }

    public File getModelFile(ArchitectureModelType modelType) {
        return switch (modelType) {
        case PCM -> getModelFile();
        case UML -> new File(model.replace("/pcm/", "/uml/").replace(".repository", ".uml"));
        };
    }

    /**
     * @return the File that represents the text for this project
     */
    public File getTextFile() {
        return new File(textFile);
    }

    /**
     * Return the map of additional configuration options
     * 
     * @return the map of additional configuration options
     */
    public Map<String, String> getAdditionalConfigurations() {
        return ArDoCo.loadAdditionalConfigs(getAdditionalConfigurationsFile());
    }

    /**
     * Returns a {@link File} that points to the text file containing additional configurations
     * 
     * @return the file for additional configurations
     */
    public File getAdditionalConfigurationsFile() {
        return new File(this.configurationsFile);
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
