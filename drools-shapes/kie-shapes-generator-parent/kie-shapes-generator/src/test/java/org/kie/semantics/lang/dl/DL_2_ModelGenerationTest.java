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

package org.kie.semantics.lang.dl;

import org.kie.semantics.builder.model.PropertyRelation;
import org.junit.Ignore;
import org.kie.api.io.Resource;
import org.kie.internal.io.ResourceFactory;
import org.kie.semantics.builder.DLFactory;
import org.kie.semantics.builder.DLFactoryBuilder;
import org.kie.semantics.builder.DLFactoryConfiguration;
import org.kie.semantics.builder.model.Concept;
import org.kie.semantics.builder.model.DRLModel;
import org.kie.semantics.builder.model.ModelFactory;
import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.builder.model.compilers.ModelCompiler;
import org.kie.semantics.builder.model.compilers.ModelCompilerFactory;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.w3._2002._07.owl.Thing;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This is a sample class to launch a rule.
 */
@SuppressWarnings("restriction")
public class DL_2_ModelGenerationTest {



    protected DLFactory factory = DLFactoryBuilder.newDLFactoryInstance();

    @Test
    public void testLoad() {
        String source = "ontologies/kmr2/kmr2_mini.owl";

        Resource res = ResourceFactory.newClassPathResource(source);

        OWLOntology ontoDescr = factory.parseOntology( res );

        assertNotNull( ontoDescr );
    }

    @Test
    public void testPackageOverride() {
        String source = "ontologies/kmr2/kmr2_mini.owl";

        Resource res = ResourceFactory.newClassPathResource(source);

        Map<String,String> overrides = new HashMap<String, String>( );
        overrides.put( "http://www.kmr.org/ontology/", "my.foo.test" );
        OntoModel model = factory.buildModel( "mini",
                                              overrides,
                                              new Resource[]{ res },
                                              DLFactoryConfiguration.newConfiguration( OntoModel.Mode.NONE ),
                                              null );

        for ( Concept con : model.getConcepts() ) {
            if ( con.getIri().equals( Thing.IRI ) ) {
                assertEquals( "org.w3._2002._07.owl", con.getPackage() );
            } else {
                assertEquals( "my.foo.test", con.getPackage() );
            }
        }

    }

    @Test
    public void testDiamondModelGenerationExternal() {
        String source = "ontologies/diamond.manchester.owl";
        Resource res = ResourceFactory.newClassPathResource( source );

        OntoModel results;

        results = factory.buildModel( "diamond",
                                      res,
                                      DLFactoryConfiguration.newConfiguration( OntoModel.Mode.FLAT ) );

        checkDiamond( results );

    }






    @Test
    public void testPropertiesGenerationExternal() {
        String source = "fuzzyDL/DLex6.owl";
        Resource res = ResourceFactory.newClassPathResource( source );

        OntoModel results = factory.buildModel( "ex6", res, DLFactoryConfiguration.newConfiguration( OntoModel.Mode.FLAT ) );


        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.DRL );
        DRLModel drlModel = (DRLModel) compiler.compile( results );

