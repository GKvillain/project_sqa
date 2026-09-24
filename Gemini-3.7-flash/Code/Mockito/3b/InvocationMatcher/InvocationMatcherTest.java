package org.mockito.internal.invocation;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    private Invocation invocation;
    private Method simpleMethod;
    private Method varargMethod;
    private Method overloadedMethod;
    private Object mockObject;

    public interface TestInterface {
        void simpleMethod(String arg);
        void varargMethod(String prefix, Object... args);
        void simpleMethod(Integer arg);
    }

    private static class CapturingMatcher implements Matcher, CapturesArguments {
        private final List<Object> captured = new ArrayList<Object>();

        public boolean matches(Object item) {
            return true;
        }

        public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}

        public void describeTo(org.hamcrest.Description description) {}

        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() {
            return captured;
        }
    }

    @Before
    public void setUp() throws Exception {
        simpleMethod = TestInterface.class.getMethod("simpleMethod", String.class);
        varargMethod = TestInterface.class.getMethod("varargMethod", String.class, Object[].class);
        overloadedMethod = TestInterface.class.getMethod("simpleMethod", Integer.class);
        mockObject = new Object();

        invocation = mock(Invocation.class);
        when(invocation.getMethod()).thenReturn(simpleMethod);
        when(invocation.getMock()).thenReturn(mockObject);
        when(invocation.getArguments()).thenReturn(new Object[]{"test"});
        when(invocation.getRawArguments()).thenReturn(new Object[]{"test"});
    }

    // Tests constructor with empty matchers converts arguments to matchers
    @Test
    public void testConstructor_emptyMatchers_convertsArgumentsToMatchers() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
        assertEquals(invocation, matcher.getInvocation());
        assertEquals(simpleMethod, matcher.getMethod());
    }

    // Tests constructor with explicit matchers
    @Test
    public void testConstructor_explicitMatchers_preservesMatchers() {
        Matcher explicitMatcher = new Equals("test");
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.asList(explicitMatcher));
        assertEquals(1, matcher.getMatchers().size());
        assertSame(explicitMatcher, matcher.getMatchers().get(0));
    }

    // Tests getLocation delegates to invocation
    @Test
    public void testGetLocation_delegatesToInvocation() {
        Location location = mock(Location.class);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertSame(location, matcher.getLocation());
    }

    // Tests toString returns formatted representation
    @Test
    public void testToString_returnsNonEmptyString() {
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String result = matcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests matches when mock, method, and arguments match
    @Test
    public void testMatches_sameMockAndMethodAndArgs_returnsTrue() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(mockObject);
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertTrue(matcher.matches(actual));
    }

    // Tests matches when mock is different
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation actual = mock(Invocation.class);
        when(actual.getMock()).thenReturn(new Object());
        when(actual.getMethod()).thenReturn(simpleMethod);
        when(actual.getArguments()).thenReturn(new Object[]{"test"});

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertFalse(matcher.matches(actual));
    }

    // Tests hasSameMethod with identical method signature
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(simpleMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod with different parameter types
    @Test
    public void testHasSameMethod_differentParamTypes_returnsFalse() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(overloadedMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod with different method name
    @Test
    public void testHasSameMethod_differentMethodName_returnsFalse() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMethod()).thenReturn(varargMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSimilarMethod for unverified invocation on same mock
    @Test
    public void testHasSimilarMethod_sameMethodUnverified_returnsTrue() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is already verified
    @Test
    public void testHasSimilarMethod_verifiedInvocation_returnsFalse() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(mockObject);
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(true);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when mock instance is different
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation candidate = mock(Invocation.class);
        when(candidate.getMock()).thenReturn(new Object());
        when(candidate.getMethod()).thenReturn(simpleMethod);
        when(candidate.isVerified()).thenReturn(false);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests captureArgumentsFrom for non-vararg method
    @Test
    public void testCaptureArgumentsFrom_nonVararg_capturesArguments() {
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Arrays.<Matcher>asList(capturingMatcher));

        Invocation targetInvocation = mock(Invocation.class);
        when(targetInvocation.getMethod()).thenReturn(simpleMethod);
        when(targetInvocation.getArgumentAt(0, Object.class)).thenReturn("capturedValue");

        matcher.captureArgumentsFrom(targetInvocation);
        assertEquals(1, capturingMatcher.getCaptured().size());
        assertEquals("capturedValue", capturingMatcher.getCaptured().get(0));
    }

    // Tests captureArgumentsFrom with varargs and multiple matchers
    @Test
    public void testCaptureArgumentsFrom_varargsWithMultipleArguments_capturesCorrectly() {
        CapturingMatcher matcher1 = new CapturingMatcher();
        CapturingMatcher matcher2 = new CapturingMatcher();
        CapturingMatcher matcher3 = new CapturingMatcher();

        Invocation varargInvocation = mock(Invocation.class);
        when(varargInvocation.getMethod()).thenReturn(varargMethod);
        when(varargInvocation.getRawArguments()).thenReturn(new Object[]{"prefix", new Object[]{"val1", "val2"}});
        when(varargInvocation.getArgumentAt(0, Object.class)).thenReturn("prefix");

        InvocationMatcher invocationMatcher = new InvocationMatcher(
                varargInvocation,
                Arrays.<Matcher>asList(matcher1, matcher2, matcher3)
        );

        Invocation target = mock(Invocation.class);
        when(target.getMethod()).thenReturn(varargMethod);
        when(target.getRawArguments()).thenReturn(new Object[]{"prefix", new Object[]{"val1", "val2"}});
        when(target.getArgumentAt(0, Object.class)).thenReturn("prefix");

        invocationMatcher.captureArgumentsFrom(target);

        assertEquals(1, matcher1.getCaptured().size());
        assertEquals("prefix", matcher1.getCaptured().get(0));
    }

    // Tests createFrom creates a list of InvocationMatchers from invocations
    @Test
    public void testCreateFrom_listOfInvocations_returnsListOfInvocationMatchers() {
        Invocation inv1 = mock(Invocation.class);
        when(inv1.getMethod()).thenReturn(simpleMethod);
        when(inv1.getArguments()).thenReturn(new Object[]{"a"});

        Invocation inv2 = mock(Invocation.class);
        when(inv2.getMethod()).thenReturn(simpleMethod);
        when(inv2.getArguments()).thenReturn(new Object[]{"b"});

        List<InvocationMatcher> result = InvocationMatcher.createFrom(Arrays.asList(inv1, inv2));
        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }

    // Tests createFrom with empty list
    @Test
    public void testCreateFrom_emptyList_returnsEmptyList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertTrue(result.isEmpty());
    }
}