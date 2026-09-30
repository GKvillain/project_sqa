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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.util.EnumResolver;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdKeyDeserializerTest {

    private DeserializationContext _context;

    public static class SampleCtorKey {
        public final String value;
        public SampleCtorKey(String value) {
            if ("fail".equals(value)) {
                throw new IllegalArgumentException("Constructor failure");
            }
            this.value = value;
        }
    }

    public static class SampleFactoryKey {
        public final String value;
        private SampleFactoryKey(String value) {
            this.value = value;
        }
        public static SampleFactoryKey valueOf(String value) {
            if ("fail".equals(value)) {
                throw new IllegalArgumentException("Factory failure");
            }
            return new SampleFactoryKey(value);
        }
    }

    public enum TestEnum {
        ALPHA,
        BETA
    }

    @Before
    public void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        _context = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), null, null);
    }

    // Tests factory method for standard string types returning StringKD
    @Test
    public void testForType_stringTypes_returnsStringKD() {
        StdKeyDeserializer kdString = StdKeyDeserializer.forType(String.class);
        assertNotNull(kdString);
        assertTrue(kdString instanceof StdKeyDeserializer.StringKD);
        assertEquals(String.class, kdString.getKeyClass());

        StdKeyDeserializer kdObject = StdKeyDeserializer.forType(Object.class);
        assertNotNull(kdObject);
        assertTrue(kdObject instanceof StdKeyDeserializer.StringKD);

        StdKeyDeserializer kdCharSequence = StdKeyDeserializer.forType(CharSequence.class);
        assertNotNull(kdCharSequence);
        assertTrue(kdCharSequence instanceof StdKeyDeserializer.StringKD);
    }

    // Tests factory method for primitive wrapper and common standard types
    @Test
    public void testForType_standardTypes_returnsNonNullDeserializer() {
        assertNotNull(StdKeyDeserializer.forType(Integer.class));
        assertNotNull(StdKeyDeserializer.forType(Long.class));
        assertNotNull(StdKeyDeserializer.forType(Boolean.class));
        assertNotNull(StdKeyDeserializer.forType(Byte.class));
        assertNotNull(StdKeyDeserializer.forType(Short.class));
        assertNotNull(StdKeyDeserializer.forType(Character.class));
        assertNotNull(StdKeyDeserializer.forType(Float.class));
        assertNotNull(StdKeyDeserializer.forType(Double.class));
        assertNotNull(StdKeyDeserializer.forType(UUID.class));
        assertNotNull(StdKeyDeserializer.forType(URI.class));
        assertNotNull(StdKeyDeserializer.forType(URL.class));
        assertNotNull(StdKeyDeserializer.forType(Class.class));
        assertNotNull(StdKeyDeserializer.forType(Locale.class));
        assertNotNull(StdKeyDeserializer.forType(Currency.class));
        assertNotNull(StdKeyDeserializer.forType(byte[].class));
        assertNotNull(StdKeyDeserializer.forType(Date.class));
        assertNotNull(StdKeyDeserializer.forType(Calendar.class));
    }

    // Tests factory method for unsupported types returning null
    @Test
    public void testForType_unsupportedType_returnsNull() {
        assertNull(StdKeyDeserializer.forType(Void.class));
        assertNull(StdKeyDeserializer.forType(java.util.List.class));
    }

    // Tests null key input
    @Test
    public void testDeserializeKey_nullKey_returnsNull() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        assertNull(kd.deserializeKey(null, _context));
    }

    // Tests boolean key deserialization with valid values
    @Test
    public void testDeserializeKey_validBoolean_returnsBooleanObject() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        assertEquals(Boolean.TRUE, kd.deserializeKey("true", _context));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", _context));
    }

    // Tests byte key deserialization including boundary unsigned values
    @Test
    public void testDeserializeKey_validByte_returnsByteObject() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        assertEquals(Byte.valueOf((byte) 0), kd.deserializeKey("0", _context));
        assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", _context));
        assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", _context));
        assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", _context));
    }

    // Tests short key deserialization within valid range
    @Test
    public void testDeserializeKey_validShort_returnsShortObject() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        assertEquals(Short.valueOf((short) 0), kd.deserializeKey("0", _context));
        assertEquals(Short.valueOf((short) 32767), kd.deserializeKey("32767", _context));
        assertEquals(Short.valueOf((short) -32768), kd.deserializeKey("-32768", _context));
    }

    // Tests character key deserialization with single character
    @Test
    public void testDeserializeKey_singleChar_returnsCharacterObject() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        assertEquals(Character.valueOf('a'), kd.deserializeKey("a", _context));
        assertEquals(Character.valueOf('9'), kd.deserializeKey("9", _context));
    }

    // Tests integer and long key deserialization
    @Test
    public void testDeserializeKey_intAndLong_returnsNumberObjects() throws IOException {
        StdKeyDeserializer kdInt = StdKeyDeserializer.forType(Integer.class);
        assertEquals(Integer.valueOf(12345), kdInt.deserializeKey("12345", _context));
        assertEquals(Integer.valueOf(-999), kdInt.deserializeKey("-999", _context));

        StdKeyDeserializer kdLong = StdKeyDeserializer.forType(Long.class);
        assertEquals(Long.valueOf(1234567890123L), kdLong.deserializeKey("1234567890123", _context));
    }

    // Tests float and double key deserialization
    @Test
    public void testDeserializeKey_floatAndDouble_returnsFloatingPointObjects() throws IOException {
        StdKeyDeserializer kdFloat = StdKeyDeserializer.forType(Float.class);
        assertEquals(Float.valueOf(3.14f), (Float) kdFloat.deserializeKey("3.14", _context), 0.001f);

        StdKeyDeserializer kdDouble = StdKeyDeserializer.forType(Double.class);
        assertEquals(Double.valueOf(1.234567), (Double) kdDouble.deserializeKey("1.234567", _context), 0.000001);
    }

    // Tests UUID key deserialization
    @Test
    public void testDeserializeKey_validUUID_returnsUUIDObject() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        String uuidStr = "d4f3b610-1a28-4e89-a299-4d6d67b7e6ab";
        assertEquals(UUID.fromString(uuidStr), kd.deserializeKey(uuidStr, _context));
    }

    // Tests URI and URL key deserialization
    @Test
    public void testDeserializeKey_validUriAndUrl_returnsCorrespondingObjects() throws IOException {
        StdKeyDeserializer kdUri = StdKeyDeserializer.forType(URI.class);
        assertEquals(URI.create("http://localhost:8080/test"), kdUri.deserializeKey("http://localhost:8080/test", _context));

        StdKeyDeserializer kdUrl = StdKeyDeserializer.forType(URL.class);
        assertEquals(new URL("http://localhost:8080/test"), kdUrl.deserializeKey("http://localhost:8080/test", _context));
    }

    // Tests Locale and Currency key deserialization
    @Test
    public void testDeserializeKey_localeAndCurrency_returnsCorrectObjects() throws IOException {
        StdKeyDeserializer kdLocale = StdKeyDeserializer.forType(Locale.class);
        assertEquals(Locale.US, kdLocale.deserializeKey("en_US", _context));

        StdKeyDeserializer kdCurrency = StdKeyDeserializer.forType(Currency.class);
        assertEquals(Currency.getInstance("USD"), kdCurrency.deserializeKey("USD", _context));
    }

    // Tests Base64 byte array key deserialization
    @Test
    public void testDeserializeKey_base64ByteArray_returnsDecodedBytes() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(byte[].class);
        byte[] result = (byte[]) kd.deserializeKey("AQID", _context);
        assertNotNull(result);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    // Tests Class type key deserialization
    @Test
    public void testDeserializeKey_validClassName_returnsClass() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        assertEquals(String.class, kd.deserializeKey("java.lang.String", _context));
    }

    // Tests Date and Calendar key deserialization
    @Test
    public void testDeserializeKey_dateAndCalendar_returnsParsedDateAndCalendar() throws IOException {
        StdKeyDeserializer kdDate = StdKeyDeserializer.forType(Date.class);
        Object dateRes = kdDate.deserializeKey("1970-01-01T00:00:00.000+0000", _context);
        assertTrue(dateRes instanceof Date);

        StdKeyDeserializer kdCal = StdKeyDeserializer.forType(Calendar.class);
        Object calRes = kdCal.deserializeKey("1970-01-01T00:00:00.000+0000", _context);
        assertTrue(calRes instanceof Calendar);
    }

    // Tests StringKD direct deserialization
    @Test
    public void testStringKD_deserializeKey_returnsOriginalString() throws IOException {
        StdKeyDeserializer.StringKD kd = StdKeyDeserializer.StringKD.forType(String.class);
        assertEquals("testKey", kd.deserializeKey("testKey", _context));
    }

    // Tests StringCtorKeyDeserializer single-arg constructor invocation
    @Test
    public void testStringCtorKeyDeserializer_validString_constructsObject() throws Exception {
        Constructor<?> ctor = SampleCtorKey.class.getDeclaredConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        Object result = kd._parse("helloCtor", _context);
        assertNotNull(result);
        assertTrue(result instanceof SampleCtorKey);
        assertEquals("helloCtor", ((SampleCtorKey) result).value);
    }

    // Tests StringFactoryKeyDeserializer static factory method invocation
    @Test
    public void testStringFactoryKeyDeserializer_validString_invokesFactory() throws Exception {
        Method method = SampleFactoryKey.class.getDeclaredMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        Object result = kd._parse("helloFactory", _context);
        assertNotNull(result);
        assertTrue(result instanceof SampleFactoryKey);
        assertEquals("helloFactory", ((SampleFactoryKey) result).value);
    }

    // Tests invalid boolean format triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidBoolean_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        kd.deserializeKey("notABool", _context);
    }

    // Tests multi-character string for Character key triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_multiCharForChar_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("abc", _context);
    }

    // Tests byte overflow triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_overflowByte_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("300", _context);
    }

    // Tests short overflow triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_overflowShort_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("40000", _context);
    }

    // Tests getKeyClass method across different StdKeyDeserializer instances
    @Test
    public void testGetKeyClass_returnsCorrectClass() {
        assertEquals(Integer.class, StdKeyDeserializer.forType(Integer.class).getKeyClass());
        assertEquals(Long.class, StdKeyDeserializer.forType(Long.class).getKeyClass());
        assertEquals(Double.class, StdKeyDeserializer.forType(Double.class).getKeyClass());
        assertEquals(UUID.class, StdKeyDeserializer.forType(UUID.class).getKeyClass());
    }

    // Tests StringKD null deserialization
    @Test
    public void testStringKD_deserializeNull_returnsNull() throws IOException {
        StdKeyDeserializer.StringKD kd = StdKeyDeserializer.StringKD.forType(String.class);
        assertNull(kd.deserializeKey(null, _context));
    }

    // Tests invalid int triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidInt_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        kd.deserializeKey("invalid_int", _context);
    }

    // Tests invalid long triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidLong_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        kd.deserializeKey("invalid_long", _context);
    }

    // Tests invalid float triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidFloat_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Float.class);
        kd.deserializeKey("invalid_float", _context);
    }

    // Tests invalid double triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidDouble_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Double.class);
        kd.deserializeKey("invalid_double", _context);
    }

    // Tests invalid UUID triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidUUID_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        kd.deserializeKey("not-a-valid-uuid", _context);
    }

    // Tests invalid URL triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidURL_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URL.class);
        kd.deserializeKey("invalid_url_protocol://", _context);
    }

    // Tests invalid URI triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidURI_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URI.class);
        kd.deserializeKey("http:// invalid uri", _context);
    }

    // Tests invalid Class name triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidClass_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        kd.deserializeKey("com.nonexistent.Class12345", _context);
    }

    // Tests invalid Currency triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidCurrency_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Currency.class);
        kd.deserializeKey("INVALID_CURRENCY_CODE", _context);
    }

    // Tests invalid Date triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidDate_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Date.class);
        kd.deserializeKey("not-a-date", _context);
    }

    // Tests invalid Calendar triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidCalendar_throwsJsonMappingException() throws IOException {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Calendar.class);
        kd.deserializeKey("not-a-calendar-date", _context);
    }

    // Tests StringCtorKeyDeserializer exception handling during deserialization
    @Test(expected = JsonMappingException.class)
    public void testStringCtorKeyDeserializer_ctorThrows_throwsJsonMappingException() throws Exception {
        Constructor<?> ctor = SampleCtorKey.class.getDeclaredConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        kd.deserializeKey("fail", _context);
    }

    // Tests StringFactoryKeyDeserializer exception handling during deserialization
    @Test(expected = JsonMappingException.class)
    public void testStringFactoryKeyDeserializer_factoryThrows_throwsJsonMappingException() throws Exception {
        Method method = SampleFactoryKey.class.getDeclaredMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        kd.deserializeKey("fail", _context);
    }

    // Tests EnumKD deserialization
    @Test
    public void testEnumKD_validEnum_deserializesCorrectly() throws Exception {
        EnumResolver enumRes = EnumResolver.constructUnsafe(TestEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(enumRes, null);
        assertEquals(TestEnum.class, enumKd.getKeyClass());
        assertEquals(TestEnum.ALPHA, enumKd.deserializeKey("ALPHA", _context));
        assertEquals(TestEnum.BETA, enumKd.deserializeKey("BETA", _context));
        assertNull(enumKd.deserializeKey(null, _context));
    }

    // Tests EnumKD invalid value triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testEnumKD_invalidEnum_throwsJsonMappingException() throws Exception {
        EnumResolver enumRes = EnumResolver.constructUnsafe(TestEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(enumRes, null);
        enumKd.deserializeKey("NON_EXISTENT", _context);
    }

    // Tests DelegatingKD deserialization
    @Test
    public void testDelegatingKD_validInput_deserializesCorrectly() throws Exception {
        JsonDeserializer<?> deser = _context.findRootValueDeserializer(_context.constructType(Integer.class));
        StdKeyDeserializer.DelegatingKD delegatingKd = new StdKeyDeserializer.DelegatingKD(Integer.class, deser);
        assertEquals(Integer.class, delegatingKd.getKeyClass());
        assertNull(delegatingKd.deserializeKey(null, _context));
        assertEquals(Integer.valueOf(42), delegatingKd.deserializeKey("42", _context));
    }

    // Tests DelegatingKD invalid input triggering handleWeirdKey
    @Test(expected = JsonMappingException.class)
    public void testDelegatingKD_invalidInput_throwsJsonMappingException() throws Exception {
        JsonDeserializer<?> deser = _context.findRootValueDeserializer(_context.constructType(Integer.class));
        StdKeyDeserializer.DelegatingKD delegatingKd = new StdKeyDeserializer.DelegatingKD(Integer.class, deser);
        delegatingKd.deserializeKey("not_a_number", _context);
    }
}