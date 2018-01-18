/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.semantics.builder.model;

import org.kie.semantics.utils.NameUtils;
import org.jdom.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SemanticXSDModelImpl extends XSDModelImpl implements SemanticXSDModel {


    private String index;

    private Map<String,Document> bindings = new HashMap<>(  );
    
    private String individualFactory;

    private Map<Namespace,String> packageInfos;

    private String empireConfig;

    private String persistenceXml;

    public Document getBindings( String namespace ) {
        return bindings.getOrDefault( namespace, null );
    }


    public void setBindings( String namespace, Document bindings ) {
        if ( this.bindings == null ) {
            this.bindings = new HashMap<>( 3 );
        }
        this.bindings.put( namespace, bindings );
    }

	@Override
	public String getSerializedBindings( String ns ) {
		try {
			return compactXML( getBindings( ns ) );
		} catch ( IOException | ParserConfigurationException | XPathExpressionException | SAXException | TransformerException e ) {
			e.printStackTrace();
		}
		return "";
	}

	public String getNamespacedPackageInfo( Namespace ns ) {
        return packageInfos.get( ns );
    }

    public void addNamespacedPackageInfo( Namespace ns, String namespaceFix ) {
        if ( packageInfos == null ) {
            packageInfos = new HashMap<>( 3 );
        }
        this.packageInfos.put( ns, namespaceFix );
    }

    public String getSerializedBindings() {
    	ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            for ( String ns : bindings.keySet() ) {
                os.write( compactXML( getBindings ( ns ) ).getBytes() );
            }
        } catch ( Exception e ) {
            return "";
        }
        return new String( os.toByteArray() );
    }



    private String compactXML( Document dox ) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
//        DocumentBuilderFactory doxFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = doxFactory.newDocumentBuilder();
//        InputSource is = new InputSource( new StringReader( source ) );
//        Document dox = builder.parse( is );
//        dox.normalize();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
                "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList)
                xpathExp.evaluate(dox, XPathConstants.NODESET);

        // Remove each empty text node from document.
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }

        TransformerFactory tFactory = TransformerFactory.newInstance();
        tFactory.setAttribute( "indent-number", 2 );
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        DOMSource domSrc = new DOMSource( dox );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult( baos );
        transformer.transform( domSrc, result );

        return new String( baos.toByteArray() );
    }

    public boolean streamBindings( File folder ) {
        try {


            for ( String prefix : prefixMap.keySet() ) {
                FileOutputStream os;
                Namespace namespace = prefixMap.get( prefix );

	            if ( "owl".equals( prefix ) ) {
		            os = new FileOutputStream( folder + File.separator + "global.xjb" );
	            } else if ( knownPrefixes.contains( prefix ) ) {
                    continue;
                } else {
                    String path = folder.getPath() + File.separator + NameUtils.namespaceURIToPackage( namespace.getURI() ) + ".xjb";
                    File target = new File( path );
                    if ( checkForBindingOverride( target ) ) {
                    	System.err.println( "Discarding " + target.getPath() );
                    	System.err.println( compactXML( getBindings( namespace.getURI() ) ) );
                    	continue;
                    }
                    os = new FileOutputStream( target );
                }

	            Document bindings = getBindings( namespace.getURI() );
	            checkSchemaLocationOverride( bindings, schemaLocations.get( namespace ) );

	            os.write( compactXML( bindings ).getBytes() );
	            os.flush();
	            os.close();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void checkSchemaLocationOverride( Document bindings, String tgtSchemaLoc ) {
        if ( tgtSchemaLoc != null ) {
            tgtSchemaLoc = tgtSchemaLoc.substring( tgtSchemaLoc.lastIndexOf( File.separator ) + 1 );
            NodeList bx = bindings.getElementsByTagName( "bindings" );
            for ( int j = 0; j < bx.getLength(); j++ ) {
                Element bind = (Element) bx.item( j );
//                        if ( "/xsd:schema".equals( bind.getAttribute( "node" ) ) ) {
                if ( bind.hasAttribute( "schemaLocation" ) ) {
                    String currSchemaLoc = bind.getAttribute( "schemaLocation" );

                    if ( ! currSchemaLoc.equals( tgtSchemaLoc ) ) {
                        bind.setAttribute( "schemaLocation", tgtSchemaLoc );
                    }
                    break;
                }
            }
        }
    }

    private boolean checkForBindingOverride( File tgt ) {
        return tgt.exists();
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public boolean streamIndex( OutputStream os ) {
        try {
            os.write( index.getBytes() );
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean streamPersistenceXml( OutputStream os ) {
        try {
            os.write( persistenceXml.getBytes() );
        } catch (IOException e) {
            return false;
        }
        return true;
    }


	public String getIndividualFactory() {
        return individualFactory;
    }

    public void setIndividualFactory(String individualFactory) {
        this.individualFactory = individualFactory;
    }

    public String getEmpireConfig() {
        return empireConfig;
    }

    public void setEmpireConfig( String empireConfig ) {
        this.empireConfig = empireConfig;
    }

    public String getPersistenceXml() {
        return persistenceXml;
    }

    public void setPersistenceXml(String persistenceXml) {
        this.persistenceXml = persistenceXml;
    }
}
