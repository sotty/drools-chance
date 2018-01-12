package org.kie.shapes.terms;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public interface ConceptScheme<T extends ConceptDescriptor> extends ConceptBase {

	default String getName() {
		return getSchemeName();
	}

	String getSchemeName();

	default String getCode() {
		return getSchemeID();
	}

	String getSchemeID();

	default URI getUri() {
		return getSchemeURI();
	}

	URI getSchemeURI();

	Stream<T> getConcepts();

	boolean subsumes( T other, T entity );

	Optional<T> lookup( ConceptDescriptor other );
}
