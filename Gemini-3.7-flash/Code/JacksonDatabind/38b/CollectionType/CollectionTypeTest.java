package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class CollectionTypeTest {

    private JavaType stringType;
    private JavaType integerType;
    private TypeBindings bindings;
    private JavaType collectionSuperClass;

    @Before
    public void setUp() {
        stringType = SimpleType.constructUnsafe(String.class);
        integerType = SimpleType.constructUnsafe(Integer.class);
        bindings = TypeBindings.create(ArrayList.class, new JavaType[]{stringType});
        collectionSuperClass = SimpleType.constructUnsafe(Object.class);
    }

    // Tests construct with full arguments
    @Test
    public void testConstruct_fullArguments_createsValidCollectionType() {
        CollectionType type = CollectionType.construct(
                ArrayList.class,
                bindings,
                collectionSuperClass,
                new JavaType[0],
                stringType
        );

        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(stringType, type.getContentType());
        assertFalse(type.useStaticType());
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
    }

    // Tests deprecated 2-argument construct method
    @Test
    @SuppressWarnings("deprecation")
    public void testConstruct_twoArguments_createsValidCollectionType() {
        CollectionType type = CollectionType.construct(ArrayList.class, stringType);

        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(stringType, type.getContentType());
        assertFalse(type.useStaticType());
    }

    // Tests withContentType returning this when contentType is identical
    @Test
    public void testWithContentType_sameType_returnsSameInstance() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);

        JavaType result = type.withContentType(stringType);

        assertSame(type, result);
    }

    // Tests withContentType returning new instance when contentType is different
    @Test
    public void testWithContentType_differentType_returnsNewInstance() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);

        JavaType result = type.withContentType(integerType);

        assertNotSame(type, result);
        assertEquals(integerType, result.getContentType());
        assertEquals(ArrayList.class, result.getRawClass());
    }

    // Tests withTypeHandler assigning type handler
    @Test
    public void testWithTypeHandler_customHandler_setsHandlerCorrectly() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        Object handler = "customTypeHandler";

        CollectionType result = type.withTypeHandler(handler);

        assertNotSame(type, result);
        assertEquals(handler, result.getTypeHandler());
        assertNull(type.getTypeHandler());
    }

    // Tests withContentTypeHandler assigning handler to element type
    @Test
    public void testWithContentTypeHandler_customHandler_setsContentTypeHandler() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        Object handler = "customContentTypeHandler";

        CollectionType result = type.withContentTypeHandler(handler);

        assertNotSame(type, result);
        assertEquals(handler, result.getContentType().getTypeHandler());
    }

    // Tests withValueHandler assigning value handler
    @Test
    public void testWithValueHandler_customHandler_setsHandlerCorrectly() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        Object handler = "customValueHandler";

        CollectionType result = type.withValueHandler(handler);

        assertNotSame(type, result);
        assertEquals(handler, result.getValueHandler());
        assertNull(type.getValueHandler());
    }

    // Tests withContentValueHandler assigning value handler to element type
    @Test
    public void testWithContentValueHandler_customHandler_setsContentValueHandler() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        Object handler = "customContentValueHandler";

        CollectionType result = type.withContentValueHandler(handler);

        assertNotSame(type, result);
        assertEquals(handler, result.getContentType().getValueHandler());
    }

    // Tests withStaticTyping when static typing is false
    @Test
    public void testWithStaticTyping_nonStatic_enablesStaticTyping() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        assertFalse(type.useStaticType());

        CollectionType result = type.withStaticTyping();

        assertNotSame(type, result);
        assertTrue(result.useStaticType());
    }

    // Tests withStaticTyping when static typing is already enabled
    @Test
    public void testWithStaticTyping_alreadyStatic_returnsSameInstance() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);
        CollectionType staticType = type.withStaticTyping();

        CollectionType result = staticType.withStaticTyping();

        assertSame(staticType, result);
    }

    // Tests refine method updating rawType and bindings
    @Test
    public void testRefine_validNewParameters_returnsRefinedType() {
        CollectionType type = CollectionType.construct(
                List.class, TypeBindings.emptyBindings(), collectionSuperClass, null, stringType);
        TypeBindings newBindings = TypeBindings.create(ArrayList.class, new JavaType[]{stringType});
        JavaType newSuperClass = SimpleType.constructUnsafe(Object.class);

        JavaType result = type.refine(ArrayList.class, newBindings, newSuperClass, new JavaType[0]);

        assertNotSame(type, result);
        assertEquals(ArrayList.class, result.getRawClass());
        assertEquals(stringType, result.getContentType());
    }

    // Tests _narrow method for narrowing subclass
    @Test
    @SuppressWarnings("deprecation")
    public void testNarrow_subclass_narrowsRawType() {
        CollectionType type = CollectionType.construct(
                Collection.class, TypeBindings.emptyBindings(), collectionSuperClass, null, stringType);

        JavaType result = type._narrow(List.class);

        assertEquals(List.class, result.getRawClass());
        assertEquals(stringType, result.getContentType());
    }

    // Tests toString format
    @Test
    public void testToString_validType_returnsExpectedFormat() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);

        String result = type.toString();

        assertEquals("[collection type; class java.util.ArrayList, contains [simple type, class java.lang.String]]", result);
    }

    // Tests construct with super interfaces
    @Test
    public void testConstruct_withSuperInterfaces_setsSuperInterfaces() {
        JavaType[] superInterfaces = new JavaType[]{SimpleType.constructUnsafe(List.class)};
        CollectionType type = CollectionType.construct(
                ArrayList.class,
                bindings,
                collectionSuperClass,
                superInterfaces,
                stringType
        );

        assertNotNull(type);
        assertEquals(1, type.getInterfaces().size());
        assertEquals(List.class, type.getInterfaces().get(0).getRawClass());
    }

    // Tests withContentType preserves valueHandler, typeHandler, and staticTyping
    @Test
    public void testWithContentType_withHandlersAndStaticTyping_preservesAttributes() {
        Object valHandler = "vh";
        Object typeHandler = "th";
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType)
                .withValueHandler(valHandler)
                .withTypeHandler(typeHandler)
                .withStaticTyping();

        JavaType result = type.withContentType(integerType);

        assertNotSame(type, result);
        assertEquals(integerType, result.getContentType());
        assertEquals(valHandler, result.getValueHandler());
        assertEquals(typeHandler, result.getTypeHandler());
        assertTrue(result.useStaticType());
    }

    // Tests refine preserves valueHandler, typeHandler, and staticTyping
    @Test
    public void testRefine_withHandlersAndStaticTyping_preservesAttributes() {
        Object valHandler = "vh";
        Object typeHandler = "th";
        CollectionType type = CollectionType.construct(
                Collection.class, TypeBindings.emptyBindings(), collectionSuperClass, null, stringType)
                .withValueHandler(valHandler)
                .withTypeHandler(typeHandler)
                .withStaticTyping();

        TypeBindings newBindings = TypeBindings.create(ArrayList.class, new JavaType[]{stringType});
        JavaType result = type.refine(ArrayList.class, newBindings, collectionSuperClass, new JavaType[0]);

        assertNotSame(type, result);
        assertEquals(ArrayList.class, result.getRawClass());
        assertEquals(valHandler, result.getValueHandler());
        assertEquals(typeHandler, result.getTypeHandler());
        assertTrue(result.useStaticType());
    }

    // Tests withStaticTyping also propagates static typing to contentType
    @Test
    public void testWithStaticTyping_propagatesToContentType() {
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType);

        CollectionType result = type.withStaticTyping();

        assertTrue(result.useStaticType());
        assertTrue(result.getContentType().useStaticType());
    }

    // Tests _narrow preserves staticTyping
    @Test
    @SuppressWarnings("deprecation")
    public void testNarrow_withStaticTyping_preservesStaticTyping() {
        CollectionType type = CollectionType.construct(
                Collection.class, TypeBindings.emptyBindings(), collectionSuperClass, null, stringType)
                .withStaticTyping();

        JavaType result = type._narrow(List.class);

        assertEquals(List.class, result.getRawClass());
        assertTrue(result.useStaticType());
    }

    // Tests withContentTypeHandler preserves own handlers and staticTyping
    @Test
    public void testWithContentTypeHandler_withOwnHandlers_preservesAttributes() {
        Object valHandler = "vh";
        Object typeHandler = "th";
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType)
                .withValueHandler(valHandler)
                .withTypeHandler(typeHandler)
                .withStaticTyping();

        Object contentTH = "cth";
        CollectionType result = type.withContentTypeHandler(contentTH);

        assertNotSame(type, result);
        assertEquals(contentTH, result.getContentType().getTypeHandler());
        assertEquals(valHandler, result.getValueHandler());
        assertEquals(typeHandler, result.getTypeHandler());
        assertTrue(result.useStaticType());
    }

    // Tests withContentValueHandler preserves own handlers and staticTyping
    @Test
    public void testWithContentValueHandler_withOwnHandlers_preservesAttributes() {
        Object valHandler = "vh";
        Object typeHandler = "th";
        CollectionType type = CollectionType.construct(
                ArrayList.class, bindings, collectionSuperClass, null, stringType)
                .withValueHandler(valHandler)
                .withTypeHandler(typeHandler)
                .withStaticTyping();

        Object contentVH = "cvh";
        CollectionType result = type.withContentValueHandler(contentVH);

        assertNotSame(type, result);
        assertEquals(contentVH, result.getContentType().getValueHandler());
        assertEquals(valHandler, result.getValueHandler());
        assertEquals(typeHandler, result.getTypeHandler());
        assertTrue(result.useStaticType());
    }
}