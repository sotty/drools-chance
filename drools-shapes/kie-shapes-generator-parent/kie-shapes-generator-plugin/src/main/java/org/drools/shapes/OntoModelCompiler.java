package org.drools.shapes;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jvnet.hyperjaxb3.maven2.Hyperjaxb3Mojo;
import org.kie.semantics.builder.DLFactoryConfiguration;
import org.kie.semantics.builder.model.Concept;
import org.kie.semantics.builder.model.DRLModel;
import org.kie.semantics.builder.model.JarModel;
import org.kie.semantics.builder.model.MetaclassModel;
import org.kie.semantics.builder.model.ModelFactory;
import org.kie.semantics.builder.model.OntoModel;
import org.kie.semantics.builder.model.RecognitionRuleModel;
import org.kie.semantics.builder.model.SemanticXSDModel;
import org.kie.semantics.builder.model.compilers.MetaclassModelCompiler;
import org.kie.semantics.builder.model.compilers.ModelCompiler;
import org.kie.semantics.builder.model.compilers.ModelCompilerFactory;
import org.kie.semantics.builder.model.compilers.RecognitionRuleCompiler;
import org.kie.semantics.builder.model.compilers.SemanticXSDModelCompiler;
import org.kie.semantics.utils.NameUtils;
import org.kie.semantics.utils.NamespaceUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;


public class OntoModelCompiler {


    public static final List<String> defaultOptions = Arrays.asList(
            "-extension",
            "-Xjaxbindex",
            "-Xannotate",
            "-Xinheritance",
//                                        "-XtoString",
            "-Xcopyable",
            "-Xmergeable",
//                                        "-Xvalue-constructor",
            "-Xfluent-api",
            "-Xkey-equality",
            "-Xsem-accessors",
            "-Xdefault-constructor",
            "-Xmetadata",
            "-XxcludeResolved",
            "-Xinject-code",
            "-enableIntrospection"
    );

    public static final List<String> fullOptions = Arrays.asList(
            "-extension",
            "-Xjaxbindex",
            "-Xannotate",
            "-Xinheritance",
            "-XtoString",
            "-Xcopyable",
            "-Xmergeable",
            "-Xvalue-constructor",
            "-Xfluent-api",
            "-Xkey-equality",
            "-Xsem-accessors",
            "-Xdefault-constructor",
            "-Xmetadata",
            "-XxcludeResolved",
            "-Xinject-code",
            "-enableIntrospection"
    );

    public static final List<String> minimalOptions = Arrays.asList(
            "-extension",
            "-Xjaxbindex",
            "-Xannotate",
            "-Xinheritance",
//                                        "-XtoString",
//                                        "-Xcopyable",
//                                        "-Xmergeable",
//                                        "-Xvalue-constructor",
//                                        "-Xfluent-api",
            "-Xkey-equality",
            "-Xsem-accessors",
            "-Xdefault-constructor",
            "-Xmetadata",
            "-XxcludeResolved",
            "-Xinject-code",
            "-enableIntrospection"
    );


    public enum MOJO_VARIANTS {
        JPA2( "jpa2" );

