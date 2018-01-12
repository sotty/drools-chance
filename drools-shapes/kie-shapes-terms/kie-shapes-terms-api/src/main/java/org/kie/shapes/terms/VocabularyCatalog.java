/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms;

import org.kie.shapes.terms.ConceptScheme;

import java.net.URI;
import java.util.Optional;

/**
 * Catalog of Concept Schemes
 */
public interface VocabularyCatalog {

    void register( URI schemeURI, ConceptScheme scheme );

    Optional<ConceptScheme> resolve( URI schemeURI );

    Optional<ConceptScheme> resolve( String schemeID );

    Optional<URI> lookupURI( String schemeID );

}
