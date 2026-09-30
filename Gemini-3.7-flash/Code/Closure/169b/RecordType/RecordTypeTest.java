package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private JSType NUMBER_TYPE;
  private JSType STRING_TYPE;
  private JSType BOOLEAN_TYPE;
  private JSType UNKNOWN_TYPE;
  private JSType OBJECT_TYPE;
  private JSType NO_TYPE;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    NUMBER_TYPE = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING_TYPE = registry.getNativeType(JSTypeNative.STRING_TYPE);
    BOOLEAN_TYPE = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    OBJECT_TYPE = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    NO_TYPE = registry.getNativeType(JSTypeNative.NO_TYPE);
  }

  private RecordType createRecordType(Map<String, JSType> propMap, boolean declared) {
    Map<String, RecordProperty> properties = Maps.newHashMap();
    for (Map.Entry<String, JSType> entry : propMap.entrySet()) {
      properties.put(entry.getKey(), new RecordProperty(entry.getValue(), null));
    }
    return new RecordType(registry, properties, declared);
  }

  private RecordType createRecordType(Map<String, JSType> propMap) {
    return createRecordType(propMap, true);
  }

  // Tests constructor with null RecordProperty throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullRecordProperty_throwsIllegalStateException() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("prop", null);
    new RecordType(registry, map, true);
  }

  // Tests isSynthetic returns false for declared and true for synthesized
  @Test
  public void testIsSynthetic_declaredAndSynthesized_returnsCorrectBoolean() {
    RecordType declaredType = createRecordType(ImmutableMap.of("a", NUMBER_TYPE), true);
    assertFalse(declaredType.isSynthetic());

    RecordType synthType = createRecordType(ImmutableMap.of("a", NUMBER_TYPE), false);
    assertTrue(synthType.isSynthetic());
  }

  // Tests getImplicitPrototype returns OBJECT_TYPE
  @Test
  public void testGetImplicitPrototype_normal_returnsObjectType() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertEquals(OBJECT_TYPE, record.getImplicitPrototype());
  }

  // Tests toMaybeRecordType returns self
  @Test
  public void testToMaybeRecordType_normal_returnsSelf() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertSame(record, record.toMaybeRecordType());
  }

  // Tests defineProperty returns false when record type is frozen
  @Test
  public void testDefineProperty_frozenRecord_returnsFalse() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertFalse(record.defineProperty("b", STRING_TYPE, false, null));
  }

  // Tests checkRecordEquivalenceHelper with identical properties
  @Test
  public void testCheckRecordEquivalenceHelper_identicalProperties_returnsTrue() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE, "b", STRING_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE, "b", STRING_TYPE));
    assertTrue(r1.checkRecordEquivalenceHelper(r2, false));
    assertTrue(r2.checkRecordEquivalenceHelper(r1, false));
  }

  // Tests checkRecordEquivalenceHelper with different keys
  @Test
  public void testCheckRecordEquivalenceHelper_differentKeys_returnsFalse() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("b", NUMBER_TYPE));
    assertFalse(r1.checkRecordEquivalenceHelper(r2, false));
  }

  // Tests checkRecordEquivalenceHelper with different types and tolerate unknowns
  @Test
  public void testCheckRecordEquivalenceHelper_unknownTypes_tolerateUnknowns() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("a", UNKNOWN_TYPE));

    assertFalse(r1.checkRecordEquivalenceHelper(r2, false));
    assertTrue(r1.checkRecordEquivalenceHelper(r2, true));
  }

  // Tests isSubtype for record subtyping with matching and additional properties
  @Test
  public void testIsSubtype_recordWithMoreProperties_isSubtype() {
    RecordType superType = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType subType = createRecordType(ImmutableMap.of("a", NUMBER_TYPE, "b", STRING_TYPE));

    assertTrue(subType.isSubtype(superType));
    assertFalse(superType.isSubtype(subType));
  }

  // Tests isSubtype for record with missing property
  @Test
  public void testIsSubtype_missingProperty_returnsFalse() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("b", NUMBER_TYPE));

    assertFalse(r1.isSubtype(r2));
  }

  // Tests isSubtype for declared property with incompatible type
  @Test
  public void testIsSubtype_incompatibleDeclaredProperty_returnsFalse() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("a", STRING_TYPE));

    assertFalse(r1.isSubtype(r2));
  }

  // Tests isSubtype against non-record type (OBJECT_TYPE)
  @Test
  public void testIsSubtype_againstObjectType_returnsTrue() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertTrue(record.isSubtype(OBJECT_TYPE));
  }

  // Tests isSubtype against non-record primitive type
  @Test
  public void testIsSubtype_againstPrimitiveType_returnsFalse() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertFalse(record.isSubtype(NUMBER_TYPE));
  }

  // Tests isSubtype with empty record (matches all records)
  @Test
  public void testIsSubtype_emptyRecord_isSupertypeOfAllRecords() {
    RecordType emptyRecord = createRecordType(Collections.<String, JSType>emptyMap());
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));

    assertTrue(record.isSubtype(emptyRecord));
    assertFalse(emptyRecord.isSubtype(record));
  }

  // Tests getGreatestSubtypeHelper between two compatible record types
  @Test
  public void testGetGreatestSubtypeHelper_compatibleRecords_mergesProperties() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("b", STRING_TYPE));

    JSType greatestSubtype = r1.getGreatestSubtypeHelper(r2);
    assertTrue(greatestSubtype.isRecordType());
    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
    assertEquals(NUMBER_TYPE, resultRecord.getPropertyType("a"));
    assertEquals(STRING_TYPE, resultRecord.getPropertyType("b"));
  }

  // Tests getGreatestSubtypeHelper between conflicting record types returns NO_TYPE
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("a", STRING_TYPE));

    JSType greatestSubtype = r1.getGreatestSubtypeHelper(r2);
    assertEquals(NO_TYPE, greatestSubtype);
  }

  // Tests getGreatestSubtypeHelper with non-record ObjectType
  @Test
  public void testGetGreatestSubtypeHelper_nonRecordType_returnsSubtype() {
    RecordType r = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    JSType greatestSubtype = r.getGreatestSubtypeHelper(NUMBER_TYPE);
    assertNotNull(greatestSubtype);
  }

  // Tests resolveInternal resolves internal property types
  @Test
  public void testResolveInternal_resolvesPropertyTypes() {
    NamedType namedType = new NamedType(registry, "CustomType", null, 0, 0);
    RecordType record = createRecordType(ImmutableMap.of("a", (JSType) namedType));

    ObjectType resolvedCustom = registry.createAnonymousObjectType();
    registry.declareType("CustomType", resolvedCustom);

    JSType resolvedRecord = record.resolve(new SimpleErrorReporter(), registry.getTopScope());
    assertTrue(resolvedRecord.isRecordType());
  }

  @Test
  public void testGetPropertyNode_returnsConfiguredNode() {
    Node node = Node.newString("testNode");
    Map<String, RecordProperty> properties = Maps.newHashMap();
    properties.put("prop", new RecordProperty(NUMBER_TYPE, node));
    RecordType record = new RecordType(registry, properties, true);

    assertSame(node, record.getPropertyNode("prop"));
    assertNull(record.getPropertyNode("nonExistent"));
  }

  @Test
  public void testIsPropertyDeclaredAndInferred() {
    RecordType declaredRecord = createRecordType(ImmutableMap.of("a", NUMBER_TYPE), true);
    assertTrue(declaredRecord.isPropertyDeclared("a"));
    assertFalse(declaredRecord.isPropertyInferred("a"));

    RecordType synthRecord = createRecordType(ImmutableMap.of("a", NUMBER_TYPE), false);
    assertFalse(synthRecord.isPropertyDeclared("a"));
    assertTrue(synthRecord.isPropertyInferred("a"));
  }

  @Test
  public void testToString_formatsRecordCorrectly() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    String str = record.toString();
    assertTrue(str.contains("a: number"));
  }

  @Test
  public void testToAnnotationString_formatsRecordCorrectly() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    String str = record.toAnnotationString();
    assertTrue(str.contains("a: number"));
  }

  @Test
  public void testGetPropertyNames_returnsAllPropertyKeys() {
    RecordType record = createRecordType(ImmutableMap.of("x", NUMBER_TYPE, "y", STRING_TYPE));
    Set<String> propNames = record.getPropertyNames();
    assertTrue(propNames.contains("x"));
    assertTrue(propNames.contains("y"));
    assertEquals(2, propNames.size());
  }

  @Test
  public void testGetSlot_existingAndNonExisting() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertNotNull(record.getSlot("a"));
    assertEquals(NUMBER_TYPE, record.getSlot("a").getType());
    assertNull(record.getSlot("unknownProp"));
  }

  @Test
  public void testTestForEquality_withRecordAndNonRecordTypes() {
    RecordType r1 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    RecordType r2 = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertEquals(TernaryValue.UNKNOWN, r1.testForEquality(r2));
    assertEquals(TernaryValue.FALSE, r1.testForEquality(NUMBER_TYPE));
  }

  @Test
  public void testCanTestForEqualityWith() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    assertTrue(record.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(record.canTestForEqualityWith(record));
  }

  @Test
  public void testIsSubtype_nonRecordObjectTypeWithProperties() {
    RecordType record = createRecordType(ImmutableMap.of("a", NUMBER_TYPE));
    ObjectType otherObj = registry.createAnonymousObjectType();
    otherObj.defineDeclaredProperty("a", NUMBER_TYPE, null);

    assertTrue(record.isSubtype(otherObj));

    ObjectType mismatchObj = registry.createAnonymousObjectType();
    mismatchObj.defineDeclaredProperty("a", STRING_TYPE, null);
    assertFalse(record.isSubtype(mismatchObj));
  }
}