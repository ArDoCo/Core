package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.Map;

public interface INounMappingWithDistribution extends INounMapping{

    Map<MappingKind, Double> getDistribution();

}
