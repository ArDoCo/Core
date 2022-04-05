# Architecture

The `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim` package consists of a
`measures` subpackage and the following classes:

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

#### Measures

Each word similarity measure implements the `WordSimMeasure` interface and is located under its own package
inside the `measures` subpackage.

- `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim`
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
    - ...

#### ComparisonContext

A `ComparisonContext` is meant to contain all neccessary information to recognize whether two words are similar.
Not all comparisons are made between two `IWord` instances. 
However, since an `IWord` instance can provide additional information (like context or the  corresponding sentence), 
the `firstWord` and `secondWord` fields are accessible although nullable.
The `lemmatize` field decides whether the word similarity measures should use the lemmatized version of the words.

#### NewSimilarityUtils

The static `NewSimilarityUtils` class is the main access point for word similarity comparison. 
This class will later be merged with the old `SimilarityUtils` class.
For each call to a `areWordsSimilar(...)` method, the method iterates through its stored measures and returns `true`, 
when at least one measure considers the words from the `ComparisonContext` as similar enough.
Calls to the `setMeasures(...)` method changes which measures are used for similarity comparison.

#### WordSimLoader

On startup, `NewSimilarityUtils` will load measures by calling the `loadUsingProperties()` method from the 
`WordSimLoader` class. Which measures are loaded depends on the `CommonTextToolsConfig.properties` file.

#### VectorUtils

Some word similarity measures use vector representations of words and compares them using cosine similarity.
The `VectorUtils` class next to the `wordsim` package provides helper methods for calculating the cosine similarity
or other basic vector arithmetic.
