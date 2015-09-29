package org.drools.shapes.terms;


import org.drools.shapes.terms.cts2.Cts2TermsImpl;
import org.drools.shapes.terms.operations.internal.TermsInferenceService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Instantiates a {@link org.drools.shapes.terms.operations.Terms} to be used by the 'inValueSet' Drools operator.
 */
public class TermsInferenceServiceFactory {

    private Map<String,TermsInferenceService> termsServices = new ConcurrentHashMap<String, TermsInferenceService>();
    private TermsInferenceService defaultService;

    private static TermsInferenceServiceFactory instance;

    public static synchronized TermsInferenceServiceFactory instance() {
        if(instance == null) {
            instance = new TermsInferenceServiceFactory();
        }
        return instance;
    }

    private TermsInferenceServiceFactory() {
    }
    
    public TermsInferenceService getValueSetProcessor( String kind ) {
        return termsServices.get( kind );
    }

    
    
    public TermsInferenceService getValueSetProcessor() {
    	// TODO Remove this in the future. Right now, if there is not default service defined, the Cts2TermsImpl service is instantiated. Just for allowing the old tests to run
    	if (defaultService==null) {
            instance.termsServices.put( Cts2TermsImpl.KIND, new Cts2TermsImpl() );

            instance.defaultService = instance.termsServices.get( Cts2TermsImpl.KIND );    		
    	}
    	
    	return defaultService;
    }
    
    /** 
     * Adds a terms service to the factory.
     * 
     * The default service (the one obtained from the method {@link #method getValueSetProcessor()}) will remain unchanged
     * 
     * @param kind	Human readable identifier for the service
     * @param service The service
     */
    public void putValueSetProcessor(String kind, TermsInferenceService service) {
    	termsServices.put(kind, service);
    }
    
    /**
     * Selects the service associated with {@param kind} as the default service.
     * 
     * @param kind Human readable identifier for the service
     * @throws IllegalArgumentException if the service has not been added previously (@see #method putValueSetProcessor(String, TermsInferenceService)
     */
    public void setDefaultValueSetProcessor(String kind) {
    	TermsInferenceService termsService;
    	termsService = termsServices.get(kind);
    	if (termsService!=null) {
    		defaultService = termsService;
    	} else {
    		throw new IllegalArgumentException("Service of kind '"+kind+"' not found. It must be added berfore setting it as default");
    	}
    }
    
    /**
     * Adds a new terms inference service to the factory, and selects it as the default service
     * 
     * Convenience method equivalent to calling {@link #method putValueSetProcessor(String,TermsInferenceService)} 
     * and {@link #method setValueSetProcessor(String)}
     * 
     * 
     * @param kind Human readable identifier for the service
     * @param service The service. It will become the default service
     */
    public void setDefaultValueSetProcessor(String kind, TermsInferenceService service) {
    	putValueSetProcessor(kind,service);
    	setDefaultValueSetProcessor(kind);
    }
    
    
    

}
