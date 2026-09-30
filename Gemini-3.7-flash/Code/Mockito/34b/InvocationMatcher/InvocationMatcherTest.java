package org.mockito.internal.invocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;

import static org.junit.Assert.*;

public class InvocationMatcherTest {

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        private Object captured;

        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
        }

        public void captureFrom(Object argument) {
            this.captured = argument;
        }

        public Object getCaptured() {
            return captured;
        }
    }

    // Tests captureArgumentsFrom with capturing matcher
    @Test
    public void testCaptureArgumentsFrom_capturingMatcher_capturesArgument() {
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        Invocation invocation = new InvocationBuilder().args("testArg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(capturingMatcher));

        matcher.captureArgumentsFrom(invocation);

        assertEquals("testArg", capturingMatcher.getCaptured());
    }

    // Tests captureArgumentsFrom when invocation has no arguments
    @Test
    public void testCaptureArgumentsFrom_invocationWithoutArguments_doesNotThrowException() {
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        Invocation invocationWithNoArgs = new InvocationBuilder().args(new Object[0]).toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocationWithNoArgs, Collections.<Matcher>singletonList(capturingMatcher));

        matcher.captureArgumentsFrom(invocationWithNoArgs);
    }

    // Tests captureArgumentsFrom with non-capturing matcher
    @Test
    public void testCaptureArgumentsFrom_nonCapturingMatcher_doesNotCapture() {
        Invocation invocation = new InvocationBuilder().args("testArg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        matcher.captureArgumentsFrom(invocation);
    }

    // Tests matches returns true when mock, method and arguments match
    @Test
    public void testMatches_sameMockMethodAndArgs_returnsTrue() {
        Invocation invocation = new InvocationBuilder().args("a").toInvocation();
        Invocation actual = new InvocationBuilder().args("a").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher.matches(actual));
    }

    // Tests matches returns false when arguments differ
    @Test
    public void testMatches_differentArgs_returnsFalse() {
        Invocation invocation = new InvocationBuilder().args("a").toInvocation();
        Invocation actual = new InvocationBuilder().args("b").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.matches(actual));
    }

    // Tests matches returns false when mock differs
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation invocation = new InvocationBuilder().mock("mock1").toInvocation();
        Invocation actual = new InvocationBuilder().mock("mock2").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.matches(actual));
    }

    // Tests matches returns false when method differs
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        Invocation actual = new InvocationBuilder().differentMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.matches(actual));
    }

    // Tests hasSameMethod returns true for identical methods
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().simpleMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod returns false for different methods
    @Test
    public void testHasSameMethod_differentMethod_returnsFalse() {
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().differentMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSimilarMethod returns true for same mock, unverified, same method name
    @Test
    public void testHasSimilarMethod_sameMockAndMethodUnverified_returnsTrue() {
        Invocation invocation = new InvocationBuilder().mock("mock").simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().mock("mock").simpleMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when method name is different
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation invocation = new InvocationBuilder().mock("mock").simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().mock("mock").differentMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when mock is different
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation invocation = new InvocationBuilder().mock("mock1").simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().mock("mock2").simpleMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Invocation invocation = new InvocationBuilder().mock("mock").simpleMethod().toInvocation();
        Invocation candidate = new InvocationBuilder().mock("mock").simpleMethod().verified().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests getMethod returns underlying invocation method
    @Test
    public void testGetMethod_returnsInvocationMethod() {
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(invocation.getMethod(), matcher.getMethod());
    }

    // Tests getInvocation returns original invocation
    @Test
    public void testGetInvocation_returnsOriginalInvocation() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(invocation, matcher.getInvocation());
    }

    // Tests getMatchers returns matchers created from invocation arguments
    @Test
    public void testGetMatchers_initializedFromArguments() {
        Invocation invocation = new InvocationBuilder().args("arg1", "arg2").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(2, matcher.getMatchers().size());
    }

    // Tests getLocation returns invocation location
    @Test
    public void testGetLocation_returnsInvocationLocation() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertNotNull(matcher.getLocation());
        assertEquals(invocation.getLocation(), matcher.getLocation());
    }

    // Tests toString returns non-null string representation
    @Test
    public void testToString_returnsStringRepresentation() {
        Invocation invocation = new InvocationBuilder().args("arg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String result = matcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests toString with PrintSettings returns non-null formatted string
    @Test
    public void testToString_withPrintSettings_returnsFormattedString() {
        Invocation invocation = new InvocationBuilder().args("arg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String result = matcher.toString(new PrintSettings());
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests constructor with empty matchers list falls back to argument matchers
    @Test
    public void testConstructor_withEmptyMatchersList_initializesFromArguments() {
        Invocation invocation = new InvocationBuilder().args("arg1", "arg2").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertEquals(2, matcher.getMatchers().size());
    }

    // Tests createFrom static factory method
    @Test
    public void testCreateFrom_convertsInvocationsToInvocationMatchers() {
        Invocation inv1 = new InvocationBuilder().args("a").toInvocation();
        Invocation inv2 = new InvocationBuilder().args("b").toInvocation();

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(Arrays.asList(inv1, inv2));

        assertEquals(2, matchers.size());
        assertSame(inv1, matchers.get(0).getInvocation());
        assertSame(inv2, matchers.get(1).getInvocation());
    }

    // Tests matches returns false when actual invocation is null
    @Test
    public void testMatches_nullActualInvocation_returnsFalse() {
        Invocation invocation = new InvocationBuilder().simpleMethod().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertFalse(matcher.matches(null));
    }

    // Tests hasSimilarMethod returns true when method name is same but arguments differ
    @Test
    public void testHasSimilarMethod_sameMockAndMethodNameWithDifferentArgs_returnsTrue() {
        Invocation invocation = new InvocationBuilder().mock("mock").simpleMethod().args("arg1").toInvocation();
        Invocation candidate = new InvocationBuilder().mock("mock").simpleMethod().args("arg2").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher.hasSimilarMethod(candidate));
    }
}