package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import java.util.Collection;
import java.util.Iterator;

public class ReturnsSmartNullsTest {

    // Tests true branch of delegate != null: int return type -> 0
    @Test
    public void testAnswer_intReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Collection<?> mock = Mockito.mock(Collection.class, answer);
        assertEquals(0, mock.size());
    }

    // Tests false branch of delegate != null + canImposterise false: void method
    @Test
    public void testAnswer_voidReturnType_doesNotThrow() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Runnable mock = Mockito.mock(Runnable.class, answer);
        mock.run();
    }

    // Tests false branch of delegate != null + canImposterise true: returns smart null
    @Test
    public void testAnswer_objectReturnType_returnsSmartNull() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Iterator<?> mock = Mockito.mock(Iterator.class, answer);
        Object smartNull = mock.next();
        assertNotNull(smartNull);
        // Verify it is a smart null (toString returns formatted message)
        String toString = smartNull.toString();
        assertTrue(toString.startsWith("SmartNull returned by unstubbed"));
    }

    // Tests isToString true branch of ThrowingInterceptor
    @Test
    public void testSmartNull_toString_returnsFormattedMessage() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Iterator<?> mock = Mockito.mock(Iterator.class, answer);
        Object smartNull = mock.next();
        String toString = smartNull.toString();
        assertTrue(toString.startsWith("SmartNull returned by unstubbed"));
        assertTrue(toString.contains("next()"));
    }

    // Tests isToString false branch of ThrowingInterceptor -> throws RuntimeException
    @Test(expected = RuntimeException.class)
    public void testSmartNull_otherMethod_throwsException() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Iterator<?> mock = Mockito.mock(Iterator.class, answer);
        Object smartNull = mock.next();
        smartNull.hashCode();
    }

    // --- New tests for uncovered conditions ---

    // Interface to exercise various return types
    interface PrimitiveMethods {
        boolean booleanMethod();
        byte byteMethod();
        short shortMethod();
        long longMethod();
        float floatMethod();
        double doubleMethod();
        char charMethod();
        int[] arrayMethod();
        String stringMethod();
    }

    @Test
    public void testAnswer_booleanReturnType_returnsFalse() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertFalse(mock.booleanMethod());
    }

    @Test
    public void testAnswer_byteReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals(0, mock.byteMethod());
    }

    @Test
    public void testAnswer_shortReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals(0, mock.shortMethod());
    }

    @Test
    public void testAnswer_longReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals(0L, mock.longMethod());
    }

    @Test
    public void testAnswer_floatReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals(0.0f, mock.floatMethod(), 0.0f);
    }

    @Test
    public void testAnswer_doubleReturnType_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals(0.0d, mock.doubleMethod(), 0.0d);
    }

    @Test
    public void testAnswer_charReturnType_returnsNullChar() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertEquals('\0', mock.charMethod());
    }

    @Test
    public void testAnswer_arrayReturnType_returnsEmptyArray() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        int[] result = mock.arrayMethod();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testAnswer_finalClassReturnType_returnsNull() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        PrimitiveMethods mock = Mockito.mock(PrimitiveMethods.class, answer);
        assertNull(mock.stringMethod());
    }

    @Test
    public void testSmartNull_equals_returnsFalse() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Iterator<?> mock = Mockito.mock(Iterator.class, answer);
        Object smartNull = mock.next();
        assertFalse(smartNull.equals(null));
        assertFalse(smartNull.equals("something"));
    }

    @Test
    public void testSmartNull_hashCode_returnsZero() {
        ReturnsSmartNulls answer = new ReturnsSmartNulls();
        Iterator<?> mock = Mockito.mock(Iterator.class, answer);
        Object smartNull = mock.next();
        assertEquals(0, smartNull.hashCode());
    }
}