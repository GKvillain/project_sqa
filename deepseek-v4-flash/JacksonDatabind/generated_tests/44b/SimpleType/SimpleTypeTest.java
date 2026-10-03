package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleTypeTest {

    // Tests normal construction via constructUnsafe
    @Test
    public void testConstructUnsafe_normalClass_createsInstance() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

    // Tests buildCanonicalName with null bindings (defect: throws NPE)
    @Test(expected = NullPointerException.class)
    public void testBuildCanonicalName_nullBindings_throwsNullPointerException() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        t.buildCanonicalName();
    }

    // Tests equals with same class and null bindings (defect: throws NPE)
    @Test(expected = NullPointerException.class)
    public void testEquals_nullBindings_sameClass_throwsNullPointerException() {
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = SimpleType.constructUnsafe(String.class);
        t1.equals(t2);
    }

    // Tests equals with different class (no bindings access) returns false
    @Test
    public void testEquals_differentClass_returnsFalse() {
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = SimpleType.constructUnsafe(Integer.class);
        assertFalse(t1.equals(t2));
    }

    // Tests equals with null returns false
    @Test
    public void testEquals_null_returnsFalse() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertFalse(t.equals(null));
    }

    // Tests normal construction via construct (deprecated) with valid class
    @Test
    public void testConstruct_normalClass_createsInstance() {
        SimpleType t = SimpleType.construct(String.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

    // Tests construct with Map class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_MapClass_throwsIllegalArgumentException() {
        SimpleType.construct(java.util.HashMap.class);
    }

    // Tests construct with Collection class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_CollectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(java.util.ArrayList.class);
    }

    // Tests construct with array class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_ArrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests withTypeHandler when handler unchanged returns this
    @Test
    public void testWithTypeHandler_sameHandler_returnsThis() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        Object h = t.getTypeHandler();
        assertSame(t, t.withTypeHandler(h));
    }

    // Tests withTypeHandler with different handler returns new instance
    @Test
    public void testWithTypeHandler_differentHandler_returnsNewInstance() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        Object newHandler = new Object();
        SimpleType result = t.withTypeHandler(newHandler);
        assertNotSame(t, result);
    }

    // Tests withValueHandler when handler unchanged returns this
    @Test
    public void testWithValueHandler_sameHandler_returnsThis() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        Object h = t.getValueHandler();
        assertSame(t, t.withValueHandler(h));
    }

    // Tests withValueHandler with different handler returns new instance
    @Test
    public void testWithValueHandler_differentHandler_returnsNewInstance() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        Object newHandler = new Object();
        SimpleType result = t.withValueHandler(newHandler);
        assertNotSame(t, result);
    }

    // Tests withStaticTyping when already static returns this
    @Test
    public void testWithStaticTyping_alreadyStatic_returnsThis() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        SimpleType staticT = t.withStaticTyping();
        assertSame(staticT, staticT.withStaticTyping());
    }

    // Tests withStaticTyping when not static returns new static instance
    @Test
    public void testWithStaticTyping_notStatic_returnsNewStatic() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertFalse(t.isStatic());
        SimpleType staticT = t.withStaticTyping();
        assertNotSame(t, staticT);
        assertTrue(staticT.isStatic());
    }

    // Tests withContentType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_throwsIllegalArgumentException() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        t.withContentType(null);
    }

    // Tests withContentTypeHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_throwsIllegalArgumentException() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        t.withContentTypeHandler(null);
    }

    // Tests withContentValueHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_throwsIllegalArgumentException() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        t.withContentValueHandler(null);
    }

    // Tests isContainerType returns false
    @Test
    public void testIsContainerType_returnsFalse() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertFalse(t.isContainerType());
    }

    // Tests refine returns null
    @Test
    public void testRefine_returnsNull() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertNull(t.refine(null, null, null, null));
    }

    // Tests toString contains class name
    @Test
    public void testToString_containsClassName() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        String str = t.toString();
        assertTrue(str.contains("simple type, class " + String.class.getName()));
    }

    // Tests getErasedSignature returns non-empty signature
    @Test
    public void testGetErasedSignature_returnsSignature() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = t.getErasedSignature(sb);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Tests getGenericSignature returns non-empty signature
    @Test
    public void testGetGenericSignature_returnsSignature() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = t.getGenericSignature(sb);
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    // Tests buildCanonicalName with proper bindings (via construct) does not throw
    @Test
    public void testBuildCanonicalName_withBindings_returnsCanonicalName() {
        SimpleType t = SimpleType.construct(String.class);
        String name = t.buildCanonicalName();
        assertTrue(name.startsWith("java.lang.String"));
    }

    // Tests equals with equal types from construct returns true
    @Test
    public void testEquals_equalTypes_returnsTrue() {
        SimpleType t1 = SimpleType.construct(String.class);
        SimpleType t2 = SimpleType.construct(String.class);
        assertTrue(t1.equals(t2));
    }

    // ========== New test cases for uncovered areas ==========

    // Tests getContentType returns null (SimpleType is not a container)
    @Test
    public void testGetContentType_returnsNull() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertNull(t.getContentType());
    }

    // Tests containedType returns null for any index
    @Test
    public void testContainedType_returnsNull() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertNull(t.containedType(0));
        assertNull(t.containedType(1));
    }

    // Tests containedTypeCount returns 0
    @Test
    public void testContainedTypeCount_returnsZero() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertEquals(0, t.containedTypeCount());
    }

    // Tests hasHandlers returns false by default
    @Test
    public void testHasHandlers_returnsFalse() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertFalse(t.hasHandlers());
    }

    // Tests withTypeHandler(null) returns this (default handler is null)
    @Test
    public void testWithTypeHandler_null_returnsThis() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertSame(t, t.withTypeHandler(null));
    }

    // Tests withValueHandler(null) returns this (default handler is null)
    @Test
    public void testWithValueHandler_null_returnsThis() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertSame(t, t.withValueHandler(null));
    }

    // Tests equals with self returns true (reflexive)
    @Test
    public void testEquals_sameInstance_returnsTrue() {
        SimpleType t = SimpleType.constructUnsafe(String.class);
        assertTrue(t.equals(t));
    }

    // Tests hashCode is consistent with equals for equal instances
    @Test
    public void testHashCode_consistentWithEquals() {
        SimpleType t1 = SimpleType.construct(String.class);
        SimpleType t2 = SimpleType.construct(String.class);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    // Tests constructUnsafe allows array class (unsafe construction)
    @Test
    public void testConstructUnsafe_withArrayClass_createsInstance() {
        SimpleType t = SimpleType.constructUnsafe(String[].class);
        assertNotNull(t);
        assertEquals(String[].class, t.getRawClass());
    }

    // Tests isArrayType for array class returns true (inherited behavior)
    @Test
    public void testIsArrayType_withArrayClass_returnsTrue() {
        SimpleType t = SimpleType.constructUnsafe(String[].class);
        assertTrue(t.isArrayType());
    }

    // Tests isContainerType remains false even for array class (overridden)
    @Test
    public void testIsContainerType_forArrayClass_returnsFalse() {
        SimpleType t = SimpleType.constructUnsafe(String[].class);
        assertFalse(t.isContainerType());
    }
}