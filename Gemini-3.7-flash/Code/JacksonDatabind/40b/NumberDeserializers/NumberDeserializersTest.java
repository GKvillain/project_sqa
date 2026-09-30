package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NumberDeserializersTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    // Tests finding deserializers for all primitive types
    @Test
    public void testFind_primitiveTypes_returnsMatchingDeserializers() {
        assertNotNull(NumberDeserializers.find(Integer.TYPE, Integer.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Boolean.TYPE, Boolean.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Long.TYPE, Long.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Double.TYPE, Double.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Character.TYPE, Character.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Byte.TYPE, Byte.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Short.TYPE, Short.TYPE.getName()));
        assertNotNull(NumberDeserializers.find(Float.TYPE, Float.TYPE.getName()));
    }

    // Tests finding deserializers for wrapper and standard numeric types
    @Test
    public void testFind_wrapperAndStandardTypes_returnsMatchingDeserializers() {
        assertNotNull(NumberDeserializers.find(Integer.class, Integer.class.getName()));
        assertNotNull(NumberDeserializers.find(Boolean.class, Boolean.class.getName()));
        assertNotNull(NumberDeserializers.find(Long.class, Long.class.getName()));
        assertNotNull(NumberDeserializers.find(Double.class, Double.class.getName()));
        assertNotNull(NumberDeserializers.find(Character.class, Character.class.getName()));
        assertNotNull(NumberDeserializers.find(Byte.class, Byte.class.getName()));
        assertNotNull(NumberDeserializers.find(Short.class, Short.class.getName()));
        assertNotNull(NumberDeserializers.find(Float.class, Float.class.getName()));
        assertNotNull(NumberDeserializers.find(Number.class, Number.class.getName()));
        assertNotNull(NumberDeserializers.find(BigDecimal.class, BigDecimal.class.getName()));
        assertNotNull(NumberDeserializers.find(BigInteger.class, BigInteger.class.getName()));
    }

    // Tests finding deserializer for unsupported type returns null
    @Test
    public void testFind_unknownType_returnsNull() {
        assertNull(NumberDeserializers.find(String.class, String.class.getName()));
    }

    // Tests primitive getNullValue when FAIL_ON_NULL_FOR_PRIMITIVES is enabled
    @Test(expected = JsonMappingException.class)
    public void testPrimitiveNullValue_failOnNullEnabled_throwsException() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        mapper.readValue("null", int.class);
    }

    // Tests primitive getNullValue default when FAIL_ON_NULL_FOR_PRIMITIVES is disabled
    @Test
    public void testPrimitiveNullValue_failOnNullDisabled_returnsDefault() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        int result = mapper.readValue("null", int.class);
        assertEquals(0, result);
    }

    // Tests wrapper getNullValue when FAIL_ON_NULL_FOR_PRIMITIVES is enabled returns null
    @Test
    public void testWrapperNullValue_returnsNull() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        Integer result = mapper.readValue("null", Integer.class);
        assertNull(result);
    }

    // Tests CharacterDeserializer with single char string
    @Test
    public void testCharacterDeserializer_singleCharString_returnsChar() throws IOException {
        Character c = MAPPER.readValue("\"a\"", Character.class);
        assertEquals(Character.valueOf('a'), c);
    }

    // Tests CharacterDeserializer with integer ascii value
    @Test
    public void testCharacterDeserializer_asciiNumber_returnsChar() throws IOException {
        Character c = MAPPER.readValue("65", Character.class);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests CharacterDeserializer with empty string
    @Test
    public void testCharacterDeserializer_emptyString_returnsNullOrEmpty() throws IOException {
        Character c = MAPPER.readValue("\"\"", Character.class);
        assertNull(c);
    }

    // Tests NumberDeserializer with integer and floating point values
    @Test
    public void testNumberDeserializer_numbers_returnsExpectedType() throws IOException {
        Number intNum = MAPPER.readValue("123", Number.class);
        assertTrue(intNum instanceof Integer || intNum instanceof Long);
        assertEquals(123, intNum.intValue());

        Number floatNum = MAPPER.readValue("12.34", Number.class);
        assertTrue(floatNum instanceof Double || floatNum instanceof BigDecimal);
        assertEquals(12.34, floatNum.doubleValue(), 0.001);
    }

    // Tests NumberDeserializer with special string representations
    @Test
    public void testNumberDeserializer_specialStrings_returnsExpectedValues() throws IOException {
        Number posInf = MAPPER.readValue("\"Infinity\"", Number.class);
        assertEquals(Double.POSITIVE_INFINITY, posInf);

        Number negInf = MAPPER.readValue("\"-Infinity\"", Number.class);
        assertEquals(Double.NEGATIVE_INFINITY, negInf);

        Number nan = MAPPER.readValue("\"NaN\"", Number.class);
        assertEquals(Double.NaN, nan);

        Number nullVal = MAPPER.readValue("\"null\"", Number.class);
        assertNull(nullVal);

        Number emptyVal = MAPPER.readValue("\"\"", Number.class);
        assertNull(emptyVal);
    }

    // Tests NumberDeserializer with string integers and floats
    @Test
    public void testNumberDeserializer_numericStrings_parsesCorrectly() throws IOException {
        Number intVal = MAPPER.readValue("\"42\"", Number.class);
        assertEquals(42, intVal.intValue());

        Number longVal = MAPPER.readValue("\"" + (Long.MAX_VALUE - 1) + "\"", Number.class);
        assertEquals(Long.MAX_VALUE - 1, longVal.longValue());

        Number doubleVal = MAPPER.readValue("\"3.14159\"", Number.class);
        assertEquals(3.14159, doubleVal.doubleValue(), 0.00001);
    }

    // Tests BigInteger and BigDecimal deserialization from string and number
    @Test
    public void testBigIntegerAndBigDecimal_validValues_deserializesCorrectly() throws IOException {
        BigInteger biFromStr = MAPPER.readValue("\"12345678901234567890\"", BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), biFromStr);

        BigInteger biFromNum = MAPPER.readValue("12345", BigInteger.class);
        assertEquals(BigInteger.valueOf(12345), biFromNum);

        BigDecimal bdFromStr = MAPPER.readValue("\"12345.67890\"", BigDecimal.class);
        assertEquals(new BigDecimal("12345.67890"), bdFromStr);

        BigDecimal bdFromNum = MAPPER.readValue("12345.67890", BigDecimal.class);
        assertEquals(new BigDecimal("12345.67890"), bdFromNum);
    }

    // Tests BigInteger and BigDecimal deserialization with empty string
    @Test
    public void testBigIntegerAndBigDecimal_emptyString_returnsNull() throws IOException {
        assertNull(MAPPER.readValue("\"\"", BigInteger.class));
        assertNull(MAPPER.readValue("\"\"", BigDecimal.class));
    }

    // Tests unwrap single value array feature for numbers
    @Test
    public void testUnwrapSingleValueArray_enabled_deserializesValue() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Integer intVal = mapper.readValue("[42]", Integer.class);
        assertEquals(Integer.valueOf(42), intVal);

        BigInteger biVal = mapper.readValue("[100]", BigInteger.class);
        assertEquals(BigInteger.valueOf(100), biVal);

        BigDecimal bdVal = mapper.readValue("[12.5]", BigDecimal.class);
        assertEquals(new BigDecimal("12.5"), bdVal);

        Character chVal = mapper.readValue("[\"x\"]", Character.class);
        assertEquals(Character.valueOf('x'), chVal);
    }

    // Tests invalid string mapping throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testInvalidNumberString_throwsException() throws IOException {
        MAPPER.readValue("\"not_a_number\"", Number.class);
    }

    // Tests invalid BigInteger string throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testInvalidBigIntegerString_throwsException() throws IOException {
        MAPPER.readValue("\"abc\"", BigInteger.class);
    }

    // Tests invalid BigDecimal string throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testInvalidBigDecimalString_throwsException() throws IOException {
        MAPPER.readValue("\"abc\"", BigDecimal.class);
    }
}