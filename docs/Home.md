ArDoCo (Architecture Documentation Consistency) is a framework to connect architecture documentation and models while
identifying missing or deviating elements (inconsistencies). An element can be any representable item of the model, like
a component or a relation. To do so, ArDoCo first creates trace links and then makes use of them and other information
to identify inconsistencies.

You can find [ArDoCo on GitHub](https://github.com/ArDoCo).

Before contributing, please read the [Quickstart Guide](quickstart).

JavaDocs can be found [here](https://ardoco.github.io/Core-Docs/).

## System Requirements

The `complete` profile includes all the requirements that the special profiles also need. This profile is activated by
default.

All profiles require JDK 21.

The dependencies of the other profiles at a glance:

* tlr: -
* inconsistency: -
* lissa (LInking Sketches and Software Architecture): Docker (local
  or [remote](https://github.com/ArDoCo/Core/blob/lissa/stages/diagram-recognition/src/main/kotlin/edu/kit/kastel/mcse/ardoco/lissa/diagramrecognition/informants/DockerInformant.kt#L20-L23))

## Case Studies & Benchmarks

You can test ArDoCo using our case studies and benchmarks provided in ...

* [Case Studies](https://github.com/ArDoCo/SWATTR)
* [Benchmarks](https://github.com/ArDoCo/Benchmark)

## Publications

Trace Link Recovery for Software Architecture Documentation Keim, J.; Schulz, S.; Fuchß, D.; Kocher, C.; Speit, J.;
Koziolek, A. 2021. Software Architecture: 15th European Conference, ECSA 2021, Virtual Event, Sweden, September 13-17,
2021, Proceedings. Ed.: S. Biffl, 101–116, Springer
Verlag. [doi:10.1007/978-3-030-86044-8_7](https://doi.org/10.1007/978-3-030-86044-8_7)

The initial version of ArDoCo is based on the master
thesis [Linking Software Architecture Documentation and Models](https://publikationen.bibliothek.kit.edu/1000126194).

## Contact

This project is currently developed by researchers of the Karlsruhe Institute of Technology.

You find us on our
websites: [Jan Keim](https://mcse.kastel.kit.edu/staff_Keim_Jan.php), [Sophie Corallo](https://mcse.kastel.kit.edu/staff_sophie_corallo.php),
and [Dominik Fuchß](https://mcse.kastel.kit.edu/staff_dominik_fuchss.php)
