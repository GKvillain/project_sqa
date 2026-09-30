package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

public class StdKeySerializersTest {

    private enum TestEnum {
        FIRST,
        SECOND {
            @Override
            public String toString() {
                return "custom_second";
            }
        }
    }

    private ObjectMapper mapper;
    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        jsonFactory = new JsonFactory();
    }

    // Tests getStdKeySerializer with null rawKeyType returning Dynamic serializer
    @Test
    public void testGetStdKeySerializer_nullType_returnsDynamicSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, null, true);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Dynamic);
    }

    // Tests getStdKeySerializer with Object.class returning Dynamic serializer
    @Test
    public void testGetStdKeySerializer_objectType_returnsDynamicSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Object.class, true);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Dynamic);
    }

    // Tests getStdKeySerializer with String.class returning StringKeySerializer
    @Test
    public void testGetStdKeySerializer_stringType_returnsStringSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, String.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.StringKeySerializer);
    }

    // Tests getStdKeySerializer with primitive and Number types returning DEFAULT_KEY_SERIALIZER
    @Test
    public void testGetStdKeySerializer_primitiveAndNumberTypes_returnsDefaultKeySerializer() {
        JsonSerializer<Object> intSer = StdKeySerializers.getStdKeySerializer(null, Integer.TYPE, false);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, intSer);

        JsonSerializer<Object> longSer = StdKeySerializers.getStdKeySerializer(null, Long.class, false);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, longSer);

        JsonSerializer<Object> doubleSer = StdKeySerializers.getStdKeySerializer(null, Double.class, false);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, doubleSer);
    }

    // Tests getStdKeySerializer with Class.class returning Default serializer
    @Test
    public void testGetStdKeySerializer_classType_returnsDefaultSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Class.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
    }

    // Tests getStdKeySerializer with Date and Calendar types
    @Test
    public void testGetStdKeySerializer_dateAndCalendarTypes_returnsDefaultSerializer() {
        JsonSerializer<Object> dateSer = StdKeySerializers.getStdKeySerializer(null, Date.class, false);
        assertNotNull(dateSer);
        assertTrue(dateSer instanceof StdKeySerializers.Default);

        JsonSerializer<Object> calSer = StdKeySerializers.getStdKeySerializer(null, Calendar.class, false);
        assertNotNull(calSer);
        assertTrue(calSer instanceof StdKeySerializers.Default);
    }

    // Tests getStdKeySerializer with UUID type
    @Test
    public void testGetStdKeySerializer_uuidType_returnsDefaultSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, UUID.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
    }

    // Tests getStdKeySerializer fallback when useDefault is true and false
    @Test
    public void testGetStdKeySerializer_unhandledType_returnsExpectedBasedOnUseDefault() {
        JsonSerializer<Object> serWithDefault = StdKeySerializers.getStdKeySerializer(null, Void.class, true);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, serWithDefault);

        JsonSerializer<Object> serWithoutDefault = StdKeySerializers.getStdKeySerializer(null, Void.class, false);
        assertNull(serWithoutDefault);
    }

    // Tests getFallbackKeySerializer with Enum.class returning Dynamic serializer
    @Test
    public void testGetFallbackKeySerializer_enumClass_returnsDynamicSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, Enum.class);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Dynamic);
    }

    // Tests getFallbackKeySerializer with concrete Enum types returning Default serializer
    @Test
    public void testGetFallbackKeySerializer_concreteEnumType_returnsDefaultSerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, TestEnum.class);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
    }

    // Tests getFallbackKeySerializer with null or unhandled types
    @Test
    public void testGetFallbackKeySerializer_nullAndUnhandledTypes_returnsDefaultKeySerializer() {
        JsonSerializer<Object> nullSer = StdKeySerializers.getFallbackKeySerializer(null, null);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, nullSer);

        JsonSerializer<Object> objSer = StdKeySerializers.getFallbackKeySerializer(null, Object.class);
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, objSer);
    }

    // Tests deprecated getDefault method
    @Test
    @SuppressWarnings("deprecation")
    public void testGetDefault_returnsDefaultKeySerializer() {
        assertSame(StdKeySerializers.DEFAULT_KEY_SERIALIZER, StdKeySerializers.getDefault());
    }

    // Tests serialization of Class key using Default serializer
    @Test
    public void testDefaultSerializer_serializeClassKey() throws IOException {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(String.class, "value");
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"java.lang.String\":\"value\"}", json);
    }

    // Tests serialization of UUID key using Default serializer
    @Test
    public void testDefaultSerializer_serializeUuidKey() throws IOException {
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        Map<UUID, String> map = new HashMap<UUID, String>();
        map.put(uuid, "value");
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"12345678-1234-1234-1234-123456789abc\":\"value\"}", json);
    }

    // Tests serialization of Date and Calendar keys using Default serializer
    @Test
    public void testDefaultSerializer_serializeDateAndCalendarKey() throws IOException {
        Map<Date, String> dateMap = new HashMap<Date, String>();
        Date date = new Date(0L);
        dateMap.put(date, "dateVal");
        String dateJson = mapper.writeValueAsString(dateMap);
        assertNotNull(dateJson);
        assertTrue(dateJson.contains("dateVal"));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0L);
        Map<Calendar, String> calMap = new HashMap<Calendar, String>();
        calMap.put(cal, "calVal");
        String calJson = mapper.writeValueAsString(calMap);
        assertNotNull(calJson);
        assertTrue(calJson.contains("calVal"));
    }

    // Tests serialization of Enum key with default name and WRITE_ENUMS_USING_TO_STRING
    @Test
    public void testDefaultSerializer_serializeEnumKey() throws IOException {
        Map<TestEnum, String> map = new HashMap<TestEnum, String>();
        map.put(TestEnum.SECOND, "val");

        String defaultJson = mapper.writeValueAsString(map);
        assertEquals("{\"SECOND\":\"val\"}", defaultJson);

        ObjectMapper toStringMapper = new ObjectMapper();
        toStringMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        String toStringJson = toStringMapper.writeValueAsString(map);
        assertEquals("{\"custom_second\":\"val\"}", toStringJson);
    }

    // Tests dynamic serialization with untyped EnumMap
    @Test
    public void testDynamicSerializer_serializeUntypedEnumMap() throws IOException {
        EnumMap<TestEnum, String> enumMap = new EnumMap<TestEnum, String>(TestEnum.class);
        enumMap.put(TestEnum.FIRST, "firstVal");
        String json = mapper.writeValueAsString(enumMap);
        assertEquals("{\"FIRST\":\"firstVal\"}", json);
    }

    // Tests StringKeySerializer direct serialization
    @Test
    public void testStringKeySerializer_serialize() throws IOException {
        StdKeySerializers.StringKeySerializer serializer = new StdKeySerializers.StringKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        g.writeStartObject();
        serializer.serialize("myKey", g, null);
        g.writeNumber(42);
        g.writeEndObject();
        g.close();
        assertEquals("{\"myKey\":42}", sw.toString());
    }

    // Tests Dynamic serializer readResolve initializes dynamic serializers map
    @Test
    public void testDynamicSerializer_readResolve() {
        StdKeySerializers.Dynamic dynamicSer = new StdKeySerializers.Dynamic();
        Object resolved = dynamicSer.readResolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof StdKeySerializers.Dynamic);
    }
}