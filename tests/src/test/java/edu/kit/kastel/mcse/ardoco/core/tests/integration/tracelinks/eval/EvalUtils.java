package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.text.ISentence;

import java.util.Collection;
import java.util.List;

public class EvalUtils {

    public static List<IModelInstance> getInstances(Collection<AgentDatastructure> dataCollection) {
        return dataCollection.stream().flatMap(data -> getInstances(data).stream()).toList();
    }

    public static List<IModelInstance> getInstances(AgentDatastructure data) {
        return data.getModelIds().stream().flatMap(id -> data.getModelState(id).getInstances().stream()).toList();
    }

	public static String formatLink(TestLink link, AgentDatastructure data) {
		ISentence sentence = data.getText().getSentences().stream()
			.filter(s -> s.getSentenceNumber() == link.sentenceNr())
			.findAny().orElse(null);

		IModelInstance mInstance = data.getModelIds().stream()
			.flatMap(mId -> data.getModelState(mId).getInstances().stream())
			.filter(modelInstance -> modelInstance.getUid().equals(link.modelId()))
			.findAny().orElse(null);

		var sentenceStr = sentence == null ? "NULL" : '"' + sentence.getText() + '"';
		var modelStr = mInstance == null ? "NULL" : '"' + mInstance.getFullName() + '"';

		return String.format("%s - %s [%s,%s]", modelStr, sentenceStr, link.modelId(), link.sentenceNr());
	}

	private EvalUtils() { }

}
