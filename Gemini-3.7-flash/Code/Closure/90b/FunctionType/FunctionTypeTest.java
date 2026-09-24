package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class FunctionTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private JSType numberType;
  private JSType stringType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  // Tests ordinary function creation and basic property inspection
  @Test
  public void testOrdinaryFunction_creation_returnsExpectedProperties() {
    FunctionType fn = registry.createFunctionType(numberType, stringType);

    assertTrue(fn.isOrdinaryFunction());
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertTrue(fn.isFunctionType());
    assertTrue(fn.canBeCalled());
    assertFalse(fn.hasInstanceType());
    assertEquals(numberType, fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
    assertEquals(1, fn.getMinArguments());
    assertEquals(1, fn.getMaxArguments());
  }

  // Tests constructor creation, instance type, and prototype chain
  @Test
  public void testConstructor_creation_hasInstanceTypeAndPrototype() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);

    assertTrue(ctor.isConstructor());
    assertFalse(ctor.isOrdinaryFunction());
    assertFalse(ctor.isInterface());
    assertTrue(ctor.hasInstanceType());

    ObjectType instance = ctor.getInstanceType();
    assertNotNull(instance);
    assertEquals(ctor, instance.getConstructor());

    FunctionPrototypeType prototype = ctor.getPrototype();
    assertNotNull(prototype);
    assertEquals(ctor, prototype.getOwnerFunction());
  }

  // Tests getInstanceType throwing exception on ordinary function
  @Test(expected = IllegalStateException.class)
  public void testGetInstanceType_ordinaryFunction_throwsException() {
    FunctionType fn = registry.createFunctionType(numberType);
    fn.getInstanceType();
  }

  // Tests interface function creation and type checking
  @Test
  public void testForInterface_creation_createsInterfaceType() {
    FunctionType iface = FunctionType.forInterface(registry, "IFoo", null);

    assertTrue(iface.isInterface());
    assertFalse(iface.isConstructor());
    assertFalse(iface.isOrdinaryFunction());
    assertTrue(iface.hasInstanceType());
    assertNotNull(iface.getInstanceType());
    assertEquals(0, iface.getMinArguments());
    assertEquals(0, iface.getMaxArguments());
  }

  // Tests varargs argument counting
  @Test
  public void testGetMinMaxArguments_withVarArgs_returnsMaxInteger() {
    Node param1 = Node.newString(Token.NAME, "a");
    param1.setJSType(numberType);
    Node param2 = Node.newString(Token.NAME, "b");
    param2.setJSType(stringType);
    param2.setVarArgs(true);

    Node paramsNode = new Node(Token.LP, param1, param2);
    ArrowType arrowType = new ArrowType(registry, paramsNode, numberType);
    FunctionType fn = new FunctionType(
        registry, "varArgFn", null, arrowType, null, null, false, false);

    assertEquals(1, fn.getMinArguments());
    assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
  }

  // Tests optional arguments min and max calculation
  @Test
  public void testGetMinMaxArguments_withOptionalParams_returnsCorrectBounds() {
    Node param1 = Node.newString(Token.NAME, "req");
    param1.setJSType(numberType);
    Node param2 = Node.newString(Token.NAME, "opt");
    param2.setJSType(stringType);
    param2.setOptionalArg(true);

    Node paramsNode = new Node(Token.LP, param1, param2);
    ArrowType arrowType = new ArrowType(registry, paramsNode, numberType);
    FunctionType fn = new FunctionType(
        registry, "optFn", null, arrowType, null, null, false, false);

    assertEquals(1, fn.getMinArguments());
    assertEquals(2, fn.getMaxArguments());
  }

  // Tests prototype manipulation and subclass registration
  @Test
  public void testSetPrototypeBasedOn_subclassRelationship_registersSubType() {
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);

    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());

    assertEquals(superCtor, subCtor.getSuperClassConstructor());
    List<FunctionType> subTypes = superCtor.getSubTypes();
    assertNotNull(subTypes);
    assertTrue(subTypes.contains(subCtor));
  }

  // Tests implemented interfaces and getAllImplementedInterfaces hierarchy
  @Test
  public void testImplementedInterfaces_inheritance_resolvesAllInterfaces() {
    FunctionType ifaceA = FunctionType.forInterface(registry, "IfaceA", null);
    FunctionType ifaceB = FunctionType.forInterface(registry, "IfaceB", null);

    FunctionType superCtor = registry.createConstructorType("SuperClass", null, null, null);
    superCtor.setImplementedInterfaces(ImmutableList.of(ifaceA.getInstanceType()));

    FunctionType subCtor = registry.createConstructorType("SubClass", null, null, null);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    subCtor.setImplementedInterfaces(ImmutableList.of(ifaceB.getInstanceType()));

    Iterable<ObjectType> allInterfaces = subCtor.getAllImplementedInterfaces();
    Set<ObjectType> interfaceSet = com.google.common.collect.Sets.newHashSet(allInterfaces);

    assertTrue(interfaceSet.contains(ifaceA.getInstanceType()));
    assertTrue(interfaceSet.contains(ifaceB.getInstanceType()));
  }

  // Tests lazy property definitions for call and apply
  @Test
  public void testGetPropertyType_callAndApply_lazilyDefined() {
    FunctionType fn = registry.createFunctionType(numberType, stringType);

    assertTrue(fn.hasProperty("call"));
    assertTrue(fn.hasProperty("apply"));
    assertTrue(fn.hasProperty("prototype"));

    JSType callProp = fn.getPropertyType("call");
    assertNotNull(callProp);
    assertTrue(callProp.isFunctionType());

    JSType applyProp = fn.getPropertyType("apply");
    assertNotNull(applyProp);
    assertTrue(applyProp.isFunctionType());
  }

  // Tests equivalence check for constructors, interfaces, and ordinary functions
  @Test
  public void testIsEquivalentTo_variousKinds_returnsExpected() {
    FunctionType fn1 = registry.createFunctionType(numberType, stringType);
    FunctionType fn2 = registry.createFunctionType(numberType, stringType);
    FunctionType ctor1 = registry.createConstructorType("C1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("C2", null, null, null);
    FunctionType iface1 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface2 = FunctionType.forInterface(registry, "I1", null);
    FunctionType iface3 = FunctionType.forInterface(registry, "I2", null);

    assertTrue(fn1.isEquivalentTo(fn2));
    assertFalse(ctor1.isEquivalentTo(ctor2));
    assertTrue(ctor1.isEquivalentTo(ctor1));
    assertTrue(iface1.isEquivalentTo(iface2));
    assertFalse(iface1.isEquivalentTo(iface3));
    assertFalse(fn1.isEquivalentTo(ctor1));
  }

  // Tests subtyping between ordinary functions, constructors, and interfaces
  @Test
  public void testIsSubtype_functionSubtyping_returnsExpected() {
    FunctionType fnSuper = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.ALL_TYPE), numberType);
    FunctionType fnSub = registry.createFunctionType(
        numberType, registry.getNativeType(JSTypeNative.ALL_TYPE));

    assertTrue(fnSub.isSubtype(fnSuper));

    FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
    assertTrue(fnSuper.isSubtype(iface));
    assertFalse(iface.isSubtype(fnSuper));
  }

  // Tests supremum (least supertype) and infimum (greatest subtype)
  @Test
  public void testLeastSupertypeAndGreatestSubtype_ordinaryFunctions_mergesCorrectly() {
    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);

    JSType sup = fn1.getLeastSupertype(fn2);
    assertNotNull(sup);
    assertTrue(sup.isFunctionType());
    assertEquals(
        registry.createUnionType(numberType, stringType),
        ((FunctionType) sup).getReturnType());

    JSType inf = fn1.getGreatestSubtype(fn2);
    assertNotNull(inf);
    assertTrue(inf.isFunctionType());
    assertEquals(
        registry.getNativeType(JSTypeNative.NO_TYPE),
        ((FunctionType) inf).getReturnType());
  }

  // Tests top-most defining type discovery across inheritance hierarchy
  @Test
  public void testGetTopMostDefiningType_inheritedProperty_returnsTopClass() {
    FunctionType superCtor = registry.createConstructorType("Super", null, null, null);
    superCtor.getPrototype().defineProperty("prop", numberType, false, false);

    FunctionType subCtor = registry.createConstructorType("Sub", null, null, null);
    subCtor.setPrototypeBasedOn(superCtor.getInstanceType());
    subCtor.getPrototype().defineProperty("prop", numberType, false, false);

    JSType definingType = subCtor.getTopMostDefiningType("prop");
    assertEquals(superCtor.getInstanceType(), definingType);
  }

  // Tests hasUnknownSupertype when inheritance chain has unknown type
  @Test
  public void testHasUnknownSupertype_knownAndUnknownChain_detectsCorrectly() {
    FunctionType ctor = registry.createConstructorType("A", null, null, null);
    assertFalse(ctor.hasUnknownSupertype());

    ObjectType unknownObj = (ObjectType) registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ctor.getPrototype().setImplicitPrototype(unknownObj);
    assertTrue(ctor.hasUnknownSupertype());
  }

  // Tests visitor pattern support
  @Test
  public void testVisit_visitorPattern_dispatchesCaseFunctionType() {
    FunctionType fn = registry.createFunctionType(numberType);
    Visitor<String> visitor = new Visitor<String>() {
      @Override
      public String caseFunctionType(FunctionType type) {
        return "visitedFunction";
      }

      @Override
      public String caseObjectType(ObjectType type) {
        return "visitedObject";
      }

      @Override
      public String caseUnknownType() {
        return null;
      }

      @Override
      public String caseNoType() {
        return null;
      }

      @Override
      public String caseNoObjectType() {
        return null;
      }

      @Override
      public String caseAllType() {
        return null;
      }

      @Override
      public String caseBooleanType() {
        return null;
      }

      @Override
      public String caseNullType() {
        return null;
      }

      @Override
      public String caseNumberType() {
        return null;
      }

      @Override
      public String caseStringType() {
        return null;
      }

      @Override
      public String caseVoidType() {
        return null;
      }

      @Override
      public String caseUnionType(UnionType type) {
        return null;
      }

      @Override
      public String caseParameterizedType(ParameterizedType type) {
        return null;
      }

      @Override
      public String caseTemplateType(TemplateType templateType) {
        return null;
      }
    };

    assertEquals("visitedFunction", fn.visit(visitor));
  }

  // Tests toString and toDebugHashCodeString representations
  @Test
  public void testToString_variousFunctionTypes_formatsProperly() {
    FunctionType fn = registry.createFunctionType(numberType, stringType);
    assertEquals("function (string): number", fn.toString());

    FunctionType fnInst = registry.getNativeFunctionType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    assertEquals("Function", fnInst.toString());

    assertNotNull(fn.toDebugHashCodeString());
  }

  // Tests source node get/set methods
  @Test
  public void testSource_getAndSet_worksCorrectly() {
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType fn = new FunctionType(
        registry, "foo", fnNode,
        new ArrowType(registry, new Node(Token.LP), numberType),
        null, null, false, false);

    assertSame(fnNode, fn.getSource());

    Node newFnNode = new Node(Token.FUNCTION);
    fn.setSource(newFnNode);
    assertSame(newFnNode, fn.getSource());
  }

  // Tests type resolution on FunctionType and typeOfThis
  @Test
  public void testResolveInternal_resolvesTypeOfThisAndInterfaces() {
    FunctionType iface = FunctionType.forInterface(registry, "ResolveIface", null);
    FunctionType ctor = registry.createConstructorType("ResolveCtor", null, null, null);
    ctor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    JSType resolved = ctor.resolve(errorReporter, null);
    assertNotNull(resolved);
    assertTrue(resolved.isFunctionType());
  }
}