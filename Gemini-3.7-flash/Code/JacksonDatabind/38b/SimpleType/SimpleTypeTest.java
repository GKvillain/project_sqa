package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeTest {

    // Tests normal construction via constructUnsafe
    @Test
    public void testConstructUnsafe_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertFalse(type.isContainerType());
    }

    // Tests normal construction via construct
    @Test
    public void testConstruct_validClass_returnsSimpleType() {
        SimpleType type = SimpleType.construct(Integer.class);
        assertNotNull(type);
        assertEquals(Integer.class, type.getRawClass());
        assertEquals(0, type.containedTypeCount());
    }

    // Tests construct with Map class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_mapClass_throwsIllegalArgumentException() {
        SimpleType.construct(HashMap.class);
    }

    // Tests construct with Collection class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_collectionClass_throwsIllegalArgumentException() {
        SimpleType.construct(ArrayList.class);
    }

    // Tests construct with Array class throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_arrayClass_throwsIllegalArgumentException() {
        SimpleType.construct(String[].class);
    }

    // Tests _narrow with identical subclass returns same instance
    @Test
    public void testNarrow_sameClass_returnsSameInstance() {
        SimpleType type = SimpleType.constructUnsafe(Number.class);
        JavaType narrowed = type._narrow(Number.class);
        assertSame(type, narrowed);
    }

    // Tests _narrow with actual subclass returns new SimpleType instance
    @Test
    public void testNarrow_subClass_returnsNewNarrowedInstance() {
        SimpleType type = SimpleType.constructUnsafe(Number.class);
        JavaType narrowed = type._narrow(Integer.class);
        assertNotSame(type, narrowed);
        assertEquals(Integer.class, narrowed.getRawClass());
    }

    // Tests withContentType unsupported throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentType_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentType(SimpleType.constructUnsafe(Integer.class));
    }

    // Tests withContentTypeHandler unsupported throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentTypeHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentTypeHandler("handler");
    }

    // Tests withContentValueHandler unsupported throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testWithContentValueHandler_throwsIllegalArgumentException() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        type.withContentValueHandler("handler");
    }

    // Tests withTypeHandler returns same instance when handler unchanged, or new instance when changed
    @Test
    public void testWithTypeHandler_setsHandlerCorrectly() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "handlerObject";
        SimpleType typed = type.withTypeHandler(handler);

        assertNotSame(type, typed);
        assertSame(handler, typed.getTypeHandler());
        assertSame(typed, typed.withTypeHandler(handler));
    }

    // Tests withValueHandler returns same instance when handler unchanged, or new instance when changed
    @Test
    public void testWithValueHandler_setsHandlerCorrectly() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        Object handler = "valueHandlerObject";
        SimpleType valued = type.withValueHandler(handler);

        assertNotSame(type, valued);
        assertSame(handler, valued.getValueHandler());
        assertSame(valued, valued.withValueHandler(handler));
    }

    // Tests withStaticTyping returns static type or same instance if already static
    @Test
    public void testWithStaticTyping_returnsStaticTypedInstance() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertFalse(type.useStaticType());

        SimpleType staticType = type.withStaticTyping();
        assertTrue(staticType.useStaticType());
        assertSame(staticType, staticType.withStaticTyping());
    }

    // Tests refine returns null for SimpleType
    @Test
    public void testRefine_returnsNull() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull(refined);
    }

    // Tests signatures and toString representation
    @Test
    public void testSignaturesAndToString_formatsCorrectly() {
        SimpleType type = SimpleType.constructUnsafe(String.class);

        StringBuilder erasedSb = new StringBuilder();
        type.getErasedSignature(erasedSb);
        assertEquals("Ljava/lang/String;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        type.getGenericSignature(genericSb);
        assertEquals("Ljava/lang/String;;", genericSb.toString());

        assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    // Tests equals method with various inputs
    @Test
    public void testEquals_variousInputs_returnsExpected() {
        SimpleType type1 = SimpleType.constructUnsafe(String.class);
        SimpleType type2 = SimpleType.constructUnsafe(String.class);
        SimpleType type3 = SimpleType.constructUnsafe(Integer.class);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("aString"));
        assertFalse(type1.equals(type3));
    }

    // Tests generic signatures and canonical name with TypeBindings
    @Test
    public void testBindingsGenericSignatureAndCanonicalName() {
        TypeBindings bindings = TypeBindings.create(
                SimpleType.class,
                new JavaType[] { SimpleType.constructUnsafe(String.class), SimpleType.constructUnsafe(Integer.class) }
        );
        SimpleType typeWithBindings = new SimpleType(
                SimpleType.class,
                bindings,
                null,
                null,
                null,
                null,
                false
        );

        assertEquals("com.fasterxml.jackson.databind.type.SimpleType<java.lang.String,java.lang.Integer>", typeWithCanonicalName(typeWithBindings));

        StringBuilder genericSb = new StringBuilder();
        typeWithBindings.getGenericSignature(genericSb);
        assertEquals("Lcom/fasterxml/jackson/databind/type/SimpleType<Ljava/lang/String;;Ljava/lang/Integer;;>;", genericSb.toString());
    }

    private String typeWithCanonicalName(SimpleType type) {
        return type.toCanonical();
    }

    // Tests containedType, containedTypeName, hasGenericTypes and bounds
    @Test
    public void testContainedTypesAndBindingsAccess() {
        SimpleType nonGeneric = SimpleType.constructUnsafe(String.class);
        assertFalse(nonGeneric.hasGenericTypes());
        assertEquals(0, nonGeneric.containedTypeCount());
        assertNull(nonGeneric.containedType(0));
        assertNull(nonGeneric.containedType(-1));
        assertNull(nonGeneric.containedTypeName(0));

        TypeBindings bindings = TypeBindings.create(
                SimpleType.class,
                new JavaType[] { SimpleType.constructUnsafe(String.class) }
        );
        SimpleType genericType = new SimpleType(
                SimpleType.class,
                bindings,
                null,
                null,
                null,
                null,
                false
        );

        assertTrue(genericType.hasGenericTypes());
        assertEquals(1, genericType.containedTypeCount());
        assertEquals(SimpleType.constructUnsafe(String.class), genericType.containedType(0));
        assertNull(genericType.containedType(1));
    }

    // Tests equals when raw class matches but type bindings differ
    @Test
    public void testEquals_differentBindings_returnsFalse() {
        TypeBindings bindings1 = TypeBindings.create(
                SimpleType.class,
                new JavaType[] { SimpleType.constructUnsafe(String.class) }
        );
        TypeBindings bindings2 = TypeBindings.create(
                SimpleType.class,
                new JavaType[] { SimpleType.constructUnsafe(Integer.class) }
        );

        SimpleType type1 = new SimpleType(SimpleType.class, bindings1, null, null, null, null, false);
        SimpleType type2 = new SimpleType(SimpleType.class, bindings2, null, null, null, null, false);

        assertFalse(type1.equals(type2));
    }

    // Tests canonical name without bindings
    @Test
    public void testCanonicalName_withoutBindings() {
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertEquals("java.lang.String", type.toCanonical());
    }

    // Tests constructor with superclass and interfaces
    @Test
    public void testSuperClassAndInterfaces() {
        JavaType superClass = SimpleType.constructUnsafe(Number.class);
        JavaType[] superInterfaces = new JavaType[] { SimpleType.constructUnsafe(Comparable.class) };

        SimpleType type = new SimpleType(
                Integer.class,
                TypeBindings.emptyBindings(),
                superClass,
                superInterfaces,
                null,
                null,
                false
        );

        assertEquals(superClass, type.getSuperClass());
        assertNotNull(type.getInterfaces());
        assertEquals(1, type.getInterfaces().size());
        assertEquals(Comparable.class, type.getInterfaces().get(0).getRawClass());
    }
}