package com.fasterxml.jackson.databind.deser;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test class for BeanDeserializerFactory, targeting
 * defect detection and branch coverage.
 */
public class BeanDeserializerFactoryTest {

    // Helper: reflectively call protected method isPotentialBeanType
    private boolean callIsPotentialBeanType(Class<?> type) throws Exception {
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("isPotentialBeanType", Class.class);
        m.setAccessible(true);
        return (Boolean) m.invoke(BeanDeserializerFactory.instance, type);
    }

    // Helper: reflectively call protected method checkIllegalTypes
    private void callCheckIllegalTypes(DeserializationContext ctxt, JavaType type,
                                       BeanDescription beanDesc) throws Exception {
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("checkIllegalTypes", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        m.invoke(BeanDeserializerFactory.instance, ctxt, type, beanDesc);
    }

    // Helper: reflectively get _cfgIllegalClassNames field
    private Set<String> getCfgIllegalClassNames(BeanDeserializerFactory factory) throws Exception {
        Field f = BeanDeserializerFactory.class.getDeclaredField("_cfgIllegalClassNames");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> names = (Set<String>) f.get(factory);
        return names;
    }

    // Helper: set _cfgIllegalClassNames field
    private void setCfgIllegalClassNames(BeanDeserializerFactory factory, Set<String> names) throws Exception {
        Field f = BeanDeserializerFactory.class.getDeclaredField("_cfgIllegalClassNames");
        f.setAccessible(true);
        f.set(factory, names);
    }

    // Helper: obtain DeserializationContext from an ObjectMapper
    private DeserializationContext createDeserializationContext() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getDeserializationConfig().createContext(mapper.getDeserializationConfig()
                .getBaseSettings(), null);
    }

    // Helper: create DefaultDeserializationContext for testing
    private DeserializationContext createDefaultDeserializationContext(DeserializationConfig config) {
        // Use the factory method from DefaultDeserializationContext
        return new DefaultDeserializationContext.Impl(
                (DeserializerFactory) BeanDeserializerFactory.instance,
                config.getBaseSettings());
    }

