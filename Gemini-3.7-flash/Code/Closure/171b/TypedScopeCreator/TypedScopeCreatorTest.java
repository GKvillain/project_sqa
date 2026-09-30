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

  private Scope createGlobalScope(String js) {
    return createGlobalScope("", js);
  }

  private Scope createGlobalScope(String externsJs, String js) {
    Node externs = compiler.parseTestCode(externsJs);
    Node main = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externs, main);
    return scopeCreator.createScope(root, null);
  }

  // Tests native bindings initialized in the outermost scope
  @Test
  public void testCreateInitialScope_emptyRoot_declaresNativeTypes() {
    Scope scope = createGlobalScope("");
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("undefined"));
  }

  // Tests normal inferred variable declaration in global scope
  @Test
  public void testCreateScope_globalNumberVar_infersNumberType() {
    Scope scope = createGlobalScope("var x = 1;");
    assertTrue(scope.isDeclared("x", false));
    Scope.Var xVar = scope.getVar("x");
    assertNotNull(xVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        xVar.getType());
  }

  // Tests explicit @type annotation on global variable declaration
  @Test
  public void testCreateScope_annotatedStringType_declaresStringType() {
    Scope scope = createGlobalScope("/** @type {string} */ var s = 'hello';");
    assertTrue(scope.isDeclared("s", false));
    Scope.Var sVar = scope.getVar("s");
    assertNotNull(sVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        sVar.getType());
    assertFalse(sVar.isTypeInferred());
  }

  // Tests @constructor function and automatic prototype declaration
  @Test
  public void testCreateScope_constructorFunction_createsConstructorAndPrototype() {
    Scope scope = createGlobalScope("/** @constructor */ function Foo() {}");
    assertTrue(scope.isDeclared("Foo", false));
    assertTrue(scope.isDeclared("Foo.prototype", false));
    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isConstructor());
  }

  // Tests local scope traversal for arguments and local variables
  @Test
  public void testCreateScope_localFunction_declaresParamsAndLocalVars() {
    Node externs = compiler.parseTestCode("");
    Node main = compiler.parseTestCode("function f(a, b) { var c = true; }");
    Node root = new Node(Token.BLOCK, externs, main);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node functionNode = main.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertTrue(localScope.isDeclared("a", false));
    assertTrue(localScope.isDeclared("b", false));
    assertTrue(localScope.isDeclared("c", false));
    assertFalse(localScope.isGlobal());
    assertEquals(globalScope, localScope.getParent());
  }

  // Tests @enum declaration and enum element definitions
  @Test
  public void testCreateScope_enumObjectLiteral_createsEnumType() {
    Scope scope = createGlobalScope("/** @enum {number} */ var MyEnum = { A: 1, B: 2 };");
    assertTrue(scope.isDeclared("MyEnum", false));
    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElementsType().isNumber());
  }

  // Tests @typedef registration in the type registry
  @Test
  public void testCreateScope_typedefAnnotation_registersTypeInRegistry() {
    createGlobalScope("/** @typedef {{x: number, y: number}} */ var Point;");
    JSType type = compiler.getTypeRegistry().getType("Point");
    assertNotNull(type);
    assertTrue(type.isRecordType());
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_objectLiteralWithLends_lendsPropertiesToType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Person() {}\n"
            + "Person.prototype = /** @lends {Person.prototype} */ ({ sayHi: function() {} });");
    assertTrue(scope.isDeclared("Person", false));
    Scope.Var personVar = scope.getVar("Person");
    FunctionType fnType = personVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getPrototype().hasOwnProperty("sayHi"));
  }

  // Tests qualified name property assignment declaration
  @Test
  public void testCreateScope_qualifiedNameAssignment_declaresProperty() {
    Scope scope = createGlobalScope("var ns = {}; /** @type {number} */ ns.count = 0;");
    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.count", false));
    Scope.Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        countVar.getType());
  }

  // Tests type-less stub property resolution
  @Test
  public void testCreateScope_stubPropertyDeclaration_registersProperty() {
    Scope scope = createGlobalScope("var ns = {}; ns.prop;");
    assertTrue(scope.isDeclared("ns", false));
    assertTrue(scope.isDeclared("ns.prop", false));
  }

  // Tests catch parameter declaration in local scope
  @Test
  public void testCreateScope_catchBlock_declaresCatchParam() {
    Node externs = compiler.parseTestCode("");
    Node main = compiler.parseTestCode("function test() { try { } catch (e) { var x = 1; } }");
    Node root = new Node(Token.BLOCK, externs, main);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node functionNode = main.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);
    assertTrue(localScope.isDeclared("e", false));
    assertTrue(localScope.isDeclared("x", false));
  }

  // Tests warning on JSDoc with multiple variable declaration
  @Test
  public void testCreateScope_multipleVarsWithDoc_reportsWarning() {
    Scope scope = createGlobalScope("/** @type {number} */ var x = 1, y = 2;");
    assertTrue(scope.isDeclared("x", false));
    assertTrue(scope.isDeclared("y", false));
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests @const annotation declaring non-inferred type
  @Test
  public void testCreateScope_constAnnotation_infersDeclaredConstantType() {
    Scope scope = createGlobalScope("/** @const */ var CONST_VAL = 'IMMUTABLE';");
    assertTrue(scope.isDeclared("CONST_VAL", false));
    Scope.Var constVar = scope.getVar("CONST_VAL");
    assertNotNull(constVar);
    assertFalse(constVar.isTypeInferred());
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        constVar.getType());
  }

  // Tests property defined on 'this' inside constructor body
  @Test
  public void testCreateScope_constructorThisProperty_definesPropertyOnInstance() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Widget() {\n"
            + "  /** @type {string} */ this.name = 'widget';\n"
            + "}");
    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType fnType = widgetVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getInstanceType().hasOwnProperty("name"));
  }

  // Tests patching an existing global scope by replacing a script
  @Test
  public void testPatchGlobalScope_modifiedScript_updatesGlobalScopeVariables() {
    Node externs = compiler.parseSyntheticCode("externs", "");
    Node script = compiler.parseSyntheticCode("input1.js", "var oldVar = 10;");
    Node root = new Node(Token.BLOCK, externs, script);
    Scope globalScope = scopeCreator.createScope(root, null);
    assertTrue(globalScope.isDeclared("oldVar", false));

    Node newScript = compiler.parseSyntheticCode("input1.js", "var newVar = 20;");
    scopeCreator.patchGlobalScope(globalScope, newScript);
    assertFalse(globalScope.isDeclared("oldVar", false));
    assertTrue(globalScope.isDeclared("newVar", false));
  }

  // Tests @interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    Scope scope = createGlobalScope("/** @interface */ function Disposable() {}");
    assertTrue(scope.isDeclared("Disposable", false));
    Scope.Var dispVar = scope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());
  }

  // Tests prototype method definition with @param and @return JSDoc
  @Test
  public void testCreateScope_prototypeMethodDeclaration_attachesMethodToPrototype() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Calculator() {}\n"
            + "/** @param {number} a\n"
            + " *  @param {number} b\n"
            + " *  @return {number} */\n"
            + "Calculator.prototype.add = function(a, b) { return a + b; };");
    assertTrue(scope.isDeclared("Calculator.prototype.add", false));
    Scope.Var addVar = scope.getVar("Calculator.prototype.add");
    assertNotNull(addVar);
    FunctionType fnType = addVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        fnType.getReturnType());
  }

  // Tests @extends inheritance relationship with constructor
  @Test
  public void testCreateScope_constructorWithExtends_setsSuperType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Base() {}\n"
            + "/** @constructor\n"
            + " *  @extends {Base} */\n"
            + "function Derived() {}\n"
            + "goog.inherits(Derived, Base);");
    assertTrue(scope.isDeclared("Derived", false));
    Scope.Var derivedVar = scope.getVar("Derived");
    assertNotNull(derivedVar);
    FunctionType derivedFn = derivedVar.getType().toMaybeFunctionType();
    assertNotNull(derivedFn);
    ObjectType superType = derivedFn.getSuperClassConstructor();
    assertNotNull(superType);
    assertEquals("Base", superType.getDisplayName());
  }

  // Tests named function expression within local variable assignment
  @Test
  public void testCreateScope_namedFunctionExpression_declaresInnerNameInFunctionScope() {
    Node externs = compiler.parseTestCode("");
    Node main = compiler.parseTestCode("var f = function inner(x) { return inner(x - 1); };");
    Node root = new Node(Token.BLOCK, externs, main);
    Scope globalScope = scopeCreator.createScope(root, null);
    assertTrue(globalScope.isDeclared("f", false));
    assertFalse(globalScope.isDeclared("inner", false));

    Node varNode = main.getFirstChild();
    Node fnNode = varNode.getFirstChild().getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isDeclared("inner", false));
    assertTrue(localScope.isDeclared("x", false));
  }

  // Tests @this annotation on free function
  @Test
  public void testCreateScope_functionWithThisAnnotation_bindsThisType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Item() {}\n"
            + "/** @this {Item} */ function printItem() { return this; }");
    assertTrue(scope.isDeclared("printItem", false));
    Scope.Var fnVar = scope.getVar("printItem");
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    ObjectType thisType = fnType.getTypeOfThis().toObjectType();
    assertNotNull(thisType);
    assertEquals("Item", thisType.getDisplayName());
  }

  // Tests externs script declaring properties on native/global objects
  @Test
  public void testCreateScope_externsVariableDeclaration_marksVarAsFromExterns() {
    Scope scope = createGlobalScope("var externalVar;", "var internalVar = 1;");
    assertTrue(scope.isDeclared("externalVar", false));
    assertTrue(scope.isDeclared("internalVar", false));
    Scope.Var extVar = scope.getVar("externalVar");
    Scope.Var intVar = scope.getVar("internalVar");
    assertNotNull(extVar);
    assertNotNull(intVar);
    assertTrue(extVar.isExtern());
    assertFalse(intVar.isExtern());
  }

  // Tests prototype property declared with stub annotation
  @Test
  public void testCreateScope_prototypeStubProperty_attachesPropertyType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Counter() {}\n"
            + "/** @type {number} */ Counter.prototype.count;");
    assertTrue(scope.isDeclared("Counter.prototype.count", false));
    Scope.Var countVar = scope.getVar("Counter.prototype.count");
    assertNotNull(countVar);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        countVar.getType());
  }

  // Tests @implements interface relationship on constructor
  @Test
  public void testCreateScope_constructorWithImplements_registersImplementedInterface() {
    Scope scope = createGlobalScope(
        "/** @interface */ function Printable() {}\n"
            + "/** @constructor\n"
            + " *  @implements {Printable} */\n"
            + "function Document() {}");
    assertTrue(scope.isDeclared("Document", false));
    Scope.Var docVar = scope.getVar("Document");
    assertNotNull(docVar);
    FunctionType docFn = docVar.getType().toMaybeFunctionType();
    assertNotNull(docFn);
    assertEquals(1, docFn.getImplementedInterfaces().size());
  }
}