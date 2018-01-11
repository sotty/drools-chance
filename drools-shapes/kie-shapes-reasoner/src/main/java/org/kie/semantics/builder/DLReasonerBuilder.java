package org.kie.semantics.builder;


import org.semanticweb.owlapi.model.OWLOntology;

import java.io.InputStream;

public interface DLReasonerBuilder {

    OWLOntology parseOntology( InputStream resource );

    String buildTableauRules( OWLOntology ontologyDescr, String[] classPathVisitorResources );

}
