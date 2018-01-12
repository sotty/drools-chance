package org.kie.shapes.terms.impl;

import org.kie.shapes.terms.ConceptBase;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.ValueSet;
import org.kie.shapes.terms.api.Terms;

import java.util.Optional;

public class DefaultTermsServiceImpl implements Terms {

	public static final String KIND = "default";

	@Override
	public boolean denotes( ConceptDescriptor entity, ConceptBase complexConcept, String leftPropertyURI ) {
		if ( complexConcept instanceof ValueSet ) {
			// it is a valueSet
			return ( ( ValueSet) complexConcept ).contains( entity );
		} else if ( complexConcept instanceof ConceptDescriptor ) {
			ConceptDescriptor other = (ConceptDescriptor) complexConcept;
			if ( entity.equals( other ) ) {
				return true;
			}
			if ( other.getCodeSystem().equals( entity.getCodeSystem() ) ) {
				Optional<ConceptDescriptor> left = entity.getCodeSystem().lookup( entity );
				Optional<ConceptDescriptor> right = other.getCodeSystem().lookup( other );
				return left.isPresent() && right.isPresent() && other.getCodeSystem().subsumes( right.get(),
				                                                                                left.get() );
			}
		}
		return false;
	}

	@Override
	public boolean mapsTo( ConceptDescriptor source, ConceptDescriptor target ) {
		return false;
	}
}
