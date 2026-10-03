package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.node.*;

import org.junit.Before;
import org.junit.Test;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ---------- Helper classes for tests ----------
    public static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() {}

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    public static class BeanWithObject {
        public Object data;
        public BeanWithObject() {}
        public BeanWithObject(Object d) { data = d; }
        public Object getData() { return data; }
    }

    public static class BeanWithString {
        public String text;
        public BeanWithString() {}
        public BeanWithString(String t) { text = t; }
        public String getText() { return text; }
    }

    public interface MyInterface {
        String getValue();
    }
    public static class MyImpl implements MyInterface {
        public String value;
        public MyImpl() {}
        public MyImpl(String v) { value = v; }
        public String getValue() { return value; }
    }

    public static class BeanWithInterface {
        public MyInterface item;
        public BeanWithInterface() {}
        public BeanWithInterface(MyInterface i) { item = i; }
        public MyInterface getItem() { return item; }
    }

    // ---------- Normal cases ----------

    // Tests basic serialization of a simple POJO
    @Test
    public void testWriteValue_pojo_returnsCorrectJson() throws Exception {
        SimpleBean bean = new SimpleBean("test", 42);
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":42"));
    }

    // Tests basic deserialization of simple POJO
    @Test
    public void testReadValue_jsonString_returnsPojo() throws Exception {
        String json = "{\"name\":\"test\",\"value\":42}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals("test", bean.getName());
        assertEquals(42, bean.getValue());
    }

    // ---------- Default typing tests ----------

    // Tests that with JAVA_LANG_OBJECT only Object-typed properties are typed
    @Test
    public void testEnableDefaultTyping_objectProperty_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        BeanWithObject bean = new BeanWithObject("hello");
        String json = mapper.writeValueAsString(bean);
        // property "data" declared as Object -> should have @class
        assertTrue("Expected type info for Object property", json.contains("@class"));
    }

    // Tests that with JAVA_LANG_OBJECT String-typed properties are not typed
    @Test
    public void testEnableDefaultTyping_stringProperty_noTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT);
        BeanWithString bean = new BeanWithString("test");
        String json = mapper.writeValueAsString(bean);
        // property "text" declared as String -> no type info
        assertFalse("Expected no type info for String property", json.contains("@class"));
    }

    // Tests that with OBJECT_AND_NON_CONCRETE, TreeNode (JsonNode) should NOT be typed
    // This test detects defect #17b: buggy version adds type info for TreeNode
    @Test
    public void testEnableDefaultTyping_treeNode_noTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        ObjectNode node = mapper.createObjectNode();
        node.put("key", "value");
        String json = mapper.writeValueAsString(node);
        // TreeNode is concrete, should NOT get @class; buggy version includes it
        assertFalse("TreeNode should not have type info with OBJECT_AND_NON_CONCRETE", json.contains("@class"));
    }

    // Tests that with OBJECT_AND_NON_CONCRETE, interface-typed property gets type info
    @Test
    public void testEnableDefaultTyping_interfaceProperty_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        BeanWithInterface bean = new BeanWithInterface(new MyImpl("data"));
        String json = mapper.writeValueAsString(bean);
        // declared type MyInterface (abstract) -> should get @class
        assertTrue("Expected type info for interface property", json.contains("@class"));
    }

    // Tests that with NON_CONCRETE_AND_ARRAYS, array property gets type info
    @Test
    public void testEnableDefaultTyping_arrayProperty_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        // Use a wrapper with Object[] property
        // We can test by serializing an array directly (root value):
        Object[] array = new Object[]{"string", 123};
        String json = mapper.writeValueAsString(array);
        // Array of non-concrete content should get @class? Actually the array type itself is considered.
        // For root value of array, the type is Object[], which is concrete? It's array type.
        // useForType for NON_CONCRETE_AND_ARRAYS: while array, get content type; then check.
        // If content is Object, it will be typed.
        // But simpler: we can create a bean with List<Object> property?
        // For simplicity, use direct array.
        // Expected: type info should appear (because array of non-concrete content).
        assertTrue("Expected type info for array of Object", json.contains("@class"));
    }

    // Tests that with NON_FINAL, final type (String) is not typed
    @Test
    public void testEnableDefaultTyping_finalType_noTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        String json = mapper.writeValueAsString("test");
        // String is final -> no type info
        assertFalse("Final type should not have type info", json.contains("@class"));
    }

    // Tests that with NON_FINAL, non-final type (ArrayList) gets type info
    @Test
    public void testEnableDefaultTyping_nonFinalType_includesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        List<String> list = new ArrayList<String>();
        list.add("a");
        String json = mapper.writeValueAsString(list);
        // ArrayList is not final -> should have type info
        assertTrue("Non-final type should have type info", json.contains("@class"));
    }

    // Tests that after disabling default typing, no type info is added
    @Test
    public void testDisableDefaultTyping_removesTypeInfo() throws Exception {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.disableDefaultTyping();
        BeanWithObject bean = new BeanWithObject("test");
        String json = mapper.writeValueAsString(bean);
        assertFalse("After disable, no type info", json.contains("@class"));
    }

    // ---------- Configuration tests ----------

    // Tests that configure(MapperFeature) works
    @Test
    public void testConfigure_mapperFeature_affectsSorting() throws Exception {
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        SimpleBean bean = new SimpleBean("z", 1);
        String json = mapper.writeValueAsString(bean);
        // Expect properties sorted: name before value
        int nameIdx = json.indexOf("\"name\"");
        int valueIdx = json.indexOf("\"value\"");
        assertTrue("name should appear before value", nameIdx < valueIdx);
    }

    // Tests copy constructor
    @Test
    public void testCopy_createsIndependentMapper() throws Exception {
        ObjectMapper copy = mapper.copy();
        assertNotNull(copy);
        assertNotSame(mapper, copy);
        // Verify configurations are independent
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertFalse("Copy should not be affected", copy.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    // Tests mix-in annotations
    @Test
    public void testAddMixInAnnotations_affectsSerialization() throws Exception {
        // Use a mixin to rename a property
        mapper.addMixIn(SimpleBean.class, MixInForRename.class);
        SimpleBean bean = new SimpleBean("test", 10);
        String json = mapper.writeValueAsString(bean);
        // With mixin, property "name" should become "renamed"
        assertTrue(json.contains("\"renamed\""));
        assertFalse(json.contains("\"name\""));
    }

    // Mixin class
    @JsonPropertyOrder(alphabetic = true)
    public abstract static class MixInForRename {
        @JsonProperty("renamed")
        abstract String getName();
    }

    // Tests canSerialize
    @Test
    public void testCanSerialize_returnsTrueForKnownType() throws Exception {
        assertTrue(mapper.canSerialize(SimpleBean.class));
        assertTrue(mapper.canSerialize(String.class));
        assertFalse(mapper.canSerialize(SimpleBean.class)); // redundant but ok
    }

    // Tests canDeserialize
    @Test
    public void testCanDeserialize_returnsTrueForKnownType() throws Exception {
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);
        assertTrue(mapper.canDeserialize(type));
    }

    // ---------- Edge / Invalid cases ----------

    // Tests reading from empty string should throw JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testReadValue_emptyString_throwsException() throws Exception {
        mapper.readValue("", SimpleBean.class);
    }

    // Tests reading from null string should throw exception (mapper throws IllegalArgumentException or NPE)
    @Test(expected = IllegalArgumentException.class)
    public void testReadValue_nullString_throwsException() throws Exception {
        mapper.readValue((String) null, SimpleBean.class);
    }

    // Tests serialization of null value
    @Test
    public void testWriteValue_null_returnsNullNodeJson() throws Exception {
        // writeValueAsString(null) should produce "null"
        String json = mapper.writeValueAsString(null);
        assertEquals("null", json);
    }

    // Tests readValue for null JSON token
    @Test
    public void testReadValue_nullJson_returnsNull() throws Exception {
        SimpleBean bean = mapper.readValue("null", SimpleBean.class);
        assertNull(bean);
    }

    // Tests enabling default typing with As.PROPERTY and custom property name
    @Test
    public void testEnableDefaultTypingAsProperty_customPropertyName() throws Exception {
        mapper.enableDefaultTypingAsProperty(DefaultTyping.OBJECT_AND_NON_CONCRETE, "type");
        BeanWithObject bean = new BeanWithObject("hello");
        String json = mapper.writeValueAsString(bean);
        assertTrue("Expected custom type property 'type'", json.contains("\"type\""));
    }
}