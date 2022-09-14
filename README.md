# ArDoCo Core

[![Maven Verify](https://github.com/ArDoCo/Core/workflows/Maven%20Verify/badge.svg)](https://github.com/ArDoCo/Core/actions?query=workflow%3A%22Maven+Verify%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_Core&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)
[![Latest Release](https://img.shields.io/github/release/ArDoCo/Core.svg)](https://github.com/ArDoCo/Core/releases/latest)

The goal of this project is to connect architecture documentation and models while identifying missing or deviating
elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

ArDoCo is actively developed by researchers of
the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_
of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at
the [KIT](https://www.kit.edu).

## CLI

The Core Project contains a CLI that currently supports to find trace links between PCM Models and Textual SW
Architecture Documentation.
The CLI is part of the pipeline module of this project.
The PCM models have to be converted to ontologies using [Ecore2OWL](https://github.com/kit-sdq/Ecore2OWL).
The model can also contain a (java) code model that you can insert using
the [CodeModelExtractors](https://github.com/ArDoCo/CodeModelExtractors).

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
You can find the generated JavaDocs at [ArDoCo.github.io/Core-Docs](https://ArDoCo.github.io/Core-Docs/).

### Case Studies / Benchmarks

To test the Core, you could use case studies and benchmarks provided in ..

* [ArDoCo Benchmark](https://github.com/ArDoCo/Benchmark)
* [SWATTR](https://github.com/ArDoCo/SWATTR)



### Maven

```xml
<dependencies>
	<dependency>
		<groupId>io.github.ardoco.core</groupId>
		<artifactId>pipeline</artifactId> <!-- or any other subproject -->
		<version>0.6.0-SNAPSHOT</version>
	</dependency>
</dependencies>
```

For snapshot releases, make sure to add the following repository
```xml
<repositories>
	<repository>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
		<id>mavenSnapshot</id>
		<url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
	</repository>
</repositories>
```

### Attribution

The base for this project is based on the master
thesis [Linking Software Architecture Documentation and Models](https://doi.org/10.5445/IR/1000126194).

## Acknowledgments

This work was supported by funding from the topic Engineering Secure Systems of the Helmholtz Association (HGF) and by
KASTEL Security Research Labs (46.23.01).
