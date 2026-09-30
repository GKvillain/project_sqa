package com.fasterxml.jackson.databind.ser;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BasicSerializerFactoryTest {

    private ObjectMapper _mapper;
    private SerializationConfig _config;
    private DefaultSerializerProvider.Impl _provider;
    private BasicSerializerFactory _factory;

    static class CustomBasicSerializerFactory extends BasicSerializerFactory {
        public CustomBasicSerializerFactory(SerializerFactoryConfig config) {
            super(config);
        }

        @Override
        public SerializerFactory withConfig(SerializerFactoryConfig config) {
            return new CustomBasicSerializerFactory(config);
        }

        @Override
        public JsonSerializer<Object> createSerializer(SerializerProvider prov, JavaType type)
                throws JsonMappingException {
            return null;
        }

        @Override
        protected Iterable<Serializers> customSerializers() {
            return Collections.emptyList();
        }
    }

    enum TestEnum {
        A, B, C;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum ObjectFormattedEnum {
        FIRST, SECOND;
    }

    @JsonSerialize(using = StringSerializer.class)
    static class CustomAnnotatedClass {}

    @JsonSerialize(as = Object.class)
    static class SuperTypeAnnotatedClass {}

    static class StringToIntegerConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return value == null ? 0 : value.length();
        }
    }

    @JsonSerialize(converter = StringToIntegerConverter.class)
    static class ConvertedClass {
        public String value;
    }

    @JsonSerialize(keyUsing = StdKeySerializers.StringKeySerializer.class)
    static class CustomKeyAnnotatedClass {}

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getSerializationConfig();
        _provider = (DefaultSerializerProvider.Impl) _mapper.getSerializerProvider();
        _factory = new CustomBasicSerializerFactory(null);
    }

    // Tests factory config initialization and withConfig fluent methods
    @Test
    public void testFactoryConfig_nullAndChainedMethods_createsNewInstances() {
        assertNotNull(_factory.getFactoryConfig());

        SerializerFactory factory2 = _factory.withAdditionalSerializers(new Serializers.Base());
        assertNotSame(_factory, factory2);
        assertTrue(factory2.getFactoryConfig().hasSerializers());

        SerializerFactory factory3 = _factory.withAdditionalKeySerializers(new Serializers.Base());
        assertNotSame(_factory, factory3);
        assertTrue(factory3.getFactoryConfig().hasKeySerializers());

        SerializerFactory factory4 = _factory.withSerializerModifier(new BeanSerializerModifier());
        assertNotSame(_factory, factory4);
        assertTrue(factory4.getFactoryConfig().hasSerializerModifiers());
    }

    // Tests findSerializerByLookup for standard concrete cached serializers
    @Test
    public void testFindSerializerByLookup_standardJdkTypes_returnsMatchingSerializer() {
        TypeFactory tf = _config.getTypeFactory();

        JavaType strType = tf.constructType(String.class);
        BeanDescription beanDescStr = _config.introspectClassAnnotations(strType);
        JsonSerializer<?> serStr = _factory.findSerializerByLookup(strType, _config, beanDescStr, false);
        assertNotNull(serStr);
        assertTrue(serStr instanceof StringSerializer);

        JavaType bigIntType = tf.constructType(BigInteger.class);
        BeanDescription beanDescBigInt = _config.introspectClassAnnotations(bigIntType);
        JsonSerializer<?> serBigInt = _factory.findSerializerByLookup(bigIntType, _config, beanDescBigInt, false);
        assertNotNull(serBigInt);
        assertTrue(serBigInt instanceof NumberSerializer);

        JavaType boolType = tf.constructType(Boolean.TYPE);
        BeanDescription beanDescBool = _config.introspectClassAnnotations(boolType);
        JsonSerializer<?> serBool = _factory.findSerializerByLookup(boolType, _config, beanDescBool, false);
        assertNotNull(serBool);
        assertTrue(serBool instanceof BooleanSerializer);
    }

    // Tests findSerializerByLookup for lazy-loaded serializers (e.g. java.sql.Date)
    @Test
    public void testFindSerializerByLookup_lazyJdkTypes_instantiatesSerializer() {
        TypeFactory tf = _config.getTypeFactory();
        JavaType sqlDateType = tf.constructType(java.sql.Date.class);
        BeanDescription beanDesc = _config.introspectClassAnnotations(sqlDateType);
        JsonSerializer<?> ser = _factory.findSerializerByLookup(sqlDateType, _config, beanDesc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof SqlDateSerializer);
    }

    // Tests findSerializerByLookup for ReferenceType (AtomicReference)
    @Test
    public void testFindSerializerByLookup_atomicReference_returnsAtomicReferenceSerializer() {
        TypeFactory tf = _config.getTypeFactory();
        JavaType refType = tf.constructType(AtomicReference.class);
        BeanDescription beanDesc = _config.introspectClassAnnotations(refType);
        JsonSerializer<?> ser = _factory.findSerializerByLookup(refType, _config, beanDesc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof AtomicReferenceSerializer);
    }

    // Tests findSerializerByLookup with unknown/unsupported type returning null
    @Test
    public void testFindSerializerByLookup_unsupportedType_returnsNull() {
        TypeFactory tf = _config.getTypeFactory();
        JavaType customType = tf.constructType(CustomBasicSerializerFactory.class);
        BeanDescription beanDesc = _config.introspectClassAnnotations(customType);
        JsonSerializer<?> ser = _factory.findSerializerByLookup(customType, _config, beanDesc, false);
        assertNull(ser);
    }

    // Tests findSerializerByPrimaryType for various standard types
    @Test
    public void testFindSerializerByPrimaryType_knownPrimaryTypes_returnsExpectedSerializer() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        JavaType calType = tf.constructType(Calendar.class);
        BeanDescription calDesc = _config.introspectClassAnnotations(calType);
        JsonSerializer<?> calSer = _factory.findSerializerByPrimaryType(prov, calType, calDesc, false);
        assertNotNull(calSer);
        assertTrue(calSer instanceof CalendarSerializer);

        JavaType dateType = tf.constructType(Date.class);
        BeanDescription dateDesc = _config.introspectClassAnnotations(dateType);
        JsonSerializer<?> dateSer = _factory.findSerializerByPrimaryType(prov, dateType, dateDesc, false);
        assertNotNull(dateSer);
        assertTrue(dateSer instanceof DateSerializer);

        JavaType bufType = tf.constructType(ByteBuffer.class);
        BeanDescription bufDesc = _config.introspectClassAnnotations(bufType);
        JsonSerializer<?> bufSer = _factory.findSerializerByPrimaryType(prov, bufType, bufDesc, false);
        assertNotNull(bufSer);
        assertTrue(bufSer instanceof ByteBufferSerializer);

        JavaType inetType = tf.constructType(InetAddress.class);
        BeanDescription inetDesc = _config.introspectClassAnnotations(inetType);
        JsonSerializer<?> inetSer = _factory.findSerializerByPrimaryType(prov, inetType, inetDesc, false);
        assertNotNull(inetSer);
        assertTrue(inetSer instanceof InetAddressSerializer);

        JavaType sockType = tf.constructType(InetSocketAddress.class);
        BeanDescription sockDesc = _config.introspectClassAnnotations(sockType);
        JsonSerializer<?> sockSer = _factory.findSerializerByPrimaryType(prov, sockType, sockDesc, false);
        assertNotNull(sockSer);
        assertTrue(sockSer instanceof InetSocketAddressSerializer);

        JavaType tzType = tf.constructType(TimeZone.class);
        BeanDescription tzDesc = _config.introspectClassAnnotations(tzType);
        JsonSerializer<?> tzSer = _factory.findSerializerByPrimaryType(prov, tzType, tzDesc, false);
        assertNotNull(tzSer);
        assertTrue(tzSer instanceof TimeZoneSerializer);

        JavaType csType = tf.constructType(Charset.class);
        BeanDescription csDesc = _config.introspectClassAnnotations(csType);
        JsonSerializer<?> csSer = _factory.findSerializerByPrimaryType(prov, csType, csDesc, false);
        assertNotNull(csSer);
        assertTrue(csSer instanceof ToStringSerializer);
    }

    // Tests findSerializerByPrimaryType for Map.Entry
    @Test
    public void testFindSerializerByPrimaryType_mapEntry_returnsMapEntrySerializer() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        JavaType entryType = tf.constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        BeanDescription entryDesc = _config.introspect(entryType);
        JsonSerializer<?> ser = _factory.findSerializerByPrimaryType(prov, entryType, entryDesc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof MapEntrySerializer);
    }

    // Tests findSerializerByAddonType for Iterator, Iterable, CharSequence
    @Test
    public void testFindSerializerByAddonType_addonTypes_returnsCorrectSerializers() throws Exception {
        TypeFactory tf = _config.getTypeFactory();

        JavaType iterType = tf.constructType(Iterator.class);
        BeanDescription iterDesc = _config.introspect(iterType);
        JsonSerializer<?> iterSer = _factory.findSerializerByAddonType(_config, iterType, iterDesc, false);
        assertNotNull(iterSer);
        assertTrue(iterSer instanceof IteratorSerializer);

        JavaType iterableType = tf.constructType(Iterable.class);
        BeanDescription iterableDesc = _config.introspect(iterableType);
        JsonSerializer<?> iterableSer = _factory.findSerializerByAddonType(_config, iterableType, iterableDesc, false);
        assertNotNull(iterableSer);
        assertTrue(iterableSer instanceof IterableSerializer);

        JavaType seqType = tf.constructType(CharSequence.class);
        BeanDescription seqDesc = _config.introspect(seqType);
        JsonSerializer<?> seqSer = _factory.findSerializerByAddonType(_config, seqType, seqDesc, false);
        assertNotNull(seqSer);
        assertTrue(seqSer instanceof ToStringSerializer);

        JavaType objectType = tf.constructType(Object.class);
        BeanDescription objDesc = _config.introspect(objectType);
        JsonSerializer<?> nonSer = _factory.findSerializerByAddonType(_config, objectType, objDesc, false);
        assertNull(nonSer);
    }

    // Tests createKeySerializer default handling
    @Test
    public void testCreateKeySerializer_defaultKeyTypes_returnsAppropriateKeySerializer() {
        TypeFactory tf = _config.getTypeFactory();
        JavaType stringType = tf.constructType(String.class);
        JsonSerializer<Object> keySer = _factory.createKeySerializer(_config, stringType, null);
        assertNotNull(keySer);
        assertTrue(keySer instanceof StdKeySerializer || keySer instanceof StdKeySerializers.StringKeySerializer);

        JavaType intType = tf.constructType(Integer.class);
        JsonSerializer<Object> intKeySer = _factory.createKeySerializer(_config, intType, null);
        assertNotNull(intKeySer);
    }

    // Tests buildContainerSerializer for MapType
    @Test
    public void testBuildContainerSerializer_mapType_returnsMapSerializer() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        MapType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        BeanDescription mapDesc = _config.introspect(mapType);

        JsonSerializer<?> ser = _factory.buildContainerSerializer(prov, mapType, mapDesc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof MapSerializer);
    }

    // Tests buildContainerSerializer for CollectionType (ArrayList and LinkedList)
    @Test
    public void testBuildContainerSerializer_collectionTypes_returnsIndexedOrCollectionSerializer() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        CollectionType listType = tf.constructCollectionType(ArrayList.class, Integer.class);
        BeanDescription listDesc = _config.introspect(listType);
        JsonSerializer<?> listSer = _factory.buildContainerSerializer(prov, listType, listDesc, false);
        assertNotNull(listSer);
        assertTrue(listSer instanceof IndexedListSerializer);

        CollectionType strListType = tf.constructCollectionType(ArrayList.class, String.class);
        BeanDescription strListDesc = _config.introspect(strListType);
        JsonSerializer<?> strListSer = _factory.buildContainerSerializer(prov, strListType, strListDesc, false);
        assertNotNull(strListSer);
        assertTrue(strListSer instanceof IndexedStringListSerializer);

        CollectionType linkedListType = tf.constructCollectionType(LinkedList.class, Integer.class);
        BeanDescription linkedListDesc = _config.introspect(linkedListType);
        JsonSerializer<?> linkedListSer = _factory.buildContainerSerializer(prov, linkedListType, linkedListDesc, false);
        assertNotNull(linkedListSer);
        assertTrue(linkedListSer instanceof CollectionSerializer);
    }

    // Tests buildContainerSerializer for ArrayType (String[] and Object[])
    @Test
    public void testBuildContainerSerializer_arrayTypes_returnsArraySerializers() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        ArrayType strArrayType = tf.constructArrayType(String.class);
        BeanDescription strArrayDesc = _config.introspect(strArrayType);
        JsonSerializer<?> strArraySer = _factory.buildContainerSerializer(prov, strArrayType, strArrayDesc, false);
        assertNotNull(strArraySer);
        assertTrue(strArraySer instanceof StringArraySerializer);

        ArrayType intArrayType = tf.constructArrayType(Integer.class);
        BeanDescription intArrayDesc = _config.introspect(intArrayType);
        JsonSerializer<?> intArraySer = _factory.buildContainerSerializer(prov, intArrayType, intArrayDesc, false);
        assertNotNull(intArraySer);
        assertTrue(intArraySer instanceof ObjectArraySerializer);
    }

    // Tests isIndexedList method with RandomAccess and non-RandomAccess classes
    @Test
    public void testIsIndexedList_randomAccessImplementations_returnsTrueOnlyForRandomAccess() {
        assertTrue(_factory.isIndexedList(ArrayList.class));
        assertTrue(_factory.isIndexedList(Vector.class));
        assertFalse(_factory.isIndexedList(LinkedList.class));
        assertFalse(_factory.isIndexedList(HashSet.class));
    }

    // Tests _verifyAsClass method edge cases
    @Test
    public void testVerifyAsClass_variousInputs_returnsClassOrNull() {
        assertNull(_factory._verifyAsClass(null, "test", JsonSerializer.None.class));
        assertNull(_factory._verifyAsClass(JsonSerializer.None.class, "test", JsonSerializer.None.class));
        assertNull(_factory._verifyAsClass(NoClass.class, "test", JsonSerializer.None.class));
        assertEquals(StringSerializer.class, _factory._verifyAsClass(StringSerializer.class, "test", JsonSerializer.None.class));
    }

    // Tests _verifyAsClass exception path when non-Class object passed
    @Test(expected = IllegalStateException.class)
    public void testVerifyAsClass_nonClassObject_throwsIllegalStateException() {
        _factory._verifyAsClass("NotAClassInstance", "test", JsonSerializer.None.class);
    }

    // Tests usesStaticTyping helper method
    @Test
    public void testUsesStaticTyping_nullAndNonNullTypeSerializer_returnsCorrectTyping() {
        TypeFactory tf = _config.getTypeFactory();
        JavaType strType = tf.constructType(String.class);
        BeanDescription beanDesc = _config.introspectClassAnnotations(strType);

        // When TypeSerializer is present, static typing should be false
        TypeSerializer dummyTypeSer = _factory.createTypeSerializer(_config, strType);
        if (dummyTypeSer != null) {
            assertFalse(_factory.usesStaticTyping(_config, beanDesc, dummyTypeSer));
        }

        // When TypeSerializer is null and USE_STATIC_TYPING is disabled by default
        assertFalse(_factory.usesStaticTyping(_config, beanDesc, null));
    }

    // Tests primitive array types in buildContainerSerializer
    @Test
    public void testBuildContainerSerializer_primitiveArrayTypes_returnsStdArraySerializers() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        Class<?>[] primArrayClasses = new Class<?>[] {
            boolean[].class, byte[].class, char[].class, short[].class,
            int[].class, long[].class, float[].class, double[].class
        };

        for (Class<?> cls : primArrayClasses) {
            ArrayType arrType = tf.constructArrayType(cls.getComponentType());
            BeanDescription desc = _config.introspect(arrType);
            JsonSerializer<?> ser = _factory.buildContainerSerializer(prov, arrType, desc, false);
            assertNotNull("Serializer for " + cls.getName() + " should not be null", ser);
        }
    }

    // Tests Enum serialization and Enum as Shape.OBJECT
    @Test
    public void testFindSerializerByLookup_enumTypes_returnsEnumSerializerOrNull() throws Exception {
        TypeFactory tf = _config.getTypeFactory();

        JavaType enumType = tf.constructType(TestEnum.class);
        BeanDescription enumDesc = _config.introspectClassAnnotations(enumType);
        JsonSerializer<?> ser = _factory.findSerializerByLookup(enumType, _config, enumDesc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof EnumSerializer);

        JavaType objEnumType = tf.constructType(ObjectFormattedEnum.class);
        BeanDescription objEnumDesc = _config.introspectClassAnnotations(objEnumType);
        JsonSerializer<?> objSer = _factory.findSerializerByLookup(objEnumType, _config, objEnumDesc, false);
        assertNull(objSer);
    }

    // Tests findSerializerByAnnotations with custom serializer, converter, and typing
    @Test
    public void testFindSerializerByAnnotations_customSerializerAndConverter() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        JavaType customType = tf.constructType(CustomAnnotatedClass.class);
        BeanDescription customDesc = _config.introspectClassAnnotations(customType);
        JsonSerializer<?> customSer = _factory.findSerializerByAnnotations(prov, customType, customDesc);
        assertNotNull(customSer);
        assertTrue(customSer instanceof StringSerializer);

        JavaType convertedType = tf.constructType(ConvertedClass.class);
        BeanDescription convertedDesc = _config.introspect(convertedType);
        JsonSerializer<?> convSer = _factory.findSerializerByAnnotations(prov, convertedType, convertedDesc);
        assertNotNull(convSer);
        assertTrue(convSer instanceof StdDelegatingSerializer);

        JavaType superAnnotatedType = tf.constructType(SuperTypeAnnotatedClass.class);
        BeanDescription superAnnotatedDesc = _config.introspectClassAnnotations(superAnnotatedType);
        JsonSerializer<?> superSer = _factory.findSerializerByAnnotations(prov, superAnnotatedType, superAnnotatedDesc);
        assertNull(superSer);
    }

    // Tests findSerializerByLookup for additional known JDK types
    @Test
    public void testFindSerializerByLookup_additionalJdkTypes() {
        TypeFactory tf = _config.getTypeFactory();

        Class<?>[] types = new Class<?>[] {
            UUID.class, URL.class, URI.class, Currency.class, Locale.class,
            Pattern.class, AtomicBoolean.class, AtomicInteger.class, AtomicLong.class,
            File.class, Class.class, Void.class, Void.TYPE,
            BigDecimal.class, Double.class, Double.TYPE, Float.class, Float.TYPE,
            Long.class, Long.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE,
            Byte.class, Byte.TYPE, Character.class, Character.TYPE
        };

        for (Class<?> cls : types) {
            JavaType type = tf.constructType(cls);
            BeanDescription desc = _config.introspectClassAnnotations(type);
            JsonSerializer<?> ser = _factory.findSerializerByLookup(type, _config, desc, false);
            assertNotNull("Serializer for " + cls.getName() + " should not be null", ser);
        }
    }

    // Tests findSerializerByPrimaryType for TokenBuffer, InputStream, and Reader
    @Test
    public void testFindSerializerByPrimaryType_streamAndBufferTypes() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        JavaType tokenBufType = tf.constructType(TokenBuffer.class);
        BeanDescription tbDesc = _config.introspectClassAnnotations(tokenBufType);
        JsonSerializer<?> tbSer = _factory.findSerializerByPrimaryType(prov, tokenBufType, tbDesc, false);
        assertNotNull(tbSer);
        assertTrue(tbSer instanceof TokenBufferSerializer);

        JavaType streamType = tf.constructType(InputStream.class);
        BeanDescription streamDesc = _config.introspectClassAnnotations(streamType);
        JsonSerializer<?> streamSer = _factory.findSerializerByPrimaryType(prov, streamType, streamDesc, false);
        assertNotNull(streamSer);
        assertTrue(streamSer instanceof InputStreamSerializer);

        JavaType readerType = tf.constructType(Reader.class);
        BeanDescription readerDesc = _config.introspectClassAnnotations(readerType);
        JsonSerializer<?> readerSer = _factory.findSerializerByPrimaryType(prov, readerType, readerDesc, false);
        assertNotNull(readerSer);
        assertTrue(readerSer instanceof ToStringSerializer);
    }

    // Tests findSerializerByPrimaryType for JsonSerializable implementations
    @Test
    public void testFindSerializerByPrimaryType_jsonSerializable() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        JavaType serializableType = tf.constructType(JsonSerializable.Base.class);
        BeanDescription desc = _config.introspectClassAnnotations(serializableType);
        JsonSerializer<?> ser = _factory.findSerializerByPrimaryType(prov, serializableType, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof SerializableSerializer);
    }

    // Tests createKeySerializer with annotated key serializer
    @Test
    public void testCreateKeySerializer_annotatedKeySerializer() throws Exception {
        TypeFactory tf = _config.getTypeFactory();
        JavaType keyType = tf.constructType(CustomKeyAnnotatedClass.class);
        JsonSerializer<Object> keySer = _factory.createKeySerializer(_config, keyType, null);
        assertNotNull(keySer);
        assertTrue(keySer instanceof StdKeySerializers.StringKeySerializer);
    }

    // Tests usesStaticTyping with SerializationFeature.USE_STATIC_TYPING
    @Test
    public void testUsesStaticTyping_withStaticTypingFeatureEnabled() {
        TypeFactory tf = _config.getTypeFactory();
        SerializationConfig config = _config.with(SerializationFeature.USE_STATIC_TYPING);
        JavaType type = tf.constructType(String.class);
        BeanDescription desc = config.introspectClassAnnotations(type);
        assertTrue(_factory.usesStaticTyping(config, desc, null));
    }
}