package org.mockito.internal.util.reflection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenericMasterTest {

    private final GenericMaster genericMaster = new GenericMaster();

    private String nonGenericField;
    private List<String> stringListField;
    private Map<String, Number> stringKeyMapField;

    @SuppressWarnings("rawtypes")
    private List rawListField;

    private List<List<String>> nestedListField;

    // Tests false branch: non-parameterized field returns Object.class.
    @Test
    public void testGetGenericType_nonGenericField_returnsObjectClass() throws Exception {
        Field field = GenericMasterTest.class.getDeclaredField("nonGenericField");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests normal parameterized field: simple first type argument.
    @Test
    public void testGetGenericType_stringListField_returnsStringClass() throws Exception {
        Field field = GenericMasterTest.class.getDeclaredField("stringListField");
        assertEquals(String.class, genericMaster.getGenericType(field));
    }

    // Tests that the first type argument of a multi-argument generic type is returned.
    @Test
    public void testGetGenericType_mapField_returnsFirstTypeArgumentClass() throws Exception {
        Field field = GenericMasterTest.class.getDeclaredField("stringKeyMapField");
        assertEquals(String.class, genericMaster.getGenericType(field));
    }

    // Tests boundary: raw generic type is not ParameterizedType and returns Object.class.
    @Test
    public void testGetGenericType_rawListField_returnsObjectClass() throws Exception {
        Field field = GenericMasterTest.class.getDeclaredField("rawListField");
        assertEquals(Object.class, genericMaster.getGenericType(field));
    }

    // Tests regression: nested generic field returns raw type of its actual type argument.
    @Test
    public void testGetGenericType_nestedGenericField_returnsRawTypeClass() throws Exception {
        Field field = GenericMasterTest.class.getDeclaredField("nestedListField");
        assertEquals(List.class, genericMaster.getGenericType(field));
    }

    // Tests invalid null input.
    @Test(expected = NullPointerException.class)
    public void testGetGenericType_nullField_throwsNullPointerException() {
        genericMaster.getGenericType(null);
    }
}