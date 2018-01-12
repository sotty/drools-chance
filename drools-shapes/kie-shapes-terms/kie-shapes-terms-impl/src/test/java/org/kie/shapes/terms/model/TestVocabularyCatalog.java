/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.model;

import org.kie.shapes.terms.VocabularyCatalog;
import org.kie.shapes.terms.impl.model.DefaultVocabularyCatalog;

public class TestVocabularyCatalog extends DefaultVocabularyCatalog {

    private static final VocabularyCatalog SELF = new TestVocabularyCatalog();

    public static VocabularyCatalog get() {
        return SELF;
    }
}
