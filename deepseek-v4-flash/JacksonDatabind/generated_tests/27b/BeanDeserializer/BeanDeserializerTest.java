package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.impl.*;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.util.*;

public class BeanDeserializerTest {

    // ---------- Helper beans ----------

    public static class SimpleBean {
        public int id;
        public String name;
    }

    public static class BeanWithCreator {
        int x;
        String y;

        @JsonCreator
        public BeanWithCreator(@JsonProperty("x") int x, @JsonProperty("y") String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class BeanWithId {
        public int value;
        public BeanWithId next;
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class BeanWithView {
        @JsonView(Views.Public.class)
        public int a;
        @JsonView(Views.Internal.class)
        public int b;
    }

    public static class BeanWithAnySetter {
        public int id;
        protected Map<String, Object> other = new HashMap<>();

        @JsonAnySetter
        public void set(String key, Object value) {
            other.put(key, value);
        }
    }

    public static class Address {
        public String street;
        public String city;
    }

    public static class BeanWithUnwrapped {
        public String name;
        @JsonUnwrapped
        public Address address;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(value = SubBean.class, name = "sub") })
    public static class BaseBean {
        public int baseField;
    }

    public static class SubBean extends BaseBean {
        public int subField;
    }

    // --- New helper beans for additional coverage ---

    @JsonIgnoreProperties({"ignoreMe"})
    public static class BeanWithIgnoreProperties {
        public int id;
        public String name;
        public String ignoreMe;
    }

    public static class BeanWithAnyProps {
        public int id;
        protected Map<String, Object> anyProps = new HashMap<>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            anyProps.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> getAny() {
            return anyProps;
        }
    }

    // ---------- Original Test cases ----------

    // Tests normal deserialization of a simple bean
    @Test
    public void testDeserialize_normalObject_returnsCorrectBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"name\":\"test\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
    }

    // Tests deserialization of an empty object
    @Test
    public void testDeserialize_emptyObject_returnsDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(0, bean.id);
        assertNull(bean.name);
    }

