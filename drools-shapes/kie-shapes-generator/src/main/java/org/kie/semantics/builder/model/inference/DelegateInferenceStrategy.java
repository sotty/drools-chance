/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.semantics.builder.model.inference;

import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.drools.core.util.HierarchySorter;
import org.kie.api.io.Resource;
import org.kie.semantics.builder.DLFactoryConfiguration;
import org.kie.semantics.builder.model.Annotations;
import org.kie.semantics.builder.model.Concept;
import org.kie.semantics.builder.model.Individual;
import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.builder.model.PropertyRelation;
import org.kie.semantics.builder.model.SubConceptOf;
import org.kie.semantics.util.IRIUtils;
import org.kie.semantics.utils.NameUtils;
import org.kie.semantics.utils.NamespaceUtils;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.IsAnonymous;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.w3._2002._07.owl.Thing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.kie.semantics.utils.NameUtils.createSuffix;
import static org.semanticweb.owlapi.search.EntitySearcher.getAnnotations;
import static org.semanticweb.owlapi.search.EntitySearcher.getDataPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getDomains;
import static org.semanticweb.owlapi.search.EntitySearcher.getEquivalentClasses;
import static org.semanticweb.owlapi.search.EntitySearcher.getObjectPropertyValues;
import static org.semanticweb.owlapi.search.EntitySearcher.getRanges;
import static org.semanticweb.owlapi.search.EntitySearcher.getSuperClasses;
import static org.semanticweb.owlapi.search.EntitySearcher.getTypes;

public class DelegateInferenceStrategy extends AbstractModelInferenceStrategy {

	private static Logger logger = Logger.getLogger( DelegateInferenceStrategy.class );

	private int counter = 0;

	public int minCounter = 0;
	public int maxCounter = 0;

	private Map<OWLClassExpression,OWLClassExpression> aliases;
	private Map<OWLClassExpression,Set<OWLClassExpression>> reverseAliases = new HashMap<>();
	private Map<OWLClassExpression,OWLClass> anonNameAliases = new HashMap<>();

	private Map<String, Concept> conceptCache = new LinkedHashMap<>();
	private Map<String, String> individualTypesCache = new HashMap<>();
	private Map<String, String> props = new HashMap<>();


	private static void register( String prim, String klass ) {
		IRI i1 = IRI.create( prim );
		Concept con = new Concept( i1, null, klass, true );
		primitives.put( i1.toQuotedString(), con );
	}

	private static void registerComplexConcept( String conIri, String mappedKlassName ) {
		IRI i1 = IRI.create( conIri );
		Concept con = new Concept( i1, null, mappedKlassName, false );
		complexes.put( i1.toQuotedString(), con );
	}


	public DelegateInferenceStrategy() {
		this.conceptCache.putAll( complexes );
	}

	private static Map<String, Concept> complexes = new HashMap<>();
	private static Map<String, Concept> primitives = new HashMap<>();

	static {

		register( "http://www.w3.org/2001/XMLSchema#string", "xsd:string" );

		register( "http://www.w3.org/2001/XMLSchema#dateTime", "xsd:dateTime" );

		register( "http://www.w3.org/2001/XMLSchema#date", "xsd:date" );

		register( "http://www.w3.org/2001/XMLSchema#time", "xsd:time" );

		register( "http://www.w3.org/2001/XMLSchema#int", "xsd:int" );

		register( "http://www.w3.org/2001/XMLSchema#integer", "xsd:integer" );

		register( "http://www.w3.org/2001/XMLSchema#long", "xsd:long" );

		register( "http://www.w3.org/2001/XMLSchema#float", "xsd:float" );

		register( "http://www.w3.org/2001/XMLSchema#double", "xsd:double" );

		register( "http://www.w3.org/2001/XMLSchema#short", "xsd:short" );

		register( "http://www.w3.org/2000/01/rdf-schema#Literal", "xsd:anySimpleType" );

		register( "http://www.w3.org/2000/01/rdf-schema#XMLLiteral", "xsd:anySimpleType" );

		register( "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral", "xsd:anySimpleType" );

		register( "http://www.w3.org/2001/XMLSchema#boolean", "xsd:boolean" );

		register( "http://www.w3.org/2001/XMLSchema#decimal", "xsd:decimal" );

		register( "http://www.w3.org/2001/XMLSchema#byte", "xsd:byte" );

		register( "http://www.w3.org/2001/XMLSchema#unsignedByte", "xsd:unsignedByte" );

		register( "http://www.w3.org/2001/XMLSchema#unsignedShort", "xsd:unsignedShort" );

		register( "http://www.w3.org/2001/XMLSchema#unsignedInt", "xsd:unsignedInt" );

		register( "http://www.w3.org/2001/XMLSchema#base64Binary", "xsd:base64Binary" );

		register( "http://www.w3.org/2001/XMLSchema#anyURI", "xsd:anyURI" );

		registerComplexConcept( "http://www.w3.org/2001/XMLSchema#List", List.class.getSimpleName() );

		registerComplexConcept( "http://www.w3.org/1999/02/22-rdf-syntax-ns#List", List.class.getSimpleName() );

	}


	@Override
	protected OntoModel buildProperties( OWLOntology ontoDescr, Map<InferenceTask, Resource> theory, OntoModel hierarchicalModel, DLFactoryConfiguration conf ) {

		OWLDataFactory factory = ontoDescr.getOWLOntologyManager().getOWLDataFactory();

		fillPropNamesInDataStructs( ontoDescr );

		// Complete missing domains and ranges for properties. Might be overridden later, if anything can be inferred
		createAndAddBasicProperties( ontoDescr, factory, hierarchicalModel );

		// Apply any cardinality / range restriction
		applyPropertyRestrictions( ontoDescr, hierarchicalModel, factory );

		// Compose property chains
		fixPropertyChains( ontoDescr, hierarchicalModel );

		// Manage inverse relations
		fixInverseRelations( ontoDescr, hierarchicalModel );

		// assign Key properties
		setKeys( ontoDescr );

		fixRootHierarchy( hierarchicalModel );

		validate( hierarchicalModel );

		return hierarchicalModel;
	}

	@Override
	protected OntoModel buildIndividuals(OWLOntology ontoDescr, Map<InferenceTask, Resource> theory, OntoModel hierachicalModel, DLFactoryConfiguration conf ) {

		ontoDescr.individualsInSignature( Imports.INCLUDED ).forEach( ( OWLNamedIndividual individual ) -> {

			if ( logger.isInfoEnabled() ) { logger.info( "Found Individual " + individual.getIRI() ); }

			IRI iri = individual.getIRI();

			String typeIri = individualTypesCache.get( iri.toQuotedString() );
			Concept klass = hierachicalModel.getConcept( typeIri );
			if ( klass == null ) {
				logger.error( "found individual with no class " + iri );
				System.exit( -1 );
			}

			Individual ind = new Individual( IRIUtils.fragmentOf( iri ), iri.toQuotedString(), klass.getFullyQualifiedName() );


			Multimap<OWLDataPropertyExpression, OWLLiteral> props = getDataPropertyValues( individual, ontoDescr.importsClosure() );
			for ( OWLDataPropertyExpression prop : props.keySet() ) {
				if ( !prop.isTopEntity() ) {
					PropertyRelation rel = hierachicalModel.getProperty( IRIUtils.iriOf( prop ) );
					String propName = rel.getName();
					Collection<OWLLiteral> propValues = props.get( prop );
					Set<Individual.ValueTypePair> values = new HashSet<>();
					for ( OWLLiteral tgt : propValues ) {
						String value;
						String typeName = rel.getTarget().getFullyQualifiedName();
						//TODO improve datatype checking
						switch ( typeName ) {
							case "xsd:string":
							case "xsd:dateTime":
								value = String.format( "\"%s\"", tgt.getLiteral() );
								break;
							default:
								value = tgt.getLiteral();
						}
						Individual.ValueTypePair vtp = new Individual.ValueTypePair( value, typeName );
						values.add( vtp );
					}
					ind.setPropertyValues( propName, values );
				}
			}


			Multimap<OWLObjectPropertyExpression, OWLIndividual> oprops = getObjectPropertyValues( individual, ontoDescr.importsClosure() );
			for ( OWLObjectPropertyExpression prop : oprops.keySet() ) {
				if ( ! prop.isTopEntity() ) {
					String propName = hierachicalModel.getProperty( IRIUtils.iriOf( prop ) ).getName();
					Collection<OWLIndividual> propValues = oprops.get( prop );
					Set<Individual.ValueTypePair> values = new HashSet<>();
					for ( OWLIndividual tgt : propValues ) {
						if ( tgt instanceof OWLNamedIndividual ) {
							values.add( new Individual.ValueTypePair(
									IRIUtils.fragmentOf( ((OWLNamedIndividual) tgt).getIRI() ),
									"object" ) );
						}
					}
					ind.setPropertyValues( propName, values );
				}
			}
			hierachicalModel.addIndividual( ind );

		});

		return hierachicalModel;
	}


	private void fixInverseRelations( OWLOntology ontoDescr, OntoModel hierarchicalModel ) {
		ontoDescr.axioms( AxiomType.INVERSE_OBJECT_PROPERTIES ).forEach( (ax) -> {
			String fst = IRIUtils.iriOf( ax.getFirstProperty() );
			if ( ! ax.getSecondProperty().isAnonymous() ) {
				String sec = IRIUtils.iriOf( ax.getSecondProperty() );

				PropertyRelation first = hierarchicalModel.getProperty( fst );
				PropertyRelation second = hierarchicalModel.getProperty( sec );

				if ( first != null && second != null ) {
					if ( logger.isInfoEnabled() ) { logger.info(  "Marking " + first + " as Inverse" ); }
					first.setInverse( second );
					second.setInverse( first );
				}
			}
		});
	}

