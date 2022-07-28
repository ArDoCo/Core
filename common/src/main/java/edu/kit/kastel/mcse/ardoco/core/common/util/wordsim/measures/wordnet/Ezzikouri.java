/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet;

import java.util.*;
import java.util.stream.Collectors;

import opennlp.tools.stemmer.PorterStemmer;

import org.deeplearning4j.text.stopwords.StopWords;

import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.data.Concept;
import edu.uniba.di.lacam.kdde.lexical_db.item.POS;
import edu.uniba.di.lacam.kdde.ws4j.Relatedness;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;

/**
 * A WordNet based relatedness calculator that calculates similarity based on Ezzikouri et al. 2019
 */
public class Ezzikouri extends RelatednessCalculator {

    private static final double MIN = 0.0;
    private static final double MAX = 1.0;
    private static final List<POS[]> POS_PAIRS = List.of(new POS[] { POS.NOUN, POS.NOUN }, new POS[] { POS.VERB, POS.VERB });
    private static final PorterStemmer STEMMER = new PorterStemmer();

    public Ezzikouri(ILexicalDatabase db) {
        super(db, MIN, MAX);
    }

    @Override
    protected Relatedness calcRelatedness(Concept first, Concept second) {
        if (first.getSynsetID().equals(second.getSynsetID())) {
            return new Relatedness(1.0);
        }

        Set<String> firstGloss = stem(removeStopWords(getGlossWords(first)));
        Set<String> secondGloss = stem(removeStopWords(getGlossWords(second)));
        Set<String> firstWords = stem(removeStopWords(db.getWords(first)));
        Set<String> secondWords = stem(removeStopWords(db.getWords(second)));

        // double wordsScore = intersection(firstWords, secondWords) / union(firstWords, secondWords);
        // double glossScore = intersection(firstGloss, secondGloss) / union(firstGloss, secondGloss);
        // double score = (wordsScore + glossScore) / 2.0;

        double score = (intersection(firstWords, secondWords) + intersection(firstGloss, secondGloss))
                / (union(firstWords, firstGloss, secondWords, secondGloss));

        return new Relatedness(score);
    }

    @Override
    public List<POS[]> getPOSPairs() {
        return POS_PAIRS;
    }

    private double union(Collection<String> first, Collection<String> second, Collection<String> third, Collection<String> fourth) {
        var strings = new HashSet<String>();
        strings.addAll(first);
        strings.addAll(second);
        strings.addAll(third);
        strings.addAll(fourth);
        return strings.size();
    }

    private double intersection(Set<String> first, Set<String> second) {
        // Assumption: first and second do not contain duplicates themselves (Set<> prevents that)
        int count = 0;

        for (String element : first) {
            if (second.contains(element)) {
                count++;
            }
        }

        return count;
    }

    private Set<String> stem(Set<String> strings) {
        return strings.stream().map(STEMMER::stem).collect(Collectors.toSet());
    }

    private Set<String> removeStopWords(List<String> strings) {
        var stopWords = StopWords.getStopWords();

        return strings.stream().filter(str -> !stopWords.contains(str)).collect(Collectors.toSet());
    }

    private List<String> getGlossWords(Concept concept) {
        return Arrays.stream(db.getGloss(concept, null).get(0).split(" ")).toList();
    }

}
