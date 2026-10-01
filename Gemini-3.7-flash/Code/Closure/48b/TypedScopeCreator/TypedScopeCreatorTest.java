package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
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
    return createGlobalScope("", js);
  }

  private Scope createGlobalScope(String externsJs, String js) {
    Node externsNode = compiler.parseTestCode(externsJs);
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);
    return scopeCreator.createScope(root, null);
  }

  // Tests initial global scope contains native types
  @Test
  public void testCreateInitialScope_nativeTypesDeclared_containStandardTypes() {
    Node root = new Node(Token.BLOCK, new Node(Token.SCRIPT), new Node(Token.SCRIPT));
    Scope initialScope = scopeCreator.createInitialScope(root);

    assertNotNull(initialScope.getVar("Object"));
    assertNotNull(initialScope.getVar("Array"));
    assertNotNull(initialScope.getVar("Function"));
    assertNotNull(initialScope.getVar("String"));
    assertNotNull(initialScope.getVar("Number"));
    assertNotNull(initialScope.getVar("Boolean"));
    assertNotNull(initialScope.getVar("Date"));
    assertNotNull(initialScope.getVar("RegExp"));
    assertNotNull(initialScope.getVar("Error"));
    assertNotNull(initialScope.getVar("undefined"));
    assertNotNull(initialScope.getVar("ActiveXObject"));
  }

  // Tests declaration of primitive variables
  @Test
  public void testCreateScope_primitiveVarDeclarations_typesInferredCorrectly() {
    Scope scope = createGlobalScope(
        "var num = 123;\n" +
        "var str = 'hello';\n" +
        "var bool = true;\n" +
        "var n = null;\n" +
        "var undef = void 0;"
    );

    Scope.Var numVar = scope.getVar("num");
    assertNotNull(numVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), numVar.getType());

    Scope.Var strVar = scope.getVar("str");
    assertNotNull(strVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), strVar.getType());

    Scope.Var boolVar = scope.getVar("bool");
    assertNotNull(boolVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), boolVar.getType());

    Scope.Var nullVar = scope.getVar("n");
    assertNotNull(nullVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE), nullVar.getType());

    Scope.Var undefVar = scope.getVar("undef");
    assertNotNull(undefVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.VOID_TYPE), undefVar.getType());
  }

  // Tests function declaration in global scope
  @Test
  public void testCreateScope_functionDeclaration_declaresFunctionType() {
    Scope scope = createGlobalScope("function foo(a, b) { return a; }");
    Scope.Var fooVar = scope.getVar("foo");

    assertNotNull(fooVar);
    assertTrue(fooVar.getType().isFunctionType());
    assertFalse(fooVar.isTypeInferred());
  }

  // Tests local scope creation and parameter handling
  @Test
  public void testCreateScope_localScope_definesParametersAndLocals() {
    String js = "function outer(param1) { var local1 = 42; return param1; }";
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    Scope globalScope = scopeCreator.createScope(root, null);
    Node functionNode = mainNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("local1"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), localScope.getVar("local1").getType());
    assertEquals(globalScope, localScope.getParent());
  }

  // Tests catch block scope variable declaration
  @Test
  public void testCreateScope_catchBlock_definesCatchParameter() {
    String js = "function f() { try { } catch (e) { var x = 1; } }";
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    Scope globalScope = scopeCreator.createScope(root, null);
    Node functionNode = mainNode.getFirstChild();
    Scope localScope = scopeCreator.createScope(functionNode, globalScope);

    assertNotNull(localScope.getVar("e"));
  }

  // Tests object literal properties and structure
  @Test
  public void testCreateScope_objectLiteral_propertiesAttached() {
    Scope scope = createGlobalScope("var obj = { a: 1, b: 'test' };");
    Scope.Var objVar = scope.getVar("obj");

    assertNotNull(objVar);
    JSType type = objVar.getType();
    assertTrue(type.isObjectType());
    ObjectType objType = type.toObjectType();
    assertTrue(objType.hasProperty("a"));
    assertTrue(objType.hasProperty("b"));
  }

  // Tests enum declaration with @enum annotation
  @Test
  public void testCreateScope_enumDeclaration_registersEnumType() {
    Scope scope = createGlobalScope(
        "/** @enum {number} */\n" +
        "var MyEnum = { FIRST: 1, SECOND: 2 };"
    );

    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = enumVar.getType().toMaybeEnumType();
    assertNotNull(enumType);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), enumType.getElementsType());
    assertNotNull(scope.getVar("MyEnum.FIRST"));
    assertNotNull(scope.getVar("MyEnum.SECOND"));
  }

  // Tests typedef declaration in global scope
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    Scope scope = createGlobalScope(
        "/** @typedef {{name: string, age: number}} */\n" +
        "var PersonType;\n" +
        "/** @type {PersonType} */\n" +
        "var p;"
    );

    Scope.Var pVar = scope.getVar("p");
    assertNotNull(pVar);
    assertTrue(pVar.getType().isObjectType());
  }

  // Tests @lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_lendsPropertiesToType() {
    Scope scope = createGlobalScope(
        "/** @constructor */\n" +
        "function MyClass() {}\n" +
        "var methods = /** @lends {MyClass.prototype} */ ({\n" +
        "  foo: function() { return 1; }\n" +
        "});"
    );

    Scope.Var classVar = scope.getVar("MyClass");
    assertNotNull(classVar);
    FunctionType ctor = classVar.getType().toMaybeFunctionType();
    assertNotNull(ctor);
    ObjectType proto = ctor.getPrototype();
    assertTrue(proto.hasProperty("foo"));
  }

  // Tests stub property declarations on qualified names
  @Test
  public void testCreateScope_stubPropertyDeclaration_registersProperty() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "ns.stubProp;\n"
    );

    Scope.Var nsVar = scope.getVar("ns");
    assertNotNull(nsVar);
    assertTrue(scope.isDeclared("ns.stubProp", false));
  }

  // Tests constructor initialization and prototype creation
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototypeSlot() {
    Scope scope = createGlobalScope(
        "/** @constructor */\n" +
        "function Base() {}\n" +
        "Base.prototype.greet = function() {};"
    );

    Scope.Var protoVar = scope.getVar("Base.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObjectType());
    assertTrue(scope.isDeclared("Base.prototype.greet", false));
  }

  // Tests patchGlobalScope to re-traverse modified script
  @Test
  public void testPatchGlobalScope_modifiedScript_updatesGlobalScope() {
    Node externsNode = compiler.parseTestCode("");
    Node script1 = compiler.parseTestCode("var a = 1;");
    script1.setSourceName("file1.js");
    Node root = new Node(Token.BLOCK, externsNode, script1);

    Scope globalScope = scopeCreator.createScope(root, null);
    assertNotNull(globalScope.getVar("a"));

    Node newScript1 = compiler.parseTestCode("var b = 2;");
    newScript1.setSourceName("file1.js");
    scopeCreator.patchGlobalScope(globalScope, newScript1);

    assertNull(globalScope.getVar("a"));
    assertNotNull(globalScope.getVar("b"));
  }

  // Tests multiple var declaration with single JSDoc warning condition
  @Test
  public void testCreateScope_multipleVarDefWithJSDoc_reportsWarning() {
    createGlobalScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests qualified name re-declaration without @type (Closure Defect 48)
  @Test
  public void testCreateScope_qualifiedNameAssignment_inferredProperty() {
    Scope scope = createGlobalScope(
        "var goog = {};\n" +
        "goog.storage = {};\n" +
        "goog.storage.Storage = function() {};\n" +
        "goog.storage.Storage = function() {};"
    );

    Scope.Var storageVar = scope.getVar("goog.storage.Storage");
    assertNotNull(storageVar);
    assertTrue(storageVar.getType().isFunctionType());
  }

  // Tests qualified name declaration with explicit @type annotation
  @Test
  public void testCreateScope_qualifiedNameWithExplicitType_declaredNotUndeclared() {
    Scope scope = createGlobalScope(
        "var ns = {};\n" +
        "/** @type {number} */\n" +
        "ns.count = 0;"
    );

    Scope.Var countVar = scope.getVar("ns.count");
    assertNotNull(countVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), countVar.getType());
    assertFalse(countVar.isTypeInferred());
  }

  // Tests interface definition with @interface
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    Scope scope = createGlobalScope(
        "/** @interface */\n" +
        "function Disposable() {}\n" +
        "Disposable.prototype.dispose = function() {};"
    );

    Scope.Var dispVar = scope.getVar("Disposable");
    assertNotNull(dispVar);
    assertTrue(dispVar.getType().isInterface());
    assertTrue(dispVar.getType().isFunctionType());
  }

  // Tests constructor with @extends and @implements annotations
  @Test
  public void testCreateScope_constructorInheritance_setsUpHierarchy() {
    Scope scope = createGlobalScope(
        "/** @interface */\n" +
        "function Printable() {}\n" +
        "/** @constructor */\n" +
        "function SuperClass() {}\n" +
        "/**\n" +
        " * @constructor\n" +
        " * @extends {SuperClass}\n" +
        " * @implements {Printable}\n" +
        " */\n" +
        "function SubClass() {}"
    );

    Scope.Var subVar = scope.getVar("SubClass");
    assertNotNull(subVar);
    assertTrue(subVar.getType().isConstructor());
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    FunctionType superCtor = subCtor.getSuperClassConstructor();
    assertNotNull(superCtor);
    assertEquals(scope.getVar("SuperClass").getType(), superCtor);
  }

  // Tests annotated function with @param and @return annotations
  @Test
  public void testCreateScope_annotatedFunction_parameterAndReturnTypesSet() {
    Scope scope = createGlobalScope(
        "/**\n" +
        " * @param {string} name\n" +
        " * @param {number=} opt_age\n" +
        " * @return {boolean}\n" +
        " */\n" +
        "function validate(name, opt_age) {\n" +
        "  return true;\n" +
        "}"
    );

    Scope.Var valVar = scope.getVar("validate");
    assertNotNull(valVar);
    FunctionType fnType = valVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertEquals(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        fnType.getReturnType()
    );
  }

  // Tests this-type annotation with @this
  @Test
  public void testCreateScope_annotatedThisType_setsTypeOfThis() {
    String js =
        "/** @constructor */\n" +
        "function Context() {}\n" +
        "/** @this {Context} */\n" +
        "function runInContext() {}";
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    Scope globalScope = scopeCreator.createScope(root, null);
    Node fnNode = mainNode.getLastChild();
    Scope fnScope = scopeCreator.createScope(fnNode, globalScope);

    Scope.Var contextVar = globalScope.getVar("Context");
    FunctionType contextCtor = contextVar.getType().toMaybeFunctionType();
    assertEquals(contextCtor.getInstanceType(), fnScope.getTypeOfThis());
  }

  // Tests getter and setter definitions in object literal
  @Test
  public void testCreateScope_objectLiteralGettersAndSetters_inferredProperties() {
    Scope scope = createGlobalScope(
        "var obj = {\n" +
        "  get x() { return 1; },\n" +
        "  set x(val) {}\n" +
        "};"
    );

    Scope.Var objVar = scope.getVar("obj");
    assertNotNull(objVar);
    assertTrue(objVar.getType().isObjectType());
    ObjectType objType = objVar.getType().toObjectType();
    assertTrue(objType.hasProperty("x"));
  }

  // Tests function expression assigned to variable with recursive reference
  @Test
  public void testCreateScope_namedFunctionExpression_definesLocalName() {
    String js = "var fact = function f(n) { return n <= 1 ? 1 : n * f(n - 1); };";
    Node externsNode = compiler.parseTestCode("");
    Node mainNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, mainNode);

    Scope globalScope = scopeCreator.createScope(root, null);
    Node varNode = mainNode.getFirstChild();
    Node fnNode = varNode.getFirstChild().getFirstChild();
    Scope fnScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(globalScope.getVar("fact"));
    assertNull(globalScope.getVar("f"));
    assertNotNull(fnScope.getVar("f"));
  }

  // Tests constructor with prototype property declared with @type
  @Test
  public void testCreateScope_prototypePropertyWithType_declaresTypedProperty() {
    Scope scope = createGlobalScope(
        "/** @constructor */\n" +
        "function Widget() {}\n" +
        "/** @type {string} */\n" +
        "Widget.prototype.id = '';"
    );

    Scope.Var idVar = scope.getVar("Widget.prototype.id");
    assertNotNull(idVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), idVar.getType());
    assertFalse(idVar.isTypeInferred());
  }
}