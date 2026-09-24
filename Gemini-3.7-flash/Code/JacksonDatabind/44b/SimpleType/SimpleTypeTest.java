package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    // Tests construct with a standard class
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
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

    // Tests construct with array class throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests constructUnsafe creates SimpleType without hierarchy introspection
    @Test
    public void testConstructUnsafe_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.constructUnsafe(Object.class);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
    }

    // Tests narrowing to the same class returns the same instance
    @Test
    public void testNarrow_sameClass_returnsSameInstance() {
        SimpleType type = SimpleType.construct(Number.class);
        JavaType narrowed = type._narrow(Number.class);
        assertSame(type, narrowed);
    }

    // Tests narrowing to a subclass creates a new narrowed instance
    @Test
    public void testNarrow_subClass_returnsNarrowedType() {
        SimpleType type = SimpleType.construct(Number.class);
        JavaType narrowed = type._narrow(Integer.class);
        assertNotNull(narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals(type, narrowed.getSuperClass());
    }

    // Tests withContentType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentType(SimpleType.construct(Integer.class));
    }

    // Tests withContentTypeHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentTypeHandler("handler");
    }

    // Tests withContentValueHandler throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.construct(String.class);
        type.withContentValueHandler("handler");
    }

    // Tests withValueHandler returns new instance when changed, same instance when unchanged
    @Test
    public void testWithValueHandler_changesHandlerCorrectly() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = new Object();
        SimpleType withHandler = type.withValueHandler(handler);

        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getValueHandler());

        SimpleType sameHandler = withHandler.withValueHandler(handler);
        assertSame(withHandler, sameHandler);
    }

    // Tests withTypeHandler returns new instance when changed, same instance when unchanged
    @Test
    public void testWithTypeHandler_changesHandlerCorrectly() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = new Object();
        SimpleType withHandler = type.withTypeHandler(handler);

        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getTypeHandler());

        SimpleType sameHandler = withHandler.withTypeHandler(handler);
        assertSame(withHandler, sameHandler);
    }

    // Tests withStaticTyping toggles static typing flag
    @Test
    public void testWithStaticTyping_togglesStaticTyping() {
        SimpleType type = SimpleType.construct(String.class);
        assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        assertTrue(staticType.useStaticType());

        SimpleType sameStaticType = staticType.withStaticTyping();
        assertSame(staticType, sameStaticType);
    }

    // Tests refine returns null for SimpleType
    @Test
    public void testRefine_returnsNull() {
        SimpleType type = SimpleType.construct(String.class);
        assertNull(type.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    // Tests equals and hashCode implementation
    @Test
    public void testEquals_symmetricAndDifferentClassHandling() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        SimpleType type3 = SimpleType.construct(Integer.class);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertFalse(type1.equals(type3));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("non-type object"));
    }

    // Tests toString returns formatted simple type string
    @Test
    public void testToString_returnsCorrectFormat() {
        SimpleType type = SimpleType.construct(String.class);
        assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    // Tests signature generation methods
    @Test
    public void testGetSignatures_returnsValidSignatures() {
        SimpleType type = SimpleType.construct(String.class);

        StringBuilder erasedSb = new StringBuilder();
        type.getErasedSignature(erasedSb);
        assertEquals("Ljava/lang/String;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        type.getGenericSignature(genericSb);
        assertEquals("Ljava/lang/String;;", genericSb.toString());
    }

    // Tests multi-level inheritance superclass resolution via construct
    @Test
    public void testConstruct_multiLevelSuperclassResolution() {
        SimpleType type = SimpleType.construct(Integer.class);
        JavaType superClass = type.getSuperClass();
        assertNotNull(superClass);
        assertEquals(Number.class, superClass.getRawClass());

        JavaType rootClass = superClass.getSuperClass();
        assertNotNull(rootClass);
        assertEquals(Object.class, rootClass.getRawClass());
    }
}