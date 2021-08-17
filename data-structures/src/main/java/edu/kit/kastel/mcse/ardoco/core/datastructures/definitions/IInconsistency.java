package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ICopyable;

public interface IInconsistency extends ICopyable<IInconsistency> {

    /**
     * Returns the reason why there is an inconsistency
     *
     * @return The reason of inconsistency
     */
    String getReason();

    /**
     * Returns the name of the type of inconsistency
     *
     * @return the name of the type of inconsistency
     */
    String getType();

    /**
     * Return a list with String arrays as entries. The entries should have the format to first state the type of
     * inconsistency, then the sentence number and third the id of the model element or the name of the text element (or
     * both). Fourth entry can be an optional confidence value
     *
     * @return List with String arrays as entry with the format {SentenceNumber, ModelElementId/TextElement, (optional)
     *         confidence}.
     */
    ImmutableList<String[]> toFileOutput();

}
