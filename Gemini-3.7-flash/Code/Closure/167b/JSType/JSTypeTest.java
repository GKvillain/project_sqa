package com.google.javascript.rhino.jstype;

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
  private JSType allType;
  private JSType unknownType;
  private JSType noType;
  private JSType nullType;
  private JSType voidType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
  }

  // Tests null-safe equivalence method with both null and non-null arguments
  @Test
  public void testIsEquivalent_nullAndNonNullInputs_returnsExpectedResult() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
  }

  // Tests equals and hashCode contract on base types
  @Test
  public void testEqualsAndHashCode_sameAndDifferentTypes_consistentResult() {
    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals("not a type"));
    assertEquals(numberType.hashCode(), numberType.hashCode());
  }

  // Tests differsFrom method logic with unknown and known types
  @Test
  public void testDiffersFrom_knownAndUnknownTypes_returnsCorrectComparison() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(numberType.differsFrom(unknownType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  // Tests subtyping lattice relationships between basic types
  @Test
  public void testIsSubtype_latticeRelationships_returnsExpectedHierarchy() {
    assertTrue(numberType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(noType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));
    assertFalse(allType.isSubtype(numberType));
  }

  // Tests canAssignTo based on subtype relations
  @Test
  public void testCanAssignTo_compatibleAndIncompatibleTypes_returnsExpected() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  // Tests isString and isNumber utility predicates
  @Test
  public void testIsStringAndIsNumber_typeCheckPredicates_returnsCorrectBooleans() {
    assertTrue(stringType.isString());
    assertFalse(numberType.isString());
    assertTrue(numberType.isNumber());
    assertFalse(stringType.isNumber());
  }

  // Tests isEmptyType method for NoType vs non-empty types
  @Test
  public void testIsEmptyType_noTypeAndNormalTypes_identifiedCorrectly() {
    assertTrue(noType.isEmptyType());
    assertFalse(numberType.isEmptyType());
    assertFalse(allType.isEmptyType());
  }

  // Tests canTestForEqualityWith and testForEquality behavior
  @Test
  public void testCanTestForEqualityWith_variousTypes_returnsExpected() {
    assertTrue(unknownType.canTestForEqualityWith(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
  }

  // Tests shallow equality check compatibility
  @Test
  public void testCanTestForShallowEqualityWith_sameAndDifferentTypes_returnsExpected() {
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
    assertTrue(noType.canTestForShallowEqualityWith(numberType));
  }

  // Tests getLeastSupertype computation (least upper bound)
  @Test
  public void testGetLeastSupertype_disjointAndEquivalentTypes_computesUnionOrSupertype() {
    assertEquals(numberType, numberType.getLeastSupertype(numberType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
    assertEquals(allType, numberType.getLeastSupertype(allType));
  }

  // Tests getGreatestSubtype computation (greatest lower bound)
  @Test
  public void testGetGreatestSubtype_disjointAndSubtypePairs_computesCorrectInfimum() {
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
    assertEquals(noType, numberType.getGreatestSubtype(stringType));
  }

  // Tests getTypesUnderEquality outcome
  @Test
  public void testGetTypesUnderEquality_compatibleTypes_returnsTypePair() {
    JSType.TypePair pair = numberType.getTypesUnderEquality(allType);
    assertEquals(numberType, pair.typeA);
    assertEquals(allType, pair.typeB);
  }

  // Tests getTypesUnderInequality outcome for identical types
  @Test
  public void testGetTypesUnderInequality_differentTypes_returnsTypePair() {
    JSType.TypePair pair = numberType.getTypesUnderInequality(stringType);
    assertEquals(numberType, pair.typeA);
    assertEquals(stringType, pair.typeB);
  }

  // Tests getTypesUnderShallowInequality for null and void
  @Test
  public void testGetTypesUnderShallowInequality_nullAndVoidTypes_returnsExpectedRestrictedPair() {
    JSType.TypePair pairNull = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(pairNull.typeA);
    assertNull(pairNull.typeB);

    JSType.TypePair pairVoid = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(pairVoid.typeA);
    assertNull(pairVoid.typeB);

    JSType.TypePair pairDiff = numberType.getTypesUnderShallowInequality(stringType);
    assertEquals(numberType, pairDiff.typeA);
    assertEquals(stringType, pairDiff.typeB);
  }

  // Tests getRestrictedTypeGivenToBooleanOutcome restricting truthy/falsy values
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_restrictsBasedOnBooleanLiteral() {
    JSType restrictedTrue = nullType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(restrictedTrue.isEmptyType());

    JSType restrictedFalse = nullType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertEquals(nullType, restrictedFalse);
  }

  // Tests autobox and dereference behavior on primitive value type
  @Test
  public void testAutoboxAndDereference_primitiveValue_returnsAutoboxedObjectType() {
    JSType autoboxed = numberType.autobox();
    assertNotNull(autoboxed);
    assertTrue(autoboxed.isObject());

    ObjectType dereferenced = numberType.dereference();
    assertNotNull(dereferenced);
    assertTrue(dereferenced.isObject());
  }

  // Tests isNullable check
  @Test
  public void testIsNullable_nullAndOtherTypes_returnsCorrectBoolean() {
    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());
  }

  // Tests static downcast helpers with null inputs
  @Test
  public void testToMaybeFunctionAndParameterizedType_nullInput_returnsNull() {
    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(JSType.toMaybeFunctionType(numberType));
    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeParameterizedType(numberType));
    assertNull(JSType.toMaybeTemplateType(null));
    assertNull(JSType.toMaybeTemplateType(numberType));
  }

  // Tests safeResolve helper with null and non-null types
  @Test
  public void testSafeResolve_nullAndResolvedTypes_handledSafely() {
    assertNull(JSType.safeResolve(null, null, null));
    JSType resolved = JSType.safeResolve(numberType, null, null);
    assertEquals(numberType, resolved);
    assertTrue(numberType.isResolved());
  }

  // Tests ALPHA comparator for sorting types
  @Test
  public void testAlphaComparator_comparesByToString() {
    int cmp = JSType.ALPHA.compare(numberType, stringType);
    assertEquals(numberType.toString().compareTo(stringType.toString()), cmp);
  }

  // Tests restrictByNotNullOrUndefined for primitive and union types
  @Test
  public void testRestrictByNotNullOrUndefined_primitivesAndUnions_restrictsCorrectly() {
    assertEquals(numberType, numberType.restrictByNotNullOrUndefined());
    assertTrue(nullType.restrictByNotNullOrUndefined().isEmptyType());
    assertTrue(voidType.restrictByNotNullOrUndefined().isEmptyType());

    JSType nullableNumber = registry.createUnionType(numberType, nullType, voidType);
    assertEquals(numberType, nullableNumber.restrictByNotNullOrUndefined());
  }

  // Tests matchesNumberContext, matchesStringContext, matchesObjectContext predicates
  @Test
  public void testMatchesContexts_primitiveAndSpecialTypes_evaluatesExpectedContexts() {
    assertTrue(numberType.matchesNumberContext());
    assertFalse(numberType.matchesStringContext());
    assertFalse(numberType.matchesObjectContext());

    assertFalse(stringType.matchesNumberContext());
    assertTrue(stringType.matchesStringContext());
    assertFalse(stringType.matchesObjectContext());

    assertFalse(objectType.matchesNumberContext());
    assertFalse(objectType.matchesStringContext());
    assertTrue(objectType.matchesObjectContext());

    assertTrue(allType.matchesNumberContext());
    assertTrue(allType.matchesStringContext());
    assertTrue(allType.matchesObjectContext());

    assertTrue(unknownType.matchesNumberContext());
    assertTrue(unknownType.matchesStringContext());
    assertTrue(unknownType.matchesObjectContext());
  }

  // Tests canCastTo relationship logic
  @Test
  public void testCanCastTo_compatibleAndIncompatiblePairs_returnsExpectedBoolean() {
    assertTrue(numberType.canCastTo(allType));
    assertTrue(numberType.canCastTo(numberType));
    assertTrue(numberType.canCastTo(unknownType));
    assertFalse(numberType.canCastTo(stringType));
  }

  // Tests various is* predicate methods on native types
  @Test
  public void testTypeKindPredicates_nativeTypes_returnsExpectedBooleans() {
    assertTrue(allType.isAllType());
    assertFalse(numberType.isAllType());

    assertTrue(unknownType.isUnknownType());
    assertFalse(numberType.isUnknownType());

    assertTrue(nullType.isNullType());
    assertFalse(numberType.isNullType());

    assertTrue(voidType.isVoidType());
    assertFalse(numberType.isVoidType());

    assertTrue(booleanType.isBooleanValueType());
    assertFalse(numberType.isBooleanValueType());

    assertTrue(stringType.isStringValueType());
    assertFalse(numberType.isStringValueType());

    assertTrue(numberType.isNumberValueType());
    assertFalse(stringType.isNumberValueType());

    assertTrue(objectType.isObject());
    assertFalse(numberType.isObject());
    assertTrue(objectType.isTheObjectType());
    assertFalse(numberType.isTheObjectType());

    assertFalse(numberType.isGlobalThisType());
    assertFalse(numberType.isLoose());
    assertFalse(numberType.canBeCalled());
    assertFalse(numberType.hasDisplayName());
    assertNull(numberType.getDisplayName());
  }

  // Tests toMaybe* downcasting helper methods on various types
  @Test
  public void testToMaybeDowncastMethods_variousTypes_downcastsOrReturnsNull() {
    assertNotNull(objectType.toMaybeObjectType());
    assertNull(numberType.toMaybeObjectType());

    JSType union = registry.createUnionType(numberType, stringType);
    assertNotNull(union.toMaybeUnionType());
    assertNull(numberType.toMaybeUnionType());

    assertNull(numberType.toMaybeRecordType());
    assertNull(numberType.toMaybeEnumElementType());

    FunctionType fnType = registry.createFunctionType(numberType);
    assertNotNull(fnType.toMaybeFunctionType());
    assertTrue(fnType.canBeCalled());
    assertTrue(fnType.isFunctionType());
    assertTrue(fnType.isOrdinaryFunction());
    assertFalse(fnType.isConstructor());
    assertFalse(fnType.isInterface());
    assertFalse(fnType.isNominalConstructor());
    assertFalse(fnType.isNominalType());
    assertFalse(fnType.isInstanceType());
  }

  // Tests getTypesUnderShallowEquality method
  @Test
  public void testGetTypesUnderShallowEquality_identicalAndDisjointTypes_returnsExpectedPairs() {
    JSType.TypePair pairSame = numberType.getTypesUnderShallowEquality(numberType);
    assertEquals(numberType, pairSame.typeA);
    assertEquals(numberType, pairSame.typeB);

    JSType.TypePair pairDiff = numberType.getTypesUnderShallowEquality(stringType);
    assertNull(pairDiff.typeA);
    assertNull(pairDiff.typeB);

    JSType.TypePair pairNullVoid = nullType.getTypesUnderShallowEquality(voidType);
    assertNull(pairNullVoid.typeA);
    assertNull(pairNullVoid.typeB);
  }

  // Tests toAnnotationString on basic types
  @Test
  public void testToAnnotationString_primitiveAndObjectType_returnsExpectedAnnotations() {
    assertEquals("number", numberType.toAnnotationString());
    assertEquals("string", stringType.toAnnotationString());
    assertEquals("boolean", booleanType.toAnnotationString());
    assertEquals("null", nullType.toAnnotationString());
    assertEquals("undefined", voidType.toAnnotationString());
  }

  // Tests filterBySubtype, collapseUnion and findPropertyType methods
  @Test
  public void testFilterBySubtypeAndPropertyLookup_unionAndPrimitiveTypes() {
    JSType union = registry.createUnionType(numberType, stringType);
    assertEquals(numberType, union.filterBySubtype(numberType));
    assertNotNull(union.collapseUnion());

    assertNull(numberType.findPropertyType("prop"));
    assertNull(objectType.findPropertyType("prop"));
  }

  // Tests testForEquality on various primitive combinations
  @Test
  public void testForEquality_primitivePairs_returnsExpectedTernaryValues() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(numberType));
    assertEquals(TernaryValue.FALSE, numberType.testForEquality(stringType));
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(nullType));
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
    assertEquals(TernaryValue.TRUE, voidType.testForEquality(nullType));
  }

  // Tests dereference and autobox on null and void types
  @Test
  public void testAutoboxAndDereference_nullAndVoidTypes_returnsExpectedResults() {
    assertNull(nullType.dereference());
    assertNull(voidType.dereference());

    assertEquals(nullType, nullType.autobox());
    assertEquals(voidType, voidType.autobox());
  }

  // Tests isSubtype with null argument
  @Test
  public void testIsSubtype_nullTargetType_returnsFalse() {
    assertFalse(numberType.isSubtype(null));
  }

  // Tests isEquivalentTo method directly
  @Test
  public void testIsEquivalentTo_matchingAndNonMatchingTypes_returnsExpectedBoolean() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.isEquivalentTo(stringType));
    assertFalse(numberType.isEquivalentTo(null));
  }
}