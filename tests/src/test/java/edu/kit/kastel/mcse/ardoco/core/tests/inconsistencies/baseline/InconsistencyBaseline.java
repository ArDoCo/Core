/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline;

import java.util.Map;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;

public class InconsistencyBaseline extends AbstractExecutionStage {

    public InconsistencyBaseline() {
        super();
    }

    @Override
    public void execute(DataStructure data, Map<String, String> additionalSettings) {
        data.getModelIds().forEach(mid -> data.setInconsistencyState(mid, new InconsistencyState(additionalSettings)));
        this.applyConfiguration(additionalSettings);

        var sentences = Sets.mutable.fromStream(data.getText().getSentences().stream().map(ISentence::getSentenceNumber));
        for (var model : data.getModelIds()) {
            var traceLinks = data.getConnectionState(model).getTraceLinks();
            var sentencesWithTraceLinks = traceLinks.collect(TraceLink::getSentenceNumber).toSet();
            MutableSet<Integer> sentencesWithoutTraceLinks = sentences.withoutAll(sentencesWithTraceLinks);

            IInconsistencyState inconsistencyState = data.getInconsistencyState(model);
            for (var sentence : sentencesWithoutTraceLinks) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency("", sentence + 1, 0.69));
            }
        }

    }
}
