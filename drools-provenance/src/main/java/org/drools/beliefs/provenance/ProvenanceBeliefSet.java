package org.drools.beliefs.provenance;

import org.drools.core.metadata.MetaCallableTask;
import org.drools.core.spi.Activation;
import org.w3.ns.prov.Activity;

import java.util.Collection;

public interface ProvenanceBeliefSet {

    public Collection<? extends Activity> getGeneratingActivities();

    public void recordActivity( MetaCallableTask task, Activation justifier, boolean positiveAssertion );

}
