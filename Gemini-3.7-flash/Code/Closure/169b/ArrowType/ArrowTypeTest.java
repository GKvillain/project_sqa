package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
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

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
  }

  // Tests constructor initialization with null parameters and return type
  @Test
  public void testConstructor_nullParametersAndReturn_initializesDefaults() {
    ArrowType arrowType = new ArrowType(registry, null, null);
    assertNotNull(arrowType.parameters);
    assertEquals(unknownType, arrowType.returnType);
    assertFalse(arrowType.returnTypeInferred);
  }

  // Tests constructor initialization with explicit returnTypeInferred flag
  @Test
  public void testConstructor_withInferredFlag_setsFieldCorrectly() {
    Node params = registry.createParameters(numberType);
    ArrowType arrowType = new ArrowType(registry, params, stringType, true);
    assertTrue(arrowType.returnTypeInferred);
    assertEquals(stringType, arrowType.returnType);
  }

  // Tests isSubtype when comparing against a non-ArrowType instance
  @Test
  public void testIsSubtype_nonArrowType_returnsFalse() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    assertFalse(arrowType.isSubtype(numberType));
  }

  // Tests isSubtype when return type is not a subtype (covariant check failure)
  @Test
  public void testIsSubtype_incompatibleReturnType_returnsFalse() {
    ArrowType arrow1 = new ArrowType(registry, null, stringType);
    ArrowType arrow2 = new ArrowType(registry, null, numberType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype with identical signatures returning true
  @Test
  public void testIsSubtype_identicalSignatures_returnsTrue() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType);
    ArrowType arrow2 = new ArrowType(registry, params2, stringType);
    assertTrue(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype contravariant parameter type check
  @Test
  public void testIsSubtype_contravariantParameters_checksSubtyping() {
    Node paramsSub = registry.createParameters(allType);
    Node paramsSuper = registry.createParameters(numberType);
    ArrowType sub = new ArrowType(registry, paramsSub, stringType);
    ArrowType sup = new ArrowType(registry, paramsSuper, stringType);
    assertTrue(sub.isSubtype(sup));
    assertFalse(sup.isSubtype(sub));
  }

  // Tests isSubtype when expected parameter is missing in supertype
  @Test
  public void testIsSubtype_missingRequiredParameter_returnsFalse() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, numberType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  // Tests isSubtype when supertype has varargs unknown top function
  @Test
  public void testIsSubtype_topFunctionVarArgs_returnsTrue() {
    Node paramsSub = registry.createParameters(numberType);
    Node paramsTop = registry.createParametersWithVarArgs(unknownType);
    ArrowType sub = new ArrowType(registry, paramsSub, unknownType);
    ArrowType top = new ArrowType(registry, paramsTop, unknownType);
    assertTrue(sub.isSubtype(top));
  }

  // Tests hasEqualParameters with equivalent parameter lists
  @Test
  public void testHasEqualParameters_matchingParams_returnsTrue() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, numberType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertTrue(arrow1.hasEqualParameters(arrow2, false));
  }

  // Tests hasEqualParameters with different parameter counts
  @Test
  public void testHasEqualParameters_differentLength_returnsFalse() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, numberType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertFalse(arrow1.hasEqualParameters(arrow2, false));
  }

  // Tests checkArrowEquivalenceHelper with equivalent return and parameter types
  @Test
  public void testCheckArrowEquivalenceHelper_equivalentTypes_returnsTrue() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType);
    ArrowType arrow2 = new ArrowType(registry, params2, stringType);
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, false));
  }

  // Tests checkArrowEquivalenceHelper with different return types
  @Test
  public void testCheckArrowEquivalenceHelper_differentReturnType_returnsFalse() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType);
    ArrowType arrow2 = new ArrowType(registry, params2, numberType);
    assertFalse(arrow1.checkArrowEquivalenceHelper(arrow2, false));
  }

  // Tests hashCode consistency and field sensitivity
  @Test
  public void testHashCode_equalObjects_produceSameHashCode() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType, false);
    ArrowType arrow2 = new ArrowType(registry, params2, stringType, false);
    assertEquals(arrow1.hashCode(), arrow2.hashCode());

    ArrowType arrow3 = new ArrowType(registry, params1, stringType, true);
    assertEquals(arrow1.hashCode() + 1, arrow3.hashCode());
  }

  // Tests getLeastSupertype throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetLeastSupertype_always_throwsException() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    arrowType.getLeastSupertype(numberType);
  }

  // Tests getGreatestSubtype throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testGetGreatestSubtype_always_throwsException() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    arrowType.getGreatestSubtype(numberType);
  }

  // Tests testForEquality throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testTestForEquality_always_throwsException() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    arrowType.testForEquality(numberType);
  }

  // Tests visit throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testVisit_always_throwsException() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    arrowType.visit(null);
  }

  // Tests getPossibleToBooleanOutcomes returns BooleanLiteralSet.TRUE
  @Test
  public void testGetPossibleToBooleanOutcomes_always_returnsTrue() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    assertEquals(BooleanLiteralSet.TRUE, arrowType.getPossibleToBooleanOutcomes());
  }

  // Tests hasUnknownParamsOrReturn when types contain unknown
  @Test
  public void testHasUnknownParamsOrReturn_withUnknown_returnsTrue() {
    ArrowType arrowWithUnknownReturn = new ArrowType(registry, registry.createParameters(numberType), unknownType);
    assertTrue(arrowWithUnknownReturn.hasUnknownParamsOrReturn());

    ArrowType arrowWithUnknownParam = new ArrowType(registry, registry.createParameters(unknownType), numberType);
    assertTrue(arrowWithUnknownParam.hasUnknownParamsOrReturn());
  }

  // Tests hasUnknownParamsOrReturn when types contain no unknown
  @Test
  public void testHasUnknownParamsOrReturn_withoutUnknown_returnsFalse() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(numberType), stringType);
    assertFalse(arrow.hasUnknownParamsOrReturn());
  }

  // Tests toStringHelper returning constant string
  @Test
  public void testToStringHelper_anyCall_returnsArrowTypeTag() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    assertEquals("[ArrowType]", arrowType.toStringHelper(false));
    assertEquals("[ArrowType]", arrowType.toStringHelper(true));
  }

  // Tests hasAnyTemplateInternal when no templates exist
  @Test
  public void testHasAnyTemplateInternal_nonTemplated_returnsFalse() {
    ArrowType arrowType = new ArrowType(registry, registry.createParameters(numberType), stringType);
    assertFalse(arrowType.hasAnyTemplateInternal());
  }

  // Tests isArrowType and toMaybeArrowType
  @Test
  public void testIsArrowTypeAndToMaybeArrowType() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    assertTrue(arrowType.isArrowType());
    assertSame(arrowType, arrowType.toMaybeArrowType());
  }

  // Tests visit with RelationshipVisitor throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testVisit_relationshipVisitor_throwsException() {
    ArrowType arrowType = new ArrowType(registry, null, numberType);
    arrowType.visit(null, numberType);
  }

  // Tests resolveInternal resolves returnType and parameters
  @Test
  public void testResolveInternal_resolvesTypes() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    StaticScope<JSType> scope = null;
    NamedType unresolvedReturn = new NamedType(registry, "NamedReturn", "source", 1, 1);
    NamedType unresolvedParam = new NamedType(registry, "NamedParam", "source", 1, 1);

    registry.declareType("NamedReturn", numberType);
    registry.declareType("NamedParam", stringType);

    Node params = registry.createParameters(unresolvedParam);
    ArrowType arrowType = new ArrowType(registry, params, unresolvedReturn);

    ArrowType resolved = (ArrowType) arrowType.resolveInternal(reporter, scope);
    assertNotNull(resolved);
    assertEquals(numberType, resolved.returnType);
    assertEquals(stringType, resolved.parameters.getFirstChild().getJSType());
  }

  // Tests hasAnyTemplateInternal when return type or parameter has template type
  @Test
  public void testHasAnyTemplateInternal_withTemplateTypes_returnsTrue() {
    TemplateType templateType = registry.createTemplateType("T");

    ArrowType arrowWithTemplateReturn = new ArrowType(registry, null, templateType);
    assertTrue(arrowWithTemplateReturn.hasAnyTemplateInternal());

    Node paramsWithTemplate = registry.createParameters(templateType);
    ArrowType arrowWithTemplateParam = new ArrowType(registry, paramsWithTemplate, numberType);
    assertTrue(arrowWithTemplateParam.hasAnyTemplateInternal());
  }

  // Tests isSubtype with optional parameters and varargs
  @Test
  public void testIsSubtype_optionalAndVarArgsParameters() {
    Node optParams = registry.createOptionalParameters(numberType);
    Node reqParams = registry.createParameters(numberType);
    Node varArgs = registry.createParametersWithVarArgs(numberType);

    ArrowType optArrow = new ArrowType(registry, optParams, numberType);
    ArrowType reqArrow = new ArrowType(registry, reqParams, numberType);
    ArrowType varArgsArrow = new ArrowType(registry, varArgs, numberType);

    // Function accepting fewer/optional params can substitute one requiring params
    assertTrue(optArrow.isSubtype(reqArrow));
    // Varargs arrow subtyping
    assertTrue(varArgsArrow.isSubtype(reqArrow));
    assertTrue(varArgsArrow.isSubtype(optArrow));
  }

  // Tests checkArrowEquivalenceHelper with EquivalenceMethod variants
  @Test
  public void testCheckArrowEquivalenceHelper_withEquivalenceMethod() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType);
    ArrowType arrow2 = new ArrowType(registry, params2, stringType);

    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.IDENTITY));
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.INVARIANT));
    assertTrue(arrow1.checkArrowEquivalenceHelper(arrow2, EquivalenceMethod.DATA_FLOW));

    Node diffParams = registry.createParameters(stringType);
    ArrowType arrow3 = new ArrowType(registry, diffParams, stringType);
    assertFalse(arrow1.checkArrowEquivalenceHelper(arrow3, EquivalenceMethod.IDENTITY));
  }

  // Tests hasEqualParameters with EquivalenceMethod variants
  @Test
  public void testHasEqualParameters_withEquivalenceMethod() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    ArrowType arrow1 = new ArrowType(registry, params1, stringType);
    ArrowType arrow2 = new ArrowType(registry, params2, stringType);

    assertTrue(arrow1.hasEqualParameters(arrow2, EquivalenceMethod.IDENTITY));
    assertTrue(arrow1.hasEqualParameters(arrow2, EquivalenceMethod.INVARIANT));
  }

  // Tests isSubtype with empty super parameters and non-empty sub parameters
  @Test
  public void testIsSubtype_emptySuperParameters_returnsFalseWhenSubHasRequired() {
    Node paramsSub = registry.createParameters(numberType);
    ArrowType sub = new ArrowType(registry, paramsSub, numberType);
    ArrowType sup = new ArrowType(registry, null, numberType);
    assertFalse(sub.isSubtype(sup));
  }
}