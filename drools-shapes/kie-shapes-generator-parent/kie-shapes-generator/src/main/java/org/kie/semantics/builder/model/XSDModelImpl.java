
package org.kie.semantics.builder.model;

import org.apache.commons.collections15.list.UnmodifiableList;
import org.drools.core.util.StringUtils;
import org.kie.semantics.util.IRIUtils;
import org.kie.semantics.utils.NameUtils;
import org.kie.semantics.utils.NamespaceUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.kie.internal.io.ResourceFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class XSDModelImpl extends ModelImpl implements XSDModel {


//    private Document schema;

    // map : namespace --> schema
    private Map<Namespace, Document> schemas = new HashMap<>();

    // map : prefix --> namespace
    protected Map<String,Namespace> prefixMap = new HashMap<>();

    // map : namespace (String) --> prefix
    protected Map<String,String> reversePrefixMap = new HashMap<>();

    // map : namespace --> schema file
    protected Map<Namespace, String> schemaLocations = new HashMap<>();

    protected static List<String> knownPrefixes = Collections.unmodifiableList( Arrays.asList( "owl",
                                                                                             "xsi",
                                                                                             "xsd" ) );

    XSDModelImpl() {

    }


    @Override
    public void initFromBaseModel( OntoModel base ) {
        super.initFromBaseModel(base);

        setNamespace( "xsd", "http://www.w3.org/2001/XMLSchema" );
        setNamespace( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        setNamespace( "tns", base.getDefaultNamespace() );
//        setNamespace( "xjc", "http://java.sun.com/xml/ns/jaxb/xjc" );

        schemas.put( getNamespace( "tns" ), initDocument( this.getDefaultNamespace() ) );
    }

    private Document initDocument( String tgtNamespace ) {
        Document dox = new Document();

        Element root = new Element( "schema", getNamespace( "xsd" ) );

        root.setAttribute( "elementFormDefault", "qualified" );
        root.setAttribute( "targetNamespace", NamespaceUtils.removeLastSeparator( tgtNamespace ) );

        for ( Namespace ns : prefixMap.values() ) {
            root.addNamespaceDeclaration( ns );
        }

        dox.addContent( root );

	    OWLOntology current = getOntology().getOWLOntologyManager().getOntology( IRI.create( tgtNamespace ) );
	    if ( current != null ) {
		    System.out.println( current.getOntologyID() + " imports " + current.imports() );
		    current.imports().forEach( ( onto ) -> {
			    System.out.println( " \t\t importing " + Namespace.getNamespace( IRIUtils.ontologyNamespace( onto ) ) );
			    String ns = IRIUtils.ontologyNamespace( onto );
			    if ( getNamespace( ns ) != null ) {
				    addImport( dox, Namespace.getNamespace( ns ) );
			    }
		    } );

		    if ( !getNamespace( "tns" ).getURI().equals( NamespaceUtils.removeLastSeparator( tgtNamespace ) ) ) {
			    addImport( dox, getNamespace( "tns" ), true );
		    }
	    }
        return dox;
    }


	public boolean streamAll( OutputStream os ) {
        try {
            os.write( getOWLSchema().getBytes() );
            for ( Namespace ns : schemas.keySet() ) {
	            os.write( serialize( schemas.get( ns ) ).getBytes() );
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
        return true;
    }

    private Optional<File> toSchemaFile( String folderPath, String prefix ) {
	    if ( "owl".equals( prefix ) ) {
		    return Optional.of( new File( folderPath + File.separator + "owlThing.xsd" ) );
	    } else if ( knownPrefixes.contains( prefix ) ) {
		    return Optional.empty();
	    } else if ( "tns".equals( prefix ) ) {
		    return Optional.of( new File( folderPath + File.separator + getDefaultPackage() + ".xsd" ) );
	    } else {
		    String path = folderPath + File.separator + NameUtils.namespaceURIToPackage( prefixMap.get( prefix ).getURI() ) + ".xsd";
		    return Optional.of( new File( path ) );
	    }
    }

    public boolean stream( File folder ) {
        try {

			// 'mocks' of pre-existing schemas that have been extended have been built at this point.
	        // these 'mocks' are used to anchor the extending types. Any reference to the mocks must be
	        // rewired to the actual, full schemas, and the mocks discarded
	        Collection<String> overrides = new ArrayList<>();
	        prefixMap.keySet().stream()
	                 .filter( (pfx) -> ! knownPrefixes.contains( pfx ) )
	                 .forEach( (prefix) -> {
		                 toSchemaFile( folder.getPath(), prefix )
				                 .map( (tgtFile) -> checkForSchemaOverride( tgtFile, prefixMap.get( prefix ) )
						                 .map( overrides::add ) );
	                 });


	        for ( String prefix : prefixMap.keySet() ) {
                FileOutputStream os = null;
                byte[] schemaBytes = new byte[0];
		        Optional<File> target;

                if ( overrides.contains( prefix ) ) {
                	// do not write 'mock' schemas that have already been detached
                	continue;
                }

                target = toSchemaFile( folder.getPath(), prefix );

		        if ( "owl".equals( prefix ) ) {
			        File owlSchemaFile = target.orElseThrow( IllegalStateException::new );
		        	if ( ! owlSchemaFile.exists() ) {
				        os = new FileOutputStream( owlSchemaFile );
				        schemaBytes = getOWLSchema().getBytes();
			        }
		        } else if ( knownPrefixes.contains( prefix ) ) {
                    continue;
                } else {
					File schemaFile = target.orElseThrow( IllegalStateException::new );
			        os = new FileOutputStream( schemaFile );
			        if ( "tns".equals( prefix ) ) {
				        schemaBytes = serialize( getXSDSchema( prefixMap.get( "tns" ) ) ).getBytes();
			        } else {
			        	Namespace ns = prefixMap.get( prefix );
			        	if ( ! schemas.containsKey( ns ) ) {
					        throw new IllegalStateException( "XSD Model stream : unrecognized namespace " + ns );
				        }
			            schemaBytes = serialize( schemas.get( ns ) ).getBytes();
			        }
                }

                if ( os != null  ) {
                    schemaLocations.put( prefixMap.get( prefix ),
                                         target.orElseThrow( IllegalStateException::new ).getPath() );
	                System.err.println( "Serializing XSD " + toSchemaFile( "...", prefix).orElse( null ) );
					System.err.println( new String( schemaBytes ) );
	                os.write( schemaBytes );
                    os.flush();
                    os.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Optional<String> checkForSchemaOverride( File tgt, Namespace namespace ) {
        int j = 0;
        File target = tgt;
        String path = target.getPath();
        String ns = namespace.getURI();
        boolean override = false;

        while ( target.exists() ) {
            target = new File( path.replace( ".xsd", "_" + ( j++ ) + ".xsd" ) );
            override = true;
        }
        if ( override ) {
	        final String fName = tgt.getName();
	        for ( Document dox : schemas.values() ) {
		        List<?> imports = dox.getRootElement().getChildren( "import", NamespaceUtils.getNamespaceByPrefix( "xsd" ).orElseThrow( UnsupportedOperationException::new ) );
		        imports.stream().filter( Element.class::isInstance ).map( Element.class::cast ).forEach( (el) -> {
			        if ( el.getAttributeValue( "namespace" ).equals( ns ) ) {
				        el.setAttribute( "schemaLocation", fName );
			        }
		        });
	        }
        }

        return override ? Optional.of( namespace.getPrefix() ) : Optional.empty();
    }

    public Map<String, Namespace> getAssignedPrefixes() {
        return prefixMap;
    }

    public Namespace getNamespace(String ns) {
        return prefixMap.get( ns );
    }

    public Collection<Namespace> getNamespaces() {
        return prefixMap.values();
    }


    public void setNamespace( String prefix, String namespace ) {
        namespace = NamespaceUtils.removeLastSeparator( namespace );

        Namespace ns = Namespace.getNamespace( prefix, namespace );
        prefixMap.put( prefix, ns );
        reversePrefixMap.put( namespace, prefix );

        for ( Document dox : schemas.values() ) {
            dox.getRootElement().addNamespaceDeclaration( ns );
        }
    }


    public void addTrait( String name, Object trait ) {

        XSDTypeDescr descr = (XSDTypeDescr) trait;

        String prefix = reversePrefixMap.get( descr.getNamespace() );
        Document schema = getXSDSchema( prefixMap.get( prefix ) );

        schema.getRootElement().addContent( descr.getDeclaration() );
        schema.getRootElement().addContent( descr.getDefinition() );

        // now resolve required imports
        for ( String depNs : descr.getDependencies() ) {
            if ( ! reversePrefixMap.containsKey( depNs ) ) {
                String px = mapNamespaceToPrefix( depNs );
                createXSDSchema( Namespace.getNamespace( px, depNs ) );
            }
            addImport( schema, prefixMap.get( reversePrefixMap.get( depNs ) ), true );
        }

    }

    public Document getXSDSchema() {
        return schemas.get( getNamespace( "tns" ) );
    }

//    private Document getXSDSchema( String type ) {
//        if ( type.indexOf( ":" ) >= 0 ) {
//            return getXSDSchema();
//        }
//        if ( definesType( getXSDSchema(), type ) ) {
//            return getXSDSchema();
//        }
//        for ( Document sub : subSchemas.values() ) {
//            if ( definesType(sub, type) ) {
//                return sub;
//            }
//        }
//        return getXSDSchema();
//    }

    public Document getXSDSchema( Namespace altNamespace ) {
        return schemas.containsKey( altNamespace ) ?
            schemas.get( altNamespace ) : createXSDSchema( altNamespace );
    }

    private Document createXSDSchema( Namespace altNamespace ) {
        Document dox = initDocument( altNamespace.getURI() );
        schemas.put( altNamespace, dox );
        return dox;
    }




    public void addImport( Document dox, Namespace altNamespace ) {
        addImport( dox, altNamespace, false );
    }

    public void addImport( Document dox, Namespace altNamespace, boolean forceTNS ) {
        if ( ! forceTNS && altNamespace.equals( getNamespace( "tns" ) ) ) {
            return;
        }
        List<?> imports = dox.getRootElement().getChildren( "import", NamespaceUtils.getNamespaceByPrefix( "xsd" ).orElseThrow( UnsupportedOperationException::new ) );
        for ( Object e : imports ) {
            if ( ((Element) e).getAttributeValue( "namespace" ).equals( altNamespace.getURI() ) ) {
                return;
            }
        }

        Element imp = new Element( "import", getNamespace( "xsd" ) );
            imp.setAttribute( "namespace", altNamespace.getURI() );
            imp.setAttribute( "schemaLocation", getSchemaName( altNamespace ) );

        dox.getRootElement().addContent( 0, imp );
    }

    private String getSchemaName( Namespace altNamespace ) {
        if ( NamespaceUtils.getNamespaceByPrefix( "owl" ).orElseThrow( UnsupportedOperationException::new ).getURI().equals( altNamespace.getURI() ) ) {
            return "owlThing.xsd";
        } else {
//            String prefix = NamespaceUtils.compareNamespaces( altNamespace.getURI(), getDefaultNamespace() )
//                    ? ""
//                    : ( "_" + altNamespace.getPrefix() );
//            return getName() + prefix + ".xsd";
            return NameUtils.namespaceURIToPackage( altNamespace.getURI() ) + ".xsd";
        }
    }


    public String mapNamespaceToPrefix( String namespace ) {
        namespace = NamespaceUtils.removeLastSeparator( namespace );
        String prefix;
        if ( StringUtils.isEmpty( namespace ) ) {
            prefix = "tns";
        } else if ( NamespaceUtils.compareNamespaces( getDefaultNamespace(), namespace ) ) {
            prefix = "tns";
        } else if ( reversePrefixMap.containsKey( namespace ) ) {
            prefix = reversePrefixMap.get( namespace );
        } else if ( NamespaceUtils.isKnownSchema(namespace) ) {
            prefix = NamespaceUtils.getPrefix( namespace ).orElseThrow( IllegalStateException::new );
            reversePrefixMap.put( namespace, prefix );
            setNamespace( prefix, namespace );
        } else {
            prefix = "ns" + ( reversePrefixMap.size() + 1 );
            reversePrefixMap.put( namespace, prefix );
            setNamespace( prefix, namespace );
        }
        return prefix;
    }


    public Object getTrait(String name) {
        return null;
    }



    public Set<String> getTraitNames() {
	    //TODO
        return new HashSet<>();
    }


    @Override
    protected String traitsToString() {
        return serialize( getXSDSchema() );
    }


    protected String serialize( Document dox ) {
        StringWriter out = new StringWriter();
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat() );
        try {
            outputter.output( dox, out );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toString();
    }

    public String getOWLSchema() {
        InputStream schemaIS;
        try {
            schemaIS = ResourceFactory.newClassPathResource( "org/kie/semantics/builder/model/compilers/owlThing.xsd" ).getInputStream();
            int n = schemaIS.available();
            byte[] data = new byte[ n ];
            if ( n == schemaIS.read( data ) ) {
	            return new String( data );
            } else {
            	return "";
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
    }








    public static class XSDTypeDescr {

        private String name;
        private String namespace;
        private String effectiveName;
        private String effectiveType;

        private Element declaration;
        private Element definition;

        private Set<String> dependencies;

        public XSDTypeDescr( String name, String namespace, String effectiveName, String effectiveType, Element declaration, Element definition, Set<String> dependencies ) {
            this.name = name;
            this.namespace = namespace;
            this.effectiveName = effectiveName;
            this.effectiveType = effectiveType;
            this.declaration = declaration;
            this.definition = definition;
            this.dependencies = dependencies;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getEffectiveName() {
            return effectiveName;
        }

        public String getEffectiveType() {
            return effectiveType;
        }

        public Element getDeclaration() {
            return declaration;
        }

        public Element getDefinition() {
            return definition;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }
    }

}