        private String label;
        MOJO_VARIANTS( String lab ) {
            label = lab;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum COMPILATION_OPTIONS {
        DEFAULT( defaultOptions ), MINIMAL( minimalOptions ), FULL( fullOptions );

        private List<String> options;
        COMPILATION_OPTIONS( List<String> opts ) {
            options = opts;
        }

        public List<String> getOptions() {
            return options;
        }
    }


    public enum AXIOM_INFERENCE {
        LITE( DLFactoryConfiguration.liteAxiomGenerators ), DEFAULT( DLFactoryConfiguration.defaultAxiomGenerators ), FULL( DLFactoryConfiguration.fullAxiomGenerators ), NONE( DLFactoryConfiguration.minimalAxiomGenerators );

        private List<InferredAxiomGenerator<? extends OWLAxiom>> gens;

        AXIOM_INFERENCE( List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators ) {
            gens = axiomGenerators;
        }

        public List<InferredAxiomGenerator<? extends OWLAxiom>> getGenerators() {
            return gens;
        }
    }

    private File folder;
    private OntoModel model;


    public static final String METAINF = "META-INF";
    public static final String DRL = "DRL";
    public static final String JAVA = "java";
    public static final String XJC = "xjc";
    public static final String CLASSES = "classes";


    public static String mavenTarget = "target";
    public static String mavenSource = mavenTarget + File.separator + "generated-sources";




    public static String metaInfDirName = mavenSource + File.separator + METAINF;
    public static String drlDirName = mavenSource + File.separator + DRL;
    public static String javaDirName = mavenSource + File.separator + JAVA;
    public static String xjcDirName = mavenSource + File.separator + XJC;

    public static String binDirName = mavenTarget + File.separator + CLASSES;


    private File metaInfDir;
    private File drlDir;
    private File javaDir;
    private File xjcDir;

    private File binDir;

    private List<File> preexistingSchemas = new ArrayList<>();
    private List<File> preesistingBindings = new ArrayList<>();



    public OntoModelCompiler( OntoModel model, File rootFolder, boolean useEnhancedNames ) {
        this.folder = tryGetFolder( rootFolder )
		        .orElseThrow( () -> new ShapeCasterException( "Unable to locate or create target folder " + rootFolder.getAbsolutePath() ) );
        initDirectories();

        this.model = model;
        model.setUseEnhancedNames( useEnhancedNames );

        lookupExistingSchemas();
    }

    private void lookupExistingSchemas() {
        File folder = tryGetMetaInfDir()
		        .orElseThrow( () -> new ShapeCasterException( "Unable to access main target folder" ) );
        File[] schemas = folder.listFiles( ( dir, name ) -> name.endsWith( ".xsd" ) && ! "owlThing.xsd".equals( name ) );
	    if ( schemas != null ) {
		    preexistingSchemas.addAll( Arrays.asList( schemas ) );
	    }

	    File[] bindings = folder.listFiles( ( dir, name ) -> name.endsWith( ".xjb" ) && ! "global.xjb".equals( name ) );
	    if ( bindings != null ) {
		    preesistingBindings.addAll( Arrays.asList( bindings ) );
	    }
    }

    private void initDirectories() {
        metaInfDir = initDir( metaInfDirName );

        drlDir = initDir( drlDirName );

        javaDir = initDir( javaDirName );

        xjcDir = initDir( xjcDirName );

        binDir = initDir( binDirName );
    }

	private File initDir( String dirName ) {
		File f = new File( folder.getPath() + File.separator + dirName );
		return tryGetFolder( f )
				.orElseThrow( () -> new ShapeCasterException( "Unable to create folder " + dirName ) );
	}

	public boolean clearSources() {
    	Optional<File> mid = tryGetMetaInfDir();
    	if ( mid.isPresent() ) {
    	    File[] files = mid.get().listFiles();
    	    return files != null
    	    	? Arrays.stream( files ).map( File::delete ).reduce( Boolean::logicalAnd ).orElse( false )
	            : false;
	    } else {
    		return false;
	    }
    }

    public boolean existsResult() {
    	return tryGetJavaDir().map( ( j) -> { File[] x = j.listFiles(); return x != null && x.length > 0; } ).orElse( false )
    	    || tryGetXjcDir().map( ( j) -> { File[] x = j.listFiles(); return x != null && x.length > 0; } ).orElse( false )
    	    || tryGetDrlDir().map( ( j) -> { File[] x = j.listFiles(); return x != null && x.length > 0; } ).orElse( false );
    }



    public List<Diagnostic<? extends JavaFileObject>> compileOnTheFly(List<String> options, MOJO_VARIANTS variant) {

        streamJavaInterfaces( false );

        streamXSDsWithBindings( null );
        mojo( options, variant );

	    return doCompile();
    }




    public void fixResolvedClasses() {
        for ( Concept con : model.getConcepts() ) {
            if ( con.getResolvedAs() != Concept.Resolution.NONE ) {
                if ( con.getChosenProperties().size() > 0 ) {
                    // This is very likely an extension/restriction of the original concept, so we need to redefine it
                    String extPack = model.getDefaultPackage() + "." + con.getPackage();
                    model.removeConcept( con );
                    Concept original = con.clone();
                    original.getSubConcepts().clear();
                    original.getSubConcepts().add( con );
                    con.getSuperConcepts().clear();
                    con.addSuperConcept( original );
                    con.setChosenSuperConcept( original );
                    con.setPackage( extPack );

                    URI xuri = NameUtils.packageToNamespaceURI( extPack );
                    con.setNamespace( xuri.toASCIIString() );
                    con.setIri( IRI.create( NameUtils.separatingName( xuri.toASCIIString() ) + con.getName() ) );

                    model.addConcept( con );
                    model.addConcept( original );

                }
            }
        }
    }



    public boolean streamDRLDeclares() throws MojoExecutionException {
        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.DRL );
        DRLModel drlModel = (DRLModel) compiler.compile( model );

	    Optional<File> path = tryGetFolder( new File( getDrlDir().getPath(), model.getDefaultPackage().replace( '.', File.separatorChar ) ) );
		String targetFileName = model.getName() + "_declare.drl";

	    return streamModel( path.orElseThrow( () -> new ShapeCasterException( "Unable to access folder" ) ),
	                        targetFileName,
	                        drlModel.getDRL().getBytes() );
    }

