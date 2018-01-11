package org.kie.semantics.builder;


import org.kie.semantics.lang.DLReasonerTemplateManager;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.mvel2.templates.TemplateRegistry;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.MissingOntologyHeaderStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.InputStream;

public class DLReasonerBuilderImpl implements DLReasonerBuilder {


    private static DLReasonerBuilder instance = new DLReasonerBuilderImpl();

    public static DLReasonerBuilder getInstance() {
        return instance;
    }

    private DLReasonerBuilderImpl() {

    }

    public OWLOntology parseOntology( InputStream[] resources ) {
        try {

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
            config.setMissingOntologyHeaderStrategy( MissingOntologyHeaderStrategy.IMPORT_GRAPH );

            OWLOntology onto = null;
            for ( InputStream res : resources ) {
                OWLOntologyDocumentSource source = new StreamDocumentSource( res );
                onto = manager.loadOntologyFromOntologyDocument( source, config );
            }

            return onto;
        } catch ( OWLOntologyCreationException e ) {
	        e.printStackTrace();
	        return null;
        }
    }


    public OWLOntology parseOntology( InputStream resource ) {
        return parseOntology( new InputStream[] { resource } );
    }

	public String buildTableauRules( OWLOntology ontologyDescr, String[] visitor ) {
		return DLReasonerTemplateManager.getTableauRegistry( DLReasonerTemplateManager.DLFamilies.FALC )
		                                .map( (reg) -> buildTableauRules( ontologyDescr, visitor, reg ) )
		                                .orElse( "" );
	}

	protected String buildTableauRules( OWLOntology ontologyDescr, String[] visitor, TemplateRegistry reg ) {
		KieHelper kie = new KieHelper();
		for ( String r : visitor ) {
			kie.addFromClassPath( r );
		}

		KieSession ksession = kie.getKieContainer().newKieSession();

		StringBuilder out = new StringBuilder();
		ksession.setGlobal( "out", out );
		ksession.setGlobal( "registry", reg );

		ksession.fireAllRules();

		ksession.insert( ontologyDescr );
		ksession.fireAllRules();

		String tableauRules = out.toString();

		ksession.dispose();

		return tableauRules;
	}

}
