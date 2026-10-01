package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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

  private FunctionType createFunction(Node params, JSType returnType) {
    return registry.createFunctionType(returnType, params);
  }

  private FunctionType createConstructor(String name, Node params, JSType returnType) {
    return registry.createConstructorType(name, null, params, returnType);
  }

  private FunctionType createInterface(String name) {
    return registry.createInterfaceType(name, null);
  }

  // Tests constructor, ordinary function, and interface kind predicates
  @Test
  public void testKindPredicates_differentKinds_returnExpectedFlags() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ordinary = createFunction(params, numberType);
    assertTrue(ordinary.isOrdinaryFunction());
    assertFalse(ordinary.isConstructor());
    assertFalse(ordinary.isInterface());
    assertFalse(ordinary.hasInstanceType());

    FunctionType ctor = createConstructor("MyClass", params, numberType);
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());

    FunctionType iface = createInterface("MyInterface");
    assertFalse(iface.isOrdinaryFunction());
    assertFalse(iface.isConstructor());
    assertTrue(iface.isInterface());
    assertTrue(iface.hasInstanceType());
  }

  // Tests makesStructs and makesDicts inheritance from superclass
  @Test
  public void testMakesStructsAndDicts_inheritance_propagatesFlags() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType superCtor = createConstructor("SuperClass", params, null);
    FunctionType subCtor = createConstructor("SubClass", params, null);

    assertFalse(subCtor.makesStructs());
    assertFalse(subCtor.makesDicts());

    superCtor.setStruct();
    assertTrue(superCtor.makesStructs());
    assertFalse(superCtor.makesDicts());

    subCtor.getPrototype().setImplicitPrototype(superCtor.getPrototype());
    assertTrue(subCtor.makesStructs());

    FunctionType dictSuper = createConstructor("DictSuper", params, null);
    dictSuper.setDict();
    FunctionType dictSub = createConstructor("DictSub", params, null);
    dictSub.getPrototype().setImplicitPrototype(dictSuper.getPrototype());
    assertTrue(dictSub.makesDicts());
  }

  // Tests min and max arguments calculation for required, optional, and varargs
  @Test
  public void testGetMinAndMaxArguments_variousParamSignatures_calculatesCorrectly() {
    Node params = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "req");
    p1.setJSType(numberType);
    Node p2 = Node.newString(Token.NAME, "opt");
    p2.setJSType(stringType);
    p2.setOptionalArg(true);
    Node p3 = Node.newString(Token.NAME, "rest");
    p3.setJSType(booleanType);
    p3.setVarArgs(true);

    params.addChildToBack(p1);
    params.addChildToBack(p2);
    params.addChildToBack(p3);

    FunctionType fn = createFunction(params, numberType);
    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());

    Node fixedParams = new Node(Token.PARAM_LIST);
    Node fixed1 = Node.newString(Token.NAME, "a");
    fixed1.setJSType(numberType);
    Node fixed2 = Node.newString(Token.NAME, "b");
    fixed2.setJSType(stringType);
    fixedParams.addChildToBack(fixed1);
    fixedParams.addChildToBack(fixed2);

    FunctionType fixedFn = createFunction(fixedParams, numberType);
    assertEquals(2, fixedFn.getMinArguments());
    assertEquals(2, fixedFn.getMaxArguments());
  }

  // Tests getPrototype lazy initialization and slot retrieval
  @Test
  public void testPrototype_lazyInitializationAndSlots_returnsConsistentPrototype() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ctor = createConstructor("Widget", params, null);

    assertNotNull(ctor.getPrototype());
    assertEquals("Widget.prototype", ctor.getPrototype().getReferenceName());
    assertNotNull(ctor.getSlot("prototype"));
    assertEquals(ctor.getPrototype(), ctor.getSlot("prototype").getType());

    Set<String> propNames = ctor.getOwnPropertyNames();
    assertTrue(propNames.contains("prototype"));
  }

  // Tests setPrototype with null or invalid values
  @Test
  public void testSetPrototype_nullOrInstanceMismatch_handlesGracefully() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ctor = createConstructor("Widget", params, null);

    assertFalse(ctor.setPrototype(null, null));
    assertFalse(ctor.setPrototype(ctor.getInstanceType(), null));

    ObjectType newProto = registry.createAnonymousObjectType(objectType);
    assertTrue(ctor.setPrototype(newProto, null));
    assertEquals(newProto, ctor.getPrototype());
  }

  // Tests implemented interfaces handling on constructor
  @Test
  public void testImplementedInterfaces_constructor_registersAndRetrievesCorrectly() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ctor = createConstructor("Service", params, null);
    FunctionType iface = createInterface("IService");

    assertFalse(ctor.hasImplementedInterfaces());
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    assertTrue(ctor.hasImplementedInterfaces());
    List<ObjectType> impls = ImmutableList.copyOf(ctor.getImplementedInterfaces());
    assertEquals(1, impls.size());
    assertEquals(iface.getInstanceType(), impls.get(0));
  }

  // Tests exception when setting implemented interfaces on ordinary function
  @Test(expected = UnsupportedOperationException.class)
  public void testSetImplementedInterfaces_ordinaryFunction_throwsException() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType fn = createFunction(params, numberType);
    FunctionType iface = createInterface("IService");
    fn.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));
  }

  // Tests extended interfaces handling on interface
  @Test
  public void testExtendedInterfaces_interfaceType_addsAndCountsCorrectly() {
    FunctionType superIface = createInterface("SuperIface");
    FunctionType subIface = createInterface("SubIface");

    assertEquals(0, subIface.getExtendedInterfacesCount());
    subIface.setExtendedInterfaces(ImmutableList.of(superIface.getInstanceType()));

    assertEquals(1, subIface.getExtendedInterfacesCount());
    List<ObjectType> extended = ImmutableList.copyOf(subIface.getAllExtendedInterfaces());
    assertEquals(1, extended.size());
    assertEquals(superIface.getInstanceType(), extended.get(0));
  }

  // Tests exception when setting extended interfaces on constructor
  @Test(expected = UnsupportedOperationException.class)
  public void testSetExtendedInterfaces_constructor_throwsException() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ctor = createConstructor("ClassA", params, null);
    ctor.setExtendedInterfaces(ImmutableList.<ObjectType>of());
  }

  // Tests lazy properties: call, apply, bind
  @Test
  public void testGetPropertyType_callApplyBind_definesPropertiesCorrectly() {
    Node params = new Node(Token.PARAM_LIST);
    Node p = Node.newString(Token.NAME, "x");
    p.setJSType(numberType);
    params.addChildToBack(p);

    FunctionType fn = createFunction(params, stringType);

    JSType callProp = fn.getPropertyType("call");
    assertNotNull(callProp);
    assertTrue(callProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    assertNotNull(applyProp);
    assertTrue(applyProp.isFunctionType());

    JSType bindProp = fn.getPropertyType("bind");
    assertNotNull(bindProp);
    assertTrue(bindProp.isFunctionType());
  }

  // Tests getBindReturnType with argument count variations
  @Test
  public void testGetBindReturnType_variousArgs_returnsExpectedFunction() {
    Node params = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(numberType);
    Node p2 = Node.newString(Token.NAME, "b");
    p2.setJSType(stringType);
    params.addChildToBack(p1);
    params.addChildToBack(p2);

    FunctionType fn = createFunction(params, booleanType);

    FunctionType boundAll = fn.getBindReturnType(-1);
    assertEquals(booleanType, boundAll.getReturnType());

    FunctionType boundOne = fn.getBindReturnType(2);
    assertEquals(booleanType, boundOne.getReturnType());
    assertEquals(1, boundOne.getParametersNode().getChildCount());
  }

  // Tests supAndInfHelper for equivalent and subtype functions
  @Test
  public void testSupAndInfHelper_ordinaryFunctions_computesSupAndInf() {
    Node params1 = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "a");
    p1.setJSType(numberType);
    params1.addChildToBack(p1);

    Node params2 = new Node(Token.PARAM_LIST);
    Node p2 = Node.newString(Token.NAME, "a");
    p2.setJSType(numberType);
    params2.addChildToBack(p2);

    FunctionType fn1 = createFunction(params1, numberType);
    FunctionType fn2 = createFunction(params2, numberType);

    FunctionType sup = fn1.supAndInfHelper(fn2, true);
    assertNotNull(sup);
    assertTrue(sup.isOrdinaryFunction());

    FunctionType inf = fn1.supAndInfHelper(fn2, false);
    assertNotNull(inf);
    assertTrue(inf.isOrdinaryFunction());

    FunctionType sameSup = fn1.supAndInfHelper(fn1, true);
    assertEquals(fn1, sameSup);
  }

  // Tests checkFunctionEquivalenceHelper
  @Test
  public void testCheckFunctionEquivalenceHelper_constructorsAndInterfaces_checksEquality() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType ctor1 = createConstructor("Foo", params, numberType);
    FunctionType ctor2 = createConstructor("Foo", params, numberType);

    assertTrue(ctor1.checkFunctionEquivalenceHelper(ctor1, EquivalenceMethod.IDENTITY));
    assertFalse(ctor1.checkFunctionEquivalenceHelper(ctor2, EquivalenceMethod.IDENTITY));

    FunctionType iface1 = createInterface("IFoo");
    FunctionType iface2 = createInterface("IFoo");
    FunctionType iface3 = createInterface("IBar");

    assertTrue(iface1.checkFunctionEquivalenceHelper(iface2, EquivalenceMethod.IDENTITY));
    assertFalse(iface1.checkFunctionEquivalenceHelper(iface3, EquivalenceMethod.IDENTITY));
    assertFalse(ctor1.checkFunctionEquivalenceHelper(iface1, EquivalenceMethod.IDENTITY));
  }

  // Tests isSubtype for functions and interfaces
  @Test
  public void testIsSubtype_interfaceAndFunctions_returnsExpectedSubtyping() {
    FunctionType iface = createInterface("IFoo");
    Node params = new Node(Token.PARAM_LIST);
    FunctionType fn = createFunction(params, numberType);

    assertTrue(fn.isSubtype(iface));
    assertFalse(iface.isSubtype(fn));

    FunctionType fn2 = createFunction(params, numberType);
    assertTrue(fn.isSubtype(fn2));
  }

  // Tests toStringHelper formatting for annotations and general strings
  @Test
  public void testToStringHelper_withVariousParams_formatsCorrectString() {
    Node params = new Node(Token.PARAM_LIST);
    Node p1 = Node.newString(Token.NAME, "req");
    p1.setJSType(numberType);
    Node p2 = Node.newString(Token.NAME, "opt");
    p2.setJSType(stringType);
    p2.setOptionalArg(true);
    Node p3 = Node.newString(Token.NAME, "rest");
    p3.setJSType(booleanType);
    p3.setVarArgs(true);

    params.addChildToBack(p1);
    params.addChildToBack(p2);
    params.addChildToBack(p3);

    FunctionType fn = createFunction(params, numberType);
    String str = fn.toStringHelper(false);
    assertTrue(str.startsWith("function ("));
    assertTrue(str.contains("number"));
    assertTrue(str.contains("string="));
    assertTrue(str.contains("...[boolean]"));
    assertTrue(str.endsWith("): number"));
  }

  // Tests cloneWithoutArrowType creates clean constructor
  @Test
  public void testCloneWithoutArrowType_validConstructor_clonesWithoutArrow() {
    Node params = new Node(Token.PARAM_LIST);
    Node p = Node.newString(Token.NAME, "x");
    p.setJSType(numberType);
    params.addChildToBack(p);

    FunctionType ctor = createConstructor("ClonedClass", params, numberType);
    FunctionType cloned = ctor.cloneWithoutArrowType();

    assertTrue(cloned.isConstructor());
    assertEquals("ClonedClass", cloned.getReferenceName());
    assertEquals(0, cloned.getParametersNode().getChildCount());
  }

  // Tests getTopMostDefiningType and getSuperClassConstructor
  @Test
  public void testGetTopMostDefiningType_hierarchy_resolvesTopDefiningType() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType baseCtor = createConstructor("Base", params, null);
    baseCtor.getPrototype().defineProperty("sharedProp", numberType, false, null);

    FunctionType subCtor = createConstructor("Sub", params, null);
    subCtor.getPrototype().setImplicitPrototype(baseCtor.getPrototype());
    subCtor.getInstanceType().defineProperty("sharedProp", numberType, false, null);

    assertEquals(baseCtor, subCtor.getSuperClassConstructor());
    ObjectType topType = subCtor.getTopMostDefiningType("sharedProp");
    assertEquals(baseCtor.getInstanceType(), topType);
  }

  // Tests clearCachedValues and subtype hierarchy traversal
  @Test
  public void testClearCachedValues_withSubtypes_clearsWithoutErrors() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType baseCtor = createConstructor("BaseClass", params, null);
    FunctionType subCtor = createConstructor("SubClass", params, null);

    subCtor.getPrototype().setImplicitPrototype(baseCtor.getPrototype());
    baseCtor.clearCachedValues();

    List<FunctionType> subTypes = baseCtor.getSubTypes();
    assertNotNull(subTypes);
    assertTrue(subTypes.contains(subCtor));
  }

  // Tests isConstructor and isInterface mutators and typeOfThis handling
  @Test
  public void testTypeOfThisAndConstructorFlags() {
    Node params = new Node(Token.PARAM_LIST);
    ObjectType customThis = registry.createAnonymousObjectType(objectType);
    FunctionType fnWithThis = registry.createFunctionType(customThis, numberType, ImmutableList.<JSType>of());
    assertEquals(customThis, fnWithThis.getTypeOfThis());

    FunctionType ctor = createConstructor("Constructed", params, null);
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertEquals(ctor.getInstanceType(), ctor.getTypeOfThis());
  }

  // Tests isReturnTypeInferred flag
  @Test
  public void testIsReturnTypeInferred() {
    Node params = new Node(Token.PARAM_LIST);
    FunctionType fn = registry.createFunctionTypeWithInferredReturnType(
        objectType, numberType, params, null, null);
    assertTrue(fn.isReturnTypeInferred());

    FunctionType fnExplicit = createFunction(params, numberType);
    assertFalse(fnExplicit.isReturnTypeInferred());
  }
}