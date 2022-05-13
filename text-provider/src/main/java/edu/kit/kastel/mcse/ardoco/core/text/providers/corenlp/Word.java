/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.ISentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;

class Word implements IWord {

    private final CoreLabel token;
    private final CoreDocument coreDocument;
    private final int index;
    private ISentence sentence = null;
    private IWord preWord = null;
    private IWord nextWord = null;

    Word(CoreLabel token, int index, CoreDocument coreDocument) {
        this.token = token;
        this.index = index;
        this.coreDocument = coreDocument;
    }

    @Override
    public int getSentenceNo() {
        return token.sentIndex();
    }

    @Override
    public ISentence getSentence() {
        if (this.sentence == null) {
            int sentenceNo = getSentenceNo();
            var coreSentence = coreDocument.sentences().get(sentenceNo);
            sentence = new Sentence(coreSentence, sentenceNo);
        }
        return sentence;
    }

    @Override
    public String getText() {
        return token.get(CoreAnnotations.TextAnnotation.class);
    }

    @Override
    public POSTag getPosTag() {
        String posString = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        return POSTag.get(posString);
    }

    @Override
    public IWord getPreWord() {
        int preWordIndex = index - 1;
        if (preWord == null && preWordIndex > 0) {
            var token = coreDocument.tokens().get(preWordIndex);
            preWord = new Word(token, preWordIndex, coreDocument);
        }
        return preWord;
    }

    @Override
    public IWord getNextWord() {
        int nextWordIndex = index + 1;
        var tokens = coreDocument.tokens();
        if (nextWord == null && nextWordIndex < tokens.size()) {
            // TODO
            var token = tokens.get(nextWordIndex);
            nextWord = new Word(token, nextWordIndex, coreDocument);
        }
        return nextWord;
    }

    @Override
    public int getPosition() {
        return index;
    }

    @Override
    public String getLemma() {
        return token.get(CoreAnnotations.LemmaAnnotation.class);
    }

    @Override
    public ImmutableList<IWord> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
        MutableList<IWord> dependencyWords = Lists.mutable.empty();
        List<TypedDependency> dependencies = getDependenciesOfType(dependencyTag);
        for (var typedDependency : dependencies) {
            var target = typedDependency.dep().backingLabel();
            var source = typedDependency.gov().backingLabel();
            if (source.beginPosition() == token.beginPosition()) {
                var targetWord = getCorrespondingWordForFirstTokenBasedOnSecondToken(target, source);
                dependencyWords.add(targetWord);
            }
        }
        return dependencyWords.toImmutable();
    }

    @Override
    public ImmutableList<IWord> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
        MutableList<IWord> dependencyWords = Lists.mutable.empty();
        List<TypedDependency> dependencies = getDependenciesOfType(dependencyTag);
        for (var typedDependency : dependencies) {
            var target = typedDependency.dep().backingLabel();
            var source = typedDependency.gov().backingLabel();
            if (target.beginPosition() == token.beginPosition()) {
                var word = getCorrespondingWordForFirstTokenBasedOnSecondToken(source, target);
                dependencyWords.add(word);
            }
        }
        return dependencyWords.toImmutable();
    }

    private IWord getCorrespondingWordForFirstTokenBasedOnSecondToken(CoreLabel firstToken, CoreLabel secondToken) {
        var firstTokenIndex = (firstToken.index() - secondToken.index()) + index;
        return new Word(firstToken, firstTokenIndex, coreDocument);
    }

    private List<TypedDependency> getDependenciesOfType(DependencyTag dependencyTag) {
        List<TypedDependency> typedDependencies = Lists.mutable.empty();
        var coreSentence = coreDocument.sentences().get(getSentenceNo());
        SemanticGraph dependencies = coreSentence.dependencyParse();
        for (var typedDependency : dependencies.typedDependencies()) {
            GrammaticalRelation rel = typedDependency.reln();
            if (dependencyTag.name().equalsIgnoreCase(rel.getShortName())) {
                typedDependencies.add(typedDependency);
            }
        }
        return typedDependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Word word = (Word) o;

        return word.getText().equals(this.getText()) && getPosition() == word.getPosition() && getPosTag() == word.getPosTag();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getPosTag(), getText());
    }
}
