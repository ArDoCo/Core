/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.informants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Triple;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ContextPhrase;

public class AbbreviationInformant extends Informant {
    @Configurable
    private int adjacencyLimit = 4;
    @Configurable
    private int characterLimit = 10;
    @Configurable
    private double multiplicativeOrderThreshold = 0.8;
    @Configurable
    private double upperCaseThreshold = 0.6;
    @Configurable
    private double unrepresentedFraction = 0.2;
    @Configurable
    private boolean allowCrossSentenceSearch = false;
    @Configurable
    private boolean requireSpecialCharacter = true;
    @Configurable
    private double rewardInitialMatch = 0.5;
    @Configurable
    private double rewardCaseMatch = 0.5;
    @Configurable
    private double rewardAnyMatch = 0.5;
    private List<Character> brackets = List.of('(', ')');

    public AbbreviationInformant(DataRepository dataRepository) {
        super(AbbreviationInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        ImmutableList<Word> words = DataRepositoryHelper.getAnnotatedText(getDataRepository()).words();
        ImmutableList<Phrase> phrases = DataRepositoryHelper.getAnnotatedText(dataRepository).phrases();
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : words) {
            if (AbbreviationDisambiguationHelper.couldBeAbbreviation(word.getText(), upperCaseThreshold)) {
                findMeaning(textState, phrases, word);
            }
            findBrackets(textState, phrases, word);
        }
    }

    void findMeaning(TextState textState, ImmutableList<Phrase> phrases, Word word) {
        findBefore(textState, phrases, word);
        findAfter(textState, phrases, word);
    }

    void findBefore(TextState textState, ImmutableList<Phrase> phrases, Word word) {
        var initial = word.getText().toLowerCase(Locale.US).substring(0, 1);
        var optBracket = findClosest(Word::getPreWord, word, brackets, adjacencyLimit, List.of());
        if (optBracket.isEmpty())
            return;
        var bracket = optBracket.orElseThrow();
        var optPrev = findFurthestWithSharedInitial(Word::getPreWord, bracket, initial, adjacencyLimit - (word.getPosition() - bracket.getPosition()) + word
                .getText()
                .length(), brackets);
        if (optPrev.isPresent()) {
            var meaningList = between(optPrev.orElseThrow(), word);
            if (!requireSpecialCharacter || meaningList.stream().anyMatch(w -> !Character.isLetter(w.getText().toCharArray()[0]))) {
                extractShortestMeaning(textState, phrases, word, meaningList);
            }
        }
    }

    void findAfter(TextState textState, ImmutableList<Phrase> phrases, Word word) {
        var initial = word.getText().toLowerCase(Locale.US).substring(0, 1);
        var optBracket = findClosest(Word::getNextWord, word, brackets, adjacencyLimit, List.of());
        if (optBracket.isEmpty())
            return;
        var bracket = optBracket.orElseThrow();
        var optNext = findClosestWithSharedInitial(Word::getNextWord, bracket, initial, adjacencyLimit - (bracket.getPosition() - word.getPosition()),
                brackets);
        if (optNext.isPresent()) {
            var expectedMaxLength = Math.round(word.getText().length() / (1 - unrepresentedFraction));
            var meaningList = next(optNext.orElseThrow(), expectedMaxLength, true, brackets);
            var between = between(word, optNext.orElseThrow());
            if (!requireSpecialCharacter || between.stream().anyMatch(w -> !Character.isLetter(w.getText().toCharArray()[0]))) {
                extractShortestMeaning(textState, phrases, word, meaningList);
            }
        }
    }

    /**
     * {@return a list of words between a and b, the first is inclusive, the last is exclusive}
     *
     * @param a a word
     * @param b a word
     */
    List<Word> between(Word a, Word b) {
        if (a == b)
            return List.of();
        var first = a.getPosition() < b.getPosition() ? a : b;
        var second = a.getPosition() < b.getPosition() ? b : a;

        var list = new ArrayList<Word>();
        var next = first;
        while (next != second) {
            list.add(next);
            next = next.getNextWord();
        }
        return list;
    }

