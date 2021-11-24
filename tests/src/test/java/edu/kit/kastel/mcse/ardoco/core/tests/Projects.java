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

public enum Projects {
    MEDIASTORE(//
            "src/test/resources/mediastore/mediastore.owl", //
            "src/test/resources/mediastore/mediastore_w_text.owl", //
            "src/test/resources/mediastore/mediastore.txt", //
            "src/test/resources/mediastore/goldstandard.csv" //
    ), //
    TEAMMATES( //
            "src/test/resources/teammates/inconsistency/tm.owl", //
            "src/test/resources/teammates/teammates_w_text.owl", //
            "src/test/resources/teammates/teammates.txt", //
            "src/test/resources/teammates/goldstandard.csv" //
    ), //
    TEASTORE( //
            "src/test/resources/teastore/teastore.owl", //
            "src/test/resources/teastore/teastore_w_text.owl", //
            "src/test/resources/teastore/teastore.txt", //
            "src/test/resources/teastore/goldstandard.csv");

    private String model;
    private String textOntology;
    private String textFile;
    private String goldStandard;

    Projects(String model, String textOntology, String textFile, String goldStandard) {
        this.model = model;
        this.textOntology = textOntology;
        this.textFile = textFile;
        this.goldStandard = goldStandard;
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

}
