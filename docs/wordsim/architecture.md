# Word Similarity Comparisons in ArDoCo: Architecture

TODO: Update once NewSimilarityUtils is finalized

ArDoCo provides a main access point for word similarity comparison under the
`edu.kit.kastel.mcse.ardoco.core.common.util.wordsim` package.

The main classes of this package are the following:

```
┌────────────────────────────────────────────────────┐   ┌────────────────────────┐
│ <<interface>> WordSimMeasure                       │   │ ComparisonContext      │
├────────────────────────────────────────────────────┤   ├────────────────────────┤ 
│ + areWordsSimilar(ctx: ComparisonContext): boolean │   │ + firstString: String  │ 
└────────────────────────────────────────────────────┘   │ + secondString: String │ 
                                                         │ + firstWord: IWord     │ 
                                                         │ + secondWord: IWord    │ 
┌───────────────────────────────────────────────┐        │ + lemmatize: boolean   │ 
│ <<static>> WordSimLoader                      │        ├────────────────────────┤ 
├───────────────────────────────────────────────┤        │ + firstTerm(): String  │ 
│ + loadUsingProperties(): List<WordSimMeasure> │        │ + secondTerm(): String │ 
└───────────────────────────────────────────────┘        └────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐ 
│ <<static>> NewSimilarityUtils                                     │ 
├───────────────────────────────────────────────────────────────────┤ 
│ - MEASURES: List<WordSimMeasure>                                  │ 
├───────────────────────────────────────────────────────────────────┤ 
│ + setMeasures(measures: Collection<WordSimMeasure>)               │ 
│ + areWordsSimilar(ctx: ComparisonContext): boolean                │ 
│ + areWordsSimilar(firstWord: String, secondWord: String): boolean │ 
│ + areWordsSimilar(firstWord: IWord, secondWord: IWord): boolean   │ 
│ + areWordsSimilar(firstWord: String, secondWord: IWord): boolean  │ 
└───────────────────────────────────────────────────────────────────┘ 
```

## NewSimilarityUtils

The static `NewSimilarityUtils` class is the main access point for word similarity comparison.
It provides multiple static methods that allow easy comparison between strings or `IWord` instances.
The class allows customizing how it performs its comparisons, by either passing a custom comparison strategy to
one of its methods, or by setting the strategy and used measures with the `setMeasures()` and `setStrategy()` methods.
Changes through `setMeasures()` and `setStrategy()` will persist, meaning that any subsequent calls for word comparisons
will use the default measures and strategy that was previously set.

## ComparisonContext

A `ComparisonContext` is meant to contain all necessary information to recognize whether two words are similar.
Since not all comparisons involve `IWord` instances, the fields `firstWord: IWord` and `secondWord: IWord` are nullable.
However, these fields can provide additional information (like the corresponding sentence of a word) which similarity
measures can utilize. The `lemmatize` field decides whether the word similarity measures should use the lemmatized
version of the words.

## Measures

Each word similarity measure implements the `WordSimMeasure` interface and is located under its own package
inside the `measures` subpackage.

- `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim`
    - ...
    - `measures`
        - `equality`
            - `EqualityMeasure.java`
        - `fastText`
        - `glove`
        - `jarowinkler`
        - `levenshtein`
        - `ngram`
        - `sewordsim`
        - `wordnet`

## WordSimLoader

On startup, `NewSimilarityUtils` will load measures by calling the `loadUsingProperties()` method from the
`WordSimLoader` class. Which measures are loaded depends on the `CommonTextToolsConfig.properties` file.

## VectorUtils

Some word similarity measures use vector representations of words and compares them using cosine similarity.
The `VectorUtils` class next to the `wordsim` package provides helper methods for calculating the cosine similarity
and other basic vector arithmetic.
