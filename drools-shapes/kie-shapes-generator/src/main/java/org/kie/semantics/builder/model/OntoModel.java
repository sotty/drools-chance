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

package org.kie.semantics.builder.model;

import org.drools.core.util.CodedHierarchy;
import org.drools.semantics.builder.model.hierarchy.DatabaseModelProcessor;
import org.drools.semantics.builder.model.hierarchy.FlatModelProcessor;
import org.drools.semantics.builder.model.hierarchy.HierarchicalModelProcessor;
import org.drools.semantics.builder.model.hierarchy.ModelHierarchyProcessor;
import org.drools.semantics.builder.model.hierarchy.NullModelProcessor;
import org.drools.semantics.builder.model.hierarchy.OptimizedModelProcessor;
import org.drools.semantics.builder.model.hierarchy.VariantModelProcessor;
import org.drools.semantics.util.area.AreaTxn;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OntoModel extends Cloneable {


    enum Mode  {
        HIERARCHY( new HierarchicalModelProcessor() ),
        FLAT( new FlatModelProcessor() ),
        VARIANT( new VariantModelProcessor() ),
        OPTIMIZED( new OptimizedModelProcessor() ),
        DATABASE( new DatabaseModelProcessor() ),
        NONE( new NullModelProcessor() );

        private ModelHierarchyProcessor processor;

        Mode( ModelHierarchyProcessor prox ) {
            processor = prox;
        }

        public ModelHierarchyProcessor getProcessor() {
            return processor;
        }
    }


	OWLOntology getOntology();

	void setOntology( OWLOntology onto );

	String getDefaultPackage();
        
	void setDefaultPackage( String pack );

	Map<String,String> getPackageNameMappings();

	Set<String> getAllPackageNames();

    String getName();
        
    void setName( String name );

    String getDefaultNamespace();

    void setDefaultNamespace( String ns );
    
    

    List<Concept> getConcepts();

    Concept getConcept( String id );

    void addConcept( Concept con );

    Concept removeConcept( Concept con );


    Set<Individual> getIndividuals();
    
    void addIndividual( Individual i );
    
    Individual removeIndividual( Individual i );


    Set<SubConceptOf> getSubConcepts();

    void addSubConceptOf( SubConceptOf sub );

    SubConceptOf getSubConceptOf( String sub, String sup );

    boolean removeSubConceptOf( SubConceptOf sub );



    Set<PropertyRelation> getProperties();

    PropertyRelation addProperty( PropertyRelation rel );

    PropertyRelation removeProperty( PropertyRelation rel );

    PropertyRelation getProperty( String iri );


    void sort();

    boolean isHierarchyConsistent();

    Mode getMode();


    ClassLoader getClassLoader();

    void setClassLoader( ClassLoader classLoader );


    void reassignConceptCodes();

    CodedHierarchy<Concept> getConceptHierarchy();

    void buildAreaTaxonomy();

    AreaTxn<Concept,PropertyRelation> getAreaTaxonomy();

    /* interface only - do not extend drools Thing */
    boolean isStandalone();

    void setStandalone( boolean standalone );

    /* interface only - expose only the minimal functionalities */
    boolean isMinimal();

    void setMinimal( boolean minimal );

    boolean isUseEnhancedNames();

    void setUseEnhancedNames( boolean useEnhancedNames );

    boolean isTraiting();

    void setTraiting( boolean traiting );
}
