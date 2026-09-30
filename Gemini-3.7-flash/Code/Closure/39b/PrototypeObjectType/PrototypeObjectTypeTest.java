package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  // Tests constructor with null implicit prototype sets default ObjectType
  @Test
  public void testConstructor_nullImplicitPrototype_defaultsToObjectPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "MyClass", null, false);
    assertEquals("MyClass", type.getClassName());
    assertEquals("MyClass", type.getReferenceName());
    assertTrue(type.hasReferenceName());
    assertEquals(objectPrototype, type.getImplicitPrototype());
    assertFalse(type.isNativeObjectType());
  }

  // Tests constructor with explicit implicit prototype and nativeType flag
  @Test
  public void testConstructor_nativeTypeTrue_retainsNativeFlag() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "NativeObj", null, true);
    assertTrue(type.isNativeObjectType());
    assertNull(type.getImplicitPrototype());
  }

  // Tests defineProperty, hasProperty, hasOwnProperty, and getSlot
  @Test
  public void testDefineProperty_validProperty_successfullyDefinedAndRetrieved() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, objectPrototype, false);
    boolean defined = type.defineProperty("foo", numberType, false, null);

    assertTrue(defined);
    assertTrue(type.hasProperty("foo"));
    assertTrue(type.hasOwnProperty("foo"));
    assertNotNull(type.getSlot("foo"));
    assertEquals(numberType, type.getPropertyType("foo"));
    assertFalse(type.isPropertyTypeInferred("foo"));
    assertTrue(type.isPropertyTypeDeclared("foo"));
  }

  // Tests defineProperty fails when property is already declared
  @Test
  public void testDefineProperty_alreadyDeclared_returnsFalse() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, objectPrototype, false);
    type.defineProperty("bar", stringType, false, null);

    boolean definedAgain = type.defineProperty("bar", stringType, false, null);
    assertFalse(definedAgain);
  }

  // Tests removeProperty on existing and non-existing property
  @Test
  public void testRemoveProperty_existingAndNonExistingProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, objectPrototype, false);
    type.defineProperty("temp", booleanType, true, null);

    assertTrue(type.removeProperty("temp"));
    assertFalse(type.hasOwnProperty("temp"));
    assertFalse(type.removeProperty("nonExistent"));
  }

  // Tests inheritance of properties from implicit prototype
  @Test
  public void testGetSlot_inheritedFromPrototype_returnsPrototypeSlot() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, false);
    parent.defineProperty("parentProp", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertNotNull(child.getSlot("parentProp"));
    assertEquals(numberType, child.getPropertyType("parentProp"));
  }

  // Tests getPropertiesCount calculation with local and prototype properties
  @Test
  public void testGetPropertiesCount_withLocalAndInheritedProperties() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, null, null, true);
    parent.defineProperty("p1", numberType, false, null);
    parent.defineProperty("p2", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, null, parent, false);
    child.defineProperty("p2", numberType, false, null); // overridden
    child.defineProperty("c1", stringType, false, null);

    assertEquals(3, child.getPropertiesCount());
  }

  // Tests collectPropertyNames across inheritance chain
  @Test
  public void testCollectPropertyNames_aggregatesNames() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, null, null, true);
    parent.defineProperty("parentKey", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, null, parent, false);
    child.defineProperty("childKey", stringType, false, null);

    Set<String> names = child.getPropertyNames();
    assertTrue(names.contains("parentKey"));
    assertTrue(names.contains("childKey"));
  }

  // Tests matching contexts and unboxing behaviors
  @Test
  public void testContextMatchingAndUnboxing() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, objectPrototype, false);
    assertTrue(type.matchesObjectContext());
    assertFalse(type.canBeCalled());
    assertNull(type.unboxesTo());
    assertNull(type.getConstructor());
  }

  // Tests owner function and reference name derived from owner
  @Test
  public void testOwnerFunction_referenceNameDerivedFromOwner() {
    PrototypeObjectType prototype = new PrototypeObjectType(registry, null, objectPrototype, false);
    assertNull(prototype.getReferenceName());
    assertFalse(prototype.hasReferenceName());

    FunctionType ctor = registry.createConstructorType("Foo", null, null, prototype);
    prototype.setOwnerFunction(ctor);

    assertEquals(ctor, prototype.getOwnerFunction());
    assertEquals("Foo.prototype", prototype.getReferenceName());
    assertTrue(prototype.hasReferenceName());
  }

  // Tests setting owner function twice throws exception
  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunction_alreadySet_throwsIllegalStateException() {
    PrototypeObjectType prototype = new PrototypeObjectType(registry, null, objectPrototype, false);
    FunctionType ctor1 = registry.createConstructorType("First", null, null, prototype);
    FunctionType ctor2 = registry.createConstructorType("Second", null, null, prototype);

    prototype.setOwnerFunction(ctor1);
    prototype.setOwnerFunction(ctor2);
  }

  // Tests toStringHelper with named prototype object
  @Test
  public void testToStringHelper_withReferenceName_returnsReferenceName() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "NamedType", objectPrototype, false);
    assertEquals("NamedType", type.toStringHelper(false));
    assertEquals("NamedType", type.toStringHelper(true));
  }

  // Tests toStringHelper pretty printing under property limit
  @Test
  public void testToStringHelper_prettyPrintFewProperties_printsAllProperties() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    type.defineProperty("a", numberType, false, null);
    type.defineProperty("b", stringType, false, null);
    type.setPrettyPrint(true);

    String result = type.toStringHelper(false);
    assertEquals("{a: number, b: string}", result);
  }

  // Tests toStringHelper pretty printing exceeding max property limit
  @Test
  public void testToStringHelper_prettyPrintManyProperties_truncatesWithEllipsis() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    type.defineProperty("prop1", numberType, false, null);
    type.defineProperty("prop2", numberType, false, null);
    type.defineProperty("prop3", numberType, false, null);
    type.defineProperty("prop4", numberType, false, null);
    type.defineProperty("prop5", numberType, false, null);
    type.setPrettyPrint(true);

    String result = type.toStringHelper(false);
    assertEquals("{prop1: number, prop2: number, prop3: number, prop4: number, ...}", result);
  }

  // Tests toStringHelper when prettyPrint is false
  @Test
  public void testToStringHelper_prettyPrintFalse_returnsSummary() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    type.defineProperty("prop1", numberType, false, null);
    type.setPrettyPrint(false);

    assertEquals("{...}", type.toStringHelper(false));
  }

  // Tests isSubtype with prototype inheritance relationship
  @Test
  public void testIsSubtype_prototypeChain() {
    PrototypeObjectType base = new PrototypeObjectType(registry, "Base", null, false);
    PrototypeObjectType derived = new PrototypeObjectType(registry, "Derived", base, false);

    assertTrue(derived.isSubtype(base));
    assertFalse(base.isSubtype(derived));
    assertTrue(derived.isSubtype(derived));
  }

  // Tests resolveInternal resolves properties and implicit prototype
  @Test
  public void testResolveInternal_resolvesPropertiesAndPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Resolvable", objectPrototype, false);
    type.defineProperty("prop", numberType, false, null);

    JSType resolved = type.resolveInternal(null, null);
    assertSame(type, resolved);
    assertTrue(type.isResolved());
  }

  // Tests three-argument constructor defaulting nativeType to false
  @Test
  public void testConstructor_threeArgs_defaultsToNonNative() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "ThreeArgs", objectPrototype);
    assertFalse(type.isNativeObjectType());
    assertEquals("ThreeArgs", type.getClassName());
    assertEquals(objectPrototype, type.getImplicitPrototype());
  }

  // Tests setImplicitPrototype updates the prototype reference
  @Test
  public void testSetImplicitPrototype_updatesImplicitPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "ChildType", null, false);
    PrototypeObjectType newParent = new PrototypeObjectType(registry, "NewParent", null, false);

    type.setImplicitPrototype(newParent);
    assertEquals(newParent, type.getImplicitPrototype());
  }

  // Tests toStringHelper with empty properties and prettyPrint enabled
  @Test
  public void testToStringHelper_emptyPropertiesPrettyPrint_returnsEmptyBraces() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    type.setPrettyPrint(true);
    assertEquals("{}", type.toStringHelper(false));
  }

  // Tests getPropertyType returns unknown type for nonexistent property
  @Test
  public void testGetPropertyType_nonExistentProperty_returnsUnknownType() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "TestType", null, false);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getPropertyType("nonExistent"));
  }

  // Tests isPropertyTypeInferred and isPropertyTypeDeclared for nonexistent property
  @Test
  public void testPropertyTypeInferredAndDeclared_nonExistentProperty_returnsFalse() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "TestType", null, false);
    assertFalse(type.isPropertyTypeInferred("missing"));
    assertFalse(type.isPropertyTypeDeclared("missing"));
    assertNull(type.getSlot("missing"));
  }

  // Tests defining an inferred property
  @Test
  public void testDefineProperty_inferredProperty_setsInferredFlag() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "InferredObj", objectPrototype, false);
    boolean defined = type.defineProperty("inferredProp", numberType, true, null);

    assertTrue(defined);
    assertTrue(type.isPropertyTypeInferred("inferredProp"));
    assertFalse(type.isPropertyTypeDeclared("inferredProp"));
  }

  // Tests redefining an inferred property with a declared property succeeds
  @Test
  public void testDefineProperty_overrideInferredWithDeclared_succeeds() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "OverrideObj", objectPrototype, false);
    type.defineProperty("prop", numberType, true, null);
    assertTrue(type.isPropertyTypeInferred("prop"));

    boolean redefined = type.defineProperty("prop", stringType, false, null);
    assertTrue(redefined);
    assertFalse(type.isPropertyTypeInferred("prop"));
    assertTrue(type.isPropertyTypeDeclared("prop"));
    assertEquals(stringType, type.getPropertyType("prop"));
  }

  // Tests isPropertyInExterns, getPropertyNode, and doc info defaults
  @Test
  public void testPropertyMetadata_nodeAndExternDefaults() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "MetaObj", objectPrototype, false);
    type.defineProperty("metaProp", numberType, false, null);

    assertFalse(type.isPropertyInExterns("metaProp"));
    assertNull(type.getPropertyNode("metaProp"));
    assertNull(type.getDocInfoForProperty("metaProp"));
    type.setPropertyJSDocInfo("metaProp", null);
    assertNull(type.getDocInfoForProperty("metaProp"));
  }

  // Tests isSubtype against unknown type and non-object types
  @Test
  public void testIsSubtype_againstNativeTypes() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, objectPrototype, false);
    JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);

    assertTrue(type.isSubtype(unknownType));
    assertTrue(type.isSubtype(objectPrototype));
    assertFalse(type.isSubtype(numberType));
    assertFalse(type.isSubtype(stringType));
  }

  // Tests hasOwnProperty does not return true for properties on the prototype chain
  @Test
  public void testHasOwnProperty_doesNotTraversePrototypeChain() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, false);
    parent.defineProperty("p", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    assertFalse(child.hasOwnProperty("p"));
    assertTrue(child.hasProperty("p"));
    assertFalse(child.hasOwnProperty("notDefined"));
    assertFalse(child.hasProperty("notDefined"));
  }
}