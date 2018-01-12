package org.kie.semantics.builder.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

public class Annotations {

    public static final IRI ATTRIBUTE = IRI.create( "http://drools.org/shapes/attribute" );

    public static boolean hasAnnotation( OWLEntity subj, IRI iri, OWLOntology o ) {
	    return EntitySearcher.getAnnotations( subj,
	                                          o.importsClosure(),
	                                          o.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty( iri ) )
	                         .findAny()
	                         .isPresent();

    }
}
