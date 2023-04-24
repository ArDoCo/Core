/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

public interface PhraseMappingChangeListener {
    void onDelete(PhraseMapping deletedPhraseMapping, PhraseMapping replacement);
}
