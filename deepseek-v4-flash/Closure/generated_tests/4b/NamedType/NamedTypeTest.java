package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class NamedTypeTest {

  private RecordingErrorReporter reporter;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    reporter = new RecordingErrorReporter();
    registry = new JSTypeRegistry(reporter);
    registry.setLastGeneration(true);
  }

  // Tests null reference validation in the constructor.
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullReference_throwsException() {
    new NamedType(registry, null, "source", 0, 0);
  }

  // Tests that reference name and type flags are exposed correctly.
  @Test
  public void testGetReferenceName_returnsReferenceAndTypeFlags() {
    NamedType type = new NamedType(registry, "My.Type", "source", 3, 4);

    assertEquals("My.Type", type.getReferenceName());
    assertTrue(type.hasReferenceName());
    assertTrue(type.isNamedType());
    assertTrue(type.isNominalType());
    assertEquals("My.Type".hashCode(), type.hashCode());
    assertEquals("My.Type", type.toStringHelper(false));
  }

  // Tests that the referenced type is unknown before resolution.
  @Test
  public void testGetReferencedType_beforeResolution_isUnknown() {
    NamedType type = new NamedType(registry, "MyType", "source", 0, 0);

    assertEquals(
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE),
        type.getReferencedType());
  }

  // Tests property definition on an unresolved NamedType, then verifies the
  // property is committed after resolution.
  @Test
  public void testDefineProperty_unresolvedType_commitsPropertyAfterResolve() {
    ObjectType target = registry.createObjectType(
        "Foo", null, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    registry.registerType(target, "Foo");

    NamedType type = new NamedType(registry, "Foo", "source", 0, 0);
    JSType propType = registry.getNativeObjectType(JSTypeNative.NUMBER_TYPE);

    assertTrue(type.defineProperty("bar", propType, true, new Node(0)));
    type.resolveInternal(reporter, emptyScope());

    assertTrue(target.hasProperty("bar"));
    assertEquals(propType, target.getPropertyType("bar"));
    assertEquals(0, reporter.warningCount);
  }

  // Tests successful registry-based resolution in the final generation.
  @Test
  public void testResolveInternal_viaRegistry_lastGeneration_returnsReferencedType() {
    JSType expected = registry.getType("Object");
    assertNotNull(expected);

    NamedType type = new NamedType(registry, "Object", "source", 0, 0);
    JSType result = type.resolveInternal(reporter, emptyScope());

    assertEquals(expected, result);
    assertEquals(expected, type.getReferencedType());
    assertEquals(0, reporter.warningCount);
  }

  // Tests that resolution in a non-final generation keeps the NamedType object.
  @Test
  public void testResolveInternal_viaRegistry_notLastGeneration_returnsThis() {
    registry.setLastGeneration(false);
    JSType expected = registry.getType("Object");
    assertNotNull(expected);

    NamedType type = new NamedType(registry, "Object", "source", 0, 0);
    JSType result = type.resolveInternal(reporter, emptyScope());

    assertSame(type, result);
  }

  // Tests that an unresolvable type in the final generation warns and resolves
  // to UNKNOWN_TYPE.
  @Test
  public void testResolveInternal_unknownType_lastGeneration_warnsAndResolvesToUnknown() {
    NamedType type = new NamedType(registry, "Unknown", "source", 5, 6);

    JSType result = type.resolveInternal(reporter, emptyScope());

    assertEquals(1, reporter.warningCount);
    assertTrue(reporter.lastWarning.contains("Unknown type Unknown"));
    assertEquals(
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), result);
  }

  // Tests that forward-declared type names do not warn and resolve to
  // NO_RESOLVED_TYPE.
  @Test
  public void testResolveInternal_forwardDeclaredType_lastGeneration_resolvesToNoResolvedTypeWithoutWarning() {
    registry.addForwardDeclaredType("Forward");

    NamedType type = new NamedType(registry, "Forward", "source", 0, 0);
    JSType result = type.resolveInternal(reporter, emptyScope());

    assertEquals(0, reporter.warningCount);
    assertEquals(
        registry.getNativeObjectType(JSTypeNative.NO_RESOLVED_TYPE), result);
  }

  // Tests the empty-reference boundary case.
  @Test
  public void testResolveInternal_emptyReference_lastGeneration_warns() {
    NamedType type = new NamedType(registry, "", "source", 0, 0);

    JSType result = type.resolveInternal(reporter, emptyScope());

    assertEquals(1, reporter.warningCount);
    assertTrue(reporter.lastWarning.contains("Unknown type"));
    assertEquals(
        registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE), result);
  }

  // Tests the lookup path where the first scope slot exists but has null type.
  @Test
  public void testResolveInternal_slotWithNullType_lastGeneration_warns() {
    HashMap<String, JSType> slots = new HashMap<String, JSType>();
    slots.put("Foo", null);
    NamedType type = new NamedType(registry, "Foo", "source", 1, 1);

    type.resolveInternal(reporter, new TestStaticScope(slots));

    assertEquals(1, reporter.warningCount);
    assertTrue(reporter.lastWarning.contains("Unknown type Foo"));
  }

  // Tests that an unresolved type in a non-final generation remains as this
  // NamedType and does not produce a warning.
  @Test
  public void testResolveInternal_unresolvedType_notLastGeneration_returnsThisWithoutWarning() {
    registry.setLastGeneration(false);
    NamedType type = new NamedType(registry, "NotThere", "source", 0, 0);

    JSType result = type.resolveInternal(reporter, emptyScope());

    assertSame(type, result);
    assertEquals(0, reporter.warningCount);
  }

  // Tests that getTypedefType returns the slot type when present.
  @Test
  public void testGetTypedefType_withType_returnsSlotType() {
    NamedType type = new NamedType(registry, "Foo", "source", 0, 0);
    JSType expected = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    TestStaticSlot slot = new TestStaticSlot("Foo", expected);

    JSType result = type.getTypedefType(reporter, slot, "Foo");

    assertEquals(expected, result);
  }

  // Tests that a validator set before resolution is applied when the type is
  // resolved.
  @Test
  public void testSetValidator_beforeResolution_appliesOnResolve() {
    final boolean[] applied = new boolean[1];
    Predicate<JSType> validator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        applied[0] = true;
        return true;
      }
    };

    NamedType type = new NamedType(registry, "Object", "source", 0, 0);

    assertTrue(type.setValidator(validator));
    assertFalse(applied[0]);

    type.resolveInternal(reporter, emptyScope());

    assertTrue(applied[0]);
    assertEquals(0, reporter.warningCount);
  }

  private static StaticScope<JSType> emptyScope() {
    return new TestStaticScope(new HashMap<String, JSType>());
  }

  private static class RecordingErrorReporter implements ErrorReporter {
    private int warningCount;
    private String lastWarning;

    @Override
    public void warning(String message, String sourceName, int line,
        int lineOffset) {
      warningCount++;
      lastWarning = message;
    }

    @Override
    public void error(String message, String sourceName, int line,
        int lineOffset) {
      throw new AssertionError("Unexpected error: " + message);
    }
  }

  private static class TestStaticSlot implements StaticSlot<JSType> {
    private final String name;
    private final JSType type;

    TestStaticSlot(String name, JSType type) {
      this.name = name;
      this.type = type;
    }

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
  }

  private static class TestStaticScope implements StaticScope<JSType> {
    private final Map<String, JSType> slots;

    TestStaticScope(Map<String, JSType> slots) {
      this.slots = new HashMap<String, JSType>(slots);
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      if (!slots.containsKey(name)) {
        return null;
      }
      return new TestStaticSlot(name, slots.get(name));
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }
  }
}