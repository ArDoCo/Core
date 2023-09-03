package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.RecommendedInstancesConfidenceAgent;

public class DiagramInconsistencyChecker extends ExecutionStage {
    public DiagramInconsistencyChecker(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        super(List.of(new InconsistencyAgent(dataRepository), new RecommendedInstancesConfidenceAgent(dataRepository)),
                DiagramInconsistencyChecker.class.getSimpleName(), dataRepository, additionalConfigs);
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramInconsistency States");
        var diagramInconsistencyStates = new DiagramInconsistencyStatesImpl();
        getDataRepository().addData(DiagramInconsistencyStates.ID, diagramInconsistencyStates);
    }

    private BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> previousCharacterMatchFunction;

    @Override
    protected void before() {
        super.before();
        previousCharacterMatchFunction = WordSimUtils.getCharacterMatchFunction();
        WordSimUtils.setCharacterMatchFunction(UnicodeCharacter.EQUAL_OR_HOMOGLYPH);
    }

    @Override
    protected void after() {
        WordSimUtils.setCharacterMatchFunction(previousCharacterMatchFunction);
        super.after();
    }
}
