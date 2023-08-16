package edu.kit.kastel.mcse.ardoco.core.textextraction.informants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ContextPhrase;

public class AbbreviationInformant extends Informant {
    private final static Logger logger = LoggerFactory.getLogger(AbbreviationInformant.class);
    @Configurable
    private int meaningPartsLimit = 6;
    @Configurable
    private int abbreviationPartsLimit = 6;
    @Configurable
    private int characterLimit = 10;
    @Configurable
    private double multiplicativeOrderThreshold = 0.9;
    @Configurable
    private int additiveOrderThreshold = 1;

    public AbbreviationInformant(DataRepository dataRepository) {
        super(AbbreviationInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        ImmutableList<Word> words = DataRepositoryHelper.getAnnotatedText(getDataRepository()).getWords();
        ImmutableList<Phrase> phrases = DataRepositoryHelper.getAnnotatedText(dataRepository).getPhrases();
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : words) {
            findBrackets(textState, phrases, word);
        }
    }

    void findBrackets(TextState textState, ImmutableList<Phrase> phrases, Word word) {
        var text = word.getText();
        if (text.equals("(")) {
            var optWord = findSingularWordInBracket(word);
            if (optWord.isEmpty())
                return;
            var candidate = optWord.orElseThrow();
            if (candidate.getText().length() > characterLimit)
                return;
            var preceding = findWordsPrecedingBracket(word);
            if (preceding.isEmpty())
                return;
            findMeaningBeforeBracket(textState, phrases, preceding, candidate);
        }
    }

    void findMeaningBeforeBracket(TextState textState, ImmutableList<Phrase> phrases, List<Word> preBracketWords, Word abbreviationCandidate) {
        for (int i = 1; i <= preBracketWords.size(); i++) {
            var subList = new ArrayList<>(preBracketWords).subList(0, i);
            Collections.reverse(subList);
            var meaningCandidates = subList.stream().map(Word::getText).collect(Collectors.joining(" "));

            var lMeaning = meaningCandidates.toLowerCase(Locale.US);
            var lAbbreviation = abbreviationCandidate.getText().toLowerCase(Locale.US);
            var shareInitial = AbbreviationDisambiguationHelper.shareInitial(lMeaning, lAbbreviation);

            if (!shareInitial)
                continue;

            var inOrder = AbbreviationDisambiguationHelper.containsInOrder(lMeaning, lAbbreviation);
            var dynamicThreshold = Math.max(1,
                    Math.min(lAbbreviation.length() - additiveOrderThreshold, Math.round(lAbbreviation.length() * multiplicativeOrderThreshold)));

            if (inOrder < dynamicThreshold)
                continue;

            if (i == 1) {
                textState.addWordAbbreviation(abbreviationCandidate.getText(), abbreviationCandidate);
            }

            Phrase shortestPhrase;
            var optShortestPhrase = phrases.stream().filter(p -> p.getText().equals(meaningCandidates)).min(Comparator.comparingInt(a -> a.getText().length()));
            if (optShortestPhrase.isEmpty()) {
                var sentence = subList.get(0).getSentence();
                shortestPhrase = new ContextPhrase(Lists.immutable.ofAll(subList), sentence);
                sentence.addPhrase(shortestPhrase);
                logger.info("Added phrase {}", shortestPhrase);
            } else {
                shortestPhrase = optShortestPhrase.orElseThrow();
            }
            textState.addPhraseAbbreviation(abbreviationCandidate.getText(), shortestPhrase);
        }
    }

    Optional<Word> findSingularWordInBracket(Word leadingOpeningBracket) {
        Word content = leadingOpeningBracket.getNextWord();
        if (content != null && content.getNextWord() != null && content.getNextWord().getText().equals(")"))
            return Optional.of(content);
        return Optional.empty();
    }

    List<Word> findWordsInBracket(Word leadingOpeningBracket) {
        var list = new ArrayList<Word>();
        var nextWord = leadingOpeningBracket.getNextWord();
        while (nextWord != null && !nextWord.getText().equals(")") && list.size() < abbreviationPartsLimit) {
            list.add(nextWord);
            nextWord = nextWord.getNextWord();
        }
        return list;
    }

    List<Word> findWordsPrecedingBracket(Word leadingOpeningBracket) {
        var list = new ArrayList<Word>();
        var prevWord = leadingOpeningBracket.getPreWord();
        while (prevWord != null && !prevWord.getText().equals(")") && list.size() < meaningPartsLimit) {
            list.add(prevWord);
            prevWord = prevWord.getPreWord();
        }
        return list;
    }
}
