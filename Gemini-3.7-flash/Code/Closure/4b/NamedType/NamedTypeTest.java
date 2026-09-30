package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NamedTypeTest {

  private JSTypeRegistry registry;
  private TestErrorReporter errorReporter;
  private TestScope scope;

  private static class TestErrorReporter extends SimpleErrorReporter {
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    public List<String> getWarnings() {
      return warnings;
    }

    public List<String> getErrors() {
      return errors;
    }
  }

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    scope = new TestScope();
  }

  // Tests constructor with null reference throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullReference_throwsException() {
    new NamedType(registry, null, "source.js", 1, 0);
  }

  // Tests basic getters and property checking methods
  @Test
  public void testBasicGetters_validInput_returnsExpectedValues() {
    NamedType namedType = new NamedType(registry, "MyType", "source.js", 10, 5);

    assertEquals("MyType", namedType.getReferenceName());
    assertEquals("MyType", namedType.toStringHelper(false));
    assertEquals("MyType", namedType.toStringHelper(true));
    assertTrue(namedType.hasReferenceName());
    assertTrue(namedType.isNamedType());
    assertTrue(namedType.isNominalType());
    assertEquals("MyType".hashCode(), namedType.hashCode());
  }

  // Tests type resolution via registry lookup
  @Test
  public void testResolveInternal_viaRegistry_resolvesSuccessfully() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    registry.declareType("RegisteredType", stringType);

    NamedType namedType = new NamedType(registry, "RegisteredType", "source.js", 1, 0);
    assertFalse(namedType.isResolved());

    JSType resolved = namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(stringType, namedType.getReferencedType());
    assertNotNull(resolved);
  }

  // Tests property continuations queueing and committing upon resolution
  @Test
  public void testDefineProperty_unresolvedType_commitsAfterResolution() {
    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    registry.declareType("TargetObj", objectType);

    NamedType namedType = new NamedType(registry, "TargetObj", "source.js", 1, 0);
    Node propNode = new Node(0);
    boolean propDefined = namedType.defineProperty("customProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, propNode);
    assertTrue(propDefined);

    namedType.resolveInternal(errorReporter, scope);
    assertTrue(namedType.isResolved());
  }

  // Tests defineProperty when already resolved delegates directly to super
  @Test
  public void testDefineProperty_alreadyResolvedType_definesPropertyDirectly() {
    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    registry.declareType("ResolvedObj", objectType);

    NamedType namedType = new NamedType(registry, "ResolvedObj", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);
    assertTrue(namedType.isResolved());

    boolean propDefined = namedType.defineProperty("directProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, new Node(0));
    assertTrue(propDefined);
  }

  // Tests resolution via properties resolving constructor function to instance type
  @Test
  public void testResolveInternal_viaPropertiesConstructorFunction_resolvesInstanceType() {
    FunctionType ctorType = registry.createConstructorType("CustomClass", null, null, null);
    scope.addSlot("CustomClass", ctorType);

    NamedType namedType = new NamedType(registry, "CustomClass", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(ctorType.getInstanceType(), namedType.getReferencedType());
  }

  // Tests resolution via properties resolving EnumType to elements type
  @Test
  public void testResolveInternal_viaPropertiesEnumType_resolvesElementsType() {
    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    scope.addSlot("MyEnum", enumType);

    NamedType namedType = new NamedType(registry, "MyEnum", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(enumType.getElementsType(), namedType.getReferencedType());
  }

  // Tests resolution via nested property chains (e.g. a.b.c)
  @Test
  public void testResolveInternal_nestedPropertyChain_resolvesCorrectly() {
    ObjectType parentObj = registry.createAnonymousObjectType();
    FunctionType nestedCtor = registry.createConstructorType("NestedCtor", null, null, null);
    parentObj.defineProperty("SubClass", nestedCtor, false, null);
    scope.addSlot("Namespace", parentObj);

    NamedType namedType = new NamedType(registry, "Namespace.SubClass", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(nestedCtor.getInstanceType(), namedType.getReferencedType());
  }

  // Tests resolution with empty component or invalid property path
  @Test
  public void testLookupViaProperties_invalidPropertyName_returnsNullAndHandlesUnresolved() {
    NamedType emptyStart = new NamedType(registry, ".Invalid", "source.js", 1, 0);
    emptyStart.resolveInternal(errorReporter, scope);

    ObjectType parentObj = registry.createAnonymousObjectType();
    scope.addSlot("Root", parentObj);
    NamedType emptySegment = new NamedType(registry, "Root..Child", "source.js", 1, 0);
    emptySegment.resolveInternal(errorReporter, scope);

    assertNotNull(emptyStart.getReferencedType());
    assertNotNull(emptySegment.getReferencedType());
  }

  // Tests forward declared type resolves to NO_RESOLVED_TYPE without warning
  @Test
  public void testResolveInternal_forwardDeclaredType_resolvesToNoResolvedType() {
    registry.forwardDeclareType("ForwardDeclaredType");

    NamedType namedType = new NamedType(registry, "ForwardDeclaredType", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(registry.getNativeObjectType(JSTypeNative.NO_RESOLVED_TYPE), namedType.getReferencedType());
  }

  // Tests unresolved type not forward declared records a warning
  @Test
  public void testResolveInternal_unknownTypeNotForwardDeclared_emitsWarning() {
    NamedType namedType = new NamedType(registry, "NonExistentType", "source.js", 42, 10);
    namedType.resolveInternal(errorReporter, scope);

    assertFalse(errorReporter.getWarnings().isEmpty());
    assertTrue(errorReporter.getWarnings().get(0).contains("NonExistentType"));
  }

  // Tests setValidator behavior before and after resolution
  @Test
  public void testSetValidator_beforeAndAfterResolution_validatesProperly() {
    final boolean[] validated = new boolean[] { false };
    Predicate<JSType> validator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType type) {
        validated[0] = true;
        return true;
      }
    };

    registry.declareType("ValidatableType", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    NamedType namedType = new NamedType(registry, "ValidatableType", "source.js", 1, 0);

    assertTrue(namedType.setValidator(validator));
    assertFalse(validated[0]);

    namedType.resolveInternal(errorReporter, scope);
    assertTrue(validated[0]);

    assertTrue(namedType.setValidator(Predicates.<JSType>alwaysTrue()));
  }

  // Tests lookup when slot type is ALL_TYPE or NO_TYPE
  @Test
  public void testLookupViaProperties_allTypeOrNoTypeSlot_returnsNull() {
    scope.addSlot("allSlot", registry.getNativeType(JSTypeNative.ALL_TYPE));
    scope.addSlot("noSlot", registry.getNativeType(JSTypeNative.NO_TYPE));

    NamedType namedType1 = new NamedType(registry, "allSlot.sub", "source.js", 1, 0);
    namedType1.resolveInternal(errorReporter, scope);

    NamedType namedType2 = new NamedType(registry, "noSlot.sub", "source.js", 1, 0);
    namedType2.resolveInternal(errorReporter, scope);

    assertNotNull(namedType1.getReferencedType());
    assertNotNull(namedType2.getReferencedType());
  }

  // Tests cycle detection with EnumElementType
  @Test
  public void testCheckEnumElementCycle_cyclicEnumElement_handlesCycle() {
    NamedType namedType = new NamedType(registry, "CyclicEnum", "source.js", 1, 0);
    EnumType enumType = registry.createEnumType("CyclicEnum", null, namedType);
    EnumElementType enumElementType = enumType.getElementsType();

    registry.declareType("CyclicEnum", enumElementType);
    namedType.resolveInternal(errorReporter, scope);

    assertEquals(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), namedType.getReferencedType());
  }

  // Tests typedef resolution via scope slot
  @Test
  public void testResolveInternal_typedefInScope_resolvesCorrectly() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    scope.addSlot("TypedefSlot", numberType);

    NamedType namedType = new NamedType(registry, "TypedefSlot", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(numberType, namedType.getReferencedType());
  }

  // Tests equals method
  @Test
  public void testEquals_sameAndDifferentTypes_returnsExpected() {
    NamedType type1 = new NamedType(registry, "TypeA", "source.js", 1, 0);
    NamedType type2 = new NamedType(registry, "TypeA", "source2.js", 2, 0);
    NamedType type3 = new NamedType(registry, "TypeB", "source.js", 1, 0);

    assertTrue(type1.equals(type1));
    assertTrue(type1.equals(type2));
    assertFalse(type1.equals(type3));
    assertFalse(type1.equals(null));
    assertFalse(type1.equals("NotAType"));
  }

  // Tests resolution when scope is null
  @Test
  public void testResolveInternal_nullScope_resolvesFromRegistry() {
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    registry.declareType("GlobalBool", booleanType);

    NamedType namedType = new NamedType(registry, "GlobalBool", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, null);

    assertTrue(namedType.isResolved());
    assertEquals(booleanType, namedType.getReferencedType());
  }

  // Tests resolving already resolved type does not re-resolve
  @Test
  public void testResolveInternal_alreadyResolved_returnsImmediately() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    registry.declareType("OnceType", stringType);

    NamedType namedType = new NamedType(registry, "OnceType", "source.js", 1, 0);
    JSType firstResolved = namedType.resolveInternal(errorReporter, scope);
    JSType secondResolved = namedType.resolveInternal(errorReporter, scope);

    assertSame(firstResolved, secondResolved);
  }

  // Helper test scope implementation
  private static class TestScope implements StaticScope<JSType> {
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();

    void addSlot(String name, JSType type) {
      slots.put(name, new SimpleSlot(name, type, false));
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      return slots.get(name);
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      return slots.get(name);
    }

    @Override
    public StaticScope<JSType> getParentScope() {
      return null;
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }
  }
}