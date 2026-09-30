package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    // Tests normal construction of SimpleType using construct()
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
    }

    // Tests exception path when constructing SimpleType for a Map class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    // Tests exception path when constructing SimpleType for a Collection class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    // Tests exception path when constructing SimpleType for an array class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests constructUnsafe method creates a valid SimpleType instance
    @Test
    public void testConstructUnsafe_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.constructUnsafe(Object.class);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    // Tests narrowing type to the exact same class returns this
    @Test
    public void testNarrow_sameClass_returnsSameInstance() {
        SimpleType type = SimpleType.construct(CharSequence.class);
        JavaType narrowed = type._narrow(CharSequence.class);
        assertSame(type, narrowed);
    }

    // Tests narrowing type to a subclass returns a new SimpleType with the subclass
    @Test
    public void testNarrow_subClass_returnsNewNarrowedType() {
        SimpleType type = SimpleType.construct(CharSequence.class);
        JavaType narrowed = type._narrow(String.class);
        assertNotSame(type, narrowed);
        assertEquals(String.class, narrowed.getRawClass());
    }

    // Tests that withContentType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_anyType_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentType(type);
    }

    // Tests that withContentTypeHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_anyHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentTypeHandler("handler");
    }

    // Tests that withContentValueHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_anyHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentValueHandler("handler");
    }

    // Tests withTypeHandler returns same instance when handler is identical
    @Test
    public void testWithTypeHandler_sameHandler_returnsSameInstance() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType withNull = type.withTypeHandler(null);
        assertSame(type, withNull);
    }

    // Tests withTypeHandler returns new instance with updated handler
    @Test
    public void testWithTypeHandler_differentHandler_returnsNewInstanceWithHandler() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = "customTypeHandler";
        SimpleType withHandler = type.withTypeHandler(handler);
        assertNotSame(type, withHandler);
        assertEquals(handler, withHandler.getTypeHandler());
    }

    // Tests withValueHandler returns same instance when handler is identical
    @Test
    public void testWithValueHandler_sameHandler_returnsSameInstance() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType withNull = type.withValueHandler(null);
        assertSame(type, withNull);
    }

    // Tests withValueHandler returns new instance with updated value handler
    @Test
    public void testWithValueHandler_differentHandler_returnsNewInstanceWithValueHandler() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = "customValueHandler";
        SimpleType withHandler = type.withValueHandler(handler);
        assertNotSame(type, withHandler);
        assertEquals(handler, withHandler.getValueHandler());
    }

    // Tests withStaticTyping changes static typing flag and returns same instance when already static
    @Test
    public void testWithStaticTyping_toggleStatic_returnsStaticInstance() {
        SimpleType type = SimpleType.construct(String.class);
        assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        assertNotSame(type, staticType);
        assertTrue(staticType.useStaticType());

        SimpleType sameStatic = staticType.withStaticTyping();
        assertSame(staticType, sameStatic);
    }

    // Tests refine method behavior
    @Test
    public void testRefine_validInputs_returnsNull() {
        SimpleType type = SimpleType.construct(String.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull(refined);
    }

    // Tests buildCanonicalName and toString
    @Test
    public void testToStringAndCanonicalName_validType_returnsFormattedString() {
        SimpleType type = SimpleType.construct(String.class);
        String str = type.toString();
        assertEquals("[simple type, class java.lang.String]", str);
        assertEquals("java.lang.String", type.toCanonical());
    }

    // Tests getErasedSignature and getGenericSignature methods
    @Test
    public void testGetSignatures_validType_returnsCorrectSignatures() {
        SimpleType type = SimpleType.construct(String.class);
        StringBuilder erased = new StringBuilder();
        type.getErasedSignature(erased);
        assertEquals("Ljava/lang/String;", erased.toString());

        StringBuilder generic = new StringBuilder();
        type.getGenericSignature(generic);
        assertEquals("Ljava/lang/String;;", generic.toString());
    }

    // Tests equals method with various comparison paths
    @Test
    public void testEquals_variousObjects_returnsCorrectBoolean() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        SimpleType type3 = SimpleType.construct(Integer.class);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(type3));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("not a SimpleType"));
    }

    // Tests constructor with TypeBindings having generic arguments
    @Test
    public void testGenericBindings_withTypeParameters_buildsCorrectSignatures() {
        JavaType stringType = SimpleType.construct(String.class);
        TypeBindings bindings = TypeBindings.create(List.class, new JavaType[] { stringType });
        SimpleType typeWithBindings = new SimpleType(List.class, bindings, null, null);

        assertEquals("java.util.List<java.lang.String>", typeWithBindings.toCanonical());

        StringBuilder generic = new StringBuilder();
        typeWithBindings.getGenericSignature(generic);
        assertEquals("Ljava/util/List<Ljava/lang/String;;>;", generic.toString());
    }
}