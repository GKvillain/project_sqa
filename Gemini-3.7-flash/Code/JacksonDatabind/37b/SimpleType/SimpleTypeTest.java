package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleTypeTest {

    // Tests normal construction of SimpleType with valid raw class
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
    }

    // Tests exception path when constructing SimpleType with Map class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    // Tests exception path when constructing SimpleType with Collection class
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    // Tests exception path when constructing SimpleType with array class
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
    }

    // Tests _narrow when subclass is identical to current raw class
    @Test
    public void testNarrow_sameClass_returnsSameInstance() {
        SimpleType type = SimpleType.constructUnsafe(Number.class);
        assertSame(type, type._narrow(Number.class));
    }

    // Tests _narrow when subclass is different
    @Test
    public void testNarrow_differentClass_returnsNewInstance() {
        SimpleType type = SimpleType.constructUnsafe(Number.class);
        SimpleType narrowed = (SimpleType) type._narrow(Integer.class);
        assertNotSame(type, narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
    }

    // Tests exception path for withContentType
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_anyInput_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentType(type);
    }

    // Tests exception path for withContentTypeHandler
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_anyHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentTypeHandler("handler");
    }

    // Tests exception path for withContentValueHandler
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_anyHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentValueHandler("handler");
    }

    // Tests withTypeHandler when setting a new handler and when setting the same handler
    @Test
    public void testWithTypeHandler_validHandler_returnsUpdatedInstanceOrSame() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "customTypeHandler";

        SimpleType withHandler = type.withTypeHandler(handler);
        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getTypeHandler());

        assertSame(withHandler, withHandler.withTypeHandler(handler));
    }

    // Tests withValueHandler when setting a new handler and when setting the same handler
    @Test
    public void testWithValueHandler_validHandler_returnsUpdatedInstanceOrSame() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "customValueHandler";

        SimpleType withHandler = type.withValueHandler(handler);
        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getValueHandler());

        assertSame(withHandler, withHandler.withValueHandler(handler));
    }

    // Tests withStaticTyping on non-static and already-static SimpleType
    @Test
    public void testWithStaticTyping_nonStaticAndStatic_returnsExpectedInstance() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        assertNotSame(type, staticType);
        assertTrue(staticType.useStaticType());

        assertSame(staticType, staticType.withStaticTyping());
    }

    // Tests refine method behavior
    @Test
    public void testRefine_validInput_returnsNull() {
        SimpleType type = SimpleType.constructUnsafe(Object.class);
        assertNull(type.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    // Tests erased and generic signature generation
    @Test
    public void testGetSignatures_validClass_appendsCorrectSignature() {
        SimpleType type = SimpleType.constructUnsafe(String.class);

        StringBuilder erasedSb = new StringBuilder();
        type.getErasedSignature(erasedSb);
        assertEquals("Ljava/lang/String;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        type.getGenericSignature(genericSb);
        assertEquals("Ljava/lang/String;", genericSb.toString());
    }

    // Tests toString formatting
    @Test
    public void testToString_validType_returnsFormattedString() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    // Tests equals method with various scenarios
    @Test
    public void testEquals_variousScenarios_returnsCorrectBoolean() {
        SimpleType type1 = SimpleType.constructUnsafe(String.class);
        SimpleType type2 = SimpleType.constructUnsafe(String.class);
        SimpleType type3 = SimpleType.constructUnsafe(Integer.class);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("notAType"));
        assertFalse(type1.equals(type3));
    }
}