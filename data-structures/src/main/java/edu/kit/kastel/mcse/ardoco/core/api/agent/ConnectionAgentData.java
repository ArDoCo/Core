/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IConnectionData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IModelData;
import edu.kit.kastel.mcse.ardoco.core.api.data.IRecommendationData;
import edu.kit.kastel.mcse.ardoco.core.api.data.ITextData;

public interface ConnectionAgentData extends IConnectionData, IRecommendationData, ITextData, IModelData {
}
