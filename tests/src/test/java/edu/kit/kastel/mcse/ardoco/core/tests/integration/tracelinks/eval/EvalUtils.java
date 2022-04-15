package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;

import java.util.Collection;
import java.util.List;

public class EvalUtils {

    public static List<IModelInstance> getInstances(Collection<AgentDatastructure> dataCollection) {
        return dataCollection.stream().flatMap(data -> getInstances(data).stream()).toList();
    }

    public static List<IModelInstance> getInstances(AgentDatastructure data) {
        return data.getModelIds().stream().flatMap(id -> data.getModelState(id).getInstances().stream()).toList();
    }

	private EvalUtils() { }

}
