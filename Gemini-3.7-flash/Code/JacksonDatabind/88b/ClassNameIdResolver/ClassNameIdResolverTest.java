package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ClassNameIdResolverTest {

    private TypeFactory _typeFactory;
    private DeserializationContext _context;
    private JavaType _baseType;
    private ClassNameIdResolver _resolver;

    private enum SampleEnum {
        A {
            @Override
            public String toString() {
                return "A_VAL";
            }
        },
        B
    }

    private class NonStaticInner {
    }

    @Before
    public void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        _typeFactory = mapper.getTypeFactory();
        _context = mapper.getDeserializationContext();
        _baseType = _typeFactory.constructType(Object.class);
        _resolver = new ClassNameIdResolver(_baseType, _typeFactory);
    }

    // Tests mechanism returns CLASS
    @Test
    public void testGetMechanism_returnsClassId() {
        assertEquals(JsonTypeInfo.Id.CLASS, _resolver.getMechanism());
    }

    // Tests description for known type ids
    @Test
    public void testGetDescForKnownTypeIds_returnsDescription() {
        assertEquals("class name used as type id", _resolver.getDescForKnownTypeIds());
    }

    // Tests registerSubtype does not alter resolver state or fail
    @Test
    public void testRegisterSubtype_doesNothing() {
        _resolver.registerSubtype(String.class, "string");
        assertEquals(JsonTypeInfo.Id.CLASS, _resolver.getMechanism());
    }

    // Tests standard object class id resolution
    @Test
    public void testIdFromValue_standardObject_returnsClassName() {
        String id = _resolver.idFromValue("Hello World");
        assertEquals(String.class.getName(), id);
    }

    // Tests idFromValueAndType with explicit type
    @Test
    public void testIdFromValueAndType_explicitType_returnsProvidedClassName() {
        String id = _resolver.idFromValueAndType("Hello", Integer.class);
        assertEquals(Integer.class.getName(), id);
    }

    // Tests enum constant with specialized class body
    @Test
    public void testIdFromValue_enumWithSubclass_returnsEnumClassName() {
        String id = _resolver.idFromValue(SampleEnum.A);
        assertEquals(SampleEnum.class.getName(), id);
    }

    // Tests standard enum constant without specialized class body
    @Test
    public void testIdFromValue_standardEnum_returnsEnumClassName() {
        String id = _resolver.idFromValue(SampleEnum.B);
        assertEquals(SampleEnum.class.getName(), id);
    }

    // Tests EnumSet handling in java.util package
    @Test
    public void testIdFromValue_enumSet_returnsCanonicalType() {
        EnumSet<SampleEnum> set = EnumSet.of(SampleEnum.B);
        String id = _resolver.idFromValue(set);
        String expected = _typeFactory.constructCollectionType(EnumSet.class, SampleEnum.class).toCanonical();
        assertEquals(expected, id);
    }

    // Tests EnumMap handling in java.util package
    @Test
    public void testIdFromValue_enumMap_returnsCanonicalType() {
        EnumMap<SampleEnum, String> map = new EnumMap<SampleEnum, String>(SampleEnum.class);
        map.put(SampleEnum.B, "test");
        String id = _resolver.idFromValue(map);
        String expected = _typeFactory.constructMapType(EnumMap.class, SampleEnum.class, Object.class).toCanonical();
        assertEquals(expected, id);
    }

    // Tests Arrays.asList wrapper mapped to java.util.ArrayList
    @Test
    public void testIdFromValue_arraysAsList_returnsArrayList() {
        List<String> list = Arrays.asList("a", "b");
        String id = _resolver.idFromValue(list);
        assertEquals("java.util.ArrayList", id);
    }

    // Tests non-static inner class generalizing to base type
    @Test
    public void testIdFromValue_nonStaticInnerClass_generalizesToBaseType() {
        NonStaticInner inner = new NonStaticInner();
        String id = _resolver.idFromValue(inner);
        assertEquals(Object.class.getName(), id);
    }

    // Tests type resolution from generic id
    @Test
    public void testTypeFromId_genericId_returnsConstructedJavaType() throws IOException {
        String id = "java.util.ArrayList<java.lang.String>";
        JavaType resultType = _resolver.typeFromId(_context, id);
        assertNotNull(resultType);
        assertEquals(ArrayList.class, resultType.getRawClass());
        assertEquals(String.class, resultType.getContentType().getRawClass());
    }

    // Tests standard type resolution from non-generic class name
    @Test
    public void testTypeFromId_standardClassName_returnsJavaType() throws IOException {
        String id = String.class.getName();
        JavaType resultType = _resolver.typeFromId(_context, id);
        assertNotNull(resultType);
        assertEquals(String.class, resultType.getRawClass());
    }

    // Tests subtype specialization when resolving from class id
    @Test
    public void testTypeFromId_specializedSubtype_returnsSpecializedJavaType() throws IOException {
        JavaType mapBaseType = _typeFactory.constructType(Map.class);
        ClassNameIdResolver mapResolver = new ClassNameIdResolver(mapBaseType, _typeFactory);
        JavaType resultType = mapResolver.typeFromId(_context, HashMap.class.getName());
        assertNotNull(resultType);
        assertEquals(HashMap.class, resultType.getRawClass());
        assertTrue(Map.class.isAssignableFrom(resultType.getRawClass()));
    }

    // Tests unknown class name handling
    @Test
    public void testTypeFromId_unknownClassName_returnsNullOrHandled() throws IOException {
        JavaType resultType = _resolver.typeFromId(_context, "com.fasterxml.jackson.nonexistent.Class123");
        assertNull(resultType);
    }

    // Tests invalid class name syntax throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTypeFromId_invalidClassName_throwsIllegalArgumentException() throws IOException {
        _resolver.typeFromId(_context, "invalid-class-name;;");
    }
}