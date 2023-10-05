package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents.DiagramConnectionAgent;

/**
 * This stage is responsible for the creation of {@link edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi} trace links.
 */
public class DiagramConnectionGenerator extends ExecutionStage {

    /**
     * Sole constructor for the stage.
     *
     * @param additionalConfigs the additional configs that should be applied
     * @param dataRepository    the data repository that should be used
     */
    public DiagramConnectionGenerator(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        super(List.of(new DiagramConnectionAgent(dataRepository)), "DiagramConnectionGenerator", dataRepository, additionalConfigs);
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramConnectionGenerator States");
        var diagramConnectionStates = new DiagramConnectionStatesImpl();
        getDataRepository().addData(DiagramConnectionStates.ID, diagramConnectionStates);
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
