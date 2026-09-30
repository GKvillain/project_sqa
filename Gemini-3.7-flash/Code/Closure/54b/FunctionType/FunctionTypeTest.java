package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class FunctionTypeTest {
  private JSTypeRegistry registry;
  private FunctionType numberFunction;
  private FunctionType ctorFoo;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    
    // Normal function (number) -> number
    Node paramsNode = new Node(Token.LP,
        Node.newString(Token.NAME, "a"));
    paramsNode.getFirstChild().setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    ArrowType arrow = new ArrowType(
        registry, paramsNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    numberFunction = new FunctionType(
        registry, "numFn", null, arrow, null, null, false, false);

    // Constructor Foo
    ArrowType ctorArrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    ctorFoo = new FunctionType(
        registry, "Foo", null, ctorArrow, null, null, true, false);
  }

  // Tests constructor/interface/ordinary function identification
  @Test
  public void testKindProperties_variousFunctionKinds_returnCorrectFlags() {
    assertTrue(ctorFoo.isConstructor());
    assertFalse(ctorFoo.isInterface());
    assertFalse(ctorFoo.isOrdinaryFunction());
    assertTrue(ctorFoo.hasInstanceType());
    assertNotNull(ctorFoo.getInstanceType());

    assertFalse(numberFunction.isConstructor());
    assertFalse(numberFunction.isInterface());
    assertTrue(numberFunction.isOrdinaryFunction());
    assertFalse(numberFunction.hasInstanceType());

    FunctionType interfaceType = FunctionType.forInterface(registry, "InterfaceFoo", null);
    assertTrue(interfaceType.isInterface());
    assertFalse(interfaceType.isConstructor());
    assertFalse(interfaceType.isOrdinaryFunction());
    assertTrue(interfaceType.hasInstanceType());
  }

  // Tests argument counting methods (min/max arguments)
  @Test
  public void testGetMinMaxArguments_variousParams_calculatesCorrectCount() {
    assertEquals(1, numberFunction.getMinArguments());
    assertEquals(1, numberFunction.getMaxArguments());

    // Function with optional and var_args params
    Node required = Node.newString(Token.NAME, "req");
    required.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node opt = Node.newString(Token.NAME, "opt");
    opt.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    opt.setOptionalArg(true);
    Node varargs = Node.newString(Token.NAME, "rest");
    varargs.setJSType(registry.getNativeType(JSTypeNative.ALL_TYPE));
    varargs.setVarArgs(true);

    Node params = new Node(Token.LP, required, opt, varargs);
    ArrowType arrow = new ArrowType(registry, params, registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn = new FunctionType(registry, "fn", null, arrow, null, null, false, false);

    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  // Tests lazy prototype initialization and property slots
  @Test
  public void testGetPrototype_lazyInitialization_returnsValidPrototype() {
    assertFalse(ctorFoo.getOwnPropertyNames().contains("prototype"));
    ObjectType proto = ctorFoo.getPrototype();
    assertNotNull(proto);
    assertTrue(ctorFoo.getOwnPropertyNames().contains("prototype"));
    assertNotNull(ctorFoo.getSlot("prototype"));
    assertSame(proto, ctorFoo.getPrototype());
  }

  // Tests setPrototype with null or self instance type
  @Test
  public void testSetPrototype_nullAndInstanceType_returnsFalse() {
    assertFalse(ctorFoo.setPrototype(null));
    assertFalse(ctorFoo.setPrototype((PrototypeObjectType) ctorFoo.getInstanceType()));
  }

  // Tests setting prototype based on another object type
  @Test
  public void testSetPrototypeBasedOn_namedAndAnonymousTypes_updatesPrototypeCorrectly() {
    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ctorFoo.setPrototypeBasedOn(objectType);
    assertNotNull(ctorFoo.getPrototype());
    assertSame(ctorFoo, ctorFoo.getPrototype().getOwnerFunction());

    // Anonymous prototype
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectType);
    ctorFoo.setPrototypeBasedOn(anon);
    assertSame(anon, ctorFoo.getPrototype());
  }

  // Tests subtype hierarchy tracking when setting prototypes
  @Test
  public void testGetSuperClassConstructor_withSubclassing_returnsCorrectSuperClass() {
    ArrowType subArrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType ctorBar = new FunctionType(
        registry, "Bar", null, subArrow, null, null, true, false);

    PrototypeObjectType barProto = new PrototypeObjectType(
        registry, "Bar.prototype", ctorFoo.getInstanceType());
    ctorBar.setPrototype(barProto);

    assertSame(ctorFoo, ctorBar.getSuperClassConstructor());
    List<FunctionType> subTypes = ctorFoo.getSubTypes();
    assertNotNull(subTypes);
    assertTrue(subTypes.contains(ctorBar));
  }

  // Tests implemented interfaces handling and inheritance
  @Test
  public void testImplementedInterfaces_hierarchy_returnsAllImplemented() {
    FunctionType ifaceA = FunctionType.forInterface(registry, "IfaceA", null);
    FunctionType ifaceB = FunctionType.forInterface(registry, "IfaceB", null);

    assertFalse(ctorFoo.hasImplementedInterfaces());
    ctorFoo.setImplementedInterfaces(ImmutableList.<ObjectType>of(ifaceA.getInstanceType()));
    assertTrue(ctorFoo.hasImplementedInterfaces());

    ArrowType subArrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType ctorSub = new FunctionType(
        registry, "Sub", null, subArrow, null, null, true, false);
    ctorSub.setPrototype(new PrototypeObjectType(registry, "Sub.prototype", ctorFoo.getInstanceType()));
    ctorSub.setImplementedInterfaces(ImmutableList.<ObjectType>of(ifaceB.getInstanceType()));

    Iterable<ObjectType> allInterfaces = ctorSub.getAllImplementedInterfaces();
    assertTrue(Iterables.contains(allInterfaces, ifaceA.getInstanceType()));
    assertTrue(Iterables.contains(allInterfaces, ifaceB.getInstanceType()));
  }

  // Tests extended interfaces on an interface type
  @Test
  public void testExtendedInterfaces_interfaceType_extendsCorrectly() {
    FunctionType superIface = FunctionType.forInterface(registry, "SuperIface", null);
    FunctionType subIface = FunctionType.forInterface(registry, "SubIface", null);

    subIface.setExtendedInterfaces(ImmutableList.<ObjectType>of(superIface.getInstanceType()));
    assertEquals(1, subIface.getExtendedInterfacesCount());
    assertTrue(Iterables.contains(subIface.getExtendedInterfaces(), superIface.getInstanceType()));
    assertTrue(Iterables.contains(subIface.getAllExtendedInterfaces(), superIface.getInstanceType()));
  }

  // Tests setExtendedInterfaces throwing on non-interface function
  @Test(expected = UnsupportedOperationException.class)
  public void testSetExtendedInterfaces_ordinaryFunction_throwsException() {
    numberFunction.setExtendedInterfaces(ImmutableList.<ObjectType>of());
  }

  // Tests lazy definition of "call" and "apply" properties
  @Test
  public void testGetPropertyType_callAndApply_lazilyDefined() {
    assertTrue(numberFunction.getPropertyType("call").isFunctionType());
    assertTrue(numberFunction.getPropertyType("apply").isFunctionType());
    assertTrue(numberFunction.hasProperty("call"));
    assertTrue(numberFunction.hasProperty("apply"));
  }

  // Tests isEquivalentTo for ordinary functions and interfaces
  @Test
  public void testIsEquivalentTo_matchingAndNonMatchingTypes_returnsExpected() {
    assertTrue(numberFunction.isEquivalentTo(numberFunction));
    assertFalse(numberFunction.isEquivalentTo(ctorFoo));
    assertFalse(numberFunction.isEquivalentTo(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));

    FunctionType iface1 = FunctionType.forInterface(registry, "SameName", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "SameName", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "DiffName", null);
    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(iface3));
    assertEquals(iface1.hashCode(), iface2.hashCode());
  }

  // Tests toString output formatting
  @Test
  public void testToString_ordinaryAndConstructor_formatsCorrectly() {
    assertEquals("function (number): number", numberFunction.toString());
    assertEquals("function (new:Foo): undefined", ctorFoo.toString());
  }

  // Tests isSubtype relationships
  @Test
  public void testIsSubtype_variousTypes_returnsCorrectSubtyping() {
    assertTrue(numberFunction.isSubtype(numberFunction));
    assertTrue(numberFunction.isSubtype(registry.getNativeType(JSTypeNative.OBJECT_TYPE)));
    assertTrue(numberFunction.isSubtype(registry.getNativeType(JSTypeNative.FUNCTION_TYPE)));

    FunctionType interfaceType = FunctionType.forInterface(registry, "AnInterface", null);
    // Any function is subtype of an interface
    assertTrue(numberFunction.isSubtype(interfaceType));
    // Interface is not subtype of other functions
    assertFalse(interfaceType.isSubtype(numberFunction));
  }

  // Tests getLeastSupertype and getGreatestSubtype for function lattice
  @Test
  public void testLeastSupertypeAndGreatestSubtype_ordinaryFunctions_mergesCorrectly() {
    ArrowType arrow2 = new ArrowType(
        registry, numberFunction.getParametersNode(), registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType stringReturnFn = new FunctionType(
        registry, "strFn", null, arrow2, null, null, false, false);

    JSType leastSuper = numberFunction.getLeastSupertype(stringReturnFn);
    assertTrue(leastSuper.isFunctionType());
    assertEquals(registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE)),
        leastSuper.toMaybeFunctionType().getReturnType());

    JSType greatestSub = numberFunction.getGreatestSubtype(stringReturnFn);
    assertTrue(greatestSub.isFunctionType());
    assertTrue(greatestSub.toMaybeFunctionType().getReturnType().isNoType());
  }

  // Tests getTopMostDefiningType across inheritance chain
  @Test
  public void testGetTopMostDefiningType_hierarchyWithProperty_findsTopDefiningType() {
    ctorFoo.getPrototype().defineProperty("prop", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    ArrowType subArrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType ctorBar = new FunctionType(
        registry, "Bar", null, subArrow, null, null, true, false);
    ctorBar.setPrototype(new PrototypeObjectType(registry, "Bar.prototype", ctorFoo.getInstanceType()));

    ArrowType bazArrow = new ArrowType(
        registry, new Node(Token.LP), registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType ctorBaz = new FunctionType(
        registry, "Baz", null, bazArrow, null, null, true, false);
    ctorBaz.setPrototype(new PrototypeObjectType(registry, "Baz.prototype", ctorBar.getInstanceType()));

    ObjectType topType = ctorBaz.getTopMostDefiningType("prop");
    assertSame(ctorFoo.getInstanceType(), topType);
  }

  // Tests resolveInternal and type resolution
  @Test
  public void testResolveInternal_unresolvedTypes_resolvesCorrectly() {
    ErrorReporter reporter = new SimpleErrorReporter();
    StaticScope<JSType> scope = null;

    JSType resolved = numberFunction.resolve(reporter, scope);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
  }

  // Tests visitor pattern support
  @Test
  public void testVisit_functionTypeVisitor_dispatchesCaseFunctionType() {
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enumElem"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "fn:" + type.getReferenceName(); }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    };

    assertEquals("fn:numFn", numberFunction.visit(visitor));
  }

  // Tests cloneWithNewReturnType and return type inference flag
  @Test
  public void testCloneWithNewReturnType_clonesCorrectly() {
    FunctionType cloned = numberFunction.cloneWithNewReturnType(
        registry.getNativeType(JSTypeNative.STRING_TYPE), true);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), cloned.getReturnType());
    assertTrue(cloned.isReturnTypeInferred());
  }

  // Tests getBindReturnType
  @Test
  public void testGetBindReturnType_bindsArgumentsCorrectly() {
    FunctionType bound0 = numberFunction.getBindReturnType(0);
    assertNotNull(bound0);
    assertTrue(bound0.isFunctionType());

    FunctionType bound1 = numberFunction.getBindReturnType(1);
    assertNotNull(bound1);
    assertEquals(0, bound1.getMinArguments());
  }

  // Tests tryRemoveTypeOfThis
  @Test
  public void testTryRemoveTypeOfThis_transformsTypeOfThis() {
    FunctionType withoutThis = numberFunction.tryRemoveTypeOfThis();
    assertNotNull(withoutThis);
  }

  // Tests source node get and set
  @Test
  public void testSourceNode_getAndSet() {
    Node node = new Node(Token.FUNCTION);
    numberFunction.setSource(node);
    assertSame(node, numberFunction.getSource());
  }

  // Tests canBeCalled and autoboxesTo
  @Test
  public void testCanBeCalledAndAutoboxesTo() {
    assertTrue(numberFunction.canBeCalled());
    assertNull(numberFunction.autoboxesTo());
  }

  // Tests hasEqualParameters
  @Test
  public void testHasEqualParameters_matchingAndNonMatching() {
    assertTrue(numberFunction.hasEqualParameters(numberFunction, false));
    assertTrue(numberFunction.hasEqualParameters(numberFunction, true));
    assertFalse(numberFunction.hasEqualParameters(ctorFoo, false));
  }
}