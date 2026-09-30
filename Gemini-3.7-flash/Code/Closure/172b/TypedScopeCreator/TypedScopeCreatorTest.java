package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
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
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  // Tests primitive literal variable declarations in global scope
  @Test
  public void testCreateScope_primitiveLiterals_declaresCorrectTypes() {
    String js = "var n = 1; var s = 'hello'; var b = true; var nl = null; var u = undefined; var r = /abc/;";
    Scope scope = createGlobalScope(js);

    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        scope.getVar("n").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        scope.getVar("s").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        scope.getVar("b").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE),
        scope.getVar("nl").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE),
        scope.getVar("u").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.REGEXP_TYPE),
        scope.getVar("r").getType());
  }

  // Tests native types declared in initial scope
  @Test
  public void testCreateInitialScope_nativeTypesDeclared() {
    String js = "";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
  }

  // Tests function declaration and JSDoc parameter/return types
  @Test
  public void testCreateScope_functionDeclaration_declaresFunctionType() {
    String js = "/** @param {number} x\n * @return {string} */ function f(x) { return 'a'; }";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("f");
    assertNotNull(var);
    assertTrue(var.getType().isFunctionType());

    FunctionType fnType = var.getType().toMaybeFunctionType();
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        fnType.getReturnType());
  }

  // Tests constructor declaration and automatic prototype slot creation
  @Test
  public void testCreateScope_constructorDeclaration_createsInstanceAndPrototype() {
    String js = "/** @constructor */ function Foo() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("Foo");
    assertNotNull(var);
    FunctionType fnType = var.getType().toMaybeFunctionType();
    assertTrue(fnType.isConstructor());

    Scope.Var protoVar = scope.getVar("Foo.prototype");
    assertNotNull(protoVar);
    assertEquals(fnType.getPrototype(), protoVar.getType());
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    String js = "/** @interface */ function AnInterface() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("AnInterface");
    assertNotNull(var);
    FunctionType fnType = var.getType().toMaybeFunctionType();
    assertTrue(fnType.isInterface());
  }

  // Tests enum declaration with elements
  @Test
  public void testCreateScope_enumDeclaration_createsEnumTypeAndElements() {
    String js = "/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("MyEnum");
    assertNotNull(var);
    assertTrue(var.getType().isEnumType());

    EnumType enumType = (EnumType) var.getType();
    assertTrue(enumType.getElements().contains("FIRST"));
    assertTrue(enumType.getElements().contains("SECOND"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        enumType.getElementsType());
  }

  // Tests typedef declaration and alias resolution
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    String js = "/** @typedef {(string|number)} */ var StringOrNum;";
    Scope scope = createGlobalScope(js);

    JSType type = compiler.getTypeRegistry().getType("StringOrNum");
    assertNotNull(type);
    assertTrue(type.isUnionType());
  }

  // Tests qualified name property declaration
  @Test
  public void testCreateScope_qualifiedName_definesDeclaredProperty() {
    String js = "var ns = {}; /** @type {number} */ ns.count = 0;";
    Scope scope = createGlobalScope(js);

    Scope.Var nsVar = scope.getVar("ns");
    assertNotNull(nsVar);
    ObjectType nsType = ObjectType.cast(nsVar.getType());
    assertNotNull(nsType);
    assertTrue(nsType.hasProperty("count"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        nsType.getPropertyType("count"));
  }

  // Tests @const property inference and constancy behavior
  @Test
  public void testCreateScope_constVariable_inferredCorrectly() {
    String js = "/** @const */ var MY_CONST = 42;";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("MY_CONST");
    assertNotNull(var);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        var.getType());
    assertFalse(var.isTypeInferred());
  }

  // Tests @const qualified name inferred vs declared
  @Test
  public void testCreateScope_constQualifiedName_inferredCorrectly() {
    String js = "var goog = {}; /** @const */ goog.FOO = 'bar';";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("goog.FOO");
    assertNotNull(var);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        var.getType());
    assertFalse(var.isTypeInferred());
  }

  // Tests local scope creation and parameter handling
  @Test
  public void testCreateScope_localScope_definesParametersAndLocalVars() {
    String js = "function outer(param1) { var localVar = 10; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node scriptNode = root.getLastChild();
    Node functionNode = scriptNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("localVar"));
    assertEquals(globalScope, localScope.getParent());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        localScope.getVar("localVar").getType());
  }

  // Tests catch block scope variable declaration
  @Test
  public void testCreateScope_catchBlock_definesCatchVar() {
    String js = "function testCatch() { try { } catch (e) { var x = e; } }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node scriptNode = root.getLastChild();
    Node functionNode = scriptNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE),
        localScope.getVar("e").getType());
  }

  // Tests @lends annotation on object literals
  @Test
  public void testCreateScope_lendsAnnotation_attachesPropertiesToTarget() {
    String js = "/** @constructor */ function Person() {}\n"
        + "Person.prototype = /** @lends {Person.prototype} */ ({\n"
        + "  /** @type {string} */ name: 'anonymous'\n"
        + "});";
    Scope scope = createGlobalScope(js);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType fnType = personVar.getType().toMaybeFunctionType();
    ObjectType protoType = fnType.getPrototype();
    assertTrue(protoType.hasProperty("name"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        protoType.getPropertyType("name"));
  }

  // Tests stub declarations resolving to unknown type
  @Test
  public void testCreateScope_stubDeclaration_resolvesToUnknown() {
    String js = "var ns = {}; ns.stubProp;";
    Scope scope = createGlobalScope(js);

    Scope.Var stubVar = scope.getVar("ns.stubProp");
    assertNotNull(stubVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.UNKNOWN_TYPE),
        stubVar.getType());
  }

  // Tests multiple var declaration with JSDoc emits warning
  @Test
  public void testCreateScope_multipleVarDeclarationWithJsDoc_reportsWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests @lends with unknown variable emits warning
  @Test
  public void testCreateScope_unknownLendsTarget_reportsWarning() {
    String js = "var obj = /** @lends {NonExistent} */ ({ a: 1 });";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests constructor initialization missing emits warning
  @Test
  public void testCreateScope_uninitializedConstructor_reportsWarning() {
    String js = "/** @constructor */ var MyClass;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
  }

  // Tests patching global scope when modifying script
  @Test
  public void testPatchGlobalScope_removesOldVarsAndAddsNewVars() {
    String js1 = "var x = 1; var y = 2;";
    Node root = compiler.parseTestCode(js1);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    assertNotNull(globalScope.getVar("x"));
    assertNotNull(globalScope.getVar("y"));

    String js2 = "var x = 1; var z = 3;";
    Node newScript = compiler.parseTestCode(js2).getLastChild();
    scopeCreator.patchGlobalScope(globalScope, newScript);

    assertNotNull(globalScope.getVar("x"));
    assertNull(globalScope.getVar("y"));
    assertNotNull(globalScope.getVar("z"));
  }

  // Tests conditional assignment to qualified function name remains inferred
  @Test
  public void testCreateScope_conditionalFunctionAssignment_isInferred() {
    String js = "var ns = {}; if (true) { ns.fn = function() {}; }";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("ns.fn");
    assertNotNull(fnVar);
    assertTrue(fnVar.isTypeInferred());
  }

  // Tests method definition on prototype inheriting from overridden function
  @Test
  public void testCreateScope_prototypeMethodOverride_infersBaseSignature() {
    String js = "/** @constructor */ function Base() {}\n"
        + "/** @param {number} x\n * @return {string} */\n"
        + "Base.prototype.foo = function(x) { return ''; };\n"
        + "/** @constructor\n * @extends {Base} */ function Sub() {}\n"
        + "Sub.prototype = new Base();\n"
        + "Sub.prototype.foo = function(x) { return 'sub'; };";
    Scope scope = createGlobalScope(js);

    Scope.Var subVar = scope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    ObjectType subProto = subCtor.getPrototype();
    JSType fooType = subProto.getPropertyType("foo");
    assertTrue(fooType.isFunctionType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        fooType.toMaybeFunctionType().getReturnType());
  }

  // Tests interface implementation and registration
  @Test
  public void testCreateScope_implementsInterface_registersImplementedInterfaces() {
    String js = "/** @interface */ function Disposable() {}\n"
        + "/** @constructor\n * @implements {Disposable} */ function Sub() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var subVar = scope.getVar("Sub");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertTrue(subCtor.getImplementedInterfaces().iterator().hasNext());
  }

  // Tests interface extending another interface
  @Test
  public void testCreateScope_interfaceExtendsInterface_registersExtendedInterfaces() {
    String js = "/** @interface */ function InterfaceA() {}\n"
        + "/** @interface\n * @extends {InterfaceA} */ function InterfaceB() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var bVar = scope.getVar("InterfaceB");
    assertNotNull(bVar);
    FunctionType bType = bVar.getType().toMaybeFunctionType();
    assertTrue(bType.isInterface());
    assertTrue(bType.getExtendedInterfaces().iterator().hasNext());
  }

  // Tests @this annotation on function declaration
  @Test
  public void testCreateScope_thisAnnotation_setsTypeOfThis() {
    String js = "/** @this {Date} */ function formatDate() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("formatDate");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertEquals(compiler.getTypeRegistry().getType("Date"), fnType.getTypeOfThis());
  }

  // Tests optional and var_args parameters in function types
  @Test
  public void testCreateScope_optionalAndVarArgsParameters_createsCorrectParameters() {
    String js = "/** @param {number=} opt_x\n * @param {...string} var_args */ function fn(opt_x, var_args) {}";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("fn");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    Node paramNode = fnType.getParametersNode().getFirstChild();
    assertTrue(paramNode.isOptionalArg());
    Node varArgsNode = paramNode.getNext();
    assertTrue(varArgsNode.isVarArgs());
  }

  // Tests 'arguments' object defined automatically in local function scope
  @Test
  public void testCreateScope_argumentsInLocalScope_definesArgumentsVar() {
    String js = "function testFn() { return arguments; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node scriptNode = root.getLastChild();
    Node functionNode = scriptNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    Scope.Var argsVar = localScope.getVar("arguments");
    assertNotNull(argsVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.ARGUMENTS_TYPE),
        argsVar.getType());
  }

  // Tests named function expression creates local variable for the function name
  @Test
  public void testCreateScope_namedFunctionExpression_definesInLocalScope() {
    String js = "var f = function namedFn() {};";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node varNode = root.getLastChild().getFirstChild();
    Node fnNode = varNode.getFirstChild().getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("namedFn"));
    assertNull(globalScope.getVar("namedFn"));
  }

  // Tests @define annotation creates non-inferred variable
  @Test
  public void testCreateScope_defineAnnotation_marksVarAsDeclared() {
    String js = "/** @define {boolean} */ var COMPILED = false;";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("COMPILED");
    assertNotNull(var);
    assertFalse(var.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        var.getType());
  }

  // Tests prototype stub property declaration with JSDoc
  @Test
  public void testCreateScope_prototypeStubProperty_declaredOnPrototype() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "/** @type {number} */ Foo.prototype.count;";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    ObjectType protoType = fooVar.getType().toMaybeFunctionType().getPrototype();
    assertTrue(protoType.hasProperty("count"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        protoType.getPropertyType("count"));
  }

  // Tests nested object literal and namespace property declaration
  @Test
  public void testCreateScope_nestedNamespaceProperties_buildsObjectChain() {
    String js = "var a = {}; a.b = {}; /** @type {string} */ a.b.c = 'hello';";
    Scope scope = createGlobalScope(js);

    Scope.Var aVar = scope.getVar("a");
    assertNotNull(aVar);
    Scope.Var cVar = scope.getVar("a.b.c");
    assertNotNull(cVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        cVar.getType());
  }

  // Tests @template annotation on generic function
  @Test
  public void testCreateScope_templateAnnotation_createsTemplateFunction() {
    String js = "/** @template T\n * @param {T} x\n * @return {T} */ function identity(x) { return x; }";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("identity");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertTrue(fnType.isGeneric());
    assertEquals(1, fnType.getTemplateTypeMap().getTemplateKeys().size());
  }
}