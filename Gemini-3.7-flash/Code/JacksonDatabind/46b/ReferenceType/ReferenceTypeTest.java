package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    private JavaType refTargetType;
    private ReferenceType referenceType;

    @Before
    public void setUp() {
        refTargetType = SimpleType.construct(String.class);
        referenceType = ReferenceType.construct(AtomicReference.class, refTargetType, null, null);
    }

    // Tests construction and basic properties
    @Test
    public void testConstruct_validArguments_createsReferenceType() {
        assertNotNull(referenceType);
        assertEquals(AtomicReference.class, referenceType.getRawClass());
        assertEquals(refTargetType, referenceType.getReferencedType());
        assertTrue(referenceType.isReferenceType());
        assertFalse(referenceType.useStaticType());
        assertNull(referenceType.getValueHandler());
        assertNull(referenceType.getTypeHandler());
    }

    // Tests contained type count is always 1
    @Test
    public void testContainedTypeCount_returnsOne() {
        assertEquals(1, referenceType.containedTypeCount());
    }

    // Tests contained type by index (valid index 0 and invalid indices)
    @Test
    public void testContainedType_variousIndices_returnsExpectedTypeOrNull() {
        assertEquals(refTargetType, referenceType.containedType(0));
        assertNull(referenceType.containedType(1));
        assertNull(referenceType.containedType(-1));
    }

    // Tests contained type name by index
    @Test
    public void testContainedTypeName_variousIndices_returnsTOrNull() {
        assertEquals("T", referenceType.containedTypeName(0));
        assertNull(referenceType.containedTypeName(1));
        assertNull(referenceType.containedTypeName(-1));
    }

    // Tests getParameterSource returns raw class
    @Test
    public void testGetParameterSource_returnsRawClass() {
        assertEquals(AtomicReference.class, referenceType.getParameterSource());
    }

    // Tests withTypeHandler when handler is identical vs different
    @Test
    public void testWithTypeHandler_sameAndDifferentHandler_returnsExpectedInstance() {
        Object handler = "customTypeHandler";
        ReferenceType updated = referenceType.withTypeHandler(handler);
        assertNotSame(referenceType, updated);
        assertEquals(handler, updated.getTypeHandler());

        ReferenceType sameHandler = updated.withTypeHandler(handler);
        assertSame(updated, sameHandler);
    }

    // Tests withValueHandler when handler is identical vs different
    @Test
    public void testWithValueHandler_sameAndDifferentHandler_returnsExpectedInstance() {
        Object handler = "customValueHandler";
        ReferenceType updated = referenceType.withValueHandler(handler);
        assertNotSame(referenceType, updated);
        assertEquals(handler, updated.getValueHandler());

        ReferenceType sameHandler = updated.withValueHandler(handler);
        assertSame(updated, sameHandler);
    }

    // Tests withContentTypeHandler when handler is identical vs different
    @Test
    public void testWithContentTypeHandler_sameAndDifferentHandler_returnsExpectedInstance() {
        Object handler = "contentTypeHandler";
        ReferenceType updated = referenceType.withContentTypeHandler(handler);
        assertNotSame(referenceType, updated);
        assertEquals(handler, updated.getReferencedType().getTypeHandler());

        ReferenceType sameHandler = updated.withContentTypeHandler(handler);
        assertSame(updated, sameHandler);
    }

    // Tests withContentValueHandler when handler is identical vs different
    @Test
    public void testWithContentValueHandler_sameAndDifferentHandler_returnsExpectedInstance() {
        Object handler = "contentValueHandler";
        ReferenceType updated = referenceType.withContentValueHandler(handler);
        assertNotSame(referenceType, updated);
        assertEquals(handler, updated.getReferencedType().getValueHandler());

        ReferenceType sameHandler = updated.withContentValueHandler(handler);
        assertSame(updated, sameHandler);
    }

    // Tests withStaticTyping when already static vs non-static
    @Test
    public void testWithStaticTyping_nonStaticAndStatic_returnsExpectedInstance() {
        ReferenceType staticRefType = referenceType.withStaticTyping();
        assertNotSame(referenceType, staticRefType);
        assertTrue(staticRefType.useStaticType());

        ReferenceType sameStatic = staticRefType.withStaticTyping();
        assertSame(staticRefType, sameStatic);
    }

    // Tests narrowing of raw class
    @Test
    public void testNarrow_subclass_returnsNarrowedReferenceType() {
        class CustomAtomicReference<T> extends AtomicReference<T> {
            private static final long serialVersionUID = 1L;
        }
        JavaType narrowed = referenceType._narrow(CustomAtomicReference.class);
        assertNotNull(narrowed);
        assertEquals(CustomAtomicReference.class, narrowed.getRawClass());
        assertEquals(refTargetType, ((ReferenceType) narrowed).getReferencedType());
    }

    // Tests generic signature formatting
    @Test
    public void testGetGenericSignature_validType_appendsCorrectSignature() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = referenceType.getGenericSignature(sb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", result.toString());
    }

    // Tests erased signature formatting
    @Test
    public void testGetErasedSignature_validType_appendsCorrectSignature() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = referenceType.getErasedSignature(sb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", result.toString());
    }

    // Tests buildCanonicalName and toString methods
    @Test
    public void testToStringAndCanonicalName_validType_returnsFormattedString() {
        String canonicalName = referenceType.buildCanonicalName();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String>", canonicalName);

        String str = referenceType.toString();
        assertTrue(str.startsWith("[reference type, class "));
        assertTrue(str.contains(canonicalName));
    }

    // Tests equals and identity comparisons
    @Test
    public void testEquals_variousObjects_returnsCorrectBoolean() {
        assertTrue(referenceType.equals(referenceType));
        assertFalse(referenceType.equals(null));
        assertFalse(referenceType.equals("not-a-type"));

        ReferenceType same = ReferenceType.construct(AtomicReference.class, refTargetType, null, null);
        assertTrue(referenceType.equals(same));

        JavaType intTargetType = SimpleType.construct(Integer.class);
        ReferenceType diffTarget = ReferenceType.construct(AtomicReference.class, intTargetType, null, null);
        assertFalse(referenceType.equals(diffTarget));

        ReferenceType diffClass = ReferenceType.construct(Object.class, refTargetType, null, null);
        assertFalse(referenceType.equals(diffClass));
    }

    // Tests construct overload with 2 arguments
    @Test
    public void testConstruct_twoArguments_createsReferenceType() {
        ReferenceType twoArgRefType = ReferenceType.construct(AtomicReference.class, refTargetType);
        assertNotNull(twoArgRefType);
        assertEquals(AtomicReference.class, twoArgRefType.getRawClass());
        assertEquals(refTargetType, twoArgRefType.getReferencedType());
    }

    // Tests upgradeFrom method
    @Test
    public void testUpgradeFrom_validBaseType_createsReferenceType() {
        JavaType baseType = SimpleType.construct(AtomicReference.class);
        ReferenceType upgraded = ReferenceType.upgradeFrom(baseType, refTargetType);
        assertNotNull(upgraded);
        assertEquals(AtomicReference.class, upgraded.getRawClass());
        assertEquals(refTargetType, upgraded.getReferencedType());
    }

    // Tests getContentType and hasContentType methods
    @Test
    public void testGetContentTypeAndHasContentType() {
        assertTrue(referenceType.hasContentType());
        assertEquals(refTargetType, referenceType.getContentType());
    }

    // Tests withContentType method
    @Test
    public void testWithContentType_sameAndDifferentContentType() {
        assertSame(referenceType, referenceType.withContentType(refTargetType));

        JavaType intType = SimpleType.construct(Integer.class);
        ReferenceType updated = referenceType.withContentType(intType);
        assertNotSame(referenceType, updated);
        assertEquals(intType, updated.getContentType());
        assertEquals(intType, updated.getReferencedType());
    }

    // Tests isAnchorType and getAnchorType methods
    @Test
    public void testAnchorType() {
        assertTrue(referenceType.isAnchorType());
        assertNotNull(referenceType.getAnchorType());
        assertEquals(referenceType.getRawClass(), referenceType.getAnchorType().getRawClass());
    }

    // Tests hashCode consistency
    @Test
    public void testHashCode_consistentWithEquals() {
        ReferenceType same = ReferenceType.construct(AtomicReference.class, refTargetType, null, null);
        assertEquals(referenceType.hashCode(), same.hashCode());
    }
}