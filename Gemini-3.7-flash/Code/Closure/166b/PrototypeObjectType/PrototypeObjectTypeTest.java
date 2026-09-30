package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private ObjectType objectPrototype;
  private PrototypeObjectType protoObject;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    protoObject = new PrototypeObjectType(registry, "TestClass", objectPrototype);
  }

  // Tests constructor with default implicit prototype
  @Test
  public void testConstructor_withNullImplicitPrototype_defaultsToObjectPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Anon", null);
    assertNotNull(type.getImplicitPrototype());
    assertEquals(objectPrototype, type.getImplicitPrototype());
    assertEquals("Anon", type.getReferenceName());
    assertTrue(type.hasReferenceName());
  }

  // Tests constructor for native type
  @Test
  public void testConstructor_nativeType_setsNativeFlag() {
    PrototypeObjectType nativeObj = new PrototypeObjectType(registry, "NativeObj", null, true);
    assertTrue(nativeObj.isNativeObjectType());
    assertNull(nativeObj.getImplicitPrototype());
  }

  // Tests property definition, retrieval, and removal
  @Test
  public void testDefineProperty_andRemoveProperty_managesSlotsCorrectly() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Node propNode = Node.newString("propA");

    boolean defined = protoObject.defineProperty("propA", numberType, false, propNode);
    assertTrue(defined);
    assertTrue(protoObject.hasOwnProperty("propA"));
    assertTrue(protoObject.hasProperty("propA"));
    assertEquals(numberType, protoObject.getPropertyType("propA"));
    assertEquals(propNode, protoObject.getPropertyNode("propA"));
    assertTrue(protoObject.isPropertyTypeDeclared("propA"));
    assertFalse(protoObject.isPropertyTypeInferred("propA"));

    // Cannot redefine own declared property
    boolean redefined = protoObject.defineProperty("propA", numberType, true, null);
    assertFalse(redefined);

    // Remove property
    boolean removed = protoObject.removeProperty("propA");
    assertTrue(removed);
    assertFalse(protoObject.hasOwnProperty("propA"));
    assertFalse(protoObject.removeProperty("nonExistent"));
  }

  // Tests property resolution from implicit prototype chain
  @Test
  public void testGetSlot_inheritsFromImplicitPrototype() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    parent.defineProperty("inheritedProp", stringType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    assertFalse(child.hasOwnProperty("inheritedProp"));
    assertTrue(child.hasProperty("inheritedProp"));
    assertEquals(stringType, child.getPropertyType("inheritedProp"));
    assertTrue(child.isPropertyTypeInferred("inheritedProp"));
  }

  // Tests property counting with overrides
  @Test
  public void testGetPropertiesCount_withInheritance_returnsCorrectCount() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    parent.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    parent.defineProperty("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
    child.defineProperty("c", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);

    assertEquals(2, child.getOwnPropertyNames().size());
    assertEquals(parent.getPropertiesCount() + 1, child.getPropertiesCount());
  }

  // Tests property names collection
  @Test
  public void testCollectPropertyNames_collectsAllPropertiesAlongChain() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    parent.defineProperty("p1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("p2", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    Set<String> names = new HashSet<String>();
    child.collectPropertyNames(names);
    assertTrue(names.contains("p1"));
    assertTrue(names.contains("p2"));
  }

  // Tests JSDoc information handling on properties
  @Test
  public void testSetPropertyJSDocInfo_setsAndRetrievesInfo() {
    JSDocInfo info = new JSDocInfo();
    protoObject.setPropertyJSDocInfo("dynamicProp", info);

    assertTrue(protoObject.hasOwnProperty("dynamicProp"));
    assertEquals(info, protoObject.getOwnPropertyJSDocInfo("dynamicProp"));
    assertNull(protoObject.getOwnPropertyJSDocInfo("unknownProp"));
  }

  // Tests context matching methods
  @Test
  public void testContextMatching_matchesExpectedContexts() {
    assertTrue(protoObject.matchesObjectContext());
    assertFalse(protoObject.canBeCalled());

    // Native string unboxing and context
    ObjectType stringObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), stringObj.unboxesTo());
    assertTrue(stringObj.matchesStringContext());
    assertTrue(stringObj.matchesNumberContext());

    // Native number unboxing
    ObjectType numberObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), numberObj.unboxesTo());
    assertTrue(numberObj.matchesNumberContext());

    // Native boolean unboxing
    ObjectType booleanObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), booleanObj.unboxesTo());
  }

  // Tests pretty printing behavior of anonymous object
  @Test
  public void testToStringHelper_prettyPrinting_formatsPropertiesCorrectly() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    anon.setPrettyPrint(true);
    assertTrue(anon.isPrettyPrint());

    anon.defineProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    anon.defineProperty("y", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    String str = anon.toStringHelper(false);
    assertEquals("{x: number, y: string}", str);

    anon.setPrettyPrint(false);
    assertEquals("{...}", anon.toStringHelper(false));
    assertEquals("?", anon.toStringHelper(true));
  }

  // Tests reference name with owner function
  @Test
  public void testGetReferenceName_withOwnerFunction_returnsFunctionNameWithPrototype() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    assertFalse(anon.hasReferenceName());
    assertNull(anon.getReferenceName());

    FunctionType fn = registry.createFunctionType(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    // Test owner function attachment
    anon.setOwnerFunction(fn);
    assertEquals(fn, anon.getOwnerFunction());
    assertNotNull(anon.getReferenceName());
    assertTrue(anon.hasReferenceName());
  }

  // Tests owner function reset state check exception path
  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunction_whenAlreadySet_throwsException() {
    FunctionType fn1 = registry.createFunctionType(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    FunctionType fn2 = registry.createFunctionType(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    protoObject.setOwnerFunction(fn1);
    protoObject.setOwnerFunction(fn2);
  }

  // Tests subtype checking along prototype chain
  @Test
  public void testIsSubtype_checksPrototypeHierarchy() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertTrue(child.isSubtype(parent));
    assertFalse(parent.isSubtype(child));
    assertTrue(child.isSubtype(registry.getNativeType(JSTypeNative.OBJECT_TYPE)));
  }

  // Tests matchRecordTypeConstraint on anonymous prototype object
  @Test
  public void testMatchRecordTypeConstraint_infersPropertiesFromConstraint() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", numberType, null);
    builder.addProperty("b", stringType, null);
    RecordType recordType = builder.build();

    anon.matchConstraint(recordType);

    assertTrue(anon.hasProperty("a"));
    assertTrue(anon.hasProperty("b"));
    assertTrue(anon.isPropertyTypeInferred("a"));
    assertTrue(anon.isPropertyTypeInferred("b"));
  }

  // Tests matchConstraint on named prototype object is ignored
  @Test
  public void testMatchConstraint_onNamedType_doesNotModifyProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("prop", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    RecordType recordType = builder.build();

    protoObject.matchConstraint(recordType);
    assertFalse(protoObject.hasProperty("prop"));
  }

  // Tests resolveInternal resolving properties and implicit prototype
  @Test
  public void testResolveInternal_resolvesCorrectly() {
    protoObject.defineProperty("prop", registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), true, null);
    JSType resolved = protoObject.resolve(new SimpleErrorReporter(), null);
    assertSame(protoObject, resolved);
    assertTrue(protoObject.isResolved());
  }
}