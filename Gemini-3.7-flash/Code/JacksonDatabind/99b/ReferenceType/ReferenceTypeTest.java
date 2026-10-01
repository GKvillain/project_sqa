package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    // Tests factory construction and basic type properties
    @Test
    public void testConstruct_validInputs_returnsConfiguredReferenceType() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertNotNull(refType);
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(refdType, refType.getContentType());
        assertEquals(refdType, refType.getReferencedType());
        assertTrue(refType.hasContentType());
        assertTrue(refType.isReferenceType());
        assertTrue(refType.isAnchorType());
        assertEquals(refType, refType.getAnchorType());
    }

    // Tests deprecated construct factory method
    @Test
    public void testConstruct_deprecatedFactory_returnsConfiguredReferenceType() {
        JavaType refdType = SimpleType.constructUnsafe(Integer.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, refdType);

        assertNotNull(refType);
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(refdType, refType.getContentType());
    }

    // Tests upgradeFrom with valid SimpleType base
    @Test
    public void testUpgradeFrom_validBaseType_upgradesToReferenceType() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        JavaType refdType = SimpleType.constructUnsafe(String.class);

        ReferenceType refType = ReferenceType.upgradeFrom(baseType, refdType);

        assertNotNull(refType);
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(refdType, refType.getReferencedType());
        assertTrue(refType.isAnchorType());
    }

    // Tests upgradeFrom throws exception when referencedType is null
    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFrom_nullReferencedType_throwsIllegalArgumentException() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType.upgradeFrom(baseType, null);
    }

    // Tests withContentType with same and different content types
    @Test
    public void testWithContentType_sameAndDifferent_returnsExpectedInstances() {
        JavaType refdType1 = SimpleType.constructUnsafe(String.class);
        JavaType refdType2 = SimpleType.constructUnsafe(Integer.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType1);

        assertSame(refType, refType.withContentType(refdType1));

        JavaType changed = refType.withContentType(refdType2);
        assertNotSame(refType, changed);
        assertEquals(refdType2, changed.getContentType());
    }

    // Tests withTypeHandler with same and different handler objects
    @Test
    public void testWithTypeHandler_sameAndNewHandler_returnsExpectedInstances() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertNull(refType.getTypeHandler());
        assertSame(refType, refType.withTypeHandler(null));

        Object handler = "customTypeHandler";
        ReferenceType withH = refType.withTypeHandler(handler);
        assertNotSame(refType, withH);
        assertEquals(handler, withH.getTypeHandler());
        assertSame(withH, withH.withTypeHandler(handler));
    }

    // Tests withContentTypeHandler with same and different handler objects
    @Test
    public void testWithContentTypeHandler_sameAndNewHandler_returnsExpectedInstances() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertSame(refType, refType.withContentTypeHandler(null));

        Object handler = "customContentTypeHandler";
        ReferenceType withH = refType.withContentTypeHandler(handler);
        assertNotSame(refType, withH);
        assertEquals(handler, withH.getContentType().getTypeHandler());
        assertSame(withH, withH.withContentTypeHandler(handler));
    }

    // Tests withValueHandler with same and different handler objects
    @Test
    public void testWithValueHandler_sameAndNewHandler_returnsExpectedInstances() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertNull(refType.getValueHandler());
        assertSame(refType, refType.withValueHandler(null));

        Object handler = "customValueHandler";
        ReferenceType withH = refType.withValueHandler(handler);
        assertNotSame(refType, withH);
        assertEquals(handler, withH.getValueHandler());
        assertSame(withH, withH.withValueHandler(handler));
    }

    // Tests withContentValueHandler with same and different handler objects
    @Test
    public void testWithContentValueHandler_sameAndNewHandler_returnsExpectedInstances() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertSame(refType, refType.withContentValueHandler(null));

        Object handler = "customContentValueHandler";
        ReferenceType withH = refType.withContentValueHandler(handler);
        assertNotSame(refType, withH);
        assertEquals(handler, withH.getContentType().getValueHandler());
        assertSame(withH, withH.withContentValueHandler(handler));
    }

    // Tests withStaticTyping toggling static typing flag
    @Test
    public void testWithStaticTyping_toggledAndRetained_returnsExpectedInstances() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        assertFalse(refType.useStaticType());

        ReferenceType staticRef = refType.withStaticTyping();
        assertNotSame(refType, staticRef);
        assertTrue(staticRef.useStaticType());
        assertSame(staticRef, staticRef.withStaticTyping());
    }

    // Tests refine method producing updated ReferenceType
    @Test
    public void testRefine_customClassAndBindings_returnsRefinedReferenceType() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        JavaType refined = refType.refine(AtomicReference.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertEquals(AtomicReference.class, refined.getRawClass());
        assertEquals(refdType, refined.getContentType());
    }

    // Tests _narrow deprecated method
    @Test
    public void testNarrow_subclass_returnsNarrowedReferenceType() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(Object.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        JavaType narrowed = refType._narrow(AtomicReference.class);
        assertEquals(AtomicReference.class, narrowed.getRawClass());
        assertEquals(refdType, narrowed.getContentType());
    }

    // Tests erased and generic signature formatting
    @Test
    public void testSignatures_validReferenceType_returnsCorrectSignatures() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        StringBuilder erasedSb = new StringBuilder();
        refType.getErasedSignature(erasedSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        refType.getGenericSignature(genericSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", genericSb.toString());
    }

    // Tests buildCanonicalName and toCanonical formatting
    @Test
    public void testToCanonical_validReferenceType_containsClosingAngleBracket() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        String canonical = refType.toCanonical();
        assertTrue(canonical.startsWith("java.util.concurrent.atomic.AtomicReference<"));
        assertTrue(canonical.contains("java.lang.String"));
    }

    // Tests toString formatting
    @Test
    public void testToString_validReferenceType_returnsDescriptiveString() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType refType = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType);

        String str = refType.toString();
        assertTrue(str.startsWith("[reference type, class "));
        assertTrue(str.contains(AtomicReference.class.getName()));
        assertTrue(str.contains(String.class.getName()));
    }

    // Tests equals and hashCode consistency
    @Test
    public void testEquals_variousComparisons_returnsCorrectBooleans() {
        JavaType refdType1 = SimpleType.constructUnsafe(String.class);
        JavaType refdType2 = SimpleType.constructUnsafe(Integer.class);

        ReferenceType type1 = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType1);
        ReferenceType type1Duplicate = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType1);
        ReferenceType type2 = ReferenceType.construct(AtomicReference.class,
                TypeBindings.emptyBindings(), null, null, refdType2);
        ReferenceType otherRaw = ReferenceType.construct(Object.class,
                TypeBindings.emptyBindings(), null, null, refdType1);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type1Duplicate));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("notAType"));
        assertFalse(type1.equals(type2));
        assertFalse(type1.equals(otherRaw));
    }
}