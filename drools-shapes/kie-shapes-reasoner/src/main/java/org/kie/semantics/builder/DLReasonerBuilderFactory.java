package org.kie.semantics.builder;


public class DLReasonerBuilderFactory {

    public static DLReasonerBuilder getBuilder() {
        return DLReasonerBuilderImpl.getInstance();
    }

}
