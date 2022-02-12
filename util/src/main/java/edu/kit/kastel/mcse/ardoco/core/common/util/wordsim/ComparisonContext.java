package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A ComparisonContext contains all information that can be used for comparing similarity between objects that occur within ArDoCo.
 */
public record ComparisonContext(double similarityThreshold, @Nonnull String firstString, @Nonnull String secondString, @Nullable IWord firstWord,
                                @Nullable IWord secondWord, @Nullable IModelInstance firstModel, @Nullable IModelInstance secondModel, boolean lemmatize) {

    public ComparisonContext(double similarityThreshold, String firstString, String secondString, boolean lemmatize) {
        this(similarityThreshold, firstString, secondString, null, null, null, null, lemmatize);
    }

    public ComparisonContext(double similarityThreshold, IWord firstWord, IWord secondWord, boolean lemmatize) {
        this(similarityThreshold, firstWord.getText(), secondWord.getText(), firstWord, secondWord, null, null, lemmatize);
    }

    public ComparisonContext(double similarityThreshold, IModelInstance firstModel, IModelInstance secondModel, boolean lemmatize) {
        this(similarityThreshold, firstModel.getLongestName(), secondModel.getLongestName(), null, null, firstModel, secondModel, lemmatize);
    }

    /**
     * Finds the most appropriate string representation by the first object in this comparison object.
     * This method can be used as a shorthand to avoid going through all variables that could possibly represent the first object.
     *
     * @return the most appropriate string presentation of the first object in this comparison
     */
    @Nonnull public String firstTerm() {
        return findAppropriateTerm(firstString, firstWord, firstModel);
    }

    /**
     * Finds the most appropriate string representation by the second object in this comparison object.
     * This method can be used as a shorthand to avoid going through all variables that could possibly represent the second object.
     *
     * @return the most appropriate string presentation of the second object in this comparison
     */
    @Nonnull public String secondTerm() {
        return findAppropriateTerm(secondString, secondWord, secondModel);
    }

    private String findAppropriateTerm(@Nonnull String string, @Nullable IWord word, @Nullable IModelInstance model) {
        Objects.requireNonNull(string);

        if (word != null) {
            return lemmatize ? word.getLemma() : word.getText();
        } else if (model != null) {
            return model.getLongestName();
        } else {
            return string;
        }
    }

}
