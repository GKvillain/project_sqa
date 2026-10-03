package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // ---------- Helper types for default typing tests ----------

    static abstract class AbstractBase {
        public int id;
    }

    static class ConcreteDerived extends AbstractBase {
        public String name;
    }

    static final class FinalBean {
        public int value;
    }

    // ---------- Basic read/write tests ----------

    // Tests simple round-trip POJO serialization/deserialization
    @Test
    public void testReadValue_StringToPojo_returnsCorrectObject() throws IOException {
        String json = "{\"id\":1,\"name\":\"test\"}";
        ConcreteDerived result = mapper.readValue(json, ConcreteDerived.class);
        assertEquals(1, result.id);
        assertEquals("test", result.name);
    }

    // Tests reading JSON null tree returns NullNode
    @Test
    public void testReadTree_nullContent_returnsNullNode() throws IOException {
        JsonNode node = mapper.readTree("null");
        assertTrue(node.isNull());
    }

    // Tests writeValueAsString produces valid JSON
    @Test
    public void testWriteValueAsString_pojo_returnsJsonString() throws JsonProcessingException {
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 2;
        obj.name = "foo";
        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("\"id\":2"));
        assertTrue(json.contains("\"name\":\"foo\""));
    }

    // ---------- Default typing tests ----------

    // Tests that enableDefaultTyping(OBJECT_AND_NON_CONCRETE) adds type info for non-concrete types
    @Test
    public void testDefaultTyping_objectAndNonConcrete_includesTypeInfo() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 10;
        obj.name = "type";
        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("@class"));
    }

    // Tests that enableDefaultTyping excludes TreeNode subtypes (Issue #88)
    @Test
    public void testDefaultTyping_objectAndNonConcrete_excludesTreeNode() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        ObjectNode root = mapper.createObjectNode();
        root.put("x", 1);
        String json = mapper.writeValueAsString(root);
        // Should not contain @class because TreeNode is excluded
        assertFalse(json.contains("@class"));
    }

    // Tests that NON_CONCRETE_AND_ARRAYS includes arrays of non-concrete types
    @Test
    public void testDefaultTyping_nonConcreteAndArrays_includesArrayOfNonConcrete() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        List<AbstractBase> list = new ArrayList<>();
        ConcreteDerived d = new ConcreteDerived();
        d.id = 5;
        d.name = "array";
        list.add(d);
        String json = mapper.writeValueAsString(list);
        assertTrue(json.contains("@class"));
    }

    // Tests that NON_CONCRETE_AND_ARRAYS excludes arrays of final types
    @Test
    public void testDefaultTyping_nonConcreteAndArrays_excludesArrayOfFinal() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        List<String> list = new ArrayList<>();
        list.add("hello");
        String json = mapper.writeValueAsString(list);
        // String is final and concrete, so no type info
        assertFalse(json.contains("@class"));
    }

    // Tests that NON_FINAL excludes final types even if they are non-concrete? Actually final is concrete.
    @Test
    public void testDefaultTyping_nonFinal_excludesFinalType() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        FinalBean bean = new FinalBean();
        bean.value = 42;
        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("@class"));
    }

    // Tests that NON_FINAL excludes TreeNode subtypes
    @Test
    public void testDefaultTyping_nonFinal_excludesTreeNode() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ArrayNode arr = mapper.createArrayNode();
        arr.add(1);
        String json = mapper.writeValueAsString(arr);
        assertFalse(json.contains("@class"));
    }

    // Tests that enableDefaultTyping with EXTERNAL_PROPERTY throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEnableDefaultTyping_externalProperty_throwsException() {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, 
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    // Tests enableDefaultTypingAsProperty works with custom property name
    @Test
    public void testEnableDefaultTypingAsProperty_usesPropertyName() throws IOException {
        mapper.enableDefaultTypingAsProperty(DefaultTyping.OBJECT_AND_NON_CONCRETE, "type");
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 100;
        obj.name = "prop";
        String json = mapper.writeValueAsString(obj);
        assertTrue(json.contains("\"type\""));
    }

    // ---------- copy() tests ----------

    // Tests that copy() produces a mapper with same default typing configuration
    @Test
    public void testCopy_hasSameDefaultTyping() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ObjectMapper copy = mapper.copy();
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 1;
        obj.name = "copy";
        String jsonOrigin = mapper.writeValueAsString(obj);
        String jsonCopy = copy.writeValueAsString(obj);
        assertEquals(jsonOrigin, jsonCopy);
    }

    // Tests that modifying the original after copy does not affect copy
    @Test
    public void testCopy_isIndependent() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
        ObjectMapper copy = mapper.copy();
        mapper.disableDefaultTyping();
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 2;
        obj.name = "indep";
        String jsonOrigin = mapper.writeValueAsString(obj);
        String jsonCopy = copy.writeValueAsString(obj);
        // Original has no type info, copy still has
        assertFalse(jsonOrigin.contains("@class"));
        assertTrue(jsonCopy.contains("@class"));
    }

    // ---------- Mix-in test ----------

    // Tests that addMixIn overrides serialization behavior
    @Test
    public void testAddMixIn_overridesAnnotations() throws IOException {
        // Create a simple bean without any annotations
        mixin.Class1 = Class1.class; // dummy
        // Actually we need a concrete test: we'll use a mixin that adds @JsonIgnore on a property
        // Let's define a simple bean with getters
        mapper.addMixIn(SimpleBean.class, SimpleBeanMixin.class);
        SimpleBean bean = new SimpleBean();
        bean.x = 1;
        bean.y = "hidden";
        String json = mapper.writeValueAsString(bean);
        // With mixin, "y" should be ignored
        assertFalse(json.contains("hidden"));
    }

    // Helper classes for mixin test
    public static class SimpleBean {
        public int x;
        public String y;
    }

    // Mixin that adds @JsonIgnore on property "y"
    public abstract static class SimpleBeanMixin {
        @com.fasterxml.jackson.annotation.JsonIgnore
        public abstract String getY();
    }

    // ---------- Module registration test (simple) ----------

    // Tests that registerModule does not throw and returns the mapper
    @Test
    public void testRegisterModule_acceptsModule() {
        com.fasterxml.jackson.databind.Module mod = new SimpleModule();
        mapper.registerModule(mod);
        assertTrue(true); // no exception means success
    }

    static class SimpleModule extends com.fasterxml.jackson.databind.Module {
        @Override
        public String getModuleName() { return "TestModule"; }
        @Override
        public Version version() { return Version.unknownVersion(); }
        @Override
        public void setupModule(SetupContext context) { }
    }

    // ---------- Conversion test ----------

    // Tests convertValue with same type returns the same object (no conversion)
    @Test
    public void testConvertValue_sameType_returnsOriginal() {
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 7;
        obj.name = "same";
        Object result = mapper.convertValue(obj, ConcreteDerived.class);
        assertSame(obj, result); // should be the same instance
    }

    // Tests valueToTree on a POJO
    @Test
    public void testValueToTree_pojo_returnsJsonNode() {
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 3;
        obj.name = "tree";
        JsonNode root = mapper.valueToTree(obj);
        assertNotNull(root);
        assertEquals(3, root.get("id").asInt());
        assertEquals("tree", root.get("name").asText());
    }

    // Tests treeToValue with JsonNode cast shortcut
    @Test
    public void testTreeToValue_jsonNodeCast_returnsSameNode() {
        ObjectNode on = mapper.createObjectNode();
        on.put("key", "val");
        JsonNode result = mapper.treeToValue(on, JsonNode.class);
        assertSame(on, result);
    }

    // ---------- Additional tests for uncovered areas ----------

    // Tests polymorphic deserialization with default typing
    @Test
    public void testPolymorphicDeserialization_withDefaultTyping() throws IOException {
        mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 7;
        obj.name = "poly";
        String json = mapper.writeValueAsString(obj);
        // Deserialize as AbstractBase
        AbstractBase result = mapper.readValue(json, AbstractBase.class);
        assertTrue(result instanceof ConcreteDerived);
        assertEquals(7, result.id);
        assertEquals("poly", ((ConcreteDerived) result).name);
    }

    // Tests reading JSON from byte array
    @Test
    public void testReadValueFromBytes() throws IOException {
        String json = "{\"id\":10,\"name\":\"bytes\"}";
        byte[] bytes = json.getBytes("UTF-8");
        ConcreteDerived result = mapper.readValue(bytes, ConcreteDerived.class);
        assertEquals(10, result.id);
        assertEquals("bytes", result.name);
    }

    // Tests writing JSON to byte array
    @Test
    public void testWriteValueAsBytes() throws IOException {
        ConcreteDerived obj = new ConcreteDerived();
        obj.id = 20;
        obj.name = "byteout";
        byte[] bytes = mapper.writeValueAsBytes(obj);
        String json = new String(bytes, "UTF-8");
        assertTrue(json.contains("\"id\":20"));
    }

    // Tests readerForUpdating to update an existing object
    @Test
    public void testReaderForUpdating_updatesExisting() throws IOException {
        ConcreteDerived target = new ConcreteDerived();
        target.id = 1;
        target.name = "old";
        String json = "{\"id\":2,\"name\":\"new\"}";
        ConcreteDerived updated = mapper.readerForUpdating(target).readValue(json);
        assertSame(target, updated);
        assertEquals(2, target.id);
        assertEquals("new", target.name);
    }

    // Helper container for date format tests
    static class DateContainer {
        public java.util.Date date;
    }

    // Tests setting and using a date format for serialization
    @Test
    public void testDateFormat_serialization() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        java.util.Date date = sdf.parse("2023-01-15");
        DateContainer container = new DateContainer();
        container.date = date;
        String json = mapper.writeValueAsString(container);
        assertTrue(json.contains("\"2023-01-15\""));
    }

    // Tests deserialization with date format
    @Test
    public void testDateFormat_deserialization() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        String json = "{\"date\":\"2023-01-15\"}";
        DateContainer container = mapper.readValue(json, DateContainer.class);
        assertEquals(sdf.parse("2023-01-15"), container.date);
    }
}