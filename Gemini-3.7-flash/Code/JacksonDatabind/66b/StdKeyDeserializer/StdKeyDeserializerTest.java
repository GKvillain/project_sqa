package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.util.EnumResolver;
import org.junit.Before;
import org.junit.Test;

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

import static org.junit.Assert.*;

public class StdKeyDeserializerTest {

    private ObjectMapper _mapper;
    private DeserializationContext _context;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        DefaultDeserializationContext defaultContext = (DefaultDeserializationContext) _mapper.getDeserializationContext();
        _context = defaultContext.createInstance(_mapper.getDeserializationConfig(),
                _mapper.getFactory().createParser("{}"), null);
    }

    private DeserializationContext createContextWithFeature(DeserializationFeature feature, boolean state) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(feature, state);
        DefaultDeserializationContext defaultContext = (DefaultDeserializationContext) mapper.getDeserializationContext();
        return defaultContext.createInstance(mapper.getDeserializationConfig(),
                mapper.getFactory().createParser("{}"), null);
    }

    // Tests String and Object factory mappings and StringKD behavior
    @Test
    public void testForType_stringAndObject_returnsStringKD() throws Exception {
        StdKeyDeserializer strKd = StdKeyDeserializer.forType(String.class);
        assertNotNull(strKd);
        assertEquals(String.class, strKd.getKeyClass());
        assertEquals("hello", strKd.deserializeKey("hello", _context));
        assertNull(strKd.deserializeKey(null, _context));

        StdKeyDeserializer objKd = StdKeyDeserializer.forType(Object.class);
        assertNotNull(objKd);
        assertEquals(Object.class, objKd.getKeyClass());
        assertEquals("world", objKd.deserializeKey("world", _context));
    }

    // Tests UUID key deserialization
    @Test
    public void testForType_uuid_returnsUuid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        assertNotNull(kd);
        assertEquals(UUID.class, kd.getKeyClass());
        UUID expected = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        Object result = kd.deserializeKey("12345678-1234-1234-1234-123456789abc", _context);
        assertEquals(expected, result);
    }

    // Tests Integer and Long key deserialization
    @Test
    public void testForType_integerAndLong_returnsCorrectNumbers() throws Exception {
        StdKeyDeserializer intKd = StdKeyDeserializer.forType(Integer.class);
        assertNotNull(intKd);
        assertEquals(Integer.valueOf(12345), intKd.deserializeKey("12345", _context));
        assertEquals(Integer.valueOf(-99), intKd.deserializeKey("-99", _context));

        StdKeyDeserializer longKd = StdKeyDeserializer.forType(Long.class);
        assertNotNull(longKd);
        assertEquals(Long.valueOf(1234567890123L), longKd.deserializeKey("1234567890123", _context));
    }

    // Tests Boolean key deserialization paths
    @Test
    public void testForType_boolean_returnsExpectedValues() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        assertNotNull(kd);
        assertEquals(Boolean.TRUE, kd.deserializeKey("true", _context));
        assertEquals(Boolean.FALSE, kd.deserializeKey("false", _context));
    }

    // Tests invalid Boolean key handling
    @Test(expected = JsonMappingException.class)
    public void testDeserializeKey_invalidBoolean_throwsException() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        kd.deserializeKey("not-bool", _context);
    }

    // Tests Byte key boundaries including unsigned range support up to 255
    @Test
    public void testForType_byte_handlesValidAndBoundaryValues() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        assertNotNull(kd);
        assertEquals(Byte.valueOf((byte) 0), kd.deserializeKey("0", _context));
        assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", _context));
        assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", _context));
        assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", _context));
    }

    // Tests Byte overflow
    @Test(expected = JsonMappingException.class)
    public void testForType_byteOverflow_throwsException() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("256", _context);
    }

    // Tests Short key deserialization and boundaries
    @Test
    public void testForType_short_handlesValidValues() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        assertNotNull(kd);
        assertEquals(Short.valueOf((short) 32767), kd.deserializeKey("32767", _context));
        assertEquals(Short.valueOf((short) -32768), kd.deserializeKey("-32768", _context));
    }

    // Tests Short overflow
    @Test(expected = JsonMappingException.class)
    public void testForType_shortOverflow_throwsException() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("32768", _context);
    }

    // Tests Character key deserialization and length checks
    @Test
    public void testForType_char_handlesSingleChar() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        assertNotNull(kd);
        assertEquals(Character.valueOf('A'), kd.deserializeKey("A", _context));
    }

    // Tests invalid multi-char Character key
    @Test(expected = JsonMappingException.class)
    public void testForType_charMultiChar_throwsException() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("AB", _context);
    }

    // Tests Float and Double key deserialization
    @Test
    public void testForType_floatAndDouble_deserializesCorrectly() throws Exception {
        StdKeyDeserializer floatKd = StdKeyDeserializer.forType(Float.class);
        assertNotNull(floatKd);
        assertEquals(Float.valueOf(1.25f), floatKd.deserializeKey("1.25", _context));

        StdKeyDeserializer doubleKd = StdKeyDeserializer.forType(Double.class);
        assertNotNull(doubleKd);
        assertEquals(Double.valueOf(123.456), doubleKd.deserializeKey("123.456", _context));
    }

    // Tests URI and URL key deserialization
    @Test
    public void testForType_uriAndUrl_deserializesCorrectly() throws Exception {
        StdKeyDeserializer uriKd = StdKeyDeserializer.forType(URI.class);
        assertNotNull(uriKd);
        assertEquals(URI.create("http://localhost:8080/path"), uriKd.deserializeKey("http://localhost:8080/path", _context));

        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        assertNotNull(urlKd);
        assertEquals(new URL("http://localhost:8080/path"), urlKd.deserializeKey("http://localhost:8080/path", _context));
    }

    // Tests Date and Calendar key deserialization
    @Test
    public void testForType_dateAndCalendar_deserializesCorrectly() throws Exception {
        StdKeyDeserializer dateKd = StdKeyDeserializer.forType(Date.class);
        assertNotNull(dateKd);
        Object dateResult = dateKd.deserializeKey("1970-01-01T00:00:00.000+0000", _context);
        assertTrue(dateResult instanceof Date);

        StdKeyDeserializer calKd = StdKeyDeserializer.forType(Calendar.class);
        assertNotNull(calKd);
        Object calResult = calKd.deserializeKey("1970-01-01T00:00:00.000+0000", _context);
        assertTrue(calResult instanceof Calendar);
    }

    // Tests Locale, Currency and Class key deserialization
    @Test
    public void testForType_localeCurrencyAndClass_deserializesCorrectly() throws Exception {
        StdKeyDeserializer locKd = StdKeyDeserializer.forType(Locale.class);
        assertNotNull(locKd);
        assertEquals(Locale.US, locKd.deserializeKey("en_US", _context));

        StdKeyDeserializer curKd = StdKeyDeserializer.forType(Currency.class);
        assertNotNull(curKd);
        assertEquals(Currency.getInstance("USD"), curKd.deserializeKey("USD", _context));

        StdKeyDeserializer clsKd = StdKeyDeserializer.forType(Class.class);
        assertNotNull(clsKd);
        assertEquals(String.class, clsKd.deserializeKey("java.lang.String", _context));
    }

    // Tests unsupported type returns null from factory
    @Test
    public void testForType_unsupportedType_returnsNull() {
        assertNull(StdKeyDeserializer.forType(Void.class));
        assertNull(StdKeyDeserializer.forType(Object[].class));
    }

    // Tests null key returns null across deserializers
    @Test
    public void testDeserializeKey_nullKey_returnsNull() throws Exception {
        StdKeyDeserializer intKd = StdKeyDeserializer.forType(Integer.class);
        assertNull(intKd.deserializeKey(null, _context));
    }

    // Tests StringCtorKeyDeserializer instantiation and parsing
    @Test
    public void testStringCtorKeyDeserializer_customConstructor_instantiates() throws Exception {
        Constructor<?> ctor = StringCtorKey.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        Object result = kd.deserializeKey("myKey", _context);
        assertTrue(result instanceof StringCtorKey);
        assertEquals("myKey", ((StringCtorKey) result).getValue());
    }

    // Tests StringFactoryKeyDeserializer instantiation and parsing
    @Test
    public void testStringFactoryKeyDeserializer_customFactoryMethod_instantiates() throws Exception {
        Method method = StringFactoryKey.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        Object result = kd.deserializeKey("factoryKey", _context);
        assertTrue(result instanceof StringFactoryKey);
        assertEquals("factoryKey", ((StringFactoryKey) result).getValue());
    }

    // Tests EnumKD by-name resolution and unknown value handling
    @Test
    public void testEnumKD_standardAndUnknownValues() throws Exception {
        EnumResolver res = EnumResolver.constructUnsafe(TestEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(res, null);

        Object val = kd.deserializeKey("ALPHA", _context);
        assertEquals(TestEnum.ALPHA, val);

        DeserializationContext unknownNullCtx = createContextWithFeature(
                DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        Object nullVal = kd.deserializeKey("UNKNOWN_KEY", unknownNullCtx);
        assertNull(nullVal);
    }

    // Tests EnumKD resolution using toString
    @Test
    public void testEnumKD_readEnumsUsingToString() throws Exception {
        EnumResolver res = EnumResolver.constructUnsafe(TestEnum.class, _context.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(res, null);

        DeserializationContext toStringCtx = createContextWithFeature(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        Object val = kd.deserializeKey("alpha_custom", toStringCtx);
        assertEquals(TestEnum.ALPHA, val);
    }

    public static class StringCtorKey {
        private final String _value;

        public StringCtorKey(String value) {
            _value = value;
        }

        public String getValue() {
            return _value;
        }
    }

    public static class StringFactoryKey {
        private final String _value;

        private StringFactoryKey(String value) {
            _value = value;
        }

        public static StringFactoryKey valueOf(String value) {
            return new StringFactoryKey(value);
        }

        public String getValue() {
            return _value;
        }
    }

    public enum TestEnum {
        ALPHA,
        BETA;

        @Override
        public String toString() {
            return name().toLowerCase() + "_custom";
        }
    }
}