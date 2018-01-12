/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.model.datatypes;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.impl.model.DefaultConceptCoding;

import java.net.URI;


public class CD extends DefaultConceptCoding {

	public CD( String code, String displayName, URI uri, ConceptScheme codeSystem ) {
		super( code, displayName, uri, codeSystem );
	}

	public CD( String code, String displayName, ConceptScheme codeSystem ) {
		super( code, displayName, codeSystem );
	}

	public CD( ConceptDescriptor other ) {
		super( other );
	}
}