    //-----------------------------------------------------------------
    // Test isPotentialBeanType
    //-----------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_primitiveClass_throwsException() throws Exception {
        callIsPotentialBeanType(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_arrayClass_throwsException() throws Exception {
        callIsPotentialBeanType(int[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_enumClass_throwsException() throws Exception {
        callIsPotentialBeanType(Thread.State.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_proxyClass_throwsException() throws Exception {
        Class<?> proxyClass = Proxy.getProxyClass(getClass().getClassLoader(), Runnable.class);
        callIsPotentialBeanType(proxyClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanType_localClass_throwsException() throws Exception {
        // Define a local class inside a method
        class LocalInner { }
        callIsPotentialBeanType(LocalInner.class);
    }

    @Test
    public void testIsPotentialBeanType_staticInnerClass_returnsTrue() throws Exception {
        assertTrue(callIsPotentialBeanType(SimpleBean.class));
    }

    @Test
    public void testIsPotentialBeanType_normalClass_returnsTrue() throws Exception {
        assertTrue(callIsPotentialBeanType(String.class));
    }

    //-----------------------------------------------------------------
    // Test checkIllegalTypes and default illegal class names
    //-----------------------------------------------------------------

    @Test
    public void testDefaultIllegalClassNames_containsExpectedClasses() throws Exception {
        Set<String> names = getCfgIllegalClassNames(BeanDeserializerFactory.instance);
        assertTrue("Missing InvokerTransformer (commons-collections3)",
                names.contains("org.apache.commons.collections.functors.InvokerTransformer"));
        assertTrue("Missing InstantiateTransformer (commons-collections3)",
                names.contains("org.apache.commons.collections.functors.InstantiateTransformer"));
        assertTrue("Missing InvokerTransformer (commons-collections4)",
                names.contains("org.apache.commons.collections4.functors.InvokerTransformer"));
        assertTrue("Missing InstantiateTransformer (commons-collections4)",
                names.contains("org.apache.commons.collections4.functors.InstantiateTransformer"));
        assertTrue("Missing ConvertedClosure",
                names.contains("org.codehaus.groovy.runtime.ConvertedClosure"));
        assertTrue("Missing MethodClosure",
                names.contains("org.codehaus.groovy.runtime.MethodClosure"));
        assertTrue("Missing ObjectFactory",
                names.contains("org.springframework.beans.factory.ObjectFactory"));
        assertTrue("Missing TemplatesImpl (com.sun)",
                names.contains("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));
        assertTrue("Missing TemplatesImpl (apache)",
                names.contains("org.apache.xalan.xsltc.trax.TemplatesImpl"));
    }

    @Test(expected = JsonMappingException.class)
    public void testCheckIllegalTypes_illegalClass_throwsException() throws Exception {
        // Temporarily add a controllable illegal class name
        BeanDeserializerFactory factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        Set<String> illegal = new HashSet<>();
        illegal.add("com.fasterxml.jackson.databind.deser.BeanDeserializerFactoryTest$SimpleBean");
        setCfgIllegalClassNames(factory, illegal);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        // Use a real DeserializationContext (which throws via reportBadTypeDefinition)
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        // Invoke checkIllegalTypes via reflection (or directly if accessible?)
        // We'll call the method on factory
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("checkIllegalTypes", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        m.invoke(factory, ctxt, type, beanDesc);
    }

    //-----------------------------------------------------------------
    // Test createBeanDeserializer for Throwable
    //-----------------------------------------------------------------

    @Test
    public void testCreateBeanDeserializer_throwableType_returnsThrowableDeserializer() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(RuntimeException.class);
        BeanDescription beanDesc = config.introspect(type);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        JsonDeserializer<?> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        // The result should be an instance of ThrowableDeserializer
        assertNotNull("Throwable deserializer should not be null", deser);
        assertTrue("Should be ThrowableDeserializer", deser instanceof ThrowableDeserializer);
    }

    //-----------------------------------------------------------------
    // Test createBeanDeserializer for normal bean
    //-----------------------------------------------------------------

    @Test
    public void testCreateBeanDeserializer_beanType_returnsBeanDeserializer() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        JsonDeserializer<?> deser = factory.createBeanDeserializer(ctxt, type, beanDesc);
        assertNotNull("Bean deserializer should not be null", deser);
        // It should be a BeanDeserializer (or subclass)
        assertTrue("Should be BeanDeserializer", deser.getClass().getName().contains("BeanDeserializer"));
    }

    //-----------------------------------------------------------------
    // Test createBuilderBasedDeserializer (basic)
    //-----------------------------------------------------------------

    @Test
    public void testCreateBuilderBasedDeserializer_validBuilder_returnsNonNulll() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType valueType = TypeFactory.defaultInstance().constructType(BuiltFromBuilder.class);
        BeanDescription builderDesc = config.introspectForBuilder(
                TypeFactory.defaultInstance().constructType(SimpleBuilder.class));
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        JsonDeserializer<?> deser = factory.createBuilderBasedDeserializer(ctxt, valueType, builderDesc, SimpleBuilder.class);
        assertNotNull("Builder-based deserializer should not be null", deser);
    }

    //-----------------------------------------------------------------
    // Test findStdDeserializer for standard type
    //-----------------------------------------------------------------

    @Test
    public void testFindStdDeserializer_stringType_returnsNonNull() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanDescription beanDesc = config.introspect(type);
        // Invoke protected method via reflection
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("findStdDeserializer", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        JsonDeserializer<?> deser = (JsonDeserializer<?>) m.invoke(factory, ctxt, type, beanDesc);
        assertNotNull("Should find standard deserializer for String", deser);
    }

    //-----------------------------------------------------------------
    // Test isIgnorableType via reflection
    //-----------------------------------------------------------------

    @Test
    public void testIsIgnorableType_nonIgnorable_returnsFalse() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("isIgnorableType", DeserializationConfig.class,
                        BeanDescription.class, Class.class, Map.class);
        m.setAccessible(true);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        Map<Class<?>,Boolean> cache = new HashMap<>();
        boolean result = (Boolean) m.invoke(factory, config, beanDesc, SimpleBean.class, cache);
        assertFalse("SimpleBean should not be ignorable", result);
    }

    @Test
    public void testIsIgnorableType_ignorableType_returnsTrue() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("isIgnorableType", DeserializationConfig.class,
                        BeanDescription.class, Class.class, Map.class);
        m.setAccessible(true);
        JavaType type = TypeFactory.defaultInstance().constructType(IgnorableType.class);
        BeanDescription beanDesc = config.introspect(type);
        Map<Class<?>,Boolean> cache = new HashMap<>();
        boolean result = (Boolean) m.invoke(factory, config, beanDesc, IgnorableType.class, cache);
        assertTrue("IgnorableType should be ignorable", result);
    }

    //-----------------------------------------------------------------
    // Test materializeAbstractType with a simple resolver
    //-----------------------------------------------------------------

    @Test
    public void testMaterializeAbstractType_withResolver_returnsConcrete() throws Exception {
        // Create a factory config with a resolver that maps AbstractBean -> SimpleBean
        DeserializerFactoryConfig configWithResolver = new DeserializerFactoryConfig()
                .withAbstractTypeResolver(new AbstractTypeResolver() {
                    @Override
                    public JavaType resolveAbstractType(DeserializationConfig config, BeanDescription beanDesc) {
                        if (beanDesc.getBeanClass() == AbstractBean.class) {
                            return TypeFactory.defaultInstance().constructType(SimpleBean.class);
                        }
                        return null;
                    }
                });
        BeanDeserializerFactory factory = new BeanDeserializerFactory(configWithResolver);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType abstractType = TypeFactory.defaultInstance().constructType(AbstractBean.class);
        BeanDescription abstractDesc = config.introspect(abstractType);
        // Invoke materializeAbstractType
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("materializeAbstractType", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        JavaType result = (JavaType) m.invoke(factory, ctxt, abstractType, abstractDesc);
        assertNotNull("Materialized type should not be null", result);
        assertEquals("Should map to SimpleBean", SimpleBean.class, result.getRawClass());
    }

    //-----------------------------------------------------------------
    // NEW: Test checkIllegalTypes with a class that is NOT illegal (no exception)
    //-----------------------------------------------------------------

    @Test
    public void testCheckIllegalTypes_legalClass_noException() throws Exception {
        BeanDeserializerFactory factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        // Use default illegal names, which should NOT include our test class
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        // Invoke checkIllegalTypes - should not throw
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("checkIllegalTypes", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        m.invoke(factory, ctxt, type, beanDesc);
        // If we get here without exception, test passes
    }

    //-----------------------------------------------------------------
    // NEW: Test checkIllegalTypes with null illegal names set (empty set)
    //-----------------------------------------------------------------

    @Test
    public void testCheckIllegalTypes_emptyIllegalSet_noException() throws Exception {
        BeanDeserializerFactory factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        // Set empty set of illegal class names
        setCfgIllegalClassNames(factory, new HashSet<String>());
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        DeserializationContext ctxt = createDefaultDeserializationContext(config);
        // Invoke checkIllegalTypes - should not throw
        java.lang.reflect.Method m = BeanDeserializerFactory.class
                .getDeclaredMethod("checkIllegalTypes", DeserializationContext.class,
                        JavaType.class, BeanDescription.class);
        m.setAccessible(true);
        m.invoke(factory, ctxt, type, beanDesc);
    }

    //-----------------------------------------------------------------
    // Inner classes for test data
    //-----------------------------------------------------------------

    // Simple bean for testing
    public static class SimpleBean {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Abstract bean for materialization test
    public abstract static class AbstractBean {
        public abstract String getValue();
    }

    // Builder class for builder-based deserialization test
    public static class SimpleBuilder {
        private String value;
        public SimpleBuilder withValue(String v) { this.value = v; return this; }
        public BuiltFromBuilder build() { return new BuiltFromBuilder(value); }
    }

    // The type built by the builder
    public static class BuiltFromBuilder {
        private String value;
        public BuiltFromBuilder(String v) { this.value = v; }
        public String getValue() { return value; }
    }

    // Class annotated with @JsonIgnoreType
    @JsonIgnoreType
    public static class IgnorableType {
        public int x;
    }
}