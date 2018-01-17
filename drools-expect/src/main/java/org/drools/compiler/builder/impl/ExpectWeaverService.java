package org.drools.compiler.builder.impl;

import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.internal.weaver.KieWeaverService;
import org.kie.api.io.ResourceType;

public class ExpectWeaverService implements KieWeaverService<ECEPackage> {

    public ResourceType getResourceType() {
        return ECE.ECE;
    }

    public Class getServiceInterface() {
        return KieWeaverService.class;
    }

    public void merge( KieBase kieBase, KiePackage kiePkg, ECEPackage rtPkg ) {
        System.out.println( "Weaver Merge!!" );
    }

    public void weave( KieBase kieBase, KiePackage kiePkg, ECEPackage rtPkg ) {
        System.out.println( "Weaver Weave!!" );
    }

}
