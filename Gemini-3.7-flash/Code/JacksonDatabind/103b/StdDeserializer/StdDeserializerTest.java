package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdDeserializerTest {

    private static class ConcreteStdDeserializer extends StdDeserializer<Object> {
        private static final long serialVersionUID = 1L;

        public ConcreteStdDeserializer(Class<?> vc) {
            super(vc);
        }

        public ConcreteStdDeserializer(JavaType vt) {
            super(vt);
        }

        public ConcreteStdDeserializer(StdDeserializer<?> src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText();
        }
    }

    private ObjectMapper mapper;
    private ConcreteStdDeserializer deser;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        deser = new ConcreteStdDeserializer(Integer.TYPE);
    }

    // Tests constructors and basic accessors
    @Test
    public void testHandledType_variousConstructors_returnsExpectedClass() {
        assertEquals(Integer.TYPE, deser.handledType());
        assertEquals(Integer.TYPE, deser.getValueClass());
        assertNull(deser.getValueType());

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ConcreteStdDeserializer fromType = new ConcreteStdDeserializer(type);
        assertEquals(String.class, fromType.handledType());

        ConcreteStdDeserializer fromNullType = new ConcreteStdDeserializer((JavaType) null);
        assertEquals(Object.class, fromNullType.handledType());

        ConcreteStdDeserializer copied = new ConcreteStdDeserializer(fromType);
        assertEquals(String.class, copied.handledType());
    }

    // Tests helper arithmetic overflow methods
    @Test
    public void testOverflowHelpers_boundaryValues_identifiesOverflowCorrectly() {
        assertFalse(deser._byteOverflow(0));
        assertFalse(deser._byteOverflow(255));
        assertFalse(deser._byteOverflow(-128));
        assertTrue(deser._byteOverflow(-129));
        assertTrue(deser._byteOverflow(256));

        assertFalse(deser._shortOverflow(0));
        assertFalse(deser._shortOverflow(Short.MAX_VALUE));
        assertFalse(deser._shortOverflow(Short.MIN_VALUE));
        assertTrue(deser._shortOverflow(Short.MAX_VALUE + 1));
        assertTrue(deser._shortOverflow(Short.MIN_VALUE - 1));

        assertFalse(deser._intOverflow(0L));
        assertFalse(deser._intOverflow((long) Integer.MAX_VALUE));
        assertFalse(deser._intOverflow((long) Integer.MIN_VALUE));
        assertTrue(deser._intOverflow((long) Integer.MAX_VALUE + 1L));
        assertTrue(deser._intOverflow((long) Integer.MIN_VALUE - 1L));
    }

    // Tests helper null and number methods
    @Test
    public void testNonNullNumberAndNeitherNull_variousInputs_returnsExpectedResult() {
        Number nonNull = deser._nonNullNumber(null);
        assertEquals(0, nonNull.intValue());

        Number original = Integer.valueOf(42);
        assertSame(original, deser._nonNullNumber(original));

        assertTrue(StdDeserializer._neitherNull("a", "b"));
        assertFalse(StdDeserializer._neitherNull(null, "b"));
        assertFalse(StdDeserializer._neitherNull("a", null));
        assertFalse(StdDeserializer._neitherNull(null, null));
    }

    // Tests numeric string check
    @Test
    public void testIsIntNumber_variousStrings_identifiesIntegersCorrectly() {
        assertTrue(deser._isIntNumber("12345"));
        assertTrue(deser._isIntNumber("+12345"));
        assertTrue(deser._isIntNumber("-12345"));
        assertFalse(deser._isIntNumber(""));
        assertFalse(deser._isIntNumber("123a45"));
        assertFalse(deser._isIntNumber("12.34"));
    }

    // Tests special float string constants
    @Test
    public void testSpecialFloatStrings_validKeywords_identifiesKeywords() {
        assertTrue(deser._isPosInf("Infinity"));
        assertTrue(deser._isPosInf("INF"));
        assertFalse(deser._isPosInf("other"));

        assertTrue(deser._isNegInf("-Infinity"));
        assertTrue(deser._isNegInf("-INF"));
        assertFalse(deser._isNegInf("other"));

        assertTrue(deser._isNaN("NaN"));
        assertFalse(deser._isNaN("other"));

        assertTrue(deser._hasTextualNull("null"));
        assertFalse(deser._hasTextualNull("other"));

        assertTrue(deser._isEmptyOrTextualNull(""));
        assertTrue(deser._isEmptyOrTextualNull("null"));
        assertFalse(deser._isEmptyOrTextualNull("not-null"));
    }

    // Tests double parsing static helper
    @Test
    public void testParseDouble_nastySmallDouble_returnsMinNormal() {
        double result = StdDeserializer.parseDouble("2.2250738585072012e-308");
        assertEquals(Double.MIN_NORMAL, result, 0.0);

        double standard = StdDeserializer.parseDouble("123.456");
        assertEquals(123.456, standard, 0.0001);
    }

    // Tests parsing boolean primitive from JSON tokens
    @Test
    public void testParseBooleanPrimitive_validTokens_parsesCorrectly() throws Exception {
        JsonParser pTrue = mapper.getFactory().createParser("true");
        pTrue.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertTrue(deser._parseBooleanPrimitive(pTrue, ctxt));

        JsonParser pFalse = mapper.getFactory().createParser("false");
        pFalse.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pFalse, ctxt));

        JsonParser pInt1 = mapper.getFactory().createParser("1");
        pInt1.nextToken();
        assertTrue(deser._parseBooleanPrimitive(pInt1, ctxt));

        JsonParser pInt0 = mapper.getFactory().createParser("0");
        pInt0.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pInt0, ctxt));

        JsonParser pStrTrue = mapper.getFactory().createParser("\"True\"");
        pStrTrue.nextToken();
        assertTrue(deser._parseBooleanPrimitive(pStrTrue, ctxt));

        JsonParser pStrFalse = mapper.getFactory().createParser("\"False\"");
        pStrFalse.nextToken();
        assertFalse(deser._parseBooleanPrimitive(pStrFalse, ctxt));
    }

    // Tests parsing int primitive from string and tokens
    @Test
    public void testParseIntPrimitive_validValues_parsesCorrectly() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        assertEquals(123, deser._parseIntPrimitive(ctxt, "123"));
        assertEquals(2147483647, deser._parseIntPrimitive(ctxt, "2147483647"));

        JsonParser pInt = mapper.getFactory().createParser("42");
        pInt.nextToken();
        assertEquals(42, deser._parseIntPrimitive(pInt, ctxt));

        JsonParser pStr = mapper.getFactory().createParser("\"100\"");
        pStr.nextToken();
        assertEquals(100, deser._parseIntPrimitive(pStr, ctxt));
    }

    // Tests parsing long primitive from string and tokens
    @Test
    public void testParseLongPrimitive_validValues_parsesCorrectly() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        assertEquals(1234567890123L, deser._parseLongPrimitive(ctxt, "1234567890123"));

        JsonParser pLong = mapper.getFactory().createParser("9876543210");
        pLong.nextToken();
        assertEquals(9876543210L, deser._parseLongPrimitive(pLong, ctxt));
    }

    // Tests parsing float primitive from special strings and values
    @Test
    public void testParseFloatPrimitive_specialStrings_parsesCorrectly() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        assertEquals(Float.POSITIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "Infinity"), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, deser._parseFloatPrimitive(ctxt, "-Infinity"), 0.0f);
        assertTrue(Float.isNaN(deser._parseFloatPrimitive(ctxt, "NaN")));
        assertEquals(12.5f, deser._parseFloatPrimitive(ctxt, "12.5"), 0.0f);
    }

    // Tests parsing double primitive from special strings and values
    @Test
    public void testParseDoublePrimitive_specialStrings_parsesCorrectly() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        assertEquals(Double.POSITIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "Infinity"), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, deser._parseDoublePrimitive(ctxt, "-Infinity"), 0.0);
        assertTrue(Double.isNaN(deser._parseDoublePrimitive(ctxt, "NaN")));
        assertEquals(99.99, deser._parseDoublePrimitive(ctxt, "99.99"), 0.0001);
    }

    // Tests date parsing from timestamp and empty string
    @Test
    public void testParseDate_variousInputs_parsesCorrectly() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pInt = mapper.getFactory().createParser("1000000");
        pInt.nextToken();
        Date date = deser._parseDate(pInt, ctxt);
        assertNotNull(date);
        assertEquals(1000000L, date.getTime());

        Date nullDate = deser._parseDate("", ctxt);
        assertNull(nullDate);
    }

    // Tests parsing string helper
    @Test
    public void testParseString_validStringToken_returnsText() throws Exception {
        JsonParser p = mapper.getFactory().createParser("\"hello world\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        assertEquals("hello world", deser._parseString(p, ctxt));
    }

    // Tests null provider resolution for FAIL, AS_EMPTY, and SKIP
    @Test
    public void testFindNullProvider_differentNullSettings_returnsCorrectProvider() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        NullValueProvider failProvider = deser._findNullProvider(ctxt, null, Nulls.FAIL, deser);
        assertTrue(failProvider instanceof NullsFailProvider);

        NullValueProvider skipProvider = deser._findNullProvider(ctxt, null, Nulls.SKIP, deser);
        assertSame(NullsConstantProvider.skipper(), skipProvider);

        NullValueProvider emptyProvider = deser._findNullProvider(ctxt, null, Nulls.AS_EMPTY, deser);
        assertTrue(emptyProvider instanceof NullsAsEmptyProvider);

        assertNull(deser._findNullProvider(ctxt, null, Nulls.DEFAULT, deser));
    }

    // Tests coercion of empty string and textual null when allowed
    @Test
    public void testCoerceEmptyStringAndTextualNull_allowedCoercion_returnsNull() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();

        assertNull(deser._coerceEmptyString(ctxt, false));
        assertNull(deser._coerceTextualNull(ctxt, false));
    }

    // Tests fail-on-null-for-primitives verification
    @Test(expected = JsonMappingException.class)
    public void testVerifyNullForPrimitive_whenFeatureEnabled_throwsException() throws Exception {
        ObjectMapper failMapper = new ObjectMapper();
        failMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        DeserializationContext ctxt = failMapper.getDeserializationContext();

        deser._verifyNullForPrimitive(ctxt);
    }

    // Tests scalar coercion verification when coercion is disabled
    @Test(expected = JsonMappingException.class)
    public void testVerifyStringForScalarCoercion_coercionDisabled_throwsException() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
        DeserializationContext ctxt = strictMapper.getDeserializationContext();

        deser._verifyStringForScalarCoercion(ctxt, "some-string");
    }

    // Tests format feature discovery helper
    @Test
    public void testFindFormatFeature_withoutOverrides_returnsNull() {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Boolean feat = deser.findFormatFeature(ctxt, null, Integer.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertNull(feat);
    }
}