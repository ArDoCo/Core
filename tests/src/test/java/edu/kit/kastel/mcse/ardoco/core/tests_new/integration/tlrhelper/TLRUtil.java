package edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import org.eclipse.collections.api.factory.Lists;

import java.util.List;

public class TLRUtil {

    public static List<TestLink> getTraceLinks(DataRepository data) {
        var traceLinks = Lists.mutable.<TestLink>empty();
        var connectionStates = data.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        var modelStates = data.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        List<ConnectionState> connectionStatesList = modelStates.modelIds()
                .stream()
                .map(modelStates::getModelState)
                .map(ModelExtractionState::getMetamodel)
                .map(connectionStates::getConnectionState)
                .toList();
        for (var connectionState : connectionStatesList) {
            traceLinks.addAll(connectionState.getTraceLinks().stream().map(TestLink::new).toList());
        }
        return traceLinks.toList();
    }
}
