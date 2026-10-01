package com.google.javascript.jscomp.type;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.StaticSlot;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * JUnit 4 test class for ChainableReverseAbstractInterpreter.
 * Tests the chain, delegation, type refinement helper methods, and null handling.
 */
public class ChainableReverseAbstractInterpreterTest {

    // ----- Helper implementations -----

    private static class TestInterpreter extends ChainableReverseAbstractInterpreter {
        // Track whether firstPreciserScopeKnowingConditionOutcome was called
        boolean delegateCalled = false;

        public TestInterpreter(CodingConvention convention, JSTypeRegistry typeRegistry) {
            super(convention, typeRegistry);
        }

        @Override
        protected FlowScope getPreciserScopeKnowingConditionOutcome(
                Node condition, FlowScope blindScope, boolean outcome) {
            delegateCalled = true;
            return blindScope;
        }

        // Expose protected methods for testing
        @Override
        public JSType getNativeType(JSTypeNative typeId) {
            throw new UnsupportedOperationException("Not mocked, avoid calling");
        }
    }

    // Minimal FlowScope implementation for testing getTypeIfRefinable
    private static class SimpleScope implements FlowScope {
        private final Map<String, StaticSlot> slots = new HashMap<String, StaticSlot>();

        public void addSlot(String name, JSType type) {
            slots.put(name, new SimpleStaticSlot(type));
        }

        @Override
        public StaticSlot getSlot(String name) {
            return slots.get(name);
        }

        @Override
        public void inferSlotType(String name, JSType type) {
            // no-op for testing
        }

        @Override
        public void inferQualifiedSlot(Node node, String qualifiedName, JSType origType, JSType type) {
            // no-op for testing
        }

        // Other required FlowScope methods (unused in tests)
        @Override
        public void link() { throw new UnsupportedOperationException(); }

        @Override
        public void join() { throw new UnsupportedOperationException(); }

        @Override
        public FlowScope createChildFlowScope() { throw new UnsupportedOperationException(); }

        @Override
        public FlowScope createFlowScopeForNode(Node node) { throw new UnsupportedOperationException(); }

        @Override
        public boolean isSealed() { throw new UnsupportedOperationException(); }

        @Override
        public void seal() { throw new UnsupportedOperationException(); }
    }

    private static class SimpleStaticSlot implements StaticSlot {
        private final JSType type;

        SimpleStaticSlot(JSType type) { this.type = type; }

        @Override
        public JSType getType() { return type; }

        // Other required StaticSlot methods (unused)
        @Override
        public boolean isTypeInferred() { throw new UnsupportedOperationException(); }

        @Override
        public boolean isTypeDeclared() { throw new UnsupportedOperationException(); }

        @Override
        public String getName() { throw new UnsupportedOperationException(); }
    }

    // ----- Test cases -----

    // Tests constructor with null convention → should throw NullPointerException
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullConvention_throwsNullPointerException() {
        new TestInterpreter(null, null);
    }

    // Tests constructor with valid convention → instance created successfully
    @Test
    public void testConstructor_validConvention_createsInstance() {
        CodingConvention convention = new CodingConvention() {
            // Provide minimal default implementations
            @Override public boolean isConstant(String name) { return false; }
            @Override public boolean isConstantKey(String name) { return false; }
            @Override public boolean isValidEnumKey(String name) { return false; }
            @Override public boolean isExported(String name, boolean local) { return false; }
            @Override public boolean isPrivate(String name) { return false; }
            @Override public boolean isOptionalParameter(Node param) { return false; }
            @Override public boolean isVarArgsParameter(Node param) { return false; }
            @Override public void applySubclassing(com.google.javascript.rhino.jstype.FunctionType funcType, com.google.javascript.rhino.jstype.ObjectType instance, boolean isSubclass) { }
            @Override public String identifyTypeDeclarationNode(Node n) { return null; }
            @Override public com.google.javascript.rhino.JSTypeExpression getTypeDeclaration(Node n) { return null; }
            @Override public void applyForEach(com.google.javascript.jscomp.CodingConvention.Callback callback) { }
            @Override public boolean requiresSuperclassCall(com.google.javascript.rhino.Node function, com.google.javascript.rhino.Node superClass) { return false; }
            @Override public boolean requiresPrecedingLineSemicolons() { return false; }
            @Override public boolean isCollectionType(String name) { return false; }
            @Override public boolean isOverride(String name) { return false; }
            @Override public boolean isFunctionCall(com.google.javascript.jscomp.CodingConvention.SubclassType type) { return false; }
        };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        assertNotNull(interpreter);
        assertSame(interpreter, interpreter.getFirst());
    }

