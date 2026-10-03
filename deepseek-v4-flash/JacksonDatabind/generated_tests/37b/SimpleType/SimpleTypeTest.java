package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class SimpleTypeTest {

    @Test
    public void testConstructUnsafe_normalClass_returnsSimpleType() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertNotNull(type);
        assertFalse(type.isContainerType());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructUnsafe_toString_throwsNullPointerException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.toString();
    }

    @Test(expected = NullPointerException.class)
    public void testConstructUnsafe_getGenericSignature_throwsNullPointerException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.getGenericSignature(new StringBuilder());
    }

    @Test(expected = NullPointerException.class)
    public void testEquals_differentBindings_throwsNullPointerException() {
        SimpleType type1 = SimpleType.constructUnsafe(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        type1.equals(type2);
    }

    @Test
    public void testConstruct_normalClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertNotNull(type);
        assertFalse(type.isContainerType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(int[].class);
    }

    @Test
    public void testIsContainerType_false() {
        SimpleType type = SimpleType.constructUnsafe(Double.class);
        assertFalse(type.isContainerType());
    }

    @Test
    public void testWithTypeHandler_sameHandler_returnsThis() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertSame(type, type.withTypeHandler(null));
    }

    @Test
    public void testWithTypeHandler_differentHandler_returnsNewInstance() {
        SimpleType type = SimpleType.construct(Integer.class);
        SimpleType newType = type.withTypeHandler(new Object());
        assertNotNull(newType);
        assertNotSame(type, newType);
    }

    @Test
    public void testWithValueHandler_sameHandler_returnsThis() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertSame(type, type.withValueHandler(null));
    }

    @Test
    public void testWithValueHandler_differentHandler_returnsNewInstance() {
        SimpleType type = SimpleType.construct(Integer.class);
        SimpleType newType = type.withValueHandler(new Object());
        assertNotNull(newType);
        assertNotSame(type, newType);
    }

    @Test
    public void testWithStaticTyping_first_returnsNewInstance() {
        SimpleType type = SimpleType.construct(Integer.class);
        SimpleType staticType = type.withStaticTyping();
        assertNotNull(staticType);
        assertNotSame(type, staticType);
    }

    @Test
    public void testWithStaticTyping_twice_returnsSame() {
        SimpleType type = SimpleType.construct(Integer.class);
        SimpleType staticType = type.withStaticTyping();
        assertSame(staticType, staticType.withStaticTyping());
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertTrue(type.equals(type));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertFalse(type.equals(null));
    }

    @Test
    public void testEquals_differentClass_returnsFalse() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertFalse(type.equals("not a SimpleType"));
    }

    @Test
    public void testEquals_differentSimpleTypeClass_returnsFalse() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(Integer.class);
        assertFalse(type1.equals(type2));
    }

    @Test
    public void testEquals_sameClassSameBindings_returnsTrue() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        assertTrue(type1.equals(type2));
    }

    @Test
    public void testToString_constructNormal_containsClassName() {
        SimpleType type = SimpleType.construct(Integer.class);
        String str = type.toString();
        assertTrue(str.contains("[simple type, class java.lang.Integer]"));
    }

    @Test
    public void testGetErasedSignature_constructNormal_returnsCorrectSignature() {
        SimpleType type = SimpleType.construct(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertNotNull(result);
        assertTrue(result.toString().contains("java.lang.String"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(Integer.class);
        type.withContentType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(Integer.class);
        type.withContentTypeHandler(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(Integer.class);
        type.withContentValueHandler(null);
    }

    @Test
    public void testRefine_returnsNull() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertNull(type.refine(Integer.class, null, null, null));
    }
}