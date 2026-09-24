package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    // Tests upgradeFrom with valid SimpleType base
    @Test
    public void testUpgradeFrom_validBase_createsReferenceType() {
        SimpleType base = SimpleType.constructUnsafe(AtomicReference.class);
        SimpleType ref = SimpleType.constructUnsafe(String.class);

        ReferenceType rt = ReferenceType.upgradeFrom(base, ref);

        assertNotNull(rt);
        assertEquals(AtomicReference.class, rt.getRawClass());
        assertEquals(ref, rt.getReferencedType());
        assertEquals(ref, rt.getContentType());
        assertTrue(rt.isReferenceType());
        assertTrue(rt.hasContentType());
        assertTrue(rt.isAnchorType());
        assertEquals(rt, rt.getAnchorType());
    }

    // Tests upgradeFrom throwing exception when refdType is null
    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFrom_nullRefdType_throwsException() {
        SimpleType base = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType.upgradeFrom(base, null);
    }

    // Tests canonical name construction including closing bracket (Defects4J 99)
    @Test
    public void testBuildCanonicalName_standardType_includesClosingAngleBracket() {
        SimpleType base = SimpleType.constructUnsafe(AtomicReference.class);
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(base, ref);

        String canonical = rt.toCanonical();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String>", canonical);
    }

    // Tests construct factory method with TypeBindings and supertypes
    @Test
    public void testConstruct_withBindings_returnsConfiguredInstance() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        TypeBindings bindings = TypeBindings.create(AtomicReference.class, new JavaType[]{ref});
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, bindings, null, null, ref);

        assertNotNull(rt);
        assertEquals(AtomicReference.class, rt.getRawClass());
        assertEquals(ref, rt.getReferencedType());
        assertTrue(rt.isAnchorType());
    }

    // Tests deprecated construct method with raw class and refType
    @SuppressWarnings("deprecation")
    @Test
    public void testConstruct_deprecated_returnsReferenceType() {
        SimpleType ref = SimpleType.constructUnsafe(Integer.class);
        ReferenceType rt = ReferenceType.construct(AtomicReference.class, ref);

        assertNotNull(rt);
        assertEquals(AtomicReference.class, rt.getRawClass());
        assertEquals(ref, rt.getReferencedType());
    }

    // Tests withContentType returning same instance if identical, or new instance if different
    @Test
    public void testWithContentType_sameAndDifferent_returnsExpected() {
        SimpleType ref1 = SimpleType.constructUnsafe(String.class);
        SimpleType ref2 = SimpleType.constructUnsafe(Integer.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref1);

        JavaType same = rt.withContentType(ref1);
        assertSame(rt, same);

        JavaType modified = rt.withContentType(ref2);
        assertNotSame(rt, modified);
        assertEquals(ref2, modified.getContentType());
        assertEquals(rt.getAnchorType(), ((ReferenceType) modified).getAnchorType());
    }

    // Tests withTypeHandler and withContentTypeHandler
    @Test
    public void testWithTypeHandler_andWithContentTypeHandler_modifiesHandlers() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        String typeHandler = "typeHandlerObj";
        ReferenceType withTH = rt.withTypeHandler(typeHandler);
        assertSame(withTH, withTH.withTypeHandler(typeHandler));
        assertEquals(typeHandler, withTH.getTypeHandler());

        String contentTH = "contentTypeHandlerObj";
        ReferenceType withCTH = rt.withContentTypeHandler(contentTH);
        assertSame(withCTH, withCTH.withContentTypeHandler(contentTH));
        assertEquals(contentTH, withCTH.getContentType().getTypeHandler());
    }

    // Tests withValueHandler and withContentValueHandler
    @Test
    public void testWithValueHandler_andWithContentValueHandler_modifiesHandlers() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        String valHandler = "valueHandlerObj";
        ReferenceType withVH = rt.withValueHandler(valHandler);
        assertSame(withVH, withVH.withValueHandler(valHandler));
        assertEquals(valHandler, withVH.getValueHandler());

        String contentVH = "contentValueHandlerObj";
        ReferenceType withCVH = rt.withContentValueHandler(contentVH);
        assertSame(withCVH, withCVH.withContentValueHandler(contentVH));
        assertEquals(contentVH, withCVH.getContentType().getValueHandler());
    }

    // Tests withStaticTyping
    @Test
    public void testWithStaticTyping_createsStaticInstance() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        assertFalse(rt.useStaticType());
        ReferenceType staticRt = rt.withStaticTyping();
        assertTrue(staticRt.useStaticType());
        assertSame(staticRt, staticRt.withStaticTyping());
    }

    // Tests refine method
    @Test
    public void testRefine_validClass_returnsRefinedReferenceType() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        JavaType refined = rt.refine(AtomicReference.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertEquals(AtomicReference.class, refined.getRawClass());
        assertEquals(ref, refined.getContentType());
    }

    // Tests _narrow method
    @SuppressWarnings("deprecation")
    @Test
    public void testNarrow_subclass_returnsNarrowedType() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        JavaType narrowed = rt._narrow(AtomicReference.class);
        assertNotNull(narrowed);
        assertEquals(AtomicReference.class, narrowed.getRawClass());
    }

    // Tests getErasedSignature and getGenericSignature
    @Test
    public void testSignatures_returnsExpectedFormat() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        StringBuilder erasedSb = new StringBuilder();
        rt.getErasedSignature(erasedSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        rt.getGenericSignature(genericSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", genericSb.toString());
    }

    // Tests toString method
    @Test
    public void testToString_returnsDescriptiveString() {
        SimpleType ref = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), ref);

        String str = rt.toString();
        assertTrue(str.startsWith("[reference type, class "));
        assertTrue(str.contains(AtomicReference.class.getName()));
    }

    // Tests equals and hashCode behaviors
    @Test
    public void testEquals_variousObjects_returnsCorrectComparison() {
        SimpleType refStr = SimpleType.constructUnsafe(String.class);
        SimpleType refInt = SimpleType.constructUnsafe(Integer.class);
        ReferenceType rt1 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refStr);
        ReferenceType rt2 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refStr);
        ReferenceType rt3 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refInt);

        assertTrue(rt1.equals(rt1));
        assertTrue(rt1.equals(rt2));
        assertFalse(rt1.equals(rt3));
        assertFalse(rt1.equals(null));
        assertFalse(rt1.equals("someString"));
    }
}