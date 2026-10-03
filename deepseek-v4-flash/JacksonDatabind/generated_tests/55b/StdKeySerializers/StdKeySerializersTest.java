package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.util.Calendar;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.RawValue;

public class StdKeySerializersTest {

    private final ObjectMapper MAPPER = new ObjectMapper();
    private final SerializationConfig CONFIG = MAPPER.getSerializationConfig();

    // Test getStdKeySerializer with null key type -> returns Dynamic
    @Test
    public void testGetStdKeySerializer_nullKeyType_returnsDynamic() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, null, true);
        assertTrue("Should be Dynamic", ser instanceof StdKeySerializers.Dynamic);
    }

    // Test getStdKeySerializer with Object.class key type -> returns Dynamic
    @Test
    public void testGetStdKeySerializer_ObjectKeyType_returnsDynamic() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, Object.class, true);
        assertTrue("Should be Dynamic", ser instanceof StdKeySerializers.Dynamic);
    }

    // Test getStdKeySerializer with String key type -> returns StringKeySerializer
    @Test
    public void testGetStdKeySerializer_StringKeyType_returnsStringKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, String.class, true);
        assertTrue("Should be StringKeySerializer", ser instanceof StdKeySerializers.StringKeySerializer);
    }

    // Test getStdKeySerializer with primitive key type (int) -> returns default key serializer
    @Test
    public void testGetStdKeySerializer_primitiveType_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, int.class, true);
        assertSame("Should be DEFAULT_KEY_SERIALIZER", StdKeySerializers.getDefault(), ser);
    }

    // Test getStdKeySerializer with Number subtype (Integer) -> returns default key serializer
    @Test
    public void testGetStdKeySerializer_NumberType_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, Integer.class, true);
        assertSame("Should be DEFAULT_KEY_SERIALIZER", StdKeySerializers.getDefault(), ser);
    }

    // Test getStdKeySerializer with Class key type -> returns Default with TYPE_CLASS
    @Test
    public void testGetStdKeySerializer_ClassType_returnsDefaultWithTypeClass() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, Class.class, true);
        assertTrue("Should be Default", ser instanceof StdKeySerializers.Default);
    }

    // Test getStdKeySerializer with Date key type -> returns Default with TYPE_DATE
    @Test
    public void testGetStdKeySerializer_DateType_returnsDefaultWithTypeDate() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, Date.class, true);
        assertTrue("Should be Default", ser instanceof StdKeySerializers.Default);
    }

    // Test getStdKeySerializer with Calendar key type -> returns Default with TYPE_CALENDAR
    @Test
    public void testGetStdKeySerializer_CalendarType_returnsDefaultWithTypeCalendar() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, Calendar.class, true);
        assertTrue("Should be Default", ser instanceof StdKeySerializers.Default);
    }

    // Test getStdKeySerializer with UUID key type -> returns Default with TYPE_TO_STRING
    @Test
    public void testGetStdKeySerializer_UUIDType_returnsDefaultWithTypeToString() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, java.util.UUID.class, true);
        assertTrue("Should be Default", ser instanceof StdKeySerializers.Default);
    }

    // Test getStdKeySerializer with unknown type and useDefault=true -> returns default key serializer
    @Test
    public void testGetStdKeySerializer_unknownTypeUseDefaultTrue_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, getClass(), true);
        assertSame("Should be DEFAULT_KEY_SERIALIZER", StdKeySerializers.getDefault(), ser);
    }

    // Test getStdKeySerializer with unknown type and useDefault=false -> returns null
    @Test
    public void testGetStdKeySerializer_unknownTypeUseDefaultFalse_returnsNull() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(CONFIG, getClass(), false);
        assertNull("Should be null", ser);
    }

    // Test getFallbackKeySerializer with rawKeyType == Enum.class -> returns Dynamic
    @Test
    public void testGetFallbackKeySerializer_EnumClass_returnsDynamic() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(CONFIG, Enum.class);
        assertTrue("Should be Dynamic", ser instanceof StdKeySerializers.Dynamic);
    }

    // Test getFallbackKeySerializer with an enum type -> returns Default with TYPE_ENUM
    @Test
    public void testGetFallbackKeySerializer_enumType_returnsDefaultWithTypeEnum() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(CONFIG, java.util.concurrent.TimeUnit.class);
        assertTrue("Should be Default", ser instanceof StdKeySerializers.Default);
    }

    // Test getFallbackKeySerializer with non-enum type -> returns default key serializer
    @Test
    public void testGetFallbackKeySerializer_nonEnumType_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(CONFIG, String.class);
        assertSame("Should be DEFAULT_KEY_SERIALIZER", StdKeySerializers.getDefault(), ser);
    }

    // Test getFallbackKeySerializer with null key type -> returns default key serializer
    @Test
    public void testGetFallbackKeySerializer_nullType_returnsDefaultKeySerializer() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(CONFIG, null);
        assertSame("Should be DEFAULT_KEY_SERIALIZER", StdKeySerializers.getDefault(), ser);
    }

    // Test getDefault returns the same default key serializer instance
    @Test
    public void testGetDefault_returnsDefaultKeySerializer() {
        assertNotNull("Should not be null", StdKeySerializers.getDefault());
    }

    // Integration test: serializing a map with an Enum key should write the enum name by default
    @Test
    public void testDefaultSerialize_enumKey_writesName() throws Exception {
        Map<java.util.concurrent.TimeUnit, String> map = new HashMap<>();
        map.put(java.util.concurrent.TimeUnit.SECONDS, "value");
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"SECONDS\":\"value\"}", json);
    }

    // Integration test: serializing a map with a custom key type uses Dynamic serializer
    // and falls back to toString()
    @Test
    public void testDynamicSerialize_customKey_usesToString() throws Exception {
        class CustomKey {
            private final String name;
            CustomKey(String name) { this.name = name; }
            @Override
            public String toString() { return name; }
        }
        Map<CustomKey, String> map = new HashMap<>();
        CustomKey key = new CustomKey("myKey");
        map.put(key, "value");
        String json = MAPPER.writeValueAsString(map);
        assertEquals("{\"myKey\":\"value\"}", json);
    }

}