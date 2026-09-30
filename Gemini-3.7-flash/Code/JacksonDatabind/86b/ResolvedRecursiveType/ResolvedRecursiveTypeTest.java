package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class ResolvedRecursiveTypeTest {

    // Tests constructor initialization and default unresolved state
    @Test
    public void testConstructor_validClassAndBindings_initializesUnresolvedState() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertNull(type.getSelfReferencedType());
        assertFalse(type.isContainerType());
    }

    // Tests setReference setting the referenced type successfully
    @Test
    public void testSetReference_validJavaType_setsSelfReferencedType() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);
        assertSame(refType, type.getSelfReferencedType());
    }

    // Tests exception thrown when setReference is called more than once
    @Test(expected = IllegalStateException.class)
    public void testSetReference_calledTwice_throwsIllegalStateException() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);
        type.setReference(refType);
    }

    // Tests toString format when reference is unresolved
    @Test
    public void testToString_unresolvedReference_returnsUnresolvedRepresentation() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertEquals("[recursive type; UNRESOLVED]", type.toString());
    }

    // Tests toString format when reference is resolved
    @Test
    public void testToString_resolvedReference_returnsRawClassName() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);
        assertEquals("[recursive type; java.lang.String]", type.toString());
    }

    // Tests getGenericSignature delegates to referenced type
    @Test
    public void testGetGenericSignature_resolvedReference_delegatesToReferencedType() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertEquals(refType.getGenericSignature(new StringBuilder()).toString(), result.toString());
    }

    // Tests getErasedSignature delegates to referenced type
    @Test
    public void testGetErasedSignature_resolvedReference_delegatesToReferencedType() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertEquals(refType.getErasedSignature(new StringBuilder()).toString(), result.toString());
    }

    // Tests equals when comparing same instance
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertTrue(type.equals(type));
    }

    // Tests equals when comparing with null
    @Test
    public void testEquals_nullObject_returnsFalse() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(type.equals(null));
    }

    // Tests equals when reference is unresolved
    @Test
    public void testEquals_unresolvedReference_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        assertFalse(type1.equals(type2));
    }

    // Tests equals with different class type
    @Test
    public void testEquals_differentObjectType_returnsFalse() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type.setReference(refType);
        assertFalse(type.equals("not-a-type"));
    }

    // Tests equals when both instances resolve to the same type
    @Test
    public void testEquals_sameReferencedType_returnsTrue() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refType = SimpleType.constructUnsafe(String.class);
        type1.setReference(refType);
        type2.setReference(refType);

        assertTrue(type1.equals(type2));
    }

    // Tests equals when instances resolve to different types
    @Test
    public void testEquals_differentReferencedType_returnsFalse() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        type1.setReference(SimpleType.constructUnsafe(String.class));
        type2.setReference(SimpleType.constructUnsafe(Integer.class));

        assertFalse(type1.equals(type2));
    }

    // Tests fluent modifier methods returning the same instance (no-op behavior)
    @Test
    public void testWithMethods_variousModifiers_returnSameInstance() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType dummyType = SimpleType.constructUnsafe(Integer.class);

        assertSame(type, type.withContentType(dummyType));
        assertSame(type, type.withTypeHandler("handler"));
        assertSame(type, type.withContentTypeHandler("contentHandler"));
        assertSame(type, type.withValueHandler("valHandler"));
        assertSame(type, type.withContentValueHandler("contentValHandler"));
        assertSame(type, type.withStaticTyping());
        assertSame(type, type._narrow(String.class));
    }

    // Tests refine method returns null
    @Test
    public void testRefine_validArguments_returnsNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull(refined);
    }
}