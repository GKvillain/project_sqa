package org.mockito.internal.util.reflection;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GenericMasterTest {

    private GenericMaster genericMaster;

    public String nonGenericString;
    public int primitiveInt;
    public List rawList;
    public List<String> stringList;
    public Set<Integer> integerSet;
    public List<GenericMasterTest> customTypeList;
    public Set<List<String>> nestedGeneric;
    public Map<Set<String>, Integer> mapNestedKey;
    public Map<String, List<Integer>> mapSimpleKeyNestedVal;
    public Comparable<Double> comparableDouble;

    @Before
    public void setUp() {
        genericMaster = new GenericMaster();
    }

    private Field getField(String fieldName) throws NoSuchFieldException {
        return getClass().getField(fieldName);
    }

    // Tests non-generic reference type field
    @Test
    public void testGetGenericType_nonGenericField_returnsObjectClass() throws NoSuchFieldException {
        Field field = getField("nonGenericString");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests primitive type field
    @Test
    public void testGetGenericType_primitiveField_returnsObjectClass() throws NoSuchFieldException {
        Field field = getField("primitiveInt");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests raw type field without generics
    @Test
    public void testGetGenericType_rawTypeField_returnsObjectClass() throws NoSuchFieldException {
        Field field = getField("rawList");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests simple parameterized generic list
    @Test
    public void testGetGenericType_simpleGenericList_returnsTypeArgument() throws NoSuchFieldException {
        Field field = getField("stringList");
        assertEquals(String.class, genericMaster.getGenericType(field));
    }

    // Tests simple parameterized generic set
    @Test
    public void testGetGenericType_simpleGenericSet_returnsTypeArgument() throws NoSuchFieldException {
        Field field = getField("integerSet");
        assertEquals(Integer.class, genericMaster.getGenericType(field));
    }

    // Tests generic field with custom class parameter
    @Test
    public void testGetGenericType_customTypeGeneric_returnsCustomClass() throws NoSuchFieldException {
        Field field = getField("customTypeList");
        assertEquals(GenericMasterTest.class, genericMaster.getGenericType(field));
    }

    // Tests parameterized generic interface
    @Test
    public void testGetGenericType_genericInterface_returnsTypeArgument() throws NoSuchFieldException {
        Field field = getField("comparableDouble");
        assertEquals(Double.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic parameterized type
    @Test
    public void testGetGenericType_nestedGenericCollection_returnsRawType() throws NoSuchFieldException {
        Field field = getField("nestedGeneric");
        assertEquals(List.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic in first type argument of map
    @Test
    public void testGetGenericType_nestedGenericMapKey_returnsRawType() throws NoSuchFieldException {
        Field field = getField("mapNestedKey");
        assertEquals(Set.class, genericMaster.getGenericType(field));
    }

    // Tests map with simple key and nested generic value
    @Test
    public void testGetGenericType_mapSimpleKey_returnsFirstTypeArgument() throws NoSuchFieldException {
        Field field = getField("mapSimpleKeyNestedVal");
        assertEquals(String.class, genericMaster.getGenericType(field));
    }
}