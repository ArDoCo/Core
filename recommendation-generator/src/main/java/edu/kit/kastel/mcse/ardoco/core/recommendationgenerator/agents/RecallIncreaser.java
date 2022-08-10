package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class RecallIncreaser extends PipelineAgent {

    public RecallIncreaser(DataRepository dataRepository) {
        super(RecallIncreaser.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {

        var textState = DataRepositoryHelper.getTextState(getDataRepository());

        combineNameMappings(textState);
    }

    private void combineNameMappings(TextState textState) {

        var nounMappings = textState.getNounMappings();

        var mergedNounMappings = Lists.mutable.empty();

        for (NounMapping nounMapping : nounMappings) {

            if (mergedNounMappings.contains(nounMapping)) {
                continue;
            }

            var similarNounMappings = textState.getNounMappings()
                    .select(nm -> SimilarityUtils.areNounMappingsSimilar(nounMapping, nm) && nounMapping.getKind().equals(nm.getKind()));
            mergedNounMappings.addAllIterable(similarNounMappings);
            textState.mergeNounMappings(nounMapping, similarNounMappings.toList(), this);

        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // No Delegates
    }
}
