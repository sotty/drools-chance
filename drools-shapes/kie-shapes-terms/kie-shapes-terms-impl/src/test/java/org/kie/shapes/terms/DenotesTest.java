package org.kie.shapes.terms;

import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.conf.EvaluatorOption;
import org.kie.internal.utils.KieHelper;
import org.kie.shapes.terms.model.Condition;
import org.kie.shapes.terms.model.MockSNOMED;
import org.kie.shapes.terms.model.Observation;
import org.kie.shapes.terms.model.TestCodes;
import org.kie.shapes.terms.model.datatypes.CD;
import org.kie.shapes.terms.evaluator.DenotesEvaluatorDefinition;

import java.util.Date;

public class DenotesTest {

    @Test
    public void testCDEqualityInRule() {
        String rule = "package org.drools.shapes.terms; " +
                      "import " + Condition.class.getName() + "; " +
                      "import " + CD.class.getName() + "; " +
                      "import " + TestCodes.class.getName() + "; " +

                      "rule RedAlert " +
                      "when " +
                      "     $c : Condition( $pid : pid, code == TestCodes.EndocrineSystemDisease ) " +
                      "then " +
                      "     System.out.println( 'We see that Patient ' + $pid + ' has a serious problem ' ); " +
                      "end ";

        Condition c1 = new Condition( "1",
                                      new Date(),
                                      TestCodes.EndocrineSystemDisease,
                                      "JohnDoe" );

        KieSession ks = new KieHelper()
                .addContent( rule, ResourceType.DRL )
                .build()
                .newKieSession();

        ks.insert( c1 );
        ks.fireAllRules();
    }

    @Test
    public void testDenotesInRule() {
        String rule = "package org.drools.shapes.terms; " +
                      "import " + Condition.class.getName() + "; " +
                      "import " + Observation.class.getName() + "; " +
                      "import " + CD.class.getName() + "; " +
                      "import " + MockSNOMED.class.getName() + "; " +

                      "rule RedAlert " +
                      "dialect 'mvel' " +
                      "when " +
                      "     $c : Observation( $pid : pid, " +
		               "                      code denotes MockSNOMED.MedicalProblem, " +
                      "                       $val : value denotes MockSNOMED.AcuteDisease ) " +
                      "then " +
                      "     System.out.println( 'Patient ' + $pid + ' has an acute disease : ' + $val ); " +
                      "end ";

        // This is not the RIM observation, but a similar thing : Observation( id, effectiveTime, code, value, patientId )
        Observation c1 = new Observation( "obs1",
                                      new Date(),
                                      // we can use "literal" CDs
                                      new CD( MockSNOMED.MedicalProblem ),
                                      // or CDs built on the fly
                                      new CD( "95653008",
                                              "acute migraine",
                                              MockSNOMED.SELF),
                                      "JohnDoe" );

        Observation c2 = new Observation( "obs2",
                                      new Date(),
                                      // we can use "literal" CDs
                                      new CD( MockSNOMED.MedicalProblem ),
                                      // or CDs built on the fly
                                      new CD( MockSNOMED.DiabetesTypeII ),
                                      "JohnDoe" );

        KieSession ks = new KieHelper( EvaluatorOption.get( "denotes", new DenotesEvaluatorDefinition() ) )
                .addContent( rule, ResourceType.DRL )
                .build()
                .newKieSession();

        ks.insert( c1 );
        ks.insert( c2 );
        ks.fireAllRules();

    }

}
