/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.baseline;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;

public class InconsistencyBaseline extends AbstractExecutionStage {

    public InconsistencyBaseline(DataRepository dataRepository) {
        super("InconsistencyBaseline", dataRepository);
    }

    @Override
    public void run() {
        var inconsistencyStates = InconsistencyStatesImpl.build();
        DataRepository dataRepository = getDataRepository();
        dataRepository.addData(InconsistencyStates.ID, inconsistencyStates);

        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);

        var sentences = Sets.mutable.fromStream(text.getSentences().stream().map(Sentence::getSentenceNumber));
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var traceLinks = connectionStates.getConnectionState(metamodel).getTraceLinks();
            var sentencesWithTraceLinks = traceLinks.collect(TraceLink::getSentenceNumber).toSet();
            MutableSet<Integer> sentencesWithoutTraceLinks = sentences.withoutAll(sentencesWithTraceLinks);

            InconsistencyState inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            for (var sentence : sentencesWithoutTraceLinks) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency("", sentence + 1, 0.69));
            }
        }
    }
}
