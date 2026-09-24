package com.fasterxml.jackson.databind.ser.std;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.junit.Test;
import static org.junit.Assert.*;

public class StdKeySerializersTest {

    private final JsonFactory JSON_F = new JsonFactory();
    private final ObjectMapper MAPPER = new ObjectMapper();

    private enum TestEnum {
        FIRST,
        SECOND;

        @Override
        public String toString() {
            return "custom_" + name().toLowerCase();
        }
    }

    // Tests getStdKeySerializer with null and Object type returning Dynamic serializer
    @Test
    public void testGetStdKeySerializer_nullOrObjectType_returnsDynamic() {
        JsonSerializer<Object> serNull = StdKeySerializers.getStdKeySerializer(null, null, true);
        assertNotNull(serNull);
        assertTrue(serNull instanceof StdKeySerializers.Dynamic);

        JsonSerializer<Object> serObj = StdKeySerializers.getStdKeySerializer(null, Object.class, false);
        assertNotNull(serObj);
        assertTrue(serObj instanceof StdKeySerializers.Dynamic);
    }

    // Tests getStdKeySerializer with String type returning StringKeySerializer
    @Test
    public void testGetStdKeySerializer_stringType_returnsStringKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, String.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.StringKeySerializer);
    }

    // Tests getStdKeySerializer with primitive and Number types returning DEFAULT_KEY_SERIALIZER
    @Test
    public void testGetStdKeySerializer_primitiveAndNumberTypes_returnsDefaultKeySerializer() {
        JsonSerializer<Object> serIntPrim = StdKeySerializers.getStdKeySerializer(null, int.class, false);
        assertNotNull(serIntPrim);
        assertTrue(serIntPrim instanceof StdKeySerializer);

        JsonSerializer<Object> serLongObj = StdKeySerializers.getStdKeySerializer(null, Long.class, false);
        assertNotNull(serLongObj);
        assertTrue(serLongObj instanceof StdKeySerializer);

        JsonSerializer<Object> serDouble = StdKeySerializers.getStdKeySerializer(null, Double.class, false);
        assertNotNull(serDouble);
        assertTrue(serDouble instanceof StdKeySerializer);
    }

    // Tests getStdKeySerializer with Class, Date, Calendar, and UUID types
    @Test
    public void testGetStdKeySerializer_standardJdkTypes_returnsDefaultSerializer() {
        JsonSerializer<Object> serClass = StdKeySerializers.getStdKeySerializer(null, Class.class, false);
        assertNotNull(serClass);
        assertTrue(serClass instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serDate = StdKeySerializers.getStdKeySerializer(null, Date.class, false);
        assertNotNull(serDate);
        assertTrue(serDate instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serCalendar = StdKeySerializers.getStdKeySerializer(null, GregorianCalendar.class, false);
        assertNotNull(serCalendar);
        assertTrue(serCalendar instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serUUID = StdKeySerializers.getStdKeySerializer(null, UUID.class, false);
        assertNotNull(serUUID);
        assertTrue(serUUID instanceof StdKeySerializers.Default);
    }

    // Tests getStdKeySerializer with unhandled type and useDefault flag
    @Test
    public void testGetStdKeySerializer_unhandledType_respectsUseDefault() {
        JsonSerializer<Object> serWithDefault = StdKeySerializers.getStdKeySerializer(null, StringBuffer.class, true);
        assertNotNull(serWithDefault);
        assertTrue(serWithDefault instanceof StdKeySerializer);

        JsonSerializer<Object> serWithoutDefault = StdKeySerializers.getStdKeySerializer(null, StringBuffer.class, false);
        assertNull(serWithoutDefault);
    }

    // Tests getFallbackKeySerializer for null, generic Enum, concrete Enum, and other types
    @Test
    public void testGetFallbackKeySerializer_variousTypes_returnsExpectedSerializer() {
        JsonSerializer<Object> serNull = StdKeySerializers.getFallbackKeySerializer(null, null);
        assertNotNull(serNull);
        assertTrue(serNull instanceof StdKeySerializer);

        JsonSerializer<Object> serGenericEnum = StdKeySerializers.getFallbackKeySerializer(null, Enum.class);
        assertNotNull(serGenericEnum);
        assertTrue(serGenericEnum instanceof StdKeySerializers.Dynamic);

        JsonSerializer<Object> serConcreteEnum = StdKeySerializers.getFallbackKeySerializer(null, TestEnum.class);
        assertNotNull(serConcreteEnum);
        assertTrue(serConcreteEnum instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serOther = StdKeySerializers.getFallbackKeySerializer(null, StringBuffer.class);
        assertNotNull(serOther);
        assertTrue(serOther instanceof StdKeySerializer);
    }

    // Tests deprecated getDefault method
    @Test
    public void testGetDefault_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getDefault();
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializer);
    }

    // Tests StringKeySerializer serialization
    @Test
    public void testStringKeySerializer_serialize_writesFieldName() throws Exception {
        StdKeySerializers.StringKeySerializer ser = new StdKeySerializers.StringKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.writeStartObject();

        ser.serialize("testKey", gen, MAPPER.getSerializerProviderInstance());

        gen.writeString("testValue");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"testKey\":\"testValue\"}", sw.toString());
    }

    // Tests Default serializer for Class type
    @Test
    public void testDefaultSerializer_classType_writesClassName() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_CLASS, Class.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.writeStartObject();

        ser.serialize(String.class, gen, MAPPER.getSerializerProviderInstance());

        gen.writeString("val");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"java.lang.String\":\"val\"}", sw.toString());
    }

    // Tests Default serializer for UUID type and default toString fallback
    @Test
    public void testDefaultSerializer_toStringType_writesToStringValue() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_TO_STRING, UUID.class);
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.writeStartObject();

        ser.serialize(uuid, gen, MAPPER.getSerializerProviderInstance());

        gen.writeString("val");
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"123e4567-e89b-12d3-a456-426614174000\":\"val\"}", sw.toString());
    }

    // Tests Default serializer for Enum type with name() and toString() serialization features
    @Test
    public void testDefaultSerializer_enumType_respectsSerializationFeature() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_ENUM, TestEnum.class);

        // Feature disabled -> use Enum.name()
        ObjectMapper mapperWithoutToString = new ObjectMapper();
        mapperWithoutToString.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = JSON_F.createGenerator(sw1);
        gen1.writeStartObject();
        ser.serialize(TestEnum.FIRST, gen1, mapperWithoutToString.getSerializerProviderInstance());
        gen1.writeString("val");
        gen1.writeEndObject();
        gen1.close();
        assertEquals("{\"FIRST\":\"val\"}", sw1.toString());

        // Feature enabled -> use Enum.toString()
        ObjectMapper mapperWithToString = new ObjectMapper();
        mapperWithToString.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = JSON_F.createGenerator(sw2);
        gen2.writeStartObject();
        ser.serialize(TestEnum.FIRST, gen2, mapperWithToString.getSerializerProviderInstance());
        gen2.writeString("val");
        gen2.writeEndObject();
        gen2.close();
        assertEquals("{\"custom_first\":\"val\"}", sw2.toString());
    }

    // Tests Default serializer for Date and Calendar types
    @Test
    public void testDefaultSerializer_dateAndCalendarTypes_serializesCorrectly() throws Exception {
        Date date = new Date(1451606400000L);
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1451606400000L);

        Map<Date, String> dateMap = new HashMap<Date, String>();
        dateMap.put(date, "dateVal");
        String dateJson = MAPPER.writeValueAsString(dateMap);
        assertTrue(dateJson.contains("1451606400000") || dateJson.contains("2016"));

        Map<Calendar, String> calMap = new HashMap<Calendar, String>();
        calMap.put(calendar, "calVal");
        String calJson = MAPPER.writeValueAsString(calMap);
        assertTrue(calJson.contains("1451606400000") || calJson.contains("2016"));
    }

    // Tests Dynamic key serializer resolving multiple distinct key types dynamically
    @Test
    public void testDynamicKeySerializer_multipleKeyTypes_serializesDynamically() throws Exception {
        StdKeySerializers.Dynamic dynamicSer = new StdKeySerializers.Dynamic();
        SerializerProvider prov = MAPPER.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = JSON_F.createGenerator(sw);
        gen.writeStartObject();

        dynamicSer.serialize("stringKey", gen, prov);
        gen.writeNumber(1);

        dynamicSer.serialize(Integer.valueOf(123), gen, prov);
        gen.writeNumber(2);

        dynamicSer.serialize("anotherString", gen, prov);
        gen.writeNumber(3);

        gen.writeEndObject();
        gen.close();

        assertEquals("{\"stringKey\":1,\"123\":2,\"anotherString\":3}", sw.toString());
    }

    // Tests Dynamic serializer readResolve deserialization method
    @Test
    public void testDynamicKeySerializer_readResolve_restoresState() throws Exception {
        StdKeySerializers.Dynamic dynamicSer = new StdKeySerializers.Dynamic();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dynamicSer);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof StdKeySerializers.Dynamic);
    }
}