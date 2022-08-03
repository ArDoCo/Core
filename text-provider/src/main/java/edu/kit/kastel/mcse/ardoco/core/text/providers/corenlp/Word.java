/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;

class Word implements IWord {

    private final CoreLabel token;
    private final Text parent;
    private final int index;
    private IWord preWord = null;
    private IWord nextWord = null;

    private final int sentenceNo;
    private IPhrase phrase;
    private final String text;
    private final POSTag posTag;

    Word(CoreLabel token, int index, Text parent) {
        this.token = token;
        this.index = index;
        this.parent = parent;

        this.sentenceNo = token.sentIndex();
        this.text = token.get(CoreAnnotations.TextAnnotation.class);
        this.posTag = POSTag.get(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

    }

    private IPhrase loadPhrase() {
        var phrase = parent.getSentences().get(sentenceNo).getPhrases().stream().filter(p -> p.getContainedWords().contains(this)).findFirst().orElseThrow();
        var subPhrases = List.of(phrase);
        while (!subPhrases.isEmpty()) {
            phrase = subPhrases.get(0);
            subPhrases = phrase.getSubPhrases().stream().filter(p -> p.getContainedWords().contains(this)).toList();
        }
        return phrase;
    }

    @Override
    public int getSentenceNo() {
        return sentenceNo;
    }

    @Override
    public IPhrase getPhrase() {
        if (this.phrase == null) {
            this.phrase = loadPhrase();
        }
        return this.phrase;
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
    public IWord getPreWord() {
        int preWordIndex = index - 1;
        if (preWord == null && preWordIndex > 0) {
            preWord = parent.getWords().get(preWordIndex);
        }
        return preWord;
    }

    @Override
    public IWord getNextWord() {
        int nextWordIndex = index + 1;
        if (nextWord == null && nextWordIndex < parent.getLength()) {
            nextWord = parent.getWords().get(nextWordIndex);
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
        return parent.getWords().get(firstTokenIndex);
    }

    private List<TypedDependency> getDependenciesOfType(DependencyTag dependencyTag) {
        List<TypedDependency> typedDependencies = Lists.mutable.empty();
        var sentence = (Sentence) parent.getSentences().get(getSentenceNo());
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
        if (o == null || !(o instanceof Word))
            return false;
        if (o instanceof Word word) {
            return word.getText().equals(this.getText()) && getPosition() == word.getPosition() && getPosTag() == word.getPosTag()
                    && getSentenceNo() == word.getSentenceNo();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPosition(), getPosTag(), getText(), getSentenceNo());
    }
}
