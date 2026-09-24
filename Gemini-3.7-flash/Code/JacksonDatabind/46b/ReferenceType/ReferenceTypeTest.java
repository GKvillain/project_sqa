package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    private JavaType stringType;
    private JavaType intType;
    private ReferenceType refType;

    @Before
    public void setUp() {
        stringType = SimpleType.constructUnsafe(String.class);
        intType = SimpleType.constructUnsafe(Integer.class);
        refType = ReferenceType.construct(AtomicReference.class, stringType, null, null);
    }

    // Tests getGenericSignature format to detect defect in closing bracket/semicolon
    @Test
    public void testGetGenericSignature_validReferenceType_returnsCorrectGenericSignature() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = refType.getGenericSignature(sb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", result.toString());
    }

    // Tests getErasedSignature
    @Test
    public void testGetErasedSignature_validReferenceType_returnsErasedSignature() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = refType.getErasedSignature(sb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", result.toString());
    }

    // Tests isReferenceType returns true
    @Test
    public void testIsReferenceType_always_returnsTrue() {
        assertTrue(refType.isReferenceType());
    }

    // Tests getReferencedType returns correct inner JavaType
    @Test
    public void testGetReferencedType_initializedType_returnsReferencedType() {
        assertSame(stringType, refType.getReferencedType());
    }

    // Tests containedTypeCount returns 1
    @Test
    public void testContainedTypeCount_always_returnsOne() {
        assertEquals(1, refType.containedTypeCount());
    }

    // Tests containedType with valid and invalid indices
    @Test
    public void testContainedType_boundaryIndices_returnsExpectedTypeOrNull() {
        assertSame(stringType, refType.containedType(0));
        assertNull(refType.containedType(1));
        assertNull(refType.containedType(-1));
    }

    // Tests containedTypeName with valid and invalid indices
    @Test
    public void testContainedTypeName_boundaryIndices_returnsExpectedNameOrNull() {
        assertEquals("T", refType.containedTypeName(0));
        assertNull(refType.containedTypeName(1));
        assertNull(refType.containedTypeName(-1));
    }

    // Tests getParameterSource returns the raw class
    @Test
    public void testGetParameterSource_always_returnsRawClass() {
        assertEquals(AtomicReference.class, refType.getParameterSource());
    }

    // Tests buildCanonicalName and toCanonical format
    @Test
    public void testBuildCanonicalName_validType_returnsCanonicalName() {
        String canonical = refType.toCanonical();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String>", canonical);
    }

    // Tests toString format
    @Test
    public void testToString_validType_returnsFormattedString() {
        String str = refType.toString();
        assertTrue(str.startsWith("[reference type, class "));
        assertTrue(str.contains("java.util.concurrent.atomic.AtomicReference<java.lang.String>"));
    }

    // Tests withTypeHandler with same and different handler
    @Test
    public void testWithTypeHandler_sameAndDifferentHandler_behavesCorrectly() {
        Object handler = "customTypeHandler";
        ReferenceType updated = refType.withTypeHandler(handler);
        assertNotSame(refType, updated);
        assertSame(handler, updated.getTypeHandler());

        ReferenceType same = updated.withTypeHandler(handler);
        assertSame(updated, same);
    }

    // Tests withContentTypeHandler with same and different handler
    @Test
    public void testWithContentTypeHandler_sameAndDifferentHandler_behavesCorrectly() {
        Object handler = "contentTypeHandler";
        ReferenceType updated = refType.withContentTypeHandler(handler);
        assertNotSame(refType, updated);
        assertSame(handler, updated.getReferencedType().getTypeHandler());

        ReferenceType same = updated.withContentTypeHandler(handler);
        assertSame(updated, same);
    }

    // Tests withValueHandler with same and different handler
    @Test
    public void testWithValueHandler_sameAndDifferentHandler_behavesCorrectly() {
        Object handler = "customValueHandler";
        ReferenceType updated = refType.withValueHandler(handler);
        assertNotSame(refType, updated);
        assertSame(handler, updated.getValueHandler());

        ReferenceType same = updated.withValueHandler(handler);
        assertSame(updated, same);
    }

    // Tests withContentValueHandler with same and different handler
    @Test
    public void testWithContentValueHandler_sameAndDifferentHandler_behavesCorrectly() {
        Object handler = "contentValueHandler";
        ReferenceType updated = refType.withContentValueHandler(handler);
        assertNotSame(refType, updated);
        assertSame(handler, updated.getReferencedType().getValueHandler());

        ReferenceType same = updated.withContentValueHandler(handler);
        assertSame(updated, same);
    }

    // Tests withStaticTyping when already static and when not static
    @Test
    public void testWithStaticTyping_asStaticFalseAndTrue_behavesCorrectly() {
        assertFalse(refType.useStaticType());
        ReferenceType staticRef = refType.withStaticTyping();
        assertNotSame(refType, staticRef);
        assertTrue(staticRef.useStaticType());

        ReferenceType same = staticRef.withStaticTyping();
        assertSame(staticRef, same);
    }

    // Tests _narrow method with subclass
    @Test
    public void testNarrow_subclass_returnsNewReferenceTypeWithSubclass() {
        JavaType narrowed = refType._narrow(AtomicReference.class);
        assertNotNull(narrowed);
        assertTrue(narrowed.isReferenceType());
        assertEquals(AtomicReference.class, narrowed.getRawClass());
        assertEquals(stringType, ((ReferenceType) narrowed).getReferencedType());
    }

    // Tests equals and hashCode
    @Test
    public void testEquals_variousScenarios_returnsExpectedResults() {
        assertTrue(refType.equals(refType));
        assertFalse(refType.equals(null));
        assertFalse(refType.equals("some string"));

        ReferenceType sameType = ReferenceType.construct(AtomicReference.class, stringType, null, null);
        assertTrue(refType.equals(sameType));
        assertEquals(refType.hashCode(), sameType.hashCode());

        ReferenceType diffRefType = ReferenceType.construct(AtomicReference.class, intType, null, null);
        assertFalse(refType.equals(diffRefType));

        ReferenceType diffClass = ReferenceType.construct(Object.class, stringType, null, null);
        assertFalse(refType.equals(diffClass));
    }

    // Tests getContentType method
    @Test
    public void testGetContentType_initializedType_returnsReferencedType() {
        assertSame(stringType, refType.getContentType());
    }

    // Tests construct helper methods
    @Test
    public void testConstruct_twoParameters_constructsCorrectly() {
        ReferenceType constructed = ReferenceType.construct(AtomicReference.class, stringType);
        assertNotNull(constructed);
        assertSame(stringType, constructed.getReferencedType());
        assertEquals(AtomicReference.class, constructed.getRawClass());
    }

    // Tests upgradeFrom method
    @Test
    public void testUpgradeFrom_baseTypeAndReferencedType_upgradesSuccessfully() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType upgraded = ReferenceType.upgradeFrom(baseType, stringType);
        assertNotNull(upgraded);
        assertEquals(AtomicReference.class, upgraded.getRawClass());
        assertSame(stringType, upgraded.getReferencedType());
    }

    // Tests isAnchorType method
    @Test
    public void testIsAnchorType_always_returnsFalse() {
        assertFalse(refType.isAnchorType());
    }
}