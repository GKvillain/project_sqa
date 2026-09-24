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
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

public class TypedScopeCreatorTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  // Tests native bindings initialized in createInitialScope
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Node root = new Node(Token.BLOCK);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createInitialScope(root);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  // Tests global var definition without type annotation
  @Test
  public void testCreateScope_untypedGlobalVar() {
    String js = "var a = 10;";
    Scope scope = createGlobalScope(js);

    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertTrue(varA.isTypeInferred());
  }

  // Tests global var definition with type annotation
  @Test
  public void testCreateScope_typedGlobalVar() {
    String js = "/** @type {number} */ var a = 10;";
    Scope scope = createGlobalScope(js);

    Scope.Var varA = scope.getVar("a");
    assertNotNull(varA);
    assertFalse(varA.isTypeInferred());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), varA.getType());
  }

  // Tests global function declaration
  @Test
  public void testCreateScope_functionDeclaration() {
    String js = "/** @param {string} x\n * @return {number} */ function foo(x) { return 0; }";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("foo");
    assertNotNull(fnVar);
    assertTrue(fnVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) fnVar.getType();
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fnType.getReturnType());
  }

  // Tests constructor function and prototype declaration in global scope
  @Test
  public void testCreateScope_constructorAndPrototype() {
    String js = "/** @constructor */ function Foo() {}\n" +
                "Foo.prototype.bar = function() {};";
    Scope scope = createGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);
    FunctionType ctorType = (FunctionType) fooVar.getType();
    assertTrue(ctorType.isConstructor());

    Scope.Var protoVar = scope.getVar("Foo.prototype");
    assertNotNull(protoVar);
  }

  // Tests local scope creation and parameter inference
  @Test
  public void testCreateScope_localScope() {
    String js = "/** @param {string} x */ function foo(x) { var y = x; }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("x"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), localScope.getVar("x").getType());
    assertNotNull(localScope.getVar("y"));
  }

  // Tests catch block scope slot definition
  @Test
  public void testCreateScope_catchBlockInLocalScope() {
    String js = "function foo() { try { } catch (e) { var z = e; } }";
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("z"));
  }

  // Tests enum definition in global scope
  @Test
  public void testCreateScope_enumDeclaration() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, B: 2 };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("A"));
    assertTrue(enumType.hasOwnProperty("B"));
  }

  // Tests duplicate enum elements report warning
  @Test
  public void testCreateScope_duplicateEnumKeys_reportsWarning() {
    String js = "/** @enum {number} */ var MyEnum = { A: 1, A: 2 };";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_DUP, compiler.getWarnings()[0].getType());
  }

  // Tests non-constant enum key warning
  @Test
  public void testCreateScope_nonConstantEnumKey_reportsWarning() {
    String js = "/** @enum {number} */ var MyEnum = { abc: 1 };";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT, compiler.getWarnings()[0].getType());
  }

  // Tests typedef declaration in global scope
  @Test
  public void testCreateScope_typedefDeclaration() {
    String js = "/** @typedef {{name: string, age: number}} */ var Person;";
    Scope scope = createGlobalScope(js);

    JSType personType = registry.getType("Person");
    assertNotNull(personType);
    assertTrue(personType.isRecordType());
  }

  // Tests malformed typedef reports warning
  @Test
  public void testCreateScope_malformedTypedef_reportsWarning() {
    String js = "/** @typedef {nonexistentType} */ var Malformed;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF, compiler.getWarnings()[0].getType());
  }

  // Tests properties declared on this within constructor
  @Test
  public void testCreateScope_collectPropertiesOnThis() {
    String js = "/** @constructor */ function Person() {\n" +
                "  /** @type {string} */ this.name = 'test';\n" +
                "  /** @type {number} */ this.age = 30;\n" +
                "}";
    Scope scope = createGlobalScope(js);

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType ctorType = (FunctionType) personVar.getType();
    ObjectType instanceType = ctorType.getInstanceType();

    assertTrue(instanceType.hasProperty("name"));
    assertTrue(instanceType.hasProperty("age"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), instanceType.getPropertyType("name"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), instanceType.getPropertyType("age"));
  }

  // Tests goog.inherits subclass relationship handling
  @Test
  public void testCreateScope_subclassInheritance() {
    String js = "var goog = {}; goog.inherits = function(child, parent) {};\n" +
                "/** @constructor */ function SuperClass() {}\n" +
                "/** @constructor\n * @extends {SuperClass} */ function SubClass() {}\n" +
                "goog.inherits(SubClass, SuperClass);";
    Scope scope = createGlobalScope(js);

    Scope.Var subVar = scope.getVar("SubClass");
    assertNotNull(subVar);
    FunctionType subCtor = (FunctionType) subVar.getType();
    assertEquals(scope.getVar("SuperClass").getType(), subCtor.getSuperClassConstructor());
  }

  // Tests multiple var declaration with single JSDoc emits warning
  @Test
  public void testCreateScope_multipleVarDefWithJSDoc_reportsWarning() {
    String js = "/** @type {number} */ var a = 1, b = 2;";
    createGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF, compiler.getWarnings()[0].getType());
  }

  // Tests stub declaration in global scope creates unknown property type
  @Test
  public void testCreateScope_stubDeclaration() {
    String js = "var ns = {}; ns.stubProp;";
    Scope scope = createGlobalScope(js);

    Scope.Var stubVar = scope.getVar("ns.stubProp");
    assertNotNull(stubVar);
    assertTrue(stubVar.isTypeInferred());
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), stubVar.getType());
  }

  // Tests function expression alias
  @Test
  public void testCreateScope_functionAlias() {
    String js = "/** @constructor */ function Base() {}\n" +
                "var Alias = Base;";
    Scope scope = createGlobalScope(js);

    Scope.Var aliasVar = scope.getVar("Alias");
    assertNotNull(aliasVar);
    assertTrue(aliasVar.getType() instanceof FunctionType);
    assertTrue(((FunctionType) aliasVar.getType()).isConstructor());
  }
}