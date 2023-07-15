/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramG;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramsG;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum DiagramProject implements Serializable {
    MEDIASTORE(//
            "benchmark/mediastore/model_2016/pcm/ms.repository", //
            "benchmark/mediastore/text_2016/mediastore.txt", //
            "benchmark/mediastore/text_2016/goldstandard.csv", //
            "configurations/ms/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/mediastore/text_2016/goldstandard_UME.csv", //
            "benchmark/mediastore/diagrams_2016/goldstandard.json", //
            new ExpectedResults(.999, .620, .765, .978, .778, .999), //
            new ExpectedResults(.212, .792, .328, .702, .227, .690), //
            new ExpectedResults(.866, .896, .88, .982, .871, .988) //
    ), //
    TEASTORE( //
            "benchmark/teastore/model_2020/pcm/teastore.repository", //
            "benchmark/teastore/text_2020/teastore.txt", //
            "benchmark/teastore/text_2020/goldstandard.csv", //
            "configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/teastore/text_2020/goldstandard_UME.csv", //
            "benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.962, .703, .784, .957, .808, .994), //
            new ExpectedResults(.999, .740, .850, .972, .847, .999) //
    ), //
    TEAMMATES( //
            "benchmark/teammates/model_2021/pcm/teammates.repository", //
            "benchmark/teammates/text_2021/teammates.txt", //
            "benchmark/teammates/text_2021/goldstandard.csv", //
            "configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/teammates/text_2021/goldstandard_UME.csv", //
            "benchmark/teammates/diagrams_2023/goldstandard.json", //
            new ExpectedResults(.555, .882, .681, .965, .688, .975), //
            new ExpectedResults(.175, .745, .279, .851, .287, .851), //
            new ExpectedResults(.436, .696, .536, .973, .539, .979) //
    ), //
    BIGBLUEBUTTON( //
            "benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "benchmark/bigbluebutton/text_2021/bigbluebutton.txt", //
            "benchmark/bigbluebutton/text_2021/goldstandard.csv", //
            "configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/bigbluebutton/text_2021/goldstandard_UME.csv", //
            "benchmark/bigbluebutton/diagrams_2021/goldstandard.json", //
            new ExpectedResults(.875, .826, .850, .985, .835, .985), //
            new ExpectedResults(.887, .461, .429, .956, .534, .984), //
            new ExpectedResults(.845, .745, .792, .978, .783, .991) //
    ), //
    TEASTORE_HISTORICAL( //
            "benchmark/teastore/model_2020/pcm/teastore.repository", //
            "benchmark/teastore/text_2018/teastore_2018_AB.txt", //
            "benchmark/teastore/text_2018/goldstandard_AB.csv", //
            "configurations/ts/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/teastore/text_2018/goldstandard_AB_UME.csv", //
            "benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(.999, .740, .850, .984, .853, .999), //
            new ExpectedResults(.163, .982, .278, .376, .146, .289), //
            new ExpectedResults(0, 0, 0, 0, 0, 0) //
    ), //
    TEAMMATES_HISTORICAL( //
            "benchmark/teammates/model_2021/pcm/teammates.repository", //
            "benchmark/teammates/text_2015/teammates_2015.txt", //
            "benchmark/teammates/text_2015/goldstandard.csv", //
            "configurations/tm/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/teammates/text_2015/goldstandard_UME.csv", //
            "benchmark/teammates/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.524, .695, .597, .970, .589, .979), //
            new ExpectedResults(.168, .629, .263, .863, .260, .870), //
            new ExpectedResults(0, 0, 0, 0, 0, 0) //
    ), //
    BIGBLUEBUTTON_HISTORICAL( //
            "benchmark/bigbluebutton/model_2021/pcm/bbb.repository", //
            "benchmark/bigbluebutton/text_2015/bigbluebutton_2015.txt", //
            "benchmark/bigbluebutton/text_2015/goldstandard.csv", //
            "configurations/bbb/filterlists_all.txt", // options: filterlists_none.txt, filterlists_onlyCommon.txt, filterlists_all.txt
            "benchmark/bigbluebutton/text_2015/goldstandard_UME.csv", //
            "benchmark/bigbluebutton/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.807, .617, .699, .978, .695, .993), //
            new ExpectedResults(.085, .175, .111, .813, .018, .869), //
            new ExpectedResults(0, 0, 0, 0, 0, 0) //
    );

    private static final Logger logger = LoggerFactory.getLogger(DiagramProject.class);

    private static final Map<String, File> tempFiles = Maps.mutable.empty();

    private final Map<ArchitectureModelType, File> allModelFiles = Maps.mutable.empty();

    private final String model;
    private final String text;
    private final String configurations;
    private final String goldStandardTraceabilityLinkRecovery;
    private final String goldStandardMissingTextForModelElement;
    private final String goldStandardDiagrams;
    private final ExpectedResults expectedTraceLinkResults;
    private final ExpectedResults expectedInconsistencyResults;

    private final ExpectedResults expectedDiagramTraceLinkResults;

    private final ArchitectureModelType architectureModelType;

    DiagramProject(String model, String text, String goldStandardTraceabilityLinkRecovery, String configurations, String goldStandardMissingTextForModelElement,
            String goldStandardDiagrams, ExpectedResults expectedTraceLinkResults, ExpectedResults expectedInconsistencyResults,
            ExpectedResults expectedDiagramTraceLinkResults) {
        //We need to keep the paths as well, because the actual files are just temporary at this point due to jar packaging
        this.model = model;
        this.architectureModelType = setupArchitectureModelType();
        this.text = text;
        this.configurations = configurations;
        this.goldStandardTraceabilityLinkRecovery = goldStandardTraceabilityLinkRecovery;
        this.goldStandardMissingTextForModelElement = goldStandardMissingTextForModelElement;
        this.goldStandardDiagrams = goldStandardDiagrams;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
        this.expectedDiagramTraceLinkResults = expectedDiagramTraceLinkResults;
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
        if (model.contains("/pcm/")) {
            return ArchitectureModelType.PCM;
        } else if (model.contains("/uml/")) {
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

    private static File getTemporaryFileFromString(String path) {
        //Create .tmp file with ArDoCo prefix
        try {
            var is = DiagramProject.class.getClassLoader().getResourceAsStream(path);
            var temp = File.createTempFile("ArDoCo", null);
            temp.deleteOnExit();
            Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFiles.put(path, temp);
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getFileFromTestResources(@NotNull String path) {
        return tempFiles.computeIfAbsent(path, f -> getTemporaryFileFromString(path));
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

    private static <T extends Enum<T>> List<T> filterForProjects(Collection<T> unfilteredProjects, Predicate<T> filter) {
        return unfilteredProjects.stream().filter(filter).toList();
    }

    /**
     * Returns the File that represents the model for this project.
     *
     * @return the File that represents the model for this project
     */
    public File getModelFile() {
        return getFileFromTestResources(model);
    }

    /**
     * Returns the File that represents the model for this project with the given model type.
     *
     * @param modelType the model type
     * @return the File that represents the model for this project or null
     */
    public File getModelFile(ArchitectureModelType modelType) {
        var newModelFile = allModelFiles.getOrDefault(modelType, null);
        if (newModelFile == null) {
            newModelFile = getFileFromTestResources(model.replace(getModelDir(architectureModelType), getModelDir(modelType))
                    .replace(getModelExt(architectureModelType), getModelExt(modelType)));
            allModelFiles.put(modelType, newModelFile);
        } else {
            return newModelFile;
        }

        return newModelFile;
    }

    /**
     * TODO This should probably be part of {@link ArchitectureModelType}
     */
    private String getModelDir(ArchitectureModelType architectureModelType) {
        return switch (architectureModelType) {
            case PCM -> "/pcm/";
            case UML -> "/uml/";
        };
    }

    /**
     * TODO This should probably be part of {@link ArchitectureModelType}
     */
    private String getModelExt(ArchitectureModelType architectureModelType) {
        return switch (architectureModelType) {
            case PCM -> ".repository";
            case UML -> ".uml";
        };
    }

    /**
     * Returns the String path that represents the text for this project.
     *
     * @return the path as a string
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the File that represents the text for this project.
     *
     * @return the File that represents the text for this project
     */
    public File getTextFile() {
        return getFileFromTestResources(text);
    }

    /**
     * Return the map of additional configuration options
     *
     * @return the map of additional configuration options
     */
    public Map<String, String> getAdditionalConfigurations() {
        return ConfigurationHelper.loadAdditionalConfigs(getAdditionalConfigurationsFile());
    }

    /**
     * Returns a {@link File} that points to the text file containing additional configurations
     *
     * @return the file for additional configurations
     */
    public File getAdditionalConfigurationsFile() {
        return getFileFromTestResources(configurations);
    }

    /**
     * Returns the {@link GoldStandard} for this project.
     *
     * @return the File that represents the gold standard for this project
     */
    public File getTlrGoldStandardFile() {
        return getFileFromTestResources(goldStandardTraceabilityLinkRecovery);
    }

    /**
     * Returns a string-list of entries as goldstandard for TLR for this project.
     *
     * @return a list with the entries of the goldstandard for TLR
     */
    public ImmutableList<String> getTlrGoldStandard() {
        var path = Paths.get(this.getTlrGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove(0);
        return Lists.immutable.ofAll(goldLinks);
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

    public MutableList<String> getMissingTextForModelElementGoldStandard() {
        var path = Paths.get(this.getMissingTextForModelElementGoldStandardFile().toURI());
        List<String> goldLinks = Lists.mutable.empty();
        try {
            goldLinks = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        goldLinks.remove("missingModelElementID");
        return Lists.mutable.ofAll(goldLinks);
    }

    private File getMissingTextForModelElementGoldStandardFile() {
        return getFileFromTestResources(goldStandardMissingTextForModelElement);
    }

    /**
     * Returns the {@link GoldStandard} for this project.
     *
     * @return the File that represents the gold standard for this project
     */
    public File getDiagramsGoldStandardFile() {
        return getFileFromTestResources(goldStandardDiagrams);
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
     * Returns the expected results from the diagram trace link recovery
     *
     * @return the expectedDiagramTraceLinkResults
     */
    public ExpectedResults getExpectedDiagramTraceLinkResults() {
        return expectedDiagramTraceLinkResults;
    }

    /**
     * Returns the expected results for Inconsistency Detection.
     *
     * @return the expectedInconsistencyResults
     */
    public ExpectedResults getExpectedInconsistencyResults() {
        return expectedInconsistencyResults;
    }

    public ImmutableSet<DiaTexTraceLink> getDiagramTextTraceLinksFromGoldstandard() {
        return getDiagramTextTraceLinksFromGoldstandard(text);
    }

    public ImmutableSet<DiaTexTraceLink> getDiagramTextTraceLinksFromGoldstandard(@Nullable String textGoldstandard) {
        MutableSet<DiaTexTraceLink> diagramLinks = Sets.mutable.empty();
        return Sets.immutable.fromStream(getDiagramsFromGoldstandard().stream().flatMap(d -> d.getTraceLinks(textGoldstandard).stream()));
    }

    public ImmutableSet<DiagramG> getDiagramsFromGoldstandard() {
        try {
            var objectMapper = new ObjectMapper();
            var file = getDiagramsGoldStandardFile();
            objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramProject.class, this));
            return Sets.immutable.ofAll(Set.of(objectMapper.readValue(file, DiagramsG.class).diagrams));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

