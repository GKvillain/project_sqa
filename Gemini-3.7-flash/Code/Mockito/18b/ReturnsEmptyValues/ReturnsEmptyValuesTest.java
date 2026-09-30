package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    // Tests primitive type handling
    @Test
    public void testReturnValueFor_primitiveInt_returnsZero() {
        Object result = returnsEmptyValues.returnValueFor(int.class);
        assertEquals(0, result);
    }

    // Tests primitive boolean type handling
    @Test
    public void testReturnValueFor_primitiveBoolean_returnsFalse() {
        Object result = returnsEmptyValues.returnValueFor(boolean.class);
        assertEquals(false, result);
    }

    // Tests primitive wrapper type handling
    @Test
    public void testReturnValueFor_wrapperInteger_returnsZero() {
        Object result = returnsEmptyValues.returnValueFor(Integer.class);
        assertEquals(0, result);
    }

    // Tests Iterable interface return value (Defects4J 18b)
    @Test
    public void testReturnValueFor_iterableInterface_returnsEmptyIterable() {
        Object result = returnsEmptyValues.returnValueFor(Iterable.class);
        assertNotNull(result);
        assertTrue(result instanceof Iterable);
        assertFalse(((Iterable<?>) result).iterator().hasNext());
    }

    // Tests Collection interface return value
    @Test
    public void testReturnValueFor_collectionInterface_returnsEmptyLinkedList() {
        Object result = returnsEmptyValues.returnValueFor(Collection.class);
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
        assertTrue(((Collection<?>) result).isEmpty());
    }

    // Tests Set interface return value
    @Test
    public void testReturnValueFor_setInterface_returnsEmptyHashSet() {
        Object result = returnsEmptyValues.returnValueFor(Set.class);
        assertNotNull(result);
        assertTrue(result instanceof HashSet);
        assertTrue(((Set<?>) result).isEmpty());
    }

    // Tests HashSet class return value
    @Test
    public void testReturnValueFor_hashSetClass_returnsEmptyHashSet() {
        Object result = returnsEmptyValues.returnValueFor(HashSet.class);
        assertNotNull(result);
        assertTrue(result instanceof HashSet);
        assertTrue(((HashSet<?>) result).isEmpty());
    }

    // Tests SortedSet interface return value
    @Test
    public void testReturnValueFor_sortedSetInterface_returnsEmptyTreeSet() {
        Object result = returnsEmptyValues.returnValueFor(SortedSet.class);
        assertNotNull(result);
        assertTrue(result instanceof TreeSet);
        assertTrue(((SortedSet<?>) result).isEmpty());
    }

    // Tests TreeSet class return value
    @Test
    public void testReturnValueFor_treeSetClass_returnsEmptyTreeSet() {
        Object result = returnsEmptyValues.returnValueFor(TreeSet.class);
        assertNotNull(result);
        assertTrue(result instanceof TreeSet);
        assertTrue(((TreeSet<?>) result).isEmpty());
    }

    // Tests LinkedHashSet class return value
    @Test
    public void testReturnValueFor_linkedHashSetClass_returnsEmptyLinkedHashSet() {
        Object result = returnsEmptyValues.returnValueFor(LinkedHashSet.class);
        assertNotNull(result);
        assertTrue(result instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) result).isEmpty());
    }

    // Tests List interface return value
    @Test
    public void testReturnValueFor_listInterface_returnsEmptyLinkedList() {
        Object result = returnsEmptyValues.returnValueFor(List.class);
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
        assertTrue(((List<?>) result).isEmpty());
    }

    // Tests LinkedList class return value
    @Test
    public void testReturnValueFor_linkedListClass_returnsEmptyLinkedList() {
        Object result = returnsEmptyValues.returnValueFor(LinkedList.class);
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
        assertTrue(((LinkedList<?>) result).isEmpty());
    }

    // Tests ArrayList class return value
    @Test
    public void testReturnValueFor_arrayListClass_returnsEmptyArrayList() {
        Object result = returnsEmptyValues.returnValueFor(ArrayList.class);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
        assertTrue(((ArrayList<?>) result).isEmpty());
    }

    // Tests Map interface return value
    @Test
    public void testReturnValueFor_mapInterface_returnsEmptyHashMap() {
        Object result = returnsEmptyValues.returnValueFor(Map.class);
        assertNotNull(result);
        assertTrue(result instanceof HashMap);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    // Tests HashMap class return value
    @Test
    public void testReturnValueFor_hashMapClass_returnsEmptyHashMap() {
        Object result = returnsEmptyValues.returnValueFor(HashMap.class);
        assertNotNull(result);
        assertTrue(result instanceof HashMap);
        assertTrue(((HashMap<?, ?>) result).isEmpty());
    }

    // Tests SortedMap interface return value
    @Test
    public void testReturnValueFor_sortedMapInterface_returnsEmptyTreeMap() {
        Object result = returnsEmptyValues.returnValueFor(SortedMap.class);
        assertNotNull(result);
        assertTrue(result instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) result).isEmpty());
    }

    // Tests TreeMap class return value
    @Test
    public void testReturnValueFor_treeMapClass_returnsEmptyTreeMap() {
        Object result = returnsEmptyValues.returnValueFor(TreeMap.class);
        assertNotNull(result);
        assertTrue(result instanceof TreeMap);
        assertTrue(((TreeMap<?, ?>) result).isEmpty());
    }

    // Tests LinkedHashMap class return value
    @Test
    public void testReturnValueFor_linkedHashMapClass_returnsEmptyLinkedHashMap() {
        Object result = returnsEmptyValues.returnValueFor(LinkedHashMap.class);
        assertNotNull(result);
        assertTrue(result instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) result).isEmpty());
    }

    // Tests unsupported non-collection reference type returns null
    @Test
    public void testReturnValueFor_nonCollectionType_returnsNull() {
        Object result = returnsEmptyValues.returnValueFor(String.class);
        assertNull(result);
    }

    // Tests null type input returns null
    @Test
    public void testReturnValueFor_nullType_returnsNull() {
        Object result = returnsEmptyValues.returnValueFor(null);
        assertNull(result);
    }

    // Tests primitive byte and wrapper Byte
    @Test
    public void testReturnValueFor_byteAndByteWrapper() {
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(byte.class));
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(Byte.class));
    }

    // Tests primitive short and wrapper Short
    @Test
    public void testReturnValueFor_shortAndShortWrapper() {
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(short.class));
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(Short.class));
    }

    // Tests primitive long and wrapper Long
    @Test
    public void testReturnValueFor_longAndLongWrapper() {
        assertEquals(0L, returnsEmptyValues.returnValueFor(long.class));
        assertEquals(0L, returnsEmptyValues.returnValueFor(Long.class));
    }

    // Tests primitive float and wrapper Float
    @Test
    public void testReturnValueFor_floatAndFloatWrapper() {
        assertEquals(0.0F, (Float) returnsEmptyValues.returnValueFor(float.class), 0.0F);
        assertEquals(0.0F, (Float) returnsEmptyValues.returnValueFor(Float.class), 0.0F);
    }

    // Tests primitive double and wrapper Double
    @Test
    public void testReturnValueFor_doubleAndDoubleWrapper() {
        assertEquals(0.0D, (Double) returnsEmptyValues.returnValueFor(double.class), 0.0D);
        assertEquals(0.0D, (Double) returnsEmptyValues.returnValueFor(Double.class), 0.0D);
    }

    // Tests primitive char and wrapper Character
    @Test
    public void testReturnValueFor_charAndCharacterWrapper() {
        assertEquals('\u0000', returnsEmptyValues.returnValueFor(char.class));
        assertEquals('\u0000', returnsEmptyValues.returnValueFor(Character.class));
    }

    // Tests Boolean wrapper
    @Test
    public void testReturnValueFor_wrapperBoolean() {
        assertEquals(false, returnsEmptyValues.returnValueFor(Boolean.class));
    }

    // Tests empty array return value for Object array
    @Test
    public void testReturnValueFor_objectArray_returnsEmptyArray() {
        Object result = returnsEmptyValues.returnValueFor(String[].class);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertEquals(0, Array.getLength(result));
    }

    // Tests empty array return value for primitive array
    @Test
    public void testReturnValueFor_primitiveArray_returnsEmptyArray() {
        Object result = returnsEmptyValues.returnValueFor(int[].class);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertEquals(0, Array.getLength(result));
    }
}