package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TypedScopeCreatorTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new GoogleCodingConvention());
    compiler.initOptions(options);
  }

  private Node parseAndGetRoot(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return root;
  }

  private Node findNode(Node root, int tokenType) {
    if (root.getType() == tokenType) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findNode(child, tokenType);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private Node findFunctionNode(Node root, String name) {
    if (root.getType() == Token.FUNCTION) {
      Node nameNode = root.getFirstChild();
      if (nameNode != null && name.equals(nameNode.getString())) {
        return root;
      }
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFunctionNode(child, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // Tests creation of initial scope and presence of native types
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Node root = parseAndGetRoot("");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope initialScope = creator.createInitialScope(root);

    assertNotNull(initialScope);
    assertTrue(initialScope.isGlobal());
    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("Function"));
    assertNotNull(initialScope.getVar("String"));
    assertNotNull(initialScope.getVar("Number"));
    assertNotNull(initialScope.getVar("Boolean"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("Error"));
    assertNotNull(initialScope.getVar("undefined"));
  }

  // Tests global variable declaration with primitive types
  @Test
  public void testCreateScope_globalVariables_inferredAndDeclared() {
    String js = "var a = 1; var b = 'hello'; var c = true; var d = null;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isGlobal());
    assertTrue(globalScope.isDeclared("a", false));
    assertTrue(globalScope.isDeclared("b", false));
    assertTrue(globalScope.isDeclared("c", false));
    assertTrue(globalScope.isDeclared("d", false));
  }

  // Tests constructor declaration and prototype property attachment in global scope
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototypeAndInstance() {
    String js = "/** @constructor */ function Foo() {}";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("Foo", false));
    assertTrue(globalScope.isDeclared("Foo.prototype", false));
    Scope.Var fooVar = globalScope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());
  }

  // Tests enum declaration and enum elements registration
  @Test
  public void testCreateScope_enumDeclaration_registersEnumType() {
    String js = "/** @enum {number} */ var Color = { RED: 1, GREEN: 2, BLUE: 3 };";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("Color", false));
    Scope.Var colorVar = globalScope.getVar("Color");
    assertNotNull(colorVar);
    assertTrue(colorVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) colorVar.getType();
    assertTrue(enumType.hasOwnProperty("RED"));
    assertTrue(enumType.hasOwnProperty("GREEN"));
    assertTrue(enumType.hasOwnProperty("BLUE"));
  }

  // Tests warning on duplicate enum elements
  @Test
  public void testCreateScope_duplicateEnumKeys_reportsWarning() {
    String js = "/** @enum {string} */ var Direction = { NORTH: 'N', NORTH: 'DUPLICATE' };";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests warning on non-constant enum keys
  @Test
  public void testCreateScope_invalidEnumKey_reportsWarning() {
    String js = "/** @enum {number} */ var Numbers = { invalidKey: 1 };";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests typedef creation and type registry declaration
  @Test
  public void testCreateScope_typedef_declaresTypeInRegistry() {
    String js = "/** @typedef {(string|number)} */ var MyType;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertNotNull(globalScope);
    JSType declaredType = compiler.getTypeRegistry().getType("MyType");
    assertNotNull(declaredType);
    assertTrue(declaredType.isUnionType());
  }

  // Tests malformed typedef reporting warning
  @Test
  public void testCreateScope_malformedTypedef_reportsWarning() {
    String js = "/** @typedef */ var BadTypedef;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests local scope creation and parameter type resolution
  @Test
  public void testCreateScope_localScope_resolvesParameters() {
    String js = "/** @param {number} x\n * @param {string} y\n */\n"
        + "function testFn(x, y) { var z = x; }";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testFn");
    assertNotNull(fnNode);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertTrue(localScope.isLocal());
    assertEquals(globalScope, localScope.getParent());
    assertTrue(localScope.isDeclared("x", false));
    assertTrue(localScope.isDeclared("y", false));
    assertTrue(localScope.isDeclared("z", false));

    Scope.Var varX = localScope.getVar("x");
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), varX.getType());
    Scope.Var varY = localScope.getVar("y");
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), varY.getType());
  }

  // Tests bleeding function name inside local scope
  @Test
  public void testCreateScope_bleedingFunction_declaredInLocalScope() {
    String js = "var outer = function inner() { var a = 1; };";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "inner");
    assertNotNull(fnNode);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("inner", false));
    assertFalse(globalScope.isDeclared("inner", false));
  }

  // Tests catch variable definition in local scope
  @Test
  public void testCreateScope_catchBlock_declaresCatchVariable() {
    String js = "function f() { try { var a = 1; } catch (err) { var b = err; } }";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "f");
    assertNotNull(fnNode);
    Scope localScope = creator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("err", false));
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
  }

  // Tests stub property declaration in externs / stub declarations
  @Test
  public void testCreateScope_stubPropertyDeclaration_resolved() {
    String js = "var ns = {}; ns.prop;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("ns", false));
    assertTrue(globalScope.isDeclared("ns.prop", false));
  }

  // Tests method declaration on constructor prototype
  @Test
  public void testCreateScope_prototypeMethodAssignment_setsType() {
    String js = "/** @constructor */ function Car() {}\n"
        + "/** @param {number} speed */ Car.prototype.drive = function(speed) {};";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("Car.prototype.drive", false));
    Scope.Var driveVar = globalScope.getVar("Car.prototype.drive");
    assertNotNull(driveVar);
    assertTrue(driveVar.getType() instanceof FunctionType);
  }

  // Tests multiple var declarations with jsdoc warning
  @Test
  public void testCreateScope_multipleVarWithJsDoc_reportsWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    creator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests function with @this annotation collects properties
  @Test
  public void testCreateScope_functionWithThisType_collectsProperties() {
    String js = "/** @constructor */ function Widget() {}\n"
        + "/** @this {Widget} */ function initWidget() {\n"
        + "  /** @type {string} */ this.name = 'test';\n"
        + "}";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var widgetVar = globalScope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType widgetCtor = (FunctionType) widgetVar.getType();
    ObjectType instanceType = widgetCtor.getInstanceType();
    assertTrue(instanceType.hasOwnProperty("name"));
  }

  // Tests local function declaration scoping inside another function
  @Test
  public void testCreateScope_innerFunctionDeclaration_scopedLocally() {
    String js = "function outer() {\n"
        + "  /** @param {number} x */\n"
        + "  function inner(x) {}\n"
        + "}";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Node outerFn = findFunctionNode(root, "outer");
    assertNotNull(outerFn);
    Scope localScope = creator.createScope(outerFn, globalScope);

    assertTrue(localScope.isDeclared("inner", false));
    assertFalse(globalScope.isDeclared("inner", false));
  }

  // Tests interface declaration and registration
  @Test
  public void testCreateScope_interfaceDeclaration_registersInterface() {
    String js = "/** @interface */ function Disposable() {}\n"
        + "Disposable.prototype.dispose = function() {};";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("Disposable", false));
    Scope.Var dispVar = globalScope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());
  }

  // Tests constructor properties assigned through this in constructor body
  @Test
  public void testCreateScope_constructorPropertiesViaThis_collectedOnInstance() {
    String js = "/** @constructor */ function Person(name) {\n"
        + "  /** @type {string} */ this.name = name;\n"
        + "  /** @type {number} */ this.age = 0;\n"
        + "}";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var personVar = globalScope.getVar("Person");
    assertNotNull(personVar);
    FunctionType personCtor = (FunctionType) personVar.getType();
    ObjectType instanceType = personCtor.getInstanceType();
    assertTrue(instanceType.hasOwnProperty("name"));
    assertTrue(instanceType.hasOwnProperty("age"));
  }

  // Tests inheritance with @extends
  @Test
  public void testCreateScope_subclassInheritance_linksSuperType() {
    String js = "/** @constructor */ function Animal() {}\n"
        + "/** @constructor\n * @extends {Animal} */ function Dog() {}";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var dogVar = globalScope.getVar("Dog");
    assertNotNull(dogVar);
    FunctionType dogCtor = (FunctionType) dogVar.getType();
    FunctionType superCtor = dogCtor.getSuperClassConstructor();
    assertNotNull(superCtor);
    assertEquals("Animal", superCtor.getReferenceName());
  }

  // Tests lends annotation on object literals
  @Test
  public void testCreateScope_lendsAnnotation_attachesPropertiesToTarget() {
    String js = "/** @constructor */ function Point() {}\n"
        + "var mixin = /** @lends {Point.prototype} */ ({\n"
        + "  /** @return {number} */ getX: function() { return 0; }\n"
        + "});";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var pointVar = globalScope.getVar("Point");
    assertNotNull(pointVar);
    FunctionType pointCtor = (FunctionType) pointVar.getType();
    assertTrue(pointCtor.getPrototype().hasOwnProperty("getX"));
  }

  // Tests nested namespace property declarations
  @Test
  public void testCreateScope_nestedNamespaceProperties_declaredInGlobalScope() {
    String js = "var app = {}; app.models = {}; app.models.User = function() {};";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("app", false));
    assertTrue(globalScope.isDeclared("app.models", false));
    assertTrue(globalScope.isDeclared("app.models.User", false));
  }

  // Tests function return type annotation and optional/var_args parameters
  @Test
  public void testCreateScope_optionalAndVarArgsParameters_resolvesCorrectTypes() {
    String js = "/**\n"
        + " * @param {string} req\n"
        + " * @param {number=} opt_num\n"
        + " * @param {...boolean} var_args\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function fullFn(req, opt_num, var_args) { return true; }";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var fnVar = globalScope.getVar("fullFn");
    assertNotNull(fnVar);
    FunctionType fnType = (FunctionType) fnVar.getType();
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), fnType.getReturnType());

    Node fnNode = findFunctionNode(root, "fullFn");
    assertNotNull(fnNode);
    Scope localScope = creator.createScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("req", false));
    assertTrue(localScope.isDeclared("opt_num", false));
    assertTrue(localScope.isDeclared("var_args", false));
  }

  // Tests constant variable annotation
  @Test
  public void testCreateScope_constVariable_marksVarAsConst() {
    String js = "/** @const */ var MAX_COUNT = 100;";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    Scope.Var maxVar = globalScope.getVar("MAX_COUNT");
    assertNotNull(maxVar);
    assertTrue(maxVar.isConst());
  }

  // Tests enum declared as a property on an existing namespace
  @Test
  public void testCreateScope_namespacedEnum_registersCorrectly() {
    String js = "var ns = {};\n"
        + "/** @enum {number} */ ns.Status = { OK: 200, NOT_FOUND: 404 };";
    Node root = parseAndGetRoot(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);

    assertTrue(globalScope.isDeclared("ns.Status", false));
    Scope.Var statusVar = globalScope.getVar("ns.Status");
    assertNotNull(statusVar);
    assertTrue(statusVar.getType() instanceof EnumType);
    EnumType statusEnum = (EnumType) statusVar.getType();
    assertTrue(statusEnum.hasOwnProperty("OK"));
    assertTrue(statusEnum.hasOwnProperty("NOT_FOUND"));
  }
}