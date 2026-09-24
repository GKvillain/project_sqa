package org.mockito.internal.invocation;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class InvocationMatcherTest {

    private Invocation invocation;
    private InvocationMatcher invocationMatcher;

    @Before
    public void setUp() {
        invocation = new InvocationBuilder().args("arg1", "arg2").toInvocation();
        invocationMatcher = new InvocationMatcher(invocation);
    }

    // Tests constructor with empty matchers list creates matchers from invocation arguments
    @Test
    public void testInvocationMatcher_emptyMatchers_createsMatchersFromArgs() {
        assertEquals(2, invocationMatcher.getMatchers().size());
        assertEquals(invocation, invocationMatcher.getInvocation());
    }

    // Tests constructor with explicitly provided matchers
    @Test
    public void testInvocationMatcher_explicitMatchers_usesProvidedMatchers() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("arg1"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        assertEquals(matchers, matcher.getMatchers());
    }

    // Tests getMethod returns underlying invocation method
    @Test
    public void testGetMethod_validInvocation_returnsMethod() {
        Method method = invocationMatcher.getMethod();
        assertNotNull(method);
        assertEquals(invocation.getMethod(), method);
    }

    // Tests getInvocation returns the invocation passed at creation
    @Test
    public void testGetInvocation_validState_returnsInvocation() {
        assertSame(invocation, invocationMatcher.getInvocation());
    }

    // Tests toString returns formatted representation
    @Test
    public void testToString_validInvocation_returnsNonEmptyString() {
        String stringRepresentation = invocationMatcher.toString();
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("simpleMethod"));
    }

    // Tests matches returns true for identical invocation
    @Test
    public void testMatches_sameInvocation_returnsTrue() {
        assertTrue(invocationMatcher.matches(invocation));
    }

    // Tests matches returns false when arguments do not match
    @Test
    public void testMatches_differentArguments_returnsFalse() {
        Invocation differentArgs = new InvocationBuilder().args("arg1", "different").toInvocation();
        assertFalse(invocationMatcher.matches(differentArgs));
    }

    // Tests matches returns false when mock instance differs
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation differentMock = new InvocationBuilder().mock("differentMock").args("arg1", "arg2").toInvocation();
        assertFalse(invocationMatcher.matches(differentMock));
    }

    // Tests matches returns false when method differs
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation differentMethod = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(invocationMatcher.matches(differentMethod));
    }

    // Tests hasSimilarMethod returns true for unverified candidate with same method and mock
    @Test
    public void testHasSimilarMethod_sameMethodAndMockUnverified_returnsTrue() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).args("other1", "other2").toInvocation();
        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when method names differ
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).differentMethod().toInvocation();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is already verified
    @Test
    public void testHasSimilarMethod_alreadyVerifiedCandidate_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).args("arg1", "arg2").toInvocation();
        candidate.markVerified();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is on a different mock
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock("otherMock").args("arg1", "arg2").toInvocation();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSameMethod returns true for invocations with the same method signature
    @Test
    public void testHasSameMethod_sameMethodSignature_returnsTrue() {
        Invocation candidate = new InvocationBuilder().args("other1", "other2").toInvocation();
        assertTrue(invocationMatcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod returns false for different method signatures
    @Test
    public void testHasSameMethod_differentMethodSignature_returnsFalse() {
        Invocation candidate = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(invocationMatcher.hasSameMethod(candidate));
    }

    // Tests getLocation delegates to invocation getLocation
    @Test
    public void testGetLocation_validInvocation_returnsLocation() {
        Location location = invocationMatcher.getLocation();
        assertNotNull(location);
        assertEquals(invocation.getLocation(), location);
    }

    // Tests captureArgumentsFrom captures arguments when matchers implement CapturesArguments
    @Test
    public void testCaptureArgumentsFrom_nonVarargs_capturesArguments() {
        CapturingMatcher<Object> capturingMatcher1 = new CapturingMatcher<Object>();
        CapturingMatcher<Object> capturingMatcher2 = new CapturingMatcher<Object>();
        List<Matcher> matchers = Arrays.<Matcher>asList(capturingMatcher1, capturingMatcher2);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);

        assertEquals("arg1", capturingMatcher1.getLastValue());
        assertEquals("arg2", capturingMatcher2.getLastValue());
    }

    // Tests captureArgumentsFrom with varargs method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCaptureArgumentsFrom_varargsInvocation_throwsUnsupportedOperationException() {
        Invocation varargInvocation = new InvocationBuilder().args("a", new String[]{"b", "c"}).varargs("a").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(varargInvocation);
        matcher.captureArgumentsFrom(varargInvocation);
    }

    // Tests createFrom converts a list of Invocations into a list of InvocationMatchers
    @Test
    public void testCreateFrom_listOfInvocations_returnsInvocationMatchers() {
        Invocation inv1 = new InvocationBuilder().args("1").toInvocation();
        Invocation inv2 = new InvocationBuilder().args("2").toInvocation();
        List<Invocation> invocations = Arrays.asList(inv1, inv2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }

    // Tests createFrom with empty list returns empty list
    @Test
    public void testCreateFrom_emptyList_returnsEmptyList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Tests captureArgumentsFrom when matcher does not implement CapturesArguments (e.g. Equals matcher)
    @Test
    public void testCaptureArgumentsFrom_nonCapturingMatcher_doesNotThrow() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("arg1"), new Equals("arg2"));
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
    }

    // Tests captureArgumentsFrom with mixed capturing and non-capturing matchers
    @Test
    public void testCaptureArgumentsFrom_mixedMatchers_capturesOnlyCapturingOnes() {
        CapturingMatcher<Object> capturingMatcher = new CapturingMatcher<Object>();
        List<Matcher> matchers = Arrays.<Matcher>asList(new Equals("arg1"), capturingMatcher);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);

        assertEquals("arg2", capturingMatcher.getLastValue());
    }

    // Tests matches returns false when actual invocation has a different number of arguments
    @Test
    public void testMatches_differentArgumentCount_returnsFalse() {
        Invocation singleArgInvocation = new InvocationBuilder().args("arg1").toInvocation();
        assertFalse(invocationMatcher.matches(singleArgInvocation));
    }
}