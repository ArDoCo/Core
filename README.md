# Core
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)

[![Maven Verify](https://github.com/ArDoCo/Core/workflows/Maven%20Verify/badge.svg)](https://github.com/ArDoCo/Core/actions?query=workflow%3A%22Maven+Verify%22)


The goal of this project is to connect architecture documentation and models while identifying missing or deviating elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.


## CLI
The Core Project contains a CLI that currently supports to find trace links between PCM Models and Textual SW Architecture Documentation.
The CLI is part of the pipeline module of this project.
The PCM models have to be converted to ontologies using [Ecore2OWL](https://github.com/kit-sdq/Ecore2OWL).
The model can also contain a (java) code model that you can insert using the [CodeModelExtractors](https://github.com/ArDoCo/CodeModelExtractors).

### Usage
```
usage: java -jar ardoco-core-pipeline.jar
-c,--conf <arg>                  path to the additional config file
-h,--help                        show this message
-ma,--model-architecture <arg>   path to the architecture model
-mc,--model-code <arg>           path to the java code model
-n,--name <arg>                  name of the run
-o,--out <arg>                   path to the output directory
-t,--text <arg>                  path to the text file
```

### Documentation
For more information about the setup or the architecture have a look on the [docs](https://ardoco.github.io/Core).
The docs are at some points deprecated, the general overview and setup should still hold.

### Case Studies / Benchmarks
To test the Core, you could use case studies and benchmarks provided in ..
* [ArDoCo Benchmark](https://github.com/ArDoCo/Benchmark)
* [SWATTR](https://github.com/ArDoCo/SWATTR)

### Attribution
The base for this project is based on the master thesis [Linking Software Architecture Documentation and Models](https://doi.org/10.5445/IR/1000126194).
