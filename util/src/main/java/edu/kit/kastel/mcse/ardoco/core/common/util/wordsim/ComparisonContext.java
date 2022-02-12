package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public record ComparisonContext(double similarityThreshold,
                                String firstString, String secondString,
                                @Nullable IWord firstWord, @Nullable IWord secondWord,
                                @Nullable IModelInstance firstModel, @Nullable IModelInstance secondModel,
                                boolean lemmatize) {

    public ComparisonContext(double similarityThreshold, String firstString, String secondString, boolean lemmatize) {
        this(similarityThreshold, firstString, secondString, null, null, null, null, lemmatize);
    }

    public ComparisonContext(double similarityThreshold, IWord firstWord, IWord secondWord, boolean lemmatize) {
        this(similarityThreshold, firstWord.getText(), secondWord.getText(), firstWord, secondWord, null, null, lemmatize);
    }

    public ComparisonContext(double similarityThreshold, IModelInstance firstModel, IModelInstance secondModel, boolean lemmatize) {
        this(similarityThreshold, firstModel.getLongestName(), secondModel.getLongestName(), null, null, firstModel, secondModel, lemmatize);
    }

    public String firstTerm() {
        return findAppropriateTerm(firstString, firstWord, firstModel);
    }

    public String secondTerm() {
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
