package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType allType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;
  private JSType noType;
  private JSType noObjectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
  }

  // Tests isEquivalentTo on identical, equal, and different types
  @Test
  public void testIsEquivalentTo_basicTypes_returnsExpected() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.isEquivalentTo(stringType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
  }

  // Tests differsFrom handling with unknown types and concrete types
  @Test
  public void testDiffersFrom_variousTypes_returnsExpected() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  // Tests subtyping relation for native primitive and bottom/top types
  @Test
  public void testIsSubtype_primitiveAndTopBottom_returnsCorrectSubtyping() {
    assertTrue(numberType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(noType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));
    assertFalse(allType.isSubtype(numberType));
  }

  // Tests greatest subtype (meet) for distinct primitive types
  @Test
  public void testGetGreatestSubtype_distinctPrimitives_returnsNoType() {
    JSType meet = numberType.getGreatestSubtype(stringType);
    assertTrue(meet.isNoType());
  }

  // Tests greatest subtype with unknown and all types
  @Test
  public void testGetGreatestSubtype_unknownAndAll_returnsExpected() {
    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));
    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(numberType, numberType.getGreatestSubtype(numberType));
  }

  // Tests least supertype (join) of primitive types
  @Test
  public void testGetLeastSupertype_primitiveTypes_createsUnion() {
    JSType join = numberType.getLeastSupertype(stringType);
    assertTrue(join.isUnionType());
    assertSame(numberType, numberType.getLeastSupertype(numberType));
  }

  // Tests testForEquality between incompatible primitives
  @Test
  public void testTestForEquality_differentPrimitives_returnsFalseOrUnknown() {
    assertEquals(TernaryValue.FALSE, voidType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, unknownType.testForEquality(numberType));
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
  }

  // Tests getTypesUnderEquality for equality narrowing
  @Test
  public void testGetTypesUnderEquality_differentTypes_returnsNarrowedPair() {
    JSType.TypePair pairFalse = voidType.getTypesUnderEquality(numberType);
    assertNull(pairFalse.typeA);
    assertNull(pairFalse.typeB);

    JSType.TypePair pairUnknown = numberType.getTypesUnderEquality(numberType);
    assertSame(numberType, pairUnknown.typeA);
    assertSame(numberType, pairUnknown.typeB);
  }

  // Tests getTypesUnderInequality for inequality narrowing
  @Test
  public void testGetTypesUnderInequality_identicalTypes_returnsExpected() {
    JSType.TypePair pair = numberType.getTypesUnderInequality(numberType);
    assertSame(numberType, pair.typeA);
    assertSame(numberType, pair.typeB);
  }

  // Tests getTypesUnderShallowEquality and shallow inequality
  @Test
  public void testGetTypesUnderShallowEqualityAndInequality_nullAndVoid_returnsExpected() {
    JSType.TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertSame(numberType, shallowEq.typeA);
    assertSame(numberType, shallowEq.typeB);

    JSType.TypePair shallowIneqNull = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(shallowIneqNull.typeA);
    assertNull(shallowIneqNull.typeB);

    JSType.TypePair shallowIneqDiff = numberType.getTypesUnderShallowInequality(stringType);
    assertSame(numberType, shallowIneqDiff.typeA);
    assertSame(stringType, shallowIneqDiff.typeB);
  }

  // Tests boolean outcome restrictions
  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome_variousTypes_restrictedCorrectly() {
    JSType restrictedTrue = objectType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertSame(objectType, restrictedTrue);

    JSType restrictedFalse = objectType.getRestrictedTypeGivenToBooleanOutcome(false);
    assertTrue(restrictedFalse.isNoType());

    JSType restrictedUnknown = unknownType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(restrictedUnknown.isCheckedUnknownType());
  }

  // Tests autobox and dereference functionality
  @Test
  public void testAutoboxAndDereference_primitiveAndObject_returnsExpected() {
    JSType autoboxedNumber = numberType.autobox();
    assertTrue(autoboxedNumber.isObject());
    assertNotNull(numberType.dereference());

    JSType derefObj = objectType.dereference();
    assertSame(objectType, derefObj);
  }

  // Tests canAssignTo behavior
  @Test
  public void testCanAssignTo_subtypesAndIncompatible_returnsCorrectBoolean() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  // Tests resolve and clearResolved lifecycle
  @Test
  public void testResolveAndClearResolved_primitiveType_resolvesCorrectly() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(new SimpleErrorReporter(), null);
    assertSame(numberType, resolved);
    assertTrue(numberType.isResolved());

    numberType.clearResolved();
    assertFalse(numberType.isResolved());
  }

  // Tests static ALPHA comparator ordering
  @Test
  public void testAlphaComparator_differentTypes_sortsDeterministically() {
    int comparison = JSType.ALPHA.compare(numberType, stringType);
    assertTrue(comparison != 0);
    assertEquals(0, JSType.ALPHA.compare(numberType, numberType));
  }

  // Tests downcast helper methods
  @Test
  public void testToMaybeDowncasts_primitiveType_returnsNull() {
    assertNull(numberType.toMaybeUnionType());
    assertNull(numberType.toMaybeFunctionType());
    assertNull(numberType.toMaybeEnumElementType());
    assertNull(numberType.toMaybeEnumType());
    assertNull(numberType.toMaybeParameterizedType());
    assertNull(numberType.toMaybeTemplateType());
    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeTemplateType(null));
  }

  // Tests equals, hashCode, and toString representations
  @Test
  public void testEqualsAndHashCode_basicTypes_consistentWithContract() {
    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals("notAJSType"));
    assertEquals(numberType.hashCode(), numberType.hashCode());
    assertNotNull(numberType.toString());
    assertNotNull(numberType.toAnnotationString());
    assertNotNull(numberType.toDebugHashCodeString());
  }

  @Test
  public void testTypePredicates_nativeTypes_returnsExpectedValues() {
    assertTrue(numberType.isNumber());
    assertFalse(numberType.isString());
    assertFalse(numberType.isBoolean());
    assertTrue(stringType.isString());
    assertTrue(booleanType.isBoolean());
    assertTrue(nullType.isNullType());
    assertTrue(voidType.isVoidType());
    assertTrue(unknownType.isUnknownType());
    assertTrue(allType.isAllType());
    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(objectType.isObject());
    assertFalse(numberType.isNullable());
    assertTrue(nullType.isNullable());
    assertTrue(voidType.isNullable());
    assertFalse(numberType.isRecordType());
    assertFalse(numberType.isTemplateType());
    assertFalse(numberType.isNominalType());
    assertFalse(numberType.isInstanceType());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isOrdinaryFunction());
    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isNominalConstructor());
    assertFalse(numberType.isFunctionPrototypeType());
    assertFalse(numberType.isGlobalThisType());
  }

  @Test
  public void testFilterBySubtype_nativeTypes_filtersAccurately() {
    assertSame(numberType, numberType.filterBySubtype(numberType));
    assertSame(noType, numberType.filterBySubtype(stringType));
    assertSame(numberType, numberType.filterByNotSubtype(stringType));
    assertSame(noType, numberType.filterByNotSubtype(numberType));
  }

  @Test
  public void testRestrictByNotNullOrUndefined_primitivesAndUnion_returnsExpected() {
    assertSame(numberType, numberType.restrictByNotNullOrUndefined());
    assertSame(noType, nullType.restrictByNotNullOrUndefined());
    assertSame(noType, voidType.restrictByNotNullOrUndefined());

    JSType unionWithNull = registry.createUnionType(numberType, nullType);
    assertSame(numberType, unionWithNull.restrictByNotNullOrUndefined());
  }

  @Test
  public void testCanTestForEquality_nativeTypes_checksProperly() {
    assertTrue(numberType.canTestForEqualityWith(numberType));
    assertTrue(numberType.canTestForEqualityWith(unknownType));
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
  }

  @Test
  public void testVisitorPattern_nativeType_invokedCorrectly() {
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enumElement"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "boolean"; }
      @Override public String caseNoObjectType() { return "noObject"; }
      @Override public String caseFunctionType(FunctionType type) { return "function"; }
      @Override public String caseObjectType(ObjectType type) { return "object"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    };
    assertEquals("number", numberType.visit(visitor));
    assertEquals("string", stringType.visit(visitor));
    assertEquals("boolean", booleanType.visit(visitor));
    assertEquals("unknown", unknownType.visit(visitor));
    assertEquals("all", allType.visit(visitor));
  }

  @Test
  public void testJSDocInfoAndLooseProperties_basicOperations_behaveCorrectly() {
    assertNull(numberType.getJSDocInfo());
    JSDocInfo info = new JSDocInfo();
    numberType.setJSDocInfo(info);
    assertSame(info, numberType.getJSDocInfo());

    assertFalse(numberType.isLoose());
    assertNull(numberType.findPropertyType("foo"));
    assertFalse(numberType.canBeCalled());
    assertFalse(numberType.isTheObjectType());
    assertFalse(numberType.isDict());
    assertFalse(numberType.isStruct());
  }

  @Test
  public void testCollapseUnionAndDisplayName_nativeTypes_returnsExpected() {
    assertSame(numberType, numberType.collapseUnion());
    assertFalse(numberType.hasDisplayName());
    assertNull(numberType.getDisplayName());
    assertNotNull(numberType.getTemplateTypeMap());
  }
}