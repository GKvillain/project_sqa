package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.jstype.JSType.TypePair;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;
  private JSType allType;
  private JSType unknownType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
  }

  // Tests isString and isNumber classifications
  @Test
  public void testIsStringAndIsNumber_primitiveAndObjectTypes_returnsExpectedResults() {
    assertTrue(stringType.isString());
    assertFalse(stringType.isNumber());
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isString());
    assertFalse(booleanType.isString());
    assertFalse(booleanType.isNumber());

    JSType stringObjType = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    JSType numberObjType = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(stringObjType.isString());
    assertTrue(numberObjType.isNumber());
  }

  // Tests isEmptyType method with bottom types
  @Test
  public void testIsEmptyType_bottomTypes_returnsTrue() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    assertTrue(registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE).isEmptyType());
    assertFalse(numberType.isEmptyType());
    assertFalse(objectType.isEmptyType());
  }

  // Tests isNullable check
  @Test
  public void testIsNullable_nullAndOtherTypes_returnsCorrectBoolean() {
    assertTrue(nullType.isNullable());
    assertFalse(voidType.isNullable());
    assertFalse(numberType.isNullable());
    assertFalse(objectType.isNullable());

    JSType unionWithNull = registry.createUnionType(numberType, nullType);
    assertTrue(unionWithNull.isNullable());
  }

  // Tests subtype relationship for primitive and top/bottom types
  @Test
  public void testIsSubtype_latticeRelationships_returnsCorrectSubtypeResult() {
    assertTrue(numberType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(noType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));
    assertFalse(allType.isSubtype(numberType));
  }

  // Tests differsFrom method regarding unknown types
  @Test
  public void testDiffersFrom_knownAndUnknownTypes_handlesUnknownProperly() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(numberType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(numberType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  // Tests getLeastSupertype computation
  @Test
  public void testGetLeastSupertype_variousPairs_returnsLatticeJoin() {
    assertSame(numberType, numberType.getLeastSupertype(numberType));
    assertSame(allType, numberType.getLeastSupertype(allType));
    assertSame(numberType, noType.getLeastSupertype(numberType));

    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
    assertTrue(numberType.isSubtype(union));
    assertTrue(stringType.isSubtype(union));
  }

  // Tests getGreatestSubtype computation
  @Test
  public void testGetGreatestSubtype_subtypesAndDisjointTypes_returnsLatticeMeet() {
    assertSame(numberType, numberType.getGreatestSubtype(numberType));
    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(noType, numberType.getGreatestSubtype(stringType));
    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));
    assertSame(noObjectType, objectType.getGreatestSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE).getGreatestSubtype(registry.getNativeType(JSTypeNative.DATE_TYPE))));
  }

  // Tests testForEquality between various types
  @Test
  public void testTestForEquality_primitiveAndSpecialTypes_returnsExpectedTernary() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));
  }

  // Tests shallow equality check capabilities
  @Test
  public void testCanTestForShallowEqualityWith_disjointAndCommonTypes_evaluatesProperly() {
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(numberType.canTestForShallowEqualityWith(allType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
    assertFalse(numberType.canTestForShallowEqualityWith(booleanType));
    assertTrue(noType.canTestForShallowEqualityWith(numberType));
  }

  // Tests getTypesUnderEquality and getTypesUnderInequality
  @Test
  public void testGetTypesUnderEqualityAndInequality_compatibleTypes_returnsValidPairs() {
    TypePair eqPair = numberType.getTypesUnderEquality(numberType);
    assertSame(numberType, eqPair.typeA);
    assertSame(numberType, eqPair.typeB);

    TypePair ineqPair = numberType.getTypesUnderInequality(numberType);
    assertSame(numberType, ineqPair.typeA);
    assertSame(numberType, ineqPair.typeB);
  }

  // Tests getTypesUnderShallowInequality for null and void types
  @Test
  public void testGetTypesUnderShallowInequality_nullAndVoidTypes_returnsNullComponents() {
    TypePair nullPair = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(nullPair.typeA);
    assertNull(nullPair.typeB);

    TypePair voidPair = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(voidPair.typeA);
    assertNull(voidPair.typeB);

    TypePair numPair = numberType.getTypesUnderShallowInequality(numberType);
    assertSame(numberType, numPair.typeA);
    assertSame(numberType, numPair.typeB);
  }

  // Tests getRestrictedTypeGivenToBooleanOutcome
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_booleanOutcomes_restrictsProperly() {
    JSType restrictedTrue = objectType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertSame(objectType, restrictedTrue);

    JSType restrictedFalse = objectType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertSame(noType, restrictedFalse);

    JSType nullFalse = nullType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertSame(nullType, nullFalse);

    JSType nullTrue = nullType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertSame(noType, nullTrue);
  }

  // Tests static downcast helper methods with null inputs
  @Test
  public void testToMaybeDowncasts_nullInputs_returnsNull() {
    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeTemplateType(null));
    assertNull(numberType.toMaybeFunctionType());
    assertNull(numberType.toMaybeParameterizedType());
    assertNull(numberType.toMaybeTemplateType());
    assertNull(numberType.toMaybeUnionType());
    assertNull(numberType.toMaybeEnumType());
    assertNull(numberType.toMaybeEnumElementType());
  }

  // Tests resolve and resolve lifecycle
  @Test
  public void testResolveAndClearResolved_primitiveType_maintainsResolvedState() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(null, null);
    assertSame(numberType, resolved);
    assertTrue(numberType.isResolved());

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    JSType forced = numberType.forceResolve(null, null);
    assertSame(numberType, forced);
    assertTrue(numberType.isResolved());
    numberType.clearResolved();
  }

  // Tests static isEquivalent method
  @Test
  public void testIsEquivalent_nullAndSameTypes_handlesNullCorrectly() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(new Object()));
  }

  // Tests filterNoResolvedType helper behavior
  @Test
  public void testFilterNoResolvedType_singleAndUnionTypes_filtersCorrectly() {
    assertSame(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertSame(numberType, JSType.filterNoResolvedType(numberType));

    JSType union = registry.createUnionType(numberType, noResolvedType);
    JSType filtered = JSType.filterNoResolvedType(union);
    assertSame(numberType, filtered);
  }

  // Tests context matching predicates
  @Test
  public void testContextMatching_primitiveTypes_returnsExpectedBooleans() {
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertTrue(numberType.matchesNumberContext());
    assertFalse(stringType.matchesNumberContext());
    assertFalse(numberType.matchesStringContext());
    assertFalse(numberType.matchesObjectContext());
  }

  // Tests autoboxing, dereference, and property lookup
  @Test
  public void testAutoboxAndDereference_primitiveNumber_dereferencesToNumberObject() {
    JSType autoboxed = numberType.autobox();
    assertNotNull(autoboxed);
    assertTrue(autoboxed.isObject());

    ObjectType dereferenced = numberType.dereference();
    assertNotNull(dereferenced);
    assertSame(autoboxed, dereferenced);

    assertNull(numberType.findPropertyType("nonExistentProperty"));
  }

  // Tests toString, toAnnotationString, toDebugHashCodeString, and ALPHA comparator
  @Test
  public void testStringRepresentationsAndComparator_validTypes_generatesCorrectStrings() {
    assertEquals("number", numberType.toString());
    assertEquals("number", numberType.toAnnotationString());
    assertEquals("{" + numberType.hashCode() + "}", numberType.toDebugHashCodeString());

    int comp = JSType.ALPHA.compare(numberType, stringType);
    assertTrue(comp < 0);
    assertEquals(0, JSType.ALPHA.compare(numberType, numberType));
  }

  // Tests setValidator predicate application
  @Test
  public void testSetValidator_predicate_evaluatesPredicate() {
    Predicate<JSType> acceptAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    };
    Predicate<JSType> rejectAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return false;
      }
    };

    assertTrue(numberType.setValidator(acceptAll));
    assertFalse(numberType.setValidator(rejectAll));
  }
}