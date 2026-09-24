package com.google.javascript.jscomp;

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
    options.checkMissingGetCssNameLevel = CheckLevel.OFF;
  }

  private void testTypes(String js, DiagnosticType expectedWarning) {
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "var window; function alert(x) {}")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("testcode.js", js)
    };
    compiler.compile(externs, inputs, options);

    if (expectedWarning != null) {
      boolean found = false;
      for (JSError warning : compiler.getWarnings()) {
        if (warning.getType() == expectedWarning) {
          found = true;
          break;
        }
      }
      for (JSError error : compiler.getErrors()) {
        if (error.getType() == expectedWarning) {
          found = true;
          break;
        }
      }
      assertTrue("Expected warning/error: " + expectedWarning.key, found);
    } else {
      assertEquals("Expected 0 errors", 0, compiler.getErrorCount());
      assertEquals("Expected 0 warnings", 0, compiler.getWarningCount());
    }
  }

  private void testTypes(String js) {
    testTypes(js, null);
  }

  // Tests object literal with numeric keys (regression for Defect 66)
  @Test
  public void testObjectLit_numericKeys_noErrors() {
    testTypes("var a = {0: 1, 1: 'hello', 2: true};");
  }

  // Tests object literal with string keys
  @Test
  public void testObjectLit_stringKeys_noErrors() {
    testTypes("var a = {'foo': 1, 'bar': 2};");
  }

  // Tests function call with correct parameter types and counts
  @Test
  public void testFunctionCall_validArguments_noWarnings() {
    testTypes("/** @param {number} x\n @return {number} */ function f(x) { return x + 1; } f(10);");
  }

  // Tests function call with wrong argument count
  @Test
  public void testFunctionCall_wrongArgumentCount_reportsWarning() {
    testTypes("/** @param {number} x */ function f(x) {} f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests calling a non-callable expression
  @Test
  public void testCall_nonCallableType_reportsWarning() {
    testTypes("var x = 123; x();", TypeCheck.NOT_CALLABLE);
  }

  // Tests calling constructor without 'new'
  @Test
  public void testConstructorCall_withoutNew_reportsWarning() {
    testTypes("/** @constructor */ function Foo() {} Foo();", TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  // Tests instantiation of non-constructor
  @Test
  public void testNew_nonConstructor_reportsWarning() {
    testTypes("var x = 123; new x();", TypeCheck.NOT_A_CONSTRUCTOR);
  }

  // Tests deterministic comparison yielding constant result
  @Test
  public void testComparison_deterministicEquality_reportsWarning() {
    testTypes("var x = 1 === '1';", TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
  }

  // Tests bitwise operation on non-integer types
  @Test
  public void testBitwiseOperation_invalidType_reportsWarning() {
    testTypes("var x = 'abc' >> 1;", TypeCheck.BIT_OPERATION);
  }

  // Tests delete operator on invalid operand
  @Test
  public void testDelete_invalidOperand_reportsWarning() {
    testTypes("delete (1 + 2);", TypeCheck.BAD_DELETE);
  }

  // Tests access to inexistent enum element
  @Test
  public void testEnum_inexistentElement_reportsWarning() {
    testTypes("/** @enum {number} */ var MyEnum = { A: 1, B: 2 }; var x = MyEnum.C;", TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  // Tests interface function having a non-empty body
  @Test
  public void testInterface_nonEmptyFunction_reportsWarning() {
    testTypes("/** @interface */ function Foo() {} Foo.prototype.bar = function() { return 1; };", TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  // Tests constructor extending an interface directly
  @Test
  public void testConstructor_extendingInterface_reportsWarning() {
    testTypes("/** @interface */ function AnInterface() {} /** @constructor\n @extends {AnInterface} */ function Bar() {}", TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  // Tests implementing a non-interface type
  @Test
  public void testConstructor_implementingNonInterface_reportsWarning() {
    testTypes("/** @constructor */ function NotAnInterface() {} /** @constructor\n @implements {NotAnInterface} */ function Bar() {}", TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  // Tests @noTypeCheck annotation suppression
  @Test
  public void testNoTypeCheck_suppressesWarnings() {
    testTypes("/** @noTypeCheck */ function f() { var x = 123; x(); }", null);
  }

  // Tests function expecting 'this' type invoked unbound
  @Test
  public void testExpectedThisType_unboundCall_reportsWarning() {
    testTypes("/** @this {{a: number}} */ function f() {} f();", TypeCheck.EXPECTED_THIS_TYPE);
  }

  // Tests instanceof operator checks
  @Test
  public void testInstanceOf_validOperands_noErrors() {
    testTypes("/** @constructor */ function Foo() {} var f = new Foo(); var isFoo = f instanceof Foo;");
  }
}