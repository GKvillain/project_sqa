package com.google.javascript.jscomp.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.GoogleCodingConvention;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Before;
import org.junit.Test;

public class SemanticReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private SemanticReverseAbstractInterpreter interpreter;
  private Scope scope;
  private FlowScope blindScope;

  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType objectType;
  private JSType unknownType;
  private JSType numberOrString;
  private JSType numberOrNull;
  private JSType numberOrUndefined;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    interpreter = new SemanticReverseAbstractInterpreter(
        new GoogleCodingConvention(), registry);
    scope = Scope.createGlobalScope(new Node(Token.BLOCK));
    blindScope = LinkedFlowScope.createEntryLattice(scope);

    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);

    numberOrString = registry.createUnionType(numberType, stringType);
    numberOrNull = registry.createUnionType(numberType, nullType);
    numberOrUndefined = registry.createUnionType(numberType, voidType);
  }

  private Node createVar(String name, JSType type) {
    Node node = Node.newString(Token.NAME, name);
    node.setJSType(type);
    scope.declare(name, node, type, null);
    blindScope.inferSlotType(name, type);
    return node;
  }

  private JSType getInferredType(FlowScope flowScope, String name) {
    StaticSlot<JSType> slot = flowScope.getSlot(name);
    return slot != null ? slot.getType() : null;
  }

  // Tests typeof equality outcome true
  @Test
  public void testGetPreciserScope_typeOfEq_refinesToTargetType() {
    Node varNode = createVar("a", numberOrString);
    Node typeOfNode = new Node(Token.TYPEOF, varNode);
    Node strNode = Node.newString("string");
    Node eqNode = new Node(Token.EQ, typeOfNode, strNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        eqNode, blindScope, true);

    assertEquals(stringType, getInferredType(result, "a"));
  }

  // Tests typeof equality outcome false
  @Test
  public void testGetPreciserScope_typeOfEqFalse_refinesWithoutTargetType() {
    Node varNode = createVar("a", numberOrString);
    Node typeOfNode = new Node(Token.TYPEOF, varNode);
    Node strNode = Node.newString("string");
    Node eqNode = new Node(Token.EQ, typeOfNode, strNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        eqNode, blindScope, false);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests typeof strict inequality outcome true
  @Test
  public void testGetPreciserScope_typeOfShne_refinesWithoutTargetType() {
    Node varNode = createVar("a", numberOrString);
    Node typeOfNode = new Node(Token.TYPEOF, varNode);
    Node strNode = Node.newString("string");
    Node shneNode = new Node(Token.SHNE, typeOfNode, strNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        shneNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests truthiness restriction on variable node
  @Test
  public void testGetPreciserScope_nameConditionTrue_restrictsNull() {
    Node varNode = createVar("a", numberOrNull);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        varNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests falsiness restriction on variable node
  @Test
  public void testGetPreciserScope_nameConditionFalse_restrictsToNull() {
    Node varNode = createVar("a", numberOrNull);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        varNode, blindScope, false);

    assertEquals(nullType, getInferredType(result, "a"));
  }

  // Tests logical AND with outcome true
  @Test
  public void testGetPreciserScope_andOutcomeTrue_restrictsBothOperands() {
    Node left = createVar("a", numberOrNull);
    Node right = createVar("b", numberOrNull);
    Node andNode = new Node(Token.AND, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        andNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
    assertEquals(numberType, getInferredType(result, "b"));
  }

  // Tests logical AND with outcome false and short-circuiting
  @Test
  public void testGetPreciserScope_andOutcomeFalse_refinesCommonVariable() {
    Node left = createVar("a", numberOrNull);
    Node right = createVar("a", numberOrNull);
    Node andNode = new Node(Token.AND, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        andNode, blindScope, false);

    assertEquals(nullType, getInferredType(result, "a"));
  }

  // Tests logical OR with outcome false
  @Test
  public void testGetPreciserScope_orOutcomeFalse_restrictsBothOperands() {
    Node left = createVar("a", numberOrNull);
    Node right = createVar("b", numberOrNull);
    Node orNode = new Node(Token.OR, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        orNode, blindScope, false);

    assertEquals(nullType, getInferredType(result, "a"));
    assertEquals(nullType, getInferredType(result, "b"));
  }

  // Tests logical OR with outcome true
  @Test
  public void testGetPreciserScope_orOutcomeTrue_refinesCommonVariable() {
    Node left = createVar("a", numberOrNull);
    Node right = createVar("a", numberOrNull);
    Node orNode = new Node(Token.OR, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        orNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests strict equality between two refinable variables
  @Test
  public void testGetPreciserScope_sheqOutcomeTrue_restrictsToSubtype() {
    Node left = createVar("a", numberOrString);
    Node right = createVar("b", numberType);
    Node sheqNode = new Node(Token.SHEQ, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        sheqNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
    assertEquals(numberType, getInferredType(result, "b"));
  }

  // Tests strict inequality outcome false
  @Test
  public void testGetPreciserScope_shneOutcomeFalse_actsLikeSheq() {
    Node left = createVar("a", numberOrString);
    Node right = createVar("b", numberType);
    Node shneNode = new Node(Token.SHNE, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        shneNode, blindScope, false);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests logical NOT operator inverting outcome
  @Test
  public void testGetPreciserScope_notOperator_invertsConditionOutcome() {
    Node varNode = createVar("a", numberOrNull);
    Node notNode = new Node(Token.NOT, varNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        notNode, blindScope, true);

    assertEquals(nullType, getInferredType(result, "a"));
  }

  // Tests inequality comparison removing undefined
  @Test
  public void testGetPreciserScope_inequalityComparison_restrictsUndefined() {
    Node left = createVar("a", numberOrUndefined);
    Node right = createVar("b", numberOrUndefined);
    Node ltNode = new Node(Token.LT, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        ltNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
    assertEquals(numberType, getInferredType(result, "b"));
  }

  // Tests instanceof operator outcome true
  @Test
  public void testGetPreciserScope_instanceOfTrue_restrictsToConstructorInstance() {
    FunctionType constructorType = registry.getNativeFunctionType(
        JSTypeNative.OBJECT_FUNCTION_TYPE);
    JSType unionType = registry.createUnionType(numberType, objectType);

    Node left = createVar("a", unionType);
    Node right = Node.newString(Token.NAME, "Object");
    right.setJSType(constructorType);
    Node instanceOfNode = new Node(Token.INSTANCEOF, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        instanceOfNode, blindScope, true);

    assertEquals(objectType, getInferredType(result, "a"));
  }

  // Tests instanceof operator outcome false
  @Test
  public void testGetPreciserScope_instanceOfFalse_restrictsAwayFromConstructorInstance() {
    FunctionType constructorType = registry.getNativeFunctionType(
        JSTypeNative.OBJECT_FUNCTION_TYPE);
    JSType unionType = registry.createUnionType(numberType, objectType);

    Node left = createVar("a", unionType);
    Node right = Node.newString(Token.NAME, "Object");
    right.setJSType(constructorType);
    Node instanceOfNode = new Node(Token.INSTANCEOF, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        instanceOfNode, blindScope, false);

    assertEquals(numberType, getInferredType(result, "a"));
  }

  // Tests 'in' operator inferring unknown property slot on object
  @Test
  public void testGetPreciserScope_inOperator_infersQualifiedProperty() {
    Node objectNode = createVar("obj", objectType);
    Node propNode = Node.newString("foo");
    Node inNode = new Node(Token.IN, propNode, objectNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        inNode, blindScope, true);

    assertNotNull(result.getSlot("obj.foo"));
    assertEquals(unknownType, getInferredType(result, "obj.foo"));
  }

  // Tests assignment condition outcome true
  @Test
  public void testGetPreciserScope_assignCondition_refinesAssignedValue() {
    Node left = createVar("a", numberOrNull);
    Node right = createVar("b", numberOrNull);
    Node assignNode = new Node(Token.ASSIGN, left, right);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        assignNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "a"));
    assertEquals(numberType, getInferredType(result, "b"));
  }

  // Tests switch CASE condition outcome true
  @Test
  public void testGetPreciserScope_switchCaseTrue_restrictsCondition() {
    Node switchVal = createVar("x", numberOrString);
    Node switchBlock = new Node(Token.SWITCH, switchVal);
    Node caseVal = createVar("y", numberType);
    Node caseNode = new Node(Token.CASE, caseVal);
    switchBlock.addChildToBack(caseNode);

    FlowScope result = interpreter.getPreciserScopeKnowingConditionOutcome(
        caseNode, blindScope, true);

    assertEquals(numberType, getInferredType(result, "x"));
  }
}