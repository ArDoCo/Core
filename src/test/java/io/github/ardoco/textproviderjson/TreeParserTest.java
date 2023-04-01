/* Licensed under MIT 2023. */
package io.github.ardoco.textproviderjson;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.ardoco.textproviderjson.converter.DtoToObjectConverter;
import io.github.ardoco.textproviderjson.textobject.PhraseImpl;
import io.github.ardoco.textproviderjson.textobject.WordImpl;
import io.github.ardoco.textproviderjson.textobject.text.Phrase;
import io.github.ardoco.textproviderjson.textobject.text.Word;

class TreeParserTest {

    String tree = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (PRP me))) (. .)))";
    List<Word> words = new ArrayList<>(List.of(new WordImpl(null, 1, 0, "This", PosTag.DETERMINER, null, null, null), new WordImpl(null, 2, 0, "is",
            PosTag.VERB_SINGULAR_PRESENT_THIRD_PERSON, null, null, null), new WordImpl(null, 3, 0, "me", PosTag.PRONOUN_PERSONAL, null, null, null),
            new WordImpl(null, 4, 0, ".", PosTag.CLOSER, null, null, null)));
    Phrase subsubphrase = new PhraseImpl(Lists.immutable.of(words.get(2)), null, PhraseType.NP, new ArrayList<>());
    List<Phrase> subphrases = List.of(new PhraseImpl(Lists.immutable.of(words.get(0)), null, PhraseType.NP, new ArrayList<>()), new PhraseImpl(Lists.immutable
            .of(words.get(1)), null, PhraseType.VP, List.of(subsubphrase)));
    Phrase expectedPhrase = new PhraseImpl(Lists.immutable.of(words.get(3)), null, PhraseType.S, subphrases);

    @Test
    void parseConstituencyTreeTest() {
        DtoToObjectConverter converter = new DtoToObjectConverter();
        Phrase parsedPhrase = converter.parseConstituencyTree(tree, words, null);
        parsedPhrase.getContainedWords();
        parsedPhrase.getSubPhrases();
        Assertions.assertEquals(expectedPhrase, parsedPhrase);
    }
}
