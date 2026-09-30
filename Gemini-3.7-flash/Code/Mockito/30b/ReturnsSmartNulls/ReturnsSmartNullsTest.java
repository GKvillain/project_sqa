package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.SmartNullPointerException;

import java.util.List;

import static org.junit.Assert.*;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;
    private SampleInterface sampleMock;

    interface SampleInterface {
        int getPrimitiveInt();
        String getString();
        List<String> getList();
        SampleInterface getNested();
        SampleInterface getNestedWithArgs(String first, int second);
        FinalClass getFinalClass();
        NonFinalClass getNonFinalClass();
        void voidMethod();
        String[] getArray();
        int[] getPrimitiveArray();
    }

    static final class FinalClass {
    }

    static class NonFinalClass {
        public void doSomething() {
        }
    }

    @Before
    public void setUp() {
        returnsSmartNulls = new ReturnsSmartNulls();
        sampleMock = Mockito.mock(SampleInterface.class, returnsSmartNulls);
    }

    // Tests that primitive return values are handled by ReturnsMoreEmptyValues delegate
    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultPrimitive() {
        int result = sampleMock.getPrimitiveInt();
        assertEquals(0, result);
    }

    // Tests that String return value is handled by ReturnsMoreEmptyValues delegate
    @Test
    public void testAnswer_stringReturnType_returnsEmptyString() {
        String result = sampleMock.getString();
        assertEquals("", result);
    }

    // Tests that collection return value is handled by ReturnsMoreEmptyValues delegate
    @Test
    public void testAnswer_collectionReturnType_returnsEmptyCollection() {
        List<String> result = sampleMock.getList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests that mockable object return type returns a SmartNull proxy
    @Test
    public void testAnswer_mockableType_returnsSmartNullObject() {
        SampleInterface nested = sampleMock.getNested();
        assertNotNull(nested);
    }

    // Tests that un-mockable (final class) return type returns ordinary null
    @Test
    public void testAnswer_finalClassReturnType_returnsNull() {
        FinalClass result = sampleMock.getFinalClass();
        assertNull(result);
    }

    // Tests toString on SmartNull returned from a no-arg method call
    @Test
    public void testSmartNull_toStringNoArgs_returnsInformativeMessage() {
        SampleInterface nested = sampleMock.getNested();
        String str = nested.toString();
        assertEquals("SmartNull returned by unstubbed getNested() method on mock", str);
    }

    // Tests toString on SmartNull returned from a method call with arguments
    @Test
    public void testSmartNull_toStringWithArgs_returnsInformativeMessageWithArgs() {
        SampleInterface nested = sampleMock.getNestedWithArgs("foo", 42);
        String str = nested.toString();
        assertEquals("SmartNull returned by unstubbed getNestedWithArgs(foo, 42) method on mock", str);
    }

    // Tests that invoking a method on SmartNull throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testSmartNull_invokeMethod_throwsSmartNullPointerException() {
        SampleInterface nested = sampleMock.getNested();
        nested.getPrimitiveInt();
    }

    // Tests that the exception message from SmartNullPointerException contains unstubbed method details
    @Test
    public void testSmartNull_exceptionMessage_containsExpectedDetails() {
        SampleInterface nested = sampleMock.getNested();
        try {
            nested.getString();
            fail("Expected SmartNullPointerException to be thrown");
        } catch (SmartNullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    // Tests that non-final classes can also be returned as SmartNull proxy
    @Test
    public void testAnswer_nonFinalClassReturnType_returnsSmartNullObject() {
        NonFinalClass nestedClass = sampleMock.getNonFinalClass();
        assertNotNull(nestedClass);
    }

    // Tests that invoking a method on SmartNull non-final class throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testSmartNull_nonFinalClass_invokeMethod_throwsSmartNullPointerException() {
        NonFinalClass nestedClass = sampleMock.getNonFinalClass();
        nestedClass.doSomething();
    }

    // Tests that void methods return null and do not throw exception
    @Test
    public void testAnswer_voidMethod_returnsNull() {
        sampleMock.voidMethod();
    }

    // Tests that object array return types return empty array from delegate
    @Test
    public void testAnswer_objectArrayReturnType_returnsEmptyArray() {
        String[] result = sampleMock.getArray();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests that primitive array return types return empty array from delegate
    @Test
    public void testAnswer_primitiveArrayReturnType_returnsEmptyArray() {
        int[] result = sampleMock.getPrimitiveArray();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // Tests that SmartNullPointerException contains details about the unstubbed invocation
    @Test
    public void testSmartNull_exceptionMessage_containsUnstubbedInvocationDetails() {
        SampleInterface nested = sampleMock.getNestedWithArgs("bar", 99);
        try {
            nested.getPrimitiveInt();
            fail("Expected SmartNullPointerException to be thrown");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("getNestedWithArgs"));
        }
    }
}