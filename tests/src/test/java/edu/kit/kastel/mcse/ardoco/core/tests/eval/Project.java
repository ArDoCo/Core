/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArDoCo;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/model_2016/pcm/ms.repository", //
            "src/test/resources/benchmark/mediastore/text_2016/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/text_2016/goldstandard.csv", //
            "src/test/resources/configurations/ms/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/mediastore/text_2016/goldstandard_UME.csv", //
            new ExpectedResults(.999, .620, .765, .978, .778, .999), //
            new ExpectedResults(.212, .792, .328, .702, .227, .690) //
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/model_2020/pcm/teastore.repository", //
            "src/test/resources/benchmark/teastore/text_2020/teastore.txt", //
            "src/test/resources/benchmark/teastore/text_2020/goldstandard.csv", //
            "src/test/resources/configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/teastore/text_2020/goldstandard_UME.csv", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.962, .703, .784, .957, .808, .994) //
    ), //
    TEASTORE_HISTORIC( //
            "src/test/resources/benchmark/teastore/model_2020/pcm/teastore.repository", //
            "src/test/resources/benchmark/teastore/text_2018/teastore_2018_AB.txt", //
            "src/test/resources/benchmark/teastore/text_2018/goldstandard_AB.csv", //
            "src/test/resources/configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/teastore/text_2018/goldstandard_AB_UME.csv", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.163, .982, .278, .376, .146, .289) //
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/model_2021/pcm/teammates.repository", //
            "src/test/resources/benchmark/teammates/text_2021/teammates.txt", //
            "src/test/resources/benchmark/teammates/text_2021/goldstandard.csv", //
            "src/test/resources/configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/teammates/text_2021/goldstandard_UME.csv", //
            new ExpectedResults(.555, .899, .686, .973, .695, .976), //
            new ExpectedResults(.178, .759, .284, .851, .291, .851) //
    ), //
    TEAMMATES_HISTORIC( // 
            "src/test/resources/benchmark/teammates/model_2021/pcm/teammates.repository", //
            "src/test/resources/benchmark/teammates/text_2015/teammates_2015.txt", //
            "src/test/resources/benchmark/teammates/text_2015/goldstandard.csv", //
            "src/test/resources/configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/teammates/text_2015/goldstandard_UME.csv", //
            new ExpectedResults(.524, .695, .597, .970, .589, .979), //
            new ExpectedResults(.168, .629, .263, .863, .260, .870) //
    ), //
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/text_2021/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/text_2021/goldstandard.csv", //
            "src/test/resources/configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/bigbluebutton/text_2021/goldstandard_UME.csv", //
            new ExpectedResults(.877, .826, .850, .984, .843, .993), //
            new ExpectedResults(.887, .461, .429, .956, .534, .984) //
    ), //
    BIGBLUEBUTTON_HISTORIC( //
            "src/test/resources/benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/text_2015/bigbluebutton_2015.txt", //
            "src/test/resources/benchmark/bigbluebutton/text_2015/goldstandard.csv", //
            "src/test/resources/configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/bigbluebutton/text_2015/goldstandard_UME.csv", //
            new ExpectedResults(.807, .617, .699, .978, .695, .993), //
            new ExpectedResults(.085, .175, .111, .813, .018, .869) //
    ), //
    JABREF( //
            "src/test/resources/benchmark/jabref/model_2021/pcm/jabref.repository", //
            "src/test/resources/benchmark/jabref/text_2021/jabref.txt", //
            "src/test/resources/benchmark/jabref/text_2021/goldstandard.csv", //
            "src/test/resources/configurations/jabref/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/jabref/text_2021/goldstandard_UME.csv", //
            new ExpectedResults(.899, .999, .946, .973, .932, .966), //
            new ExpectedResults(1.0, .443, .443, .845, .616, 1.0) //
    ), //
    JABREF_HISTORIC( //
            "src/test/resources/benchmark/jabref/model_2021/pcm/jabref.repository", //
            "src/test/resources/benchmark/jabref/text_2016/jabref_2016.txt", //
            "src/test/resources/benchmark/jabref/text_2016/goldstandard.csv", //
            "src/test/resources/configurations/jabref/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "src/test/resources/benchmark/jabref/text_2016/goldstandard_UME.csv", //
            new ExpectedResults(.817, .999, .899, .966, .886, .960), //
            new ExpectedResults(.110, .110, .110, .366, -.249, .475) //
    );

    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private final String model;
    private final String textFile;
    private final String configurationsFile;
    private final String goldStandardTraceabilityLinkRecovery;
    private final String goldStandardMissingTextForModelElement;
    private final ExpectedResults expectedTraceLinkResults;
    private final ExpectedResults expectedInconsistencyResults;

    Project(String model, String textFile, String goldStandardTraceabilityLinkRecovery, String configurationsFile,
            String goldStandardMissingTextForModelElement, ExpectedResults expectedTraceLinkResults, ExpectedResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.configurationsFile = configurationsFile;
        this.goldStandardTraceabilityLinkRecovery = goldStandardTraceabilityLinkRecovery;
        this.goldStandardMissingTextForModelElement = goldStandardMissingTextForModelElement;
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
     * Returns the {@link GoldStandard} for this project.
     * 
     * @return the File that represents the gold standard for this project
     */
    public File getTlrGoldStandardFile() {
        return new File(goldStandardTraceabilityLinkRecovery);
    }

    /**
     * Returns a string-list of entries as goldstandard for TLR for this project.
     * 
     * @return a list with the entries of the goldstandard for TLR
     */
    public List<String> getTlrGoldStandard() {
        var path = Paths.get(this.getTlrGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove(0);
        return goldLinks;
    }

    /**
     * Returns the {@link GoldStandard} for this project for the given model connector.
     * 
     * @param pcmModel the model connector (pcm)
     * @return the {@link GoldStandard} for this project
     */
    public GoldStandard getTlrGoldStandard(ModelConnector pcmModel) {
        return new GoldStandard(getTlrGoldStandardFile(), pcmModel);
    }

    public List<String> getMissingTextForModelElementGoldStandard() {
        var path = Paths.get(this.getMissingTextForModelElementGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove("missingModelElementID");
        return goldLinks;
    }

    private File getMissingTextForModelElementGoldStandardFile() {
        return new File(goldStandardMissingTextForModelElement);
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
