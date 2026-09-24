package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
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
    compiler.initOptions(options);
  }

  private Node parse(String js) {
    return compiler.parseTestCode(js);
  }

  private Scope createGlobalScope(Node root) {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  // Tests initial native types present in global scope
  @Test
  public void testCreateInitialScope_nativeTypesDeclared() {
    Node root = parse("");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope initialScope = scopeCreator.createInitialScope(root);

    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
  }

  // Tests global primitive variable declarations and type inference
  @Test
  public void testCreateScope_primitiveVars_typesInferred() {
    Node root = parse("var num = 42; var str = 'hello'; var bool = true; var nul = null;");
    Scope scope = createGlobalScope(root);

    Scope.Var numVar = scope.getVar("num");
    assertNotNull(numVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), numVar.getType());

    Scope.Var strVar = scope.getVar("str");
    assertNotNull(strVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), strVar.getType());

    Scope.Var boolVar = scope.getVar("bool");
    assertNotNull(boolVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), boolVar.getType());

    Scope.Var nulVar = scope.getVar("nul");
    assertNotNull(nulVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE), nulVar.getType());
  }

  // Tests constructor declaration and its prototype creation
  @Test
  public void testCreateScope_constructorDeclaration_createsPrototypeVar() {
    Node root = parse("/** @constructor */ function Person(name) { this.name = name; }");
    Scope scope = createGlobalScope(root);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    assertTrue(personVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Person.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    Node root = parse("/** @interface */ function Disposable() {}");
    Scope scope = createGlobalScope(root);

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
  }

  // Tests enum declaration and enum elements
  @Test
  public void testCreateScope_enumDeclaration_definesEnumElements() {
    Node root = parse("/** @enum {number} */ var Status = { OK: 200, NOT_FOUND: 404 };");
    Scope scope = createGlobalScope(root);

    Scope.Var statusVar = scope.getVar("Status");
    assertNotNull(statusVar);
    assertTrue(statusVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) statusVar.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("NOT_FOUND"));
  }

  // Tests local scope creation and parameter declarations
  @Test
  public void testCreateScope_localScope_declaresParametersAndLocalVars() {
    Node root = parse("function calculate(a, b) { var result = a + b; return result; }");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node scriptNode = root.getLastChild();
    Node fnNode = scriptNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertNotNull(localScope.getVar("result"));
    assertEquals(globalScope, localScope.getParent());
  }

  // Tests catch block scope variable declaration
  @Test
  public void testCreateScope_catchBlock_declaresCatchVar() {
    Node root = parse("try { var x = 1; } catch (err) { var y = err; }");
    Scope scope = createGlobalScope(root);

    assertNotNull(scope.getVar("x"));
    assertNotNull(scope.getVar("err"));
    assertNotNull(scope.getVar("y"));
  }

  // Tests typedef declaration and type registration
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    Node root = parse("/** @typedef {(string|number)} */ var StringOrNum;");
    Scope scope = createGlobalScope(root);

    assertNotNull(scope.getVar("StringOrNum"));
    JSType registered = compiler.getTypeRegistry().getType("StringOrNum");
    assertNotNull(registered);
    assertTrue(registered.isUnionType());
  }

  // Tests qualified name property assignment
  @Test
  public void testCreateScope_qualifiedNameAssignment_definesPropertySlot() {
    Node root = parse("var ns = {}; /** @type {number} */ ns.count = 10;");
    Scope scope = createGlobalScope(root);

    Scope.Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), countVar.getType());
  }

  // Tests object literal with @lends annotation
  @Test
  public void testCreateScope_objectLiteralWithLends_attachesProperties() {
    Node root = parse(
        "/** @constructor */ function Widget() {}\n" +
        "Widget.prototype = /** @lends {Widget.prototype} */ ({ " +
        "  /** @type {string} */ title: 'test'\n" +
        "});");
    Scope scope = createGlobalScope(root);

    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType fnType = widgetVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    ObjectType proto = fnType.getPrototype();
    assertTrue(proto.hasProperty("title"));
  }

  // Tests patched global scope re-traversal
  @Test
  public void testPatchGlobalScope_updatesVariableDeclarations() {
    Node root1 = parse("var a = 1;");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root1, null);
    assertNotNull(globalScope.getVar("a"));

    Node root2 = parse("var b = 2;");
    Node scriptNode = root2.getLastChild();
    scopeCreator.patchGlobalScope(globalScope, scriptNode);

    assertNotNull(globalScope.getVar("b"));
  }

  // Tests hoisted function declaration handling
  @Test
  public void testCreateScope_hoistedFunction_declaredBeforeUse() {
    Node root = parse("var res = compute(); function compute() { return 1; }");
    Scope scope = createGlobalScope(root);

    assertNotNull(scope.getVar("compute"));
    assertNotNull(scope.getVar("res"));
    assertTrue(scope.getVar("compute").getType().isFunctionType());
  }

  // Tests nested functions with closed-over variables (regression-oriented for Defects4J Closure 168)
  @Test
  public void testCreateScope_nestedFunctionsAndEscapedVars_handlesInnerVars() {
    Node root = parse(
        "function outer() {\n" +
        "  var self = this;\n" +
        "  function inner() {\n" +
        "    var ref = self;\n" +
        "    function deep() {\n" +
        "      return ref;\n" +
        "    }\n" +
        "  }\n" +
        "}");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node outerFn = root.getLastChild().getFirstChild();
    Scope outerScope = scopeCreator.createScope(outerFn, globalScope);
    assertNotNull(outerScope.getVar("self"));
    assertNotNull(outerScope.getVar("inner"));

    Node innerFn = outerFn.getLastChild().getFirstChild().getNext();
    Scope innerScope = scopeCreator.createScope(innerFn, outerScope);
    assertNotNull(innerScope.getVar("ref"));
    assertNotNull(innerScope.getVar("deep"));
  }

  // Tests uninitialized variable declaration (inferred as unknown or undefined)
  @Test
  public void testCreateScope_uninitializedVar_declaredInScope() {
    Node root = parse("var uninit;");
    Scope scope = createGlobalScope(root);

    Scope.Var uninitVar = scope.getVar("uninit");
    assertNotNull(uninitVar);
    assertTrue(uninitVar.isTypeInferred());
  }

  // Tests prototype method assignment creates correct function type
  @Test
  public void testCreateScope_prototypeMethodAssignment_setsFunctionType() {
    Node root = parse(
        "/** @constructor */ function Animal() {}\n" +
        "/** @param {string} sound */\n" +
        "Animal.prototype.speak = function(sound) { return sound; };");
    Scope scope = createGlobalScope(root);

    Scope.Var speakVar = scope.getVar("Animal.prototype.speak");
    assertNotNull(speakVar);
    assertTrue(speakVar.getType().isFunctionType());
    FunctionType fnType = speakVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
  }

  // Tests constructor inheritance with @extends annotation
  @Test
  public void testCreateScope_constructorInheritance_setsSuperClassConstructor() {
    Node root = parse(
        "/** @constructor */ function Base() {}\n" +
        "/** @constructor\n * @extends {Base} */ function Derived() {}");
    Scope scope = createGlobalScope(root);

    Scope.Var derivedVar = scope.getVar("Derived");
    assertNotNull(derivedVar);
    FunctionType derivedFn = derivedVar.getType().toMaybeFunctionType();
    assertNotNull(derivedFn);
    FunctionType baseFn = scope.getVar("Base").getType().toMaybeFunctionType();
    assertEquals(baseFn.getInstanceType(), derivedFn.getSuperClassConstructor().getInstanceType());
  }

  // Tests interface implementation with @implements annotation
  @Test
  public void testCreateScope_interfaceImplementation_recordsImplementedInterface() {
    Node root = parse(
        "/** @interface */ function Clickable() {}\n" +
        "/** @constructor\n * @implements {Clickable} */ function Button() {}");
    Scope scope = createGlobalScope(root);

    Scope.Var buttonVar = scope.getVar("Button");
    assertNotNull(buttonVar);
    FunctionType buttonFn = buttonVar.getType().toMaybeFunctionType();
    assertNotNull(buttonFn);
    assertEquals(1, buttonFn.getImplementedInterfaces().size());
  }

  // Tests @this annotation in function JSDoc
  @Test
  public void testCreateScope_thisAnnotation_bindsTypeOfThis() {
    Node root = parse(
        "/** @constructor */ function Context() {}\n" +
        "/** @this {Context} */ function execute() { return this; }");
    Scope scope = createGlobalScope(root);

    Scope.Var executeVar = scope.getVar("execute");
    assertNotNull(executeVar);
    FunctionType fnType = executeVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    Scope.Var contextVar = scope.getVar("Context");
    assertEquals(contextVar.getType().toMaybeFunctionType().getInstanceType(), fnType.getTypeOfThis());
  }

  // Tests record type annotation on a variable
  @Test
  public void testCreateScope_recordTypeAnnotation_createsRecordType() {
    Node root = parse("/** @type {{x: number, y: string}} */ var point;");
    Scope scope = createGlobalScope(root);

    Scope.Var pointVar = scope.getVar("point");
    assertNotNull(pointVar);
    ObjectType objType = pointVar.getType().toMaybeObjectType();
    assertNotNull(objType);
    assertTrue(objType.isRecordType());
    assertTrue(objType.hasProperty("x"));
    assertTrue(objType.hasProperty("y"));
  }

  // Tests function expression assigned to variable with parameter annotations
  @Test
  public void testCreateScope_functionExpression_infersReturnTypeAndParams() {
    Node root = parse(
        "/**\n" +
        " * @param {number} x\n" +
        " * @return {boolean}\n" +
        " */\n" +
        "var isPositive = function(x) { return x > 0; };");
    Scope scope = createGlobalScope(root);

    Scope.Var varSlot = scope.getVar("isPositive");
    assertNotNull(varSlot);
    FunctionType fnType = varSlot.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), fnType.getReturnType());
  }

  // Tests multiple variable declarations in a single var statement
  @Test
  public void testCreateScope_multipleVarDeclarationsInSingleStatement() {
    Node root = parse("var x = 1, y = 'abc', z = false;");
    Scope scope = createGlobalScope(root);

    assertNotNull(scope.getVar("x"));
    assertNotNull(scope.getVar("y"));
    assertNotNull(scope.getVar("z"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), scope.getVar("x").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), scope.getVar("y").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), scope.getVar("z").getType());
  }

  // Tests @const annotation on variable declaration
  @Test
  public void testCreateScope_constVariable_inferredCorrectly() {
    Node root = parse("/** @const */ var MAX_SIZE = 100;");
    Scope scope = createGlobalScope(root);

    Scope.Var constVar = scope.getVar("MAX_SIZE");
    assertNotNull(constVar);
    assertTrue(constVar.isConst());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), constVar.getType());
  }
}