package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
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
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private Scope createLocalScope(Scope globalScope, Node functionNode) {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(functionNode.getLastChild(), globalScope);
  }

  // Tests initial native types declaration in outermost scope
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Node root = new Node(0);
    Scope initialScope = scopeCreator.createInitialScope(root);

    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Function"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("String"));
    assertNotNull(initialScope.getVar("Number"));
    assertNotNull(initialScope.getVar("Boolean"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
  }

  // Tests global variable declaration with number type
  @Test
  public void testCreateScope_globalVarDeclaration_createsScopeVar() {
    Scope scope = createGlobalScope("var x = 10;");
    Scope.Var varX = scope.getVar("x");

    assertNotNull(varX);
    assertNotNull(varX.getType());
    assertTrue(varX.getType().isNumber());
    assertFalse(varX.isTypeInferred());
  }

  // Tests global hoisted function literal and its parameter definitions
  @Test
  public void testCreateScope_functionDeclaration_createsFunctionType() {
    Scope scope = createGlobalScope("function foo(a, b) { return a; }");
    Scope.Var fooVar = scope.getVar("foo");

    assertNotNull(fooVar);
    assertNotNull(fooVar.getType());
    assertTrue(fooVar.getType().isFunctionType());
  }

  // Tests local scope creation and local variable resolution
  @Test
  public void testCreateScope_localScope_createsLocalVariables() {
    String js = "function outer(param1) { var localVar = param1; return localVar; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node script = root.getFirstChild();
    Node functionNode = script.getFirstChild();
    assertEquals(0, compiler.getErrorCount());

    Scope localScope = scopeCreator.createScope(functionNode.getLastChild(), globalScope);
    assertNotNull(localScope.getVar("localVar"));
    assertNotNull(localScope.getVar("param1"));
    assertEquals(globalScope, localScope.getParent());
  }

  // Tests constructor declaration and prototype property synthesis
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototype() {
    String js = "/** @constructor */ function MyClass() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests prototype object literal assignment and inheritance resolution
  @Test
  public void testCreateScope_prototypeAssignment_handlesOverriddenPrototype() {
    String js = "/** @constructor */ function SuperClass() {}\n"
        + "/** @constructor \n * @extends {SuperClass} */ function SubClass() {}\n"
        + "SubClass.prototype = { method: function() {} };";
    Scope scope = createGlobalScope(js);

    Scope.Var subClassVar = scope.getVar("SubClass");
    assertNotNull(subClassVar);
    assertTrue(subClassVar.getType().isConstructor());

    FunctionType subCtor = subClassVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    assertNotNull(subCtor.getPrototype());
  }

  // Tests enum declaration and element members definition
  @Test
  public void testCreateScope_enumDeclaration_createsEnumType() {
    String js = "/** @enum {number} */ var Status = { OK: 1, ERROR: 2 };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("Status");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("ERROR"));
    assertEquals(0, compiler.getErrorCount());
  }

  // Tests typedef annotation evaluation and registration
  @Test
  public void testCreateScope_typedefDeclaration_registersDeclaredType() {
    String js = "/** @typedef {string|number} */ var StringOrNumber;";
    Scope scope = createGlobalScope(js);

    JSType registeredType = compiler.getTypeRegistry().getType("StringOrNumber");
    assertNotNull(registeredType);
    assertTrue(registeredType.isUnionType());
  }

  // Tests catch block variable definition in local scope
  @Test
  public void testCreateScope_catchBlock_definesCatchVariable() {
    String js = "function testCatch() { try { var x = 1; } catch (err) { var y = err; } }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node script = root.getFirstChild();
    Node fnNode = script.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode.getLastChild(), globalScope);

    assertNotNull(localScope.getVar("err"));
  }

  // Tests @lends annotation attached to object literal
  @Test
  public void testCreateScope_lendsAnnotation_attachesProperties() {
    String js = "/** @constructor */ function Widget() {}\n"
        + "var props = /** @lends {Widget.prototype} */ ({ render: function() {} });";
    Scope scope = createGlobalScope(js);

    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType widgetType = widgetVar.getType().toMaybeFunctionType();
    ObjectType proto = widgetType.getPrototype();
    assertTrue(proto.hasProperty("render"));
  }

  // Tests qualified stub property declaration registration
  @Test
  public void testCreateScope_stubDeclaration_registersProperty() {
    String js = "var ns = {}; ns.stubProperty;";
    Scope scope = createGlobalScope(js);

    Scope.Var stubVar = scope.getVar("ns.stubProperty");
    assertNotNull(stubVar);
    assertTrue(stubVar.isTypeInferred());
  }

  // Tests multiple variables in a single var statement
  @Test
  public void testCreateScope_multipleVarDeclaration_definesAll() {
    String js = "var a = 1, b = 'hello', c = true;";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));
    assertTrue(scope.getVar("a").getType().isNumber());
    assertTrue(scope.getVar("b").getType().isString());
    assertTrue(scope.getVar("c").getType().isBooleanValueType());
  }

  // Tests interface type declaration with @interface annotation
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    String js = "/** @interface */ function Disposable() {}\n"
        + "Disposable.prototype.dispose = function() {};";
    Scope scope = createGlobalScope(js);

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
  }

  // Tests patchGlobalScope when updating script nodes
  @Test
  public void testPatchGlobalScope_updatesGlobalScopeVariables() {
    String js1 = "var oldVar = 10;";
    Node root1 = compiler.parseTestCode(js1);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root1, null);

    assertNotNull(globalScope.getVar("oldVar"));

    String js2 = "var newVar = 20;";
    Node root2 = compiler.parseTestCode(js2);
    Node script2 = root2.getFirstChild();

    scopeCreator.patchGlobalScope(globalScope, script2);

    assertNotNull(globalScope.getVar("newVar"));
  }

  // Tests global Window constructor setup for GlobalThis prototype
  @Test
  public void testCreateScope_windowConstructor_modifiesGlobalThis() {
    String js = "/** @constructor */ function Window() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var windowVar = scope.getVar("Window");
    assertNotNull(windowVar);
    assertTrue(windowVar.getType().isConstructor());
  }

  // Tests function with @param and @return JSDoc annotations
  @Test
  public void testCreateScope_annotatedFunction_resolvesParamAndReturnTypes() {
    String js = "/** @param {string} a\n * @return {number} */\nfunction f(a) { return 0; }";
    Scope scope = createGlobalScope(js);
    Scope.Var fVar = scope.getVar("f");

    assertNotNull(fVar);
    FunctionType fnType = fVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getReturnType().isNumber());
  }

  // Tests prototype method assignment creates property on prototype
  @Test
  public void testCreateScope_prototypeMethodDeclaration_registersMethod() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.bar = function(x) { return x; };";
    Scope scope = createGlobalScope(js);

    Scope.Var barVar = scope.getVar("Foo.prototype.bar");
    assertNotNull(barVar);
    assertTrue(barVar.getType().isFunctionType());
  }

  // Tests namespaced property with declared @type annotation
  @Test
  public void testCreateScope_namespacedPropertyWithJSDoc_hasDeclaredType() {
    String js = "var ns = {};\n/** @type {string} */ ns.prop = 'hello';";
    Scope scope = createGlobalScope(js);

    Scope.Var propVar = scope.getVar("ns.prop");
    assertNotNull(propVar);
    assertTrue(propVar.getType().isString());
    assertFalse(propVar.isTypeInferred());
  }

  // Tests named function expression does not pollute global scope
  @Test
  public void testCreateScope_namedFunctionExpression_definesNameInLocalScope() {
    String js = "var f = function myNamedFn() {};";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    assertNotNull(globalScope.getVar("f"));
    assertNull(globalScope.getVar("myNamedFn"));
  }

  // Tests function with @this JSDoc annotation
  @Test
  public void testCreateScope_functionWithThisAnnotation_bindsThisType() {
    String js = "/** @this {Array} */ function arrayFn() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("arrayFn");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertNotNull(fnType.getTypeOfThis());
    assertTrue(fnType.getTypeOfThis().isObject());
  }

  // Tests stub declaration with @type JSDoc annotation
  @Test
  public void testCreateScope_stubDeclarationWithType_declaresPropertyType() {
    String js = "var ns = {};\n/** @type {number} */ ns.count;";
    Scope scope = createGlobalScope(js);

    Scope.Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertTrue(countVar.getType().isNumber());
    assertFalse(countVar.isTypeInferred());
  }

  // Tests optional and var_args parameter type declarations
  @Test
  public void testCreateScope_optionalAndVarArgs_registersCorrectTypes() {
    String js = "/** @param {string=} opt_a\n * @param {...number} var_args */\nfunction variadic(opt_a, var_args) {}";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("variadic");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
  }

  // Tests object literal properties inferred on type
  @Test
  public void testCreateScope_objectLiteralTypes_infersPropertyTypes() {
    String js = "var obj = { a: 1, b: 'str' };";
    Scope scope = createGlobalScope(js);

    Scope.Var objVar = scope.getVar("obj");
    assertNotNull(objVar);
    ObjectType objType = objVar.getType().toObjectType();
    assertNotNull(objType);
    assertTrue(objType.hasProperty("a"));
    assertTrue(objType.hasProperty("b"));
  }

  // Tests anonymous function in call expression creating local scope
  @Test
  public void testCreateScope_anonymousFunctionInCall_createsLocalScope() {
    String js = "(function(x) { var y = x; })(10);";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node script = root.getFirstChild();
    Node exprResult = script.getFirstChild();
    Node callNode = exprResult.getFirstChild();
    Node fnNode = callNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode.getLastChild(), globalScope);

    assertNotNull(localScope.getVar("x"));
    assertNotNull(localScope.getVar("y"));
  }

  // Tests duplicate var declaration in the same scope
  @Test
  public void testCreateScope_duplicateVarDeclaration_handlesRedeclaration() {
    String js = "var a = 1; var a = 2;";
    Scope scope = createGlobalScope(js);

    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertTrue(varA.getType().isNumber());
  }
}