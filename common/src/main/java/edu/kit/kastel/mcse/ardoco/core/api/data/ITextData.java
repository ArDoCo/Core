/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public interface ITextData extends IData {
    IText getText();

    void setTextState(ITextState state);

    ITextState getTextState();
}
