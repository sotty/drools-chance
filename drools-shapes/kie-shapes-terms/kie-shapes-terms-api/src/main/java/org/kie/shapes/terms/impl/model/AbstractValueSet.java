package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ValueSet;

import java.net.URI;
import java.util.Optional;

public abstract class AbstractValueSet<T extends ConceptDescriptor> implements ValueSet {

	private String id;
	private URI uri;
	private String name;

	private T pivotalConcept;

	protected AbstractValueSet( String id, String name, URI uri ) {
		this.id = id;
		this.uri = uri;
		this.name = name;
	}

	protected void setPivotalConcept( T pivotalConcept ) {
		this.pivotalConcept = pivotalConcept;
	}

	@Override
	public Optional<ConceptDescriptor> getPivotalConcept() {
		return Optional.ofNullable( pivotalConcept );
	}

	@Override
	public String getCode() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public URI getUri() {
		return uri;
	}
}
