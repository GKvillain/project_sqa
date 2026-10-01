package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ReturnsSmartNullsTest {

    private ReturnsSmartNulls returnsSmartNulls;
    private SampleInterface sampleMock;

    interface SampleInterface {
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        String getString();
        List<String> getList();
        Set<String> getSet();
        Map<String, String> getMap();
        SampleDependency getDependency();
        SampleDependency getDependencyWithArgs(String arg, int number);
        FinalClass getFinalClass();
    }

    interface SampleDependency {
        void performAction();
        String compute();
    }

    static final class FinalClass {
    }

    @Before
    public void setUp() {
        returnsSmartNulls = new ReturnsSmartNulls();
        sampleMock = Mockito.mock(SampleInterface.class, (Answer<?>) returnsSmartNulls);
    }

    // Tests default return value for primitive integer
    @Test
    public void testAnswer_primitiveInt_returnsZero() {
        int result = sampleMock.getPrimitiveInt();
        assertEquals(0, result);
    }

    // Tests default return value for primitive boolean
    @Test
    public void testAnswer_primitiveBoolean_returnsFalse() {
        boolean result = sampleMock.getPrimitiveBoolean();
        assertFalse(result);
    }

    // Tests default return value for String (from ReturnsMoreEmptyValues delegate)
    @Test
    public void testAnswer_stringReturnType_returnsEmptyString() {
        String result = sampleMock.getString();
        assertEquals("", result);
    }

    // Tests default return value for List collection
    @Test
    public void testAnswer_listReturnType_returnsEmptyList() {
        List<String> result = sampleMock.getList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests default return value for Set collection
    @Test
    public void testAnswer_setReturnType_returnsEmptySet() {
        Set<String> result = sampleMock.getSet();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests default return value for Map collection
    @Test
    public void testAnswer_mapReturnType_returnsEmptyMap() {
        Map<String, String> result = sampleMock.getMap();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests non-mockable final class returns null
    @Test
    public void testAnswer_finalClassReturnType_returnsNull() {
        FinalClass result = sampleMock.getFinalClass();
        assertNull(result);
    }

    // Tests mockable interface returns SmartNull proxy
    @Test
    public void testAnswer_mockableInterface_returnsSmartNullProxy() {
        SampleDependency dependency = sampleMock.getDependency();
        assertNotNull(dependency);
    }

    // Tests toString method on SmartNull proxy for unstubbed method without arguments
    @Test
    public void testSmartNull_toStringWithoutArgs_returnsFormattedMessage() {
        SampleDependency dependency = sampleMock.getDependency();
        String toStringResult = dependency.toString();
        assertTrue(toStringResult.contains("SmartNull returned by unstubbed getDependency() method on mock"));
    }

    // Tests toString method on SmartNull proxy for unstubbed method with arguments
    @Test
    public void testSmartNull_toStringWithArgs_containsMethodCallInfo() {
        SampleDependency dependency = sampleMock.getDependencyWithArgs("testParam", 42);
        String toStringResult = dependency.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.startsWith("SmartNull returned by unstubbed"));
        assertTrue(toStringResult.contains("getDependencyWithArgs"));
    }

    // Tests invoking method on SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testSmartNull_invokingMethod_throwsSmartNullPointerException() {
        SampleDependency dependency = sampleMock.getDependency();
        dependency.performAction();
    }

    // Tests invoking method returning value on SmartNull proxy throws SmartNullPointerException
    @Test(expected = SmartNullPointerException.class)
    public void testSmartNull_invokingMethodWithReturnValue_throwsSmartNullPointerException() {
        SampleDependency dependency = sampleMock.getDependency();
        dependency.compute();
    }
}