package org.kie.shapes.terms;


import org.kie.shapes.terms.api.Terms;
import org.kie.shapes.terms.impl.DefaultTermsServiceImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instantiates a {@link Terms} to be used by the 'inValueSet' Drools operator.
 */
public class TermsInferenceServiceFactory {

    private Map<String,Terms> termsServices = new ConcurrentHashMap<String, Terms>();
    private Terms defaultService;

    private static TermsInferenceServiceFactory instance;

    public static synchronized TermsInferenceServiceFactory instance() {
        if(instance == null) {
            instance = new TermsInferenceServiceFactory();
        }

        // TODO DO This dynamically...
        instance.termsServices.put( DefaultTermsServiceImpl.KIND, new DefaultTermsServiceImpl() );

        instance.defaultService = instance.termsServices.get( DefaultTermsServiceImpl.KIND );

        return instance;
    }

    private TermsInferenceServiceFactory() {
    }

    public Terms getTerminologyService( String kind ) {
        return  termsServices.get( kind );
    }

    public Terms getTerminologyService() {
        return defaultService;
    }

}
