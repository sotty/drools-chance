package org.drools.shapes;


import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.drools.core.metadata.MetadataHolder;
import org.kie.semantics.builder.model.compilers.SemanticXSDModelCompilerImpl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.util.HashMap;

public class
        MetaClassPlugin extends MetadataPlugin {

    private static final String metaAttribTempl = "metaAttrib";

    public String getOptionName() {
        return "Xmetaclass";
    }

    public String getUsage() {
        return "  -Xmetaclass";
    }



    @Override
    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {

        for (ClassOutline co : outline.getClasses() ) {

            CPluginCustomization c = co.target.getCustomizations().find( uri, "type" );
            if( c == null ) {
                continue;
            }

            HashMap<String, Object> map = new HashMap<>();
            map.put( "klassName", co.target.shortName );
            map.put( "typeName", c.element.getAttribute( "name" ) );
            map.put( "package", c.element.getAttribute( "package" ) );
            map.put( "supertypeName", c.element.getAttribute( "parent" ) );
            map.put( "supertypePackage", c.element.getAttribute( "parentPackage" ) );
            map.put( "typeIri", c.element.getAttribute( "iri" ) );

            String metaAttrib = SemanticXSDModelCompilerImpl.getTemplatedCode( metaAttribTempl, map );

            co.implClass._implements( MetadataHolder.class );
            co.implClass.direct( metaAttrib );

            c.markAsAcknowledged();

        }

        return true;
    }

}
