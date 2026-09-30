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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ClassNameIdResolverTest {

    private ObjectMapper _mapper;
    private TypeFactory _typeFactory;
    private JavaType _baseType;
    private ClassNameIdResolver _resolver;
    private SerializerProvider _serializerProvider;
    private DeserializationContext _deserContext;

    // Test enum for enum subtypes testing
    private enum TestEnum {
        VALUE_A {
            @Override
            public String toString() {
                return "A";
            }
        },
        VALUE_B
    }

    // Non-static inner class for testing inner class handling
    private class InnerNonStatic {
    }

    // Static nested class
    private static class StaticNested {
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _typeFactory = _mapper.getTypeFactory();
        _baseType = _typeFactory.constructType(Object.class);
        _resolver = new ClassNameIdResolver(_baseType, _typeFactory, LaissezFaireSubTypeValidator.instance);
        _serializerProvider = _mapper.getSerializerProviderInstance();
        _deserContext = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) _mapper.getDeserializationContext())
                .createInstance(_mapper.getDeserializationConfig(), null, null);
    }

    @Test
    public void testConstruct_viaFactoryMethod() {
        ClassNameIdResolver resolver = ClassNameIdResolver.construct(_baseType, _mapper.getDeserializationConfig(), LaissezFaireSubTypeValidator.instance);
        assertNotNull(resolver);
        assertEquals(JsonTypeInfo.Id.CLASS, resolver.getMechanism());
    }

    @Test
    public void testGetMechanism_returnsClassMechanism() {
        assertEquals(JsonTypeInfo.Id.CLASS, _resolver.getMechanism());
    }

    @Test
    public void testGetDescForKnownTypeIds_returnsExpectedDescription() {
        assertEquals("class name used as type id", _resolver.getDescForKnownTypeIds());
    }

    @Test
    public void testRegisterSubtype_doesNotThrow() {
        _resolver.registerSubtype(String.class, "customName");
    }

    @Test
    public void testIdFromBaseType_returnsBaseTypeCanonicalName() {
        JavaType stringType = _typeFactory.constructType(String.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(stringType, _typeFactory, LaissezFaireSubTypeValidator.instance);
        assertEquals("java.lang.String", resolver.idFromBaseType());
    }

    @Test
    public void testIdFromValue_plainObject_returnsCanonicalClassName() {
        String id = _resolver.idFromValue("a simple string");
        assertEquals("java.lang.String", id);
    }

    @Test
    public void testIdFromValue_plainEnum_returnsEnumClassName() {
        String id = _resolver.idFromValue(TestEnum.VALUE_B);
        assertEquals(TestEnum.class.getName(), id);
    }

    @Test
    public void testIdFromValue_enumSubclass_returnsBaseEnumClassName() {
        String id = _resolver.idFromValue(TestEnum.VALUE_A);
        assertEquals(TestEnum.class.getName(), id);
    }

    @Test
    public void testIdFromValue_enumSet_returnsConstructedCollectionCanonicalType() {
        EnumSet<TestEnum> set = EnumSet.of(TestEnum.VALUE_A, TestEnum.VALUE_B);
        String id = _resolver.idFromValue(set);
        String expected = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class).toCanonical();
        assertEquals(expected, id);
    }

    @Test
    public void testIdFromValue_emptyEnumSet_returnsDefaultEnumSetType() {
        EnumSet<TestEnum> set = EnumSet.noneOf(TestEnum.class);
        String id = _resolver.idFromValue(set);
        assertNotNull(id);
        assertTrue(id.contains("EnumSet"));
    }

    @Test
    public void testIdFromValue_enumMap_returnsConstructedMapCanonicalType() {
        EnumMap<TestEnum, String> map = new EnumMap<TestEnum, String>(TestEnum.class);
        map.put(TestEnum.VALUE_A, "val");
        String id = _resolver.idFromValue(map);
        String expected = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, Object.class).toCanonical();
        assertEquals(expected, id);
    }

    @Test
    public void testIdFromValue_emptyEnumMap_returnsDefaultEnumMapType() {
        EnumMap<TestEnum, String> map = new EnumMap<TestEnum, String>(TestEnum.class);
        String id = _resolver.idFromValue(map);
        assertNotNull(id);
        assertTrue(id.contains("EnumMap"));
    }

    @Test
    public void testIdFromValue_arraysAsList_returnsArrayListClassName() {
        List<String> list = Arrays.asList("one", "two");
        String id = _resolver.idFromValue(list);
        assertEquals("java.util.ArrayList", id);
    }

    @Test
    public void testIdFromValue_nonStaticInnerClass_revertsToBaseType() {
        InnerNonStatic inner = new InnerNonStatic();
        String id = _resolver.idFromValue(inner);
        assertEquals(Object.class.getName(), id);
    }

    @Test
    public void testIdFromValue_staticNestedClass_returnsActualClassName() {
        StaticNested nested = new StaticNested();
        String id = _resolver.idFromValue(nested);
        assertEquals(StaticNested.class.getName(), id);
    }

    @Test
    public void testIdFromValueAndType_explicitType_returnsExplicitClassName() {
        String id = _resolver.idFromValueAndType("test", Integer.class);
        assertEquals("java.lang.Integer", id);
    }

    @Test
    public void testIdFromValueAndType_nullValueWithExplicitType() {
        String id = _resolver.idFromValueAndType(null, Double.class);
        assertEquals("java.lang.Double", id);
    }

    @Test
    public void testTypeFromId_basicClassName_returnsSpecializedJavaType() throws Exception {
        JavaType type = _resolver.typeFromId(_serializerProvider, "java.lang.String");
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testTypeFromId_genericCanonicalString_returnsGenericJavaType() throws Exception {
        String genericId = "java.util.ArrayList<java.lang.String>";
        JavaType type = _resolver.typeFromId(_serializerProvider, genericId);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testTypeFromId_nonExistentClass_returnsNullWhenNotDeserializationContext() throws Exception {
        JavaType type = _resolver.typeFromId(_serializerProvider, "com.nonexistent.Class12345");
        assertNull(type);
    }

    @Test(expected = InvalidTypeIdException.class)
    public void testTypeFromId_nonExistentClass_throwsWhenDeserializationContext() throws Exception {
        _resolver.typeFromId(_deserContext, "com.nonexistent.Class12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypeFromId_invalidTypeSyntax_throwsIllegalArgumentException() throws Exception {
        _resolver.typeFromId(_serializerProvider, "java.lang.String<invalid syntax");
    }
}