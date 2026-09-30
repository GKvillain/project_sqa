package com.google.javascript.rhino.jstype;

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
    registry = new JSTypeRegistry(null);
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

  // Tests isEmptyType for NoType, NoObjectType, and NoResolvedType
  @Test
  public void testIsEmptyType_emptyTypes_returnsTrue() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
  }

  // Tests isEmptyType for non-empty types
  @Test
  public void testIsEmptyType_nonEmptyTypes_returnsFalse() {
    assertFalse(numberType.isEmptyType());
    assertFalse(stringType.isEmptyType());
    assertFalse(objectType.isEmptyType());
    assertFalse(unknownType.isEmptyType());
    assertFalse(allType.isEmptyType());
    assertFalse(nullType.isEmptyType());
    assertFalse(voidType.isEmptyType());
  }

  // Tests isString and isNumber checks
  @Test
  public void testIsStringAndIsNumber_validTypes_returnsExpected() {
    assertTrue(stringType.isString());
    assertFalse(stringType.isNumber());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isString());
    assertFalse(booleanType.isString());
    assertFalse(booleanType.isNumber());
  }

  // Tests static and instance isEquivalent
  @Test
  public void testIsEquivalent_sameAndDifferentTypes_returnsCorrect() {
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, stringType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.isEquivalentTo(stringType));
  }

  // Tests equals and hashCode
  @Test
  public void testEqualsAndHashCode_variousTypes_correctBehavior() {
    assertEquals(numberType, numberType);
    assertNotEquals(numberType, stringType);
    assertNotEquals(numberType, "not a type");
    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
    assertEquals("{" + numberType.hashCode() + "}", numberType.toDebugHashCodeString());
  }

  // Tests testForEquality for empty types
  @Test
  public void testTestForEquality_emptyTypes_returnsTrueOrUnknown() {
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noObjectType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noType));
  }

  // Tests testForEquality for unknown, all, and noResolved types
  @Test
  public void testTestForEquality_unknownAndAllTypes_returnsUnknown() {
    assertEquals(TernaryValue.UNKNOWN, unknownType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(stringType));
    assertEquals(TernaryValue.UNKNOWN, noResolvedType.testForEquality(numberType));
  }

  // Tests testForEquality for function type comparisons
  @Test
  public void testTestForEquality_functionTypeComparisons_returnsExpected() {
    JSType funcType = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals(TernaryValue.UNKNOWN, funcType.testForEquality(objectType));
    assertEquals(TernaryValue.FALSE, funcType.testForEquality(numberType));
    assertEquals(TernaryValue.FALSE, numberType.testForEquality(funcType));
  }

  // Tests canTestForEqualityWith and canTestForShallowEqualityWith
  @Test
  public void testCanTestForEquality_compatibleAndIncompatibleTypes_returnsExpected() {
    assertTrue(numberType.canTestForEqualityWith(stringType));
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
    assertTrue(unknownType.canTestForShallowEqualityWith(numberType));
  }

  // Tests getGreatestSubtype for various type combinations
  @Test
  public void testGetGreatestSubtype_variousTypes_returnsCorrectMeet() {
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
    assertEquals(unknownType, unknownType.getGreatestSubtype(numberType));
    assertEquals(noType, numberType.getGreatestSubtype(stringType));
    assertEquals(noObjectType, objectType.getGreatestSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE)));
  }

  // Tests getLeastSupertype for identical and different types
  @Test
  public void testGetLeastSupertype_variousTypes_returnsCorrectJoin() {
    assertEquals(numberType, numberType.getLeastSupertype(numberType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
  }

  // Tests filterNoResolvedType helper method
  @Test
  public void testFilterNoResolvedType_noResolvedType_returnsBaseNoResolvedType() {
    assertEquals(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertEquals(numberType, JSType.filterNoResolvedType(numberType));
  }

  // Tests differsFrom handling of unknown and regular types
  @Test
  public void testDiffersFrom_unknownAndRegularTypes_returnsExpected() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(numberType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(numberType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  // Tests dereference method for primitive and object types
  @Test
  public void testDereference_primitivesAndObjects_returnsObjectTypeOrNull() {
    ObjectType numberObj = numberType.dereference();
    assertNotNull(numberObj);
    assertTrue(numberObj.isObject());

    ObjectType objObj = objectType.dereference();
    assertNotNull(objObj);
    assertEquals(objectType, objObj);

    assertNull(nullType.dereference());
    assertNull(voidType.dereference());
  }

  // Tests getTypesUnderEquality and getTypesUnderInequality
  @Test
  public void testGetTypesUnderEqualityAndInequality_variousPairs_returnsRestrictedPairs() {
    JSType.TypePair eqPair = numberType.getTypesUnderEquality(stringType);
    assertNotNull(eqPair);
    assertEquals(numberType, eqPair.typeA);
    assertEquals(stringType, eqPair.typeB);

    JSType.TypePair ineqPair = numberType.getTypesUnderInequality(stringType);
    assertNotNull(ineqPair);
    assertEquals(numberType, ineqPair.typeA);
    assertEquals(stringType, ineqPair.typeB);

    JSType.TypePair shallowEqPair = numberType.getTypesUnderShallowEquality(stringType);
    assertNotNull(shallowEqPair);
    assertEquals(noType, shallowEqPair.typeA);
    assertEquals(noType, shallowEqPair.typeB);

    JSType.TypePair nullShallowIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNotNull(nullShallowIneq);
    assertNull(nullShallowIneq.typeA);
    assertNull(nullShallowIneq.typeB);
  }

  // Tests matches context methods
  @Test
  public void testMatchesContext_numberAndOtherTypes_returnsExpected() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(stringType.matchesNumberContext());
    assertFalse(stringType.matchesInt32Context());
    assertFalse(stringType.matchesUint32Context());
    assertFalse(numberType.matchesStringContext());
    assertFalse(numberType.matchesObjectContext());
  }

  // Tests resolution lifecycle: resolve, isResolved, clearResolved, safeResolve
  @Test
  public void testResolveLifecycle_typeResolution_resolvesProperly() {
    assertNotNull(JSType.safeResolve(null, null, null));
    assertNull(JSType.safeResolve(null, null, null));

    JSType resolved = numberType.resolve(null, null);
    assertEquals(numberType, resolved);
    assertTrue(numberType.isResolved());

    numberType.clearResolved();
    assertFalse(numberType.isResolved());
  }

  // Tests getRestrictedTypeGivenToBooleanOutcome
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_variousOutcomes_returnsRestricted() {
    JSType restrictedTrue = objectType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertEquals(objectType, restrictedTrue);

    JSType restrictedFalse = objectType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertEquals(noType, restrictedFalse);
  }

  // Tests displayName defaults
  @Test
  public void testDisplayName_defaultType_returnsNullAndFalse() {
    assertNull(numberType.getDisplayName());
    assertFalse(numberType.hasDisplayName());
  }

  // Tests comparator ALPHA
  @Test
  public void testAlphaComparator_orderTypes_sortsAlphabetically() {
    int benchComparison = numberType.toString().compareTo(stringType.toString());
    int comparison = JSType.ALPHA.compare(numberType, stringType);
    assertEquals(benchComparison, comparison);
  }

  // Tests type predicates (isNullable, isVoidType, isNullType, isUnknownType, isAllType, isUnionType, isFunctionType, isObject)
  @Test
  public void testTypePredicates() {
    assertTrue(nullType.isNullType());
    assertFalse(numberType.isNullType());

    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());

    assertTrue(voidType.isVoidType());
    assertFalse(numberType.isVoidType());

    assertTrue(unknownType.isUnknownType());
    assertFalse(numberType.isUnknownType());

    JSType checkedUnknown = registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
    assertTrue(checkedUnknown.isCheckedUnknownType());
    assertFalse(unknownType.isCheckedUnknownType());

    assertTrue(allType.isAllType());
    assertFalse(numberType.isAllType());

    assertTrue(noType.isNoType());
    assertFalse(numberType.isNoType());

    assertTrue(noObjectType.isNoObjectType());
    assertFalse(objectType.isNoObjectType());

    assertTrue(noResolvedType.isNoResolvedType());
    assertFalse(numberType.isNoResolvedType());

    assertTrue(objectType.isObject());
    assertFalse(numberType.isObject());

    assertFalse(numberType.isUnionType());
    assertFalse(numberType.isFunctionType());
    assertFalse(numberType.isEnumElementType());
    assertFalse(numberType.isTemplateType());
    assertFalse(numberType.isRecordType());
  }

  // Tests type downcasting / toMaybe methods
  @Test
  public void testToMaybeMethods() {
    assertNull(numberType.toMaybeFunctionType());
    assertNull(numberType.toMaybeObjectType());
    assertNull(numberType.toMaybeUnionType());
    assertNull(numberType.toMaybeRecordType());
    assertNull(numberType.toMaybeTemplateType());
    assertNull(numberType.toMaybeEnumElementType());
    assertNull(numberType.toMaybeParameterizedType());

    assertNotNull(objectType.toMaybeObjectType());
    JSType funcType = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertNotNull(funcType.toMaybeObjectType());
  }

  // Tests restrictByNotNullOrUndefined
  @Test
  public void testRestrictByNotNullOrUndefined() {
    assertEquals(numberType, numberType.restrictByNotNullOrUndefined());
    assertEquals(noType, nullType.restrictByNotNullOrUndefined());
    assertEquals(noType, voidType.restrictByNotNullOrUndefined());

    JSType unionWithNull = registry.createUnionType(numberType, nullType);
    assertEquals(numberType, unionWithNull.restrictByNotNullOrUndefined());
  }

  // Tests autoboxing and unboxing
  @Test
  public void testAutoboxesAndUnboxesTo() {
    JSType numObj = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertEquals(numObj, numberType.autoboxesTo());
    assertNull(objectType.autoboxesTo());

    assertEquals(numberType, numObj.unboxesTo());
    assertNull(numberType.unboxesTo());
  }

  // Tests subtyping and assignment relations
  @Test
  public void testSubtypingAndAssignability() {
    assertTrue(numberType.isSubtype(numberType));
    assertTrue(numberType.isSubtypeOf(numberType));
    assertFalse(numberType.isSubtype(stringType));
    assertFalse(numberType.isSubtypeOf(stringType));

    assertTrue(noType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(unknownType.isSubtype(numberType));

    assertTrue(numberType.canAssignTo(numberType));
    assertFalse(numberType.canAssignTo(stringType));
    assertTrue(numberType.canAssignTo(unknownType));
  }

  // Tests boolean type predicates and context matches
  @Test
  public void testBooleanAndStringContext() {
    assertTrue(booleanType.isBooleanValueType());
    assertFalse(numberType.isBooleanValueType());

    assertTrue(stringType.isStringValueType());
    assertFalse(booleanType.isStringValueType());

    assertTrue(numberType.isNumberValueType());
    assertFalse(stringType.isNumberValueType());

    assertTrue(stringType.matchesStringContext());
    assertTrue(objectType.matchesObjectContext());
    assertFalse(booleanType.matchesObjectContext());
    assertFalse(numberType.canBeCalled());
  }

  // Tests built-in object classification
  @Test
  public void testObjectClassification() {
    JSType dateType = registry.getNativeType(JSTypeNative.DATE_TYPE);
    JSType regexpType = registry.getNativeType(JSTypeNative.REGEXP_TYPE);
    JSType arrayType = registry.getNativeType(JSTypeNative.ARRAY_TYPE);

    assertTrue(dateType.isDateType());
    assertFalse(numberType.isDateType());

    assertTrue(regexpType.isRegexpType());
    assertFalse(numberType.isRegexpType());

    assertTrue(arrayType.isArrayType());
    assertFalse(numberType.isArrayType());

    assertTrue(objectType.isTheObjectType());
    assertFalse(numberType.isTheObjectType());
  }

  // Tests toAnnotationString and toString representation
  @Test
  public void testToStringAndAnnotationString() {
    assertEquals("number", numberType.toString());
    assertEquals("string", stringType.toString());
    assertEquals("boolean", booleanType.toString());
    assertEquals("number", numberType.toAnnotationString());
  }

  // Tests property query defaults on primitive types
  @Test
  public void testPropertyQueriesOnPrimitives() {
    assertFalse(numberType.hasProperty("toString"));
    assertNotNull(numberType.findPropertyType("toString"));
    assertNull(nullType.findPropertyType("toString"));
    assertNull(voidType.findPropertyType("toString"));
  }

  // Tests getPossibleInstance
  @Test
  public void testGetPossibleInstance() {
    assertNull(numberType.getPossibleInstance());
    assertNull(objectType.getPossibleInstance());
  }

  // Tests collapseUnion
  @Test
  public void testCollapseUnion() {
    assertEquals(numberType, numberType.collapseUnion());
    JSType union = registry.createUnionType(numberType, stringType);
    assertEquals(union, union.collapseUnion());
  }

  // Tests nominal and constructor predicates
  @Test
  public void testNominalAndConstructorPredicates() {
    assertFalse(numberType.isNominalType());
    assertFalse(numberType.isNominalConstructor());
    assertFalse(numberType.isInstanceType());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isOrdinaryFunction());
    assertFalse(numberType.isGlobalThisType());
  }
}