package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private TypeCheck check(String js, DiagnosticType expectedWarning) {
    return check("", js, expectedWarning);
  }

  private TypeCheck check(String externs, String js, DiagnosticType expectedWarning) {
    Node externsRoot = compiler.parseTestCode(externs);
    Node jsRoot = compiler.parseTestCode(js);
    new Node(Token.BLOCK, externsRoot, jsRoot);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck check = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        registry,
        CheckLevel.WARNING,
        CheckLevel.OFF);

    check.processForTesting(externsRoot, jsRoot);

    if (expectedWarning != null) {
      assertTrue("Expected at least one warning", compiler.getWarningCount() > 0);
      boolean found = false;
      for (JSError warning : compiler.getWarnings()) {
        if (expectedWarning.key.equals(warning.getType().key)) {
          found = true;
          break;
        }
      }
      assertTrue("Expected warning " + expectedWarning.key + " not found", found);
    } else {
      assertEquals("Expected no warnings", 0, compiler.getWarningCount());
      assertEquals("Expected no errors", 0, compiler.getErrorCount());
    }
    return check;
  }

  // Tests function call expecting 'this' context when called as a free function (Closure 69 regression)
  @Test
  public void testVisitCall_functionWithExplicitThisTypeCalledWithoutThis_reportsExpectedThisType() {
    String js = "/** @type {function(this:Array, number): undefined} */ var f;" +
                "f(1);";
    check(js, TypeCheck.EXPECTED_THIS_TYPE);
  }

  // Tests normal function call with valid arguments and return type
  @Test
  public void testVisitCall_validCall_noWarnings() {
    String js = "/** @param {number} x\n * @return {number} */ function f(x) { return x + 1; } f(10);";
    check(js, null);
  }

  // Tests calling a non-function value
  @Test
  public void testVisitCall_notCallableType_reportsNotCallable() {
    String js = "var x = 123; x();";
    check(js, TypeCheck.NOT_CALLABLE);
  }

  // Tests calling a constructor function directly without 'new'
  @Test
  public void testVisitCall_constructorCalledWithoutNew_reportsConstructorNotCallable() {
    String js = "/** @constructor */ function Foo() {} Foo();";
    check(js, TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  // Tests function call with too few arguments
  @Test
  public void testVisitParameterList_tooFewArguments_reportsWrongArgumentCount() {
    String js = "/** @param {number} a\n * @param {number} b */ function f(a, b) {} f(1);";
    check(js, TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests function call with too many arguments
  @Test
  public void testVisitParameterList_tooManyArguments_reportsWrongArgumentCount() {
    String js = "/** @param {number} a */ function f(a) {} f(1, 2);";
    check(js, TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests instantiating a non-constructor with 'new'
  @Test
  public void testVisitNew_instantiateNonConstructor_reportsNotAConstructor() {
    String js = "var x = 42; new x();";
    check(js, TypeCheck.NOT_A_CONSTRUCTOR);
  }

  // Tests bitwise operator applied to invalid operand type
  @Test
  public void testVisitBinaryOperator_bitOperationOnString_reportsBitOperation() {
    String js = "var x = 'hello'; var y = ~x;";
    check(js, TypeCheck.BIT_OPERATION);
  }

  // Tests shift operator applied to invalid operand type
  @Test
  public void testVisitBinaryOperator_shiftOperationOnString_reportsBitOperation() {
    String js = "var x = 'hello'; var y = x >> 2;";
    check(js, TypeCheck.BIT_OPERATION);
  }

  // Tests accessing nonexistent element on enum
  @Test
  public void testCheckPropertyAccess_nonexistentEnumElement_reportsInexistentEnumElement() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1 }; var val = MyEnum.B;";
    check(js, TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  // Tests constructor extending an interface instead of a class
  @Test
  public void testVisitFunction_constructorExtendsInterface_reportsConflictingExtendedType() {
    String js = "/** @interface */ function AnInterface() {} " +
                "/** @constructor\n * @extends {AnInterface} */ function MyClass() {}";
    check(js, TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  // Tests interface implementing another interface instead of extending
  @Test
  public void testVisitFunction_interfaceImplementsInterface_reportsConflictingImplementedType() {
    String js = "/** @interface */ function InterfaceA() {} " +
                "/** @interface\n * @implements {InterfaceA} */ function InterfaceB() {}";
    check(js, TypeCheck.CONFLICTING_IMPLEMENTED_TYPE);
  }

  // Tests constructor implementing a non-interface class
  @Test
  public void testVisitFunction_implementsNonInterface_reportsBadImplementedType() {
    String js = "/** @constructor */ function ClassA() {} " +
                "/** @constructor\n * @implements {ClassA} */ function ClassB() {}";
    check(js, TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  // Tests function declaration masking existing variable
  @Test
  public void testShouldTraverse_functionMasksVariable_reportsFunctionMasksVariable() {
    String js = "var myVar = 10; function myVar() {}";
    check(js, TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  // Tests delete operator on invalid reference expression
  @Test
  public void testVisit_deleteInvalidOperand_reportsBadDelete() {
    String js = "delete (1 + 2);";
    check(js, TypeCheck.BAD_DELETE);
  }

  // Tests getTypedPercent calculation
  @Test
  public void testGetTypedPercent_validTypedProgram_returnsExpectedPercentage() {
    String js = "/** @type {number} */ var a = 1; /** @type {number} */ var b = 2;";
    TypeCheck tc = check(js, null);
    double percent = tc.getTypedPercent();
    assertTrue("Typed percent should be greater than 0.0", percent > 0.0);
    assertTrue("Typed percent should be <= 100.0", percent <= 100.0);
  }
}