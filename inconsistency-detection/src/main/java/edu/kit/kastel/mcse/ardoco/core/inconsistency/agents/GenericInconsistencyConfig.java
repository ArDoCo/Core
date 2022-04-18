/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

public class GenericInconsistencyConfig extends Configuration {

    public static final GenericInconsistencyConfig DEFAULT_CONFIG = new GenericInconsistencyConfig();

    private static final String MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD = "MissingModelElementInconsistencyAgent_threshold";
    private static final String TYPES_WITH_REQUIRED_DOCUMENTATION = "ModelElementTypesWithRequiredDocumentation";
    private static final String DOCUMENTATION_REQUIREMENT_WHITELIST = "ModelElementsDocumentationRequirementWhitelist";

    private final double missingModelInstanceInconsistencyThreshold;
    private final List<String> typesWithRequiredDocumentation;
    private final List<String> documentationRequirementWhitelist;

    public GenericInconsistencyConfig() {
        var config = new ResourceAccessor("/configs/InconsistencyCheckerConfig.properties", true);

        missingModelInstanceInconsistencyThreshold = config.getPropertyAsDouble(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD);

        var typesWithRequiredDocumentationProperty = config.getProperty(TYPES_WITH_REQUIRED_DOCUMENTATION);
        typesWithRequiredDocumentation = Lists.mutable.of(typesWithRequiredDocumentationProperty.split(" "));
        var documentationRequirementWhitelistProperty = config.getProperty(DOCUMENTATION_REQUIREMENT_WHITELIST);
        documentationRequirementWhitelist = Lists.mutable.of(documentationRequirementWhitelistProperty.split(" "));
    }

    public GenericInconsistencyConfig(Map<String, String> configs) {
        missingModelInstanceInconsistencyThreshold = Double.parseDouble(configs.get(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD));

        var typesWithRequiredDocumentationProperty = configs.get(TYPES_WITH_REQUIRED_DOCUMENTATION);
        typesWithRequiredDocumentation = Lists.mutable.of(typesWithRequiredDocumentationProperty.split(" "));
        var documentationRequirementWhitelistProperty = configs.get(DOCUMENTATION_REQUIREMENT_WHITELIST);
        documentationRequirementWhitelist = Lists.mutable.of(documentationRequirementWhitelistProperty.split(" "));
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(MISSING_MODEL_ELEMENT_INCONSISTENCY_AGENT_THRESHOLD, Double.toString(missingModelInstanceInconsistencyThreshold));
    }

    public double getMissingModelInstanceInconsistencyThreshold() {
        return missingModelInstanceInconsistencyThreshold;
    }

    /**
     * @return the typesWithRequiredDocumentation
     */
    public List<String> getTypesWithRequiredDocumentation() {
        return typesWithRequiredDocumentation;
    }

    /**
     * @return the documentationRequirementWhitelist
     */
    public List<String> getDocumentationRequirementWhitelist() {
        return documentationRequirementWhitelist;
    }

}