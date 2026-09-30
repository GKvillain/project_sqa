package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_STRING;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;
import com.google.javascript.rhino.jstype.UnknownType;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeValidatorTest {
    private AbstractCompiler compiler;
    private TypeValidator validator;
    private JSTypeRegistry registry;
    private Node dummyNode;

    @Before
    public void setUp() {
        compiler = new Compiler();
        registry = compiler.getTypeRegistry();
        validator = new TypeValidator(compiler);
        validator.setShouldReport(false);
        dummyNode = new Node(com.google.javascript.rhino.Token.SCRIPT);
    }

    private NodeTraversal createNodeTraversal(final boolean globalScope) {
        return new NodeTraversal(compiler, dummyNode) {
            @Override
            public String getSourceName() {
                return "test.js";
            }

            @Override
            public boolean inGlobalScope() {
                return globalScope;
            }

            @Override
            public JSError makeError(Node n, DiagnosticType type, String... args) {
                return JSError.make(getSourceName(), n, type, args);
            }
        };
    }

    private int getMismatchCount() {
        int count = 0;
        for (TypeValidator.TypeMismatch mismatch : validator.getMismatches()) {
            count++;
        }
        return count;
    }

    // Tests for expectObject
    @Test
    public void testExpectObject_objectType_returnsTrue() {
        NodeTraversal t = createNodeTraversal(true);
        JSType objectType = registry.getNativeType(OBJECT_TYPE);
        boolean result = validator.expectObject(t, dummyNode, objectType, "msg");
        assertTrue(result);
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectObject_nullType_returnsFalse() {
        NodeTraversal t = createNodeTraversal(true);
        JSType nullType = registry.getNativeType(NULL_TYPE);
        boolean result = validator.expectObject(t, dummyNode, nullType, "msg");
        assertFalse(result);
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for expectString
    @Test
    public void testExpectString_stringType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        validator.expectString(t, dummyNode, stringType, "msg");
        assertEquals(0, getMismatchCount());
    }

    // Tests for expectNumber
    @Test
    public void testExpectNumber_numberType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectNumber(t, dummyNode, numberType, "msg");
        assertEquals(0, getMismatchCount());
    }

    // Tests for expectBitwiseable
    @Test
    public void testExpectBitwiseable_numberType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectBitwiseable(t, dummyNode, numberType, "msg");
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectBitwiseable_objectType_issuesWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType objectType = registry.getNativeType(OBJECT_TYPE);
        validator.expectBitwiseable(t, dummyNode, objectType, "msg");
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for expectStringOrNumber
    @Test
    public void testExpectStringOrNumber_stringType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        validator.expectStringOrNumber(t, dummyNode, stringType, "msg");
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectStringOrNumber_numberType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectStringOrNumber(t, dummyNode, numberType, "msg");
        assertEquals(0, getMismatchCount());
    }

    // Tests for expectNotNullOrUndefined
    @Test
    public void testExpectNotNullOrUndefined_stringType_returnsTrue() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        boolean result = validator.expectNotNullOrUndefined(t, dummyNode, stringType, "msg", stringType);
        assertTrue(result);
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectNotNullOrUndefined_nullType_returnsFalse() {
        NodeTraversal t = createNodeTraversal(true);
        JSType nullType = registry.getNativeType(NULL_TYPE);
        boolean result = validator.expectNotNullOrUndefined(t, dummyNode, nullType, "msg",
                registry.getNativeType(OBJECT_TYPE));
        assertFalse(result);
        assertTrue(getMismatchCount() > 0);
    }

    @Test
    public void testExpectNotNullOrUndefined_getPropLocalScopeNullType_returnsTrue() {
        // Tests the edge case described in issue 109
        NodeTraversal t = createNodeTraversal(false); // not global scope
        Node getPropNode = new Node(com.google.javascript.rhino.Token.GETPROP);
        JSType nullType = registry.getNativeType(NULL_TYPE);
        boolean result = validator.expectNotNullOrUndefined(t, getPropNode, nullType, "msg",
                registry.getNativeType(OBJECT_TYPE));
        assertTrue(result);
        assertEquals(0, getMismatchCount());
    }

    // Tests for expectSwitchMatchesCase
    @Test
    public void testExpectSwitchMatchesCase_matchingTypes_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        validator.expectSwitchMatchesCase(t, dummyNode, stringType, stringType);
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectSwitchMatchesCase_nonMatchingTypes_issuesWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectSwitchMatchesCase(t, dummyNode, stringType, numberType);
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for expectCanAssignTo
    @Test
    public void testExpectCanAssignTo_assignableTypes_returnsTrue() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        boolean result = validator.expectCanAssignTo(t, dummyNode, stringType, stringType, "msg");
        assertTrue(result);
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectCanAssignTo_nonAssignableTypes_returnsFalse() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        boolean result = validator.expectCanAssignTo(t, dummyNode, stringType, numberType, "msg");
        assertFalse(result);
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for expectCanOverride
    @Test
    public void testExpectCanOverride_assignable_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        validator.expectCanOverride(t, dummyNode, stringType, stringType, "prop",
                registry.getNativeType(OBJECT_TYPE));
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectCanOverride_nonAssignable_issuesWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectCanOverride(t, dummyNode, stringType, numberType, "prop",
                registry.getNativeType(OBJECT_TYPE));
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for expectCanCast
    @Test
    public void testExpectCanCast_castableType_doesNotIssueWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        validator.expectCanCast(t, dummyNode, stringType, stringType);
        assertEquals(0, getMismatchCount());
    }

    @Test
    public void testExpectCanCast_nonCastableType_issuesWarning() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectCanCast(t, dummyNode, stringType, numberType);
        assertTrue(getMismatchCount() > 0);
    }

    // Tests for getMismatches and TypeMismatch fields
    @Test
    public void testGetMismatches_returnsListOfMismatches() {
        NodeTraversal t = createNodeTraversal(true);
        JSType stringType = registry.getNativeType(STRING_TYPE);
        JSType numberType = registry.getNativeType(NUMBER_TYPE);
        validator.expectCanAssignTo(t, dummyNode, stringType, numberType, "msg");
        Iterable<TypeValidator.TypeMismatch> mismatches = validator.getMismatches();
        assertNotNull(mismatches);
        assertTrue(mismatches.iterator().hasNext());
        TypeValidator.TypeMismatch mismatch = mismatches.iterator().next();
        // Verify the types are stored, order not guaranteed
        assertTrue((mismatch.typeA.isEquivalentTo(stringType) && mismatch.typeB.isEquivalentTo(numberType)) ||
                   (mismatch.typeA.isEquivalentTo(numberType) && mismatch.typeB.isEquivalentTo(stringType)));
    }
}