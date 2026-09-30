package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests constructor throwing IllegalStateException when property map contains null RecordProperty
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullRecordProperty_throwsException() {
    Map<String, RecordTypeBuilder.RecordProperty> map = new HashMap<String, RecordTypeBuilder.RecordProperty>();
    map.put("prop", null);
    new RecordType(registry, map);
  }

  // Tests isEquivalentTo comparing against identical reference
  @Test
  public void testIsEquivalentTo_sameInstance_returnsTrue() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertTrue(record.isEquivalentTo(record));
  }

  // Tests isEquivalentTo against non-record type
  @Test
  public void testIsEquivalentTo_nonRecordType_returnsFalse() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertFalse(record.isEquivalentTo(numberType));
    assertFalse(record.isEquivalentTo(objectType));
  }

  // Tests isEquivalentTo against record type with different keys
  @Test
  public void testIsEquivalentTo_differentKeys_returnsFalse() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", numberType, null)
        .build();
    assertFalse(record1.isEquivalentTo(record2));
    assertFalse(record2.isEquivalentTo(record1));
  }

  // Tests isEquivalentTo against record type with same key but different types
  @Test
  public void testIsEquivalentTo_differentPropertyTypes_returnsFalse() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();
    assertFalse(record1.isEquivalentTo(record2));
  }

  // Tests isEquivalentTo against record type with identical keys and types
  @Test
  public void testIsEquivalentTo_identicalKeysAndTypes_returnsTrue() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .addProperty("a", numberType, null)
        .build();
    assertTrue(record1.isEquivalentTo(record2));
  }

  // Tests getImplicitPrototype returns native OBJECT_TYPE
  @Test
  public void testGetImplicitPrototype_returnsNativeObjectType() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertEquals(objectType, record.getImplicitPrototype());
  }

  // Tests defineProperty returns false when record is frozen
  @Test
  public void testDefineProperty_frozenRecord_returnsFalse() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    boolean result = record.defineProperty("b", stringType, false, null);
    assertFalse(result);
  }

  // Tests toMaybeRecordType returns self
  @Test
  public void testToMaybeRecordType_returnsSelf() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertSame(record, record.toMaybeRecordType());
  }

  // Tests isSubtype with identical record type
  @Test
  public void testIsSubtype_identicalRecordType_returnsTrue() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertTrue(record1.isSubtype(record2));
  }

  // Tests isSubtype: type with more properties is subtype of type with fewer properties
  @Test
  public void testIsSubtype_supersetProperties_returnsTrue() {
    RecordType recordSub = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    RecordType recordSuper = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertTrue(recordSub.isSubtype(recordSuper));
    assertFalse(recordSuper.isSubtype(recordSub));
  }

  // Tests isSubtype: incompatible property types returns false
  @Test
  public void testIsSubtype_incompatiblePropertyType_returnsFalse() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();
    assertFalse(record1.isSubtype(record2));
  }

  // Tests isSubtype against native OBJECT_TYPE
  @Test
  public void testIsSubtype_objectType_returnsTrue() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertTrue(record.isSubtype(objectType));
  }

  // Tests isSubtype against primitive non-record type
  @Test
  public void testIsSubtype_primitiveType_returnsFalse() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertFalse(record.isSubtype(numberType));
  }

  // Tests getLeastSupertype with non-record type
  @Test
  public void testGetLeastSupertype_nonRecordType_returnsUnionOrObject() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    JSType result = record.getLeastSupertype(numberType);
    assertNotNull(result);
    assertTrue(result.isUnionType() || result.isEquivalentTo(objectType));
  }

  // Tests getLeastSupertype between two record types
  @Test
  public void testGetLeastSupertype_twoRecordTypes_commonProperties() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("c", booleanType, null)
        .build();
    JSType leastSuper = record1.getLeastSupertype(record2);
    assertNotNull(leastSuper);
    assertTrue(leastSuper.isRecordType() || leastSuper.isUnionType() || leastSuper.isEquivalentTo(objectType));
  }

  // Tests getGreatestSubtypeHelper with compatible record types combining properties
  @Test
  public void testGetGreatestSubtypeHelper_compatibleRecordTypes_combinesProperties() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();
    JSType greatestSub = record1.getGreatestSubtypeHelper(record2);
    assertNotNull(greatestSub);
    assertTrue(greatestSub.isRecordType());
    RecordType resRecord = greatestSub.toMaybeRecordType();
    assertTrue(resRecord.hasProperty("a"));
    assertTrue(resRecord.hasProperty("b"));
  }

  // Tests getGreatestSubtypeHelper with conflicting property types returns NO_TYPE
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    RecordType record1 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType record2 = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();
    JSType greatestSub = record1.getGreatestSubtypeHelper(record2);
    assertEquals(registry.getNativeObjectType(JSTypeNative.NO_TYPE), greatestSub);
  }

  // Tests getGreatestSubtypeHelper with non-record ObjectType
  @Test
  public void testGetGreatestSubtypeHelper_nonRecordType_returnsSubtype() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    JSType greatestSub = record.getGreatestSubtypeHelper(objectType);
    assertNotNull(greatestSub);
  }

  // Tests resolveInternal resolves properties within scope
  @Test
  public void testResolveInternal_resolvesContainedProperties() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    JSType resolved = record.resolveInternal(null, null);
    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
    assertEquals(numberType, resolved.toMaybeRecordType().getPropertyType("a"));
  }
}