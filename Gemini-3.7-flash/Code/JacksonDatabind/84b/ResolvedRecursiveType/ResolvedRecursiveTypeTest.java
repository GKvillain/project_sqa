package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ResolvedRecursiveTypeTest {

    // Tests initial state before reference is set
    @Test
    public void testGetSelfReferencedType_initiallyNull_returnsNull() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertNull(recursiveType.getSelfReferencedType());
    }

    // Tests setting reference successfully
    @Test
    public void testSetReference_validJavaType_setsReferencedType() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);

        recursiveType.setReference(refType);

        assertSame(refType, recursiveType.getSelfReferencedType());
    }

    // Tests exception path when setting reference more than once
    @Test(expected = IllegalStateException.class)
    public void testSetReference_calledTwice_throwsIllegalStateException() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);

        recursiveType.setReference(refType);
        recursiveType.setReference(refType);
    }

    // Tests getGenericSignature delegation to referenced type
    @Test
    public void testGetGenericSignature_withReferencedType_delegatesToReferencedType() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getGenericSignature(sb);

        assertNotNull(result);
        assertEquals("Ljava/lang/String;", result.toString());
    }

    // Tests getErasedSignature delegation to referenced type
    @Test
    public void testGetErasedSignature_withReferencedType_delegatesToReferencedType() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(Integer.class);
        recursiveType.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = recursiveType.getErasedSignature(sb);

        assertNotNull(result);
        assertEquals("Ljava/lang/Integer;", result.toString());
    }

    // Tests isContainerType always returns false
    @Test
    public void testIsContainerType_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse(recursiveType.isContainerType());
    }

    // Tests fluent modifier methods return this
    @Test
    public void testFluentModifierMethods_returnSameInstance() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType dummyType = SimpleType.constructUnsafe(String.class);

        assertSame(recursiveType, recursiveType.withContentType(dummyType));
        assertSame(recursiveType, recursiveType.withTypeHandler("handler"));
        assertSame(recursiveType, recursiveType.withContentTypeHandler("handler"));
        assertSame(recursiveType, recursiveType.withValueHandler("valueHandler"));
        assertSame(recursiveType, recursiveType.withContentValueHandler("contentValueHandler"));
        assertSame(recursiveType, recursiveType.withStaticTyping());
        assertSame(recursiveType, recursiveType._narrow(Object.class));
    }

    // Tests refine returns null
    @Test
    public void testRefine_returnsNull() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull(recursiveType.refine(Object.class, TypeBindings.emptyBindings(), null, null));
    }

    // Tests toString when reference is unresolved
    @Test
    public void testToString_unresolved_returnsUnresolvedRepresentation() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertEquals("[recursive type; UNRESOLVED]", recursiveType.toString());
    }

    // Tests toString when reference is resolved
    @Test
    public void testToString_resolved_returnsReferencedClassName() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        recursiveType.setReference(refType);

        assertEquals("[recursive type; java.lang.String]", recursiveType.toString());
    }

    // Tests equals with self instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertTrue(recursiveType.equals(recursiveType));
    }

    // Tests equals with null
    @Test
    public void testEquals_null_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse(recursiveType.equals(null));
    }

    // Tests equals when this reference is unresolved
    @Test
    public void testEquals_unresolvedThis_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        type2.setReference(SimpleType.constructUnsafe(String.class));

        assertFalse(type1.equals(type2));
    }

    // Tests equals with different class type
    @Test
    public void testEquals_differentClass_returnsFalse() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        recursiveType.setReference(SimpleType.constructUnsafe(String.class));

        assertFalse(recursiveType.equals("some string"));
    }

    // Tests equals with matching resolved references
    @Test
    public void testEquals_sameReferencedTypes_returnsTrue() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        type1.setReference(SimpleType.constructUnsafe(String.class));
        type2.setReference(SimpleType.constructUnsafe(String.class));

        assertTrue(type1.equals(type2));
    }

    // Tests equals with different referenced types
    @Test
    public void testEquals_differentReferencedTypes_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());

        type1.setReference(SimpleType.constructUnsafe(String.class));
        type2.setReference(SimpleType.constructUnsafe(Integer.class));

        assertFalse(type1.equals(type2));
    }
}