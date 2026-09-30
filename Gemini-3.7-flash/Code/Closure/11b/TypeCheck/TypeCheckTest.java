package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.checkTypes = true;
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
  }

  private void testTypes(String js) {
    testTypes(js, (String[]) null);
  }

  private void testTypes(String js, String warning) {
    testTypes(js, warning != null ? new String[] { warning } : new String[0]);
  }

  private void testTypes(String js, String[] warnings) {
    SourceFile externFile = SourceFile.fromCode("externs.js", "");
    SourceFile inputFile = SourceFile.fromCode("input.js", js);
    compiler.compile(externFile, inputFile, options);

    JSError[] actualWarnings = compiler.getWarnings();
    JSError[] actualErrors = compiler.getErrors();

    if (warnings == null || warnings.length == 0) {
      assertEquals("Expected no errors: " + formatErrors(actualErrors), 0, actualErrors.length);
      assertEquals("Expected no warnings: " + formatErrors(actualWarnings), 0, actualWarnings.length);
    } else {
      int total = actualWarnings.length + actualErrors.length;
      assertTrue("Expected warnings or errors, but found none", total > 0);
      for (String expected : warnings) {
        boolean found = false;
        for (JSError error : actualErrors) {
          if (error.getDescription().contains(expected)) {
            found = true;
            break;
          }
        }
        if (!found) {
          for (JSError warn : actualWarnings) {
            if (warn.getDescription().contains(expected)) {
              found = true;
              break;
            }
          }
        }
        assertTrue("Expected message containing '" + expected + "', but got: "
            + formatErrors(actualErrors) + " " + formatErrors(actualWarnings), found);
      }
    }
  }

  private String formatErrors(JSError[] errors) {
    StringBuilder sb = new StringBuilder();
    for (JSError e : errors) {
      sb.append(e.getDescription()).append("; ");
    }
    return sb.toString();
  }

  // Tests property access on null assignment target
  @Test
  public void testVisitGetProp_nullPropertyAssignment_warns() {
    testTypes("/** @param {null} x */ function f(x) { x.foo = 1; }", "No properties on this expression");
  }

  // Tests property access on undefined assignment target
  @Test
  public void testVisitGetProp_undefinedPropertyAssignment_warns() {
    testTypes("/** @param {undefined} x */ function f(x) { x.foo = 1; }", "No properties on this expression");
  }

  // Tests normal property assignment on typed object
  @Test
  public void testVisitGetProp_validPropertyAssignment_noWarning() {
    testTypes("/** @param {{foo: number}} x */ function f(x) { x.foo = 1; }");
  }

  // Tests bitwise NOT operation on non-integer
  @Test
  public void testVisitBinaryOperator_bitNotString_warns() {
    testTypes("var x = ~'abc';", "operator ~ cannot be applied to string");
  }

  // Tests bitwise AND operation with invalid operand
  @Test
  public void testVisitBinaryOperator_bitAndString_warns() {
    testTypes("var x = 'abc' & 5;", "bad left operand to bitwise operator");
  }

  // Tests numeric increment on non-numeric type
  @Test
  public void testVisitUnary_incrementString_warns() {
    testTypes("var x = 'abc'; x++;", "increment/decrement");
  }

  // Tests new operator on non-constructor
  @Test
  public void testVisitNew_numberAsConstructor_warns() {
    testTypes("var x = new 123();", "cannot instantiate non-constructor");
  }

  // Tests calling non-function type
  @Test
  public void testVisitCall_numberAsFunction_warns() {
    testTypes("var x = 123; x();", "number expressions are not callable");
  }

  // Tests function call with too few arguments
  @Test
  public void testVisitCall_missingRequiredArgument_warns() {
    testTypes("/** @param {number} a\n@param {number} b */ function f(a, b) {} f(1);",
        "Function f: called with 1 argument(s). Function requires at least 2 argument(s)");
  }

  // Tests function call with too many arguments
  @Test
  public void testVisitCall_extraArgument_warns() {
    testTypes("/** @param {number} a */ function f(a) {} f(1, 2);",
        "Function f: called with 2 argument(s). Function requires at least 1 argument(s) and no more than 1 argument(s)");
  }

  // Tests return type mismatch
  @Test
  public void testVisitReturn_inconsistentReturnType_warns() {
    testTypes("/** @return {number} */ function f() { return 'abc'; }", "inconsistent return type");
  }

  // Tests variable initialization type mismatch
  @Test
  public void testVisitVar_typeMismatchInitialization_warns() {
    testTypes("/** @type {number} */ var x = 'abc';", "initializing variable");
  }

  // Tests deterministic strict equality comparison between incompatible types
  @Test
  public void testVisitComparison_deterministicStrictEquals_warns() {
    testTypes("var x = (1 === '1');", "condition always evaluates to false");
  }

  // Tests instanceof operator on non-object
  @Test
  public void testVisitInstanceOf_primitiveLeftOperand_warns() {
    testTypes("var x = 1 instanceof Object;", "deterministic instanceof yields false");
  }

  // Tests in operator with non-object right operand
  @Test
  public void testVisitIn_primitiveRightOperand_warns() {
    testTypes("var x = 'a' in 123;", "'in' requires an object");
  }

  // Tests interface implementing another interface
  @Test
  public void testVisitFunction_interfaceImplementsInterface_warns() {
    testTypes("/** @interface */ function I() {}\n/** @interface\n@implements {I} */ function J() {}",
        "interface cannot implement this type");
  }

  // Tests valid function declaration and call
  @Test
  public void testVisitFunction_validUsage_noWarning() {
    testTypes("/** @param {number} a\n@return {number} */ function f(a) { return a + 1; } f(5);");
  }

  // Tests variable reassignment type mismatch
  @Test
  public void testVisitAssign_typeMismatch_warns() {
    testTypes("/** @type {number} */ var x = 1; x = 'abc';", "assignment");
  }

  // Tests unknown type annotation in JSDoc
  @Test
  public void testVisitTypeAnnotation_unknownType_warns() {
    testTypes("/** @type {NonExistentTypeName} */ var x;", "Unknown type NonExistentTypeName");
  }

  // Tests superclass extending a primitive type
  @Test
  public void testVisitFunction_extendsPrimitive_warns() {
    testTypes("/** @constructor\n * @extends {number} */ function C() {}", "superclass must be a constructor");
  }

  // Tests unimplemented interface methods on implementing constructor
  @Test
  public void testVisitFunction_unimplementedInterfaceMethod_warns() {
    testTypes("/** @interface */ function I() { /** @type {function(): void} */ this.m; }\n"
        + "/** @constructor\n * @implements {I} */ function C() {}",
        "property m on interface I is not implemented by type C");
  }

  // Tests invalid type cast between incompatible types
  @Test
  public void testVisitCast_invalidTypeCast_warns() {
    testTypes("var x = /** @type {boolean} */ (123);", "invalid cast");
  }

  // Tests bitwise shift operation with string operand
  @Test
  public void testVisitBinaryOperator_bitShiftString_warns() {
    testTypes("var x = 'abc' >> 2;", "bad left operand to bitwise operator");
  }
}