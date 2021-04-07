package edu.kit.ipd.consistency_analyzer.datastructures;

import java.util.Map;

public interface INounMappingWithDistribution extends INounMapping{

    Map<MappingKind, Double> getDistribution();

}
