package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class ResolvedRecursiveTypeTest {

    // Tests setReference and getSelfReferencedType with valid input
    @Test
    public void testSetReference_validType_setsAndReturnsReference() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertNull(recursiveType.getSelfReferencedType());

        SimpleType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        assertSame(refType, recursiveType.getSelfReferencedType());
    }

    // Tests exception path when calling setReference multiple times
    @Test(expected = IllegalStateException.class)
    public void testSetReference_calledTwice_throwsIllegalStateException() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        SimpleType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);
        recursiveType.setReference(refType);
    }

    // Tests toString when reference is unresolved
    @Test
    public void testToString_unresolvedReference_returnsUnresolvedString() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertEquals("[recursive type; UNRESOLVED]", recursiveType.toString());
    }

    // Tests toString when reference is resolved
    @Test
    public void testToString_resolvedReference_returnsResolvedClassName() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        SimpleType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);
        assertEquals("[recursive type; java.lang.String]", recursiveType.toString());
    }

    // Tests equals comparing same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertTrue(recursiveType.equals(recursiveType));
    }

    // Tests equals comparing with null
    @Test
    public void testEquals_nullObject_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(recursiveType.equals(null));
    }

    // Tests equals comparing with different type
    @Test
    public void testEquals_differentClass_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(recursiveType.equals("some string"));
    }

    // Tests equals when this instance is unresolved
    @Test
    public void testEquals_unresolvedReference_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(type1.equals(type2));
    }

    // Tests equals when both instances have equal referenced types
    @Test
    public void testEquals_equalReferencedTypes_returnsTrue() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        SimpleType refType1 = SimpleType.constructUnsafe(String.class);
        SimpleType refType2 = SimpleType.constructUnsafe(String.class);

        type1.setReference(refType1);
        type2.setReference(refType2);

        assertTrue(type1.equals(type2));
    }

    // Tests equals when instances have different referenced types
    @Test
    public void testEquals_differentReferencedTypes_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());

        type1.setReference(SimpleType.constructUnsafe(String.class));
        type2.setReference(SimpleType.constructUnsafe(Integer.class));

        assertFalse(type1.equals(type2));
    }

    // Tests getGenericSignature delegation to referenced type
    @Test
    public void testGetGenericSignature_resolvedReference_delegatesCorrectly() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        SimpleType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getGenericSignature(sb);
        assertNotNull(result);
        assertEquals(refType.getGenericSignature(), result.toString());
    }

    // Tests getErasedSignature delegation to referenced type
    @Test
    public void testGetErasedSignature_resolvedReference_delegatesCorrectly() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        SimpleType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getErasedSignature(sb);
        assertNotNull(result);
        assertEquals(refType.getErasedSignature(), result.toString());
    }

    // Tests isContainerType returns false
    @Test
    public void testIsContainerType_always_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(recursiveType.isContainerType());
    }

    // Tests refine returns null
    @Test
    public void testRefine_always_returnsNull() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertNull(recursiveType.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    // Tests all with* methods and _narrow return this instance
    @Test
    public void testWithMethodsAndNarrow_returnSameInstance() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        SimpleType dummyType = SimpleType.constructUnsafe(String.class);

        assertSame(recursiveType, recursiveType.withContentType(dummyType));
        assertSame(recursiveType, recursiveType.withTypeHandler("handler"));
        assertSame(recursiveType, recursiveType.withContentTypeHandler("contentHandler"));
        assertSame(recursiveType, recursiveType.withValueHandler("valueHandler"));
        assertSame(recursiveType, recursiveType.withContentValueHandler("contentValueHandler"));
        assertSame(recursiveType, recursiveType.withStaticTyping());
        assertSame(recursiveType, recursiveType._narrow(String.class));
    }
}