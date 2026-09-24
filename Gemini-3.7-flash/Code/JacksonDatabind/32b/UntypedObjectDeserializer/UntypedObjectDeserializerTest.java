package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class UntypedObjectDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests deserializing basic JSON types to natural Java objects
    @Test
    public void testDeserialize_basicTypes_returnsNaturalObjects() throws Exception {
        Object strVal = mapper.readValue("\"hello\"", Object.class);
        assertEquals("hello", strVal);

        Object intVal = mapper.readValue("123", Object.class);
        assertEquals(Integer.valueOf(123), intVal);

        Object boolTrue = mapper.readValue("true", Object.class);
        assertEquals(Boolean.TRUE, boolTrue);

        Object boolFalse = mapper.readValue("false", Object.class);
        assertEquals(Boolean.FALSE, boolFalse);

        Object nullVal = mapper.readValue("null", Object.class);
        assertNull(nullVal);

        Object doubleVal = mapper.readValue("12.5", Object.class);
        assertEquals(Double.valueOf(12.5), doubleVal);
    }

    // Tests empty, 1-entry, 2-entry, and multiple-entry JSON maps
    @Test
    public void testDeserialize_mapVariations_returnsLinkedHashMap() throws Exception {
        Object emptyMap = mapper.readValue("{}", Object.class);
        assertTrue(emptyMap instanceof Map);
        assertTrue(((Map<?, ?>) emptyMap).isEmpty());

        Object singleMap = mapper.readValue("{\"k1\":\"v1\"}", Object.class);
        assertTrue(singleMap instanceof Map);
        Map<?, ?> map1 = (Map<?, ?>) singleMap;
        assertEquals(1, map1.size());
        assertEquals("v1", map1.get("k1"));

        Object twoMap = mapper.readValue("{\"k1\":\"v1\",\"k2\":2}", Object.class);
        assertTrue(twoMap instanceof Map);
        Map<?, ?> map2 = (Map<?, ?>) twoMap;
        assertEquals(2, map2.size());
        assertEquals("v1", map2.get("k1"));
        assertEquals(Integer.valueOf(2), map2.get("k2"));

        Object multiMap = mapper.readValue("{\"k1\":1,\"k2\":2,\"k3\":3,\"k4\":4}", Object.class);
        assertTrue(multiMap instanceof Map);
        Map<?, ?> map3 = (Map<?, ?>) multiMap;
        assertEquals(4, map3.size());
        assertEquals(Integer.valueOf(4), map3.get("k4"));
    }

    // Tests empty, 1-element, 2-element, and multiple-element JSON arrays
    @Test
    public void testDeserialize_arrayVariations_returnsList() throws Exception {
        Object emptyList = mapper.readValue("[]", Object.class);
        assertTrue(emptyList instanceof List);
        assertTrue(((List<?>) emptyList).isEmpty());

        Object singleList = mapper.readValue("[\"a\"]", Object.class);
        assertTrue(singleList instanceof List);
        List<?> list1 = (List<?>) singleList;
        assertEquals(1, list1.size());
        assertEquals("a", list1.get(0));

        Object twoList = mapper.readValue("[\"a\", \"b\"]", Object.class);
        assertTrue(twoList instanceof List);
        List<?> list2 = (List<?>) twoList;
        assertEquals(2, list2.size());
        assertEquals("a", list2.get(0));
        assertEquals("b", list2.get(1));

        Object multiList = mapper.readValue("[1, 2, 3, 4, 5]", Object.class);
        assertTrue(multiList instanceof List);
        List<?> list3 = (List<?>) multiList;
        assertEquals(5, list3.size());
        assertEquals(Integer.valueOf(5), list3.get(4));
    }

    // Tests deserializing JSON arrays when USE_JAVA_ARRAY_FOR_JSON_ARRAY is enabled
    @Test
    public void testDeserialize_useJavaArrayForJsonArray_returnsObjectArray() throws Exception {
        mapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        Object emptyArr = mapper.readValue("[]", Object.class);
        assertTrue(emptyArr instanceof Object[]);
        assertEquals(0, ((Object[]) emptyArr).length);

        Object arr = mapper.readValue("[\"x\", 10, true]", Object.class);
        assertTrue(arr instanceof Object[]);
        Object[] values = (Object[]) arr;
        assertEquals(3, values.length);
        assertEquals("x", values[0]);
        assertEquals(Integer.valueOf(10), values[1]);
        assertEquals(Boolean.TRUE, values[2]);
    }

    // Tests float and int coercion settings (USE_BIG_DECIMAL_FOR_FLOATS, USE_BIG_INTEGER_FOR_INTS)
    @Test
    public void testDeserialize_numberCoercionFeatures() throws Exception {
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Object dec = mapper.readValue("123.456", Object.class);
        assertTrue(dec instanceof BigDecimal);
        assertEquals(new BigDecimal("123.456"), dec);

        mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Object bigInt = mapper.readValue("123456", Object.class);
        assertTrue(bigInt instanceof BigInteger);
        assertEquals(new BigInteger("123456"), bigInt);

        mapper.disable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        Object longVal = mapper.readValue("123456", Object.class);
        assertTrue(longVal instanceof Long);
        assertEquals(Long.valueOf(123456L), longVal);
    }

    // Tests polymorphic deserialization with type info (deserializeWithType)
    @Test
    public void testDeserializeWithType_defaultTyping_handlesObjectWrapper() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);

        Object intVal = mapper.readValue("123", Object.class);
        assertEquals(Integer.valueOf(123), intVal);

        Object strVal = mapper.readValue("\"test\"", Object.class);
        assertEquals("test", strVal);

        Object boolVal = mapper.readValue("true", Object.class);
        assertEquals(Boolean.TRUE, boolVal);

        Object nullVal = mapper.readValue("null", Object.class);
        assertNull(nullVal);
    }

    // Tests deserializeWithType for float with BigDecimal enabled
    @Test
    public void testDeserializeWithType_floatWithBigDecimal() throws Exception {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        Object decVal = mapper.readValue("12.34", Object.class);
        assertTrue(decVal instanceof BigDecimal);
        assertEquals(new BigDecimal("12.34"), decVal);
    }

    // Tests UntypedObjectDeserializer.Vanilla direct usage and methods
    @Test
    public void testVanilla_directDeserialization() throws Exception {
        UntypedObjectDeserializer.Vanilla vanilla = new UntypedObjectDeserializer.Vanilla();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser parser = mapper.getFactory().createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        Object mapResult = vanilla.deserialize(parser, ctxt);
        assertTrue(mapResult instanceof Map);
        assertEquals(Integer.valueOf(1), ((Map<?, ?>) mapResult).get("a"));
        parser.close();

        parser = mapper.getFactory().createParser("[1, 2, 3]");
        parser.nextToken(); // START_ARRAY
        Object listResult = vanilla.deserialize(parser, ctxt);
        assertTrue(listResult instanceof List);
        assertEquals(3, ((List<?>) listResult).size());
        parser.close();
    }

    // Tests Vanilla deserializer when USE_JAVA_ARRAY_FOR_JSON_ARRAY is active
    @Test
    public void testVanilla_emptyAndNonEmptyArraysToArray() throws Exception {
        UntypedObjectDeserializer.Vanilla vanilla = new UntypedObjectDeserializer.Vanilla();
        ObjectMapper arrMapper = new ObjectMapper();
        arrMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        DeserializationContext ctxt = arrMapper.getDeserializationContext();

        JsonParser emptyParser = arrMapper.getFactory().createParser("[]");
        emptyParser.nextToken();
        Object emptyArr = vanilla.deserialize(emptyParser, ctxt);
        assertTrue(emptyArr instanceof Object[]);
        assertEquals(0, ((Object[]) emptyArr).length);
        emptyParser.close();

        JsonParser arrParser = arrMapper.getFactory().createParser("[1, 2]");
        arrParser.nextToken();
        Object arr = vanilla.deserialize(arrParser, ctxt);
        assertTrue(arr instanceof Object[]);
        assertEquals(2, ((Object[]) arr).length);
        arrParser.close();
    }

    // Tests UntypedObjectDeserializer with custom deserializers
    @Test
    public void testResolveAndCustomDeserializers() throws Exception {
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(String.class, new StdScalarDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "CUSTOM:" + p.getText();
            }
        });
        mapper.registerModule(mod);

        Object val = mapper.readValue("\"sample\"", Object.class);
        assertEquals("CUSTOM:sample", val);

        Object mapVal = mapper.readValue("{\"key\":\"value\"}", Object.class);
        assertTrue(mapVal instanceof Map);
        assertEquals("CUSTOM:value", ((Map<?, ?>) mapVal).get("key"));
    }

    // Tests UntypedObjectDeserializer constructors and isCachable method
    @Test
    public void testConstructorsAndIsCachable() {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer();
        assertTrue(deser.isCachable());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Object.class);
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(LinkedHashMap.class, String.class, Object.class);
        UntypedObjectDeserializer customDeser = new UntypedObjectDeserializer(listType, mapType);
        assertTrue(customDeser.isCachable());
    }

    // Tests createContextual returns Vanilla instance when standard deserializers are used
    @Test
    public void testCreateContextual_standard_returnsVanilla() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.resolve(ctxt);

        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        assertTrue(contextual instanceof UntypedObjectDeserializer.Vanilla);
    }

    // Tests invalid token triggering JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidToken_throwsMappingException() throws Exception {
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // END_ARRAY
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer();
        deser.deserialize(p, mapper.getDeserializationContext());
    }
}