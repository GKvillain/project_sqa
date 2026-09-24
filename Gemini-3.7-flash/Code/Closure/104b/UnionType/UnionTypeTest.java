package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class UnionTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;
  private JSType unknownType;
  private JSType noType;
  private JSType noObjectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    booleanType = registry.getNativeType(BOOLEAN_TYPE);
    nullType = registry.getNativeType(NULL_TYPE);
    voidType = registry.getNativeType(VOID_TYPE);
    objectType = registry.getNativeType(OBJECT_TYPE);
    unknownType = registry.getNativeType(UNKNOWN_TYPE);
    noType = registry.getNativeType(NO_TYPE);
    noObjectType = registry.getNativeType(NO_OBJECT_TYPE);
  }

  // Tests isUnionType returns true for UnionType instances
  @Test
  public void testIsUnionType_unionInstance_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());
  }

  // Tests matchesNumberContext when one alternate matches number context
  @Test
  public void testMatchesNumberContext_containsNumber_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.matchesNumberContext());
  }

  // Tests matchesNumberContext when no alternate matches number context
  @Test
  public void testMatchesNumberContext_onlyVoidAndNull_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(voidType, nullType);
    assertFalse(union.matchesNumberContext());
  }

  // Tests matchesStringContext when one alternate matches string context
  @Test
  public void testMatchesStringContext_containsString_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.matchesStringContext());
  }

  // Tests matchesObjectContext when alternates are not null or void
  @Test
  public void testMatchesObjectContext_containsObject_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(objectType, stringType);
    assertTrue(union.matchesObjectContext());
  }

  // Tests matchesObjectContext when alternates are only null and void
  @Test
  public void testMatchesObjectContext_onlyNullAndVoid_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(nullType, voidType);
    assertFalse(union.matchesObjectContext());
  }

  // Tests isNullable when union contains null
  @Test
  public void testIsNullable_containsNull_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(numberType, nullType);
    assertTrue(union.isNullable());
  }

  // Tests isNullable when union does not contain null or nullable types
  @Test
  public void testIsNullable_noNull_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(numberType, booleanType);
    assertFalse(union.isNullable());
  }

  // Tests isUnknownType when one alternate is unknown
  @Test
  public void testIsUnknownType_containsUnknown_returnsTrue() {
    UnionType union = (UnionType) registry.createUnionType(numberType, unknownType);
    assertTrue(union.isUnknownType());
  }

  // Tests isUnknownType when no alternate is unknown
  @Test
  public void testIsUnknownType_noUnknown_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertFalse(union.isUnknownType());
  }

  // Tests isObject when all alternates are objects
  @Test
  public void testIsObject_allObjects_returnsTrue() {
    ObjectType obj1 = registry.createAnonymousObjectType();
    ObjectType obj2 = registry.createAnonymousObjectType();
    UnionType union = (UnionType) registry.createUnionType(obj1, obj2);
    assertTrue(union.isObject());
  }

  // Tests isObject when some alternates are primitives
  @Test
  public void testIsObject_containsPrimitive_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(objectType, numberType);
    assertFalse(union.isObject());
  }

  // Tests contains method for present and absent alternates
  @Test
  public void testContains_alternateCheck_returnsExpected() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.contains(numberType));
    assertTrue(union.contains(stringType));
    assertFalse(union.contains(booleanType));
  }

  // Tests equals and hashCode consistency
  @Test
  public void testEquals_sameAlternatesDifferentOrder_areEqual() {
    UnionType union1 = (UnionType) registry.createUnionType(numberType, stringType);
    UnionType union2 = (UnionType) registry.createUnionType(stringType, numberType);
    assertEquals(union1, union2);
    assertEquals(union1.hashCode(), union2.hashCode());
    assertFalse(union1.equals(numberType));
  }

  // Tests restrictByNotNullOrUndefined removes null and void from union
  @Test
  public void testRestrictByNotNullOrUndefined_containsNullAndVoid_removesBoth() {
    UnionType union = (UnionType) registry.createUnionType(numberType, nullType, voidType);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertEquals(numberType, restricted);
  }

  // Tests getRestrictedUnion removes subtypes of specified type
  @Test
  public void testGetRestrictedUnion_removeNumber_returnsString() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    JSType restricted = union.getRestrictedUnion(numberType);
    assertEquals(stringType, restricted);
  }

  // Tests meet method between two object unions when intersection is empty
  @Test
  public void testMeet_disjointObjectUnions_returnsNoObjectType() {
    ObjectType obj1 = registry.createAnonymousObjectType();
    ObjectType obj2 = registry.createAnonymousObjectType();
    ObjectType obj3 = registry.createAnonymousObjectType();
    ObjectType obj4 = registry.createAnonymousObjectType();

    UnionType union1 = (UnionType) registry.createUnionType(obj1, obj2);
    UnionType union2 = (UnionType) registry.createUnionType(obj3, obj4);

    JSType result = union1.meet(union2);
    assertEquals(noObjectType, result);
  }

  // Tests meet method between non-object unions when intersection is empty
  @Test
  public void testMeet_disjointPrimitiveUnions_returnsNoType() {
    UnionType union1 = (UnionType) registry.createUnionType(numberType, stringType);
    UnionType union2 = (UnionType) registry.createUnionType(booleanType, nullType);

    JSType result = union1.meet(union2);
    assertEquals(noType, result);
  }

  // Tests isSubtype when union is subtype of ALL_TYPE and not of primitive
  @Test
  public void testIsSubtype_variousTypes_returnsExpected() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    JSType allType = registry.getNativeType(ALL_TYPE);
    assertTrue(union.isSubtype(allType));
    assertFalse(union.isSubtype(numberType));
  }

  // Tests testForEquality across different combinations
  @Test
  public void testTestForEquality_mixedAlternates_returnsUnknown() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertEquals(TernaryValue.UNKNOWN, union.testForEquality(numberType));
  }
}