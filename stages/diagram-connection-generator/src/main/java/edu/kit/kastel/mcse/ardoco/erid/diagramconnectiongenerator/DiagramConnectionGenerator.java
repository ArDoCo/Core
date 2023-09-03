package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator;

import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents.InitialDiagramConnectionAgent;

public class DiagramConnectionGenerator extends ExecutionStage {

    public DiagramConnectionGenerator(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        super(List.of(new InitialDiagramConnectionAgent(dataRepository)), "DiagramConnectionGenerator", dataRepository, additionalConfigs);
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramConnectionGenerator States");
        var diagramConnectionStates = new DiagramConnectionStatesImpl();
        getDataRepository().addData(DiagramConnectionStates.ID, diagramConnectionStates);
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
