package org.kie.shapes.terms;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface ValueSet extends ConceptBase {

	Optional<ConceptDescriptor> getPivotalConcept();

	Stream<ConceptDescriptor> getConcepts();

	default java.lang.String getValueSetId() {
		return getCode();
	}

	default java.lang.String getValueSetName() {
		return getName();
	}

	boolean contains( ConceptDescriptor cd );

}
