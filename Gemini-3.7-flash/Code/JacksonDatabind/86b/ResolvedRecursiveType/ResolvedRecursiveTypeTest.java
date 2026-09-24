package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResolvedRecursiveTypeTest {

    // Tests initial state and getSelfReferencedType when unresolved
    @Test
    public void testGetSelfReferencedType_initiallyNull_returnsNull() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertNull(recursiveType.getSelfReferencedType());
    }

    // Tests setReference and getSelfReferencedType on normal resolution
    @Test
    public void testSetReference_validReference_setsReferencedType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        JavaType refType = SimpleType.constructUnsafe(String.class);

        recursiveType.setReference(refType);

        assertSame(refType, recursiveType.getSelfReferencedType());
    }

    // Tests setReference called multiple times throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testSetReference_calledTwice_throwsIllegalStateException() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        JavaType refType1 = SimpleType.constructUnsafe(String.class);
        JavaType refType2 = SimpleType.constructUnsafe(Integer.class);

        recursiveType.setReference(refType1);
        recursiveType.setReference(refType2);
    }

    // Tests getGenericSignature delegation to referenced type
    @Test
    public void testGetGenericSignature_resolvedType_delegatesToReferencedType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, bindings);
        JavaType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getGenericSignature(sb);

        assertNotNull(result);
        assertEquals(refType.getGenericSignature(), result.toString());
    }

    // Tests getErasedSignature delegation to referenced type
    @Test
    public void testGetErasedSignature_resolvedType_delegatesToReferencedType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, bindings);
        JavaType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getErasedSignature(sb);

        assertNotNull(result);
        assertEquals(refType.getErasedSignature(), result.toString());
    }

    // Tests withContentType returning this instance
    @Test
    public void testWithContentType_anyType_returnsSameInstance() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        JavaType dummyType = SimpleType.constructUnsafe(String.class);

        assertSame(recursiveType, recursiveType.withContentType(dummyType));
    }

    // Tests withTypeHandler, withContentTypeHandler, withValueHandler, withContentValueHandler, withStaticTyping
    @Test
    public void testWithHandlersAndTyping_variousInputs_returnsSameInstance() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        Object handler = new Object();

        assertSame(recursiveType, recursiveType.withTypeHandler(handler));
        assertSame(recursiveType, recursiveType.withContentTypeHandler(handler));
        assertSame(recursiveType, recursiveType.withValueHandler(handler));
        assertSame(recursiveType, recursiveType.withContentValueHandler(handler));
        assertSame(recursiveType, recursiveType.withStaticTyping());
    }

    // Tests deprecated _narrow returning this instance
    @SuppressWarnings("deprecation")
    @Test
    public void testNarrow_anySubclass_returnsSameInstance() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertSame(recursiveType, recursiveType._narrow(String.class));
    }

    // Tests refine returning null
    @Test
    public void testRefine_variousInputs_returnsNull() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertNull(recursiveType.refine(String.class, bindings, null, null));
    }

    // Tests isContainerType returning false
    @Test
    public void testIsContainerType_always_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertFalse(recursiveType.isContainerType());
    }

    // Tests toString when unresolved
    @Test
    public void testToString_unresolved_containsUnresolvedTag() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertEquals("[recursive type; UNRESOLVED", recursiveType.toString());
    }

    // Tests toString when resolved
    @Test
    public void testToString_resolved_containsReferencedClassName() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        JavaType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        assertEquals("[recursive type; java.lang.String", recursiveType.toString());
    }

    // Tests equals with same object reference
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertTrue(recursiveType.equals(recursiveType));
    }

    // Tests equals with null input
    @Test
    public void testEquals_null_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);

        assertFalse(recursiveType.equals(null));
    }

    // Tests equals when unresolved
    @Test
    public void testEquals_unresolvedThis_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, bindings);
        type2.setReference(SimpleType.constructUnsafe(String.class));

        assertFalse(type1.equals(type2));
    }

    // Tests equals with different class object
    @Test
    public void testEquals_differentClass_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, bindings);
        recursiveType.setReference(SimpleType.constructUnsafe(String.class));

        assertFalse(recursiveType.equals("some string"));
    }

    // Tests equals with resolved matching reference
    @Test
    public void testEquals_sameReferencedType_returnsTrue() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, bindings);
        JavaType refType = SimpleType.constructUnsafe(String.class);

        type1.setReference(refType);
        type2.setReference(refType);

        assertTrue(type1.equals(type2));
    }

    // Tests equals with resolved different reference
    @Test
    public void testEquals_differentReferencedType_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, bindings);

        type1.setReference(SimpleType.constructUnsafe(String.class));
        type2.setReference(SimpleType.constructUnsafe(Integer.class));

        assertFalse(type1.equals(type2));
    }
}