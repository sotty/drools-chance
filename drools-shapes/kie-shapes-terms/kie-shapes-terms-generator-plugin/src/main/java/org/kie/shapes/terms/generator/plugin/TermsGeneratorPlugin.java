package org.kie.shapes.terms.generator.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.generator.JavaGenerator;
import org.kie.shapes.terms.generator.TerminologyGenerator;
import org.kie.shapes.terms.generator.util.Loader;
import org.kie.shapes.terms.impl.model.DefaultConceptScheme;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Goal
 *
 * @goal generate-terms
 *
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class TermsGeneratorPlugin extends AbstractMojo {


    /**
     * @parameter default-value="false"
     */
    private boolean reason = false;

    public boolean isReason() {
        return reason;
    }

    public void setReason(boolean reason) {
        this.reason = reason;
    }

    /**
     * @parameter
     */
    private List<String> owlFiles;

    public List<String> getOwlFile() {
        return owlFiles;
    }

    public void setOwlFile(List<String> owlFile) {
        this.owlFiles = owlFiles;
    }

    /**
     * @parameter
     */
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * @parameter default-value="./target/generated-sources"
     */
    private File outputDirectory;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            OWLOntology ontology = new Loader().loadOntology( owlFiles.toArray( new String[owlFiles.size()] ) );

            TerminologyGenerator terminologyGenerator = new TerminologyGenerator( ontology, this.reason );

            Map<URI, ConceptScheme<ConceptDescriptor>> codeSystems = terminologyGenerator.traverse();

            if ( ! outputDirectory.exists() ) {
                outputDirectory.mkdirs();
            }

            new JavaGenerator().generate( codeSystems.values(), packageName, outputDirectory );
        } catch ( Exception e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
            throw new MojoExecutionException( e.getMessage() );
        }

    }

}



