package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    // Tests primitive int return value
    @Test
    public void testReturnValueFor_primitiveInt_returnsZero() {
        assertEquals(0, returnsEmptyValues.returnValueFor(int.class));
    }

    // Tests primitive boolean return value
    @Test
    public void testReturnValueFor_primitiveBoolean_returnsFalse() {
        assertEquals(false, returnsEmptyValues.returnValueFor(boolean.class));
    }

    // Tests primitive wrapper Integer return value
    @Test
    public void testReturnValueFor_wrapperInteger_returnsZero() {
        assertEquals(0, returnsEmptyValues.returnValueFor(Integer.class));
    }

    // Tests primitive wrapper Boolean return value
    @Test
    public void testReturnValueFor_wrapperBoolean_returnsFalse() {
        assertEquals(false, returnsEmptyValues.returnValueFor(Boolean.class));
    }

    // Tests Collection interface return value
    @Test
    public void testReturnValueFor_collectionInterface_returnsEmptyLinkedList() {
        Object result = returnsEmptyValues.returnValueFor(Collection.class);
        assertTrue(result instanceof LinkedList);
        assertTrue(((Collection<?>) result).isEmpty());
    }

    // Tests List interface return value
    @Test
    public void testReturnValueFor_listInterface_returnsEmptyLinkedList() {
        Object result = returnsEmptyValues.returnValueFor(List.class);
        assertTrue(result instanceof LinkedList);
        assertTrue(((List<?>) result).isEmpty());
    }

    // Tests ArrayList concrete class return value
    @Test
    public void testReturnValueFor_arrayListClass_returnsEmptyArrayList() {
        Object result = returnsEmptyValues.returnValueFor(ArrayList.class);
        assertTrue(result instanceof ArrayList);
        assertTrue(((ArrayList<?>) result).isEmpty());
    }

    // Tests Set interface return value
    @Test
    public void testReturnValueFor_setInterface_returnsEmptyHashSet() {
        Object result = returnsEmptyValues.returnValueFor(Set.class);
        assertTrue(result instanceof HashSet);
        assertTrue(((Set<?>) result).isEmpty());
    }

    // Tests SortedSet interface return value
    @Test
    public void testReturnValueFor_sortedSetInterface_returnsEmptyTreeSet() {
        Object result = returnsEmptyValues.returnValueFor(SortedSet.class);
        assertTrue(result instanceof TreeSet);
        assertTrue(((SortedSet<?>) result).isEmpty());
    }

    // Tests LinkedHashSet concrete class return value
    @Test
    public void testReturnValueFor_linkedHashSetClass_returnsEmptyLinkedHashSet() {
        Object result = returnsEmptyValues.returnValueFor(LinkedHashSet.class);
        assertTrue(result instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) result).isEmpty());
    }

    // Tests Map interface return value
    @Test
    public void testReturnValueFor_mapInterface_returnsEmptyHashMap() {
        Object result = returnsEmptyValues.returnValueFor(Map.class);
        assertTrue(result instanceof HashMap);
        assertTrue(((Map<?, ?>) result).isEmpty());
    }

    // Tests SortedMap interface return value
    @Test
    public void testReturnValueFor_sortedMapInterface_returnsEmptyTreeMap() {
        Object result = returnsEmptyValues.returnValueFor(SortedMap.class);
        assertTrue(result instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) result).isEmpty());
    }

    // Tests LinkedHashMap concrete class return value
    @Test
    public void testReturnValueFor_linkedHashMapClass_returnsEmptyLinkedHashMap() {
        Object result = returnsEmptyValues.returnValueFor(LinkedHashMap.class);
        assertTrue(result instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) result).isEmpty());
    }

    // Tests unsupported non-collection return type
    @Test
    public void testReturnValueFor_unsupportedClass_returnsNull() {
        assertNull(returnsEmptyValues.returnValueFor(String.class));
        assertNull(returnsEmptyValues.returnValueFor(Object.class));
    }

    // Tests compareTo method when comparing mock to itself (Defects4J Mockito-24 regression test)
    @SuppressWarnings("unchecked")
    @Test
    public void testAnswer_compareToSelf_returnsZero() {
        Comparable<Object> mock = Mockito.mock(Comparable.class);
        assertEquals(0, mock.compareTo(mock));
    }

    // Tests compareTo method when comparing mock to a different object
    @SuppressWarnings("unchecked")
    @Test
    public void testAnswer_compareToDifferentObject_returnsNonZero() {
        Comparable<Object> mock = Mockito.mock(Comparable.class);
        assertEquals(1, mock.compareTo(new Object()));
    }

    // Tests toString method with default mock name
    @Test
    public void testAnswer_toStringDefaultName_returnsDefaultDescription() {
        Date mock = Mockito.mock(Date.class);
        assertNotNull(mock.toString());
        assertTrue(mock.toString().startsWith("Mock for Date, hashCode: "));
    }

    // Tests toString method with custom mock name
    @Test
    public void testAnswer_toStringCustomName_returnsCustomName() {
        Date mock = Mockito.mock(Date.class, Mockito.withSettings().name("customMockName"));
        assertEquals("customMockName", mock.toString());
    }
}