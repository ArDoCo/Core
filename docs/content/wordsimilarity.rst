
Word similarity metrics
=======================

In ArDoCo, we use Word Similarity Metrics to assess how similar words (or strings) are.
To use these metrics, simply call the appropriate methods in the `SimilarityUtils class <https://github.com/ArDoCo/Core/blob/main/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/util/SimilarityUtils.java>`_.
The package `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim <https://github.com/ArDoCo/Core/blob/main/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/util/wordsim>`_ provides different means to tailor the comparisons, most notably via the `WordSimUtils class <https://github.com/ArDoCo/Core/blob/main/common/src/main/java/edu/kit/kastel/mcse/ardoco/core/common/util/wordsim/WordSimUtils.java>`_.
Example usages can be found below.

Usage
------

To compare similarity between words in ArDoCo, simply call the static `WordSimUtils` class.
Words can be represented as strings or as `IWord` instances which may allow some similarity measures to utilize additional contextual information.

To configure the similarity computation, individual word similarity measures can be enabled/disabled and configured
using either the `CommonTextToolsConfig.properties` (which is only loaded on startup) or by dynamically setting
the measures during runtime, using the `setMeasures()` method in `WordSimUtils`.

Examples
^^^^^^^^^

.. code-block:: java
    :caption: Basic usage

    var first = "database";
    var second = "datastore";
    boolean similar = WordSimUtils.areWordsSimilar(first, second);

.. code-block:: java
    :caption: Using a specific strategy

    var first = "database";
    var second = "datastore";
    var similar = WordSimUtils.areWordsSimilar(first, second, ComparisonStrategy.MAJORITY);


.. code-block:: java
    :caption: Using a custom context

    IWord first = ...;
    IWord second = ...;
    var similar = WordSimUtils.areWordsSimilar(new ComparisonContext(
        first,
        second,
        true // lemmatize?
    ));


Architecture
-------------

ArDoCo provides a main access point for word similarity comparison in the `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim` package.

The main classes of this package are the following:

.. uml:: wordsim/WordSim.plantuml


WordSimUtils
^^^^^^^^^^^^^^

The static `WordSimUtils` class is the main access point for word similarity comparison.
It provides multiple static methods that allow easy comparison between strings or `IWord` instances.
The class allows customizing how it performs its comparisons, by either passing a custom comparison strategy to
one of its methods, or by setting the strategy and used measures with the `setMeasures()` and `setStrategy()` methods.
Changes through `setMeasures()` and `setStrategy()` will persist, meaning that any subsequent calls for word comparisons
will use the default measures and strategy that was previously set.


ComparisonContext
^^^^^^^^^^^^^^^^^^

A `ComparisonContext` is meant to contain all necessary information to recognize whether two words are similar.
Since not all comparisons involve `IWord` instances, the fields `firstWord: IWord` and `secondWord: IWord` are nullable.
However, these fields can provide additional information (like the corresponding sentence of a word) which similarity
measures can utilize. The `lemmatize` field decides whether the word similarity measures should use the lemmatized
version of the words.

ComparisonStrategy
^^^^^^^^^^^^^^^^^^^^

A `ComparisonStrategy` decides how the verdicts of multiple WSMs regarding a specific comparison are combined.
As of right now, two strategies are already implemented:

- The `AtleastOneStrategy` is the initial default strategy. With this strategy, a word pair is considered similar if at least one of the given measures consider the word pair as similar.
- The `MajorityStrategy` considers a word pair as similar enough, if the majority of all WSMs agree that the word pair is similar.

Measures
^^^^^^^^^

Each word similarity measure implements the `WordSimMeasure` interface and is located under its own package
inside the `measures` subpackage.

- `edu.kit.kastel.mcse.ardoco.core.common.util.wordsim`
    - ...
    - `measures`
        - `equality`
        - `fastText`
        - `glove`
        - `jarowinkler`
        - `levenshtein`
        - `ngram`
        - `sewordsim`

WordSimLoader
^^^^^^^^^^^^^^^

On startup, `WordSimUtils` will load measures by calling the `loadUsingProperties()` method from the `WordSimLoader` class.
The measures that are loaded can be configured in the `CommonTextToolsConfig.properties` file.

Vector-based similarity measures
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Some word similarity measures use vector representations of words which are then compared with cosine similarity.
Since many of these measures work almost the same, common behaviour is abstracted away.

.. uml:: wordsim/VectorSimMeasures.plantuml

The `VectorBasedWordSimMeasure` acts as a base class for vector-based measures.
It requires a `WordVectorDataSource` to fetch vector representations of the words it wants to compare.
One implementation of such a data source is `VectorSqliteDatabase` that fetches vector representations from an sqlite file.
To create such a sqlite database from an existing word2vec-like embedding text file, the `WordVectorSqliteImporter` can be used.
It scans the embedding text file and inserts the vectors into the sqlite database.
The importer can be extended to allow filtering and pre-processing the words before they are inserted.
The `VectorUtils` class provides helper methods for calculating the cosine similarity and other basic vector arithmetic.
