package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    // Tests construct with a standard class
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType st = SimpleType.construct(String.class);
        assertNotNull(st);
        assertEquals(String.class, st.getRawClass());
        assertFalse(st.isContainerType());
    }

    // Tests construct with Object.class boundary
    @Test
    public void testConstruct_objectClass_returnsSimpleType() {
        SimpleType st = SimpleType.construct(Object.class);
        assertNotNull(st);
        assertEquals(Object.class, st.getRawClass());
        assertNull(st.getSuperClass());
    }

    // Tests construct with Map class throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    // Tests construct with Collection class throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    // Tests construct with Array class throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests constructUnsafe method
    @Test
    public void testConstructUnsafe_validClass_returnsSimpleType() {
        SimpleType st = SimpleType.constructUnsafe(Number.class);
        assertNotNull(st);
        assertEquals(Number.class, st.getRawClass());
    }

    // Tests _narrow with same class returns same instance
    @Test
    public void testNarrow_sameClass_returnsSameInstance() {
        SimpleType st = SimpleType.construct(Number.class);
        JavaType narrowed = st._narrow(Number.class);
        assertSame(st, narrowed);
    }

    // Tests _narrow with subclass returns new SimpleType
    @Test
    public void testNarrow_subclass_returnsNarrowedType() {
        SimpleType st = SimpleType.construct(Number.class);
        JavaType narrowed = st._narrow(Integer.class);
        assertNotNull(narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
    }

    // Tests withTypeHandler when handler is same and different
    @Test
    public void testWithTypeHandler_sameAndDifferentHandler_returnsExpected() {
        SimpleType st = SimpleType.construct(String.class);
        String handler = "handler1";
        SimpleType withH = st.withTypeHandler(handler);
        assertNotSame(st, withH);
        assertSame(withH, withH.withTypeHandler(handler));
        assertEquals(handler, withH.getTypeHandler());
    }

    // Tests withValueHandler when handler is same and different
    @Test
    public void testWithValueHandler_sameAndDifferentHandler_returnsExpected() {
        SimpleType st = SimpleType.construct(String.class);
        String handler = "valHandler";
        SimpleType withH = st.withValueHandler(handler);
        assertNotSame(st, withH);
        assertSame(withH, withH.withValueHandler(handler));
        assertEquals(handler, withH.getValueHandler());
    }

    // Tests withStaticTyping branch when false and already true
    @Test
    public void testWithStaticTyping_togglesAndPreserves() {
        SimpleType st = SimpleType.construct(String.class);
        assertFalse(st.useStaticType());
        SimpleType staticSt = st.withStaticTyping();
        assertTrue(staticSt.useStaticType());
        assertSame(staticSt, staticSt.withStaticTyping());
    }

    // Tests unsupported operation withContentType throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_throwsIllegalArgumentException() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentType(st);
    }

    // Tests unsupported operation withContentTypeHandler throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_throwsIllegalArgumentException() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentTypeHandler("handler");
    }

    // Tests unsupported operation withContentValueHandler throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_throwsIllegalArgumentException() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentValueHandler("handler");
    }

    // Tests refine method returns null
    @Test
    public void testRefine_returnsNull() {
        SimpleType st = SimpleType.construct(String.class);
        assertNull(st.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    // Tests signatures and toString
    @Test
    public void testSignaturesAndToString_returnsValidStringRepresentations() {
        SimpleType st = SimpleType.construct(String.class);
        StringBuilder sbErased = new StringBuilder();
        st.getErasedSignature(sbErased);
        assertEquals("Ljava/lang/String;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        st.getGenericSignature(sbGeneric);
        assertEquals("Ljava/lang/String;;", sbGeneric.toString());

        String str = st.toString();
        assertTrue(str.contains("java.lang.String"));
    }

    // Tests equals method branches
    @Test
    public void testEquals_variousObjects_returnsCorrectBoolean() {
        SimpleType st1 = SimpleType.construct(String.class);
        SimpleType st2 = SimpleType.construct(String.class);
        SimpleType st3 = SimpleType.construct(Integer.class);

        assertTrue(st1.equals(st1));
        assertFalse(st1.equals(null));
        assertFalse(st1.equals("not-a-type"));
        assertFalse(st1.equals(st3));
        assertTrue(st1.equals(st2));
    }

    // Tests contained types and generic checks for empty bindings
    @Test
    public void testContainedTypes_emptyBindings_returnsEmptyAndNull() {
        SimpleType st = SimpleType.construct(String.class);
        assertEquals(0, st.containedTypeCount());
        assertNull(st.containedType(0));
        assertNull(st.containedType(-1));
        assertNull(st.containedTypeName(0));
        assertFalse(st.hasGenericTypes());
    }

    // Tests constructor and signatures with non-empty type bindings
    @Test
    public void testContainedTypes_withBindings() {
        JavaType strType = SimpleType.construct(String.class);
        JavaType intType = SimpleType.construct(Integer.class);
        TypeBindings bindings = TypeBindings.create(java.util.Map.Entry.class, new JavaType[] { strType, intType });
        JavaType objType = SimpleType.construct(Object.class);
        SimpleType custom = new SimpleType(java.util.Map.Entry.class, bindings, objType, null, null, null, false);

        assertTrue(custom.hasGenericTypes());
        assertEquals(2, custom.containedTypeCount());
        assertEquals(strType, custom.containedType(0));
        assertEquals(intType, custom.containedType(1));
        assertNull(custom.containedType(2));
        assertNotNull(custom.containedTypeName(0));
        assertNotNull(custom.containedTypeName(1));
        assertNull(custom.containedTypeName(2));

        StringBuilder sb = new StringBuilder();
        custom.getGenericSignature(sb);
        assertTrue(sb.toString().contains("Ljava/lang/String;"));

        String canonical = custom.toCanonical();
        assertTrue(canonical.contains("java.util.Map$Entry"));
    }

    // Tests equals with different handlers, static typing, and bindings
    @Test
    public void testEquals_withHandlersAndFlags() {
        SimpleType st1 = SimpleType.construct(String.class);
        SimpleType st2 = st1.withValueHandler("vh");
        SimpleType st3 = st1.withTypeHandler("th");
        SimpleType st4 = st1.withStaticTyping();

        assertFalse(st1.equals(st2));
        assertFalse(st1.equals(st3));
        assertFalse(st1.equals(st4));
        assertFalse(st2.equals(st3));

        JavaType intType = SimpleType.construct(Integer.class);
        TypeBindings b1 = TypeBindings.create(java.util.Map.Entry.class, new JavaType[] { st1, st1 });
        TypeBindings b2 = TypeBindings.create(java.util.Map.Entry.class, new JavaType[] { st1, intType });
        SimpleType custom1 = new SimpleType(java.util.Map.Entry.class, b1, null, null, null, null, false);
        SimpleType custom2 = new SimpleType(java.util.Map.Entry.class, b2, null, null, null, null, false);
        assertFalse(custom1.equals(custom2));
    }

    // Tests construct with interface and primitive classes
    @Test
    public void testConstruct_interfaceAndPrimitiveClasses() {
        SimpleType ifaceType = SimpleType.construct(Comparable.class);
        assertNotNull(ifaceType);
        assertTrue(ifaceType.isInterface());

        SimpleType primType = SimpleType.construct(int.class);
        assertNotNull(primType);
        assertTrue(primType.isPrimitive());
        assertEquals("I", primType.getGenericSignature());
        assertEquals("I", primType.getErasedSignature());
    }

    // Tests direct SimpleType(Class<?>) protected constructor
    @Test
    public void testProtectedConstructor_singleArg() {
        SimpleType st = new SimpleType(Boolean.class);
        assertNotNull(st);
        assertEquals(Boolean.class, st.getRawClass());
    }
}