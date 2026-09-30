package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableMap;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // Tests constructor and reference name for named and anonymous prototypes
  @Test
  public void testConstructor_namedAndAnonymous_referenceName() {
    PrototypeObjectType namedType = new PrototypeObjectType(registry, "MyClass", objectType);
    assertTrue(namedType.hasReferenceName());
    assertEquals("MyClass", namedType.getReferenceName());
    assertNull(namedType.getConstructor());

    PrototypeObjectType anonType = new PrototypeObjectType(registry, null, objectType);
    assertFalse(anonType.hasReferenceName());
    assertNull(anonType.getReferenceName());
  }

  // Tests owner function and prototype reference name
  @Test
  public void testOwnerFunction_referenceName() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, objectType);
    FunctionType fnType = registry.createConstructorType("Foo", null, null, proto);
    proto.setOwnerFunction(fnType);

    assertEquals(fnType, proto.getOwnerFunction());
    assertTrue(proto.hasReferenceName());
    assertEquals("Foo.prototype", proto.getReferenceName());
  }

  // Tests setOwnerFunction exception when already set to a different function
  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunction_alreadySet_throwsException() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, objectType);
    FunctionType fn1 = registry.createConstructorType("Foo1", null, null, proto);
    FunctionType fn2 = registry.createConstructorType("Foo2", null, null, proto);
    proto.setOwnerFunction(fn1);
    proto.setOwnerFunction(fn2);
  }

  // Tests defining, querying, and removing properties on PrototypeObjectType
  @Test
  public void testPropertyOperations_defineQueryRemove() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Foo", objectType);
    Node node = new Node(0);

    boolean defined = proto.defineProperty("age", numberType, false, node);
    assertTrue(defined);
    assertTrue(proto.hasProperty("age"));
    assertTrue(proto.hasOwnProperty("age"));
    assertTrue(proto.isPropertyTypeDeclared("age"));
    assertFalse(proto.isPropertyTypeInferred("age"));
    assertEquals(numberType, proto.getPropertyType("age"));
    assertEquals(node, proto.getPropertyNode("age"));

    boolean redefined = proto.defineProperty("age", stringType, true, null);
    assertFalse(redefined);

    boolean removed = proto.removeProperty("age");
    assertTrue(removed);
    assertFalse(proto.hasOwnProperty("age"));

    boolean removedAgain = proto.removeProperty("age");
    assertFalse(removedAgain);
  }

  // Tests property inheritance across implicit prototype chain
  @Test
  public void testPropertyInheritance_implicitPrototypeChain() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", objectType);
    parent.defineProperty("parentProp", stringType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("childProp", numberType, false, null);

    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasProperty("childProp"));
    assertTrue(child.hasOwnProperty("childProp"));
    assertFalse(child.hasProperty("nonExistent"));

    assertEquals(stringType, child.getPropertyType("parentProp"));
    assertEquals(numberType, child.getPropertyType("childProp"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), child.getPropertyType("nonExistent"));

    Set<String> ownProps = child.getOwnPropertyNames();
    assertEquals(1, ownProps.size());
    assertTrue(ownProps.contains("childProp"));
  }

  // Tests property count with and without prototype properties
  @Test
  public void testGetPropertiesCount_withInheritedProperties() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, true);
    parent.defineProperty("p1", numberType, false, null);
    parent.defineProperty("p2", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("p2", stringType, false, null);
    child.defineProperty("c1", booleanType, false, null);

    assertEquals(2, parent.getPropertiesCount());
    assertEquals(3, child.getPropertiesCount());
  }

  // Tests JSDoc info handling for properties
  @Test
  public void testPropertyJSDocInfo_getAndSet() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Foo", objectType);
    JSDocInfo info = new JSDocInfo();

    proto.defineProperty("prop", numberType, false, null);
    assertNull(proto.getOwnPropertyJSDocInfo("prop"));

    proto.setPropertyJSDocInfo("prop", info);
    assertEquals(info, proto.getOwnPropertyJSDocInfo("prop"));

    proto.setPropertyJSDocInfo("inferredProp", info);
    assertTrue(proto.hasOwnProperty("inferredProp"));
    assertTrue(proto.isPropertyTypeInferred("inferredProp"));
    assertEquals(info, proto.getOwnPropertyJSDocInfo("inferredProp"));
  }

  // Tests matchesNumberContext, matchesStringContext, and unboxesTo
  @Test
  public void testContextMatching_andUnboxing() {
    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);

    assertTrue(numObj.matchesNumberContext());
    assertTrue(strObj.matchesStringContext());
    assertTrue(boolObj.matchesNumberContext());
    assertTrue(boolObj.matchesStringContext());

    assertEquals(numberType, numObj.unboxesTo());
    assertEquals(stringType, strObj.unboxesTo());
    assertEquals(booleanType, boolObj.unboxesTo());

    PrototypeObjectType custom = new PrototypeObjectType(registry, "Custom", objectType);
    assertTrue(custom.matchesObjectContext());
    assertFalse(custom.canBeCalled());
    assertNull(custom.unboxesTo());
  }

  // Tests pretty printing toStringHelper behavior
  @Test
  public void testToStringHelper_prettyPrint() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    anon.defineProperty("z", numberType, false, null);
    anon.defineProperty("a", stringType, false, null);

    assertEquals("{...}", anon.toStringHelper(false));
    assertEquals("?", anon.toStringHelper(true));

    anon.setPrettyPrint(true);
    assertTrue(anon.isPrettyPrint());
    String pretty = anon.toStringHelper(false);
    assertTrue(pretty.contains("a: string"));
    assertTrue(pretty.contains("z: number"));
  }

  // Tests isSubtype with record types and prototype hierarchy
  @Test
  public void testIsSubtype_recordTypeAndHierarchy() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", objectType);
    parent.defineProperty("a", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("b", stringType, false, null);

    assertTrue(child.isSubtype(parent));
    assertFalse(parent.isSubtype(child));

    ObjectType recordType = registry.createRecordType(
        ImmutableMap.<String, JSType>of("a", numberType));
    assertTrue(child.isSubtype(recordType));

    ObjectType missingRecordType = registry.createRecordType(
        ImmutableMap.<String, JSType>of("unknownProp", numberType));
    assertFalse(child.isSubtype(missingRecordType));
  }

  // Tests matchConstraint against record type to infer properties
  @Test
  public void testMatchConstraint_recordType_infersProperties() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectType);
    ObjectType constraint = registry.createRecordType(
        ImmutableMap.<String, JSType>of("x", numberType, "y", stringType));

    anon.matchConstraint(constraint);

    assertTrue(anon.hasProperty("x"));
    assertTrue(anon.hasProperty("y"));
    assertTrue(anon.isPropertyTypeInferred("x"));
    assertTrue(anon.isPropertyTypeInferred("y"));
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE).getLeastSupertype(numberType),
        anon.getPropertyType("x"));
  }

  // Tests matchConstraint on existing declared property does not overwrite
  @Test
  public void testMatchConstraint_existingDeclaredProperty_notOverwritten() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectType);
    anon.defineProperty("x", booleanType, false, null);

    ObjectType constraint = registry.createRecordType(
        ImmutableMap.<String, JSType>of("x", numberType));

    anon.matchConstraint(constraint);

    assertEquals(booleanType, anon.getPropertyType("x"));
    assertFalse(anon.isPropertyTypeInferred("x"));
  }

  // Tests resolveInternal to resolve property types
  @Test
  public void testResolveInternal_resolvesProperties() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Foo", objectType);
    proto.defineProperty("prop", numberType, false, null);

    ErrorReporter reporter = null;
    JSType resolved = proto.resolveInternal(reporter, null);

    assertSame(proto, resolved);
    assertEquals(numberType, proto.getPropertyType("prop"));
  }

  // Tests nativeObjectType flag
  @Test
  public void testIsNativeObjectType_trueAndFalse() {
    PrototypeObjectType nativeProto = new PrototypeObjectType(registry, "Object", null, true);
    assertTrue(nativeProto.isNativeObjectType());

    PrototypeObjectType nonNativeProto = new PrototypeObjectType(registry, "Custom", objectType, false);
    assertFalse(nonNativeProto.isNativeObjectType());
  }

  // Tests implicit prototype fallback manipulation and checkState
  @Test
  public void testSetImplicitPrototype_updatesFallback() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Custom", null, true);
    assertNull(proto.getImplicitPrototype());

    proto.setImplicitPrototype(objectType);
    assertEquals(objectType, proto.getImplicitPrototype());
  }
}