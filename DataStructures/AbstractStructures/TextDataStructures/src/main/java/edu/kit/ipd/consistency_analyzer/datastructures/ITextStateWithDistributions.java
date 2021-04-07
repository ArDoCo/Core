package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.List;

public interface ITextStateWithDistributions extends ITextState {

    List<INounMappingWithDistribution> getMappingsWithDistributions();

}
