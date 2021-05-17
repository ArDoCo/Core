package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.InconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceLink;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

@MetaInfServices(InconsistencyAgent.class)
public class NameInconsistencyAgent extends InconsistencyAgent {

    public NameInconsistencyAgent() {
        super(GenericInconsistencyConfig.class);
    }

    private NameInconsistencyAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, GenericInconsistencyConfig inconsistencyConfig) {
        super(DependencyType.MODEL_RECOMMENDATION_CONNECTION, GenericInconsistencyConfig.class, text, textState, modelState, recommendationState,
                connectionState, inconsistencyState);
    }

    @Override
    public InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config) {
        return new NameInconsistencyAgent(text, textState, modelState, recommendationState, connectionState, inconsistencyState,
                (GenericInconsistencyConfig) config);

    }

    @Override
    public void exec() {
        // TODO Auto-generated method stub
        System.out.println("Executing NameInconsistencyAgent");
        List<IInstanceLink> tracelinks = connectionState.getInstanceLinks();
        for (IInstanceLink tracelink : tracelinks) {
            IInstance modelInstance = tracelink.getModelInstance();
            String modelName = modelInstance.getLongestName();
            System.out.println("\tTracelink for " + modelName);
            IRecommendedInstance recommendationInstance = tracelink.getTextualInstance();
            List<INounMapping> nameMappings = recommendationInstance.getNameMappings();
            for (INounMapping nameMapping : nameMappings) {
                String nameMappingReference = nameMapping.getReference();
                System.out.println("\t\tReference: " + nameMappingReference);
                List<String> occurences = nameMapping.getOccurrences();
                for (String occurence : occurences) {
                    System.out.println("\t\t\t Occurence: " + occurence);
                    // TODO: idea: if the occurence does not match the modelName, generate a inconsistency warning
                }
            }
        }
    }

}
