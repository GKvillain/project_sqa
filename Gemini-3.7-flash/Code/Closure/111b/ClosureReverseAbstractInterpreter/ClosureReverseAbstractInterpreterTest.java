package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_VOID;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_STRING_BOOLEAN;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ClosureReverseAbstractInterpreter interpreter;
  private FlowScope blindScope;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new GoogleCodingConvention();
    interpreter = new ClosureReverseAbstractInterpreter(convention, registry);
    blindScope = new SemanticReverseAbstractInterpreter(convention, registry).getPreciserScopeKnowingConditionOutcome(
        new Node(Token.TRUE), null, true);
  }

  private Node createGoogCall(String fnName, String paramName) {
    Node callee = new Node(Token.GETPROP, Node.newString(Token.NAME, "goog"), Node.newString(Token.STRING, fnName));
    Node param = Node.newString(Token.NAME, paramName);
    return new Node(Token.CALL, callee, param);
  }

  private FlowScope createScopeWithVar(String name, JSType type) {
    FlowScope scope = blindScope.createChildFlowScope();
    scope.inferSlotType(name, type);
    return scope;
  }

  // Tests goog.isArray when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsArray_trueOutcome() {
    JSType targetType = registry.getNativeType(OBJECT_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isArray", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(ARRAY_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isArray when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsArray_falseOutcome() {
    JSType targetType = registry.getNativeType(ARRAY_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isArray", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
  }

  // Tests goog.isObject when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsObject_trueOutcome() {
    JSType targetType = registry.getNativeType(ALL_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isObject", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isObject when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsObject_falseOutcome() {
    JSType targetType = registry.getNativeType(ALL_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isObject", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    JSType expected = registry.createUnionType(
        registry.getNativeType(NUMBER_STRING_BOOLEAN),
        registry.getNativeType(NULL_VOID));
    assertEquals(expected, result.getSlot("x").getType());
  }

  // Tests goog.isDef when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsDef_trueOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(VOID_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isDef", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(STRING_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isDef when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsDef_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(VOID_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isDef", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(VOID_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isNull when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsNull_trueOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isNull", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NULL_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isNull when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsNull_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isNull", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(STRING_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isDefAndNotNull when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsDefAndNotNull_trueOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isDefAndNotNull", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isDefAndNotNull when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsDefAndNotNull_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isDefAndNotNull", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NULL_VOID), result.getSlot("x").getType());
  }

  // Tests goog.isString when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsString_trueOutcome() {
    JSType targetType = registry.getNativeType(ALL_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isString", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(STRING_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isBoolean when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsBoolean_trueOutcome() {
    JSType targetType = registry.getNativeType(ALL_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isBoolean", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(BOOLEAN_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isNumber when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsNumber_trueOutcome() {
    JSType targetType = registry.getNativeType(ALL_TYPE);
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isNumber", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isFunction when true outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsFunction_trueOutcome() {
    FlowScope scope = createScopeWithVar("x", registry.getNativeType(ALL_TYPE));
    Node call = createGoogCall("isFunction", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
    assertTrue(result.getSlot("x").getType().isFunctionType() ||
               result.getSlot("x").getType().isSubtype(registry.getNativeType(OBJECT_TYPE)));
  }

  // Tests non-goog call returns unmodified scope from next interpreter
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_nonGoogCall_fallsThrough() {
    Node callee = new Node(Token.GETPROP, Node.newString(Token.NAME, "other"), Node.newString(Token.STRING, "isDef"));
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("x", registry.getNativeType(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
  }

  // Tests non-call condition node falls through
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_nonCallNode_fallsThrough() {
    Node condition = Node.newString(Token.NAME, "x");
    FlowScope scope = createScopeWithVar("x", registry.getNativeType(STRING_TYPE));

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope, true);
    assertNotNull(result);
  }

  // Tests goog.isString when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsString_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isString", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isBoolean when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsBoolean_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(BOOLEAN_TYPE),
        registry.getNativeType(NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isBoolean", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isNumber when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsNumber_falseOutcome() {
    JSType targetType = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(STRING_TYPE));
    FlowScope scope = createScopeWithVar("x", targetType);
    Node call = createGoogCall("isNumber", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
    assertEquals(registry.getNativeType(STRING_TYPE), result.getSlot("x").getType());
  }

  // Tests goog.isFunction when false outcome
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googIsFunction_falseOutcome() {
    FlowScope scope = createScopeWithVar("x", registry.getNativeType(ALL_TYPE));
    Node call = createGoogCall("isFunction", "x");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertNotNull(result);
  }

  // Tests goog call without arguments
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_googCallNoArgs() {
    Node callee = new Node(Token.GETPROP, Node.newString(Token.NAME, "goog"), Node.newString(Token.STRING, "isDef"));
    Node call = new Node(Token.CALL, callee);

    FlowScope scope = createScopeWithVar("x", registry.getNativeType(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
  }

  // Tests direct call node with simple function name (not GETPROP)
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_simpleCallName() {
    Node callee = Node.newString(Token.NAME, "isDef");
    Node param = Node.newString(Token.NAME, "x");
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("x", registry.getNativeType(STRING_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
  }

  // Tests goog call with qualified name property parameter like a.b
  @Test
  public void testGetPreciserScopeKnowingConditionOutcome_qualifiedNameParam() {
    Node callee = new Node(Token.GETPROP, Node.newString(Token.NAME, "goog"), Node.newString(Token.STRING, "isString"));
    Node param = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("a.b", registry.getNativeType(ALL_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(result);
  }
}