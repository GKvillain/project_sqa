package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter reporter;
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;

  @Before
  public void setUp() {
    reporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(reporter);
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  // Tests constructor with default implicit prototype and basic property definition
  @Test
  public void testDefineProperty_newProperty_definesSuccessfully() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "CustomClass", null);
    assertEquals("CustomClass", type.getReferenceName());
    assertTrue(type.hasReferenceName());
    assertEquals(objectPrototype, type.getImplicitPrototype());

    Node propNode = Node.newString("x");
    boolean added = type.defineProperty("x", numberType, false, propNode);
    assertTrue(added);
    assertTrue(type.hasOwnProperty("x"));
    assertTrue(type.hasProperty("x"));
    assertTrue(type.isPropertyTypeDeclared("x"));
    assertFalse(type.isPropertyTypeInferred("x"));
    assertEquals(numberType, type.getPropertyType("x"));
    assertEquals(propNode, type.getPropertyNode("x"));
  }

  // Tests duplicate property definition when already declared
  @Test
  public void testDefineProperty_alreadyDeclared_returnsFalse() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "CustomClass", null);
    type.defineProperty("x", numberType, false, null);

    boolean addedAgain = type.defineProperty("x", stringType, true, null);
    assertFalse(addedAgain);
    assertEquals(numberType, type.getPropertyType("x"));
  }

  // Tests removeProperty on existing and non-existing property
  @Test
  public void testRemoveProperty_existingAndNonExisting() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    type.defineProperty("x", numberType, true, null);

    assertTrue(type.removeProperty("x"));
    assertFalse(type.hasOwnProperty("x"));
    assertFalse(type.removeProperty("x"));
    assertFalse(type.removeProperty("nonExistent"));
  }

  // Tests prototype inheritance for getSlot, hasProperty, and getPropertiesCount
  @Test
  public void testPropertyInheritance_fromImplicitPrototype() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    parent.defineProperty("parentProp", stringType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("childProp", numberType, false, null);

    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasProperty("childProp"));
    assertTrue(child.hasOwnProperty("childProp"));
    assertEquals(stringType, child.getPropertyType("parentProp"));

    Set<String> collected = new TreeSet<String>();
    child.collectPropertyNames(collected);
    assertTrue(collected.contains("childProp"));
    assertTrue(collected.contains("parentProp"));
  }

  // Tests getSlot and property resolution with null implicit prototype
  @Test
  public void testGetSlot_nullImplicitPrototype_returnsNullForUnknownProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    assertNull(type.getImplicitPrototype());
    assertNull(type.getSlot("foo"));
    assertEquals(0, type.getPropertiesCount());
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getPropertyType("foo"));
    assertFalse(type.isPropertyTypeDeclared("foo"));
    assertFalse(type.isPropertyTypeInferred("foo"));
    assertFalse(type.isPropertyInExterns("foo"));
  }

  // Tests JSDocInfo association with properties and inferred auto-creation
  @Test
  public void testPropertyJSDocInfo_getAndSet() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "TestType", null);
    JSDocInfo info = new JSDocInfo();

    type.setPropertyJSDocInfo("autoProp", info);
    assertTrue(type.hasOwnProperty("autoProp"));
    assertEquals(info, type.getOwnPropertyJSDocInfo("autoProp"));

    assertNull(type.getOwnPropertyJSDocInfo("unknown"));
  }

  // Tests context matching for object, string, and number contexts
  @Test
  public void testMatchesContext_variousContexts() {
    PrototypeObjectType objType = new PrototypeObjectType(registry, "PlainObj", null);
    assertTrue(objType.matchesObjectContext());
    assertFalse(objType.canBeCalled());

    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(numObj.matchesNumberContext());
    assertTrue(numObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), numObj.unboxesTo());

    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertTrue(strObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), strObj.unboxesTo());

    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertTrue(boolObj.matchesNumberContext());
    assertTrue(boolObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), boolObj.unboxesTo());
  }

  // Tests toStringHelper and pretty printing
  @Test
  public void testToString_namedAndPrettyPrintAnonymous() {
    PrototypeObjectType namedType = new PrototypeObjectType(registry, "MyClass", null);
    assertEquals("MyClass", namedType.toString());

    PrototypeObjectType anonType = new PrototypeObjectType(registry, null, null);
    assertEquals("{...}", anonType.toString());
    assertEquals("?", anonType.toStringHelper(true));

    anonType.setPrettyPrint(true);
    assertTrue(anonType.isPrettyPrint());
    anonType.defineProperty("b", numberType, false, null);
    anonType.defineProperty("a", stringType, false, null);

    String prettyStr = anonType.toString();
    assertTrue(prettyStr.startsWith("{"));
    assertTrue(prettyStr.contains("a: string"));
    assertTrue(prettyStr.contains("b: number"));
    assertTrue(prettyStr.endsWith("}"));
  }

  // Tests reference name when prototype is owned by a constructor function
  @Test
  public void testOwnerFunction_referenceName() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    assertNull(proto.getReferenceName());
    assertFalse(proto.hasReferenceName());

    FunctionType ctor = registry.createConstructorType("FooBar", null, null, null);
    proto.setOwnerFunction(ctor);
    assertEquals(ctor, proto.getOwnerFunction());
    assertEquals("FooBar.prototype", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());
  }

  // Tests setOwnerFunction throws exception when set twice
  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunction_alreadySet_throwsException() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    FunctionType ctor1 = registry.createConstructorType("Foo", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("Bar", null, null, null);
    proto.setOwnerFunction(ctor1);
    proto.setOwnerFunction(ctor2);
  }

  // Tests isSubtype with record type and prototype chain
  @Test
  public void testIsSubtype_recordTypeAndPrototypes() {
    PrototypeObjectType typeA = new PrototypeObjectType(registry, "A", null);
    typeA.defineProperty("x", numberType, false, null);

    PrototypeObjectType typeB = new PrototypeObjectType(registry, "B", typeA);
    typeB.defineProperty("y", stringType, false, null);

    assertTrue(typeB.isSubtype(typeA));
    assertFalse(typeA.isSubtype(typeB));

    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("x", numberType, null);
    JSType recordType = rtb.build();

    assertTrue(typeA.isSubtype(recordType));
    assertTrue(typeB.isSubtype(recordType));
  }

  // Tests matchConstraint with RecordType
  @Test
  public void testMatchConstraint_recordType_infersProperties() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);

    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("age", numberType, null);
    rtb.addProperty("name", stringType, null);
    ObjectType constraintRecord = (ObjectType) rtb.build();

    anon.matchConstraint(constraintRecord);

    assertTrue(anon.hasProperty("age"));
    assertTrue(anon.hasProperty("name"));
    assertTrue(anon.isPropertyTypeInferred("age"));
    assertTrue(anon.isPropertyTypeInferred("name"));
  }

  // Tests matchConstraint on type with existing declared property does not overwrite
  @Test
  public void testMatchConstraint_existingDeclaredProperty_notOverwritten() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    anon.defineProperty("prop", stringType, false, null);

    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("prop", numberType, null);
    ObjectType constraintRecord = (ObjectType) rtb.build();

    anon.matchConstraint(constraintRecord);
    assertEquals(stringType, anon.getPropertyType("prop"));
    assertTrue(anon.isPropertyTypeDeclared("prop"));
  }

  // Tests resolveInternal resolves properties and implicit prototype
  @Test
  public void testResolveInternal_resolvesTypes() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Resolvable", null);
    type.defineProperty("x", numberType, false, null);

    JSType resolved = type.resolve(reporter, null);
    assertSame(type, resolved);
    assertTrue(type.isResolved());
    assertEquals(numberType, type.getPropertyType("x"));
  }
}