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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
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
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

/**
 * This enum captures the different case studies that are used for evaluation in the integration tests.
 */
public enum DiagramProject implements Serializable {
    MEDIASTORE(//
            Project.MEDIASTORE, //
            "/benchmark/mediastore/diagrams_2016/goldstandard.json", //
            new ExpectedResults(.887, .94, .913, .978, .9, .983) //
    ), //
    TEASTORE( //
            Project.TEASTORE, //
            "/benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(1, .821, .901, .973, .892, 1) //
    ), //
    TEAMMATES( //
            Project.TEAMMATES, //
            "/benchmark/teammates/diagrams_2023/goldstandard.json", //
            new ExpectedResults(.468, .834, .599, .958, .606, .963) //
    ), //
    BIGBLUEBUTTON( //
            Project.BIGBLUEBUTTON, //
            "/benchmark/bigbluebutton/diagrams_2021/goldstandard.json", //
            new ExpectedResults(.874, .835, .854, .976, .841, .989) //
    ), //
    TEASTORE_HISTORICAL( //
            Project.TEASTORE_HISTORICAL, //
            "/benchmark/teastore/diagrams_2018/goldstandard.json", //
            new ExpectedResults(1, .961, .980, .993, 0.976, 1) //
    ), //
    TEAMMATES_HISTORICAL( //
            Project.TEAMMATES_HISTORICAL, //
            "/benchmark/teammates/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.426, .744, .542, .959, .544, .967) //
    ), //
    BIGBLUEBUTTON_HISTORICAL( //
            Project.BIGBLUEBUTTON_HISTORICAL, //
            "/benchmark/bigbluebutton/diagrams_2015/goldstandard.json", //
            new ExpectedResults(.752, .927, .831, .969, .819, .973) //
    );

    private static class Helper {
        //This is necessary because static fields are always initialized after enum construction
        public static Logger logger = LoggerFactory.getLogger(DiagramProject.class);
    }

    private static final Map<String, File> tempFiles = Maps.mutable.empty();

    private final Map<ArchitectureModelType, File> allModelFiles = Maps.mutable.empty();

    public final String model;
    public final String text;
    public final String configurations;
    public final String goldStandardTraceabilityLinkRecovery;
    public final String goldStandardMissingTextForModelElement;
    public final String goldStandardDiagrams;
    public final ExpectedResults expectedTraceLinkResults;
    public final ExpectedResults expectedInconsistencyResults;

    public final ExpectedResults expectedDiagramTraceLinkResults;

    public final ArchitectureModelType architectureModelType;

    private List<Sentence> sentences;
    private boolean sourceModified = false;

    DiagramProject(Project project, String goldStandardDiagrams, ExpectedResults expectedDiagramTraceLinkResults) {
        //We need to keep the paths as well, because the actual files are just temporary at this point due to jar packaging
        //TODO I'd rather access the paths directly then create an unnecessary temporary file and handle, not sure how to go about this yet
        this.model = project.getModelResourceName();
        this.text = project.getTextResourceName();
        this.configurations = project.getAdditionalConfigurationsResourceName();
        this.goldStandardTraceabilityLinkRecovery = project.getTlrGoldStandardResourceName();
        this.goldStandardMissingTextForModelElement = project.getMissingTextForModelElementGoldStandardResourceName();
        this.goldStandardDiagrams = goldStandardDiagrams;

        //Make sure to calculate the checksums for all resources
        checksum(this.model);
        checksum(this.text);
        checksum(this.configurations);
        checksum(this.goldStandardTraceabilityLinkRecovery);
        checksum(this.goldStandardMissingTextForModelElement);
        checksum(this.goldStandardDiagrams);
        //Bump version to invalidate caches
        if (this.sourceModified)
            bumpVersion();
        //

        this.expectedTraceLinkResults = getExpectedTraceLinkResults();
        this.expectedInconsistencyResults = getExpectedInconsistencyResults();
        this.expectedDiagramTraceLinkResults = expectedDiagramTraceLinkResults;

        this.architectureModelType = setupArchitectureModelType();
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

    private static @NotNull File getTemporaryFileFromString(@NotNull String path) {
        //Create .tmp file with ArDoCo prefix
        try {
            var is = DiagramProject.class.getResourceAsStream(path);
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
            Helper.logger.error(e.getMessage(), e);
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
            Helper.logger.error(e.getMessage(), e);
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

    public Set<DiaGSTraceLink> getDiagramTraceLinks(@NotNull List<Sentence> sentences) {
        return getDiagramTraceLinks(sentences, text);
    }

    public Map<TraceType, List<DiaGSTraceLink>> getDiagramTraceLinksAsMap(@NotNull List<Sentence> sentences) {
        var traceLinks = getDiagramTraceLinks(sentences);
        return traceLinks.stream().collect(Collectors.groupingBy(DiaGSTraceLink::getTraceType));
    }

    private Set<DiaGSTraceLink> getDiagramTraceLinks(@NotNull List<Sentence> sentences, @Nullable String textGoldstandard) {
        this.sentences = sentences;
        return getDiagrams().stream().flatMap(d -> d.getTraceLinks(textGoldstandard).stream()).collect(Collectors.toSet());
    }

    public Set<DiagramG> getDiagrams() {
        try {
            var objectMapper = new ObjectMapper();
            var file = getDiagramsGoldStandardFile();
            objectMapper.setInjectableValues(new InjectableValues.Std().addValue(DiagramProject.class, this));
            return Set.of(objectMapper.readValue(file, DiagramsG.class).diagrams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Sentence> getSentences() {
        return List.copyOf(sentences);
    }

    private void checksum(String path) {
        try (var resource = DiagramProject.class.getResourceAsStream(path)) {
            if (resource == null)
                throw new IllegalArgumentException("No such resource at path " + path);
            String md5 = DigestUtils.md5Hex(resource);
            if (!Objects.equals(Preferences.userNodeForPackage(DiagramProject.class).get(path, null), md5)) {
                Preferences.userNodeForPackage(DiagramProject.class).put(path, md5);
                this.sourceModified = true;
                Helper.logger.info("Checksum for source file {} doesn't match", path);
                return;
            }
            Helper.logger.info("Checksum for source file {} matches", path);
        } catch (IOException e) {
            Helper.logger.error("Couldn't calculate checksum for resource at " + path, e);
        }
    }

    public long getSourceFilesVersion() {
        var version = Preferences.userNodeForPackage(DiagramProject.class).getLong("version", -1L);
        if (version == -1L)
            bumpVersion();
        return version;
    }

    private void bumpVersion() {
        Preferences.userNodeForPackage(DiagramProject.class).putLong("version", System.currentTimeMillis());
    }
}

