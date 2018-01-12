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

package org.kie.semantics.lang.dl.lang;

import org.drools.core.io.impl.ClassPathResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class OwlAPITest {

    private static Logger log;

    private boolean visual = false;
    public boolean isVisual() {	return visual; }
    public void setVisual(boolean visual) {	this.visual = visual; }



    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log = Logger.getAnonymousLogger();
        log.setLevel(Level.INFO);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }



    @Test
    public void kmr2_ontology() {
        test_ontology("ontologies/kmr2/kmr2_mini.owl");
    }

    @Test
    public void test_test() {
        test_ontology("ontologies/diamondProp.manchester.owl");
    }


    @Test
    public void test_ontolog5() {
        test_ontology("ontologies/sem_rules.owl");
    }

    @Test
    public void test_ontology6() {
        test_ontology("ontologies/mixed/mixed.owl");
    }


    public void test_ontology(String ontoName) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
            ClassPathResource res = new ClassPathResource( ontoName );
            OWLOntology onto = manager.loadOntologyFromOntologyDocument( res.getInputStream() );
            assertNotNull( onto );

            onto.axioms( AxiomType.DECLARATION, Imports.INCLUDED )
                .map( OWLDeclarationAxiom::getEntity )
                .filter( OWLClass.class::isInstance )
                .forEach( (ent) -> EntitySearcher.getEquivalentClasses( ent.asOWLClass(), onto.importsClosure() ).forEach( ( x ) -> {
	                              log.info( "***********" );
	                              log.info( x.toString() );
	                              log.info( "" + x.isAnonymous() );
	                              log.info( "" + x.isClassExpressionLiteral() );
	                              log.info( "" + (x instanceof OWLNaryBooleanClassExpression) );

	                              log.info( "*********** \n\n" );
                              } ) );
            log.info( onto.toString() );
        } catch (OWLOntologyCreationException | IOException e) {
            fail( e.getMessage() );
        }

    }

}
