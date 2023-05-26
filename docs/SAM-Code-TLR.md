# Traceability Link Recovery between Software Architecture Models (SAMs) and Code

The sub-project ARCOTL (Architecture-Code-Trace Links) aims to automatically generate trace links between code and a model of the architecture.
It supports multiple programming languages for the code (Java and Shell) and metamodels for the architecture model (PCM and UML).
To this end the project introduces intermediate models for the architecture (AMTL - Architecture Model for Trace Links) and Code (CMTL - Code Model for Trace
Links).
Trace links are created between instances of these metamodels.
The trace links each have exactly one architecture endpoint and one code endpoint. This is specified by the TLM (Trace Link Model).
The AMTL- and CMTL-instances get extracted from the architecture model and from the code.


