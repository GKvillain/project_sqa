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
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(root, null);
  }

  private Scope createLocalScope(Scope parentScope, Node functionNode) {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(functionNode, parentScope);
  }

  // Tests native types initialized in global scope
  @Test
  public void testCreateInitialScope_nativeTypes_registered() {
    Scope scope = createGlobalScope("");

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  // Tests declared variable with @type annotation
  @Test
  public void testCreateScope_declaredVarWithType_createsDeclaredSlot() {
    Scope scope = createGlobalScope("/** @type {number} */ var x;");

    Scope.Var varX = scope.getVar("x");
    assertNotNull(varX);
    assertFalse(varX.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getType("number"), varX.getType());
  }

  // Tests untyped variable declaration (inferred type)
  @Test
  public void testCreateScope_untypedVar_createsInferredSlot() {
    Scope scope = createGlobalScope("var x = 1;");

    Scope.Var varX = scope.getVar("x");
    assertNotNull(varX);
    assertTrue(varX.isTypeInferred());
  }

  // Tests function declaration with return and param annotations
  @Test
  public void testCreateScope_functionDeclarationWithJSDoc_createsFunctionType() {
    Scope scope = createGlobalScope(
        "/**\n" +
        " * @param {string} a\n" +
        " * @return {number}\n" +
        " */\n" +
        "function foo(a) { return 0; }");

    Scope.Var fooVar = scope.getVar("foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);

    FunctionType fnType = (FunctionType) fooVar.getType();
    assertEquals(compiler.getTypeRegistry().getType("number"), fnType.getReturnType());
  }

  // Tests function declaration with void return type (Defects4J Closure 144 regression)
  @Test
  public void testCreateScope_functionDeclarationWithVoidReturn_hasVoidReturnType() {
    Scope scope = createGlobalScope(
        "/**\n" +
        " * @param {number} x\n" +
        " * @return {void}\n" +
        " */\n" +
        "function bar(x) {}");

    Scope.Var barVar = scope.getVar("bar");
    assertNotNull(barVar);
    assertTrue(barVar.getType() instanceof FunctionType);

    FunctionType fnType = (FunctionType) barVar.getType();
    assertEquals(compiler.getTypeRegistry().getType("undefined"), fnType.getReturnType());
  }

  // Tests constructor declaration and prototype property scope registration
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototype() {
    Scope scope = createGlobalScope("/** @constructor */ function MyClass() {}");

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    assertTrue(classVar.getType() instanceof FunctionType);

    FunctionType ctorType = (FunctionType) classVar.getType();
    assertTrue(ctorType.isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull(protoVar);
    assertEquals(ctorType.getPrototype(), protoVar.getType());
  }

  // Tests constructor with @this property collection
  @Test
  public void testCreateScope_constructorWithThisProperties_collectsProperties() {
    Scope scope = createGlobalScope(
        "/** @constructor */\n" +
        "function Person() {\n" +
        "  /** @type {string} */\n" +
        "  this.name = 'John';\n" +
        "}");

    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    FunctionType ctor = (FunctionType) personVar.getType();
    ObjectType instanceType = ctor.getInstanceType();

    assertTrue(instanceType.hasProperty("name"));
    assertEquals(compiler.getTypeRegistry().getType("string"),
        instanceType.getPropertyType("name"));
  }

  // Tests local scope creation and parameter slots
  @Test
  public void testCreateScope_localScope_declaresParametersAndLocalVars() {
    Node root = compiler.parseTestCode(
        "/**\n" +
        " * @param {string} a\n" +
        " * @return {number}\n" +
        " */\n" +
        "function outer(a) {\n" +
        "  /** @type {number} */\n" +
        "  var b = 1;\n" +
        "  return b;\n" +
        "}");

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node functionNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertTrue(localScope.isLocal());
    assertNotNull(localScope.getVar("a"));
    assertNotNull(localScope.getVar("b"));
    assertEquals(compiler.getTypeRegistry().getType("number"),
        localScope.getVar("b").getType());
  }

  // Tests catch block scope variable in local scope
  @Test
  public void testCreateScope_catchBlock_definesLocalSlot() {
    Node root = compiler.parseTestCode(
        "function fn() {\n" +
        "  try {\n" +
        "  } catch (e) {\n" +
        "    var localInsideCatch = 1;\n" +
        "  }\n" +
        "}");

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("localInsideCatch"));
  }

  // Tests valid enum creation
  @Test
  public void testCreateScope_enumDeclaration_definesEnumElements() {
    Scope scope = createGlobalScope(
        "/** @enum {number} */\n" +
        "var Colors = {\n" +
        "  RED: 1,\n" +
        "  BLUE: 2\n" +
        "};");

    Scope.Var colorsVar = scope.getVar("Colors");
    assertNotNull(colorsVar);
    assertTrue(colorsVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) colorsVar.getType();
    assertTrue(enumType.hasOwnProperty("RED"));
    assertTrue(enumType.hasOwnProperty("BLUE"));
    assertEquals(0, compiler.getWarningCount());
  }

  // Tests duplicate key warning in enum definition
  @Test
  public void testCreateScope_duplicateEnumKey_reportsWarning() {
    createGlobalScope(
        "/** @enum {number} */\n" +
        "var DuplicateEnum = {\n" +
        "  KEY: 1,\n" +
        "  KEY: 2\n" +
        "};");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_DUP,
        compiler.getWarnings()[0].getType());
  }

  // Tests non-constant key warning in enum definition
  @Test
  public void testCreateScope_nonConstantEnumKey_reportsWarning() {
    createGlobalScope(
        "/** @enum {number} */\n" +
        "var BadEnum = {\n" +
        "  lowerCaseKey: 1\n" +
        "};");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.ENUM_NOT_CONSTANT,
        compiler.getWarnings()[0].getType());
  }

  // Tests typedef declaration and registration in TypeRegistry
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    createGlobalScope("/** @typedef {number|string} */ var StringOrNumber;");

    JSType declaredType = compiler.getTypeRegistry().getType("StringOrNumber");
    assertNotNull(declaredType);
    assertTrue(declaredType.isUnionType());
  }

  // Tests multiple var declarations in single statement with JSDoc
  @Test
  public void testCreateScope_multipleVarDeclarationWithJSDoc_reportsWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF,
        compiler.getWarnings()[0].getType());
  }

  // Tests stub property declaration on namespace
  @Test
  public void testCreateScope_stubPropertyDeclaration_registersProperty() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "ns.stubProp;");

    Scope.Var stubVar = scope.getVar("ns.stubProp");
    assertNotNull(stubVar);
    assertTrue(stubVar.isTypeInferred());
  }

  // Tests method overriding prototype type inference
  @Test
  public void testCreateScope_prototypeMethodOverride_infersType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Base() {}\n" +
        "/** @param {string} x\n" +
        "  * @return {boolean} */\n" +
        "Base.prototype.foo = function(x) { return true; };\n" +
        "/** @constructor\n" +
        "  * @extends {Base} */ function Sub() {}\n" +
        "goog.inherits(Sub, Base);\n" +
        "Sub.prototype.foo = function(x) { return false; };");

    Scope.Var subFoo = scope.getVar("Sub.prototype.foo");
    assertNotNull(subFoo);
    assertTrue(subFoo.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) subFoo.getType();
    assertEquals(compiler.getTypeRegistry().getType("boolean"), fnType.getReturnType());
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    Scope scope = createGlobalScope(
        "/** @interface */ function Disposable() {}\n" +
        "Disposable.prototype.dispose = function() {};");

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType() instanceof FunctionType);
    FunctionType ifaceType = (FunctionType) ifaceVar.getType();
    assertTrue(ifaceType.isInterface());
  }

  // Tests namespaced function expression assignment
  @Test
  public void testCreateScope_namespacedFunctionExpression_infersSignature() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "/**\n" +
        " * @param {string} name\n" +
        " * @return {boolean}\n" +
        " */\n" +
        "ns.check = function(name) { return name.length > 0; };");

    Scope.Var checkVar = scope.getVar("ns.check");
    assertNotNull(checkVar);
    assertTrue(checkVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) checkVar.getType();
    assertEquals(compiler.getTypeRegistry().getType("boolean"), fnType.getReturnType());
    assertFalse(checkVar.isTypeInferred());
  }

  // Tests object literal property declaration with JSDoc
  @Test
  public void testCreateScope_objectLiteralWithJSDocProperties_definesTypedProperties() {
    Scope scope = createGlobalScope(
        "var config = {\n" +
        "  /** @type {number} */\n" +
        "  timeout: 5000,\n" +
        "  /** @type {string} */\n" +
        "  host: 'localhost'\n" +
        "};");

    Scope.Var configVar = scope.getVar("config");
    assertNotNull(configVar);
    ObjectType objType = ObjectType.cast(configVar.getType());
    assertNotNull(objType);
    assertEquals(compiler.getTypeRegistry().getType("number"), objType.getPropertyType("timeout"));
    assertEquals(compiler.getTypeRegistry().getType("string"), objType.getPropertyType("host"));
  }

  // Tests function with @this type annotation
  @Test
  public void testCreateScope_functionWithThisAnnotation_bindsThisType() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function Context() {}\n" +
        "/** @this {Context} */ function handler() { return this; }");

    Scope.Var handlerVar = scope.getVar("handler");
    assertNotNull(handlerVar);
    assertTrue(handlerVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) handlerVar.getType();
    ObjectType thisType = fnType.getTypeOfThis().toObjectType();
    assertNotNull(thisType);
    assertEquals(compiler.getTypeRegistry().getType("Context"), thisType);
  }

  // Tests local scope arguments variable presence
  @Test
  public void testCreateScope_localScope_containsArgumentsVar() {
    Node root = compiler.parseTestCode("function fn() { return arguments.length; }");
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getArgumentsVar());
    assertEquals("arguments", localScope.getArgumentsVar().getName());
  }

  // Tests prototype inheritance via prototype assignment
  @Test
  public void testCreateScope_prototypeAssignmentInheritance_registersSubtypePrototype() {
    Scope scope = createGlobalScope(
        "/** @constructor */ function SuperClass() {}\n" +
        "SuperClass.prototype.superMethod = function() {};\n" +
        "/** @constructor\n" +
        " *  @extends {SuperClass} */\n" +
        "function SubClass() {}\n" +
        "SubClass.prototype = new SuperClass();");

    Scope.Var subProtoVar = scope.getVar("SubClass.prototype");
    assertNotNull(subProtoVar);
    FunctionType subCtor = (FunctionType) scope.getVar("SubClass").getType();
    assertNotNull(subCtor);
    assertTrue(subCtor.isConstructor());
    assertEquals(subCtor.getPrototype(), subProtoVar.getType());
  }
}