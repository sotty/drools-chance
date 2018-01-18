package org.kie.semantics.builder.model.hierarchy.opt;


import org.kie.semantics.builder.model.Concept;
import org.kie.semantics.builder.model.ConceptImplProxy;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.heuristic.selector.move.factory.MoveListFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignSuperMoveFactory implements MoveListFactory<OptimalHierarchy> {

	public List<AssignDomainMove> createMoveList( OptimalHierarchy hier ) {
        Set<AssignDomainMove> moveSet = new HashSet<>();

		// Concept thing = hier.getTop();

        for ( ConceptImplProxy con : hier.getCons() ) {

//            if ( !Thing.IRI.equals( con.getIri() ) ) {
//                for ( ConProxy candidate : hier.getInheritances() ) {
//                    if ( con != candidate ) {
//                        moveSet.add( new AssignDomainMove( con, candidate ) );
//                    }
//                }
//            }


            addSuperConcepts( con, con.getConcept(), moveSet, hier );

//            if ( con.getConcept().getSuperConcepts().size() > 1 ) {
//                addSuperConcepts( con, con.getConcept(), moveSet, hier );
//            }
        }

        return new ArrayList<>( moveSet );
    }

    private void addSuperConcepts( ConceptImplProxy base, Concept con, Set<AssignDomainMove> moveList, OptimalHierarchy hier ) {
        for ( Concept x : con.getSuperConcepts() ) {
            AssignDomainMove move = new AssignDomainMove( base, hier.getCon( x.getIri() ) );
//                move.setVerbose( true );
            moveList.add( move );
            addSuperConcepts( base, x, moveList, hier );
        }
    }


	public static class AssignDomainMove implements Move<OptimalHierarchy> {

        private ConceptImplProxy con;
        private ConceptImplProxy next;
		private ConceptImplProxy curr;

        private boolean verbose = false;

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public AssignDomainMove( ConceptImplProxy con, ConceptImplProxy next ) {
            this.con = con;
            this.next = next;
            this.curr = con.getChosenSuper();
        }

        public boolean isMoveDoable( ScoreDirector scoreDirector ) {
//            if ( verbose ) { System.out.println( "Checking doability of " + this.toString() ); }
//            return next != prev && ! isSubConceptOf( next, con );
            return con.getChosenSuper() == null || ( ! next.getIri().equals( con.getChosenSuper().getIri() ) );
        }

        public Move<OptimalHierarchy> createUndoMove( ScoreDirector scoreDirector ) {
            AssignDomainMove undo = new AssignDomainMove( con, curr );
            return undo;
        }

        public Move<OptimalHierarchy> doMove( ScoreDirector scoreDirector ) {
            scoreDirector.beforeVariableChanged( con, "chosenSuper" );
            OptimalHierarchy hier = (OptimalHierarchy) scoreDirector.getWorkingSolution();
            ConceptImplProxy prev = hier.getCon( con.getChosenSuper().getIri() );

            if ( verbose ) {
                System.out.println( "Setting " + next.getIri() + " as super of " + con.getIri() + ( con.getChosenSuper() != null ? " in place of " + prev.getIri() : " " ) );
            }
            if ( prev != null ) {
                for ( String key : prev.getAvailablePropertiesVirtual().keySet() ) {
                    if ( ! con.getChosenProperties().containsKey( key ) && ! next.getAvailablePropertiesVirtual().containsKey( key ) ) {
                        con.getChosenProperties().put( key, prev.getAvailablePropertiesVirtual().get( key ) );
                    }
                }
            }

            con.setChosenSuper( next );

            for ( String key : next.getAvailablePropertiesVirtual().keySet() ) {
                if ( con.getChosenProperties().containsKey( key ) ) {
                    if ( verbose ) { System.out.println(" \tRemoving inherited property " + key +  " from concept " + con.getIri() ); }
                    con.getChosenProperties().remove( key );
                }
            }


            if ( verbose ) {
                System.out.println( "Done Setting " + next.getIri() + " as super of " + con.getIri() + ( prev != null ? " in place of " + prev.getIri() : " " ) );
                System.out.println( con );
            }

            if ( ! con.validate() ) {
                throw new IllegalStateException( "The concept " + con.getIri() + " is in an illegal state " );
            }

            scoreDirector.afterVariableChanged( con, "chosenSuper" );
            scoreDirector.triggerVariableListeners();

	        return createUndoMove( scoreDirector );
        }

        @Override
        public String getSimpleMoveTypeDescription() {
            return "TODO";
        }

        public Collection<?> getPlanningEntities() {
            return Collections.singleton( con );
        }

        public Collection<?> getPlanningValues() {
            return Collections.singletonList( next );
        }

        @Override
        public String toString() {
            return con.getIri() + " => " + next.getIri();
        }

        @Override
        public boolean equals(Object o) {
	        if ( this == o ) return true;
	        if ( o == null || getClass() != o.getClass() ) return false;

	        AssignDomainMove that = ( AssignDomainMove ) o;

	        return ( con != null ? con.equals( that.con ) : that.con == null ) && ( next != null ? next.equals( that.next ) : that.next == null );
        }

        @Override
        public int hashCode() {
            int result = con != null ? con.hashCode() : 0;
            result = 31 * result + (next != null ? next.hashCode() : 0);
            return result;
        }
    }
}
