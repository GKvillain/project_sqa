package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

public class UnionTypeTest {

  private JSTypeRegistry registry;
  private JSType NUMBER_TYPE;
  private JSType STRING_TYPE;
  private JSType BOOLEAN_TYPE;
  private JSType NULL_TYPE;
  private JSType VOID_TYPE;
  private JSType UNKNOWN_TYPE;
  private JSType ALL_TYPE;
  private JSType OBJECT_TYPE;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    NUMBER_TYPE = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING_TYPE = registry.getNativeType(JSTypeNative.STRING_TYPE);
    BOOLEAN_TYPE = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    NULL_TYPE = registry.getNativeType(JSTypeNative.NULL_TYPE);
    VOID_TYPE = registry.getNativeType(JSTypeNative.VOID_TYPE);
    UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ALL_TYPE = registry.getNativeType(JSTypeNative.ALL_TYPE);
    OBJECT_TYPE = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    Collection<JSType> list = ImmutableList.copyOf(types);
    return new UnionType(registry, list);
  }

  // Tests getAlternates and contains method
  @Test
  public void testContains_matchingAndNonMatchingTypes_returnsExpected() {
    UnionType union = createUnion(NUMBER_TYPE, STRING_TYPE);
    assertTrue(union.contains(NUMBER_TYPE));
    assertTrue(union.contains(STRING_TYPE));
    assertFalse(union.contains(BOOLEAN_TYPE));
    assertSame(union, union.toMaybeUnionType());
  }

  // Tests matchesNumberContext across alternates
  @Test
  public void testMatchesNumberContext_withNumericAndNonNumeric_returnsTrue() {
    UnionType union1 = createUnion(NUMBER_TYPE, STRING_TYPE);
    assertTrue(union1.matchesNumberContext());

    UnionType union2 = createUnion(VOID_TYPE, NULL_TYPE);
    assertFalse(union2.matchesNumberContext());
  }

  // Tests matchesStringContext across alternates
  @Test
  public void testMatchesStringContext_withStringAndNonString_returnsExpected() {
    UnionType union1 = createUnion(STRING_TYPE, NUMBER_TYPE);
    assertTrue(union1.matchesStringContext());

    UnionType union2 = createUnion(VOID_TYPE);
    assertFalse(union2.matchesStringContext());
  }

  // Tests matchesObjectContext across alternates
  @Test
  public void testMatchesObjectContext_withObjectAndPrimitive_returnsExpected() {
    UnionType union1 = createUnion(OBJECT_TYPE, NUMBER_TYPE);
    assertTrue(union1.matchesObjectContext());

    UnionType union2 = createUnion(NULL_TYPE, VOID_TYPE);
    assertFalse(union2.matchesObjectContext());
  }

  // Tests isNullable predicate
  @Test
  public void testIsNullable_withAndWithoutNull_returnsExpected() {
    UnionType unionWithNull = createUnion(NUMBER_TYPE, NULL_TYPE);
    assertTrue(unionWithNull.isNullable());

    UnionType unionWithoutNull = createUnion(NUMBER_TYPE, BOOLEAN_TYPE);
    assertFalse(unionWithoutNull.isNullable());
  }

  // Tests isUnknownType predicate
  @Test
  public void testIsUnknownType_withAndWithoutUnknown_returnsExpected() {
    UnionType unionWithUnknown = createUnion(NUMBER_TYPE, UNKNOWN_TYPE);
    assertTrue(unionWithUnknown.isUnknownType());

    UnionType unionWithoutUnknown = createUnion(NUMBER_TYPE, STRING_TYPE);
    assertFalse(unionWithoutUnknown.isUnknownType());
  }

  // Tests isObject predicate
  @Test
  public void testIsObject_withAllObjectsOrMixed_returnsExpected() {
    UnionType objectUnion = createUnion(OBJECT_TYPE);
    assertTrue(objectUnion.isObject());

    UnionType mixedUnion = createUnion(OBJECT_TYPE, NUMBER_TYPE);
    assertFalse(mixedUnion.isObject());
  }

  // Tests isSubtype against unknown, all type, and specific types
  @Test
  public void testIsSubtype_variousTargets_returnsExpected() {
    UnionType numOrStr = createUnion(NUMBER_TYPE, STRING_TYPE);
    assertTrue(numOrStr.isSubtype(UNKNOWN_TYPE));
    assertTrue(numOrStr.isSubtype(ALL_TYPE));
    assertFalse(numOrStr.isSubtype(NUMBER_TYPE));

    JSType superUnion = registry.createUnionType(NUMBER_TYPE, STRING_TYPE, BOOLEAN_TYPE);
    assertTrue(numOrStr.isSubtype(superUnion));
  }

  // Tests canAssignTo behavior
  @Test
  public void testCanAssignTo_assignableAndUnknown_returnsExpected() {
    UnionType numOrStr = createUnion(NUMBER_TYPE, STRING_TYPE);
    JSType superUnion = registry.createUnionType(NUMBER_TYPE, STRING_TYPE, BOOLEAN_TYPE);
    assertTrue(numOrStr.canAssignTo(superUnion));
    assertFalse(numOrStr.canAssignTo(NUMBER_TYPE));

    UnionType withUnknown = createUnion(NUMBER_TYPE, UNKNOWN_TYPE);
    assertTrue(withUnknown.canAssignTo(NUMBER_TYPE));
  }

  // Tests restrictByNotNullOrUndefined
  @Test
  public void testRestrictByNotNullOrUndefined_removesNullAndVoid() {
    UnionType union = createUnion(NUMBER_TYPE, NULL_TYPE, VOID_TYPE);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertEquals(NUMBER_TYPE, restricted);
  }

  // Tests getRestrictedUnion by removing subtypes
  @Test
  public void testGetRestrictedUnion_removesSpecifiedSubtype() {
    UnionType union = createUnion(NUMBER_TYPE, STRING_TYPE);
    JSType restricted = union.getRestrictedUnion(NUMBER_TYPE);
    assertEquals(STRING_TYPE, restricted);
  }

  // Tests testForEquality across union alternates
  @Test
  public void testTestForEquality_sameAndDifferentOutcomes_returnsExpected() {
    UnionType numOrStr = createUnion(NUMBER_TYPE, STRING_TYPE);
    assertEquals(TernaryValue.UNKNOWN, numOrStr.testForEquality(NUMBER_TYPE));
    assertEquals(TernaryValue.FALSE, numOrStr.testForEquality(VOID_TYPE));
  }

  // Tests getLeastSupertype with union and non-union
  @Test
  public void testGetLeastSupertype_withSubtypeAlternate_returnsThis() {
    UnionType numOrStr = createUnion(NUMBER_TYPE, STRING_TYPE);
    JSType leastSuper = numOrStr.getLeastSupertype(NUMBER_TYPE);
    assertEquals(numOrStr, leastSuper);
  }

  // Tests meet method on UnionType
  @Test
  public void testMeet_overlappingAndDisjointTypes_returnsExpected() {
    UnionType numOrStr = createUnion(NUMBER_TYPE, STRING_TYPE);
    JSType intersection = numOrStr.meet(NUMBER_TYPE);
    assertEquals(NUMBER_TYPE, intersection);

    UnionType strOrBool = createUnion(STRING_TYPE, BOOLEAN_TYPE);
    JSType unionMeet = numOrStr.meet(strOrBool);
    assertEquals(STRING_TYPE, unionMeet);
  }

  // Tests checkUnionEquivalenceHelper
  @Test
  public void testCheckUnionEquivalenceHelper_equalAndUnequalUnions() {
    UnionType union1 = createUnion(NUMBER_TYPE, STRING_TYPE);
    UnionType union2 = createUnion(STRING_TYPE, NUMBER_TYPE);
    UnionType union3 = createUnion(NUMBER_TYPE, BOOLEAN_TYPE);

    assertTrue(union1.checkUnionEquivalenceHelper(union2, false));
    assertFalse(union1.checkUnionEquivalenceHelper(union3, false));
  }

  // Tests collapseUnion behavior
  @Test
  public void testCollapseUnion_mixedValuesAndObjects_returnsAllType() {
    UnionType mixed = createUnion(NUMBER_TYPE, OBJECT_TYPE);
    assertEquals(ALL_TYPE, mixed.collapseUnion());

    UnionType withUnknown = createUnion(NUMBER_TYPE, UNKNOWN_TYPE);
    assertEquals(UNKNOWN_TYPE, withUnknown.collapseUnion());
  }

  // Tests toStringHelper and toDebugHashCodeString
  @Test
  public void testToStringAndDebugString_formatsCorrectly() {
    UnionType union = createUnion(NUMBER_TYPE, STRING_TYPE);
    String str = union.toStringHelper(false);
    assertTrue(str.startsWith("(") && str.endsWith(")"));
    assertTrue(str.contains("number") && str.contains("string"));

    String debugStr = union.toDebugHashCodeString();
    assertTrue(debugStr.startsWith("{(") && debugStr.endsWith(")}"));
  }

  // Tests TypePair operations (getTypesUnderEquality, getTypesUnderInequality)
  @Test
  public void testGetTypesUnderEqualityAndInequality_returnsValidPairs() {
    UnionType union = createUnion(NUMBER_TYPE, NULL_TYPE);
    JSType.TypePair eqPair = union.getTypesUnderEquality(NULL_TYPE);
    assertNotNull(eqPair.typeA);
    assertNotNull(eqPair.typeB);

    JSType.TypePair ineqPair = union.getTypesUnderInequality(NULL_TYPE);
    assertNotNull(ineqPair.typeA);
    assertNotNull(ineqPair.typeB);
  }

  // Tests autobox method
  @Test
  public void testAutobox_primitiveUnion_returnsObjectUnion() {
    UnionType union = createUnion(NUMBER_TYPE, STRING_TYPE);
    JSType autoboxed = union.autobox();
    assertTrue(autoboxed.isObject());
  }

  // Tests findPropertyType when property exists or does not exist
  @Test
  public void testFindPropertyType_withNullAndObject_handlesFiltering() {
    ObjectType objWithProp = registry.createAnonymousObjectType();
    objWithProp.defineDeclaredProperty("foo", NUMBER_TYPE, null);

    UnionType union = createUnion(NULL_TYPE, objWithProp);
    JSType propType = union.findPropertyType("foo");
    assertEquals(NUMBER_TYPE, propType);

    assertNull(union.findPropertyType("nonExistent"));
  }
}