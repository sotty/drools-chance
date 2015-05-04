
import com.foo.MySubKlass;
import com.foo.MySubKlassImpl;
import com.foo.MySubKlass_;
import org.drools.core.common.ProjectClassLoader;
import org.drools.core.factmodel.traits.Entity;
import org.drools.core.factmodel.traits.InstantiatorFactory;
import org.drools.core.factmodel.traits.Thing;
import org.drools.core.factmodel.traits.Traitable;
import org.drools.core.factmodel.traits.TraitableBean;
import org.drools.core.util.StandaloneTraitFactory;
import org.junit.Test;
import org.test.MyKlass;
import org.test.MyTargetKlass;
import org.w3._2002._07.owl.ThingImpl;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MetadataTest {


    @Test
    public void testKlassAndMySubKlassWithImpl() {
        MySubKlass ski = new com.foo.MySubKlassImpl();
        ski.setSubProp( 42 );
        ski.setProp( "hello" );

        MySubKlass_ sk = new MySubKlass_( ski );

        assertEquals( 42, (int) sk.subProp.get( ski ) );
        assertEquals( "hello", sk.prop.get( ski ) );

        sk.modify().prop( "bye" ).subProp( -99 ).call();

        assertEquals( -99, (int) sk.subProp.get( ski ) );
        assertEquals( "bye", sk.prop.get( ski ) );
    }

    @Test
    public void testKlassAndMySubKlassWithHolderImpl() {
        com.foo.MySubKlassImpl ski = new com.foo.MySubKlassImpl();
        ski.setSubProp( 42 );
        ski.setProp( "hello" );

        MySubKlass_ sk = ski.get_();

        assertEquals( 42, (int) sk.subProp.get( ski ) );
        assertEquals( "hello", sk.prop.get( ski ) );

        sk.modify().prop( "bye" ).subProp( -99 ).call();

        assertEquals( -99, (int) sk.subProp.get( ski ) );
        assertEquals( "bye", sk.prop.get( ski ) );
    }


    @Test
    public void testKlassAndMySubKlassWithInterfaces() {
        MySubKlass ski = new Foo();
        ski.setSubProp( 42 );
        ski.setProp( "hello" );

        MySubKlass_ sk = new MySubKlass_( ski );

        assertEquals( 42, (int) sk.subProp.get( ski ) );
        assertEquals( "hello", sk.prop.get( ski ) );

        sk.modify().subProp( -99 ).prop( "bye" ).call();

        assertEquals( -99, (int) sk.subProp.get( ski ) );
        assertEquals( "bye", sk.prop.get( ski ) );

        System.out.println( ((Foo) ski).map );
        Map tgt = new HashMap();
        tgt.put( "prop", "bye" );
        tgt.put( "subProp", -99 );
        assertEquals( tgt, ((Foo) ski).map );
    }

    @Test
    public void testDelayedInstantiation() {
        MySubKlass sk = MySubKlass_.newMySubKlass( "123" ).prop( "hello" ).call();

        assertEquals( "hello", sk.getProp() );
        assertEquals( URI.create( "123" ), sk.getId() );
        assertTrue( sk instanceof MySubKlassImpl );
    }

    @Test
    public void testDelayedInstantiationWithFactory() {
        MySubKlass sk = MySubKlass_.newMySubKlass( "123" ).prop( "hello" ).setInstantiatorFactory(
                new InstantiatorFactory() {
                    @Override
                    public TraitableBean instantiate( Class<? extends Thing> trait, Object id ) {
                        Foo foo = new Foo();
                        foo.setDyEntryId( id.toString() );
                        return foo;
                    }

                    @Override
                    public Object createId( Class<?> klass ) {
                        return UUID.randomUUID().toString();
                    }
                }
        ).call();

        assertEquals( "hello", sk.getProp() );
        assertEquals( URI.create( "123" ), sk.getId() );
        assertTrue( sk instanceof Foo );
    }

    @Test
    public void testDonWithArgs() {
        Foo foo = new Foo();
        foo.setDyEntryId( "123" );
        foo._setDynamicProperties( new HashMap(  ) );

        MySubKlass sk = MySubKlass_.donMySubKlass( foo )
                .prop( "hello" )
                .subProp( 32 )
                .setTraitFactory( new StandaloneTraitFactory( ProjectClassLoader.createProjectClassLoader() ) )
                .call();

        assertEquals( "hello", sk.getProp() );
        assertEquals( URI.create( "123" ), sk.getId() );
    }


    @Test
    public void testImplicitID() {
        MySubKlass msk = MySubKlass_.newMySubKlass().call();
        assertNotNull( msk.getId() );
    }

    @Traitable
    public static class Foo extends ThingImpl implements MySubKlass {

        public Map<String,Object> map = new HashMap<String,Object>();

        @Override
        public List<MyTargetKlass> getLinks() {
            return null;
        }

        @Override
        public void setLinks( List<MyTargetKlass> value ) {

        }

        @Override
        public void addLinks( Object x ) {

        }

        @Override
        public void addLinks( MyTargetKlass x ) {

        }

        @Override
        public void removeLinks( Object x ) {

        }

        @Override
        public Date getTimestamp() {
            return (Date)map.get("timestamp");
        }

        @Override
        public void setTimestamp(Date value) {
            map.put("timestamp",value);
        }

        @Override
        public void addTimestamp(Date x) {

        }

        @Override
        public void removeTimestamp(Object x) {

        }

        @Override
        public String getProp() {
            return (String) map.get( "prop" );
        }

        @Override
        public void setProp( String value ) {
            map.put( "prop", value );
        }

        @Override
        public void addProp( String x ) {

        }

        @Override
        public void removeProp( Object x ) {

        }

        @Override
        public Boolean getFlag() {
            return (Boolean) map.get( "flag" );
        }

        @Override
        public void setFlag( Boolean value ) {
            map.put( "flag", value );
        }

        @Override
        public void addFlag( Boolean x ) {

        }

        @Override
        public void removeFlag( Object x ) {

        }

        @Override
        public Integer getSubProp() {
            return (Integer) map.get( "subProp" );
        }

        @Override
        public void setSubProp( Integer value ) {
            map.put( "subProp", value );
        }

        @Override
        public void addSubProp( Integer x ) {

        }

        @Override
        public void removeSubProp( Object x ) {

        }
    }


}

