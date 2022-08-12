/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;

class WordImpl implements Word {

    private final CoreLabel token;
    private final CoreDocument coreDocument;
    private final int index;
    private Sentence sentence = null;
    private Word preWord = null;
    private Word nextWord = null;

    WordImpl(CoreLabel token, int index, CoreDocument coreDocument) {
        this.token = token;
        this.index = index;
        this.coreDocument = coreDocument;
    }

    @Override
    public int getSentenceNo() {
        return token.sentIndex();
    }

    @Override
    public Sentence getSentence() {
        if (this.sentence == null) {
            int sentenceNo = getSentenceNo();
            var coreSentence = coreDocument.sentences().get(sentenceNo);
            sentence = new SentenceImpl(coreSentence, sentenceNo);
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
    public Word getPreWord() {
        int preWordIndex = index - 1;
        if (preWord == null && preWordIndex > 0) {
            var coreDocumentToken = coreDocument.tokens().get(preWordIndex);
            preWord = new WordImpl(coreDocumentToken, preWordIndex, coreDocument);
        }
        return preWord;
    }

    @Override
    public Word getNextWord() {
        int nextWordIndex = index + 1;
        var tokens = coreDocument.tokens();
        if (nextWord == null && nextWordIndex < tokens.size()) {
            var coreDocumentToken = tokens.get(nextWordIndex);
            nextWord = new WordImpl(coreDocumentToken, nextWordIndex, coreDocument);
        }
        return nextWord;
    }

    @Override
    public int getPosition() {
        return index;
    }

    protected int getPositionInSentence() {
        return this.token.index();
    }

    protected int getBeginCharPosition() {
        return this.token.beginPosition();
    }

    @Override
    public String getLemma() {
        return token.get(CoreAnnotations.LemmaAnnotation.class);
    }

    @Override
    public ImmutableList<Word> getOutgoingDependencyWordsWithType(DependencyTag dependencyTag) {
        MutableList<Word> dependencyWords = Lists.mutable.empty();
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
    public ImmutableList<Word> getIncomingDependencyWordsWithType(DependencyTag dependencyTag) {
        MutableList<Word> dependencyWords = Lists.mutable.empty();
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

    private Word getCorrespondingWordForFirstTokenBasedOnSecondToken(CoreLabel firstToken, CoreLabel secondToken) {
        var firstTokenIndex = (firstToken.index() - secondToken.index()) + index;
        return new WordImpl(firstToken, firstTokenIndex, coreDocument);
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
        if (!(o instanceof WordImpl word))
            return false;
        return word.getText().equals(this.getText()) && getPosition() == word.getPosition() && getPosTag() == word.getPosTag();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getPosTag(), getText());
    }
}
