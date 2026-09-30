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
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Scope parseAndCreateGlobalScope(String js) {
    Node root = compiler.parseTestCode(js);
    return scopeCreator.createScope(root, null);
  }

  private Node findFunctionNode(Node n, String fnName) {
    if (n.getType() == Token.FUNCTION) {
      if (fnName == null || fnName.equals(n.getFirstChild().getString())) {
        return n;
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunctionNode(child, fnName);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  // Tests initial scope containing native types
  @Test
  public void testCreateInitialScope_containsNativeTypes() {
    Node root = compiler.parseTestCode("");
    Scope scope = scopeCreator.createInitialScope(root);

    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("String"));
    assertNotNull(scope.getVar("Number"));
    assertNotNull(scope.getVar("Boolean"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("goog.typedef"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  // Tests VAR declaration with type inference
  @Test
  public void testCreateScope_varDeclaration_infersType() {
    String js = "var a = 10; var b = 'hello'; var c = true;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));

    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        scope.getVar("a").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        scope.getVar("b").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE),
        scope.getVar("c").getType());
  }

  // Tests VAR declaration with JSDoc @type annotation
  @Test
  public void testCreateScope_varWithJSDocType_declaresType() {
    String js = "/** @type {string} */ var str = 'test';";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var varStr = scope.getVar("str");
    assertNotNull(varStr);
    assertFalse(varStr.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        varStr.getType());
  }

  // Tests constructor declaration creates prototype property in scope
  @Test
  public void testCreateScope_constructorDeclaration_definesPrototype() {
    String js = "/** @constructor */ function Foo() {}";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    assertTrue(fooVar.getType() instanceof FunctionType);
    assertTrue(((FunctionType) fooVar.getType()).isConstructor());

    Scope.Var protoVar = scope.getVar("Foo.prototype");
    assertNotNull(protoVar);
  }

  // Tests enum declaration and element resolution
  @Test
  public void testCreateScope_enumDeclaration_definesEnumType() {
    String js = "/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var enumVar = scope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("FIRST"));
    assertTrue(enumType.hasOwnProperty("SECOND"));
  }

  // Tests enum with non-object initializer triggers warning
  @Test
  public void testCreateScope_enumInitializerNonObject_reportsWarning() {
    String js = "/** @enum {number} */ var NotAnObject = 42;";
    parseAndCreateGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER.key, compiler.getWarnings()[0].getType().key);
  }

  // Tests local scope creation and parameter declaration
  @Test
  public void testCreateScope_localScope_declaresParametersAndLocals() {
    String js = "function testFn(param1, param2) { var localVar = param1; return localVar; }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testFn");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertTrue(localScope.isLocal());
    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("param2"));
    assertNotNull(localScope.getVar("localVar"));
    assertEquals(globalScope, localScope.getParent());
  }

  // Tests catch block variable declaration in local scope
  @Test
  public void testCreateScope_catchBlock_declaresCatchParam() {
    String js = "function handle() { try { } catch (e) { var x = e; } }";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "handle");
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("x"));
  }

  // Tests stub declaration resolution in global scope
  @Test
  public void testCreateScope_stubPropertyDeclaration_resolvesUnknown() {
    String js = "var obj = {}; obj.stubProp;";
    Scope scope = parseAndCreateGlobalScope(js);

    assertNotNull(scope.getVar("obj"));
    Scope.Var propVar = scope.getVar("obj.stubProp");
    assertNotNull(propVar);
    assertTrue(propVar.isTypeInferred());
  }

  // Tests typedef declaration and registration in type registry
  @Test
  public void testCreateScope_typedef_registersType() {
    String js = "/** @typedef {{name: string, age: number}} */ var Person;";
    parseAndCreateGlobalScope(js);

    JSType personType = compiler.getTypeRegistry().getType("Person");
    assertNotNull(personType);
    assertTrue(personType.isRecordType());
  }

  // Tests property assignment on prototype with @this
  @Test
  public void testCreateScope_collectPropertiesWithThis() {
    String js = "/** @constructor */ function Car() { /** @type {number} */ this.speed = 0; }";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var carVar = scope.getVar("Car");
    assertNotNull(carVar);
    FunctionType carType = (FunctionType) carVar.getType();
    ObjectType instanceType = carType.getInstanceType();
    assertTrue(instanceType.hasOwnProperty("speed"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        instanceType.getPropertyType("speed"));
  }

  // Tests qualified name assignment inside a local scope (Defects4J Closure 95 regression)
  @Test
  public void testCreateScope_qualifiedNameInLocalScope_declaredInGlobalScope() {
    String js = "var ns = {}; (function() { /** @type {number} */ ns.val = 42; })();";
    Node root = compiler.parseTestCode(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, null);
    assertNotNull(fnNode);
    Scope localScope = scopeCreator.createScope(fnNode, globalScope);

    Scope.Var nsValGlobal = globalScope.getVar("ns.val");
    assertNotNull(nsValGlobal);
    assertFalse(nsValGlobal.isTypeInferred());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        nsValGlobal.getType());

    Scope.Var nsValLocal = localScope.getVar("ns.val");
    assertNotNull(nsValLocal);
  }

  // Tests literal types (null, void, regexp, objectlit)
  @Test
  public void testCreateScope_literals_attachedCorrectTypes() {
    String js = "var n = null; var v = undefined; var r = /abc/; var obj = {};";
    Scope scope = parseAndCreateGlobalScope(js);

    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE),
        scope.getVar("n").getType());
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.REGEXP_TYPE),
        scope.getVar("r").getType());
    assertTrue(scope.getVar("obj").getType().isObjectType());
  }

  // Tests multiple var declarations in single statement with jsdoc
  @Test
  public void testCreateScope_multipleVarDefWithJSDoc_reportsWarning() {
    String js = "/** @type {number} */ var x = 1, y = 2;";
    parseAndCreateGlobalScope(js);

    assertTrue(compiler.getWarningCount() >= 1);
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration() {
    String js = "/** @interface */ function Disposable() {}";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var varDisp = scope.getVar("Disposable");
    assertNotNull(varDisp);
    assertTrue(varDisp.getType() instanceof FunctionType);
    assertTrue(((FunctionType) varDisp.getType()).isInterface());
  }

  // Tests @lends annotation attached to object literal
  @Test
  public void testCreateScope_lendsAnnotation() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = /** @lends {Foo.prototype} */ ({ "
        + "  bar: function() {} "
        + "});";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var fooVar = scope.getVar("Foo");
    assertNotNull(fooVar);
    FunctionType fooType = (FunctionType) fooVar.getType();
    ObjectType protoType = fooType.getPrototype();
    assertTrue(protoType.hasProperty("bar"));
  }

  // Tests function type annotation with @param and @return
  @Test
  public void testCreateScope_functionTypeAnnotation_withParamsAndReturn() {
    String js = "/**\n"
        + " * @param {number} count\n"
        + " * @param {string} name\n"
        + " * @return {boolean}\n"
        + " */\n"
        + "function check(count, name) { return count > 0; }";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var fnVar = scope.getVar("check");
    assertNotNull(fnVar);
    assertTrue(fnVar.getType() instanceof FunctionType);
    FunctionType fnType = (FunctionType) fnVar.getType();
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), fnType.getReturnType());
  }

  // Tests malformed typedef reports warning
  @Test
  public void testCreateScope_malformedTypedef_reportsWarning() {
    String js = "/** @typedef */ var BadTypedef;";
    parseAndCreateGlobalScope(js);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF.key, compiler.getWarnings()[0].getType().key);
  }

  // Tests function expression assigned to a variable
  @Test
  public void testCreateScope_functionExpressionAssignedToVar() {
    String js = "var add = function(a, b) { return a + b; };";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var addVar = scope.getVar("add");
    assertNotNull(addVar);
    assertTrue(addVar.getType() instanceof FunctionType);
  }

  // Tests namespaced function declaration
  @Test
  public void testCreateScope_namespacedFunctionDeclaration() {
    String js = "var goog = {}; goog.math = {};\n"
        + "/** @param {number} x @return {number} */\n"
        + "goog.math.abs = function(x) { return x < 0 ? -x : x; };";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var mathAbs = scope.getVar("goog.math.abs");
    assertNotNull(mathAbs);
    assertTrue(mathAbs.getType() instanceof FunctionType);
  }

  // Tests @this annotation on function
  @Test
  public void testCreateScope_thisAnnotationOnFunction() {
    String js = "/** @constructor */ function User() {}\n"
        + "/** @this {User} */ function greet() { return this; }";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var greetVar = scope.getVar("greet");
    assertNotNull(greetVar);
    FunctionType fnType = (FunctionType) greetVar.getType();
    ObjectType thisType = fnType.getTypeOfThis().toObjectType();
    assertNotNull(thisType);
    assertEquals("User", thisType.getReferenceName());
  }

  // Tests inheritance via @extends
  @Test
  public void testCreateScope_extendsInheritance() {
    String js = "/** @constructor */ function Base() {}\n"
        + "/** @constructor\n * @extends {Base} */ function Derived() {}\n"
        + "Derived.prototype = new Base();";
    Scope scope = parseAndCreateGlobalScope(js);

    Scope.Var derivedVar = scope.getVar("Derived");
    assertNotNull(derivedVar);
    FunctionType derivedType = (FunctionType) derivedVar.getType();
    FunctionType baseType = (FunctionType) scope.getVar("Base").getType();
    assertTrue(derivedType.getPrototype().isSubtype(baseType.getPrototype()));
  }
}