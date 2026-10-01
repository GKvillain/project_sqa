package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private TypeCheck createTypeCheck() {
    return new TypeCheck(compiler, compiler.getReverseAbstractInterpreter(), compiler.getTypeRegistry());
  }

  private TypeCheck testAndCheck(String js) {
    return testAndCheck("", js);
  }

  private TypeCheck testAndCheck(String externsJs, String js) {
    Node externsRoot = compiler.parseTestCode(externsJs);
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);
    TypeCheck tc = createTypeCheck();
    tc.processForTesting(externsRoot, jsRoot);
    return tc;
  }

  // Tests that getTypedPercent returns 0.0 when no nodes have been typed
  @Test
  public void testGetTypedPercent_empty_returnsZero() {
    TypeCheck tc = createTypeCheck();
    assertEquals(0.0, tc.getTypedPercent(), 0.001);
  }

  // Tests typing percentage for basic variable declarations and object literals
  @Test
  public void testGetTypedPercent_simpleVariable_returnsExpectedPercentage() {
    TypeCheck tc = testAndCheck("var x = 1;");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests object literal with number, string, and identifier keys
  @Test
  public void testVisit_objectLiteralKeys_typesCorrectly() {
    TypeCheck tc = testAndCheck("var obj = {1: 'a', 'b': 2, c: 3};");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests getter and setter in object literal
  @Test
  public void testVisit_objectLiteralGetterSetter_noErrors() {
    TypeCheck tc = testAndCheck("var obj = { get a() { return 1; }, set a(val) {} };");
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests bitwise operation on non-integer types triggers warning
  @Test
  public void testVisit_bitOperationInvalidType_reportsWarning() {
    testAndCheck("var x = ~'hello';");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.BIT_OPERATION, compiler.getWarnings()[0].getType());
  }

  // Tests calling a non-callable value triggers warning
  @Test
  public void testVisit_callingNonFunction_reportsWarning() {
    testAndCheck("var x = 1; x();");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.NOT_CALLABLE, compiler.getWarnings()[0].getType());
  }

  // Tests delete operator on a non-reference triggers warning
  @Test
  public void testVisit_badDelete_reportsWarning() {
    testAndCheck("delete (1 + 2);");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.BAD_DELETE, compiler.getWarnings()[0].getType());
  }

  // Tests deterministic comparison equality triggers warning
  @Test
  public void testVisit_deterministicTest_reportsWarning() {
    testAndCheck("var x = 1 === 'str';");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.DETERMINISTIC_TEST_NO_RESULT, compiler.getWarnings()[0].getType());
  }

  // Tests function call with too few arguments triggers warning
  @Test
  public void testVisit_wrongArgumentCount_reportsWarning() {
    testAndCheck("/** @param {number} a \n @param {number} b */ function f(a, b) {} f(1);");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.WRONG_ARGUMENT_COUNT, compiler.getWarnings()[0].getType());
  }

  // Tests instanceof operator requires an object operand
  @Test
  public void testVisit_instanceofNonObject_reportsWarning() {
    testAndCheck("var x = 1 instanceof 2;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests 'in' operator requires object operand
  @Test
  public void testVisit_inOperatorNonObject_reportsWarning() {
    testAndCheck("var x = 'prop' in 123;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests calling constructor without 'new' keyword
  @Test
  public void testVisit_constructorNotCallable_reportsWarning() {
    testAndCheck("/** @constructor */ function Foo() {} Foo();");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.CONSTRUCTOR_NOT_CALLABLE, compiler.getWarnings()[0].getType());
  }

  // Tests instantiating a non-constructor triggers warning
  @Test
  public void testVisit_notAConstructor_reportsWarning() {
    testAndCheck("var x = 1; new x();");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.NOT_A_CONSTRUCTOR, compiler.getWarnings()[0].getType());
  }

  // Tests function with inconsistent return type triggers warning
  @Test
  public void testVisit_inconsistentReturnType_reportsWarning() {
    testAndCheck("/** @return {number} */ function f() { return 'hello'; }");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests reportMissingProperties chaining method
  @Test
  public void testReportMissingProperties_chaining_returnsSelf() {
    TypeCheck tc = createTypeCheck();
    TypeCheck result = tc.reportMissingProperties(false);
    assertSame(tc, result);
  }

  // Tests array and regexp literals are correctly typed
  @Test
  public void testVisit_arrayAndRegExpLiterals_typedWithoutWarnings() {
    TypeCheck tc = testAndCheck("var arr = [1, 2, 3]; var re = /abc/;");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests unary increment and decrement type check
  @Test
  public void testVisit_incrementDecrement_numericTypeChecked() {
    testAndCheck("var x = 1; x++; ++x; x--; --x;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests typeof and void operators
  @Test
  public void testVisit_typeofAndVoid_typedWithoutWarnings() {
    TypeCheck tc = testAndCheck("var s = typeof 1; var v = void 0;");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests type mismatch on variable initialization
  @Test
  public void testVisit_typeMismatchAssignment_reportsWarning() {
    testAndCheck("/** @type {number} */ var x = 'string';");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests ternary hook operator correctly computes unified type
  @Test
  public void testVisit_ternaryHook_typesCorrectly() {
    TypeCheck tc = testAndCheck("var x = true ? 1 : 2;");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests invalid type cast warning
  @Test
  public void testVisit_invalidCast_reportsWarning() {
    testAndCheck("var x = /** @type {boolean} */ (1);");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST, compiler.getWarnings()[0].getType());
  }

  // Tests interface method implementation requirement
  @Test
  public void testVisit_unimplementedInterfaceMethod_reportsWarning() {
    testAndCheck(
        "/** @interface */ function Foo() {}\n"
            + "Foo.prototype.bar = function() {};\n"
            + "/** @constructor @implements {Foo} */ function Bar() {}");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests enum value element type mismatch
  @Test
  public void testVisit_enumElementTypeMismatch_reportsWarning() {
    testAndCheck("/** @enum {number} */ var E = { A: 1, B: 'invalid' };");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests suppression of type errors using @noTypeCheck
  @Test
  public void testVisit_noTypeCheckAnnotation_suppressesWarnings() {
    testAndCheck("/** @noTypeCheck */ function f() { var x = 1; x(); }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests binary addition and logical operators
  @Test
  public void testVisit_binaryAdditionAndLogicalOperators_typesWithoutWarnings() {
    TypeCheck tc = testAndCheck("var a = 'foo' + 1; var b = 2 + 3; var c = true && false; var d = null || 'str';");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests method overriding with signature mismatch
  @Test
  public void testVisit_methodOverrideMismatch_reportsWarning() {
    testAndCheck(
        "/** @constructor */ function Super() {}\n"
            + "Super.prototype.foo = function(/** number */ x) {};\n"
            + "/** @constructor @extends {Super} */ function Sub() {}\n"
            + "Sub.prototype.foo = function(/** string */ x) {};");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests switch statement condition and case checking
  @Test
  public void testVisit_switchStatement_typesCorrectly() {
    TypeCheck tc = testAndCheck("var x = 1; switch(x) { case 1: break; case 2: break; default: break; }");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests loop conditions typing
  @Test
  public void testVisit_loopConditionStatements_typesCorrectly() {
    TypeCheck tc = testAndCheck("var i = 0; while (i < 5) { i++; } do { i--; } while (i > 0); for (var j = 0; j < 5; j++) {}");
    assertTrue(tc.getTypedPercent() > 0.0);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }
}