# ArDoCo Core

[![Maven Verify](https://github.com/ArDoCo/Core/workflows/Maven%20Verify/badge.svg)](https://github.com/ArDoCo/Core/actions?query=workflow%3A%22Maven+Verify%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_Core&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)
[![Latest Release](https://img.shields.io/github/release/ArDoCo/Core.svg)](https://github.com/ArDoCo/Core/releases/latest)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7274034.svg)](https://doi.org/10.5281/zenodo.7274034)

The goal of this project is to connect architecture documentation and models while identifying missing or deviating
elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

ArDoCo is actively developed by researchers of
the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_
of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at
the [KIT](https://www.kit.edu).

## User Interfaces
To be able to execute the core algorithms from this repository, you can write own user interfaces that (should) use the [ArDoCoRunner](https://github.com/ArDoCo/Core/blob/main/pipeline/src/main/java/edu/kit/kastel/mcse/ardoco/core/pipeline/ArDoCoRunner.java).

We provide an example Command Line Interface (CLI) at [ArDoCo/CLI](https://github.com/ArDoCo/CLI).

Future user interfaces like a Graphical User Interface (GUI) or a web interface are planned for the future.

## Documentation

For more information about the setup or the architecture have a look on the [Wiki](https://github.com/ArDoCo/Core/wiki).
The docs are at some points deprecated, the general overview and setup should still hold.
You can find the generated JavaDocs at [ArDoCo.github.io/Core-Docs](https://ArDoCo.github.io/Core-Docs/).

## Case Studies / Benchmarks

To test the Core, you could use case studies and benchmarks provided in ..

* [ArDoCo Benchmark](https://github.com/ArDoCo/Benchmark)
* [SWATTR](https://github.com/ArDoCo/SWATTR)

## Maven

```xml
<dependencies>
	<dependency>
		<groupId>io.github.ardoco.core</groupId>
		<artifactId>pipeline</artifactId> <!-- or any other subproject -->
		<version>VERSION</version>
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

## Attribution

The initial version of this project is based on the master thesis [Linking Software Architecture Documentation and Models](https://doi.org/10.5445/IR/1000126194).

## Acknowledgements

This work was supported by funding from the topic Engineering Secure Systems of the Helmholtz Association (HGF) and by
KASTEL Security Research Labs (46.23.01).