    public boolean streamRecognitionRules( Properties properties ) {
        RecognitionRuleCompiler compiler = (RecognitionRuleCompiler) ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.RL );
        compiler.configure( properties );

        RecognitionRuleModel ruleModel = (RecognitionRuleModel) compiler.compile( model );

	    Optional<File> path = tryGetFolder( new File( getDrlDir().getPath(), model.getDefaultPackage().replace( '.', File.separatorChar ) ) );
        String targetFileName = model.getName() + "_recognition.drl";

        return streamModel( path.orElseThrow( () -> new ShapeCasterException( "Unable to access folder" ) ),
                            targetFileName,
                            ruleModel.getDRL().getBytes() );
    }

    public boolean streamMetaclasses( boolean withDefaultClasses ) {
        MetaclassModelCompiler mcompiler = (MetaclassModelCompiler) ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.METACLASS );
        MetaclassModel metaModel = (MetaclassModel) mcompiler.compile( model, withDefaultClasses );

        boolean success = metaModel.save( getJavaDir().getPath() );
        String regexSeparator = File.separator;
        if (regexSeparator.equals("\\"))
            regexSeparator = "\\\\";
        if ( withDefaultClasses ) {
            for ( String pack : model.getAllPackageNames() ) {
                if ( NamespaceUtils.isOWL( pack ) ) {
                    continue;
                }
                try {
                	String targetPath = getJavaDir() + File.separator + pack.replaceAll( "\\.", regexSeparator );
	                String targetFileName = "MetaFactory.java";
	                success &= streamModel( new File( targetPath ), targetFileName, mcompiler.buildFactory(pack).getBytes() );
                } catch (RuntimeException re) {
                    re.printStackTrace();
                }
            }
        }

