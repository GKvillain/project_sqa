package com.google.javascript.rhino.jstype;

import com.google.common.collect.Sets;
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
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  // Tests property definition, retrieval, and existence check
  @Test
  public void testDefineProperty_newProperty_success() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    Node node = new Node(1);
    boolean defined = type.defineProperty("prop1", numberType, false, node);

    assertTrue(defined);
    assertTrue(type.hasOwnProperty("prop1"));
    assertTrue(type.hasProperty("prop1"));
    assertEquals(numberType, type.getPropertyType("prop1"));
    assertEquals(node, type.getPropertyNode("prop1"));
    assertFalse(type.isPropertyTypeInferred("prop1"));
    assertTrue(type.isPropertyTypeDeclared("prop1"));
  }

  // Tests duplicate declaration of already declared property
  @Test
  public void testDefineProperty_duplicateDeclaredProperty_returnsFalse() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    type.defineProperty("prop1", numberType, false, null);

    boolean redefined = type.defineProperty("prop1", stringType, false, null);
    assertFalse(redefined);
    assertEquals(numberType, type.getPropertyType("prop1"));
  }

  // Tests property removal
  @Test
  public void testRemoveProperty_existingAndNonExisting_removesCorrectly() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    type.defineProperty("prop1", numberType, true, null);

    assertTrue(type.removeProperty("prop1"));
    assertFalse(type.hasOwnProperty("prop1"));
    assertFalse(type.removeProperty("prop1"));
  }

  // Tests property resolution through prototype chain
  @Test
  public void testGetSlot_inheritedFromPrototype_findsProperty() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", objectPrototype);
    parent.defineProperty("parentProp", stringType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertNotNull(child.getSlot("parentProp"));
    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertEquals(stringType, child.getPropertyType("parentProp"));
  }

  // Tests properties count calculation with and without prototype properties
  @Test
  public void testGetPropertiesCount_withInheritance_returnsTotalCount() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Proto", null, true);
    proto.defineProperty("a", numberType, true, null);
    proto.defineProperty("b", stringType, true, null);

    PrototypeObjectType obj = new PrototypeObjectType(registry, "Obj", proto);
    obj.defineProperty("b", stringType, true, null); // overrides proto 'b'
    obj.defineProperty("c", booleanType, true, null);

    assertEquals(2, proto.getPropertiesCount());
    assertEquals(3, obj.getPropertiesCount());
  }

  // Tests JSDoc info management on properties
  @Test
  public void testPropertyJSDocInfo_setAndGet_preservesInfo() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    JSDocInfo info = new JSDocInfo();

    type.setPropertyJSDocInfo("dynamicProp", info);
    assertTrue(type.hasOwnProperty("dynamicProp"));
    assertEquals(info, type.getOwnPropertyJSDocInfo("dynamicProp"));
    assertNull(type.getOwnPropertyJSDocInfo("nonExistent"));
  }

  // Tests matching contexts (number, string, object)
  @Test
  public void testMatchesContexts_standardObject_evaluatesCorrectly() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);

    assertTrue(type.matchesObjectContext());
    assertFalse(type.canBeCalled());
    assertFalse(type.matchesNumberContext());
    assertFalse(type.matchesStringContext());
  }

  // Tests unboxesTo method for non-primitive wrapper
  @Test
  public void testUnboxesTo_standardObject_returnsNull() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    assertNull(type.unboxesTo());
  }

  // Tests getReferenceName and hasReferenceName
  @Test
  public void testReferenceName_namedAndAnonymous_returnsExpected() {
    PrototypeObjectType named = new PrototypeObjectType(registry, "NamedClass", objectPrototype);
    assertTrue(named.hasReferenceName());
    assertEquals("NamedClass", named.getReferenceName());

    PrototypeObjectType anonymous = new PrototypeObjectType(registry, null, objectPrototype);
    assertFalse(anonymous.hasReferenceName());
    assertNull(anonymous.getReferenceName());
  }

  // Tests owner function and prototype reference name
  @Test
  public void testOwnerFunction_setAndGet_updatesReferenceName() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, objectPrototype);
    FunctionType ctor = registry.createConstructorType("MyClass", null, null, null);
    proto.setOwnerFunction(ctor);

    assertEquals(ctor, proto.getOwnerFunction());
    assertTrue(proto.hasReferenceName());
    assertEquals("MyClass.prototype", proto.getReferenceName());
  }

  // Tests setting owner function when already set throws exception
  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunction_alreadySet_throwsIllegalStateException() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, objectPrototype);
    FunctionType ctor1 = registry.createConstructorType("C1", null, null, null);
    FunctionType ctor2 = registry.createConstructorType("C2", null, null, null);

    proto.setOwnerFunction(ctor1);
    proto.setOwnerFunction(ctor2);
  }

  // Tests toStringHelper with named type
  @Test
  public void testToStringHelper_namedType_returnsClassName() {
    PrototypeObjectType named = new PrototypeObjectType(registry, "MyType", objectPrototype);
    assertEquals("MyType", named.toStringHelper(false));
    assertEquals("MyType", named.toStringHelper(true));
  }

  // Tests toStringHelper without pretty printing
  @Test
  public void testToStringHelper_anonymousWithoutPrettyPrint_returnsEllipsis() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectPrototype);
    anon.setPrettyPrint(false);
    assertFalse(anon.isPrettyPrint());
    assertEquals("{...}", anon.toStringHelper(false));
  }

  // Tests toStringHelper with pretty printing up to 4 properties
  @Test
  public void testToStringHelper_prettyPrintFewProperties_printsAllProperties() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    anon.defineProperty("p1", numberType, true, null);
    anon.defineProperty("p2", stringType, true, null);
    anon.setPrettyPrint(true);

    String result = anon.toStringHelper(false);
    assertEquals("{p1: number, p2: string}", result);
  }

  // Tests toStringHelper with pretty printing exceeding max limit (Defects4J 39 regression)
  @Test
  public void testToStringHelper_prettyPrintMoreThanMaxProperties_handlesLimit() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    anon.defineProperty("a", numberType, true, null);
    anon.defineProperty("b", numberType, true, null);
    anon.defineProperty("c", numberType, true, null);
    anon.defineProperty("d", numberType, true, null);
    anon.defineProperty("e", numberType, true, null);
    anon.setPrettyPrint(true);

    String forDisplay = anon.toStringHelper(false);
    assertEquals("{a: number, b: number, c: number, d: number, ...}", forDisplay);

    anon.setPrettyPrint(true);
    String forAnnotation = anon.toStringHelper(true);
    // When forAnnotations is true, it should not truncate or should produce consistent representation
    assertNotNull(forAnnotation);
  }

  // Tests recursive pretty printing prevents infinite recursion
  @Test
  public void testToStringHelper_recursiveSelfReference_preventsInfiniteLoop() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    anon.defineProperty("self", anon, true, null);
    anon.setPrettyPrint(true);

    String result = anon.toStringHelper(false);
    assertEquals("{self: {...}}", result);
  }

  // Tests subtype relationship on prototype chain
  @Test
  public void testIsSubtype_prototypeHierarchy_detectsSubtype() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", objectPrototype);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertTrue(child.isSubtype(parent));
    assertTrue(child.isSubtype(objectPrototype));
    assertFalse(parent.isSubtype(child));
  }

  // Tests collectPropertyNames across prototype hierarchy
  @Test
  public void testCollectPropertyNames_withPrototype_collectsAll() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, true);
    parent.defineProperty("p1", numberType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("p2", stringType, true, null);

    Set<String> names = new TreeSet<String>();
    child.collectPropertyNames(names);

    assertEquals(2, names.size());
    assertTrue(names.contains("p1"));
    assertTrue(names.contains("p2"));
  }

  // Tests resolveInternal method resolves prototype and properties
  @Test
  public void testResolveInternal_resolvesTypes() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", objectPrototype);
    type.defineProperty("x", numberType, true, null);

    JSType resolved = type.resolveInternal(new SimpleErrorReporter(), null);
    assertNotNull(resolved);
    assertTrue(resolved.isResolved());
    assertEquals(numberType, type.getPropertyType("x"));
  }
}