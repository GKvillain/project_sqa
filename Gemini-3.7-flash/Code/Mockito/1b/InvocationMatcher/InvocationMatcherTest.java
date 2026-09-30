package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvocationMatcherTest {

    private interface TestMethods {
        void simpleMethod(String arg);
        void overloadedMethod(String arg);
        void overloadedMethod(Integer arg);
        void multiArgMethod(String arg1, int arg2);
        void varargMethod(String... args);
        void differentMethod(String arg);
    }

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        private final List<Object> captured = new ArrayList<Object>();

        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("capturing");
        }

        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() {
            return captured;
        }
    }

    private Object mockObject;
    private Method simpleMethod;
    private Method overloadedMethodString;
    private Method overloadedMethodInt;
    private Method multiArgMethod;
    private Method varargMethod;
    private Method differentMethod;

    @Before
    public void setUp() throws Exception {
        mockObject = new Object();
        simpleMethod = TestMethods.class.getMethod("simpleMethod", String.class);
        overloadedMethodString = TestMethods.class.getMethod("overloadedMethod", String.class);
        overloadedMethodInt = TestMethods.class.getMethod("overloadedMethod", Integer.class);
        multiArgMethod = TestMethods.class.getMethod("multiArgMethod", String.class, int.class);
        varargMethod = TestMethods.class.getMethod("varargMethod", String[].class);
        differentMethod = TestMethods.class.getMethod("differentMethod", String.class);
    }

    private Invocation createInvocation(Object mock, Method method, Object[] args, Object[] rawArgs, boolean verified) {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getMock()).thenReturn(mock);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation.getArguments()).thenReturn(args != null ? args : new Object[0]);
        when(invocation.getRawArguments()).thenReturn(rawArgs != null ? rawArgs : (args != null ? args : new Object[0]));
        when(invocation.isVerified()).thenReturn(verified);
        return invocation;
    }

    // Tests constructor with empty matchers list converting arguments to matchers
    @Test
    public void testInvocationMatcher_emptyMatchers_convertsArgumentsToMatchers() {
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(1, matcher.getMatchers().size());
        assertSame(invocation, matcher.getInvocation());
        assertSame(simpleMethod, matcher.getMethod());
    }

    // Tests constructor with explicit matchers list
    @Test
    public void testInvocationMatcher_explicitMatchers_usesProvidedMatchers() {
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        List<Matcher> matchers = Collections.<Matcher>singletonList(new Equals("test"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertEquals(1, matcher.getMatchers().size());
        assertSame(matchers.get(0), matcher.getMatchers().get(0));
    }

    // Tests getLocation delegating to invocation
    @Test
    public void testGetLocation_delegatesToInvocation_returnsLocation() {
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Location location = mock(Location.class);
        when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        assertSame(location, matcher.getLocation());
    }

    // Tests toString returning non-null formatted string
    @Test
    public void testToString_validInvocation_returnsFormattedString() {
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String result = matcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests matches when mock, method, and arguments are the same
    @Test
    public void testMatches_sameMockMethodAndArguments_returnsTrue() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertTrue(matcher.matches(invocation2));
    }

    // Tests matches when mock is different
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(new Object(), simpleMethod, new Object[]{"test"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.matches(invocation2));
    }

    // Tests matches when arguments differ
    @Test
    public void testMatches_differentArguments_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test1"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, simpleMethod, new Object[]{"test2"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.matches(invocation2));
    }

    // Tests hasSameMethod with identical method signatures
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, simpleMethod, new Object[]{"other"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertTrue(matcher.hasSameMethod(invocation2));
    }

    // Tests hasSameMethod with different parameter count
    @Test
    public void testHasSameMethod_differentParameterCount_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, multiArgMethod, new Object[]{"test", 1}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSameMethod(invocation2));
    }

    // Tests hasSameMethod with different parameter types
    @Test
    public void testHasSameMethod_differentParameterTypes_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, overloadedMethodString, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, overloadedMethodInt, new Object[]{1}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSameMethod(invocation2));
    }

    // Tests hasSameMethod with different method names
    @Test
    public void testHasSameMethod_differentMethodNames_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, differentMethod, new Object[]{"test"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSameMethod(invocation2));
    }

    // Tests hasSimilarMethod with same unverified method on same mock
    @Test
    public void testHasSimilarMethod_sameMethodUnverifiedSameMock_returnsTrue() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, simpleMethod, new Object[]{"different"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertTrue(matcher.hasSimilarMethod(invocation2));
    }

    // Tests hasSimilarMethod when candidate is already verified
    @Test
    public void testHasSimilarMethod_candidateIsVerified_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, simpleMethod, new Object[]{"different"}, null, true);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSimilarMethod(invocation2));
    }

    // Tests hasSimilarMethod when mock instance is different
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(new Object(), simpleMethod, new Object[]{"different"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSimilarMethod(invocation2));
    }

    // Tests hasSimilarMethod when method names are different
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, differentMethod, new Object[]{"test"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.hasSimilarMethod(invocation2));
    }

    // Tests captureArgumentsFrom with non-varargs invocation
    @Test
    public void testCaptureArgumentsFrom_nonVarArgs_capturesArgumentsCorrectly() {
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"capturedValue"}, null, false);
        when(invocation.getArgumentAt(0, Object.class)).thenReturn("capturedValue");

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(capturingMatcher));
        matcher.captureArgumentsFrom(invocation);

        assertEquals(1, capturingMatcher.getCaptured().size());
        assertEquals("capturedValue", capturingMatcher.getCaptured().get(0));
    }

    // Tests captureArgumentsFrom with varargs invocation triggering exception path
    @Test(expected = UnsupportedOperationException.class)
    public void testCaptureArgumentsFrom_varargsInvocation_throwsUnsupportedOperationException() {
        Invocation invocation = createInvocation(mockObject, varargMethod, new Object[]{new String[]{"a", "b"}}, new Object[]{new String[]{"a", "b"}}, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        matcher.captureArgumentsFrom(invocation);
    }

    // Tests createFrom factory method creating list of InvocationMatchers
    @Test
    public void testCreateFrom_listOfInvocations_returnsListOfMatchers() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"a"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, differentMethod, new Object[]{"b"}, null, false);
        List<Invocation> invocations = Arrays.asList(invocation1, invocation2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(invocation1, result.get(0).getInvocation());
        assertSame(invocation2, result.get(1).getInvocation());
    }

    // Tests matches when method signatures are different
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation invocation1 = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, differentMethod, new Object[]{"test"}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertFalse(matcher.matches(invocation2));
    }

    // Tests hasSimilarMethod with overloaded method on same mock
    @Test
    public void testHasSimilarMethod_overloadedMethodSameMockUnverified_returnsTrue() {
        Invocation invocation1 = createInvocation(mockObject, overloadedMethodString, new Object[]{"test"}, null, false);
        Invocation invocation2 = createInvocation(mockObject, overloadedMethodInt, new Object[]{123}, null, false);

        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        assertTrue(matcher.hasSimilarMethod(invocation2));
    }

    // Tests captureArgumentsFrom with matcher that does not implement CapturesArguments
    @Test
    public void testCaptureArgumentsFrom_nonCapturingMatcher_doesNotThrow() {
        Invocation invocation = createInvocation(mockObject, simpleMethod, new Object[]{"test"}, null, false);
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(new Equals("test")));

        matcher.captureArgumentsFrom(invocation);
    }

    // Tests createFrom with empty list
    @Test
    public void testCreateFrom_emptyList_returnsEmptyList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}