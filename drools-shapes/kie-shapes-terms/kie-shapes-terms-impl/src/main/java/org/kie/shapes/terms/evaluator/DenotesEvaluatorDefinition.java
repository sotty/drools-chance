package org.kie.shapes.terms.evaluator;


import com.clarkparsia.empire.annotation.RdfProperty;
import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.factmodel.traits.TraitProxy;
import org.drools.core.rule.VariableRestriction;
import org.drools.core.spi.Evaluator;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;
import org.kie.shapes.terms.ConceptBase;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.TermsInferenceServiceFactory;
import org.kie.shapes.terms.api.Terms;

import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Custom Drools 'denotes' operator.
 */
public class DenotesEvaluatorDefinition implements EvaluatorDefinition {

    private static final String DENOTES_OP = "denotes";

    private DenotesEvaluator denotesEval = new DenotesEvaluator(
		    Operator.addOperatorToRegistry( DENOTES_OP, false )
    );

    private DenotesEvaluator notDenotesEval = new DenotesEvaluator(
    		Operator.addOperatorToRegistry(DENOTES_OP, true)
    );

    public DenotesEvaluatorDefinition() {
        super();
    }


    @Override
    public String[] getEvaluatorIds() {
        return new String[]{ DENOTES_OP };
    }

    @Override
    public boolean isNegatable() {
        return true;
    }

    @Override
    public Evaluator getEvaluator( ValueType type, String operatorId, boolean isNegated, String parameterText, Target leftTarget, Target rightTarget ) {
        return isNegated ? notDenotesEval : denotesEval;
    }

    @Override
    public Evaluator getEvaluator(ValueType type, String operatorId, boolean isNegated, String parameterText) {
    	return isNegated ? notDenotesEval : denotesEval;
    }

    @Override
    public Evaluator getEvaluator(ValueType type, Operator operator, String parameterText) {
    	return operator.isNegated() ? notDenotesEval : denotesEval;
    }

    @Override
    public Evaluator getEvaluator(ValueType type, Operator operator) {
    	return operator.isNegated() ? notDenotesEval : denotesEval;
    }

    @Override
    public boolean supportsType(ValueType type) {
        return true;
    }

    @Override
    public Target getTarget() {
        return Target.BOTH;
    }

    @Override
    public void writeExternal(ObjectOutput out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readExternal(ObjectInput in) {
        throw new UnsupportedOperationException();
    }

	public static class DenotesEvaluator extends BaseEvaluator {

        private Terms eval;

        public DenotesEvaluator(Operator operator) {
            super(ValueType.BOOLEAN_TYPE, operator);
            // TODO This should be kind-configurable
            eval = TermsInferenceServiceFactory.instance().getTerminologyService();
		}

		@Override
        public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value ) {
            Object right = value.getValue();
        	Object left = extractor.getValue( factHandle.getObject() );

            return this.evaluate( left, right, workingMemory, extractor );
        }

        private String getPropertyURI( InternalReadAccessor extractor ) {
            RdfProperty ann = extractor.getNativeReadMethod().getAnnotation( RdfProperty.class );
            return ann != null ? ann.value() : null;
        }

        @Override
        public boolean evaluate( InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle leftFh, InternalReadAccessor rightExtractor, InternalFactHandle rightFh ) {
            Object left = leftExtractor.getValue( workingMemory, leftFh.getObject() );
            Object right = rightExtractor.getValue( workingMemory, rightFh.getObject() );
            return this.evaluate( left, right, workingMemory, rightExtractor );
        }

        @Override
        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableRestriction.VariableContextEntry context, InternalFactHandle right) {
            throw new UnsupportedOperationException( "TODO" );
        }

        @Override
        public boolean evaluateCachedRight( InternalWorkingMemory workingMemory, VariableRestriction.VariableContextEntry context, InternalFactHandle left ) {
            throw new UnsupportedOperationException( "TODO" );
        }

        private boolean evaluate( Object left, Object right, InternalWorkingMemory workingMemory, InternalReadAccessor extractor ) {
            if( right == null || left == null) {
                return this.getOperator().isNegated();
            }

            if( left instanceof TraitProxy ) {
                left = ( (TraitProxy) left ).getObject();
            }
            //TODO : these casts are potentially unsafe, but the check should be done at compile time, not at runtime

            boolean answer = false;

            answer = eval.denotes( (ConceptDescriptor) left , ( ConceptBase ) right, getPropertyURI( extractor ) );

            return this.getOperator().isNegated() != answer;
        }


    }

}
