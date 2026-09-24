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
    options.setCheckTypes(true);
    compiler.initOptions(options);
  }

  private Node parse(String js) {
    return compiler.parseTestCode(js);
  }

  private Scope createGlobalScope(Node root) {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  // Tests basic variable declaration in global scope
  @Test
  public void testCreateScope_varDeclaration_declaresVariable() {
    Node root = parse("var a = 1;");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("a", false));
    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), varA.getType());
  }

  // Tests string, boolean and null literal type inference
  @Test
  public void testCreateScope_literalTypes_inferredCorrectly() {
    Node root = parse("var str = 'hello'; var b = true; var n = null;");
    Scope scope = createGlobalScope(root);

    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        scope.getVar("str").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        scope.getVar("b").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE),
        scope.getVar("n").getType());
  }

  // Tests function declaration and function type creation
  @Test
  public void testCreateScope_functionDeclaration_declaresFunction() {
    Node root = parse("function foo(x, y) { return x; }");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("foo", false));
    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isFunctionType());
  }

  // Tests constructor declaration and prototype creation
  @Test
  public void testCreateScope_constructor_declaresConstructorAndPrototype() {
    Node root = parse("/** @constructor */ function MyClass() {}");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("MyClass", false));
    assertTrue(scope.isDeclared("MyClass.prototype", false));
    Scope.Var classVar = scope.getVar("MyClass");
    assertTrue(classVar.getType().isConstructor());
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interface_declaresInterfaceType() {
    Node root = parse("/** @interface */ function MyInterface() {}");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("MyInterface", false));
    Scope.Var ifaceVar = scope.getVar("MyInterface");
    assertTrue(ifaceVar.getType().isInterface());
  }

  // Tests enum declaration and element type verification
  @Test
  public void testCreateScope_enumDeclaration_declaresEnumType() {
    Node root = parse("/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("MyEnum", false));
    Scope.Var enumVar = scope.getVar("MyEnum");
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("FIRST"));
    assertTrue(enumType.hasOwnProperty("SECOND"));
  }

  // Tests typedef declaration in global scope
  @Test
  public void testCreateScope_typedef_registersType() {
    Node root = parse("/** @typedef {(string|number)} */ var StringOrNum;");
    Scope scope = createGlobalScope(root);

    JSType typedefType = compiler.getTypeRegistry().getType("StringOrNum");
    assertNotNull(typedefType);
    assertTrue(typedefType.isUnionType());
  }

  // Tests local scope creation inside function
  @Test
  public void testCreateScope_localScope_containsParametersAndLocalVars() {
    Node root = parse("function outer(param1) { var localVar = 'test'; }");
    Scope globalScope = createGlobalScope(root);

    Node fnNode = root.getFirstChild();
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("param1", false));
    assertTrue(localScope.isDeclared("localVar", false));
    assertFalse(localScope.isGlobal());
  }

  // Tests catch parameter declaration in local scope
  @Test
  public void testCreateScope_catchBlock_declaresCatchVariable() {
    Node root = parse("function testCatch() { try { } catch (err) { } }");
    Scope globalScope = createGlobalScope(root);

    Node fnNode = root.getFirstChild();
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertTrue(localScope.isDeclared("err", true));
  }

  // Tests qualified name assignment on prototype
  @Test
  public void testCreateScope_prototypePropertyAssignment_declaresProperty() {
    Node root = parse("/** @constructor */ function Foo() {} Foo.prototype.bar = function() { return 1; };");
    Scope scope = createGlobalScope(root);

    Scope.Var fooVar = scope.getVar("Foo");
    FunctionType fooCtor = fooVar.getType().toMaybeFunctionType();
    ObjectType proto = fooCtor.getPrototype();
    assertTrue(proto.hasProperty("bar"));
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_appliesPropertiesToTarget() {
    Node root = parse(
        "/** @constructor */ function Target() {}\n"
        + "var obj = /** @lends {Target.prototype} */ ({ method: function() {} });");
    Scope scope = createGlobalScope(root);

    Scope.Var targetVar = scope.getVar("Target");
    FunctionType targetCtor = targetVar.getType().toMaybeFunctionType();
    ObjectType proto = targetCtor.getPrototype();
    assertTrue(proto.hasProperty("method"));
  }

  // Tests @lends with subproperties and namespaced objects
  @Test
  public void testCreateScope_lendsOnNamespacedObject_attachesTypes() {
    Node root = parse(
        "/** @constructor */ function Person() {}\n"
        + "Person.methods = /** @lends {Person.prototype} */ ({ "
        + "  getName: function() { return 'name'; }"
        + "});");
    Scope scope = createGlobalScope(root);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType personCtor = personVar.getType().toMaybeFunctionType();
    assertTrue(personCtor.getPrototype().hasProperty("getName"));
  }

  // Tests initial scope containing native types
  @Test
  public void testCreateInitialScope_containsNativeTypes() {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Node root = parse("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertTrue(scope.isDeclared("Object", false));
    assertTrue(scope.isDeclared("Function", false));
    assertTrue(scope.isDeclared("Array", false));
    assertTrue(scope.isDeclared("String", false));
    assertTrue(scope.isDeclared("Number", false));
    assertTrue(scope.isDeclared("Boolean", false));
    assertTrue(scope.isDeclared("RegExp", false));
    assertTrue(scope.isDeclared("Date", false));
    assertTrue(scope.isDeclared("Error", false));
    assertTrue(scope.isDeclared("undefined", false));
  }

  // Tests multiple var definitions warning
  @Test
  public void testCreateScope_multipleVarDefWithJSDoc_reportsWarning() {
    Node root = parse("/** @type {number} */ var a = 1, b = 2;");
    createGlobalScope(root);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests invalid enum initializer reporting warning
  @Test
  public void testCreateScope_invalidEnumInitializer_reportsWarning() {
    Node root = parse("/** @enum {number} */ var MyEnum = 123;");
    createGlobalScope(root);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key,
        compiler.getWarnings()[0].getType().key);
  }

  // Tests patchGlobalScope updating script variables
  @Test
  public void testPatchGlobalScope_updatesVariables() {
    Node scriptRoot1 = parse("var x = 10;");
    scriptRoot1.setStaticSourceFile(new SimpleSourceFile("test.js", false));
    Scope globalScope = createGlobalScope(scriptRoot1);

    assertTrue(globalScope.isDeclared("x", false));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        globalScope.getVar("x").getType());

    Node scriptRoot2 = parse("var x = 'updated';");
    scriptRoot2.setStaticSourceFile(new SimpleSourceFile("test.js", false));

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    scopeCreator.patchGlobalScope(globalScope, scriptRoot2);

    assertTrue(globalScope.isDeclared("x", false));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        globalScope.getVar("x").getType());
  }

  // Tests stub property declaration resolved to unknown
  @Test
  public void testCreateScope_stubPropertyDeclaration_resolvesStub() {
    Node root = parse("var ns = {}; ns.stubProp;");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("ns", false));
    Scope.Var nsVar = scope.getVar("ns");
    assertNotNull(nsVar);
  }

  // Tests @const annotation on qualified name
  @Test
  public void testCreateScope_constAnnotation_infersDeclaredType() {
    Node root = parse("var ns = {}; /** @const */ ns.CONSTANT_VAL = 42;");
    Scope scope = createGlobalScope(root);

    assertTrue(scope.isDeclared("ns.CONSTANT_VAL", false));
    Scope.Var constVar = scope.getVar("ns.CONSTANT_VAL");
    assertNotNull(constVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        constVar.getType());
  }

  // Tests object literal with @lends pointing to unknown variable
  @Test
  public void testCreateScope_unknownLends_reportsWarning() {
    Node root = parse("var obj = /** @lends {NonExistentTarget} */ ({ prop: 1 });");
    createGlobalScope(root);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS.key,
        compiler.getWarnings()[0].getType().key);
  }
}