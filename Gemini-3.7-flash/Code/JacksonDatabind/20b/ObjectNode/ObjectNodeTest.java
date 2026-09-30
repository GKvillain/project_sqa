package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.RawValue;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    // Tests initial state and core node type properties
    @Test
    public void testGetNodeTypeAndToken_emptyNode_returnsObjectAndStartObject() {
        assertEquals(JsonNodeType.OBJECT, objectNode.getNodeType());
        assertEquals(JsonToken.START_OBJECT, objectNode.asToken());
        assertEquals(0, objectNode.size());
        assertFalse(objectNode.elements().hasNext());
        assertFalse(objectNode.fieldNames().hasNext());
    }

    // Tests get and path methods with index and fieldName
    @Test
    public void testGetAndPath_existingAndNonExisting_returnsExpectedNodes() {
        objectNode.put("name", "John");

        assertNull(objectNode.get(0));
        assertTrue(objectNode.path(0).isMissingNode());

        assertEquals("John", objectNode.get("name").asText());
        assertEquals("John", objectNode.path("name").asText());

        assertNull(objectNode.get("nonExisting"));
        assertTrue(objectNode.path("nonExisting").isMissingNode());
    }

    // Tests _at method with JsonPointer
    @Test
    public void testAt_jsonPointer_resolvesMatchingProperty() {
        objectNode.put("key", "value");
        JsonPointer ptr = JsonPointer.compile("/key");
        JsonNode result = objectNode._at(ptr);
        assertNotNull(result);
        assertEquals("value", result.asText());
    }

    // Tests deepCopy creating an independent copy
    @Test
    public void testDeepCopy_nestedObject_createsIndependentCopy() {
        ObjectNode child = objectNode.putObject("child");
        child.put("innerKey", "innerVal");

        ObjectNode copy = objectNode.deepCopy();
        assertEquals(objectNode, copy);
        assertNotSame(objectNode, copy);
        assertNotSame(objectNode.get("child"), copy.get("child"));

        copy.put("newField", 123);
        assertNull(objectNode.get("newField"));
    }

    // Tests fields and elements iterators
    @Test
    public void testFieldsAndElements_populatedNode_iteratesCorrectly() {
        objectNode.put("a", 1);
        objectNode.put("b", 2);

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        assertTrue(fields.hasNext());
        Map.Entry<String, JsonNode> first = fields.next();
        assertEquals("a", first.getKey());
        assertEquals(1, first.getValue().asInt());

        Iterator<JsonNode> elements = objectNode.elements();
        assertTrue(elements.hasNext());
        assertEquals(1, elements.next().asInt());
    }

    // Tests with() method creates child ObjectNode or returns existing
    @Test
    public void testWith_validProperty_returnsObjectNode() {
        ObjectNode child = objectNode.with("subObj");
        assertNotNull(child);
        assertSame(child, objectNode.with("subObj"));
    }

    // Tests with() method throwing exception when property is not an ObjectNode
    @Test(expected = UnsupportedOperationException.class)
    public void testWith_nonObjectNodeProperty_throwsException() {
        objectNode.put("subProp", "stringValue");
        objectNode.with("subProp");
    }

    // Tests withArray() method creates ArrayNode or returns existing
    @Test
    public void testWithArray_validProperty_returnsArrayNode() {
        ArrayNode arr = objectNode.withArray("items");
        assertNotNull(arr);
        assertSame(arr, objectNode.withArray("items"));
    }

    // Tests withArray() method throwing exception when property is not an ArrayNode
    @Test(expected = UnsupportedOperationException.class)
    public void testWithArray_nonArrayNodeProperty_throwsException() {
        objectNode.put("items", 123);
        objectNode.withArray("items");
    }

    // Tests finding values recursively in tree
    @Test
    public void testFindValueAndValues_nestedStructure_returnsMatches() {
        objectNode.put("target", "top");
        ObjectNode child = objectNode.putObject("sub");
        child.put("target", "nested");

        JsonNode foundFirst = objectNode.findValue("target");
        assertNotNull(foundFirst);
        assertEquals("top", foundFirst.asText());

        List<JsonNode> foundList = objectNode.findValues("target", null);
        assertEquals(2, foundList.size());
        assertEquals("top", foundList.get(0).asText());
        assertEquals("nested", foundList.get(1).asText());

        List<String> textList = objectNode.findValuesAsText("target", null);
        assertEquals(2, textList.size());
        assertEquals("top", textList.get(0));
        assertEquals("nested", textList.get(1));

        assertNull(objectNode.findValue("nonExistent"));
    }

    // Tests finding parent nodes recursively
    @Test
    public void testFindParentAndParents_nestedStructure_returnsParentNodes() {
        ObjectNode child = objectNode.putObject("sub");
        child.put("innerTarget", "val");

        JsonNode parent = objectNode.findParent("innerTarget");
        assertSame(child, parent);

        List<JsonNode> parents = objectNode.findParents("innerTarget", null);
        assertEquals(1, parents.size());
        assertSame(child, parents.get(0));

        assertNull(objectNode.findParent("nonExistent"));
    }

    // Tests set, setAll and replace mutator methods
    @Test
    public void testSetAndSetAllAndReplace_variousInputs_updatesMapCorrectly() {
        objectNode.set("k1", null);
        assertTrue(objectNode.get("k1").isNullNode());

        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("k2", factory.textNode("v2"));
        map.put("k3", null);
        objectNode.setAll(map);

        assertEquals("v2", objectNode.get("k2").asText());
        assertTrue(objectNode.get("k3").isNullNode());

        ObjectNode other = new ObjectNode(factory);
        other.put("k4", "v4");
        objectNode.setAll(other);
        assertEquals("v4", objectNode.get("k4").asText());

        JsonNode old = objectNode.replace("k4", factory.textNode("v4_new"));
        assertEquals("v4", old.asText());
        assertEquals("v4_new", objectNode.get("k4").asText());

        JsonNode oldNull = objectNode.replace("k4", null);
        assertEquals("v4_new", oldNull.asText());
        assertTrue(objectNode.get("k4").isNullNode());
    }

    // Tests without, remove, removeAll and retain methods
    @Test
    public void testWithoutAndRemoveAndRetain_validKeys_modifiesChildren() {
        objectNode.put("a", 1).put("b", 2).put("c", 3).put("d", 4);

        objectNode.without("a");
        assertNull(objectNode.get("a"));

        objectNode.without(Arrays.asList("b"));
        assertNull(objectNode.get("b"));

        JsonNode removed = objectNode.remove("c");
        assertNotNull(removed);
        assertEquals(3, removed.asInt());
        assertNull(objectNode.remove("c"));

        objectNode.put("e", 5).put("f", 6);
        objectNode.remove(Arrays.asList("d"));
        assertNull(objectNode.get("d"));

        objectNode.retain("e");
        assertEquals(1, objectNode.size());
        assertNotNull(objectNode.get("e"));
        assertNull(objectNode.get("f"));

        objectNode.removeAll();
        assertEquals(0, objectNode.size());
    }

    // Tests primitive and object typed put methods including null handling
    @Test
    public void testTypedPutMethods_primitivesAndWrappers_storesAppropriateNodes() {
        objectNode.put("shortP", (short) 1);
        objectNode.put("shortW", (Short) null);
        objectNode.put("shortW2", Short.valueOf((short) 2));

        objectNode.put("intP", 10);
        objectNode.put("intW", (Integer) null);
        objectNode.put("intW2", Integer.valueOf(20));

        objectNode.put("longP", 100L);
        objectNode.put("longW", (Long) null);
        objectNode.put("longW2", Long.valueOf(200L));

        objectNode.put("floatP", 1.5f);
        objectNode.put("floatW", (Float) null);
        objectNode.put("floatW2", Float.valueOf(2.5f));

        objectNode.put("doubleP", 3.14);
        objectNode.put("doubleW", (Double) null);
        objectNode.put("doubleW2", Double.valueOf(6.28));

        objectNode.put("bigDec", (BigDecimal) null);
        objectNode.put("bigDec2", new BigDecimal("123.45"));

        objectNode.put("str", (String) null);
        objectNode.put("str2", "hello");

        objectNode.put("boolP", true);
        objectNode.put("boolW", (Boolean) null);
        objectNode.put("boolW2", Boolean.FALSE);

        objectNode.put("bytes", (byte[]) null);
        objectNode.put("bytes2", new byte[]{1, 2, 3});

        objectNode.putNull("nullKey");
        objectNode.putPOJO("pojoKey", "aPojo");

        assertTrue(objectNode.get("shortW").isNullNode());
        assertEquals((short) 2, objectNode.get("shortW2").shortValue());

        assertTrue(objectNode.get("intW").isNullNode());
        assertEquals(20, objectNode.get("intW2").intValue());

        assertTrue(objectNode.get("longW").isNullNode());
        assertEquals(200L, objectNode.get("longW2").longValue());

        assertTrue(objectNode.get("floatW").isNullNode());
        assertEquals(2.5f, objectNode.get("floatW2").floatValue(), 0.001f);

        assertTrue(objectNode.get("doubleW").isNullNode());
        assertEquals(6.28, objectNode.get("doubleW2").doubleValue(), 0.001);

        assertTrue(objectNode.get("bigDec").isNullNode());
        assertEquals(new BigDecimal("123.45"), objectNode.get("bigDec2").decimalValue());

        assertTrue(objectNode.get("str").isNullNode());
        assertEquals("hello", objectNode.get("str2").textValue());

        assertTrue(objectNode.get("boolW").isNullNode());
        assertFalse(objectNode.get("boolW2").booleanValue());

        assertTrue(objectNode.get("bytes").isNullNode());
        assertTrue(objectNode.get("bytes2").isBinary());

        assertTrue(objectNode.get("nullKey").isNullNode());
        assertTrue(objectNode.get("pojoKey").isPojo());
    }

    // Tests equals, hashCode and toString methods
    @Test
    public void testEqualsHashCodeAndToString_variousStates_behaveCorrectly() {
        ObjectNode node1 = new ObjectNode(factory);
        ObjectNode node2 = new ObjectNode(factory);

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("string"));
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());

        node1.put("k", "v");
        assertFalse(node1.equals(node2));

        node2.put("k", "v");
        assertTrue(node1.equals(node2));
        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals("{\"k\":\"v\"}", node1.toString());

        node1.put("k2", "v2");
        assertEquals("{\"k\":\"v\",\"k2\":\"v2\"}", node1.toString());
    }

    // Tests deprecated put and putAll methods
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedPutAndPutAll_validInputs_setsValuesProperly() {
        objectNode.put("rawNull", (JsonNode) null);
        assertTrue(objectNode.get("rawNull").isNullNode());

        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("m1", factory.numberNode(10));
        objectNode.putAll(map);
        assertEquals(10, objectNode.get("m1").asInt());

        ObjectNode other = new ObjectNode(factory);
        other.put("o1", "text");
        objectNode.putAll(other);
        assertEquals("text", objectNode.get("o1").asText());
    }

    // Tests constructor with existing children map
    @Test
    public void testConstructor_withMap_initializesCorrectly() {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("entryKey", factory.textNode("entryVal"));
        ObjectNode nodeWithMap = new ObjectNode(factory, map);
        assertEquals(1, nodeWithMap.size());
        assertEquals("entryVal", nodeWithMap.get("entryKey").asText());
    }

    // Tests isEmpty method
    @Test
    public void testIsEmpty_emptyAndNonEmpty_returnsExpectedResult() {
        assertTrue(objectNode.isEmpty(null));
        objectNode.put("key", "value");
        assertFalse(objectNode.isEmpty(null));
    }

    // Tests findPath method
    @Test
    public void testFindPath_existingAndNonExisting_returnsFoundOrMissingNode() {
        objectNode.put("target", "val");
        JsonNode found = objectNode.findPath("target");
        assertEquals("val", found.asText());

        JsonNode notFound = objectNode.findPath("nonExistent");
        assertTrue(notFound.isMissingNode());
    }

    // Tests putArray method
    @Test
    public void testPutArray_validProperty_createsAndReturnsArrayNode() {
        ArrayNode arr = objectNode.putArray("arrKey");
        assertNotNull(arr);
        assertTrue(objectNode.get("arrKey").isArray());
        assertSame(arr, objectNode.get("arrKey"));
    }

    // Tests BigInteger and RawValue put methods
    @Test
    public void testPutBigIntegerAndRawValue_validAndNull_storesCorrectly() {
        objectNode.put("bigInt", BigInteger.valueOf(100));
        objectNode.put("bigIntNull", (BigInteger) null);
        assertEquals(BigInteger.valueOf(100), objectNode.get("bigInt").bigIntegerValue());
        assertTrue(objectNode.get("bigIntNull").isNullNode());

        RawValue raw = new RawValue("[1,2,3]");
        objectNode.putRawValue("raw", raw);
        assertTrue(objectNode.get("raw") instanceof ValueNode);

        objectNode.putRawValue("rawNull", null);
        assertTrue(objectNode.get("rawNull").isNullNode());
    }

    // Tests retain method with Collection
    @Test
    public void testRetain_collection_keepsOnlySpecifiedKeys() {
        objectNode.put("k1", 1).put("k2", 2).put("k3", 3);
        objectNode.retain(Arrays.asList("k1", "k3"));
        assertEquals(2, objectNode.size());
        assertNotNull(objectNode.get("k1"));
        assertNull(objectNode.get("k2"));
        assertNotNull(objectNode.get("k3"));
    }

    // Tests equals with custom Comparator
    @Test
    public void testEquals_withComparator_comparesProperly() {
        ObjectNode n1 = factory.objectNode();
        n1.put("a", 1);
        ObjectNode n2 = factory.objectNode();
        n2.put("a", 1);

        Comparator<JsonNode> comp = new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                return o1.equals(o2) ? 0 : 1;
            }
        };

        assertTrue(n1.equals(comp, n2));
        n2.put("b", 2);
        assertFalse(n1.equals(comp, n2));
        assertFalse(n1.equals(comp, factory.nullNode()));
    }

    // Tests serialization through ObjectMapper
    @Test
    public void testSerialization_objectNode_serializesToJsonString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        objectNode.put("prop", "val");
        String json = mapper.writeValueAsString(objectNode);
        assertEquals("{\"prop\":\"val\"}", json);
    }

    // Tests findValues, findValuesAsText, and findParents with non-null accumulator lists
    @Test
    public void testFindMethods_withAccumulatorList_populatesProvidedList() {
        objectNode.put("k", "v1");
        ObjectNode child = objectNode.putObject("child");
        child.put("k", "v2");

        List<JsonNode> valList = new ArrayList<JsonNode>();
        List<JsonNode> returnedValList = objectNode.findValues("k", valList);
        assertSame(valList, returnedValList);
        assertEquals(2, valList.size());

        List<String> txtList = new ArrayList<String>();
        List<String> returnedTxtList = objectNode.findValuesAsText("k", txtList);
        assertSame(txtList, returnedTxtList);
        assertEquals(2, txtList.size());

        List<JsonNode> parentList = new ArrayList<JsonNode>();
        List<JsonNode> returnedParentList = objectNode.findParents("k", parentList);
        assertSame(parentList, returnedParentList);
        assertEquals(2, parentList.size());
    }
}