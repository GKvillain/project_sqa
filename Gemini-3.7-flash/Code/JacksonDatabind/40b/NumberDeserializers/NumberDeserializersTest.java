package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class NumberDeserializersTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests finding deserializers for all primitive types
    @Test
    public void testFind_primitiveTypes_returnsPrimitiveDeserializers() {
        assertSame(NumberDeserializers.IntegerDeserializer.primitiveInstance,
                NumberDeserializers.find(Integer.TYPE, Integer.TYPE.getName()));
        assertSame(NumberDeserializers.BooleanDeserializer.primitiveInstance,
                NumberDeserializers.find(Boolean.TYPE, Boolean.TYPE.getName()));
        assertSame(NumberDeserializers.LongDeserializer.primitiveInstance,
                NumberDeserializers.find(Long.TYPE, Long.TYPE.getName()));
        assertSame(NumberDeserializers.DoubleDeserializer.primitiveInstance,
                NumberDeserializers.find(Double.TYPE, Double.TYPE.getName()));
        assertSame(NumberDeserializers.CharacterDeserializer.primitiveInstance,
                NumberDeserializers.find(Character.TYPE, Character.TYPE.getName()));
        assertSame(NumberDeserializers.ByteDeserializer.primitiveInstance,
                NumberDeserializers.find(Byte.TYPE, Byte.TYPE.getName()));
        assertSame(NumberDeserializers.ShortDeserializer.primitiveInstance,
                NumberDeserializers.find(Short.TYPE, Short.TYPE.getName()));
        assertSame(NumberDeserializers.FloatDeserializer.primitiveInstance,
                NumberDeserializers.find(Float.TYPE, Float.TYPE.getName()));
    }

    // Tests finding deserializers for wrapper and big numeric types
    @Test
    public void testFind_wrapperAndBigNumericTypes_returnsMatchingDeserializers() {
        assertSame(NumberDeserializers.IntegerDeserializer.wrapperInstance,
                NumberDeserializers.find(Integer.class, Integer.class.getName()));
        assertSame(NumberDeserializers.BooleanDeserializer.wrapperInstance,
                NumberDeserializers.find(Boolean.class, Boolean.class.getName()));
        assertSame(NumberDeserializers.LongDeserializer.wrapperInstance,
                NumberDeserializers.find(Long.class, Long.class.getName()));
        assertSame(NumberDeserializers.DoubleDeserializer.wrapperInstance,
                NumberDeserializers.find(Double.class, Double.class.getName()));
        assertSame(NumberDeserializers.CharacterDeserializer.wrapperInstance,
                NumberDeserializers.find(Character.class, Character.class.getName()));
        assertSame(NumberDeserializers.ByteDeserializer.wrapperInstance,
                NumberDeserializers.find(Byte.class, Byte.class.getName()));
        assertSame(NumberDeserializers.ShortDeserializer.wrapperInstance,
                NumberDeserializers.find(Short.class, Short.class.getName()));
        assertSame(NumberDeserializers.FloatDeserializer.wrapperInstance,
                NumberDeserializers.find(Float.class, Float.class.getName()));
        assertSame(NumberDeserializers.NumberDeserializer.instance,
                NumberDeserializers.find(Number.class, Number.class.getName()));
        assertSame(NumberDeserializers.BigDecimalDeserializer.instance,
                NumberDeserializers.find(BigDecimal.class, BigDecimal.class.getName()));
        assertSame(NumberDeserializers.BigIntegerDeserializer.instance,
                NumberDeserializers.find(BigInteger.class, BigInteger.class.getName()));
    }

    // Tests finding deserializer for unknown non-numeric type returns null
    @Test
    public void testFind_nonNumericType_returnsNull() {
        assertNull(NumberDeserializers.find(String.class, String.class.getName()));
    }

    // Tests getNullValue for primitive with FAIL_ON_NULL_FOR_PRIMITIVES throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testGetNullValue_primitiveWithFailOnNullFeature_throwsException() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        m.readValue("null", int.class);
    }

    // Tests getNullValue for primitive returns default zero value when fail feature is disabled
    @Test
    public void testGetNullValue_primitiveWithoutFailOnNullFeature_returnsDefaultZero() throws Exception {
        Integer nullValue = mapper.readValue("null", int.class);
        assertEquals(Integer.valueOf(0), nullValue);
    }

    // Tests getNullValue for wrapper returns null
    @Test
    public void testGetNullValue_wrapper_returnsNull() throws Exception {
        assertNull(mapper.readValue("null", Integer.class));
    }

    // Tests character deserialization from ascii integer
    @Test
    public void testCharacterDeserializer_asciiIntValue_returnsCharacter() throws Exception {
        Character result = mapper.readValue("65", Character.class);
        assertEquals(Character.valueOf('A'), result);
    }

    // Tests character deserialization from single character string
    @Test
    public void testCharacterDeserializer_singleCharString_returnsCharacter() throws Exception {
        Character result = mapper.readValue("\"Z\"", Character.class);
        assertEquals(Character.valueOf('Z'), result);
    }

    // Tests character deserialization with single value array unwrapping
    @Test
    public void testCharacterDeserializer_unwrapSingleValueArray_returnsCharacter() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Character result = m.readValue("[\"C\"]", Character.class);
        assertEquals(Character.valueOf('C'), result);
    }

    // Tests character deserialization failure on array with more than one element
    @Test(expected = JsonMappingException.class)
    public void testCharacterDeserializer_multipleValuesInArray_throwsException() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        m.readValue("[\"A\", \"B\"]", Character.class);
    }

    // Tests number deserialization for special floating-point strings
    @Test
    public void testNumberDeserializer_specialFloatStrings_returnsDoubleConstants() throws Exception {
        Object nan = mapper.readValue("\"NaN\"", Number.class);
        assertEquals(Double.NaN, (Double) nan, 0.0);

        Object posInf = mapper.readValue("\"Infinity\"", Number.class);
        assertEquals(Double.POSITIVE_INFINITY, (Double) posInf, 0.0);

        Object negInf = mapper.readValue("\"-Infinity\"", Number.class);
        assertEquals(Double.NEGATIVE_INFINITY, (Double) negInf, 0.0);
    }

    // Tests number deserialization for integer and decimal strings
    @Test
    public void testNumberDeserializer_validNumberStrings_returnsParsedNumbers() throws Exception {
        Object intNum = mapper.readValue("\"12345\"", Number.class);
        assertEquals(Integer.valueOf(12345), intNum);

        Object doubleNum = mapper.readValue("\"123.45\"", Number.class);
        assertEquals(123.45, ((Number) doubleNum).doubleValue(), 0.001);
    }

    // Tests number deserialization with USE_BIG_DECIMAL_FOR_FLOATS and USE_BIG_INTEGER_FOR_INTS
    @Test
    public void testNumberDeserializer_withBigFeatures_returnsBigTypes() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        m.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

        Object bigDec = m.readValue("\"123.4567890123456789\"", Number.class);
        assertTrue(bigDec instanceof BigDecimal);

        Object bigInt = m.readValue("\"98765432109876543210\"", Number.class);
        assertTrue(bigInt instanceof BigInteger);
    }

    // Tests BigInteger deserializer with string and float coercion disabled
    @Test
    public void testBigIntegerDeserializer_validStringAndInt_returnsBigInteger() throws Exception {
        BigInteger result = mapper.readValue("\"12345678901234567890\"", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), result);

        BigInteger empty = mapper.readValue("\"\"", BigInteger.class);
        assertNull(empty);
    }

    // Tests BigInteger deserializer when float to int coercion is disabled throws exception
    @Test(expected = JsonMappingException.class)
    public void testBigIntegerDeserializer_floatValueWithoutAcceptFloatAsInt_throwsException() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        m.readValue("12.34", BigInteger.class);
    }

    // Tests BigDecimal deserializer with string, empty string, and array unwrapping
    @Test
    public void testBigDecimalDeserializer_validStringAndEmptyString_returnsBigDecimalOrNull() throws Exception {
        BigDecimal result = mapper.readValue("\"12345.67890\"", BigDecimal.class);
        assertEquals(new BigDecimal("12345.67890"), result);

        BigDecimal empty = mapper.readValue("\"\"", BigDecimal.class);
        assertNull(empty);

        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        BigDecimal unwrapResult = m.readValue("[\"99.99\"]", BigDecimal.class);
        assertEquals(new BigDecimal("99.99"), unwrapResult);
    }

    // Tests primitive deserialization with empty string and FAIL_ON_NULL_FOR_PRIMITIVES
    @Test(expected = JsonMappingException.class)
    public void testPrimitiveDeserializer_emptyStringWithFailOnNullForPrimitives_throwsException() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        m.readValue("\"\"", Integer.TYPE);
    }

    // Tests wrapper deserialization with empty string returning null
    @Test
    public void testWrapperDeserializer_emptyString_returnsNull() throws Exception {
        Integer result = mapper.readValue("\"\"", Integer.class);
        assertNull(result);
    }
}