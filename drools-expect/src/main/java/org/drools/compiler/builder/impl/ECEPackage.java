package org.drools.compiler.builder.impl;

import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.ResourceType;

public class ECEPackage implements ResourceTypePackage {

    public ResourceType getResourceType() {
        return ECE.ECE;
    }

}
