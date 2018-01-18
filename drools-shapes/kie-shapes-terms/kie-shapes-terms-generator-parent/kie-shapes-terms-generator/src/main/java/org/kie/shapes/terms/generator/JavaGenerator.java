package org.kie.shapes.terms.generator;

import org.kie.semantics.utils.NameUtils;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.kie.shapes.terms.impl.model.DefaultConceptScheme;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaGenerator {

    private SimpleTemplateRegistry registry;

    public JavaGenerator() {
        registry = new SimpleTemplateRegistry();
        prepareTemplates();
    }

    private void prepareTemplates() {
        registry.addNamedTemplate( "concepts-java",
                                   TemplateCompiler.compileTemplate( getResource( "concepts-java.mvel" ) ) );
    }

    private InputStream getResource( String templ ) {
        return JavaGenerator.class.getResourceAsStream( "/templates/" + templ );
    }


    public void generate( Collection<ConceptScheme<ConceptDescriptor>> conceptSchemes, String packageName, File outputDir ) {
        outputDir.mkdirs();

        this.generateConcepts( conceptSchemes, packageName, outputDir );
    }

    protected void generateConcepts( Collection<ConceptScheme<ConceptDescriptor>> conceptSchemes, String packageName, File outputDir ) {
        CompiledTemplate compiled = registry.getNamedTemplate( "concepts-java" );

	    for( ConceptScheme<ConceptDescriptor> conceptScheme : conceptSchemes ) {
		    conceptScheme.getConcepts().forEach( (cd) -> {
			    String className = NameUtils.getTermCodeSystemName( conceptScheme.getSchemeName() );
			    String innerPackageName = NameUtils.namespaceURIToPackage( IRI.create( conceptScheme.getSchemeURI() ).getNamespace() );

			    Map<String,Object> context = new HashMap<>();
			    context.put( "conceptScheme", conceptScheme );
			    context.put( "concepts", conceptScheme.getConcepts().collect( Collectors.toList() ) );
			    context.put( "typeName", className );
			    context.put( "packageName", innerPackageName );
			    context.put( "implClassName", DefaultConceptScheme.class.getName() );
			    context.put( "typeIntf", ConceptDescriptor.class );

			    String mainText = (String) TemplateRuntime.execute( compiled, this, context );
			    this.writeToFile( mainText, outputDir, innerPackageName, className );
		    } );
	    }
    }


    private void writeToFile(
            String content,
            File outputDir,
            String packageName,
            String className) {

    	System.out.println( content );

        File outputFile = createJavaFile(outputDir, packageName, className);

        FileWriter writer = null;
        try {
            writer = new FileWriter( outputFile );
            writer.write( content );
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File createJavaFile(File outputDir, String packageName, String className) {
        File packageDir = new File(outputDir, packageName.replace('.', File.separatorChar));
        if ( ! packageDir.exists() ) {
            packageDir.mkdirs();
        }

        return new File(packageDir, className + ".java");
    }



}
