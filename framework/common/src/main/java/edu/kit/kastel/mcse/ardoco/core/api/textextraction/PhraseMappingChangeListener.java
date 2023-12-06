/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.io.Serializable;

public interface PhraseMappingChangeListener extends Serializable {
    void onDelete(PhraseMapping deletedPhraseMapping, PhraseMapping replacement);
}
