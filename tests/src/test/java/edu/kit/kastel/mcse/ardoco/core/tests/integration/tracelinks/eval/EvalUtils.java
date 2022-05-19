/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import java.util.Collection;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;

public class EvalUtils {

    public static List<IModelInstance> getInstances(Collection<DataStructure> dataCollection) {
        return dataCollection.stream().flatMap(data -> getInstances(data).stream()).toList();
    }

    public static List<IModelInstance> getInstances(DataStructure data) {
        return data.getModelIds().stream().flatMap(id -> data.getModelState(id).getInstances().stream()).toList();
    }

    public static List<TraceLink> getTraceLinks(DataStructure data) {
        return data.getModelIds().stream().flatMap(mId -> data.getConnectionState(mId).getTraceLinks().stream()).toList();
    }

    public static String formatLink(TestLink link, DataStructure data) {
        ISentence sentence = data.getText().getSentences().stream().filter(s -> s.getSentenceNumber() == link.sentenceNr()).findAny().orElse(null);

        IModelInstance mInstance = getInstances(data).stream().filter(modelInstance -> modelInstance.getUid().equals(link.modelId())).findAny().orElse(null);

        var sentenceStr = sentence == null ? "NULL" : '"' + sentence.getText() + '"';
        var modelStr = mInstance == null ? "NULL" : '"' + mInstance.getFullName() + '"';

        return String.format("%s - %s [%s,%s]", modelStr, sentenceStr, link.modelId(), link.sentenceNr());
    }

    private EvalUtils() {
    }

}
