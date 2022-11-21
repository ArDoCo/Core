/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;

class WordImpl implements Word {

    private final CoreLabel token;
    private final TextImpl parent;
    private final int index;
    private Word preWord = null;
    private Word nextWord = null;

    private final int sentenceNo;
    private Phrase phrase;
    private final String text;
    private final POSTag posTag;

    WordImpl(CoreLabel token, int index, TextImpl parent) {
        this.token = token;
        this.index = index;
        this.parent = parent;

        this.sentenceNo = token.sentIndex();
        this.text = token.get(CoreAnnotations.TextAnnotation.class);
        this.posTag = POSTag.get(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
    }

    @Override
    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public Sentence getSentence() {
        return this.parent.getSentences().get(this.sentenceNo);
    }

    @Override
    public Phrase getPhrase() {
        if (this.phrase == null) {
            this.phrase = loadPhrase();
        }
        return this.phrase;
    }

    private Phrase loadPhrase() {
        var currentPhrase = getSentence().getPhrases().stream().filter(p -> p.getContainedWords().contains(this)).findFirst().orElseThrow();
        var subPhrases = List.of(currentPhrase);
        while (!subPhrases.isEmpty()) {
            currentPhrase = subPhrases.get(0);
            subPhrases = currentPhrase.getSubPhrases().stream().filter(p -> p.getContainedWords().contains(this)).toList();
        }
        return currentPhrase;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public POSTag getPosTag() {
        return this.posTag;
    }

    @Override
    public Word getPreWord() {
        int preWordIndex = index - 1;
        if (preWord == null && preWordIndex > 0) {
            preWord = parent.words().get(preWordIndex);
        }
        return preWord;
    }

    @Override
    public Word getNextWord() {
        int nextWordIndex = index + 1;
        if (nextWord == null && nextWordIndex < parent.getLength()) {
            nextWord = parent.words().get(nextWordIndex);
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
        return parent.words().get(firstTokenIndex);
    }

    private List<TypedDependency> getDependenciesOfType(DependencyTag dependencyTag) {
        List<TypedDependency> typedDependencies = Lists.mutable.empty();
        var sentence = (SentenceImpl) parent.getSentences().get(getSentenceNo());
        SemanticGraph dependencies = sentence.dependencyParse();
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

        return word.getText().equals(this.getText()) && getPosition() == word.getPosition() && getPosTag() == word.getPosTag() && getSentenceNo() == word
                .getSentenceNo();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getPosTag(), getText(), getSentenceNo());
    }
}
