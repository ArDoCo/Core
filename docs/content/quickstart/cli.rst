Command Line Interface
===========================

`ArDoCo Core <https://github.com/ArDoCo/Core>`_ contains a CLI that supports the execution of ArDoCo.

It is necessary to specify an input model as well as a textual documentation.
Usually, our model is an architectural model.
However, the model can also contain a (Java) code model that you can insert using the `CodeModelExtractors <https://github.com/ArDoCo/CodeModelExtractors>`_.

All results (trace links, inconsistencies, etc. between the input model and documentation) are written to the specified output location.

The `CLI <https://github.com/ArDoCo/Core/blob/main/cli/src/main/java/edu/kit/kastel/mcse/ardoco/core/pipeline/ArDoCoCLI.java>`_ is part of the `cli module <https://github.com/ArDoCo/Core/tree/main/cli>`_ of ArDoCo.

.. note::

    In previous versions, we imported models as ontologies converted by `Ecore2OWL <https://github.com/kit-sdq/Ecore2OWL>`_.

Usage
----------

If not changed, the :doc:`standard configuration <standardConfiguration>` will be used.

CLI Parameters
^^^^^^^^^^^^^^

.. code-block::

        usage: java -jar ardoco-core-pipeline.jar

        -c,--conf <arg>                  path to the additional config file
        -h,--help                        show this message
        -ma,--model-architecture <arg>   path to the architecture model
        -mc,--model-code <arg>           path to the java code model
        -n,--name <arg>                  name of the run
        -o,--out <arg>                   path to the output directory
        -t,--text <arg>                  path to the text file

