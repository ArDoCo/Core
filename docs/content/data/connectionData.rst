Connection Data
=================

.. warning:: This site is deprecated


Connection State
---------------------
The ConnectionState consists of `Instance Links <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/connectionGenerator/state/InstanceLink.java>`_ and `Relation Links <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/connectionGenerator/state/RelationLink.java>`_.

`Instance Links <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/connectionGenerator/state/InstanceLink.java>`_ consist of a textualInstance represented by a recommended instance, and a model instance represented by an instance from the model.
The probability of it measures the similarity of both instances.
Two instances are similar if the names as well the types are similar.
If the approach had found a recommended instance _car_ of type _class_ and an instance with _car_ as long name and _class_ as long type would exist, they should be connected within an instance link.

The `Relation Links <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/connectionGenerator/state/RelationLink.java>`_ consist of a textual relation represented by a recommended relation and a model relation represented by a Relation of the model.
The probability of it measures the similarity of both relations.


Linked Instance
--------------------

Linked Relation
---------------------