    // Tests that a VALUE_STRING token results in a mapping exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_stringToken_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "\"hello\"";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests that a VALUE_NUMBER_INT token results in a mapping exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_numberToken_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "123";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests that a START_ARRAY token results in a mapping exception (no delegating creator)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_arrayToken_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[1,2]";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests that a VALUE_TRUE token results in a mapping exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_booleanToken_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "true";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests unknown property with default setting (fail on unknown)
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unknownProperty_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"unknown\":\"val\"}";
        mapper.readValue(json, SimpleBean.class);
    }

    // Tests unknown property when ignore is enabled
    @Test
    public void testDeserialize_unknownProperty_ignored() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"unknown\":\"val\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
    }

    // Tests property-based creator (non-default construction)
    @Test
    public void testDeserialize_propertyBasedCreator_returnsCorrect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"x\":10,\"y\":\"hello\"}";
        BeanWithCreator bean = mapper.readValue(json, BeanWithCreator.class);
        assertEquals(10, bean.getX());
        assertEquals("hello", bean.getY());
    }

    // Tests property-based creator with extra unknown property (ignored)
    @Test
    public void testDeserialize_propertyBasedCreatorWithUnknown_ok() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        String json = "{\"x\":1,\"extra\":\"ignore\"}";
        BeanWithCreator bean = mapper.readValue(json, BeanWithCreator.class);
        assertEquals(1, bean.getX());
    }

    // Tests deserialization with object id references
    @Test
    public void testDeserialize_withObjectIdReference_works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@id\":1,\"value\":10,\"next\":{\"@id\":2,\"value\":20}}";
        BeanWithId bean = mapper.readValue(json, BeanWithId.class);
        assertEquals(10, bean.value);
        assertNotNull(bean.next);
        assertEquals(20, bean.next.value);
    }

    // Tests view processing (only properties visible in the active view are set)
    @Test
    public void testDeserialize_withView_returnsOnlyVisibleProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":1,\"b\":2}";
        ObjectReader reader = mapper.readerFor(BeanWithView.class).withView(Views.Public.class);
        BeanWithView bean = reader.readValue(json);
        assertEquals(1, bean.a);
        assertEquals(0, bean.b);
    }

    // Tests non-vanilla processing (any setter) – forces _vanillaProcessing false
    @Test
    public void testDeserialize_withAnySetter_nonVanilla() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"extra\":\"val\"}";
        BeanWithAnySetter bean = mapper.readValue(json, BeanWithAnySetter.class);
        assertEquals(1, bean.id);
        assertEquals("val", bean.other.get("extra"));
    }

    // Tests unwrapped property handling
    @Test
    public void testDeserialize_withUnwrappedProperty_works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"john\",\"street\":\"123 Main\",\"city\":\"NYC\"}";
        BeanWithUnwrapped bean = mapper.readValue(json, BeanWithUnwrapped.class);
        assertEquals("john", bean.name);
        assertNotNull(bean.address);
        assertEquals("123 Main", bean.address.street);
        assertEquals("NYC", bean.address.city);
    }

    // Tests polymorphic deserialization with external type id
    @Test
    public void testDeserialize_polymorphicType_works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"sub\",\"baseField\":1,\"subField\":2}";
        BaseBean bean = mapper.readValue(json, BaseBean.class);
        assertTrue(bean instanceof SubBean);
        SubBean sub = (SubBean) bean;
        assertEquals(1, sub.baseField);
        assertEquals(2, sub.subField);
    }

    // Tests updating an existing bean (deserialize with bean argument)
    @Test
    public void testDeserialize_updateExistingBean_works() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean existing = new SimpleBean();
        existing.id = 100;
        existing.name = "old";
        SimpleBean updated = mapper.readerForUpdating(existing).readValue("{\"name\":\"new\"}");
        assertSame(existing, updated);
        assertEquals(100, existing.id);
        assertEquals("new", existing.name);
    }

    // ---------- New Test cases for uncovered areas ----------

    // Tests handling of null field values (primitive and object)
    @Test
    public void testDeserialize_nullFields_handlesCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // For primitive int, null becomes 0; for String, null stays null
        String json = "{\"id\":null,\"name\":null}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(0, bean.id);
        assertNull(bean.name);
    }

    // Tests @JsonIgnoreProperties annotation ignoring specified properties
    @Test
    public void testDeserialize_withJsonIgnoreProperties_ignoresSpecified() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"name\":\"test\",\"ignoreMe\":\"shouldBeIgnored\"}";
        BeanWithIgnoreProperties bean = mapper.readValue(json, BeanWithIgnoreProperties.class);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
        assertNull(bean.ignoreMe); // should be ignored
    }

    // Tests full any-setter/any-getter support (non-vanilla with both annotations)
    @Test
    public void testDeserialize_withAnySetterAndGetter_fullAnySupport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":5,\"x\":10,\"y\":\"hello\"}";
        BeanWithAnyProps bean = mapper.readValue(json, BeanWithAnyProps.class);
        assertEquals(5, bean.id);
        // Check that extra properties are stored in the map
        assertEquals(10, bean.getAny().get("x"));
        assertEquals("hello", bean.getAny().get("y"));
        // Also verify that the any-getter map is populated correctly (non-null)
        assertNotNull(bean.getAny());
    }

    // Tests deserialization using TokenBuffer (simulating buffered tokens)
    @Test
    public void testDeserialize_usingTokenBuffer_valid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Build a TokenBuffer with valid JSON structure
        TokenBuffer buf = new TokenBuffer(mapper, false);
        buf.writeStartObject();
        buf.writeNumberField("id", 42);
        buf.writeStringField("name", "bufferTest");
        buf.writeEndObject();
        // Deserialize from the TokenBuffer's parser
        JsonParser parser = buf.asParser();
        SimpleBean bean = mapper.readValue(parser, SimpleBean.class);
        assertEquals(42, bean.id);
        assertEquals("bufferTest", bean.name);
    }
}