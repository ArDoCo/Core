/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;

public enum CodeProject {
    MEDIASTORE(//
            Project.MEDIASTORE, //
            "https://github.com/ArDoCo/MediaStore3.git", //
            "../../temp/code/mediastore",//
            "src/test/resources/codeModels/mediastore",//
            "src/test/resources/gs-code-tlr/goldstandard-mediastore.csv", //
            "src/test/resources/gs-sad-code-tlr/goldstandard-mediastore.csv", //
            new ExpectedResults(.98, .99, .99, .99, .99, .99), //
            new ExpectedResults(.99, .51, .67, .99, .71, .99) //
    ),

    TEASTORE(Project.TEASTORE, //
            "https://github.com/ArDoCo/TeaStore.git", //
            "../../temp/code/teastore",//
            "src/test/resources/codeModels/teastore",//
            "src/test/resources/gs-code-tlr/goldstandard-teastore.csv",//
            "src/test/resources/gs-sad-code-tlr/goldstandard-teastore.csv",//
            new ExpectedResults(.97, .97, .97, .99, .96, .99), //
            new ExpectedResults(.99, .95, .97, .99, .97, .99) //
    ),

    TEAMMATES(Project.TEAMMATES, //
            "https://github.com/ArDoCo/teammates.git",//
            "../../temp/code/teammates",//
            "src/test/resources/codeModels/teammates",//
            "src/test/resources/gs-code-tlr/goldstandard-teammates.csv",//
            "src/test/resources/gs-sad-code-tlr/goldstandard-teammates.csv",//
            new ExpectedResults(.99, .99, .99, .99, .99, .99), //
            new ExpectedResults(.58, .99, .73, .97, .75, .97) //
    ),

    BIGBLUEBUTTON(Project.BIGBLUEBUTTON,//
            "https://github.com/ArDoCo/bigbluebutton.git",//
            "../../temp/code/bigbluebutton",//
            "src/test/resources/codeModels/bigbluebutton",//
            "src/test/resources/gs-code-tlr/goldstandard-bigbluebutton.csv",//
            "src/test/resources/gs-sad-code-tlr/goldstandard-bigbluebutton.csv",//
            new ExpectedResults(.93, .98, .96, .99, .95, .99), //
            new ExpectedResults(.77, .98, .86, .98, .86, .98) //
    ),

    JABREF(Project.JABREF, //
            "https://github.com/ArDoCo/jabref.git",//
            "../../temp/code/jabref",//
            "src/test/resources/codeModels/jabref",//
            "src/test/resources/gs-code-tlr/goldstandard-jabref.csv", //
            "src/test/resources/gs-sad-code-tlr/goldstandard-jabref.csv", //
            new ExpectedResults(.99, .99, .99, .99, .99, .99), //
            new ExpectedResults(.88, .99, .93, .96, .91, .93) //
    );

    private static final Logger logger = LoggerFactory.getLogger(Project.class);

    private final String codeRepository;
    private final String codeLocation;
    private final String codeModelLocation;
    private final String samCodeGoldStandardLocation;
    private final String sadCodeGoldStandardLocation;
    private final Project project;
    private final ExpectedResults expectedResultsForSamCode;
    private final ExpectedResults expectedResultsForSadSamCode;

    CodeProject(Project project, String codeRepository, String codeLocation, String codeModelLocation, String samCodeGoldStandardLocation,
            String sadCodeGoldStandardLocation, ExpectedResults expectedResultsForSamCode, ExpectedResults expectedResultsForSadSamCode) {
        this.project = project;
        this.codeRepository = codeRepository;
        this.codeLocation = codeLocation;
        this.codeModelLocation = codeModelLocation;
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

    public String getCodeLocation() {
        return codeLocation;
    }

    public String getCodeModelLocation() {
        return codeModelLocation;
    }

    public ExpectedResults getExpectedResultsForSamCode() {
        return expectedResultsForSamCode;
    }

    public ExpectedResults getExpectedResultsForSadSamCode() {
        return expectedResultsForSadSamCode;
    }

    public ImmutableList<String> getSamCodeGoldStandard() {
        File samCodeGoldStandardFile = new File(samCodeGoldStandardLocation);
        List<String> lines = getLinesFromGoldStandardFile(samCodeGoldStandardFile);

        MutableList<String> goldStandard = Lists.mutable.empty();
        for (var line : lines) {
            if (line.isBlank())
                continue;
            var parts = line.split(",");
            String modelElementId = parts[0];
            String codeElementId = parts[2];
            goldStandard.add(TestUtil.createTraceLinkString(modelElementId, codeElementId));
        }
        return goldStandard.toImmutable();
    }

    public ImmutableList<String> getSadCodeGoldStandard() {
        File sadCodeGoldStandardFile = new File(sadCodeGoldStandardLocation);
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

}
