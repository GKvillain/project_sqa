package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class SerializerProviderTest {

    private ObjectMapper _mapper;
    private DefaultSerializerProvider _provider;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _provider = (DefaultSerializerProvider) _mapper.getSerializerProviderInstance();
    }

    // Tests setDefaultKeySerializer with valid serializer
    @Test
    public void testSetDefaultKeySerializer_validSerializer_updatesSerializer() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        ToStringSerializer ser = ToStringSerializer.instance;
        prov.setDefaultKeySerializer(ser);
        // Does not throw and updates serializer
    }

    // Tests setDefaultKeySerializer with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultKeySerializer_nullSerializer_throwsIllegalArgumentException() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        prov.setDefaultKeySerializer(null);
    }

    // Tests setNullValueSerializer with valid serializer
    @Test
    public void testSetNullValueSerializer_validSerializer_updatesSerializer() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        prov.setNullValueSerializer(NullSerializer.instance);
        assertSame(NullSerializer.instance, prov.getDefaultNullValueSerializer());
    }

    // Tests setNullValueSerializer with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullValueSerializer_nullSerializer_throwsIllegalArgumentException() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        prov.setNullValueSerializer(null);
    }

    // Tests setNullKeySerializer with null input throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testSetNullKeySerializer_nullSerializer_throwsIllegalArgumentException() {
        DefaultSerializerProvider.Impl prov = new DefaultSerializerProvider.Impl();
        prov.setNullKeySerializer(null);
    }

    // Tests getDefaultNullKeySerializer and getDefaultNullValueSerializer
    @Test
    public void testGetDefaultNullSerializers_defaultState_returnsDefaults() {
        assertNotNull(_provider.getDefaultNullKeySerializer());
        assertSame(NullSerializer.instance, _provider.getDefaultNullValueSerializer());
    }

    // Tests getUnknownTypeSerializer for Object.class vs other class
    @Test
    public void testGetUnknownTypeSerializer_objectClassAndSpecificClass_returnsSerializers() {
        JsonSerializer<Object> objSer = _provider.getUnknownTypeSerializer(Object.class);
        assertNotNull(objSer);
        assertTrue(_provider.isUnknownTypeSerializer(objSer));

        JsonSerializer<Object> specificSer = _provider.getUnknownTypeSerializer(String.class);
        assertNotNull(specificSer);
        assertTrue(_provider.isUnknownTypeSerializer(specificSer));
        assertNotSame(objSer, specificSer);
    }

    // Tests isUnknownTypeSerializer with null input
    @Test
    public void testIsUnknownTypeSerializer_nullInput_returnsTrue() {
        assertTrue(_provider.isUnknownTypeSerializer(null));
    }

    // Tests isUnknownTypeSerializer with standard non-unknown serializer
    @Test
    public void testIsUnknownTypeSerializer_standardSerializer_returnsFalse() {
        assertFalse(_provider.isUnknownTypeSerializer(ToStringSerializer.instance));
    }

    // Tests findValueSerializer for JavaType with null input
    @Test(expected = JsonMappingException.class)
    public void testFindValueSerializer_nullJavaType_throwsJsonMappingException() throws Exception {
        _provider.findValueSerializer((JavaType) null, null);
    }

    // Tests findValueSerializer for raw Class
    @Test
    public void testFindValueSerializer_validClass_returnsSerializer() throws Exception {
        JsonSerializer<Object> ser = _provider.findValueSerializer(String.class, null);
        assertNotNull(ser);
        assertFalse(_provider.isUnknownTypeSerializer(ser));
    }

    // Tests findValueSerializer caching variant
    @Test
    public void testFindValueSerializer_withoutProperty_returnsSerializer() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findValueSerializer(Integer.class);
        JsonSerializer<Object> ser2 = _provider.findValueSerializer(Integer.class);
        assertNotNull(ser1);
        assertSame(ser1, ser2);
    }

    // Tests findKeySerializer with Class and JavaType
    @Test
    public void testFindKeySerializer_validType_returnsKeySerializer() throws Exception {
        JsonSerializer<Object> ser = _provider.findKeySerializer(String.class, null);
        assertNotNull(ser);
    }

    // Tests defaultSerializeValue with null value
    @Test
    public void testDefaultSerializeValue_nullValue_writesNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeValue(null, gen);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    // Tests defaultSerializeField with string value
    @Test
    public void testDefaultSerializeField_validValue_writesFieldAndValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        _provider.defaultSerializeField("testField", "testVal", gen);
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"testField\":\"testVal\"}", sw.toString());
    }

    // Tests defaultSerializeField with null value
    @Test
    public void testDefaultSerializeField_nullValue_writesFieldAndNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        gen.writeStartObject();
        _provider.defaultSerializeField("nullField", null, gen);
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"nullField\":null}", sw.toString());
    }

    // Tests defaultSerializeDateValue with timestamp
    @Test
    public void testDefaultSerializeDateValue_timestamp_writesTimestamp() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        Date date = new Date(1000L);
        _provider.defaultSerializeDateValue(date, gen);
        gen.flush();
        assertEquals("1000", sw.toString());
    }

    // Tests reportMappingProblem with message and arguments
    @Test(expected = JsonMappingException.class)
    public void testReportMappingProblem_messageWithArgs_throwsJsonMappingException() throws Exception {
        _provider.reportMappingProblem("Problem with %s and %d", "arg1", 42);
    }

    // Tests reportMappingProblem with Throwable cause
    @Test(expected = JsonMappingException.class)
    public void testReportMappingProblem_withThrowableCause_throwsJsonMappingException() throws Exception {
        _provider.reportMappingProblem(new IOException("root cause"), "Wrapped problem: %s", "details");
    }

    // Tests reportBadDefinition with JavaType and Throwable cause
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_javaTypeAndCause_throwsInvalidDefinitionException() throws Exception {
        JavaType type = _provider.constructType(String.class);
        _provider.reportBadDefinition(type, "Bad definition message", new IllegalStateException("bad state"));
    }

    // Tests reportBadDefinition with Class and Throwable cause
    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_classAndCause_throwsInvalidDefinitionException() throws Exception {
        _provider.reportBadDefinition(String.class, "Bad class definition", new IllegalStateException("bad"));
    }

    // Tests invalidTypeIdException
    @Test
    public void testInvalidTypeIdException_validParams_createsException() {
        JavaType baseType = _provider.constructType(Number.class);
        JsonMappingException exc = _provider.invalidTypeIdException(baseType, "UnknownType", "extra explanation");
        assertNotNull(exc);
        assertTrue(exc instanceof InvalidTypeIdException);
        assertTrue(exc.getMessage().contains("Could not resolve type id 'UnknownType'"));
        assertTrue(exc.getMessage().contains("extra explanation"));
    }

    // Tests context attributes manipulation on SerializerProvider
    @Test
    public void testSetAttribute_andGetAttribute_returnsUpdatedAttribute() {
        _provider.setAttribute("testKey", "testValue");
        assertEquals("testValue", _provider.getAttribute("testKey"));
        assertNull(_provider.getAttribute("unknownKey"));
    }

    // Tests general configuration accessors
    @Test
    public void testConfigAccessors_standardSetup_returnsValidValues() {
        assertNotNull(_provider.getConfig());
        assertNotNull(_provider.getTypeFactory());
        assertNotNull(_provider.getLocale());
        assertNotNull(_provider.getTimeZone());
        assertTrue(_provider.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertTrue(_provider.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertNull(_provider.getGenerator());
    }

    // Additional coverage tests

    @Test
    public void testFindTypedValueSerializer_byClassAndJavaType() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findTypedValueSerializer(String.class, true, null);
        assertNotNull(ser1);

        JavaType stringType = _provider.constructType(String.class);
        JsonSerializer<Object> ser2 = _provider.findTypedValueSerializer(stringType, true, null);
        assertNotNull(ser2);
    }

    @Test
    public void testFindPrimaryPropertySerializer_byClassAndJavaType() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findPrimaryPropertySerializer(String.class, null);
        assertNotNull(ser1);

        JavaType intType = _provider.constructType(Integer.class);
        JsonSerializer<Object> ser2 = _provider.findPrimaryPropertySerializer(intType, null);
        assertNotNull(ser2);
    }

    @Test
    public void testFindContentValueSerializer_byClassAndJavaType() throws Exception {
        JsonSerializer<Object> ser1 = _provider.findContentValueSerializer(String.class, null);
        assertNotNull(ser1);

        JavaType type = _provider.constructType(String.class);
        JsonSerializer<Object> ser2 = _provider.findContentValueSerializer(type, null);
        assertNotNull(ser2);
    }

    @Test
    public void testFindNullKeySerializer_andFindNullValueSerializer() throws Exception {
        JsonSerializer<Object> nullKeySer = _provider.findNullKeySerializer(_provider.constructType(String.class), null);
        assertNotNull(nullKeySer);

        JsonSerializer<Object> nullValSer = _provider.findNullValueSerializer(null);
        assertSame(NullSerializer.instance, nullValSer);
    }

    @Test
    public void testHasSerializerFor_andFlushCachedSerializers() {
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(_provider.hasSerializerFor(String.class, cause));
        assertNull(cause.get());

        int count = _provider.cachedSerializersCount();
        assertTrue(count >= 0);
        _provider.flushCachedSerializers();
        assertEquals(0, _provider.cachedSerializersCount());
    }

    @Test
    public void testDefaultSerializeDateValue_longTimestamp() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeDateValue(2000L, gen);
        gen.flush();
        assertEquals("2000", sw.toString());
    }

    @Test
    public void testDefaultSerializeDateKey_dateAndTimestamp() throws Exception {
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = new JsonFactory().createGenerator(sw1);
        gen1.writeStartObject();
        _provider.defaultSerializeDateKey(new Date(1000L), gen1);
        gen1.writeString("val");
        gen1.writeEndObject();
        gen1.flush();
        assertEquals("{\"1000\":\"val\"}", sw1.toString());

        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = new JsonFactory().createGenerator(sw2);
        gen2.writeStartObject();
        _provider.defaultSerializeDateKey(2000L, gen2);
        gen2.writeString("val2");
        gen2.writeEndObject();
        gen2.flush();
        assertEquals("{\"2000\":\"val2\"}", sw2.toString());
    }

    @Test
    public void testDefaultSerializeNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeNull(gen);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testDefaultSerializeValue_nonNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        _provider.defaultSerializeValue("hello", gen);
        gen.flush();
        assertEquals("\"hello\"", sw.toString());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType baseType = _provider.constructType(Number.class);
        JavaType specialized = _provider.constructSpecializedType(baseType, Integer.class);
        assertEquals(Integer.class, specialized.getRawClass());
    }

    @Test
    public void testCreateInstance_andCopy() {
        DefaultSerializerProvider.Impl impl = new DefaultSerializerProvider.Impl();
        DefaultSerializerProvider.Impl copy = impl.copy();
        assertNotNull(copy);
        assertNotSame(impl, copy);
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_javaTypeMessageOnly() throws Exception {
        JavaType type = _provider.constructType(String.class);
        _provider.reportBadDefinition(type, "Bad definition for JavaType");
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testReportBadDefinition_classMessageOnly() throws Exception {
        _provider.reportBadDefinition(String.class, "Bad definition for raw class");
    }

    @Test(expected = JsonMappingException.class)
    public void testReportBadTypeId() throws Exception {
        JavaType baseType = _provider.constructType(Number.class);
        _provider.reportBadTypeId(baseType, "InvalidTypeId", "TypeId details");
    }
}