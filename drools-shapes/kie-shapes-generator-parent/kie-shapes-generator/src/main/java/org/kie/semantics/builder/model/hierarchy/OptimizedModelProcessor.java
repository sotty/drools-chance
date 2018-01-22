package org.kie.semantics.builder.model.hierarchy;


import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.builder.model.hierarchy.opt.OptimalHierarchy;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.solver.XStreamXmlSolverFactory;

public class OptimizedModelProcessor implements ModelHierarchyProcessor {

    public void process( OntoModel model ) {

        SolverFactory<OptimalHierarchy> solverFactory = new XStreamXmlSolverFactory<>();
        ((XStreamXmlSolverFactory) solverFactory).configure( "org/kie/semantics/builder/model/hierarchy/hier_joined_config.xml" );

        Solver<OptimalHierarchy> solver = solverFactory.buildSolver();

        OptimalHierarchy problem = new OptimalHierarchy( model );

        solver.solve( problem );

        OptimalHierarchy solvedHierarchy = solver.getBestSolution();

        System.out.println( "\n\n\n\n ********************************************** \n\n\n" );
        System.out.println( " Final solution :" );
        System.out.println( solvedHierarchy );
        System.out.println( "\n\n\n\n ********************************************** \n\n\n" );

        if ( ! solvedHierarchy.validate() ) {
            throw new IllegalStateException( "The final Solution is invalid and can't be used to generate the code" + problem.getInvalidConcepts() );
        }

        solvedHierarchy.updateModel( model );

    }

}