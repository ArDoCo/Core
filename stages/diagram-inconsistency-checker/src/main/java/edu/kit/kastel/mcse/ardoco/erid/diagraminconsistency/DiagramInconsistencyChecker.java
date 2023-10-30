/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency;

import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.DiagramInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents.RecommendedInstancesConfidenceAgent;

/**
 * This stage is responsible for creating the inconsistencies and uses them to adjust the confidence of recommended instances.
 */
public class DiagramInconsistencyChecker extends ExecutionStage {
    /**
     * Sole constructor of the stage.
     *
     * @param additionalConfigs the additional configs that should be used
     * @param dataRepository    the data repository that should be used
     */
    public DiagramInconsistencyChecker(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        super(List.of(new DiagramInconsistencyAgent(dataRepository), new RecommendedInstancesConfidenceAgent(dataRepository)), DiagramInconsistencyChecker.class
                .getSimpleName(), dataRepository, additionalConfigs);
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramInconsistency States");
        var diagramInconsistencyStates = new DiagramInconsistencyStatesImpl();
        getDataRepository().addData(DiagramInconsistencyStates.ID, diagramInconsistencyStates);
    }

    private BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> previousCharacterMatchFunction;

    /**
     * Saves the previous character match function and sets a character match function capable of matching homoglyphs.
     */
    @Override
    protected void before() {
        super.before();
        previousCharacterMatchFunction = WordSimUtils.getCharacterMatchFunction();
        WordSimUtils.setCharacterMatchFunction(UnicodeCharacter.EQUAL_OR_HOMOGLYPH);
    }

    /**
     * Sets the character match function back to the previous function.
     */
    @Override
    protected void after() {
        WordSimUtils.setCharacterMatchFunction(previousCharacterMatchFunction);
        super.after();
    }
}