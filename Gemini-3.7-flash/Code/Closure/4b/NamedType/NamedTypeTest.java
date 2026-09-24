package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class NamedTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private TestScope scope;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    scope = new TestScope();
  }

  // Tests constructor with null reference throwing NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullReference_throwsNullPointerException() {
    new NamedType(registry, null, "test.js", 1, 0);
  }

  // Tests basic getter methods and flags
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

  // Tests property definition before resolution buffers property continuations
  @Test
  public void testDefineProperty_beforeResolution_buffersProperty() {
    NamedType namedType = new NamedType(registry, "TargetType", "source.js", 1, 0);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    Node propertyNode = new Node(0);

    assertFalse(namedType.isResolved());
    boolean defined = namedType.defineProperty("prop1", stringType, false, propertyNode);
    assertTrue(defined);
  }

  // Tests property definition after resolution delegates to super
  @Test
  public void testDefineProperty_afterResolution_definesOnResolvedType() {
    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    scope.addSlot("MyObj", objectType);

    NamedType namedType = new NamedType(registry, "MyObj", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    Node propertyNode = new Node(0);
    boolean defined = namedType.defineProperty("prop2", stringType, false, propertyNode);
    assertTrue(defined);
  }

  // Tests resolving a type via property lookup for a constructor function
  @Test
  public void testResolveInternal_constructorFunctionInScope_resolvesToInstanceType() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    scope.addSlot("Foo", ctor);

    NamedType namedType = new NamedType(registry, "Foo", "source.js", 1, 0);
    JSType resolved = namedType.resolveInternal(errorReporter, scope);

    assertNotNull(resolved);
    assertTrue(namedType.isResolved());
    assertEquals(ctor.getInstanceType(), namedType.getReferencedType());
  }

  // Tests resolving nested property chain like Foo.Bar
  @Test
  public void testResolveInternal_nestedPropertyLookup_resolvesCorrectly() {
    ObjectType parentObj = registry.createObjectType("Foo", null, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    FunctionType nestedCtor = registry.createConstructorType("Bar", null, null, null);
    parentObj.defineProperty("Bar", nestedCtor, false, new Node(0));
    scope.addSlot("Foo", parentObj);

    NamedType namedType = new NamedType(registry, "Foo.Bar", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(nestedCtor.getInstanceType(), namedType.getReferencedType());
  }

  // Tests resolving an EnumType via property lookup
  @Test
  public void testResolveInternal_enumTypeInScope_resolvesToElementsType() {
    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    scope.addSlot("MyEnum", enumType);

    NamedType namedType = new NamedType(registry, "MyEnum", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertEquals(enumType.getElementsType(), namedType.getReferencedType());
  }

  // Tests resolving an unresolved type emits a warning when not forward declared
  @Test
  public void testResolveInternal_unknownType_emitsWarning() {
    NamedType namedType = new NamedType(registry, "UnknownType", "source.js", 42, 7);
    namedType.resolveInternal(errorReporter, scope);

    assertEquals(1, errorReporter.warnings().length);
    assertTrue(errorReporter.warnings()[0].contains("Bad type annotation. Unknown type UnknownType"));
    assertTrue(namedType.isResolved());
  }

  // Tests resolving forward-declared type does not emit warning
  @Test
  public void testResolveInternal_forwardDeclaredType_noWarningEmitted() {
    registry.forwardDeclareType("ForwardDeclaredType");
    NamedType namedType = new NamedType(registry, "ForwardDeclaredType", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertEquals(0, errorReporter.warnings().length);
    assertTrue(namedType.isResolved());
  }

  // Tests property continuation commitment on successful resolution
  @Test
  public void testResolveInternal_withContinuations_commitsPropertiesToResolvedType() {
    ObjectType objectType = registry.createObjectType("CustomTarget", null, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    scope.addSlot("CustomTarget", objectType);

    NamedType namedType = new NamedType(registry, "CustomTarget", "source.js", 1, 0);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    namedType.defineProperty("dynamicProp", numberType, false, new Node(0));

    namedType.resolveInternal(errorReporter, scope);

    assertTrue(namedType.isResolved());
    assertTrue(objectType.hasProperty("dynamicProp"));
  }

  // Tests setting validator before resolution applies when resolved
  @Test
  public void testSetValidator_beforeResolution_appliesOnResolve() {
    final boolean[] validated = new boolean[] { false };
    Predicate<JSType> validator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType type) {
        validated[0] = true;
        return true;
      }
    };

    FunctionType ctor = registry.createConstructorType("ValidatedType", null, null, null);
    scope.addSlot("ValidatedType", ctor);

    NamedType namedType = new NamedType(registry, "ValidatedType", "source.js", 1, 0);
    boolean accepted = namedType.setValidator(validator);
    assertTrue(accepted);
    assertFalse(validated[0]);

    namedType.resolveInternal(errorReporter, scope);
    assertTrue(validated[0]);
  }

  // Tests setting validator after resolution validates immediately
  @Test
  public void testSetValidator_afterResolution_validatesImmediately() {
    final boolean[] validated = new boolean[] { false };
    Predicate<JSType> validator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType type) {
        validated[0] = true;
        return true;
      }
    };

    ObjectType objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    scope.addSlot("ImmediateObj", objectType);

    NamedType namedType = new NamedType(registry, "ImmediateObj", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);
    assertTrue(namedType.isResolved());

    boolean accepted = namedType.setValidator(validator);
    assertTrue(accepted);
  }

  // Tests lookup via properties with empty first component
  @Test
  public void testResolveViaProperties_emptyFirstComponent_remainsUnresolved() {
    NamedType namedType = new NamedType(registry, ".InvalidName", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertEquals(1, errorReporter.warnings().length);
  }

  // Tests lookup with slot of AllType returns null and emits warning
  @Test
  public void testResolveViaProperties_allTypeSlot_handledAsUnresolved() {
    scope.addSlot("Wildcard", registry.getNativeType(JSTypeNative.ALL_TYPE));
    NamedType namedType = new NamedType(registry, "Wildcard", "source.js", 1, 0);
    namedType.resolveInternal(errorReporter, scope);

    assertEquals(1, errorReporter.warnings().length);
  }

  // Helper test scope implementation
  private static class TestScope implements StaticScope<JSType> {
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();

    void addSlot(final String name, final JSType type) {
      slots.put(name, new StaticSlot<JSType>() {
        @Override
        public String getName() {
          return name;
        }

        @Override
        public JSType getType() {
          return type;
        }

        @Override
        public boolean isTypeInferred() {
          return false;
        }

        @Override
        public StaticReference<JSType> getDeclaration() {
          return null;
        }

        @Override
        public JSDocInfo getJSDocInfo() {
          return null;
        }
      });
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticScope<JSType> getParentScope() {
      return null;
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
    public JSType getTypeOfThis() {
      return null;
    }
  }
}