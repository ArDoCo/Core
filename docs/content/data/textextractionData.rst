
Extracted Text Data
====================

.. warning:: This site is deprecated

Text Extraction State
------------------------
The `Text Extraction State <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/TextExtractionState.java>`_ stores all possibly relevant textual information, that could indicate model elements.
Currently, this information is stored in `Noun Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/NounMapping.java>`_, `Term Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/TermMapping.java>`_, and `Relation Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/RelationMapping.java>`_.

`Noun Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/NounMapping.java>`_ are groups of found nouns, that possibly refer to the same element.
Since the text extraction is independent from the model Noun Mappings can be seen as a clustering of similar terms.
The references to the textual source are stored in the PARSE nodes.
The reference contains a representable name for this cluster.
In contrast to the reference, the occurrences collect all different appearances of the terms.

`Term Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/TermMapping.java>`_ are composed terms (e.g. train wagon).
Thereby, they refer to multiple Noun Mappings.
Both mappings (Noun Mappings and Term Mappings) have a `Mapping Kind <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/MappingKind.java>`_.
The Mapping Kind is set to the kind of the mapping.
If it is more likely that the mapping is a name of a later element (e.g. train wagon) it is set to ``NAME``.
However, if it is more likely that the mapping is a type of an element (e.g. class or component) it is set to ``TYPE``.
If the analyzers or solvers can not decide whether the mapping is a name or a type the kind is set to ``NAME_OR_TYPE``.
Nevertheless, the certainty of a mapping to be of its kind is stored in its probability.

`Relation Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/RelationMapping.java>`_ store imaginable textual relations between Noun Mappings.
Up to this point, the approach considers only non directional relations.
The probability of a RelationMapping is the probability that the NounMappings have a connection in this constellation.


Entity Mapping
_____________________

Relation Mapping
__________________________________

