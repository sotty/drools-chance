package org.kie.semantics.util;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;


public class IRIUtils {

	public static String ontologyNamespace( OWLOntology ontoDescr ) {
		return ontoDescr.getOntologyID().getOntologyIRI()
		                .map( (u) -> u.toURI().toString() )
		                .orElse( "" );
	}

	public static String iriOf( OWLClassExpression expr ) {
		return expr.asOWLClass().getIRI().toQuotedString();
	}

	public static String iriOf( OWLDataPropertyExpression expr ) {
		return expr.asOWLDataProperty().getIRI().toQuotedString();
	}

	public static String iriOf( OWLObjectPropertyExpression expr ) {
		return expr.asOWLObjectProperty().getIRI().toQuotedString();
	}

	public static String fragmentOf( IRI iri ) {
		return iri.getRemainder().orElse( "MISSING" );
	}

	public static String iriOf( OWLDatatype datatype ) {
		return datatype.getIRI().toQuotedString();
	}

	public static String iriOf( OWLDataRange dataRange ) {
		return iriOf( dataRange.asOWLDatatype() );
	}

	public static String iriOf( OWLNamedIndividual ind ) {
		return ind.getIRI().toQuotedString();
	}


}
