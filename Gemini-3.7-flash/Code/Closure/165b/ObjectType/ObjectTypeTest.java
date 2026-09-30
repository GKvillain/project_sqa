package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.TernaryValue.FALSE;
import static com.google.javascript.rhino.jstype.TernaryValue.UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObjectTypeTest {

  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
  }

  // Helper concrete implementation of ObjectType for unit testing
  private static class TestObjectType extends ObjectType {
    private final String referenceName;
    private ObjectType implicitPrototype;
    private final Map<String, Property> properties = new HashMap<String, Property>();
    private Iterable<ObjectType> extendedInterfaces = Collections.emptyList();
    private FunctionType ownerFunction = null;

    TestObjectType(JSTypeRegistry registry, String referenceName, ObjectType implicitPrototype) {
      super(registry);
      this.referenceName = referenceName;
      this.implicitPrototype = implicitPrototype;
    }

    void setImplicitPrototype(ObjectType implicitPrototype) {
      this.implicitPrototype = implicitPrototype;
    }

    void setExtendedInterfaces(Iterable<ObjectType> interfaces) {
      this.extendedInterfaces = interfaces;
    }

    @Override
    public String getReferenceName() {
      return referenceName;
    }

    @Override
    public FunctionType getConstructor() {
      return null;
    }

    @Override
    public ObjectType getImplicitPrototype() {
      return implicitPrototype;
    }

    @Override
    public Property getSlot(String name) {
      Property p = properties.get(name);
      if (p != null) {
        return p;
      }
      if (implicitPrototype != null) {
        return implicitPrototype.getSlot(name);
      }
      return null;
    }

    @Override
    public JSType getPropertyType(String propertyName) {
      Property p = getSlot(propertyName);
      return p == null ? registry.getNativeType(JSTypeNative.UNKNOWN_TYPE) : p.getType();
    }

    @Override
    public boolean hasProperty(String propertyName) {
      return properties.containsKey(propertyName)
          || (implicitPrototype != null && implicitPrototype.hasProperty(propertyName));
    }

    @Override
    public boolean hasOwnProperty(String propertyName) {
      return properties.containsKey(propertyName);
    }

    @Override
    public boolean isPropertyTypeInferred(String propertyName) {
      Property p = getSlot(propertyName);
      return p != null && p.isTypeInferred();
    }

    @Override
    public boolean isPropertyTypeDeclared(String propertyName) {
      return !isPropertyTypeInferred(propertyName);
    }

    @Override
    public int getPropertiesCount() {
      return properties.size();
    }

    @Override
    void collectPropertyNames(Set<String> props) {
      props.addAll(properties.keySet());
      if (implicitPrototype != null) {
        implicitPrototype.collectPropertyNames(props);
      }
    }

    @Override
    boolean defineProperty(String propertyName, JSType type, boolean inferred, Node propertyNode) {
      properties.put(propertyName, new Property(propertyName, type, inferred, propertyNode));
      return true;
    }

    @Override
    public Iterable<ObjectType> getCtorExtendedInterfaces() {
      return extendedInterfaces;
    }

    @Override
    public FunctionType getOwnerFunction() {
      return ownerFunction;
    }

    @Override
    void setOwnerFunction(FunctionType type) {
      this.ownerFunction = type;
    }
  }

  // Tests default methods and property getters on ObjectType
  @Test
  public void testDefaultGetters_returnsExpectedDefaults() {
    TestObjectType obj = new TestObjectType(registry, "Foo", null);
    assertNull(obj.getRootNode());
    assertNull(obj.getTypeOfThis());
    assertNull(obj.getParameterType());
    assertNull(obj.getIndexType());
    assertNull(obj.getPropertyNode("prop"));
    assertNull(obj.getOwnPropertyJSDocInfo("prop"));
    assertFalse(obj.removeProperty("prop"));
    assertFalse(obj.isPropertyInExterns("prop"));
    assertFalse(obj.isNativeObjectType());
    assertFalse(obj.hasReferenceName());
    assertEquals(BooleanLiteralSet.TRUE, obj.getPossibleToBooleanOutcomes());
    assertTrue(obj.isObject());
    assertEquals(0, obj.getOwnPropertyNames().size());
  }

  // Tests parent scope delegation to implicit prototype
  @Test
  public void testGetParentScope_withPrototype_returnsImplicitPrototype() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    TestObjectType child = new TestObjectType(registry, "Child", parent);
    assertSame(parent, child.getParentScope());
    assertNull(parent.getParentScope());
  }

  // Tests own slot retrieval when property exists on current object vs prototype
  @Test
  public void testGetOwnSlot_existingAndInheritedProperties() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    parent.defineProperty("parentProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    TestObjectType child = new TestObjectType(registry, "Child", parent);
    child.defineProperty("childProp", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    assertNotNull(child.getOwnSlot("childProp"));
    assertEquals("childProp", child.getOwnSlot("childProp").getName());
    assertNull(child.getOwnSlot("parentProp"));
    assertNotNull(child.getSlot("parentProp"));
  }

  // Tests reference name normalization and delegate suffix creation
  @Test
  public void testGetNormalizedReferenceName_withAndWithoutSuffix() {
    TestObjectType plain = new TestObjectType(registry, "MyClass", null);
    assertEquals("MyClass", plain.getNormalizedReferenceName());
    assertEquals("MyClass", plain.getDisplayName());

    TestObjectType suffixed = new TestObjectType(registry, "MyClass(suffix)", null);
    assertEquals("MyClass", suffixed.getNormalizedReferenceName());
    assertEquals("MyClass", suffixed.getDisplayName());

    assertEquals("(delegate)", ObjectType.createDelegateSuffix("delegate"));

    TestObjectType anon = new TestObjectType(registry, null, null);
    assertNull(anon.getNormalizedReferenceName());
    assertNull(anon.getDisplayName());
  }

  // Tests JSDocInfo propagation from prototype chain
  @Test
  public void testGetJSDocInfo_fallbackToPrototype() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    JSDocInfo parentInfo = new JSDocInfo();
    parent.setJSDocInfo(parentInfo);

    TestObjectType child = new TestObjectType(registry, "Child", parent);
    assertSame(parentInfo, child.getJSDocInfo());

    JSDocInfo childInfo = new JSDocInfo();
    child.setJSDocInfo(childInfo);
    assertSame(childInfo, child.getJSDocInfo());
  }

  // Tests cycle detection when implicit prototype has no cycle
  @Test
  public void testDetectImplicitPrototypeCycle_noCycle_returnsFalse() {
    TestObjectType grandparent = new TestObjectType(registry, "Grandparent", null);
    TestObjectType parent = new TestObjectType(registry, "Parent", grandparent);
    TestObjectType child = new TestObjectType(registry, "Child", parent);

    assertFalse(child.detectImplicitPrototypeCycle());
    assertFalse(grandparent.detectImplicitPrototypeCycle());
  }

  // Tests cycle detection when an implicit prototype loop exists
  @Test
  public void testDetectImplicitPrototypeCycle_cyclePresent_returnsTrue() {
    TestObjectType a = new TestObjectType(registry, "A", null);
    TestObjectType b = new TestObjectType(registry, "B", a);
    a.setImplicitPrototype(b);

    assertTrue(a.detectImplicitPrototypeCycle());
  }

  // Tests equality test against other JSTypes
  @Test
  public void testTestForEquality_objectAndNonObject() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);

    assertEquals(UNKNOWN, obj.testForEquality(numberType));
    assertEquals(FALSE, obj.testForEquality(nullType));
  }

  // Tests defining declared property
  @Test
  public void testDefineDeclaredProperty_validProperty_registeredSuccessfully() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Node node = new Node(0);

    boolean defined = obj.defineDeclaredProperty("age", numType, node);
    assertTrue(defined);
    assertTrue(obj.hasProperty("age"));
    assertTrue(obj.hasOwnDeclaredProperty("age"));
    assertEquals(numType, obj.findPropertyType("age"));
  }

  // Tests defining inferred property with least supertype merging
  @Test
  public void testDefineInferredProperty_mergesTypes() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    obj.defineInferredProperty("data", numType, null);
    assertEquals(numType, obj.getPropertyType("data"));

    obj.defineInferredProperty("data", strType, null);
    JSType expectedUnion = registry.createUnionType(numType, strType);
    assertEquals(expectedUnion, obj.getPropertyType("data"));
  }

  // Tests findPropertyType when property does not exist
  @Test
  public void testFindPropertyType_nonExistentProperty_returnsNull() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    assertNull(obj.findPropertyType("nonExistent"));
  }

  // Tests getPropertyNames collecting names from prototype chain
  @Test
  public void testGetPropertyNames_collectsFromChain() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    parent.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    TestObjectType child = new TestObjectType(registry, "Child", parent);
    child.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    Set<String> names = child.getPropertyNames();
    assertEquals(Sets.newHashSet("a", "b"), names);
  }

  // Tests isImplicitPrototype method for self and ancestor prototype
  @Test
  public void testIsImplicitPrototype_matchesAncestor() {
    TestObjectType grandparent = new TestObjectType(registry, "Grandparent", null);
    TestObjectType parent = new TestObjectType(registry, "Parent", grandparent);
    TestObjectType child = new TestObjectType(registry, "Child", parent);
    TestObjectType other = new TestObjectType(registry, "Other", null);

    assertTrue(child.isImplicitPrototype(child));
    assertTrue(child.isImplicitPrototype(parent));
    assertTrue(child.isImplicitPrototype(grandparent));
    assertFalse(child.isImplicitPrototype(other));
  }

  // Tests isUnknownType and caching behaviors
  @Test
  public void testIsUnknownType_andCachedValues() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    assertFalse(obj.hasCachedValues());

    assertFalse(obj.isUnknownType());
    assertTrue(obj.hasCachedValues());

    obj.clearCachedValues();
    assertFalse(obj.hasCachedValues());
  }

  // Tests isUnknownType with extended interfaces
  @Test
  public void testIsUnknownType_withExtendedInterface() {
    TestObjectType iface = new TestObjectType(registry, "Interface", null);
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    obj.setExtendedInterfaces(ImmutableList.<ObjectType>of(iface));

    assertFalse(obj.isUnknownType());
  }

  // Tests ObjectType.cast static method with null and non-null types
  @Test
  public void testCast_nullAndObjectType() {
    assertNull(ObjectType.cast(null));

    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    assertSame(obj, ObjectType.cast(obj));

    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull(ObjectType.cast(numType));
  }

  // Tests isFunctionPrototypeType and owner function assignment
  @Test
  public void testIsFunctionPrototypeType_andOwnerFunction() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    assertFalse(obj.isFunctionPrototypeType());

    FunctionType fnType = registry.createFunctionType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    obj.setOwnerFunction(fnType);
    assertTrue(obj.isFunctionPrototypeType());
    assertSame(fnType, obj.getOwnerFunction());
  }

  // Tests ObjectType.Property inner class methods
  @Test
  public void testPropertyClass_methodsAndFields() {
    Node node = new Node(0);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    ObjectType.Property prop = new ObjectType.Property("myProp", numType, true, node);

    assertEquals("myProp", prop.getName());
    assertEquals(numType, prop.getType());
    assertTrue(prop.isTypeInferred());
    assertSame(node, prop.getNode());
    assertSame(prop, prop.getSymbol());
    assertSame(prop, prop.getDeclaration());
    assertNull(prop.getSourceFile());
    assertFalse(prop.isFromExterns());

    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    prop.setType(strType);
    assertEquals(strType, prop.getType());

    JSDocInfo info = new JSDocInfo();
    prop.setJSDocInfo(info);
    assertSame(info, prop.getJSDocInfo());

    Node newNode = new Node(1);
    prop.setNode(newNode);
    assertSame(newNode, prop.getNode());
  }

  // Tests ObjectType.Property with null node
  @Test
  public void testPropertyClass_withNullNode() {
    ObjectType.Property prop = new ObjectType.Property("prop", null, false, null);
    assertNull(prop.getNode());
    assertNull(prop.getSourceFile());
    assertNull(prop.getDeclaration());
    assertFalse(prop.isFromExterns());
  }

  // Tests Visitor caseObjectType dispatch
  @Test
  public void testVisit_dispatchesToCaseObjectType() {
    TestObjectType obj = new TestObjectType(registry, "Obj", null);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseObjectType(ObjectType type) { return "ObjectType"; }
      @Override public String caseNoType() { return null; }
      @Override public String caseEnumElementType(EnumElementType type) { return null; }
      @Override public String caseAllType() { return null; }
      @Override public String caseBooleanType() { return null; }
      @Override public String caseNoObjectType() { return null; }
      @Override public String caseFunctionType(FunctionType type) { return null; }
      @Override public String caseNullType() { return null; }
      @Override public String caseNumberType() { return null; }
      @Override public String caseStringType() { return null; }
      @Override public String caseVoidType() { return null; }
      @Override public String caseUnionType(UnionType type) { return null; }
      @Override public String caseUnknownType() { return null; }
    };
    assertEquals("ObjectType", obj.visit(visitor));
  }
}