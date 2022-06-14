/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.baseline;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.stage.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;

public class InconsistencyBaseline extends AbstractExecutionStage {

    public InconsistencyBaseline(DataRepository dataRepository) {
        super("InconsistencyBaseline", dataRepository);
    }

    @Override
    public void run() {
        var inconsistencyStates = InconsistencyStates.build();
        DataRepository dataRepository = getDataRepository();
        dataRepository.addData(IInconsistencyStates.ID, inconsistencyStates);

        var text = getText(dataRepository);
        var modelStates = getModelStatesData(dataRepository);
        var connectionStates = getConnectionStates(dataRepository);

        var sentences = Sets.mutable.fromStream(text.getSentences().stream().map(ISentence::getSentenceNumber));
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var traceLinks = connectionStates.getConnectionState(metamodel).getTraceLinks();
            var sentencesWithTraceLinks = traceLinks.collect(TraceLink::getSentenceNumber).toSet();
            MutableSet<Integer> sentencesWithoutTraceLinks = sentences.withoutAll(sentencesWithTraceLinks);

            IInconsistencyState inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            for (var sentence : sentencesWithoutTraceLinks) {
                inconsistencyState.addInconsistency(new MissingModelInstanceInconsistency("", sentence + 1, 0.69));
            }
        }
    }

    public static IText getText(DataRepository dataRepository) {
        var preprocessingData = dataRepository.getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow();
        return preprocessingData.getText();
    }

    public static ModelStates getModelStatesData(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();
    }

    public static IConnectionStates getConnectionStates(DataRepository dataRepository) {
        return dataRepository.getData(IConnectionStates.ID, IConnectionStates.class).orElseThrow();
    }

}
