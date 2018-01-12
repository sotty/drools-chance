/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.impl.model;

import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.VocabularyCatalog;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultVocabularyCatalog implements VocabularyCatalog {

    protected static final Map<URI,ConceptScheme> entries = new ConcurrentHashMap<>();

	@Override
	public void register( URI schemeURI, ConceptScheme scheme ) {
		entries.put( schemeURI, scheme );
	}

	@Override
	public Optional<ConceptScheme> resolve( URI schemeURI ) {
		return Optional.ofNullable( entries.get( schemeURI ) );
	}

	@Override
	public Optional<ConceptScheme> resolve( String schemeID ) {
		return entries.values().stream()
		              .filter( (s) -> schemeID.equals( s.getSchemeID() ) )
		              .findAny();
	}

	@Override
	public Optional<URI> lookupURI( String schemeID ) {
		return resolve( schemeID )
				.map( ConceptScheme::getSchemeURI );
	}
}
