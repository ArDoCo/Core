/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import java.io.Serializable;

public interface PhraseMappingChangeListener extends Serializable {
    void onDelete(PhraseMapping deletedPhraseMapping, PhraseMapping replacement);
}
