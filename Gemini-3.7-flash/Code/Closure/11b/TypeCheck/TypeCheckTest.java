package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest extends CompilerTypeTestCase {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  // Tests property assignment on null object
  @Test
  public void testVisitGetProp_assignToNullProperty_reportsWarning() {
    testTypes(
        "/** @type {null} */ var x; x.foo = 1;",
        "No properties on this expression\n" +
        "found   : null\n" +
        "required: Object");
  }

  // Tests property assignment on undefined object
  @Test
  public void testVisitGetProp_assignToUndefinedProperty_reportsWarning() {
    testTypes(
        "/** @type {undefined} */ var x; x.foo = 1;",
        "No properties on this expression\n" +
        "found   : undefined\n" +
        "required: Object");
  }

  // Tests property access on valid object
  @Test
  public void testVisitGetProp_validPropertyAccess_noWarning() {
    testTypes("var x = {foo: 1}; var y = x.foo;");
  }

  // Tests valid function call
  @Test
  public void testVisitCall_validCall_noWarning() {
    testTypes("function f(/** number */ x) {} f(1);");
  }

  // Tests function call with wrong argument type
  @Test
  public void testVisitCall_wrongArgumentType_reportsWarning() {
    testTypes(
        "function f(/** number */ x) {} f('abc');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

  // Tests calling non-callable expression
  @Test
  public void testVisitCall_notCallable_reportsWarning() {
    testTypes(
        "var x = 123; x();",
        TypeCheck.NOT_CALLABLE);
  }

  // Tests calling constructor without new keyword
  @Test
  public void testVisitCall_constructorWithoutNew_reportsWarning() {
    testTypes(
        "/** @constructor */ function Foo() {} Foo();",
        TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  // Tests calling function with too few arguments
  @Test
  public void testVisitCall_tooFewArguments_reportsWarning() {
    testTypes(
        "function f(x, y) {} f(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests calling function with too many arguments
  @Test
  public void testVisitCall_tooManyArguments_reportsWarning() {
    testTypes(
        "function f(x) {} f(1, 2);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests instantiating non-constructor
  @Test
  public void testVisitNew_notAConstructor_reportsWarning() {
    testTypes(
        "var x = 123; new x();",
        TypeCheck.NOT_A_CONSTRUCTOR);
  }

  // Tests valid new instantiation
  @Test
  public void testVisitNew_validConstructor_noWarning() {
    testTypes("/** @constructor */ function Foo() {} var x = new Foo();");
  }

  // Tests bitwise operation on invalid type
  @Test
  public void testVisitBinaryOperator_badBitOperation_reportsWarning() {
    testTypes(
        "var x = 'abc' ^ 1;",
        TypeCheck.BIT_OPERATION);
  }

  // Tests bitwise NOT on string
  @Test
  public void testVisitBitNot_stringOperand_reportsWarning() {
    testTypes(
        "var x = ~'abc';",
        TypeCheck.BIT_OPERATION);
  }

  // Tests deterministic equality comparison
  @Test
  public void testVisitEqual_deterministicComparison_reportsWarning() {
    testTypes(
        "var x = 1 === 'abc';",
        TypeCheck.DETERMINISTIC_TEST);
  }

  // Tests inconsistent return type
  @Test
  public void testVisitReturn_inconsistentReturnType_reportsWarning() {
    testTypes(
        "/** @return {number} */ function f() { return 'abc'; }",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

  // Tests variable initialization type mismatch
  @Test
  public void testVisitVar_typeMismatch_reportsWarning() {
    testTypes(
        "/** @type {number} */ var x = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

  // Tests invalid delete operator
  @Test
  public void testVisitUnary_incNonNumber_reportsWarning() {
    testTypes(
        "var x = 'abc'; x++;",
        "increment/decrement\n" +
        "found   : string\n" +
        "required: number");
  }

  // Tests relational comparison mismatch
  @Test
  public void testVisitRelationalOperator_typeMismatch_reportsWarning() {
    testTypes(
        "var x = true < 5;",
        "left side of numeric comparison\n" +
        "found   : boolean\n" +
        "required: number");
  }

  // Tests instanceof requires object
  @Test
  public void testVisitInstanceOf_nonObject_reportsWarning() {
    testTypes(
        "var x = 1 instanceof 2;",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }
}