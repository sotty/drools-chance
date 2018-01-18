package org.kie.semantics.builder.model.hierarchy.opt;


import org.kie.semantics.builder.model.Concept;
import org.kie.semantics.builder.model.ConceptImplProxy;
import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.builder.model.PropertyRelation;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.w3._2002._07.owl.Thing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

@PlanningSolution
public class OptimalHierarchy  {

	@ProblemFactCollectionProperty
	private Collection<Concept> availableConcepts;

    private Collection<PropertyRelation> availableProperties;

    private Concept top;

    private LinkedHashMap<String, ConceptImplProxy> inheritances;

    @PlanningScore
    private HardSoftScore score;


    public OptimalHierarchy() {

    }

    public OptimalHierarchy( OntoModel model ) {
        availableConcepts = Collections.unmodifiableCollection( model.getConcepts() );
        availableProperties = Collections.unmodifiableCollection( model.getProperties() );

        top = model.getConcept( Thing.IRI );

        inheritances = new LinkedHashMap<>( availableConcepts.size() );

        for ( Concept c  : availableConcepts ) {
            ConceptImplProxy x = new ConceptImplProxy( c );
            inheritances.put( x.getIri(), x );
        }
    }


    public OptimalHierarchy( OptimalHierarchy opt ) {
        this.availableConcepts = opt.availableConcepts;
        this.availableProperties = opt.availableProperties;

        inheritances = new LinkedHashMap<>();
        for ( String key : opt.inheritances.keySet() ) {
            inheritances.put( key, opt.inheritances.get( key ).clone() );
        }

        score = opt.score;
    }


    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "cons")
    public Collection<ConceptImplProxy> getCons() {
        return inheritances.values();
    }

    public LinkedHashMap<String, ConceptImplProxy> getInheritances() {
        return inheritances;
    }


    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }


    public Collection<?> getProblemFacts() {
        return availableConcepts;
    }

    public OptimalHierarchy cloneSolution() {
        return new OptimalHierarchy( this );
    }



    public Collection<Concept> getAvailableConcepts() {
        return availableConcepts;
    }


    public Collection<PropertyRelation> getAvailableProperties() {
        return availableProperties;
    }


    public Concept getTop() {
        return top;
    }

    public void setTop(Concept top) {
        this.top = top;
    }

    public ConceptImplProxy getCon( String iri ) {
        return inheritances.get( iri );
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder( "Optimized Hierarchy ( " + score + " ) \n" );
        for ( ConceptImplProxy con : inheritances.values() ) {
            s.append( "\t " ).append( con ).append( "\n" );
        }
        return s.toString();
    }



    public void updateModel( OntoModel model ) {

        for ( ConceptImplProxy con : getCons() ) {
            Concept x = con.getConcept();
            Concept sup = con.getChosenSuper().getConcept();

             if ( x == sup ) {
                 //TODO Check how this is possible. With partial inference some classes may
                 // may be generated and not classified, resulting in apparent inconsistencies
                 sup = model.getConcept( Thing.IRI );
             }

            x.setChosenSuperConcept( sup );
//            x.setChosenSuper( sup.getIri() );
            sup.getChosenSubConcepts().add( x );
            x.setChosenProperties( con.getChosenProperties() );

            x.setImplementingCon( con );
        }

    }

    public Set<String> getInvalidConcepts() {
        Set<String> invalidConcepts = new HashSet<>();
        for ( ConceptImplProxy con : getCons() ) {
            if ( ! con.validate() ) {
                invalidConcepts.add( con.getIri() );
            }
        }
        return invalidConcepts;
    }

    public boolean validate() {
        return getInvalidConcepts().isEmpty();
    }
}
