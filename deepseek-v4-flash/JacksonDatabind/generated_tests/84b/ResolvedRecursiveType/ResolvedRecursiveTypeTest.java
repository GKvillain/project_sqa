package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

public class ResolvedRecursiveTypeTest {
    
    // Test constructor with valid erasedType and bindings
    @Test
    public void testConstructor_validInput_createsInstance() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // Test setReference with valid reference sets the field
    @Test
    public void testSetReference_validReference_setsReferencedType() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref = new ResolvedRecursiveType(Integer.class, bindings);
        type.setReference(ref);
        assertSame(ref, type.getSelfReferencedType());
    }

    // Test setReference called twice throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testSetReference_calledTwice_throwsIllegalStateException() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref1 = new ResolvedRecursiveType(Integer.class, bindings);
        JavaType ref2 = new ResolvedRecursiveType(Double.class, bindings);
        type.setReference(ref1);
        type.setReference(ref2);
    }

    // Test getSelfReferencedType after setReference returns the correct reference
    @Test
    public void testGetSelfReferencedType_afterSetReference_returnsReference() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType ref = new ResolvedRecursiveType(Integer.class, bindings);
        type.setReference(ref);
        assertEquals(ref, type.getSelfReferencedType());
    }

    // Test getSelfReferencedType when not set returns null
    @Test
    public void testGetSelfReferencedType_notSet_returnsNull() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertNull(type.getSelfReferencedType());
    }

    // Test getGenericSignature delegates to referenced type
    @Test
    public void testGetGenericSignature_referencedTypeSet_delegatesToReference() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType simpleType = TypeFactory.defaultInstance().constructType(String.class);
        ResolvedRecursiveType ref = new ResolvedRecursiveType(String.class, bindings);
        ref.setReference(simpleType);
        type.setReference(ref);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test getErasedSignature delegates to referenced type
    @Test
    public void testGetErasedSignature_referencedTypeSet_delegatesToReference() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType simpleType = TypeFactory.defaultInstance().constructType(String.class);
        ResolvedRecursiveType ref = new ResolvedRecursiveType(String.class, bindings);
        ref.setReference(simpleType);
        type.setReference(ref);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Test withContentType returns this
    @Test
    public void testWithContentType_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType contentType = TypeFactory.defaultInstance().constructType(Integer.class);
        assertSame(type, type.withContentType(contentType));
    }

    // Test withTypeHandler returns this
    @Test
    public void testWithTypeHandler_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertSame(type, type.withTypeHandler(new Object()));
    }

    // Test withContentTypeHandler returns this
    @Test
    public void testWithContentTypeHandler_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertSame(type, type.withContentTypeHandler(new Object()));
    }

    // Test withValueHandler returns this
    @Test
    public void testWithValueHandler_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertSame(type, type.withValueHandler(new Object()));
    }

    // Test withContentValueHandler returns this
    @Test
    public void testWithContentValueHandler_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertSame(type, type.withContentValueHandler(new Object()));
    }

    // Test withStaticTyping returns this
    @Test
    public void testWithStaticTyping_anyInput_returnsThis() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertSame(type, type.withStaticTyping());
    }

    // Test isContainerType returns false
    @Test
    public void testIsContainerType_always_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertFalse(type.isContainerType());
    }

    // Test toString with null referenced type returns UNRESOLVED string
    @Test
    public void testToString_unresolved_containsUNRESOLVED() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        String str = type.toString();
        assertTrue(str.contains("UNRESOLVED"));
        assertTrue(str.contains("[recursive type;"));
    }

    // Test toString with resolved referenced type includes class name
    @Test
    public void testToString_resolved_containsClassName() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        JavaType simpleType = TypeFactory.defaultInstance().constructType(String.class);
        ResolvedRecursiveType ref = new ResolvedRecursiveType(String.class, bindings);
        ref.setReference(simpleType);
        type.setReference(ref);
        String str = type.toString();
        assertTrue(str.contains("java.lang.String"));
    }

    // Test equals when same instance returns true
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertTrue(type.equals(type));
    }

    // Test equals when null returns false
    @Test
    public void testEquals_nullInput_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);
        assertFalse(type.equals(null));
    }

    // Test equals when referencedType is null returns false
    @Test
    public void testEquals_unresolvedReference_returnsFalse() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings);
        assertFalse(type1.equals(type2));
        assertFalse(type2.equals(type1));
    }

    // Test equals when both types have same referenced type returns true
    @Test
    public void testEquals_sameReferencedType_returnsTrue() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, bindings);
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, bindings);
        JavaType simpleType = TypeFactory.defaultInstance().constructType(String.class);
        ResolvedRecursiveType ref = new ResolvedRecursiveType(String.class, bindings);
        ref.setReference(simpleType);
        type1.setReference(ref);
        type2.setReference(ref);
        assertTrue(type1.equals(type2));
    }
}