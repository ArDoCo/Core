# Word Similarity Comparisons in ArDoCo: Architecture

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

┌────────────────────────────────────────────────────────────────────────────────────┐
│ <<interface>> ComparisonStrategy                                                   │
├────────────────────────────────────────────────────────────────────────────────────┤
│ + areWordsSimilar(ctx: ComparisonContext, measures: List<WordSimMeasure>): boolean │
└────────────────────────────────────────────────────────────────────────────────────┘
                                         ╱╲ (implements) 
┌────────────────────────────────────────┴┴──────────────────────────────────────────┐
│ AtleastOneStrategy                                                                 │
├────────────────────────────────────────────────────────────────────────────────────┤
│ + areWordsSimilar(ctx: ComparisonContext, measures: List<WordSimMeasure>): boolean │
└────────────────────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│ <<static>> WordSimUtils                                                                         │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ - MEASURES: List<WordSimMeasure>                                                                │
│ - STRATEGY: ComparisonStrategy                                                                  │
├─────────────────────────────────────────────────────────────────────────────────────────────────┤
│ + setMeasures(measures: Collection<WordSimMeasure>)                                             │
│ + setStrategy(strategy: ComparisonStrategy)                                                     │
│ + areWordsSimilar(ctx: ComparisonContext, strategy: ComparisonStrategy): boolean                │
│ + areWordsSimilar(ctx: ComparisonContext): boolean                                              │
│ + areWordsSimilar(firstWord: String, secondWord: String): boolean                               │
│ + areWordsSimilar(firstWord: String, secondWord: String, strategy: ComparisonStrategy): boolean │
│ + areWordsSimilar(firstWord: IWord, secondWord: IWord): boolean                                 │
│ + areWordsSimilar(firstWord: IWord, secondWord: IWord, strategy: ComparisonStrategy): boolean   │
│ + areWordsSimilar(firstWord: String, secondWord: IWord): boolean                                │
│ + areWordsSimilar(firstWord: String, secondWord: IWord, strategy: ComparisonStrategy): boolean  │
└─────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## WordSimUtils

The static `WordSimUtils` class is the main access point for word similarity comparison.
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

## ComparisonStrategy

A `ComparisonStrategy` decides how the verdicts of multiple WSMs regarding a specific comparison are combined.
As of right now, two strategies are already implemented:
- The `AtleastOneStrategy` is the initial default strategy. With this strategy, a word pair is considered similar if at least
  one of the given measures consider the word pair as similar.
- The `MajorityStrategy` considers a word pair as similar enough, if the majority of all WSMs agree that the word pair is similar.

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
        - `nasari`

## WordSimLoader

On startup, `NewSimilarityUtils` will load measures by calling the `loadUsingProperties()` method from the
`WordSimLoader` class. Which measures are loaded depends on the `CommonTextToolsConfig.properties` file.

## Vector-based similarity measures

Some word similarity measures use vector representations of words which are then compared with cosine similarity.
Since many of these measures work almost the same, common behaviour is abstracted away.

```
┌────────────────────────────────────────────────────────────────────────────────────┐
│ <<abstract>> VectorBasedWordSimMeasure (implements WordSimMeasure)                 │
├────────────────────────────────────────────────────────────────────────────────────┤
│ - vectorDataSource: WordVectorDataSource                                           │
│ - vectorCache: Map<String, float[]>                                                │
├────────────────────────────────────────────────────────────────────────────────────┤
│ * VectorBasedWordSimMeasure(vectorDataSource: WordVectorDataSource)                │
│ + compareVectors(firstWord: String, secondWord: String)                            │
└────────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────────────────┐
│ <<interface>> WordVectorDataSource                                                 │
├────────────────────────────────────────────────────────────────────────────────────┤
│ + getWordVector(word: String): Optional<float[]>                                   │
└────────────────────────────────────────────────────────────────────────────────────┘
                                         ╱╲ (implements) 
┌────────────────────────────────────────┴┴──────────────────────────────────────────┐
│ VectorSqliteDatabase                                                               │
├────────────────────────────────────────────────────────────────────────────────────┤
│ + VectorSqliteDatabase(sqliteFile: Path)                                           │
│ + getWordVector(word: String): Optional<float[]>                                   │
│ + close()                                                                          │
└────────────────────────────────────────────────────────────────────────────────────┘
┌────────────────────────────────────────────────────────────────────────────────────┐
│ <<static>> VectorUtils                                                             │
├────────────────────────────────────────────────────────────────────────────────────┤
│ + cosineSimilarity(firstVec: float[], secondVec: float[]): double                  │
│ + isZero(vector: float[]): boolean                                                 │
│ + add(result: double[], toAdd: double[])                                           │
│ + scale(vector: double[], scalar: double)                                          │
└────────────────────────────────────────────────────────────────────────────────────┘
```

The `VectorBasedWordSimMeasure` acts as a base class for vector-based measures.
It requires a `WordVectorDataSource` to fetch vector representations of the words it wants to compare.
One implementation of such a data source is `VectorSqliteDatabase` that fetches vector representations from an sqlite file.
To create such a sqlite database from an existing word2vec-like embedding text file, the `WordVectorSqliteImporter` can be used.
It scans the embedding text file and inserts the vectors into the sqlite database.
The importer can be extended to allow filtering and pre-processing the words before they are inserted.
The `VectorUtils` class provides helper methods for calculating the cosine similarity and other basic vector arithmetic.
