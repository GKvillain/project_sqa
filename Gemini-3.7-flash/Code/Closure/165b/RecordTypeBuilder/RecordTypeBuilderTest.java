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

  // Tests that building an empty RecordTypeBuilder returns the native Object type
  @Test
  public void testBuild_emptyBuilder_returnsNativeObjectType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType result = builder.build();

    assertNotNull(result);
    assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
  }

  // Tests adding a single property returns the builder instance and builds a RecordType
  @Test
  public void testAddProperty_singleProperty_returnsBuilderAndBuildsRecordType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("propA");

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

  // Tests adding multiple distinct properties chains correctly
  @Test
  public void testAddProperty_multipleDistinctProperties_returnsBuilderAndBuildsAllProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node nodeA = Node.newString("propA");
    Node nodeB = Node.newString("propB");

    RecordTypeBuilder builderStep1 = builder.addProperty("propA", numberType, nodeA);
    RecordTypeBuilder builderStep2 = builder.addProperty("propB", stringType, nodeB);

    assertSame(builder, builderStep1);
    assertSame(builder, builderStep2);

    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("propA"));
    assertTrue(recordType.hasProperty("propB"));
    assertEquals(numberType, recordType.getPropertyType("propA"));
    assertEquals(stringType, recordType.getPropertyType("propB"));
  }

  // Tests that adding a duplicate property returns null (branch where properties.containsKey is true)
  @Test
  public void testAddProperty_duplicatePropertyName_returnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = Node.newString("propA");
    Node node2 = Node.newString("propA");

    RecordTypeBuilder firstAdd = builder.addProperty("propA", numberType, node1);
    RecordTypeBuilder duplicateAdd = builder.addProperty("propA", stringType, node2);

    assertNotNull(firstAdd);
    assertNull(duplicateAdd);
  }

  // Tests that build after failed duplicate add still preserves the original property
  @Test
  public void testBuild_afterDuplicateProperty_buildsWithOriginalProperty() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node1 = Node.newString("propA");
    Node node2 = Node.newString("propA");

    builder.addProperty("propA", numberType, node1);
    builder.addProperty("propA", stringType, node2);

    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("propA"));
    assertEquals(numberType, recordType.getPropertyType("propA"));
    assertEquals(node1, recordType.getPropertyNode("propA"));
  }

  // Tests adding a property with null type and null node
  @Test
  public void testAddProperty_nullTypeAndNode_succeeds() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    RecordTypeBuilder returnedBuilder = builder.addProperty("nullableProp", null, null);

    assertSame(builder, returnedBuilder);
    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("nullableProp"));
    assertNull(recordType.getPropertyNode("nullableProp"));
  }

  // Tests adding property with empty string name
  @Test
  public void testAddProperty_emptyPropertyName_succeeds() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    Node node = Node.newString("");

    RecordTypeBuilder returnedBuilder = builder.addProperty("", booleanType, node);

    assertSame(builder, returnedBuilder);
    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty(""));
    assertEquals(booleanType, recordType.getPropertyType(""));
  }

  // Tests RecordProperty helper class getters
  @Test
  public void testRecordProperty_getters_returnProvidedValues() {
    Node node = Node.newString("prop");
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(stringType, node);

    assertSame(stringType, prop.getType());
    assertSame(node, prop.getPropertyNode());
  }

  // Tests RecordProperty helper class with null values
  @Test
  public void testRecordProperty_nullValues_returnNull() {
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(null, null);

    assertNull(prop.getType());
    assertNull(prop.getPropertyNode());
  }

  // Tests multiple additions and non-existent property check on built RecordType
  @Test
  public void testBuild_multipleProperties_doesNotContainUnaddedProperty() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("x", numberType, null);
    builder.addProperty("y", numberType, null);

    JSType result = builder.build();
    assertTrue(result.isRecordType());

    RecordType recordType = (RecordType) result;
    assertTrue(recordType.hasProperty("x"));
    assertTrue(recordType.hasProperty("y"));
    assertFalse(recordType.hasProperty("z"));
  }

  // Tests RecordProperty equals and hashCode with equal instances
  @Test
  public void testRecordProperty_equalsAndHashCode_equalInstances() {
    Node node = Node.newString("prop");
    RecordTypeBuilder.RecordProperty prop1 = new RecordTypeBuilder.RecordProperty(stringType, node);
    RecordTypeBuilder.RecordProperty prop2 = new RecordTypeBuilder.RecordProperty(stringType, node);

    assertTrue(prop1.equals(prop1));
    assertTrue(prop1.equals(prop2));
    assertTrue(prop2.equals(prop1));
    assertEquals(prop1.hashCode(), prop2.hashCode());
  }

  // Tests RecordProperty equals with null and different object types
  @Test
  public void testRecordProperty_equals_nullAndDifferentType() {
    Node node = Node.newString("prop");
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(stringType, node);

    assertFalse(prop.equals(null));
    assertFalse(prop.equals("nonRecordPropertyObject"));
  }

  // Tests RecordProperty equals with different JSTypes
  @Test
  public void testRecordProperty_equals_differentType() {
    Node node = Node.newString("prop");
    RecordTypeBuilder.RecordProperty prop1 = new RecordTypeBuilder.RecordProperty(stringType, node);
    RecordTypeBuilder.RecordProperty prop2 = new RecordTypeBuilder.RecordProperty(numberType, node);

    assertFalse(prop1.equals(prop2));
    assertFalse(prop2.equals(prop1));
  }

  // Tests RecordProperty equals with different Nodes
  @Test
  public void testRecordProperty_equals_differentNode() {
    Node node1 = Node.newString("prop1");
    Node node2 = Node.newString("prop2");
    RecordTypeBuilder.RecordProperty prop1 = new RecordTypeBuilder.RecordProperty(stringType, node1);
    RecordTypeBuilder.RecordProperty prop2 = new RecordTypeBuilder.RecordProperty(stringType, node2);

    assertFalse(prop1.equals(prop2));
    assertFalse(prop2.equals(prop1));
  }

  // Tests RecordProperty equals and hashCode with null fields
  @Test
  public void testRecordProperty_equalsAndHashCode_nullFields() {
    RecordTypeBuilder.RecordProperty propNull1 = new RecordTypeBuilder.RecordProperty(null, null);
    RecordTypeBuilder.RecordProperty propNull2 = new RecordTypeBuilder.RecordProperty(null, null);
    Node node = Node.newString("prop");
    RecordTypeBuilder.RecordProperty propNonNull = new RecordTypeBuilder.RecordProperty(stringType, node);

    assertTrue(propNull1.equals(propNull2));
    assertEquals(propNull1.hashCode(), propNull2.hashCode());
    assertFalse(propNull1.equals(propNonNull));
    assertFalse(propNonNull.equals(propNull1));
  }
}