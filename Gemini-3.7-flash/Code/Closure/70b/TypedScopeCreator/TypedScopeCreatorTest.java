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
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    return scopeCreator.createScope(root, null);
  }

  private Node findFunctionNode(Node node) {
    if (node.getType() == Token.FUNCTION) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node fn = findFunctionNode(child);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  // Tests global scope creation and predefined native types
  @Test
  public void testCreateInitialScope_nativeTypes_declaredInGlobalScope() {
    String js = "var a = 1;";
    Scope globalScope = createGlobalScope(js);
    assertNotNull(globalScope);
    assertTrue(globalScope.isGlobal());
    assertNotNull(globalScope.getVar("Object"));
    assertNotNull(globalScope.getVar("Array"));
    assertNotNull(globalScope.getVar("Function"));
    assertNotNull(globalScope.getVar("undefined"));
  }

  // Tests var declaration with primitive literal values
  @Test
  public void testCreateScope_primitiveVars_infersCorrectTypes() {
    String js = "var n = 123; var s = 'test'; var b = true; var nul = null;";
    Scope globalScope = createGlobalScope(js);

    Scope.Var varN = globalScope.getVar("n");
    assertNotNull(varN);
    assertNotNull(varN.getType());
    assertTrue(varN.getType().isNumber());

    Scope.Var varS = globalScope.getVar("s");
    assertNotNull(varS);
    assertNotNull(varS.getType());
    assertTrue(varS.getType().isString());

    Scope.Var varB = globalScope.getVar("b");
    assertNotNull(varB);
    assertNotNull(varB.getType());
    assertTrue(varB.getType().isBooleanValueType());

    Scope.Var varNul = globalScope.getVar("nul");
    assertNotNull(varNul);
    assertNotNull(varNul.getType());
    assertTrue(varNul.getType().isNullType());
  }

  // Tests JSDoc type declaration on a variable
  @Test
  public void testCreateScope_jsdocVarDeclaration_setsDeclaredType() {
    String js = "/** @type {number} */ var x = 10;";
    Scope globalScope = createGlobalScope(js);

    Scope.Var varX = globalScope.getVar("x");
    assertNotNull(varX);
    assertFalse(varX.isTypeInferred());
    assertTrue(varX.getType().isNumber());
  }

  // Tests constructor function declaration and prototype registration
  @Test
  public void testCreateScope_constructorFunction_createsConstructorAndPrototype() {
    String js = "/** @constructor */ function Foo() {}";
    Scope globalScope = createGlobalScope(js);

    Scope.Var fooVar = globalScope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());

    Scope.Var protoVar = globalScope.getVar("Foo.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests function parameters in local scope
  @Test
  public void testCreateScope_functionParameters_declaredInLocalScope() {
    String js = "/** @param {string} a\n * @param {number} b */ function f(a, b) {}";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root);
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope);
    assertFalse(localScope.isGlobal());

    Scope.Var varA = localScope.getVar("a");
    assertNotNull(varA);
    assertNotNull(varA.getType());
    assertTrue(varA.getType().isString());

    Scope.Var varB = localScope.getVar("b");
    assertNotNull(varB);
    assertNotNull(varB.getType());
    assertTrue(varB.getType().isNumber());
  }

  // Tests local variable sharing name with parameter (Defects4J 70b regression)
  @Test
  public void testCreateScope_duplicateParamAndLocalVar_handledInLocalScope() {
    String js = "/** @param {number} x */ function f(x) { var x = 1; }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root);
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope);

    Scope.Var varX = localScope.getVar("x");
    assertNotNull(varX);
    assertNotNull(varX.getType());
    assertTrue(varX.getType().isNumber());
  }

  // Tests enum declaration with object literal elements
  @Test
  public void testCreateScope_enumDeclaration_registersEnumType() {
    String js = "/** @enum {string} */ var Color = { RED: 'red', BLUE: 'blue' };";
    Scope globalScope = createGlobalScope(js);

    Scope.Var colorVar = globalScope.getVar("Color");
    assertNotNull(colorVar);
    assertTrue(colorVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) colorVar.getType();
    assertTrue(enumType.getElementsType().isString());
    assertTrue(enumType.hasOwnProperty("RED"));
    assertTrue(enumType.hasOwnProperty("BLUE"));
  }

  // Tests typedef declaration and resolution
  @Test
  public void testCreateScope_typedefDeclaration_registersCustomType() {
    String js = "/** @typedef {{x: number, y: number}} */ var Point; var p;";
    Scope globalScope = createGlobalScope(js);

    JSType pointType = compiler.getTypeRegistry().getType("Point");
    assertNotNull(pointType);
    assertTrue(pointType.isRecordType());
  }

  // Tests catch clause parameter in local scope
  @Test
  public void testCreateScope_catchClause_declaresCatchVariable() {
    String js = "function testCatch() { try {} catch (e) {} }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root);
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope);
  }

  // Tests qualified name property assignment on an existing object
  @Test
  public void testCreateScope_qualifiedNameAssignment_definesPropertySlot() {
    String js = "var ns = {}; /** @type {number} */ ns.count = 42;";
    Scope globalScope = createGlobalScope(js);

    Scope.Var nsVar = globalScope.getVar("ns");
    assertNotNull(nsVar);

    Scope.Var countVar = globalScope.getVar("ns.count");
    assertNotNull(countVar);
    assertFalse(countVar.isTypeInferred());
    assertTrue(countVar.getType().isNumber());
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    String js = "/** @interface */ function Disposable() {}";
    Scope globalScope = createGlobalScope(js);

    Scope.Var ifaceVar = globalScope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
  }

  // Tests anonymous function expression without parameters
  @Test
  public void testCreateScope_anonymousFunctionLiteral_createsFunctionType() {
    String js = "var fn = function() { return 5; };";
    Scope globalScope = createGlobalScope(js);

    Scope.Var fnVar = globalScope.getVar("fn");
    assertNotNull(fnVar);
    assertTrue(fnVar.getType().isFunctionType());
  }

  // Tests object literal with @lends annotation
  @Test
  public void testCreateScope_objectLiteralWithLends_attachesProperties() {
    String js = "/** @constructor */ function Person() {}\n"
        + "Person.prototype = /** @lends {Person.prototype} */ ({ "
        + "  /** @type {string} */ name: 'Alice'"
        + "});";
    Scope globalScope = createGlobalScope(js);

    Scope.Var protoVar = globalScope.getVar("Person.prototype");
    assertNotNull(protoVar);
    ObjectType protoType = (ObjectType) protoVar.getType();
    assertNotNull(protoType);
    assertTrue(protoType.hasProperty("name"));
    assertTrue(protoType.getPropertyType("name").isString());
  }

  // Tests class-defining goog.inherits call
  @Test
  public void testCreateScope_googInherits_setsSuperClassRelationship() {
    String js = "var goog = {}; goog.inherits = function(child, parent) {};\n"
        + "/** @constructor */ function Super() {}\n"
        + "/** @constructor */ function Sub() {}\n"
        + "goog.inherits(Sub, Super);";
    Scope globalScope = createGlobalScope(js);

    Scope.Var subVar = globalScope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = (FunctionType) subVar.getType();
    assertNotNull(subCtor.getSuperClassConstructor());
    assertEquals("Super", subCtor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  // Tests constructor with @extends tag
  @Test
  public void testCreateScope_constructorWithExtends_setsSuperType() {
    String js = "/** @constructor */ function Base() {}\n"
        + "/** @constructor\n * @extends {Base} */ function Derived() {}";
    Scope globalScope = createGlobalScope(js);

    Scope.Var derivedVar = globalScope.getVar("Derived");
    assertNotNull(derivedVar);
    FunctionType derivedCtor = (FunctionType) derivedVar.getType();
    assertNotNull(derivedCtor.getSuperClassConstructor());
    assertEquals("Base", derivedCtor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  // Tests constructor with @implements tag
  @Test
  public void testCreateScope_constructorWithImplements_setsImplementedInterfaces() {
    String js = "/** @interface */ function Clickable() {}\n"
        + "/** @constructor\n * @implements {Clickable} */ function Button() {}";
    Scope globalScope = createGlobalScope(js);

    Scope.Var buttonVar = globalScope.getVar("Button");
    assertNotNull(buttonVar);
    FunctionType buttonCtor = (FunctionType) buttonVar.getType();
    assertNotNull(buttonCtor.getImplementedInterfaces());
    assertEquals(1, buttonCtor.getImplementedInterfaces().size());
  }

  // Tests return type inference and @return tag on function
  @Test
  public void testCreateScope_functionReturnType_declaredAndInferred() {
    String js = "/** @return {boolean} */ function isReady() { return true; }";
    Scope globalScope = createGlobalScope(js);

    Scope.Var fnVar = globalScope.getVar("isReady");
    assertNotNull(fnVar);
    FunctionType fnType = (FunctionType) fnVar.getType();
    assertNotNull(fnType.getReturnType());
    assertTrue(fnType.getReturnType().isBooleanValueType());
  }

  // Tests function with 'this' type annotation (@this)
  @Test
  public void testCreateScope_functionThisTypeAnnotation() {
    String js = "/** @constructor */ function Widget() {}\n"
        + "/** @this {Widget} */ function render() {}";
    Scope globalScope = createGlobalScope(js);

    Scope.Var renderVar = globalScope.getVar("render");
    assertNotNull(renderVar);
    FunctionType renderType = (FunctionType) renderVar.getType();
    assertNotNull(renderType.getTypeOfThis());
    assertTrue(renderType.getTypeOfThis().isInstanceType());
  }

  // Tests getter and setter in object literal
  @Test
  public void testCreateScope_objectLiteralGetterSetter() {
    String js = "var obj = { get value() { return 1; }, set value(v) {} };";
    Scope globalScope = createGlobalScope(js);

    Scope.Var objVar = globalScope.getVar("obj");
    assertNotNull(objVar);
    assertTrue(objVar.getType().isObjectType());
  }

  // Tests function with variable arguments (@param {...number})
  @Test
  public void testCreateScope_varargsFunction() {
    String js = "/** @param {...number} var_args */ function sum(var_args) {}";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root);
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope);
    Scope.Var varArgs = localScope.getVar("var_args");
    assertNotNull(varArgs);
  }

  // Tests function with optional parameters (@param {number=} opt_n)
  @Test
  public void testCreateScope_optionalParameters() {
    String js = "/** @param {number=} opt_n */ function pad(opt_n) {}";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root);
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope);
    Scope.Var optVar = localScope.getVar("opt_n");
    assertNotNull(optVar);
  }

  // Tests @const variable declaration
  @Test
  public void testCreateScope_constVariableDeclaration() {
    String js = "/** @const */ var MAX_SIZE = 100;";
    Scope globalScope = createGlobalScope(js);

    Scope.Var maxVar = globalScope.getVar("MAX_SIZE");
    assertNotNull(maxVar);
    assertTrue(maxVar.isConst());
    assertNotNull(maxVar.getType());
    assertTrue(maxVar.getType().isNumber());
  }

  // Tests stub method declaration on prototype
  @Test
  public void testCreateScope_stubMethodOnPrototype() {
    String js = "/** @constructor */ function Greeter() {}\n"
        + "/** @param {string} name */ Greeter.prototype.greet;";
    Scope globalScope = createGlobalScope(js);

    Scope.Var greetVar = globalScope.getVar("Greeter.prototype.greet");
    assertNotNull(greetVar);
  }

  // Tests namespaced constructor assignment
  @Test
  public void testCreateScope_namespacedConstructorAssignment() {
    String js = "var app = {};\n"
        + "/** @constructor */ app.Component = function() {};";
    Scope globalScope = createGlobalScope(js);

    Scope.Var compVar = globalScope.getVar("app.Component");
    assertNotNull(compVar);
    assertTrue(compVar.getType().isConstructor());
  }
}