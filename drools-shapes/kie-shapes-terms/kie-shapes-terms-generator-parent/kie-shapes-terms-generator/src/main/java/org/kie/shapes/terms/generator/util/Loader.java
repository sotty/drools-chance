package org.kie.shapes.terms.generator.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Loader {


    public OWLOntology loadOntology( String[] resources ) throws OWLOntologyCreationException, FileNotFoundException {
        OWLOntology ontology = null;
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        for ( String res : resources ) {
            ontology = loadOntologyPiece( res, manager );
        }

        return ontology;
    }

    private OWLOntology loadOntologyPiece( String file, OWLOntologyManager manager ) throws OWLOntologyCreationException, FileNotFoundException {
        InputStream inputStream;

        File res = new File( file );

        try {
            if(! res.exists() ) {
                inputStream = Loader.class.getResourceAsStream( file );
            } else {
                inputStream =  new FileInputStream( res );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        return manager.loadOntologyFromOntologyDocument( inputStream );
    }

}
