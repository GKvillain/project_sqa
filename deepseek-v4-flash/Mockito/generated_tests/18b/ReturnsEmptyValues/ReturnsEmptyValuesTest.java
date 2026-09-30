package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.*;

import org.mockito.invocation.InvocationOnMock;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returns = new ReturnsEmptyValues();

    // Tests for returnValueFor
    @Test
    public void testReturnValueFor_primitiveBoolean() {
        Object result = returns.returnValueFor(boolean.class);
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testReturnValueFor_primitiveInt() {
        Object result = returns.returnValueFor(int.class);
        assertEquals(Integer.valueOf(0), result);
    }

    @Test
    public void testReturnValueFor_Collection() {
        Object result = returns.returnValueFor(Collection.class);
        assertTrue(result instanceof LinkedList);
        assertTrue(((Collection) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_Set() {
        Object result = returns.returnValueFor(Set.class);
        assertTrue(result instanceof HashSet);
        assertTrue(((Set) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_List() {
        Object result = returns.returnValueFor(List.class);
        assertTrue(result instanceof LinkedList);
        assertTrue(((List) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_Map() {
        Object result = returns.returnValueFor(Map.class);
        assertTrue(result instanceof HashMap);
        assertTrue(((Map) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_HashMap() {
        Object result = returns.returnValueFor(HashMap.class);
        assertTrue(result instanceof HashMap);
        assertTrue(((HashMap) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_ArrayList() {
        Object result = returns.returnValueFor(ArrayList.class);
        assertTrue(result instanceof ArrayList);
        assertTrue(((ArrayList) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_TreeSet() {
        Object result = returns.returnValueFor(TreeSet.class);
        assertTrue(result instanceof TreeSet);
        assertTrue(((TreeSet) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_LinkedHashMap() {
        Object result = returns.returnValueFor(LinkedHashMap.class);
        assertTrue(result instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_unknownClass_returnsNull() {
        Object result = returns.returnValueFor(String.class);
        assertNull(result);
    }

    @Test
    public void testReturnValueFor_Iterable_returnsEmptyCollection() {
        // Iterable should return empty collection; buggy version returns null
        Object result = returns.returnValueFor(Iterable.class);
        assertNotNull("Iterable should not return null", result);
        assertTrue("Iterable should be a Collection", result instanceof Collection);
        assertTrue("Iterable should be empty", ((Collection) result).isEmpty());
    }

    @Test
    public void testReturnValueFor_void_returnsNull() {
        Object result = returns.returnValueFor(void.class);
        assertNull(result);
    }

    // Tests for answer
    @Test
    public void testAnswer_toString_defaultName() throws Exception {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        Object mockObj = mock(Object.class);
        when(invocation.getMock()).thenReturn(mockObj);
        when(invocation.getArguments()).thenReturn(new Object[0]);

        Object result = returns.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof String);
        String str = (String) result;
        assertTrue(str.startsWith("Mock for "));
        assertTrue(str.contains("hashCode: "));
    }

    @Test
    public void testAnswer_toString_customName() throws Exception {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method toStringMethod = Object.class.getMethod("toString");
        when(invocation.getMethod()).thenReturn(toStringMethod);
        Object mockObj = mock(Object.class, withSettings().name("myCustomMock"));
        when(invocation.getMock()).thenReturn(mockObj);
        when(invocation.getArguments()).thenReturn(new Object[0]);

        Object result = returns.answer(invocation);
        assertNotNull(result);
        assertEquals("myCustomMock", result.toString());
    }

    @Test
    public void testAnswer_compareTo_sameReference() throws Exception {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        Object mockObj = mock(Object.class);
        when(invocation.getMock()).thenReturn(mockObj);
        when(invocation.getArguments()).thenReturn(new Object[]{mockObj});

        Object result = returns.answer(invocation);
        assertEquals(0, result);
    }

    @Test
    public void testAnswer_compareTo_differentReference() throws Exception {
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        when(invocation.getMethod()).thenReturn(compareToMethod);
        Object mockObj1 = mock(Object.class);
        Object mockObj2 = mock(Object.class);
        when(invocation.getMock()).thenReturn(mockObj1);
        when(invocation.getArguments()).thenReturn(new Object[]{mockObj2});

        Object result = returns.answer(invocation);
        assertEquals(1, result);
    }

    @Test
    public void testAnswer_nonSpecialMethod_returnsNull() throws Exception {
        // method that returns primitive int (non-special) -> returns 0
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method lengthMethod = String.class.getMethod("length");
        when(invocation.getMethod()).thenReturn(lengthMethod);
        when(invocation.getMock()).thenReturn(mock(Object.class));
        when(invocation.getArguments()).thenReturn(new Object[0]);

        Object result = returns.answer(invocation);
        assertEquals(0, result);
    }

    @Test
    public void testAnswer_nonSpecialMethod_returnsNullForString() throws Exception {
        // method that returns Class<?> (non-primitive, non-collection) -> returns null
        InvocationOnMock invocation = mock(InvocationOnMock.class);
        Method getClassMethod = Object.class.getMethod("getClass");
        when(invocation.getMethod()).thenReturn(getClassMethod);
        when(invocation.getMock()).thenReturn(mock(Object.class));
        when(invocation.getArguments()).thenReturn(new Object[0]);

        Object result = returns.answer(invocation);
        assertNull(result);
    }
}