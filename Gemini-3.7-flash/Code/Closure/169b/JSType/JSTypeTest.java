package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType unknownType;
  private JSType allType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests basic type identity predicates
  @Test
  public void testTypePredicates_nativeTypes_returnsExpectedBooleans() {
    assertTrue(numberType.isNumberValueType());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isString());
    assertFalse(numberType.isObject());

    assertTrue(stringType.isStringValueType());
    assertTrue(stringType.isString());
    assertFalse(stringType.isNumber());

    assertTrue(booleanType.isBooleanValueType());
    assertTrue(nullType.isNullType());
    assertTrue(nullType.isNullable());
    assertTrue(voidType.isVoidType());
    assertTrue(unknownType.isUnknownType());
    assertTrue(allType.isAllType());
    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noResolvedType.isNoResolvedType());
    assertTrue(objectType.isObject());
  }

  // Tests isEmptyType method for bottom types and normal types
  @Test
  public void testIsEmptyType_bottomAndNormalTypes_returnsCorrectStatus() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    assertTrue(registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE).isEmptyType());
    assertFalse(numberType.isEmptyType());
    assertFalse(objectType.isEmptyType());
  }

  // Tests isEquivalentTo for identical, equal, and distinct types
  @Test
  public void testIsEquivalentTo_variousTypes_evaluatesEquivalence() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, stringType));
    assertFalse(numberType.isEquivalentTo(stringType));
    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(new Object()));
  }

  // Tests differsFrom method regarding unknown types and standard types
  @Test
  public void testDiffersFrom_typesWithUnknown_evaluatesCorrectly() {
    assertFalse(unknownType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(numberType));
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
  }

  // Tests isSubtype for lattice relationships
  @Test
  public void testIsSubtype_latticeRelations_evaluatesHierarchy() {
    assertTrue(noType.isSubtype(numberType));
    assertTrue(noType.isSubtype(objectType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(numberType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));

    JSType unionNumStr = registry.createUnionType(numberType, stringType);
    assertTrue(numberType.isSubtype(unionNumStr));
    assertTrue(stringType.isSubtype(unionNumStr));
    assertFalse(unionNumStr.isSubtype(numberType));
  }

  // Tests checkEquivalenceHelper for union types
  @Test
  public void testCheckEquivalence_unionTypes_detectsEquivalence() {
    JSType union1 = registry.createUnionType(numberType, stringType);
    JSType union2 = registry.createUnionType(stringType, numberType);
    JSType union3 = registry.createUnionType(numberType, booleanType);

    assertTrue(union1.isEquivalentTo(union2));
    assertFalse(union1.isEquivalentTo(union3));
    assertTrue(union1.isInvariant(union2));
  }

  // Tests checkEquivalenceHelper and subtype with RecordType
  @Test
  public void testRecordType_equivalenceAndMeet_handlesRecordStructures() {
    RecordTypeBuilder rtb1 = new RecordTypeBuilder(registry);
    rtb1.addProperty("a", numberType, null);
    RecordType rec1 = rtb1.build();

    RecordTypeBuilder rtb2 = new RecordTypeBuilder(registry);
    rtb2.addProperty("a", numberType, null);
    RecordType rec2 = rtb2.build();

    RecordTypeBuilder rtb3 = new RecordTypeBuilder(registry);
    rtb3.addProperty("a", stringType, null);
    RecordType rec3 = rtb3.build();

    assertTrue(rec1.isEquivalentTo(rec2));
    assertFalse(rec1.isEquivalentTo(rec3));

    JSType meet = rec1.getGreatestSubtype(rec2);
    assertTrue(meet.isEquivalentTo(rec1));
  }

  // Tests parameterized type equivalence and conversion
  @Test
  public void testParameterizedType_equivalenceAndDowncast_evaluatesCorrectly() {
    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    ParameterizedType paramArrayNum = registry.createParameterizedType(arrayType, numberType);
    ParameterizedType paramArrayNum2 = registry.createParameterizedType(arrayType, numberType);
    ParameterizedType paramArrayStr = registry.createParameterizedType(arrayType, stringType);

    assertTrue(paramArrayNum.isParameterizedType());
    assertNotNull(paramArrayNum.toMaybeParameterizedType());
    assertNotNull(JSType.toMaybeParameterizedType(paramArrayNum));
    assertNull(JSType.toMaybeParameterizedType(numberType));

    assertTrue(paramArrayNum.isEquivalentTo(paramArrayNum2));
    assertFalse(paramArrayNum.isEquivalentTo(paramArrayStr));
    assertFalse(paramArrayNum.isEquivalentTo(arrayType));
  }

  // Tests getLeastSupertype computation
  @Test
  public void testGetLeastSupertype_variousPairs_returnsLUB() {
    assertEquals(numberType, numberType.getLeastSupertype(numberType));
    assertEquals(allType, numberType.getLeastSupertype(allType));
    assertEquals(unknownType, numberType.getLeastSupertype(unknownType));

    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
    assertTrue(union.toMaybeUnionType().getAlternates().contains(numberType));
    assertTrue(union.toMaybeUnionType().getAlternates().contains(stringType));

    JSType unionWithUnion = union.getLeastSupertype(booleanType);
    assertTrue(unionWithUnion.isUnionType());
    assertTrue(unionWithUnion.toMaybeUnionType().getAlternates().contains(booleanType));
  }

  // Tests getGreatestSubtype computation
  @Test
  public void testGetGreatestSubtype_variousPairs_returnsGLB() {
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    assertEquals(noType, numberType.getGreatestSubtype(stringType));
    assertEquals(noObjectType, objectType.getGreatestSubtype(registry.getNativeType(JSTypeNative.REGEXP_TYPE)));
    assertEquals(numberType, numberType.getGreatestSubtype(allType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertEquals(numberType, union.getGreatestSubtype(numberType));
    assertEquals(stringType, union.getGreatestSubtype(stringType));
    assertEquals(noType, union.getGreatestSubtype(booleanType));
  }

  // Tests filterNoResolvedType helper method behavior
  @Test
  public void testFilterNoResolvedType_unionAndResolvedTypes_filtersProperly() {
    assertEquals(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertEquals(numberType, JSType.filterNoResolvedType(numberType));

    JSType unionWithNoResolved = registry.createUnionType(numberType, noResolvedType);
    JSType filtered = JSType.filterNoResolvedType(unionWithNoResolved);
    assertEquals(numberType, filtered);
  }

  // Tests testForEquality for primitive, empty, and function types
  @Test
  public void testTestForEquality_variousPairs_returnsTernaryValues() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));

    FunctionType fnType = registry.createFunctionType(numberType);
    assertEquals(TernaryValue.FALSE, fnType.testForEquality(nullType));
    assertEquals(TernaryValue.FALSE, fnType.testForEquality(voidType));
    assertEquals(TernaryValue.UNKNOWN, fnType.testForEquality(objectType));
  }

  // Tests canTestForShallowEqualityWith
  @Test
  public void testCanTestForShallowEqualityWith_differentTypes_evaluatesCorrectly() {
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(noType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));

    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);
    assertTrue(fn1.canTestForShallowEqualityWith(fn2));
  }

  // Tests getTypesUnderEquality and getTypesUnderInequality
  @Test
  public void testGetTypesUnderEqualityAndInequality_pairEvaluations() {
    JSType.TypePair pairEq = numberType.getTypesUnderEquality(stringType);
    assertNull(pairEq.typeA);
    assertNull(pairEq.typeB);

    JSType.TypePair pairIneq = numberType.getTypesUnderInequality(stringType);
    assertEquals(numberType, pairIneq.typeA);
    assertEquals(stringType, pairIneq.typeB);

    JSType.TypePair shallowIneqNull = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(shallowIneqNull.typeA);
    assertNull(shallowIneqNull.typeB);

    JSType.TypePair shallowIneqDiff = numberType.getTypesUnderShallowInequality(stringType);
    assertEquals(numberType, shallowIneqDiff.typeA);
    assertEquals(stringType, shallowIneqDiff.typeB);
  }

  // Tests getRestrictedTypeGivenToBooleanOutcome
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_variousOutcomes() {
    JSType restrictedUnknown = unknownType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(restrictedUnknown.isCheckedUnknownType());

    JSType restrictedObjTrue = objectType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertEquals(objectType, restrictedObjTrue);

    JSType restrictedObjFalse = objectType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertEquals(noType, restrictedObjFalse);
  }

  // Tests autoboxing and dereferencing
  @Test
  public void testAutoboxAndDereference_scalarsAndObjects() {
    ObjectType boxedNum = numberType.autoboxesTo() == null ? null : (ObjectType) numberType.autoboxesTo();
    assertNotNull(boxedNum);
    assertEquals(boxedNum, numberType.autobox());
    assertEquals(boxedNum, numberType.dereference());

    ObjectType derefObj = objectType.dereference();
    assertEquals(objectType, derefObj);
  }

  // Tests context matching predicates
  @Test
  public void testContextMatching_scalarTypes_matchesExpectedContexts() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(numberType.matchesStringContext());
    assertFalse(numberType.matchesObjectContext());

    assertTrue(stringType.matchesStringContext());
    assertFalse(stringType.matchesNumberContext());

    assertTrue(objectType.matchesObjectContext());
  }

  // Tests string and debug representations
  @Test
  public void testToStringAndDebug_variousTypes_generatesCorrectRepresentation() {
    assertEquals("number", numberType.toString());
    assertEquals("number", numberType.toAnnotationString());
    assertNotNull(numberType.toDebugHashCodeString());
    assertTrue(numberType.toDebugHashCodeString().startsWith("{"));
    assertTrue(numberType.toDebugHashCodeString().endsWith("}"));
    assertTrue(JSType.ALPHA.compare(numberType, stringType) < 0);
  }

  // Tests resolve and clearResolved lifecycle
  @Test
  public void testResolveLifecycle_typeResolution_resolvesAndClearsState() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(new SimpleErrorReporter(), null);
    assertEquals(numberType, resolved);
    assertTrue(numberType.isResolved());

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    JSType safe = JSType.safeResolve(null, new SimpleErrorReporter(), null);
    assertNull(safe);
  }

  // Tests nominal constructor predicate
  @Test
  public void testIsNominalConstructor_nativeAndStructuralFunctions() {
    FunctionType objCtor = registry.getNativeFunctionType(JSTypeNative.OBJECT_FUNCTION_TYPE);
    assertTrue(objCtor.isNominalConstructor());

    FunctionType anonFn = registry.createFunctionType(numberType);
    assertFalse(anonFn.isNominalConstructor());
    assertFalse(numberType.isNominalConstructor());
  }

  // Tests restrictByNotNullOrUndefined method
  @Test
  public void testRestrictByNotNullOrUndefined_unionAndPrimitives_restrictsCorrectly() {
    JSType union = registry.createUnionType(numberType, nullType, voidType);
    assertEquals(numberType, union.restrictByNotNullOrUndefined());

    assertEquals(numberType, numberType.restrictByNotNullOrUndefined());
    assertTrue(nullType.restrictByNotNullOrUndefined().isEmptyType());
    assertTrue(voidType.restrictByNotNullOrUndefined().isEmptyType());
    assertEquals(allType, allType.restrictByNotNullOrUndefined());
  }

  // Tests canBeCalled and canCastTo methods
  @Test
  public void testCanBeCalledAndCanCastTo_callableAndCastingTypes() {
    FunctionType fn = registry.createFunctionType(numberType);
    assertTrue(fn.canBeCalled());
    assertFalse(numberType.canBeCalled());
    assertTrue(unknownType.canBeCalled());
    assertTrue(allType.canBeCalled());

    assertTrue(numberType.canCastTo(stringType));
    assertTrue(numberType.canCastTo(numberType));
    assertTrue(objectType.canCastTo(registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE)));
  }

  // Tests enum and element type queries and conversions
  @Test
  public void testEnumTypeAndEnumElementType_predicatesAndDowncasts() {
    EnumType enumType = registry.createEnumType("MyEnum", null, numberType);
    EnumElementType elemType = enumType.getElementsType();

    assertTrue(enumType.isEnumType());
    assertNotNull(enumType.toMaybeEnumType());
    assertNull(numberType.toMaybeEnumType());

    assertTrue(elemType.isEnumElementType());
    assertNotNull(elemType.toMaybeEnumElementType());
    assertNull(numberType.toMaybeEnumElementType());
    assertEquals(enumType, elemType.getEnumType());
    assertEquals(numberType, elemType.getPrimitiveType());
  }

  // Tests downcasting helper methods (toMaybe...)
  @Test
  public void testToMaybeDowncasts_differentTypeClasses() {
    assertNotNull(objectType.toMaybeObjectType());
    assertNull(numberType.toMaybeObjectType());

    FunctionType fn = registry.createFunctionType(numberType);
    assertNotNull(fn.toMaybeFunctionType());
    assertNull(numberType.toMaybeFunctionType());

    JSType union = registry.createUnionType(numberType, stringType);
    assertNotNull(union.toMaybeUnionType());
    assertNull(numberType.toMaybeUnionType());

    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("prop", numberType, null);
    RecordType rec = rtb.build();
    assertTrue(rec.isRecordType());
    assertNotNull(rec.toMaybeRecordType());
    assertNull(numberType.toMaybeRecordType());
  }

  // Tests shallow equality type pair methods
  @Test
  public void testGetTypesUnderShallowEquality_identicalAndDisjointTypes() {
    JSType.TypePair samePair = numberType.getTypesUnderShallowEquality(numberType);
    assertEquals(numberType, samePair.typeA);
    assertEquals(numberType, samePair.typeB);

    JSType.TypePair diffPair = numberType.getTypesUnderShallowEquality(stringType);
    assertNull(diffPair.typeA);
    assertNull(diffPair.typeB);
  }

  // Tests null and void equality semantics
  @Test
  public void testTestForEquality_nullAndVoidPairs() {
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(nullType));
    assertEquals(TernaryValue.TRUE, voidType.testForEquality(voidType));
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
    assertEquals(TernaryValue.TRUE, voidType.testForEquality(nullType));
    assertEquals(TernaryValue.FALSE, nullType.testForEquality(numberType));
    assertEquals(TernaryValue.FALSE, voidType.testForEquality(numberType));
  }

  // Tests collapseUnion and property lookup defaults
  @Test
  public void testCollapseUnionAndFindPropertyType_defaultBehaviors() {
    assertEquals(numberType, numberType.collapseUnion());
    JSType unionSame = registry.createUnionType(numberType, numberType);
    assertEquals(numberType, unionSame.collapseUnion());

    assertNull(numberType.findPropertyType("nonExistent"));
    assertTrue(objectType.isTheObjectType());
    assertFalse(numberType.isTheObjectType());
  }
}