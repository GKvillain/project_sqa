package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private Node findFunctionNode(Node root, String name) {
    if (root.isFunction()) {
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

  // Tests that primitive variable declarations receive expected types
  @Test
  public void testCreateScope_primitiveTypes_declaredCorrectly() {
    String js = "/** @type {number} */ var num = 42;\n"
        + "/** @type {string} */ var str = 'hello';\n"
        + "/** @type {boolean} */ var bool = true;";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("num", false));
    assertEquals("number", scope.getVar("num").getType().toString());
    assertTrue(scope.isDeclared("str", false));
    assertEquals("string", scope.getVar("str").getType().toString());
    assertTrue(scope.isDeclared("bool", false));
    assertEquals("boolean", scope.getVar("bool").getType().toString());
  }

  // Tests function declarations and parameter types in global scope
  @Test
  public void testCreateScope_functionDeclaration_registersFunctionAndParamTypes() {
    String js = "/**\n"
        + " * @param {number} a\n"
        + " * @param {string} b\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function foo(a, b) { return true; }";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("foo", false));
    JSType type = scope.getVar("foo").getType();
    assertTrue(type.isFunctionType());
    FunctionType fnType = type.toMaybeFunctionType();
    assertEquals("boolean", fnType.getReturnType().toString());
    assertEquals(2, fnType.getParametersCount());
  }

  // Tests constructor declaration creates instance and prototype slots
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototypeAndInstance() {
    String js = "/** @constructor */ function Person(name) { this.name = name; }";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("Person", false));
    assertTrue(scope.isDeclared("Person.prototype", false));
    FunctionType ctor = scope.getVar("Person").getType().toMaybeFunctionType();
    assertTrue(ctor.isConstructor());
    assertNotNull(ctor.getInstanceType());
  }

  // Tests const with type cast / known type (Defects4J Closure-17 regression)
  @Test
  public void testCreateScope_constWithKnownType_infersCorrectDeclaredType() {
    String js = "/** @type {number} */ var SOME_INT = 1;\n"
        + "/** @const */ var SOME_UNION = /** @type {string|number} */ (SOME_INT);";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("SOME_UNION", false));
    JSType unionType = scope.getVar("SOME_UNION").getType();
    assertNotNull(unionType);
    assertEquals("(number|string)", unionType.toString());
    assertFalse(scope.getVar("SOME_UNION").isTypeInferred());
  }

  // Tests const with Closure OR idiom (var x = x || {})
  @Test
  public void testCreateScope_constWithOrIdiom_evaluatesCorrectType() {
    String js = "var ns = ns || {};";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("ns", false));
    assertNotNull(scope.getVar("ns").getType());
  }

  // Tests enum declaration and enum elements creation
  @Test
  public void testCreateScope_enumDeclaration_createsEnumTypeAndElements() {
    String js = "/** @enum {string} */ var Color = { RED: 'r', GREEN: 'g', BLUE: 'b' };";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("Color", false));
    JSType type = scope.getVar("Color").getType();
    assertTrue(type instanceof EnumType);
    EnumType enumType = (EnumType) type;
    assertEquals("string", enumType.getElementsType().toString());
    assertTrue(enumType.getElements().contains("RED"));
    assertTrue(enumType.getElements().contains("GREEN"));
    assertTrue(enumType.getElements().contains("BLUE"));
  }

  // Tests enum with invalid non-constant key reports warning
  @Test
  public void testCreateScope_enumInvalidKey_reportsWarning() {
    String js = "/** @enum {number} */ var BadEnum = { 'invalid-key': 1 };";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_NOT_CONSTANT.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests typedef declaration and alias resolution
  @Test
  public void testCreateScope_typedef_createsDeclaredType() {
    String js = "/** @typedef {{x: number, y: number}} */ var Point;\n"
        + "/** @type {Point} */ var p = {x: 1, y: 2};";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("p", false));
    JSType pType = scope.getVar("p").getType();
    assertTrue(pType.isRecordType() || pType.isObjectType());
    assertTrue(pType.toObjectType().hasProperty("x"));
    assertTrue(pType.toObjectType().hasProperty("y"));
  }

  // Tests malformed typedef reports warning
  @Test
  public void testCreateScope_malformedTypedef_reportsWarning() {
    String js = "/** @typedef {NonExistentType} */ var BadTypedef;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_appliesPropertiesToTarget() {
    String js = "/** @constructor */ function Widget() {}\n"
        + "/** @type {Widget} */ var w = new Widget();\n"
        + "var obj = /** @lends {Widget.prototype} */ ({ render: function() {} });";
    Scope scope = createGlobalScope(js);

    assertTrue(scope.isDeclared("Widget", false));
    ObjectType proto = scope.getVar("Widget.prototype").getType().toObjectType();
    assertTrue(proto.hasProperty("render"));
  }

  // Tests @lends on undeclared variable reports warning
  @Test
  public void testCreateScope_unknownLends_reportsWarning() {
    String js = "var obj = /** @lends {NonExistentTarget} */ ({ foo: 1 });";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests @lends on non-object type reports warning
  @Test
  public void testCreateScope_lendsOnNonObject_reportsWarning() {
    String js = "/** @type {number} */ var nonObj = 10;\n"
        + "var obj = /** @lends {nonObj} */ ({ foo: 1 });";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests local scope creation for function parameters and local variables
  @Test
  public void testCreateScope_localScope_declaresLocalVarsAndParameters() {
    String js = "function outer(param1, param2) {\n"
        + "  var localVar = 100;\n"
        + "  return param1 + localVar;\n"
        + "}";
    Node root = compiler.parseTestCode(js);
    scopeCreator = new TypedScopeCreator(compiler);
    Scope global = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "outer");
    assertNotNull(fnNode);
    Scope localScope = scopeCreator.createScope(fnNode, global);

    assertTrue(localScope.isDeclared("param1", false));
    assertTrue(localScope.isDeclared("param2", false));
    assertTrue(localScope.isDeclared("localVar", false));
    assertFalse(localScope.isDeclared("outer", false));
  }

  // Tests catch block defines catch variable slot in local scope
  @Test
  public void testCreateScope_catchBlock_definesCatchSlot() {
    String js = "function testCatch() {\n"
        + "  try { throw 'err'; } catch (e) { var x = e; }\n"
        + "}";
    Node root = compiler.parseTestCode(js);
    scopeCreator = new TypedScopeCreator(compiler);
    Scope global = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testCatch");
    assertNotNull(fnNode);
    Scope localScope = scopeCreator.createScope(fnNode, global);

    assertTrue(localScope.isDeclared("e", false));
    assertTrue(localScope.isDeclared("x", false));
  }

  // Tests initial global scope contains standard built-in native types
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Node root = compiler.parseTestCode("");
    scopeCreator = new TypedScopeCreator(compiler);
    Scope initialScope = scopeCreator.createInitialScope(root);

    assertTrue(initialScope.isDeclared("Object", false));
    assertTrue(initialScope.isDeclared("Function", false));
    assertTrue(initialScope.isDeclared("Array", false));
    assertTrue(initialScope.isDeclared("String", false));
    assertTrue(initialScope.isDeclared("Number", false));
    assertTrue(initialScope.isDeclared("Boolean", false));
    assertTrue(initialScope.isDeclared("Date", false));
    assertTrue(initialScope.isDeclared("RegExp", false));
    assertTrue(initialScope.isDeclared("Error", false));
    assertTrue(initialScope.isDeclared("undefined", false));
    assertTrue(initialScope.isDeclared("ActiveXObject", false));
  }

  // Tests patchGlobalScope removes old variables and re-traverses updated script
  @Test
  public void testPatchGlobalScope_removesOldAndTraversesNewScript() {
    String js1 = "var oldVar = 1;";
    Node scriptRoot = compiler.parseTestCode(js1);
    scopeCreator = new TypedScopeCreator(compiler);
    Scope global = scopeCreator.createScope(scriptRoot, null);
    assertTrue(global.isDeclared("oldVar", false));

    String js2 = "var newVar = 2;";
    Node newScriptRoot = compiler.parseTestCode(js2);
    scopeCreator.patchGlobalScope(global, newScriptRoot);

    assertFalse(global.isDeclared("oldVar", false));
    assertTrue(global.isDeclared("newVar", false));
  }

  // Tests constructor initialization check warning
  @Test
  public void testCreateScope_uninitializedConstructor_reportsWarning() {
    String js = "/** @constructor */ var UninitCtor;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests interface initialization check warning
  @Test
  public void testCreateScope_uninitializedInterface_reportsWarning() {
    String js = "/** @interface */ var UninitIface;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }
}