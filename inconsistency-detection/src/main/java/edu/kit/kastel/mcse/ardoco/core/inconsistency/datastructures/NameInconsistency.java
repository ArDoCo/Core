package edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

public class NameInconsistency implements IInconsistency {

    private static final String INCONSISTENCY_TYPE_NAME = "NameInconsistency";

    private static final String REASON_FORMAT_STRING = "Inconsistent naming in trace link between textual occurence \"%s\" (sentence %d) and model element \"%s\" (%s)";

    private IModelInstance modelInstance;
    private IWord word;

    public NameInconsistency(IModelInstance modelInstance, IWord word) {
        this.modelInstance = modelInstance;
        this.word = word;
    }

    @Override
    public String getReason() {
        int sentenceNo = word.getSentenceNo() + 1;
        String textOccurence = word.getText();
        String modelOccurence = modelInstance.getLongestName();
        String uid = modelInstance.getUid();
        return String.format(Locale.US, REASON_FORMAT_STRING, textOccurence, sentenceNo, modelOccurence, uid);
    }

    @Override
    public IInconsistency createCopy() {
        return new NameInconsistency(modelInstance.createCopy(), word);
    }

    @Override
    public ImmutableList<String[]> toFileOutput() {
        MutableList<String[]> returnList = Lists.mutable.empty();
        var sentenceNo = "" + (word.getSentenceNo());
        var modelUid = modelInstance.getUid();
        returnList.add(new String[] { getType(), sentenceNo, word.getText(), modelInstance.getLongestName(), modelUid });
        return returnList.toImmutable();
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInstance, word);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NameInconsistency other = (NameInconsistency) obj;
        return Objects.equals(modelInstance, other.modelInstance) && Objects.equals(word, other.word);
    }

}
