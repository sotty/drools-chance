package org.kie.semantics.builder.reasoner;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DLogicTransformer {

    private OWLOntology onto;

    public DLogicTransformer( OWLOntology onto ) {
        this.onto = onto;
    }

    public Stream<OWLClassExpression> getDefinitions( OWLClassExpression expr ) {
        if ( ! expr.isAnonymous() ) {
            return EntitySearcher.getEquivalentClasses( expr.asOWLClass(), onto.importsClosure() )
                                 .filter( (def) -> ! expr.equals( def ) );
        } else {
            throw new UnsupportedOperationException( "Unable to process " + expr + ", not a named class." );
        }
    }

    public Stream<OWLClassExpression> getNormalizedDefinitions( OWLClassExpression expr ) {
        if ( ! expr.isAnonymous() ) {
        	return getDefinitions( expr ).map( this::toDNF );
        } else {
            throw new UnsupportedOperationException( "Unable to process " + expr + ", not a named class." );
        }
    }

    public Map<OWLClassExpression,OWLClassExpression> getDefinitions() {
        Map<OWLClassExpression,OWLClassExpression> defs = new HashMap<>();

	    onto.classesInSignature( Imports.INCLUDED )
	        .forEach( (klass) -> onto.equivalentClassesAxioms( klass )
	                                 .flatMap( (eq) -> eq.asPairwiseAxioms().stream() )
	                                 .map( (equiv) -> equiv.classExpressions().collect( Collectors.toList() ) )
	                                 .forEach( (args) -> {
		                                   if ( args.size() == 2 && !args.get( 0 ).isAnonymous() ) {
			                                   defs.put( klass, toDNF( args.get( 1 ) ) );
		                                   } else {
			                                   throw new UnsupportedOperationException( "Unable to process equivalence for " + klass );
		                                   }
	                                   }) );
	    return defs;
    }

    public OWLClassExpression toDNF( OWLClassExpression in ) {
        OWLClassExpression nnf = in.getNNF();
        DNFVisitor visitor = new DNFVisitor();
        nnf.accept( visitor );
	    return visitor.getDNF();
    }

    private class DNFVisitor implements OWLClassExpressionVisitor {

        private Stack<List<OWLClassExpression>> DNF;
        private OWLDataFactory factory;

        private DNFVisitor() {
            factory = onto.getOWLOntologyManager().getOWLDataFactory();
            DNF = new Stack<>();
            DNF.push( new ArrayList<>() );
        }

        public OWLClassExpression getDNF() {
            Set<OWLClassExpression> set = new HashSet<>( DNF.pop() );
            if ( set.size() == 1 && set.iterator().next() instanceof OWLObjectUnionOf ) {
                return set.iterator().next();
            } else {
                return factory.getOWLObjectUnionOf( set );
            }
        }

        @ParametersAreNonnullByDefault
        public void visit( OWLClass ce ) {
            DNF.peek().add( processAtom( ce ) );
        }

        private OWLClassExpression processAtom( OWLClassExpression ce ) {
            return factory.getOWLObjectIntersectionOf( ce );
        }


        @ParametersAreNonnullByDefault
        public void visit( OWLObjectIntersectionOf ce ) {
        	int n = ( int ) ce.operands().count();
            List<List<OWLClassExpression>> bits = new ArrayList<>( n );
            int card = 1;

            for ( OWLClassExpression x : ce.operands().collect( Collectors.toList() ) ) {
                DNF.push( new ArrayList<>() );
                x.accept( this );
                List<OWLClassExpression> bit = DNF.pop();
                card *= bit.size();
                bits.add( bit );
            }

            List<OWLClassExpression>[] ands = new ArrayList[ card ];
            for ( int j = 0; j < card; j++ ) {
                ands[ j ] = new ArrayList<>();
            }

            int blocks = 1;
	        for ( List<OWLClassExpression> bit : bits ) {
		        int M = bit.size();

		        for ( int j = 0; j < M; j++ ) {
			        int reps = card / ( blocks * M );
			        int step = card / blocks;

			        for ( int i = 0; i < blocks; i++ ) {
				        for ( int l = 0; l < reps; l++ ) {
					        ands[ j * reps + i * step + l ].add( bit.get( j ) );
				        }
			        }

		        }
		        blocks *= M;
	        }

            OWLObjectIntersectionOf[] owlAnds = new OWLObjectIntersectionOf[ ands.length ];
            for ( int j = 0; j < ands.length; j++ ) {
                List<OWLClassExpression> args = ands[ j ];
                for ( int k = 0; k < args.size(); k++ ) {
                    OWLClassExpression expr = args.get( k );
                    if ( expr instanceof OWLObjectUnionOf && ((OWLObjectUnionOf) expr).operands().count() == 1 ) {
                        args.set( k, ((OWLObjectUnionOf) expr).operands().iterator().next() );
                    }
                }
                HashSet<OWLClassExpression> andArgs = new HashSet<>();
                for ( OWLClassExpression expr : ands[ j ] ) {
                    if ( expr instanceof OWLObjectIntersectionOf ) {
                        andArgs.addAll( ((OWLObjectIntersectionOf) expr).operands().collect( Collectors.toList() ) );
                    } else {
                        andArgs.add( expr );
                    }
                }
                owlAnds[ j ] = factory.getOWLObjectIntersectionOf( andArgs );
            }

            DNF.peek().add( factory.getOWLObjectUnionOf( owlAnds ) );
        }

	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectUnionOf ce ) {
            DNF.push( new ArrayList<>() );
            ce.operands().forEach( (x) -> x.accept( this ) );
            List<OWLClassExpression> newArgs = DNF.pop();
            DNF.peek().addAll( newArgs );
        }

	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectComplementOf ce ) {
            DNF.peek().add( processAtom( ce ) );
        }



	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectSomeValuesFrom ce ) {
            DNF.peek().add( factory.getOWLObjectSomeValuesFrom( ce.getProperty(), toDNF( ce.getFiller() ) ) );
        }

	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectAllValuesFrom ce ) {
//            DNF.peek().add( factory.getOWLObjectAllValuesFrom( ce.getProperty(), toDNF( ce.getFiller() ) ) );
            DNF.peek().add(
                    factory.getOWLObjectComplementOf(
                            factory.getOWLObjectSomeValuesFrom( ce.getProperty(),
                                    toDNF( factory.getOWLObjectComplementOf( ce.getFiller() ).getNNF() )
                            )
                    )
            );
        }

	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectMinCardinality ce ) {
            DNF.peek().add( factory.getOWLObjectMinCardinality( ce.getCardinality(), ce.getProperty(), toDNF( ce.getFiller() ) ) );
        }

	    @ParametersAreNonnullByDefault
        public void visit( OWLObjectExactCardinality ce ) {
            DNF.peek().add( factory.getOWLObjectExactCardinality( ce.getCardinality(), ce.getProperty(), toDNF( ce.getFiller() ) ) );
        }

        @ParametersAreNonnullByDefault
        public void visit( OWLObjectMaxCardinality ce ) {
            DNF.peek().add( factory.getOWLObjectMaxCardinality( ce.getCardinality(), ce.getProperty(), toDNF( ce.getFiller() ) ) );
        }

	    @Override
	    @ParametersAreNonnullByDefault
	    public void doDefault( Object object ) {
		    DNF.peek().add( processAtom( ( OWLClassExpression ) object ) );
	    }

    }
}
