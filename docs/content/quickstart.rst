Quickstart
======

Please use the provided `formatter <https://github.com/ArDoCo/Core/blob/main/formatter.xml>`_ when contributing.

Please acknowledge the `code of conduct <https://github.com/ArDoCo/Core/blob/main/CODE_OF_CONDUCT.md>`_.



Command Line Interface
----

The `core of ArDoCo <https://github.com/ArDoCo/Core>`_ contains a CLI that currently supports to find trace links between :abbr:`PCM (Palladio Component Model)`, an architectural model, and textual software architecture documentation.
The model can also contain a (java) code model that you can insert using the `CodeModelExtractors <https://github.com/ArDoCo/CodeModelExtractors>`_.

The `CLI <https://github.com/ArDoCo/Core/blob/main/pipeline/src/main/java/edu/kit/kastel/mcse/ardoco/core/pipeline/ArDoCoCLI.java>`_ is part of the `pipeline module <https://github.com/ArDoCo/Core/tree/main/pipeline>`_ of this project.

.. note:: In previous versions, we imported models as ontologies converted by `Ecore2OWL <https://github.com/kit-sdq/Ecore2OWL>`_.


Usage
^^^^^^

.. code-block::

        usage: java -jar ardoco-core-pipeline.jar

        -c,--conf <arg>                  path to the additional config file
        -h,--help                        show this message
        -ma,--model-architecture <arg>   path to the architecture model
        -mc,--model-code <arg>           path to the java code model
        -n,--name <arg>                  name of the run
        -o,--out <arg>                   path to the output directory
        -p,--provided <arg>              path to a JSON Text (already preprocessed)
        -t,--text <arg>                  path to the text file

