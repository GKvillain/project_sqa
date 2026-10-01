package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.U2U_CONSTRUCTOR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.SimpleSlot;
import com.google.javascript.rhino.jstype.StaticSlot;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ChainableReverseAbstractInterpreterTest {

  private JSTypeRegistry registry;
  private CodingConvention convention;
  private TestInterpreter interpreter;

  private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
    private FlowScope returnedScope;

    TestInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry) {
      super(convention, typeRegistry);
    }

    TestInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry, FlowScope returnedScope) {
      super(convention, typeRegistry);
      this.returnedScope = returnedScope;
    }

    @Override
    public FlowScope getPreciserScopeKnowingConditionOutcome(
        Node condition, FlowScope blindScope, boolean outcome) {
      return returnedScope != null ? returnedScope : blindScope;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    convention = new DefaultCodingConvention();
    interpreter = new TestInterpreter(convention, registry);
  }

  private FlowScope createMockFlowScope(final Map<String, StaticSlot<JSType>> slots,
                                        final Map<String, JSType> inferredSlots) {
    return (FlowScope) Proxy.newProxyInstance(
        FlowScope.class.getClassLoader(),
        new Class<?>[] { FlowScope.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getSlot".equals(name)) {
              String varName = (String) args[0];
              return slots != null ? slots.get(varName) : null;
            } else if ("inferSlotType".equals(name)) {
              if (inferredSlots != null) {
                inferredSlots.put((String) args[0], (JSType) args[1]);
              }
              return null;
            } else if ("inferQualifiedSlot".equals(name)) {
              if (inferredSlots != null) {
                inferredSlots.put((String) args[1], (JSType) args[3]);
              }
              return null;
            }
            return null;
          }
        });
  }

  // Tests appending links to chain and verifying first link consistency
  @Test
  public void testAppend_validChain_linksMaintainedCorrectly() {
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter third = new TestInterpreter(convention, registry);

    ChainableReverseAbstractInterpreter resultSecond = first.append(second);
    ChainableReverseAbstractInterpreter resultThird = second.append(third);

    assertSame(second, resultSecond);
    assertSame(third, resultThird);
    assertSame(first, first.getFirst());
    assertSame(first, second.getFirst());
    assertSame(first, third.getFirst());
  }

  // Tests appending link that already has nextLink throws exception
  @Test(expected = IllegalArgumentException.class)
  public void testAppend_linkAlreadyAppended_throwsException() {
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry);
    TestInterpreter third = new TestInterpreter(convention, registry);

    first.append(second);
    third.append(second);
  }

  // Tests firstPreciserScopeKnowingConditionOutcome delegates to first link
  @Test
  public void testFirstPreciserScopeKnowingConditionOutcome_delegatesToFirst() {
    FlowScope scope = createMockFlowScope(null, null);
    Node cond = new Node(Token.TRUE);
    FlowScope result = interpreter.firstPreciserScopeKnowingConditionOutcome(cond, scope, true);
    assertSame(scope, result);
  }

  // Tests nextPreciserScopeKnowingConditionOutcome returns blind scope when no next link
  @Test
  public void testNextPreciserScopeKnowingConditionOutcome_noNextLink_returnsBlindScope() {
    FlowScope scope = createMockFlowScope(null, null);
    Node cond = new Node(Token.TRUE);
    FlowScope result = interpreter.nextPreciserScopeKnowingConditionOutcome(cond, scope, true);
    assertSame(scope, result);
  }

  // Tests getTypeIfRefinable for NAME node found in scope
  @Test
  public void testGetTypeIfRefinable_nameInScope_returnsSlotType() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    slots.put("x", new SimpleSlot("x", numType, false));
    FlowScope scope = createMockFlowScope(slots, null);

    Node nameNode = Node.newString(Token.NAME, "x");
    JSType result = interpreter.getTypeIfRefinable(nameNode, scope);
    assertSame(numType, result);
  }

  // Tests getTypeIfRefinable for NAME node not in scope returns null
  @Test
  public void testGetTypeIfRefinable_nameNotInScope_returnsNull() {
    FlowScope scope = createMockFlowScope(new HashMap<String, StaticSlot<JSType>>(), null);
    Node nameNode = Node.newString(Token.NAME, "y");
    JSType result = interpreter.getTypeIfRefinable(nameNode, scope);
    assertNull(result);
  }

  // Tests getTypeIfRefinable for GETPROP node
  @Test
  public void testGetTypeIfRefinable_qualifiedGetProp_returnsType() {
    Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    JSType strType = registry.getNativeType(STRING_TYPE);
    slots.put("a.b", new SimpleSlot("a.b", strType, false));
    FlowScope scope = createMockFlowScope(slots, null);

    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node getprop = new Node(Token.GETPROP, a, b);

    JSType result = interpreter.getTypeIfRefinable(getprop, scope);
    assertSame(strType, result);
  }

  // Tests declareNameInScope for NAME node
  @Test
  public void testDeclareNameInScope_nameNode_infersSlotType() {
    Map<String, JSType> inferred = new HashMap<String, JSType>();
    FlowScope scope = createMockFlowScope(null, inferred);
    Node nameNode = Node.newString(Token.NAME, "x");
    JSType strType = registry.getNativeType(STRING_TYPE);

    interpreter.declareNameInScope(scope, nameNode, strType);
    assertSame(strType, inferred.get("x"));
  }

  // Tests declareNameInScope for GETPROP node
  @Test
  public void testDeclareNameInScope_getPropNode_infersQualifiedSlot() {
    Map<String, JSType> inferred = new HashMap<String, JSType>();
    FlowScope scope = createMockFlowScope(null, inferred);
    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node getprop = new Node(Token.GETPROP, a, b);
    JSType boolType = registry.getNativeType(BOOLEAN_TYPE);

    interpreter.declareNameInScope(scope, getprop, boolType);
    assertSame(boolType, inferred.get("a.b"));
  }

  // Tests declareNameInScope for THIS node (Defects4J Closure 19 target regression)
  @Test
  public void testDeclareNameInScope_thisNode_handledWithoutException() {
    FlowScope scope = createMockFlowScope(null, null);
    Node thisNode = new Node(Token.THIS);
    JSType objType = registry.getNativeType(OBJECT_TYPE);

    interpreter.declareNameInScope(scope, thisNode, objType);
  }

  // Tests declareNameInScope for unsupported node throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testDeclareNameInScope_unsupportedNode_throwsException() {
    FlowScope scope = createMockFlowScope(null, null);
    Node numberNode = new Node(Token.NUMBER);
    interpreter.declareNameInScope(scope, numberNode, registry.getNativeType(NUMBER_TYPE));
  }

  // Tests getRestrictedWithoutUndefined with void type
  @Test
  public void testGetRestrictedWithoutUndefined_voidType_returnsNull() {
    JSType voidType = registry.getNativeType(VOID_TYPE);
    JSType result = interpreter.getRestrictedWithoutUndefined(voidType);
    assertNull(result);
  }

  // Tests getRestrictedWithoutUndefined with union type containing undefined
  @Test
  public void testGetRestrictedWithoutUndefined_unionWithUndefined_removesUndefined() {
    JSType union = registry.createUnionType(NUMBER_TYPE, VOID_TYPE);
    JSType result = interpreter.getRestrictedWithoutUndefined(union);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result);
  }

  // Tests getRestrictedWithoutUndefined with null input
  @Test
  public void testGetRestrictedWithoutUndefined_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutUndefined(null));
  }

  // Tests getRestrictedWithoutNull with null type
  @Test
  public void testGetRestrictedWithoutNull_nullType_returnsNull() {
    JSType nullType = registry.getNativeType(NULL_TYPE);
    JSType result = interpreter.getRestrictedWithoutNull(nullType);
    assertNull(result);
  }

  // Tests getRestrictedWithoutNull with union type containing null
  @Test
  public void testGetRestrictedWithoutNull_unionWithNull_removesNull() {
    JSType union = registry.createUnionType(STRING_TYPE, NULL_TYPE);
    JSType result = interpreter.getRestrictedWithoutNull(union);
    assertEquals(registry.getNativeType(STRING_TYPE), result);
  }

  // Tests getRestrictedByTypeOfResult with matching typeof "number"
  @Test
  public void testGetRestrictedByTypeOfResult_numberTypeMatchesNumber_returnsNumber() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(numType, "number", true);
    assertEquals(numType, result);
  }

  // Tests getRestrictedByTypeOfResult with non-matching typeof "string" on number
  @Test
  public void testGetRestrictedByTypeOfResult_numberTypeMatchesString_returnsNull() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(numType, "string", true);
    assertNull(result);
  }

  // Tests getRestrictedByTypeOfResult with typeof != "number" on union type
  @Test
  public void testGetRestrictedByTypeOfResult_unionNotEqualsNumber_filtersNumber() {
    JSType union = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(union, "number", false);
    assertEquals(registry.getNativeType(STRING_TYPE), result);
  }

  // Tests getRestrictedByTypeOfResult with null type input and outcome true
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeEqualsBoolean_returnsBoolean() {
    JSType result = interpreter.getRestrictedByTypeOfResult(null, "boolean", true);
    assertEquals(registry.getNativeType(BOOLEAN_TYPE), result);
  }

  // Tests getRestrictedByTypeOfResult with null type input and outcome false
  @Test
  public void testGetRestrictedByTypeOfResult_nullTypeOutcomeFalse_returnsNull() {
    JSType result = interpreter.getRestrictedByTypeOfResult(null, "boolean", false);
    assertNull(result);
  }

  // Tests nextPreciserScopeKnowingConditionOutcome delegates to nextLink when present
  @Test
  public void testNextPreciserScopeKnowingConditionOutcome_hasNextLink_delegatesToNext() {
    FlowScope initialScope = createMockFlowScope(null, null);
    FlowScope expectedScope = createMockFlowScope(null, null);
    TestInterpreter first = new TestInterpreter(convention, registry);
    TestInterpreter second = new TestInterpreter(convention, registry, expectedScope);
    first.append(second);

    Node cond = new Node(Token.TRUE);
    FlowScope result = first.nextPreciserScopeKnowingConditionOutcome(cond, initialScope, true);
    assertSame(expectedScope, result);
  }

  // Tests getTypeIfRefinable for node that is neither NAME nor GETPROP returns null
  @Test
  public void testGetTypeIfRefinable_unsupportedNode_returnsNull() {
    FlowScope scope = createMockFlowScope(null, null);
    Node numberNode = new Node(Token.NUMBER);
    assertNull(interpreter.getTypeIfRefinable(numberNode, scope));
  }

  // Tests getTypeIfRefinable for GETPROP node not present in scope returns null
  @Test
  public void testGetTypeIfRefinable_qualifiedGetPropNotInScope_returnsNull() {
    FlowScope scope = createMockFlowScope(new HashMap<String, StaticSlot<JSType>>(), null);
    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node getprop = new Node(Token.GETPROP, a, b);

    assertNull(interpreter.getTypeIfRefinable(getprop, scope));
  }

  // Tests getTypeIfRefinable for GETPROP node that is not a qualified name returns null
  @Test
  public void testGetTypeIfRefinable_nonQualifiedGetProp_returnsNull() {
    FlowScope scope = createMockFlowScope(null, null);
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    Node propNode = Node.newString(Token.NAME, "b");
    Node getprop = new Node(Token.GETPROP, callNode, propNode);

    assertNull(interpreter.getTypeIfRefinable(getprop, scope));
  }

  // Tests getRestrictedWithoutUndefined on non-void type returns same type
  @Test
  public void testGetRestrictedWithoutUndefined_nonVoidType_returnsOriginalType() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    JSType result = interpreter.getRestrictedWithoutUndefined(strType);
    assertEquals(strType, result);
  }

  // Tests getRestrictedWithoutNull with null input returns null
  @Test
  public void testGetRestrictedWithoutNull_nullInput_returnsNull() {
    assertNull(interpreter.getRestrictedWithoutNull(null));
  }

  // Tests getRestrictedWithoutNull on non-null type returns same type
  @Test
  public void testGetRestrictedWithoutNull_nonNullType_returnsOriginalType() {
    JSType numType = registry.getNativeType(NUMBER_TYPE);
    JSType result = interpreter.getRestrictedWithoutNull(numType);
    assertEquals(numType, result);
  }

  // Tests getRestrictedByTypeOfResult with typeof "undefined"
  @Test
  public void testGetRestrictedByTypeOfResult_undefinedType() {
    JSType union = registry.createUnionType(STRING_TYPE, VOID_TYPE);
    JSType resultTrue = interpreter.getRestrictedByTypeOfResult(union, "undefined", true);
    assertEquals(registry.getNativeType(VOID_TYPE), resultTrue);

    JSType resultFalse = interpreter.getRestrictedByTypeOfResult(union, "undefined", false);
    assertEquals(registry.getNativeType(STRING_TYPE), resultFalse);
  }

  // Tests getRestrictedByTypeOfResult with typeof "function"
  @Test
  public void testGetRestrictedByTypeOfResult_functionType() {
    JSType funcType = registry.getNativeType(U2U_CONSTRUCTOR_TYPE);
    JSType union = registry.createUnionType(NUMBER_TYPE, U2U_CONSTRUCTOR_TYPE);

    JSType resultTrue = interpreter.getRestrictedByTypeOfResult(union, "function", true);
    assertEquals(funcType, resultTrue);

    JSType resultFalse = interpreter.getRestrictedByTypeOfResult(union, "function", false);
    assertEquals(registry.getNativeType(NUMBER_TYPE), resultFalse);
  }

  // Tests getRestrictedByTypeOfResult with typeof "object"
  @Test
  public void testGetRestrictedByTypeOfResult_objectType() {
    JSType objType = registry.getNativeType(OBJECT_TYPE);
    JSType union = registry.createUnionType(STRING_TYPE, OBJECT_TYPE);

    JSType resultTrue = interpreter.getRestrictedByTypeOfResult(union, "object", true);
    assertEquals(objType, resultTrue);

    JSType resultFalse = interpreter.getRestrictedByTypeOfResult(union, "object", false);
    assertEquals(registry.getNativeType(STRING_TYPE), resultFalse);
  }

  // Tests getRestrictedByTypeOfResult with unrecognized typeof string
  @Test
  public void testGetRestrictedByTypeOfResult_unknownTypeOfString() {
    JSType strType = registry.getNativeType(STRING_TYPE);
    JSType resultTrue = interpreter.getRestrictedByTypeOfResult(strType, "invalid_type", true);
    assertNull(resultTrue);

    JSType resultFalse = interpreter.getRestrictedByTypeOfResult(strType, "invalid_type", false);
    assertEquals(strType, resultFalse);
  }

  // Tests getRestrictedByTypeOfResult with ALL_TYPE
  @Test
  public void testGetRestrictedByTypeOfResult_allType() {
    JSType allType = registry.getNativeType(ALL_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(allType, "string", true);
    assertEquals(registry.getNativeType(STRING_TYPE), result);
  }

  // Tests getRestrictedByTypeOfResult with UNKNOWN_TYPE
  @Test
  public void testGetRestrictedByTypeOfResult_unknownType() {
    JSType unknownType = registry.getNativeType(UNKNOWN_TYPE);
    JSType result = interpreter.getRestrictedByTypeOfResult(unknownType, "number", true);
    assertEquals(registry.getNativeType(NUMBER_TYPE), result);
  }
}