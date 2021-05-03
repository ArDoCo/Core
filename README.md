# Core
 [![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)
 
[![Maven Verify](https://github.com/ArDoCo/Core/workflows/Maven%20Verify/badge.svg)](https://github.com/ArDoCo/Core/actions?query=workflow%3A%22Maven+Verify%22)


The goal of this project is to connect architecture documentation and models while identifying missing or deviating elements. An element can be any representable item of the model, like a component or a relation.

This project is based on the master thesis [Linking Software Architecture Documentation and Models](https://doi.org/10.5445/IR/1000126194).

For more information about the setup or the architecture have a look on the [wiki](https://github.com/ArDoCo/Core/wiki/Overview).

## CLI
The Core Project contains a CLI that currently supports to find trace links between PCM Models and Textual SW Architecture Documentation. The CLI is part of the pipeline module of this project. The PCM models have to be converted to ontologies using [Ecore2OWL](https://github.com/kit-sdq/Ecore2OWL).

### Usage
```
java -jar ardoco-core-pipeline \
	
	-n NAME_OF_THE_PROJECT (will be stored in the results)
	-m PATH_TO_THE_PCM_MODEL_AS_OWL (use Ecore2OWL to obtain PCM models as ontology)
	-t PATH_TO_PLAIN_TEXT
	-o PATH_TO_OUTPUT_FOLDER
	
	Optional Parameters:
	
	-c CONFIG_FILE (the config file can override any default configuration using the standard property syntax (see config files in src/main/resources)
	
```

### Case Studies
To test the Core, you could use case studies provided in ..
* [ArDoCo Case Studies](https://github.com/ArDoCo/CaseStudies)
* [SWATTR](https://github.com/ArDoCo/SWATTR)