	private void setKeys( OWLOntology ontoDescr ) {
		ontoDescr.axioms( AxiomType.HAS_KEY ).forEach( (hasKey) -> {
			Concept con = conceptCache.get( IRIUtils.iriOf( hasKey.getClassExpression() ) );
			hasKey.dataPropertyExpressions()
			      .forEach( (expr) -> con.addKey( IRIUtils.iriOf( expr ) ) );
			hasKey.objectPropertyExpressions()
			      .forEach( (expr) -> con.addKey( IRIUtils.iriOf( expr ) ) );
		});
	}

	private void fixPropertyChains( OWLOntology ontoDescr, OntoModel hierarchicalModel ) {
		ontoDescr.axioms( AxiomType.SUB_PROPERTY_CHAIN_OF ).forEach( (ax) -> {
			String propIri = IRIUtils.iriOf( ax.getSuperProperty() );
			PropertyRelation prop = hierarchicalModel.getProperty( propIri );
			List<PropertyRelation> chain = new ArrayList<>();
			for ( OWLObjectPropertyExpression link : ax.getPropertyChain() ) {
				chain.add( hierarchicalModel.getProperty( IRIUtils.iriOf( link ) ) );
			}
			prop.addPropertyChain( chain );
		});
	}


	private void applyPropertyRestrictions( OWLOntology ontoDescr, OntoModel hierarchicalModel, OWLDataFactory factory ) {

		for ( PropertyRelation prop : hierarchicalModel.getProperties() ) {
			if ( prop.getTarget() == null ) {
				logger.warn( "Property without target concept " +  prop.getName() );
			}
		}


		Map<String, Set<OWLClassExpression>> supers = new HashMap<>();
		ontoDescr.classesInSignature( Imports.INCLUDED )
		         .forEach( (klass) -> supers.put( IRIUtils.iriOf( klass ), new HashSet<>() )
		                 );

//        ontoDescr.axioms( AxiomType.SUBCLASS_OF, Imports.INCLUDED ).forEach( (subConceptOfSet) -> {
		Set<OWLSubClassOfAxiom> subConceptOfSet = ontoDescr.axioms( AxiomType.SUBCLASS_OF, Imports.INCLUDED ).collect( Collectors.toSet() );

		ontoDescr.classesInSignature( Imports.INCLUDED ).forEach( (klass) -> {
			if ( isDelegating( klass ) ) {
				OWLClassExpression delegate = aliases.get( klass );
				supers.get( IRIUtils.iriOf( delegate ) ).addAll( getSuperClasses( klass, ontoDescr.importsClosure() ).collect( Collectors.toSet() ) );
			} else {
				Set<OWLClassExpression> sup = supers.get( IRIUtils.iriOf( klass ) );

				Set<OWLClassExpression> ancestors = new HashSet<>();
				for ( OWLSubClassOfAxiom subx : subConceptOfSet ) {
					if ( subx.getSubClass().equals( klass ) ) {
						ancestors.add( subx.getSuperClass() );
					}
				}

				sup.addAll(ancestors);
				for ( OWLClassExpression anc : ancestors ) {
					if ( reverseAliases.containsKey( anc ) ) {
						sup.addAll(reverseAliases.get(anc));
					}
				}
			}
		} );


		for ( Concept con : hierarchicalModel.getConcepts() ) {      //use concepts as they're sorted!

			if ( isDelegating( con.getIri() ) ) {
				continue;
			}

			LinkedList<OWLClassExpression> orderedSupers = new LinkedList<>();
			// cardinalities should be fixed last
			if ( supers.containsKey( con.getIri() ) ) {
				for ( OWLClassExpression sup : supers.get( con.getIri() ) ) {
					if ( sup instanceof OWLCardinalityRestriction ) {
						orderedSupers.addLast( sup );
					} else {
						orderedSupers.addFirst( sup );
					}
				}
			}

			for ( OWLClassExpression sup : orderedSupers ) {
				if ( sup.isClassExpressionLiteral() ) {
					continue;
				}

				processSuperClass( sup, con, hierarchicalModel, factory );
			}


//            for ( PropertyRelation prop : con.getProperties().values() ) {
//                if ( prop.isRestricted() && ( prop.getMaxCard() == null || prop.getMaxCard() > 1 ) ) {
//                    prop.setName( prop.getName() + "s" );
//                }
//            }
		}

		for ( PropertyRelation prop : hierarchicalModel.getProperties() ) {
			if ( prop.isAttribute() && ( prop.getDomain().isAbstrakt() || prop.getTarget().isAbstrakt() ) ) {
				prop.getDomain().removeProperty( prop.getProperty() );
				hierarchicalModel.removeProperty( prop );
			}
		}

	}

