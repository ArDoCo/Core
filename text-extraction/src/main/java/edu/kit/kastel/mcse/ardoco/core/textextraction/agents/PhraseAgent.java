package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.List;
import java.util.StringJoiner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;

/**
 * Agent that is responsible for looking at phrases and extracting {@link INounMapping}s from compound nouns etc.
 *
 * @author Jan Keim
 *
 */
@MetaInfServices(TextAgent.class)
public class PhraseAgent extends TextAgent {

    private static final double PHRASE_CONFIDENCE = 0.6;

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent() {
        super(GenericTextConfig.class);
    }

    private PhraseAgent(IText text, ITextState textState) {
        super(GenericTextConfig.class, text, textState);
    }

    @Override
    public TextAgent create(IText text, ITextState textState, Configuration config) {
        return new PhraseAgent(text, textState);
    }

    @Override
    public void exec() {
        // TODO
        for (var word : text.getWords()) {
            var phrase = getCompoundPhrases(word);
            if (phrase.size() > 1) {
                var reference = createReferenceForPhrase(phrase);
                var occurences = phrase.collect(IWord::getText);
                var phraseNounMapping = new NounMapping(phrase, MappingKind.NAME, PHRASE_CONFIDENCE, reference, occurences);
                textState.addNounMapping(phraseNounMapping);
            }
        }
    }

    private static String createReferenceForPhrase(ImmutableList<IWord> phrase) {
        StringJoiner referenceJoiner = new StringJoiner(" ");
        for (var w : phrase) {
            referenceJoiner.add(w.getText());
        }
        return referenceJoiner.toString();
    }

    private ImmutableList<IWord> getCompoundPhrases(IWord word) {
        var deps = Lists.mutable.of(word);
        deps.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.COMPOUND).toList());
        var sortedWords = deps.toSortedListBy(IWord::getPosition);
        List<IWord> returnList = Lists.mutable.empty();
        for (var currWord : sortedWords) {
            if (!textState.isNodeContainedByTypeNodes(currWord)) {
                returnList.add(currWord);
            }

        }
        return Lists.immutable.ofAll(returnList);
    }

}
