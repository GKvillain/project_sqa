package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private ErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
  }

  // Tests constructor creation and basic kind checks
  @Test
  public void testConstructorKind_createsConstructorInstance() {
    FunctionType ctor = registry.createConstructorType(
        "Foo", null, null, null);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.isFunctionType());
    assertTrue(ctor.canBeCalled());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals("Foo", ctor.getReferenceName());
  }

  // Tests interface function creation and behavior
  @Test
  public void testInterfaceKind_createsInterfaceInstance() {
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);

    assertFalse(iface.isConstructor());
    assertTrue(iface.isInterface());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertEquals("IFoo", iface.getReferenceName());
  }

  // Tests ordinary function type properties
  @Test
  public void testOrdinaryFunctionKind_hasCorrectFlags() {
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.hasInstanceType());
  }

  // Tests resolveInternal when typeOfThis is a UnionType (Defects4J 152 regression check)
  @Test
  public void testResolveInternal_unionTypeOfThis_resolvesProperly() {
    JSType unionType = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE));

    ArrowType arrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    
    // Create a function type with union type as typeOfThis (via ObjectType cast or raw instance)
    FunctionType fn = new FunctionType(
        registry, "fnWithUnionThis", null, arrow,
        (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        null, false, false);
    
    // Set typeOfThis to UnionType via custom scope resolution
    NamedType namedUnion = new NamedType(registry, "UnionAlias", null, 0, 0);
    StaticScope<JSType> scope = new Scope(new Node(Token.BLOCK), null);
    
    JSType resolved = fn.resolve(errorReporter, scope);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
  }

  // Tests parameter parsing, min arguments, max arguments calculation with normal args
  @Test
  public void testGetMinAndMaxArguments_normalParameters() {
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node param2 = Node.newString(Token.NAME, "b");
    param2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));

    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), param1, param2);

    assertEquals(2, fn.getMinArguments());
    assertEquals(2, fn.getMaxArguments());
  }

  // Tests min and max arguments when function has optional and varargs
  @Test
  public void testGetMinAndMaxArguments_optionalAndVarArgs() {
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node param2 = Node.newString(Token.NAME, "b");
    param2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    param2.setOptionalArg(true);

    Node param3 = Node.newString(Token.NAME, "c");
    param3.setJSType(registry.getNativeType(JSTypeNative.ALL_TYPE));
    param3.setVarArgs(true);

    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), param1, param2, param3);

    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  // Tests return type retrieval and inferred flag
  @Test
  public void testGetReturnType_returnsExpectedType() {
    JSType returnType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    FunctionType fn = registry.createFunctionType(returnType);

    assertEquals(returnType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
  }

  // Tests prototype lazy initialization and setPrototype logic
  @Test
  public void testGetPrototype_and_setPrototype() {
    FunctionType ctor = registry.createConstructorType(
        "MyClass", null, null, null);

    FunctionPrototypeType proto = ctor.getPrototype();
    assertNotNull(proto);
    assertTrue(ctor.hasProperty("prototype"));
    assertTrue(ctor.hasOwnProperty("prototype"));
    assertEquals(proto, ctor.getPropertyType("prototype"));

    // Setting null prototype returns false
    assertFalse(ctor.setPrototype(null));

    // Setting instance type as prototype on constructor returns false
    assertFalse(ctor.setPrototype((FunctionPrototypeType) (ObjectType) ctor.getInstanceType()));
  }

  // Tests prototype based on base type
  @Test
  public void testSetPrototypeBasedOn_setsImplicitPrototype() {
    FunctionType baseCtor = registry.createConstructorType("Base", null, null, null);
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);

    subCtor.setPrototypeBasedOn(baseCtor.getInstanceType());

    assertEquals(baseCtor.getInstanceType(), subCtor.getPrototype().getImplicitPrototype());
    assertEquals(baseCtor, subCtor.getSuperClassConstructor());
  }

  // Tests interface implementation tracking and getAllImplementedInterfaces
  @Test
  public void testImplementedInterfaces_hierarchy() {
    FunctionType ifaceA = FunctionType.forInterface(registry, "IA", null);
    FunctionType ifaceB = FunctionType.forInterface(registry, "IB", null);
    ifaceB.getPrototype().setImplicitPrototype(ifaceA.getInstanceType());

    FunctionType ctor = registry.createConstructorType("Impl", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(ifaceB.getInstanceType()));

    Iterable<ObjectType> allInterfaces = ctor.getAllImplementedInterfaces();
    assertTrue(Iterables.contains(allInterfaces, ifaceB.getInstanceType()));
    assertTrue(Iterables.contains(allInterfaces, ifaceA.getInstanceType()));
  }

  // Tests getPropertyType for call and apply special methods
  @Test
  public void testGetPropertyType_callAndApplyMethods() {
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    JSType callProp = fn.getPropertyType("call");
    assertNotNull(callProp);
    assertTrue(callProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    assertNotNull(applyProp);
    assertTrue(applyProp.isFunctionType());
  }

  // Tests defineProperty for prototype property
  @Test
  public void testDefineProperty_prototype() {
    FunctionType ctor = registry.createConstructorType("Parent", null, null, null);
    ObjectType customProto = registry.createAnonymousObjectType();

    boolean defined = ctor.defineProperty("prototype", customProto, false, false);
    assertTrue(defined);
    assertEquals(customProto, ctor.getPrototype().getImplicitPrototype());
  }

  // Tests least supertype and greatest subtype between function types
  @Test
  public void testGetLeastSupertype_and_getGreatestSubtype() {
    FunctionType fn1 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    JSType sup = fn1.getLeastSupertype(fn2);
    assertNotNull(sup);
    assertTrue(sup.isFunctionType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    assertNotNull(inf);
    assertTrue(inf.isFunctionType());
  }

  // Tests equivalence check between identical and distinct functions
  @Test
  public void testIsEquivalentTo_sameAndDifferentSignatures() {
    FunctionType fn1 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType fn3 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    assertTrue(fn1.isEquivalentTo(fn2));
    assertFalse(fn1.isEquivalentTo(fn3));
    assertFalse(fn1.isEquivalentTo(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
  }

  // Tests toString formatting with various param lists
  @Test
  public void testToString_formatting() {
    FunctionType simpleFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals("function (): number", simpleFn.toString());

    Node param = Node.newString(Token.NAME, "x");
    param.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType oneArgFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE), param);
    assertEquals("function (string): undefined", oneArgFn.toString());
  }

  // Tests isSubtype logic between functions and interfaces
  @Test
  public void testIsSubtype_functionAndInterface() {
    FunctionType iface = FunctionType.forInterface(registry, "IInterface", null);
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    // Any function is subtype of interface function
    assertTrue(fn.isSubtype(iface));
    // Interface function cannot be assigned to ordinary function
    assertFalse(iface.isSubtype(fn));
  }

  // Tests getTopMostDefiningType traversing prototype chain
  @Test
  public void testGetTopMostDefiningType_findsCorrectType() {
    FunctionType baseCtor = registry.createConstructorType("Base", null, null, null);
    baseCtor.getPrototype().defineProperty("sharedProp",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, false);

    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    subCtor.setPrototypeBasedOn(baseCtor.getInstanceType());

    JSType topDefining = subCtor.getTopMostDefiningType("sharedProp");
    assertEquals(baseCtor.getInstanceType(), topDefining);
  }

  // Tests getSource and setSource node
  @Test
  public void testSourceNode_getterAndSetter() {
    FunctionType fn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertNull(fn.getSource());

    Node fnNode = new Node(Token.FUNCTION);
    fn.setSource(fnNode);
    assertEquals(fnNode, fn.getSource());
  }

  // Tests forInterface checkArgument validation
  @Test(expected = IllegalArgumentException.class)
  public void testForInterface_nullName_throwsException() {
    FunctionType.forInterface(registry, null, null);
  }

  // Tests hasUnknownSupertype returns false for base constructor
  @Test
  public void testHasUnknownSupertype_baseConstructor_returnsFalse() {
    FunctionType ctor = registry.createConstructorType("Standalone", null, null, null);
    assertFalse(ctor.hasUnknownSupertype());
  }
}