	private void processSuperClass( OWLClassExpression sup, Concept con, OntoModel hierarchicalModel, OWLDataFactory factory ) {
		PropertyRelation rel;
		Concept tgt;
		switch ( sup.getClassExpressionType() ) {
			case DATA_SOME_VALUES_FROM:
				//check that it's a subclass of the domain. Should have already been done, or it would be an inconsistency
				break;
			case DATA_ALL_VALUES_FROM:
				OWLDataAllValuesFrom forall = (OWLDataAllValuesFrom) sup;
				tgt = getDataRangeConcept( forall.getProperty(), forall.getFiller(), factory );
				rel = extractProperty( con, IRIUtils.iriOf( forall.getProperty() ), tgt, null, null, true );
				if ( rel != null ) {
					logger.info( "Processed Data property " + IRIUtils.iriOf( forall.getProperty() ) );
//                    hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( forall.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case DATA_MIN_CARDINALITY:
				OWLDataMinCardinality min = (OWLDataMinCardinality) sup;
				tgt = getDataRangeConcept( min.getProperty(), min.getFiller(), factory );
				rel = extractProperty( con, IRIUtils.iriOf( min.getProperty() ), tgt, min.getCardinality(), null, false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( min.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case DATA_MAX_CARDINALITY:
				OWLDataMaxCardinality max = (OWLDataMaxCardinality) sup;
				tgt = getDataRangeConcept( max.getProperty(), max.getFiller(), factory );
				rel = extractProperty( con, IRIUtils.iriOf( max.getProperty() ), tgt, null, max.getCardinality(), false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( max.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case DATA_EXACT_CARDINALITY:
				OWLDataExactCardinality ex = (OWLDataExactCardinality) sup;
				tgt = getDataRangeConcept( ex.getProperty(), ex.getFiller(), factory );
				rel = extractProperty( con, IRIUtils.iriOf( ex.getProperty() ), tgt, ex.getCardinality(), ex.getCardinality(), false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( ex.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_SOME_VALUES_FROM:
				OWLObjectSomeValuesFrom someO = (OWLObjectSomeValuesFrom) sup;
				if ( filterAliases( someO.getFiller() ).isAnonymous() ) {
					logger.warn( ": Complex unaliased restriction " + someO );
					break;
				}
				tgt = conceptCache.get( IRIUtils.iriOf( filterAliases( someO.getFiller() ) ) );
				rel = extractProperty( con, IRIUtils.iriOf( someO.getProperty() ), tgt, 1, null, false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( someO.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_ALL_VALUES_FROM:
				OWLObjectAllValuesFrom forallO = (OWLObjectAllValuesFrom) sup;
				if ( filterAliases( forallO.getFiller() ).isAnonymous() ) {
					logger.warn( ": Complex unaliased restriction " + forallO );
					break;
				}
				tgt = conceptCache.get( IRIUtils.iriOf( filterAliases( forallO.getFiller() ) ) );
				rel = extractProperty( con, IRIUtils.iriOf( forallO.getProperty() ), tgt, null, null, true );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( forallO.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_MIN_CARDINALITY:
				OWLObjectMinCardinality minO = (OWLObjectMinCardinality) sup;
				tgt = conceptCache.get( IRIUtils.iriOf( filterAliases( minO.getFiller() ) ) );
				rel = extractProperty( con, IRIUtils.iriOf( minO.getProperty() ), tgt, minO.getCardinality(), null, false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( minO.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_MAX_CARDINALITY:
				OWLObjectMaxCardinality maxO = (OWLObjectMaxCardinality) sup;
				tgt = conceptCache.get( IRIUtils.iriOf( filterAliases( maxO.getFiller() ) ) );
				rel = extractProperty( con, IRIUtils.iriOf( maxO.getProperty() ), tgt, null, maxO.getCardinality(), false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( maxO.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_EXACT_CARDINALITY:
				OWLObjectExactCardinality exO = (OWLObjectExactCardinality) sup;
				tgt = conceptCache.get( IRIUtils.iriOf( filterAliases( exO.getFiller() ) ) );
				rel = extractProperty( con, IRIUtils.iriOf( exO.getProperty() ), tgt, exO.getCardinality(), exO.getCardinality(), false );
				if ( rel != null ) {
					hierarchicalModel.addProperty( rel );
				} else {
					logger.warn( " Could not find property " + IRIUtils.iriOf( exO.getProperty() ) + " restricted in class " + con.getIri() );
				}
				break;
			case OBJECT_INTERSECTION_OF:
				OWLObjectIntersectionOf and = (OWLObjectIntersectionOf) sup;
				for ( OWLClassExpression arg : and.asConjunctSet() ) {
					processSuperClass( arg, con, hierarchicalModel, factory );
				}
				break;
			case OWL_CLASS:
				break;
			default:
				logger.warn( " Cannot handle " + sup );
		}

	}

	private Concept getDataRangeConcept( OWLDataPropertyExpression property, OWLDataRange range, OWLDataFactory factory ) {
		OWLDatatype datatype = dataRangeToDataType( range, factory );
		if ( datatype != null && primitives.containsKey( IRIUtils.iriOf( datatype ) ) ) {
			return primitives.get( IRIUtils.iriOf( datatype ) );
		} else if ( conceptCache.containsKey( IRIUtils.iriOf( datatype ) ) ) {
			return conceptCache.get( IRIUtils.iriOf( datatype ) );
		} else {
			return conceptCache.get( IRI.create( property.asOWLDataProperty().getIRI().getNamespace(),
			                                     NameUtils.capitalize( IRIUtils.fragmentOf( property.asOWLDataProperty().getIRI() ) ) + "DataRange" ).toQuotedString() );
		}
	}



	private PropertyRelation extractProperty( Concept con, String propIri, Concept target, Integer min, Integer max, boolean restrictTarget ) {
		if ( target == null || target.getName() == null ) {
			logger.error( "Null target for property " + propIri );
			return null;
		}

		String restrictedSuffix = createSuffix( target.getName(), true );
		String restrictedPropIri = propIri.replace( ">", restrictedSuffix + ">" );

		boolean alreadyRestricted = false;
		PropertyRelation rel = con.getProperties().get( propIri );
		PropertyRelation originalProperty;

		if ( rel != null ) {
			originalProperty = rel;
			rel = cloneRel( originalProperty );
		} else {
			rel = con.getProperties().get( restrictedPropIri );

			if ( rel != null ) {
				originalProperty = rel;
				alreadyRestricted = true;
				rel = cloneRel( originalProperty );
			} else {
				PropertyRelation source = inheritPropertyCopy( con, con, restrictedPropIri );

				if ( source != null ) {
					originalProperty = source;
					alreadyRestricted = true;
					rel = cloneRel( originalProperty );
				} else {
					originalProperty = inheritPropertyCopy( con, con, propIri );
					if ( originalProperty != null ) {
						rel = cloneRel( originalProperty );
					} else {
						IRI propIRI = propIri.startsWith( "<" )
								? IRI.create( propIri.substring( 1, propIri.indexOf( "#" ) ), propIri.substring( propIri.indexOf( "#" ) + 1, propIri.length() - 1 ) )
								: IRI.create( propIri );
						originalProperty = new PropertyRelation(
								con.getIri(),
								propIri,
								target.getIri(),
								IRIUtils.fragmentOf( propIRI )
						);
						originalProperty.setDomain( conceptCache.get( originalProperty.getSubject() ) );
						originalProperty.setTarget( conceptCache.get( originalProperty.getObject() ) );
						con.addProperty( propIri, originalProperty );
						rel = cloneRel( originalProperty );
					}
				}
			}
		}


		if ( rel != null ) {
			boolean tgtRestrictionApplied = restrictTarget && ! rel.getTarget().equals( target );
			if ( tgtRestrictionApplied ) {
				rel.restrictTargetTo( target );
			}

			boolean dirty = false;
			if ( target.getIri().equals( Thing.IRI )
					|| target.getIri().equals("<http://www.w3.org/2000/01/rdf-schema#Literal>" )
					|| target.getEquivalentConcepts().contains( conceptCache.get( Thing.IRI ) )
					) {
				target = rel.getTarget();
				restrictedSuffix = createSuffix( target.getName(), true );
				restrictedPropIri = propIri.replace ( ">", restrictedSuffix + ">" );
//                dirty = true;
			}
			if ( ! rel.getTarget().equals( target ) && ! restrictTarget ) {
				//TODO FIXME : check that target is really restrictive!
				if ( ! target.getIri().equals( Thing.IRI )
						&& ! target.getIri().equals("<http://www.w3.org/2000/01/rdf-schema#Literal>")
						) {
					rel.setTarget( target );
					rel.setObject( target.getIri() );
					dirty = true;
				}
			}
			if ( min != null && min > rel.getMinCard() ) {
				rel.setMinCard( min );
//                if ( min > 1 ) {
				dirty = true;
//                }
			}
			if ( max != null && ( rel.getMaxCard() == null || max < rel.getMaxCard() ) ) {
				rel.setMaxCard( max );
				if ( max == 1 ) {
					if ( alreadyRestricted ) {
						con.removeProperty( restrictedPropIri );
					}
					restrictedSuffix = createSuffix( target.getName(), false );
					restrictedPropIri = propIri.replace ( ">", restrictedSuffix + ">" );
				}
				dirty = true;
			}
			if ( rel.isAttribute() && ! rel.getTarget().equals( target ) ) {
				dirty = true;
				rel.setTarget( target );
			}
			if ( dirty ) {
				if ( ( target.isPrimitive() && rel.getDomain().equals( con ) ) || rel.isAttribute() ) {
					if ( rel.getMaxCard() != null && rel.getMaxCard() <= 1 ) {
						rel.setSimple( true );
					}
				} else {
					rel.setRestricted( true );
					rel.setName( originalProperty.getBaseProperty().getName() + restrictedSuffix );
					rel.setProperty( restrictedPropIri );
				}
			}

			rel.setSubject( con.getIri() );
			rel.setDomain( con );

			for ( Concept sup : con.getSuperConcepts() ) {
				if ( sup.getEffectiveProperties().contains( rel ) ) {
					rel.setInherited( true );
					break;
				}
			}

			if ( ! rel.mirrors( rel.getBaseProperty() ) ) {
				con.addProperty( rel.getProperty(), rel );
			}


			if ( dirty ) {
				rel.setBaseProperty( originalProperty );
			} else {
				if ( logger.isDebugEnabled() ) { logger.debug( "No real restriction detected II "+ rel.getName() ); }
				rel = originalProperty;
			}

			rel.getDomain().addProperty( rel.getProperty(), rel );

			return rel;

		} else {
			if ( logger.isDebugEnabled() ) { logger.debug( "No real restriction detected" ); }
			// rel is null and that is what will be returned
		}

		return null;
	}




	private PropertyRelation inheritPropertyCopy( Concept original, Concept current, String propIri ) {
		PropertyRelation rel;
		if ( original != null ) {
			for ( Concept sup : current.getSuperConcepts() ) {
//            String key = propIri.replace( "As"+current.getName(), "As"+sup.getName() );
				rel = sup.getProperties().get( propIri );
				if ( rel != null ) {
					return rel;
				} else {
					rel = inheritPropertyCopy( original, sup, propIri );
					if ( rel != null ) {
						return rel;
					}
				}
			}
		}
		return null;
	}

	private PropertyRelation cloneRel( PropertyRelation rel ) {
		PropertyRelation clonedRel = new PropertyRelation( rel.getSubject(), rel.getProperty(), rel.getObject(), rel.getName() );
		clonedRel.setMinCard( rel.getMinCard() );
		clonedRel.setMaxCard( rel.getMaxCard() );
		clonedRel.setTarget( rel.getTarget() );
		clonedRel.setDomain( rel.getDomain() );
		clonedRel.setRestricted( rel.isRestricted() );
		clonedRel.setFunctional( rel.isFunctional() );
		clonedRel.setSimple( rel.isSimple() );
		clonedRel.setAttribute( rel.isAttribute() );
		clonedRel.setInverse( rel.getInverse() );
		return clonedRel;

	}


	private void createAndAddBasicProperties( OWLOntology ontoDescr, OWLDataFactory factory, OntoModel hierarchicalModel ) {
		final List<OWLProperty> missDomain = new ArrayList<>();
		final List<OWLDataProperty> missDataRange = new ArrayList<>();
		final List<OWLObjectProperty> missObjectRange = new ArrayList<>();

		ontoDescr.dataPropertiesInSignature( Imports.INCLUDED ).forEach( (dp) -> {
			if ( ! dp.isOWLTopDataProperty() && ! dp.isOWLBottomDataProperty() ) {
				Set<OWLClassExpression> domains = getDomains( dp, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
				if ( domains.isEmpty() ) {
					domains.add( factory.getOWLThing() );
					logger.warn( "Added missing domain for" + dp);
					missDomain.add( dp );
				}
				Set<OWLDataRange> ranges = getRanges( dp, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
				if ( ranges.isEmpty() ) {
					ranges.add( factory.getRDFPlainLiteral() );
					logger.warn( "Added missing range for" + dp);
					missDataRange.add( dp );
				}

				for ( OWLClassExpression domain : domains ) {
					for ( OWLDataRange range : ranges ) {
						OWLClassExpression realDom = filterAliases( domain );
						OWLDatatype dataRange = dataRangeToDataType( range, factory );

						Concept con;
						PropertyRelation rel;

						if ( dataRange != null && primitives.containsKey( IRIUtils.iriOf( dataRange ) ) ) {
							rel = new PropertyRelation( IRIUtils.iriOf( realDom ),
							                            IRIUtils.iriOf( dp ),
							                            IRIUtils.iriOf( dataRange ),
							                            props.get( IRIUtils.iriOf( dp ) ) );
							con = conceptCache.get( rel.getSubject() );
							rel.setTarget( primitives.get( rel.getObject() ) );
							rel.setFunctional( ontoDescr.functionalDataPropertyAxioms( dp ).findFirst().isPresent() );
						} else {
							Concept dRangeCon = createConceptForComplexDatatype( range, dp, hierarchicalModel );

							// no datarange -> complex data type, let's try to rebuild as object property
							rel = new PropertyRelation( IRIUtils.iriOf( realDom ),
							                            IRIUtils.iriOf( dp ),
							                            dRangeCon.getIri(),
							                            props.get( IRIUtils.iriOf( dp ) ) );
							con = conceptCache.get( rel.getSubject() );
							rel.setTarget( conceptCache.get( rel.getObject() ) );
							rel.setFunctional( ontoDescr.functionalDataPropertyAxioms( dp ).findAny().isPresent() );
						}

						rel.setDomain( con );
						con.addProperty( rel.getProperty(), rel );
						hierarchicalModel.addProperty( rel );
						rel.resolve();
					}
				}
			}
		});


		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).forEach( ( op) -> {
			if ( ! op.isOWLTopObjectProperty() && ! op.isOWLBottomObjectProperty() ) {
				Set<OWLClassExpression> domains = getDomains( op, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
				if ( domains.isEmpty() ) {
					domains.add( factory.getOWLThing() );
					logger.warn("Added missing domain for " + op);
					missDomain.add( op );
				}
				Set<OWLClassExpression> ranges = getRanges( op, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
				if ( ranges.isEmpty() ) {
					ranges.add( factory.getOWLThing() );
					logger.warn("Added missing range for " + op);
					missObjectRange.add( op );
				}

				for ( OWLClassExpression domain : domains ) {
					for ( OWLClassExpression range : ranges ) {
						OWLClassExpression realDom = filterAliases( domain );
						OWLClassExpression realRan = filterAliases( range );

						if ( realDom.isAnonymous() || realRan.isAnonymous() ) {
							logger.error("Domain and Range should no be anonymous at this point : DOM " + realDom + " RAN : " + range);
						}
						PropertyRelation rel = new PropertyRelation( IRIUtils.iriOf( realDom ),
						                                             IRIUtils.iriOf( op ),
						                                             IRIUtils.iriOf( realRan ),
						                                             props.get( IRIUtils.iriOf( op ) ) );

						Concept con = conceptCache.get( rel.getSubject() );
						rel.setTarget( conceptCache.get( rel.getObject() ) );
						rel.setDomain( con );
						rel.setAttribute( Annotations.hasAnnotation( op, Annotations.ATTRIBUTE, ontoDescr ) );
						rel = hierarchicalModel.addProperty( rel );
						con.addProperty( rel.getProperty(), rel );
						rel.resolve();

					}
				}
			}
		});

		if ( logger.isInfoEnabled() ) {
			logger.info( "Misses : ");
			logger.info( missDomain );
			logger.info( missDataRange );
			logger.info( missObjectRange );
		}
	}

	private Concept createConceptForComplexDatatype( OWLDataRange dataRange, OWLDataProperty prop, OntoModel model ) {
		// processing a complex data range
		// it is not possible to do this earlier, since datatypes do not have inheritance.
		// so, we create a Concept late in the generation process, which will be mapped to an intf/class
		IRI iri = IRI.create( prop.getIRI().getNamespace(), NameUtils.capitalize( IRIUtils.fragmentOf( prop.getIRI() ) ) + "DataRange" );
		Concept rangeCon = new Concept( iri,
		                                model.getPackageNameMappings(),
		                                NameUtils.capitalize( IRIUtils.fragmentOf( iri ) ),
		                                false );
		if ( dataRange instanceof OWLDataUnionOf ) {
			rangeCon.addSuperConcept( conceptCache.get( Thing.IRI ) );
			OWLDataUnionOf.class.cast( dataRange ).operands()
			                    .filter( (member) -> ! primitives.containsKey( IRIUtils.iriOf( member ) ) )
			                    .map( OWLDatatype.class::cast )
			                    .forEach( (datatype) -> {
				                    Concept complexDT = new Concept( datatype.getIRI(),
				                                                     model.getPackageNameMappings(),
				                                                     IRIUtils.fragmentOf( datatype.getIRI() ),
				                                                     false );
				                    complexDT.addSuperConcept( rangeCon );
				                    conceptCache.put( IRIUtils.iriOf( datatype ), complexDT );
				                    model.addConcept( complexDT );
			                    });
		} else if ( dataRange instanceof OWLDataIntersectionOf ) {
			OWLDataIntersectionOf.class.cast( dataRange ).operands()
			                           .filter( (member) -> ! primitives.containsKey( IRIUtils.iriOf( member ) ) )
			                           .map( OWLDatatype.class::cast )
			                           .forEach( (datatype) -> {
				                           Concept complexDT = new Concept( datatype.getIRI(),
				                                                            model.getPackageNameMappings(),
				                                                            IRIUtils.fragmentOf( datatype.getIRI() ),
				                                                            false );
				                           rangeCon.addSuperConcept( complexDT );
				                           complexDT.addSuperConcept( conceptCache.get( Thing.IRI ) );
				                           conceptCache.put( IRIUtils.iriOf( datatype ), complexDT );
				                           model.addConcept( complexDT );
			                           });

		}

		this.conceptCache.put( iri.toQuotedString(), rangeCon );
		rangeCon.setAnonymous( true );
		model.addConcept( rangeCon );
		return rangeCon;
	}

	private OWLDatatype dataRangeToDataType( OWLDataRange range, OWLDataFactory factory ) {
		if ( range.isOWLDatatype() ) {
			return range.asOWLDatatype();
		}
		if ( range instanceof OWLDataOneOf ) {
			OWLDataOneOf oneOf = (OWLDataOneOf) range;
			return oneOf.values()
			            .distinct()
			            .map( OWLLiteral::getDatatype )
			            .reduce( (acc, el) -> acc.equals( el ) ? acc : factory.getTopDatatype() )
			            .orElse( factory.getTopDatatype() );
		}
		return factory.getTopDatatype();
	}


	private Map<OWLClassExpression,OWLClassExpression> buildAliasesForEquivalentClasses(OWLOntology ontoDescr) {
		Map<OWLClassExpression,OWLClassExpression> aliases = new HashMap<>();
		Set<OWLEquivalentClassesAxiom> pool = new HashSet<>();
		Set<OWLEquivalentClassesAxiom> temp = new HashSet<>();


		ontoDescr.classesInSignature( Imports.INCLUDED )
		         .flatMap( ontoDescr::equivalentClassesAxioms )
		         .map( OWLEquivalentClassesAxiom::asPairwiseAxioms )
		         .forEach( pool::addAll );

		boolean stable = false;
		while ( ! stable ) {
			stable = true;
			temp.addAll( pool );
			for ( OWLEquivalentClassesAxiom eq2 : pool ) {

				List<OWLClassExpression> pair = eq2.classExpressions().collect( Collectors.toList() );
				OWLClassExpression first = pair.get( 0 );
				OWLClassExpression secnd = pair.get( 1 );
				OWLClassExpression removed;

				if ( aliases.containsValue(first) ) {
					// add to existing eqSet, put reversed
					// A->X,  add X->C          ==> A->X, C->X
					removed = aliases.put( secnd, first );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				} else if ( aliases.containsValue(secnd) ) {
					// add to existing eqSet, put as is
					// A->X,  add C->X          ==> A->X, C->X
					removed = aliases.put( first, secnd );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				} else if ( aliases.containsKey(first) ) {
					// apply transitivity, reversed
					// A->X,  add A->C          ==> A->X, C->X
					removed = aliases.put( secnd, aliases.get( first ) );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				} else if ( aliases.containsKey(secnd) ) {
					// apply transitivity, as is
					// A->X,  add C->A          ==> A->X, C->X
					removed = aliases.put( first, aliases.get( secnd ) );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				} else if ( ! first.isAnonymous() ) {
					removed = aliases.put( secnd, first );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				} else if ( ! secnd.isAnonymous() ) {
					removed = aliases.put( first, secnd );
					if ( removed != null ) {
						logger.warn("DUPLICATE KEY WHILE RESOLVING EQUALITIES" + removed + " for value " + eq2);
					}

					stable = false;
					temp.remove( eq2 );
				}
				// else both anonymous

			}
			pool.clear();
			pool.addAll(temp);
		}

		if ( ! pool.isEmpty() ) {
			logger.error("COULD NOT RESOLVE ANON=ANON EQUALITIES " + pool);
			for ( OWLEquivalentClassesAxiom eq2 : pool ) {
				List<OWLClassExpression> l = eq2.classExpressions().collect( Collectors.toList() );
				if ( ! (l.get(0).isAnonymous() && l.get(1).isAnonymous())) {
					logger.error(" EQUALITY WAS NOT RESOLVED " + l);
				}
			}
		}


		if ( logger.isInfoEnabled() ) {
			logger.info("----------------------------------------------------------------------- " + aliases.size());
			for( Map.Entry<OWLClassExpression,OWLClassExpression> entry : aliases.entrySet() ) {
				logger.trace(entry.getKey() + " == " + entry.getValue());
			}
			logger.info("-----------------------------------------------------------------------");
		}


		for ( Map.Entry<OWLClassExpression,OWLClassExpression> entry : aliases.entrySet() ) {
			OWLClassExpression key = entry.getKey();
			OWLClassExpression val = entry.getValue();
			if ( ! reverseAliases.containsKey( val ) ) {
				reverseAliases.put( val, new HashSet<>() );
			}
			reverseAliases.get( val ).add( key );
		}
		return aliases;
	}


	private boolean processComplexSuperClassesDomainAndRanges(OWLOntology ontoDescr, OWLDataFactory factory) {
		boolean dirty;
		dirty = processComplexSuperclasses( ontoDescr, factory );
		dirty |= processComplexDataPropertyDomains( ontoDescr, factory );
		dirty |= processComplexObjectPropertyDomains( ontoDescr, factory );
		dirty |= processComplexObjectPropertyRanges( ontoDescr, factory );
		return dirty;
	}


	private boolean processComplexSuperclasses( final OWLOntology ontoDescr, final OWLDataFactory factory ) {
		Set<OWLClass> newClasses = new HashSet<>();
		ontoDescr.axioms( AxiomType.SUBCLASS_OF, Imports.INCLUDED ).forEach( (sub) -> {

			if ( sub.getSuperClass().isAnonymous() ) {
				if ( sub.getSuperClass() instanceof OWLObjectUnionOf ) {
					Set<OWLClassExpression> dis = sub.getSuperClass().asDisjunctSet();
					List<OWLClassExpression> disjunctList = new ArrayList<>( dis );
					disjunctList.sort( Comparator.comparing( o -> asNamedClass( o, factory, ontoDescr ) ) );

					Iterator<OWLClassExpression> disjuncts = disjunctList.iterator();

					StringBuilder orNames = new StringBuilder( IRIUtils.fragmentOf( asNamedClass( disjuncts.next(), factory, ontoDescr ).getIRI() ) );
					while ( disjuncts.hasNext() ) {
						OWLClass disjunct = asNamedClass( disjuncts.next(), factory, ontoDescr );
						orNames.append( "or" ).append( IRIUtils.fragmentOf( disjunct.asOWLClass().getIRI() ) );
					}

					OWLClass unjon = factory.getOWLClass( IRI.create(
							NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) + orNames ) );

					applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( unjon ) ) );
					applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( sub.getSuperClass(), unjon ) ) );
					newClasses.add( unjon );
				}
			}
		});
		return ! newClasses.isEmpty();
	}

	private OWLClass asNamedClass( OWLClassExpression owlClass, OWLDataFactory factory, OWLOntology ontoDescr ) {
		if ( ! owlClass.isAnonymous() ) {
			return owlClass.asOWLClass();
		}

		if ( anonNameAliases.containsKey( owlClass ) ) {
			return anonNameAliases.get( owlClass );
		}

		OWLClass alias = factory.getOWLClass( IRI.create(
				NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) + "Anon" + counter++ ) );

		applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( alias ) ) );
		applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( owlClass, alias ) ) );

		anonNameAliases.put( owlClass, alias );
		return alias;
	}



	private boolean processComplexObjectPropertyDomains( OWLOntology ontoDescr, OWLDataFactory factory ) {
		final Set<OWLObjectProperty> props = new HashSet<>();
		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).forEach( (op) -> {

			String typeName = NameUtils.buildNameFromIri( op.getIRI().getNamespace(), IRIUtils.fragmentOf( op.getIRI() ) );

			Set<OWLClassExpression> domainClasses = getDomains( op, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
			if ( domainClasses.size() > 1 ) {
				OWLObjectIntersectionOf and = factory.getOWLObjectIntersectionOf( domainClasses );

				ontoDescr.objectPropertyDomainAxioms( op ).forEach( (dom) ->
						                                                    applyAxiom( ontoDescr, new RemoveAxiom( ontoDescr, dom ) ) );
				applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( op, and ) ) );

				props.add( op );
			}

			if ( getDomains( op, ontoDescr.importsClosure() ).count() > 1 ) {
				logger.warn( "Property " + op + " should have a single domain class, found " + getDomains( op, ontoDescr ).collect( Collectors.toList() ) );
			}

			getDomains( op, ontoDescr.importsClosure() )
					.filter( IsAnonymous::isAnonymous )
					.forEach( (dom) -> {
						OWLClass domain = factory.getOWLClass( IRI.create(
								NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) +
										NameUtils.capitalize( typeName ) +
										"Domain" ) );

						OWLAnnotationAssertionAxiom ann = factory.getOWLAnnotationAssertionAxiom( factory.getOWLAnnotationProperty( IRI.create( "http://www.w3.org/2000/01/rdf-schema#comment" ) ),
						                                                                          domain.getIRI(),
						                                                                          factory.getOWLLiteral( "abstract", OWL2Datatype.XSD_STRING ) );

						if ( logger.isDebugEnabled() ) { logger.debug("REPLACED ANON DOMAIN " + op + " with " + domain + ", was " + dom); }

						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( domain ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( domain, dom ) ) );

						OWLOntology defining = lookupDefiningOntology( ontoDescr, op );

						applyAxiom( ontoDescr, new RemoveAxiom( defining, defining.objectPropertyDomainAxioms( op )
						                                                          .findFirst()
						                                                          .orElseThrow( IllegalStateException::new ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( op, domain ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, ann ) );

						props.add( op );
					});

		} );
		return ! props.isEmpty();
	}

	private boolean processComplexDataPropertyDomains(OWLOntology ontoDescr, OWLDataFactory factory ) {

		final Set<OWLDataProperty> props = new HashSet<>();
		ontoDescr.dataPropertiesInSignature( Imports.INCLUDED ).forEach( (dp) -> {

			String typeName = NameUtils.buildNameFromIri( dp.getIRI().getNamespace(), IRIUtils.fragmentOf( dp.getIRI() ) );

			Set<OWLClassExpression> domainClasses = getDomains( dp, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
			if ( domainClasses.size() > 1 ) {
				OWLObjectIntersectionOf and = factory.getOWLObjectIntersectionOf( domainClasses );

				ontoDescr.dataPropertyDomainAxioms( dp ).forEach( (dom) ->
						                                                  applyAxiom( ontoDescr, new RemoveAxiom( ontoDescr, dom ) ) );
				applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDataPropertyDomainAxiom( dp, and ) ) );

				props.add( dp );
			}

			if ( getDomains( dp, ontoDescr.importsClosure() ).count() > 1 ) {
				logger.warn( "Prdperty " + dp + " should have a single domain class, found " + getDomains( dp, ontoDescr ).collect( Collectors.toList() ) );
			}

			getDomains( dp, ontoDescr.importsClosure() )
					.filter( IsAnonymous::isAnonymous )
					.forEach( (dom) -> {
						OWLClass domain = factory.getOWLClass( IRI.create(
								NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) +
										NameUtils.capitalize( typeName ) +
										"Domain" ) );

						OWLAnnotationAssertionAxiom ann = factory.getOWLAnnotationAssertionAxiom( factory.getOWLAnnotationProperty( IRI.create( "http://www.w3.org/2000/01/rdf-schema#comment" ) ),
						                                                                          domain.getIRI(),
						                                                                          factory.getOWLLiteral( "abstract", OWL2Datatype.XSD_STRING ) );

						if ( logger.isDebugEnabled() ) { logger.debug("REPLACED ANON DOMAIN " + dp + " with " + domain + ", was " + dom); }

						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( domain ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( domain, dom ) ) );

						OWLOntology defining = lookupDefiningOntology( ontoDescr, dp );

						applyAxiom( defining, new RemoveAxiom( defining, defining.dataPropertyDomainAxioms( dp )
						                                                         .findFirst()
						                                                         .orElseThrow( IllegalStateException::new ) ) );
						applyAxiom( defining, new AddAxiom( defining, factory.getOWLDataPropertyDomainAxiom(dp, domain) ) );
						applyAxiom( defining, new AddAxiom( defining, ann ) );

						props.add( dp );
					});

		} );
		return ! props.isEmpty();

	}


	private boolean processComplexObjectPropertyRanges( OWLOntology ontoDescr, OWLDataFactory factory ) {
		final Set<OWLObjectProperty> props = new HashSet<>();
		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).forEach( (op) -> {

			String typeName = NameUtils.buildNameFromIri( op.getIRI().getNamespace(), IRIUtils.fragmentOf( op.getIRI() ) );

			Set<OWLClassExpression> rangeClasses = getRanges( op, ontoDescr.importsClosure() ).collect( Collectors.toSet() );
			if ( rangeClasses.size() > 1 ) {
				OWLObjectIntersectionOf and = factory.getOWLObjectIntersectionOf( rangeClasses );

				ontoDescr.objectPropertyRangeAxioms( op ).forEach( ( ran ) ->
						                                                   applyAxiom( ontoDescr, new RemoveAxiom( ontoDescr, ran ) ) );
				applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLObjectPropertyRangeAxiom( op, and ) ) );

				props.add( op );
			}

			if ( getRanges( op, ontoDescr.importsClosure() ).count() > 1 ) {
				logger.warn( "Property " + op + " should have a single range class, found " + getRanges( op, ontoDescr ).collect( Collectors.toList() ) );
			}


			getRanges( op, ontoDescr.importsClosure() )
					.filter( IsAnonymous::isAnonymous )
					.forEach( (ran) -> {
						OWLClass range = factory.getOWLClass( IRI.create(
								NameUtils.separatingName( IRIUtils.ontologyNamespace( ontoDescr ) ) +
										NameUtils.capitalize( typeName ) +
										"Range" ) );

						OWLAnnotationAssertionAxiom ann = factory.getOWLAnnotationAssertionAxiom( factory.getOWLAnnotationProperty( IRI.create( "http://www.w3.org/2000/01/rdf-schema#comment" ) ),
						                                                                          range.getIRI(),
						                                                                          factory.getOWLLiteral( "abstract", OWL2Datatype.XSD_STRING ) );

						if ( logger.isDebugEnabled() ) { logger.debug("REPLACED ANON RANGE " + op + " with " + range + ", was " + ran ); }

						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( range ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( range, ran ) ) );

						OWLOntology defining = lookupDefiningOntology( ontoDescr, op );

						applyAxiom( ontoDescr, new RemoveAxiom( defining, defining.objectPropertyRangeAxioms( op )
						                                                          .findFirst()
						                                                          .orElseThrow( IllegalStateException::new ) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLObjectPropertyRangeAxiom( op, range) ) );
						applyAxiom( ontoDescr, new AddAxiom( ontoDescr, ann ) );

						props.add( op );
					});
		});
		return ! props.isEmpty();
	}


	public static OWLOntology lookupDefiningOntology( final OWLOntology ontoDescr, final OWLEntity axiom ) {
		return ontoDescr.importsClosure()
		                .reduce( ontoDescr, (od, imp) ->
				                imp.axioms( Imports.EXCLUDED )
				                   .anyMatch( ( ax) -> ax.equals( axiom ) ) ? imp : od );
	}


	private boolean preProcessIndividuals( OWLOntology ontoDescr, OWLDataFactory factory ) {
		Set<OWLNamedIndividual> inds = new HashSet<>();
		ontoDescr.individualsInSignature( Imports.INCLUDED ).forEach( (ind) ->  {

			OWLOntology defining = lookupDefiningOntology( ontoDescr, ind );

			declareAnonymousIndividualSupertypes( defining, factory, ind );

			if ( logger.isInfoEnabled() ) {
				logger.info( "Defining ontology :  " + defining );
			}
			if ( logger.isTraceEnabled() ) {
				defining.axioms( AxiomType.CLASS_ASSERTION ).forEach( logger::trace );
			}
			if ( logger.isInfoEnabled() ) {
				logger.info( "Getting types for individual " + ind.getIRI() );
			}

			Set<OWLClassExpression> types = getTypes( ind, defining ).collect( Collectors.toSet() );

			if ( logger.isInfoEnabled() ) {
				logger.info( "Found types in defining ontology" + types );
			}

			types = simplify( types, defining );
			if ( types.size() > 1 ) {
				inds.add( ind );

				OWLObjectIntersectionOf and = factory.getOWLObjectIntersectionOf( types );

				if ( logger.isDebugEnabled() ) {
					logger.debug( " Individual " + ind + " got a new combined type " + and );
				}

				OWLClass type = factory.getOWLClass( IRI.create( ind.getIRI().getNamespace() + NameUtils.compactUpperCase( IRIUtils.fragmentOf( ind.getIRI() ) ) + "Type" ) );

				applyAxiom( defining, new AddAxiom( defining, factory.getOWLDeclarationAxiom( type ) ) );
				applyAxiom( defining, new AddAxiom( defining, factory.getOWLSubClassOfAxiom( type, and ) ) );
				applyAxiom( defining, new AddAxiom( defining, factory.getOWLClassAssertionAxiom( type, ind ) ) );

				individualTypesCache.put( IRIUtils.iriOf( ind ), IRIUtils.iriOf( type ) );
			} else if ( types.isEmpty() ) {
				if ( logger.isDebugEnabled() ) {
					logger.debug( "WARNING no type detected for individual " + IRIUtils.iriOf( ind ) );
				}
			} else {
				individualTypesCache.put( IRIUtils.iriOf( ind ), IRIUtils.iriOf( types.iterator().next() ) );
			}

		});
		return ! inds.isEmpty();
	}


	private void declareAnonymousIndividualSupertypes( OWLOntology ontoDescr, OWLDataFactory factory, OWLNamedIndividual ind ) {
		List<OWLClassExpression> types = getTypes( ind, ontoDescr.importsClosure() ).collect( Collectors.toList() );
		types.stream()
		     .filter( IsAnonymous::isAnonymous )
		     .forEach( (type) -> {
			     OWLClass temp = factory.getOWLClass( IRI.create( ind.getIRI().getNamespace() + NameUtils.compactUpperCase( IRIUtils.fragmentOf( ind.getIRI() ) + "RestrictedType" + types.indexOf( type ) ) ) );
			     applyAxiom( ontoDescr, new RemoveAxiom( ontoDescr, factory.getOWLClassAssertionAxiom( type, ind ) ) );
			     applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( temp ) ) );
			     applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom(temp, type) ) );
			     applyAxiom( ontoDescr, new AddAxiom( ontoDescr, factory.getOWLClassAssertionAxiom( temp, ind ) ) );
		     });
	}


	private Set<OWLClassExpression> simplify( Set<OWLClassExpression> types, OWLOntology ontoDescr ) {
		if ( types.size() == 1 ) {
			return types;
		}
		Set<OWLClassExpression> ans = new HashSet<>( types );
		for ( OWLClassExpression klass1 : types ) {
			for ( OWLClassExpression klass2 : types ) {
				if ( isSuperClass( ontoDescr, klass1, klass2 ) ) {
					ans.remove( klass1 );
				}
			}
		}
		return ans;
	}


	public static boolean isSuperClass( OWLOntology ontoDescr, OWLClassExpression klass1, OWLClassExpression klass2 ) {
		return ontoDescr.importsClosure()
		                .anyMatch( (onto) ->
				                           onto.subClassAxiomsForSuperClass( klass1.asOWLClass() )
				                               .map( OWLSubClassOfAxiom::getSubClass )
				                               .anyMatch( (sub) -> sub.equals( klass2 )
						                               || (! sub.equals( klass2 ) && isSuperClass( onto, sub, klass2 ) ) ) );

	}


	public static boolean isSuperClass( OWLOntology ontoDescr, Set<OWLClassExpression> supers, OWLClassExpression klass2 ) {
		OWLClass k = klass2.asOWLClass();
		OWLOntology defining = lookupDefiningOntology( ontoDescr, k );
		return defining.subClassAxiomsForSubClass( k )
		               .map( OWLSubClassOfAxiom::getSuperClass )
		               .anyMatch( (sup)->
			supers.contains( sup )
					|| ( ! sup.isAnonymous() && isSuperClass( ontoDescr, supers, sup ) ) );
	}


	private boolean processQuantifiedRestrictions( OWLOntology ontoDescr ) {
		Set<OWLClassExpression> classes = new HashSet<>();

		// infer domain / range from quantified restrictions...
		ontoDescr.classesInSignature( Imports.INCLUDED ).forEach( (klass) -> {

			getSuperClasses( klass, ontoDescr.importsClosure() ).forEach( (clax) -> {
				if ( processQuantifiedRestrictionsInClass( klass, clax.getNNF(), ontoDescr ) ) {
					classes.add( clax );
				}
			});

			getEquivalentClasses( klass, ontoDescr.importsClosure() )
					.filter( (def) -> ! klass.equals( def ) )
					.forEach( (clax) -> {
				if ( processQuantifiedRestrictionsInClass( klass, clax.getNNF(), ontoDescr ) ) {
					classes.add( clax );
				}
			});

		});

		return ! classes.isEmpty();
	}

	private boolean processQuantifiedRestrictionsInClass(OWLClass klass, OWLClassExpression clax, OWLOntology ontology ) {

		RestrictionInferencingVisitor visitor = new RestrictionInferencingVisitor( klass, ontology );
		clax.accept( visitor );

		return visitor.isDirty();
	}


	private void fillPropNamesInDataStructs( OWLOntology ontoDescr ) {
		ontoDescr.dataPropertiesInSignature( Imports.INCLUDED )
		         .filter( (dp) -> ! dp.isTopEntity() && ! dp.isBottomEntity() )
		         .forEach( (dp) -> {
			         String propName = NameUtils.buildLowCaseNameFromIri( IRIUtils.fragmentOf( dp.getIRI() ) );
			         props.put( IRIUtils.iriOf( dp ), propName );
		         });

		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED )
		         .filter( (op) -> ! op.isTopEntity() && ! op.isBottomEntity() )
		         .forEach( (op) -> {
			         String propName = NameUtils.buildLowCaseNameFromIri( IRIUtils.fragmentOf( op.getIRI() ) );
			         props.put( IRIUtils.iriOf( op ), propName );
		         });
	}


	private String reportStats( OWLOntology ontoDescr ) {
		StringBuilder sb = new StringBuilder();

		sb.append( " *** Stats for ontology  : " ).append( IRIUtils.ontologyNamespace( ontoDescr ) );

		sb.append( " Number of classes " ).append( ontoDescr.classesInSignature( Imports.INCLUDED ).count() );

		sb.append( " Number of datatypes " ).append( ontoDescr.datatypesInSignature( Imports.INCLUDED ).count() );

		sb.append( " Number of dataProps " ).append( ontoDescr.dataPropertiesInSignature( Imports.INCLUDED ).count() );
		sb.append( "\t Number of dataProp domains " ).append( ontoDescr.getAxiomCount( AxiomType.DATA_PROPERTY_DOMAIN ) );
		ontoDescr.dataPropertiesInSignature( Imports.INCLUDED ).forEach( (p) -> {
			int num = (int) ontoDescr.dataPropertyDomainAxioms( p ).count();
			switch ( num ) {
				case 0:
					break;
				case 1:
					OWLDataPropertyDomainAxiom dom = ontoDescr.dataPropertyDomainAxioms( p ).iterator().next();
					if ( ! ( dom.getDomain() instanceof OWLClass ) ) {
						sb.append( "\t\t Complex Domain" ).append( p ).append( " --> " ).append( dom.getDomain() );
					}
					break;
				default:
					sb.append( "\t\t Domain" ).append( p ).append( " --> " ).append( num );
			}
		});

		sb.append( "\t Number of dataProp ranges " ).append( ontoDescr.getAxiomCount( AxiomType.DATA_PROPERTY_RANGE ) );
		ontoDescr.dataPropertiesInSignature( Imports.INCLUDED ).forEach( (p) -> {
			int num = ( int ) ontoDescr.dataPropertyRangeAxioms( p ).count();
			switch ( num ) {
				case 0:
					break;
				case 1:
					OWLDataPropertyRangeAxiom range = ontoDescr.dataPropertyRangeAxioms( p ).iterator().next();
					if ( ! ( range.getRange() instanceof OWLDatatype ) ) {
						sb.append( "\t\t Complex Range" ).append( p ).append( " --> " ).append( range.getRange() );
					}
					break;
				default:
					sb.append( "\t\t Range" ).append( p ).append( " --> " ).append( num );
			}
		});

		sb.append( " Number of objProps " ).append( ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).count() );
		sb.append( "\t Number of objProp domains " ).append( ontoDescr.getAxiomCount( AxiomType.OBJECT_PROPERTY_DOMAIN ) );
		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).forEach(  (p) -> {
			int num = ( int ) ontoDescr.objectPropertyDomainAxioms( p ).count();
			switch ( num ) {
				case 0:
					break;
				case 1:
					OWLObjectPropertyDomainAxiom dom = ontoDescr.objectPropertyDomainAxioms( p ).iterator().next();
					if ( ! ( dom.getDomain() instanceof OWLClass ) ) {
						sb.append( "\t\t Complex Domain" ).append( p ).append( " --> " ).append( dom.getDomain() );
					}
					break;
				default:
					sb.append( "\t\t Domain" ).append( p ).append( " --> " ).append( num );
			}
		});

		sb.append( "\t Number of objProp ranges " ).append( ontoDescr.getAxiomCount( AxiomType.OBJECT_PROPERTY_RANGE ) );
		ontoDescr.objectPropertiesInSignature( Imports.INCLUDED ).forEach( (p) -> {
			int num = ( int ) ontoDescr.objectPropertyRangeAxioms( p ).count();
			switch ( num ) {
				case 0:
					break;
				case 1:
					OWLObjectPropertyRangeAxiom range = ontoDescr.objectPropertyRangeAxioms( p ).iterator().next();
					if ( ! ( range.getRange() instanceof OWLClass ) ) {
						sb.append( "\t\t Complex Domain" ).append( p ).append( " --> " ).append( range.getRange() );
					}
					break;
				default:
					sb.append( "\t\t Range" ).append( p ).append( " --> " ).append( num );
			}
		});

		return sb.toString();
	}



	protected OntoModel buildClassLattice( OWLOntology ontoDescr,
	                                       Map<InferenceTask,
			                                       Resource> theory,
	                                       OntoModel baseModel,
	                                       DLFactoryConfiguration conf ) {

		boolean dirty;
		OWLDataFactory factory = ontoDescr.getOWLOntologyManager().getOWLDataFactory();

		launchReasoner( true, ontoDescr, conf.getAxiomGens() );

		// reify complex superclasses, domains and ranges
		dirty = processComplexSuperClassesDomainAndRanges( ontoDescr, factory );

		//************************************************************************************************************************************/

		// check individuals for multiple inheritance
		dirty |= preProcessIndividuals( ontoDescr, ontoDescr.getOWLOntologyManager().getOWLDataFactory() );

		//************************************************************************************************************************************/

		// new classes have been added, classify the
		launchReasoner( dirty, ontoDescr, conf.getAxiomGens() );

		//************************************************************************************************************************************/

		// reify complex restriction fillers
		dirty = processQuantifiedRestrictions( ontoDescr );

		//************************************************************************************************************************************

		// new classes have been added, classify the
		if ( ! conf.isDisableFullReasoner() ) {
			launchReasoner( dirty, ontoDescr, conf.getAxiomGens() );
		}

		//************************************************************************************************************************************

		// resolve aliases, choosing delegators
		aliases = buildAliasesForEquivalentClasses( ontoDescr );

		//************************************************************************************************************************************

		if ( logger.isInfoEnabled() ) {
			logger.info( reportStats( ontoDescr ) );
		}


		// classes are stable now
		addConceptsToModel( ontoDescr, baseModel );

		// lattice is too. applies aliasing
		addSubConceptsToModel( ontoDescr, baseModel );

		return baseModel;
	}


	private void fixRootHierarchy(OntoModel model) {
		Concept thing = model.getConcept( Thing.IRI );
		if ( thing.getProperties().size() > 0 ) {
			Concept localRoot = new Concept( IRI.create( NameUtils.separatingName( model.getDefaultNamespace() ) + "RootThing" ),
			                                 model.getPackageNameMappings(),
			                                 "RootThing",
			                                 false );
			model.addConcept( localRoot );

			localRoot.addSuperConcept( thing );
			thing.getSubConcepts().add( localRoot );

			for ( String propIri : thing.getProperties().keySet() ) {
				PropertyRelation rel = thing.getProperty( propIri );
				rel.setDomain( localRoot );
				localRoot.addProperty( propIri, rel );
			}
			thing.getProperties().clear();

			for ( Concept con : model.getConcepts() ) {
				if ( con == localRoot ) {
					continue;
				}
				if ( con.getSuperConcepts().contains( thing ) ) {
					con.getSuperConcepts().remove( thing );
					con.getSuperConcepts().add( localRoot );
				}
				if ( thing.getSubConcepts().contains( con ) ) {
					thing.getSubConcepts().remove( con );
					localRoot.getSubConcepts().add( con );
				}
			}

			for ( PropertyRelation prop : model.getProperties() ) {
				fixProperty( prop, thing, localRoot );
			}
		}
	}

	private void fixProperty( PropertyRelation prop, Concept thing, Concept localRoot ) {
		if ( prop.getDomain() == thing ) {
			prop.setDomain( localRoot );
		}
		if ( prop.getTarget() == thing ) {
			prop.setTarget( localRoot );
		}
		for ( PropertyRelation sub : prop.getRestrictedProperties() ) {
			fixProperty( sub, thing, localRoot );
		}
	}


	private void processSubConceptAxiom( OWLClassExpression subClass, OWLClassExpression superClass, Map<String, Collection<String>> supers, String thing ) {
		if ( ! superClass.isAnonymous() || superClass instanceof OWLObjectUnionOf ) {

			String sub = IRIUtils.iriOf( filterAliases( subClass ) );
			String sup = isDelegating( superClass ) ?
					thing : IRIUtils.iriOf( filterAliases( superClass ) );

			addSuper( sub, sup, supers );
		} else if ( superClass instanceof OWLObjectIntersectionOf ) {
			OWLObjectIntersectionOf and = (OWLObjectIntersectionOf) superClass;
			for ( OWLClassExpression ex : and.asConjunctSet() ) {
				processSubConceptAxiom( subClass, ex, supers, thing );
			}
		}
	}

	private void addSuper(String sub, String sup, Map<String, Collection<String>> supers) {
		if ( sub.equals( sup ) ) {
			return;
		}
		Collection<String> ancestors = supers.computeIfAbsent( sub, k -> new HashSet<>() );
		ancestors.add( sup );
	}

	private void addSubConceptsToModel( OWLOntology ontoDescr, OntoModel model) {

		Map<String,Collection<String>> supers = new HashMap<>();

		String thing = IRIUtils.iriOf( ontoDescr.getOWLOntologyManager().getOWLDataFactory().getOWLThing() );
		supers.put( thing, Collections.emptySet() );

		ontoDescr.axioms( AxiomType.SUBCLASS_OF ).forEach( (ax) ->
				                                                   processSubConceptAxiom( ax.getSubClass(), ax.getSuperClass(), supers, thing ) );

		ontoDescr.axioms( AxiomType.EQUIVALENT_CLASSES ).forEach( (ax) ->
				                                                          processSubConceptAxiom( ax.classExpressions().findFirst().orElseThrow( IllegalStateException::new ),
				                                                                                  filterAliases( ax.classExpressions().skip( 1 ).findFirst().orElseThrow( IllegalStateException::new ) ),
				                                                                                  supers,
				                                                                                  thing ) );


		for ( OWLClassExpression delegator : aliases.keySet() ) {
			if ( ! delegator.isAnonymous() ) {
				addSuper( IRIUtils.iriOf( delegator ), thing, supers );
			}
		}

		for ( OWLClassExpression delegator : aliases.keySet() ) {
			if ( ! delegator.isAnonymous() ) {
				OWLClassExpression delegate = aliases.get( delegator );
				String sub = IRIUtils.iriOf( delegate );
				String sup = IRIUtils.iriOf( delegator );
				conceptCache.get( sub ).getEquivalentConcepts().add( conceptCache.get( sup ) );
				addSuper(sub, sup, supers);
			}
		}

		HierarchySorter<String> sorter = new HierarchySorter<>();
		List<String> sortedTemp = sorter.sort( supers );
		List<String> sortedCons = new ArrayList<>( sortedTemp.size() + complexes.size() + 3 );
		for ( Concept complex : complexes.values() ) {
			// we need to fix both the proper concept and the metadata.
			// "Complex" datatype concepts
			addSuper( complex.getIri(), thing, supers );
			complex.addSuperConcept( conceptCache.get( Thing.IRI ) );
			sortedCons.add( complex.getIri() );
		}
		sortedCons.addAll( sortedTemp );

		ArrayList<String> missing = new ArrayList<>( conceptCache.keySet() );
		missing.removeAll( supers.keySet() );

		LinkedHashMap<String,Concept> sortedCache = new LinkedHashMap<>();
		for ( String miss : missing ) {
			sortedCache.put( miss, conceptCache.get( miss ) );
			supers.put( miss, Collections.singletonList( thing ) );
		}

		for ( String con : sortedCons ) {
			sortedCache.put( con, conceptCache.get( con ) );
		}
		conceptCache.clear();
		conceptCache = sortedCache;


		reduceTransitiveInheritance( sortedCache, supers );


		for ( String con : supers.keySet() ) {
			Collection<String> parents = supers.get( con );
			for ( String sup : parents ) {
				SubConceptOf subConceptOf = new SubConceptOf( con, sup );
				model.addSubConceptOf( subConceptOf );
				conceptCache.get( subConceptOf.getSubject() ).addSuperConcept( conceptCache.get( subConceptOf.getObject() ) );
			}
		}
	}

	private void reduceTransitiveInheritance(LinkedHashMap<String, Concept> sortedCache, Map<String, Collection<String>> supers) {
		Map<String, Collection<String>> taboos = new HashMap<>();
		for ( String con : sortedCache.keySet() ) {
			Collection<String> ancestors = supers.get( con );
			Set<String> taboo = new HashSet<>();
			for ( String anc : ancestors ) {
				if ( taboos.containsKey( anc ) ) {
					taboo.addAll( taboos.get( anc ) );
				}
			}
			ancestors.removeAll( taboo );
			taboo.addAll( ancestors );
			taboos.put( con, taboo );
		}
	}

	private boolean isDelegating( OWLClassExpression superClass ) {
		return aliases.containsKey( superClass );
	}

	private boolean isDelegating( String classIri ) {
		for ( OWLClassExpression klass : aliases.keySet() ) {
			if ( ! klass.isAnonymous() && IRIUtils.iriOf( klass ).equals( classIri ) ) {
				return true;
			}
		}
		return false;
	}

	private OWLClassExpression filterAliases( OWLClassExpression klass ) {
		return aliases.getOrDefault( klass, klass );
	}


	private void addConceptsToModel( OWLOntology ontoDescr, OntoModel baseModel ) {

		ontoDescr.classesInSignature( Imports.INCLUDED ).forEach( (con) -> {
			if ( baseModel.getConcept( IRIUtils.iriOf( con ) ) == null ) {
				Concept concept =  new Concept( con.getIRI(),
				                                baseModel.getPackageNameMappings(),
				                                NameUtils.buildNameFromIri( con.getIRI().getScheme(), IRIUtils.fragmentOf( con.getIRI() ) ),
				                                con.isOWLDatatype() );

				getAnnotations( con, ontoDescr )
						.filter( (ann) -> ann.getProperty().isComment() && ann.getValue() instanceof OWLLiteral )
						.forEach( (ann) -> {
							OWLLiteral lit = (OWLLiteral) ann.getValue();
							if ( lit.getLiteral().trim().equals( "abstract" ) ) {
								concept.setAbstrakt( true );
							}
						});

				if ( concept.getName().endsWith( "Range" ) || concept.getName().endsWith( "Domain" ) || concept.getName().matches( "\\S*Filler\\d+" ) ) {
					concept.setAnonymous( true );
				}

				baseModel.addConcept( concept );
				conceptCache.put( IRIUtils.iriOf( con ), concept );
			}
		});
	}


	private void validate( OntoModel model ) {

		for ( PropertyRelation rel : model.getProperties() ) {
			if ( ! rel.isRestricted() ) {
				if ( rel != rel.getBaseProperty() && ! rel.isAttribute() ) {
					throw new IllegalStateException( "Property is not restricted, but not a base property either " + rel + " >> base " + rel.getBaseProperty() );
				}
			}
			for ( PropertyRelation rest : rel.getRestrictedProperties() ) {
				checkForRestriction( rest, rel, rel.getBaseProperty() );
			}
		}
	}

	private void checkForRestriction( PropertyRelation restricted, PropertyRelation parent, PropertyRelation base ) {
		if ( restricted.getBaseProperty() != base ) {
			throw new IllegalStateException( "Inconsistent base property for" + restricted + " >> base " + restricted.getBaseProperty() + " , expected " + base );
		}
		if ( restricted.getImmediateBaseProperty() != parent ) {
			throw new IllegalStateException( "Inconsistent parent property for" + restricted + " >> parent  " + restricted.getImmediateBaseProperty() + " , expected " + parent );
		}
		int diff = diff( restricted, parent );
		if ( 0 == diff ) {
			throw new IllegalStateException( "Inconsistent restriction for " + restricted + " >> parent  " + parent  );
		}
		for ( PropertyRelation subRestr : restricted.getRestrictedProperties() ) {
			checkForRestriction( subRestr, restricted, base );
		}
	}

	private enum DIFF_BY {
		DOMAIN( (byte) 1 ), RANGE( (byte) 2 ), MIN( (byte) 4 ), MAX( (byte) 8 );

		DIFF_BY( byte x ) { bit = x; }

		private byte bit;

		public byte getBit() { return bit; }
	}

	private int diff( PropertyRelation restricted, PropertyRelation parent ) {
		int diff = 0;
		if ( ! restricted.getDomain().equals( parent.getDomain() ) ) {
			diff |= DIFF_BY.DOMAIN.getBit();
		}
		if ( ! restricted.getTarget().equals( parent.getTarget() ) ) {
			diff |= DIFF_BY.RANGE.getBit();
		}
		if ( restricted.getMinCard() == null && parent.getMinCard() != null
				|| ! restricted.getMinCard().equals( parent.getMinCard() ) ) {
			diff |= DIFF_BY.MIN.getBit();
		}
		if ( restricted.getMaxCard() == null && parent.getMaxCard() != null
				|| restricted.getMaxCard() != null && parent.getMaxCard() == null
				|| ( restricted.getMaxCard() != null && ! restricted.getMaxCard().equals( parent.getMaxCard() ) )
				) {
			diff |= DIFF_BY.MAX.getBit();
		}
		return diff;
	}







	private void launchReasoner( boolean dirty,
	                             OWLOntology ontoDescr,
	                             List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators ) {
		if ( dirty ) {
			long now = new Date().getTime();
			if ( logger.isInfoEnabled() ) {
				logger.info( " START REASONER " );
			}

			OWLReasoner owler = initReasoner( ontoDescr );

			InferredOntologyGenerator reasoner = new InferredOntologyGenerator( owler, axiomGenerators );

			reasoner.fillOntology( ontoDescr.getOWLOntologyManager().getOWLDataFactory(), ontoDescr );

			if ( ! owler.isConsistent() ) {
				throw new RuntimeException( "Inconsistent ontology" );
			}

			if ( logger.isInfoEnabled() ) {
				logger.info( " STOP REASONER : time elapsed >> " + ( new Date().getTime() - now ) );
			}

		} else {
			if ( logger.isInfoEnabled() ) {
				logger.info( " REASONER NOT NEEDED" );
			}
		}


	}




	private static Set<IRI> registeredDTHandlers = new HashSet<>();

	protected OWLReasoner initReasoner( OWLOntology ontoDescr ) {

		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);


		ontoDescr.datatypesInSignature( Imports.INCLUDED ).forEach( (datatype) -> {
			IRI dtIRI = datatype.getIRI();

			if ( ! "xsd".equals( dtIRI.getNamespace() )
					&& ! dtIRI.getNamespace().equals( NamespaceUtils.getHashedNamespaceByPrefix( "xsd" ) )
					&& ! dtIRI.getNamespace().equals( NamespaceUtils.getHashedNamespaceByPrefix( "rdf" ) )
					&& ! dtIRI.getNamespace().equals( NamespaceUtils.getHashedNamespaceByPrefix( "owl" ) )
					&& ! registeredDTHandlers.contains( dtIRI ) ) {

				//DatatypeRegistry.registerDatatypeHandler( new ComplexDatatypeHandler( datatype, ontoDescr ) );

				// is there a way to get the set of registered handlers from the registry?
				// a duplicate registration throws an error, so we need to prevent it manually
				registeredDTHandlers.add( dtIRI );
			}
		} );

		OWLReasonerFactory reasonerFactory = new ReasonerFactory();

		OWLReasoner owler = reasonerFactory.createReasoner( ontoDescr, config );
		owler.precomputeInferences(
				InferenceType.CLASS_HIERARCHY,
				InferenceType.CLASS_ASSERTIONS,

				InferenceType.OBJECT_PROPERTY_ASSERTIONS,
				InferenceType.DATA_PROPERTY_ASSERTIONS,

				InferenceType.DIFFERENT_INDIVIDUALS,
				InferenceType.SAME_INDIVIDUAL,

				InferenceType.DISJOINT_CLASSES,

				//                                        InferenceType.DATA_PROPERTY_HIERARCHY,
				InferenceType.OBJECT_PROPERTY_HIERARCHY
		                          );


		if ( ! owler.isConsistent() ) {
			throw new RuntimeException( "Inconsistent ontology " );
		}

		return owler;
	}


	private void applyAxiom( OWLOntology ontology, OWLOntologyChange change ) {
		ontology.getOWLOntologyManager().applyChange( change );
	}

}
