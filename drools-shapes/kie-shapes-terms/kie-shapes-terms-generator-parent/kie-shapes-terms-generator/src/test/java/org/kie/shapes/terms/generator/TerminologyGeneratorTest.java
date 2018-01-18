package org.kie.shapes.terms.generator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kie.shapes.terms.ConceptDescriptor;
import org.kie.shapes.terms.ConceptScheme;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;

public class TerminologyGeneratorTest {

    private static Map<URI, ConceptScheme<ConceptDescriptor>> ConceptSchemeMap;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void init() {
        ConceptSchemeMap = doGenerate();
    }

    @Test
    public void testGenerateConceptSchemes() {

        assertEquals(1, ConceptSchemeMap.size());

        ConceptScheme cs = ConceptSchemeMap.values().iterator().next();

        assertEquals("SCH1", cs.getSchemeName());
        assertEquals("http://test/generator#concept_scheme1", cs.getSchemeURI().toString() );
    }

    @Test
    public void testGenerateConceptsWithReasoning() {
        assertEquals(1, ConceptSchemeMap.size());

        Stream<ConceptDescriptor> concepts = ConceptSchemeMap.values().iterator().next().getConcepts();

        assertEquals(2, concepts.count() );
    }

    @Test
    public void testGenerateConceptsPopulated() {
        assertEquals(1, ConceptSchemeMap.size());

        Stream<ConceptDescriptor> concepts = ConceptSchemeMap.values().iterator().next().getConcepts();

	    concepts.forEach( (concept) -> {
            assertNotNull( concept.getCode() );
            assertNotNull( concept.getCodeSystem() );
            assertNotNull( concept.getName() );
        });
    }



    @Test
    public void testClassCompilation() {
        try {

            File src = folder.newFolder( "src" );
            File target = folder.newFolder( "output" );

            new JavaGenerator().generate( ConceptSchemeMap.values(), "org.drools.test", src );
            showDirContent( folder );

            List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile( src, target );

            boolean success = true;
            for ( Diagnostic diag : diagnostics ) {
                System.out.println( diag.getKind() + " : " + diag );
                if ( diag.getKind() == Diagnostic.Kind.ERROR ) {
                    success = false;
                }
            }
            assertTrue( success );

            ClassLoader urlKL = new URLClassLoader(
                    new URL[] { target.toURI().toURL() },
                    Thread.currentThread().getContextClassLoader()
            );

            Class scheme = Class.forName( "test.generator.SCH1", true, urlKL );

            Field ns = scheme.getField( "schemeID" );
            Assert.assertEquals( "0.0.0.0", ns.get( null ) );

            Object code = scheme.getEnumConstants()[0];
            assertTrue( code instanceof ConceptDescriptor );
            assertEquals( "6789", ((ConceptDescriptor) code).getCode() );

        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
    }

    private List<Diagnostic<? extends JavaFileObject>> doCompile( File source, File target ) {
        List<File> list = new LinkedList<File>();

        explore( source, list );

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = jc.getStandardFileManager( diagnostics, null, null );
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles( list );
        List<String> jcOpts = Arrays.asList( "-d", target.getPath() );
        JavaCompiler.CompilationTask task = jc.getTask( null, fileManager, diagnostics, jcOpts, null, compilationUnits );
        task.call();
        return diagnostics.getDiagnostics();
    }


    public static Map<URI, ConceptScheme<ConceptDescriptor>> doGenerate() {
        try {
            OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
            OWLOntology o = owlOntologyManager.loadOntologyFromOntologyDocument( TerminologyGeneratorTest.class.getResourceAsStream( "/test.owl" ) );

            TerminologyGenerator generator = new TerminologyGenerator( o, true );

            return generator.traverse();
        } catch ( Exception e ) {
            e.printStackTrace();
            fail( e.getMessage() );
        }
        return null;
    }

    private void showDirContent(TemporaryFolder folder) {
        showDirContent( folder.getRoot(), 0 );
    }

    private void showDirContent( File file, int i ) {
        System.out.println( tab(i) + " " + file.getName() );
        if ( file.isDirectory() ) {
            for ( File sub : file.listFiles() ) {
                showDirContent( sub, i + 1 );
            }
        }
    }

    private String tab( int n ) {
        StringBuilder sb = new StringBuilder(  );
    	for ( int j = 0; j < n; j++ ) {
        	sb.append( "\t" );
        }
    	return sb.toString();
    }

    private void explore( File dir, List<File> files ) {
        for ( File f : dir.listFiles() ) {
            if ( f.getName().endsWith( ".java" ) ) {
                files.add( f );
            }
            if ( f.isDirectory() ) {
                explore( f, files );
            }
        }
    }

}
