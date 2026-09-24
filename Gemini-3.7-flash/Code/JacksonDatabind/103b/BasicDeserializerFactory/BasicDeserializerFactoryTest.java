package com.fasterxml.jackson.databind.deser;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.ArrayBlockingQueueDeserializer;
import com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumMapDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapEntryDeserializer;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.PrimitiveArrayDeserializers;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.deser.std.TokenBufferDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BasicDeserializerFactoryTest {

    private ObjectMapper _mapper;
    private DeserializationContext _ctxt;
    private DeserializationConfig _config;
    private BasicDeserializerFactory _factory;
    private TypeFactory _typeFactory;

    enum TestEnum {
        A, B, C;
    }

    static class CustomList<E> extends ArrayList<E> {
        private static final long serialVersionUID = 1L;
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _ctxt = _mapper.getDeserializationContext();
        _config = _mapper.getDeserializationConfig();
        _factory = BeanDeserializerFactory.instance;
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests fluent config modification methods
    @Test
    public void testWithConfig_modifications_returnsNewConfiguredInstances() {
        DeserializerFactoryConfig config = _factory.getFactoryConfig();
        assertNotNull(config);

        Deserializers extraDesers = new Deserializers.Base();
        DeserializerFactory f1 = _factory.withAdditionalDeserializers(extraDesers);
        assertNotSame(_factory, f1);

        KeyDeserializers extraKeyDesers = new SimpleKeyDeserializers();
        DeserializerFactory f2 = _factory.withAdditionalKeyDeserializers(extraKeyDesers);
        assertNotSame(_factory, f2);

        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {};
        DeserializerFactory f3 = _factory.withDeserializerModifier(modifier);
        assertNotSame(_factory, f3);

        AbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        DeserializerFactory f4 = _factory.withAbstractTypeResolver(resolver);
        assertNotSame(_factory, f4);

        ValueInstantiators vi = new SimpleValueInstantiators();
        DeserializerFactory f5 = _factory.withValueInstantiators(vi);
        assertNotSame(_factory, f5);
    }

    // Tests abstract type mapping with resolver
    @Test
    public void testMapAbstractType_registeredResolver_mapsToTargetSubtype() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        DeserializerFactory factory = _factory.withAbstractTypeResolver(resolver);

        JavaType abstractType = _typeFactory.constructType(CharSequence.class);
        JavaType mappedType = factory.mapAbstractType(_config, abstractType);
        assertEquals(String.class, mappedType.getRawClass());
    }

    // Tests abstract type mapping cycle or invalid subtype exception
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractType_invalidMapping_throwsException() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, (Class) Set.class);
        DeserializerFactory factory = _factory.withAbstractTypeResolver(resolver);

        JavaType listType = _typeFactory.constructType(List.class);
        factory.mapAbstractType(_config, listType);
    }

    // Tests standard ValueInstantiator resolution for empty JDK collections and JsonLocation
    @Test
    public void testFindValueInstantiator_standardTypes_returnsExpectedInstantiators() throws Exception {
        BeanDescription locDesc = _config.introspectClassAnnotations(JsonLocation.class);
        ValueInstantiator locInst = _factory.findValueInstantiator(_ctxt, locDesc);
        assertNotNull(locInst);
        assertTrue(locInst.canCreateFromObjectWith());

        BeanDescription emptyListDesc = _config.introspectClassAnnotations(Collections.EMPTY_LIST.getClass());
        ValueInstantiator emptyListInst = _factory.findValueInstantiator(_ctxt, emptyListDesc);
        assertNotNull(emptyListInst);
        assertTrue(emptyListInst.canCreateUsingDefault());

        BeanDescription emptySetDesc = _config.introspectClassAnnotations(Collections.EMPTY_SET.getClass());
        ValueInstantiator emptySetInst = _factory.findValueInstantiator(_ctxt, emptySetDesc);
        assertNotNull(emptySetInst);

        BeanDescription emptyMapDesc = _config.introspectClassAnnotations(Collections.EMPTY_MAP.getClass());
        ValueInstantiator emptyMapInst = _factory.findValueInstantiator(_ctxt, emptyMapDesc);
        assertNotNull(emptyMapInst);
    }

    // Tests primitive, string, and object array deserializer creation
    @Test
    public void testCreateArrayDeserializer_variousTypes_createsCorrectDeserializers() throws Exception {
        JavaType intArrayType = _typeFactory.constructType(int[].class);
        BeanDescription desc1 = _config.introspect(intArrayType);
        JsonDeserializer<?> deser1 = _factory.createArrayDeserializer(_ctxt, (ArrayType) intArrayType, desc1);
        assertTrue(deser1 instanceof PrimitiveArrayDeserializers);

        JavaType strArrayType = _typeFactory.constructType(String[].class);
        BeanDescription desc2 = _config.introspect(strArrayType);
        JsonDeserializer<?> deser2 = _factory.createArrayDeserializer(_ctxt, (ArrayType) strArrayType, desc2);
        assertTrue(deser2 instanceof StringArrayDeserializer);

        JavaType objArrayType = _typeFactory.constructType(Object[].class);
        BeanDescription desc3 = _config.introspect(objArrayType);
        JsonDeserializer<?> deser3 = _factory.createArrayDeserializer(_ctxt, (ArrayType) objArrayType, desc3);
        assertTrue(deser3 instanceof ObjectArrayDeserializer);
    }

    // Tests collection deserializer creation including EnumSet, String collection, and standard fallback
    @Test
    public void testCreateCollectionDeserializer_standardTypes_createsCorrectDeserializers() throws Exception {
        CollectionType enumSetType = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription desc1 = _config.introspect(enumSetType);
        JsonDeserializer<?> deser1 = _factory.createCollectionDeserializer(_ctxt, enumSetType, desc1);
        assertNotNull(deser1);

        CollectionType strListType = _typeFactory.constructCollectionType(List.class, String.class);
        BeanDescription desc2 = _config.introspect(strListType);
        JsonDeserializer<?> deser2 = _factory.createCollectionDeserializer(_ctxt, strListType, desc2);
        assertTrue(deser2 instanceof StringCollectionDeserializer);

        CollectionType objListType = _typeFactory.constructCollectionType(List.class, Object.class);
        BeanDescription desc3 = _config.introspect(objListType);
        JsonDeserializer<?> deser3 = _factory.createCollectionDeserializer(_ctxt, objListType, desc3);
        assertTrue(deser3 instanceof CollectionDeserializer);

        CollectionType queueType = _typeFactory.constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        BeanDescription desc4 = _config.introspect(queueType);
        JsonDeserializer<?> deser4 = _factory.createCollectionDeserializer(_ctxt, queueType, desc4);
        assertTrue(deser4 instanceof ArrayBlockingQueueDeserializer);
    }

    // Tests mapping of abstract collection types to concrete collection fallbacks
    @Test
    public void testMapAbstractCollectionType_interfaceTypes_returnsConcreteFallback() {
        CollectionType listType = _typeFactory.constructCollectionType(List.class, String.class);
        CollectionType fallbackList = _factory._mapAbstractCollectionType(listType, _config);
        assertNotNull(fallbackList);
        assertEquals(ArrayList.class, fallbackList.getRawClass());

        CollectionType setType = _typeFactory.constructCollectionType(Set.class, String.class);
        CollectionType fallbackSet = _factory._mapAbstractCollectionType(setType, _config);
        assertNotNull(fallbackSet);
        assertEquals(HashSet.class, fallbackSet.getRawClass());

        CollectionType customType = _typeFactory.constructCollectionType(CustomList.class, String.class);
        CollectionType fallbackCustom = _factory._mapAbstractCollectionType(customType, _config);
        assertNull(fallbackCustom);
    }

    // Tests map deserializer creation for EnumMap and standard abstract/concrete maps
    @Test
    public void testCreateMapDeserializer_validTypes_createsCorrectDeserializers() throws Exception {
        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription desc1 = _config.introspect(enumMapType);
        JsonDeserializer<?> deser1 = _factory.createMapDeserializer(_ctxt, enumMapType, desc1);
        assertTrue(deser1 instanceof EnumMapDeserializer);

        MapType mapType = _typeFactory.constructMapType(Map.class, String.class, Object.class);
        BeanDescription desc2 = _config.introspect(mapType);
        JsonDeserializer<?> deser2 = _factory.createMapDeserializer(_ctxt, mapType, desc2);
        assertTrue(deser2 instanceof MapDeserializer);

        MapType concurrentMapType = _typeFactory.constructMapType(ConcurrentMap.class, String.class, String.class);
        BeanDescription desc3 = _config.introspect(concurrentMapType);
        JsonDeserializer<?> deser3 = _factory.createMapDeserializer(_ctxt, concurrentMapType, desc3);
        assertTrue(deser3 instanceof MapDeserializer);
    }

    // Tests EnumMap creation failure when key type is not an Enum
    @Test(expected = IllegalArgumentException.class)
    public void testCreateMapDeserializer_enumMapWithNonEnumKey_throwsException() throws Exception {
        MapType badEnumMapType = _typeFactory.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription desc = _config.introspect(badEnumMapType);
        _factory.createMapDeserializer(_ctxt, badEnumMapType, desc);
    }

    // Tests Enum deserializer creation for standard enum
    @Test
    public void testCreateEnumDeserializer_standardEnum_createsEnumDeserializer() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        BeanDescription desc = _config.introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_ctxt, enumType, desc);
        assertTrue(deser instanceof EnumDeserializer);
    }

    // Tests Tree deserializer creation for JsonNode and subclasses
    @Test
    public void testCreateTreeDeserializer_jsonNodeClass_returnsValidDeserializer() throws Exception {
        JavaType nodeType = _typeFactory.constructType(JsonNode.class);
        BeanDescription desc = _config.introspectClassAnnotations(nodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(_config, nodeType, desc);
        assertNotNull(deser);
    }

    // Tests Reference deserializer creation for AtomicReference
    @Test
    public void testCreateReferenceDeserializer_atomicReference_returnsAtomicReferenceDeserializer() throws Exception {
        ReferenceType refType = (ReferenceType) _typeFactory.constructReferenceType(AtomicReference.class, _typeFactory.constructType(String.class));
        BeanDescription desc = _config.introspect(refType);
        JsonDeserializer<?> deser = _factory.createReferenceDeserializer(_ctxt, refType, desc);
        assertTrue(deser instanceof AtomicReferenceDeserializer);
    }

    // Tests KeyDeserializer creation for Enum and String-based types
    @Test
    public void testCreateKeyDeserializer_enumAndString_returnsValidKeyDeserializers() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer keyDes1 = _factory.createKeyDeserializer(_ctxt, enumType);
        assertNotNull(keyDes1);

        JavaType intType = _typeFactory.constructType(Integer.class);
        KeyDeserializer keyDes2 = _factory.createKeyDeserializer(_ctxt, intType);
        assertNotNull(keyDes2);
    }

    // Tests default deserializers for core types: Object, String, Iterable, Map.Entry, TokenBuffer
    @Test
    public void testFindDefaultDeserializer_wellKnownTypes_returnsExpectedInstances() throws Exception {
        JavaType objType = _typeFactory.constructType(Object.class);
        BeanDescription desc1 = _config.introspect(objType);
        JsonDeserializer<?> deser1 = _factory.findDefaultDeserializer(_ctxt, objType, desc1);
        assertTrue(deser1 instanceof UntypedObjectDeserializer);

        JavaType strType = _typeFactory.constructType(String.class);
        BeanDescription desc2 = _config.introspect(strType);
        JsonDeserializer<?> deser2 = _factory.findDefaultDeserializer(_ctxt, strType, desc2);
        assertTrue(deser2 instanceof StringDeserializer);

        JavaType iterType = _typeFactory.constructType(Iterable.class);
        BeanDescription desc3 = _config.introspect(iterType);
        JsonDeserializer<?> deser3 = _factory.findDefaultDeserializer(_ctxt, iterType, desc3);
        assertNotNull(deser3);

        JavaType entryType = _typeFactory.constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        BeanDescription desc4 = _config.introspect(entryType);
        JsonDeserializer<?> deser4 = _factory.findDefaultDeserializer(_ctxt, entryType, desc4);
        assertTrue(deser4 instanceof MapEntryDeserializer);

        JavaType tbType = _typeFactory.constructType(TokenBuffer.class);
        BeanDescription desc5 = _config.introspect(tbType);
        JsonDeserializer<?> deser5 = _factory.findDefaultDeserializer(_ctxt, tbType, desc5);
        assertTrue(deser5 instanceof TokenBufferDeserializer);
    }

    // Tests collection-like and map-like deserializer methods when no custom deserializer configured
    @Test
    public void testCreateCollectionAndMapLikeDeserializer_noCustom_returnsNull() throws Exception {
        CollectionLikeType colLikeType = _typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription desc1 = _config.introspect(colLikeType);
        JsonDeserializer<?> deser1 = _factory.createCollectionLikeDeserializer(_ctxt, colLikeType, desc1);
        assertNull(deser1);

        MapLikeType mapLikeType = _typeFactory.constructMapLikeType(HashMap.class, String.class, String.class);
        BeanDescription desc2 = _config.introspect(mapLikeType);
        JsonDeserializer<?> deser2 = _factory.createMapLikeDeserializer(_ctxt, mapLikeType, desc2);
        assertNull(deser2);
    }

    // Tests _valueInstantiatorInstance when instDef is null or an already-created instance
    @Test
    public void testValueInstantiatorInstance_nullOrInstance_returnsExpected() throws Exception {
        ValueInstantiator inst = _factory._valueInstantiatorInstance(_config, null, null);
        assertNull(inst);

        ValueInstantiator.Base baseInst = new ValueInstantiator.Base(Object.class);
        ValueInstantiator returned = _factory._valueInstantiatorInstance(_config, null, baseInst);
        assertSame(baseInst, returned);
    }

    // Tests _valueInstantiatorInstance invalid definition type throwing IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstance_invalidDefType_throwsException() throws Exception {
        _factory._valueInstantiatorInstance(_config, null, "notAClassOrInstance");
    }
}