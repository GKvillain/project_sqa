package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.CodingConventions;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChainableReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private TestInterpreter interpreter;

  private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
    public TestInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry) {
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
    convention = CodingConventions.getDefault();
    interpreter = new TestInterpreter(convention, registry);
  }

  // Tests constructor null convention throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testConstructor_nullConvention_throwsException() {
    new TestInterpreter(null, registry);
  }

  // Tests append chains two interpreters and preserves firstLink
  @Test
  public void testAppend_validInterpreter_linksCorrectly() {
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter result = (TestInterpreter) interpreter.append(second);

    assertSame(second, result);
    assertSame(interpreter, interpreter.getFirst());
    assertSame(interpreter, second.getFirst());
  }

  // Tests append throws exception when appended link already has a next link
  @Test(expected = IllegalArgumentException.class)
  public void testAppend_alreadyChainedInterpreter_throwsException() {
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter third = new TestInterpreter(convention, registry);
    second.append(third);
    interpreter.append(second);
  }

  // Tests getRestrictedWithoutUndefined with undefined type returns null
  @Test
  public void testGetRestrictedWithoutUndefined_voidType_returnsNull() {
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    assertNull(interpreter.getRestrictedWithoutUndefined(voidType));
  }

  // Tests getRestrictedWithoutUndefined with null input returns null
  @Test
  public void testGetRestrictedWithoutUndefined_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutUndefined(null));
  }

  // Tests getRestrictedWithoutUndefined with string type returns string type
  @Test
  public void testGetRestrictedWithoutUndefined_stringType_returnsStringType() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertSame(stringType, interpreter.getRestrictedWithoutUndefined(stringType));
  }

  // Tests getRestrictedWithoutUndefined with union type removes undefined
  @Test
  public void testGetRestrictedWithoutUndefined_unionType_removesUndefined() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType union = registry.createUnionType(stringType, voidType);

    JSType restricted = interpreter.getRestrictedWithoutUndefined(union);
    assertEquals(stringType, restricted);
  }

  // Tests getRestrictedWithoutNull with null type returns null
  @Test
  public void testGetRestrictedWithoutNull_nullType_returnsNull() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    assertNull(interpreter.getRestrictedWithoutNull(nullType));
  }

  // Tests getRestrictedWithoutNull with null input returns null
  @Test
  public void testGetRestrictedWithoutNull_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutNull(null));
  }

  // Tests getRestrictedWithoutNull with number type returns number type
  @Test
  public void testGetRestrictedWithoutNull_numberType_returnsNumberType() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertSame(numberType, interpreter.getRestrictedWithoutNull(numberType));
  }

  // Tests getRestrictedWithoutNull with union type removes null
  @Test
  public void testGetRestrictedWithoutNull_unionType_removesNull() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType union = registry.createUnionType(numberType, nullType);

    JSType restricted = interpreter.getRestrictedWithoutNull(union);
    assertEquals(numberType, restricted);
  }

  // Tests getRestrictedByTypeOfResult with null type and resultEqualsValue true
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeTrue_returnsNativeType() {
    JSType result = interpreter.getRestrictedByTypeOfResult(null, "number", true);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), result);
  }

  // Tests getRestrictedByTypeOfResult with null type and resultEqualsValue false
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeFalse_returnsNull() {
    JSType result = interpreter.getRestrictedByTypeOfResult(null, "number", false);
    assertNull(result);
  }

  // Tests getRestrictedByTypeOfResult with matching string type
  @Test
  public void testGetRestrictedByTypeOfResult_stringMatchesString_returnsString() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(stringType, "string", true);
    assertEquals(stringType, result);
  }

  // Tests getRestrictedByTypeOfResult with non-matching typeof string
  @Test
  public void testGetRestrictedByTypeOfResult_stringMatchesNumber_returnsNull() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(stringType, "number", true);
    assertNull(result);
  }

  // Tests getRestrictedByTypeOfResult with union type filters matching alternatives
  @Test
  public void testGetRestrictedByTypeOfResult_unionType_filtersCorrectly() {
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType union = registry.createUnionType(stringType, numberType);

    JSType result = interpreter.getRestrictedByTypeOfResult(union, "string", true);
    assertEquals(stringType, result);
  }

  // Tests getTypeIfRefinable for non-refinable node returns null
  @Test
  public void testGetTypeIfRefinable_numberNode_returnsNull() {
    Node node = Node.newNumber(42);
    assertNull(interpreter.getTypeIfRefinable(node, null));
  }

  // Tests declareNameInScope with unsupported node throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testDeclareNameInScope_thisNode_throwsException() {
    Node thisNode = new Node(Token.THIS);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    interpreter.declareNameInScope(null, thisNode, stringType);
  }

  // Tests declareNameInScope with number node throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testDeclareNameInScope_numberNode_throwsException() {
    Node numberNode = Node.newNumber(10);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    interpreter.declareNameInScope(null, numberNode, numberType);
  }
}