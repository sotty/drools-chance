package org.kie.semantics.utils;


import org.jdom.Namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NamespaceUtils {

    private static final String OWL_PACKAGE = "org.w3._2002._07.owl";

    private static Map<String, Namespace> knownNamespaces;

    static {
        knownNamespaces = new HashMap<>();
        knownNamespaces.put( Namespace.XML_NAMESPACE.getURI(), Namespace.XML_NAMESPACE );
        knownNamespaces.put( Namespace.NO_NAMESPACE.getURI(), Namespace.NO_NAMESPACE );
        knownNamespaces.put( "http://www.w3.org/2002/07/owl", Namespace.getNamespace( "owl", "http://www.w3.org/2002/07/owl" ) );
        knownNamespaces.put( "http://www.w3.org/2001/XMLSchema", Namespace.getNamespace( "xsd", "http://www.w3.org/2001/XMLSchema" ) );
        knownNamespaces.put( "http://www.w3.org/1999/02/22-rdf-syntax-ns", Namespace.getNamespace( "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns" ) );
    }

    public static boolean isKnownSchema( String namespace ) {
        return knownNamespaces.containsKey( namespace );
    }

    public static Optional<String> getPrefix( String namespace ) {
        if ( knownNamespaces.containsKey( namespace ) ) {
            return Optional.of( knownNamespaces.get( namespace ).getPrefix() );
        } else {
            return Optional.empty();
        }
    }

    public static Optional<Namespace> getNamespaceByPrefix( String prefix ) {
        return knownNamespaces.values().stream()
                              .filter( (ns) -> ns.getPrefix().equals( prefix ) )
                              .findFirst();
    }

	public static String getHashedNamespaceByPrefix( String prefix ) {
		return getNamespaceByPrefix( prefix ).map( (n) -> n.getURI() + "#" ).orElse( prefix + "#" );
	}


	public static boolean compareNamespaces( String ns1, String ns2 ) {
        if ( ns1 == null ) {
            return ( ns2 == null );
        }
        ns1 = removeLastSeparator( ns1 );
        ns2 = removeLastSeparator( ns2 );
	    return ns1.equals( ns2 );

    }

    public static String removeLastSeparator( String ns1 ) {
        if ( ns1.endsWith( "/" ) || ns1.endsWith( "#" ) ) {
            return ns1.substring( 0, ns1.length() - 1 );
        }
        return ns1;
    }

    public static boolean isOWL( String packageName ) {
        return OWL_PACKAGE.equals( packageName );
    }

}
