package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckTest {

  private Compiler compiler;
  private TypeCheck typeCheck;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private void check(String js) {
    check("", js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private void check(String externs, String js) {
    check(externs, js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private void check(String externs, String js, CheckLevel reportMissingOverride, CheckLevel reportUnknownTypes) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node externsRoot = compiler.parseTestCode(externs);
    Node jsRoot = compiler.parseTestCode(js);
    Node parent = new Node(Token.BLOCK, externsRoot, jsRoot);

    typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        reportMissingOverride,
        reportUnknownTypes);

    typeCheck.processForTesting(externsRoot, jsRoot);
  }

  private boolean hasWarning(DiagnosticType type) {
    for (JSError warning : compiler.getWarnings()) {
      if (warning.getType() == type) {
        return true;
      }
    }
    return false;
  }

  private boolean hasError(DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == type) {
        return true;
      }
    }
    return false;
  }

  // Tests valid JS code without type errors
  @Test
  public void testProcess_validCode_noErrorsOrWarnings() {
    check("var x = 10; var y = 20; var z = x + y;");
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests delete operator with invalid non-reference operand
  @Test
  public void testVisit_badDeleteOperand_reportsBadDeleteWarning() {
    check("delete 42;");
    assertTrue(hasWarning(TypeCheck.BAD_DELETE));
  }

  // Tests calling a non-callable expression
  @Test
  public void testVisit_notCallableExpression_reportsNotCallableWarning() {
    check("var x = 123; x();");
    assertTrue(hasWarning(TypeCheck.NOT_CALLABLE));
  }

  // Tests invoking constructor without new keyword
  @Test
  public void testVisit_constructorCalledWithoutNew_reportsConstructorNotCallable() {
    check("/** @constructor */ function Foo() {} Foo();");
    assertTrue(hasWarning(TypeCheck.CONSTRUCTOR_NOT_CALLABLE));
  }

  // Tests wrong argument count when invoking a function
  @Test
  public void testVisit_wrongArgumentCount_reportsWrongArgumentCountWarning() {
    check("/** @param {number} a\n * @param {number} b */ function add(a, b) {} add(1);");
    assertTrue(hasWarning(TypeCheck.WRONG_ARGUMENT_COUNT));
  }

  // Tests deterministic equality test between incompatible types
  @Test
  public void testVisit_deterministicEquality_reportsDeterministicTest() {
    check("var x = 1 === '1';");
    assertTrue(hasWarning(TypeCheck.DETERMINISTIC_TEST_NO_RESULT) || hasWarning(TypeCheck.DETERMINISTIC_TEST));
  }

  // Tests bitwise operator applied to non-int32 context
  @Test
  public void testVisit_badTypeForBitOperation_reportsBitOperationWarning() {
    check("var x = ~'hello';");
    assertTrue(hasWarning(TypeCheck.BIT_OPERATION));
  }

  // Tests constructor extending an interface
  @Test
  public void testVisit_conflictingExtendedType_reportsConflictingExtendedType() {
    check(
        "/** @interface */ function AnInterface() {}\n" +
        "/** @constructor\n * @extends {AnInterface} */ function AClass() {}");
    assertTrue(hasError(TypeCheck.CONFLICTING_EXTENDED_TYPE) || hasWarning(TypeCheck.CONFLICTING_EXTENDED_TYPE));
  }

  // Tests class implementing a non-interface constructor
  @Test
  public void testVisit_implementsNonInterface_reportsBadImplementedType() {
    check(
        "/** @constructor */ function BaseClass() {}\n" +
        "/** @constructor\n * @implements {BaseClass} */ function SubClass() {}");
    assertTrue(hasWarning(TypeCheck.BAD_IMPLEMENTED_TYPE));
  }

  // Tests interface property mismatch when implemented in class
  @Test
  public void testVisit_hiddenInterfacePropertyMismatch_reportsMismatchWarning() {
    check("",
        "/** @interface */ function FooInterface() {}\n" +
        "/** @type {number} */ FooInterface.prototype.prop;\n" +
        "/** @constructor\n * @implements {FooInterface} */ function FooImpl() {}\n" +
        "/** @type {string} */ FooImpl.prototype.prop = 'bad';",
        CheckLevel.WARNING, CheckLevel.OFF);
    assertTrue(hasWarning(TypeCheck.HIDDEN_INTERFACE_PROPERTY_MISMATCH));
  }

  // Tests @override annotation on a property not present in superclass or interface
  @Test
  public void testVisit_unknownOverride_reportsUnknownOverrideWarning() {
    check("",
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor\n * @extends {Super} */ function Sub() {}\n" +
        "/** @override */ Sub.prototype.nonExistent = function() {};",
        CheckLevel.WARNING, CheckLevel.OFF);
    assertTrue(hasWarning(TypeCheck.UNKNOWN_OVERRIDE));
  }

  // Tests interface member function with a non-empty body
  @Test
  public void testVisit_interfaceFunctionNotEmpty_reportsWarning() {
    check(
        "/** @interface */ function Intf() {}\n" +
        "Intf.prototype.method = function() { return 1; };");
    assertTrue(hasWarning(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY));
  }

  // Tests instantiated non-constructor
  @Test
  public void testVisit_instantiateNonConstructor_reportsNotAConstructor() {
    check("var x = 123; new x();");
    assertTrue(hasWarning(TypeCheck.NOT_A_CONSTRUCTOR));
  }

  // Tests getTypedPercent calculation
  @Test
  public void testGetTypedPercent_partiallyTypedCode_returnsPercentage() {
    check("/** @type {number} */ var a = 1; var b = 2;");
    double percent = typeCheck.getTypedPercent();
    assertTrue(percent >= 0.0 && percent <= 100.0);
  }

  // Tests reporting unknown expressions when enabled
  @Test
  public void testVisit_reportUnknownTypes_reportsWarning() {
    check("", "function f(x) { return x.foo(); }", CheckLevel.OFF, CheckLevel.WARNING);
    assertTrue(hasWarning(TypeCheck.UNKNOWN_EXPR_TYPE));
  }

  // Tests accessing an inexistent property on a known object type
  @Test
  public void testVisit_inexistentProperty_reportsInexistentProperty() {
    check("/** @type {{foo: number}} */ var obj = {foo: 1}; var val = obj.bar;");
    assertTrue(hasWarning(TypeCheck.INEXISTENT_PROPERTY));
  }

  // Tests accessing property on null or undefined
  @Test
  public void testVisit_illegalPropertyAccess_reportsIllegalPropertyAccess() {
    check("/** @type {null} */ var n = null; var val = n.foo;");
    assertTrue(hasWarning(TypeCheck.ILLEGAL_PROPERTY_ACCESS));
  }

  // Tests interface attempting to implement another interface or class
  @Test
  public void testVisit_interfaceImplements_reportsInterfacesCannotImplement() {
    check(
        "/** @interface */ function SuperIntf() {}\n" +
        "/** @interface\n * @implements {SuperIntf} */ function SubIntf() {}");
    assertTrue(hasWarning(TypeCheck.INTERFACES_CANNOT_IMPLEMENT));
  }

  // Tests interface extending a non-interface constructor
  @Test
  public void testVisit_interfaceExtendsClass_reportsInterfaceCanOnlyExtendInterfaces() {
    check(
        "/** @constructor */ function SuperClass() {}\n" +
        "/** @interface\n * @extends {SuperClass} */ function SubIntf() {}");
    assertTrue(hasWarning(TypeCheck.INTERFACE_CAN_ONLY_EXTEND_INTERFACES));
  }

  // Tests duplicate object keys in object literal
  @Test
  public void testVisit_duplicateObjectKey_reportsDuplicateObjectKey() {
    check("var obj = { a: 1, a: 2 };");
    assertTrue(hasWarning(TypeCheck.DUPLICATE_OBJECT_KEY));
  }

  // Tests enum initialization with non-constant expressions
  @Test
  public void testVisit_enumNotConstant_reportsEnumNotConstant() {
    check("var x = 1; /** @enum {number} */ var MyEnum = { A: x };");
    assertTrue(hasWarning(TypeCheck.ENUM_NOT_CONSTANT));
  }
}