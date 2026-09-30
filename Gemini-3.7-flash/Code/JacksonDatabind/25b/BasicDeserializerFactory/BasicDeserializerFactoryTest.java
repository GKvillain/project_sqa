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
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
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

    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private BasicDeserializerFactory _factory;

    private enum TestEnum {
        A, B, C
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = BeanDeserializerFactory.instance;
    }

    // Tests fluent configuration methods creating updated factory instances
    @Test
    public void testWithConfig_allVariants_returnsUpdatedFactories() {
        DeserializerFactory f1 = _factory.withAdditionalDeserializers(new Deserializers.Base());
        assertNotNull(f1);
        assertTrue(f1.getFactoryConfig().hasDeserializers());

        DeserializerFactory f2 = _factory.withAdditionalKeyDeserializers(new KeyDeserializers() {
            @Override
            public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                return null;
            }
        });
        assertNotNull(f2);
        assertTrue(f2.getFactoryConfig().hasKeyDeserializers());

        DeserializerFactory f3 = _factory.withDeserializerModifier(new BeanDeserializerModifier());
        assertNotNull(f3);
        assertTrue(f3.getFactoryConfig().hasDeserializerModifiers());

        DeserializerFactory f4 = _factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        assertNotNull(f4);
        assertTrue(f4.getFactoryConfig().hasAbstractTypeResolvers());

        DeserializerFactory f5 = _factory.withValueInstantiators(new SimpleValueInstantiators());
        assertNotNull(f5);
        assertTrue(f5.getFactoryConfig().hasValueInstantiators());
    }

    // Tests mapping of abstract types using registered abstract type resolvers
    @Test
    public void testMapAbstractType_validMapping_returnsConcreteType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        DeserializerFactory f = _factory.withAbstractTypeResolver(resolver);

        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType abstractType = config.constructType(CharSequence.class);
        JavaType resolved = f.mapAbstractType(config, abstractType);

        assertNotNull(resolved);
        assertEquals(String.class, resolved.getRawClass());
    }

    // Tests mapping of abstract types when target is not a subtype (exception path)
    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractType_invalidMapping_throwsIllegalArgumentException() throws Exception {
        AbstractTypeResolver resolver = new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.getRawClass() == List.class) {
                    return config.constructType(Map.class);
                }
                return null;
            }

            @Override
            public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
                return null;
            }
        };

        DeserializerFactory f = _factory.withAbstractTypeResolver(resolver);
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType listType = config.constructType(List.class);

        f.mapAbstractType(config, listType);
    }

    // Tests mapping abstract type when no resolver is registered (returns original type)
    @Test
    public void testMapAbstractType_noMapping_returnsSameType() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType listType = config.constructType(List.class);
        JavaType resolved = _factory.mapAbstractType(config, listType);

        assertSame(listType, resolved);
    }

    // Tests default deserializer resolution for standard Java/Jackson types
    @Test
    public void testFindDefaultDeserializer_standardTypes_returnsExpectedDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        // Object (untyped)
        JavaType objType = config.constructType(Object.class);
        BeanDescription objDesc = config.introspectClassAnnotations(objType);
        JsonDeserializer<?> objDeser = _factory.findDefaultDeserializer(_context, objType, objDesc);
        assertNotNull(objDeser);

        // String & CharSequence
        JavaType strType = config.constructType(String.class);
        BeanDescription strDesc = config.introspectClassAnnotations(strType);
        JsonDeserializer<?> strDeser = _factory.findDefaultDeserializer(_context, strType, strDesc);
        assertNotNull(strDeser);

        // Primitives & Numbers
        JavaType intType = config.constructType(int.class);
        BeanDescription intDesc = config.introspectClassAnnotations(intType);
        JsonDeserializer<?> intDeser = _factory.findDefaultDeserializer(_context, intType, intDesc);
        assertNotNull(intDeser);

        // Date types
        JavaType dateType = config.constructType(Date.class);
        BeanDescription dateDesc = config.introspectClassAnnotations(dateType);
        JsonDeserializer<?> dateDeser = _factory.findDefaultDeserializer(_context, dateType, dateDesc);
        assertNotNull(dateDeser);

        // TokenBuffer
        JavaType tbType = config.constructType(TokenBuffer.class);
        BeanDescription tbDesc = config.introspectClassAnnotations(tbType);
        JsonDeserializer<?> tbDeser = _factory.findDefaultDeserializer(_context, tbType, tbDesc);
        assertNotNull(tbDeser);
    }

    // Tests default deserializer resolution for Iterable and Map.Entry
    @Test
    public void testFindDefaultDeserializer_iterableAndMapEntry_returnsDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();

        JavaType iterableType = tf.constructType(Iterable.class);
        BeanDescription iterDesc = config.introspectClassAnnotations(iterableType);
        JsonDeserializer<?> iterDeser = _factory.findDefaultDeserializer(_context, iterableType, iterDesc);
        assertNotNull(iterDeser);

        JavaType entryType = tf.constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        BeanDescription entryDesc = config.introspectClassAnnotations(entryType);
        JsonDeserializer<?> entryDeser = _factory.findDefaultDeserializer(_context, entryType, entryDesc);
        assertNotNull(entryDeser);
    }

    // Tests creation of array deserializers for primitive, String, and Object types
    @Test
    public void testCreateArrayDeserializer_variousTypes_returnsArrayDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();

        // Primitive int array
        ArrayType intArrayType = tf.constructArrayType(int.class);
        BeanDescription intDesc = config.introspect(intArrayType);
        JsonDeserializer<?> intArrDeser = _factory.createArrayDeserializer(_context, intArrayType, intDesc);
        assertNotNull(intArrDeser);

        // String array
        ArrayType strArrayType = tf.constructArrayType(String.class);
        BeanDescription strDesc = config.introspect(strArrayType);
        JsonDeserializer<?> strArrDeser = _factory.createArrayDeserializer(_context, strArrayType, strDesc);
        assertNotNull(strArrDeser);

        // Object array
        ArrayType objArrayType = tf.constructArrayType(Object.class);
        BeanDescription objDesc = config.introspect(objArrayType);
        JsonDeserializer<?> objArrDeser = _factory.createArrayDeserializer(_context, objArrayType, objDesc);
        assertNotNull(objArrDeser);
    }

    // Tests creation of collection deserializers with standard concrete, abstract, and specialized types
    @Test
    public void testCreateCollectionDeserializer_standardTypes_returnsCollectionDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();

        // Abstract List (mapped fallback to ArrayList)
        CollectionType listType = tf.constructCollectionType(List.class, String.class);
        BeanDescription listDesc = config.introspect(listType);
        JsonDeserializer<?> listDeser = _factory.createCollectionDeserializer(_context, listType, listDesc);
        assertNotNull(listDeser);

        // Abstract Set (mapped fallback to HashSet)
        CollectionType setType = tf.constructCollectionType(Set.class, Integer.class);
        BeanDescription setDesc = config.introspect(setType);
        JsonDeserializer<?> setDeser = _factory.createCollectionDeserializer(_context, setType, setDesc);
        assertNotNull(setDeser);

        // EnumSet
        CollectionType enumSetType = tf.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription enumSetDesc = config.introspect(enumSetType);
        JsonDeserializer<?> enumSetDeser = _factory.createCollectionDeserializer(_context, enumSetType, enumSetDesc);
        assertNotNull(enumSetDeser);

        // ArrayBlockingQueue (requires non-default constructor handling)
        CollectionType abqType = tf.constructCollectionType(ArrayBlockingQueue.class, String.class);
        BeanDescription abqDesc = config.introspect(abqType);
        JsonDeserializer<?> abqDeser = _factory.createCollectionDeserializer(_context, abqType, abqDesc);
        assertNotNull(abqDeser);
    }

    // Tests creation of map deserializers for standard maps, abstract maps, and EnumMap
    @Test
    public void testCreateMapDeserializer_standardTypes_returnsMapDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();

        // Abstract Map (mapped fallback to LinkedHashMap)
        MapType mapType = tf.constructMapType(Map.class, String.class, Object.class);
        BeanDescription mapDesc = config.introspect(mapType);
        JsonDeserializer<?> mapDeser = _factory.createMapDeserializer(_context, mapType, mapDesc);
        assertNotNull(mapDeser);

        // Concrete TreeMap
        MapType treeMapType = tf.constructMapType(TreeMap.class, String.class, String.class);
        BeanDescription treeMapDesc = config.introspect(treeMapType);
        JsonDeserializer<?> treeMapDeser = _factory.createMapDeserializer(_context, treeMapType, treeMapDesc);
        assertNotNull(treeMapDeser);

        // EnumMap
        MapType enumMapType = tf.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription enumMapDesc = config.introspect(enumMapType);
        JsonDeserializer<?> enumMapDeser = _factory.createMapDeserializer(_context, enumMapType, enumMapDesc);
        assertNotNull(enumMapDeser);
    }

    // Tests createMapDeserializer with EnumMap without enum key (exception path)
    @Test(expected = IllegalArgumentException.class)
    public void testCreateMapDeserializer_enumMapWithNonEnumKey_throwsIllegalArgumentException() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();

        MapType invalidEnumMapType = tf.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription desc = config.introspect(invalidEnumMapType);

        _factory.createMapDeserializer(_context, invalidEnumMapType, desc);
    }

    // Tests creation of enum deserializer
    @Test
    public void testCreateEnumDeserializer_standardEnum_returnsEnumDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType enumType = config.constructType(TestEnum.class);
        BeanDescription desc = config.introspect(enumType);

        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_context, enumType, desc);
        assertNotNull(deser);
    }

    // Tests creation of tree deserializer for JsonNode
    @Test
    public void testCreateTreeDeserializer_jsonNodeTypes_returnsTreeDeserializer() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        JavaType nodeType = config.constructType(JsonNode.class);
        BeanDescription nodeDesc = config.introspect(nodeType);
        JsonDeserializer<?> nodeDeser = _factory.createTreeDeserializer(config, nodeType, nodeDesc);
        assertNotNull(nodeDeser);

        JavaType objNodeType = config.constructType(ObjectNode.class);
        BeanDescription objNodeDesc = config.introspect(objNodeType);
        JsonDeserializer<?> objNodeDeser = _factory.createTreeDeserializer(config, objNodeType, objNodeDesc);
        assertNotNull(objNodeDeser);
    }

    // Tests key deserializer creation for enum and standard types
    @Test
    public void testCreateKeyDeserializer_enumAndStdTypes_returnsKeyDeserializer() throws Exception {
        JavaType enumType = _mapper.constructType(TestEnum.class);
        KeyDeserializer enumKeyDeser = _factory.createKeyDeserializer(_context, enumType);
        assertNotNull(enumKeyDeser);

        JavaType strType = _mapper.constructType(String.class);
        KeyDeserializer strKeyDeser = _factory.createKeyDeserializer(_context, strType);
        assertNotNull(strKeyDeser);

        JavaType intType = _mapper.constructType(Integer.class);
        KeyDeserializer intKeyDeser = _factory.createKeyDeserializer(_context, intType);
        assertNotNull(intKeyDeser);
    }

    // Tests findValueInstantiator for standard types like JsonLocation
    @Test
    public void testFindValueInstantiator_jsonLocation_returnsInstantiator() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType locType = config.constructType(JsonLocation.class);
        BeanDescription locDesc = config.introspect(locType);

        ValueInstantiator inst = _factory.findValueInstantiator(_context, locDesc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromObjectWith());
    }

    // Tests _valueInstantiatorInstance with null and valid instance inputs
    @Test
    public void testValueInstantiatorInstance_nullAndInstance_returnsExpected() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();

        ValueInstantiator nullInst = _factory._valueInstantiatorInstance(config, null, null);
        assertNull(nullInst);

        ValueInstantiator dummy = new ValueInstantiator.Base(String.class);
        ValueInstantiator returned = _factory._valueInstantiatorInstance(config, null, dummy);
        assertSame(dummy, returned);
    }

    // Tests _valueInstantiatorInstance with invalid class type (exception path)
    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstance_invalidClass_throwsIllegalStateException() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        _factory._valueInstantiatorInstance(config, null, String.class);
    }

    // Additional tests covering custom deserializers and collection-like/map-like/reference types
    @Test
    public void testCreateCollectionLikeDeserializer_customResolver() throws Exception {
        final JsonDeserializer<?> customDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        DeserializerFactory f = _factory.withAdditionalDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type,
                    DeserializationConfig config, BeanDescription beanDesc,
                    TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) {
                return customDeser;
            }
        });

        DeserializationConfig config = _mapper.getDeserializationConfig();
        CollectionLikeType clType = config.getTypeFactory().constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(clType);

        JsonDeserializer<?> result = f.createCollectionLikeDeserializer(_context, clType, desc);
        assertSame(customDeser, result);
    }

    @Test
    public void testCreateMapLikeDeserializer_customResolver() throws Exception {
        final JsonDeserializer<?> customDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        DeserializerFactory f = _factory.withAdditionalDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type,
                    DeserializationConfig config, BeanDescription beanDesc,
                    KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer,
                    JsonDeserializer<?> elementDeserializer) {
                return customDeser;
            }
        });

        DeserializationConfig config = _mapper.getDeserializationConfig();
        MapLikeType mlType = config.getTypeFactory().constructMapLikeType(Map.Entry.class, String.class, Object.class);
        BeanDescription desc = config.introspectClassAnnotations(mlType);

        JsonDeserializer<?> result = f.createMapLikeDeserializer(_context, mlType, desc);
        assertSame(customDeser, result);
    }

    @Test
    public void testCreateReferenceDeserializer_atomicReference() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        ReferenceType refType = config.getTypeFactory().constructReferenceType(AtomicReference.class, config.constructType(String.class));
        BeanDescription desc = config.introspect(refType);

        JsonDeserializer<?> result = _factory.createReferenceDeserializer(_context, refType, desc);
        assertNotNull(result);
    }
}