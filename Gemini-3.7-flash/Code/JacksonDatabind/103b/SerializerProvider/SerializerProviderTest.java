package com.fasterxml.jackson.databind;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;

import static org.junit.Assert.*;

public class SerializerProviderTest {

    private ObjectMapper _mapper;
    private DefaultSerializerProvider _provider;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl src = new DefaultSerializerProvider.Impl();
        _provider = src.createInstance(_mapper.getSerializationConfig(), _mapper.getSerializerFactory());
    }

    // Tests default unknown type serializer resolution for Object.class vs specific class
    @Test
    public void testGetUnknownTypeSerializer_differentTypes_returnsExpectedSerializer() {
        JsonSerializer<Object> objSer = _provider.getUnknownTypeSerializer(Object.class);
        assertNotNull(objSer);
        assertSame(SerializerProvider.DEFAULT_UNKNOWN_SERIALIZER, objSer);

        JsonSerializer<Object> strSer = _provider.getUnknownTypeSerializer(String.class);
        assertNotNull(strSer);
        assertTrue(strSer instanceof UnknownSerializer);
        assertNotSame(objSer, strSer);
    }

    // Tests isUnknownTypeSerializer with null and UnknownSerializer instances
    @Test
    public void testIsUnknownTypeSerializer_variousSerializers_returnsExpected() {
        assertTrue(_provider.isUnknownTypeSerializer(null));
        assertTrue(_provider.isUnknownTypeSerializer(SerializerProvider.DEFAULT_UNKNOWN_SERIALIZER));

        JsonSerializer<Object> unknownSer = new UnknownSerializer(String.class);
        assertTrue(_provider.isUnknownTypeSerializer(unknownSer));

        _mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultSerializerProvider provNoFail = ((DefaultSerializerProvider.Impl) _provider)
                .createInstance(_mapper.getSerializationConfig(), _mapper.getSerializerFactory());
        assertFalse(provNoFail.isUnknownTypeSerializer(unknownSer));
    }

    // Tests null key and value serializer setters and getters
    @Test
    public void testSetAndGetSpecializedSerializers_validSerializers_setsCorrectly() {
        JsonSerializer<Object> customNullValueSer = NullSerializer.instance;
        _provider.setNullValueSerializer(customNullValueSer);
        assertSame(customNullValueSer, _provider.getDefaultNullValueSerializer());

        JsonSerializer<Object> customNullKeySer = SerializerProvider.DEFAULT_NULL_KEY_SERIALIZER;
        _provider.setNullKeySerializer(customNullKeySer);
        assertSame(customNullKeySer, _provider.getDefaultNullKeySerializer());

        JsonSerializer<Object> customKeySer = NullSerializer.instance;
        _provider.setDefaultKeySerializer(customKeySer);
    }

    // Tests exception path when null is passed to setDefaultKeySerializer
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultKeySerializer_nullInput_throwsIllegalArgumentException() {
        _provider.setDefaultKeySerializer(null);
    }

    // Tests exception path when null is passed to setNullValueSerializer
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullValueSerializer_nullInput_throwsIllegalArgumentException() {
        _provider.setNullValueSerializer(null);
    }

    // Tests exception path when null is passed to setNullKeySerializer
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullKeySerializer_nullInput_throwsIllegalArgumentException() {
        _provider.setNullKeySerializer(null);
    }

    // Tests findValueSerializer with class and javaType
    @Test
    public void testFindValueSerializer_validTypes_returnsSerializer() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findValueSerializer(String.class, null);
        assertNotNull(ser1);

        JsonSerializer<Object> ser2 = _provider.findValueSerializer(String.class);
        assertNotNull(ser2);
        assertSame(ser1, ser2);

        JavaType type = _mapper.constructType(Integer.class);
        JsonSerializer<Object> ser3 = _provider.findValueSerializer(type, null);
        assertNotNull(ser3);

        JsonSerializer<Object> ser4 = _provider.findValueSerializer(type);
        assertNotNull(ser4);
    }

    // Tests findValueSerializer when passing null JavaType
    @Test(expected = JsonMappingException.class)
    public void testFindValueSerializer_nullJavaType_throwsJsonMappingException() throws Exception {
        _provider.findValueSerializer((JavaType) null, null);
    }

    // Tests findPrimaryPropertySerializer with class and javaType
    @Test
    public void testFindPrimaryPropertySerializer_validTypes_returnsSerializer() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findPrimaryPropertySerializer(String.class, null);
        assertNotNull(ser1);

        JavaType type = _mapper.constructType(Long.class);
        JsonSerializer<Object> ser2 = _provider.findPrimaryPropertySerializer(type, null);
        assertNotNull(ser2);
    }

    // Tests findTypedValueSerializer caching and resolution
    @Test
    public void testFindTypedValueSerializer_validClassAndType_returnsSerializer() throws Exception {
        JsonSerializer<Object> serClass = _provider.findTypedValueSerializer(String.class, true, null);
        assertNotNull(serClass);
        // Second call should hit cache
        JsonSerializer<Object> cachedSerClass = _provider.findTypedValueSerializer(String.class, true, null);
        assertSame(serClass, cachedSerClass);

        JavaType type = _mapper.constructType(String.class);
        JsonSerializer<Object> serType = _provider.findTypedValueSerializer(type, true, null);
        assertNotNull(serType);
    }

    // Tests findKeySerializer and findNullKeySerializer
    @Test
    public void testFindKeySerializer_validType_returnsSerializer() throws Exception {
        JavaType type = _mapper.constructType(String.class);
        JsonSerializer<Object> keySer = _provider.findKeySerializer(type, null);
        assertNotNull(keySer);

        JsonSerializer<Object> keySerClass = _provider.findKeySerializer(String.class, null);
        assertNotNull(keySerClass);

        JsonSerializer<Object> nullKeySer = _provider.findNullKeySerializer(type, null);
        assertNotNull(nullKeySer);
    }

    // Tests getAttribute and setAttribute functionality
    @Test
    public void testPerCallAttributes_setAndGet_returnsCorrectValue() {
        assertNull(_provider.getAttribute("testKey"));
        _provider.setAttribute("testKey", "testValue");
        assertEquals("testValue", _provider.getAttribute("testKey"));
    }

    // Tests reportBadDefinition with type and cause
    @Test
    public void testReportBadDefinition_withCause_throwsInvalidDefinitionExceptionWithCause() {
        JavaType type = _mapper.constructType(String.class);
        Throwable cause = new IllegalStateException("Original cause");
        try {
            _provider.reportBadDefinition(type, "Bad type definition", cause);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertEquals("Bad type definition", e.getOriginalMessage());
            assertSame(cause, e.getCause());
            assertEquals(type, e.getType());
        } catch (JsonMappingException e) {
            fail("Expected InvalidDefinitionException subclass");
        }
    }

    // Tests reportBadDefinition with raw class and cause
    @Test
    public void testReportBadDefinition_withRawClassAndCause_throwsInvalidDefinitionException() {
        Throwable cause = new IllegalArgumentException("Root cause");
        try {
            _provider.reportBadDefinition(String.class, "Bad class message", cause);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertEquals("Bad class message", e.getOriginalMessage());
            assertSame(cause, e.getCause());
        } catch (JsonMappingException e) {
            fail("Expected InvalidDefinitionException subclass");
        }
    }

    // Tests reportBadTypeDefinition and reportBadPropertyDefinition with null descriptors
    @Test
    public void testReportBadDefinitions_nullBeanAndProperty_throwsInvalidDefinitionException() {
        try {
            _provider.reportBadTypeDefinition(null, "Type problem %s", "details");
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("Invalid type definition for type N/A: Type problem details"));
        } catch (JsonMappingException e) {
            fail("Expected InvalidDefinitionException");
        }

        try {
            _provider.reportBadPropertyDefinition(null, null, "Property problem %d", 123);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("Invalid definition for property N/A (of type N/A): Property problem 123"));
        } catch (JsonMappingException e) {
            fail("Expected InvalidDefinitionException");
        }
    }

    // Tests invalidTypeIdException generation
    @Test
    public void testInvalidTypeIdException_validInputs_returnsException() {
        JavaType baseType = _mapper.constructType(Object.class);
        JsonMappingException jme = _provider.invalidTypeIdException(baseType, "myId", "extra info");
        assertNotNull(jme);
        assertTrue(jme instanceof InvalidTypeIdException);
        InvalidTypeIdException ex = (InvalidTypeIdException) jme;
        assertEquals(baseType, ex.getBaseType());
        assertEquals("myId", ex.getTypeId());
        assertTrue(ex.getMessage().contains("Could not resolve type id 'myId' as a subtype of"));
        assertTrue(ex.getMessage().contains("extra info"));
    }

    // Tests defaultSerializeValue and defaultSerializeField with null and non-null values
    @Test
    public void testDefaultSerializeValueAndField_nullAndNonNull_writesExpectedJson() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        gen.writeStartObject();
        _provider.defaultSerializeField("nullField", null, gen);
        _provider.defaultSerializeField("strField", "testValue", gen);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"nullField\":null,\"strField\":\"testValue\"}", sw.toString());

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeValue(null, gen);
        gen.close();
        assertEquals("null", sw.toString());

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeValue("hello", gen);
        gen.close();
        assertEquals("\"hello\"", sw.toString());
    }

    // Tests defaultSerializeDateValue and defaultSerializeDateKey as timestamp and text
    @Test
    public void testDefaultSerializeDateValueAndKey_timestampAndFormatted_writesCorrectJson() throws IOException {
        Date testDate = new Date(1500000000000L);

        // 1. As timestamp (default enabled)
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeDateValue(testDate, gen);
        gen.close();
        assertEquals("1500000000000", sw.toString());

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeDateValue(1500000000000L, gen);
        gen.close();
        assertEquals("1500000000000", sw.toString());

        // 2. Formatted date when timestamp feature is disabled
        _mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DefaultSerializerProvider dateProv = ((DefaultSerializerProvider.Impl) _provider)
                .createInstance(_mapper.getSerializationConfig(), _mapper.getSerializerFactory());

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        dateProv.defaultSerializeDateValue(testDate, gen);
        gen.close();
        assertTrue(sw.toString().contains("2017"));

        // 3. Date key as timestamp vs formatted
        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        _provider.defaultSerializeDateKey(testDate, gen);
        gen.writeString("value");
        _provider.defaultSerializeDateKey(1500000000000L, gen);
        gen.writeString("value2");
        gen.writeEndObject();
        gen.close();
        assertTrue(sw.toString().contains("1500000000000"));
    }

    // Tests defaultSerializeNull writes JSON null
    @Test
    public void testDefaultSerializeNull_writesNullToken() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeNull(gen);
        gen.close();
        assertEquals("null", sw.toString());
    }
}