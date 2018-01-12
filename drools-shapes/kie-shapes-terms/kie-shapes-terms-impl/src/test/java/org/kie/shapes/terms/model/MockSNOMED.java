/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.kie.shapes.terms.model;


import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.Taxonomic;
import org.kie.shapes.terms.impl.model.DefaultConceptScheme;

import java.net.URI;

public enum MockSNOMED implements ConceptDescriptor, Taxonomic<MockSNOMED> {

	MedicalProblem( "33070002", "medical problem" ),
	AcuteDisease( "2704003", "acute disease", MedicalProblem ),
	DiabetesTypeII( "44054006", "diabetes type II", MedicalProblem );

	public static final String codeSystemName = "Mock-MockSNOMED-CT";
	public static final String codeSystem = "mockscId";
	public static final URI codeSystemURI = URI.create( "http://snomock.ct" );

	public static final ConceptScheme<MockSNOMED> SELF = new DefaultConceptScheme<>( codeSystem,
	                                                                                 codeSystemName,
	                                                                                 codeSystemURI,
	                                                                                 MockSNOMED.class );

	private URI	uri;
	private String displayName;
	private String code;

	private MockSNOMED[] ancestors;

	MockSNOMED( String code, String displayName, MockSNOMED... ancestors ) {
		this.uri = URI.create( "urn:oid:" + code );
		this.code = code;
		this.displayName = displayName;
		this.ancestors = ancestors == null ? new MockSNOMED[ 0 ] : ancestors;
	}

	@Override
	public String getName() {
		return displayName;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public ConceptScheme<MockSNOMED> getCodeSystem() {
		return SELF;
	}

	public MockSNOMED[] getAncestors() {
		return ancestors;
	}
}


