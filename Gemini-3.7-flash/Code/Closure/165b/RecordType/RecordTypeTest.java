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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests constructor with null RecordProperty throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testConstructor_nullRecordProperty_throwsIllegalStateException() {
    Map<String, RecordProperty> propMap = new HashMap<String, RecordProperty>();
    propMap.put("prop", null);
    new RecordType(registry, propMap);
  }

  // Tests defining property on frozen record type returns false
  @Test
  public void testDefineProperty_whenFrozen_returnsFalse() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    boolean defined = record.defineProperty("b", stringType, false, null);
    assertFalse(defined);
    assertFalse(record.hasProperty("b"));
  }

  // Tests implicit prototype is ObjectType
  @Test
  public void testGetImplicitPrototype_returnsNativeObjectType() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertEquals(objectType, record.getImplicitPrototype());
  }

  // Tests toMaybeRecordType returns the same instance
  @Test
  public void testToMaybeRecordType_returnsSelf() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertSame(record, record.toMaybeRecordType());
  }

  // Tests equivalence to self returns true
  @Test
  public void testIsEquivalentTo_sameInstance_returnsTrue() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue(record.isEquivalentTo(record));
  }

  // Tests equivalence to non-record type returns false
  @Test
  public void testIsEquivalentTo_nonRecordType_returnsFalse() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertFalse(record.isEquivalentTo(numberType));
  }

  // Tests equivalence when property keys differ returns false
  @Test
  public void testIsEquivalentTo_differentKeys_returnsFalse() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", numberType, null)
        .build();

    assertFalse(recordA.isEquivalentTo(recordB));
  }

  // Tests equivalence when property types differ returns false
  @Test
  public void testIsEquivalentTo_differentPropertyTypes_returnsFalse() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();

    assertFalse(recordA.isEquivalentTo(recordB));
  }

  // Tests equivalence when keys and types match returns true
  @Test
  public void testIsEquivalentTo_identicalProperties_returnsTrue() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();

    assertTrue(recordA.isEquivalentTo(recordB));
    assertTrue(recordB.isEquivalentTo(recordA));
  }

  // Tests subtyping when record has more properties than super record
  @Test
  public void testIsSubtype_recordWithSupersetProperties_isSubtype() {
    RecordType subRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    RecordType superRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue(subRecord.isSubtype(superRecord));
    assertFalse(superRecord.isSubtype(subRecord));
  }

  // Tests subtyping when record is missing a property
  @Test
  public void testIsSubtype_missingRequiredProperty_returnsFalse() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();

    assertFalse(recordA.isSubtype(recordB));
  }

  // Tests subtyping when property type is incompatible
  @Test
  public void testIsSubtype_incompatibleDeclaredPropertyType_returnsFalse() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();

    assertFalse(recordA.isSubtype(recordB));
  }

  // Tests subtyping with non-record target
  @Test
  public void testIsSubtype_againstNativeObjectType_returnsTrue() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue(record.isSubtype(objectType));
    assertFalse(record.isSubtype(numberType));
  }

  // Tests static isSubtype helper method directly
  @Test
  public void testStaticIsSubtype_withMatchingDeclaredProperties_returnsTrue() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("x", numberType, null)
        .addProperty("y", stringType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("x", numberType, null)
        .build();

    assertTrue(RecordType.isSubtype(recordA, recordB));
    assertFalse(RecordType.isSubtype(recordB, recordA));
  }

  // Tests greatest subtype helper when combining compatible record types
  @Test
  public void testGetGreatestSubtypeHelper_compatibleRecordTypes_combinesProperties() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();

    JSType greatestSubtype = recordA.getGreatestSubtypeHelper(recordB);
    assertNotNull(greatestSubtype);
    assertTrue(greatestSubtype.isRecordType());

    RecordType resultRecord = greatestSubtype.toMaybeRecordType();
    assertTrue(resultRecord.hasProperty("a"));
    assertTrue(resultRecord.hasProperty("b"));
    assertEquals(numberType, resultRecord.getPropertyType("a"));
    assertEquals(stringType, resultRecord.getPropertyType("b"));
  }

  // Tests greatest subtype helper with conflicting property types returns NO_TYPE
  @Test
  public void testGetGreatestSubtypeHelper_conflictingPropertyTypes_returnsNoType() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", stringType, null)
        .build();

    JSType greatestSubtype = recordA.getGreatestSubtypeHelper(recordB);
    assertEquals(registry.getNativeObjectType(JSTypeNative.NO_TYPE), greatestSubtype);
  }

  // Tests greatest subtype helper when other type is non-record type
  @Test
  public void testGetGreatestSubtypeHelper_nonRecordType_returnsSubtype() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    JSType result = record.getGreatestSubtypeHelper(numberType);
    assertNotNull(result);
  }

  // Tests resolveInternal updates property types in the record
  @Test
  public void testResolveInternal_resolvesContainedTypes() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop", numberType, null)
        .build();

    SimpleErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = record.resolveInternal(reporter, null);

    assertNotNull(resolved);
    assertTrue(resolved.isRecordType());
    assertEquals(numberType, resolved.toMaybeRecordType().getPropertyType("prop"));
  }

  @Test
  public void testIsRecordType_returnsTrue() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertTrue(record.isRecordType());
  }

  @Test
  public void testGetPropertyNode_withAndWithoutPropertyNode() {
    Node node = new Node(0);
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("withNode", numberType, node)
        .addProperty("withoutNode", stringType, null)
        .build();

    assertSame(node, record.getPropertyNode("withNode"));
    assertNull(record.getPropertyNode("withoutNode"));
    assertNull(record.getPropertyNode("nonExistent"));
  }

  @Test
  public void testIsPropertyTypeDeclaredAndInferred() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("declaredProp", numberType, null)
        .build();

    assertTrue(record.isPropertyTypeDeclared("declaredProp"));
    assertFalse(record.isPropertyTypeInferred("declaredProp"));
    assertFalse(record.isPropertyTypeDeclared("nonExistent"));
    assertFalse(record.isPropertyTypeInferred("nonExistent"));
  }

  @Test
  public void testToStringHelper_emptyAndPopulated() {
    RecordType emptyRecord = (RecordType) new RecordTypeBuilder(registry).build();
    assertEquals("{}", emptyRecord.toString());

    RecordType singleProp = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertEquals("{a: number}", singleProp.toString());

    RecordType multiProp = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .addProperty("b", stringType, null)
        .build();
    assertEquals("{a: number, b: string}", multiProp.toString());
  }

  @Test
  public void testToAnnotationString() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertEquals("{a: number}", record.toAnnotationString());
  }

  @Test
  public void testIsSynthetic() {
    RecordType declaredRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    assertFalse(declaredRecord.isSynthetic());

    RecordType syntheticRecord = new RecordType(
        registry,
        Collections.<String, RecordProperty>emptyMap(),
        false);
    assertTrue(syntheticRecord.isSynthetic());
  }

  @Test
  public void testStaticIsSubtype_nonRecordObjectTypeWithProperties() {
    RecordType targetRecord = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("prop", numberType, null)
        .build();

    ObjectType matchingObj = registry.createAnonymousObjectType();
    matchingObj.defineDeclaredProperty("prop", numberType, null);
    assertTrue(RecordType.isSubtype(matchingObj, targetRecord));

    ObjectType missingPropObj = registry.createAnonymousObjectType();
    assertFalse(RecordType.isSubtype(missingPropObj, targetRecord));

    ObjectType incompatiblePropObj = registry.createAnonymousObjectType();
    incompatiblePropObj.defineDeclaredProperty("prop", stringType, null);
    assertFalse(RecordType.isSubtype(incompatiblePropObj, targetRecord));
  }

  @Test
  public void testGetGreatestSubtype_directCall() {
    RecordType recordA = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();
    RecordType recordB = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("b", stringType, null)
        .build();

    JSType greatestSubtype = recordA.getGreatestSubtype(recordB);
    assertNotNull(greatestSubtype);
    assertTrue(greatestSubtype.isRecordType());
    assertTrue(greatestSubtype.toMaybeRecordType().hasProperty("a"));
    assertTrue(greatestSubtype.toMaybeRecordType().hasProperty("b"));
  }

  @Test
  public void testGetGreatestSubtype_withSameType() {
    RecordType record = (RecordType) new RecordTypeBuilder(registry)
        .addProperty("a", numberType, null)
        .build();

    assertSame(record, record.getGreatestSubtype(record));
  }
}