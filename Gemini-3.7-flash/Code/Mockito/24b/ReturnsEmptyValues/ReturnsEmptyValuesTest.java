package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
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

import static org.junit.Assert.*;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    // Tests primitive and primitive wrapper types return their default values
    @Test
    public void testReturnValueFor_primitiveAndWrapperTypes_returnsDefaultValues() {
        assertEquals(0, returnsEmptyValues.returnValueFor(int.class));
        assertEquals(0, returnsEmptyValues.returnValueFor(Integer.class));
        assertEquals(false, returnsEmptyValues.returnValueFor(boolean.class));
        assertEquals(false, returnsEmptyValues.returnValueFor(Boolean.class));
        assertEquals((byte) 0, returnsEmptyValues.returnValueFor(byte.class));
        assertEquals((short) 0, returnsEmptyValues.returnValueFor(short.class));
        assertEquals(0L, returnsEmptyValues.returnValueFor(long.class));
        assertEquals(0.0f, returnsEmptyValues.returnValueFor(float.class));
        assertEquals(0.0d, returnsEmptyValues.returnValueFor(double.class));
        assertEquals('\u0000', returnsEmptyValues.returnValueFor(char.class));
    }

    // Tests collection interfaces return appropriate empty collection instances
    @Test
    public void testReturnValueFor_collectionInterfaces_returnsEmptyCollections() {
        assertTrue(returnsEmptyValues.returnValueFor(Collection.class) instanceof LinkedList);
        assertTrue(returnsEmptyValues.returnValueFor(List.class) instanceof LinkedList);
        assertTrue(returnsEmptyValues.returnValueFor(Set.class) instanceof HashSet);
        assertTrue(returnsEmptyValues.returnValueFor(SortedSet.class) instanceof TreeSet);
        assertTrue(returnsEmptyValues.returnValueFor(Map.class) instanceof HashMap);
        assertTrue(returnsEmptyValues.returnValueFor(SortedMap.class) instanceof TreeMap);
    }

    // Tests concrete collection classes return appropriate empty collection instances
    @Test
    public void testReturnValueFor_concreteCollectionClasses_returnsEmptyInstances() {
        assertTrue(returnsEmptyValues.returnValueFor(LinkedList.class) instanceof LinkedList);
        assertTrue(returnsEmptyValues.returnValueFor(ArrayList.class) instanceof ArrayList);
        assertTrue(returnsEmptyValues.returnValueFor(HashSet.class) instanceof HashSet);
        assertTrue(returnsEmptyValues.returnValueFor(TreeSet.class) instanceof TreeSet);
        assertTrue(returnsEmptyValues.returnValueFor(LinkedHashSet.class) instanceof LinkedHashSet);
        assertTrue(returnsEmptyValues.returnValueFor(HashMap.class) instanceof HashMap);
        assertTrue(returnsEmptyValues.returnValueFor(TreeMap.class) instanceof TreeMap);
        assertTrue(returnsEmptyValues.returnValueFor(LinkedHashMap.class) instanceof LinkedHashMap);
    }

    // Tests unhandled reference types return null
    @Test
    public void testReturnValueFor_nonCollectionTypes_returnsNull() {
        assertNull(returnsEmptyValues.returnValueFor(String.class));
        assertNull(returnsEmptyValues.returnValueFor(Object.class));
    }

    // Tests compareTo method returns 0 when comparing a mock to itself
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAnswer_compareToSameMock_returnsZero() {
        Comparable mock = Mockito.mock(Comparable.class);
        assertEquals(0, mock.compareTo(mock));
    }

    // Tests compareTo method returns non-zero when comparing a mock to another object
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAnswer_compareToDifferentMock_returnsNonZero() {
        Comparable mock1 = Mockito.mock(Comparable.class);
        Comparable mock2 = Mockito.mock(Comparable.class);
        assertTrue(mock1.compareTo(mock2) != 0);
    }

    // Tests toString on mock with default name returns default description
    @Test
    public void testAnswer_toStringDefaultName_returnsDefaultDescription() {
        List<?> mockList = Mockito.mock(List.class);
        String expected = "Mock for List, hashCode: " + mockList.hashCode();
        assertEquals(expected, mockList.toString());
    }

    // Tests toString on mock with custom name returns custom name
    @Test
    public void testAnswer_toStringCustomName_returnsCustomName() {
        List<?> mockList = Mockito.mock(List.class, Mockito.withSettings().name("customList"));
        assertEquals("customList", mockList.toString());
    }
}