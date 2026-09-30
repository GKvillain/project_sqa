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
import static com.google.javascript.rhino.jstype.TernaryValue.FALSE;
import static com.google.javascript.rhino.jstype.TernaryValue.TRUE;
import static com.google.javascript.rhino.jstype.TernaryValue.UNKNOWN;
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

public class UnionTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;
  private JSType unknownType;
  private JSType allType;

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
    allType = registry.getNativeType(ALL_TYPE);
  }

  // Tests context matching for number, string, and object contexts
  @Test
  public void testMatchesContext_variousContexts_returnsExpected() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.matchesNumberContext());
    assertTrue(union.matchesStringContext());
    assertTrue(union.matchesObjectContext());

    UnionType voidNullUnion = (UnionType) registry.createUnionType(voidType, nullType);
    assertFalse(voidNullUnion.matchesObjectContext());
    assertFalse(voidNullUnion.matchesNumberContext());
  }

  // Tests canAssignTo with standard types and unknown type
  @Test
  public void testCanAssignTo_subtypesAndUnknown_returnsCorrectBoolean() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.canAssignTo(allType));
    assertFalse(union.canAssignTo(numberType));

    UnionType withUnknown = (UnionType) registry.createUnionType(numberType, unknownType);
    assertTrue(withUnknown.canAssignTo(stringType));
  }

  // Tests canBeCalled method
  @Test
  public void testCanBeCalled_callableAndNonCallable_returnsFalseWhenNotAllCallable() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertFalse(union.canBeCalled());
  }

  // Tests autobox method
  @Test
  public void testAutobox_primitiveUnion_autoboxesAlternates() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    JSType autoboxed = union.autobox();
    assertTrue(autoboxed.isObject());
  }

  // Tests restrictByNotNullOrUndefined
  @Test
  public void testRestrictByNotNullOrUndefined_withNullAndVoid_removesThem() {
    UnionType union = (UnionType) registry.createUnionType(numberType, nullType, voidType);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertTrue(restricted.isEquivalentTo(numberType));
  }

  // Tests testForEquality across alternates
  @Test
  public void testTestForEquality_sameAndDifferentOutcomes_returnsExpectedTernary() {
    UnionType numStrUnion = (UnionType) registry.createUnionType(numberType, stringType);
    assertEquals(FALSE, numStrUnion.testForEquality(nullType));

    UnionType numNullUnion = (UnionType) registry.createUnionType(numberType, nullType);
    assertEquals(UNKNOWN, numNullUnion.testForEquality(nullType));
  }

  // Tests isNullable, isUnknownType, isObject
  @Test
  public void testTypePredicates_predicatesReflectAlternates() {
    UnionType union = (UnionType) registry.createUnionType(numberType, nullType);
    assertTrue(union.isNullable());
    assertFalse(union.isUnknownType());
    assertFalse(union.isObject());

    UnionType objUnion = (UnionType) registry.createUnionType(objectType, objectType);
    assertTrue(objUnion.isObject());
  }

  // Tests isStruct and isDict
  @Test
  public void testIsStructAndIsDict_defaultAlternates_returnsFalse() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertFalse(union.isStruct());
    assertFalse(union.isDict());
  }

  // Tests isSubtype
  @Test
  public void testIsSubtype_variousTypes_returnsExpectedBoolean() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.isSubtype(allType));
    assertTrue(union.isSubtype(unknownType));
    assertTrue(union.isSubtype(union));
    assertFalse(union.isSubtype(numberType));
  }

  // Tests getLeastSupertype with alternate subtype
  @Test
  public void testGetLeastSupertype_subtypeOfAlternate_returnsThis() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    JSType result = union.getLeastSupertype(numberType);
    assertSame(union, result);
  }

  // Tests meet operation with single type and union type
  @Test
  public void testMeet_variousTypes_returnsCorrectIntersection() {
    UnionType union1 = (UnionType) registry.createUnionType(numberType, stringType);
    JSType meetNumber = union1.meet(numberType);
    assertTrue(meetNumber.isEquivalentTo(numberType));

    UnionType union2 = (UnionType) registry.createUnionType(stringType, booleanType);
    JSType meetUnion = union1.meet(union2);
    assertTrue(meetUnion.isEquivalentTo(stringType));

    JSType meetDisjoint = union1.meet(booleanType);
    assertTrue(meetDisjoint.isNoType());
  }

  // Tests checkUnionEquivalenceHelper and contains
  @Test
  public void testCheckUnionEquivalenceHelper_sameAndDifferent_returnsExpected() {
    UnionType u1 = (UnionType) registry.createUnionType(numberType, stringType);
    UnionType u2 = (UnionType) registry.createUnionType(stringType, numberType);
    UnionType u3 = (UnionType) registry.createUnionType(numberType, booleanType);

    assertTrue(u1.checkUnionEquivalenceHelper(u2, false));
    assertFalse(u1.checkUnionEquivalenceHelper(u3, false));
    assertTrue(u1.contains(numberType));
    assertFalse(u1.contains(booleanType));
  }

  // Tests getRestrictedUnion
  @Test
  public void testGetRestrictedUnion_removeAlternate_returnsRemaining() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    JSType restricted = union.getRestrictedUnion(numberType);
    assertTrue(restricted.isEquivalentTo(stringType));
  }

  // Tests toStringHelper format
  @Test
  public void testToStringHelper_validUnion_formatsCorrectly() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertEquals("(number|string)", union.toStringHelper(false));
  }

  // Tests getRestrictedTypeGivenToBooleanOutcome
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_trueAndFalse_filtersCorrectly() {
    UnionType union = (UnionType) registry.createUnionType(numberType, nullType);
    JSType trueRestricted = union.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(trueRestricted.isEquivalentTo(numberType));
    assertFalse(trueRestricted.isNullable());

    JSType falseRestricted = union.getRestrictedTypeGivenToBooleanOutcome(false);
    assertTrue(falseRestricted.isNullable());
  }

  // Tests getPossibleToBooleanOutcomes
  @Test
  public void testGetPossibleToBooleanOutcomes_numberAndString_returnsBoth() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertEquals(BooleanLiteralSet.BOTH, union.getPossibleToBooleanOutcomes());

    UnionType nullUnion = (UnionType) registry.createUnionType(nullType, voidType);
    assertEquals(BooleanLiteralSet.FALSE, nullUnion.getPossibleToBooleanOutcomes());
  }

  // Tests findPropertyType
  @Test
  public void testFindPropertyType_propertyOnAlternate_returnsPropertyType() {
    ObjectType objTypeWithProp = registry.createAnonymousObjectType();
    objTypeWithProp.defineDeclaredProperty("foo", numberType, null);

    UnionType union = (UnionType) registry.createUnionType(objTypeWithProp, nullType);
    JSType propType = union.findPropertyType("foo");
    assertNotNull(propType);
    assertTrue(propType.isEquivalentTo(numberType));

    assertNull(union.findPropertyType("nonExistent"));
  }

  // Tests collapseUnion
  @Test
  public void testCollapseUnion_multiplePrimitivesOrObjects_collapsesProperly() {
    UnionType primitiveUnion = (UnionType) registry.createUnionType(numberType, stringType);
    JSType collapsedPrimitive = primitiveUnion.collapseUnion();
    assertTrue(collapsedPrimitive.isAllType());

    UnionType withUnknown = (UnionType) registry.createUnionType(numberType, unknownType);
    JSType collapsedUnknown = withUnknown.collapseUnion();
    assertTrue(collapsedUnknown.isUnknownType());
  }

  // Tests getTypesUnderEquality and Inequality
  @Test
  public void testGetTypesUnderEqualityAndInequality_validUnion_returnsTypePairs() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    TypePair eqPair = union.getTypesUnderEquality(numberType);
    assertNotNull(eqPair.typeA);
    assertNotNull(eqPair.typeB);

    TypePair ineqPair = union.getTypesUnderInequality(numberType);
    assertNotNull(ineqPair.typeA);
    assertNotNull(ineqPair.typeB);

    TypePair shallowIneqPair = union.getTypesUnderShallowInequality(numberType);
    assertNotNull(shallowIneqPair.typeA);
    assertNotNull(shallowIneqPair.typeB);
  }

  // Tests toMaybeUnionType and visitor
  @Test
  public void testToMaybeUnionTypeAndVisit_returnsUnionInstance() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertSame(union, union.toMaybeUnionType());

    Visitor<String> visitor = new Visitor<String>() {
      @Override
      public String caseNoType() { return null; }
      @Override
      public String caseEnumElementType(EnumElementType type) { return null; }
      @Override
      public String caseAllType() { return null; }
      @Override
      public String caseBooleanType() { return null; }
      @Override
      public String caseNoObjectType() { return null; }
      @Override
      public String caseFunctionType(FunctionType type) { return null; }
      @Override
      public String caseObjectType(ObjectType type) { return null; }
      @Override
      public String caseUnknownType() { return null; }
      @Override
      public String caseNullType() { return null; }
      @Override
      public String caseNamedType(NamedType type) { return null; }
      @Override
      public String caseNumberType() { return null; }
      @Override
      public String caseStringType() { return null; }
      @Override
      public String caseVoidType() { return null; }
      @Override
      public String caseUnionType(UnionType type) { return "union"; }
      @Override
      public String caseTemplatizedType(TemplatizedType type) { return null; }
      @Override
      public String caseTemplateType(TemplateType templateType) { return null; }
    };
    assertEquals("union", union.visit(visitor));
  }

  // Tests isUnionType and getAlternates
  @Test
  public void testIsUnionTypeAndGetAlternates() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());
    assertEquals(2, union.getAlternates().size());
    assertTrue(union.getAlternates().contains(numberType));
    assertTrue(union.getAlternates().contains(stringType));
  }

  // Tests canTestForShallowEqualityWith and canTestForEqualityWith
  @Test
  public void testEqualityCheckingCapabilities() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    assertTrue(union.canTestForEqualityWith(numberType));
    assertTrue(union.canTestForShallowEqualityWith(numberType));
  }

  // Tests getTypesUnderShallowEquality
  @Test
  public void testGetTypesUnderShallowEquality() {
    UnionType union = (UnionType) registry.createUnionType(numberType, stringType);
    TypePair shallowEqPair = union.getTypesUnderShallowEquality(numberType);
    assertNotNull(shallowEqPair.typeA);
    assertNotNull(shallowEqPair.typeB);
  }
}