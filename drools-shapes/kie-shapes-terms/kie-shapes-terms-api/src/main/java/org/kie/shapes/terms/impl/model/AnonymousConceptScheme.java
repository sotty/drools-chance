package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public class AnonymousConceptScheme implements ConceptScheme<ConceptDescriptor> {

	private URI scheme;
	private String name;
	private String code;

	public AnonymousConceptScheme( URI scheme, String code, String label ) {
		this.scheme = scheme;
		this.name = label;
		this.code = code;
	}

	public AnonymousConceptScheme( URI scheme ) {
		this.scheme = scheme;
		this.name = scheme.getFragment();
		this.code = scheme.getFragment();
	}

	@Override
	public String getSchemeName() {
		return name;
	}

	@Override
	public String getSchemeID() {
		return code;
	}

	@Override
	public URI getSchemeURI() {
		return scheme;
	}

	@Override
	public Stream<ConceptDescriptor> getConcepts() {
		throw new UnsupportedOperationException( "Unable to track Concepts in Anonymous Scheme" );
	}

	@Override
	public boolean subsumes( ConceptDescriptor other, ConceptDescriptor entity ) {
		throw new UnsupportedOperationException( "Unable to compare Concepts in Anonymous Scheme" );
	}

	@Override
	public Optional<ConceptDescriptor> lookup( ConceptDescriptor other ) {
		return Optional.ofNullable( other );
	}
}
