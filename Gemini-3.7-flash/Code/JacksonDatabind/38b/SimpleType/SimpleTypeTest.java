package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    // Tests normal construction using construct()
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
        assertEquals("java.lang.String", type.toCanonical());
    }

    // Tests construct() throwing exception on Map class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    // Tests construct() throwing exception on Collection class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    // Tests construct() throwing exception on array class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests constructUnsafe factory method
    @Test
    public void testConstructUnsafe_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.constructUnsafe(Integer.class);
        assertNotNull(type);
        assertEquals(Integer.class, type.getRawClass());
        assertFalse(type.isContainerType());
    }

    // Tests withContentType throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_anyInput_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentType(type);
    }

    // Tests withContentTypeHandler throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_anyInput_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentTypeHandler("handler");
    }

    // Tests withContentValueHandler throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_anyInput_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentValueHandler("handler");
    }

    // Tests withTypeHandler with new and same handler
    @Test
    public void testWithTypeHandler_newAndSameHandler_returnsCorrectInstances() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "customTypeHandler";

        SimpleType typeWithHandler = type.withTypeHandler(handler);
        assertNotNull(typeWithHandler);
        assertNotSame(type, typeWithHandler);
        assertEquals(handler, typeWithHandler.getTypeHandler());

        // Same handler should return same instance
        SimpleType typeWithSameHandler = typeWithHandler.withTypeHandler(handler);
        assertSame(typeWithHandler, typeWithSameHandler);
    }

    // Tests withValueHandler with new and same handler
    @Test
    public void testWithValueHandler_newAndSameHandler_returnsCorrectInstances() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "customValueHandler";

        SimpleType typeWithHandler = type.withValueHandler(handler);
        assertNotNull(typeWithHandler);
        assertNotSame(type, typeWithHandler);
        assertEquals(handler, typeWithHandler.getValueHandler());

        // Same handler should return same instance
        SimpleType typeWithSameHandler = typeWithHandler.withValueHandler(handler);
        assertSame(typeWithHandler, typeWithSameHandler);
    }

    // Tests withStaticTyping when false and when already true
    @Test
    public void testWithStaticTyping_togglesStaticFlag() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        assertNotNull(staticType);
        assertNotSame(type, staticType);
        assertTrue(staticType.useStaticType());

        // Calling again when already static should return same instance
        SimpleType staticType2 = staticType.withStaticTyping();
        assertSame(staticType, staticType2);
    }

    // Tests refine method returns null
    @Test
    public void testRefine_anyInput_returnsNull() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull(refined);
    }

    // Tests _narrow method with same and sub class
    @Test
    public void testNarrow_sameClassAndSubClass_returnsExpectedType() {
        SimpleType type = SimpleType.constructUnsafe(CharSequence.class);

        // Same class should return this
        JavaType sameNarrow = type._narrow(CharSequence.class);
        assertSame(type, sameNarrow);

        // Subclass should return new SimpleType
        JavaType subNarrow = type._narrow(String.class);
        assertNotNull(subNarrow);
        assertNotSame(type, subNarrow);
        assertEquals(String.class, subNarrow.getRawClass());
    }

    // Tests signatures and toString
    @Test
    public void testSignaturesAndToString_validType_returnsCorrectFormat() {
        SimpleType type = SimpleType.constructUnsafe(String.class);

        StringBuilder erasedSig = type.getErasedSignature(new StringBuilder());
        assertEquals("Ljava/lang/String;", erasedSig.toString());

        StringBuilder genericSig = type.getGenericSignature(new StringBuilder());
        assertEquals("Ljava/lang/String;;", genericSig.toString());

        String str = type.toString();
        assertEquals("[simple type, class java.lang.String]", str);
    }

    // Tests equals method branches
    @Test
    public void testEquals_variousScenarios_returnsExpectedBoolean() {
        SimpleType type1 = SimpleType.constructUnsafe(String.class);
        SimpleType type2 = SimpleType.constructUnsafe(String.class);
        SimpleType type3 = SimpleType.constructUnsafe(Integer.class);

        // Same object
        assertTrue(type1.equals(type1));

        // Null object
        assertFalse(type1.equals(null));

        // Different class object
        assertFalse(type1.equals("a string"));

        // Same raw class SimpleType
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));

        // Different raw class SimpleType
        assertFalse(type1.equals(type3));
    }

    // Tests canonical name and generic signature with type parameters
    @Test
    public void testGenericBindings_withParameters_formatsCorrectly() {
        JavaType paramType = SimpleType.constructUnsafe(String.class);
        TypeBindings bindings = TypeBindings.create(SimpleTypeTest.class, new JavaType[] { paramType });
        SimpleType typeWithParams = new SimpleType(SimpleTypeTest.class, bindings, null, null, null, null, false);

        assertEquals(SimpleTypeTest.class.getName() + "<java.lang.String>", typeWithParams.toCanonical());

        StringBuilder genericSig = typeWithParams.getGenericSignature(new StringBuilder());
        assertTrue(genericSig.toString().contains("<Ljava/lang/String;;>;"));
    }
}