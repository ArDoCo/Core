/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.MissingModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.agents.UndocumentedModelElementInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class InconsistencyChecker extends AbstractExecutionStage {

    public InconsistencyChecker(DataRepository dataRepository) {
        super(List.of(new InitialInconsistencyAgent(dataRepository), new MissingModelElementInconsistencyAgent(dataRepository),
                new UndocumentedModelElementInconsistencyAgent(dataRepository)), "InconsistencyChecker", dataRepository);
    }

    /**
     * Creates an {@link InconsistencyChecker} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static InconsistencyChecker get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    @Override
    protected void initializeState() {
        var inconsistencyStates = InconsistencyStatesImpl.build(dataRepository);
        getDataRepository().addData(InconsistencyStates.ID, inconsistencyStates);
    }

}
