package com.google.javascript.jscomp.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class ChainableReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ConcreteInterpreter interpreter;

  private static class ConcreteInterpreter extends ChainableReverseAbstractInterpreter {
    ConcreteInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry) {
      super(convention, typeRegistry);
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(
        Node condition, FlowScope blindScope, boolean outcome) {
      return blindScope;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    convention = new DefaultCodingConvention();
    interpreter = new ConcreteInterpreter(convention, registry);
  }

  // Tests constructor null check for CodingConvention
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullConvention_throwsException() {
    new ConcreteInterpreter(null, registry);
  }

  // Tests appending links in a chain and verifying getFirst
  @Test
  public void testAppend_validChain_updatesFirstAndNextLink() {
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);

    assertSame(interpreter, interpreter.getFirst());
    interpreter.append(link2);
    assertSame(interpreter, link2.getFirst());

    link2.append(link3);
    assertSame(interpreter, link3.getFirst());
  }

  // Tests appending a link that already has a nextLink
  @Test(expected = IllegalArgumentException.class)
  public void testAppend_alreadyChainedLink_throwsException() {
    ConcreteInterpreter link2 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link3 = new ConcreteInterpreter(convention, registry);
    ConcreteInterpreter link4 = new ConcreteInterpreter(convention, registry);

    link2.append(link3);
    interpreter.append(link2); // link2 already has nextLink != null
  }

  // Tests getRestrictedWithoutUndefined on null input
  @Test
  public void testGetRestrictedWithoutUndefined_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutUndefined(null));
  }

  // Tests getRestrictedWithoutUndefined on VOID_TYPE
  @Test
  public void testGetRestrictedWithoutUndefined_voidType_returnsNull() {
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    assertNull(interpreter.getRestrictedWithoutUndefined(voidType));
  }

  // Tests getRestrictedWithoutUndefined on primitive types
  @Test
  public void testGetRestrictedWithoutUndefined_primitiveType_returnsSameType() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertEquals(numberType, interpreter.getRestrictedWithoutUndefined(numberType));

    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertEquals(stringType, interpreter.getRestrictedWithoutUndefined(stringType));

    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    assertEquals(booleanType, interpreter.getRestrictedWithoutUndefined(booleanType));
  }

  // Tests getRestrictedWithoutUndefined on UnionType containing undefined
  @Test
  public void testGetRestrictedWithoutUndefined_unionWithVoid_removesVoid() {
    JSType union = registry.createUnionType(
        JSTypeNative.NUMBER_TYPE, JSTypeNative.VOID_TYPE);
    JSType restricted = interpreter.getRestrictedWithoutUndefined(union);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), restricted);
  }

  // Tests getRestrictedWithoutNull on null input
  @Test
  public void testGetRestrictedWithoutNull_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutNull(null));
  }

  // Tests getRestrictedWithoutNull on NULL_TYPE
  @Test
  public void testGetRestrictedWithoutNull_nullType_returnsNull() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    assertNull(interpreter.getRestrictedWithoutNull(nullType));
  }

  // Tests getRestrictedWithoutNull on primitive types
  @Test
  public void testGetRestrictedWithoutNull_primitiveType_returnsSameType() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertEquals(stringType, interpreter.getRestrictedWithoutNull(stringType));

    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    assertEquals(voidType, interpreter.getRestrictedWithoutNull(voidType));
  }

  // Tests getRestrictedWithoutNull on UnionType containing null
  @Test
  public void testGetRestrictedWithoutNull_unionWithNull_removesNull() {
    JSType union = registry.createUnionType(
        JSTypeNative.STRING_TYPE, JSTypeNative.NULL_TYPE);
    JSType restricted = interpreter.getRestrictedWithoutNull(union);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), restricted);
  }

  // Tests getRestrictedByTypeOfResult when type is null and resultEqualsValue is true
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeTrueOutcome_returnsNativeType() {
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "number", true));
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "boolean", true));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "string", true));
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "undefined", true));
    assertEquals(registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "function", true));
    assertEquals(registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE),
        interpreter.getRestrictedByTypeOfResult(null, "unknown_type", true));
  }

  // Tests getRestrictedByTypeOfResult when type is null and resultEqualsValue is false
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeFalseOutcome_returnsNull() {
    assertNull(interpreter.getRestrictedByTypeOfResult(null, "number", false));
  }

  // Tests getRestrictedByTypeOfResult on primitive matching and non-matching typeof
  @Test
  public void testGetRestrictedByTypeOfResult_primitiveTypes_returnsExpected() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertEquals(numberType,
        interpreter.getRestrictedByTypeOfResult(numberType, "number", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(numberType, "number", false));
    assertNull(interpreter.getRestrictedByTypeOfResult(numberType, "string", true));
    assertEquals(numberType,
        interpreter.getRestrictedByTypeOfResult(numberType, "string", false));
  }

  // Tests getRestrictedByTypeOfResult with Object and Function typeof checks
  @Test
  public void testGetRestrictedByTypeOfResult_objectAndFunctionTypes_returnsExpected() {
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType fnType = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);

    assertEquals(fnType,
        interpreter.getRestrictedByTypeOfResult(objType, "function", true));
    assertEquals(fnType,
        interpreter.getRestrictedByTypeOfResult(fnType, "function", true));
    assertNull(interpreter.getRestrictedByTypeOfResult(fnType, "function", false));
  }

  // Tests getRestrictedByTypeOfResult on ALL_TYPE
  @Test
  public void testGetRestrictedByTypeOfResult_allType_restrictsCorrectly() {
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(allType, "string", true));
    assertEquals(allType,
        interpreter.getRestrictedByTypeOfResult(allType, "string", false));
  }

  // Tests getRestrictedByTypeOfResult on UnionType
  @Test
  public void testGetRestrictedByTypeOfResult_unionType_restrictsAlternates() {
    JSType union = registry.createUnionType(
        JSTypeNative.NUMBER_TYPE, JSTypeNative.STRING_TYPE);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", true));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        interpreter.getRestrictedByTypeOfResult(union, "number", false));
  }

  // Tests declareNameInScope with THIS token (should be ignored safely)
  @Test
  public void testDeclareNameInScope_thisNode_ignoredWithoutError() {
    Node thisNode = new Node(Token.THIS);
    interpreter.declareNameInScope(null, thisNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  // Tests declareNameInScope with unsupported Node token (throws exception)
  @Test(expected = IllegalArgumentException.class)
  public void testDeclareNameInScope_unsupportedNode_throwsException() {
    Node addNode = new Node(Token.ADD);
    interpreter.declareNameInScope(null, addNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  // Tests getTypeIfRefinable with unsupported Node token returns null
  @Test
  public void testGetTypeIfRefinable_unsupportedNode_returnsNull() {
    Node addNode = new Node(Token.ADD);
    assertNull(interpreter.getTypeIfRefinable(addNode, null));
  }
}