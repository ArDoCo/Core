# ArDoCo Core

[![Maven Verify](https://github.com/ArDoCo/Core/actions/workflows/verify.yml/badge.svg)](https://github.com/ArDoCo/Core/actions/workflows/verify.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ardoco.core/parent)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ArDoCo_Core&metric=alert_status)](https://sonarcloud.io/dashboard?id=ArDoCo_Core)
[![Latest Release](https://img.shields.io/github/release/ArDoCo/Core.svg)](https://github.com/ArDoCo/Core/releases/latest)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7274034.svg)](https://doi.org/10.5281/zenodo.7274034)

The goal of this project is to connect architecture documentation and models with Traceability Link Recovery (TLR) while identifying missing or deviating
elements (inconsistencies).
An element can be any representable item of the model, like a component or a relation.
To do so, we first create trace links and then make use of them and other information to identify inconsistencies.

ArDoCo is actively developed by researchers of
the _[Modelling for Continuous Software Engineering (MCSE) group](https://mcse.kastel.kit.edu)_
of _[KASTEL - Institute of Information Security and Dependability](https://kastel.kit.edu)_ at
the [KIT](https://www.kit.edu).

## User Interfaces

To be able to execute the core algorithms from this repository, you can write own user interfaces that (should) use
the [ArDoCoRunner](https://github.com/ArDoCo/Core/blob/main/pipeline/pipeline-core/src/main/java/edu/kit/kastel/mcse/ardoco/core/execution/runner/ArDoCoRunner.java).

We provide an example Command Line Interface (CLI) at [ArDoCo/CLI](https://github.com/ArDoCo/CLI) as well as a simple Graphical User Interface (GUI)
at [ArDoCo/GUI](https://github.com/ArDoCo/GUI).

Future user interfaces like an enhanced GUI or a web interface are planned.

## Documentation

For more information about the setup or the architecture have a look on the [Wiki](https://github.com/ArDoCo/Core/wiki).
The docs are at some points deprecated, the general overview and setup should still hold.

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

## Microservice for text preprocessing

Text preprocessing works locally, but there is also the option to host a microservice for this.
The benefit is that the models do not need to be loaded each time, saving some runtime (and local memory).

The microservice can be found at [ArDoCo/StanfordCoreNLP-Provider-Service](https://github.com/ArDoCo/StanfordCoreNLP-Provider-Service/).

The microservice is secured with credentials and the usage of the microservice needs to be activated and the URL of the microservice configured.
These settings can be provided to the execution via environment variables.
To do so, set the following variables:

```env
NLP_PROVIDER_SOURCE=microservice
MICROSERVICE_URL=[microservice_url]
SCNLP_SERVICE_USER=[your_username]
SCNLP_SERVICE_PASSWORD=[your_password]
```

The first variable `NLP_PROVIDER_SOURCE=microservice` activates the microservice usage.
The next three variables configure the connection, and you need to provide the configuration for your deployed microservice.

## Attribution

The initial version of this project is based on the master
thesis [Linking Software Architecture Documentation and Models](https://doi.org/10.5445/IR/1000126194).

## Acknowledgements

This work was supported by funding from the topic Engineering Secure Systems of the Helmholtz Association (HGF) and by
KASTEL Security Research Labs (46.23.01).
