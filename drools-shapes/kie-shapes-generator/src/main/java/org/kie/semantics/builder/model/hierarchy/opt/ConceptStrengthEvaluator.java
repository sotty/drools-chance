package org.kie.semantics.builder.model.hierarchy.opt;


import org.apache.commons.lang3.builder.CompareToBuilder;
import org.kie.semantics.builder.model.Concept;

import java.util.Comparator;

public class ConceptStrengthEvaluator implements Comparator<Concept> {


    public int compare( Concept con, Concept other ) {
	    return new CompareToBuilder().append( con.getAvailableProperties().size(),
		                                      other.getAvailableProperties().size() ).toComparison();
    }

}
