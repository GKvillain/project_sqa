package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

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

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues re = new ReturnsEmptyValues();

    // Helper interfaces for mocking methods with specific return types
    private interface HasObjectMethod { Object getObject(); }
    private interface HasListMethod { List<?> getList(); }

    @Test
    public void testReturnValueFor_primitiveAndWrapper_returnsDefaultValues() {
        assertEquals(0, ((Integer) re.returnValueFor(int.class)).intValue());
        assertEquals(0L, ((Long) re.returnValueFor(long.class)).longValue());
        assertEquals(false, ((Boolean) re.returnValueFor(boolean.class)).booleanValue());
        assertEquals('\0', ((Character) re.returnValueFor(char.class)).charValue());
        assertEquals(0, ((Integer) re.returnValueFor(Integer.class)).intValue());
        assertEquals(0L, ((Long) re.returnValueFor(Long.class)).longValue());
        assertEquals(false, ((Boolean) re.returnValueFor(Boolean.class)).booleanValue());
    }

    @Test
    public void testReturnValueFor_CollectionTypes_returnsEmptyCollections() {
        Object col = re.returnValueFor(Collection.class);
        assertTrue(col instanceof LinkedList);
        assertTrue(((Collection<?>) col).isEmpty());

        Object set = re.returnValueFor(Set.class);
        assertTrue(set instanceof HashSet);

        Object hashSet = re.returnValueFor(HashSet.class);
        assertTrue(hashSet instanceof HashSet);

        Object sortedSet = re.returnValueFor(SortedSet.class);
        assertTrue(sortedSet instanceof TreeSet);

        Object treeSet = re.returnValueFor(TreeSet.class);
        assertTrue(treeSet instanceof TreeSet);

        Object linkedHashSet = re.returnValueFor(LinkedHashSet.class);
        assertTrue(linkedHashSet instanceof LinkedHashSet);

        Object list = re.returnValueFor(List.class);
        assertTrue(list instanceof LinkedList);

        Object linkedList = re.returnValueFor(LinkedList.class);
        assertTrue(linkedList instanceof LinkedList);

        Object arrayList = re.returnValueFor(ArrayList.class);
        assertTrue(arrayList instanceof ArrayList);
    }

    @Test
    public void testReturnValueFor_MapTypes_returnsEmptyMaps() {
        Object map = re.returnValueFor(Map.class);
        assertTrue(map instanceof HashMap);

        Object hashMap = re.returnValueFor(HashMap.class);
        assertTrue(hashMap instanceof HashMap);

        Object sortedMap = re.returnValueFor(SortedMap.class);
        assertTrue(sortedMap instanceof TreeMap);

        Object treeMap = re.returnValueFor(TreeMap.class);
        assertTrue(treeMap instanceof TreeMap);

        Object linkedHashMap = re.returnValueFor(LinkedHashMap.class);
        assertTrue(linkedHashMap instanceof LinkedHashMap);
    }

    @Test
    public void testReturnValueFor_unknownType_returnsNull() {
        assertNull(re.returnValueFor(String.class));
        assertNull(re.returnValueFor(Object.class));
        assertNull(re.returnValueFor(java.util.Date.class));
    }

    @Test(expected = NullPointerException.class)
    public void testReturnValueFor_nullInput_throwsNullPointerException() {
        re.returnValueFor(null);
    }

    @Test
    public void testReturnValueFor_Iterable_shouldReturnEmptyCollection() {
        // This test will fail because Iterable is not handled, revealing defect.
        Object result = re.returnValueFor(Iterable.class);
        assertNotNull("Iterable should not return null", result);
        assertTrue("Iterable should return a collection", result instanceof Collection);
        assertTrue("Collection should be empty", ((Collection<?>) result).isEmpty());
    }

    @Test
    public void testAnswer_toString_defaultName_returnsDescription() {
        Object mock = Mockito.mock(Object.class);
        String str = mock.toString();
        assertTrue(str, str.startsWith("Mock for Object, hashCode: "));
    }

    @Test
    public void testAnswer_toString_customName_returnsName() {
        Object mock = Mockito.mock(Object.class, Mockito.withSettings().name("customMock"));
        assertEquals("customMock", mock.toString());
    }

    @Test
    public void testAnswer_compareTo_sameReference_returnsZero() {
        // Bug: current implementation returns 1 for same reference, expecting 0
        Comparable<Object> mock = Mockito.mock(Comparable.class);
        assertEquals(0, mock.compareTo(mock));
    }

    @Test
    public void testAnswer_compareTo_differentReference_returnsNonZero() {
        Comparable<Object> mock1 = Mockito.mock(Comparable.class);
        Comparable<Object> mock2 = Mockito.mock(Comparable.class);
        assertNotEquals(0, mock1.compareTo(mock2));
    }

    @Test
    public void testAnswer_nonPrimitiveNonCollectionMethod_returnsNull() {
        HasObjectMethod mock = Mockito.mock(HasObjectMethod.class);
        assertNull(mock.getObject());
    }

    @Test
    public void testAnswer_collectionReturningMethod_returnsEmptyCollection() {
        HasListMethod mock = Mockito.mock(HasListMethod.class);
        List<?> result = mock.getList();
        assertTrue(result instanceof LinkedList);
        assertTrue(result.isEmpty());
    }
}