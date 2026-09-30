package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private JSType NUMBER_TYPE;
  private JSType STRING_TYPE;
  private ObjectType OBJECT_TYPE;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    NUMBER_TYPE = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING_TYPE = registry.getNativeType(JSTypeNative.STRING_TYPE);
    OBJECT_TYPE = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests constructor creation and basic properties
  @Test
  public void testConstructor_basicProperties_returnsTrueForConstructor() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isInterface());
    assertFalse(ctor.isOrdinaryFunction());
    assertTrue(ctor.isFunctionType());
    assertTrue(ctor.canBeCalled());
    assertTrue(ctor.hasInstanceType());
    assertNotNull(ctor.getInstanceType());
    assertEquals(ctor.getInstanceType(), ctor.getTypeOfThis());
  }

  // Tests interface creation and basic properties
  @Test
  public void testInterface_basicProperties_returnsTrueForInterface() {
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);
    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.isFunctionType());
    assertTrue(iface.canBeCalled());
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
    assertEquals("IFoo", iface.getReferenceName());
  }

  // Tests ordinary function creation and basic properties
  @Test
  public void testOrdinaryFunction_basicProperties_returnsTrueForOrdinaryFunction() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isFunctionType());
    assertFalse(fn.hasInstanceType());
  }

  // Tests exception when getting instance type on non-constructor
  @Test(expected = IllegalStateException.class)
  public void testGetInstanceType_ordinaryFunction_throwsException() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    fn.getInstanceType();
  }

  // Tests getMinArguments and getMaxArguments with required, optional, and varargs
  @Test
  public void testGetMinAndMaxArguments_withDifferentParamTypes_returnsCorrectCounts() {
    Node paramsNode = new Node(Token.LP);
    Node reqParam = Node.newString(Token.NAME, "a");
    reqParam.setJSType(NUMBER_TYPE);
    paramsNode.addChildToBack(reqParam);

    Node optParam = Node.newString(Token.NAME, "b");
    optParam.setJSType(STRING_TYPE);
    optParam.setOptionalArg(true);
    paramsNode.addChildToBack(optParam);

    ArrowType arrow = new ArrowType(registry, paramsNode, NUMBER_TYPE);
    FunctionType fn = new FunctionType(registry, "foo", null, arrow, null, null, false, false);

    assertEquals(1, fn.getMinArguments());
    assertEquals(2, fn.getMaxArguments());

    Node varParam = Node.newString(Token.NAME, "c");
    varParam.setJSType(NUMBER_TYPE);
    varParam.setVarArgs(true);
    paramsNode.addChildToBack(varParam);

    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  // Tests getParameters when parameters node is empty
  @Test
  public void testGetParameters_noParams_returnsEmptyIterable() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    assertNotNull(fn.getParameters());
    assertFalse(fn.getParameters().iterator().hasNext());
    assertEquals(0, fn.getMinArguments());
    assertEquals(0, fn.getMaxArguments());
  }

  // Tests getPrototype and setPrototype
  @Test
  public void testGetAndSetPrototype_customPrototype_updatesPrototypeCorrectly() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    FunctionPrototypeType proto = ctor.getPrototype();
    assertNotNull(proto);
    assertEquals(ctor, proto.getOwnerFunction());

    // Setting null prototype returns false
    assertFalse(ctor.setPrototype(null));

    // Setting instance type as prototype on constructor returns false
    assertFalse(ctor.setPrototype((FunctionPrototypeType) (ObjectType) ctor.getInstanceType()));

    FunctionPrototypeType newProto = new FunctionPrototypeType(registry, ctor, OBJECT_TYPE);
    assertTrue(ctor.setPrototype(newProto));
    assertEquals(newProto, ctor.getPrototype());
  }

  // Tests setPrototypeBasedOn
  @Test
  public void testSetPrototypeBasedOn_withBaseType_setsImplicitPrototype() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    ctor.setPrototypeBasedOn(OBJECT_TYPE);
    assertEquals(OBJECT_TYPE, ctor.getPrototype().getImplicitPrototype());

    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    ctor.setPrototypeBasedOn(strObj);
    assertEquals(strObj, ctor.getPrototype().getImplicitPrototype());
  }

  // Tests prototype inheritance and superclass traversal
  @Test
  public void testGetSuperClassConstructor_withInheritance_returnsParentConstructor() {
    FunctionType parentCtor = registry.createConstructorType("Parent", null, null, null);
    FunctionType childCtor = registry.createConstructorType("Child", null, null, null);

    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());

    assertEquals(parentCtor, childCtor.getSuperClassConstructor());
    assertNull(parentCtor.getSuperClassConstructor());
    assertNotNull(parentCtor.getSubTypes());
    assertTrue(parentCtor.getSubTypes().contains(childCtor));
  }

  // Tests hasUnknownSupertype
  @Test
  public void testHasUnknownSupertype_knownAndUnknownHierarchy_detectsCorrectly() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    assertFalse(ctor.hasUnknownSupertype());

    ctor.getPrototype().setImplicitPrototype(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    assertTrue(ctor.hasUnknownSupertype());
  }

  // Tests getTopMostDefiningType
  @Test
  public void testGetTopMostDefiningType_inheritedProperty_returnsTopMostType() {
    FunctionType parentCtor = registry.createConstructorType("Parent", null, null, null);
    parentCtor.getPrototype().defineProperty("prop", NUMBER_TYPE, false, false);

    FunctionType childCtor = registry.createConstructorType("Child", null, null, null);
    childCtor.setPrototypeBasedOn(parentCtor.getInstanceType());
    childCtor.getPrototype().defineProperty("prop", NUMBER_TYPE, false, false);

    assertEquals(parentCtor.getInstanceType(), childCtor.getTopMostDefiningType("prop"));
  }

  // Tests implemented interfaces traversal
  @Test
  public void testGetAllImplementedInterfaces_withSubInterfaces_returnsAllInterfaces() {
    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I2", null);
    iface2.setPrototypeBasedOn(iface1.getInstanceType());

    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface2.getInstanceType()));

    Iterable<ObjectType> allInterfaces = ctor.getAllImplementedInterfaces();
    Set<ObjectType> ifaceSet = com.google.common.collect.Sets.newHashSet(allInterfaces);
    assertTrue(ifaceSet.contains(iface2.getInstanceType()));
    assertTrue(ifaceSet.contains(iface1.getInstanceType()));
  }

  // Tests getPropertyType for built-in properties like prototype, call, apply
  @Test
  public void testGetPropertyType_prototypeCallApply_returnsExpectedTypes() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);

    assertTrue(fn.hasProperty("prototype"));
    assertTrue(fn.hasOwnProperty("prototype"));
    assertTrue(fn.isPropertyTypeInferred("prototype"));
    assertEquals(fn.getPrototype(), fn.getPropertyType("prototype"));

    JSType callProp = fn.getPropertyType("call");
    assertNotNull(callProp);
    assertTrue(callProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    assertNotNull(applyProp);
    assertTrue(applyProp.isFunctionType());
  }

  // Tests defineProperty for prototype property
  @Test
  public void testDefineProperty_prototype_updatesPrototypeObject() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    ObjectType customObj = registry.createAnonymousObjectType();
    boolean result = fn.defineProperty("prototype", customObj, false, false);
    assertTrue(result);
    assertEquals(customObj, fn.getPrototype().getImplicitPrototype());

    // Non-object type for prototype fails
    boolean invalidResult = fn.defineProperty("prototype", NUMBER_TYPE, false, false);
    assertFalse(invalidResult);
  }

  // Tests isEquivalentTo across constructors, interfaces, and ordinary functions
  @Test
  public void testIsEquivalentTo_matchingAndNonMatchingTypes_returnsExpected() {
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE);
    FunctionType fn2 = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE);
    FunctionType fn3 = registry.createFunctionType(STRING_TYPE, NUMBER_TYPE);

    assertTrue(fn1.isEquivalentTo(fn2));
    assertFalse(fn1.isEquivalentTo(fn3));
    assertFalse(fn1.isEquivalentTo(NUMBER_TYPE));

    FunctionType ctor1 = registry.createConstructorType("C1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("C1", null, null, null);
    assertFalse(ctor1.isEquivalentTo(ctor2)); // Constructors are only equivalent to themselves
    assertTrue(ctor1.isEquivalentTo(ctor1));

    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "I2", null);
    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(iface3));
  }

  // Tests isSubtype logic for ordinary functions, interfaces, and constructors
  @Test
  public void testIsSubtype_variousFunctionTypes_returnsCorrectSubtypeRelation() {
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE);
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);

    // Any function is a subtype of an interface
    assertTrue(fn1.isSubtype(iface));
    // Interface is not a subtype of ordinary function
    assertFalse(iface.isSubtype(fn1));

    // Universal constructor instance type check
    JSType u2u = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
    assertTrue(u2u.isConstructor());
  }

  // Tests getLeastSupertype and getGreatestSubtype for ordinary functions
  @Test
  public void testLeastSupertypeAndGreatestSubtype_ordinaryFunctions_mergesCorrectly() {
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE);
    FunctionType fn2 = registry.createFunctionType(STRING_TYPE, NUMBER_TYPE);

    JSType sup = fn1.getLeastSupertype(fn2);
    assertTrue(sup.isFunctionType());
    FunctionType supFn = (FunctionType) sup;
    assertTrue(supFn.getReturnType().isUnionType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    assertTrue(inf.isFunctionType());
  }

  // Tests toString and toDebugHashCodeString
  @Test
  public void testToStringAndDebugHashCode_returnsNonEmptyString() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE, STRING_TYPE);
    String str = fn.toString();
    assertNotNull(str);
    assertTrue(str.contains("function"));

    String debugStr = fn.toDebugHashCodeString();
    assertNotNull(debugStr);
    assertTrue(debugStr.contains("function"));

    FunctionType fnNative = registry.getNativeFunctionType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnNative.toString());
  }

  // Tests resolveInternal to ensure safe resolution of arrow, prototype, and interfaces
  @Test
  public void testResolveInternal_withScope_resolvesComponents() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    JSType resolved = ctor.resolve(errorReporter, registry.getTopScope());
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
    assertTrue(((FunctionType) resolved).isConstructor());
  }

  // Tests nominal constructor and interface properties
  @Test
  public void testNominalConstructorAndInterface_nominalTypes_flagsSetCorrectly() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    assertTrue(ctor.isNominalConstructor());
    assertFalse(ctor.isNominalInterface());

    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);
    assertTrue(iface.isNominalInterface());
    assertFalse(iface.isNominalConstructor());

    FunctionType anonCtor = registry.createConstructorType(null, null, null, null);
    assertFalse(anonCtor.isNominalConstructor());
  }

  // Tests clearSubTypes and hasSubTypes
  @Test
  public void testClearSubTypes_managesSubTypesLifecycle() {
    FunctionType parent = registry.createConstructorType("Parent", null, null, null);
    FunctionType child = registry.createConstructorType("Child", null, null, null);

    assertFalse(parent.hasSubTypes());
    assertEquals(0, parent.getSubTypes().size());

    child.setPrototypeBasedOn(parent.getInstanceType());
    assertTrue(parent.hasSubTypes());
    assertEquals(1, parent.getSubTypes().size());

    parent.clearSubTypes();
    assertFalse(parent.hasSubTypes());
    assertEquals(0, parent.getSubTypes().size());
  }

  // Tests getExtendedInterfacesCount and getExtendedInterfaces
  @Test
  public void testExtendedInterfaces_interfaceHierarchy_tracksExtendedInterfaces() {
    FunctionType ifaceParent = FunctionType.forInterface(registry, "IParent", null);
    FunctionType ifaceChild = FunctionType.forInterface(registry, "IChild", null);
    ifaceChild.setExtendedInterfaces(ImmutableList.of(ifaceParent.getInstanceType()));

    assertEquals(1, ifaceChild.getExtendedInterfacesCount());
    List<ObjectType> extended = ImmutableList.copyOf(ifaceChild.getExtendedInterfaces());
    assertEquals(1, extended.size());
    assertEquals(ifaceParent.getInstanceType(), extended.get(0));

    assertEquals(0, ifaceParent.getExtendedInterfacesCount());
  }

  // Tests hasEqualCallType comparison
  @Test
  public void testHasEqualCallType_comparesCallSignaturesAccurately() {
    FunctionType fn1 = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE);
    FunctionType fn2 = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE);
    FunctionType fn3 = registry.createFunctionType(STRING_TYPE, NUMBER_TYPE);

    assertTrue(fn1.hasEqualCallType(fn2));
    assertFalse(fn1.hasEqualCallType(fn3));
  }

  // Tests getBindReturnType calculation
  @Test
  public void testGetBindReturnType_varyingArgCount_computesBoundFunctionType() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE, NUMBER_TYPE, STRING_TYPE);

    // Binding 'this' only (0 args)
    FunctionType bound0 = fn.getBindReturnType(0);
    assertNotNull(bound0);
    assertEquals(2, bound0.getMinArguments());

    // Binding 'this' + 1 arg
    FunctionType bound1 = fn.getBindReturnType(1);
    assertNotNull(bound1);
    assertEquals(1, bound1.getMinArguments());

    // Binding all args
    FunctionType bound2 = fn.getBindReturnType(2);
    assertNotNull(bound2);
    assertEquals(0, bound2.getMinArguments());
  }

  // Tests autoboxesTo method returns null for FunctionType
  @Test
  public void testAutoboxesTo_returnsNull() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    assertNull(fn.autoboxesTo());
  }

  // Tests visit method with Visitor
  @Test
  public void testVisit_visitorPattern_dispatchesToCaseFunctionType() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    Visitor<String> visitor = new Visitor<String>() {
      @Override
      public String caseNoType() { return "no"; }
      @Override
      public String caseEnumElementType(EnumElementType type) { return "enumElement"; }
      @Override
      public String caseAllType() { return "all"; }
      @Override
      public String caseBooleanType() { return "boolean"; }
      @Override
      public String caseNoObjectType() { return "noObject"; }
      @Override
      public String caseFunctionType(FunctionType type) { return "function"; }
      @Override
      public String caseObjectType(ObjectType type) { return "object"; }
      @Override
      public String caseUnknownType() { return "unknown"; }
      @Override
      public String caseNullType() { return "null"; }
      @Override
      public String caseNamedType(NamedType type) { return "named"; }
      @Override
      public String caseNumberType() { return "number"; }
      @Override
      public String caseStringType() { return "string"; }
      @Override
      public String caseVoidType() { return "void"; }
      @Override
      public String caseUnionType(UnionType type) { return "union"; }
      @Override
      public String caseTemplateType(TemplateType templateType) { return "template"; }
    };
    assertEquals("function", fn.visit(visitor));
  }

  // Tests matchConstraint method
  @Test
  public void testMatchConstraint_withTargetType_executesConstraintMatching() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    FunctionType targetFn = registry.createFunctionType(NUMBER_TYPE, STRING_TYPE);
    fn.matchConstraint(targetFn);
    // Should safely execute without throwing
    assertNotNull(fn);
  }

  // Tests getOwnSlot on defined properties
  @Test
  public void testGetOwnSlot_customAndPrototypeProperties_returnsValidSlot() {
    FunctionType fn = registry.createFunctionType(NUMBER_TYPE);
    fn.defineProperty("customProp", STRING_TYPE, false, null);

    assertNotNull(fn.getOwnSlot("customProp"));
    assertEquals(STRING_TYPE, fn.getOwnSlot("customProp").getType());
    assertNull(fn.getOwnSlot("nonExistent"));
  }

  // Tests function with custom thisType
  @Test
  public void testFunctionWithCustomThisType_typeOfThis_returnsSpecifiedType() {
    ObjectType customThis = registry.createAnonymousObjectType();
    FunctionType fn = registry.createFunctionType(customThis, NUMBER_TYPE, ImmutableList.of(STRING_TYPE));

    assertEquals(customThis, fn.getTypeOfThis());
    assertEquals(NUMBER_TYPE, fn.getReturnType());
  }

  // Tests displayName and source node tracking
  @Test
  public void testDisplayNameAndSource_accessors_returnsProvidedValues() {
    Node sourceNode = Node.newString(Token.NAME, "myFunc");
    FunctionType fn = registry.createConstructorType("MyClass", sourceNode, null, null);

    assertEquals("MyClass", fn.getDisplayName());
    assertEquals(sourceNode, fn.getSource());
  }
}