        return success;
    }

    public boolean streamJavaInterfaces( boolean includeJar ) {
        ModelCompiler jcompiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.JAR );
        JarModel jarModel = (JarModel) jcompiler.compile( model );

        boolean success = jarModel.save( getJavaDir().getPath() );

	    if ( includeJar ) {
		    Optional<File> path = tryGetFolder( new File( getBinDir().getPath() ) );
		    String targetFileName = model.getName() + ".jar";

		    success &= streamModel( path.orElseThrow( () -> new ShapeCasterException( "Unable to access folder" ) ),
		                            targetFileName,
		                            jarModel.buildJar().toByteArray() );

        }
        return success;
    }


    public boolean streamXSDsWithBindings() {
        return streamXSDsWithBindings( null );
    }

    public boolean streamXSDsWithBindings( String persistenceTemplatePath ) {
        SemanticXSDModelCompiler xcompiler = (SemanticXSDModelCompiler) ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.XSDX );
        SemanticXSDModel xmlModel = (SemanticXSDModel) xcompiler.compile( model );

        boolean success = false;
        try {
            success = xmlModel.stream( getMetaInfDir() );

            success = xmlModel.streamBindings( getMetaInfDir() );

	        if ( persistenceTemplatePath != null ) {
	            File persistenceTemplate = new File( persistenceTemplatePath );
	            if ( persistenceTemplate.exists() ) {
	                InputStream is = new FileInputStream( persistenceTemplate );
	                int n = is.available();
	                byte[] data = new byte[ n ];
	                if ( n == is.read( data ) ) {
		                xmlModel.setPersistenceXml( new String( data ) );
	                }
	                is.close();
	            }
	        }
	        success = success && streamPersistenceConfigs( xcompiler, xmlModel );

        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return success;
    }


    protected boolean streamPersistenceConfigs( SemanticXSDModelCompiler xcompiler, SemanticXSDModel xmlModel ) throws IOException {
        boolean success = true;

        xcompiler.mergeNamespacedPackageInfo( xmlModel );
        for ( org.jdom.Namespace ns : xmlModel.getNamespaces() ) {
	        String packageName = NameUtils.namespaceURIToPackage( ns.getURI() );
	        if ( packageName != null ) {
		        File out = new File( javaDir.getPath() + File.separator + packageName.replace( '.', File.separatorChar ) + File.separator + "package-info.java" );
		        if ( !out.exists() ) {
			        success &= streamModel( out.getParentFile(),
					             out.getName(),
					             xmlModel.getNamespacedPackageInfo( ns ).getBytes() );
		        }
	        }
        }


        File f2 = new File( getMetaInfDir().getPath() + File.separator + "empire.configuration.file" );
        if ( f2.exists() ) {
            xcompiler.mergeEmpireConfig( f2, xmlModel );
        }
	    success &= streamModel( f2.getParentFile(), f2.getName(), xmlModel.getEmpireConfig().getBytes() );


        File f3 = new File( getMetaInfDir().getPath() + File.separator + "empire.annotation.index" );
        if ( f3.exists() ) {
            xcompiler.mergeIndex( f3, xmlModel );
        }
	    success &= streamModel( f3.getParentFile(), f3.getName(), xmlModel.getIndex().getBytes() );


        File f4 = new File( getMetaInfDir().getPath() + File.separator + "persistence-template-hibernate.xml" );
//        File f4 = new File( tryGetXjcDir().getPath() + File.separator + "META-INF" + File.separator + "persistence.xml" );
//        if ( f4.exists() ) {
//            xcompiler.mergePersistenceXml( f4, xmlModel );
//        }
	    success &= streamModel( f4.getParentFile(), f4.getName(), xmlModel.getPersistenceXml().getBytes() );


	    return success;
    }


    public boolean streamIndividualFactory() {
        ModelCompiler compiler = ModelCompilerFactory.newModelCompiler( ModelFactory.CompileTarget.XSDX );
        SemanticXSDModel xsdModel = (SemanticXSDModel) compiler.compile( model );

	    Optional<File> targetPath = tryGetFolder( new File( getXjcDir().getPath() + File.separator + xsdModel.getDefaultPackage().replace( ".", File.separator ) ) );
		String targetFile = "IndividualFactory.java";

		return streamModel( targetPath.orElseThrow( () -> new ShapeCasterException( "Unable to access folder" ) ),
		                    targetFile,
		                    xsdModel.getIndividualFactory().getBytes() );
    }



	protected boolean streamModel( final File targetPath,
	                               final String targetFileName,
	                               final byte[] tgtModel ) {
		FileOutputStream fos;
		try {
			if ( ! targetPath.exists() ) {
				targetPath.mkdirs();
			}
			fos = new FileOutputStream( new File( targetPath, targetFileName ) );
			fos.write( tgtModel );
			fos.flush();
			fos.close();
			return true;
		} catch ( IOException e ) {
			e.printStackTrace();
			return false;
		}
	}




	public List<Diagnostic<? extends JavaFileObject>> doCompile() {
        List<File> list = new LinkedList<>();

        explore( getJavaDir(), list );
        explore( getXjcDir(), list );

        JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = jc.getStandardFileManager( diagnostics, null, null );
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles( list );
        List<String> jcOpts = Arrays.asList( "-d", getBinDir().getPath() );
        JavaCompiler.CompilationTask task = jc.getTask( null, fileManager, diagnostics, jcOpts, null, compilationUnits );
        task.call();

        boolean inf = copyMetaInfResources();
        boolean rul = copyRuleResources();
        if ( ! ( inf && rul )  ) {
			throw new ShapeCasterException( "Unable to copy output resources during compilation" );
        }

        return diagnostics.getDiagnostics();
    }

    private void explore( File dir, List<File> files ) {
    	File[] fs = dir.listFiles();
    	if ( fs != null ) {
		    for ( File f : fs ) {
			    if ( f.getName().endsWith( ".java" ) ) {
				    files.add( f );
			    }
			    if ( f.isDirectory() ) {
				    explore( f, files );
			    }
		    }
	    }
    }

    private boolean copyRuleResources() {
        File tgtDRL = new File( getBinDir().getPath() );
        return copyResources( getDrlDir(), tgtDRL );
    }

    private boolean copyMetaInfResources() {
        File tgtMetaInf = new File( getBinDir().getPath() + File.separator + METAINF );

	    return copyResources( getMetaInfDir(), tgtMetaInf )
			    && copyResources( new File( getXjcDir() + File.separator + METAINF ), tgtMetaInf );
    }

    private boolean copyResources( File source, File target ) {
        if ( ! source.exists() ) {
            return true;
        }

        final File tgt = tryGetFolder( target ).orElseThrow( () -> new ShapeCasterException( "Unable to access target folder " + target.getAbsolutePath() ) );

        File[] sources = source.listFiles();
        if ( sources == null ) {
        	return false;
        }

        List<File> fails = new ArrayList<>();
        Arrays.stream( sources )
              .filter( (f) -> ! f.getName().contains( "template" ) )
              .forEach( (f) -> {
	              try {
		              if ( f.isDirectory() ) {
			              copyResources( f, new File( tgt + File.separator + f.getName() ) );
		              } else {
			              copyFile( f, tgt );
		              }
	              } catch (IOException e) {
		              e.printStackTrace();
		              fails.add( f );
	              }
              } );
        return fails.isEmpty();
    }

    private void copyFile( File f, File tgtDir ) throws IOException {
//        System.out.println(" Trying to copy " + f.getName() + " into >>> " + tgtDir.getPath() );
        FileInputStream fis = new FileInputStream( f );
        int n = fis.available();
        byte[] buf = new byte[ n ];
        if ( n == fis.read( buf ) ) {

	        File tgt = new File( tgtDir.getPath() + File.separator + f.getName() );
	        FileOutputStream fos = new FileOutputStream( tgt );
	        fos.write( buf );

	        fis.close();
	        fos.flush();
	        fos.close();
        }
    }


    protected boolean streamMockPOM( File pom, String name ) {
        boolean success = false;
        try {
            FileOutputStream fos = new FileOutputStream( pom );
            byte[] content = ( "" +
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "  <modelVersion>4.0.0</modelVersion>\n" +
                    "  <groupId>org.test</groupId>\n" +
                    "  <artifactId>" + name + "</artifactId>\n" +
                    "</project>\n" +
                    "" ).getBytes();

            fos.write( content, 0, content.length );
            fos.flush();
            fos.close();

            success = true;
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return success;
    }


    public boolean mojo( List<String> args, MOJO_VARIANTS variant ) {
        boolean success = false;
        try {
            File pom = new File( folder.getPath() + File.separator + "pom.xml" );
            if ( ! pom.exists() ) {
                success = streamMockPOM( pom, model.getName() );
            }


            MavenProject mp = new MavenProject();

            final Hyperjaxb3Mojo mojo = new Hyperjaxb3Mojo();
            mojo.setVerbose( true );
            mojo.setNoFileHeader( true );

            mojo.setBindingDirectory( getMetaInfDir() );
            mojo.setBindingIncludes( new String[] { "*.xjb", "*.episode" } );
            mojo.setSchemaDirectory( getMetaInfDir() );

            int j = 0;
            String[] excludedSchemas = new String[ preexistingSchemas.size() ];
            for ( File f : preexistingSchemas ) {
                excludedSchemas[ j++ ] = f.getName();
            }
            mojo.setSchemaExcludes( excludedSchemas );

            int k = 0;
            String[] excludedBindings = new String[ preesistingBindings.size() ];
            for ( File f : preesistingBindings ) {
                excludedBindings[ k++ ] = f.getName();
            }
            mojo.setBindingExcludes( excludedBindings );

			mojo.setEpisodeFile( new File( getMetaInfDir().getPath() + File.separator + model.getName() + ".episode"  ) );

            mojo.setGenerateDirectory( getXjcDir() );
            mojo.setExtension(true);
            mojo.variant = variant.getLabel();

            File perx = new File( getBinDir().getPath() + File.separator + "META-INF" + File.separator + "persistence.xml" );
            if ( perx.exists() ) {
                mojo.persistenceXml = perx;
                try {
                    Document dox = parseXML( perx );

                    XPath xpath = XPathFactory.newInstance().newXPath();
                    XPathExpression expr = xpath.compile( "//persistence-unit/@name" );

                    mojo.persistenceUnitName = (String) expr.evaluate( dox, XPathConstants.STRING );

                } catch ( Exception e ) {
                    mojo.persistenceXml = new File( getMetaInfDir().getPath() + File.separator + "persistence-template-hibernate.xml" );
                    mojo.persistenceUnitName = model.getName();
                }
            } else {
                mojo.persistenceXml = new File( getMetaInfDir().getPath() + File.separator + "persistence-template-hibernate.xml" );
                mojo.persistenceUnitName = model.getName();
            }

            mojo.generateEquals = false;
            mojo.generateHashCode = false;
            mojo.setCleanPackageDirectories( false );
            mojo.setProject( mp );

            List<String> extArgs = new ArrayList<>( args );
            extArgs.add( "-npa" );
            mojo.setArgs( extArgs );

            mojo.setForceRegenerate( true );

            mojo.execute();
	        success = true;
        } catch (MojoExecutionException e) {
            e.printStackTrace();
        }
        return success;
    }



	public Optional<File> tryGetMetaInfDir() {
    	return tryGetFolder( metaInfDir );
    }

    public File getMetaInfDir() {
    	return tryGetMetaInfDir().orElseThrow( () -> new ShapeCasterException( "Unable to retrieve META-INF dir " ) );
    }

	public Optional<File> tryGetDrlDir() {
		return tryGetFolder( drlDir );
    }

    public File getDrlDir() {
	    return tryGetDrlDir().orElseThrow( () -> new ShapeCasterException( "Unable to retrieve DRL dir " ) );
    }

    public Optional<File> tryGetJavaDir() {
	    return tryGetFolder( javaDir );
    }

	public File getJavaDir() {
		return tryGetJavaDir().orElseThrow( () -> new ShapeCasterException( "Unable to retrieve JAVA dir " ) );
	}

	public Optional<File> tryGetXjcDir() {
	    return tryGetFolder( xjcDir );
    }

	public File getXjcDir() {
		return tryGetXjcDir().orElseThrow( () -> new ShapeCasterException( "Unable to retrieve XJC dir " ) );
	}

	public Optional<File> tryGetBinDir() {
	    return tryGetFolder( binDir );
    }

	public File getBinDir() {
		return tryGetBinDir().orElseThrow( () -> new ShapeCasterException( "Unable to retrieve BIN dir " ) );
	}

	private Optional<File> tryGetFolder( File folder ) {
		if ( folder == null ) {
			return Optional.empty();
		}
		if ( !folder.exists() ) {
			return folder.mkdirs() ? Optional.of( folder ) : Optional.empty();
		} else {
			return Optional.of( folder );
		}
	}


    private Document parseXML( File f ) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        InputSource xSource = new InputSource( new FileInputStream( f ) );
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse( xSource );
    }


}
