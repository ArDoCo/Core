Recommendation Data
====================

.. warning:: This site is deprecated

Recommendation State
-------------------------
The `Recommendation State <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/recommendationGenerator/state/RecommendationState.java>`_ contains all model elements that were found by the text extraction and are certain to occur in the goal model.
The goal model represents the current state of the system.
This approach tries to create this goal model through recommendations.
Criteria for recommending a textually identified model element could be its frequency in the text.
Currently, the recommendations are used for the overlap creation of model and text in the connection state.
Furthermore, certain recommendations that are not found in the model can be suggested to the user (without a model equivalent).
Thereby, failures in the model can be compensated.
The Recommendation State stores two kinds of model elements:
`Recommended Instances <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/recommendationGenerator/state/RecommendedInstance.java>`_ and
`Recommended Relations <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/recommendationGenerator/state/RecommendedRelation.java>`_.

`Recommended Instances <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/recommendationGenerator/state/RecommendedInstance.java>`_ are composed `Noun Mappings <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/textExtractor/state/NounMapping.java>`_ from the text extraction stage.
A representative name and type have to be set in additional attributes.
The probability contains the certainty, that the instance is in the goal model and thus should be suggested to the user.

`Recommended Relations <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/recommendationGenerator/state/RecommendedRelation.java>`_ are composed Recommended Instances.
A Recommended Relation has a type and separate PARSE nodes to refer to a textual source.
The probability is chosen analogously to the of Recommended Instances.


Recommended Instance
__________________________

Recommended Relation
________________________

