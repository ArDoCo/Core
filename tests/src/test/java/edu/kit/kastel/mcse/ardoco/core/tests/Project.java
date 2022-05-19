/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.GoldStandard;
import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.text.providers.json.JsonTextProvider;

public enum Project {
    MEDIASTORE(//
            "src/test/resources/benchmark/mediastore/original_model/ms.repository", //
            "src/test/resources/benchmark/mediastore/mediastore.txt", //
            "src/test/resources/benchmark/mediastore/goldstandard.csv", //
            new EvaluationResults(.999, .620, .765), //
            new EvaluationResults(.0, .0, .0)//
    ), //
    TEAMMATES( //
            "src/test/resources/benchmark/teammates/original_model/teammates.repository", //
            "src/test/resources/benchmark/teammates/teammates.txt", //
            "src/test/resources/benchmark/teammates/goldstandard.csv", //
            new EvaluationResults(.913, .880, .896), //
            new EvaluationResults(.0, .0, .0)//
    ), //
    TEASTORE( //
            "src/test/resources/benchmark/teastore/original_model/teastore.repository", //
            "src/test/resources/benchmark/teastore/teastore.txt", //
            "src/test/resources/benchmark/teastore/goldstandard.csv", //
            new EvaluationResults(.999, .713, .832), //
            new EvaluationResults(.0, .0, .0)),
    BIGBLUEBUTTON( //
            "src/test/resources/benchmark/bigbluebutton/original_model/bbb.repository", //
            "src/test/resources/benchmark/bigbluebutton/bigbluebutton.txt", //
            "src/test/resources/benchmark/bigbluebutton/goldstandard.csv", //
            new EvaluationResults(.877, .826, .850), //
            new EvaluationResults(.0, .0, .0));

    private final String model;
    private final String textFile;
    private final String goldStandard;
    private final EvaluationResults expectedTraceLinkResults;
    private final EvaluationResults expectedInconsistencyResults;
    private volatile IModelConnector modelConnector = null;

    Project(String model, String textFile, String goldStandard, EvaluationResults expectedTraceLinkResults, EvaluationResults expectedInconsistencyResults) {
        this.model = model;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
        this.expectedTraceLinkResults = expectedTraceLinkResults;
        this.expectedInconsistencyResults = expectedInconsistencyResults;
    }

    public File getModelFile() {
        return new File(model);
    }

    public File getTextFile() {
        return new File(textFile);
    }

    public File getPreprocessedTextFile() {
        return new File(this.textFile + ".json");
    }

    public File getGoldStandardFile() {
        return new File(goldStandard);
    }

    public IModelConnector getModel() {
        if (modelConnector == null) {
            synchronized (this) {
                if (modelConnector == null) {
                    try {
                        modelConnector = new PcmXMLModelConnector(getModelFile());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return modelConnector;
    }

    public IText getText() {
        return getTextPreprocessed();
    }

    public IText getTextPreprocessed() {
        try {
            ITextConnector textConnector = JsonTextProvider.loadFromFile(getPreprocessedTextFile());
            return textConnector.getAnnotatedText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IText getTextViaFile() {
        try {
            ITextConnector textConnector = new CoreNLPProvider(new FileInputStream(getTextFile()));
            return textConnector.getAnnotatedText();
        } catch (FileNotFoundException e) {
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
