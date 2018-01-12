package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.Taxonomic;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultConceptScheme<T extends Enum<T> & Taxonomic<T> & ConceptDescriptor> implements ConceptScheme<T> {

	private EnumSet<T> concepts;
	private Class<T> type;
	private EnumMap<T,EnumSet<T>> ancestry;

    private String schemeName;

    private URI schemeURI;

    private String schemeID;

    private DefaultConceptScheme() {
        super();
    }

    public DefaultConceptScheme( final String schemeID, final String schemeName, final URI schemeURI, final Class<T> type ) {
        this.schemeID = schemeID;
        this.schemeName = schemeName;
        this.schemeURI = schemeURI;
        this.type = type;
        this.concepts = EnumSet.allOf( type );
	    ancestry = this.concepts.stream()
	                            .collect( Collectors.toMap( Function.identity(),
	                                                        this::toAncestorSet,
	                                                        (k1,k2) -> k1,
	                                                        () -> new EnumMap<>( type ) ) );
    }

	private EnumSet<T> toAncestorSet( T t ) {
    	T[] ancestors = t.getAncestors();
    	return ancestors.length == 0
			    ? EnumSet.noneOf( type )
			    : EnumSet.of( ancestors[0], Arrays.copyOfRange( ancestors, 1, ancestors.length ) ) ;
	}

	public Stream<T> getConcepts() {
    	return concepts.stream();
    }

	@Override
	public boolean subsumes( T sup, T sub ) {
		return ancestry.get( sub ).contains( sup );
	}

	@Override
	public Optional<T> lookup( ConceptDescriptor cd ) {
		if ( type.isInstance( cd ) ) {
			return Optional.of( type.cast( cd ) );
		} else {
			return concepts.stream()
			               .filter( (t) -> t.getUri().equals( cd.getUri() ) )
			               .findAny();
		}
	}

	public String getSchemeName() {
        return schemeName;
    }

    public URI getSchemeURI() {
        return schemeURI;
    }

    public String getSchemeID() {
        return schemeID;
    }

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) return true;
		if ( !( o instanceof DefaultConceptScheme ) ) return false;
		DefaultConceptScheme<?> that = ( DefaultConceptScheme<?> ) o;
		return Objects.equals( schemeURI, that.schemeURI ) &&
				Objects.equals( schemeID, that.schemeID );
	}

	@Override
	public int hashCode() {
		return Objects.hash( schemeURI, schemeID );
	}

	@Override
    public String toString() {
        return "ConceptScheme{ " +
               "URI = '" + schemeURI + "( " + concepts.size() + " )" +
               '}';
    }

}
