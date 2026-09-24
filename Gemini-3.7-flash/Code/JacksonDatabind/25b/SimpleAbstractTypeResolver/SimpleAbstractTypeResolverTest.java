package com.fasterxml.jackson.databind.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleAbstractTypeResolverTest {

    private SimpleAbstractTypeResolver resolver;
    private TypeFactory typeFactory;

    // Abstract class for testing non-interface abstract type mappings
    public static abstract class AbstractTestBase {
    }

    public static class ConcreteTestImpl extends AbstractTestBase {
        public int x;
    }

    private static class NonRelatedClass {
    }

    private static class ConcreteBaseClass {
    }

    private static class ConcreteSubClass extends ConcreteBaseClass {
    }

    @Before
    public void setUp() {
        resolver = new SimpleAbstractTypeResolver();
        typeFactory = TypeFactory.defaultInstance();
    }

    // Tests adding valid interface-to-implementation mapping
    @Test
    public void testAddMapping_interfaceToImplementation_addsMappingSuccessfully() {
        resolver.addMapping(List.class, ArrayList.class);
        JavaType inputType = typeFactory.constructType(List.class);

        JavaType result = resolver.findTypeMapping(null, inputType);

        assertNotNull(result);
        assertEquals(ArrayList.class, result.getRawClass());
    }

    // Tests adding valid abstract class to concrete subclass mapping
    @Test
    public void testAddMapping_abstractClassToSubclass_addsMappingSuccessfully() {
        resolver.addMapping(AbstractTestBase.class, ConcreteTestImpl.class);
        JavaType inputType = typeFactory.constructType(AbstractTestBase.class);

        JavaType result = resolver.findTypeMapping(null, inputType);

        assertNotNull(result);
        assertEquals(ConcreteTestImpl.class, result.getRawClass());
    }

    // Tests exception when superType is the same class as subType
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_sameClassForSuperAndSub_throwsException() {
        resolver.addMapping(List.class, List.class);
    }

    // Tests exception when subType does not extend/implement superType
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_unrelatedSubtype_throwsException() {
        Class<?> rawAbstract = AbstractTestBase.class;
        @SuppressWarnings("unchecked")
        Class<AbstractTestBase> superType = (Class<AbstractTestBase>) rawAbstract;
        @SuppressWarnings("unchecked")
        Class<? extends AbstractTestBase> subType = (Class<? extends AbstractTestBase>) (Class<?>) NonRelatedClass.class;

        resolver.addMapping(superType, subType);
    }

    // Tests exception when superType is a concrete (non-abstract) class
    @Test(expected = IllegalArgumentException.class)
    public void testAddMapping_concreteSuperType_throwsException() {
        resolver.addMapping(ConcreteBaseClass.class, ConcreteSubClass.class);
    }

    // Tests findTypeMapping when no mapping is configured
    @Test
    public void testFindTypeMapping_unmappedType_returnsNull() {
        JavaType inputType = typeFactory.constructType(Map.class);

        JavaType result = resolver.findTypeMapping(null, inputType);

        assertNull(result);
    }

    // Tests findTypeMapping retaining generic type parameterization
    @Test
    public void testFindTypeMapping_parameterizedCollection_preservesGenericParameters() {
        resolver.addMapping(Collection.class, LinkedList.class);
        JavaType inputType = typeFactory.constructCollectionType(Collection.class, String.class);

        JavaType result = resolver.findTypeMapping(null, inputType);

        assertNotNull(result);
        assertEquals(LinkedList.class, result.getRawClass());
        assertEquals(1, result.containedTypeCount());
        assertEquals(String.class, result.containedType(0).getRawClass());
    }

    // Tests findTypeMapping retaining key and value generic parameters for Map
    @Test
    public void testFindTypeMapping_parameterizedMap_preservesKeyAndValueParameters() {
        resolver.addMapping(Map.class, HashMap.class);
        JavaType inputType = typeFactory.constructMapType(Map.class, String.class, Integer.class);

        JavaType result = resolver.findTypeMapping(null, inputType);

        assertNotNull(result);
        assertEquals(HashMap.class, result.getRawClass());
        assertEquals(String.class, result.getKeyType().getRawClass());
        assertEquals(Integer.class, result.getContentType().getRawClass());
    }

    // Tests resolveAbstractType default behavior returning null
    @Test
    public void testResolveAbstractType_anyInput_returnsNull() {
        JavaType inputType = typeFactory.constructType(List.class);

        JavaType result = resolver.resolveAbstractType(null, inputType);

        assertNull(result);
    }

    // Tests method chaining when adding mappings
    @Test
    public void testAddMapping_methodChaining_returnsSameInstance() {
        SimpleAbstractTypeResolver result = resolver.addMapping(List.class, ArrayList.class);

        assertSame(resolver, result);
    }

    // Tests multiple distinct mappings resolution
    @Test
    public void testFindTypeMapping_multipleMappingsConfigured_resolvesCorrectly() {
        resolver.addMapping(List.class, LinkedList.class);
        resolver.addMapping(Map.class, HashMap.class);

        JavaType listType = typeFactory.constructType(List.class);
        JavaType mapType = typeFactory.constructType(Map.class);

        JavaType listResult = resolver.findTypeMapping(null, listType);
        JavaType mapResult = resolver.findTypeMapping(null, mapType);

        assertNotNull(listResult);
        assertEquals(LinkedList.class, listResult.getRawClass());
        assertNotNull(mapResult);
        assertEquals(HashMap.class, mapResult.getRawClass());
    }

    // Tests resolveAbstractType with BeanDescription returns null
    @Test
    public void testResolveAbstractType_withBeanDescription_returnsNull() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType inputType = config.constructType(AbstractTestBase.class);
        BeanDescription beanDesc = config.introspect(inputType);

        JavaType result = resolver.resolveAbstractType(config, beanDesc);

        assertNull(result);
    }

    // Tests findTypeMapping with DeserializationConfig provided
    @Test
    public void testFindTypeMapping_withDeserializationConfig_resolvesCorrectly() {
        resolver.addMapping(AbstractTestBase.class, ConcreteTestImpl.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType inputType = config.constructType(AbstractTestBase.class);

        JavaType result = resolver.findTypeMapping(config, inputType);

        assertNotNull(result);
        assertEquals(ConcreteTestImpl.class, result.getRawClass());
    }

    // Tests deserialization using SimpleAbstractTypeResolver registered on ObjectMapper
    @Test
    public void testDeserialization_withRegisteredResolver_instantiatesMappedSubtype() throws Exception {
        resolver.addMapping(AbstractTestBase.class, ConcreteTestImpl.class);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        AbstractTestBase result = mapper.readValue("{\"x\":42}", AbstractTestBase.class);

        assertNotNull(result);
        assertTrue(result instanceof ConcreteTestImpl);
        assertEquals(42, ((ConcreteTestImpl) result).x);
    }
}