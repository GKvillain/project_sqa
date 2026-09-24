package com.google.javascript.rhino.jstype;

import com.google.common.collect.Maps;
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private JSType NUMBER_TYPE;
  private JSType STRING_TYPE;
  private JSType BOOLEAN_TYPE;
  private JSType OBJECT_TYPE;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    NUMBER_TYPE = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING_TYPE = registry.getNativeType(JSTypeNative.STRING_TYPE);
    BOOLEAN_TYPE = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    OBJECT_TYPE = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  private RecordType createRecordType(Map<String, JSType> props) {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (Map.Entry<String, JSType> entry : props.entrySet()) {
      builder.addProperty(entry.getKey(), entry.getValue(), null);
    }
    return (RecordType) builder.build();
  }

  // Tests constructor with null RecordProperty in map throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullPropertyEntry_throwsIllegalStateException() {
    Map<String, RecordProperty> propMap = new HashMap<String, RecordProperty>();
    propMap.put("a", null);
    new RecordType(registry, propMap);
  }

  // Tests isEquivalentTo when comparing with the same instance
  @Test
  public void testIsEquivalentTo_sameInstance_returnsTrue() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertTrue(record.isEquivalentTo(record));
  }

  // Tests isEquivalentTo with non-record type
  @Test
  public void testIsEquivalentTo_nonRecordType_returnsFalse() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertFalse(record.isEquivalentTo(NUMBER_TYPE));
  }

  // Tests isEquivalentTo with record having different property names
  @Test
  public void testIsEquivalentTo_differentKeys_returnsFalse() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("b", NUMBER_TYPE);
    RecordType record2 = createRecordType(props2);

    assertFalse(record1.isEquivalentTo(record2));
    assertFalse(record2.isEquivalentTo(record1));
  }

  // Tests isEquivalentTo with record having same keys but different types
  @Test
  public void testIsEquivalentTo_differentTypes_returnsFalse() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("a", STRING_TYPE);
    RecordType record2 = createRecordType(props2);

    assertFalse(record1.isEquivalentTo(record2));
  }

  // Tests isEquivalentTo with equivalent records
  @Test
  public void testIsEquivalentTo_equivalentRecords_returnsTrue() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    props1.put("b", STRING_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("a", NUMBER_TYPE);
    props2.put("b", STRING_TYPE);
    RecordType record2 = createRecordType(props2);

    assertTrue(record1.isEquivalentTo(record2));
    assertTrue(record2.isEquivalentTo(record1));
  }

  // Tests getImplicitPrototype returns native ObjectType
  @Test
  public void testGetImplicitPrototype_returnsObjectPrototype() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE),
        record.getImplicitPrototype());
  }

  // Tests defineProperty returns false when record is frozen
  @Test
  public void testDefineProperty_whenFrozen_returnsFalse() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertFalse(record.defineProperty("b", STRING_TYPE, false, null));
  }

  // Tests toMaybeRecordType returns this instance
  @Test
  public void testToMaybeRecordType_returnsSelf() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertSame(record, record.toMaybeRecordType());
  }

  // Tests isSubtype with self returns true
  @Test
  public void testIsSubtype_self_returnsTrue() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertTrue(record.isSubtype(record));
  }

  // Tests isSubtype with ObjectType returns true
  @Test
  public void testIsSubtype_objectType_returnsTrue() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertTrue(record.isSubtype(OBJECT_TYPE));
  }

  // Tests isSubtype with unrelated primitive returns false
  @Test
  public void testIsSubtype_primitiveType_returnsFalse() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    assertFalse(record.isSubtype(NUMBER_TYPE));
  }

  // Tests isSubtype when candidate record has all properties with matching types
  @Test
  public void testIsSubtype_recordWithSupersetProperties_returnsTrue() {
    Map<String, JSType> subProps = Maps.newHashMap();
    subProps.put("a", NUMBER_TYPE);
    subProps.put("b", STRING_TYPE);
    RecordType subRecord = createRecordType(subProps);

    Map<String, JSType> superProps = Maps.newHashMap();
    superProps.put("a", NUMBER_TYPE);
    RecordType superRecord = createRecordType(superProps);

    assertTrue(subRecord.isSubtype(superRecord));
    assertFalse(superRecord.isSubtype(subRecord));
  }

  // Tests isSubtype returns false when property type mismatches
  @Test
  public void testIsSubtype_mismatchedPropertyType_returnsFalse() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("a", STRING_TYPE);
    RecordType record2 = createRecordType(props2);

    assertFalse(record1.isSubtype(record2));
  }

  // Tests getLeastSupertype with another record type finds common properties
  @Test
  public void testGetLeastSupertype_withRecord_computesCommonProperties() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    props1.put("b", STRING_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("a", NUMBER_TYPE);
    props2.put("c", BOOLEAN_TYPE);
    RecordType record2 = createRecordType(props2);

    JSType leastSupertype = record1.getLeastSupertype(record2);
    assertTrue(leastSupertype.isRecordType());
    RecordType superRecord = leastSupertype.toMaybeRecordType();
    assertTrue(superRecord.hasProperty("a"));
    assertFalse(superRecord.hasProperty("b"));
    assertFalse(superRecord.hasProperty("c"));
  }

  // Tests getLeastSupertype with non-record delegates to superclass
  @Test
  public void testGetLeastSupertype_withNonRecord_delegatesToSuper() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    JSType result = record.getLeastSupertype(NUMBER_TYPE);
    assertNotNull(result);
    assertFalse(result.isRecordType());
  }

  // Tests getGreatestSubtypeHelper merges disjoint properties of two records
  @Test
  public void testGetGreatestSubtypeHelper_disjointRecords_mergesProperties() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("b", STRING_TYPE);
    RecordType record2 = createRecordType(props2);

    JSType greatestSubtype = record1.getGreatestSubtypeHelper(record2);
    assertTrue(greatestSubtype.isRecordType());
    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
  }

  // Tests getGreatestSubtypeHelper returns NO_TYPE on conflicting property types
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    Map<String, JSType> props1 = Maps.newHashMap();
    props1.put("a", NUMBER_TYPE);
    RecordType record1 = createRecordType(props1);

    Map<String, JSType> props2 = Maps.newHashMap();
    props2.put("a", STRING_TYPE);
    RecordType record2 = createRecordType(props2);

    JSType greatestSubtype = record1.getGreatestSubtypeHelper(record2);
    assertTrue(greatestSubtype.isNoType());
  }

  // Tests resolveInternal successfully resolves property types
  @Test
  public void testResolveInternal_resolvesPropertyTypes() {
    Map<String, JSType> props = Maps.newHashMap();
    props.put("a", NUMBER_TYPE);
    RecordType record = createRecordType(props);

    JSType resolved = record.resolve(new SimpleErrorReporter(), null);
    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
  }
}