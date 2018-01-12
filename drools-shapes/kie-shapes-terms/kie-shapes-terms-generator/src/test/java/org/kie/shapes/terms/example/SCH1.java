package org.kie.shapes.terms.example;

import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.Taxonomic;

import java.net.URI;

/*
	Example of generated 'terminology' class
*
* */
public enum SCH1 implements ConceptDescriptor, Taxonomic<SCH1> {


	Specific_Concept( "6789", "specific_concept", "http://test/generator#specific_concept" ),
	Nested_Specific_Concept( "12345", "nested_specific_concept", "http://test/generator#nested_specific_concept" );


	public static final String schemeName = "SCH1";
	public static final String schemeID = "0.0.0.0";
	public static final URI schemeURI = URI.create( "http://test/generator#concept_scheme1" );

	public static final ConceptScheme<SCH1> __SELF = new org.kie.shapes.terms.impl.model.DefaultConceptScheme<>( schemeID,
	                                                                                                             schemeName,
	                                                                                                             schemeURI,
	                                                                                                             SCH1.class );

	private URI	uri;
	private String displayName;
	private String code;

	private SCH1[] ancestors;

	SCH1( String code, String displayName, String uri, SCH1... ancestors ) {
		this.uri = URI.create( uri );
		this.code = code;
		this.displayName = displayName;
		this.ancestors = ancestors == null ? new SCH1[ 0 ] : ancestors;
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
	public ConceptScheme<SCH1> getCodeSystem() {
		return __SELF;
	}

	public SCH1[] getAncestors() {
		return ancestors;
	}

}


