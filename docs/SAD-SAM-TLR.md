# Traceability Link Recovery between Software Architecture Documentations (SADs) and Software Architecture Models (SAMs)

To recover trace links between SADs and SAMs, we use a pipeline approach with different major processing steps:

1. Model Extraction: Processes the architecture model.
2. Text Preprocessing: Processes the text initially, including basic text processing like tokenization, part-of-speech tagging, dependency parsing.
3. Text Extraction: Identification of potential parts of interest in the text.
4. Recommendation Generator: Further processing of interesting parts of text to generate recommendations for parts that should/could be model elements.
5. Connection Generator: Mapping of recommended parts to model parts.
