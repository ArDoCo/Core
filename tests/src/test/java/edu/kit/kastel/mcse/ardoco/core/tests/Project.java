/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.kit.ipd.parse.luna.LunaInitException;
import edu.kit.ipd.parse.luna.LunaRunException;
import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.PcmOntologyModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ParseProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ontology.OntologyTextProvider;

public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/mediastore.owl", //
            "src/test/resources/benchmark/mediastore/mediastore_w_text.owl", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            new EvaluationResults(.943, .606, .738), //
            new EvaluationResults(.99, .606, .755)//
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/teammates.owl", //
            "src/test/resources/benchmark/teammates/teammates_w_text.owl", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            new EvaluationResults(.877, .877, .877), //
            new EvaluationResults(.882, .828, .854)//
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/teastore.owl", //
            "src/test/resources/benchmark/teastore/teastore_w_text.owl", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            new EvaluationResults(.99, .713, .832), //
            new EvaluationResults(.99, .713, .832)),
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/bbb.owl", //
            "src/test/resources/benchmark/bigbluebutton/bbb_w_text.owl", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            new EvaluationResults(.734, .817, .773), //
            new EvaluationResults(.734, .817, .773));

    private final String model;
    private final String textOntology;
    private final String textFile;
    private final String goldStandard;
    private final EvaluationResults expectedTraceLinkResults;
    private final EvaluationResults expectedInconsistencyResults;

    Project(String model, String textOntology, String textFile, String goldStandard, EvaluationResults expectedTraceLinkResults,
            EvaluationResults expectedInconsistencyResults) {
        this.model = model;
        this.textOntology = textOntology;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    public File getModelFile() {
        return new File(model);
    }

    public File getTextOntologyFile() {
        return new File(textOntology);
    }

    public File getTextFile() {
        return new File(textFile);
    }

    public File getGoldStandardFile() {
        return new File(goldStandard);
    }

    public IModelConnector getModel() {
        OntologyConnector ontoConnector = new OntologyConnector(getModelFile().getAbsolutePath());
        return new PcmOntologyModelConnector(ontoConnector);
    }

    public IText getText() {
        return getTextViaOntology();
    }

    public IText getTextViaOntology() {
        var connector = new OntologyConnector(getTextOntologyFile().getAbsolutePath());
        return OntologyTextProvider.get(connector).getAnnotatedText();
    }

    public IText getTextViaFile() {

        try {
            ITextConnector textConnector = new ParseProvider(new FileInputStream(getTextFile()));
            return textConnector.getAnnotatedText();
        } catch (FileNotFoundException | LunaRunException | LunaInitException e) {
            e.printStackTrace();
            return null;
        }
    }

    public GoldStandard getGoldStandard(IModelConnector pcmModel) {
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
