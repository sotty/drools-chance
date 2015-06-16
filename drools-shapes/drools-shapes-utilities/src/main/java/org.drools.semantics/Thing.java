package org.drools.semantics;

import com.clarkparsia.empire.SupportsRdfId;
import org.drools.core.factmodel.traits.Trait;

@Trait
public interface Thing<K> extends com.clarkparsia.empire.SupportsRdfId, org.drools.core.factmodel.traits.Thing<K> {

    public String get__IndividualName();

    public void set__IndividualName( String name );

    public String getFullName();

    public SupportsRdfId.RdfKey getRdfId();
}
