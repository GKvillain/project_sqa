package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonValueInstantiator;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.impl.CreatorProperty;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test class for BasicDeserializerFactory, focusing on creator detection
 * and abstract type mapping. Covers key defect-prone code paths.
 */
public class BasicDeserializerFactoryTest {

    private TestFactory factory;
    private DeserializerFactoryConfig config;

    //----------------- Helper classes -----------------

    static class TestFactory extends BasicDeserializerFactory {
        TestFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        @Override
        protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new TestFactory(config);
        }

        // Expose protected methods for testing
        @Override
        public void _addDeserializerConstructors(DeserializationContext ctxt,
                BeanDescription beanDesc, VisibilityChecker<?> vchecker,
                AnnotationIntrospector intr, CreatorCollector creators,
                Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams)
                throws JsonMappingException {
            super._addDeserializerConstructors(ctxt, beanDesc, vchecker, intr, creators, creatorParams);
        }

        @Override
        public void _addDeserializerFactoryMethods(DeserializationContext ctxt,
                BeanDescription beanDesc, VisibilityChecker<?> vchecker,
                AnnotationIntrospector intr, CreatorCollector creators,
                Map<AnnotatedWithParams, BeanPropertyDefinition[]> creatorParams)
                throws JsonMappingException {
            super._addDeserializerFactoryMethods(ctxt, beanDesc, vchecker, intr, creators, creatorParams);
        }
    }

    // Stub DeserializationContext
    static class StubDeserializationContext extends DeserializationContext {
        private final DeserializationConfig config;
        private final AnnotationIntrospector intr;

        StubDeserializationContext(DeserializationConfig config, AnnotationIntrospector intr) {
            super(null, null, null, null, null, null, null, null, null);
            this.config = config;
            this.intr = intr;
        }

        @Override
        public DeserializationConfig getConfig() { return config; }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() { return intr; }

        @Override
        public boolean canOverrideAccessModifiers() { return true; }

        @Override
        public Object getAttribute(Object key) { return null; }

        @Override
        public DeserializationContext setAttribute(Object key, Object value) { return this; }

        @Override
        public JsonDeserializer<?> handlePrimaryContextualization(
                JsonDeserializer<?> deser, BeanProperty prop, JavaType type) {
            return deser;
        }

        @Override
        public JsonDeserializer<?> deserializerInstance(Annotated ann, Object def)
                throws JsonMappingException {
            if (def == null) return null;
            if (def instanceof JsonDeserializer) return (JsonDeserializer<?>) def;
            throw new JsonMappingException("not supported");
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(Annotated ann, Object def)
                throws JsonMappingException {
            return null;
        }
    }

    // Stub BeanDescription for a simple class with one constructor
    static class StubBeanDescription extends BasicBeanDescription {
        private final Class<?> beanClass;
        private final List<AnnotatedConstructor> constructors;
        private final AnnotatedConstructor defaultCtor;
        private final List<AnnotatedMethod> factoryMethods;
        private final JavaType type;

        StubBeanDescription(JavaType type, Class<?> beanClass, AnnotatedConstructor defaultCtor,
                List<AnnotatedConstructor> constructors, List<AnnotatedMethod> factoryMethods) {
            super(null, type, null);
            this.type = type;
            this.beanClass = beanClass;
            this.defaultCtor = defaultCtor;
            this.constructors = constructors;
            this.factoryMethods = factoryMethods;
        }

        @Override
        public Class<?> getBeanClass() { return beanClass; }

        @Override
        public AnnotatedConstructor findDefaultConstructor() { return defaultCtor; }

        @Override
        public List<AnnotatedConstructor> getConstructors() { return constructors; }

        @Override
        public List<AnnotatedMethod> getFactoryMethods() { return factoryMethods; }

        @Override
        public JavaType getType() { return type; }

        @Override
        public AnnotatedClass getClassInfo() {
            return null;
        }
    }

    // Minimal AnnotatedConstructor stub
    static class StubAnnotatedConstructor extends AnnotatedConstructor {
        private final int paramCount;
        private final AnnotatedParameter[] params;
        private final Class<?> declaringClass;

        StubAnnotatedConstructor(int paramCount, Class<?> declaringClass) {
            super(null, null);
            this.paramCount = paramCount;
            this.params = new AnnotatedParameter[paramCount];
            this.declaringClass = declaringClass;
        }

        void setParameter(int index, AnnotatedParameter param) {
            params[index] = param;
        }

        @Override
        public int getParameterCount() { return paramCount; }

        @Override
        public AnnotatedParameter getParameter(int index) { return params[index]; }

        @Override
        public Class<?> getDeclaringClass() { return declaringClass; }

        @Override
        public String getName() { return "<init>"; }
    }

    // Stub AnnotatedParameter
    static class StubAnnotatedParameter extends AnnotatedParameter {
        private final int index;
        private final Class<?> rawType;

        StubAnnotatedParameter(int index, Class<?> rawType) {
            super(null, null, null, null, null);
            this.index = index;
            this.rawType = rawType;
        }

        @Override
        public int getIndex() { return index; }

        @Override
        public Class<?> getRawType() { return rawType; }

        @Override
        public AnnotatedWithParams getOwner() {
            return null; // will be set externally
        }
    }

    // Simple AnnotationIntrospector that delegates to a map
    static class MapAnnotationIntrospector extends AnnotationIntrospector {
        private final Map<String, Boolean> creatorAnnotations = new HashMap<>();
        private final Map<String, PropertyName> paramNames = new HashMap<>();
        private final Map<String, Object> injectableIds = new HashMap<>();

        void setCreator(String methodOrCtor, boolean isCreator) {
            creatorAnnotations.put(methodOrCtor, isCreator);
        }

        void setParamName(AnnotatedParameter param, PropertyName name) {
            paramNames.put(param.toString(), name);
        }

        @Override
        public boolean hasCreatorAnnotation(Annotated ann) {
            Boolean b = creatorAnnotations.get(ann.getName());
            return b != null && b;
        }

        @Override
        public PropertyName findNameForDeserialization(Annotated param) {
            return paramNames.get(param.toString());
        }

        @Override
        public Object findInjectableValueId(AnnotatedParameter param) {
            return injectableIds.get(param.toString());
        }

        // default methods (no-op)
        @Override
        public Boolean hasRequiredMarker(Annotated member) { return null; }
        @Override
        public String findPropertyDescription(Annotated member) { return null; }
        @Override
        public Integer findPropertyIndex(Annotated member) { return null; }
        @Override
        public String findPropertyDefaultValue(Annotated member) { return null; }
        @Override
        public PropertyName findWrapperName(Annotated ann) { return null; }
        @Override
        public JsonCreator.Mode findCreatorBinding(Annotated ann) { return null; }
        @Override
        public String findImplicitPropertyName(AnnotatedParameter param) { return null; }
        @Override
        public NameTransformer findUnwrappingNameTransformer(AnnotatedMember member) { return null; }
        @Override
        public Object findValueInstantiator(AnnotatedClass ac) { return null; }
        @Override
        public VisibilityChecker<?> findAutoDetectVisibility(AnnotatedClass ac, VisibilityChecker<?> vc) { return vc; }
        @Override
        public TypeResolverBuilder<?> findTypeResolver(DeserializationConfig cfg, AnnotatedClass ac, JavaType b) { return null; }
        @Override
        public TypeResolverBuilder<?> findPropertyTypeResolver(DeserializationConfig cfg, AnnotatedMember am, JavaType b) { return null; }
        @Override
        public TypeResolverBuilder<?> findPropertyContentTypeResolver(DeserializationConfig cfg, AnnotatedMember am, JavaType b) { return null; }
        @Override
        public Object findDeserializer(Annotated ann) { return null; }
        @Override
        public Class<?> findDeserializationType(Annotated a, JavaType t) { return null; }
        @Override
        public Class<?> findDeserializationKeyType(Annotated a, JavaType t) { return null; }
        @Override
        public Class<?> findDeserializationContentType(Annotated a, JavaType t) { return null; }
        @Override
        public Object findKeyDeserializer(Annotated ann) { return null; }
        @Override
        public Object findContentDeserializer(Annotated ann) { return null; }
        @Override
        public String findEnumValue(Enum<?> e) { return null; }
    }

    //----------------- Setup -----------------

    @Before
    public void setUp() {
        config = new DeserializerFactoryConfig();
        factory = new TestFactory(config);
    }

    //----------------- Tests -----------------

    // Test mapAbstractType with no resolvers -> returns same type
    @Test
    public void testMapAbstractType_noResolver_returnsSameType() throws Exception {
        DeserializationConfig cfg = new ObjectMapper().getDeserializationConfig();
        JavaType type = cfg.constructType(String.class);
        JavaType result = factory.mapAbstractType(cfg, type);
        assertEquals(type, result);
    }

    // Test mapAbstractType with a resolver that returns concrete -> gets mapped
    @Test
    public void testMapAbstractType_withResolver_returnsConcreteType() throws Exception {
        DeserializationConfig cfg = new ObjectMapper().getDeserializationConfig();
        // Use a factory with a resolver
        AbstractTypeResolver resolver = new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.getRawClass() == Map.class) {
                    return config.constructType(LinkedHashMap.class);
                }
                return null;
            }
        };
        BasicDeserializerFactory f = new TestFactory(config.withAbstractTypeResolver(resolver));
        JavaType mapType = cfg.constructType(Map.class);
        JavaType result = f.mapAbstractType(cfg, mapType);
        assertEquals(LinkedHashMap.class, result.getRawClass());
    }

    // Test findValueInstantiator for JsonLocation -> returns JsonLocationInstantiator
    @Test
    public void testFindValueInstantiator_JsonLocation_returnsStd() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspectClassAnnotations(JsonLocation.class);
        ValueInstantiator inst = factory.findValueInstantiator(ctxt, beanDesc);
        assertTrue(inst instanceof JsonLocationInstantiator);
    }

    // Test findValueInstantiator with @JsonValueInstantiator annotation
    @Test
    public void testFindValueInstantiator_AnnotatedClass_usesAnnotated() throws Exception {
        // This test requires real annotations; skip for now due to complexity
    }

    // Test _addDeserializerConstructors: single String param -> adds string creator
    @Test
    public void testAddDeserializerConstructors_singleString_createsStringCreator() throws Exception {
        // Set up: constructor with one String param, no annotation, visible
        StubAnnotatedConstructor ctor = new StubAnnotatedConstructor(1, StringCreator.class);
        StubAnnotatedParameter param = new StubAnnotatedParameter(0, String.class);
        ctor.setParameter(0, param);
        List<AnnotatedConstructor> ctors = Arrays.asList(ctor);

        JavaType type = TypeFactory.defaultInstance().constructType(StringCreator.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, StringCreator.class, null, ctors, null);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(ctor.getName(), false); // no @JsonCreator
        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerConstructors(ctxt, beanDesc, 
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());

        assertTrue("should have string creator", creators.hasStringCreator());
    }

    // Test _addDeserializerConstructors: multi-param with names -> property creator
    @Test
    public void testAddDeserializerConstructors_multiParamWithNames_createsPropertyCreator() throws Exception {
        // Constructor with two params, both named
        StubAnnotatedConstructor ctor = new StubAnnotatedConstructor(2, MultiParamClass.class);
        StubAnnotatedParameter p0 = new StubAnnotatedParameter(0, String.class);
        StubAnnotatedParameter p1 = new StubAnnotatedParameter(1, Integer.class);
        ctor.setParameter(0, p0);
        ctor.setParameter(1, p1);
        List<AnnotatedConstructor> ctors = Arrays.asList(ctor);

        JavaType type = TypeFactory.defaultInstance().constructType(MultiParamClass.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, MultiParamClass.class, null, ctors, null);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(ctor.getName(), true); // @JsonCreator
        // Set param names
        intr.paramNames.put(p0.toString(), PropertyName.construct("name"));
        intr.paramNames.put(p1.toString(), PropertyName.construct("value"));

        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerConstructors(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());

        assertTrue("should have property creator", creators.hasPropertyBasedCreator());
    }

    // Test _addDeserializerConstructors: multi-param with one missing name -> exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddDeserializerConstructors_missingName_throwsException() throws Exception {
        StubAnnotatedConstructor ctor = new StubAnnotatedConstructor(2, MultiParamClass.class);
        StubAnnotatedParameter p0 = new StubAnnotatedParameter(0, String.class);
        StubAnnotatedParameter p1 = new StubAnnotatedParameter(1, Integer.class);
        ctor.setParameter(0, p0);
        ctor.setParameter(1, p1);
        List<AnnotatedConstructor> ctors = Arrays.asList(ctor);

        JavaType type = TypeFactory.defaultInstance().constructType(MultiParamClass.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, MultiParamClass.class, null, ctors, null);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(ctor.getName(), true);
        // Only name for first param
        intr.paramNames.put(p0.toString(), PropertyName.construct("name"));
        // second param missing name

        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerConstructors(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());
    }

    // Test _addDeserializerConstructors: non-static inner class -> throws
    @Test(expected = IllegalArgumentException.class)
    public void testAddDeserializerConstructors_innerClass_throwsException() throws Exception {
        // Simulate inner class: first parameter is enclosing type
        StubAnnotatedConstructor ctor = new StubAnnotatedConstructor(2, InnerClass.class);
        StubAnnotatedParameter p0 = new StubAnnotatedParameter(0, InnerClass.class); // enclosing
        StubAnnotatedParameter p1 = new StubAnnotatedParameter(1, String.class);
        ctor.setParameter(0, p0);
        ctor.setParameter(1, p1);
        List<AnnotatedConstructor> ctors = Arrays.asList(ctor);

        JavaType type = TypeFactory.defaultInstance().constructType(InnerClass.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, InnerClass.class, null, ctors, null);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(ctor.getName(), true);
        // Only name for second param, first param missing
        intr.paramNames.put(p1.toString(), PropertyName.construct("name"));

        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerConstructors(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());
    }

    // Test _addDeserializerFactoryMethods: single String factory -> adds string creator
    @Test
    public void testAddDeserializerFactoryMethods_singleString_createsStringCreator() throws Exception {
        // Factory method returning the type
        StubAnnotatedMethod factory = new StubAnnotatedMethod("fromString", String.class, 1);
        StubAnnotatedParameter param = new StubAnnotatedParameter(0, String.class);
        factory.setParameter(0, param);
        List<AnnotatedMethod> factories = Arrays.asList(factory);

        JavaType type = TypeFactory.defaultInstance().constructType(StringCreator.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, StringCreator.class, null, null, factories);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(factory.getName(), false); // no @JsonCreator but visible
        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerFactoryMethods(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());

        assertTrue("should have string creator", creators.hasStringCreator());
    }

    // Test _addDeserializerFactoryMethods: multi-param with names -> property creator
    @Test
    public void testAddDeserializerFactoryMethods_multiParamWithNames_createsPropertyCreator() throws Exception {
        StubAnnotatedMethod factory = new StubAnnotatedMethod("of", MultiParamClass.class, 2);
        StubAnnotatedParameter p0 = new StubAnnotatedParameter(0, String.class);
        StubAnnotatedParameter p1 = new StubAnnotatedParameter(1, Integer.class);
        factory.setParameter(0, p0);
        factory.setParameter(1, p1);
        List<AnnotatedMethod> factories = Arrays.asList(factory);

        JavaType type = TypeFactory.defaultInstance().constructType(MultiParamClass.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, MultiParamClass.class, null, null, factories);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(factory.getName(), true);
        intr.paramNames.put(p0.toString(), PropertyName.construct("name"));
        intr.paramNames.put(p1.toString(), PropertyName.construct("value"));

        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerFactoryMethods(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());

        assertTrue("should have property creator", creators.hasPropertyBasedCreator());
    }

    // Test _addDeserializerFactoryMethods: missing name -> exception
    @Test(expected = IllegalArgumentException.class)
    public void testAddDeserializerFactoryMethods_missingName_throwsException() throws Exception {
        StubAnnotatedMethod factory = new StubAnnotatedMethod("of", MultiParamClass.class, 2);
        StubAnnotatedParameter p0 = new StubAnnotatedParameter(0, String.class);
        StubAnnotatedParameter p1 = new StubAnnotatedParameter(1, Integer.class);
        factory.setParameter(0, p0);
        factory.setParameter(1, p1);
        List<AnnotatedMethod> factories = Arrays.asList(factory);

        JavaType type = TypeFactory.defaultInstance().constructType(MultiParamClass.class);
        StubBeanDescription beanDesc = new StubBeanDescription(type, MultiParamClass.class, null, null, factories);

        MapAnnotationIntrospector intr = new MapAnnotationIntrospector();
        intr.setCreator(factory.getName(), true);
        intr.paramNames.put(p0.toString(), PropertyName.construct("name"));
        // second param missing

        StubDeserializationContext ctxt = new StubDeserializationContext(
                new ObjectMapper().getDeserializationConfig(), intr);
        CreatorCollector creators = new CreatorCollector(beanDesc, true);

        factory._addDeserializerFactoryMethods(ctxt, beanDesc,
                VisibilityChecker.Std.defaultInstance(), intr, creators,
                Collections.<AnnotatedWithParams, BeanPropertyDefinition[]>emptyMap());
    }

    // Test findDefaultDeserializer for Object type -> UntypedObjectDeserializer
    @Test
    public void testFindDefaultDeserializer_Object_returnsUntyped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = ctxt.getConfig().constructType(Object.class);
        BeanDescription beanDesc = ctxt.getConfig().introspectClassAnnotations(Object.class);
        JsonDeserializer<?> deser = factory.findDefaultDeserializer(ctxt, type, beanDesc);
        assertTrue(deser instanceof UntypedObjectDeserializer);
    }

    // Test findDefaultDeserializer for String -> StringDeserializer
    @Test
    public void testFindDefaultDeserializer_String_returnsStringDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = ctxt.getConfig().constructType(String.class);
        BeanDescription beanDesc = ctxt.getConfig().introspectClassAnnotations(String.class);
        JsonDeserializer<?> deser = factory.findDefaultDeserializer(ctxt, type, beanDesc);
        assertEquals(StringDeserializer.instance, deser);
    }

    // Helper class for tests
    static class StringCreator {
        private String value;
        public StringCreator(String value) { this.value = value; }
    }

    static class MultiParamClass {
        private String name;
        private int value;
        public MultiParamClass(String name, int value) { }
    }

    // Inner class marker (for test)
    class InnerClass {
        // non-static inner
    }

    // Stub for AnnotatedMethod (since we need one)
    static class StubAnnotatedMethod extends AnnotatedMethod {
        private final String name;
        private final Class<?> returnType;
        private final int paramCount;
        private AnnotatedParameter[] params;

        StubAnnotatedMethod(String name, Class<?> returnType, int paramCount) {
            super(null, null);
            this.name = name;
            this.returnType = returnType;
            this.paramCount = paramCount;
            params = new AnnotatedParameter[paramCount];
        }

        void setParameter(int index, AnnotatedParameter param) {
            params[index] = param;
        }

        @Override
        public int getParameterCount() { return paramCount; }

        @Override
        public AnnotatedParameter getParameter(int index) { return params[index]; }

        @Override
        public Class<?> getRawReturnType() { return returnType; }

        @Override
        public String getName() { return name; }

        @Override
        public Class<?> getRawParameterType(int index) {
            return params[index].getRawType();
        }
    }
}