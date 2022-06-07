/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IDiagramDetectionData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IModelData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IRecommendationData;
import edu.kit.kastel.mcse.ardoco.core.api.data.ITextData;

public interface RecommendationAgentData extends IRecommendationData, ITextData, IModelData, IDiagramDetectionData {
}
