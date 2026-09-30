package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
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
  private JSType unknownType;
  private JSType allType;
  private JSType noType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    unknownType = registry.getNativeType(UNKNOWN_TYPE);
    allType = registry.getNativeType(ALL_TYPE);
    noType = registry.getNativeType(NO_TYPE);
  }

  // Tests constructor with null parameters and return type fallback to UNKNOWN_TYPE
  @Test
  public void testConstructor_nullArguments_initializesDefaults() {
    ArrowType arrowType = new ArrowType(registry, null, null);
    assertNotNull(arrowType.parameters);
    assertEquals(unknownType, arrowType.returnType);
    assertFalse(arrowType.returnTypeInferred);
  }

  // Tests subtyping with non-ArrowType object
  @Test
  public void testIsSubtype_nonArrowType_returnsFalse() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    assertFalse(arrowType.isSubtype(numberType));
  }

  // Tests covariant return type check in isSubtype
  @Test
  public void testIsSubtype_incompatibleReturnType_returnsFalse() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(), numberType);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(), stringType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests contravariant parameter subtyping
  @Test
  public void testIsSubtype_contravariantParameters_success() {
    // f: (all) -> number, g: (number) -> number => f is subtype of g
    ArrowType superParamArrow = new ArrowType(registry, registry.createParameters(allType), numberType);
    ArrowType subParamArrow = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertTrue(superParamArrow.isSubtype(subParamArrow));
    assertFalse(subParamArrow.isSubtype(superParamArrow));
  }

  // Tests subtyping when that has optional parameter but this has required
  @Test
  public void testIsSubtype_missingRequiredParamInTarget_returnsFalse() {
    Node reqParam = registry.createParameters(numberType);
    Node optParam = registry.createOptionalParameters(numberType);
    ArrowType reqArrow = new ArrowType(registry, reqParam, numberType);
    ArrowType optArrow = new ArrowType(registry, optParam, numberType);
    assertFalse(reqArrow.isSubtype(optArrow));
  }

  // Tests subtyping with var_args and top function check
  @Test
  public void testIsSubtype_varArgsTopFunction_returnsTrue() {
    Node reqParam = registry.createParameters(numberType);
    Node varArgsUnknown = registry.createParametersWithVarArgs(unknownType);
    ArrowType reqArrow = new ArrowType(registry, reqParam, numberType);
    ArrowType topArrow = new ArrowType(registry, varArgsUnknown, allType);
    assertTrue(reqArrow.isSubtype(topArrow));
  }

  // Tests parameter count mismatch in isSubtype
  @Test
  public void testIsSubtype_extraRequiredParameters_returnsFalse() {
    Node twoParams = registry.createParameters(numberType, stringType);
    Node oneParam = registry.createParameters(numberType);
    ArrowType twoParamArrow = new ArrowType(registry, twoParams, numberType);
    ArrowType oneParamArrow = new ArrowType(registry, oneParam, numberType);
    assertFalse(twoParamArrow.isSubtype(oneParamArrow));
  }

  // Tests hasEqualParameters with equivalent and different parameter lists
  @Test
  public void testHasEqualParameters_matchingAndMismatched_returnsCorrectValue() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow3 = new ArrowType(registry, registry.createParameters(stringType), numberType);
    ArrowType arrow4 = new ArrowType(registry, registry.createParameters(numberType, stringType), numberType);

    assertTrue(arrow1.hasEqualParameters(arrow2, false));
    assertFalse(arrow1.hasEqualParameters(arrow3, false));
    assertFalse(arrow1.hasEqualParameters(arrow4, false));
  }

  // Tests checkArrowEquivalenceHelper with matching and different return types
  @Test
  public void testCheckArrowEquivalenceHelper_checksBothReturnAndParameters() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrowDiffReturn = new ArrowType(registry, registry.createParameters(numberType), stringType);

    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, false));
    assertFalse(arrow1.checkArrowEquivalenceHelper(arrowDiffReturn, false));
  }

  // Tests hashCode consistency with equivalent and differing instances
  @Test
  public void testHashCode_equalObjects_returnsSameHashCode() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(numberType), numberType, true);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(numberType), numberType, true);
    ArrowType arrow3 = new ArrowType(registry, registry.createParameters(numberType), numberType, false);

    assertEquals(arrow1.hashCode(), arrow2.hashCode());
    assertTrue(arrow1.hashCode() != arrow3.hashCode());
  }

  // Tests hasUnknownParamsOrReturn when unknown type is present in return type
  @Test
  public void testHasUnknownParamsOrReturn_withUnknownReturn_returnsTrue() {
    ArrowType arrowUnknownReturn = new ArrowType(registry, registry.createParameters(numberType), unknownType);
    assertTrue(arrowUnknownReturn.hasUnknownParamsOrReturn());

    ArrowType arrowKnown = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertFalse(arrowKnown.hasUnknownParamsOrReturn());
  }

  // Tests hasUnknownParamsOrReturn when unknown type is present in parameters
  @Test
  public void testHasUnknownParamsOrReturn_withUnknownParam_returnsTrue() {
    ArrowType arrowUnknownParam = new ArrowType(registry, registry.createParameters(unknownType), numberType);
    assertTrue(arrowUnknownParam.hasUnknownParamsOrReturn());
  }

  // Tests getPossibleToBooleanOutcomes returns BooleanLiteralSet.TRUE
  @Test
  public void testGetPossibleToBooleanOutcomes_returnsTrue() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  // Tests toStringHelper formatting
  @Test
  public void testToStringHelper_returnsArrowTypeString() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    assertEquals("[ArrowType]", arrow.toStringHelper(false));
    assertEquals("[ArrowType]", arrow.toStringHelper(true));
  }

  // Tests hasAnyTemplateInternal returns false when no templated types are present
  @Test
  public void testHasAnyTemplateInternal_nonTemplatedTypes_returnsFalse() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertFalse(arrow.hasAnyTemplateInternal());
  }

  // Tests resolveInternal resolving return type and parameter types
  @Test
  public void testResolveInternal_resolvesParametersAndReturnType() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, numberType);
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = arrow.resolveInternal(reporter, null);
    assertEquals(arrow, resolved);
    assertEquals(numberType, arrow.returnType);
  }

  // Tests unsupported operation exceptions
  @Test(expected = UnsupportedOperationException.class)
  public void testGetLeastSupertype_throwsUnsupportedOperationException() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    arrow.getLeastSupertype(arrow);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetGreatestSubtype_throwsUnsupportedOperationException() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    arrow.getGreatestSubtype(arrow);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTestForEquality_throwsUnsupportedOperationException() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    arrow.testForEquality(arrow);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testVisit_throwsUnsupportedOperationException() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), numberType);
    arrow.visit(null);
  }

  // Tests subtyping with optional parameters on this side vs required on that side
  @Test
  public void testIsSubtype_optionalParamOnThis_isSubtypeOfRequired() {
    Node optParam = registry.createOptionalParameters(numberType);
    Node reqParam = registry.createParameters(numberType);
    ArrowType optArrow = new ArrowType(registry, optParam, numberType);
    ArrowType reqArrow = new ArrowType(registry, reqParam, numberType);
    assertTrue(optArrow.isSubtype(reqArrow));
  }

  // Tests subtyping with both having var_args parameters
  @Test
  public void testIsSubtype_bothVarArgs_contravariantCheck() {
    Node varArgsAll = registry.createParametersWithVarArgs(allType);
    Node varArgsNum = registry.createParametersWithVarArgs(numberType);
    ArrowType arrowVarArgsAll = new ArrowType(registry, varArgsAll, numberType);
    ArrowType arrowVarArgsNum = new ArrowType(registry, varArgsNum, numberType);
    assertTrue(arrowVarArgsAll.isSubtype(arrowVarArgsNum));
    assertFalse(arrowVarArgsNum.isSubtype(arrowVarArgsAll));
  }

  // Tests subtyping when that side has remaining optional parameters
  @Test
  public void testIsSubtype_thatHasRemainingOptionalParams_returnsTrue() {
    Node noParams = registry.createParameters();
    Node optParam = registry.createOptionalParameters(numberType);
    ArrowType arrowNoParams = new ArrowType(registry, noParams, numberType);
    ArrowType arrowOptParam = new ArrowType(registry, optParam, numberType);
    assertTrue(arrowNoParams.isSubtype(arrowOptParam));
  }

  // Tests subtyping when that side has remaining required parameters
  @Test
  public void testIsSubtype_thatHasRemainingRequiredParams_returnsFalse() {
    Node noParams = registry.createParameters();
    Node reqParam = registry.createParameters(numberType);
    ArrowType arrowNoParams = new ArrowType(registry, noParams, numberType);
    ArrowType arrowReqParam = new ArrowType(registry, reqParam, numberType);
    assertFalse(arrowNoParams.isSubtype(arrowReqParam));
  }

  // Tests hasEqualParameters when optionality differs
  @Test
  public void testHasEqualParameters_optionalVsRequired_returnsFalse() {
    Node reqParam = registry.createParameters(numberType);
    Node optParam = registry.createOptionalParameters(numberType);
    ArrowType reqArrow = new ArrowType(registry, reqParam, numberType);
    ArrowType optArrow = new ArrowType(registry, optParam, numberType);
    assertFalse(reqArrow.hasEqualParameters(optArrow, EquivalenceMethod.IDENTITY));
  }

  // Tests hasEqualParameters when varArgs differs
  @Test
  public void testHasEqualParameters_varArgsVsRequired_returnsFalse() {
    Node reqParam = registry.createParameters(numberType);
    Node varParam = registry.createParametersWithVarArgs(numberType);
    ArrowType reqArrow = new ArrowType(registry, reqParam, numberType);
    ArrowType varArrow = new ArrowType(registry, varParam, numberType);
    assertFalse(reqArrow.hasEqualParameters(varArrow, EquivalenceMethod.IDENTITY));
  }

  // Tests hasEqualParameters when parameter counts differ
  @Test
  public void testHasEqualParameters_differingParamCounts_returnsFalse() {
    ArrowType noParamArrow = new ArrowType(registry, registry.createParameters(), numberType);
    ArrowType oneParamArrow = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertFalse(noParamArrow.hasEqualParameters(oneParamArrow, EquivalenceMethod.IDENTITY));
    assertFalse(oneParamArrow.hasEqualParameters(noParamArrow, EquivalenceMethod.IDENTITY));
  }

  // Tests checkArrowEquivalenceHelper with EquivalenceMethod
  @Test
  public void testCheckArrowEquivalenceHelper_withEquivalenceMethod() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow3 = new ArrowType(registry, registry.createParameters(numberType), stringType);
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.IDENTITY));
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.INVARIANT));
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.DATA_FLOW));
    assertFalse(arrow1.checkArrowEquivalenceHelper(arrow3, EquivalenceMethod.IDENTITY));
  }

  // Tests hasAnyTemplateInternal when returnType has a template type
  @Test
  public void testHasAnyTemplateInternal_templatedReturnType_returnsTrue() {
    TemplateType templateType = registry.createTemplateType("T");
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), templateType);
    assertTrue(arrow.hasAnyTemplateInternal());
  }

  // Tests hasAnyTemplateInternal when a parameter has a template type
  @Test
  public void testHasAnyTemplateInternal_templatedParameter_returnsTrue() {
    TemplateType templateType = registry.createTemplateType("T");
    ArrowType arrow = new ArrowType(registry, registry.createParameters(templateType), numberType);
    assertTrue(arrow.hasAnyTemplateInternal());
  }

  // Tests hashCode with differing return types
  @Test
  public void testHashCode_differentReturnType_returnsDifferentHashCode() {
    ArrowType arrow1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType arrow2 = new ArrowType(registry, registry.createParameters(numberType), stringType);
    assertTrue(arrow1.hashCode() != arrow2.hashCode());
  }

  // Tests resolveInternal resolving unresolvable/named types in parameters and return type
  @Test
  public void testResolveInternal_withUnresolvedNamedTypes() {
    NamedType namedReturnType = new NamedType(registry, "SomeType", "source.js", 1, 1);
    NamedType namedParamType = new NamedType(registry, "ParamType", "source.js", 1, 1);
    Node params = registry.createParameters(namedParamType);
    ArrowType arrow = new ArrowType(registry, params, namedReturnType);
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    StaticScope<JSType> scope = registry.getScope();
    ArrowType resolved = (ArrowType) arrow.resolveInternal(reporter, scope);
    assertNotNull(resolved);
    assertNotNull(resolved.returnType);
    assertNotNull(resolved.parameters);
  }
}