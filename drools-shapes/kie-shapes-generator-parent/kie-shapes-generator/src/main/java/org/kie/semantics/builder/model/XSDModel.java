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


import org.jdom.Document;
import org.jdom.Namespace;

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;


public interface XSDModel extends CompiledOntoModel {


    Document getXSDSchema();

    Namespace getNamespace( String ns );

    Collection<Namespace> getNamespaces( );

    void setNamespace( String ns, String nameSpace );

    String mapNamespaceToPrefix( String ns );

    Map<String,Namespace> getAssignedPrefixes();

//    void setSchemaMode( XSDModelCompiler.XSDSchemaMode schemMode );


	boolean streamAll( OutputStream os );

}
