package org.mockito.internal.util.reflection;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GenericMasterTest {

    private GenericMaster genericMaster;

    public static class Container {
        public String nonGenericField;
        public int primitiveField;
        @SuppressWarnings("rawtypes")
        public List rawListField;
        public List<String> stringListField;
        public Set<Integer> integerSetField;
        public Map<Double, String> mapField;
        public Collection<Boolean> booleanCollectionField;
        public Set<List<String>> nestedGenericField;
        public Map<Set<Integer>, String> nestedMapKeyField;
        public List<Map<String, Object>> nestedListMapField;
        public List<Comparable<String>> nestedComparableField;
    }

    @Before
    public void setUp() {
        genericMaster = new GenericMaster();
    }

    // Tests non-generic Object field returns Object.class
    @Test
    public void testGetGenericType_nonGenericField_returnsObjectClass() throws Exception {
        Field field = Container.class.getField("nonGenericField");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests primitive field returns Object.class
    @Test
    public void testGetGenericType_primitiveField_returnsObjectClass() throws Exception {
        Field field = Container.class.getField("primitiveField");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests raw type generic field returns Object.class
    @Test
    public void testGetGenericType_rawTypeField_returnsObjectClass() throws Exception {
        Field field = Container.class.getField("rawListField");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests single parameterized generic field with String
    @Test
    public void testGetGenericType_stringListField_returnsStringClass() throws Exception {
        Field field = Container.class.getField("stringListField");
        assertEquals(String.class, genericMaster.getGenericType(field));
    }

    // Tests single parameterized generic field with Integer
    @Test
    public void testGetGenericType_integerSetField_returnsIntegerClass() throws Exception {
        Field field = Container.class.getField("integerSetField");
        assertEquals(Integer.class, genericMaster.getGenericType(field));
    }

    // Tests single parameterized generic field with Boolean
    @Test
    public void testGetGenericType_booleanCollectionField_returnsBooleanClass() throws Exception {
        Field field = Container.class.getField("booleanCollectionField");
        assertEquals(Boolean.class, genericMaster.getGenericType(field));
    }

    // Tests multi-parameter generic field returns first type argument
    @Test
    public void testGetGenericType_mapField_returnsFirstTypeArgument() throws Exception {
        Field field = Container.class.getField("mapField");
        assertEquals(Double.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic type field returns raw type of inner generic
    @Test
    public void testGetGenericType_nestedGenericField_returnsRawTypeOfInnerGeneric() throws Exception {
        Field field = Container.class.getField("nestedGenericField");
        assertEquals(List.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic map key field returns raw type of inner generic
    @Test
    public void testGetGenericType_nestedMapKeyField_returnsRawTypeOfInnerGeneric() throws Exception {
        Field field = Container.class.getField("nestedMapKeyField");
        assertEquals(Set.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic list map field returns raw type of inner generic
    @Test
    public void testGetGenericType_nestedListMapField_returnsRawTypeOfInnerGeneric() throws Exception {
        Field field = Container.class.getField("nestedListMapField");
        assertEquals(Map.class, genericMaster.getGenericType(field));
    }

    // Tests nested generic interface field returns raw type of inner generic
    @Test
    public void testGetGenericType_nestedComparableField_returnsRawTypeOfInnerGeneric() throws Exception {
        Field field = Container.class.getField("nestedComparableField");
        assertEquals(Comparable.class, genericMaster.getGenericType(field));
    }
}