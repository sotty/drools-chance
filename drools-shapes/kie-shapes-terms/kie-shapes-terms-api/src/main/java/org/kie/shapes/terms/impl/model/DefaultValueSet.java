package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ValueSet;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

public class DefaultValueSet<T extends Enum<T> & ConceptDescriptor> extends AbstractValueSet implements ValueSet {

	private EnumSet<T> concepts;

	public DefaultValueSet( String id, String name, Class<T> type, T... concepts ) {
		this( id, name, URI.create( "urn:oid:" + id ), type, concepts );
	}

	public DefaultValueSet( String id, String name, URI uri, Class<T> type, T... concepts ) {
		super( id, name, uri );
		switch ( concepts.length ) {
			case 0:
				this.concepts = EnumSet.noneOf( type );
				break;
			case 1:
				init( concepts[0] );
				break;
			default:
				init( concepts[0], Arrays.copyOfRange( concepts, 1, concepts.length ) );
		}
	}

	public DefaultValueSet( String id, String name, Class<T> type, T pivot, T... concepts ) {
		this( id, name, URI.create( "urn:oid:" + id ), type, pivot, concepts );
	}

	public DefaultValueSet( String id, String name, URI uri, Class<T> type, T pivot, T... concepts ) {
		this( id, name, uri, type, concepts );
		setPivotalConcept( pivot );
		init( pivot, concepts );
	}

	private void init( T pivot, T... concepts ) {
		this.concepts = EnumSet.of( pivot, concepts );
	}

	public boolean contains( ConceptDescriptor cd ) {
		return this.concepts.contains( cd );
	}

	@Override
	public Stream<ConceptDescriptor> getConcepts() {
		return concepts.stream()
		               .map( ConceptDescriptor.class::cast );
	}


}
