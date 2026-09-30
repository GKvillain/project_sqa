package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.EVAL_ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.URI_ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static com.google.javascript.rhino.jstype.TernaryValue.FALSE;
import static com.google.javascript.rhino.jstype.TernaryValue.TRUE;
import static com.google.javascript.rhino.jstype.TernaryValue.UNKNOWN;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UnionTypeTest {
  private JSTypeRegistry registry;
  private JSType NUMBER;
  private JSType STRING;
  private JSType BOOLEAN;
  private JSType NULL;
  private JSType VOID;
  private JSType OBJECT;
  private JSType EVAL_ERROR;
  private JSType URI_ERROR;
  private JSType NO_OBJECT;
  private JSType NO;
  private JSType UNKNOWN_T;
  private JSType ALL;
  private JSType FUNCTION;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    NUMBER = registry.getNativeType(NUMBER_TYPE);
    STRING = registry.getNativeType(STRING_TYPE);
    BOOLEAN = registry.getNativeType(BOOLEAN_TYPE);
    NULL = registry.getNativeType(NULL_TYPE);
    VOID = registry.getNativeType(VOID_TYPE);
    OBJECT = registry.getNativeType(OBJECT_TYPE);
    EVAL_ERROR = registry.getNativeType(EVAL_ERROR_TYPE);
    URI_ERROR = registry.getNativeType(URI_ERROR_TYPE);
    NO_OBJECT = registry.getNativeType(NO_OBJECT_TYPE);
    NO = registry.getNativeType(NO_TYPE);
    UNKNOWN_T = registry.getNativeType(UNKNOWN_TYPE);
    ALL = registry.getNativeType(ALL_TYPE);
    FUNCTION = registry.getNativeType(FUNCTION_INSTANCE_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    return (UnionType) registry.createUnionType(types);
  }

  // Tests meet with overlapping union types having no common subtype
  @Test
  public void testMeet_disjointUnionOfObjects_returnsNoObjectType() {
    UnionType union1 = createUnion(EVAL_ERROR, NUMBER);
    UnionType union2 = createUnion(URI_ERROR, STRING);
    JSType result = union1.meet(union2);
    assertEquals(NO_TYPE, result);
  }

  // Tests meet between two unions of object types having no common subtype (Defects4J 104b)
  @Test
  public void testMeet_disjointObjects_returnsNoObjectType() {
    UnionType union1 = createUnion(EVAL_ERROR);
    UnionType union2 = createUnion(URI_ERROR);
    JSType result = union1.meet(union2);
    assertEquals(NO_OBJECT, result);
  }

  // Tests meet with common alternate
  @Test
  public void testMeet_commonAlternate_returnsCommonType() {
    UnionType union1 = createUnion(NUMBER, STRING);
    UnionType union2 = createUnion(STRING, BOOLEAN);
    JSType result = union1.meet(union2);
    assertEquals(STRING, result);
  }

  // Tests matchesNumberContext
  @Test
  public void testMatchesNumberContext_withNumberAlternate_returnsTrue() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.matchesNumberContext());
  }

  // Tests matchesStringContext
  @Test
  public void testMatchesStringContext_withStringAlternate_returnsTrue() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.matchesStringContext());
  }

  // Tests matchesObjectContext
  @Test
  public void testMatchesObjectContext_withObjectAlternate_returnsTrue() {
    UnionType union = createUnion(OBJECT, NULL);
    assertTrue(union.matchesObjectContext());
  }

  // Tests matchesObjectContext with only null and void
  @Test
  public void testMatchesObjectContext_nullAndVoid_returnsFalse() {
    UnionType union = createUnion(NULL, VOID);
    assertFalse(union.matchesObjectContext());
  }

  // Tests canAssignTo
  @Test
  public void testCanAssignTo_validTarget_returnsTrue() {
    UnionType union = createUnion(EVAL_ERROR, URI_ERROR);
    assertTrue(union.canAssignTo(OBJECT));
    assertFalse(union.canAssignTo(NUMBER));
  }

  // Tests canBeCalled
  @Test
  public void testCanBeCalled_nonCallableAlternates_returnsFalse() {
    UnionType union = createUnion(NUMBER, STRING);
    assertFalse(union.canBeCalled());
  }

  // Tests restrictByNotNullOrUndefined
  @Test
  public void testRestrictByNotNullOrUndefined_removesNullAndVoid() {
    UnionType union = createUnion(NUMBER, NULL, VOID);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertEquals(NUMBER, restricted);
  }

  // Tests testForEquality
  @Test
  public void testTestForEquality_sameTypesAndDifferingTypes() {
    UnionType union = createUnion(NUMBER, STRING);
    assertEquals(UNKNOWN, union.testForEquality(BOOLEAN));
  }

  // Tests isNullable
  @Test
  public void testIsNullable_withNullAlternate_returnsTrue() {
    UnionType unionWithNull = createUnion(NUMBER, NULL);
    assertTrue(unionWithNull.isNullable());

    UnionType unionWithoutNull = createUnion(NUMBER, STRING);
    assertFalse(unionWithoutNull.isNullable());
  }

  // Tests isObject
  @Test
  public void testIsObject_allObjects_returnsTrue() {
    UnionType objectUnion = createUnion(EVAL_ERROR, URI_ERROR);
    assertTrue(objectUnion.isObject());

    UnionType mixedUnion = createUnion(OBJECT, NUMBER);
    assertFalse(mixedUnion.isObject());
  }

  // Tests contains
  @Test
  public void testContains_existingAndNonExistingAlternates() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.contains(NUMBER));
    assertFalse(union.contains(BOOLEAN));
  }

  // Tests getRestrictedUnion
  @Test
  public void testGetRestrictedUnion_removesSubtypes() {
    UnionType union = createUnion(NUMBER, STRING);
    JSType restricted = union.getRestrictedUnion(NUMBER);
    assertEquals(STRING, restricted);
  }

  // Tests toString formatting
  @Test
  public void testToString_formatsAlternatesSorted() {
    UnionType union = createUnion(STRING, NUMBER);
    assertEquals("(number|string)", union.toString());
  }

  // Tests isSubtype
  @Test
  public void testIsSubtype_subtypesMatch_returnsTrue() {
    UnionType union = createUnion(EVAL_ERROR, URI_ERROR);
    assertTrue(union.isSubtype(OBJECT));
    assertFalse(union.isSubtype(NUMBER));
  }

  // Tests getLeastSupertype
  @Test
  public void testGetLeastSupertype_containedSubtype_returnsThis() {
    UnionType union = createUnion(OBJECT, NUMBER);
    assertEquals(union, union.getLeastSupertype(EVAL_ERROR));
  }

  // Tests equals and hashCode
  @Test
  public void testEqualsAndHashCode_sameAlternates_areEqual() {
    UnionType union1 = createUnion(NUMBER, STRING);
    UnionType union2 = createUnion(STRING, NUMBER);
    assertEquals(union1, union2);
    assertEquals(union1.hashCode(), union2.hashCode());
    assertFalse(union1.equals(NUMBER));
  }

  // Additional coverage tests

  @Test
  public void testIsUnionTypeAndToMaybeUnionType() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.isUnionType());
    assertSame(union, union.toMaybeUnionType());
  }

  @Test
  public void testGetAlternates() {
    UnionType union = createUnion(NUMBER, STRING);
    Collection<JSType> alternates = union.getAlternates();
    assertEquals(2, alternates.size());
    assertTrue(alternates.contains(NUMBER));
    assertTrue(alternates.contains(STRING));
  }

  @Test
  public void testCanTestForEqualityWith() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.canTestForEqualityWith(NUMBER));
    assertTrue(union.canTestForEqualityWith(createUnion(STRING, BOOLEAN)));
    assertFalse(union.canTestForEqualityWith(NO));
  }

  @Test
  public void testCanTestForShallowEqualityWith() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.canTestForShallowEqualityWith(NUMBER));
    assertFalse(union.canTestForShallowEqualityWith(NO));
  }

  @Test
  public void testAutoboxesTo() {
    UnionType unionPrimitive = createUnion(NUMBER, STRING);
    assertNull(unionPrimitive.autoboxesTo());

    ObjectType numObj = NUMBER.autoboxesTo();
    ObjectType strObj = STRING.autoboxesTo();
    UnionType unionObjects = createUnion(numObj, strObj);
    assertNull(unionObjects.autoboxesTo());
  }

  @Test
  public void testCanBeCalled_callableAlternates() {
    ObjectType fnType = registry.createFunctionType(NUMBER, NUMBER);
    UnionType union = createUnion(fnType, FUNCTION);
    assertTrue(union.canBeCalled());
  }

  @Test
  public void testPropertiesAccess() {
    ObjectType record1 = registry.createRecordTypeBuilder()
        .addProperty("foo", NUMBER, null)
        .addProperty("bar", STRING, null)
        .build();
    ObjectType record2 = registry.createRecordTypeBuilder()
        .addProperty("foo", BOOLEAN, null)
        .build();

    UnionType union = createUnion(record1, record2);
    assertTrue(union.hasProperty("foo"));
    assertFalse(union.hasProperty("bar"));

    JSType fooProp = union.findPropertyType("foo");
    assertNotNull(fooProp);
    assertTrue(fooProp.isUnionType());
    assertTrue(((UnionType) fooProp).contains(NUMBER));
    assertTrue(((UnionType) fooProp).contains(BOOLEAN));

    assertNull(union.findPropertyType("bar"));
    assertNull(union.findPropertyType("baz"));

    assertNotNull(union.getSlot("foo"));
    assertNull(union.getSlot("nonExistent"));

    Collection<String> propNames = union.getPropertyNames();
    assertTrue(propNames.contains("foo"));
    assertFalse(propNames.contains("bar"));
  }

  @Test
  public void testGetPossibleWithName() {
    ObjectType record1 = registry.createRecordTypeBuilder()
        .addProperty("foo", NUMBER, null)
        .build();
    ObjectType record2 = registry.createRecordTypeBuilder()
        .addProperty("bar", STRING, null)
        .build();

    UnionType union = createUnion(record1, record2);
    JSType possibleFoo = union.getPossibleWithName("foo");
    assertEquals(record1, possibleFoo);

    JSType possibleNonExistent = union.getPossibleWithName("baz");
    assertNull(possibleNonExistent);
  }

  @Test
  public void testCollapseUnion() {
    UnionType union1 = createUnion(NUMBER, STRING);
    assertEquals(union1, union1.collapseUnion());

    UnionType unionObjects = createUnion(EVAL_ERROR, URI_ERROR);
    JSType collapsed = unionObjects.collapseUnion();
    assertNotNull(collapsed);
  }

  @Test
  public void testTypesUnderEqualityAndInequality() {
    UnionType union = createUnion(NUMBER, STRING, NULL);

    JSType.TypePair eqPair = union.getTypesUnderEquality(NUMBER);
    assertEquals(NUMBER, eqPair.typeA);
    assertEquals(NUMBER, eqPair.typeB);

    JSType.TypePair ineqPair = union.getTypesUnderInequality(NULL);
    assertFalse(ineqPair.typeA.isNullable());

    JSType.TypePair shallowEqPair = union.getTypesUnderShallowEquality(STRING);
    assertEquals(STRING, shallowEqPair.typeA);
    assertEquals(STRING, shallowEqPair.typeB);

    JSType.TypePair shallowIneqPair = union.getTypesUnderShallowInequality(STRING);
    assertFalse(((UnionType) shallowIneqPair.typeA).contains(STRING));
  }

  @Test
  public void testNominalAndInstanceProperties() {
    UnionType union = createUnion(NUMBER, STRING);
    assertFalse(union.isNominalType());
    assertFalse(union.isNominalConstructor());
    assertFalse(union.isInstanceType());
  }

  @Test
  public void testVisitor() {
    UnionType union = createUnion(NUMBER, STRING);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNumberType() { return "num"; }
      @Override public String caseStringType() { return "str"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseNoObjectType() { return "no_obj"; }
      @Override public String caseTemplateType(TemplateType templateType) { return "template"; }
    };
    assertEquals("union", union.visit(visitor));
  }

  @Test
  public void testMatchesContexts_falseBranches() {
    UnionType nonNumberUnion = createUnion(STRING, BOOLEAN);
    assertFalse(nonNumberUnion.matchesNumberContext());

    UnionType nonStringUnion = createUnion(NUMBER, BOOLEAN);
    assertFalse(nonStringUnion.matchesStringContext());
  }

  @Test
  public void testGetRestrictedUnion_edgeCases() {
    UnionType union = createUnion(NUMBER, STRING);
    // Restricting with unknown or non-overlapping type keeps alternates intact
    assertEquals(union, union.getRestrictedUnion(BOOLEAN));
    // Restricting with ALL removes everything returning NO_TYPE
    assertEquals(NO, union.getRestrictedUnion(ALL));
  }
}