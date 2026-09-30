package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import static org.junit.Assert.*;

public class CollectionLikeTypeTest {

    private JavaType stringType;
    private JavaType intType;
    private CollectionLikeType listLikeType;

    @Before
    public void setUp() {
        stringType = SimpleType.constructUnsafe(String.class);
        intType = SimpleType.constructUnsafe(Integer.class);
        listLikeType = CollectionLikeType.construct(List.class, stringType);
    }

    // Tests construction with TypeBindings and full parameters
    @Test
    public void testConstruct_withFullParameters_createsInstance() {
        TypeBindings bindings = TypeBindings.create(ArrayList.class, stringType);
        CollectionLikeType type = CollectionLikeType.construct(
                ArrayList.class, bindings, null, null, stringType);

        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(stringType, type.getContentType());
        assertTrue(type.isTrueCollectionType());
    }

    // Tests deprecated construct method with raw class and element type
    @Test
    public void testConstruct_withRawTypeAndElemType_createsInstance() {
        CollectionLikeType type = CollectionLikeType.construct(List.class, stringType);

        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(stringType, type.getContentType());
        assertTrue(type.isTrueCollectionType());
        assertTrue(type.isContainerType());
        assertTrue(type.isCollectionLikeType());
    }

    // Tests isTrueCollectionType for non-Collection raw class
    @Test
    public void testIsTrueCollectionType_nonCollectionClass_returnsFalse() {
        CollectionLikeType customType = CollectionLikeType.construct(String.class, intType);

        assertNotNull(customType);
        assertFalse(customType.isTrueCollectionType());
    }

    // Tests upgradeFrom method with valid TypeBase instance
    @Test
    public void testUpgradeFrom_validBaseType_success() {
        JavaType baseType = SimpleType.constructUnsafe(ArrayList.class);
        CollectionLikeType upgraded = CollectionLikeType.upgradeFrom(baseType, stringType);

        assertNotNull(upgraded);
        assertEquals(ArrayList.class, upgraded.getRawClass());
        assertEquals(stringType, upgraded.getContentType());
    }

    // Tests upgradeFrom with invalid non-TypeBase JavaType throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFrom_nonTypeBase_throwsException() {
        JavaType dummy = new JavaType(String.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;

            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withStaticTyping() { return this; }
            @Override
            public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override
            public boolean isContainerType() { return false; }
            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override
            public String toString() { return ""; }
            @Override
            public boolean equals(Object o) { return false; }
        };

