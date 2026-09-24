package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
  }

  private Scope parseAndCreateGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private Scope parseAndCreateLocalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node firstFn = findFirstFunctionNode(root);
    assertNotNull(firstFn);
    return scopeCreator.createScope(firstFn, globalScope);
  }

  private Node findFirstFunctionNode(Node n) {
    if (n.getType() == Token.FUNCTION) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node fn = findFirstFunctionNode(child);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  // Tests initial scope containing standard native bindings
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Node root = compiler.parseTestCode("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertTrue(scope.isDeclared("Object", false));
    assertTrue(scope.isDeclared("Array", false));
    assertTrue(scope.isDeclared("Function", false));
    assertTrue(scope.isDeclared("Date", false));
    assertTrue(scope.isDeclared("RegExp", false));
    assertTrue(scope.isDeclared("undefined", false));
    assertTrue(scope.isDeclared("ActiveXObject", false));
  }

  // Tests global variable declaration with @type annotation
  @Test
  public void testCreateScope_globalVarWithExplicitType() {
    String js = "/** @type {number} */ var x = 10;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("x", false));
    Scope.Var varX = scope.getVar("x");
    assertNotNull(varX);
    assertNotNull(varX.getType());
    assertTrue(varX.getType().isNumberValueType());
    assertFalse(varX.isTypeInferred());
  }

  // Tests global function declaration and parameter inference
  @Test
  public void testCreateScope_functionDeclarationWithJSDoc() {
    String js = "/**\n"
        + " * @param {string} a\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function foo(a) { return true; }";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("foo", false));
    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar.getType());
    assertTrue(fooVar.getType().isFunctionType());

    FunctionType fnType = (FunctionType) fooVar.getType();
    assertTrue(fnType.getReturnType().isBooleanValueType());
  }

  // Tests local scope creation and parameter slot creation
  @Test
  public void testCreateScope_localScopeParameters() {
    String js = "/** @param {number} x\n @param {string} y */\n"
        + "function bar(x, y) { var z = x; }";
    Scope localScope = parseAndCreateLocalScope(js);

    assertTrue(localScope.isDeclared("x", false));
    assertTrue(localScope.isDeclared("y", false));
    assertTrue(localScope.isDeclared("z", false));

    Scope.Var varX = localScope.getVar("x");
    assertTrue(varX.getType().isNumberValueType());
    Scope.Var varY = localScope.getVar("y");
    assertTrue(varY.getType().isStringValueType());
  }

  // Tests catch block scope and variable declaration
  @Test
  public void testCreateScope_catchBlockInLocalScope() {
    String js = "function testCatch() { try { var a = 1; } catch (err) { var b = 2; } }";
    Scope localScope = parseAndCreateLocalScope(js);

    assertTrue(localScope.isDeclared("err", false));
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
  }

  // Tests constructor declaration and prototype slot setup
  @Test
  public void testCreateScope_constructorDeclaration() {
    String js = "/** @constructor */ function Person() {}";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("Person", false));
    assertTrue(scope.isDeclared("Person.prototype", false));

    Scope.Var personVar = scope.getVar("Person");
    assertTrue(personVar.getType().isConstructor());
  }

  // Tests enum definition in global scope
  @Test
  public void testCreateScope_enumDefinition() {
    String js = "/** @enum {number} */ var Severity = { HIGH: 1, LOW: 2 };";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("Severity", false));
    Scope.Var enumVar = scope.getVar("Severity");
    assertNotNull(enumVar.getType());
    assertTrue(enumVar.getType().isEnumType());

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElementsType().isNumberValueType());
    assertTrue(enumType.hasOwnProperty("HIGH"));
    assertTrue(enumType.hasOwnProperty("LOW"));
  }

  // Tests typedef definition in global scope
  @Test
  public void testCreateScope_typedefDefinition() {
    String js = "/** @typedef {Array.<string>} */ var StringList;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("StringList", false));
    JSType declaredType = compiler.getTypeRegistry().getType("StringList");
    assertNotNull(declaredType);
    assertTrue(declaredType.isArrayType());
  }

  // Tests namespaced function assignment
  @Test
  public void testCreateScope_namespacedFunctionAssignment() {
    String js = "var ns = {};\n"
        + "/** @param {number} x */\n"
        + "ns.func = function(x) {};";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.func", false));

    Scope.Var funcVar = scope.getVar("ns.func");
    assertNotNull(funcVar.getType());
    assertTrue(funcVar.getType().isFunctionType());
  }

  // Tests object literal casting support
  @Test
  public void testCreateScope_objectLiteralCast() {
    String js = "/** @constructor */ function Options() {}\n"
        + "goog.reflect.object(Options, { key: 'val' });";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("Options", false));
  }

  // Tests stub property declaration on object
  @Test
  public void testCreateScope_stubPropertyDeclaration() {
    String js = "var obj = {}; obj.stubProperty;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("obj", false));
    assertTrue(scope.isDeclared("obj.stubProperty", false));
  }

  // Tests function declarations and namespaced functions inside local scopes (Closure 150 regression)
  @Test
  public void testCreateScope_functionDeclarationInsideLocalScope() {
    String js = "function outer() {\n"
        + "  /** @param {number} a */\n"
        + "  function inner(a) {}\n"
        + "  inner(1);\n"
        + "}";
    Scope localScope = parseAndCreateLocalScope(js);

    assertTrue(localScope.isDeclared("inner", false));
    Scope.Var innerVar = localScope.getVar("inner");
    assertNotNull(innerVar.getType());
    assertTrue(innerVar.getType().isFunctionType());
  }

  // Tests literal types attachment
  @Test
  public void testCreateScope_literalTypesAttached() {
    String js = "var n = null; var v = void 0; var b = true; var s = 'test'; var num = 123; var r = /abc/;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("n", false));
    assertTrue(scope.isDeclared("v", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("s", false));
    assertTrue(scope.isDeclared("num", false));
    assertTrue(scope.isDeclared("r", false));
  }

  // Tests multiple variables declared in a single var statement
  @Test
  public void testCreateScope_multipleVarDeclaration() {
    String js = "var a = 1, b = 2, c = 3;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("a", false));
    assertTrue(scope.isDeclared("b", false));
    assertTrue(scope.isDeclared("c", false));
  }

  // Tests prototype property assignment on constructor
  @Test
  public void testCreateScope_prototypePropertyAssignment() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "/** @type {string} */ Foo.prototype.name = 'foo';";
    Scope scope = parseAndCreateGlobalScope(js);

    assertTrue(scope.isDeclared("Foo", false));
    assertTrue(scope.isDeclared("Foo.prototype.name", false));
    Scope.Var propVar = scope.getVar("Foo.prototype.name");
    assertNotNull(propVar.getType());
    assertTrue(propVar.getType().isStringType());
  }
}