package org.kie.shapes.terms;

import org.kie.shapes.terms.impl.model.DefaultConceptCoding;
import org.kie.shapes.terms.impl.model.AnonymousConceptScheme;

import java.net.URI;

public interface CodeFactory {

	ConceptDescriptor of( URI uri,
	                      String code,
	                      String codeName,
	                      String schemeID,
	                      String schemeName,
	                      URI schemeURI );

	static ConceptDescriptor of( String s ) {
		URI codeUri = URI.create( s );
		URI schemeURI = URI.create( codeUri.getRawSchemeSpecificPart() );

		return new DefaultConceptCoding( codeUri.getFragment(),
		                                 codeUri.getFragment(),
		                                 new AnonymousConceptScheme( schemeURI ) );
	}
}
