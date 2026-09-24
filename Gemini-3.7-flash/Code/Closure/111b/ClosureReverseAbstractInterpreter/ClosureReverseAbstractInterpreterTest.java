package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.javascript.jscomp.ClosureCodingConvention;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

public class ClosureReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    convention = new ClosureCodingConvention();
    interpreter = new ClosureReverseAbstractInterpreter(convention, registry);
  }

  private Node createCallNode(String methodName, String varName) {
    Node goog = Node.newString(Token.NAME, "goog");
    Node prop = Node.newString(Token.STRING, methodName);
    Node callee = new Node(Token.GETPROP, goog, prop);
    Node param = Node.newString(Token.NAME, varName);
    return new Node(Token.CALL, callee, param);
  }

  // Tests goog.isDef when outcome is true
  @Test
  public void testGetPreciserScope_googIsDef_outcomeTrue_removesUndefined() {
    Node call = createCallNode("isDef", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(VOID_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(STRING_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isDef when outcome is false
  @Test
  public void testGetPreciserScope_googIsDef_outcomeFalse_restrictsToUndefined() {
    Node call = createCallNode("isDef", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(VOID_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertEquals(registry.getNativeType(VOID_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isNull when outcome is true
  @Test
  public void testGetPreciserScope_googIsNull_outcomeTrue_restrictsToNull() {
    Node call = createCallNode("isNull", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(NULL_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isNull when outcome is false
  @Test
  public void testGetPreciserScope_googIsNull_outcomeFalse_removesNull() {
    Node call = createCallNode("isNull", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertEquals(registry.getNativeType(STRING_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isDefAndNotNull when outcome is true
  @Test
  public void testGetPreciserScope_googIsDefAndNotNull_outcomeTrue_removesNullAndUndefined() {
    Node call = createCallNode("isDefAndNotNull", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(STRING_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isDefAndNotNull when outcome is false
  @Test
  public void testGetPreciserScope_googIsDefAndNotNull_outcomeFalse_restrictsToNullOrUndefined() {
    Node call = createCallNode("isDefAndNotNull", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    JSType expected = registry.createUnionType(
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));
    assertEquals(expected, resultScope.getSlot("x").getType());
  }

  // Tests goog.isString when outcome is true
  @Test
  public void testGetPreciserScope_googIsString_outcomeTrue_restrictsToString() {
    Node call = createCallNode("isString", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NUMBER_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(STRING_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isBoolean when outcome is true
  @Test
  public void testGetPreciserScope_googIsBoolean_outcomeTrue_restrictsToBoolean() {
    Node call = createCallNode("isBoolean", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(BOOLEAN_TYPE),
        registry.getNativeType(NUMBER_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(BOOLEAN_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isNumber when outcome is true
  @Test
  public void testGetPreciserScope_googIsNumber_outcomeTrue_restrictsToNumber() {
    Node call = createCallNode("isNumber", "x");
    JSType unionType = registry.createUnionType(
        registry.getNativeType(BOOLEAN_TYPE),
        registry.getNativeType(NUMBER_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(NUMBER_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isFunction when outcome is true
  @Test
  public void testGetPreciserScope_googIsFunction_outcomeTrue_restrictsToFunction() {
    Node call = createCallNode("isFunction", "x");
    JSType functionType = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    JSType unionType = registry.createUnionType(
        functionType,
        registry.getNativeType(STRING_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(functionType, resultScope.getSlot("x").getType());
  }

  // Tests goog.isArray when outcome is true
  @Test
  public void testGetPreciserScope_googIsArray_outcomeTrue_restrictsToArray() {
    Node call = createCallNode("isArray", "x");
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType unionType = registry.createUnionType(
        arrayType,
        registry.getNativeType(STRING_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(arrayType, resultScope.getSlot("x").getType());
  }

  // Tests goog.isArray when outcome is false
  @Test
  public void testGetPreciserScope_googIsArray_outcomeFalse_removesArray() {
    Node call = createCallNode("isArray", "x");
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType unionType = registry.createUnionType(
        arrayType,
        registry.getNativeType(STRING_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    assertEquals(registry.getNativeType(STRING_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isArray with top/all type when outcome is true
  @Test
  public void testGetPreciserScope_googIsArray_topTypeOutcomeTrue_returnsTopType() {
    Node call = createCallNode("isArray", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", registry.getNativeType(ALL_TYPE));

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(ALL_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isArray when parameter type is null/untyped
  @Test
  public void testGetPreciserScope_googIsArray_nullType_infersArray() {
    Node call = createCallNode("isArray", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(resultScope.getSlot("x"));
    assertEquals(registry.getNativeType(ARRAY_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isObject when outcome is true
  @Test
  public void testGetPreciserScope_googIsObject_outcomeTrue_restrictsToObject() {
    Node call = createCallNode("isObject", "x");
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    JSType unionType = registry.createUnionType(
        objType,
        registry.getNativeType(NUMBER_TYPE));

    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", unionType);

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(objType, resultScope.getSlot("x").getType());
  }

  // Tests goog.isObject with top type when outcome is true
  @Test
  public void testGetPreciserScope_googIsObject_topTypeOutcomeTrue_returnsNoObjectType() {
    Node call = createCallNode("isObject", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", registry.getNativeType(ALL_TYPE));

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertEquals(registry.getNativeType(NO_OBJECT_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests goog.isObject with all type when outcome is false
  @Test
  public void testGetPreciserScope_googIsObject_allTypeOutcomeFalse_returnsPrimitiveAndNullVoid() {
    Node call = createCallNode("isObject", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", registry.getNativeType(ALL_TYPE));

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, false);
    JSType expected = registry.createUnionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(BOOLEAN_TYPE),
        registry.getNativeType(NULL_TYPE),
        registry.getNativeType(VOID_TYPE));
    assertEquals(expected, resultScope.getSlot("x").getType());
  }

  // Tests goog.isObject when parameter type is null/untyped
  @Test
  public void testGetPreciserScope_googIsObject_nullType_infersObject() {
    Node call = createCallNode("isObject", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertNotNull(resultScope.getSlot("x"));
    assertEquals(registry.getNativeType(OBJECT_TYPE), resultScope.getSlot("x").getType());
  }

  // Tests non-call node delegates to next interpreter
  @Test
  public void testGetPreciserScope_nonCallNode_delegatesToNext() {
    Node nameNode = Node.newString(Token.NAME, "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(nameNode, scope, true);
    assertSame(scope, resultScope);
  }

  // Tests unknown function call does not restrict
  @Test
  public void testGetPreciserScope_unknownFunctionName_returnsBlindScope() {
    Node call = createCallNode("unknownFunction", "x");
    FlowScope scope = new LinkedFlowScope.FlowScopeJoinOp(
        new SemanticReverseAbstractInterpreter(convention, registry).new BlankFlowScope());
    scope.inferSlotType("x", registry.getNativeType(STRING_TYPE));

    FlowScope resultScope = interpreter.getPreciserScopeKnowingConditionOutcome(call, scope, true);
    assertSame(scope, resultScope);
  }
}