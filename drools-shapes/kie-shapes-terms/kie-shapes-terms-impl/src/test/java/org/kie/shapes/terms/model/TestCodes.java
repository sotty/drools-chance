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

public enum TestCodes implements ConceptDescriptor, Taxonomic<TestCodes> {

	Disease( "99", "disease", null ),
	EndocrineSystemDisease( "99.1", "endocrine system disease", Disease ),
	Diabetes( "99.1.2", "diabetes", EndocrineSystemDisease, Disease ),
	GestationalDiabetes( "99.1.2.3", "gestational diabetes", Diabetes, EndocrineSystemDisease, Disease ),
	AcuteDisease( "2704003", "acute disease", Disease );

	public static final String codeSystemName = "TestCodes";
	public static final String codeSystem = "0.0";
	public static final URI codeSystemURI = URI.create( "uri:urn:0.0" );

	public static final ConceptScheme<TestCodes> SELF = new DefaultConceptScheme<>( codeSystem,
	                                                                                codeSystemName,
	                                                                                codeSystemURI,
	                                                                                TestCodes.class );

	private URI	uri;
	private String displayName;
	private String code;

	private TestCodes[] ancestors;

	TestCodes( String code, String displayName, TestCodes... ancestors ) {
		this.uri = URI.create( "urn:oid:" + code );
		this.code = code;
		this.displayName = displayName;
		this.ancestors = ancestors == null ? new TestCodes[ 0 ] : ancestors;
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
	public ConceptScheme<TestCodes> getCodeSystem() {
		return SELF;
	}

	public TestCodes[] getAncestors() {
		return ancestors;
	}
}
