package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StdDeserializerTest {

    private ObjectMapper _mapper;
    private TestStdDeser _deser;

    private static class TestStdDeser extends StdDeserializer<Object> {
        private static final long serialVersionUID = 1L;

        protected TestStdDeser(Class<?> vc) {
            super(vc);
        }

        protected TestStdDeser(JavaType vt) {
            super(vt);
        }

        protected TestStdDeser(TestStdDeser src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _deser = new TestStdDeser(Integer.class);
    }

    private DeserializationContext createContext(ObjectMapper mapper) {
        return mapper.getDeserializationContext();
    }

    // Tests constructors and type accessors
    @Test
    public void testConstructorsAndAccessors_validInputs_returnsExpected() {
        TestStdDeser deserCls = new TestStdDeser(String.class);
        assertEquals(String.class, deserCls.handledType());
        assertEquals(String.class, deserCls.getValueClass());
        assertNull(deserCls.getValueType());

        JavaType type = TypeFactory.defaultInstance().constructType(Double.class);
        TestStdDeser deserType = new TestStdDeser(type);
        assertEquals(Double.class, deserType.handledType());

        TestStdDeser deserNullType = new TestStdDeser((JavaType) null);
        assertEquals(Object.class, deserNullType.handledType());

        TestStdDeser copyDeser = new TestStdDeser(deserCls);
        assertEquals(String.class, copyDeser.handledType());
    }

    // Tests _isIntNumber helper with various numeric and non-numeric strings
    @Test
    public void testIsIntNumber_variousInputs_returnsExpectedBoolean() {
        assertTrue(_deser._isIntNumber("12345"));
        assertTrue(_deser._isIntNumber("+123"));
        assertTrue(_deser._isIntNumber("-456"));
        assertTrue(_deser._isIntNumber("0"));
        assertFalse(_deser._isIntNumber(""));
        assertFalse(_deser._isIntNumber("12a34"));
        assertFalse(_deser._isIntNumber("+"));
        assertFalse(_deser._isIntNumber("-"));
        assertFalse(_deser._isIntNumber(" 123"));
    }

    // Tests overflow helper methods for byte, short, and int
    @Test
    public void testOverflowHelpers_boundaryValues_detectsOverflow() {
        assertFalse(_deser._byteOverflow(-128));
        assertFalse(_deser._byteOverflow(255));
        assertFalse(_deser._byteOverflow(0));
        assertTrue(_deser._byteOverflow(-129));
        assertTrue(_deser._byteOverflow(256));

        assertFalse(_deser._shortOverflow(Short.MIN_VALUE));
        assertFalse(_deser._shortOverflow(Short.MAX_VALUE));
        assertTrue(_deser._shortOverflow((int) Short.MIN_VALUE - 1));
        assertTrue(_deser._shortOverflow((int) Short.MAX_VALUE + 1));

        assertFalse(_deser._intOverflow((long) Integer.MIN_VALUE));
        assertFalse(_deser._intOverflow((long) Integer.MAX_VALUE));
        assertTrue(_deser._intOverflow((long) Integer.MIN_VALUE - 1L));
        assertTrue(_deser._intOverflow((long) Integer.MAX_VALUE + 1L));
    }

    // Tests _neitherNull helper method
    @Test
    public void testNeitherNull_variousCombinations_returnsExpected() {
        assertTrue(StdDeserializer._neitherNull("a", "b"));
        assertFalse(StdDeserializer._neitherNull(null, "b"));
        assertFalse(StdDeserializer._neitherNull("a", null));
        assertFalse(StdDeserializer._neitherNull(null, null));
    }

    // Tests infinity and NaN string detection helpers
    @Test
    public void testSpecialFloatStringDetection_variousStrings_returnsExpected() {
        assertTrue(_deser._isPosInf("Infinity"));
        assertTrue(_deser._isPosInf("INF"));
        assertFalse(_deser._isPosInf("inf"));

        assertTrue(_deser._isNegInf("-Infinity"));
        assertTrue(_deser._isNegInf("-INF"));
        assertFalse(_deser._isNegInf("-inf"));

        assertTrue(_deser._isNaN("NaN"));
        assertFalse(_deser._isNaN("nan"));
    }

    // Tests _nonNullNumber fallback
    @Test
    public void testNonNullNumber_nullAndNonNull_returnsExpected() {
        assertEquals(0, _deser._nonNullNumber(null).intValue());
        assertEquals(42, _deser._nonNullNumber(42).intValue());
    }

    // Tests _hasTextualNull and _isEmptyOrTextualNull helpers
    @Test
    public void testTextualNullChecks_variousInputs_returnsExpected() {
        assertTrue(_deser._hasTextualNull("null"));
        assertFalse(_deser._hasTextualNull("NULL"));
        assertFalse(_deser._hasTextualNull(""));

        assertTrue(_deser._isEmptyOrTextualNull(""));
        assertTrue(_deser._isEmptyOrTextualNull("null"));
        assertFalse(_deser._isEmptyOrTextualNull("foo"));
    }

    // Tests parseDouble helper for nasty double edge cases and standard numbers
    @Test
    public void testParseDouble_nastyAndStandard_returnsCorrectDouble() {
        assertEquals(Double.MIN_NORMAL, StdDeserializer.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.0);
        assertEquals(3.14159, StdDeserializer.parseDouble("3.14159"), 0.00001);
        assertEquals(0.0, StdDeserializer.parseDouble("0"), 0.0);
    }

    // Tests _parseBooleanPrimitive with boolean tokens, int tokens, and aliases
    @Test
    public void testParseBooleanPrimitive_validTokens_parsesCorrectly() throws Exception {
        JsonParser pTrue = _mapper.createParser("true");
        pTrue.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        assertTrue(_deser._parseBooleanPrimitive(pTrue, ctxt));

        JsonParser pFalse = _mapper.createParser("false");
        pFalse.nextToken();
        assertFalse(_deser._parseBooleanPrimitive(pFalse, ctxt));

        JsonParser pIntOne = _mapper.createParser("1");
        pIntOne.nextToken();
        assertTrue(_deser._parseBooleanPrimitive(pIntOne, ctxt));

        JsonParser pIntZero = _mapper.createParser("0");
        pIntZero.nextToken();
        assertFalse(_deser._parseBooleanPrimitive(pIntZero, ctxt));

        JsonParser pStrTrue = _mapper.createParser("\"True\"");
        pStrTrue.nextToken();
        assertTrue(_deser._parseBooleanPrimitive(pStrTrue, ctxt));

        JsonParser pStrFalse = _mapper.createParser("\"False\"");
        pStrFalse.nextToken();
        assertFalse(_deser._parseBooleanPrimitive(pStrFalse, ctxt));
    }

    // Tests _parseIntPrimitive and _parseLongPrimitive from int and string tokens
    @Test
    public void testParseIntAndLongPrimitive_validTokens_returnsCorrectValues() throws Exception {
        JsonParser pInt = _mapper.createParser("12345");
        pInt.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        assertEquals(12345, _deser._parseIntPrimitive(pInt, ctxt));

        JsonParser pStr = _mapper.createParser("\"987654321\"");
        pStr.nextToken();
        assertEquals(987654321, _deser._parseIntPrimitive(pStr, ctxt));

        JsonParser pLong = _mapper.createParser("9876543210123");
        pLong.nextToken();
        assertEquals(9876543210123L, _deser._parseLongPrimitive(pLong, ctxt));

        JsonParser pStrLong = _mapper.createParser("\"9876543210123\"");
        pStrLong.nextToken();
        assertEquals(9876543210123L, _deser._parseLongPrimitive(pStrLong, ctxt));
    }

    // Tests _parseBytePrimitive and _parseShortPrimitive with valid values
    @Test
    public void testParseByteAndShortPrimitive_validValues_returnsPrimitives() throws Exception {
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonParser pByte = _mapper.createParser("120");
        pByte.nextToken();
        assertEquals((byte) 120, _deser._parseBytePrimitive(pByte, ctxt));

        JsonParser pShort = _mapper.createParser("32000");
        pShort.nextToken();
        assertEquals((short) 32000, _deser._parseShortPrimitive(pShort, ctxt));
    }

    // Tests _parseFloatPrimitive and _parseDoublePrimitive with special strings
    @Test
    public void testParseFloatAndDoublePrimitive_specialStrings_returnsExpected() throws Exception {
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        assertEquals(Float.POSITIVE_INFINITY, _deser._parseFloatPrimitive(ctxt, "Infinity"), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, _deser._parseFloatPrimitive(ctxt, "-Infinity"), 0.0f);
        assertTrue(Float.isNaN(_deser._parseFloatPrimitive(ctxt, "NaN")));
        assertEquals(12.5f, _deser._parseFloatPrimitive(ctxt, "12.5"), 0.001f);

        assertEquals(Double.POSITIVE_INFINITY, _deser._parseDoublePrimitive(ctxt, "Infinity"), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, _deser._parseDoublePrimitive(ctxt, "-Infinity"), 0.0);
        assertTrue(Double.isNaN(_deser._parseDoublePrimitive(ctxt, "NaN")));
        assertEquals(123.456, _deser._parseDoublePrimitive(ctxt, "123.456"), 0.0001);
    }

    // Tests _parseDate with long timestamp, valid string, and null token
    @Test
    public void testParseDate_variousInputs_parsesDate() throws Exception {
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonParser pTimestamp = _mapper.createParser("1600000000000");
        pTimestamp.nextToken();
        Date date1 = _deser._parseDate(pTimestamp, ctxt);
        assertEquals(1600000000000L, date1.getTime());

        Date date2 = _deser._parseDate("2020-01-01T00:00:00.000+0000", ctxt);
        assertNotNull(date2);

        Date emptyDate = _deser._parseDate("", ctxt);
        assertNull(emptyDate);

        Date nullDate = _deser._parseDate("null", ctxt);
        assertNull(nullDate);
    }

    // Tests _parseString with string token and non-string token
    @Test
    public void testParseString_validAndConvertibleTokens_returnsString() throws Exception {
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonParser pStr = _mapper.createParser("\"hello\"");
        pStr.nextToken();
        assertEquals("hello", _deser._parseString(pStr, ctxt));

        JsonParser pInt = _mapper.createParser("123");
        pInt.nextToken();
        assertEquals("123", _deser._parseString(pInt, ctxt));
    }

    // Tests _findNullProvider for FAIL, SKIP, and AS_EMPTY null policies
    @Test
    public void testFindNullProvider_differentNullPolicies_returnsAppropriateProvider() throws Exception {
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        NullValueProvider failProvider = _deser._findNullProvider(ctxt, null, Nulls.FAIL, _deser);
        assertNotNull(failProvider);

        NullValueProvider skipProvider = _deser._findNullProvider(ctxt, null, Nulls.SKIP, _deser);
        assertNotNull(skipProvider);

        NullValueProvider emptyProvider = _deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, _deser);
        assertNotNull(emptyProvider);

        NullValueProvider defaultProvider = _deser._findNullProvider(ctxt, null, Nulls.DEFAULT, _deser);
        assertNull(defaultProvider);
    }

    // Tests _coerceNullToken with primitive flag triggering validation
    @Test(expected = JsonMappingException.class)
    public void testVerifyNullForPrimitive_whenEnabled_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        _deser._verifyNullForPrimitive(ctxt);
    }

    // Tests _failDoubleToIntCoercion throwing JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testFailDoubleToIntCoercion_throwsException() throws Exception {
        JsonParser p = _mapper.createParser("12.34");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        _deser._failDoubleToIntCoercion(p, ctxt, "int");
    }

    // Tests findFormatOverrides and findFormatFeature default behaviors
    @Test
    public void testFormatOverridesAndFeatures_default_returnsExpected() {
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        JsonFormat.Value format = _deser.findFormatOverrides(ctxt, null, Integer.class);
        assertNotNull(format);

        Boolean feature = _deser.findFormatFeature(ctxt, null, Integer.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertNull(feature);
    }

    // Tests _coercedTypeDesc method output for standard classes
    @Test
    public void testCoercedTypeDesc_standardClass_returnsClassName() {
        String desc = _deser._coercedTypeDesc();
        assertNotNull(desc);
        assertTrue(desc.contains("java.lang.Integer"));
    }
}