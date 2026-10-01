package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BasicDeserializerFactoryTest
{
    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    enum TestEnum {
        A, B, C;
    }

    enum CustomKeyEnum {
        VAL1, VAL2;
    }

    static class CustomKeyDeserializer extends KeyDeserializer implements Serializable {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return CustomKeyEnum.VAL1;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = BeanDeserializerFactory.instance;
        _typeFactory = _mapper.getTypeFactory();
    }

    // Tests defect 67b: Key deserializer modification for Enum types
    @Test
    public void testCreateKeyDeserializer_enumTypeWithModifier_appliesModifier() throws Exception {
        final KeyDeserializer customKeyDeser = new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) {
                return TestEnum.B;
            }
        };

        DeserializerFactory factory = _factory.withDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config, JavaType type, KeyDeserializer deser) {
                if (type.isEnumType()) {
                    return customKeyDeser;
                }
                return deser;
            }
        });

        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer kd = factory.createKeyDeserializer(_context, enumType);
        assertNotNull(kd);
        assertEquals(customKeyDeser, kd);
    }

    // Tests createKeyDeserializer for standard String-based type
    @Test
    public void testCreateKeyDeserializer_stringType_returnsKeyDeserializer() throws Exception {
        JavaType stringType = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _factory.createKeyDeserializer(_context, stringType);
        assertNotNull(kd);
        Object key = kd.deserializeKey("test", _context);
        assertEquals("test", key);
    }

    // Tests createKeyDeserializer for standard Enum type without modifiers
    @Test
    public void testCreateKeyDeserializer_enumTypeStandard_returnsEnumKeyDeserializer() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer kd = _factory.createKeyDeserializer(_context, enumType);
        assertNotNull(kd);
        Object key = kd.deserializeKey("A", _context);
        assertEquals(TestEnum.A, key);
    }

    // Tests fluent configuration methods creating new instances with configs
    @Test
    public void testWithConfig_fluentMethods_createsConfiguredFactory() {
        DeserializerFactory f = _factory;
        f = f.withAdditionalDeserializers(new SimpleDeserializers());
        f = f.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        f = f.withDeserializerModifier(new BeanDeserializerModifier());
        f = f.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        f = f.withValueInstantiators(new SimpleValueInstantiators());

        assertNotNull(f);
        DeserializerFactoryConfig config = ((BasicDeserializerFactory) f).getFactoryConfig();
        assertTrue(config.hasDeserializers());
        assertTrue(config.hasKeyDeserializers());
        assertTrue(config.hasDeserializerModifiers());
        assertTrue(config.hasAbstractTypeResolvers());
        assertTrue(config.hasValueInstantiators());
    }

    // Tests mapAbstractType with AbstractTypeResolver and error on cyclic / non-subtype resolution
    @Test
    public void testMapAbstractType_validAndInvalidMapping() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        DeserializerFactory factory = _factory.withAbstractTypeResolver(resolver);

        JavaType csType = _typeFactory.constructType(CharSequence.class);
        JavaType resolved = factory.mapAbstractType(_mapper.getDeserializationConfig(), csType);
        assertEquals(String.class, resolved.getRawClass());

        SimpleAbstractTypeResolver invalidResolver = new SimpleAbstractTypeResolver();
        Class rawList = List.class;
        Class rawString = String.class;
        try {
            invalidResolver.addMapping(rawList, rawString);
            fail("Expected IllegalArgumentException for invalid abstract type mapping");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not a subtype of"));
        }
    }

    // Tests createArrayDeserializer for primitive and String arrays
    @Test
    public void testCreateArrayDeserializer_primitiveAndStringArrays_returnsCorrectDeserializer() throws Exception {
        ArrayType intArrayType = _typeFactory.constructArrayType(int.class);
        BeanDescription intArrDesc = _mapper.getDeserializationConfig().introspect(intArrayType);
        JsonDeserializer<?> intArrDeser = _factory.createArrayDeserializer(_context, intArrayType, intArrDesc);
        assertNotNull(intArrDeser);

        ArrayType strArrayType = _typeFactory.constructArrayType(String.class);
        BeanDescription strArrDesc = _mapper.getDeserializationConfig().introspect(strArrayType);
        JsonDeserializer<?> strArrDeser = _factory.createArrayDeserializer(_context, strArrayType, strArrDesc);
        assertNotNull(strArrDeser);
    }

    // Tests createCollectionDeserializer for ArrayList and EnumSet
    @Test
    public void testCreateCollectionDeserializer_standardAndEnumSet_returnsDeserializers() throws Exception {
        CollectionType listType = _typeFactory.constructCollectionType(List.class, String.class);
        BeanDescription listDesc = _mapper.getDeserializationConfig().introspect(listType);
        JsonDeserializer<?> listDeser = _factory.createCollectionDeserializer(_context, listType, listDesc);
        assertNotNull(listDeser);

        CollectionType enumSetType = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription enumSetDesc = _mapper.getDeserializationConfig().introspect(enumSetType);
        JsonDeserializer<?> enumSetDeser = _factory.createCollectionDeserializer(_context, enumSetType, enumSetDesc);
        assertNotNull(enumSetDeser);
    }

    // Tests createCollectionLikeDeserializer
    @Test
    public void testCreateCollectionLikeDeserializer() throws Exception {
        CollectionLikeType clType = _typeFactory.constructCollectionLikeType(String.class, Integer.class);
        BeanDescription desc = _mapper.getDeserializationConfig().introspectClassAnnotations(clType);
        JsonDeserializer<?> deser = _factory.createCollectionLikeDeserializer(_context, clType, desc);
        assertNull(deser);
    }

    // Tests createMapDeserializer for abstract Map interface and EnumMap
    @Test
    public void testCreateMapDeserializer_abstractMapAndEnumMap_returnsDeserializers() throws Exception {
        MapType mapType = _typeFactory.constructMapType(Map.class, String.class, Object.class);
        BeanDescription mapDesc = _mapper.getDeserializationConfig().introspect(mapType);
        JsonDeserializer<?> mapDeser = _factory.createMapDeserializer(_context, mapType, mapDesc);
        assertNotNull(mapDeser);

        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription enumMapDesc = _mapper.getDeserializationConfig().introspect(enumMapType);
        JsonDeserializer<?> enumMapDeser = _factory.createMapDeserializer(_context, enumMapType, enumMapDesc);
        assertNotNull(enumMapDeser);
    }

    // Tests createMapLikeDeserializer
    @Test
    public void testCreateMapLikeDeserializer() throws Exception {
        MapLikeType mlType = _typeFactory.constructMapLikeType(String.class, String.class, Integer.class);
        BeanDescription desc = _mapper.getDeserializationConfig().introspectClassAnnotations(mlType);
        JsonDeserializer<?> deser = _factory.createMapLikeDeserializer(_context, mlType, desc);
        assertNull(deser);
    }

    // Tests createMapDeserializer with non-Enum key for EnumMap throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCreateMapDeserializer_invalidEnumMapKey_throwsException() throws Exception {
        MapType invalidEnumMapType = _typeFactory.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription mapDesc = _mapper.getDeserializationConfig().introspect(invalidEnumMapType);
        _factory.createMapDeserializer(_context, invalidEnumMapType, mapDesc);
    }

    // Tests createEnumDeserializer
    @Test
    public void testCreateEnumDeserializer() throws Exception {
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        BeanDescription enumDesc = _mapper.getDeserializationConfig().introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_context, enumType, enumDesc);
        assertNotNull(deser);
    }

    // Tests createTreeDeserializer for ObjectNode and ArrayNode
    @Test
    public void testCreateTreeDeserializer_jsonNodeTypes_returnsDeserializer() throws Exception {
        JavaType objNodeType = _typeFactory.constructType(ObjectNode.class);
        BeanDescription objNodeDesc = _mapper.getDeserializationConfig().introspect(objNodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(_mapper.getDeserializationConfig(), objNodeType, objNodeDesc);
        assertNotNull(deser);

        JavaType arrNodeType = _typeFactory.constructType(ArrayNode.class);
        BeanDescription arrNodeDesc = _mapper.getDeserializationConfig().introspect(arrNodeType);
        JsonDeserializer<?> arrDeser = _factory.createTreeDeserializer(_mapper.getDeserializationConfig(), arrNodeType, arrNodeDesc);
        assertNotNull(arrDeser);
    }

    // Tests findValueInstantiator for JsonLocation
    @Test
    public void testFindValueInstantiator_jsonLocation_returnsStdInstantiator() throws Exception {
        JavaType locType = _typeFactory.constructType(JsonLocation.class);
        BeanDescription desc = _mapper.getDeserializationConfig().introspect(locType);
        ValueInstantiator inst = _factory.findValueInstantiator(_context, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromObjectWith());
    }

    // Tests _valueInstantiatorInstance exception handling on invalid definition type
    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstance_invalidDefType_throwsException() throws Exception {
        BasicDeserializerFactory bdf = (BasicDeserializerFactory) _factory;
        bdf._valueInstantiatorInstance(_mapper.getDeserializationConfig(), null, "invalidStringInstantiator");
    }

    // Tests _valueInstantiatorInstance exception handling when class is not ValueInstantiator subclass
    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstance_nonInstantiatorClass_throwsException() throws Exception {
        BasicDeserializerFactory bdf = (BasicDeserializerFactory) _factory;
        bdf._valueInstantiatorInstance(_mapper.getDeserializationConfig(), null, String.class);
    }
}