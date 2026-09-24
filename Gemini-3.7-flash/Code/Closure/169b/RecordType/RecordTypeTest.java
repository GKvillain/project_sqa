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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;
  private JSType noType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    noType = registry.getNativeObjectType(JSTypeNative.NO_TYPE);
  }

  private RecordType createRecordType(Map<String, JSType> props, boolean declared) {
    Map<String, RecordProperty> propMap = new HashMap<String, RecordProperty>();
    for (Map.Entry<String, JSType> entry : props.entrySet()) {
      propMap.put(entry.getKey(), new RecordProperty(entry.getValue(), null));
    }
    return new RecordType(registry, propMap, declared);
  }

  private RecordType createRecordType(Map<String, JSType> props) {
    return createRecordType(props, true);
  }

  // Tests constructor with null RecordProperty throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullRecordProperty_throwsException() {
    Map<String, RecordProperty> propMap = new HashMap<String, RecordProperty>();
    propMap.put("a", null);
    new RecordType(registry, propMap);
  }

  // Tests isSynthetic for declared vs synthesized record types
  @Test
  public void testIsSynthetic_declaredAndSynthesized_returnsCorrectBoolean() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);

    RecordType declaredRecord = createRecordType(props, true);
    assertFalse(declaredRecord.isSynthetic());

    RecordType syntheticRecord = createRecordType(props, false);
    assertTrue(syntheticRecord.isSynthetic());
  }

  // Tests getImplicitPrototype returns OBJECT_TYPE
  @Test
  public void testGetImplicitPrototype_returnsObjectType() {
    RecordType record = createRecordType(Collections.<String, JSType>emptyMap());
    assertSame(objectType, record.getImplicitPrototype());
  }

  // Tests defineProperty returns false when record is frozen
  @Test
  public void testDefineProperty_whenFrozen_returnsFalse() {
    RecordType record = createRecordType(Collections.<String, JSType>emptyMap());
    boolean result = record.defineProperty("newProp", stringType, false, null);
    assertFalse(result);
  }

  // Tests toMaybeRecordType returns this instance
  @Test
  public void testToMaybeRecordType_returnsSelf() {
    RecordType record = createRecordType(Collections.<String, JSType>emptyMap());
    assertSame(record, record.toMaybeRecordType());
  }

  // Tests checkRecordEquivalenceHelper with identical properties
  @Test
  public void testCheckRecordEquivalenceHelper_identicalProperties_returnsTrue() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);
    props1.put("b", stringType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("a", numberType);
    props2.put("b", stringType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    assertTrue(rec1.checkRecordEquivalenceHelper(rec2, false));
    assertTrue(rec2.checkRecordEquivalenceHelper(rec1, false));
  }

  // Tests checkRecordEquivalenceHelper with different property keys
  @Test
  public void testCheckRecordEquivalenceHelper_differentKeys_returnsFalse() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("b", numberType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    assertFalse(rec1.checkRecordEquivalenceHelper(rec2, false));
  }

  // Tests checkRecordEquivalenceHelper with different property types
  @Test
  public void testCheckRecordEquivalenceHelper_differentPropertyTypes_returnsFalse() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("a", stringType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    assertFalse(rec1.checkRecordEquivalenceHelper(rec2, false));
  }

  // Tests isSubtype when subtype has identical properties
  @Test
  public void testIsSubtype_identicalProperties_returnsTrue() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);

    RecordType rec1 = createRecordType(props);
    RecordType rec2 = createRecordType(props);

    assertTrue(rec1.isSubtype(rec2));
  }

  // Tests isSubtype when subtype has superset of properties (covariance)
  @Test
  public void testIsSubtype_moreProperties_returnsTrue() {
    Map<String, JSType> subProps = new HashMap<String, JSType>();
    subProps.put("a", numberType);
    subProps.put("b", stringType);

    Map<String, JSType> superProps = new HashMap<String, JSType>();
    superProps.put("a", numberType);

    RecordType subRecord = createRecordType(subProps);
    RecordType superRecord = createRecordType(superProps);

    assertTrue(subRecord.isSubtype(superRecord));
    assertFalse(superRecord.isSubtype(subRecord));
  }

  // Tests isSubtype when property type conflicts on declared properties
  @Test
  public void testIsSubtype_conflictingDeclaredPropertyType_returnsFalse() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("a", stringType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    assertFalse(rec1.isSubtype(rec2));
  }

  // Tests isSubtype with native OBJECT_TYPE
  @Test
  public void testIsSubtype_objectType_returnsTrue() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);

    RecordType record = createRecordType(props);
    assertTrue(record.isSubtype(objectType));
  }

  // Tests isSubtype with non-object/non-record type returns false
  @Test
  public void testIsSubtype_nonRecordType_returnsFalse() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);

    RecordType record = createRecordType(props);
    assertFalse(record.isSubtype(numberType));
  }

  // Tests getGreatestSubtypeHelper between two compatible records
  @Test
  public void testGetGreatestSubtypeHelper_twoCompatibleRecords_returnsCombinedRecord() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("b", stringType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    JSType greatestSubtype = rec1.getGreatestSubtypeHelper(rec2);
    assertTrue(greatestSubtype.isRecordType());
    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
    assertEquals(numberType, resultRecord.getPropertyType("a"));
    assertEquals(stringType, resultRecord.getPropertyType("b"));
  }

  // Tests getGreatestSubtypeHelper between conflicting records returns NO_TYPE
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    Map<String, JSType> props1 = new HashMap<String, JSType>();
    props1.put("a", numberType);

    Map<String, JSType> props2 = new HashMap<String, JSType>();
    props2.put("a", stringType);

    RecordType rec1 = createRecordType(props1);
    RecordType rec2 = createRecordType(props2);

    JSType greatestSubtype = rec1.getGreatestSubtypeHelper(rec2);
    assertEquals(noType, greatestSubtype);
  }

  // Tests getGreatestSubtypeHelper with a non-record object type
  @Test
  public void testGetGreatestSubtypeHelper_nonRecordObjectType_returnsSubtype() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);

    RecordType rec = createRecordType(props);
    JSType greatestSubtype = rec.getGreatestSubtypeHelper(objectType);
    assertNotNull(greatestSubtype);
  }

  // Tests resolveInternal resolves all property types
  @Test
  public void testResolveInternal_resolvesTypes_returnsSelf() {
    Map<String, JSType> props = new HashMap<String, JSType>();
    props.put("a", numberType);
    props.put("b", stringType);

    RecordType record = createRecordType(props);
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = record.resolveInternal(reporter, null);

    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
  }
}