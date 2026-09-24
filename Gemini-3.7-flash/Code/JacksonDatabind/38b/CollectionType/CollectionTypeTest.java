package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

public class CollectionTypeTest {

    private JavaType stringType;
    private JavaType integerType;
    private CollectionType collectionType;

    @Before
    public void setUp() {
        stringType = SimpleType.constructUnsafe(String.class);
        integerType = SimpleType.constructUnsafe(Integer.class);
        collectionType = CollectionType.construct(ArrayList.class, stringType);
    }

    // Tests construct with raw class and element type
    @Test
    public void testConstruct_rawTypeAndElementType_returnsValidCollectionType() {
        CollectionType ct = CollectionType.construct(ArrayList.class, stringType);
        assertNotNull(ct);
        assertEquals(ArrayList.class, ct.getRawClass());
        assertEquals(stringType, ct.getContentType());
        assertTrue(ct.isCollectionLikeType());
        assertTrue(ct.isContainerType());
    }

    // Tests construct with full type bindings, super class, and interfaces
    @Test
    public void testConstruct_fullParameters_returnsValidCollectionType() {
        TypeBindings bindings = TypeBindings.create(ArrayList.class, stringType);
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[] { SimpleType.constructUnsafe(List.class) };

        CollectionType ct = CollectionType.construct(ArrayList.class, bindings, superClass, superInterfaces, stringType);
        assertNotNull(ct);
        assertEquals(ArrayList.class, ct.getRawClass());
        assertEquals(stringType, ct.getContentType());
        assertEquals(superClass, ct.getSuperClass());
        assertEquals(Arrays.asList(superInterfaces), ct.getInterfaces());
    }

    // Tests withContentType when passing the same element type (true branch)
    @Test
    public void testWithContentType_sameContentType_returnsSameInstance() {
        JavaType result = collectionType.withContentType(stringType);
        assertSame(collectionType, result);
    }

    // Tests withContentType when passing a different element type (false branch)
    @Test
    public void testWithContentType_differentContentType_returnsNewInstance() {
        JavaType result = collectionType.withContentType(integerType);
        assertNotSame(collectionType, result);
        assertEquals(integerType, result.getContentType());
        assertEquals(ArrayList.class, result.getRawClass());
    }

    // Tests withTypeHandler sets handler on collection type
    @Test
    public void testWithTypeHandler_customHandler_returnsNewInstanceWithHandler() {
        String handler = "customTypeHandler";
        CollectionType result = collectionType.withTypeHandler(handler);

        assertNotSame(collectionType, result);
        assertEquals(handler, result.getTypeHandler());
        assertNull(collectionType.getTypeHandler());
    }

    // Tests withContentTypeHandler sets handler on element type
    @Test
    public void testWithContentTypeHandler_customHandler_returnsNewInstanceWithContentHandler() {
        String handler = "customContentTypeHandler";
        CollectionType result = collectionType.withContentTypeHandler(handler);

        assertNotSame(collectionType, result);
        assertEquals(handler, result.getContentType().getTypeHandler());
        assertNull(collectionType.getContentType().getTypeHandler());
    }

    // Tests withValueHandler sets handler on collection type
    @Test
    public void testWithValueHandler_customHandler_returnsNewInstanceWithValueHandler() {
        String handler = "customValueHandler";
        CollectionType result = collectionType.withValueHandler(handler);

        assertNotSame(collectionType, result);
        assertEquals(handler, result.getValueHandler());
        assertNull(collectionType.getValueHandler());
    }

    // Tests withContentValueHandler sets handler on element type
    @Test
    public void testWithContentValueHandler_customHandler_returnsNewInstanceWithContentValueHandler() {
        String handler = "customContentValueHandler";
        CollectionType result = collectionType.withContentValueHandler(handler);

        assertNotSame(collectionType, result);
        assertEquals(handler, result.getContentType().getValueHandler());
        assertNull(collectionType.getContentType().getValueHandler());
    }

    // Tests withStaticTyping when static typing is not yet enabled (false branch)
    @Test
    public void testWithStaticTyping_initiallyDynamic_returnsInstanceWithStaticTyping() {
        assertFalse(collectionType.useStaticType());

        CollectionType result = collectionType.withStaticTyping();
        assertNotSame(collectionType, result);
        assertTrue(result.useStaticType());
        assertTrue(result.getContentType().useStaticType());
    }

    // Tests withStaticTyping when static typing is already enabled (true branch)
    @Test
    public void testWithStaticTyping_alreadyStatic_returnsSameInstance() {
        CollectionType staticCt = collectionType.withStaticTyping();
        CollectionType result = staticCt.withStaticTyping();

        assertSame(staticCt, result);
    }

    // Tests refine method correctly updates raw class, bindings, and supertypes
    @Test
    public void testRefine_validParameters_returnsRefinedCollectionType() {
        TypeBindings bindings = TypeBindings.create(LinkedList.class, stringType);
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[] { SimpleType.constructUnsafe(List.class) };

        JavaType refined = collectionType.refine(LinkedList.class, bindings, superClass, superInterfaces);
        assertNotNull(refined);
        assertEquals(LinkedList.class, refined.getRawClass());
        assertEquals(stringType, refined.getContentType());
        assertEquals(superClass, refined.getSuperClass());
        assertEquals(Arrays.asList(superInterfaces), refined.getInterfaces());
    }

    // Tests _narrow method updates subclass correctly
    @Test
    @SuppressWarnings("deprecation")
    public void testNarrow_subclass_returnsNarrowedCollectionType() {
        JavaType narrowed = collectionType._narrow(LinkedList.class);
        assertNotNull(narrowed);
        assertEquals(LinkedList.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getContentType());
    }

    // Tests toString format
    @Test
    public void testToString_validCollectionType_returnsExpectedFormat() {
        String expected = "[collection type; class " + ArrayList.class.getName() + ", contains " + stringType + "]";
        assertEquals(expected, collectionType.toString());
    }
}