/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.api;

import org.kie.shapes.terms.ConceptBase;
import org.kie.shapes.terms.ConceptDescriptor;

public interface TermsInference {

    boolean denotes( ConceptDescriptor entity, ConceptBase complexConcept, String leftPropertyURI );

}
