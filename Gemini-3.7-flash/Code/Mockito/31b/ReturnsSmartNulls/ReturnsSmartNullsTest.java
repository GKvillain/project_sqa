package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.SmartNullPointerException;

import static org.junit.Assert.*;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;
    private SampleInterface mock;

    interface SampleInterface {
        SampleSubInterface getSubInterface();
        SampleSubInterface getSubInterfaceWithArgs(String str, int num);
        int getPrimitiveInt();
        String getString();
        FinalClass getFinalClass();
        void doVoid();
    }

    interface SampleSubInterface {
        void execute();
        String getData();
    }

    static final class FinalClass {
    }

    @Before
    public void setUp() {
        returnsSmartNulls = new ReturnsSmartNulls();
        mock = Mockito.mock(SampleInterface.class, returnsSmartNulls);
    }

    // Tests that delegate (ReturnsMoreEmptyValues) handles primitive return types
    @Test
    public void testAnswer_primitiveReturnType_returnsDefaultPrimitive() {
        int result = mock.getPrimitiveInt();
        assertEquals(0, result);
    }

    // Tests that delegate (ReturnsMoreEmptyValues) handles String return types
    @Test
    public void testAnswer_stringReturnType_returnsEmptyString() {
        String result = mock.getString();
        assertEquals("", result);
    }

    // Tests that mockable types return a non-null SmartNull proxy
    @Test
    public void testAnswer_mockableReturnType_returnsSmartNullProxy() {
        SampleSubInterface subInterface = mock.getSubInterface();
        assertNotNull(subInterface);
    }

    // Tests that final classes cannot be imposterised and return null
    @Test
    public void testAnswer_unmockableReturnType_returnsNull() {
        FinalClass finalClass = mock.getFinalClass();
        assertNull(finalClass);
    }

    // Tests toString on SmartNull proxy for unstubbed method without arguments
    @Test
    public void testThrowingInterceptor_toStringWithoutArgs_returnsSmartNullDescription() {
        SampleSubInterface subInterface = mock.getSubInterface();
        String stringRepresentation = subInterface.toString();
        assertTrue(stringRepresentation.startsWith("SmartNull returned by unstubbed"));
        assertTrue(stringRepresentation.contains("getSubInterface()"));
    }

    // Tests toString on SmartNull proxy for unstubbed method with arguments
    @Test
    public void testThrowingInterceptor_toStringWithArgs_returnsSmartNullDescription() {
        SampleSubInterface subInterface = mock.getSubInterfaceWithArgs("param", 42);
        String stringRepresentation = subInterface.toString();
        assertTrue(stringRepresentation.startsWith("SmartNull returned by unstubbed"));
        assertTrue(stringRepresentation.contains("getSubInterfaceWithArgs"));
    }

    // Tests that invoking a method on SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testThrowingInterceptor_voidMethodCall_throwsSmartNullPointerException() {
        SampleSubInterface subInterface = mock.getSubInterface();
        subInterface.execute();
    }

    // Tests that invoking a non-void method on SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testThrowingInterceptor_nonVoidMethodCall_throwsSmartNullPointerException() {
        SampleSubInterface subInterface = mock.getSubInterface();
        subInterface.getData();
    }
}