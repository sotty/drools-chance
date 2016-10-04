package org.drools.chance.reteoo.tuples;

import org.drools.chance.core.util.IntHashMap;
import org.drools.chance.degree.Degree;
import org.drools.chance.evaluation.Evaluation;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.reteoo.FromNodeLeftTuple;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.LeftTupleSink;
import org.drools.core.reteoo.RightTuple;


public class ImperfectFromNodeLeftTuple extends FromNodeLeftTuple implements ImperfectTuple {

    protected IntHashMap<Evaluation> evaluations = new IntHashMap<Evaluation>();
    protected Evaluation lastEvaluation;

    public ImperfectFromNodeLeftTuple() {
    }

    public ImperfectFromNodeLeftTuple(InternalFactHandle factHandle, LeftTupleSink sink, boolean leftTupleMemoryEnabled) {
        super(factHandle, sink, leftTupleMemoryEnabled);
    }

    public ImperfectFromNodeLeftTuple(LeftTuple leftTuple, RightTuple rightTuple, LeftTupleSink sink) {
        super(leftTuple, rightTuple, sink);
    }

    public ImperfectFromNodeLeftTuple(LeftTuple leftTuple, RightTuple rightTuple, LeftTupleSink sink, boolean leftTupleMemoryEnabled) {
        super(leftTuple, rightTuple, sink, leftTupleMemoryEnabled);
    }

    public ImperfectFromNodeLeftTuple(LeftTuple leftTuple, RightTuple rightTuple, LeftTuple currentLeftChild, LeftTuple currentRightChild, LeftTupleSink sink, boolean leftTupleMemoryEnabled) {
        super(leftTuple, rightTuple, currentLeftChild, currentRightChild, sink, leftTupleMemoryEnabled);
    }

    public Evaluation getEvaluation() {
        return lastEvaluation;
    }

    public Evaluation getCachedEvaluation( int idx ) {
        return evaluations.get( idx );
    }

    public void setEvaluation( Evaluation evaluation ) {
        addEvaluation( evaluation );
    }

    public void addEvaluation( Evaluation evaluation ) {
        this.evaluations.put( evaluation.getNodeId(), evaluation );
        this.lastEvaluation = evaluation;
    }

    public Degree getDegree() {
        return getEvaluation().getDegree();
    }

    public int getSourceId() {
        return getEvaluation().getNodeId();
    }
}
