package org.drools.beliefs.provenance;

import org.drools.core.BeliefSystemType;
import org.drools.core.beliefsystem.BeliefSystem;
import org.drools.core.common.BeliefSystemFactory;
import org.drools.core.common.InternalWorkingMemoryEntryPoint;
import org.drools.core.common.TruthMaintenanceSystem;

public class ProvenanceBeliefSystemFactory implements BeliefSystemFactory {

	@Override
	public BeliefSystem createBeliefSystem( BeliefSystemType type, InternalWorkingMemoryEntryPoint ep, TruthMaintenanceSystem tms ) {
		return new ProvenanceBeliefSystem( ep, tms );
	}
}
