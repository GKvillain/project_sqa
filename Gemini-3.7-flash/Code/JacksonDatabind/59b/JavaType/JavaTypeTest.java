package com.fasterxml.jackson.databind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();

    // Tests raw class checks and object type detection
    @Test
    public void testGetRawClass_objectType_returnsCorrectProperties() {
        JavaType type = _typeFactory.constructType(Object.class);
        assertEquals(Object.class, type.getRawClass());
        assertTrue(type.hasRawClass(Object.class));
        assertFalse(type.hasRawClass(String.class));
        assertTrue(type.isJavaLangObject());
        assertFalse(type.isAbstract());
        assertTrue(type.isConcrete());
        assertFalse(type.isPrimitive());
        assertFalse(type.isInterface());
        assertFalse(type.isEnumType());
        assertFalse(type.isFinal());
        assertFalse(type.isThrowable());
    }

    // Tests primitive type handling
    @Test
    public void testIsPrimitive_primitiveType_returnsTrueAndIsConcrete() {
        JavaType type = _typeFactory.constructType(int.class);
        assertTrue(type.isPrimitive());
        assertTrue(type.isConcrete());
        assertFalse(type.isAbstract());
        assertFalse(type.isInterface());
    }

    // Tests interface and abstract type detection
    @Test
    public void testIsInterface_interfaceType_returnsTrueAndNotConcrete() {
        JavaType type = _typeFactory.constructType(List.class);
        assertTrue(type.isInterface());
        assertTrue(type.isAbstract());
        assertFalse(type.isConcrete());
        assertFalse(type.isFinal());
    }

    // Tests enum type detection
    @Test
    public void testIsEnumType_enumClass_returnsTrue() {
        JavaType type = _typeFactory.constructType(Thread.State.class);
        assertTrue(type.isEnumType());
        assertFalse(type.isInterface());
    }

    // Tests throwable hierarchy check
    @Test
    public void testIsThrowable_exceptionType_returnsTrue() {
        JavaType type = _typeFactory.constructType(IllegalArgumentException.class);
        assertTrue(type.isThrowable());
        assertFalse(type.isPrimitive());
    }

    // Tests subtyping check logic with matching and non-matching classes
    @Test
    public void testIsTypeOrSubTypeOf_variousClasses_returnsExpectedBoolean() {
        JavaType type = _typeFactory.constructType(ArrayList.class);
        assertTrue(type.isTypeOrSubTypeOf(ArrayList.class));
        assertTrue(type.isTypeOrSubTypeOf(List.class));
        assertTrue(type.isTypeOrSubTypeOf(Object.class));
        assertFalse(type.isTypeOrSubTypeOf(Map.class));
        assertFalse(type.isTypeOrSubTypeOf(String.class));
    }

    // Tests default implementations for non-container types
    @Test
    public void testDefaultAccessors_simpleType_returnsDefaultValues() {
        JavaType type = _typeFactory.constructType(String.class);
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertNull(type.getReferencedType());
        assertNull(type.getParameterSource());
        assertNull(type.getContentValueHandler());
        assertNull(type.getContentTypeHandler());
        assertFalse(type.isArrayType());
        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        assertTrue(type.hasContentType());
    }

    // Tests containedTypeOrUnknown when type parameter is out of range
    @Test
    public void testContainedTypeOrUnknown_indexOutOfBounds_returnsUnknownType() {
        JavaType type = _typeFactory.constructType(String.class);
        assertEquals(0, type.containedTypeCount());
        JavaType result = type.containedTypeOrUnknown(0);
        assertNotNull(result);
        assertTrue(result.isJavaLangObject());
    }

    // Tests containedTypeOrUnknown with a parameterized type
    @Test
    public void testContainedTypeOrUnknown_validIndex_returnsContainedType() {
        JavaType type = _typeFactory.constructType(new TypeReference<List<String>>() {});
        assertTrue(type.hasGenericTypes());
        assertEquals(1, type.containedTypeCount());
        JavaType elemType = type.containedTypeOrUnknown(0);
        assertNotNull(elemType);
        assertEquals(String.class, elemType.getRawClass());
    }

    // Tests value and type handler attachments
    @Test
    public void testHandlers_attachedHandlers_returnsTrueForHasHandlers() {
        JavaType baseType = _typeFactory.constructType(String.class);
        assertFalse(baseType.hasHandlers());
        assertFalse(baseType.hasValueHandler());
        assertNull(baseType.getValueHandler());
        assertNull(baseType.getTypeHandler());

        Object dummyValHandler = "valHandler";
        JavaType withVal = baseType.withValueHandler(dummyValHandler);
        assertTrue(withVal.hasHandlers());
        assertTrue(withVal.hasValueHandler());
        assertEquals(dummyValHandler, withVal.getValueHandler());

        Object dummyTypeHandler = "typeHandler";
        JavaType withType = baseType.withTypeHandler(dummyTypeHandler);
        assertTrue(withType.hasHandlers());
        assertEquals(dummyTypeHandler, withType.getTypeHandler());
    }

    // Tests useStaticType flag via withStaticTyping
    @Test
    public void testUseStaticType_staticTypingEnabled_returnsTrue() {
        JavaType dynamicType = _typeFactory.constructType(CharSequence.class);
        assertFalse(dynamicType.useStaticType());

        JavaType staticType = dynamicType.withStaticTyping();
        assertTrue(staticType.useStaticType());
    }

    // Tests signatures generation
    @Test
    public void testSignatures_stringType_returnsCorrectSignatures() {
        JavaType type = _typeFactory.constructType(String.class);
        assertEquals("Ljava/lang/String;", type.getErasedSignature());
        assertEquals("Ljava/lang/String;", type.getGenericSignature());
    }

    // Tests hashCode stability
    @Test
    public void testHashCode_equalTypes_haveSameHashCode() {
        JavaType type1 = _typeFactory.constructType(HashMap.class);
        JavaType type2 = _typeFactory.constructType(HashMap.class);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    // Tests forcedNarrowBy with identical class returning the same instance
    @Test
    public void testForcedNarrowBy_sameClass_returnsSameInstance() {
        JavaType type = _typeFactory.constructType(Number.class);
        JavaType result = type.forcedNarrowBy(Number.class);
        assertSame(type, result);
    }

    // Tests forcedNarrowBy with subclass preserving handlers
    @Test
    public void testForcedNarrowBy_subclassWithHandlers_preservesHandlers() {
        JavaType base = _typeFactory.constructType(Number.class)
                .withValueHandler("vh")
                .withTypeHandler("th");

        JavaType narrowed = base.forcedNarrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals("vh", narrowed.getValueHandler());
        assertEquals("th", narrowed.getTypeHandler());
    }

    // Tests container detection and properties for Collection, Map, Array and Reference types
    @Test
    public void testContainerTypes_collectionMapArrayAndReference_identifiedCorrectly() {
        JavaType listType = _typeFactory.constructType(new TypeReference<List<String>>() {});
        assertTrue(listType.isContainerType());
        assertTrue(listType.isCollectionLikeType());
        assertFalse(listType.isMapLikeType());
        assertFalse(listType.isArrayType());
        assertNotNull(listType.getContentType());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType mapType = _typeFactory.constructType(new TypeReference<Map<String, Integer>>() {});
        assertTrue(mapType.isContainerType());
        assertTrue(mapType.isMapLikeType());
        assertNotNull(mapType.getKeyType());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertNotNull(mapType.getContentType());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());

        JavaType arrayType = _typeFactory.constructType(String[].class);
        assertTrue(arrayType.isContainerType());
        assertTrue(arrayType.isArrayType());
        assertNotNull(arrayType.getContentType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());

        JavaType refType = _typeFactory.constructType(new TypeReference<AtomicReference<Long>>() {});
        assertTrue(refType.isReferenceType());
        assertNotNull(refType.getReferencedType());
        assertEquals(Long.class, refType.getReferencedType().getRawClass());
    }

    // Tests containedTypeName and containedType methods
    @Test
    public void testContainedTypeAndName_parameterizedMap_returnsCorrectNamesAndTypes() {
        JavaType mapType = _typeFactory.constructType(new TypeReference<Map<String, Integer>>() {});
        assertEquals(2, mapType.containedTypeCount());
        assertEquals("K", mapType.containedTypeName(0));
        assertEquals("V", mapType.containedTypeName(1));
        assertNull(mapType.containedTypeName(2));
        assertNull(mapType.containedTypeName(-1));

        assertEquals(String.class, mapType.containedType(0).getRawClass());
        assertEquals(Integer.class, mapType.containedType(1).getRawClass());
        assertNull(mapType.containedType(2));
        assertNull(mapType.containedType(-1));
    }

    // Tests hierarchy navigation: super class, interfaces, findSuperType
    @Test
    public void testHierarchyNavigation_subclass_findsSuperClassAndInterfaces() {
        JavaType arrayListType = _typeFactory.constructType(ArrayList.class);
        JavaType superClass = arrayListType.getSuperClass();
        assertNotNull(superClass);
        assertEquals(java.util.AbstractList.class, superClass.getRawClass());

        List<JavaType> interfaces = arrayListType.getInterfaces();
        assertNotNull(interfaces);
        assertFalse(interfaces.isEmpty());

        JavaType foundList = arrayListType.findSuperType(List.class);
        assertNotNull(foundList);
        assertEquals(List.class, foundList.getRawClass());

        JavaType foundSelf = arrayListType.findSuperType(ArrayList.class);
        assertSame(arrayListType, foundSelf);

        JavaType notFound = arrayListType.findSuperType(Map.class);
        assertNull(notFound);
    }

    // Tests content handlers on container types
    @Test
    public void testContentHandlers_listType_handlersAttachedCorrectly() {
        JavaType listType = _typeFactory.constructType(new TypeReference<List<String>>() {});
        Object contentValHandler = "cvh";
        Object contentTypeHandler = "cth";

        JavaType withContentHandlers = listType.withContentValueHandler(contentValHandler)
                .withContentTypeHandler(contentTypeHandler);

        assertTrue(withContentHandlers.hasHandlers());
        assertEquals(contentValHandler, withContentHandlers.getContentValueHandler());
        assertEquals(contentTypeHandler, withContentHandlers.getContentTypeHandler());
    }

    // Tests toCanonical method
    @Test
    public void testToCanonical_simpleAndParameterized_returnsExpectedCanonicalString() {
        JavaType stringType = _typeFactory.constructType(String.class);
        assertEquals("java.lang.String", stringType.toCanonical());

        JavaType listType = _typeFactory.constructType(new TypeReference<List<String>>() {});
        assertEquals("java.util.List<java.lang.String>", listType.toCanonical());
    }

    // Tests equals method across different instances and types
    @Test
    public void testEquals_variousJavaTypes_returnsExpectedEquality() {
        JavaType type1 = _typeFactory.constructType(String.class);
        JavaType type2 = _typeFactory.constructType(String.class);
        JavaType intType = _typeFactory.constructType(Integer.class);

        assertTrue(type1.equals(type1));
        assertTrue(type1.equals(type2));
        assertFalse(type1.equals(null));
        assertFalse(type1.equals("not a JavaType"));
        assertFalse(type1.equals(intType));
    }

    // Tests getBindings accessor
    @Test
    public void testGetBindings_parameterizedType_returnsNonNullBindings() {
        JavaType mapType = _typeFactory.constructType(new TypeReference<Map<String, Double>>() {});
        TypeBindings bindings = mapType.getBindings();
        assertNotNull(bindings);
        assertEquals(2, bindings.size());
        assertEquals(String.class, bindings.getBoundType(0).getRawClass());
        assertEquals(Double.class, bindings.getBoundType(1).getRawClass());
    }

    // Tests StringBuilder overload of signatures
    @Test
    public void testSignaturesWithStringBuilder_appendsSignatureCorrectly() {
        JavaType type = _typeFactory.constructType(Integer.class);
        StringBuilder sbErased = new StringBuilder("prefix:");
        StringBuilder sbGeneric = new StringBuilder("prefix:");

        assertSame(sbErased, type.getErasedSignature(sbErased));
        assertEquals("prefix:Ljava/lang/Integer;", sbErased.toString());

        assertSame(sbGeneric, type.getGenericSignature(sbGeneric));
        assertEquals("prefix:Ljava/lang/Integer;", sbGeneric.toString());
    }
}