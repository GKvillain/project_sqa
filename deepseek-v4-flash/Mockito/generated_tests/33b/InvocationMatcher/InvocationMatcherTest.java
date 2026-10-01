package org.mockito.internal.invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;

/**
 * JUnit 4 test class for InvocationMatcher.
 * Designed to detect Defects4J bug 33b (inconsistent mock comparison in hasSimilarMethod).
 */
public class InvocationMatcherTest {

    private interface CapturingMatcher extends Matcher<Object>, CapturesArguments {}

    private Invocation invocation;
    private Invocation invocation2;
    private Method method;
    private Method differentMethod;
    private Method differentNameMethod;
    private List<Matcher> matchers;
    private Matcher<Object> matcher1;
    private Matcher<Object> matcher2;
    private CapturingMatcher capturer;

    // Helper object that is equal to any other instance of the same class
    private static class EqualMock {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof EqualMock;
        }
        @Override
        public int hashCode() {
            return 42;
        }
    }

    private static class Sample {
        public void foo(String arg) {}
        public void foo(Integer arg) {}
        public void bar() {}
    }

    @Before
    public void setUp() throws Exception {
        invocation = mock(Invocation.class);
        invocation2 = mock(Invocation.class);
        method = Sample.class.getDeclaredMethod("foo", String.class);
        differentMethod = Sample.class.getDeclaredMethod("foo", Integer.class);
        differentNameMethod = Sample.class.getDeclaredMethod("bar");
        matcher1 = mock(Matcher.class);
        matcher2 = mock(Matcher.class);
        capturer = mock(CapturingMatcher.class);

        Object defaultMock = new Object();
        when(invocation.getMock()).thenReturn(defaultMock);
        when(invocation2.getMock()).thenReturn(defaultMock);

        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(method);
        when(invocation.argumentsToMatchers()).thenReturn(Arrays.asList(matcher1, matcher2));
        when(invocation.getArguments()).thenReturn(new Object[] { "arg1", "arg2" });
        when(invocation2.getArguments()).thenReturn(new Object[] { "arg1", "arg2" });
        when(invocation.isVerified()).thenReturn(false);
        when(invocation2.isVerified()).thenReturn(false);

        matchers = Arrays.asList(matcher1, matcher2);
    }

    // --- Constructor tests ---

    @Test
    public void testConstructor_emptyMatchers_usesArgumentsToMatchers() {
        List<Matcher> empty = Collections.emptyList();
        InvocationMatcher im = new InvocationMatcher(invocation, empty);
        // Should use invocation.argumentsToMatchers() which returns mocked list
        assertEquals(Arrays.asList(matcher1, matcher2), im.getMatchers());
    }

    @Test
    public void testConstructor_nonEmptyMatchers_keepsProvided() {
        List<Matcher> custom = Arrays.asList(matcher1);
        InvocationMatcher im = new InvocationMatcher(invocation, custom);
        assertEquals(custom, im.getMatchers());
    }

    @Test
    public void testConstructor_singleArg_usesArgumentsToMatchers() {
        when(invocation.argumentsToMatchers()).thenReturn(Collections.<Matcher>emptyList());
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertEquals(Collections.emptyList(), im.getMatchers());
    }

    // --- Accessor tests ---

    @Test
    public void testGetMethod_returnsInvocationMethod() {
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertEquals(method, im.getMethod());
    }

    @Test
    public void testGetInvocation_returnsStoredInvocation() {
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertSame(invocation, im.getInvocation());
    }

    @Test
    public void testGetMatchers_returnsStoredMatchers() {
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertEquals(matchers, im.getMatchers());
    }

    // --- hasSameMethod tests ---

    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        InvocationMatcher im = new InvocationMatcher(invocation);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(method);
        assertTrue(im.hasSameMethod(invocation2));
    }

    @Test
    public void testHasSameMethod_differentMethod_returnsFalse() {
        InvocationMatcher im = new InvocationMatcher(invocation);
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(differentMethod);
        assertFalse(im.hasSameMethod(invocation2));
    }

    // --- hasSimilarMethod tests (focus on bug: == vs equals) ---

    @Test
    public void testHasSimilarMethod_mockSameReference_returnsTrue() {
        EqualMock mockInstance = new EqualMock();
        when(invocation.getMock()).thenReturn(mockInstance);
        when(invocation2.getMock()).thenReturn(mockInstance); // same reference
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertTrue(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_mockDifferentReferenceButEqual_returnsTrueForMatchesButBugForHasSimilar() {
        // This test should FAIL on the buggy version because hasSimilarMethod uses == instead of equals.
        // When fixed, it should pass.
        EqualMock mock1 = new EqualMock();
        EqualMock mock2 = new EqualMock(); // different reference, but equals true
        when(invocation.getMock()).thenReturn(mock1);
        when(invocation2.getMock()).thenReturn(mock2);
        InvocationMatcher im = new InvocationMatcher(invocation);
        // hasSimilarMethod should return true (mocks are equal), but buggy returns false
        assertTrue(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        when(invocation2.getMethod()).thenReturn(differentNameMethod);
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertFalse(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        when(invocation2.isVerified()).thenReturn(true);
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertFalse(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_differentMockReference_returnsFalse() {
        Object mockA = new Object();
        Object mockB = new Object(); // not equal, not same
        when(invocation.getMock()).thenReturn(mockA);
        when(invocation2.getMock()).thenReturn(mockB);
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertFalse(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_sameMethodButDifferentMethodNotOverloaded_returnsTrue() {
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(method);
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertTrue(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_overloadedButSameArgs_returnsFalse() {
        // Overloaded method (different method objects) but arguments match
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(differentMethod); // different method
        // Arguments match via safelyArgumentsMatch -> returns true (matchers match)
        when(matcher1.matches(any())).thenReturn(true);
        when(matcher2.matches(any())).thenReturn(true);
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertFalse(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_overloadedButDifferentArgs_returnsTrue() {
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(differentMethod);
        // Arguments mismatch -> safelyArgumentsMatch returns false
        when(matcher1.matches(any())).thenReturn(false);
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertTrue(im.hasSimilarMethod(invocation2));
    }

    @Test
    public void testHasSimilarMethod_safelyArgumentsMatchException_returnsFalseAndThenTrue() {
        // safelyArgumentsMatch throws exception -> returns false, so overloadedButSameArgs false -> returns true
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(differentMethod);
        when(matcher1.matches(any())).thenThrow(new RuntimeException());
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertTrue(im.hasSimilarMethod(invocation2));
    }

    // --- captureArgumentsFrom tests ---

    @Test
    public void testCaptureArgumentsFrom_capturesMatchingArguments() {
        List<Matcher> mixedMatchers = new LinkedList<Matcher>();
        mixedMatchers.add(matcher1);
        mixedMatchers.add(capturer);
        mixedMatchers.add(matcher2);
        InvocationMatcher im = new InvocationMatcher(invocation, mixedMatchers);
        im.captureArgumentsFrom(invocation2);
        // capturer.captureFrom should have been called with the second argument
        verify(capturer).captureFrom(invocation2.getArguments()[1]);
    }

    @Test
    public void testCaptureArgumentsFrom_skipsNonCapturingMatchers() {
        List<Matcher> noCapturers = Arrays.asList(matcher1, matcher2);
        InvocationMatcher im = new InvocationMatcher(invocation, noCapturers);
        im.captureArgumentsFrom(invocation2);
        verify(capturer, never()).captureFrom(any());
    }

    @Test
    public void testCaptureArgumentsFrom_skipsIfArgumentArrayTooShort() {
        when(invocation2.getArguments()).thenReturn(new Object[] { "only" });
        List<Matcher> mixedMatchers = new LinkedList<Matcher>();
        mixedMatchers.add(matcher1);
        mixedMatchers.add(capturer);
        InvocationMatcher im = new InvocationMatcher(invocation, mixedMatchers);
        im.captureArgumentsFrom(invocation2);
        // capturer should not be called because index 1 >= length 1
        verify(capturer, never()).captureFrom(any());
    }

    // --- matches test (basic, using equals mock) ---

    @Test
    public void testMatches_mockEqualsMethodSameArgsMatch_returnsTrue() {
        EqualMock mockInstance = new EqualMock();
        when(invocation.getMock()).thenReturn(mockInstance);
        when(invocation2.getMock()).thenReturn(mockInstance); // same reference, equals true
        when(invocation.getMethod()).thenReturn(method);
        when(invocation2.getMethod()).thenReturn(method);
        // Ensure arguments match via matchers
        when(matcher1.matches(any())).thenReturn(true);
        when(matcher2.matches(any())).thenReturn(true);
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        assertTrue(im.matches(invocation2));
    }

    @Test
    public void testMatches_mockNotEquals_returnsFalse() {
        when(invocation.getMock()).thenReturn(new Object());
        when(invocation2.getMock()).thenReturn(new Object());
        InvocationMatcher im = new InvocationMatcher(invocation);
        assertFalse(im.matches(invocation2));
    }

    // --- toString (basic) ---

    @Test
    public void testToString_delegatesToInvocation() {
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        when(invocation.toString(matchers, any())).thenReturn("mocked string");
        assertEquals("mocked string", im.toString());
    }

    // --- createFrom ---

    @Test
    public void testCreateFrom_emptyList_returnsEmptyList() {
        List<Invocation> invocations = new LinkedList<Invocation>();
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateFrom_singleInvocation_returnsSingleMatcher() {
        List<Invocation> invocations = Arrays.asList(invocation);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertEquals(1, result.size());
        assertSame(invocation, result.get(0).getInvocation());
    }
}