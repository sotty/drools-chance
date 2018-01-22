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

package org.kie.semantics.builder.model.compilers;

 import org.kie.semantics.builder.model.CompiledOntoModel;
 import org.kie.semantics.builder.model.Concept;
 import org.kie.semantics.builder.model.OntoModel;
 import org.kie.semantics.utils.NameUtils;

 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;

 public abstract class ModelCompilerImpl implements ModelCompiler {

    protected CompiledOntoModel model;

    public CompiledOntoModel getModel() {
        return model;
    }

    protected abstract void setModel( OntoModel model );

    public abstract void compile( Concept con, Object target, Map<String,Object> params);

    public CompiledOntoModel compile( OntoModel model ) {

        if ( getModel() == null ) {
            setModel( model );
        }

        for ( Concept con : getModel().getConcepts() ) {
            if ( con.isPrimitive() ) {
                continue;
            }

//            String name = NameUtils.compactUpperCase( con.getName() );
//            System.out.println( "Compiling concept " + name );

            Map map = new HashMap();
            map.put( "package", con.getPackage() );
            map.put( "namespace", con.getNamespace() );
            map.put( "iri", con.getIri() );
            map.put( "name", con.getName().substring( con.getName().lastIndexOf(".") + 1 ) );
            map.put( "fullyQualifiedName", con.getFullyQualifiedName() );
            map.put( "superConcepts", getEffectiveSuperConcepts( con.getSuperConcepts(), model.isStandalone() ) );
            map.put( "subConcepts", con.getSubConcepts() );
            map.put( "properties", con.getProperties() );
            map.put( "implInterface", con.isResolved() && con.getResolvedAs().equals( Concept.Resolution.IFACE ) ? con.getFullyQualifiedName() : null );
            map.put( "implClass", con.isResolved() && con.getResolvedAs().equals( Concept.Resolution.CLASS )? con.getFullyQualifiedName() : null );
            map.put( "implProperties", con.getChosenProperties() );

//                map.put( "shadowProperties", con.getShadowProperties() );
            if ( con.isAbstrakt() ) {
                map.put( "abstract", con.isAbstrakt() );
            }
            map.put( "keys", con.getKeys() );
            map.put( "shadowed", con.isShadowed() );
            map.put( "standalone", model.isStandalone() );
            map.put( "traiting", model.isTraiting() );
            map.put( "enhancedNames", model.isUseEnhancedNames() );
            map.put( "minimal", model.isMinimal() );

            compile( con, NameUtils.getInstance(), map );
        }

        return getModel();
    }

    protected Set<Concept> getEffectiveSuperConcepts( Set<Concept> superConcepts, boolean standalone ) {
        Set<Concept> sups = new HashSet<Concept>( superConcepts );
        for ( Concept con : superConcepts ) {
            if ( con.isTop() && standalone ) {
                sups.remove( con );
            }
            if ( con.isResolved() && con.getResolvedAs() == Concept.Resolution.CLASS ) {
                sups.remove( con );
            }
        }
        return sups;
    }


}