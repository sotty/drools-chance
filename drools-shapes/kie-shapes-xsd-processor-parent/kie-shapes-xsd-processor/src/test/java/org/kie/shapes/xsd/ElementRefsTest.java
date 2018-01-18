/*
 * Copyright 2013 JBoss Inc
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

package org.kie.shapes.xsd;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.w3._2001.xmlschema.Schema;

import java.net.URL;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class ElementRefsTest {


    @Test
    public void testElementRefs() {

        OWLOntology onto;
        OWLOntologyManager manager;
        OWLDataFactory factory;
        String tns;


        Xsd2Owl converter = Xsd2OwlImpl.getInstance();

        URL url = converter.getSchemaURL( "org/kie/shapes/xsd/elementRefs.xsd" );
        Schema x = converter.parse( url );
        tns = x.getTargetNamespace() + "#";

        onto = converter.transform( x, url, true, true );

        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();


        try {
	        OWLClass left = factory.getOWLClass( IRI.create( tns, "Left" ) );
            OWLClass link = factory.getOWLClass( IRI.create( tns, "Link" ) );
            OWLDatatype simpleElement = factory.getOWLDatatype( IRI.create( tns, "SimpleElement" ) );
            OWLDatatype strElement = factory.getOWLDatatype( IRI.create( tns, "StrElement" ) );
            OWLClass mainElement = factory.getOWLClass( IRI.create( tns, "MainElement" ) );
            OWLClass testElement = factory.getOWLClass( IRI.create( tns, "TestElement" ) );

            OWLDataProperty simpleElementProp = factory.getOWLDataProperty( IRI.create( tns, "SimpleElement" ) );
            OWLDataProperty strElementProp = factory.getOWLDataProperty( IRI.create( tns, "StrElement" ) );
            OWLObjectProperty mainElementProp = factory.getOWLObjectProperty( IRI.create( tns, "MainElement" ) );
            OWLObjectProperty testElementProp = factory.getOWLObjectProperty( IRI.create( tns, "TestElement" ) );
            OWLObjectProperty linkProp = factory.getOWLObjectProperty( IRI.create( tns, "link" ) );


            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( simpleElement ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( strElement ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( mainElement ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( testElement ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( left ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( link ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( simpleElementProp ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( strElementProp ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( mainElementProp ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( testElementProp ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( linkProp ) ) );

            assertTrue( onto.containsAxiom( factory.getOWLSubClassOfAxiom(
                    link,
                    factory.getOWLObjectIntersectionOf(
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLDataAllValuesFrom(
                                            simpleElementProp,
                                            simpleElement ),
                                    factory.getOWLDataMinCardinality(
                                            2,
                                            simpleElementProp,
                                            simpleElement ),
                                    factory.getOWLDataMaxCardinality(
                                            4,
                                            simpleElementProp,
                                            simpleElement )
                            ),
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLDataAllValuesFrom(
                                            strElementProp,
                                            strElement ),
                                    factory.getOWLDataMinCardinality(
                                            2,
                                            strElementProp,
                                            strElement ),
                                    factory.getOWLDataMaxCardinality(
                                            4,
                                            strElementProp,
                                            strElement )
                            )
                    ))
            ));

            assertTrue( onto.containsAxiom( factory.getOWLSubClassOfAxiom(
                    left,
                    factory.getOWLObjectIntersectionOf(
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLObjectAllValuesFrom(
                                            mainElementProp,
                                            mainElement ),
                                    factory.getOWLObjectMinCardinality(
                                            1,
                                            mainElementProp,
                                            mainElement ),
                                    factory.getOWLObjectMaxCardinality(
                                            1,
                                            mainElementProp,
                                            mainElement )
                            ),
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLObjectAllValuesFrom(
                                            testElementProp,
                                            testElement ),
                                    factory.getOWLObjectMinCardinality(
                                            1,
                                            testElementProp,
                                            testElement ),
                                    factory.getOWLObjectMaxCardinality(
                                            1,
                                            testElementProp,
                                            testElement )
                            ),
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLObjectAllValuesFrom(
                                            linkProp,
                                            link ),
                                    factory.getOWLObjectMinCardinality(
                                            0,
                                            linkProp,
                                            link )
                            ),
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLDataAllValuesFrom(
                                            strElementProp,
                                            strElement ),
                                    factory.getOWLDataMinCardinality(
                                            1,
                                            strElementProp,
                                            strElement ),
                                    factory.getOWLDataMaxCardinality(
                                            1,
                                            strElementProp,
                                            strElement )
                            )
                    ))
            ));


        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

    }



    @Test
    public void testGroupRefs() {

        OWLOntology onto;
        OWLOntologyManager manager;
        OWLDataFactory factory;
        String tns;


        Xsd2Owl converter = Xsd2OwlImpl.getInstance();

        URL url = converter.getSchemaURL( "org/kie/shapes/xsd/groupRefs.xsd" );
        Schema x = converter.parse( url );
        tns = x.getTargetNamespace() + "#";

        onto = converter.transform( x, url, true, true );

        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();


        try {
	        OWLClass k = factory.getOWLClass( IRI.create( tns, "Test" ) );
            OWL2Datatype string = OWL2Datatype.XSD_STRING;

            OWLObjectProperty p1 = factory.getOWLObjectProperty( IRI.create( tns, "field" ) );
            OWLDataProperty p2 = factory.getOWLDataProperty( IRI.create( tns, "desc" ) );

            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( k ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( p1 ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDeclarationAxiom( p2 ) ) );


            assertTrue( onto.containsAxiom( factory.getOWLObjectPropertyDomainAxiom( p1, k ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLObjectPropertyRangeAxiom( p1, k ) ) );

            assertTrue( onto.containsAxiom( factory.getOWLDataPropertyDomainAxiom( p2, k ) ) );
            assertTrue( onto.containsAxiom( factory.getOWLDataPropertyRangeAxiom( p2, string ) ) );

            assertTrue( onto.containsAxiom( factory.getOWLSubClassOfAxiom(
                    k,
                    factory.getOWLObjectIntersectionOf(
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLObjectAllValuesFrom(
                                            p1,
                                            k ),
                                    factory.getOWLObjectMinCardinality(
                                            0,
                                            p1,
                                            k )
                            ),
                            factory.getOWLObjectIntersectionOf(
                                    factory.getOWLDataAllValuesFrom(
                                            p2,
                                            string ),
                                    factory.getOWLDataMinCardinality(
                                            1,
                                            p2,
                                            string ),
                                    factory.getOWLDataMaxCardinality(
                                            1,
                                            p2,
                                            string )
                            )
                    )
            )
            ));

        } catch ( Exception e ) {
            fail( e.getMessage() );
        }

    }


}
