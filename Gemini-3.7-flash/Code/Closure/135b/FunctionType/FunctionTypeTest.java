package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests ordinary function type flags and properties
  @Test
  public void testOrdinaryFunction_creationAndFlags() {
    FunctionType fn = new FunctionType(registry, "testFn", null, null, numberType);

    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isOrdinaryFunction());
    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertFalse(fn.isInstanceType());
    assertFalse(fn.hasInstanceType());
    assertEquals(numberType, fn.getReturnType());
  }

  // Tests constructor function type flags and instance type
  @Test
  public void testConstructor_creationAndInstanceType() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals(ctor.getInstanceType(), ctor.getTypeOfThis());
  }

  // Tests interface function type flags and instance type
  @Test
  public void testInterface_creationAndFlags() {
    FunctionType iface = new FunctionType(registry, "AnInterface", null);

    assertFalse(iface.isConstructor());
    assertTrue(iface.isInterface());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
    assertNull(iface.getParametersNode());
    assertNull(iface.getReturnType());
  }

  // Tests argument counting: required, optional, and var_args
  @Test
  public void testArgumentCount_requiredOptionalVarArgs() {
    Node paramList = new Node(Token.LP);
    Node req1 = Node.newString(Token.NAME, "req1");
    req1.setJSType(numberType);
    Node opt1 = Node.newString(Token.NAME, "opt1");
    opt1.setJSType(stringType);
    opt1.setOptionalArg(true);
    Node varArg = Node.newString(Token.NAME, "rest");
    varArg.setJSType(booleanType);
    varArg.setVarArgs(true);

    paramList.addChildToBack(req1);
    paramList.addChildToBack(opt1);
    paramList.addChildToBack(varArg);

    FunctionType fn = new FunctionType(registry, "f", null, paramList, numberType);

    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    assertNotNull(fn.getParametersNode());
  }

  // Tests argument counting when there are no parameters
  @Test
  public void testArgumentCount_nullParameters() {
    FunctionType fn = new FunctionType(registry, "f", null, null, numberType);

    assertEquals(0, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    assertFalse(fn.getParameters().iterator().hasNext());
  }

  // Tests prototype lazily initialized and setPrototype handling
  @Test
  public void testPrototype_lazyInitializationAndSetting() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);

    FunctionPrototypeType proto = ctor.getPrototype();
    assertNotNull(proto);
    assertTrue(ctor.hasProperty("prototype"));
    assertEquals(proto, ctor.getPropertyType("prototype"));
    assertTrue(ctor.isPropertyTypeInferred("prototype"));

    assertFalse(ctor.setPrototype(null));
    assertFalse(ctor.setPrototype((FunctionPrototypeType) ctor.getInstanceType()));

    FunctionPrototypeType newProto = new FunctionPrototypeType(registry, ctor, objectType);
    assertTrue(ctor.setPrototype(newProto));
    assertEquals(newProto, ctor.getPrototype());
  }

  // Tests setPrototypeBasedOn creating and updating implicit prototype
  @Test
  public void testSetPrototypeBasedOn_implicitPrototype() {
    FunctionType ctor = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);
    ObjectType customBase = registry.createAnonymousObjectType();

    ctor.setPrototypeBasedOn(customBase);
    assertEquals(customBase, ctor.getPrototype().getImplicitPrototype());

    ObjectType anotherBase = registry.createAnonymousObjectType();
    ctor.setPrototypeBasedOn(anotherBase);
    assertEquals(anotherBase, ctor.getPrototype().getImplicitPrototype());
  }

  // Tests lazy definition of "call" and "apply" properties
  @Test
  public void testCallAndApply_propertiesCreatedLazily() {
    Node paramList = new Node(Token.LP);
    Node param = Node.newString(Token.NAME, "arg0");
    param.setJSType(numberType);
    paramList.addChildToBack(param);

    FunctionType fn = new FunctionType(registry, "f", null, paramList, stringType);

    JSType callProp = fn.getPropertyType("call");
    assertTrue(callProp.isFunctionType());
    FunctionType callFn = (FunctionType) callProp;
    assertEquals(stringType, callFn.getReturnType());

    JSType applyProp = fn.getPropertyType("apply");
    assertTrue(applyProp.isFunctionType());
    FunctionType applyFn = (FunctionType) applyProp;
    assertEquals(stringType, applyFn.getReturnType());
  }

  // Tests implemented interfaces and inheritance traversal
  @Test
  public void testImplementedInterfaces_hierarchy() {
    FunctionType iface1 = new FunctionType(registry, "I1", null);
    FunctionType iface2 = new FunctionType(registry, "I2", null);

    FunctionType parentCtor = new FunctionType(
        registry, "Parent", null, null, null, null, null, true, false);
    parentCtor.setImplementedInterfaces(ImmutableList.of(iface1.getInstanceType()));

    FunctionType childCtor = new FunctionType(
        registry, "Child", null, null, null, null, null, true, false);
    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());
    childCtor.setImplementedInterfaces(ImmutableList.of(iface2.getInstanceType()));

    Iterable<ObjectType> allDirect = childCtor.getImplementedInterfaces();
    assertTrue(allDirect.iterator().hasNext());

    Iterable<ObjectType> allResolved = childCtor.getAllImplementedInterfaces();
    assertTrue(allResolved.iterator().hasNext());
  }

  // Tests getLeastSupertype and getGreatestSubtype between function types
  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    FunctionType fn1 = new FunctionType(registry, "f1", null, null, numberType);
    FunctionType fn2 = new FunctionType(registry, "f2", null, null, stringType);
    JSType fnInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);

    assertSame(fn1, fn1.getLeastSupertype(fn1));
    assertEquals(registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE),
        fn1.getLeastSupertype(fn2));
    assertEquals(fnInstance, fn1.getLeastSupertype(fnInstance));

    assertSame(fn1, fn1.getGreatestSubtype(fn1));
    assertEquals(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        fn1.getGreatestSubtype(fn2));
    assertEquals(fn1, fn1.getGreatestSubtype(fnInstance));
  }

  // Tests getSuperClassConstructor and getTopMostDefiningType
  @Test
  public void testSuperClassConstructorAndTopMostDefiningType() {
    FunctionType superCtor = new FunctionType(
        registry, "Super", null, null, null, null, null, true, false);
    superCtor.getPrototype().defineProperty("sharedProp", numberType, false, false);

    FunctionType subCtor = new FunctionType(
        registry, "Sub", null, null, null, null, null, true, false);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    assertEquals(superCtor, subCtor.getSuperClassConstructor());
    assertNull(superCtor.getSuperClassConstructor());
    assertEquals(superCtor.getInstanceType(), subCtor.getTopMostDefiningType("sharedProp"));
  }

  // Tests hasUnknownSupertype when no unknown supertype exists
  @Test
  public void testHasUnknownSupertype_knownChainReturnsFalse() {
    FunctionType superCtor = new FunctionType(
        registry, "Super", null, null, null, null, null, true, false);
    FunctionType subCtor = new FunctionType(
        registry, "Sub", null, null, null, null, null, true, false);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    assertFalse(subCtor.hasUnknownSupertype());
  }

  // Tests equals and hashCode across function kinds
  @Test
  public void testEqualsAndHashCode_constructorsAndInterfaces() {
    FunctionType ctor1 = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);
    FunctionType ctor2 = new FunctionType(
        registry, "Ctor", null, null, null, null, null, true, false);
    assertFalse(ctor1.equals(ctor2));

    FunctionType iface1 = new FunctionType(registry, "Iface", null);
    FunctionType iface2 = new FunctionType(registry, "Iface", null);
    assertTrue(iface1.equals(iface2));
    assertEquals(iface1.hashCode(), iface2.hashCode());
    assertFalse(iface1.equals(ctor1));

    FunctionType ord1 = new FunctionType(registry, "f", null, null, numberType);
    FunctionType ord2 = new FunctionType(registry, "f", null, null, numberType);
    assertTrue(ord1.equals(ord2));
    assertEquals(ord1.hashCode(), ord2.hashCode());
  }

  // Tests toString output for ordinary and special native function instance
  @Test
  public void testToString_formatting() {
    FunctionType fn = new FunctionType(registry, "f", null, null, numberType);
    assertEquals("function (): number", fn.toString());

    JSType fnInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnInstance.toString());
  }

  // Tests isSubtype relationships
  @Test
  public void testIsSubtype_variousTypes() {
    FunctionType fn1 = new FunctionType(registry, "f1", null, null, numberType);
    FunctionType fn2 = new FunctionType(registry, "f2", null, null, numberType);
    FunctionType iface = new FunctionType(registry, "Iface", null);

    assertTrue(fn1.isSubtype(fn1));
    assertTrue(fn1.isSubtype(fn2));
    assertTrue(fn1.isSubtype(iface));
    assertFalse(iface.isSubtype(fn1));
  }

  // Tests source node getter and setter
  @Test
  public void testSourceNode_getterAndSetter() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType fn = new FunctionType(registry, "f", fnNode, null, numberType);
    assertSame(fnNode, fn.getSource());

    Node newFnNode = new Node(Token.FUNCTION);
    fn.setSource(newFnNode);
    assertSame(newFnNode, fn.getSource());
  }

  // Tests constructor validation failure with invalid source node type
  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_invalidSourceNodeType_throwsException() {
    Node invalidNode = new Node(Token.NAME);
    new FunctionType(registry, "f", invalidNode, null, numberType);
  }

  // Tests getInstanceType throwing IllegalStateException on ordinary function
  @Test(expected = IllegalStateException.class)
  public void testGetInstanceType_ordinaryFunction_throwsException() {
    FunctionType fn = new FunctionType(registry, "f", null, null, numberType);
    fn.getInstanceType();
  }
}