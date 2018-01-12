package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ValueSet;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MixedValueSet extends AbstractValueSet<ConceptDescriptor> implements ValueSet {

	private Set<ConceptDescriptor> concepts;

	public MixedValueSet( String id, String name, ConceptDescriptor... concepts ) {
		this( id, name, URI.create( "urn:oid:" + id ), concepts );
	}

	public MixedValueSet( String id, String name, URI uri, ConceptDescriptor... concepts ) {
		this( id, name, uri, null, concepts );
	}

	public MixedValueSet( String id, String name, ConceptDescriptor pivot, ConceptDescriptor... concepts ) {
		this( id, name, URI.create( "urn:oid:" + id ), pivot, concepts );
	}

	public MixedValueSet( String id, String name, URI uri, ConceptDescriptor pivot, ConceptDescriptor... concepts ) {
		super( id, name, uri );
		setPivotalConcept( pivot );
		this.concepts = new HashSet<>();
		this.concepts.addAll( Arrays.asList( concepts ) );
	}


	public boolean contains( ConceptDescriptor cd ) {
		return this.concepts.contains( cd );
	}

	@Override
	public Stream<ConceptDescriptor> getConcepts() {
		return concepts.stream();
	}

}
