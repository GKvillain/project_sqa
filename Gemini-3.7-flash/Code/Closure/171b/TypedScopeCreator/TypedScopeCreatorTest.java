package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
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
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return scopeCreator.createScope(root, null);
  }

  // Tests creating initial native scope
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Node root = compiler.parseTestCode("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertTrue(scope.isGlobal());
  }

  // Tests simple global variable declarations
  @Test
  public void testCreateScope_globalVariableDeclaration_infersTypes() {
    String js = "var a = 10; var b = 'hello'; var c = true; var d = null;";
    Scope scope = createGlobalScope(js);

    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), varA.getType());

    Scope.Var varB = scope.getVar("b");
    assertNotNull(varB);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), varB.getType());

    Scope.Var varC = scope.getVar("c");
    assertNotNull(varC);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), varC.getType());

    Scope.Var varD = scope.getVar("d");
    assertNotNull(varD);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE), varD.getType());
  }

  // Tests constructor declaration and prototype creation
  @Test
  public void testCreateScope_constructorDeclaration_createsPrototype() {
    String js = "/** @constructor */ function Foo() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Foo.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests constructor without initialization reports warning
  @Test
  public void testCreateScope_uninitializedConstructor_reportsWarning() {
    String js = "/** @constructor */ var Foo;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  // Tests interface declaration without initialization reports warning
  @Test
  public void testCreateScope_uninitializedInterface_reportsWarning() {
    String js = "/** @interface */ var IFoo;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  // Tests enum declaration with object literal
  @Test
  public void testCreateScope_enumDeclaration_definesElements() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElements().contains("A"));
    assertTrue(enumType.getElements().contains("B"));
  }

  // Tests enum initializer invalid non-object literal reports warning
  @Test
  public void testCreateScope_invalidEnumInitializer_reportsWarning() {
    String js = "/** @enum {number} */ var MyEnum = 123;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER, compiler.getWarnings()[0].getType());
  }

  // Tests typedef declaration and alias resolution
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    String js = "/** @typedef {{x: number, y: string}} */ var Point;";
    Scope scope = createGlobalScope(js);

    JSType pointType = compiler.getTypeRegistry().getType("Point");
    assertNotNull(pointType);
    assertTrue(pointType.isRecordType());
  }

  // Tests local scope creation and parameter typing
  @Test
  public void testCreateScope_localScope_definesParametersAndLocalVars() {
    String js = "/**\n"
        + " * @param {string} x\n"
        + " * @param {number} y\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function testFn(x, y) {\n"
        + "  var z = true;\n"
        + "  return z;\n"
        + "}";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Scope.Var fnVar = globalScope.getVar("testFn");
    assertNotNull(fnVar);
    Node fnNode = fnVar.getNameNode().getParent();

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertFalse(localScope.isGlobal());

    Scope.Var paramX = localScope.getVar("x");
    assertNotNull(paramX);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), paramX.getType());

    Scope.Var paramY = localScope.getVar("y");
    assertNotNull(paramY);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), paramY.getType());

    Scope.Var varZ = localScope.getVar("z");
    assertNotNull(varZ);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), varZ.getType());
  }

  // Tests catch block scope variable definition
  @Test
  public void testCreateScope_catchBlock_definesCatchVar() {
    String js = "function f() { try {} catch (e) { var x = e; } }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Scope.Var fnVar = globalScope.getVar("f");
    Node fnNode = fnVar.getNameNode().getParent();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("x"));
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_lendsPropertiesToType() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = /** @lends {Foo.prototype} */ ({ bar: function() {} });";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    FunctionType fooCtor = fooVar.getType().toMaybeFunctionType();
    ObjectType proto = fooCtor.getPrototype();

    assertTrue(proto.hasProperty("bar"));
  }

  // Tests @lends on undeclared variable reports warning
  @Test
  public void testCreateScope_unknownLends_reportsWarning() {
    String js = "var obj = /** @lends {NonExistentClass.prototype} */ ({ bar: 1 });";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS, compiler.getWarnings()[0].getType());
  }

  // Tests constant variable type inference and propagation
  @Test
  public void testCreateScope_constVariable_inferredCorrectly() {
    String js = "/** @const */ var MY_CONST = 42;";
    Scope scope = createGlobalScope(js);

    Scope.Var constVar = scope.getVar("MY_CONST");
    assertNotNull(constVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), constVar.getType());
    assertFalse(constVar.isTypeInferred());
  }

  // Tests constructor alias declaration
  @Test
  public void testCreateScope_constructorAlias_registersCorrectInstanceType() {
    String js = "/** @constructor */ function Original() {}\n"
        + "var Alias = Original;";
    Scope scope = createGlobalScope(js);

    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType().isConstructor());
    assertEquals(scope.getVar("Original").getType(), aliasVar.getType());
  }

  // Tests patchGlobalScope updates modified scripts
  @Test
  public void testPatchGlobalScope_updatesScriptVariables() {
    String js = "var x = 10;";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    assertNotNull(globalScope.getVar("x"));

    Node scriptRoot = root.getLastChild().getFirstChild();
    assertTrue(scriptRoot.isScript());

    scopeCreator.patchGlobalScope(globalScope, scriptRoot);
    assertNotNull(globalScope.getVar("x"));
  }

  // Tests multiple var declarations with doc info reports warning
  @Test
  public void testCreateScope_multipleVarDeclarationWithJsDoc_reportsWarning() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    Node root = compiler.parseTestCode(js);
    scopeCreator.createScope(root, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF, compiler.getWarnings()[0].getType());
  }
}