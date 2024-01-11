/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum Project implements GoldStandardProject {
    MEDIASTORE(//
            "MS", //
            "/benchmark/mediastore/model_2016/pcm/ms.repository", //
            "/benchmark/mediastore/text_2016/mediastore.txt", //
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016.csv", //
            "/configurations/ms/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/mediastore/goldstandards/goldstandard_sad_2016-sam_2016_UME.csv", //
            new ExpectedResults(.999, .620, .765, .978, .778, .999), //
            new ExpectedResults(.212, .792, .328, .702, .227, .690) //
    ), //
    TEASTORE( //
            "TS", //
            "/benchmark/teastore/model_2020/pcm/teastore.repository", //
            "/benchmark/teastore/text_2020/teastore.txt", //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020.csv", //
            "/configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/teastore/goldstandards/goldstandard_sad_2020-sam_2020_UME.csv", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.962, .703, .784, .957, .808, .994) //
    ), //
    TEASTORE_HISTORICAL( //
            "TS-H", //
            "/benchmark/teastore/model_2020/pcm/teastore.repository", //
            "/benchmark/teastore/text_2018/teastore_2018_AB.txt", //
            "/benchmark/teastore/goldstandards/goldstandard_sad_2018-sam_2020_AB.csv", //
            "/configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/teastore/goldstandards/goldstandard_sad_2018-sam_2020_AB_UME.csv", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.163, .982, .278, .376, .146, .289) //
    ), //
    TEAMMATES( //
            "TM", //
            "/benchmark/teammates/model_2021/pcm/teammates.repository", //
            "/benchmark/teammates/text_2021/teammates.txt", //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/teammates/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            new ExpectedResults(.555, .882, .681, .965, .688, .975), //
            new ExpectedResults(.175, .745, .279, .851, .287, .851) //
    ), //
    TEAMMATES_HISTORICAL( //
            "TM-H", //
            "/benchmark/teammates/model_2021/pcm/teammates.repository", //
            "/benchmark/teammates/text_2015/teammates_2015.txt", //
            "/benchmark/teammates/goldstandards/goldstandard_sad_2015-sam_2021.csv", //
            "/configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/teammates/goldstandards/goldstandard_sad_2015-sam_2021_UME.csv", //
            new ExpectedResults(.524, .695, .597, .970, .589, .979), //
            new ExpectedResults(.168, .629, .263, .863, .260, .870) //
    ), //
    BIGBLUEBUTTON( //
            "BBB", "/benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "/benchmark/bigbluebutton/text_2021/bigbluebutton.txt", //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            new ExpectedResults(.875, .826, .850, .985, .835, .985), //
            new ExpectedResults(.887, .461, .429, .956, .534, .984) //
    ), //
    BIGBLUEBUTTON_HISTORICAL( //
            "BBB-H", "/benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "/benchmark/bigbluebutton/text_2015/bigbluebutton_2015.txt", //
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2015-sam_2021.csv", //
            "/configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/bigbluebutton/goldstandards/goldstandard_sad_2015-sam_2021_UME.csv", //
            new ExpectedResults(.807, .617, .699, .978, .695, .993), //
            new ExpectedResults(.085, .175, .111, .813, .018, .869) //
    ), //
    JABREF( //
            "JR", "/benchmark/jabref/model_2021/pcm/jabref.repository", //
            "/benchmark/jabref/text_2021/jabref.txt", //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021.csv", //
            "/configurations/jabref/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/jabref/goldstandards/goldstandard_sad_2021-sam_2021_UME.csv", //
            new ExpectedResults(.899, .999, .946, .973, .932, .966), //
            new ExpectedResults(1.0, .443, .443, .845, .616, 1.0) //
    ), //
    JABREF_HISTORICAL( //
            "JR-H", "/benchmark/jabref/model_2021/pcm/jabref.repository", //
            "/benchmark/jabref/text_2016/jabref_2016.txt", //
            "/benchmark/jabref/goldstandards/goldstandard_sad_2016-sam_2021.csv", //
            "/configurations/jabref/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "/benchmark/jabref/goldstandards/goldstandard_sad_2016-sam_2021_UME.csv", //
            new ExpectedResults(.817, .999, .899, .966, .886, .960), //
            new ExpectedResults(.110, .110, .110, .366, -.249, .475) //
    );

    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private final String alias;
    private final String model;
    private final String textFile;
    private final String configurationsFile;
    private final String goldStandardTraceabilityLinkRecovery;
    private final String goldStandardMissingTextForModelElement;
    private final ExpectedResults expectedTraceLinkResults;
    private final ExpectedResults expectedInconsistencyResults;
    private final SortedSet<String> resourceNames;

    Project(String alias, String model, String textFile, String goldStandardTraceabilityLinkRecovery, String configurationsFile,
            String goldStandardMissingTextForModelElement, ExpectedResults expectedTraceLinkResults, ExpectedResults expectedInconsistencyResults) {
        this.alias = alias;
        this.model = model;
        this.textFile = textFile;
        this.configurationsFile = configurationsFile;
        this.goldStandardTraceabilityLinkRecovery = goldStandardTraceabilityLinkRecovery;
        this.goldStandardMissingTextForModelElement = goldStandardMissingTextForModelElement;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
        resourceNames = new TreeSet<>(List.of(model, textFile, goldStandardTraceabilityLinkRecovery, configurationsFile,
                goldStandardMissingTextForModelElement));
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public File getModelFile() {
        return ProjectHelper.loadFileFromResources(model);
    }

    @Override
    public String getModelResourceName() {
        return model;
    }

    @Override
    public File getModelFile(ArchitectureModelType modelType) {
        return switch (modelType) {
        case PCM -> getModelFile();
        case UML -> ProjectHelper.loadFileFromResources(model.replace("/pcm/", "/uml/").replace(".repository", ".uml"));
        };
    }

    @Override
    public String getModelResourceName(ArchitectureModelType modelType) {
        return switch (modelType) {
        case PCM -> model;
        case UML -> model.replace("/pcm/", "/uml/").replace(".repository", ".uml");
        };
    }

    @Override
    public File getTextFile() {
        return ProjectHelper.loadFileFromResources(textFile);
    }

    @Override
    public String getTextResourceName() {
        return textFile;
    }

    /**
     * Return the map of additional configuration options
     *
     * @return the map of additional configuration options
     */
    public SortedMap<String, String> getAdditionalConfigurations() {
        return ConfigurationHelper.loadAdditionalConfigs(getAdditionalConfigurationsFile());
    }

    @Override
    public File getAdditionalConfigurationsFile() {
        return ProjectHelper.loadFileFromResources(this.configurationsFile);
    }

    @Override
    public String getAdditionalConfigurationsResourceName() {
        return configurationsFile;
    }

    @Override
    public File getTlrGoldStandardFile() {
        return ProjectHelper.loadFileFromResources(goldStandardTraceabilityLinkRecovery);
    }

    @Override
    public String getTlrGoldStandardResourceName() {
        return goldStandardTraceabilityLinkRecovery;
    }

    @Override
    public ImmutableList<String> getTlrGoldStandard() {
        var path = Paths.get(this.getTlrGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove(0);
        goldLinks.removeIf(String::isBlank);
        return Lists.immutable.ofAll(goldLinks);
    }

    @Override
    public GoldStandard getTlrGoldStandard(ArchitectureModel architectureModel) {
        return new GoldStandard(getTlrGoldStandardFile(), architectureModel);
    }

    @Override
    public MutableList<String> getMissingTextForModelElementGoldStandard() {
        var path = Paths.get(this.getMissingTextForModelElementGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove("missingModelElementID");
        goldLinks.removeIf(String::isBlank);
        return Lists.mutable.ofAll(goldLinks);
    }

    @Override
    public File getMissingTextForModelElementGoldStandardFile() {
        return ProjectHelper.loadFileFromResources(goldStandardMissingTextForModelElement);
    }

    @Override
    public String getMissingTextForModelElementGoldStandardResourceName() {
        return goldStandardMissingTextForModelElement;
    }

    @Override
    public ExpectedResults getExpectedTraceLinkResults() {
        return expectedTraceLinkResults;
    }

    @Override
    public ExpectedResults getExpectedInconsistencyResults() {
        return expectedInconsistencyResults;
    }

    @Override
    public String getProjectName() {
        return this.name();
    }

    @Override
    public SortedSet<String> getResourceNames() {
        return new TreeSet<>(resourceNames);
    }
}
