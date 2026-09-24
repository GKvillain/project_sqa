package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import static org.junit.Assert.*;

public class CollectionLikeTypeTest {

    private TypeFactory _typeFactory;
    private JavaType _elemType;
    private CollectionLikeType _collectionLikeType;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _elemType = _typeFactory.constructType(String.class);
        _collectionLikeType = CollectionLikeType.construct(ArrayList.class, _elemType);
    }

    // Tests construction with construct(Class, JavaType)
    @Test
    public void testConstruct_singleTypeParam_constructsSuccessfully() {
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, _elemType);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(_elemType, type.getContentType());
        assertTrue(type.isCollectionLikeType());
        assertTrue(type.isContainerType());
    }

    // Tests construct(Class, JavaType) when class has no type parameters
    @Test
    public void testConstruct_noTypeParams_constructsWithEmptyBindings() {
        CollectionLikeType type = CollectionLikeType.construct(String.class, _elemType);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertEquals(_elemType, type.getContentType());
    }

    // Tests construct with explicit bindings and super types
    @Test
    public void testConstruct_withBindingsAndSuperTypes_constructsSuccessfully() {
        TypeBindings bindings = TypeBindings.create(ArrayList.class, _elemType);
        CollectionLikeType type = CollectionLikeType.construct(
                ArrayList.class, bindings, null, null, _elemType);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(_elemType, type.getContentType());
    }

    // Tests upgradeFrom valid TypeBase
    @Test
    public void testUpgradeFrom_validTypeBase_returnsCollectionLikeType() {
        JavaType baseType = SimpleType.constructUnsafe(ArrayList.class);
        CollectionLikeType upgraded = CollectionLikeType.upgradeFrom(baseType, _elemType);
        assertNotNull(upgraded);
        assertEquals(ArrayList.class, upgraded.getRawClass());
        assertEquals(_elemType, upgraded.getContentType());
    }

    // Tests upgradeFrom invalid base type throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeFrom_invalidBaseType_throwsIllegalArgumentException() {
        JavaType invalidBase = new JavaType(String.class, 0, null, null, false) {
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
        CollectionLikeType.upgradeFrom(invalidBase, _elemType);
    }

    // Tests withContentType with same and different content types
    @Test
    public void testWithContentType_sameAndDifferent_returnsExpectedInstance() {
        JavaType same = _collectionLikeType.withContentType(_elemType);
        assertSame(_collectionLikeType, same);

        JavaType intType = _typeFactory.constructType(Integer.class);
        JavaType diff = _collectionLikeType.withContentType(intType);
        assertNotSame(_collectionLikeType, diff);
        assertEquals(intType, diff.getContentType());
    }

    // Tests withTypeHandler and withValueHandler
    @Test
    public void testWithTypeAndValueHandler_validHandlers_handlersSetCorrectly() {
        String typeHandler = "typeH";
        String valueHandler = "valH";
        CollectionLikeType withTH = _collectionLikeType.withTypeHandler(typeHandler);
        assertEquals(typeHandler, withTH.getTypeHandler());

        CollectionLikeType withVH = _collectionLikeType.withValueHandler(valueHandler);
        assertEquals(valueHandler, withVH.getValueHandler());
    }

    // Tests withContentTypeHandler and withContentValueHandler
    @Test
    public void testWithContentTypeAndContentValueHandler_validHandlers_contentHandlersSetCorrectly() {
        String typeH = "cTypeH";
        String valH = "cValH";
        CollectionLikeType withCTH = _collectionLikeType.withContentTypeHandler(typeH);
        assertEquals(typeH, withCTH.getContentTypeHandler());

        CollectionLikeType withCVH = _collectionLikeType.withContentValueHandler(valH);
        assertEquals(valH, withCVH.getContentValueHandler());
    }

    // Tests withStaticTyping on dynamic and static instances
    @Test
    public void testWithStaticTyping_dynamicAndStatic_returnsCorrectInstance() {
        assertFalse(_collectionLikeType.useStaticType());
        CollectionLikeType staticType = _collectionLikeType.withStaticTyping();
        assertTrue(staticType.useStaticType());

        CollectionLikeType staticTypeAgain = staticType.withStaticTyping();
        assertSame(staticType, staticTypeAgain);
    }

    // Tests refine method
    @Test
    public void testRefine_differentRawClass_returnsRefinedInstance() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType refined = _collectionLikeType.refine(LinkedList.class, bindings, null, null);
        assertEquals(LinkedList.class, refined.getRawClass());
        assertEquals(_elemType, refined.getContentType());
    }

    // Tests deprecated _narrow method
    @Test
    public void testNarrow_differentSubclass_returnsNarrowedInstance() {
        JavaType narrowed = _collectionLikeType._narrow(LinkedList.class);
        assertEquals(LinkedList.class, narrowed.getRawClass());
        assertEquals(_elemType, narrowed.getContentType());
    }

    // Tests hasHandlers when handlers are absent vs present on self or element type
    @Test
    public void testHasHandlers_variousHandlerPlacements_returnsExpectedBoolean() {
        assertFalse(_collectionLikeType.hasHandlers());

        CollectionLikeType withValH = _collectionLikeType.withValueHandler("valH");
        assertTrue(withValH.hasHandlers());

        CollectionLikeType withContentValH = _collectionLikeType.withContentValueHandler("elemValH");
        assertTrue(withContentValH.hasHandlers());
    }

    // Tests isTrueCollectionType for Collection and non-Collection types
    @Test
    public void testIsTrueCollectionType_collectionAndCustomTypes_returnsCorrectBoolean() {
        assertTrue(_collectionLikeType.isTrueCollectionType());

        CollectionLikeType customType = CollectionLikeType.construct(String.class, _elemType);
        assertFalse(customType.isTrueCollectionType());
    }

    // Tests getErasedSignature and getGenericSignature
    @Test
    public void testSignatures_validType_buildsCorrectSignatures() {
        StringBuilder erased = new StringBuilder();
        _collectionLikeType.getErasedSignature(erased);
        assertEquals("Ljava/util/ArrayList;", erased.toString());

        StringBuilder generic = new StringBuilder();
        _collectionLikeType.getGenericSignature(generic);
        assertEquals("Ljava/util/ArrayList<Ljava/lang/String;>;", generic.toString());
    }

    // Tests buildCanonicalName
    @Test
    public void testToCanonical_validType_returnsCanonicalString() {
        String canonical = _collectionLikeType.toCanonical();
        assertEquals("java.util.ArrayList<java.lang.String>", canonical);
    }

    // Tests equals and toString methods
    @Test
    public void testEqualsAndToString_variousObjects_returnsExpectedResults() {
        assertTrue(_collectionLikeType.equals(_collectionLikeType));
        assertFalse(_collectionLikeType.equals(null));
        assertFalse(_collectionLikeType.equals("some string"));

        CollectionLikeType same = CollectionLikeType.construct(ArrayList.class, _elemType);
        assertTrue(_collectionLikeType.equals(same));

        JavaType intType = _typeFactory.constructType(Integer.class);
        CollectionLikeType diffElem = CollectionLikeType.construct(ArrayList.class, intType);
        assertFalse(_collectionLikeType.equals(diffElem));

        CollectionLikeType diffClass = CollectionLikeType.construct(LinkedList.class, _elemType);
        assertFalse(_collectionLikeType.equals(diffClass));

        String str = _collectionLikeType.toString();
        assertTrue(str.contains("collection-like type"));
        assertTrue(str.contains("ArrayList"));
    }
}