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
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    scopeCreator = new TypedScopeCreator(compiler);
  }

  private Node parseAndCreateRoot(String js) {
    Node main = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    return new Node(Token.BLOCK, externs, main);
  }

  private Scope createGlobalScope(String js) {
    Node root = parseAndCreateRoot(js);
    return scopeCreator.createScope(root, null);
  }

  private Node findFunctionNode(Node n, String name) {
    if (n.isFunction()) {
      Node nameNode = n.getFirstChild();
      if (nameNode != null && name.equals(nameNode.getString())) {
        return n;
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFunctionNode(child, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // Tests native bindings in initial global scope
  @Test
  public void testCreateInitialScope_nativeTypes_declaredInScope() {
    Node root = parseAndCreateRoot("");
    Scope s = scopeCreator.createInitialScope(root);

    assertNotNull(s.getVar("Object"));
    assertNotNull(s.getVar("Array"));
    assertNotNull(s.getVar("String"));
    assertNotNull(s.getVar("Number"));
    assertNotNull(s.getVar("Boolean"));
    assertNotNull(s.getVar("RegExp"));
    assertNotNull(s.getVar("Date"));
    assertNotNull(s.getVar("Error"));
    assertNotNull(s.getVar("undefined"));
    assertNotNull(s.getVar("ActiveXObject"));
  }

  // Tests declaration of global variable with literal primitive value
  @Test
  public void testCreateScope_globalVarDeclaration_infersType() {
    Scope scope = createGlobalScope("var x = 42;");
    Scope.Var var = scope.getVar("x");

    assertNotNull(var);
    assertNotNull(var.getType());
    assertTrue(var.getType().isNumber());
  }

  // Tests literal types attachment
  @Test
  public void testCreateScope_literalTypes_attachedCorrectly() {
    String js = "var n = null; var v = void 0; var s = 'text'; var num = 123; var b = true; var r = /abc/;";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("n"));
    assertNotNull(scope.getVar("v"));
    assertNotNull(scope.getVar("s"));
    assertNotNull(scope.getVar("num"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("r"));

    assertTrue(scope.getVar("n").getType().isNullType());
    assertTrue(scope.getVar("v").getType().isVoidType());
    assertTrue(scope.getVar("s").getType().isStringType());
    assertTrue(scope.getVar("num").getType().isNumber());
    assertTrue(scope.getVar("b").getType().isBooleanValueType());
  }

  // Tests function declaration in global scope
  @Test
  public void testCreateScope_functionDeclaration_declaresFunctionAndParams() {
    Scope scope = createGlobalScope("function foo(a, b) { return a + b; }");
    Scope.Var fnVar = scope.getVar("foo");

    assertNotNull(fnVar);
    assertTrue(fnVar.getType().isFunctionType());
  }

  // Tests local scope creation and parameter declaration
  @Test
  public void testCreateScope_localScope_declaresLocalVarsAndParams() {
    String js = "function testFn(param1, param2) { var localVal = 'hello'; return param1; }";
    Node root = parseAndCreateRoot(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "testFn");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertEquals(globalScope, localScope.getParent());
    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("param2"));
    assertNotNull(localScope.getVar("localVal"));
    assertTrue(localScope.getVar("localVal").getType().isStringType());
  }

  // Tests constructor function declaration and prototype registration
  @Test
  public void testCreateScope_constructorFunction_registersPrototype() {
    String js = "/** @constructor */ function Person(name) { this.name = name; }";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("Person");
    assertNotNull(fnVar);
    assertTrue(fnVar.getType().isConstructor());

    Scope.Var protoVar = scope.getVar("Person.prototype");
    assertNotNull(protoVar);
  }

  // Tests interface declaration
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    String js = "/** @interface */ function Disposable() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var ifaceVar = scope.getVar("Disposable");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
    assertNotNull(scope.getVar("Disposable.prototype"));
  }

  // Tests enum declaration with elements
  @Test
  public void testCreateScope_enumDeclaration_definesEnumElements() {
    String js = "/** @enum {number} */ var Status = { OK: 200, NOT_FOUND: 404 };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("Status");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElementsType().isNumber());
    assertTrue(enumType.hasElement("OK"));
    assertTrue(enumType.hasElement("NOT_FOUND"));
  }

  // Tests typedef declaration and type registry recording
  @Test
  public void testCreateScope_typedefDeclaration_registersInTypeRegistry() {
    String js = "/** @typedef {(string|number)} */ var StringOrNumber;";
    createGlobalScope(js);

    JSType type = compiler.getTypeRegistry().getType("StringOrNumber");
    assertNotNull(type);
    assertTrue(type.isUnionType());
  }

  // Tests catch block scope and variable definition
  @Test
  public void testCreateScope_catchBlock_definesCatchParameter() {
    String js = "function handle() { try {} catch (e) { var handled = true; } }";
    Node root = parseAndCreateRoot(js);
    Scope globalScope = scopeCreator.createScope(root, null);

    Node fnNode = findFunctionNode(root, "handle");
    assertNotNull(fnNode);

    Scope localScope = scopeCreator.createScope(fnNode, globalScope);
    assertNotNull(localScope.getVar("e"));
    assertNotNull(localScope.getVar("handled"));
  }

  // Tests stub declaration in qualified name assignment
  @Test
  public void testCreateScope_stubDeclaration_resolvesToUnknownType() {
    String js = "var ns = {}; ns.stubProp;";
    Scope scope = createGlobalScope(js);

    Scope.Var propVar = scope.getVar("ns.stubProp");
    assertNotNull(propVar);
    assertTrue(propVar.getType().isUnknownType());
  }

  // Tests multiple variables in a single var statement
  @Test
  public void testCreateScope_multipleVarDef_definesAllVariables() {
    String js = "var a = 1, b = 'two', c = true;";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("a"));
    assertNotNull(scope.getVar("b"));
    assertNotNull(scope.getVar("c"));

    assertTrue(scope.getVar("a").getType().isNumber());
    assertTrue(scope.getVar("b").getType().isStringType());
    assertTrue(scope.getVar("c").getType().isBooleanValueType());
  }

  // Tests lends annotation on object literal
  @Test
  public void testCreateScope_lendsAnnotation_attachesPropertiesToTarget() {
    String js =
        "/** @constructor */ function Widget() {}\n"
        + "var obj = /** @lends {Widget.prototype} */ ({ render: function() {} });";
    Scope scope = createGlobalScope(js);

    Scope.Var widgetVar = scope.getVar("Widget");
    assertNotNull(widgetVar);
    FunctionType widgetType = widgetVar.getType().toMaybeFunctionType();
    assertNotNull(widgetType);
    ObjectType proto = widgetType.getPrototype();
    assertTrue(proto.hasProperty("render"));
  }

  // Tests patching global scope with updated script
  @Test
  public void testPatchGlobalScope_modifiedScript_updatesVariables() {
    String jsInitial = "var originalVar = 10;";
    Node root = parseAndCreateRoot(jsInitial);
    Scope globalScope = scopeCreator.createScope(root, null);

    assertNotNull(globalScope.getVar("originalVar"));

    Node newScript = compiler.parseTestCode("var updatedVar = 20;");
    scopeCreator.patchGlobalScope(globalScope, newScript);

    assertNotNull(globalScope.getVar("updatedVar"));
  }

  // Tests nested functions and variable escaping behavior (Defects4J Closure 168 regression)
  @Test
  public void testCreateScope_nestedFunctionsEscapedVars_analyzedWithoutException() {
    String js =
        "function parentFn() {\n"
        + "  var self = this;\n"
        + "  var counter = 0;\n"
        + "  function childFn() {\n"
        + "    counter++;\n"
        + "    function grandChildFn() {\n"
        + "      return self.counter + counter;\n"
        + "    }\n"
        + "    return grandChildFn();\n"
        + "  }\n"
        + "  return childFn();\n"
        + "}";
    Scope scope = createGlobalScope(js);
    assertNotNull(scope.getVar("parentFn"));

    Node root = parseAndCreateRoot(js);
    Node parentFnNode = findFunctionNode(root, "parentFn");
    Scope localScope = scopeCreator.createScope(parentFnNode, scope);

    assertNotNull(localScope.getVar("self"));
    assertNotNull(localScope.getVar("counter"));
    assertNotNull(localScope.getVar("childFn"));
  }

  // Tests subclass inheritance relationship declaration
  @Test
  public void testCreateScope_subclassInheritance_establishesRelationship() {
    String js =
        "/** @constructor */ function SuperClass() {}\n"
        + "/** @constructor */ function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);";
    Scope scope = createGlobalScope(js);

    Scope.Var subVar = scope.getVar("SubClass");
    assertNotNull(subVar);
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    assertEquals("SuperClass", subCtor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  // Tests function expression assigned to variable with jsdoc param and return types
  @Test
  public void testCreateScope_annotatedFunctionExpression_declaresTypedFunction() {
    String js = "/**\n"
        + " * @param {string} msg\n"
        + " * @return {number}\n"
        + " */\n"
        + "var calculateLength = function(msg) { return msg.length; };";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("calculateLength");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertTrue(fnType.getReturnType().isNumber());
  }

  // Tests prototype method assignment and property type inferencing
  @Test
  public void testCreateScope_prototypeMethodAssignment_attachesMethodToPrototype() {
    String js = "/** @constructor */ function Animal() {}\n"
        + "/** @param {string} sound */\n"
        + "Animal.prototype.speak = function(sound) { return sound; };";
    Scope scope = createGlobalScope(js);

    Scope.Var animalVar = scope.getVar("Animal");
    assertNotNull(animalVar);
    FunctionType ctor = animalVar.getType().toMaybeFunctionType();
    assertNotNull(ctor);
    ObjectType proto = ctor.getPrototype();
    assertTrue(proto.hasProperty("speak"));
  }

  // Tests static property assignment on constructor
  @Test
  public void testCreateScope_constructorStaticProperty_attachesStaticMember() {
    String js = "/** @constructor */ function MathUtil() {}\n"
        + "/** @type {number} */ MathUtil.PI = 3.14159;";
    Scope scope = createGlobalScope(js);

    Scope.Var staticVar = scope.getVar("MathUtil.PI");
    assertNotNull(staticVar);
    assertTrue(staticVar.getType().isNumber());
  }

  // Tests constructor implementing interface with @implements annotation
  @Test
  public void testCreateScope_implementsAnnotation_registersImplementedInterface() {
    String js = "/** @interface */ function Clickable() {}\n"
        + "/** @constructor\n"
        + " * @implements {Clickable} */\n"
        + "function Button() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var btnVar = scope.getVar("Button");
    assertNotNull(btnVar);
    FunctionType ctor = btnVar.getType().toMaybeFunctionType();
    assertNotNull(ctor);
    assertTrue(ctor.getImplementedInterfaces().iterator().hasNext());
  }

  // Tests constructor extending class with @extends annotation
  @Test
  public void testCreateScope_extendsAnnotation_establishesBaseClass() {
    String js = "/** @constructor */ function Base() {}\n"
        + "/** @constructor\n"
        + " * @extends {Base} */\n"
        + "function Derived() {}";
    Scope scope = createGlobalScope(js);

    Scope.Var derivedVar = scope.getVar("Derived");
    assertNotNull(derivedVar);
    FunctionType ctor = derivedVar.getType().toMaybeFunctionType();
    assertNotNull(ctor);
    assertNotNull(ctor.getSuperClassConstructor());
    assertEquals("Base", ctor.getSuperClassConstructor().getInstanceType().getReferenceName());
  }

  // Tests nested namespace object creation
  @Test
  public void testCreateScope_nestedNamespace_registersQualifiedVars() {
    String js = "var my = my || {};\n"
        + "my.app = my.app || {};\n"
        + "/** @type {string} */ my.app.version = '1.0.0';";
    Scope scope = createGlobalScope(js);

    assertNotNull(scope.getVar("my"));
    assertNotNull(scope.getVar("my.app"));
    Scope.Var verVar = scope.getVar("my.app.version");
    assertNotNull(verVar);
    assertTrue(verVar.getType().isStringType());
  }

  // Tests constant variable annotation
  @Test
  public void testCreateScope_constAnnotation_setsConstProperty() {
    String js = "/** @const */ var MAX_LIMIT = 100;";
    Scope scope = createGlobalScope(js);

    Scope.Var var = scope.getVar("MAX_LIMIT");
    assertNotNull(var);
    assertTrue(var.isConst());
    assertTrue(var.getType().isNumber());
  }

  // Tests object literal with getters and setters
  @Test
  public void testCreateScope_objectLiteralGetterSetter_createsProperties() {
    String js = "var obj = {\n"
        + "  get val() { return 10; },\n"
        + "  set val(v) {}\n"
        + "};";
    Scope scope = createGlobalScope(js);

    Scope.Var objVar = scope.getVar("obj");
    assertNotNull(objVar);
    ObjectType objType = objVar.getType().toObjectType();
    assertNotNull(objType);
    assertTrue(objType.hasProperty("val"));
  }

  // Tests function with @this type annotation
  @Test
  public void testCreateScope_thisAnnotation_bindsThisType() {
    String js = "/** @constructor */ function Context() { this.flag = true; }\n"
        + "/** @this {Context} */ function runInContext() { return this.flag; }";
    Scope scope = createGlobalScope(js);

    Scope.Var fnVar = scope.getVar("runInContext");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    ObjectType typeOfThis = fnType.getTypeOfThis().toObjectType();
    assertNotNull(typeOfThis);
    assertEquals("Context", typeOfThis.getReferenceName());
  }

  // Tests string enum declaration
  @Test
  public void testCreateScope_stringEnumDeclaration_definesStringElements() {
    String js = "/** @enum {string} */ var Direction = { NORTH: 'N', SOUTH: 'S' };";
    Scope scope = createGlobalScope(js);

    Scope.Var enumVar = scope.getVar("Direction");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.getElementsType().isStringType());
    assertTrue(enumType.hasElement("NORTH"));
    assertTrue(enumType.hasElement("SOUTH"));
  }
}