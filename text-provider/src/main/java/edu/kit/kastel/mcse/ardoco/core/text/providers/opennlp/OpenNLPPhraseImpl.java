package edu.kit.kastel.mcse.ardoco.core.text.providers.opennlp;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import opennlp.tools.parser.Parse;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OpenNLPPhraseImpl implements Phrase {

    private ImmutableList<Word> words = Lists.immutable.empty();
    private ImmutableList<Phrase> subphrases = Lists.immutable.empty();

    private final OpenNLPSentenceImpl parent;
    private final Parse[] tree;
    private final Parse thisParse;

    private String text = null;

    public OpenNLPPhraseImpl(OpenNLPSentenceImpl parent, Parse[] tree, Parse thisParse) {
        this.parent = parent;
        this.tree = tree;
        this.thisParse = thisParse;
    }

    @Override
    public int getSentenceNo() {
        return this.words.get(0).getSentenceNo();
    }

    @Override
    public String getText() {
        if(this.text == null) {
            this.text = thisParse.toString();
        }
        return this.text;
    }

    @Override
    public ImmutableList<Word> getContainedWords() {
        if (this.words.isEmpty()) {
            MutableList<Word> newWords = Lists.mutable.empty();
            if (this.getSubPhrases().size() == 1 && this.getSubPhrases().get(0).getText().equals(this.getText())) {
                POSTag newPosTag = POSTag.get(thisParse.getType());
                newWords.add(new OpenNLPWordImpl(this.thisParse.getHeadIndex(), parent.getSentenceNumber(), this.getText(), parent.getParentText(),newPosTag));
            } else {
                for(Phrase subphrase: this.getSubPhrases()) {
                    newWords.addAll(subphrase.getContainedWords().castToList());
                }
            }
            this.words = newWords.toImmutable();
        }
        return this.words;
    }



    @Override
    public PhraseType getPhraseType() {;
        return PhraseType.get(this.thisParse.getType());
    }

    public Parse getThisParse() {
        return this.thisParse;
    }

    @Override
    public ImmutableList<Phrase> getSubPhrases() {
        if (this.subphrases.isEmpty()) {
            MutableList<Phrase> newSubPhrases = Lists.mutable.empty();
            MutableList<Parse> currentParses = Lists.mutable.empty();
            currentParses.addAll(Arrays.asList(this.thisParse.getChildren()));
            while (!currentParses.isEmpty()) {
                Parse currentParse = currentParses.remove(0);
                newSubPhrases.add(new OpenNLPPhraseImpl(this.parent, this.tree, currentParse));
                if (this.thisParse.getChildren().length == 1 && this.thisParse.getChildren()[0].toString().equals(this.getText())) {
                    currentParses.addAll(Arrays.asList(currentParse.getChildren()));
                }
            }
            this.subphrases = newSubPhrases.toImmutable();
        }
        return this.subphrases;
    }

    @Override
    public boolean isSuperPhraseOf(Phrase other) {
        return this.getSubPhrases().contains(other);
    }

    @Override
    public boolean isSubPhraseOf(Phrase other) {
        return other.getSubPhrases().contains(this);
    }

    @Override
    public ImmutableMap<Word, Integer> getPhraseVector() {
        return null;
    }
}
