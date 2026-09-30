package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArrowTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType voidType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    booleanType = registry.getNativeType(BOOLEAN_TYPE);
    unknownType = registry.getNativeType(UNKNOWN_TYPE);
    voidType = registry.getNativeType(VOID_TYPE);
  }

  // Tests constructor handling of null parameters and null return type
  @Test
  public void testConstructor_nullParametersAndReturnType_defaultsToUnknown() {
    ArrowType arrow = new ArrowType(registry, null, null);
    assertNotNull(arrow.parameters);
    assertEquals(unknownType, arrow.returnType);
    assertFalse(arrow.returnTypeInferred);
  }

  // Tests constructor with explicit returnTypeInferred flag
  @Test
  public void testConstructor_withInferredReturnType_setsFlag() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, numberType, true);
    assertTrue(arrow.returnTypeInferred);
    assertEquals(numberType, arrow.returnType);
  }

  // Tests isSubtype against non-ArrowType returns false
  @Test
  public void testIsSubtype_nonArrowType_returnsFalse() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    assertFalse(arrow.isSubtype(numberType));
  }

  // Tests isSubtype with covariant return types
  @Test
  public void testIsSubtype_covariantReturnType_returnsCorrectSubtypeResult() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params, numberType);
    ArrowType arrow2 = new ArrowType(registry, params, unknownType);

    assertTrue(arrow1.isSubtype(arrow2));
    assertFalse(arrow2.isSubtype(arrow1));
  }

  // Tests isSubtype with contravariant parameter types
  @Test
  public void testIsSubtype_contravariantParameters_returnsCorrectSubtypeResult() {
    Node params1 = registry.createParameters(unknownType);
    Node params2 = registry.createParameters(numberType);

    ArrowType arrowGeneral = new ArrowType(registry, params1, voidType);
    ArrowType arrowSpecific = new ArrowType(registry, params2, voidType);

    // thatParam <: thisParam (contravariant)
    assertTrue(arrowGeneral.isSubtype(arrowSpecific));
    assertFalse(arrowSpecific.isSubtype(arrowGeneral));
  }

  // Tests isSubtype with incompatible parameter types
  @Test
  public void testIsSubtype_incompatibleParamTypes_returnsFalse() {
    Node paramsNum = registry.createParameters(numberType);
    Node paramsStr = registry.createParameters(stringType);

    ArrowType arrowNum = new ArrowType(registry, paramsNum, voidType);
    ArrowType arrowStr = new ArrowType(registry, paramsStr, voidType);

    assertFalse(arrowNum.isSubtype(arrowStr));
  }

  // Tests isSubtype with var_args on both functions
  @Test
  public void testIsSubtype_bothVarArgs_returnsTrue() {
    Node varArgs1 = registry.createParametersWithVarArgs(numberType);
    Node varArgs2 = registry.createParametersWithVarArgs(numberType);

    ArrowType arrow1 = new ArrowType(registry, varArgs1, voidType);
    ArrowType arrow2 = new ArrowType(registry, varArgs2, voidType);

    assertTrue(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype when function has more required parameters than target
  @Test
  public void testIsSubtype_fewerParamsVsMoreParams_returnsFalseWhenMissingRequiredParam() {
    Node twoParams = registry.createParameters(numberType, stringType);
    Node oneParam = registry.createParameters(numberType);

    ArrowType arrowTwo = new ArrowType(registry, twoParams, voidType);
    ArrowType arrowOne = new ArrowType(registry, oneParam, voidType);

    // A function requiring two arguments cannot substitute for a function expecting one
    assertFalse(arrowTwo.isSubtype(arrowOne));
  }

  // Tests isEquivalentTo with equal ArrowTypes
  @Test
  public void testIsEquivalentTo_identicalTypes_returnsTrue() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, stringType);

    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);

    assertTrue(arrow1.isEquivalentTo(arrow2));
    assertTrue(arrow2.isEquivalentTo(arrow1));
    assertEquals(arrow1.hashCode(), arrow2.hashCode());
  }

  // Tests isEquivalentTo with different return types and non-ArrowType
  @Test
  public void testIsEquivalentTo_differentReturnTypeOrType_returnsFalse() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params, numberType);
    ArrowType arrow2 = new ArrowType(registry, params, stringType);

    assertFalse(arrow1.isEquivalentTo(arrow2));
    assertFalse(arrow1.isEquivalentTo(numberType));
  }

  // Tests hasEqualParameters with unequal parameter counts and types
  @Test
  public void testHasEqualParameters_differentParameters_returnsFalse() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType, stringType);
    Node params3 = registry.createParameters(stringType);

    ArrowType arrow1 = new ArrowType(registry, params1, voidType);
    ArrowType arrow2 = new ArrowType(registry, params2, voidType);
    ArrowType arrow3 = new ArrowType(registry, params3, voidType);

    assertFalse(arrow1.hasEqualParameters(arrow2));
    assertFalse(arrow1.hasEqualParameters(arrow3));
  }

  // Tests hashCode consistency with returnTypeInferred flag
  @Test
  public void testHashCode_inferredFlagChangesHashCode() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params, numberType, false);
    ArrowType arrow2 = new ArrowType(registry, params, numberType, true);

    assertEquals(arrow1.hashCode() + 1, arrow2.hashCode());
  }

  // Tests hasUnknownParamsOrReturn when types are known vs unknown
  @Test
  public void testHasUnknownParamsOrReturn_knownAndUnknownTypes_returnsCorrectStatus() {
    Node knownParams = registry.createParameters(numberType);
    ArrowType knownArrow = new ArrowType(registry, knownParams, numberType);
    assertFalse(knownArrow.hasUnknownParamsOrReturn());

    Node unknownParams = registry.createParameters(unknownType);
    ArrowType unknownParamArrow = new ArrowType(registry, unknownParams, numberType);
    assertTrue(unknownParamArrow.hasUnknownParamsOrReturn());

    ArrowType unknownReturnArrow = new ArrowType(registry, knownParams, unknownType);
    assertTrue(unknownReturnArrow.hasUnknownParamsOrReturn());
  }

  // Tests getPossibleToBooleanOutcomes returns BooleanLiteralSet.TRUE
  @Test
  public void testGetPossibleToBooleanOutcomes_alwaysReturnsTrue() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  // Tests resolveInternal resolves return type and parameter types
  @Test
  public void testResolveInternal_resolvesTypes() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, numberType);
    ErrorReporter reporter = new SimpleErrorReporter();

    ArrowType resolved = (ArrowType) arrow.resolveInternal(reporter, null);
    assertNotNull(resolved);
    assertEquals(numberType, resolved.returnType);
  }

  // Tests getLeastSupertype throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetLeastSupertype_throwsException() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    arrow.getLeastSupertype(numberType);
  }

  // Tests getGreatestSubtype throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetGreatestSubtype_throwsException() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    arrow.getGreatestSubtype(numberType);
  }

  // Tests testForEquality throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testTestForEquality_throwsException() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    arrow.testForEquality(numberType);
  }

  // Tests visit throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testVisit_throwsException() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    arrow.visit(null);
  }
}