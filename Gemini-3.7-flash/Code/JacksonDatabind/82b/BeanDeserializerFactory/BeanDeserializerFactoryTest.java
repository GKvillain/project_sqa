package com.fasterxml.jackson.databind.deser;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;

public class BeanDeserializerFactoryTest {

    // Simple test beans
    static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() { }
        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;
        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true, value = {"ignoredField"})
    static class IgnoralBean {
        public int id;
        public String ignoredField;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static abstract class IgnoralBase {
        public int baseId;
    }

    static class IgnoralSub extends IgnoralBase {
        public int subId;
    }

    static class CreatorBean {
        public final int a;
        public final String b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }
    }

    interface MyInterface {
        int getVal();
    }

    static class MyInterfaceImpl implements MyInterface {
        public int val;
        @Override
        public int getVal() { return val; }
    }

    static class CustomSubFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public CustomSubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @JsonDeserialize(builder = BuilderBean.Builder.class)
    static class BuilderBean {
        final int x;
        final String y;

        BuilderBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        @JsonPOJOBuilder(withPrefix = "set")
        static class Builder {
            int x;
            String y;

            public Builder setX(int x) { this.x = x; return this; }
            public Builder setY(String y) { this.y = y; return this; }
            public BuilderBean build() { return new BuilderBean(x, y); }
        }
    }

    static class InjectBean {
        public int id;
        @JacksonInject
        public String injectedVal;
    }

    static class ParentNode {
        public int id;
        @JsonManagedReference
        public List<ChildNode> children;
    }

    static class ChildNode {
        public String name;
        @JsonBackReference
        public ParentNode parent;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiedNode {
        public int id;
        public IdentifiedNode next;
    }

    // Tests singleton instance and withConfig behavior
    @Test
    public void testInstanceAndWithConfig_sameConfig_returnsSameInstance() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        assertNotNull(factory);
        DeserializerFactory same = factory.withConfig(factory.getFactoryConfig());
        assertSame(factory, same);
    }

    // Tests withConfig with different configuration
    @Test
    public void testWithConfig_newConfig_returnsNewInstance() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    // Tests withConfig on improperly overridden subclass throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWithConfig_subclassNotOverriding_throwsIllegalStateException() {
        CustomSubFactory subFactory = new CustomSubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    // Tests createBeanDeserializer for a normal POJO
    @Test
    public void testCreateBeanDeserializer_simplePOJO_createsValidDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(
                ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer for Throwable types
    @Test
    public void testCreateBeanDeserializer_throwableType_buildsThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(
                ctxt, type, beanDesc);
        assertNotNull(deser);
        assertTrue(deser.getClass().getName().contains("ThrowableDeserializer")
                || deser instanceof BeanDeserializer);
    }

    // Tests createBeanDeserializer with abstract type resolved via AbstractTypeResolver
    @Test
    public void testCreateBeanDeserializer_abstractTypeWithResolver_resolvesAndCreates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(MyInterface.class, MyInterfaceImpl.class);
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        MyInterface result = mapper.readValue("{\"val\": 42}", MyInterface.class);
        assertNotNull(result);
        assertEquals(42, result.getVal());
    }

    // Tests isPotentialBeanType with non-bean classes
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveType_throwsException() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    // Tests isPotentialBeanType with array class
    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayType_throwsException() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    // Tests isPotentialBeanType with valid bean class
    @Test
    public void testIsPotentialBeanType_regularBean_returnsTrue() {
        boolean potential = BeanDeserializerFactory.instance.isPotentialBeanType(SimpleBean.class);
        assertTrue(potential);
    }

    // Tests checkIllegalTypes against dangerous classes via reflection/TypeFactory
    @Test
    public void testCheckIllegalTypes_illegalClassName_reportsBadType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            Class<?> cls = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
            JavaType type = mapper.constructType(cls);
            BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
            BeanDeserializerFactory.instance.checkIllegalTypes(ctxt, type, beanDesc);
            fail("Expected JsonMappingException for illegal type");
        } catch (ClassNotFoundException e) {
            // Ignored if class is not available in the current runtime environment
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("prevented for security reasons")
                    || e.getMessage().contains("Illegal type"));
        }
    }

    // Tests deserialization with @JsonCreator properties
    @Test
    public void testCreateBeanDeserializer_creatorBean_deserializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean result = mapper.readValue("{\"a\": 10, \"b\": \"test\"}", CreatorBean.class);
        assertNotNull(result);
        assertEquals(10, result.a);
        assertEquals("test", result.b);
    }

    // Tests deserialization with @JsonIgnoreProperties
    @Test
    public void testCreateBeanDeserializer_ignoredProperties_ignoresSpecifiedAndUnknown() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoralBean result = mapper.readValue("{\"id\": 1, \"ignoredField\": \"foo\", \"extra\": \"bar\"}", IgnoralBean.class);
        assertNotNull(result);
        assertEquals(1, result.id);
        assertNull(result.ignoredField);
    }

    // Tests deserialization with inherited @JsonIgnoreProperties from superclass
    @Test
    public void testCreateBeanDeserializer_inheritedIgnorals_ignoresUnknownProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoralSub result = mapper.readValue("{\"baseId\": 5, \"subId\": 10, \"unknownProp\": 123}", IgnoralSub.class);
        assertNotNull(result);
        assertEquals(5, result.baseId);
        assertEquals(10, result.subId);
    }

    // Tests createBuilderBasedDeserializer for builder-annotated bean
    @Test
    public void testCreateBuilderBasedDeserializer_builderBean_deserializesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(BuilderBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBuilderBasedDeserializer(
                ctxt, type, beanDesc, BuilderBean.Builder.class);
        assertNotNull(deser);
        assertTrue(deser.getClass().getName().contains("BuilderBasedDeserializer")
                || deser instanceof BeanDeserializerBase);

        BuilderBean result = mapper.readValue("{\"x\": 7, \"y\": \"hello\"}", BuilderBean.class);
        assertNotNull(result);
        assertEquals(7, result.x);
        assertEquals("hello", result.y);
    }

    // Tests addInjectables during bean deserialization
    @Test
    public void testAddInjectables_injectedProperty_populatesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue(String.class, "injected-value");
        mapper.setInjectableValues(injectables);

        InjectBean result = mapper.readValue("{\"id\": 99}", InjectBean.class);
        assertNotNull(result);
        assertEquals(99, result.id);
        assertEquals("injected-value", result.injectedVal);
    }

    // Tests addBackReferenceProperties during bean deserialization
    @Test
    public void testAddBackReferenceProperties_managedAndBackRefs_linksProperly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\": 1, \"children\": [{\"name\": \"c1\"}, {\"name\": \"c2\"}]}";

        ParentNode parent = mapper.readValue(json, ParentNode.class);
        assertNotNull(parent);
        assertEquals(1, parent.id);
        assertNotNull(parent.children);
        assertEquals(2, parent.children.size());
        assertSame(parent, parent.children.get(0).parent);
        assertSame(parent, parent.children.get(1).parent);
    }

    // Tests addObjectIdReader during bean deserialization
    @Test
    public void testAddObjectIdReader_cyclicIdentityReferences_resolvesIdentity() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\": 10, \"next\": 10}";

        IdentifiedNode node = mapper.readValue(json, IdentifiedNode.class);
        assertNotNull(node);
        assertEquals(10, node.id);
        assertSame(node, node.next);
    }

    // Tests findValueInstantiator with custom value instantiators in factory config
    @Test
    public void testFindValueInstantiator_customInstantiator_usedByFactory() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        SimpleValueInstantiators instantiators = new SimpleValueInstantiators();
        module.setValueInstantiators(instantiators);
        mapper.registerModule(module);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        ValueInstantiator instantiator = BeanDeserializerFactory.instance.findValueInstantiator(ctxt, beanDesc);
        assertNotNull(instantiator);
    }
}