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
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator scopeCreator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Node parseAndCreateRoot(String js) {
    return compiler.parseTestCode(js);
  }

  private Scope createGlobalScope(String js) {
    Node root = parseAndCreateRoot(js);
    return scopeCreator.createScope(root, null);
  }

  private Node findFunctionNode(Node node, String name) {
    if (node.getType() == Token.FUNCTION) {
      if (name == null || name.equals(node.getFirstChild().getString())) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunctionNode(child, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  // Tests initial native scope bindings
  @Test
  public void testCreateInitialScope_nativeBindings_present() {
    Node root = new Node(Token.BLOCK);
    Scope initialScope = scopeCreator.createInitialScope(root);

    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Object.prototype"));
    assertNotNull(initialScope.getVar("Function"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("String"));
    assertNotNull(initialScope.getVar("Number"));
    assertNotNull(initialScope.getVar("Boolean"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("Error"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
    assertNotNull(initialScope.getVar("goog.typedef"));
  }

  // Tests simple global variable declaration
  @Test
  public void testCreateScope_globalVar_inferred() {
    Scope scope = createGlobalScope("var x = 10;");
    Scope.Var var = scope.getVar("x");

    assertNotNull(var);
    assertTrue(var.isTypeInferred());
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests global variable with explicit JSDoc type annotation
  @Test
  public void testCreateScope_globalVarWithJSDoc_declared() {
    Scope scope = createGlobalScope("/** @type {number} */ var x = 10;");
    Scope.Var var = scope.getVar("x");

    assertNotNull(var);
    assertFalse(var.isTypeInferred());
    assertNotNull(var.getType());
    assertTrue(var.getType().isNumberValueType());
  }

  // Tests multiple variables in a single var statement
  @Test
  public void testCreateScope_multipleVarDeclaration_allDeclared() {
    Scope scope = createGlobalScope("var a = 1, b = 2;");
    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
  }

  // Tests multiple variables with JSDoc produces warning
  @Test
  public void testCreateScope_multipleVarDeclarationWithJSDoc_reportsWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests global function declaration
  @Test
  public void testCreateScope_functionDeclaration_registeredInScope() {
    Scope scope = createGlobalScope("function foo(a, b) { return a; }");
    Scope.Var var = scope.getVar("foo");

    assertNotNull(var);
    assertNotNull(var.getType());
    assertTrue(var.getType().isFunctionType());
  }

  // Tests constructor declaration creates prototype in scope
  @Test
  public void testCreateScope_constructorDeclaration_createsPrototype() {
    Scope scope = createGlobalScope("/** @constructor */ function Foo() {}");
    Scope.Var fooVar = scope.getVar("Foo");
    Scope.Var protoVar = scope.getVar("Foo.prototype");

    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests local scope creation for function body and parameters
  @Test
  public void testCreateScope_localScope_definesParametersAndLocalVars() {
    Node root = parseAndCreateRoot("function testFn(param1, param2) { var localVar = 1; }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testFn");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("param2"));
    assertNotNull(localScope.getVar("localVar"));
    assertNull(localScope.getVar("nonExistent"));
  }

  // Tests local catch block slot definition
  @Test
  public void testCreateScope_catchBlock_definesCatchSlot() {
    Node root = parseAndCreateRoot("function testCatch() { try {} catch (e) {} }");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testCatch");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("e"));
  }

  // Tests named function expression bleeding into local scope
  @Test
  public void testCreateScope_namedFunctionExpression_bleedingInLocalScope() {
    Node root = parseAndCreateRoot("var fn = function myBleedingFn() {};");
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "myBleedingFn");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("myBleedingFn"));
  }

  // Tests valid enum declaration
  @Test
  public void testCreateScope_validEnum_definesEnumElements() {
    Scope scope = createGlobalScope(
        "/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };");
    Scope.Var var = scope.getVar("MyEnum");

    assertNotNull(var);
    assertNotNull(var.getType());
    assertTrue(var.getType().isEnumType());
    EnumType enumType = (EnumType) var.getType();
    assertTrue(enumType.hasOwnProperty("FIRST"));
    assertTrue(enumType.hasOwnProperty("SECOND"));
  }

  // Tests enum with duplicate keys reports error/warning
  @Test
  public void testCreateScope_enumDuplicateKey_reportsWarning() {
    createGlobalScope("/** @enum {number} */ var MyEnum = { DUP: 1, DUP: 2 };");
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests enum with invalid non-constant key reports warning
  @Test
  public void testCreateScope_enumInvalidKey_reportsWarning() {
    createGlobalScope("/** @enum {number} */ var MyEnum = { 'invalid-key': 1 };");
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests enum initializer not an object literal reports warning
  @Test
  public void testCreateScope_enumInvalidInitializer_reportsWarning() {
    createGlobalScope("/** @enum {number} */ var MyEnum = 123;");
    assertTrue(compiler.getWarningCount() > 0);
  }

  // Tests JSDoc typedef in var statement
  @Test
  public void testCreateScope_typedefDeclaration_registeredInRegistry() {
    createGlobalScope("/** @typedef {string|number} */ var StringOrNumber;");
    JSType type = compiler.getTypeRegistry().getType("StringOrNumber");
    assertNotNull(type);
    assertTrue(type.isUnionType());
  }

  // Tests qualified name property stub in global scope
  @Test
  public void testCreateScope_stubPropertyDeclaration_declaredInScope() {
    Scope scope = createGlobalScope("var ns = {}; ns.prop;");
    Scope.Var var = scope.getVar("ns.prop");
    assertNotNull(var);
    assertTrue(var.isTypeInferred());
  }

  // Tests constructor prototype method assignment
  @Test
  public void testCreateScope_prototypeMethodAssignment_setsFunctionType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function A() {}\n" +
        "A.prototype.foo = function(x) { return x; };");

    Scope.Var methodVar = scope.getVar("A.prototype.foo");
    assertNotNull(methodVar);
    JSType type = methodVar.getType();
    assertNotNull(type);
    assertTrue(type.isFunctionType());
  }

  // Tests function with @this annotation collecting properties
  @Test
  public void testCreateScope_thisPropertyCollection_definesDeclaredProperty() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Widget() {\n" +
        "  /** @type {string} */ this.name = 'test';\n" +
        "}");

    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType ctorType = widgetVar.getType().toMaybeFunctionType();
    assertNotNull(ctorType);
    ObjectType instanceType = ctorType.getInstanceType();
    assertNotNull(instanceType);
    assertTrue(instanceType.hasProperty("name"));
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    Scope scope = createGlobalScope("/** @interface */ function AnInterface() {}");
    Scope.Var var = scope.getVar("AnInterface");

    assertNotNull(var);
    FunctionType fnType = var.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.isInterface());
    assertNotNull(scope.getVar("AnInterface.prototype"));
  }

  // Tests constructor with @extends tag
  @Test
  public void testCreateScope_constructorExtends_setsSuperClassConstructor() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Base() {}\n" +
        "/** @constructor\n * @extends {Base} */ function Sub() {}");

    Scope.Var subVar = scope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    FunctionType superCtor = subCtor.getSuperClassConstructor();
    assertNotNull(superCtor);
    assertEquals("Base", superCtor.getReferenceName());
  }

  // Tests constructor with @implements tag
  @Test
  public void testCreateScope_constructorImplements_registersImplementedInterfaces() {
    Scope scope = createGlobalScope(
        "/** @interface */ function Formattable() {}\n" +
        "/** @constructor\n * @implements {Formattable} */ function Doc() {}");

    Scope.Var docVar = scope.getVar("Doc");
    assertNotNull(docVar);
    FunctionType docCtor = docVar.getType().toMaybeFunctionType();
    assertNotNull(docCtor);
    assertFalse(docCtor.getImplementedInterfaces().isEmpty());
  }

  // Tests namespaced constructor declaration
  @Test
  public void testCreateScope_namespacedConstructor_definesPrototypeInScope() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "/** @constructor */ ns.Widget = function() {};");

    Scope.Var widgetVar = scope.getVar("ns.Widget");
    assertNotNull(widgetVar);
    assertTrue(widgetVar.getType().isConstructor());
    assertNotNull(scope.getVar("ns.Widget.prototype"));
  }

  // Tests function JSDoc with @param and @return types
  @Test
  public void testCreateScope_functionJSDocParamAndReturn_setsParamAndReturnTypes() {
    Scope scope = createGlobalScope(
        "/**\n" +
        " * @param {string} msg\n" +
        " * @param {number} code\n" +
        " * @return {boolean}\n" +
        " */\n" +
        "function log(msg, code) { return true; }");

    Scope.Var logVar = scope.getVar("log");
    assertNotNull(logVar);
    FunctionType fnType = logVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getReturnType().isBooleanValueType());
  }

  // Tests const variable annotation
  @Test
  public void testCreateScope_constVariable_markedAsConst() {
    Scope scope = createGlobalScope("/** @const */ var MAX_LIMIT = 100;");
    Scope.Var var = scope.getVar("MAX_LIMIT");

    assertNotNull(var);
    assertTrue(var.isConst());
  }

  // Tests object literal property inference
  @Test
  public void testCreateScope_objectLiteral_infersObjectProperties() {
    Scope scope = createGlobalScope("var config = { host: 'localhost', port: 8080 };");
    Scope.Var var = scope.getVar("config");

    assertNotNull(var);
    ObjectType objType = var.getType().toObjectType();
    assertNotNull(objType);
    assertTrue(objType.hasProperty("host"));
    assertTrue(objType.hasProperty("port"));
  }

  // Tests qualified name property with explicit @type annotation
  @Test
  public void testCreateScope_qualifiedNameWithJSDoc_declaresExplicitType() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "/** @type {number} */ ns.count = 42;");

    Scope.Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertFalse(countVar.isTypeInferred());
    assertTrue(countVar.getType().isNumberValueType());
  }

  // Tests constructor prototype replaced by object literal
  @Test
  public void testCreateScope_prototypeAssignedObjectLiteral_registersPrototypeMethods() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Calc() {}\n" +
        "Calc.prototype = {\n" +
        "  add: function(a, b) { return a + b; }\n" +
        "};");

    Scope.Var methodVar = scope.getVar("Calc.prototype.add");
    assertNotNull(methodVar);
    assertTrue(methodVar.getType().isFunctionType());
  }

  // Tests anonymous function expression assigned to variable
  @Test
  public void testCreateScope_anonymousFunctionAssignment_inferredAsFunctionType() {
    Scope scope = createGlobalScope("var compute = function(x) { return x * 2; };");
    Scope.Var var = scope.getVar("compute");

    assertNotNull(var);
    assertNotNull(var.getType());
    assertTrue(var.getType().isFunctionType());
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_attachesPropertiesToTarget() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Base() {}\n" +
        "var mixin = /** @lends {Base.prototype} */ ({\n" +
        "  sayHello: function() {}\n" +
        "});");

    Scope.Var methodVar = scope.getVar("Base.prototype.sayHello");
    assertNotNull(methodVar);
    assertTrue(methodVar.getType().isFunctionType());
  }

  // Tests redeclaration of variable in same scope
  @Test
  public void testCreateScope_redeclaredVar_keepsExistingVar() {
    Scope scope = createGlobalScope("var duplicated = 1; var duplicated = 2;");
    Scope.Var var = scope.getVar("duplicated");

    assertNotNull(var);
    assertEquals(0, compiler.getErrorCount());
  }
}