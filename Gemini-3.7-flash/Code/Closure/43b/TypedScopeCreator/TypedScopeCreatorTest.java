package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypedScopeCreatorTest extends CompilerTestCase {

  private Scope globalScope;
  private TypedScopeCreator scopeCreator;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        scopeCreator = new TypedScopeCreator(compiler);
        globalScope = scopeCreator.createScope(root, null);
      }
    };
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  private JSType getNativeType(JSTypeNative type) {
    return getLastCompiler().getTypeRegistry().getNativeType(type);
  }

  // Tests declared number variable in global scope
  @Test
  public void testCreateScope_declaredNumberVar_hasNumberType() {
    testSame("/** @type {number} */ var x = 42;");
    Scope.Var var = globalScope.getVar("x");
    assertNotNull(var);
    assertEquals(getNativeType(NUMBER_TYPE), var.getType());
    assertFalse(var.isTypeInferred());
  }

  // Tests declared string variable in global scope
  @Test
  public void testCreateScope_declaredStringVar_hasStringType() {
    testSame("/** @type {string} */ var s = 'hello';");
    Scope.Var var = globalScope.getVar("s");
    assertNotNull(var);
    assertEquals(getNativeType(STRING_TYPE), var.getType());
  }

  // Tests declared boolean variable in global scope
  @Test
  public void testCreateScope_declaredBoolVar_hasBooleanType() {
    testSame("/** @type {boolean} */ var b = true;");
    Scope.Var var = globalScope.getVar("b");
    assertNotNull(var);
    assertEquals(getNativeType(BOOLEAN_TYPE), var.getType());
  }

  // Tests function declaration and its type creation
  @Test
  public void testCreateScope_functionDeclaration_createsFunctionType() {
    testSame("/** @param {number} a\n * @return {string} */ function f(a) { return ''; }");
    Scope.Var var = globalScope.getVar("f");
    assertNotNull(var);
    assertTrue(var.getType().isFunctionType());
    FunctionType fnType = var.getType().toMaybeFunctionType();
    assertEquals(getNativeType(STRING_TYPE), fnType.getReturnType());
  }

  // Tests constructor declaration creates constructor and prototype in scope
  @Test
  public void testCreateScope_constructorDeclaration_declaresPrototypeAndConstructor() {
    testSame("/** @constructor */ function Foo() {}");
    Scope.Var ctorVar = globalScope.getVar("Foo");
    assertNotNull(ctorVar);
    assertTrue(ctorVar.getType().isConstructor());

    Scope.Var protoVar = globalScope.getVar("Foo.prototype");
    assertNotNull(protoVar);
    assertTrue(protoVar.getType().isObject());
  }

  // Tests namespaced constructor declaration
  @Test
  public void testCreateScope_namespacedConstructor_declaresConstructor() {
    testSame("var ns = {}; /** @constructor */ ns.Bar = function() {};");
    Scope.Var ctorVar = globalScope.getVar("ns.Bar");
    assertNotNull(ctorVar);
    assertTrue(ctorVar.getType().isConstructor());
    assertNotNull(globalScope.getVar("ns.Bar.prototype"));
  }

  // Tests enum declaration and member types
  @Test
  public void testCreateScope_enumDeclaration_createsEnumTypeWithElements() {
    testSame("/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };");
    Scope.Var enumVar = globalScope.getVar("MyEnum");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = (EnumType) enumVar.getType();
    assertEquals(getNativeType(NUMBER_TYPE), enumType.getElementsType());
    assertTrue(enumType.hasProperty("FIRST"));
    assertTrue(enumType.hasProperty("SECOND"));
  }

  // Tests typedef declaration and type registry registration
  @Test
  public void testCreateScope_typedefDeclaration_registersType() {
    testSame("/** @typedef {Array.<string>} */ var StringList;");
    JSType definedType = getLastCompiler().getTypeRegistry().getType("StringList");
    assertNotNull(definedType);
  }

  // Tests object literal with @lends annotation
  @Test
  public void testCreateScope_objectLiteralLends_attachesProperties() {
    testSame("/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = /** @lends {Foo.prototype} */ {\n"
        + "  /** @type {number} */\n"
        + "  bar: 1\n"
        + "};");
    Scope.Var protoVar = globalScope.getVar("Foo.prototype");
    assertNotNull(protoVar);
    ObjectType protoType = ObjectType.cast(protoVar.getType());
    assertNotNull(protoType);
    assertTrue(protoType.hasProperty("bar"));
  }

  // Tests stub declaration in global scope
  @Test
  public void testCreateScope_stubDeclaration_resolvesToUnknown() {
    testSame("var obj = {}; obj.stub;");
    Scope.Var stubVar = globalScope.getVar("obj.stub");
    assertNotNull(stubVar);
    assertEquals(getNativeType(UNKNOWN_TYPE), stubVar.getType());
  }

  // Tests warning on multiple var declaration with JSDoc
  @Test
  public void testCreateScope_multipleVarDefWithJSDoc_reportsWarning() {
    testWarning("/** @type {number} */ var a = 1, b = 2;", TypedScopeCreator.MULTIPLE_VAR_DEF);
  }

  // Tests warning on unknown @lends target
  @Test
  public void testCreateScope_unknownLendsTarget_reportsWarning() {
    testWarning("var obj = /** @lends {nonExistent} */ { x: 1 };", TypedScopeCreator.UNKNOWN_LENDS);
  }

  // Tests warning when lending properties to a non-object type
  @Test
  public void testCreateScope_lendsOnNonObject_reportsWarning() {
    testWarning("var num = 123; var obj = /** @lends {num} */ { x: 1 };", TypedScopeCreator.LENDS_ON_NON_OBJECT);
  }

  // Tests local scope creation and parameter declaration
  @Test
  public void testCreateScope_localScope_declaresParametersAndLocals() {
    testSame("function testFn(param1) { var localVar = param1; }");
    Node root = getLastCompiler().getRoot().getLastChild();
    Node fnNode = root.getFirstChild().getFirstChild();

    Scope localScope = scopeCreator.createScope(fnNode.getLastChild(), globalScope);
    assertNotNull(localScope);
    assertTrue(localScope.isLocal());
    assertNotNull(localScope.getVar("param1"));
    assertNotNull(localScope.getVar("localVar"));
  }

  // Tests catch block scope variable declaration
  @Test
  public void testCreateScope_catchBlock_declaresCatchParam() {
    testSame("function testCatch() { try {} catch (e) { var inner = e; } }");
    Node root = getLastCompiler().getRoot().getLastChild();
    Node fnNode = root.getFirstChild().getFirstChild();

    Scope localScope = scopeCreator.createScope(fnNode.getLastChild(), globalScope);
    assertNotNull(localScope);
    assertNotNull(localScope.getVar("e"));
  }

  // Tests initial scope containing standard native bindings
  @Test
  public void testCreateInitialScope_containsNativeBindings() {
    testSame("");
    Scope initialScope = scopeCreator.createInitialScope(getLastCompiler().getRoot());
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

  // Tests patchGlobalScope removes old vars and updates global scope
  @Test
  public void testPatchGlobalScope_replacesOldDeclarations() {
    testSame("var oldVar = 1;");
    assertTrue(globalScope.isDeclared("oldVar", false));

    Node scriptNode = getLastCompiler().getRoot().getLastChild().getFirstChild();
    Node newVarNode = getLastCompiler().parseSyntheticCode("var newVar = 2;").removeChildren();
    scriptNode.getParent().replaceChild(scriptNode, newVarNode);

    scopeCreator.patchGlobalScope(globalScope, newVarNode);
    assertFalse(globalScope.isDeclared("oldVar", false));
    assertTrue(globalScope.isDeclared("newVar", false));
  }

  // Tests lending properties within object literal assigning to constructor prototype
  @Test
  public void testCreateScope_lendsConstructorPrototypeHierarchical_attachesTypeCorrectly() {
    testSame("var ns = {};\n"
        + "/** @constructor */ ns.MyClass = function() {};\n"
        + "ns.MyClass.prototype = /** @lends {ns.MyClass.prototype} */ {\n"
        + "  /** @return {number} */\n"
        + "  getValue: function() { return 0; }\n"
        + "};");
    Scope.Var protoVar = globalScope.getVar("ns.MyClass.prototype");
    assertNotNull(protoVar);
    ObjectType protoType = ObjectType.cast(protoVar.getType());
    assertNotNull(protoType);
    assertTrue(protoType.hasProperty("getValue"));
    JSType methodType = protoType.getPropertyType("getValue");
    assertTrue(methodType.isFunctionType());
    assertEquals(getNativeType(NUMBER_TYPE), methodType.toMaybeFunctionType().getReturnType());
  }

  // Tests interface declaration in global scope
  @Test
  public void testCreateScope_interfaceDeclaration_createsInterfaceType() {
    testSame("/** @interface */ function AnInterface() {}\n"
        + "/** @return {string} */ AnInterface.prototype.getName = function() {};");
    Scope.Var ifaceVar = globalScope.getVar("AnInterface");
    assertNotNull(ifaceVar);
    assertTrue(ifaceVar.getType().isInterface());
    Scope.Var protoVar = globalScope.getVar("AnInterface.prototype");
    assertNotNull(protoVar);
    ObjectType protoType = ObjectType.cast(protoVar.getType());
    assertNotNull(protoType);
    assertTrue(protoType.hasProperty("getName"));
  }

  // Tests constructor with @extends annotation inherits properties
  @Test
  public void testCreateScope_constructorExtends_setsSuperClassPrototype() {
    testSame("/** @constructor */ function SuperClass() {}\n"
        + "SuperClass.prototype.superProp = 123;\n"
        + "/** @constructor\n * @extends {SuperClass} */ function SubClass() {}\n"
        + "SubClass.prototype = new SuperClass();");
    Scope.Var subCtorVar = globalScope.getVar("SubClass");
    assertNotNull(subCtorVar);
    FunctionType subCtor = subCtorVar.getType().toMaybeFunctionType();
    assertNotNull(subCtor);
    FunctionType superCtor = subCtor.getSuperClassConstructor();
    assertNotNull(superCtor);
    assertEquals("SuperClass", superCtor.getReferenceName());
  }

  // Tests constructor with @implements annotation registers implemented interface
  @Test
  public void testCreateScope_constructorImplements_setsImplementedInterfaces() {
    testSame("/** @interface */ function Disposable() {}\n"
        + "/** @constructor\n * @implements {Disposable} */ function Resource() {}");
    Scope.Var ctorVar = globalScope.getVar("Resource");
    assertNotNull(ctorVar);
    FunctionType ctor = ctorVar.getType().toMaybeFunctionType();
    assertNotNull(ctor);
    assertNotNull(ctor.getImplementedInterfaces());
    assertFalse(ctor.getImplementedInterfaces().isEmpty());
  }

  // Tests stub declaration with JSDoc type annotation on property
  @Test
  public void testCreateScope_typedStubDeclaration_hasDeclaredType() {
    testSame("var ns = {};\n"
        + "/** @type {string} */ ns.name;");
    Scope.Var stubVar = globalScope.getVar("ns.name");
    assertNotNull(stubVar);
    assertEquals(getNativeType(STRING_TYPE), stubVar.getType());
  }

  // Tests function with @this type annotation
  @Test
  public void testCreateScope_functionWithThisAnnotation_hasCorrectThisType() {
    testSame("/** @constructor */ function Context() {}\n"
        + "/** @this {Context} */ function helper() { this.x = 1; }");
    Scope.Var fnVar = globalScope.getVar("helper");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertNotNull(fnType);
    assertEquals("Context", fnType.getTypeOfThis().toMaybeObjectType().getReferenceName());
  }

  // Tests namespaced enum declaration
  @Test
  public void testCreateScope_namespacedEnum_createsEnumType() {
    testSame("var ns = {};\n"
        + "/** @enum {string} */ ns.Status = { OK: 'ok', ERROR: 'error' };");
    Scope.Var enumVar = globalScope.getVar("ns.Status");
    assertNotNull(enumVar);
    assertTrue(enumVar.getType().isEnumType());
    EnumType enumType = (EnumType) enumVar.getType();
    assertEquals(getNativeType(STRING_TYPE), enumType.getElementsType());
    assertTrue(enumType.hasProperty("OK"));
    assertTrue(enumType.hasProperty("ERROR"));
  }

  // Tests function with @return {void}
  @Test
  public void testCreateScope_functionReturningVoid_hasVoidReturnType() {
    testSame("/** @return {void} */ function doWork() {}");
    Scope.Var fnVar = globalScope.getVar("doWork");
    assertNotNull(fnVar);
    FunctionType fnType = fnVar.getType().toMaybeFunctionType();
    assertEquals(getNativeType(VOID_TYPE), fnType.getReturnType());
  }
}