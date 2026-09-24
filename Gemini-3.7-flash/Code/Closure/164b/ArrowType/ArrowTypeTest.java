package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class ArrowTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType allType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    booleanType = registry.getNativeType(BOOLEAN_TYPE);
    unknownType = registry.getNativeType(UNKNOWN_TYPE);
    allType = registry.getNativeType(ALL_TYPE);
  }

  // Tests constructor initialization with null parameters and null return type
  @Test
  public void testConstructor_nullParamsAndReturn_defaultsToUnknown() {
    ArrowType arrow = new ArrowType(registry, null, null);
    assertNotNull(arrow.parameters);
    assertEquals(unknownType, arrow.returnType);
    assertFalse(arrow.returnTypeInferred);
  }

  // Tests constructor initialization with returnTypeInferred flag set to true
  @Test
  public void testConstructor_withInferredReturn_setsFlagCorrectly() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, stringType, true);
    assertTrue(arrow.returnTypeInferred);
    assertEquals(stringType, arrow.returnType);
  }

  // Tests isSubtype when comparing with a non-ArrowType
  @Test
  public void testIsSubtype_nonArrowType_returnsFalse() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, numberType);
    assertFalse(arrow.isSubtype(numberType));
    assertFalse(arrow.isSubtype(stringType));
  }

  // Tests isSubtype when return type is not a subtype (covariance)
  @Test
  public void testIsSubtype_incompatibleReturnType_returnsFalse() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params, stringType);
    ArrowType arrow2 = new ArrowType(registry, params, numberType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype when parameter types are contravariant
  @Test
  public void testIsSubtype_contravariantParameters_returnsTrue() {
    // arrow1: (ALL_TYPE) -> NUMBER
    // arrow2: (NUMBER_TYPE) -> NUMBER
    // arrow1 <: arrow2 because NUMBER_TYPE <: ALL_TYPE (contravariant)
    Node params1 = registry.createParameters(allType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, numberType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertTrue(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype when parameter types are not contravariant
  @Test
  public void testIsSubtype_nonContravariantParameters_returnsFalse() {
    // arrow1: (NUMBER_TYPE) -> NUMBER
    // arrow2: (ALL_TYPE) -> NUMBER
    // arrow1 is not subtype of arrow2 because ALL_TYPE is not subtype of NUMBER_TYPE
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(allType);
    ArrowType arrow1 = new ArrowType(registry, params1, numberType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype when extra required parameters exist in this vs that
  @Test
  public void testIsSubtype_extraRequiredParameterInThis_returnsFalse() {
    // arrow1: (number, number) -> boolean
    // arrow2: (number) -> boolean
    // arrow1 requires two params, arrow2 only provides one, so arrow1 is not a subtype of arrow2
    Node params1 = registry.createParameters(numberType, numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype with var_args handling on both sides
  @Test
  public void testIsSubtype_varArgsBoth_returnsTrue() {
    Node params1 = registry.createParametersWithVarArgs(allType);
    Node params2 = registry.createParametersWithVarArgs(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertTrue(arrow1.isSubtype(arrow2));
  }

  // Tests hasEqualParameters with matching parameter types
  @Test
  public void testHasEqualParameters_identicalParameters_returnsTrue() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertTrue(arrow1.hasEqualParameters(arrow2));
  }

  // Tests hasEqualParameters with different parameter types
  @Test
  public void testHasEqualParameters_differentParameterTypes_returnsFalse() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, booleanType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertFalse(arrow1.hasEqualParameters(arrow2));
  }

  // Tests hasEqualParameters with different parameter counts
  @Test
  public void testHasEqualParameters_differentParameterLengths_returnsFalse() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertFalse(arrow1.hasEqualParameters(arrow2));
  }

  // Tests isEquivalentTo with equivalent ArrowTypes
  @Test
  public void testIsEquivalentTo_identicalArrowType_returnsTrue() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertTrue(arrow1.isEquivalentTo(arrow2));
  }

  // Tests isEquivalentTo when return types differ
  @Test
  public void testIsEquivalentTo_differentReturnTypes_returnsFalse() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params1, stringType);
    assertFalse(arrow1.isEquivalentTo(arrow2));
  }

  // Tests isEquivalentTo when compared with a non-ArrowType
  @Test
  public void testIsEquivalentTo_nonArrowType_returnsFalse() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    assertFalse(arrow.isEquivalentTo(numberType));
  }

  // Tests hashCode consistency for equal ArrowTypes
  @Test
  public void testHashCode_equalArrowTypes_sameHashCode() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    assertEquals(arrow1.hashCode(), arrow2.hashCode());
  }

  // Tests hasUnknownParamsOrReturn when parameters or return contain unknown type
  @Test
  public void testHasUnknownParamsOrReturn_withUnknown_returnsTrue() {
    Node paramsWithUnknown = registry.createParameters(unknownType);
    ArrowType arrow1 = new ArrowType(registry, paramsWithUnknown, numberType);
    assertTrue(arrow1.hasUnknownParamsOrReturn());

    Node concreteParams = registry.createParameters(numberType);
    ArrowType arrow2 = new ArrowType(registry, concreteParams, unknownType);
    assertTrue(arrow2.hasUnknownParamsOrReturn());
  }

  // Tests hasUnknownParamsOrReturn when all types are known
  @Test
  public void testHasUnknownParamsOrReturn_withKnownTypes_returnsFalse() {
    Node params = registry.createParameters(numberType, stringType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    assertFalse(arrow.hasUnknownParamsOrReturn());
  }

  // Tests getPossibleToBooleanOutcomes returns TRUE
  @Test
  public void testGetPossibleToBooleanOutcomes_returnsTrueLiteralSet() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  // Tests unsupported operations throw UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetLeastSupertype_throwsUnsupportedOperationException() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    arrow.getLeastSupertype(numberType);
  }

  // Tests getGreatestSubtype throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetGreatestSubtype_throwsUnsupportedOperationException() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    arrow.getGreatestSubtype(numberType);
  }

  // Tests testForEquality throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testTestForEquality_throwsUnsupportedOperationException() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    arrow.testForEquality(numberType);
  }

  // Tests visit throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testVisit_throwsUnsupportedOperationException() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, booleanType);
    arrow.visit(null);
  }
}