package org.kie.shapes.terms;

public interface ConceptDescriptor extends ConceptBase {

	<T extends ConceptDescriptor> ConceptScheme<T> getCodeSystem();

	default java.lang.String getCodeSystemId() {
		return getCodeSystem().getCode();
	}

	default java.lang.String getCodeSystemName() {
		return getCodeSystem().getName();
	}

	default java.net.URI getCodeSystemUri() {
		return getCodeSystem().getUri();
	}

}