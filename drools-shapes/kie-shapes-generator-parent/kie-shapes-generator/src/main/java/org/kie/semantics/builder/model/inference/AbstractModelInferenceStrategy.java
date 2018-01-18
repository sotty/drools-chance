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

import org.kie.api.io.Resource;
import org.kie.semantics.builder.DLFactoryConfiguration;
import org.kie.semantics.builder.model.ModelFactory;
import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.util.IRIUtils;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Map;

public abstract class AbstractModelInferenceStrategy implements ModelInferenceStrategy {



    public OntoModel buildModel( String name,
                                 Map<String,String> packageNames,
                                 OWLOntology ontoDescr,
                                 DLFactoryConfiguration conf,
                                 Map<InferenceTask, Resource> theory,
                                 ClassLoader classLoader ) {

        OntoModel baseModel = ModelFactory.newModel( name, packageNames, conf.getMode() );
        baseModel.setOntology( ontoDescr );
        baseModel.setClassLoader( classLoader );

        String oIRI = IRIUtils.ontologyNamespace( ontoDescr );
        baseModel.setDefaultPackage( packageNames.get( oIRI ) );
        baseModel.setDefaultNamespace( oIRI );

        OntoModel latticeModel = buildClassLattice( ontoDescr, theory, baseModel, conf );

        latticeModel.sort();

        OntoModel propertyModel = buildProperties( ontoDescr, theory, latticeModel, conf );

        propertyModel.sort();

        OntoModel populatedModel = buildIndividuals( ontoDescr, theory, propertyModel, conf );

        populatedModel.reassignConceptCodes();

        populatedModel.buildAreaTaxonomy();

        populatedModel.getMode().getProcessor().process( populatedModel );

        return populatedModel;
    }


    protected abstract OntoModel buildProperties( OWLOntology ontoDescr, Map<InferenceTask, Resource> theory, OntoModel hierachicalModel, DLFactoryConfiguration conf );


    protected abstract OntoModel buildIndividuals( OWLOntology ontoDescr, Map<InferenceTask, Resource> theory, OntoModel hierachicalModel, DLFactoryConfiguration conf );


    protected abstract OntoModel buildClassLattice( OWLOntology ontoDescr,
                                                    Map<InferenceTask, Resource> theory,
                                                    OntoModel baseModel,
                                                    DLFactoryConfiguration conf );


    protected abstract OWLReasoner initReasoner( OWLOntology ontoDescr );


}
