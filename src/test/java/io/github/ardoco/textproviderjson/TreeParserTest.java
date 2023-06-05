/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.textobject.PhraseImpl;
import io.github.ardoco.textproviderjson.textobject.WordImpl;

class TreeParserTest {

    String tree = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (PRP me))) (. .)))";

    List<Word> words = new ArrayList<>(List.of(new WordImpl(null, 0, 0, "This", POSTag.DETERMINER, null, null, null), new WordImpl(null, 1, 0, "is",
            POSTag.VERB_SINGULAR_PRESENT_THIRD_PERSON, null, null, null), new WordImpl(null, 2, 0, "me", POSTag.PRONOUN_PERSONAL, null, null, null),
            new WordImpl(null, 3, 0, ".", POSTag.CLOSER, null, null, null)));
    Phrase subsubphrase = new PhraseImpl(Lists.immutable.of(words.get(2)), PhraseType.NP, new ArrayList<>());
    List<Phrase> subphrases = List.of(new PhraseImpl(Lists.immutable.of(words.get(0)), PhraseType.NP, new ArrayList<>()), new PhraseImpl(Lists.immutable.of(
            words.get(1)), PhraseType.VP, List.of(subsubphrase)));
    Phrase phrase = new PhraseImpl(Lists.immutable.of(words.get(3)), PhraseType.S, subphrases);
    Phrase expectedPhrase = new PhraseImpl(Lists.immutable.empty(), PhraseType.ROOT, List.of(phrase));

    @Test
    void parseConstituencyTreeTest() {
        DtoToObjectConverter converter = new DtoToObjectConverter();
        Phrase parsedPhrase = converter.parseConstituencyTree(tree, words);
        Assertions.assertEquals(expectedPhrase, parsedPhrase);
    }
}
