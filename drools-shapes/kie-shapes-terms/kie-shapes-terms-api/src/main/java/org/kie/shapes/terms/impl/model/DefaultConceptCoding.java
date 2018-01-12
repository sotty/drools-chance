/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.impl.model;


import org.kie.shapes.terms.ConceptBase;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;

import java.net.URI;

public class DefaultConceptCoding implements ConceptDescriptor {

    private final String code;
    private final String displayName;
    private final URI uri;

    private final ConceptScheme codeSystem;

	public DefaultConceptCoding( ConceptDescriptor other ) {
		this( other.getCode(), other.getName(), other.getUri(), other.getCodeSystem() );
	}

	public DefaultConceptCoding( String code, String displayName, ConceptScheme codeSystem ) {
		this( code,
		      displayName,
		      URI.create( codeSystem.getSchemeURI().toString() + "#" + code ),
		      codeSystem );
	}

	public DefaultConceptCoding( String code, String displayName, URI uri, ConceptScheme codeSystem ) {
		this.code = code;
		this.displayName = displayName;
		this.uri = uri;
		this.codeSystem = codeSystem;
	}

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public ConceptScheme getCodeSystem() {
        return codeSystem;
    }

    @Override
    public URI getUri() {
        return uri;
    }

	@Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( !( o instanceof ConceptDescriptor ) ) return false;

        ConceptDescriptor cd = (ConceptDescriptor) o;

	    return uri.equals( cd.getUri() );
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    @Override
    public String toString() {
        return "CD{" +
               "code='" + code + '\'' +
               ", displayName='" + displayName + '\'' +
               '}';
    }
}