    // Tests append chain: returns last link and updates first link
    @Test
    public void testAppend_linksChainCorrectly() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter first = new TestInterpreter(convention, null);
        TestInterpreter second = new TestInterpreter(convention, null);
        TestInterpreter third = new TestInterpreter(convention, null);

        // Append second to first
        ChainableReverseAbstractInterpreter returned = first.append(second);
        assertSame(second, returned);
        assertSame(first, second.getFirst());

        // Append third to second
        returned = second.append(third);
        assertSame(third, returned);
        assertSame(first, third.getFirst());
    }

    // Tests getFirst returns the first link in a chain
    @Test
    public void testGetFirst_returnsFirstLink() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter first = new TestInterpreter(convention, null);
        TestInterpreter last = new TestInterpreter(convention, null);
        first.append(last);

        assertSame(first, first.getFirst());
        assertSame(first, last.getFirst());
    }

    // Tests firstPreciserScopeKnowingConditionOutcome delegates to the first link's method
    @Test
    public void testFirstPreciserScopeKnowingConditionOutcome_delegatesToFirstLink() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter first = new TestInterpreter(convention, null);
        TestInterpreter second = new TestInterpreter(convention, null);
        first.append(second);

        Node condition = new Node(Token.TRUE);
        FlowScope blindScope = new SimpleScope();

        FlowScope result = first.firstPreciserScopeKnowingConditionOutcome(condition, blindScope, true);
        // The first link's delegateCalled should be true
        assertTrue(first.delegateCalled);
        // The second link should NOT have been called
        assertFalse(second.delegateCalled);
        // The returned scope should be the blindScope
        assertSame(blindScope, result);
    }

    // Tests nextPreciserScopeKnowingConditionOutcome delegates to next link when available,
    // and returns blindScope when no next link
    @Test
    public void testNextPreciserScopeKnowingConditionOutcome_delegatesToNextLinkOrReturnsBlind() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter first = new TestInterpreter(convention, null);
        TestInterpreter second = new TestInterpreter(convention, null);
        first.append(second);

        Node condition = new Node(Token.TRUE);
        FlowScope blindScope = new SimpleScope();

        // From first, next link is second → should delegate
        FlowScope result1 = first.nextPreciserScopeKnowingConditionOutcome(condition, blindScope, false);
        assertTrue(second.delegateCalled);
        assertSame(blindScope, result1);

        // From second, no next link → should return blindScope
        // Reset delegate flag
        second.delegateCalled = false;
        FlowScope result2 = second.nextPreciserScopeKnowingConditionOutcome(condition, blindScope, true);
        assertFalse(second.delegateCalled); // next not called
        assertSame(blindScope, result2);
    }

    // Tests getTypeIfRefinable with a NAME node and no slot → returns null
    @Test
    public void testGetTypeIfRefinable_nameNodeNoSlot_returnsNull() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        Node node = Node.newString(Token.NAME, "x");
        FlowScope scope = new SimpleScope(); // no slot for "x"

        JSType result = interpreter.getTypeIfRefinable(node, scope);
        assertNull(result);
    }

    // Tests getTypeIfRefinable with a node that is not NAME or GETPROP → returns null
    @Test
    public void testGetTypeIfRefinable_nonRefinableNode_returnsNull() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        Node node = new Node(Token.NUMBER); // not a refinable type
        FlowScope scope = new SimpleScope();

        JSType result = interpreter.getTypeIfRefinable(node, scope);
        assertNull(result);
    }

    // Tests getRestrictedWithoutUndefined with null input → returns null
    @Test
    public void testGetRestrictedWithoutUndefined_nullInput_returnsNull() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        JSType result = interpreter.getRestrictedWithoutUndefined(null);
        assertNull(result);
    }

    // Tests getRestrictedWithoutNull with null input → returns null
    @Test
    public void testGetRestrictedWithoutNull_nullInput_returnsNull() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        JSType result = interpreter.getRestrictedWithoutNull(null);
        assertNull(result);
    }

    // Tests getRestrictedByTypeOfResult with null type and resultEqualsValue false → returns null
    @Test
    public void testGetRestrictedByTypeOfResult_nullTypeFalseResult_returnsNull() {
        CodingConvention convention = new CodingConvention() { /* minimal */ };
        TestInterpreter interpreter = new TestInterpreter(convention, null);
        JSType result = interpreter.getRestrictedByTypeOfResult(null, "number", false);
        assertNull(result);
    }
}