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


import org.jdom.Namespace;
import org.w3c.dom.Document;

import java.io.File;
import java.io.OutputStream;


public interface SemanticXSDModel extends XSDModel {

    Document getBindings( String namespace );

    void setBindings( String namespace, Document bindings );

    String getSerializedBindings( String namespace );

    String getSerializedBindings();

    //TODO remove
    boolean streamBindings( File folder );

    boolean stream( File folder );


    void setIndex( String index );

    boolean streamIndex( OutputStream fos);

    String getIndex();


    void setIndividualFactory( String factory );
    
    String getIndividualFactory();


    void addNamespacedPackageInfo( Namespace ns, String fix );

    String getNamespacedPackageInfo( Namespace ns );


    void setEmpireConfig( String config );

    String getEmpireConfig();


    void setPersistenceXml( String config );

    String getPersistenceXml();

}