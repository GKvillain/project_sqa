package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class InvocationMatcherTest {

    private Invocation invocation;
    private InvocationMatcher matcher;

    @Before
    public void setUp() {
        invocation = new InvocationBuilder().args("a", "b").toInvocation();
        matcher = new InvocationMatcher(invocation);
    }

    // Tests getInvocation returns the wrapped invocation
    @Test
    public void testGetInvocation_validInvocation_returnsSameInvocation() {
        assertSame(invocation, matcher.getInvocation());
    }

    // Tests getMethod returns the method of the wrapped invocation
    @Test
    public void testGetMethod_validInvocation_returnsCorrectMethod() {
        assertEquals(invocation.getMethod(), matcher.getMethod());
    }

    // Tests constructor with empty matchers builds matchers from arguments
    @Test
    public void testConstructor_emptyMatchers_derivesMatchersFromArguments() {
        InvocationMatcher customMatcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());
        assertEquals(2, customMatcher.getMatchers().size());
    }

    // Tests constructor with explicit matchers retains provided matchers
    @Test
    public void testConstructor_explicitMatchers_retainsProvidedMatchers() {
        Matcher customMatcher = new DummyMatcher();
        List<Matcher> matchersList = Collections.singletonList(customMatcher);
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchersList);
        assertEquals(1, invocationMatcher.getMatchers().size());
        assertSame(customMatcher, invocationMatcher.getMatchers().get(0));
    }

    // Tests matches returns true when mock, method, and arguments match
    @Test
    public void testMatches_sameMockMethodAndArgs_returnsTrue() {
        Invocation actual = new InvocationBuilder().args("a", "b").toInvocation();
        assertTrue(matcher.matches(actual));
    }

    // Tests matches returns false when mock instance differs
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation actual = new InvocationBuilder().mock("differentMock").args("a", "b").toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests matches returns false when method differs
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation actual = new InvocationBuilder().differentMethod().args("a", "b").toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests matches returns false when arguments differ
    @Test
    public void testMatches_differentArguments_returnsFalse() {
        Invocation actual = new InvocationBuilder().args("x", "y").toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests hasSameMethod returns true for identical method
    @Test
    public void testHasSameMethod_identicalMethod_returnsTrue() {
        Invocation candidate = new InvocationBuilder().args("differentArgs").toInvocation();
        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod returns false for different method name
    @Test
    public void testHasSameMethod_differentMethod_returnsFalse() {
        Invocation candidate = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod with interface and implementing class method signatures (Defects4J 33 defect)
    @Test
    public void testHasSameMethod_sameMethodFromInterfaceAndClass_returnsTrue() throws Exception {
        Method interfaceMethod = Iterable.class.getMethod("iterator");
        Method classMethod = List.class.getMethod("iterator");

        Invocation inv1 = Mockito.mock(Invocation.class);
        Invocation inv2 = Mockito.mock(Invocation.class);
        Mockito.when(inv1.getMethod()).thenReturn(interfaceMethod);
        Mockito.when(inv2.getMethod()).thenReturn(classMethod);

        InvocationMatcher invocationMatcher = new InvocationMatcher(inv1);
        assertTrue(invocationMatcher.hasSameMethod(inv2));
    }

    // Tests hasSimilarMethod returns true for same mock, name, unverified, different args
    @Test
    public void testHasSimilarMethod_similarMethod_returnsTrue() {
        Object sharedMock = "sharedMock";
        Invocation inv1 = new InvocationBuilder().mock(sharedMock).differentMethod().args("arg1").toInvocation();
        Invocation inv2 = new InvocationBuilder().mock(sharedMock).differentMethod().args("arg2").toInvocation();
        InvocationMatcher invocationMatcher = new InvocationMatcher(inv1);
        assertTrue(invocationMatcher.hasSimilarMethod(inv2));
    }

    // Tests hasSimilarMethod returns false when candidate method name differs
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation candidate = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is already verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Invocation candidate = new InvocationBuilder().args("a", "b").toInvocation();
        candidate.markVerified();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when mock instances differ
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock("otherMock").args("a", "b").toInvocation();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests getLocation returns location from invocation
    @Test
    public void testGetLocation_validInvocation_returnsLocation() {
        Location location = matcher.getLocation();
        assertNotNull(location);
    }

    // Tests toString produces non-null representation
    @Test
    public void testToString_defaultSettings_returnsString() {
        String str = matcher.toString();
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    // Tests toString with custom PrintSettings
    @Test
    public void testToString_customPrintSettings_returnsString() {
        PrintSettings settings = new PrintSettings();
        String str = matcher.toString(settings);
        assertNotNull(str);
        assertFalse(str.isEmpty());
    }

    // Tests captureArgumentsFrom captures arguments correctly
    @Test
    public void testCaptureArgumentsFrom_capturingMatcher_capturesArgument() {
        DummyCapturingMatcher capturingMatcher = new DummyCapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(capturingMatcher));
        Invocation actual = new InvocationBuilder().args("capturedValue").toInvocation();

        invocationMatcher.captureArgumentsFrom(actual);
        assertEquals("capturedValue", capturingMatcher.captured);
    }

    // Tests captureArgumentsFrom when invocation has fewer arguments than matchers
    @Test
    public void testCaptureArgumentsFrom_fewerArgumentsThanMatchers_doesNotThrow() {
        DummyCapturingMatcher capturingMatcher1 = new DummyCapturingMatcher();
        DummyCapturingMatcher capturingMatcher2 = new DummyCapturingMatcher();
        List<Matcher> matchersList = Arrays.<Matcher>asList(capturingMatcher1, capturingMatcher2);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchersList);
        Invocation actual = new InvocationBuilder().args("onlyOneArg").toInvocation();

        invocationMatcher.captureArgumentsFrom(actual);
        assertEquals("onlyOneArg", capturingMatcher1.captured);
        assertNull(capturingMatcher2.captured);
    }

    // Tests createFrom creates a list of InvocationMatchers from a list of Invocations
    @Test
    public void testCreateFrom_listOfInvocations_returnsListOfMatchers() {
        Invocation inv1 = new InvocationBuilder().args("1").toInvocation();
        Invocation inv2 = new InvocationBuilder().args("2").toInvocation();
        List<Invocation> invocations = Arrays.asList(inv1, inv2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }

    // Helper matcher class for testing
    private static class DummyMatcher extends BaseMatcher<Object> {
        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("dummy");
        }
    }

    // Helper capturing matcher class for testing
    private static class DummyCapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        Object captured;

        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("dummyCapturing");
        }

        public void captureFrom(Object argument) {
            this.captured = argument;
        }
    }
}