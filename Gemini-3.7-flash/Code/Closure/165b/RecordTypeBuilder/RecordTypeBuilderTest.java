package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordTypeBuilderTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  // Tests building an empty record type returns the Object type
  @Test
  public void testBuild_emptyBuilder_returnsNativeObjectType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType result = builder.build();

    assertNotNull(result);
    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
    assertFalse(result.isRecordType());
  }

  // Tests adding a single property returns the builder and builds a valid RecordType
  @Test
  public void testAddProperty_singleProperty_returnsBuilderAndCreatesRecordType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = new Node(0);

    RecordTypeBuilder returnedBuilder = builder.addProperty("propA", numberType, node);

    assertSame(builder, returnedBuilder);
    JSType result = builder.build();
    assertNotNull(result);
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("propA"));
    assertEquals(numberType, recordType.getPropertyType("propA"));
    assertEquals(node, recordType.getPropertyNode("propA"));
  }

  // Tests method chaining when adding multiple distinct properties
  @Test
  public void testAddProperty_multipleProperties_chainsSuccessfully() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    Node node3 = new Node(3);

    RecordTypeBuilder resultBuilder = builder
        .addProperty("foo", numberType, node1)
        .addProperty("bar", stringType, node2)
        .addProperty("baz", booleanType, node3);

    assertSame(builder, resultBuilder);

    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("foo"));
    assertTrue(recordType.hasProperty("bar"));
    assertTrue(recordType.hasProperty("baz"));
    assertEquals(numberType, recordType.getPropertyType("foo"));
    assertEquals(stringType, recordType.getPropertyType("bar"));
    assertEquals(booleanType, recordType.getPropertyType("baz"));
  }

  // Tests adding a duplicate property name returns null
  @Test
  public void testAddProperty_duplicatePropertyName_returnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = new Node(0);
    Node node2 = new Node(0);

    RecordTypeBuilder firstAdd = builder.addProperty("duplicateProp", numberType, node1);
    RecordTypeBuilder secondAdd = builder.addProperty("duplicateProp", stringType, node2);

    assertNotNull(firstAdd);
    assertNull(secondAdd);
  }

  // Tests building after attempting to add a duplicate property keeps the first definition
  @Test
  public void testBuild_afterDuplicateProperty_retainsOriginalProperty() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = new Node(1);
    Node node2 = new Node(2);

    builder.addProperty("x", numberType, node1);
    builder.addProperty("x", stringType, node2);

    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertEquals(numberType, recordType.getPropertyType("x"));
    assertEquals(node1, recordType.getPropertyNode("x"));
  }

  // Tests adding a property with empty string as name
  @Test
  public void testAddProperty_emptyStringName_success() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = new Node(0);

    RecordTypeBuilder returnedBuilder = builder.addProperty("", stringType, node);

    assertSame(builder, returnedBuilder);
    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty(""));
    assertEquals(stringType, recordType.getPropertyType(""));
  }

  // Tests adding a property with null type and null node
  @Test
  public void testAddProperty_nullTypeAndNode_success() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);

    RecordTypeBuilder returnedBuilder = builder.addProperty("nullProp", null, null);

    assertSame(builder, returnedBuilder);
    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("nullProp"));
    assertNull(recordType.getPropertyType("nullProp"));
    assertNull(recordType.getPropertyNode("nullProp"));
  }

  // Tests RecordProperty getters directly
  @Test
  public void testRecordProperty_getters_returnProvidedValues() {
    Node node = new Node(42);
    RecordTypeBuilder.RecordProperty prop =
        new RecordTypeBuilder.RecordProperty(numberType, node);

    assertSame(numberType, prop.getType());
    assertSame(node, prop.getPropertyNode());
  }

  // Tests RecordProperty with null values
  @Test
  public void testRecordProperty_nullValues_returnsNull() {
    RecordTypeBuilder.RecordProperty prop =
        new RecordTypeBuilder.RecordProperty(null, null);

    assertNull(prop.getType());
    assertNull(prop.getPropertyNode());
  }
}