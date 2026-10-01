package com.fasterxml.jackson.databind.deser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumMapDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumSetDeserializer;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapEntryDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.deser.std.PrimitiveArrayDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BasicDeserializerFactoryTest {

    private ObjectMapper _objectMapper;
    private DeserializationContext _context;
    private DeserializationConfig _config;
    private TypeFactory _typeFactory;
    private BasicDeserializerFactory _factory;

    private enum TestEnum { ALPHA, BETA }

    static class CustomValueBean {
        final String value;
        CustomValueBean(String value) { this.value = value; }
        @JsonValue
        public String getValue() { return value; }
    }

    static class CustomCreatorBean {
        final String name;
        final int age;
        @JsonCreator
        public CustomCreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return "custom:" + key;
        }
    }

    @Before
    public void setUp() {
        _objectMapper = new ObjectMapper();
        _config = _objectMapper.getDeserializationConfig();
        _context = _objectMapper.getDeserializationContext();
        _typeFactory = _config.getTypeFactory();
        _factory = BeanDeserializerFactory.instance;
    }

    // Tests fluent configuration chaining of factory
    @Test
    public void testWithConfigMethods_chaining_returnsNewConfiguredInstances() {
        DeserializerFactory factory = _factory;
        factory = factory.withAdditionalDeserializers(new Deserializers.Base());
        factory = factory.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        factory = factory.withDeserializerModifier(new BeanDeserializerModifier());
        factory = factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        factory = factory.withValueInstantiators(new SimpleValueInstantiators());

        DeserializerFactoryConfig config = ((BasicDeserializerFactory) factory).getFactoryConfig();
        assertTrue(config.hasDeserializers());
        assertTrue(config.hasKeyDeserializers());
        assertTrue(config.hasDeserializerModifiers());
        assertTrue(config.hasAbstractTypeResolvers());
        assertTrue(config.hasValueInstantiators());
    }

    // Tests mapping of abstract types with registered resolver
    @Test
    public void testMapAbstractType_withRegisteredResolver_returnsConcreteType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        BasicDeserializerFactory factory = (BasicDeserializerFactory) _factory.withAbstractTypeResolver(resolver);

        JavaType abstractType = _typeFactory.constructType(CharSequence.class);
        JavaType mappedType = factory.mapAbstractType(_config, abstractType);

        assertEquals(String.class, mappedType.getRawClass());
    }

    // Tests mapping abstract type when unresolved returns original type
    @Test
    public void testMapAbstractType_unregistered_returnsOriginalType() throws Exception {
        JavaType abstractType = _typeFactory.constructType(CharSequence.class);
        JavaType mappedType = _factory.mapAbstractType(_config, abstractType);

        assertSame(abstractType, mappedType);
    }

    // Tests exception on invalid abstract type mapping hierarchy
    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractType_invalidSubtypeResolution_throwsIllegalArgumentException() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, (Class) Map.class);
        BasicDeserializerFactory factory = (BasicDeserializerFactory) _factory.withAbstractTypeResolver(resolver);

        JavaType listType = _typeFactory.constructType(List.class);
        factory.mapAbstractType(_config, listType);
    }

    // Tests standard JDK value instantiators for empty collections and location
    @Test
    public void testFindValueInstantiator_standardSpecialTypes_returnsInstantiator() throws Exception {
        JavaType emptyListType = _typeFactory.constructType(Collections.EMPTY_LIST.getClass());
        BeanDescription listDesc = _config.introspect(emptyListType);
        ValueInstantiator listInst = _factory.findValueInstantiator(_context, listDesc);
        assertNotNull(listInst);
        assertTrue(listInst.canCreateUsingDefault());

        JavaType locationType = _typeFactory.constructType(JsonLocation.class);
        BeanDescription locDesc = _config.introspect(locationType);
        ValueInstantiator locInst = _factory.findValueInstantiator(_context, locDesc);
        assertNotNull(locInst);
    }

    // Tests default deserializer for Object, String, and Java standard types
    @Test
    public void testFindDefaultDeserializer_wellKnownTypes_returnsExpectedDeserializers() throws Exception {
        JavaType objType = _typeFactory.constructType(Object.class);
        BeanDescription objDesc = _config.introspect(objType);
        JsonDeserializer<?> objDeser = _factory.findDefaultDeserializer(_context, objType, objDesc);
        assertTrue(objDeser instanceof UntypedObjectDeserializer);

        JavaType strType = _typeFactory.constructType(String.class);
        BeanDescription strDesc = _config.introspect(strType);
        JsonDeserializer<?> strDeser = _factory.findDefaultDeserializer(_context, strType, strDesc);
        assertSame(StringDeserializer.instance, strDeser);

        JavaType intType = _typeFactory.constructType(int.class);
        BeanDescription intDesc = _config.introspect(intType);
        JsonDeserializer<?> intDeser = _factory.findDefaultDeserializer(_context, intType, intDesc);
        assertNotNull(intDeser);
    }

    // Tests default deserializer for Iterable upgrading to Collection
    @Test
    public void testFindDefaultDeserializer_iterableType_returnsCollectionDeserializer() throws Exception {
        JavaType iterType = _typeFactory.constructType(Iterable.class);
        BeanDescription iterDesc = _config.introspect(iterType);
        JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_context, iterType, iterDesc);
        assertNotNull(deser);
    }

    // Tests default deserializer for Map.Entry
    @Test
    public void testFindDefaultDeserializer_mapEntryType_returnsMapEntryDeserializer() throws Exception {
        JavaType entryType = _typeFactory.constructMapEntryType(Map.Entry.class, String.class, Integer.class);
        BeanDescription entryDesc = _config.introspect(entryType);
        JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_context, entryType, entryDesc);
        assertTrue(deser instanceof MapEntryDeserializer);
    }

    // Tests creation of primitive array deserializers
    @Test
    public void testCreateArrayDeserializer_primitiveAndStringArrays_returnsSpecificDeserializers() throws Exception {
        ArrayType byteArrType = _typeFactory.constructArrayType(byte.class);
        BeanDescription byteDesc = _config.introspect(byteArrType);
        JsonDeserializer<?> byteDeser = _factory.createArrayDeserializer(_context, byteArrType, byteDesc);
        assertTrue(byteDeser instanceof PrimitiveArrayDeserializers);

        ArrayType strArrType = _typeFactory.constructArrayType(String.class);
        BeanDescription strDesc = _config.introspect(strArrType);
        JsonDeserializer<?> strDeser = _factory.createArrayDeserializer(_context, strArrType, strDesc);
        assertSame(StringArrayDeserializer.instance, strDeser);
    }

    // Tests collection deserializer with interface fallback mapping
    @Test
    public void testCreateCollectionDeserializer_interfaceTypeFallback_createsArrayListDeserializer() throws Exception {
        CollectionType listType = _typeFactory.constructCollectionType(List.class, String.class);
        BeanDescription desc = _config.introspect(listType);
        JsonDeserializer<?> deser = _factory.createCollectionDeserializer(_context, listType, desc);
        assertTrue(deser instanceof StringCollectionDeserializer);
    }

    // Tests collection deserializer for EnumSet
    @Test
    public void testCreateCollectionDeserializer_enumSet_createsEnumSetDeserializer() throws Exception {
        CollectionType setType = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription desc = _config.introspect(setType);
        JsonDeserializer<?> deser = _factory.createCollectionDeserializer(_context, setType, desc);
        assertNotNull(deser);
    }

    // Tests map deserializer with interface fallback mapping
    @Test
    public void testCreateMapDeserializer_interfaceFallback_createsMapDeserializer() throws Exception {
        MapType mapType = _typeFactory.constructMapType(Map.class, String.class, Object.class);
        BeanDescription desc = _config.introspect(mapType);
        JsonDeserializer<?> deser = _factory.createMapDeserializer(_context, mapType, desc);
        assertNotNull(deser);

        MapType concurrentType = _typeFactory.constructMapType(ConcurrentMap.class, String.class, String.class);
        BeanDescription concurrentDesc = _config.introspect(concurrentType);
        JsonDeserializer<?> concurrentDeser = _factory.createMapDeserializer(_context, concurrentType, concurrentDesc);
        assertNotNull(concurrentDeser);
    }

    // Tests map deserializer for EnumMap with invalid non-enum key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateMapDeserializer_enumMapWithNonEnumKey_throwsIllegalArgumentException() throws Exception {
        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription desc = _config.introspect(enumMapType);
        _factory.createMapDeserializer(_context, enumMapType, desc);
    }

    // Tests creation of enum deserializer
    @Test
    public void testCreateEnumDeserializer_standardEnum_returnsEnumDeserializer() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        BeanDescription desc = _config.introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_context, enumType, desc);
        assertTrue(deser instanceof EnumDeserializer);
    }

    // Tests creation of tree node deserializer
    @Test
    public void testCreateTreeDeserializer_jsonNodeTypes_returnsTreeDeserializer() throws Exception {
        JavaType nodeType = _typeFactory.constructType(ObjectNode.class);
        BeanDescription desc = _config.introspect(nodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(_config, nodeType, desc);
        assertNotNull(deser);
    }

    // Tests creation of reference type deserializer for AtomicReference
    @Test
    public void testCreateReferenceDeserializer_atomicReference_returnsAtomicReferenceDeserializer() throws Exception {
        ReferenceType refType = _typeFactory.constructReferenceType(AtomicReference.class, _typeFactory.constructType(String.class));
        BeanDescription desc = _config.introspect(refType);
        JsonDeserializer<?> deser = _factory.createReferenceDeserializer(_context, refType, desc);
        assertTrue(deser instanceof AtomicReferenceDeserializer);
    }

    // Tests key deserializers for standard String, int, and Enum types
    @Test
    public void testCreateKeyDeserializer_stdAndEnumTypes_returnsKeyDeserializer() throws Exception {
        JavaType strType = _typeFactory.constructType(String.class);
        KeyDeserializer strKeyDeser = _factory.createKeyDeserializer(_context, strType);
        assertNull(strKeyDeser);

        JavaType intType = _typeFactory.constructType(Integer.class);
        KeyDeserializer intKeyDeser = _factory.createKeyDeserializer(_context, intType);
        assertNotNull(intKeyDeser);

        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer enumKeyDeser = _factory.createKeyDeserializer(_context, enumType);
        assertNotNull(enumKeyDeser);
    }

    // --- New tests covering remaining paths ---

    @Test
    public void testCreateMapDeserializer_validEnumMap_createsEnumMapDeserializer() throws Exception {
        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription desc = _config.introspect(enumMapType);
        JsonDeserializer<?> deser = _factory.createMapDeserializer(_context, enumMapType, desc);
        assertTrue(deser instanceof EnumMapDeserializer);
    }

    @Test
    public void testCreateCollectionDeserializer_variousStandardCollections() throws Exception {
        // Set fallback
        CollectionType setType = _typeFactory.constructCollectionType(Set.class, Integer.class);
        BeanDescription setDesc = _config.introspect(setType);
        JsonDeserializer<?> setDeser = _factory.createCollectionDeserializer(_context, setType, setDesc);
        assertNotNull(setDeser);

        // SortedSet fallback
        CollectionType sortedSetType = _typeFactory.constructCollectionType(SortedSet.class, String.class);
        BeanDescription sortedSetDesc = _config.introspect(sortedSetType);
        JsonDeserializer<?> sortedSetDeser = _factory.createCollectionDeserializer(_context, sortedSetType, sortedSetDesc);
        assertNotNull(sortedSetDeser);

        // Queue fallback
        CollectionType queueType = _typeFactory.constructCollectionType(Queue.class, String.class);
        BeanDescription queueDesc = _config.introspect(queueType);
        JsonDeserializer<?> queueDeser = _factory.createCollectionDeserializer(_context, queueType, queueDesc);
        assertNotNull(queueDeser);

        // LinkedList concrete
        CollectionType linkedListType = _typeFactory.constructCollectionType(LinkedList.class, Double.class);
        BeanDescription linkedListDesc = _config.introspect(linkedListType);
        JsonDeserializer<?> linkedListDeser = _factory.createCollectionDeserializer(_context, linkedListType, linkedListDesc);
        assertNotNull(linkedListDeser);

        // TreeSet concrete
        CollectionType treeSetType = _typeFactory.constructCollectionType(TreeSet.class, String.class);
        BeanDescription treeSetDesc = _config.introspect(treeSetType);
        JsonDeserializer<?> treeSetDeser = _factory.createCollectionDeserializer(_context, treeSetType, treeSetDesc);
        assertNotNull(treeSetDeser);
    }

    @Test
    public void testCreateMapDeserializer_sortedAndNavigableMapFallbacks() throws Exception {
        // SortedMap fallback
        MapType sortedMapType = _typeFactory.constructMapType(SortedMap.class, String.class, String.class);
        BeanDescription sortedMapDesc = _config.introspect(sortedMapType);
        JsonDeserializer<?> sortedMapDeser = _factory.createMapDeserializer(_context, sortedMapType, sortedMapDesc);
        assertNotNull(sortedMapDeser);

        // NavigableMap fallback
        MapType navMapType = _typeFactory.constructMapType(NavigableMap.class, String.class, String.class);
        BeanDescription navMapDesc = _config.introspect(navMapType);
        JsonDeserializer<?> navMapDeser = _factory.createMapDeserializer(_context, navMapType, navMapDesc);
        assertNotNull(navMapDeser);

        // ConcurrentNavigableMap fallback
        MapType cnavMapType = _typeFactory.constructMapType(ConcurrentNavigableMap.class, String.class, String.class);
        BeanDescription cnavMapDesc = _config.introspect(cnavMapType);
        JsonDeserializer<?> cnavMapDeser = _factory.createMapDeserializer(_context, cnavMapType, cnavMapDesc);
        assertNotNull(cnavMapDeser);

        // TreeMap concrete
        MapType treeMapType = _typeFactory.constructMapType(TreeMap.class, String.class, String.class);
        BeanDescription treeMapDesc = _config.introspect(treeMapType);
        JsonDeserializer<?> treeMapDeser = _factory.createMapDeserializer(_context, treeMapType, treeMapDesc);
        assertNotNull(treeMapDeser);
    }

    @Test
    public void testFindDefaultDeserializer_datesNumbersAndTokenBuffer() throws Exception {
        // Date
        JavaType dateType = _typeFactory.constructType(Date.class);
        BeanDescription dateDesc = _config.introspect(dateType);
        JsonDeserializer<?> dateDeser = _factory.findDefaultDeserializer(_context, dateType, dateDesc);
        assertNotNull(dateDeser);

        // Calendar
        JavaType calType = _typeFactory.constructType(Calendar.class);
        BeanDescription calDesc = _config.introspect(calType);
        JsonDeserializer<?> calDeser = _factory.findDefaultDeserializer(_context, calType, calDesc);
        assertNotNull(calDeser);

        // TokenBuffer
        JavaType tbType = _typeFactory.constructType(TokenBuffer.class);
        BeanDescription tbDesc = _config.introspect(tbType);
        JsonDeserializer<?> tbDeser = _factory.findDefaultDeserializer(_context, tbType, tbDesc);
        assertNotNull(tbDeser);

        // Number
        JavaType numType = _typeFactory.constructType(Number.class);
        BeanDescription numDesc = _config.introspect(numType);
        JsonDeserializer<?> numDeser = _factory.findDefaultDeserializer(_context, numType, numDesc);
        assertNotNull(numDeser);

        // Boolean
        JavaType boolType = _typeFactory.constructType(Boolean.class);
        BeanDescription boolDesc = _config.introspect(boolType);
        JsonDeserializer<?> boolDeser = _factory.findDefaultDeserializer(_context, boolType, boolDesc);
        assertNotNull(boolDeser);
    }

    @Test
    public void testCustomDeserializers_interceptFactoryCreation() throws Exception {
        final JsonDeserializer<?> mockDeser = new StringDeserializer();
        SimpleDeserializers customDesers = new SimpleDeserializers();
        customDesers.addDeserializer(CustomCreatorBean.class, (JsonDeserializer) mockDeser);

        BasicDeserializerFactory customFactory = (BasicDeserializerFactory) _factory.withAdditionalDeserializers(customDesers);

        JavaType customType = _typeFactory.constructType(CustomCreatorBean.class);
        BeanDescription customDesc = _config.introspect(customType);
        JsonDeserializer<?> deser = customFactory.createBeanDeserializer(_context, customType, customDesc);

        assertSame(mockDeser, deser);
    }

    @Test
    public void testCustomKeyDeserializers_interceptKeyCreation() throws Exception {
        SimpleKeyDeserializers customKeyDesers = new SimpleKeyDeserializers();
        final KeyDeserializer customKeyDeser = new CustomKeyDeserializer();
        customKeyDesers.addDeserializer(CustomCreatorBean.class, customKeyDeser);

        BasicDeserializerFactory customFactory = (BasicDeserializerFactory) _factory.withAdditionalKeyDeserializers(customKeyDesers);

        JavaType type = _typeFactory.constructType(CustomCreatorBean.class);
        KeyDeserializer resultKeyDeser = customFactory.createKeyDeserializer(_context, type);

        assertSame(customKeyDeser, resultKeyDeser);
    }

    @Test
    public void testCustomValueInstantiators_interceptFindValueInstantiator() throws Exception {
        SimpleValueInstantiators vi = new SimpleValueInstantiators();
        final ValueInstantiator customVI = new StdValueInstantiator(_config, CustomCreatorBean.class);
        vi.addValueInstantiator(CustomCreatorBean.class, customVI);

        BasicDeserializerFactory customFactory = (BasicDeserializerFactory) _factory.withValueInstantiators(vi);

        JavaType type = _typeFactory.constructType(CustomCreatorBean.class);
        BeanDescription desc = _config.introspect(type);
        ValueInstantiator resultVI = customFactory.findValueInstantiator(_context, desc);

        assertSame(customVI, resultVI);
    }

    @Test
    public void testCollectionLikeAndMapLikeDeserializerCreation() throws Exception {
        CollectionLikeType colLikeType = _typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription colDesc = _config.introspectClassAnnotations(colLikeType);
        JsonDeserializer<?> colLikeDeser = _factory.createCollectionLikeDeserializer(_context, colLikeType, colDesc);
        assertNotNull(colLikeDeser);

        MapLikeType mapLikeType = _typeFactory.constructMapLikeType(HashMap.class, String.class, Integer.class);
        BeanDescription mapDesc = _config.introspectClassAnnotations(mapLikeType);
        JsonDeserializer<?> mapLikeDeser = _factory.createMapLikeDeserializer(_context, mapLikeType, mapDesc);
        assertNotNull(mapLikeDeser);
    }

    @Test
    public void testDeserializerModifier_modifiesArrayCollectionMapAndEnum() throws Exception {
        final boolean[] called = new boolean[4];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                called[0] = true;
                return deserializer;
            }
            @Override
            public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                called[1] = true;
                return deserializer;
            }
            @Override
            public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                called[2] = true;
                return deserializer;
            }
            @Override
            public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                called[3] = true;
                return deserializer;
            }
        };

        BasicDeserializerFactory factory = (BasicDeserializerFactory) _factory.withDeserializerModifier(modifier);

        ArrayType arrType = _typeFactory.constructArrayType(String.class);
        factory.createArrayDeserializer(_context, arrType, _config.introspect(arrType));
        assertTrue(called[0]);

        CollectionType colType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        factory.createCollectionDeserializer(_context, colType, _config.introspect(colType));
        assertTrue(called[1]);

        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, String.class);
        factory.createMapDeserializer(_context, mapType, _config.introspect(mapType));
        assertTrue(called[2]);

        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        factory.createEnumDeserializer(_context, enumType, _config.introspect(enumType));
        assertTrue(called[3]);
    }

    @Test
    public void testFindValueInstantiator_withCreatorProperties() throws Exception {
        JavaType type = _typeFactory.constructType(CustomCreatorBean.class);
        BeanDescription desc = _config.introspect(type);
        ValueInstantiator instantiator = _factory.findValueInstantiator(_context, desc);
        assertNotNull(instantiator);
        assertTrue(instantiator.canCreateFromObjectWith());
        assertEquals(2, instantiator.getFromObjectArguments(_config).length);
    }
}