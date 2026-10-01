package org.mockito.internal.invocation;

import org.junit.Test;
import org.hamcrest.Matcher;
import org.mockito.invocation.Invocation;
import org.mockito.internal.matchers.CapturesArguments;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvocationMatcherTest {

    // Test helper methods for reflection and stubbing
    public String sampleMethod(String arg) { return arg; }
    public String sampleMethod(Integer arg) { return arg.toString(); }
    public String overloadedMethod(String arg) { return arg; }
    public String overloadedMethod(Integer arg) { return arg.toString(); }
    public void varargsMethod(Object... args) {}

    private Invocation mockInvocation(Object mock, Method method, Object[] args, boolean verified) {
        Invocation inv = mock(Invocation.class);
        when(inv.getMock()).thenReturn(mock);
        when(inv.getMethod()).thenReturn(method);
        when(inv.getArguments()).thenReturn(args);
        when(inv.getRawArguments()).thenReturn(args);
        when(inv.isVerified()).thenReturn(verified);
        return inv;
    }

    // Tests constructor with empty matcher list and arguments present
    @Test
    public void testConstructor_emptyMatchersWithArgs_createsMatchersFromArgs() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation invocation = mockInvocation(this, m, new Object[]{"hello"}, false);
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertNotNull(im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    // Tests constructor with empty matcher list and no arguments
    @Test
    public void testConstructor_emptyMatchersWithNoArgs_createsEmptyMatchers() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation invocation = mockInvocation(this, m, new Object[0], false);
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertNotNull(im.getMatchers());
        assertTrue(im.getMatchers().isEmpty());
    }

    // Tests getMethod()
    @Test
    public void testGetMethod_returnsMethod() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation invocation = mockInvocation(this, m, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertSame(m, im.getMethod());
    }

    // Tests getInvocation()
    @Test
    public void testGetInvocation_returnsSameInvocation() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation inv = mockInvocation(this, m, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(inv);

        assertSame(inv, im.getInvocation());
    }

    // Tests getMatchers() when a non-empty list is supplied
    @Test
    public void testGetMatchers_returnsSameListWhenNonEmpty() {
        Matcher matcher = mock(Matcher.class);
        List<Matcher> matchers = Arrays.asList(matcher);
        InvocationMatcher im = new InvocationMatcher(mock(Invocation.class), matchers);

        assertSame(matchers, im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    // Tests toString() does not throw
    @Test
    public void testToString_nonNull() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation invocation = mockInvocation(this, m, new Object[]{"a"}, false);
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertNotNull(im.toString());
    }

    // Tests matches() with identical mock, method and arguments
    @Test
    public void testMatches_sameMockMethodArgs_returnsTrue() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Object mock = new Object();
        Invocation wanted = mockInvocation(mock, m, new Object[]{"same"}, false);
        Invocation actual = mockInvocation(mock, m, new Object[]{"same"}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertTrue(im.matches(actual));
    }

    // Tests matches() with different mock
    @Test
    public void testMatches_differentMock_returnsFalse() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation wanted = mockInvocation(new Object(), m, new Object[]{"x"}, false);
        Invocation actual = mockInvocation(new Object(), m, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertFalse(im.matches(actual));
    }

    // Tests matches() with different method
    @Test
    public void testMatches_differentMethod_returnsFalse() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("overloadedMethod", String.class);
        Invocation wanted = mockInvocation(this, m1, new Object[]{"x"}, false);
        Invocation actual = mockInvocation(this, m2, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertFalse(im.matches(actual));
    }

    // Tests matches() with different arguments
    @Test
    public void testMatches_differentArguments_returnsFalse() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation wanted = mockInvocation(this, m, new Object[]{"a"}, false);
        Invocation actual = mockInvocation(this, m, new Object[]{"b"}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertFalse(im.matches(actual));
    }

    // Tests hasSameMethod() with exactly the same method
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation inv1 = mockInvocation(this, m, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(this, m, new Object[]{"y"}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertTrue(im.hasSameMethod(inv2));
    }

    // Tests hasSameMethod() with different method name
    @Test
    public void testHasSameMethod_differentName_returnsFalse() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("overloadedMethod", String.class);
        Invocation inv1 = mockInvocation(this, m1, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(this, m2, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertFalse(im.hasSameMethod(inv2));
    }

    // Tests hasSameMethod() with same name but different parameter types
    @Test
    public void testHasSameMethod_differentParameterTypes_returnsFalse() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("sampleMethod", Integer.class);
        Invocation inv1 = mockInvocation(this, m1, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(this, m2, new Object[]{1}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertFalse(im.hasSameMethod(inv2));
    }

    // Tests hasSimilarMethod() with different method name
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("overloadedMethod", String.class);
        Invocation inv1 = mockInvocation(this, m1, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(this, m2, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertFalse(im.hasSimilarMethod(inv2));
    }

    // Tests hasSimilarMethod() when candidate is verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation inv1 = mockInvocation(this, m, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(this, m, new Object[]{"x"}, true);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertFalse(im.hasSimilarMethod(inv2));
    }

    // Tests hasSimilarMethod() with different mock
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation inv1 = mockInvocation(new Object(), m, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(new Object(), m, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertFalse(im.hasSimilarMethod(inv2));
    }

    // Tests hasSimilarMethod() with same method, unverified and same mock
    @Test
    public void testHasSimilarMethod_sameMethod_returnsTrue() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Object mock = new Object();
        Invocation inv1 = mockInvocation(mock, m, new Object[]{"x"}, false);
        Invocation inv2 = mockInvocation(mock, m, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(inv1);

        assertTrue(im.hasSimilarMethod(inv2));
    }

    // Tests hasSimilarMethod() with overloaded method and matching arguments
    @Test
    public void testHasSimilarMethod_overloadedSameArgs_returnsFalse() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("sampleMethod", Integer.class);
        Object mock = new Object();
        Invocation wanted = mockInvocation(mock, m1, new Object[]{"x"}, false);
        Invocation candidate = mockInvocation(mock, m2, new Object[]{"x"}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertFalse(im.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod() with overloaded method and different arguments
    @Test
    public void testHasSimilarMethod_overloadedDifferentArgs_returnsTrue() throws Exception {
        Method m1 = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Method m2 = InvocationMatcherTest.class.getMethod("sampleMethod", Integer.class);
        Object mock = new Object();
        Invocation wanted = mockInvocation(mock, m1, new Object[]{"x"}, false);
        Invocation candidate = mockInvocation(mock, m2, new Object[]{123}, false);
        InvocationMatcher im = new InvocationMatcher(wanted);

        assertTrue(im.hasSimilarMethod(candidate));
    }

    // Tests captureArgumentsFrom() with non-varargs and a non-capturing matcher
    @Test
    public void testCaptureArgumentsFrom_nonVarargs_noCapturingMatchers_noException() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation wanted = mockInvocation(this, m, new Object[]{"x"}, false);
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(mock(Matcher.class));
        InvocationMatcher im = new InvocationMatcher(wanted, matchers);

        Invocation actual = mockInvocation(this, m, new Object[]{"x"}, false);
        im.captureArgumentsFrom(actual);

        verify(actual, never()).getArgumentAt(anyInt(), any());
    }

    // Tests captureArgumentsFrom() with non-varargs and a capturing matcher
    @Test
    public void testCaptureArgumentsFrom_nonVarargs_capturingMatcher_called() throws Exception {
        Method m = InvocationMatcherTest.class.getMethod("sampleMethod", String.class);
        Invocation wanted = mockInvocation(this, m, new Object[]{"x"}, false);

        Matcher matcher = mock(Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        List<Matcher> matchers = Arrays.asList(matcher);
        InvocationMatcher im = new InvocationMatcher(wanted, matchers);

        Invocation actual = mockInvocation(this, m, new Object[]{"y"}, false);
        when(actual.getArgumentAt(0, Object.class)).thenReturn("captured");

        im.captureArgumentsFrom(actual);

        CapturesArguments capturer = (CapturesArguments) matcher;
        verify(capturer).captureFrom("captured");
    }

    // Tests createFrom() returns a list of InvocationMatcher
    @Test
    public void testCreateFrom_returnsMatchersForEachInvocation() {
        Invocation inv1 = mock(Invocation.class);
        Invocation inv2 = mock(Invocation.class);
        List<Invocation> list = Arrays.asList(inv1, inv2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(list);

        assertEquals(2, result.size());
        assertSame(inv1, result.get(0).getInvocation());
        assertSame(inv2, result.get(1).getInvocation());
    }
}