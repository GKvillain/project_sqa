package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.EnumResolver;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdKeyDeserializerTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;

    enum TestEnum {
        ALPHA,
        BETA;
    }

    static class FailingConstructor {
        public FailingConstructor(String val) {
            throw new IllegalArgumentException("Constructor failure: " + val);
        }
    }

    static class FailingFactory {
        public static FailingFactory valueOf(String val) {
            throw new IllegalArgumentException("Factory failure: " + val);
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
    }

    // Tests factory method for standard String and Object types
    @Test
    public void testForType_stringAndObjectTypes_returnsStringKD() {
        StdKeyDeserializer kdString = StdKeyDeserializer.forType(String.class);
        assertNotNull(kdString);
        assertTrue(kdString instanceof StdKeyDeserializer.StringKD);
        assertEquals(String.class, kdString.getKeyClass());

        StdKeyDeserializer kdObject = StdKeyDeserializer.forType(Object.class);
        assertNotNull(kdObject);
        assertTrue(kdObject instanceof StdKeyDeserializer.StringKD);
        assertEquals(Object.class, kdObject.getKeyClass());
    }

    // Tests factory method for supported primitive and wrapper types
    @Test
    public void testForType_supportedStandardTypes_returnsNonNull() {
        Class<?>[] supportedTypes = new Class<?>[] {
            UUID.class, Integer.class, Long.class, java.util.Date.class,
            java.util.Calendar.class, Boolean.class, Byte.class, Character.class,
            Short.class, Float.class, Double.class, URI.class, URL.class,
            Class.class, Locale.class, Currency.class
        };

        for (Class<?> type : supportedTypes) {
            StdKeyDeserializer kd = StdKeyDeserializer.forType(type);
            assertNotNull("Deserializer should exist for " + type.getName(), kd);
            assertEquals(type, kd.getKeyClass());
        }
    }

    // Tests factory method for unsupported types returning null
    @Test
    public void testForType_unsupportedType_returnsNull() {
        assertNull(StdKeyDeserializer.forType(Void.class));
    }

    // Tests null key input returns null
    @Test
    public void testDeserializeKey_nullKey_returnsNull() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        assertNull(kd.deserializeKey(null, _context));
    }

    // Tests boolean key deserialization for true, false, and invalid values
    @Test
    public void testDeserializeKey_booleanKeys_returnsCorrectValues() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        assertEquals(Boolean.TRUE, kd.deserializeKey("true", _context));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", _context));
    }

    // Tests boolean invalid key exception handling
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidBooleanKey_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        kd.deserializeKey("not-a-boolean", _context);
    }

    // Tests byte key boundaries and unsigned byte range up to 255
    @Test
    public void testDeserializeKey_byteBoundaries_returnsCorrectValues() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", _context));
        assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", _context));
        assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", _context));
    }

    // Tests byte overflow exception handling
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_byteOverflow_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("256", _context);
    }

    // Tests short key boundaries
    @Test
    public void testDeserializeKey_shortBoundaries_returnsCorrectValues() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        assertEquals(Short.valueOf((short) -32768), kd.deserializeKey("-32768", _context));
        assertEquals(Short.valueOf((short) 32767), kd.deserializeKey("32767", _context));
    }

    // Tests short overflow exception handling
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_shortOverflow_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("32768", _context);
    }

    // Tests character key valid single char
    @Test
    public void testDeserializeKey_validCharacter_returnsChar() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        assertEquals(Character.valueOf('Z'), kd.deserializeKey("Z", _context));
    }

    // Tests character key invalid length exception handling
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidCharacterLength_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("multi", _context);
    }

    // Tests int and long parsing
    @Test
    public void testDeserializeKey_intAndLong_returnsCorrectValues() throws IOException {
        StdKeyDeserializer intKd = StdKeyDeserializer.forType(Integer.class);
        assertEquals(Integer.valueOf(12345), intKd.deserializeKey("12345", _context));

        StdKeyDeserializer longKd = StdKeyDeserializer.forType(Long.class);
        assertEquals(Long.valueOf(9876543210L), longKd.deserializeKey("9876543210", _context));
    }

    // Tests float and double parsing
    @Test
    public void testDeserializeKey_floatAndDouble_returnsCorrectValues() throws IOException {
        StdKeyDeserializer floatKd = StdKeyDeserializer.forType(Float.class);
        assertEquals(Float.valueOf(3.14f), (Float) floatKd.deserializeKey("3.14", _context), 0.0001f);

        StdKeyDeserializer doubleKd = StdKeyDeserializer.forType(Double.class);
        assertEquals(Double.valueOf(2.71828), (Double) doubleKd.deserializeKey("2.71828", _context), 0.000001d);
    }

    // Tests UUID key parsing
    @Test
    public void testDeserializeKey_validUUID_returnsUUID() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        String uuidStr = "e0c4b727-4a0b-47e2-be00-b6c860c28308";
        assertEquals(UUID.fromString(uuidStr), kd.deserializeKey(uuidStr, _context));
    }

    // Tests URI and URL parsing
    @Test
    public void testDeserializeKey_uriAndUrl_returnsCorrectObjects() throws IOException {
        StdKeyDeserializer uriKd = StdKeyDeserializer.forType(URI.class);
        assertEquals(URI.create("http://localhost:8080/path"), uriKd.deserializeKey("http://localhost:8080/path", _context));

        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        assertEquals(new URL("http://localhost:8080/path"), urlKd.deserializeKey("http://localhost:8080/path", _context));
    }

    // Tests Locale and Currency parsing via FromStringDeserializer
    @Test
    public void testDeserializeKey_localeAndCurrency_returnsCorrectObjects() throws IOException {
        StdKeyDeserializer localeKd = StdKeyDeserializer.forType(Locale.class);
        assertEquals(Locale.US, localeKd.deserializeKey("en_US", _context));

        StdKeyDeserializer currencyKd = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.getInstance("USD"), currencyKd.deserializeKey("USD", _context));
    }

    // Tests StringKD returns string key directly
    @Test
    public void testStringKD_deserializeKey_returnsOriginalKey() throws IOException {
        StdKeyDeserializer.StringKD kd = StdKeyDeserializer.StringKD.forType(String.class);
        assertEquals("sampleKey", kd.deserializeKey("sampleKey", _context));
        assertEquals(String.class, kd.getKeyClass());
    }

    // Tests single-string-constructor key deserializer
    @Test
    public void testStringCtorKeyDeserializer_instantiatesViaConstructor() throws Exception {
        Constructor<?> ctor = String.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        assertEquals("hello", kd.deserializeKey("hello", _context));
        assertEquals(String.class, kd.getKeyClass());
    }

    // Tests static factory method key deserializer
    @Test
    public void testStringFactoryKeyDeserializer_instantiatesViaFactoryMethod() throws Exception {
        Method factoryMethod = UUID.class.getMethod("fromString", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(factoryMethod);
        String uuidStr = "e0c4b727-4a0b-47e2-be00-b6c860c28308";
        assertEquals(UUID.fromString(uuidStr), kd.deserializeKey(uuidStr, _context));
        assertEquals(UUID.class, kd.getKeyClass());
    }

    // Tests primitive types lookup via forType mapping
    @Test
    public void testForType_primitiveTypes_returnsNonNullDeserializers() {
        Class<?>[] primitiveTypes = new Class<?>[] {
            boolean.class, byte.class, char.class, short.class,
            int.class, long.class, float.class, double.class
        };

        for (Class<?> type : primitiveTypes) {
            StdKeyDeserializer kd = StdKeyDeserializer.forType(type);
            assertNotNull("Deserializer should exist for primitive " + type.getName(), kd);
        }
    }

    // Tests Class type key deserializer with valid class name
    @Test
    public void testDeserializeKey_classType_returnsLoadedClass() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        assertEquals(String.class, kd.deserializeKey("java.lang.String", _context));
    }

    // Tests Class type key deserializer with invalid class name
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidClassName_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        kd.deserializeKey("com.nonexistent.NoSuchClass", _context);
    }

    // Tests Date and Calendar deserialization
    @Test
    public void testDeserializeKey_dateAndCalendar_returnsParsedObjects() throws IOException {
        StdKeyDeserializer dateKd = StdKeyDeserializer.forType(Date.class);
        Object parsedDate = dateKd.deserializeKey("2020-01-01T00:00:00.000+0000", _context);
        assertNotNull(parsedDate);
        assertTrue(parsedDate instanceof Date);

        StdKeyDeserializer calKd = StdKeyDeserializer.forType(Calendar.class);
        Object parsedCal = calKd.deserializeKey("2020-01-01T00:00:00.000+0000", _context);
        assertNotNull(parsedCal);
        assertTrue(parsedCal instanceof Calendar);
    }

    // Tests invalid int key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidInt_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        kd.deserializeKey("invalid_int", _context);
    }

    // Tests invalid long key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidLong_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        kd.deserializeKey("invalid_long", _context);
    }

    // Tests invalid float key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidFloat_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Float.class);
        kd.deserializeKey("invalid_float", _context);
    }

    // Tests invalid double key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidDouble_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Double.class);
        kd.deserializeKey("invalid_double", _context);
    }

    // Tests invalid URI key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidURI_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URI.class);
        kd.deserializeKey("http://invalid uri with spaces", _context);
    }

    // Tests invalid URL key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidURL_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URL.class);
        kd.deserializeKey("invalid_url_protocol", _context);
    }

    // Tests invalid Date key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidDate_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Date.class);
        kd.deserializeKey("not-a-valid-date", _context);
    }

    // Tests invalid UUID key parsing exception
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidUUID_throwsException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        kd.deserializeKey("invalid-uuid", _context);
    }

    // Tests DelegatingKD
    @Test
    public void testDelegatingKD_validAndNullHandling() throws Exception {
        JsonDeserializer<Integer> intDeser = new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
                return 42;
            }
        };
        StdKeyDeserializer.DelegatingKD delegatingKd = new StdKeyDeserializer.DelegatingKD(Integer.class, intDeser);
        assertEquals(Integer.class, delegatingKd.getKeyClass());
        assertNull(delegatingKd.deserializeKey(null, _context));
        assertEquals(42, delegatingKd.deserializeKey("anyKey", _context));
    }

    // Tests EnumKD deserialization
    @Test
    public void testEnumKD_validAndInvalidValues() throws Exception {
        EnumResolver<TestEnum> enumResolver = EnumResolver.constructUnsafe(TestEnum.class, _mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(enumResolver, null);
        assertEquals(TestEnum.class, enumKd.getKeyClass());

        assertEquals(TestEnum.ALPHA, enumKd.deserializeKey("ALPHA", _context));
        assertEquals(TestEnum.BETA, enumKd.deserializeKey("BETA", _context));
    }

    // Tests EnumKD invalid key exception
    @Test(expected = JsonMappingException.class)
    public void testEnumKD_invalidEnum_throwsException() throws Exception {
        EnumResolver<TestEnum> enumResolver = EnumResolver.constructUnsafe(TestEnum.class, _mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(enumResolver, null);
        enumKd.deserializeKey("GAMMA", _context);
    }

    // Tests StringCtorKeyDeserializer exception handling on construction failure
    @Test(expected = JsonMappingException.class)
    public void testStringCtorKeyDeserializer_throwingConstructor_throwsException() throws Exception {
        Constructor<?> ctor = FailingConstructor.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        kd.deserializeKey("fail", _context);
    }

    // Tests StringFactoryKeyDeserializer exception handling on factory failure
    @Test(expected = JsonMappingException.class)
    public void testStringFactoryKeyDeserializer_throwingFactory_throwsException() throws Exception {
        Method factoryMethod = FailingFactory.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(factoryMethod);
        kd.deserializeKey("fail", _context);
    }
}