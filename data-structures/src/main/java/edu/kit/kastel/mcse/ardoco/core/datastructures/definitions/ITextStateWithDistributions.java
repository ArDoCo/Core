package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface ITextStateWithDistributions extends ITextState {

    List<INounMappingWithDistribution> getMappingsWithDistributions();

}
