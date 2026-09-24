package com.fasterxml.jackson.databind.deser;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BasicDeserializerFactoryTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private BasicDeserializerFactory _factory;
    private TypeFactory _typeFactory;

    enum TestEnum {
        A, B, C;
    }

    interface CustomInterface {
    }

    static class CustomImpl implements CustomInterface {
    }

    static abstract class AbstractCustomList<E> extends AbstractList<E> {
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = BeanDeserializerFactory.instance;
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests fluent with-methods for factory configuration
    @Test
    public void testWithConfig_modifications_returnsNewFactoryInstance() {
        DeserializerFactory f = _factory;
        
        f = f.withAdditionalDeserializers(new SimpleDeserializers());
        assertNotNull(f);
        assertTrue(f.getFactoryConfig().hasDeserializers());

        f = f.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        assertNotNull(f);
        assertTrue(f.getFactoryConfig().hasKeyDeserializers());

        f = f.withDeserializerModifier(new BeanDeserializerModifier());
        assertNotNull(f);
        assertTrue(f.getFactoryConfig().hasDeserializerModifiers());

        f = f.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotNull(f);
        assertTrue(f.getFactoryConfig().hasAbstractTypeResolvers());

        f = f.withValueInstantiators(new SimpleValueInstantiators());
        assertNotNull(f);
        assertTrue(f.getFactoryConfig().hasValueInstantiators());
    }

    // Tests mapping of abstract types via registered resolver
    @Test
    public void testMapAbstractType_registeredResolver_resolvesSubtype() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CustomInterface.class, CustomImpl.class);

        DeserializerFactory factory = _factory.withAbstractTypeResolver(resolver);
        DeserializationConfig config = _mapper.getDeserializationConfig();

        JavaType abstractType = _typeFactory.constructType(CustomInterface.class);
        JavaType concreteType = factory.mapAbstractType(config, abstractType);

        assertEquals(CustomImpl.class, concreteType.getRawClass());
    }

    // Tests mapping of abstract types without resolver returns input type
    @Test
    public void testMapAbstractType_unregistered_returnsOriginalType() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType abstractType = _typeFactory.constructType(CustomInterface.class);
        JavaType resultType = _factory.mapAbstractType(config, abstractType);

        assertEquals(abstractType, resultType);
    }

    // Tests invalid abstract type mapping hierarchy throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractType_incompatibleType_throwsException() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        // String is not a subtype of CustomInterface
        resolver.addMapping(CustomInterface.class, (Class) String.class);

        DeserializerFactory factory = _factory.withAbstractTypeResolver(resolver);
        DeserializationConfig config = _mapper.getDeserializationConfig();

        JavaType abstractType = _typeFactory.constructType(CustomInterface.class);
        factory.mapAbstractType(config, abstractType);
    }

    // Tests default deserializer for Object.class and String.class
    @Test
    public void testFindDefaultDeserializer_basicTypes_returnsExpectedDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        
        JavaType objType = _typeFactory.constructType(Object.class);
        BeanDescription objDesc = config.introspectClassAnnotations(objType);
        JsonDeserializer<?> objDeser = _factory.findDefaultDeserializer(_context, objType, objDesc);
        assertTrue(objDeser instanceof UntypedObjectDeserializer);

        JavaType strType = _typeFactory.constructType(String.class);
        BeanDescription strDesc = config.introspectClassAnnotations(strType);
        JsonDeserializer<?> strDeser = _factory.findDefaultDeserializer(_context, strType, strDesc);
        assertSame(StringDeserializer.instance, strDeser);

        JavaType charSeqType = _typeFactory.constructType(CharSequence.class);
        BeanDescription charSeqDesc = config.introspectClassAnnotations(charSeqType);
        JsonDeserializer<?> charSeqDeser = _factory.findDefaultDeserializer(_context, charSeqType, charSeqDesc);
        assertSame(StringDeserializer.instance, charSeqDeser);
    }

    // Tests default deserializer for AtomicReference and TokenBuffer
    @Test
    public void testFindDefaultDeserializer_specialJdkTypes_returnsDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        JavaType refType = _typeFactory.constructType(AtomicReference.class);
        BeanDescription refDesc = config.introspectClassAnnotations(refType);
        JsonDeserializer<?> refDeser = _factory.findDefaultDeserializer(_context, refType, refDesc);
        assertNotNull(refDeser);

        JavaType tbType = _typeFactory.constructType(com.fasterxml.jackson.databind.util.TokenBuffer.class);
        BeanDescription tbDesc = config.introspectClassAnnotations(tbType);
        JsonDeserializer<?> tbDeser = _factory.findDefaultDeserializer(_context, tbType, tbDesc);
        assertNotNull(tbDeser);
    }

    // Tests default deserializer for Iterable upgrading to Collection
    @Test
    public void testFindDefaultDeserializer_iterable_returnsCollectionDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType iterType = _typeFactory.constructType(Iterable.class);
        BeanDescription desc = config.introspectClassAnnotations(iterType);
        JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_context, iterType, desc);
        assertNotNull(deser);
    }

    // Tests default deserializer for Map.Entry
    @Test
    public void testFindDefaultDeserializer_mapEntry_returnsMapEntryDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType entryType = _typeFactory.constructMapLikeType(Map.Entry.class, String.class, Object.class);
        BeanDescription desc = config.introspectClassAnnotations(entryType);
        JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_context, entryType, desc);
        assertNotNull(deser);
    }

    // Tests array deserializer creation for primitive and object arrays
    @Test
    public void testCreateArrayDeserializer_types_returnsArrayDesers() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        ArrayType intArrayType = _typeFactory.constructArrayType(int.class);
        BeanDescription intDesc = config.introspectClassAnnotations(intArrayType);
        JsonDeserializer<?> intDeser = _factory.createArrayDeserializer(_context, intArrayType, intDesc);
        assertNotNull(intDeser);

        ArrayType strArrayType = _typeFactory.constructArrayType(String.class);
        BeanDescription strDesc = config.introspectClassAnnotations(strArrayType);
        JsonDeserializer<?> strDeser = _factory.createArrayDeserializer(_context, strArrayType, strDesc);
        assertNotNull(strDeser);

        ArrayType objArrayType = _typeFactory.constructArrayType(Object.class);
        BeanDescription objDesc = config.introspectClassAnnotations(objArrayType);
        JsonDeserializer<?> objDeser = _factory.createArrayDeserializer(_context, objArrayType, objDesc);
        assertNotNull(objDeser);
    }

    // Tests collection fallback mappings (List -> ArrayList, Set -> HashSet, Queue -> LinkedList)
    @Test
    public void testCreateCollectionDeserializer_interfaceFallbacks_createsDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        CollectionType listType = _typeFactory.constructCollectionType(List.class, String.class);
        BeanDescription listDesc = config.introspectClassAnnotations(listType);
        JsonDeserializer<?> listDeser = _factory.createCollectionDeserializer(_context, listType, listDesc);
        assertNotNull(listDeser);

        CollectionType setType = _typeFactory.constructCollectionType(Set.class, Integer.class);
        BeanDescription setDesc = config.introspectClassAnnotations(setType);
        JsonDeserializer<?> setDeser = _factory.createCollectionDeserializer(_context, setType, setDesc);
        assertTrue(setDeser instanceof CollectionDeserializer);

        CollectionType queueType = _typeFactory.constructCollectionType(Queue.class, String.class);
        BeanDescription queueDesc = config.introspectClassAnnotations(queueType);
        JsonDeserializer<?> queueDeser = _factory.createCollectionDeserializer(_context, queueType, queueDesc);
        assertNotNull(queueDeser);
    }

    // Tests EnumSet collection deserializer creation
    @Test
    public void testCreateCollectionDeserializer_enumSet_createsEnumSetDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        CollectionType enumSetType = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription desc = config.introspectClassAnnotations(enumSetType);
        JsonDeserializer<?> deser = _factory.createCollectionDeserializer(_context, enumSetType, desc);
        assertNotNull(deser);
    }

    // Tests non-concrete collection type without polymorphic type handler throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateCollectionDeserializer_abstractCollectionWithoutFallback_throwsException() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        CollectionType customListType = _typeFactory.constructCollectionType(AbstractCustomList.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(customListType);
        _factory.createCollectionDeserializer(_context, customListType, desc);
    }

    // Tests map fallback mappings (Map -> LinkedHashMap, ConcurrentMap -> ConcurrentHashMap, SortedMap -> TreeMap)
    @Test
    public void testCreateMapDeserializer_interfaceFallbacks_createsMapDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        MapType mapType = _typeFactory.constructMapType(Map.class, String.class, String.class);
        BeanDescription mapDesc = config.introspectClassAnnotations(mapType);
        JsonDeserializer<?> mapDeser = _factory.createMapDeserializer(_context, mapType, mapDesc);
        assertTrue(mapDeser instanceof MapDeserializer);

        MapType concurrentMapType = _typeFactory.constructMapType(ConcurrentMap.class, String.class, Object.class);
        BeanDescription concDesc = config.introspectClassAnnotations(concurrentMapType);
        JsonDeserializer<?> concDeser = _factory.createMapDeserializer(_context, concurrentMapType, concDesc);
        assertNotNull(concDeser);

        MapType sortedMapType = _typeFactory.constructMapType(SortedMap.class, String.class, Object.class);
        BeanDescription sortDesc = config.introspectClassAnnotations(sortedMapType);
        JsonDeserializer<?> sortDeser = _factory.createMapDeserializer(_context, sortedMapType, sortDesc);
        assertNotNull(sortDeser);
    }

    // Tests EnumMap with non-enum key throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateMapDeserializer_enumMapWithNonEnumKey_throwsException() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        MapType invalidEnumMapType = _typeFactory.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(invalidEnumMapType);
        _factory.createMapDeserializer(_context, invalidEnumMapType, desc);
    }

    // Tests EnumMap with valid enum key creates deserializer
    @Test
    public void testCreateMapDeserializer_enumMapWithEnumKey_success() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(enumMapType);
        JsonDeserializer<?> deser = _factory.createMapDeserializer(_context, enumMapType, desc);
        assertNotNull(deser);
    }

    // Tests createEnumDeserializer creates standard enum deserializer
    @Test
    public void testCreateEnumDeserializer_standardEnum_createsDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        BeanDescription desc = config.introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_context, enumType, desc);
        assertNotNull(deser);
    }

    // Tests createTreeDeserializer for JsonNode types
    @Test
    public void testCreateTreeDeserializer_jsonNode_returnsNodeDeser() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType nodeType = _typeFactory.constructType(JsonNode.class);
        BeanDescription desc = config.introspectClassAnnotations(nodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(config, nodeType, desc);
        assertNotNull(deser);
    }

    // Tests createKeyDeserializer for enum types and string-based types
    @Test
    public void testCreateKeyDeserializer_enumAndString_returnsKeyDeser() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer enumKeyDeser = _factory.createKeyDeserializer(_context, enumType);
        assertNotNull(enumKeyDeser);

        JavaType stringType = _typeFactory.constructType(String.class);
        KeyDeserializer strKeyDeser = _factory.createKeyDeserializer(_context, stringType);
        assertNotNull(strKeyDeser);
    }

    // Tests findValueInstantiator for standard type JsonLocation
    @Test
    public void testFindValueInstantiator_jsonLocation_returnsInstantiator() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType type = _typeFactory.constructType(JsonLocation.class);
        BeanDescription desc = config.introspect(type);
        ValueInstantiator instantiator = _factory.findValueInstantiator(_context, desc);
        assertNotNull(instantiator);
        assertTrue(instantiator.canCreateFromObjectWith());
    }
}