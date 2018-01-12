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

package org.kie.semantics.builder;


import org.drools.semantics.builder.model.OntoModel;
import org.kie.api.io.Resource;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Map;

public interface DLFactory {



	OWLOntology parseOntology( Resource resource );


	OntoModel buildModel( String name,
	                      Resource res,
	                      DLFactoryConfiguration conf );

	OntoModel buildModel( String name,
	                      Resource res,
	                      DLFactoryConfiguration conf,
	                      ClassLoader classLoader );


	OntoModel buildModel( String name,
	                      Resource[] res,
	                      DLFactoryConfiguration conf );

	OntoModel buildModel( String name,
	                      Resource[] res,
	                      DLFactoryConfiguration conf,
	                      ClassLoader lodaer );

	OntoModel buildModel( String name,
	                      Map<String,String> packageNames,
	                      Resource[] res,
	                      DLFactoryConfiguration conf,
	                      ClassLoader lodaer );


}
