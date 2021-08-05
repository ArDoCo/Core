package edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures;

import java.util.Locale;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

public class NameInconsistency implements IInconsistency {

    private static final String REASON_FORMAT_STRING = "Inconsistent naming in trace link between textual occurence \"%s\" (sentence %d) and model element \"%s\" (%s)";

    private IModelInstance modelInstance;
    private IWord word;

    public NameInconsistency(IModelInstance modelInstance, IWord word) {
        this.modelInstance = modelInstance;
        this.word = word;
    }

    @Override
    public String getReason() {
        int sentenceNo = word.getSentenceNo();
        String textOccurence = word.getText();
        String modelOccurence = modelInstance.getLongestName();
        String uid = modelInstance.getUid();
        return String.format(Locale.US, REASON_FORMAT_STRING, textOccurence, sentenceNo, modelOccurence, uid);
    }

    @Override
    public IInconsistency createCopy() {
        return new NameInconsistency(modelInstance.createCopy(), word);
    }

}
