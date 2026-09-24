package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType objectType;
  private JSType nullType;
  private JSType voidType;
  private JSType unknownType;
  private JSType allType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
  }

  // Tests isEmptyType on empty types (NoType, NoObjectType, NoResolvedType)
  @Test
  public void testIsEmptyType_emptyTypes_returnsTrue() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
  }

  // Tests isEmptyType on non-empty types
  @Test
  public void testIsEmptyType_nonEmptyTypes_returnsFalse() {
    assertFalse(numberType.isEmptyType());
    assertFalse(stringType.isEmptyType());
    assertFalse(objectType.isEmptyType());
    assertFalse(allType.isEmptyType());
    assertFalse(unknownType.isEmptyType());
  }

  // Tests isString and isNumber predicates
  @Test
  public void testIsStringAndIsNumber_matchingAndNonMatchingTypes_returnsExpected() {
    assertTrue(stringType.isString());
    assertFalse(numberType.isString());
    assertTrue(numberType.isNumber());
    assertFalse(stringType.isNumber());
  }

  // Tests isNullable predicate
  @Test
  public void testIsNullable_nullAndNonNullTypes_returnsExpected() {
    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());
    assertFalse(objectType.isNullable());
  }

  // Tests isEquivalentTo and static isEquivalent helper with nulls and values
  @Test
  public void testIsEquivalent_nullAndNonNullCombinations_returnsCorrectResult() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals("notAJSType"));
  }

  // Tests differsFrom with unknown and non-unknown types
  @Test
  public void testDiffersFrom_unknownAndRegularTypes_returnsCorrectResult() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(numberType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(numberType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  // Tests canAssignTo relationship
  @Test
  public void testCanAssignTo_subtypesAndSuperTypes_returnsCorrectBoolean() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertTrue(noType.canAssignTo(numberType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  // Tests getLeastSupertype computation
  @Test
  public void testGetLeastSupertype_variousTypes_returnsUnionOrSupremum() {
    assertEquals(numberType, numberType.getLeastSupertype(numberType));
    assertEquals(allType, numberType.getLeastSupertype(allType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
  }

  // Tests getGreatestSubtype computation
  @Test
  public void testGetGreatestSubtype_subtypesAndDisjointTypes_returnsInfimum() {
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    assertEquals(numberType, numberType.getGreatestSubtype(allType));
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
    assertEquals(noType, numberType.getGreatestSubtype(stringType));
    assertEquals(noObjectType, objectType.getGreatestSubtype(objectType.getGreatestSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE))));
  }

  // Tests testForEquality between empty types and other types
  @Test
  public void testTestForEquality_emptyTypes_returnsTrueOrUnknown() {
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noObjectType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
  }

  // Tests getTypesUnderEquality and getTypesUnderInequality
  @Test
  public void testGetTypesUnderEqualityAndInequality_compatibleTypes_returnsTypePairs() {
    JSType.TypePair eqPair = numberType.getTypesUnderEquality(stringType);
    assertNotNull(eqPair);

    JSType.TypePair ineqPair = numberType.getTypesUnderInequality(stringType);
    assertNotNull(ineqPair);
    assertEquals(numberType, ineqPair.typeA);
    assertEquals(stringType, ineqPair.typeB);
  }

  // Tests getTypesUnderShallowEquality and getTypesUnderShallowInequality
  @Test
  public void testGetTypesUnderShallowEqualityAndInequality_nullAndVoidTypes_returnsExpectedPair() {
    JSType.TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertEquals(numberType, shallowEq.typeA);
    assertEquals(numberType, shallowEq.typeB);

    JSType.TypePair nullShallowIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(nullShallowIneq.typeA);
    assertNull(nullShallowIneq.typeB);

    JSType.TypePair voidShallowIneq = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(voidShallowIneq.typeA);
    assertNull(voidShallowIneq.typeB);

    JSType.TypePair numShallowIneq = numberType.getTypesUnderShallowInequality(stringType);
    assertEquals(numberType, numShallowIneq.typeA);
    assertEquals(stringType, numShallowIneq.typeB);
  }

  // Tests dereference and toObjectType methods
  @Test
  public void testDereferenceAndToObjectType_primitiveAndObjectTypes_returnsExpected() {
    assertNull(numberType.toObjectType());
    assertNotNull(objectType.toObjectType());
    assertNotNull(stringType.dereference());
    assertNull(nullType.dereference());
    assertNull(voidType.dereference());
  }

  // Tests matchesInt32Context and matchesUint32Context
  @Test
  public void testContextMatching_numberAndNonNumberTypes_returnsExpected() {
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(stringType.matchesInt32Context());
    assertFalse(stringType.matchesUint32Context());
  }

  // Tests resolve, forceResolve, safeResolve and clearResolved lifecycle
  @Test
  public void testResolveLifecycle_resolvingType_updatesResolvedState() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(null, null);
    assertNotNull(resolved);
    assertTrue(numberType.isResolved());

    JSType safeResolved = JSType.safeResolve(numberType, null, null);
    assertEquals(resolved, safeResolved);
    assertNull(JSType.safeResolve(null, null, null));

    JSType forceResolved = numberType.forceResolve(null, null);
    assertEquals(resolved, forceResolved);

    numberType.clearResolved();
    assertFalse(numberType.isResolved());
  }

  // Tests setValidator predicate evaluation
  @Test
  public void testSetValidator_customPredicate_appliesPredicate() {
    boolean accepted = numberType.setValidator(new Predicate<JSType>() {
      public boolean apply(JSType type) {
        return type.isNumber();
      }
    });
    assertTrue(accepted);

    boolean rejected = stringType.setValidator(new Predicate<JSType>() {
      public boolean apply(JSType type) {
        return type.isNumber();
      }
    });
    assertFalse(rejected);
  }

  // Tests toDebugHashCodeString format
  @Test
  public void testToDebugHashCodeString_validType_returnsFormattedString() {
    String debugStr = numberType.toDebugHashCodeString();
    assertTrue(debugStr.startsWith("{"));
    assertTrue(debugStr.endsWith("}"));
  }

  // Tests hasDisplayName and getDisplayName default behavior
  @Test
  public void testDisplayName_defaultType_returnsNullOrFalse() {
    assertNull(numberType.getDisplayName());
    assertFalse(numberType.hasDisplayName());
  }
}