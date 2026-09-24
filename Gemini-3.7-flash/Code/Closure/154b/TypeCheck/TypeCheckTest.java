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

  private TypeCheck createTypeCheck() {
    return createTypeCheck(CheckLevel.WARNING, CheckLevel.OFF);
  }

  private TypeCheck createTypeCheck(CheckLevel reportMissingOverride, CheckLevel reportUnknownTypes) {
    JSTypeRegistry registry = compiler.getTypeRegistry();
    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreter();
    return new TypeCheck(compiler, rai, registry, reportMissingOverride, reportUnknownTypes);
  }

  private void check(String js) {
    check("", js);
  }

  private void check(String externs, String js) {
    Node externsRoot = compiler.parseTestCode(externs);
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);
    TypeCheck tc = createTypeCheck();
    tc.processForTesting(externsRoot, jsRoot);
  }

  private void checkWithWarnings(String js, int expectedWarnings) {
    check(js);
    assertEquals(expectedWarnings, compiler.getWarningCount());
  }

  private void checkWithErrors(String js, int expectedErrors) {
    check(js);
    assertEquals(expectedErrors, compiler.getErrorCount());
  }

  // Tests valid numeric assignment
  @Test
  public void testVisitAssign_validNumber_noWarnings() {
    check("/** @type {number} */ var x = 10; x = 20;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests invalid assignment type mismatch
  @Test
  public void testVisitAssign_typeMismatch_reportsWarning() {
    checkWithWarnings("/** @type {number} */ var x = 10; x = 'hello';", 1);
  }

  // Tests binary arithmetic operators with valid numbers
  @Test
  public void testVisitBinaryOperator_validNumbers_noWarnings() {
    check("var x = 5 + 10; var y = 5 * 2; var z = 10 / 2; var w = 10 - 3;");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests bitwise operators with non-numeric operand
  @Test
  public void testVisitBinaryOperator_invalidBitwiseOperand_reportsWarning() {
    checkWithWarnings("var x = 'str' & 5;", 1);
  }

  // Tests calling non-callable expression
  @Test
  public void testVisitCall_nonCallable_reportsWarning() {
    checkWithWarnings("var x = 10; x();", 1);
  }

  // Tests constructor called without new keyword
  @Test
  public void testVisitCall_constructorWithoutNew_reportsWarning() {
    checkWithWarnings("/** @constructor */ function Foo() {} Foo();", 1);
  }

  // Tests function call with wrong number of arguments
  @Test
  public void testVisitParameterList_wrongArgumentCount_reportsWarning() {
    checkWithWarnings("function foo(a, b) {} foo(1);", 1);
  }

  // Tests instantiating non-constructor
  @Test
  public void testVisitNew_nonConstructor_reportsWarning() {
    checkWithWarnings("var x = 10; new x();", 1);
  }

  // Tests valid constructor instantiation with new keyword
  @Test
  public void testVisitNew_validConstructor_noWarnings() {
    check("/** @constructor */ function Foo() {} var f = new Foo();");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests inconsistent return type in function
  @Test
  public void testVisitReturn_typeMismatch_reportsWarning() {
    checkWithWarnings("/** @return {number} */ function foo() { return 'notANumber'; }", 1);
  }

  // Tests valid return type matching declaration
  @Test
  public void testVisitReturn_correctType_noWarnings() {
    check("/** @return {number} */ function foo() { return 42; }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests interface member function must have empty body
  @Test
  public void testVisitFunction_interfaceFunctionNotEmpty_reportsWarning() {
    checkWithWarnings(
        "/** @interface */ function Foo() {}" +
        "Foo.prototype.bar = function() { return 1; };", 1);
  }

  // Tests interface extending constructor causes conflicting type warning
  @Test
  public void testVisitFunction_conflictingExtendedType_reportsWarning() {
    checkWithWarnings(
        "/** @constructor */ function Super() {}" +
        "/** @interface \n * @extends {Super} */ function Sub() {}", 1);
  }

  // Tests subclass overriding superclass property with mismatched type
  @Test
  public void testCheckDeclaredPropertyInheritance_mismatchedOverride_reportsWarning() {
    checkWithWarnings(
        "/** @constructor */ function Super() {}" +
        "/** @type {number} */ Super.prototype.foo = 1;" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}" +
        "/** @override \n * @type {string} */ Sub.prototype.foo = 'str';", 1);
  }

  // Tests interface property implementation mismatch
  @Test
  public void testCheckDeclaredPropertyInheritance_interfacePropertyMismatch_reportsWarning() {
    checkWithWarnings(
        "/** @interface */ function AnInterface() {}" +
        "/** @type {number} */ AnInterface.prototype.foo;" +
        "/** @constructor \n * @implements {AnInterface} */ function Impl() {}" +
        "/** @type {string} */ Impl.prototype.foo = 'str';", 1);
  }

  // Tests equality comparison between incompatible types
  @Test
  public void testVisitEq_deterministicComparison_reportsWarning() {
    checkWithWarnings("var x = (1 === 'str');", 1);
  }

  // Tests delete operator with non-reference operand
  @Test
  public void testVisitDelprop_invalidOperand_reportsWarning() {
    checkWithWarnings("delete 1;", 1);
  }

  // Tests typed percent calculation on empty and non-empty scripts
  @Test
  public void testGetTypedPercent_returnsValidPercentage() {
    Node externsRoot = compiler.parseTestCode("");
    Node jsRoot = compiler.parseTestCode("var x = 1;");
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);
    TypeCheck tc = createTypeCheck();
    tc.processForTesting(externsRoot, jsRoot);
    double percent = tc.getTypedPercent();
    assertTrue(percent >= 0.0 && percent <= 100.0);
  }

  // Tests @noTypeCheck suppresses type warnings
  @Test
  public void testCheckNoTypeCheckSection_suppressesWarnings() {
    check("/** @noTypeCheck */ function foo() { var x = 10; x = 'str'; }");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests enum initialization with incompatible element type
  @Test
  public void testCheckEnumInitializer_incompatibleType_reportsWarning() {
    checkWithWarnings("/** @enum {number} */ var MyEnum = { A: 'str' };", 1);
  }

  // Tests property access on null
  @Test
  public void testVisitGetProp_propertyAccessOnNull_reportsWarning() {
    checkWithWarnings("var x = null; x.foo = 1;", 1);
  }

  // Tests 'in' operator with non-object right-hand operand
  @Test
  public void testVisitIn_nonObject_reportsWarning() {
    checkWithWarnings("var x = 'foo' in 123;", 1);
  }

  // Tests 'instanceof' operator with non-constructor/non-interface right-hand operand
  @Test
  public void testVisitInstanceOf_invalidRhs_reportsWarning() {
    checkWithWarnings("var x = {} instanceof 123;", 1);
  }

  // Tests array literal element type mismatch
  @Test
  public void testVisitArrayLiteral_typeMismatch_reportsWarning() {
    checkWithWarnings("/** @type {Array<number>} */ var arr = ['str'];", 1);
  }

  // Tests object literal key-value type mismatch
  @Test
  public void testVisitObjectLiteral_typeMismatch_reportsWarning() {
    checkWithWarnings("/** @type {{a: number}} */ var obj = {a: 'str'};", 1);
  }

  // Tests variable redeclaration with incompatible types
  @Test
  public void testVisitVar_redeclaredWithIncompatibleType_reportsWarning() {
    checkWithWarnings("/** @type {number} */ var x = 1; /** @type {string} */ var x = 'a';", 1);
  }

  // Tests unary increment on non-numeric type
  @Test
  public void testVisitUnaryIncDec_nonNumeric_reportsWarning() {
    checkWithWarnings("var x = 'abc'; x++;", 1);
  }

  // Tests 'for...in' loop over non-object
  @Test
  public void testVisitForIn_nonObject_reportsWarning() {
    checkWithWarnings("for (var x in 123) {}", 1);
  }

  // Tests class failing to implement interface method
  @Test
  public void testInterface_unimplementedMethod_reportsWarning() {
    checkWithWarnings(
        "/** @interface */ function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        "/** @constructor \n * @implements {Foo} */ function Baz() {}", 1);
  }

  // Tests missing @override reporting when configured
  @Test
  public void testMissingOverride_reportsWarning() {
    Node externsRoot = compiler.parseTestCode("");
    Node jsRoot = compiler.parseTestCode(
        "/** @constructor */ function Super() {}" +
        "Super.prototype.foo = function() {};" +
        "/** @constructor \n * @extends {Super} */ function Sub() {}" +
        "Sub.prototype.foo = function() {};");
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);
    TypeCheck tc = createTypeCheck(CheckLevel.WARNING, CheckLevel.OFF);
    tc.processForTesting(externsRoot, jsRoot);
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests extending a non-constructor type
  @Test
  public void testExtends_nonConstructor_reportsWarning() {
    checkWithWarnings("var NotAClass = 123; /** @constructor \n * @extends {NotAClass} */ function Sub() {}", 1);
  }

  // Tests implementing a non-interface type
  @Test
  public void testImplements_nonInterface_reportsWarning() {
    checkWithWarnings("var NotAnInterface = 123; /** @constructor \n * @implements {NotAnInterface} */ function Sub() {}", 1);
  }
}