    /**
     * {@return a list containing the next words of maximum length amount}
     *
     * @param a             starting word (inclusive)
     * @param amount        the amount
     * @param skipNonLetter skip all words beginning with a non-letter character
     * @param stopAt        character to stop at
     */
    List<Word> next(Word a, long amount, boolean skipNonLetter, List<Character> stopAt) {
        var list = new ArrayList<Word>();
        var n = a;
        char c;
        for (int i = 0; i < amount; i++) {
            list.add(n);
            do {
                n = n.getNextWord();
                if (n == null)
                    return list;
                c = n.getText().toCharArray()[0];
                if (stopAt.contains(c)) {
                    return list;
                }
            } while (skipNonLetter && !Character.isLetter(c));
        }
        return list;
    }

    Optional<Word> findFurthestWithSharedInitial(UnaryOperator<Word> iterate, Word word, String match, int tries, List<Character> stopAt) {
        if (tries <= 0)
            return Optional.empty();
        var optNext = Optional.ofNullable(iterate.apply(word));
        if (optNext.isEmpty()) {
            return Optional.empty();
        }
        var next = optNext.orElseThrow();

        if (!allowCrossSentenceSearch && word.getSentenceNo() != next.getSentenceNo())
            return Optional.empty();

        var c = next.getText().toCharArray()[0];
        if (!Character.isLetter(c)) {
            if (stopAt.contains(c))
                return Optional.empty();
            else
                return findFurthestWithSharedInitial(iterate, next, match, tries, stopAt);
        }

        if (AbbreviationDisambiguationHelper.shareInitial(next.getText().toLowerCase(Locale.US), match.toLowerCase(Locale.US)))
            return Optional.of(findFurthestWithSharedInitial(iterate, next, match, tries - 1, stopAt).orElse(next));

        return findFurthestWithSharedInitial(iterate, next, match, tries - 1, stopAt);
    }

    Optional<Word> findClosestWithSharedInitial(UnaryOperator<Word> iterate, Word word, String match, int tries, List<Character> stopAt) {
        if (tries <= 0)
            return Optional.empty();
        var optNext = Optional.ofNullable(iterate.apply(word));
        if (optNext.isEmpty()) {
            return Optional.empty();
        }
        var next = optNext.orElseThrow();

        if (!allowCrossSentenceSearch && word.getSentenceNo() != next.getSentenceNo())
            return Optional.empty();

        var c = next.getText().toCharArray()[0];
        if (!Character.isLetter(c)) {
            if (stopAt.contains(c))
                return Optional.empty();
            else
                return findClosestWithSharedInitial(iterate, next, match, tries, stopAt);
        }

        if (AbbreviationDisambiguationHelper.shareInitial(next.getText().toLowerCase(Locale.ENGLISH), match.toLowerCase(Locale.ENGLISH)))
            return optNext;

        return findClosestWithSharedInitial(iterate, next, match, tries - 1, stopAt);
    }

