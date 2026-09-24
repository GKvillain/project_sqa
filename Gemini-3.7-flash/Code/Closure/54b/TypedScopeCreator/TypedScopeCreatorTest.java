package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
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

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Scope createScope(String js) {
    Node root = compiler.parseTestCode(js);
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  // Tests initial native types declaration in global scope
  @Test
  public void testCreateInitialScope_declaresNativeTypes() {
    Scope scope = createScope("");
    assertTrue(scope.isGlobal());
    assertNotNull(scope.getVar("Object"));
    assertNotNull(scope.getVar("Array"));
    assertNotNull(scope.getVar("Function"));
    assertNotNull(scope.getVar("Date"));
    assertNotNull(scope.getVar("RegExp"));
    assertNotNull(scope.getVar("Error"));
    assertNotNull(scope.getVar("undefined"));
    assertNotNull(scope.getVar("ActiveXObject"));
  }

  // Tests primitive literal variable declarations
  @Test
  public void testDefineVar_primitiveLiterals_infersCorrectTypes() {
    Scope scope = createScope("var num = 42; var str = 'test'; var bool = true; var nul = null;");
    
    Scope.Var numVar = scope.getVar("num");
    assertNotNull(numVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), numVar.getType());

    Scope.Var strVar = scope.getVar("str");
    assertNotNull(strVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE), strVar.getType());

    Scope.Var boolVar = scope.getVar("bool");
    assertNotNull(boolVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.BOOLEAN_TYPE), boolVar.getType());

    Scope.Var nulVar = scope.getVar("nul");
    assertNotNull(nulVar);
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NULL_TYPE), nulVar.getType());
  }

  // Tests constructor declaration and automatic prototype slot creation
  @Test
  public void testDefineSlot_constructorFunction_createsPrototypeSlot() {
    Scope scope = createScope("/** @constructor */ function MyClass() {}");
    
    Scope.Var ctorVar = scope.getVar("MyClass");
    assertNotNull(ctorVar);
    assertTrue(ctorVar.getType().isFunctionType());
    assertTrue(ctorVar.getType().toMaybeFunctionType().isConstructor());

    Scope.Var protoVar = scope.getVar("MyClass.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isFunctionPrototypeType());
  }

  // Tests prototype property assignment on a constructor
  @Test
  public void testMaybeDeclareQualifiedName_prototypeMethod_assignsProperty() {
    Scope scope = createScope(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar = function(x) { return x; };"
    );

    Scope.Var ctorVar = scope.getVar("Foo");
    FunctionType ctorType = ctorVar.getType().toMaybeFunctionType();
    ObjectType protoType = ctorType.getPrototype();

    assertTrue(protoType.hasProperty("bar"));
    assertTrue(protoType.getPropertyType("bar").isFunctionType());
  }

  // Tests re-declaring prototype with an object literal
  @Test
  public void testMaybeDeclareQualifiedName_redefinePrototype_untyped() {
    Scope scope = createScope(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { methodA: function() {}, propB: 123 };"
    );

    Scope.Var ctorVar = scope.getVar("Foo");
    FunctionType ctorType = ctorVar.getType().toMaybeFunctionType();
    ObjectType protoType = ctorType.getPrototype();

    assertNotNull(protoType);
    assertTrue(protoType.hasProperty("methodA"));
    assertTrue(protoType.hasProperty("propB"));
  }

  // Tests inheritance and prototype replacement with @extends
  @Test
  public void testMaybeDeclareQualifiedName_extendsWithPrototypeObjectLiteral() {
    Scope scope = createScope(
        "/** @constructor */ function Super() {}\n" +
        "/** @constructor\n * @extends {Super} */ function Sub() {}\n" +
        "Sub.prototype = { subMethod: function() {} };"
    );

    Scope.Var subVar = scope.getVar("Sub");
    FunctionType subCtor = subVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    assertEquals(scope.getVar("Super").getType().toMaybeFunctionType(), subCtor.getSuperClassConstructor());
  }

  // Tests enum declaration and element definition
  @Test
  public void testCreateEnumTypeFromNodes_validEnum_definesElements() {
    Scope scope = createScope("/** @enum {number} */ var Status = { OK: 200, NOT_FOUND: 404 };");

    Scope.Var enumVar = scope.getVar("Status");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType() instanceof EnumType);

    EnumType enumType = (EnumType) enumVar.getType();
    assertTrue(enumType.hasOwnProperty("OK"));
    assertTrue(enumType.hasOwnProperty("NOT_FOUND"));
    assertEquals(compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE), enumType.getElementsType());
  }

  // Tests duplicate enum key warning
  @Test
  public void testCreateEnumTypeFromNodes_duplicateEnumKeys_reportsWarning() {
    createScope("/** @enum {number} */ var DuplicateEnum = { DUP: 1, DUP: 2 };");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests invalid enum initializer warning
  @Test
  public void testDefineSlot_enumInitializerNotObjectLit_reportsWarning() {
    createScope("/** @enum {number} */ var BadEnum = 123;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests typedef parsing and scope registration
  @Test
  public void testCheckForTypedef_validTypedef_declaresTypeInRegistry() {
    Scope scope = createScope("/** @typedef {{name: string, age: number}} */ var Person;");
    
    Scope.Var personVar = scope.getVar("Person");
    assertNotNull(personVar);
    
    JSType registeredType = compiler.getTypeRegistry().getType("Person");
    assertNotNull(registeredType);
    assertTrue(registeredType.isRecordType());
  }

  // Tests malformed typedef reporting warning
  @Test
  public void testCheckForTypedef_malformedTypedef_reportsWarning() {
    createScope("/** @typedef {NonExistentType_XYZ} */ var InvalidType;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests local scope creation and parameter definitions
  @Test
  public void testCreateLocalScope_functionParameters_definesSlots() {
    Node root = compiler.parseTestCode("function outer(param1, param2) { var localVal = true; }");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope global = creator.createScope(root, null);

    Node functionNode = root.getFirstChild().getFirstChild();
    Scope localScope = creator.createScope(functionNode, global);

    assertFalse(localScope.isGlobal());
    assertEquals(global, localScope.getParent());
    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("param2"));
    assertNotNull(localScope.getVar("localVal"));
  }

  // Tests catch block scope variable definition
  @Test
  public void testDefineCatch_catchBlockVariable_definesLocalSlot() {
    Node root = compiler.parseTestCode("function testCatch() { try {} catch (e) { var inCatch = 1; } }");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope global = creator.createScope(root, null);

    Node functionNode = root.getFirstChild().getFirstChild();
    Scope localScope = creator.createScope(functionNode, global);

    assertNotNull(localScope.getVar("e"));
  }

  // Tests object literal @lends annotation
  @Test
  public void testDefineObjectLiteral_lendsAnnotation_appliesPropertiesToLendedType() {
    Scope scope = createScope(
        "/** @constructor */ function Foo() {}\n" +
        "var myObj = /** @lends {Foo.prototype} */ ({ fooProp: 'hello' });"
    );

    Scope.Var ctorVar = scope.getVar("Foo");
    FunctionType ctorType = ctorVar.getType().toMaybeFunctionType();
    ObjectType protoType = ctorType.getPrototype();

    assertTrue(protoType.hasProperty("fooProp"));
  }

  // Tests @lends with non-existing variable reports warning
  @Test
  public void testDefineObjectLiteral_unknownLendsTarget_reportsWarning() {
    createScope("var myObj = /** @lends {UnknownConstructor.prototype} */ ({ a: 1 });");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests constructor without initialization reports warning
  @Test
  public void testDefineSlot_uninitializedConstructor_reportsWarning() {
    createScope("/** @constructor */ var UninitializedCtor;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests multiple var declaration with jsdoc reports warning
  @Test
  public void testDefineVar_multipleVarsWithDoc_reportsWarning() {
    createScope("/** @type {number} */ var a = 1, b = 2;");
    assertEquals(1, compiler.getWarningCount());
  }

  // Tests global scope patch on script update
  @Test
  public void testPatchGlobalScope_removesAndRetraversesVars() {
    Node root = compiler.parseTestCode("var a = 10; var b = 20;");
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    Scope global = creator.createScope(root, null);

    assertNotNull(global.getVar("a"));
    assertNotNull(global.getVar("b"));

    Node scriptNode = root.getFirstChild();
    scriptNode.removeChildren();
    Node newVarNode = compiler.parseTestCode("var c = 30;").removeChildren();
    scriptNode.addChildToFront(newVarNode);

    creator.patchGlobalScope(global, scriptNode);

    assertNull(global.getVar("a"));
    assertNull(global.getVar("b"));
    assertNotNull(global.getVar("c"));
  }
}