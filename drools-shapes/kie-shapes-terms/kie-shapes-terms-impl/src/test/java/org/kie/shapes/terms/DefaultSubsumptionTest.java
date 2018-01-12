package org.kie.shapes.terms;

import org.junit.Test;
import org.kie.shapes.terms.api.Terms;
import org.kie.shapes.terms.impl.DefaultTermsServiceImpl;
import org.kie.shapes.terms.impl.model.DefaultValueSet;
import org.kie.shapes.terms.model.MockSNOMED;
import org.kie.shapes.terms.model.TestCodes;
import org.kie.shapes.terms.model.datatypes.CD;

import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultSubsumptionTest {

	private Terms termsInferenceService;

	@Test
	public void testInValueSet() {
		DefaultTermsServiceImpl evaluator = new DefaultTermsServiceImpl();

		ConceptDescriptor left = MockSNOMED.DiabetesTypeII;

		ValueSet right = new DefaultValueSet<>( "0.0",
		                                        "mock",
		                                        MockSNOMED.class,
		                                        new MockSNOMED[] {
				                                        MockSNOMED.DiabetesTypeII,
				                                        MockSNOMED.AcuteDisease,
				                                        MockSNOMED.MedicalProblem } );

		assertTrue( evaluator.denotes( left, right, null ) );
	}

	@Test
	public void testNotInValueSet() {
		DefaultTermsServiceImpl evaluator = new DefaultTermsServiceImpl();
		ConceptDescriptor left = TestCodes.Diabetes;

		ValueSet right = new DefaultValueSet<>( "nil", "empty", TestCodes.class );

		assertFalse( evaluator.denotes( left, right, null ) );
	}

	@Test
	public void testEntitySubsumes() {
		DefaultTermsServiceImpl evaluator = new DefaultTermsServiceImpl();
		ConceptDescriptor left = MockSNOMED.DiabetesTypeII;

		ConceptDescriptor right = MockSNOMED.MedicalProblem;

		assertTrue( evaluator.denotes( left, right, null ) );
	}

	@Test
	public void testEntitySelfSubsumes() {
		DefaultTermsServiceImpl evaluator = new DefaultTermsServiceImpl();
		ConceptDescriptor left = MockSNOMED.DiabetesTypeII;

		ConceptDescriptor right = MockSNOMED.DiabetesTypeII;

		assertTrue(evaluator.denotes(left, right, null));
	}

	@Test
	public void testEntityNotSubsumes() {
		DefaultTermsServiceImpl evaluator = new DefaultTermsServiceImpl();

		ConceptDescriptor left = MockSNOMED.DiabetesTypeII;

		ConceptDescriptor right = new CD( "zzz", "zzz", URI.create( "urn:zzz" ), MockSNOMED.SELF );

		assertFalse(evaluator.denotes(left, right, null));
	}


}
