package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private FlowScope blindScope;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    interpreter = new ClosureReverseAbstractInterpreter(
        new DefaultCodingConvention(), registry);
    Node root = new Node(Token.BLOCK);
    blindScope = LinkedFlowScope.createEntryLattice(root);
  }

  private Node createGoogCall(String methodName, String paramName) {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, methodName);
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node param = Node.newString(Token.NAME, paramName);
    return new Node(Token.CALL, callee, param);
  }

  private FlowScope createScopeWithVar(String name, JSType type) {
    FlowScope scope = blindScope.createChildFlowScope();
    scope.inferSlotType(name, type);
    return scope;
  }

  // Tests goog.isDef when outcome is true
  @Test
  public void testIsDef_trueOutcome_removesUndefined() {
    JSType stringOrUndefined = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    FlowScope scope = createScopeWithVar("a", stringOrUndefined);
    Node condition = createGoogCall("isDef", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isDef when outcome is false
  @Test
  public void testIsDef_falseOutcome_returnsBlindScope() {
    JSType stringOrUndefined = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    FlowScope scope = createScopeWithVar("a", stringOrUndefined);
    Node condition = createGoogCall("isDef", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertSame(scope, result);
  }

  // Tests goog.isNull when outcome is true
  @Test
  public void testIsNull_trueOutcome_returnsNullType() {
    JSType stringOrNull = registry.createNullableType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", stringOrNull);
    Node condition = createGoogCall("isNull", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isNull when outcome is false
  @Test
  public void testIsNull_falseOutcome_removesNull() {
    JSType stringOrNull = registry.createNullableType(
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", stringOrNull);
    Node condition = createGoogCall("isNull", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isDefAndNotNull when outcome is true
  @Test
  public void testIsDefAndNotNull_trueOutcome_removesNullAndUndefined() {
    JSType fullUnion = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NULL_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    FlowScope scope = createScopeWithVar("a", fullUnion);
    Node condition = createGoogCall("isDefAndNotNull", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isDefAndNotNull when outcome is false
  @Test
  public void testIsDefAndNotNull_falseOutcome_returnsBlindScope() {
    JSType fullUnion = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NULL_TYPE));
    FlowScope scope = createScopeWithVar("a", fullUnion);
    Node condition = createGoogCall("isDefAndNotNull", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertSame(scope, result);
  }

  // Tests goog.isString when outcome is true
  @Test
  public void testIsString_trueOutcome_restrictsToString() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isString", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isString when outcome is false
  @Test
  public void testIsString_falseOutcome_removesString() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isString", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isBoolean when outcome is true
  @Test
  public void testIsBoolean_trueOutcome_restrictsToBoolean() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isBoolean", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isNumber when outcome is true
  @Test
  public void testIsNumber_trueOutcome_restrictsToNumber() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isNumber", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isFunction when outcome is true
  @Test
  public void testIsFunction_trueOutcome_restrictsToFunction() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isFunction", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isArray when outcome is true
  @Test
  public void testIsArray_trueOutcome_restrictsToArray() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isArray", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isArray when outcome is false
  @Test
  public void testIsArray_falseOutcome_removesArray() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isArray", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getTypeInternal());
  }

  // Tests goog.isObject when outcome is true
  @Test
  public void testIsObject_trueOutcome_restrictsToObject() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isObject", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);

    assertEquals(registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isObject when outcome is false
  @Test
  public void testIsObject_falseOutcome_removesObject() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isObject", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        result.getSlot("a").getType());
  }

  // Tests non-CALL node returns blindScope
  @Test
  public void testGetPreciserScope_nonCallNode_returnsBlindScope() {
    Node nameNode = Node.newString(Token.NAME, "a");
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        nameNode, blindScope, true);
    assertSame(blindScope, result);
  }

  // Tests CALL node with more than 2 children returns blindScope
  @Test
  public void testGetPreciserScope_wrongChildCount_returnsBlindScope() {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node param1 = Node.newString(Token.NAME, "a");
    Node param2 = Node.newString(Token.NAME, "b");
    Node call = new Node(Token.CALL, callee, param1, param2);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);
    assertSame(blindScope, result);
  }

  // Tests callee is not a goog method returns blindScope
  @Test
  public void testGetPreciserScope_nonGoogMethod_returnsBlindScope() {
    Node other = Node.newString(Token.NAME, "other");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node callee = new Node(Token.GETPROP, other, prop);
    Node param = Node.newString(Token.NAME, "a");
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("a", registry.getNativeType(JSTypeNative.ALL_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  // Tests unknown goog method returns blindScope
  @Test
  public void testGetPreciserScope_unknownGoogMethod_returnsBlindScope() {
    Node condition = createGoogCall("unknownMethod", "a");
    FlowScope scope = createScopeWithVar("a", registry.getNativeType(JSTypeNative.ALL_TYPE));

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, true);
    assertSame(scope, result);
  }

  // Tests goog.isBoolean when outcome is false
  @Test
  public void testIsBoolean_falseOutcome_removesBoolean() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isBoolean", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isNumber when outcome is false
  @Test
  public void testIsNumber_falseOutcome_removesNumber() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isNumber", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests goog.isFunction when outcome is false
  @Test
  public void testIsFunction_falseOutcome_removesFunction() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FlowScope scope = createScopeWithVar("a", union);
    Node condition = createGoogCall("isFunction", "a");

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        condition, scope, false);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a").getType());
  }

  // Tests CALL node with 0 arguments (1 child) returns blindScope
  @Test
  public void testGetPreciserScope_noArguments_returnsBlindScope() {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node call = new Node(Token.CALL, callee);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, blindScope, true);
    assertSame(blindScope, result);
  }

  // Tests CALL node with callee as a plain NAME node returns blindScope
  @Test
  public void testGetPreciserScope_calleeIsNameNode_returnsBlindScope() {
    Node callee = Node.newString(Token.NAME, "isDef");
    Node param = Node.newString(Token.NAME, "a");
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("a", registry.getNativeType(JSTypeNative.ALL_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }

  // Tests goog method call with a qualified name (GETPROP) argument
  @Test
  public void testIsDef_qualifiedNameProp_trueOutcome_removesUndefined() {
    JSType stringOrUndefined = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    FlowScope scope = createScopeWithVar("a.b", stringOrUndefined);

    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.STRING, "b");
    Node getprop = new Node(Token.GETPROP, a, b);

    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node call = new Node(Token.CALL, callee, getprop);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);

    assertNotNull(result.getSlot("a.b"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        result.getSlot("a.b").getType());
  }

  // Tests goog method call with complex non-qualified-name expression argument
  @Test
  public void testIsDef_nonQualifiedNameParam_returnsBlindScope() {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, "isDef");
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node param = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newNumber(1));
    Node call = new Node(Token.CALL, callee, param);

    FlowScope scope = createScopeWithVar("a", registry.getNativeType(JSTypeNative.ALL_TYPE));
    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        call, scope, true);
    assertSame(scope, result);
  }
}