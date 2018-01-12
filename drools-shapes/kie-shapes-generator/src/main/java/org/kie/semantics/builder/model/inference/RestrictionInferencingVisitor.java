package org.kie.semantics.builder.model.inference;

import org.apache.log4j.Logger;
import org.drools.semantics.utils.NameUtils;
import org.kie.semantics.util.IRIUtils;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.HasDomain;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLNaryClassAxiom;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedDataRestriction;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.kie.semantics.builder.model.inference.DelegateInferenceStrategy.isSuperClass;
import static org.kie.semantics.builder.model.inference.DelegateInferenceStrategy.lookupDefiningOntology;

public class RestrictionInferencingVisitor implements OWLClassExpressionVisitor {

	private static Logger logger = Logger.getLogger( RestrictionInferencingVisitor.class );

	private OWLClass inKlass;
	private OWLOntology ontoDescr;
	private OWLDataFactory factory;

	private Map<OWLClassExpression, OWLClass> fillerCache = new HashMap<>();

	private boolean dirty = false;
	private int counter = 0;

	public RestrictionInferencingVisitor( OWLClass klass, OWLOntology ontology ) {
		this.inKlass = klass;
		this.ontoDescr = ontology;
		this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
	}

	private void process( OWLClassExpression expr ) {

		if ( expr instanceof OWLNaryBooleanClassExpression ) {
			for (OWLClassExpression clax : ((OWLNaryBooleanClassExpression) expr).getOperandsAsList() ) {
				process( clax );
			}
		} else if ( expr instanceof OWLQuantifiedObjectRestriction ) {

			if ( logger.isInfoEnabled() ) {
				logger.info( "Process Quantified Object Restriction " + expr );
			}

			OWLQuantifiedObjectRestriction rest = (OWLQuantifiedObjectRestriction) expr;

			OWLObjectProperty prop = rest.getProperty().asOWLObjectProperty();
			OWLClassExpression fil = rest.getFiller();

			boolean inDomain = checkIsPropertyInDomain( prop, inKlass, ontoDescr );
			if ( ! inDomain ) {
				rewirePropertyDomain( prop, expr, ontoDescr );
			}
			reifyFiller( prop, fil );

			process( fil );
		} else if ( expr instanceof OWLQuantifiedDataRestriction ) {
			if ( logger.isInfoEnabled() ) {
				logger.info( "Process Quantified Data Restriction " + expr );
			}
		}
	}

	private void rewirePropertyDomain( OWLObjectProperty prop, OWLClassExpression expr, OWLOntology ontoDescr ) {
		OWLClass thing = ontoDescr.getOWLOntologyManager().getOWLDataFactory().getOWLThing();

		OWLOntology defining = lookupDefiningOntology( ontoDescr, prop );
		Set<OWLObjectPropertyDomainAxiom> domains = defining.objectPropertyDomainAxioms( prop ).collect( Collectors.toSet() );

		OWLClassExpression propDomain = thing;
		for ( OWLObjectPropertyDomainAxiom dox : domains ) {
			if ( ! dox.getDomain().isAnonymous() ) {
				propDomain = dox.getDomain();
			}
			ontoDescr.getOWLOntologyManager().applyChange( new RemoveAxiom( ontoDescr, dox ) );
		}

		OWLClass extDomain = factory.getOWLClass( IRI.create(
				prop.getIRI().getNamespace(), NameUtils.capitalize( IRIUtils.fragmentOf( prop.getIRI() ) + "ExtraDomain" + counter++ ) ) );
		ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( extDomain ) ) );
		ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLSubClassOfAxiom( propDomain, extDomain ) ) );
		ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLSubClassOfAxiom( extDomain, thing ) ) );
		ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( prop, extDomain ) ) );


		Set<OWLClassExpression> aliases = new HashSet<>();
		ontoDescr.axioms( AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED ).forEach( (eq) -> {
			if ( eq.contains( expr ) ) {
				aliases.addAll( eq.classExpressions().collect( Collectors.toSet() ) );
			}
		});

		ontoDescr.axioms( AxiomType.SUBCLASS_OF, Imports.INCLUDED )
		         .filter( (sub) -> aliases.contains( sub.getSuperClass() ) && ! sub.getSubClass().equals( extDomain ) )
		         .forEach( (sub) -> {
				ontoDescr.getOWLOntologyManager().applyChange( new RemoveAxiom( ontoDescr, sub )  );
				ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLSubClassOfAxiom( sub.getSubClass(), extDomain ) )  );
			});
		dirty = true;
	}

	private boolean checkIsPropertyInDomain( OWLObjectProperty prop, OWLClass inKlass, OWLOntology ontoDescr ) {
		OWLOntology defining = lookupDefiningOntology( ontoDescr, prop );

		Set<OWLClassExpression> domains = defining.objectPropertyDomainAxioms( prop )
		                                          .map( HasDomain::getDomain )
		                                          .collect( Collectors.toSet() );
		if ( domains.size() == 0 || domains.contains( inKlass ) || isSuperClass( ontoDescr, domains, inKlass ) ) {
			return true;
		}

		defining = lookupDefiningOntology( ontoDescr, inKlass );
		return defining.equivalentClassesAxioms( inKlass )
		        .flatMap( OWLNaryClassAxiom::classExpressions )
		        .filter( (ce) -> ! ce.isAnonymous() )
		        .anyMatch( (ce) -> isSuperClass( ontoDescr, domains, ce ) );
	}

	private void reifyFiller( OWLObjectProperty prop, OWLClassExpression fil ) {
		if ( fil.isAnonymous() ) {
			OWLClass filler = fillerCache.get( fil );

			if ( filler == null ) {
				String fillerName =
						NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) +
								NameUtils.capitalize( IRIUtils.fragmentOf( inKlass.getIRI() ) ) +
								NameUtils.capitalize( IRIUtils.fragmentOf( prop.getIRI() ) ) +
								"Filler" +
								(counter++);
				filler = factory.getOWLClass( IRI.create( fillerName ) );
				fillerCache.put( fil, filler );
			} else {
				if ( logger.isDebugEnabled() )  { logger.debug( "REUSED FILLER FOR" + fil ); }
			}

			dirty = true;
			ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( filler ) ) );
			ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( filler, fil ) ) );
		}
	}

	@Override
	@ParametersAreNonnullByDefault
	public void visit( OWLObjectHasSelf ce ) {
		throw new UnsupportedOperationException();
	}

	@Override
	@ParametersAreNonnullByDefault
	public void visit(OWLObjectOneOf ce ) {

	}

	@Override
	@ParametersAreNonnullByDefault
	public void doDefault( Object object ) {
		if ( object instanceof OWLClassExpression ) {
			process ( OWLClassExpression.class.cast( object ) );
		}
	}

	public boolean isDirty() {
		return dirty;
	}
}