        check( results, drlModel );
    }



    private void checkDiamond(OntoModel results) {
        assertTrue(results.getSubConceptOf("<_Right>", "<_Top>") != null || results.getSubConceptOf("<_Right>", "<_C0>") != null);
        assertTrue(results.getSubConceptOf("<_Left>", "<_Top>") != null || results.getSubConceptOf("<_Left>", "<_C0>") != null);

        assertNotNull(results.getSubConceptOf("<_Low>", "<_Left>"));
        assertNotNull(results.getSubConceptOf("<_Low>", "<_Right>"));
        assertNotNull(results.getSubConceptOf("<_Bottom>", "<_Low>"));

    }













    protected void check( OntoModel results, DRLModel drlModel ) {

        System.out.println( "\n\n\n\n\n\n\n\n\n\n\n\n\n\n" );
        System.out.println( results );
        System.out.println(" -------------------------------");
        System.out.println( drlModel.getDRL() );
        System.out.println(" -------------------------------");

        String ns = "http://jboss.org/drools/semantics/Example6#";

        assertNotNull( results.getConcept( "<_A>") );
        assertNotNull( results.getConcept( "<_B>") );
        assertNotNull( results.getConcept( "<_C>") );
        assertNotNull( results.getConcept( "<_D>") );
        assertNotNull( results.getConcept( "<" + ns +"MyPropRange>") );
        assertNotNull( results.getConcept( "<" + ns +"YourPropDomain>") );
        assertNotNull( results.getConcept( "<" + ns +"ZimpleDomain>") );

        assertNotNull( results.getSubConceptOf( "<" + ns +"YourPropDomain>", "<_B>" ) );
        assertNotNull( results.getSubConceptOf( "<_C>", "<" + ns +"MyPropRange>" ) );
        assertNotNull( results.getSubConceptOf( "<" + ns +"YourPropDomain>", "<_C>" ) );
        assertNotNull( results.getSubConceptOf( "<_D>", "<" + ns +"MyPropRange>" ) );
        assertNotNull( results.getSubConceptOf( "<_D>", "<" + ns +"ZimpleDomain>" ) );
        assertNotNull( results.getSubConceptOf( "<_B>", "<" + ns +"ZimpleDomain>" ) );

        assertEquals( 3, results.getProperties().size() );
        assertNotNull( results.getProperty( "<_myProp>" ) );
        assertNotNull(results.getProperty("<_yourProp>"));
        assertNotNull( results.getProperty( "<_zimple>" ) );
        assertTrue(results.getProperty("<_myProp>").getSubject().equals("<_A>"));
        assertTrue(results.getProperty("<_myProp>").getObject().equals("<" + ns +"MyPropRange>"));
        assertTrue( results.getProperty( "<_yourProp>" ).getSubject().equals( "<" + ns +"YourPropDomain>" ) );
        assertTrue( results.getProperty( "<_yourProp>" ).getObject().equals( "<_D>" ) );
        assertTrue( results.getProperty( "<_zimple>" ).getSubject().equals( "<" + ns +"ZimpleDomain>" ) );
        assertTrue( results.getProperty( "<_zimple>" ).getObject().equals( "<http://www.w3.org/2001/XMLSchema#int>" ) );


        assertTrue(
                results.getConcept("<_C>").getSuperConcepts().contains(results.getConcept("<" + ns +"MyPropRange>"))
        );
        assertTrue(
                results.getConcept( "<_D>" ).getSuperConcepts().contains( results.getConcept( "<" + ns +"MyPropRange>" ) )
        );
        assertTrue(
                results.getConcept( "<" + ns +"YourPropDomain>" ).getSuperConcepts().contains( results.getConcept( "<_C>" ) )
        );
        assertTrue(
                results.getConcept( "<" + ns +"YourPropDomain>" ).getSuperConcepts().contains( results.getConcept( "<_B>" ) )
        );

        assertTrue(
                results.getConcept( "<" + ns +"YourPropDomain>").getProperties().containsValue(
                        results.getProperty("<_yourProp>")
                )
        );
        assertTrue(
                results.getConcept( "<_A>").getProperties().containsValue(
                        results.getProperty("<_myProp>")
                )
        );

        assertTrue(
                results.getConcept("<" + ns +"YourPropDomain>").getProperties().get(
                        "<_yourProp>"
                ).getTarget().equals(results.getConcept("<_D>"))
                );
        assertTrue(
                results.getConcept( "<_A>").getProperties().get(
                        "<_myProp>"
                ).getTarget().equals( results.getConcept( "<" + ns +"MyPropRange>" ) )
        );

        assertTrue(
                results.getConcept( "<" + ns +"ZimpleDomain>").getProperties().get(
                        "<_zimple>"
                ).getTarget().equals( new Concept( IRI.create( "http://www.w3.org/2001/XMLSchema#int" ), null, "java.lang.Integer", true ) )
        );




    }





    @Test
    public void testComplexAnonymous() {
        String source = "fuzzyDL/DLex8.owl";
        Resource res = ResourceFactory.newClassPathResource( source );

        OntoModel results = factory.buildModel( "ex8", res, DLFactoryConfiguration.newConfiguration( OntoModel.Mode.FLAT ) );


        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler(ModelFactory.CompileTarget.DRL);
        DRLModel drlModel = (DRLModel) compiler.compile( results );

        System.out.println( drlModel.getDRL() );
    }



    @Test
    @Ignore
    public void testComplexDatatype() {

        Resource res = ResourceFactory.newClassPathResource( "ontologies/complexCustomDT.owl" );
        OWLOntology onto = factory.parseOntology( res );
        OntoModel ontoModel = factory.buildModel( "test",
                                                  res,
                                                  DLFactoryConfiguration.newConfiguration( OntoModel.Mode.OPTIMIZED,
                                                                                           DLFactoryConfiguration.defaultAxiomGenerators ) );
        assertTrue( ontoModel.isHierarchyConsistent() );

    }

    @Test
    public void testRestrictedCardinalityProperty() {

        Resource res = ResourceFactory.newClassPathResource( "ontologies/cardinalAttribute.owl" );
        OWLOntology onto = factory.parseOntology( res );
        OntoModel ontoModel = factory.buildModel( "test",
                                                  res,
                                                  DLFactoryConfiguration.newConfiguration( OntoModel.Mode.OPTIMIZED,
                                                                                           DLFactoryConfiguration.defaultAxiomGenerators ) );
        assertTrue( ontoModel.isHierarchyConsistent() );

        Concept c = ontoModel.getConcept( "<http://org/drools/test#Klass>" );
        assertNotNull( c );

        assertEquals( 2, c.getProperties().size() );

        PropertyRelation p1 = c.getProperty( "<http://org/drools/test#myAttr>" );
        assertNotNull( p1 );
        assertTrue( p1.isSimple() );
        assertTrue( p1.isAttribute() );
        assertFalse( p1.isRestricted() );

        PropertyRelation p2 = c.getProperty( "<http://org/drools/test#myRelTgt>" );
        assertNotNull( p2 );
        assertFalse( p2.isSimple() );
        assertFalse( p2.isAttribute() );
        assertTrue( p2.isRestricted() );

    }

   @Test
    public void testCrossAttributes() {

        Resource res = ResourceFactory.newClassPathResource( "ontologies/crossAttributes.owl" );
        OWLOntology onto = factory.parseOntology( res );
        OntoModel ontoModel = factory.buildModel( "test",
                                                  res,
                                                  DLFactoryConfiguration.newConfiguration( OntoModel.Mode.OPTIMIZED,
                                                                                           DLFactoryConfiguration.defaultAxiomGenerators ) );
        assertTrue( ontoModel.isHierarchyConsistent() );

       Concept c1 = ontoModel.getConcept( "<http://org/drools/test#Klass1>" );
       Concept t1 = ontoModel.getConcept( "<http://org/drools/test#Tgt1>" );
       Concept c2 = ontoModel.getConcept( "<http://org/drools/test#Klass2>" );
       Concept t2 = ontoModel.getConcept( "<http://org/drools/test#Tgt2>" );
       Concept dom = ontoModel.getConcept( "<http://org/drools/test#MyAttrDomain>" );
       Concept ran = ontoModel.getConcept( "<http://org/drools/test#MyAttrRange>" );
       assertNotNull( c1 );
       assertNotNull( t1 );
       assertNotNull( c2 );
       assertNotNull( t2 );
       assertNotNull( dom );
       assertNotNull( ran );

       assertEquals( 0, dom.getProperties().size() );
       assertEquals( 0, ran.getProperties().size() );
       assertEquals( 1, c1.getProperties().size() );
       assertEquals( 1, c2.getProperties().size() );
       assertEquals( 0, t1.getProperties().size() );
       assertEquals( 0, t2.getProperties().size() );

       PropertyRelation p1 = c1.getProperty( "<http://org/drools/test#myAttr>" );
       assertNotNull( p1 );
       assertFalse( p1.isSimple() );
       assertTrue( p1.isAttribute() );
       assertFalse( p1.isRestricted() );
       assertEquals( c1, p1.getDomain() );
       assertEquals( t1, p1.getTarget() );


       PropertyRelation p2 = c2.getProperty( "<http://org/drools/test#myAttr>" );
       assertNotNull( p2 );
       assertTrue( p2.isSimple() );
       assertTrue( p2.isAttribute() );
       assertFalse( p2.isRestricted() );
       assertEquals( c2, p2.getDomain() );
       assertEquals( t2, p2.getTarget() );
       
   }


}