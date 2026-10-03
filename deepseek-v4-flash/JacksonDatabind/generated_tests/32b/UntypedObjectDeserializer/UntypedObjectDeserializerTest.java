package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;

public class UntypedObjectDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests deserializing a string value
    @Test
    public void testDeserialize_stringToken_returnsString() throws Exception {
        Object result = mapper.readValue("\"hello\"", Object.class);
        assertEquals("hello", result);
    }

    // Tests deserializing an integer value
    @Test
    public void testDeserialize_integerToken_returnsInteger() throws Exception {
        Object result = mapper.readValue("42", Object.class);
        assertEquals(Integer.valueOf(42), result);
    }

    // Tests deserializing a float value
    @Test
    public void testDeserialize_floatToken_returnsDouble() throws Exception {
        Object result = mapper.readValue("3.14", Object.class);
        assertEquals(Double.valueOf(3.14), result);
    }

    // Tests deserializing boolean true
    @Test
    public void testDeserialize_trueToken_returnsBooleanTrue() throws Exception {
        Object result = mapper.readValue("true", Object.class);
        assertEquals(Boolean.TRUE, result);
    }

    // Tests deserializing boolean false
    @Test
    public void testDeserialize_falseToken_returnsBooleanFalse() throws Exception {
        Object result = mapper.readValue("false", Object.class);
        assertEquals(Boolean.FALSE, result);
    }

    // Tests deserializing null
    @Test
    public void testDeserialize_nullToken_returnsNull() throws Exception {
        Object result = mapper.readValue("null", Object.class);
        assertNull(result);
    }

    // Tests empty JSON object
    @Test
    public void testDeserialize_emptyObject_returnsEmptyLinkedHashMap() throws Exception {
        Object result = mapper.readValue("{}", Object.class);
        assertTrue(result instanceof LinkedHashMap);
        assertEquals(0, ((Map) result).size());
    }

    // Tests object with one key-value pair
    @Test
    public void testDeserialize_oneEntryObject_returnsMap() throws Exception {
        Object result = mapper.readValue("{\"k\":\"v\"}", Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.size());
        assertEquals("v", map.get("k"));
    }

    // Tests object with two key-value pairs
    @Test
    public void testDeserialize_twoEntryObject_returnsMap() throws Exception {
        Object result = mapper.readValue("{\"a\":1,\"b\":2}", Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(1), map.get("a"));
        assertEquals(Integer.valueOf(2), map.get("b"));
    }

    // Tests object with more than two entries (general loop)
    @Test
    public void testDeserialize_multiEntryObject_returnsMap() throws Exception {
        String json = "{\"a\":1,\"b\":2,\"c\":3,\"d\":4}";
        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(4, map.size());
    }

    // Tests empty JSON array
    @Test
    public void testDeserialize_emptyArray_returnsEmptyArrayList() throws Exception {
        Object result = mapper.readValue("[]", Object.class);
        assertTrue(result instanceof ArrayList);
        assertEquals(0, ((List) result).size());
    }

    // Tests array with one element
    @Test
    public void testDeserialize_oneElementArray_returnsArrayList() throws Exception {
        Object result = mapper.readValue("[1]", Object.class);
        assertTrue(result instanceof ArrayList);
        List<Object> list = (List<Object>) result;
        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
    }

    // Tests array with two elements
    @Test
    public void testDeserialize_twoElementArray_returnsArrayList() throws Exception {
        Object result = mapper.readValue("[1,2]", Object.class);
        assertTrue(result instanceof ArrayList);
        List<Object> list = (List<Object>) result;
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
    }

    // Tests array with many elements (exercises ObjectBuffer)
    @Test
    public void testDeserialize_manyElementArray_returnsArrayList() throws Exception {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        Object result = mapper.readValue(sb.toString(), Object.class);
        assertTrue(result instanceof ArrayList);
        List<Object> list = (List<Object>) result;
        assertEquals(20, list.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    // Tests array when USE_JAVA_ARRAY_FOR_JSON_ARRAY is enabled (returns Object[])
    @Test
    public void testDeserialize_arrayWithJavaArrayFeature_returnsObjectArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        Object result = localMapper.readValue("[1,2,3]", Object.class);
        assertTrue(result instanceof Object[]);
        Object[] arr = (Object[]) result;
        assertEquals(3, arr.length);
        assertEquals(Integer.valueOf(1), arr[0]);
        assertEquals(Integer.valueOf(2), arr[1]);
        assertEquals(Integer.valueOf(3), arr[2]);
    }

    // Tests empty array with USE_JAVA_ARRAY_FOR_JSON_ARRAY (returns NO_OBJECTS)
    @Test
    public void testDeserialize_emptyArrayWithJavaArrayFeature_returnsEmptyObjectArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        Object result = localMapper.readValue("[]", Object.class);
        assertTrue(result instanceof Object[]);
        assertEquals(0, ((Object[]) result).length);
    }

    // Tests nested JSON object
    @Test
    public void testDeserialize_nestedObject_returnsNestedMap() throws Exception {
        String json = "{\"outer\":{\"inner\":\"value\"}}";
        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> outer = (Map<String, Object>) result;
        assertEquals(1, outer.size());
        Object inner = outer.get("outer");
        assertTrue(inner instanceof LinkedHashMap);
        assertEquals("value", ((Map) inner).get("inner"));
    }

    // Tests float value with USE_BIG_DECIMAL_FOR_FLOATS feature
    @Test
    public void testDeserialize_floatWithBigDecimalFeature_returnsBigDecimal() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Object result = localMapper.readValue("3.14", Object.class);
        assertEquals(new BigDecimal("3.14"), result);
    }

    // Tests integer value with USE_BIG_INTEGER_FOR_INTS feature
    @Test
    public void testDeserialize_integerWithBigIntegerFeature_returnsBigInteger() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Object result = localMapper.readValue("42", Object.class);
        assertEquals(BigInteger.valueOf(42), result);
    }

    // ====== New test cases for uncovered coverage ======

    // Tests deserializing a long value that exceeds Integer.MAX_VALUE
    @Test
    public void testDeserialize_longValue_returnsLong() throws Exception {
        Object result = mapper.readValue("2147483648", Object.class); // > Integer.MAX_VALUE
        assertEquals(Long.valueOf(2147483648L), result);
    }

    // Tests object containing a null value
    @Test
    public void testDeserialize_objectWithNullValue_returnsMapWithNull() throws Exception {
        Object result = mapper.readValue("{\"key\":null}", Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.size());
        assertNull(map.get("key"));
    }

    // Tests array containing null elements
    @Test
    public void testDeserialize_arrayWithNullElements_returnsArrayListWithNulls() throws Exception {
        Object result = mapper.readValue("[1, null, \"three\"]", Object.class);
        assertTrue(result instanceof ArrayList);
        List<Object> list = (List<Object>) result;
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertNull(list.get(1));
        assertEquals("three", list.get(2));
    }

    // Tests nested array inside an object
    @Test
    public void testDeserialize_nestedArrayInObject_returnsMapWithArrayList() throws Exception {
        String json = "{\"arr\":[1,2,3]}";
        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof LinkedHashMap);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(1, map.size());
        Object arr = map.get("arr");
        assertTrue(arr instanceof ArrayList);
        List<Object> list = (List<Object>) arr;
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    // Tests nested object inside an array
    @Test
    public void testDeserialize_nestedObjectInArray_returnsArrayListWithMap() throws Exception {
        String json = "[{\"a\":1}]";
        Object result = mapper.readValue(json, Object.class);
        assertTrue(result instanceof ArrayList);
        List<Object> list = (List<Object>) result;
        assertEquals(1, list.size());
        Object obj = list.get(0);
        assertTrue(obj instanceof LinkedHashMap);
        assertEquals(Integer.valueOf(1), ((Map) obj).get("a"));
    }

    // Tests a very large integer with USE_BIG_INTEGER_FOR_INTS feature
    @Test
    public void testDeserialize_veryLargeIntegerWithBigIntegerFeature_returnsBigInteger() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Object result = localMapper.readValue("123456789012345678901234567890", Object.class);
        assertEquals(new BigInteger("123456789012345678901234567890"), result);
    }

    // Tests boolean uppercase variants (TRUE, True) – should still map to boolean
    @Test
    public void testDeserialize_booleanUppercaseTokens_returnsBoolean() throws Exception {
        Object result1 = mapper.readValue("TRUE", Object.class);
        assertEquals(Boolean.TRUE, result1);
        Object result2 = mapper.readValue("True", Object.class);
        assertEquals(Boolean.TRUE, result2);
        Object result3 = mapper.readValue("FALSE", Object.class);
        assertEquals(Boolean.FALSE, result3);
        Object result4 = mapper.readValue("False", Object.class);
        assertEquals(Boolean.FALSE, result4);
    }
}