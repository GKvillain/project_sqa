package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  private RecordType createRecordType(String name, JSType type) {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty(name, type, null);
    return (RecordType) builder.build();
  }

  private RecordType createRecordType(String name1, JSType type1, String name2, JSType type2) {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty(name1, type1, null);
    builder.addProperty(name2, type2, null);
    return (RecordType) builder.build();
  }

  // Tests exception when property map contains a null RecordProperty
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullRecordProperty_throwsIllegalStateException() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("foo", null);
    new RecordType(registry, map);
  }

  // Tests implicit prototype is native Object type
  @Test
  public void testGetImplicitPrototype_returnsNativeObjectType() {
    RecordType record = createRecordType("a", numberType);
    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), record.getImplicitPrototype());
  }

  // Tests toMaybeRecordType returns self
  @Test
  public void testToMaybeRecordType_returnsSelf() {
    RecordType record = createRecordType("a", numberType);
    assertSame(record, record.toMaybeRecordType());
  }

  // Tests isEquivalentTo when comparing with same instance
  @Test
  public void testIsEquivalentTo_sameInstance_returnsTrue() {
    RecordType record = createRecordType("a", numberType);
    assertTrue(record.isEquivalentTo(record));
  }

  // Tests isEquivalentTo when comparing with non-record type
  @Test
  public void testIsEquivalentTo_nonRecordType_returnsFalse() {
    RecordType record = createRecordType("a", numberType);
    assertFalse(record.isEquivalentTo(numberType));
  }

  // Tests isEquivalentTo when comparing identical record types
  @Test
  public void testIsEquivalentTo_identicalRecords_returnsTrue() {
    RecordType record1 = createRecordType("a", numberType, "b", stringType);
    RecordType record2 = createRecordType("a", numberType, "b", stringType);
    assertTrue(record1.isEquivalentTo(record2));
    assertTrue(record2.isEquivalentTo(record1));
  }

  // Tests isEquivalentTo when property keys differ
  @Test
  public void testIsEquivalentTo_differentPropertyKeys_returnsFalse() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("b", numberType);
    assertFalse(record1.isEquivalentTo(record2));
  }

  // Tests isEquivalentTo when property types differ
  @Test
  public void testIsEquivalentTo_differentPropertyTypes_returnsFalse() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("a", stringType);
    assertFalse(record1.isEquivalentTo(record2));
  }

  // Tests defineProperty returns false when record is frozen
  @Test
  public void testDefineProperty_whenFrozen_returnsFalse() {
    RecordType record = createRecordType("a", numberType);
    assertFalse(record.defineProperty("b", stringType, false, null));
  }

  // Tests isSubtype when target is native Object type
  @Test
  public void testIsSubtype_targetIsObjectType_returnsTrue() {
    RecordType record = createRecordType("a", numberType);
    assertTrue(record.isSubtype(objectType));
  }

  // Tests isSubtype when target is non-record type
  @Test
  public void testIsSubtype_targetIsPrimitiveType_returnsFalse() {
    RecordType record = createRecordType("a", numberType);
    assertFalse(record.isSubtype(numberType));
  }

  // Tests isSubtype structural subtyping with matching and extra properties
  @Test
  public void testIsSubtype_recordWithMoreProperties_isSubtypeOfRecordWithFewerProperties() {
    RecordType subRecord = createRecordType("a", numberType, "b", stringType);
    RecordType superRecord = createRecordType("a", numberType);
    assertTrue(subRecord.isSubtype(superRecord));
    assertFalse(superRecord.isSubtype(subRecord));
  }

  // Tests isSubtype when a required property is missing
  @Test
  public void testIsSubtype_missingRequiredProperty_returnsFalse() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("b", numberType);
    assertFalse(record1.isSubtype(record2));
  }

  // Tests isSubtype when property type is incompatible
  @Test
  public void testIsSubtype_incompatiblePropertyType_returnsFalse() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("a", stringType);
    assertFalse(record1.isSubtype(record2));
  }

  // Tests getGreatestSubtypeHelper merging disjoint properties of two record types
  @Test
  public void testGetGreatestSubtypeHelper_disjointRecords_mergesProperties() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("b", stringType);
    JSType greatestSubtype = record1.getGreatestSubtypeHelper(record2);

    assertTrue(greatestSubtype.isRecordType());
    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
    assertTrue(resultRecord.getPropertyType("a").isEquivalentTo(numberType));
    assertTrue(resultRecord.getPropertyType("b").isEquivalentTo(stringType));
  }

  // Tests getGreatestSubtypeHelper when records have conflicting property types
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("a", stringType);
    JSType greatestSubtype = record1.getGreatestSubtypeHelper(record2);
    assertEquals(registry.getNativeObjectType(JSTypeNative.NO_TYPE), greatestSubtype);
  }

  // Tests getGreatestSubtypeHelper with a non-record type
  @Test
  public void testGetGreatestSubtypeHelper_nonRecordType_returnsSubtype() {
    RecordType record = createRecordType("a", numberType);
    JSType result = record.getGreatestSubtypeHelper(objectType);
    assertNotNull(result);
  }

  // Tests resolveInternal successfully resolves contained property types
  @Test
  public void testResolveInternal_resolvesTypesCorrectly() {
    RecordType record = createRecordType("a", numberType, "b", stringType);
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = record.resolve(reporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
    assertTrue(resolved.toMaybeRecordType().hasProperty("a"));
    assertTrue(resolved.toMaybeRecordType().hasProperty("b"));
  }

  // Tests isRecordType returns true
  @Test
  public void testIsRecordType_returnsTrue() {
    RecordType record = createRecordType("a", numberType);
    assertTrue(record.isRecordType());
  }

  // Tests empty record construction and properties
  @Test
  public void testEmptyRecordType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    RecordType emptyRecord = (RecordType) builder.build();
    assertNotNull(emptyRecord);
    assertTrue(emptyRecord.isRecordType());
    assertFalse(emptyRecord.hasProperty("a"));
  }

  // Tests isEquivalentTo when property counts differ
  @Test
  public void testIsEquivalentTo_differentPropertyCount_returnsFalse() {
    RecordType record1 = createRecordType("a", numberType);
    RecordType record2 = createRecordType("a", numberType, "b", stringType);
    assertFalse(record1.isEquivalentTo(record2));
    assertFalse(record2.isEquivalentTo(record1));
  }

  // Tests getGreatestSubtypeHelper with overlapping identical properties
  @Test
  public void testGetGreatestSubtypeHelper_overlappingIdenticalProperties() {
    RecordType record1 = createRecordType("a", numberType, "b", stringType);
    RecordType record2 = createRecordType("a", numberType, "c", booleanType);
    JSType greatestSubtype = record1.getGreatestSubtypeHelper(record2);

    assertTrue(greatestSubtype.isRecordType());
    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
    assertTrue(resultRecord.hasProperty("c"));
    assertTrue(resultRecord.getPropertyType("a").isEquivalentTo(numberType));
  }

  // Tests getPropertyNode and property declaration metadata
  @Test
  public void testPropertyNodeAndDeclaredStatus() {
    Node node = Node.newString("a");
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", numberType, node);
    RecordType record = (RecordType) builder.build();

    assertSame(node, record.getPropertyNode("a"));
    assertNull(record.getPropertyNode("nonExistent"));
    assertTrue(record.isPropertyDeclared("a"));
    assertFalse(record.isPropertyDeclared("nonExistent"));
    assertFalse(record.isPropertyInExterns("a"));
  }

  // Tests toString / toStringHelper representation
  @Test
  public void testToStringRepresentation() {
    RecordType record = createRecordType("a", numberType);
    String str = record.toString();
    assertNotNull(str);
    assertTrue(str.contains("a"));
    assertTrue(str.contains("number"));
  }
}