    /**
     * Tries to find the closest word that begins with any character from a list of provided characters and stops if no tries are left or if a forbidden
     * character is reached.
     *
     * @param iterate a function which returns the "next" word relative to the origin
     * @param word    the word that is the point of origin (This word is not evaluated!)
     * @param search  the list of initial characters
     * @param tries   tries left
     * @param stopAt  list of forbidden characters, e.g. {"(", ")"} if we are not allowed to enter brackets
     * @return an optional closest word
     */
    Optional<Word> findClosest(UnaryOperator<Word> iterate, Word word, List<Character> search, int tries, List<Character> stopAt) {
        if (tries <= 0)
            return Optional.empty();
        var optNext = Optional.ofNullable(iterate.apply(word));
        if (optNext.isEmpty()) {
            return Optional.empty();
        }
        var next = optNext.orElseThrow();

        if (!allowCrossSentenceSearch && word.getSentenceNo() != next.getSentenceNo())
            return Optional.empty();

        var c = next.getText().toCharArray()[0];
        if (search.contains(c))
            return optNext;
        if (stopAt.contains(c))
            return Optional.empty();
        return findClosest(iterate, next, search, tries - 1, stopAt);
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
            var optPrev = findFurthestWithSharedInitial(Word::getPreWord, word, candidate.getText(), adjacencyLimit, brackets);
            if (optPrev.isEmpty())
                return;
            var preceding = between(optPrev.orElseThrow(), word);
            extractShortestMeaning(textState, phrases, candidate, preceding);
        }
    }

    /**
     * Calculates a score for each potential meaning to an abbreviation. Each sublist of the word list is treated as a potential meaning.
     * E.g. the word list {"Eurovision", "Song", "Contest"} is treated as "Eurovision", "Eurovision Song" and "Eurovision Song Contest".
     * A higher score is considered better.
     *
     * @param abbreviationCandidate the abbreviation candidate, e.g. "ESC"
     * @param wordList              a list of words, e.g. {"Eurovision", "Song", "Contest"}
     * @param caseSensitive         whether the calculation should be case-sensitive
     * @return a list containing a triple with the score, list of words and the potential meaning
     */
    List<Triple<Double, ArrayList<Word>, String>> getScores(Word abbreviationCandidate, List<Word> wordList, boolean caseSensitive) {
        ArrayList<Triple<Double, ArrayList<Word>, String>> meaningScores = new ArrayList<>();
        for (int i = 1; i <= wordList.size(); i++) {
            var subList = new ArrayList<>(new ArrayList<>(wordList).subList(0, i));
            var lMeaning = subList.stream().map(Word::getText).collect(Collectors.joining(" "));
            var lAbbreviation = abbreviationCandidate.getText();
            if (!caseSensitive) {
                lMeaning = lMeaning.toLowerCase(Locale.US);
                lAbbreviation = lAbbreviation.toLowerCase(Locale.US);
            }
            var shareInitial = AbbreviationDisambiguationHelper.shareInitial(lMeaning, lAbbreviation);

            if (shareInitial) {
                processCandidate(lMeaning, lAbbreviation, caseSensitive, meaningScores, subList);
            }
        }
        return meaningScores;
    }

    void processCandidate(String lMeaning, String lAbbreviation, boolean caseSensitive, ArrayList<Triple<Double, ArrayList<Word>, String>> meaningScores,
            ArrayList<Word> subList) {
        var inOrder = AbbreviationDisambiguationHelper.containsInOrder(lMeaning, lAbbreviation);
        var dynamicThreshold = Math.max(1, Math.ceil(lAbbreviation.length() * multiplicativeOrderThreshold));

        if (inOrder >= dynamicThreshold) {
            if (!caseSensitive) {
                var asInitial = AbbreviationDisambiguationHelper.maximumAbbreviationScore(lMeaning, lAbbreviation, 1, 0, 0, 0);
                if (asInitial < dynamicThreshold)
                    return;
            }

            var newScore = AbbreviationDisambiguationHelper.maximumAbbreviationScore(lMeaning, lAbbreviation, rewardInitialMatch, rewardAnyMatch,
                    rewardCaseMatch, 0);
            if (meaningScores.stream().noneMatch(t -> t.first() >= newScore)) {
                meaningScores.add(new Triple<>(newScore, subList, lMeaning));
            }
        }
    }

    void extractShortestMeaning(TextState textState, ImmutableList<Phrase> phrases, Word abbreviationCandidate, List<Word> meaningList) {
        var meaningScores = getScores(abbreviationCandidate, meaningList, true);
        if (meaningScores.isEmpty())
            meaningScores = getScores(abbreviationCandidate, meaningList, false);

        var optTriple = meaningScores.stream().max(Comparator.comparingDouble(Triple::first));
        if (optTriple.isPresent()) {
            var triple = optTriple.orElseThrow();
            var subList = triple.second();

            if (subList.size() == 1) {
                var meaning = subList.get(0);
                //Only add meanings which expand the abbreviation
                if (meaning.getText().length() > abbreviationCandidate.getText().length())
                    textState.addWordAbbreviation(abbreviationCandidate.getText(), meaning);
            }

            Phrase shortestPhrase;
            var optShortestPhrase = phrases.stream()
                    .filter(p -> p.getText().equalsIgnoreCase(triple.third()))
                    .min(Comparator.comparingInt(a -> a.getText().length()));
            if (optShortestPhrase.isEmpty()) {
                var sentence = subList.get(0).getSentence();
                shortestPhrase = new ContextPhrase(Lists.immutable.ofAll(subList), sentence);
                sentence.addPhrase(shortestPhrase);
                logger.debug("Added phrase {}", shortestPhrase);
            } else {
                shortestPhrase = optShortestPhrase.orElseThrow();
            }
            //Only add meanings which expand the abbreviation
            if (shortestPhrase.getText().length() > abbreviationCandidate.getText().length())
                textState.addPhraseAbbreviation(abbreviationCandidate.getText(), shortestPhrase);
        }
    }

    Optional<Word> findSingularWordInBracket(Word leadingOpeningBracket) {
        Word content = leadingOpeningBracket.getNextWord();
        if (content != null && content.getNextWord() != null && content.getNextWord().getText().equals(")"))
            return Optional.of(content);
        return Optional.empty();
    }
}
