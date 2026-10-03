import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CollectionDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ------------------------- basic -------------------------

    // Tests normal array deserialization into List<Integer>
    @Test
    public void testDeserialize_basicArray_returnsList() throws Exception {
        List<Integer> result = mapper.readValue("[1,2,3]", new TypeReference<List<Integer>>() {});
        assertEquals(Arrays.asList(1,2,3), result);
    }

    // Tests empty array returns empty list
    @Test
    public void testDeserialize_emptyArray_returnsEmptyList() throws Exception {
        List<String> result = mapper.readValue("[]", new TypeReference<List<String>>() {});
        assertTrue(result.isEmpty());
    }

    // Tests null value inside array
    @Test
    public void testDeserialize_nullValue_returnsListWithNull() throws Exception {
        List<String> result = mapper.readValue("[null]", new TypeReference<List<String>>() {});
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    // ------------------------- single value as array (global) -------------------------

    // Tests single value with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled returns one-element list
    @Test
    public void testDeserialize_singleValueAsArrayEnabled_returnsListWithOneElement() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = mapper.readValue("\"a\"", new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("a"), result);
    }

    // Tests single value with ACCEPT_SINGLE_VALUE_AS_ARRAY disabled throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_singleValueAsArrayDisabled_throwsMappingException() throws Exception {
        // default is disabled
        mapper.readValue("\"a\"", new TypeReference<List<String>>() {});
    }

    // Tests single null with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled returns list with null
    @Test
    public void testDeserialize_singleValueAsArrayEnabled_null_returnsListWithNull() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = mapper.readValue("null", new TypeReference<List<String>>() {});
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    // ------------------------- empty string handling -------------------------

    // Tests empty string without special feature: should throw because empty string cannot be converted to List
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_emptyString_throwsMappingException() throws Exception {
        mapper.readValue("\"\"", new TypeReference<List<String>>() {});
    }

    // Tests empty string with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled:
    // expected behavior (bug 62b?) – after fix, empty string should become empty list
    @Test
    public void testDeserialize_emptyStringWithSingleValueAsArrayEnabled_returnsEmptyList() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        try {
            List<String> result = mapper.readValue("\"\"", new TypeReference<List<String>>() {});
            assertTrue(result.isEmpty());
        } catch (JsonMappingException e) {
            // If this exception is thrown, the bug is present; we fail
            fail("Empty string with ACCEPT_SINGLE_VALUE_AS_ARRAY should return empty list, but threw: " + e.getMessage());
        }
    }

    // ------------------------- delegate creator -------------------------

    // Custom collection class with delegate creator that accepts a comma-separated string
    static class MyList extends ArrayList<String> {
        @JsonCreator
        public MyList(String value) {
            for (String s : value.split(",")) {
                add(s.trim());
            }
        }
    }

    @Test
    public void testDeserialize_withDelegateCreator_usesDelegateDeserializer() throws Exception {
        MyList list = mapper.readValue("\"a,b,c\"", MyList.class);
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    // ------------------------- polymorphic content (type deserializer) -------------------------

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    static abstract class Animal {
        public String name;
    }

    @JsonTypeName("dog")
    static class Dog extends Animal {
        public int barkVolume;
    }

    @JsonTypeName("cat")
    static class Cat extends Animal {
        public boolean likesCream;
    }

    @Test
    public void testDeserialize_withTypeDeserializer_usesTypeInfo() throws Exception {
        String json = "[{\"type\":\"dog\",\"name\":\"Rex\",\"barkVolume\":3},{\"type\":\"cat\",\"name\":\"Whiskers\",\"likesCream\":true}]";
        List<Animal> result = mapper.readValue(json, new TypeReference<List<Animal>>() {});
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof Dog);
        assertEquals("Rex", result.get(0).name);
        assertTrue(result.get(1) instanceof Cat);
        assertEquals("Whiskers", result.get(1).name);
    }

    // ------------------------- forward reference (identity info) -------------------------

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class Node {
        public int id;
        public Node next;
    }

    @Test
    public void testDeserialize_forwardReference_resolvesCorrectly() throws Exception {
        String json = "[{\"@id\":1,\"next\":{\"@id\":2,\"next\":1}}]";
        List<Node> nodes = mapper.readValue(json, new TypeReference<List<Node>>() {});
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        Node first = nodes.get(0);
        Node second = nodes.get(1);
        assertSame(first, second.next); // circular reference resolved
    }

    // ------------------------- isCachable (indirect check) -------------------------

    // isCachable is not directly testable from outside; but we can observe behavior via ObjectMapper caching.
    // We skip.

    // ------------------------- handleNonArray branch coverage -------------------------

    // Tests that a non-array token (e.g. object) throws mapping exception
    @Test(expected = JsonMappingException.class)
    public void testHandleNonArray_objectToken_throwsMappingException() throws Exception {
        mapper.readValue("{\"a\":1}", new TypeReference<List<String>>() {});
    }

    // Tests that numeric token (non-array) with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled is treated as single element
    @Test
    public void testHandleNonArray_numberWithSingleValueAsArrayEnabled_returnsListWithOneElement() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<Integer> result = mapper.readValue("42", new TypeReference<List<Integer>>() {});
        assertEquals(Arrays.asList(42), result);
    }

    // Tests that a non-array token with ACCEPT_SINGLE_VALUE_AS_ARRAY disabled throws
    @Test(expected = JsonMappingException.class)
    public void testHandleNonArray_numberWithSingleValueAsArrayDisabled_throwsMappingException() throws Exception {
        mapper.readValue("42", new TypeReference<List<Integer>>() {});
    }

    // Tests that boolean token with ACCEPT_SINGLE_VALUE_AS_ARRAY enabled returns list with boolean element
    @Test
    public void testHandleNonArray_booleanWithSingleValueAsArrayEnabled_returnsListWithOneElement() throws Exception {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<Boolean> result = mapper.readValue("true", new TypeReference<List<Boolean>>() {});
        assertEquals(Arrays.asList(true), result);
    }

    // ------------------------- empty array vs empty string with per-property @JsonFormat -------------------------

    static class Wrapper {
        @JsonFormat(acceptSingleValueAsArray = true)
        public List<String> items;
    }

    @Test
    public void testDeserialize_perPropertyAcceptSingleValueAsArray_emptyString_returnsEmptyList() throws Exception {
        Wrapper w = mapper.readValue("{\"items\":\"\"}", Wrapper.class);
        assertNotNull(w.items);
        assertTrue(w.items.isEmpty());
    }

    @Test
    public void testDeserialize_perPropertyAcceptSingleValueAsArray_singleString_returnsListWithOneElement() throws Exception {
        Wrapper w = mapper.readValue("{\"items\":\"abc\"}", Wrapper.class);
        assertNotNull(w.items);
        assertEquals(Arrays.asList("abc"), w.items);
    }

    @Test
    public void testDeserialize_perPropertyAcceptSingleValueAsArray_nullValue_returnsNull() throws Exception {
        Wrapper w = mapper.readValue("{\"items\":null}", Wrapper.class);
        assertNull(w.items);  // null property stays null
    }

    // ------------------------- exception path with forwarded reference -------------------------
    // Already tested above.

    // ------------------------- boundary: very large array? Not necessary.

    // ------------------------- additional branch: _delegateDeserializer != null -------------------------
    // Covered by testDeserialize_withDelegateCreator_usesDelegateDeserializer above.
}