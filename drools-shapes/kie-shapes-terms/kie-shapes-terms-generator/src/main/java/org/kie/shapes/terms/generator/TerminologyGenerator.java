package org.kie.shapes.terms.generator;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.impl.model.AnonymousConceptScheme;
import org.kie.shapes.terms.impl.model.DefaultConceptCoding;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AsOWLNamedIndividual;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TerminologyGenerator {

	private static final IRI CONCEPT_SCHEME = IRI.create( "http://www.w3.org/2004/02/skos/core#ConceptScheme" );
	private static final IRI CONCEPT = IRI.create( "http://www.w3.org/2004/02/skos/core#Concept" );
	private static final IRI LABEL = IRI.create( "http://www.w3.org/2004/02/skos/core#prefLabel" );
	private static final IRI NOTATION = IRI.create( "http://www.w3.org/2004/02/skos/core#notation" );
	private static final IRI IN_SCHEME = IRI.create( "http://www.w3.org/2004/02/skos/core#inScheme" );

	private static final IRI OID = IRI.create( "https://www.hl7.org/oid" );

	private OWLOntology model;

	public TerminologyGenerator( OWLOntology o, boolean reason ) {
		this.model = o;
		if ( reason ) {
			this.doReason( o );
		}
	}

	public Map<URI, ConceptScheme<ConceptDescriptor>> traverse() {

		OWLOntologyManager manager = model.getOWLOntologyManager();
		OWLDataFactory odf = manager.getOWLDataFactory();

		// build the code systems first
		Map<URI, ConceptScheme<ConceptDescriptor>> codeSystems;
		codeSystems = model.individualsInSignature( Imports.INCLUDED )
		                   .filter( this::isConceptScheme )
		                   .map( x -> toScheme( x, model ) )
		                   .collect( Collectors.toMap( ConceptScheme::getSchemeURI,
		                                               Function.identity() ) );

		// then the concepts
		model.individualsInSignature( Imports.INCLUDED )
		     .filter( this::isConcept )
		     .forEach( (ind) -> toCode( ind,
		                                getOrCreateSchemes( ind, model, codeSystems ),
		                                model ) );
		return codeSystems;
	}

	private Collection<ConceptScheme<ConceptDescriptor>> getOrCreateSchemes( OWLNamedIndividual ind,
	                                                                         OWLOntology model,
	                                                                         Map<URI,ConceptScheme<ConceptDescriptor>> codeSystems ) {
		return getPropertyValues( ind, model, IN_SCHEME )
		                     .map( AsOWLNamedIndividual::asOWLNamedIndividual )
		                     .map( (sch) -> codeSystems.getOrDefault( getURI( sch ), toScheme( sch, model ) ) )
		                     .collect( Collectors.toSet() );
	}

	private ConceptScheme<ConceptDescriptor> toScheme( OWLNamedIndividual ind, OWLOntology model ) {
		URI uri = getURI( ind );
		String code = getDataValues( ind, model, NOTATION ).findFirst()
		                                                   .orElse( getAnnotationValues( ind, model, OID ).findFirst()
		                                                                                            .orElse( uri.getFragment() ) );
		String label = getAnnotationValues( ind, model, LABEL ).findFirst().orElse( uri.getFragment() );

		return new MutableConceptScheme( uri, code, label );
	}

	private ConceptDescriptor toCode( OWLNamedIndividual ind,
	                                  Collection<ConceptScheme<ConceptDescriptor>> schemes,
	                                  OWLOntology model ) {
		if ( schemes.size() >= 2 ) {
			throw new UnsupportedOperationException( "TBD: Unable to handle concepts in more than 2 schemes" );
		}
		MutableConceptScheme scheme = schemes.isEmpty() ? null : ( MutableConceptScheme ) schemes.iterator().next();

		URI uri = getURI( ind );

		String code = getDataValues( ind, model, NOTATION ).findFirst().orElse( uri.getFragment() );
		String label = getAnnotationValues( ind, model, LABEL ).findFirst().orElse( uri.getFragment() );

		ConceptDescriptor cd = new DefaultConceptCoding( code, label, uri, scheme );
		if ( scheme != null ) {
			scheme.addConcept( cd );
		}
		return cd;
	}

	private URI getURI( HasIRI x ) {
		return URI.create( x.getIRI().toString() );
	}

	private boolean isConcept( OWLNamedIndividual ind ) {
		return is( ind, CONCEPT );
	}

	private boolean isConceptScheme( OWLNamedIndividual ind ) {
		return is( ind, CONCEPT_SCHEME);
    }
	private boolean is( OWLNamedIndividual ind, IRI type ) {
		return EntitySearcher.getTypes( ind, model.importsClosure() )
		              .anyMatch( (kls) -> ! kls.isAnonymous() && kls.asOWLClass().getIRI().equals( type ) );
	}

	private Stream<OWLIndividual> getPropertyValues( OWLNamedIndividual ind, OWLOntology model, IRI prop ) {
		OWLObjectProperty p = model.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty( prop );
		return EntitySearcher.getObjectPropertyValues( ind,
		                                               p,
		                                               model.importsClosure() );
	}


	private Stream<String> getAnnotationValues( OWLNamedIndividual ind, OWLOntology model, IRI prop ) {
		OWLAnnotationProperty p = model.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty( prop );
		return EntitySearcher.getAnnotationObjects( ind, model.importsClosure(), p )
		                     .map( OWLAnnotation::getValue )
							 .map( OWLAnnotationValue::asLiteral )
		                     .filter( Optional::isPresent )
		                     .map( Optional::get )
		                     .map( OWLLiteral::getLiteral );
	}

	private Stream<String> getDataValues( OWLNamedIndividual ind, OWLOntology model, IRI prop ) {
		OWLDataProperty p = model.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty( prop );
		return EntitySearcher.getDataPropertyValues( ind, p, model.importsClosure() )
		                     .map( OWLLiteral::getLiteral );
	}


	private void doReason( OWLOntology o ) {
        OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        OWLReasoner owler = reasonerFactory.createReasoner( o );

        InferredOntologyGenerator reasoner = new InferredOntologyGenerator( owler );

        OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

        reasoner.fillOntology( owlOntologyManager.getOWLDataFactory(), o );
    }



	private class MutableConceptScheme extends AnonymousConceptScheme {

		private Set<ConceptDescriptor> concepts = new HashSet<>();

		public MutableConceptScheme( URI uri, String code, String label ) {
			super( uri, code, label );
		}

		public void addConcept( ConceptDescriptor cd ) {
			this.concepts.add( cd );
		}

		@Override
		public Stream<ConceptDescriptor> getConcepts() {
			return concepts.stream();
		}
	}
}
