package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TypeCheckTest extends CompilerTestCase {

  private CheckLevel reportMissingOverride = CheckLevel.OFF;
  private CheckLevel reportUnknownTypes = CheckLevel.OFF;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    reportMissingOverride = CheckLevel.OFF;
    reportUnknownTypes = CheckLevel.OFF;
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        reportMissingOverride,
        reportUnknownTypes);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private void testTypes(String js) {
    testSame(js);
  }

  private void testTypes(String js, DiagnosticType warning) {
    test(js, warning);
  }

  private void testTypes(String js, String description) {
    testSame(js);
  }

  // Tests var_args parameter type checking (Defects4J Closure 96 defect check)
  @Test
  public void testVisitParameterList_varArgsSubsequentArguments_validatesCorrectly() {
    testTypes(
        "/** @param {string} x\n" +
        "  * @param {...number} var_args */\n" +
        "function f(x, var_args) {}\n" +
        "f('a', 1, 2, 'bad');",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  // Tests normal function call with valid arguments
  @Test
  public void testVisitCall_validArguments_noWarning() {
    testTypes(
        "/** @param {number} a\n" +
        "  * @param {string} b */\n" +
        "function f(a, b) {}\n" +
        "f(1, 'hello');");
  }

  // Tests wrong argument count when too few arguments are passed
  @Test
  public void testVisitParameterList_tooFewArguments_reportsWrongArgumentCount() {
    testTypes(
        "/** @param {number} a\n" +
        "  * @param {number} b */\n" +
        "function f(a, b) {}\n" +
        "f(1);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests wrong argument count when too many arguments are passed
  @Test
  public void testVisitParameterList_tooManyArguments_reportsWrongArgumentCount() {
    testTypes(
        "/** @param {number} a */\n" +
        "function f(a) {}\n" +
        "f(1, 2);",
        TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests calling a non-callable expression
  @Test
  public void testVisitCall_nonCallableType_reportsNotCallable() {
    testTypes(
        "var x = 123;\n" +
        "x();",
        TypeCheck.NOT_CALLABLE);
  }

  // Tests calling constructor without new keyword
  @Test
  public void testVisitCall_constructorWithoutNew_reportsConstructorNotCallable() {
    testTypes(
        "/** @constructor */ function Foo() {}\n" +
        "Foo();",
        TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  // Tests new operator on non-constructor
  @Test
  public void testVisitNew_nonConstructor_reportsNotAConstructor() {
    testTypes(
        "var x = 123;\n" +
        "new x();",
        TypeCheck.NOT_A_CONSTRUCTOR);
  }

  // Tests delete operator operand check
  @Test
  public void testVisit_badDelete_reportsBadDelete() {
    testTypes(
        "delete (1 + 1);",
        TypeCheck.BAD_DELETE);
  }

  // Tests bitwise operation with invalid operand type
  @Test
  public void testVisitBinaryOperator_invalidBitwiseOperand_reportsBitOperation() {
    testTypes(
        "var x = 'abc' >> 1;",
        TypeCheck.BIT_OPERATION);
  }

  // Tests deterministic equality test between incompatible types
  @Test
  public void testVisit_deterministicTest_reportsDeterministicTest() {
    testTypes(
        "var x = 1;\n" +
        "var y = 'str';\n" +
        "if (x === y) {}",
        TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
  }

  // Tests numeric comparison with string context
  @Test
  public void testVisit_numericComparison_typeMismatch() {
    testTypes(
        "var x = 1 < true;",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  // Tests 'in' operator requiring object right hand side
  @Test
  public void testVisit_inOperatorNonObject_reportsTypeMismatch() {
    testTypes(
        "var x = 'prop' in 123;",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  // Tests instanceof operator requiring object
  @Test
  public void testVisit_instanceofNonObject_reportsTypeMismatch() {
    testTypes(
        "var x = 1 instanceof 2;",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  // Tests inconsistent return type
  @Test
  public void testVisitReturn_inconsistentReturnType_reportsTypeMismatch() {
    testTypes(
        "/** @return {number} */\n" +
        "function f() {\n" +
        "  return 'abc';\n" +
        "}",
        TypeValidator.TYPE_MISMATCH_WARNING);
  }

  // Tests missing @override annotation when overriding superclass property
  @Test
  public void testCheckDeclaredPropertyInheritance_missingOverride_reportsHiddenSuperclassProperty() {
    reportMissingOverride = CheckLevel.WARNING;
    testTypes(
        "/** @constructor */ function Super() {}\n" +
        "Super.prototype.foo = function() {};\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "Sub.prototype.foo = function() {};",
        TypeCheck.HIDDEN_SUPERCLASS_PROPERTY);
  }

  // Tests @override annotation on a method that does not exist on superclass
  @Test
  public void testCheckDeclaredPropertyInheritance_unknownOverride_reportsUnknownOverride() {
    testTypes(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor @extends {Super} */ function Sub() {}\n" +
        "/** @override */ Sub.prototype.nonExistent = function() {};",
        TypeCheck.UNKNOWN_OVERRIDE);
  }

  // Tests conflicting extended type
  @Test
  public void testVisitFunction_conflictingExtendedType_reportsConflictingExtendedType() {
    testTypes(
        "/** @interface */ function Int() {}\n" +
        "/** @constructor @extends {Int} */ function Foo() {}",
        TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  // Tests typed percent calculation getter
  @Test
  public void testGetTypedPercent_calculation_returnsExpectedPercentage() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    TypeCheck checker = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        registry,
        CheckLevel.OFF,
        CheckLevel.OFF);

    assertEquals(0.0, checker.getTypedPercent(), 0.001);
  }
}