        CollectionLikeType.upgradeFrom(dummy, stringType);
    }

    // Tests withContentType returning same instance when content type is identical
    @Test
    public void testWithContentType_sameType_returnsSameInstance() {
        JavaType result = listLikeType.withContentType(stringType);
        assertSame(listLikeType, result);
    }

    // Tests withContentType returning new instance when content type differs
    @Test
    public void testWithContentType_differentType_returnsNewInstance() {
        JavaType result = listLikeType.withContentType(intType);

        assertNotSame(listLikeType, result);
        assertEquals(intType, result.getContentType());
        assertEquals(listLikeType.getRawClass(), result.getRawClass());
    }

    // Tests withValueHandler and getContentValueHandler
    @Test
    public void testWithValueHandler_and_withContentValueHandler() {
        Object valHandler = "customValueHandler";
        Object contentValHandler = "customContentValHandler";

        CollectionLikeType withVal = listLikeType.withValueHandler(valHandler);
        assertEquals(valHandler, withVal.getValueHandler());
        assertTrue(withVal.hasHandlers());

        CollectionLikeType withContentVal = listLikeType.withContentValueHandler(contentValHandler);
        assertEquals(contentValHandler, withContentVal.getContentValueHandler());
        assertTrue(withContentVal.hasHandlers());
    }

    // Tests withTypeHandler and getContentTypeHandler
    @Test
    public void testWithTypeHandler_and_withContentTypeHandler() {
        Object typeHandler = "customTypeHandler";
        Object contentTypeHandler = "customContentTypeHandler";

        CollectionLikeType withType = listLikeType.withTypeHandler(typeHandler);
        assertEquals(typeHandler, withType.getTypeHandler());
        assertTrue(withType.hasHandlers());

        CollectionLikeType withContentType = listLikeType.withContentTypeHandler(contentTypeHandler);
        assertEquals(contentTypeHandler, withContentType.getContentTypeHandler());
        assertTrue(withContentType.hasHandlers());
    }

    // Tests hasHandlers when neither handler is set
    @Test
    public void testHasHandlers_noHandlers_returnsFalse() {
        assertFalse(listLikeType.hasHandlers());
        assertNull(listLikeType.getContentValueHandler());
        assertNull(listLikeType.getContentTypeHandler());
    }

    // Tests withStaticTyping behavior and idempotence
    @Test
    public void testWithStaticTyping_createsStaticInstance() {
        assertFalse(listLikeType.useStaticType());

        CollectionLikeType staticType = listLikeType.withStaticTyping();
        assertTrue(staticType.useStaticType());

        CollectionLikeType staticAgain = staticType.withStaticTyping();
        assertSame(staticType, staticAgain);
    }

    // Tests refine method updating raw class and bindings
    @Test
    public void testRefine_updatesClassAndBindings() {
        TypeBindings bindings = TypeBindings.create(LinkedList.class, stringType);
        JavaType refined = listLikeType.refine(LinkedList.class, bindings, null, null);

        assertNotNull(refined);
        assertEquals(LinkedList.class, refined.getRawClass());
        assertEquals(stringType, refined.getContentType());
    }

    // Tests _narrow method
    @Test
    public void testNarrow_returnsNarrowedType() {
        JavaType narrowed = listLikeType._narrow(LinkedList.class);

        assertNotNull(narrowed);
        assertEquals(LinkedList.class, narrowed.getRawClass());
        assertEquals(stringType, narrowed.getContentType());
    }

    // Tests signatures and canonical name generation
    @Test
    public void testSignatures_andCanonicalName() {
        StringBuilder sbErased = new StringBuilder();
        listLikeType.getErasedSignature(sbErased);
        assertEquals("Ljava/util/List;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        listLikeType.getGenericSignature(sbGeneric);
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", sbGeneric.toString());

        String canonical = listLikeType.toCanonical();
        assertEquals("java.util.List<java.lang.String>", canonical);
    }

    // Tests equals method branches
    @Test
    public void testEquals_variousScenarios() {
        assertTrue(listLikeType.equals(listLikeType));
        assertFalse(listLikeType.equals(null));
        assertFalse(listLikeType.equals("notAType"));

        CollectionLikeType same = CollectionLikeType.construct(List.class, stringType);
        assertTrue(listLikeType.equals(same));

        CollectionLikeType diffElem = CollectionLikeType.construct(List.class, intType);
        assertFalse(listLikeType.equals(diffElem));

        CollectionLikeType diffClass = CollectionLikeType.construct(ArrayList.class, stringType);
        assertFalse(listLikeType.equals(diffClass));
    }

    // Tests toString format
    @Test
    public void testToString_returnsExpectedString() {
        String str = listLikeType.toString();
        assertTrue(str.contains("[collection-like type; class java.util.List"));
        assertTrue(str.contains("contains " + stringType));
    }

    // Tests hasHandlers when element type has value handler or type handler
    @Test
    public void testHasHandlers_whenElementTypeHasHandlers_returnsTrue() {
        JavaType elemWithValueHandler = stringType.withValueHandler("elemValHandler");
        CollectionLikeType cltWithValue = CollectionLikeType.construct(List.class, elemWithValueHandler);
        assertTrue(cltWithValue.hasHandlers());

        JavaType elemWithTypeHandler = stringType.withTypeHandler("elemTypeHandler");
        CollectionLikeType cltWithType = CollectionLikeType.construct(List.class, elemWithTypeHandler);
        assertTrue(cltWithType.hasHandlers());
    }

    // Tests withContentTypeHandler and withContentValueHandler updates element type directly
    @Test
    public void testWithContentTypeAndValueHandlers_modifiesElementType() {
        CollectionLikeType withContentHandlers = listLikeType
                .withContentTypeHandler("typeH")
                .withContentValueHandler("valH");

        assertEquals("typeH", withContentHandlers.getContentType().getTypeHandler());
        assertEquals("valH", withContentHandlers.getContentType().getValueHandler());
        assertEquals("typeH", withContentHandlers.getContentTypeHandler());
        assertEquals("valH", withContentHandlers.getContentValueHandler());
    }

    // Tests refine method with superClass and superInterfaces
    @Test
    public void testRefine_withSuperClassAndSuperInterfaces() {
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        JavaType[] superInterfaces = new JavaType[] { SimpleType.constructUnsafe(Cloneable.class) };
        TypeBindings bindings = TypeBindings.create(ArrayList.class, stringType);

        JavaType refined = listLikeType.refine(ArrayList.class, bindings, superClass, superInterfaces);

        assertNotNull(refined);
        assertEquals(superClass, refined.getSuperClass());
        assertEquals(1, refined.getInterfaces().size());
        assertEquals(Cloneable.class, refined.getInterfaces().get(0).getRawClass());
    }

    // Tests upgradeFrom preserving handlers and static typing from baseType
    @Test
    public void testUpgradeFrom_preservesBaseTypeHandlersAndStatic() {
        JavaType baseType = SimpleType.constructUnsafe(ArrayList.class)
                .withValueHandler("vh")
                .withTypeHandler("th")
                .withStaticTyping();

        CollectionLikeType upgraded = CollectionLikeType.upgradeFrom(baseType, stringType);

        assertNotNull(upgraded);
        assertEquals("vh", upgraded.getValueHandler());
        assertEquals("th", upgraded.getTypeHandler());
        assertTrue(upgraded.useStaticType());
    }
}