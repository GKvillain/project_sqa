package org.mockito.internal.invocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;
import org.mockitousage.InvocationBuilder;
import org.mockitoutil.TestBase;

import static org.junit.Assert.*;

public class InvocationMatcherTest extends TestBase {

    private Invocation simpleInvocation;
    private InvocationMatcher simpleInvocationMatcher;

    @Before
    public void setUp() {
        simpleInvocation = new InvocationBuilder().args("arg1").toInvocation();
        simpleInvocationMatcher = new InvocationMatcher(simpleInvocation);
    }

    // Tests defect 34: captureArgumentsFrom with fewer invocation arguments than matchers
    @Test
    public void testCaptureArgumentsFrom_whenInvocationHasNoArguments_doesNotThrowException() {
        Invocation noArgInvocation = new InvocationBuilder().toInvocation();
        CaptureMatcher captureMatcher = new CaptureMatcher();
        InvocationMatcher matcher = new InvocationMatcher(simpleInvocation, Collections.<Matcher>singletonList(captureMatcher));

        matcher.captureArgumentsFrom(noArgInvocation);
        assertNull(captureMatcher.getCaptured());
    }

    // Tests capturing argument from invocation when arguments match
    @Test
    public void testCaptureArgumentsFrom_capturesArgumentSuccessfully() {
        CaptureMatcher captureMatcher = new CaptureMatcher();
        InvocationMatcher matcher = new InvocationMatcher(simpleInvocation, Collections.<Matcher>singletonList(captureMatcher));
        Invocation invocationToCapture = new InvocationBuilder().args("capturedValue").toInvocation();

        matcher.captureArgumentsFrom(invocationToCapture);
        assertEquals("capturedValue", captureMatcher.getCaptured());
    }

    // Tests constructor with single invocation parameter creates matchers from invocation arguments
    @Test
    public void testConstructor_withSingleInvocation_convertsArgumentsToMatchers() {
        Invocation invocation = new InvocationBuilder().args("foo", "bar").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(2, matcher.getMatchers().size());
        assertSame(invocation, matcher.getInvocation());
    }

    // Tests constructor with explicit matchers list
    @Test
    public void testConstructor_withExplicitMatchers_usesProvidedMatchers() {
        Matcher customMatcher = new CustomDummyMatcher();
        InvocationMatcher matcher = new InvocationMatcher(simpleInvocation, Collections.singletonList(customMatcher));

        assertEquals(1, matcher.getMatchers().size());
        assertSame(customMatcher, matcher.getMatchers().get(0));
    }

    // Tests getMethod returns underlying invocation's method
    @Test
    public void testGetMethod_returnsInvocationMethod() {
        Method expectedMethod = simpleInvocation.getMethod();
        assertEquals(expectedMethod, simpleInvocationMatcher.getMethod());
    }

    // Tests getLocation returns underlying invocation's location
    @Test
    public void testGetLocation_returnsInvocationLocation() {
        Location location = simpleInvocationMatcher.getLocation();
        assertNotNull(location);
        assertEquals(simpleInvocation.getLocation(), location);
    }

    // Tests matches returns true for matching mock, method, and arguments
    @Test
    public void testMatches_sameMockMethodAndArguments_returnsTrue() {
        Invocation actual = new InvocationBuilder().args("arg1").mock(simpleInvocation.getMock()).toInvocation();
        assertTrue(simpleInvocationMatcher.matches(actual));
    }

    // Tests matches returns false when mock instance differs
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation actual = new InvocationBuilder().args("arg1").mock("differentMock").toInvocation();
        assertFalse(simpleInvocationMatcher.matches(actual));
    }

    // Tests matches returns false when method differs
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation actual = new InvocationBuilder().differentMethod().mock(simpleInvocation.getMock()).toInvocation();
        assertFalse(simpleInvocationMatcher.matches(actual));
    }

    // Tests matches returns false when arguments differ
    @Test
    public void testMatches_differentArguments_returnsFalse() {
        Invocation actual = new InvocationBuilder().args("differentArg").mock(simpleInvocation.getMock()).toInvocation();
        assertFalse(simpleInvocationMatcher.matches(actual));
    }

    // Tests hasSameMethod returns true for identical methods
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Invocation sameMethodInvocation = new InvocationBuilder().simpleMethod().toInvocation();
        assertTrue(simpleInvocationMatcher.hasSameMethod(sameMethodInvocation));
    }

    // Tests hasSameMethod returns false for different methods
    @Test
    public void testHasSameMethod_differentMethod_returnsFalse() {
        Invocation differentMethodInvocation = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(simpleInvocationMatcher.hasSameMethod(differentMethodInvocation));
    }

    // Tests hasSimilarMethod returns true for same method name, mock, and unverified candidate
    @Test
    public void testHasSimilarMethod_similarInvocation_returnsTrue() {
        Invocation candidate = new InvocationBuilder().args("differentArg").mock(simpleInvocation.getMock()).toInvocation();
        assertTrue(simpleInvocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate method name differs
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation candidate = new InvocationBuilder().differentMethod().mock(simpleInvocation.getMock()).toInvocation();
        assertFalse(simpleInvocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate mock differs
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation candidate = new InvocationBuilder().args("arg1").mock("otherMock").toInvocation();
        assertFalse(simpleInvocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is already verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Invocation candidate = new InvocationBuilder().args("arg1").mock(simpleInvocation.getMock()).verified().toInvocation();
        assertFalse(simpleInvocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests toString returns formatted invocation string
    @Test
    public void testToString_returnsNonNullString() {
        String result = simpleInvocationMatcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests toString with custom PrintSettings
    @Test
    public void testToString_withPrintSettings_returnsNonNullString() {
        PrintSettings settings = new PrintSettings();
        String result = simpleInvocationMatcher.toString(settings);
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    private static class CaptureMatcher extends BaseMatcher<Object> implements CapturesArguments {
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

    private static class CustomDummyMatcher extends BaseMatcher<Object> {
        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
        }
    }
}