package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private TypeCheck typeCheck;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private void testTypes(String js) {
    testTypes("", js);
  }

  private void testTypes(String externs, String js) {
    testTypes(externs, js, CheckLevel.OFF);
  }

  private void testTypes(String externs, String js, CheckLevel missingOverrideLevel) {
    Node externsNode = compiler.parseTestCode(externs);
    Node jsNode = compiler.parseTestCode(js);
    Node externsAndJs = new Node(Token.BLOCK, externsNode, jsNode);
    typeCheck = new TypeCheck(
        compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        missingOverrideLevel,
        CheckLevel.OFF);
    typeCheck.processForTesting(externsNode, jsNode);
  }

  // Tests interface extending non-existent type does not crash (Bug 2b regression)
  @Test
  public void testCheckInterfaceConflictProperties_nonExistentInterface_doesNotCrash() {
    String js = "/** @interface */ function I1() {}\n"
        + "/** @interface\n * @extends {I1}\n * @extends {nonExistentInterface} */\n"
        + "function I2() {}\n";
    testTypes(js);
  }

  // Tests extending multiple interfaces with conflicting property types
  @Test
  public void testCheckInterfaceConflictProperties_incompatibleExtendedPropertyType_reportsWarning() {
    String js = "/** @interface */ function I1() {}\n"
        + "/** @type {number} */ I1.prototype.prop;\n"
        + "/** @interface */ function I2() {}\n"
        + "/** @type {string} */ I2.prototype.prop;\n"
        + "/** @interface\n * @extends {I1}\n * @extends {I2} */\n"
        + "function I3() {}\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.INCOMPATIBLE_EXTENDED_PROPERTY_TYPE, compiler.getWarnings()[0].getType());
  }

  // Tests invocation of non-callable expression
  @Test
  public void testVisitCall_notCallable_reportsWarning() {
    String js = "var x = 123; x();";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.NOT_CALLABLE, compiler.getWarnings()[0].getType());
  }

  // Tests constructor called directly without new keyword
  @Test
  public void testVisitCall_constructorNotCallable_reportsWarning() {
    String js = "/** @constructor */ function Foo() {} Foo();";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.CONSTRUCTOR_NOT_CALLABLE, compiler.getWarnings()[0].getType());
  }

  // Tests instantiation of a non-constructor
  @Test
  public void testVisitNew_notAConstructor_reportsWarning() {
    String js = "var x = 42; new x();";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.NOT_A_CONSTRUCTOR, compiler.getWarnings()[0].getType());
  }

  // Tests function call with incorrect number of arguments
  @Test
  public void testVisitParameterList_wrongArgumentCount_reportsWarning() {
    String js = "/** @param {number} a\n * @param {number} b */ function f(a, b) {} f(1);";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.WRONG_ARGUMENT_COUNT, compiler.getWarnings()[0].getType());
  }

  // Tests deterministic comparison between incompatible types
  @Test
  public void testVisit_deterministicTest_reportsWarning() {
    String js = "var a = 'hello'; var b = 42; if (a === b) {}";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.DETERMINISTIC_TEST, compiler.getWarnings()[0].getType());
  }

  // Tests bitwise operation applied to non-integer type
  @Test
  public void testVisitBinaryOperator_bitOperationOnString_reportsWarning() {
    String js = "var x = 'abc' ^ 2;";
    testTypes(js);
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests IN operator used with struct instance
  @Test
  public void testVisit_inUsedWithStruct_reportsWarning() {
    String js = "/** @struct @constructor */ function S() { this.x = 1; }\n"
        + "var s = new S();\n"
        + "var res = 'x' in s;\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.IN_USED_WITH_STRUCT, compiler.getWarnings()[0].getType());
  }

  // Tests adding a property to a struct instance after construction
  @Test
  public void testCheckPropCreation_illegalPropertyCreationOnStruct_reportsWarning() {
    String js = "/** @struct @constructor */ function S() { this.x = 1; }\n"
        + "var s = new S();\n"
        + "s.y = 2;\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ILLEGAL_PROPERTY_CREATION, compiler.getWarnings()[0].getType());
  }

  // Tests struct object literal with quoted key
  @Test
  public void testVisitObjLitKey_illegalQuotedKeyInStruct_reportsWarning() {
    String js = "/** @struct */ var obj = {'a': 1};";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ILLEGAL_OBJLIT_KEY, compiler.getWarnings()[0].getType());
  }

  // Tests overriding superclass property with mismatching type
  @Test
  public void testCheckDeclaredPropertyInheritance_hiddenSuperclassPropertyMismatch_reportsWarning() {
    String js = "/** @constructor */ function Super() {}\n"
        + "/** @type {number} */ Super.prototype.foo = 1;\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "/** @override @type {string} */ Sub.prototype.foo = 'bar';\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH, compiler.getWarnings()[0].getType());
  }

  // Tests overriding superclass property without @override annotation
  @Test
  public void testCheckDeclaredPropertyInheritance_missingOverride_reportsWarning() {
    String js = "/** @constructor */ function Super() {}\n"
        + "/** @type {number} */ Super.prototype.foo = 1;\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "/** @type {number} */ Sub.prototype.foo = 2;\n";
    testTypes("", js, CheckLevel.WARNING);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.HIDDEN_SUPERCLASS_PROPERTY, compiler.getWarnings()[0].getType());
  }

  // Tests @override annotation on non-existent superclass property
  @Test
  public void testCheckDeclaredPropertyInheritance_unknownOverride_reportsWarning() {
    String js = "/** @constructor */ function Super() {}\n"
        + "/** @constructor @extends {Super} */ function Sub() {}\n"
        + "/** @override */ Sub.prototype.bar = function() {};\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.UNKNOWN_OVERRIDE, compiler.getWarnings()[0].getType());
  }

  // Tests non-empty function body in interface member declaration
  @Test
  public void testVisitInterfaceGetprop_interfaceFunctionNotEmpty_reportsWarning() {
    String js = "/** @interface */ function Intf() {}\n"
        + "Intf.prototype.method = function() { return 1; };\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY, compiler.getWarnings()[0].getType());
  }

  // Tests calculation of typed percentage
  @Test
  public void testGetTypedPercent_validScript_returnsPositivePercentage() {
    String js = "var x = 1; var y = 2; var z = x + y;";
    testTypes(js);
    assertTrue(typeCheck.getTypedPercent() > 0.0);
  }

  // Tests inconsistent return type in function body
  @Test
  public void testVisitReturn_inconsistentReturnType_reportsWarning() {
    String js = "/** @return {number} */ function f() { return 'string'; }";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.INCONSISTENT_RETURN_TYPE, compiler.getWarnings()[0].getType());
  }

  // Tests unknown typeof operand value
  @Test
  public void testVisit_unknownTypeofValue_reportsWarning() {
    String js = "var x = 1; if (typeof x === 'numb') {}";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.UNKNOWN_TYPEOF_VALUE, compiler.getWarnings()[0].getType());
  }

  // Tests missing interface method implementation in constructor
  @Test
  public void testCheckInterfaceMethod_notImplemented_reportsWarning() {
    String js = "/** @interface */ function I() {}\n"
        + "I.prototype.foo = function() {};\n"
        + "/** @constructor @implements {I} */ function C() {}\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.INTERFACE_METHOD_NOT_IMPLEMENTED, compiler.getWarnings()[0].getType());
  }

  // Tests bracket access on struct instance
  @Test
  public void testVisitGetElem_illegalPropertyAccessOnStruct_reportsWarning() {
    String js = "/** @struct @constructor */ function S() { this.x = 1; }\n"
        + "var s = new S();\n"
        + "var val = s['x'];\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ILLEGAL_PROPERTY_ACCESS, compiler.getWarnings()[0].getType());
  }

  // Tests dot property access on dict instance
  @Test
  public void testVisitGetProp_illegalPropertyAccessOnDict_reportsWarning() {
    String js = "/** @dict @constructor */ function D() {}\n"
        + "var d = new D();\n"
        + "var val = d.x;\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ILLEGAL_PROPERTY_ACCESS, compiler.getWarnings()[0].getType());
  }

  // Tests property access on inexistent property of a known type
  @Test
  public void testVisitGetprop_inexistentProperty_reportsWarning() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "var f = new Foo();\n"
        + "var val = f.nonExistentProp;\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.INEXISTENT_PROPERTY, compiler.getWarnings()[0].getType());
  }

  // Tests interface extending a class instead of an interface
  @Test
  public void testCheckInterfaceExtends_classInsteadOfInterface_reportsWarning() {
    String js = "/** @constructor */ function Cls() {}\n"
        + "/** @interface @extends {Cls} */ function Intf() {}\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.CONFLICTING_EXTENDED_TYPE, compiler.getWarnings()[0].getType());
  }

  // Tests constructor implementing a non-interface class
  @Test
  public void testCheckImplements_notAnInterface_reportsWarning() {
    String js = "/** @constructor */ function Cls() {}\n"
        + "/** @constructor @implements {Cls} */ function Sub() {}\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.CONFLICTING_IMPLEMENTED_TYPE, compiler.getWarnings()[0].getType());
  }

  // Tests deterministic test condition with no possible truthy result
  @Test
  public void testVisit_deterministicTestNoResult_reportsWarning() {
    String js = "var x = null;\n"
        + "if (x) {}\n";
    testTypes(js);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.DETERMINISTIC_TEST_NO_RESULT, compiler.getWarnings()[0].getType());
  }
}