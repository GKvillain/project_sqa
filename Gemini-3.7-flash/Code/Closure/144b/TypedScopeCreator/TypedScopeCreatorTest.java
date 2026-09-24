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
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Node parse(String js) {
    return compiler.parseTestCode(js);
  }

  private Scope createGlobalScope(String js) {
    Node root = parse(js);
    return scopeCreator.createScope(root, null);
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

  // Tests that native types are initialized in the initial scope
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Node root = parse("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  // Tests global scope creation with simple variable declaration
  @Test
  public void testCreateScope_globalVariableDeclaration() {
    Scope scope = createGlobalScope("var x = 10;");

    assertTrue(scope.isDeclared("x", false));
    Scope.Var varX = scope.getVar("x");
    assertNotNull(varX);
    assertTrue(varX.isTypeInferred());
  }

  // Tests typed variable declaration via JSDoc
  @Test
  public void testCreateScope_typedVariableDeclaration() {
    Scope scope = createGlobalScope("/** @type {number} */ var x = 10;");

    Scope.Var varX = scope.getVar("x");
    assertNotNull(varX);
    assertFalse(varX.isTypeInferred());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        varX.getType());
  }

  // Tests constructor function declaration and prototype registration
  @Test
  public void testCreateScope_constructorDeclaration() {
    Scope scope = createGlobalScope("/** @constructor */ function Foo() {}");

    Scope.Var varFoo = scope.getVar("Foo");
    assertNotNull(varFoo);
    assertTrue(varFoo.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) varFoo.getType();
    assertTrue(fnType.isConstructor());
    assertTrue(scope.isDeclared("Foo.prototype", false));
  }

  // Tests function with return type and param annotations
  @Test
  public void testCreateScope_functionReturnAndParamTypes() {
    Scope scope = createGlobalScope(
        "/** @param {string} a\n" +
        " *  @return {number}\n" +
        " */\n" +
        "function foo(a) { return a.length; }");

    Scope.Var varFoo = scope.getVar("foo");
    assertNotNull(varFoo);
    assertTrue(varFoo.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) varFoo.getType();
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        fnType.getReturnType());
  }

  // Tests local scope creation and parameter bindings
  @Test
  public void testCreateScope_localScopeParameters() {
    Node root = parse("/** @param {string} a */ function foo(a) { var b = 1; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "foo");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
    assertFalse(localScope.isDeclared("foo", false));
  }

  // Tests enum declaration and element registration
  @Test
  public void testCreateScope_enumDeclaration() {
    Scope scope = createGlobalScope(
        "/** @enum {number} */ var Status = { OK: 1, ERROR: 2 };");

    Scope.Var varStatus = scope.getVar("Status");
    assertNotNull(varStatus);
    assertTrue(varStatus.getType() instanceof EnumType);
    EnumType enumType = (EnumType) varStatus.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("ERROR"));
  }

  // Tests enum initialization error when not object literal or enum
  @Test
  public void testCreateScope_invalidEnumInitializerReportsWarning() {
    createGlobalScope("/** @enum {number} */ var Status = 123;");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        TypedScopeCreator.ENUM_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests duplicate key warning in enum declaration
  @Test
  public void testCreateScope_duplicateEnumKeyReportsWarning() {
    createGlobalScope(
        "/** @enum {number} */ var Status = { DUP: 1, DUP: 2 };");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_DUP.key, compiler.getWarnings()[0].getType().key);
  }

  // Tests non-constant key warning in enum declaration
  @Test
  public void testCreateScope_nonConstantEnumKeyReportsWarning() {
    createGlobalScope(
        "/** @enum {number} */ var Status = { invalidKey: 1 };");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        TypeCheck.ENUM_NOT_CONSTANT.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests typedef declaration and type registry registration
  @Test
  public void testCreateScope_typedefDeclaration() {
    createGlobalScope(
        "/** @typedef {(string|number)} */ var StringOrNum;");

    JSType declaredType = compiler.getTypeRegistry().getType("StringOrNum");
    assertNotNull(declaredType);
    assertTrue(declaredType.isUnionType());
  }

  // Tests prototype method assignment and context inference
  @Test
  public void testCreateScope_prototypeMethodAssignment() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Bar() {}\n" +
        "/** @return {string} */ Bar.prototype.getName = function() { return ''; };");

    Scope.Var varBar = scope.getVar("Bar");
    assertNotNull(varBar);
    FunctionType ctor = (FunctionType) varBar.getType();
    ObjectType proto = ctor.getPrototype();
    assertTrue(proto.hasProperty("getName"));
    assertTrue(proto.getPropertyType("getName") instanceof FunctionType);
  }

  // Tests property declaration via this type in constructor
  @Test
  public void testCreateScope_constructorThisPropertyDeclaration() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Baz() {\n" +
        "  /** @type {number} */ this.age = 0;\n" +
        "}");

    Scope.Var varBaz = scope.getVar("Baz");
    assertNotNull(varBaz);
    FunctionType ctor = (FunctionType) varBaz.getType();
    ObjectType instanceType = ctor.getInstanceType();
    assertTrue(instanceType.hasProperty("age"));
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        instanceType.getPropertyType("age"));
  }

  // Tests constructor alias creates proper function type binding
  @Test
  public void testCreateScope_constructorAlias() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Original() {}\n" +
        "var Alias = Original;");

    Scope.Var varAlias = scope.getVar("Alias");
    assertNotNull(varAlias);
    assertTrue(varAlias.getType() instanceof FunctionType);
    assertTrue(((FunctionType) varAlias.getType()).isConstructor());
  }

  // Tests catch block scope binding in local scope
  @Test
  public void testCreateScope_catchBlockInLocalScope() {
    Node root = parse("function testCatch() { try {} catch (e) { var x = e; } }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testCatch");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("e", false));
    assertTrue(localScope.isDeclared("x", false));
  }

  // Tests stub property declaration resolved to unknown type
  @Test
  public void testCreateScope_stubPropertyDeclaration() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "ns.stubProp;");

    Scope.Var varNs = scope.getVar("ns");
    assertNotNull(varNs);
    assertTrue(scope.isDeclared("ns.stubProp", false));
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE),
        scope.getVar("ns.stubProp").getType());
  }

  // Tests multiple var declaration with JSDoc warning
  @Test
  public void testCreateScope_multipleVarDeclarationWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(
        TypeCheck.MULTIPLE_VAR_DEF.key,
        compiler.getWarnings()[0].getType().key);
  }
}