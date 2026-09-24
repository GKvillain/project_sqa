package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectNodeTest {

    private JsonNodeFactory factory;
    private ObjectNode objectNode;

    @Before
    public void setUp() {
        factory = JsonNodeFactory.instance;
        objectNode = new ObjectNode(factory);
    }

    // Tests getNodeType and asToken for ObjectNode
    @Test
    public void testGetNodeTypeAndToken_default_returnsObjectAndStartObject() {
        assertEquals(JsonNodeType.OBJECT, objectNode.getNodeType());
        assertEquals(JsonToken.START_OBJECT, objectNode.asToken());
    }

    // Tests get and path with integer index (which should return null/missing)
    @Test
    public void testGetAndPath_intIndex_returnsNullAndMissing() {
        assertNull(objectNode.get(0));
        assertTrue(objectNode.path(0).isMissingNode());
    }

    // Tests set, get, size, elements, and fieldNames
    @Test
    public void testSetAndGet_validProperties_returnsCorrectNodesAndSize() {
        objectNode.set("str", factory.textNode("value"));
        objectNode.set("nullVal", null);

        assertEquals(2, objectNode.size());
        assertEquals("value", objectNode.get("str").asText());
        assertTrue(objectNode.get("nullVal").isNull());
        assertTrue(objectNode.path("nonExistent").isMissingNode());

        Iterator<String> fieldNames = objectNode.fieldNames();
        assertTrue(fieldNames.hasNext());
        assertEquals("str", fieldNames.next());
    }

    // Tests put primitives and wrapper types including null handling
    @Test
    public void testPut_primitiveAndWrapperTypes_storesValuesCorrectly() {
        objectNode.put("shortVal", (short) 1);
        objectNode.put("shortNull", (Short) null);
        objectNode.put("intVal", 100);
        objectNode.put("intNull", (Integer) null);
        objectNode.put("longVal", 1000L);
        objectNode.put("longNull", (Long) null);
        objectNode.put("floatVal", 1.5f);
        objectNode.put("floatNull", (Float) null);
        objectNode.put("doubleVal", 2.5d);
        objectNode.put("doubleNull", (Double) null);
        objectNode.put("bigDecimalVal", new BigDecimal("123.45"));
        objectNode.put("bigDecimalNull", (BigDecimal) null);
        objectNode.put("stringVal", "hello");
        objectNode.put("stringNull", (String) null);
        objectNode.put("boolVal", true);
        objectNode.put("boolNull", (Boolean) null);
        objectNode.put("bytesVal", new byte[]{1, 2, 3});
        objectNode.put("bytesNull", (byte[]) null);
        objectNode.putNull("explicitNull");
        objectNode.putPOJO("pojo", "plainStringPojo");

        assertEquals(1, objectNode.get("shortVal").asInt());
        assertTrue(objectNode.get("shortNull").isNull());
        assertEquals(100, objectNode.get("intVal").asInt());
        assertTrue(objectNode.get("intNull").isNull());
        assertEquals(1000L, objectNode.get("longVal").asLong());
        assertTrue(objectNode.get("longNull").isNull());
        assertEquals(1.5f, objectNode.get("floatVal").asDouble(), 0.001);
        assertTrue(objectNode.get("floatNull").isNull());
        assertEquals(2.5d, objectNode.get("doubleVal").asDouble(), 0.001);
        assertTrue(objectNode.get("doubleNull").isNull());
        assertEquals(new BigDecimal("123.45"), objectNode.get("bigDecimalVal").decimalValue());
        assertTrue(objectNode.get("bigDecimalNull").isNull());
        assertEquals("hello", objectNode.get("stringVal").asText());
        assertTrue(objectNode.get("stringNull").isNull());
        assertTrue(objectNode.get("boolVal").asBoolean());
        assertTrue(objectNode.get("boolNull").isNull());
        assertNotNull(objectNode.get("bytesVal"));
        assertTrue(objectNode.get("bytesNull").isNull());
        assertTrue(objectNode.get("explicitNull").isNull());
        assertNotNull(objectNode.get("pojo"));
    }

    // Tests putObject and putArray factory helper methods
    @Test
    public void testPutObjectAndPutArray_validCalls_createsAndAttachesNodes() {
        ObjectNode childObj = objectNode.putObject("childObj");
        ArrayNode childArr = objectNode.putArray("childArr");

        assertNotNull(childObj);
        assertNotNull(childArr);
        assertSame(childObj, objectNode.get("childObj"));
        assertSame(childArr, objectNode.get("childArr"));
    }

    // Tests with() creating new ObjectNode and returning existing ObjectNode
    @Test
    public void testWith_validCalls_createsOrReturnsObjectNode() {
        ObjectNode child = objectNode.with("sub");
        assertNotNull(child);
        assertSame(child, objectNode.with("sub"));
    }

    // Tests with() throwing UnsupportedOperationException when property is not ObjectNode
    @Test(expected = UnsupportedOperationException.class)
    public void testWith_nonObjectNodeProperty_throwsException() {
        objectNode.put("prop", 123);
        objectNode.with("prop");
    }

    // Tests withArray() creating new ArrayNode and returning existing ArrayNode
    @Test
    public void testWithArray_validCalls_createsOrReturnsArrayNode() {
        ArrayNode arr = objectNode.withArray("arr");
        assertNotNull(arr);
        assertSame(arr, objectNode.withArray("arr"));
    }

    // Tests withArray() throwing UnsupportedOperationException when property is not ArrayNode
    @Test(expected = UnsupportedOperationException.class)
    public void testWithArray_nonArrayNodeProperty_throwsException() {
        objectNode.put("prop", "text");
        objectNode.withArray("prop");
    }

    // Tests deepCopy on nested ObjectNodes
    @Test
    public void testDeepCopy_nestedObject_createsIndependentCopy() {
        objectNode.put("key", "value");
        ObjectNode child = objectNode.putObject("child");
        child.put("subKey", 42);

        ObjectNode copy = objectNode.deepCopy();
        assertEquals(objectNode, copy);
        assertNotSame(objectNode, copy);
        assertNotSame(objectNode.get("child"), copy.get("child"));

        copy.put("key", "modified");
        assertFalse(objectNode.get("key").asText().equals(copy.get("key").asText()));
    }

    // Tests findValue and findValues across hierarchy
    @Test
    public void testFindValueAndFindValues_nestedNodes_findsExpectedValues() {
        objectNode.put("target", "top");
        ObjectNode child = objectNode.putObject("child");
        child.put("target", "nested");
        child.put("other", "otherVal");

        JsonNode found = objectNode.findValue("target");
        assertNotNull(found);
        assertEquals("top", found.asText());

        JsonNode nonExistent = objectNode.findValue("notFound");
        assertNull(nonExistent);

        List<JsonNode> values = objectNode.findValues("target", null);
        assertEquals(2, values.size());
        assertEquals("top", values.get(0).asText());
        assertEquals("nested", values.get(1).asText());

        List<String> textValues = objectNode.findValuesAsText("target", null);
        assertEquals(2, textValues.size());
        assertEquals("top", textValues.get(0));
        assertEquals("nested", textValues.get(1));
    }

    // Tests findParent and findParents across hierarchy
    @Test
    public void testFindParentAndFindParents_nestedNodes_findsExpectedParents() {
        objectNode.put("topKey", 1);
        ObjectNode child = objectNode.putObject("child");
        child.put("nestedKey", 2);

        JsonNode parent1 = objectNode.findParent("topKey");
        assertSame(objectNode, parent1);

        JsonNode parent2 = objectNode.findParent("nestedKey");
        assertSame(child, parent2);

        assertNull(objectNode.findParent("nonExistent"));

        List<JsonNode> parents = objectNode.findParents("nestedKey", null);
        assertEquals(1, parents.size());
        assertSame(child, parents.get(0));
    }

    // Tests replace and setAll methods
    @Test
    public void testReplaceAndSetAll_variousInputs_updatesCorrectly() {
        assertNull(objectNode.replace("k1", factory.textNode("v1")));
        JsonNode old = objectNode.replace("k1", factory.textNode("v2"));
        assertEquals("v1", old.asText());
        assertEquals("v2", objectNode.get("k1").asText());

        objectNode.replace("k1", null);
        assertTrue(objectNode.get("k1").isNull());

        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("m1", factory.numberNode(10));
        map.put("m2", null);
        objectNode.setAll(map);
        assertEquals(10, objectNode.get("m1").asInt());
        assertTrue(objectNode.get("m2").isNull());

        ObjectNode other = new ObjectNode(factory);
        other.put("o1", "otherVal");
        objectNode.setAll(other);
        assertEquals("otherVal", objectNode.get("o1").asText());
    }

    // Tests remove, removeAll, without, and retain
    @Test
    public void testRemoveAndRetain_variousKeys_modifiesChildren() {
        objectNode.put("a", 1).put("b", 2).put("c", 3).put("d", 4);

        JsonNode removed = objectNode.remove("a");
        assertEquals(1, removed.asInt());
        assertNull(objectNode.remove("nonExistent"));

        objectNode.remove(Arrays.asList("b"));
        assertNull(objectNode.get("b"));

        objectNode.without("c");
        assertNull(objectNode.get("c"));

        objectNode.put("e", 5).put("f", 6);
        objectNode.without(Arrays.asList("e"));
        assertNull(objectNode.get("e"));

        objectNode.retain("d", "f");
        assertEquals(2, objectNode.size());
        assertNotNull(objectNode.get("d"));
        assertNotNull(objectNode.get("f"));

        objectNode.retain(Arrays.asList("d"));
        assertEquals(1, objectNode.size());
        assertNotNull(objectNode.get("d"));

        objectNode.removeAll();
        assertEquals(0, objectNode.size());
    }

    // Tests equals, hashCode, and toString
    @Test
    public void testEqualsAndHashCodeAndToString_validObjects_behavesCorrectly() {
        ObjectNode node1 = new ObjectNode(factory);
        node1.put("k", "v");

        ObjectNode node2 = new ObjectNode(factory);
        node2.put("k", "v");

        assertTrue(node1.equals(node1));
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals("{\"k\":\"v\"}", node1.toString());

        assertFalse(node1.equals(null));
        assertFalse(node1.equals("someString"));

        node2.put("k2", "v2");
        assertFalse(node1.equals(node2));
    }

    // Tests _at with JsonPointer
    @Test
    public void testAt_validPointer_returnsMatchingProperty() {
        objectNode.put("field", "val");
        JsonPointer ptr = JsonPointer.compile("/field");
        JsonNode result = objectNode._at(ptr);
        assertNotNull(result);
        assertEquals("val", result.asText());
    }

    // Tests custom map constructor
    @Test
    public void testConstructor_withMap_initializesCorrectly() {
        Map<String, JsonNode> kids = new LinkedHashMap<String, JsonNode>();
        kids.put("key", factory.textNode("value"));
        ObjectNode customNode = new ObjectNode(factory, kids);

        assertEquals(1, customNode.size());
        assertEquals("value", customNode.get("key").asText());
    }
}