# Word Similarity Comparisons in ArDoCo: Usage

To compare similarity between words in ArDoCo, simply call the static `NewSimilarityUtils` class.
Words can be represented as strings or as `IWord` instances which may allow some similarity measures to utilize the
additional contextual information.

To configure the similarity computation, individual word similarity measures can be enabled/disabled and configured
using either the `CommonTextToolsConfig.properties` (which is only loaded on startup) or by dynamically setting
the measures during runtime, using the `setMeasures()` method in `NewSimilarityUtils`.

## Examples

```java
var first = "database";
var second = "datastore";

boolean similar = NewSimilarityUtils.areWordsSimilar(first, second);
```

Using a specific strategy:

```java
var similar = NewSimilarityUtils.areWordsSimilar(first, second, ComparisonStrategy.MAJORITY);
```

Using a custom context:

```java
IWord first = ...;
IWord second = ...;

var similar = NewSimilarityUtils.areWordsSimilar(new ComparisonContext(
    first,
    second,
    true // lemmatize?
));
```
