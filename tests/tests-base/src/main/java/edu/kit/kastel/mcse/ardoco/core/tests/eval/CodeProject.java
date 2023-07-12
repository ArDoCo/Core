/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import static edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper.ANALYZE_CODE_DIRECTLY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.TraceLinkUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

public enum CodeProject {
    MEDIASTORE(//
            Project.MEDIASTORE, //
            "https://github.com/ArDoCo/MediaStore3.git", //
            "94c398fa02b3d6b8d71517522a7206d37ed3a9af", //
            "/benchmark/mediastore/model_2016/code/codeModel.acm",//
            "/benchmark/mediastore/model_2016/goldstandard_sam-code.csv", //
            "/benchmark/mediastore/text_2016/goldstandard_code.csv", //
            new ExpectedResults(.975, .995, .985, .995, .985, .995), //
            new ExpectedResults(.995, .515, .675, .990, .715, .999) //
    ),

    TEASTORE(Project.TEASTORE, //
            "https://github.com/ArDoCo/TeaStore.git", //
            "bdc49020a55cfa97eaabbb25744fefbc2697defa", //
            "/benchmark/teastore/model_2022/code/codeModel.acm",//
            "/benchmark/teastore/model_2022/goldstandard_sam_2020-code.csv", //
            "/benchmark/teastore/text_2020/goldstandard_code_2022.csv", //
            new ExpectedResults(.975, .975, .975, .997, .965, .999), //
            new ExpectedResults(.999, .708, .829, .976, .831, .999) //
    ),

    TEAMMATES(Project.TEAMMATES, //
            "https://github.com/ArDoCo/teammates.git",//
            "b24519a2af9e17b2bc9c025e87e4cf60009c425d",//
            "/benchmark/teammates/model_2023/code/codeModel.acm",//
            "/benchmark/teammates/model_2023/goldstandard_sam_2021-code.csv",//
            "/benchmark/teammates/text_2021/goldstandard_code_2023.csv", //
            new ExpectedResults(.999, .999, .999, .999, .999, .999), //
            new ExpectedResults(.705, .909, .795, .975, .785, .975) //
    ),

    BIGBLUEBUTTON(Project.BIGBLUEBUTTON,//
            "https://github.com/ArDoCo/bigbluebutton.git",//
            "8fa2507d6c3865a9850004fd6fefd09738e68406",//
            "/benchmark/bigbluebutton/model_2023/code/codeModel.acm",//
            "/benchmark/bigbluebutton/model_2023/goldstandard_sam_2021-code.csv", //
            "/benchmark/bigbluebutton/text_2021/goldstandard_code_2023.csv", //
            new ExpectedResults(.874, .953, .912, .989, .908, .985), //
            new ExpectedResults(.765, .905, .835, .985, .825, .985) //
    ),

    JABREF(Project.JABREF, //
            "https://github.com/ArDoCo/jabref.git",//
            "6269698cae437610ec79c38e6dd611eef7e88afe",//
            "/benchmark/jabref/model_2023/code/codeModel.acm",//
            "/benchmark/jabref/model_2023/goldstandard_sam_2021-code.csv", //
            "/benchmark/jabref/text_2021/goldstandard_code_2023.csv", //
            new ExpectedResults(.999, .999, .999, .999, .999, .999), //
            new ExpectedResults(.885, .999, .935, .960, .915, .935) //
    );

    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private final String codeRepository;
    private final String commitHash;
    private final String codeModelLocationInResources;
    private final String samCodeGoldStandardLocation;
    private final String sadCodeGoldStandardLocation;
    private final Project project;
    private final ExpectedResults expectedResultsForSamCode;
    private final ExpectedResults expectedResultsForSadSamCode;

    CodeProject(Project project, String codeRepository, String commitHash, String codeModelLocationInResources, String samCodeGoldStandardLocation,
            String sadCodeGoldStandardLocation, ExpectedResults expectedResultsForSamCode, ExpectedResults expectedResultsForSadSamCode) {
        this.project = project;
        this.codeRepository = codeRepository;
        this.commitHash = commitHash;
        this.codeModelLocationInResources = codeModelLocationInResources;
        this.samCodeGoldStandardLocation = samCodeGoldStandardLocation;
        this.sadCodeGoldStandardLocation = sadCodeGoldStandardLocation;
        this.expectedResultsForSamCode = expectedResultsForSamCode;
        this.expectedResultsForSadSamCode = expectedResultsForSadSamCode;
    }

    public Project getProject() {
        return project;
    }

    public String getCodeRepository() {
        return codeRepository;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getCodeLocation() {
        return getTemporaryCodeLocation().getAbsolutePath();
    }

    public String getCodeModelDirectory() {
        try {
            loadCodeModelFromResourcesIfNeeded();
            return getTemporaryCodeLocation().getAbsolutePath();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public void loadCodeModelFromResourcesIfNeeded() throws IOException {
        if (ANALYZE_CODE_DIRECTLY.get())
            return;

        File temporaryCodeLocation = getTemporaryCodeLocation();
        File codeModelFile = new File(temporaryCodeLocation + "/codeModel.acm");
        try (InputStream is = getClass().getResourceAsStream(this.codeModelLocationInResources)) {
            try (FileOutputStream fos = new FileOutputStream(codeModelFile)) {
                is.transferTo(fos);
            }
        }
    }

    public ExpectedResults getExpectedResultsForSamCode() {
        return expectedResultsForSamCode;
    }

    public ExpectedResults getExpectedResultsForSadSamCode() {
        return expectedResultsForSadSamCode;
    }

    public ImmutableList<String> getSamCodeGoldStandard() {
        File samCodeGoldStandardFile = ProjectHelper.loadFileFromResources(samCodeGoldStandardLocation);
        List<String> lines = getLinesFromGoldStandardFile(samCodeGoldStandardFile);

        MutableList<String> goldStandard = Lists.mutable.empty();
        for (var line : lines) {
            if (line.isBlank())
                continue;
            var parts = line.split(",");
            String modelElementId = parts[0];
            String codeElementId = parts[2];
            goldStandard.add(TraceLinkUtilities.createTraceLinkString(modelElementId, codeElementId));
        }
        return goldStandard.toImmutable();
    }

    public ImmutableList<String> getSadCodeGoldStandard() {
        File sadCodeGoldStandardFile = ProjectHelper.loadFileFromResources(sadCodeGoldStandardLocation);
        List<String> lines = getLinesFromGoldStandardFile(sadCodeGoldStandardFile);
        return Lists.immutable.ofAll(lines);
    }

    private static List<String> getLinesFromGoldStandardFile(File samCodeGoldStandardFile) {
        var path = Paths.get(samCodeGoldStandardFile.toURI());
        List<String> lines = Lists.mutable.empty();
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        lines.remove(0);
        lines = lines.stream().filter(Predicate.not(String::isBlank)).toList();
        return lines;
    }

    private File getTemporaryCodeLocation() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        var temporary = new File(tmpdir + File.separator + "ArDoCo" + File.separator + project.name());
        logger.debug("Location of Code: {}", temporary.getAbsolutePath());
        temporary.mkdirs();
        return temporary;
    }

}
