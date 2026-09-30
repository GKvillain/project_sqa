package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType allType;
  private JSType unknownType;
  private JSType noType;
  private JSType noObjectType;
  private JSType objectType;

  @Before
  public void setUp() {
    ErrorReporter reporter = new ErrorReporter() {
      public void warning(String message, String sourceName, int line, int lineOffset) {}
      public void error(String message, String sourceName, int line, int lineOffset) {}
    };
    registry = new JSTypeRegistry(reporter);

    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests getTypesUnderInequality between void and void (Defects4J Closure-146)
  @Test
  public void testGetTypesUnderInequality_voidTypeWithVoidType_returnsNullPair() {
    JSType.TypePair pair = voidType.getTypesUnderInequality(voidType);
    assertNotNull(pair);
    assertNull(pair.typeA);
    assertNull(pair.typeB);
  }

  // Tests getTypesUnderInequality between null and null
  @Test
  public void testGetTypesUnderInequality_nullTypeWithNullType_returnsNullPair() {
    JSType.TypePair pair = nullType.getTypesUnderInequality(nullType);
    assertNotNull(pair);
    assertNull(pair.typeA);
    assertNull(pair.typeB);
  }

  // Tests getTypesUnderInequality between different types
  @Test
  public void testGetTypesUnderInequality_numberAndString_returnsSameTypes() {
    JSType.TypePair pair = numberType.getTypesUnderInequality(stringType);
    assertNotNull(pair);
    assertEquals(numberType, pair.typeA);
    assertEquals(stringType, pair.typeB);
  }

  // Tests getTypesUnderEquality for same types
  @Test
  public void testGetTypesUnderEquality_numberAndNumber_returnsTypes() {
    JSType.TypePair pair = numberType.getTypesUnderEquality(numberType);
    assertNotNull(pair);
    assertEquals(numberType, pair.typeA);
    assertEquals(numberType, pair.typeB);
  }

  // Tests getTypesUnderShallowEquality and getTypesUnderShallowInequality
  @Test
  public void testGetTypesUnderShallowEqualityAndInequality_nullAndVoid() {
    JSType.TypePair shallowEq = nullType.getTypesUnderShallowEquality(voidType);
    assertNotNull(shallowEq);
    assertEquals(noType, shallowEq.typeA);
    assertEquals(noType, shallowEq.typeB);

    JSType.TypePair shallowIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNotNull(shallowIneq);
    assertNull(shallowIneq.typeA);
    assertNull(shallowIneq.typeB);

    JSType.TypePair shallowIneqVoid = voidType.getTypesUnderShallowInequality(voidType);
    assertNotNull(shallowIneqVoid);
    assertNull(shallowIneqVoid.typeA);
    assertNull(shallowIneqVoid.typeB);
  }

  // Tests differsFrom method with unknown type and concrete types
  @Test
  public void testDiffersFrom_variousTypes_correctComparisons() {
    assertTrue(unknownType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(unknownType));
    assertFalse(unknownType.differsFrom(unknownType));
    assertTrue(numberType.differsFrom(stringType));
    assertFalse(numberType.differsFrom(numberType));
  }

  // Tests isEquivalent, isEquivalentTo, equals and hashCode
  @Test
  public void testEquivalenceAndEquality_sameAndDifferentTypes() {
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));

    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals(new Object()));
    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
  }

  // Tests isSubtype helper static and instance methods
  @Test
  public void testIsSubtype_latticeRelationships() {
    assertTrue(JSType.isSubtype(numberType, unknownType));
    assertTrue(JSType.isSubtype(numberType, allType));
    assertTrue(JSType.isSubtype(numberType, numberType));
    assertFalse(JSType.isSubtype(numberType, stringType));
    assertTrue(noType.isSubtype(numberType));
  }

  // Tests getLeastSupertype computation
  @Test
  public void testGetLeastSupertype_differentCombinations() {
    assertEquals(allType, numberType.getLeastSupertype(allType));
    assertEquals(numberType, numberType.getLeastSupertype(noType));
    assertEquals(allType, JSType.getLeastSupertype(numberType, allType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
  }

  // Tests getGreatestSubtype computation
  @Test
  public void testGetGreatestSubtype_differentCombinations() {
    assertEquals(numberType, numberType.getGreatestSubtype(allType));
    assertEquals(noType, numberType.getGreatestSubtype(noType));
    assertEquals(unknownType, numberType.getGreatestSubtype(unknownType));
    assertEquals(unknownType, unknownType.getGreatestSubtype(numberType));
    assertEquals(unknownType, unknownType.getGreatestSubtype(unknownType));
    assertEquals(numberType, numberType.getGreatestSubtype(numberType));
    assertEquals(noType, numberType.getGreatestSubtype(stringType));
    assertEquals(objectType, JSType.getGreatestSubtype(objectType, objectType));
  }

  // Tests dereference and autoboxing behavior
  @Test
  public void testDereferenceAndAutoboxesTo() {
    ObjectType numObj = numberType.dereference();
    assertNotNull(numObj);
    assertTrue(numObj.isObject());

    JSType autobox = numberType.autoboxesTo();
    assertNotNull(autobox);
    assertTrue(autobox.isObject());

    assertNull(nullType.dereference());
    assertNull(voidType.dereference());
    assertNull(numberType.unboxesTo());
  }

  // Tests context matching predicates
  @Test
  public void testContextMatching_numericAndContextMethods() {
    assertTrue(numberType.matchesNumberContext());
    assertFalse(stringType.matchesNumberContext());
    assertTrue(stringType.matchesStringContext());
    assertFalse(numberType.matchesStringContext());
    assertTrue(objectType.matchesObjectContext());
    assertFalse(nullType.matchesObjectContext());
  }

  // Tests boolean restriction logic
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    assertEquals(nullType, nullType.getRestrictedTypeGivenToBooleanOutcome(false));
    assertEquals(noType, nullType.getRestrictedTypeGivenToBooleanOutcome(true));

    assertEquals(objectType, objectType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertEquals(noType, objectType.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  // Tests resolution lifecycle methods
  @Test
  public void testResolveLifecycle() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(null, null);
    assertEquals(numberType, resolved);
    assertTrue(numberType.isResolved());

    // Calling resolve again should return cached result
    assertEquals(resolved, numberType.resolve(null, null));

    JSType safeResolved = JSType.safeResolve(null, null, null);
    assertNull(safeResolved);

    JSType forced = stringType.forceResolve(null, null);
    assertEquals(stringType, forced);
    assertTrue(stringType.isResolved());
  }

  // Tests debug string and default property inspection
  @Test
  public void testToDebugHashCodeStringAndFindPropertyType() {
    String debugStr = numberType.toDebugHashCodeString();
    assertTrue(debugStr.startsWith("{"));
    assertTrue(debugStr.endsWith("}"));

    assertNull(nullType.findPropertyType("toString"));
    assertNotNull(numberType.findPropertyType("toString"));
    assertNull(numberType.getJSDocInfo());
  }

  // Tests default boolean predicate flags
  @Test
  public void testTypeClassificationPredicates() {
    assertTrue(numberType.isNumberValueType());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isNumberObjectType());

    assertTrue(stringType.isStringValueType());
    assertTrue(stringType.isString());
    assertFalse(stringType.isStringObjectType());

    assertTrue(booleanType.isBooleanValueType());
    assertTrue(booleanType.isBoolean());
    assertFalse(booleanType.isBooleanObjectType());

    assertTrue(nullType.isNullType());
    assertTrue(voidType.isVoidType());
    assertTrue(allType.isAllType());
    assertTrue(unknownType.isUnknownType());

    assertFalse(numberType.isString());
    assertFalse(numberType.isNoType());
    assertFalse(numberType.isNoObjectType());
    assertFalse(numberType.isEmptyType());
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());

    assertFalse(numberType.isNullable());
    assertTrue(nullType.isNullable());
    assertTrue(voidType.isNullable());

    assertFalse(numberType.canBeCalled());
    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isOrdinaryFunction());
    assertFalse(numberType.isFunctionType());
    assertFalse(numberType.isNominalType());
    assertFalse(numberType.isInstanceType());
    assertFalse(numberType.isUnionType());
    assertFalse(numberType.isEnumElementType());
    assertFalse(numberType.isRecordType());
    assertFalse(numberType.isTemplateType());
    assertFalse(numberType.isDateType());
    assertFalse(numberType.isRegexpType());
    assertFalse(numberType.isArrayType());
    assertFalse(numberType.isObject());
    assertTrue(objectType.isObject());
  }

  // Tests canAssignTo and equality test capabilities
  @Test
  public void testCanAssignToAndCanTestForEquality() {
    assertTrue(numberType.canAssignTo(numberType));
    assertFalse(numberType.canAssignTo(stringType));

    assertTrue(numberType.canTestForEqualityWith(numberType));
    assertTrue(numberType.canTestForEqualityWith(stringType));

    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(numberType.canTestForShallowEqualityWith(stringType));
  }

  // Tests displayName and toString
  @Test
  public void testDisplayNameAndToString() {
    assertNotNull(numberType.toString());
    assertEquals(numberType.toString(), numberType.getDisplayName());
  }
}