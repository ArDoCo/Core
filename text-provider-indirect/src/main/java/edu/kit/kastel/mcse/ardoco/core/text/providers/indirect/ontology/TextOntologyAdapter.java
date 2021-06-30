package edu.kit.kastel.mcse.ardoco.core.text.providers.indirect.ontology;

import edu.kit.kastel.mcse.ardoco.core.ontology.OntologyConnector;

public class TextOntologyAdapter {

    private OntologyConnector ontologyConnector;

    public TextOntologyAdapter(String ontologyPath) {
        ontologyConnector = new OntologyConnector(ontologyPath);
    }

    public TextOntologyAdapter(OntologyConnector ontologyConnector) {
        this.ontologyConnector = ontologyConnector;
    }

}
