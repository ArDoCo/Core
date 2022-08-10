/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.PhraseMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;

public class NameTypeExtractor2 extends Informant {

    public NameTypeExtractor2(DataRepository dataRepository) {
        super("NameTypeExtractor2", dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
    }

    private void combineNameAndTypes(TextState textState, RecommendationState recommendationState) {

        var nounMappings = textState.getNounMappings();
        var nameMappings = nounMappings.select(nm -> nm.getProbabilityForKind(MappingKind.NAME) > 0.3);
        var typeMappings = nounMappings.select(nm -> nm.getProbabilityForKind(MappingKind.TYPE) > 0.5);

        for (PhraseMapping phraseMapping : textState.getPhraseMappings()) {

            var nounMappingsOfPhraseMapping = textState.getNounMappingsByPhraseMapping(phraseMapping);

            NounMapping name = null;
            NounMapping type = null;
            NounMapping nort = null;

            for (NounMapping nounMappingOfPM : nounMappingsOfPhraseMapping) {

                if (nameMappings.contains(nounMappingOfPM) && typeMappings.contains(nounMappingOfPM)) {
                    if (nort == null) {
                        nort = nounMappingOfPM;
                    } else {
                        var nortKind = nort.getKind();
                        var nounMappingOfPMKind = nounMappingOfPM.getKind();
                        if (nortKind == nounMappingOfPMKind) {
                            double nortKindProb = nort.getProbabilityForKind(nortKind);
                            double nounMappingOfPMKindProb = nounMappingOfPM.getProbabilityForKind(nounMappingOfPMKind);
                            if (nortKindProb > nounMappingOfPMKindProb) {
                                if (nortKind.equals(MappingKind.NAME)) {
                                    name = nort;
                                    type = nounMappingOfPM;
                                    nort = null;
                                } else {
                                    name = nounMappingOfPM;
                                    type = nort;
                                    nort = null;
                                }

                            } else {

                                if (nortKind.equals(MappingKind.NAME)) {
                                    name = nounMappingOfPM;
                                    type = nort;
                                    nort = null;
                                } else {
                                    name = nort;
                                    type = nounMappingOfPM;
                                    nort = null;
                                }

                            }

                        } else {
                            if (nortKind.equals(MappingKind.NAME)) {
                                name = nort;
                                type = nounMappingOfPM;
                                nort = null;
                            } else {
                                name = nounMappingOfPM;
                                type = nort;
                                nort = null;
                            }
                        }
                    }
                } else if (nameMappings.contains(nounMappingOfPM)) {
                    if (name == null) {
                        name = nounMappingOfPM;
                    } else {
                        break;
                    }
                } else if (typeMappings.contains(nounMappingOfPM)) {
                    if (type == null) {
                        type = nounMappingOfPM;
                    } else {
                        break;
                    }

                }

            }

            if (type == null && nort != null) {
                type = nort;
            }
            if (name == null && nort != null) {
                name = nort;
            }

            if (name != null && type != null) {
                recommendationState.addRecommendedInstance(name.getReference(), type.getReference(), this, 0.8, Lists.immutable.with(name),
                        Lists.immutable.with(type));

            }

        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // handle additional config
    }
}
