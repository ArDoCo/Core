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
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;

@MetaInfServices(TextAgent.class)
public class PhraseAgent extends TextAgent {

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent() {
        super(GenericTextConfig.class);
    }

    private PhraseAgent(IText text, ITextState textState, GenericTextConfig config) {
        super(GenericTextConfig.class, text, textState);
    }

    @Override
    public TextAgent create(IText text, ITextState textState, Configuration config) {
        return new PhraseAgent(text, textState, (GenericTextConfig) config);
    }

    @Override
    public void exec() {
        // TODO Auto-generated method stub
        logger.info("Executing PhraseAgent");

        for (var word : text.getWords()) {
            var phrase = getCompoundPhrases(word);
            if (phrase.size() > 1) {
                var reference = createReferenceForPhrase(phrase);
                var occurences = phrase.collect(IWord::getText);
                var phraseNounMapping = new NounMapping(phrase, MappingKind.NAME, 0.6, reference, occurences);
                textState.addNounMapping(phraseNounMapping);
                // TODO check
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
