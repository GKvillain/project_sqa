package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;
import org.junit.Test;

public class ResolvedRecursiveTypeTest {

    // Helper: create an empty-bindings ResolvedRecursiveType for String
    private ResolvedRecursiveType createResolvedRecursiveType() {
        return new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
    }

    // Helper: obtain a JavaType for String
    private JavaType createStringJavaType() {
        return TypeFactory.defaultInstance().constructType(String.class);
    }

    // Tests constructor initial state: _referencedType == null, toString shows UNRESOLVED
    @Test
    public void testConstructorInitialState() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertNull("Self reference should be null initially", type.getSelfReferencedType());
        String str = type.toString();
        assertTrue("toString should contain UNRESOLVED", str.contains("UNRESOLVED"));
        assertTrue("toString should start with [recursive type;", str.contains("[recursive type;"));
    }

    // Tests setReference with valid reference and subsequent access
    @Test
    public void testSetReferenceAndGet() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        JavaType ref = createStringJavaType();
        type.setReference(ref);
        assertSame("getSelfReferencedType should return the set reference", ref, type.getSelfReferencedType());
        String str = type.toString();
        assertTrue("toString should contain the erased class name", str.contains("java.lang.String"));
        assertFalse("toString should not contain UNRESOLVED", str.contains("UNRESOLVED"));
    }

    // Tests that calling setReference twice throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testSetReferenceDoubleCallThrowsException() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        JavaType ref = createStringJavaType();
        type.setReference(ref);
        type.setReference(ref);
    }

    // Tests getGenericSignature when reference is resolved
    @Test
    public void testGetGenericSignatureResolved() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        type.setReference(createStringJavaType());
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertSame("getGenericSignature should return the same StringBuilder", sb, result);
        assertEquals("Generic signature of java.lang.String", "Ljava/lang/String;", sb.toString());
    }

    // Tests getGenericSignature when reference is unresolved -> NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetGenericSignatureUnresolvedThrowsNPE() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        type.getGenericSignature(new StringBuilder());
    }

    // Tests getErasedSignature when reference is resolved
    @Test
    public void testGetErasedSignatureResolved() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        type.setReference(createStringJavaType());
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertSame("getErasedSignature should return the same StringBuilder", sb, result);
        assertEquals("Erased signature of java.lang.String", "Ljava/lang/String;", sb.toString());
    }

    // Tests getErasedSignature when reference is unresolved -> NullPointerException
    @Test(expected = NullPointerException.class)
    public void testGetErasedSignatureUnresolvedThrowsNPE() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        type.getErasedSignature(new StringBuilder());
    }

    // Tests all "withXxx" methods (except withContentType) return this
    @Test
    public void testWithMethodsReturnThis() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertSame("withContentType should return this", type, type.withContentType(null));
        assertSame("withTypeHandler should return this", type, type.withTypeHandler(null));
        assertSame("withContentTypeHandler should return this", type, type.withContentTypeHandler(null));
        assertSame("withValueHandler should return this", type, type.withValueHandler(null));
        assertSame("withContentValueHandler should return this", type, type.withContentValueHandler(null));
        assertSame("withStaticTyping should return this", type, type.withStaticTyping());
    }

    // Tests isContainerType always returns false
    @Test
    public void testIsContainerTypeReturnsFalse() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertFalse("isContainerType should be false", type.isContainerType());
    }

    // Tests refine always returns null
    @Test
    public void testRefineReturnsNull() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType result = type.refine(String.class, bindings, null, null);
        assertNull("refine should return null", result);
    }

    // Tests deprecated _narrow returns this
    @Test
    public void testNarrowReturnsThis() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertSame("_narrow should return this", type, type._narrow(Object.class));
    }

    // Test equals with same instance
    @Test
    public void testEqualsSameInstanceReturnsTrue() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertTrue("equals(this) should be true", type.equals(type));
    }

    // Test equals with null
    @Test
    public void testEqualsNullReturnsFalse() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertFalse("equals(null) should be false", type.equals(null));
    }

    // Test equals when both _referencedType are null (unresolved)
    @Test
    public void testEqualsUnresolvedReturnsFalse() {
        ResolvedRecursiveType type1 = createResolvedRecursiveType();
        ResolvedRecursiveType type2 = createResolvedRecursiveType();
        assertFalse("Two unresolved types should not be equal", type1.equals(type2));
    }

    // Test equals when _referencedType are different
    @Test
    public void testEqualsResolvedDifferentTypesReturnsFalse() {
        ResolvedRecursiveType type1 = createResolvedRecursiveType();
        ResolvedRecursiveType type2 = createResolvedRecursiveType();
        type1.setReference(createStringJavaType());
        type2.setReference(TypeFactory.defaultInstance().constructType(Integer.class));
        assertFalse("Resolved with different referenced types should not be equal", type1.equals(type2));
    }

    // Test equals when _referencedType are logically equal
    @Test
    public void testEqualsResolvedSameTypesReturnsTrue() {
        ResolvedRecursiveType type1 = createResolvedRecursiveType();
        ResolvedRecursiveType type2 = createResolvedRecursiveType();
        JavaType ref1 = createStringJavaType();
        JavaType ref2 = TypeFactory.defaultInstance().constructType(String.class); // different instance, equal
        type1.setReference(ref1);
        type2.setReference(ref2);
        assertTrue("Resolved with equal referenced types should be equal", type1.equals(type2));
    }

    // ========== New tests for uncovered parts ==========

    // Test hashCode consistency with equals (required by contract)
    @Test
    public void testHashCodeConsistentWithEquals() {
        ResolvedRecursiveType type1 = createResolvedRecursiveType();
        ResolvedRecursiveType type2 = createResolvedRecursiveType();
        // Unresolved: not equal, so hashCodes may differ (not required to be equal)
        // Resolved with equal references must have equal hash codes
        JavaType ref = createStringJavaType();
        type1.setReference(ref);
        type2.setReference(ref); // same instance
        assertEquals("hashCode should be equal for equal types", type1.hashCode(), type2.hashCode());

        // Different references should generally produce different hash codes (not guaranteed but sensible)
        ResolvedRecursiveType type3 = createResolvedRecursiveType();
        type3.setReference(TypeFactory.defaultInstance().constructType(Integer.class));
        assertNotEquals("Hash codes for different types should differ (likely)", type1.hashCode(), type3.hashCode());
    }

    // Test isRecursiveType returns true
    @Test
    public void testIsRecursiveTypeReturnsTrue() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertTrue("isRecursiveType should return true", type.isRecursiveType());
    }

    // Test getContentType returns null
    @Test
    public void testGetContentTypeReturnsNull() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertNull("getContentType should be null", type.getContentType());
    }

    // Test isReferenceType returns false
    @Test
    public void testIsReferenceTypeReturnsFalse() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        assertFalse("isReferenceType should be false", type.isReferenceType());
    }

    // Test equals with a non-ResolvedRecursiveType object
    @Test
    public void testEqualsNonResolvedRecursiveTypeReturnsFalse() {
        ResolvedRecursiveType type = createResolvedRecursiveType();
        JavaType other = TypeFactory.defaultInstance().constructType(String.class);
        assertFalse("equals with a different JavaType subtype should be false", type.equals(other));
    }
}