package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private TypeCheck check(String js) {
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);

    Node externsRoot = new Node(Token.BLOCK);
    Node jsRoot = compiler.parseTestCode(js);
    new Node(Token.BLOCK, externsRoot, jsRoot);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreter();
    TypeCheck typeCheck = new TypeCheck(compiler, rai, registry);
    typeCheck.processForTesting(externsRoot, jsRoot);
    return typeCheck;
  }

  private void check(String js, DiagnosticType expectedDiagnostic) {
    check(js);
    boolean found = false;
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType() == expectedDiagnostic) {
        found = true;
        break;
      }
    }
    if (!found) {
      for (JSError error : compiler.getErrors()) {
        if (error.getType() == expectedDiagnostic) {
          found = true;
          break;
        }
      }
    }
    assertTrue("Expected diagnostic: " + expectedDiagnostic.key, found);
  }

  // Tests defect 69: function with explicit this-type called without a this object
  @Test
  public void testVisitCall_functionWithExplicitThisCalledWithoutReceiver_reportsExpectedThisType() {
    String js = "/** @type {function(this:Object)} */ function f() {} f();";
    check(js, TypeCheck.EXPECTED_THIS_TYPE);
  }

  // Tests valid function call
  @Test
  public void testVisitCall_validCall_noWarnings() {
    String js = "/** @param {number} x */ function f(x) {} f(1);";
    check(js);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests call with wrong number of arguments
  @Test
  public void testVisitCall_wrongArgumentCount_reportsWrongArgumentCount() {
    String js = "function f(a, b) {} f(1);";
    check(js, TypeCheck.WRONG_ARGUMENT_COUNT);
  }

  // Tests call on a non-callable object
  @Test
  public void testVisitCall_notCallable_reportsNotCallable() {
    String js = "var x = 1; x();";
    check(js, TypeCheck.NOT_CALLABLE);
  }

  // Tests instantiation of a non-constructor
  @Test
  public void testVisitNew_nonConstructor_reportsNotAConstructor() {
    String js = "var x = 1; new x();";
    check(js, TypeCheck.NOT_A_CONSTRUCTOR);
  }

  // Tests bad bitwise operation on non-integer
  @Test
  public void testVisitBinaryOperator_bitOperationOnString_reportsBitOperation() {
    String js = "var x = ~'hello';";
    check(js, TypeCheck.BIT_OPERATION);
  }

  // Tests deterministic equality test warning
  @Test
  public void testVisitBinaryOperator_deterministicTest_reportsDeterministicTest() {
    String js = "var x = 1 === '1';";
    check(js, TypeCheck.DETERMINISTIC_TEST_NO_RESULT);
  }

  // Tests deleting non-reference operand
  @Test
  public void testVisitDelprop_nonReference_reportsBadDelete() {
    String js = "delete (1 + 2);";
    check(js, TypeCheck.BAD_DELETE);
  }

  // Tests function masking a variable in outer scope
  @Test
  public void testVisitFunction_masksVariable_reportsFunctionMasksVariable() {
    String js = "var foo = 1; function test() { function foo() {} }";
    check(js, TypeCheck.FUNCTION_MASKS_VARIABLE);
  }

  // Tests inconsistent return type
  @Test
  public void testVisitReturn_inconsistentReturnType_reportsTypeMismatch() {
    String js = "/** @return {number} */ function f() { return 'string'; }";
    check(js);
    assertTrue(compiler.getWarningCount() > 0 || compiler.getErrorCount() > 0);
  }

  // Tests getTypedPercent calculation
  @Test
  public void testGetTypedPercent_validCode_returnsValidPercentage() {
    String js = "/** @type {number} */ var x = 10; var y = x + 5;";
    TypeCheck tc = check(js);
    double percent = tc.getTypedPercent();
    assertTrue(percent >= 0.0 && percent <= 100.0);
  }

  // Tests null node passed to check throws exception
  @Test(expected = NullPointerException.class)
  public void testCheck_nullNode_throwsException() {
    TypeCheck typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    typeCheck.check(null, false);
  }

  // Tests accessing nonexistent property on record type
  @Test
  public void testVisitGetProp_inexistentProperty_reportsInexistentProperty() {
    String js = "/** @type {{a: number}} */ var obj = {a: 1}; var y = obj.b;";
    check(js, TypeCheck.INEXISTENT_PROPERTY);
  }

  // Tests implementing non-interface constructor
  @Test
  public void testVisitFunction_implementNonInterface_reportsBadImplementedType() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "/** @constructor\n * @implements {Foo} */ function Bar() {}";
    check(js, TypeCheck.BAD_IMPLEMENTED_TYPE);
  }

  // Tests missing interface method implementation
  @Test
  public void testVisitFunction_unimplementedInterfaceMethod_reportsInterfaceMethodNotImplemented() {
    String js = "/** @interface */ function Foo() {}\n" +
                "Foo.prototype.bar = function() {};\n" +
                "/** @constructor\n * @implements {Foo} */ function Bar() {}";
    check(js, TypeCheck.INTERFACE_METHOD_NOT_IMPLEMENTED);
  }

  // Tests unknown method override
  @Test
  public void testVisitAssign_unknownOverride_reportsUnknownOverride() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "/** @override */ Foo.prototype.bar = function() {};";
    check(js, TypeCheck.UNKNOWN_OVERRIDE);
  }

  // Tests constructor called as function
  @Test
  public void testVisitCall_constructorCalledAsFunction_reportsConstructorNotCallable() {
    String js = "/** @constructor */ function Foo() {}\nFoo();";
    check(js, TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  // Tests accessing inexistent enum element
  @Test
  public void testVisitGetProp_inexistentEnumElement_reportsInexistentEnumElement() {
    String js = "/** @enum {number} */ var E = { A: 1 }; var x = E.B;";
    check(js, TypeCheck.INEXISTENT_ENUM_ELEMENT);
  }

  // Tests overriding prototype with primitive value
  @Test
  public void testVisitAssign_overridePrototypeWithNonObject_reportsOverridingPrototypeWithNonObject() {
    String js = "/** @constructor */ function Foo() {}\nFoo.prototype = 1;";
    check(js, TypeCheck.OVERRIDING_PROTOTYPE_WITH_NON_OBJECT);
  }

  // Tests illegal property access on null
  @Test
  public void testVisitGetProp_accessOnNull_reportsIllegalPropertyAccess() {
    String js = "/** @type {null} */ var x = null; var y = x.foo;";
    check(js, TypeCheck.ILLEGAL_PROPERTY_ACCESS);
  }
}