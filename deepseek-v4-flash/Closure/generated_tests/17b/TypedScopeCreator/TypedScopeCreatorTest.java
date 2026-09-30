package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.TypedScopeCreator;
import com.google.javascript.jscomp.TypeCheck;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test for TypedScopeCreator.
 * Focuses on bug 17b: scope creation, type declarations, and warnings.
 */
public class TypedScopeCreatorTest {

  private Compiler compiler;
  private TypedScopeCreator creator;
  private Scope globalScope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setIdeMode(true);
    compiler.initOptions(options);
  }

  private Scope createGlobalScope(String code) {
    Node root = compiler.parseSyntheticCode(code);
    creator = new TypedScopeCreator(compiler);
    globalScope = creator.createScope(root, null);
    return globalScope;
  }

  private JSError getFirstWarning() {
    if (compiler.getWarnings() != null && compiler.getWarnings().length > 0) {
      return compiler.getWarnings()[0];
    }
    return null;
  }

  private JSType getVariableType(String varName) {
    Scope.Var var = globalScope.getVar(varName);
    return var != null ? var.getType() : null;
  }

  // Tests that native types are present in initial scope
  @Test
  public void testCreateInitialScope_containsNativeTypes() {
    createGlobalScope("var x;");
    assertNotNull(globalScope.getVar("Object"));
    assertNotNull(globalScope.getVar("Array"));
    assertNotNull(globalScope.getVar("undefined"));
  }

  // Tests variable with @type annotation
  @Test
  public void testDefineVar_withTypeAnnotation() {
    createGlobalScope("/** @type {number} */ var x;");
    JSType type = getVariableType("x");
    assertNotNull("x should have type", type);
    assertEquals("number", type.toString());
  }

  // Tests inferred variable type from literal
  @Test
  public void testDefineVar_noTypeAnnotation_inferred() {
    createGlobalScope("var x = 42;");
    JSType type = getVariableType("x");
    assertNotNull("x should have inferred type", type);
    assertTrue(type.isNumberValueType() || type.isNumberObjectType());
  }

  // Tests global function declaration with return type
  @Test
  public void testDefineFunctionLiteral_global() {
    createGlobalScope("/** @return {string} */ function f() { return ''; }");
    JSType type = getVariableType("f");
    assertNotNull("f should be declared", type);
    assertTrue("f should be a function type", type.isFunctionType());
    FunctionType funcType = type.toMaybeFunctionType();
    assertEquals("string", funcType.getReturnType().toString());
  }

  // Tests enumeration declared with object literal
  @Test
  public void testEnumDeclaration_withObjectLiteral() {
    createGlobalScope("/** @enum {string} */ var E = {A:'a', B:'b'};");
    JSType type = getVariableType("E");
    assertNotNull("E should be declared", type);
    assertTrue("E should be an EnumType", type instanceof EnumType);
    EnumType enumType = (EnumType) type;
    assertEquals("string", enumType.getElementsType().toString());
    assertNotNull(enumType.getElements().get("A"));
    assertNotNull(enumType.getElements().get("B"));
  }

  // Tests typedef declaration
  @Test
  public void testTypedefDeclaration() {
    createGlobalScope("/** @typedef {string|number} */ var MyType;");
    JSType type = getVariableType("MyType");
    assertNotNull("MyType should be declared", type);
    assertEquals("(number|string)", type.toString());
  }

  // Tests constructor without initializer should report CTOR_INITIALIZER
  @Test
  public void testConstructorDeclaration_missingInitializer_reportsError() {
    createGlobalScope("/** @constructor */ var Foo;");
    JSError warning = getFirstWarning();
    assertNotNull("Should report CTOR_INITIALIZER", warning);
    assertEquals(TypedScopeCreator.CTOR_INITIALIZER, warning.getType());
  }

  // Tests interface without initializer should report IFACE_INITIALIZER
  @Test
  public void testInterfaceDeclaration_missingInitializer_reportsError() {
    createGlobalScope("/** @interface */ var Foo;");
    JSError warning = getFirstWarning();
    assertNotNull("Should report IFACE_INITIALIZER", warning);
    assertEquals(TypedScopeCreator.IFACE_INITIALIZER, warning.getType());
  }

  // Tests stub property with @type annotation
  @Test
  public void testQualifiedName_propertyStub() {
    createGlobalScope("var goog = {}; /** @type {number} */ goog.x;");
    Scope.Var var = globalScope.getVar("goog.x");
    assertNotNull("goog.x should be declared", var);
    assertEquals("number", var.getType().toString());
  }

  // Tests @lends with unknown variable produces UNKNOWN_LENDS warning
  @Test
  public void testLendsAnnotation_unknownVariable_warns() {
    createGlobalScope("/** @lends {unknown} */ var obj = {};");
    JSError warning = getFirstWarning();
    assertNotNull("Should report UNKNOWN_LENDS", warning);
    assertEquals(TypedScopeCreator.UNKNOWN_LENDS, warning.getType());
  }

  // Tests @lends on non-object type produces LENDS_ON_NON_OBJECT
  @Test
  public void testLendsAnnotation_nonObjectType_warns() {
    createGlobalScope(
        "/** @type {number} */ var y = 1; /** @lends {y} */ var obj = {a:1};");
    JSError warning = getFirstWarning();
    assertNotNull("Should report LENDS_ON_NON_OBJECT", warning);
    assertEquals(TypedScopeCreator.LENDS_ON_NON_OBJECT, warning.getType());
  }

  // Tests multiple variable definitions cause MULTIPLE_VAR_DEF
  @Test
  public void testMultipleVarDefinitions_reportsError() {
    createGlobalScope("/** @type {number} */ var x, y;");
    JSError warning = getFirstWarning();
    assertNotNull("Should report MULTIPLE_VAR_DEF", warning);
    assertEquals(TypeCheck.MULTIPLE_VAR_DEF, warning.getType());
  }

  // Tests catch variable is declared in global scope (no enclosing function)
  @Test
  public void testCatchParameter_globalScope_declared() {
    createGlobalScope("try {} catch(e) {}");
    Scope.Var var = globalScope.getVar("e");
    assertNotNull("e should be declared", var);
  }

  // Tests enum initializer that is not object literal or enum produces ENUM_INITIALIZER
  @Test
  public void testEnumDeclaration_invalidInitializer_warns() {
    createGlobalScope("/** @enum {string} */ var E = 42;");
    JSError warning = getFirstWarning();
    assertNotNull("Should report ENUM_INITIALIZER", warning);
    assertEquals(TypedScopeCreator.ENUM_INITIALIZER, warning.getType());
  }

  // Tests malformed typedef (no type information) produces MALFORMED_TYPEDEF
  @Test
  public void testMalformedTypedef_warns() {
    createGlobalScope("/** @typedef */ var BadTypedef;");
    JSError warning = getFirstWarning();
    assertNotNull("Should report MALFORMED_TYPEDEF", warning);
    assertEquals(TypedScopeCreator.MALFORMED_TYPEDEF, warning.getType());
  }

  // Tests constructor with prototype property assignment
  @Test
  public void testConstructor_prototypeAssignment() {
    createGlobalScope(
        "/** @constructor */ function Foo() {}; Foo.prototype.method = function() { return 1; };");
    Scope.Var protoVar = globalScope.getVar("Foo.prototype");
    assertNotNull("Foo.prototype should be declared", protoVar);
    assertNotNull("Foo.prototype should have a type", protoVar.getType());
    ObjectType protoObj = protoVar.getType().toObjectType();
    assertNotNull("Foo.prototype should be an object type", protoObj);
    JSType methodType = protoObj.getPropertyType("method");
    assertNotNull("Foo.prototype should have method property", methodType);
    assertTrue("method should be a function", methodType.isFunctionType());
  }
}