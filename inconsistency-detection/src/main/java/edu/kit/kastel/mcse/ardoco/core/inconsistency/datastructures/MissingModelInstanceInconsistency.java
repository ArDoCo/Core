package edu.kit.kastel.mcse.ardoco.core.inconsistency.datastructures;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;

public class MissingModelInstanceInconsistency implements IInconsistency {

    private static final String REASON_FORMAT_STRING = "Text indicates that \"%s\" should be contained in the model(s) but could not be found.";

    private IRecommendedInstance textualInstance;

    public MissingModelInstanceInconsistency(IRecommendedInstance textualInstance) {
        this.textualInstance = textualInstance;
    }

    @Override
    public String getReason() {
        var name = textualInstance.getName();
        return String.format(REASON_FORMAT_STRING, name);
    }

}
