package org.mockito.internal.invocation;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.mockito.invocation.Invocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
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

    // Tests constructor with empty matchers creating default matchers from arguments
    @Test
    public void testConstructor_withEmptyMatchers_createsDefaultMatchers() {
        Invocation inv = new InvocationBuilder().args("foo").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(inv, Collections.<Matcher>emptyList());

        assertEquals(1, matcher.getMatchers().size());
        assertTrue(matcher.getMatchers().get(0) instanceof Equals);
    }

    // Tests constructor with explicit matchers
    @Test
    public void testConstructor_withExplicitMatchers_usesProvidedMatchers() {
        Matcher customMatcher = new CustomMatcher();
        List<Matcher> matchers = Arrays.asList(customMatcher);
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertEquals(1, matcher.getMatchers().size());
        assertSame(customMatcher, matcher.getMatchers().get(0));
    }

    // Tests getMethod returns underlying invocation method
    @Test
    public void testGetMethod_returnsInvocationMethod() {
        Method expectedMethod = invocation.getMethod();
        assertEquals(expectedMethod, invocationMatcher.getMethod());
    }

    // Tests getInvocation returns the invocation passed to constructor
    @Test
    public void testGetInvocation_returnsInvocation() {
        assertSame(invocation, invocationMatcher.getInvocation());
    }

    // Tests getMatchers returns matchers list
    @Test
    public void testGetMatchers_returnsMatchers() {
        assertNotNull(invocationMatcher.getMatchers());
        assertEquals(2, invocationMatcher.getMatchers().size());
    }

    // Tests toString prints method call with matchers
    @Test
    public void testToString_returnsStringRepresentation() {
        String result = invocationMatcher.toString();
        assertNotNull(result);
        assertTrue(result.contains("simpleMethod"));
    }

    // Tests matches when invocation matches target invocation
    @Test
    public void testMatches_sameInvocation_returnsTrue() {
        assertTrue(invocationMatcher.matches(invocation));
    }

    // Tests matches when method differs
    @Test
    public void testMatches_differentMethod_returnsFalse() {
        Invocation differentMethodInvocation = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(invocationMatcher.matches(differentMethodInvocation));
    }

    // Tests matches when arguments differ
    @Test
    public void testMatches_differentArguments_returnsFalse() {
        Invocation differentArgsInvocation = new InvocationBuilder().args("other1", "other2").toInvocation();
        assertFalse(invocationMatcher.matches(differentArgsInvocation));
    }

    // Tests matches when mock differs
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Invocation differentMockInvocation = new InvocationBuilder().mock(new Object()).args("arg1", "arg2").toInvocation();
        assertFalse(invocationMatcher.matches(differentMockInvocation));
    }

    // Tests hasSameMethod with identical method
    @Test
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Invocation sameMethodInvocation = new InvocationBuilder().args("other1", "other2").toInvocation();
        assertTrue(invocationMatcher.hasSameMethod(sameMethodInvocation));
    }

    // Tests hasSameMethod with different method name
    @Test
    public void testHasSameMethod_differentMethodName_returnsFalse() {
        Invocation differentMethodInvocation = new InvocationBuilder().differentMethod().toInvocation();
        assertFalse(invocationMatcher.hasSameMethod(differentMethodInvocation));
    }

    // Tests hasSameMethod with different parameter count
    @Test
    public void testHasSameMethod_differentParamCount_returnsFalse() {
        Invocation oneArgInvocation = new InvocationBuilder().args("onlyOne").toInvocation();
        assertFalse(invocationMatcher.hasSameMethod(oneArgInvocation));
    }

    // Tests hasSimilarMethod for unverified same mock and method
    @Test
    public void testHasSimilarMethod_similarInvocation_returnsTrue() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).args("other1", "other2").toInvocation();
        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod for overloaded method
    @Test
    public void testHasSimilarMethod_overloadedMethod_returnsTrue() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).method("simpleMethod").arg("singleArg").toInvocation();
        assertTrue(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when method name is different
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).differentMethod().toInvocation();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when mock is different
    @Test
    public void testHasSimilarMethod_differentMock_returnsFalse() {
        Invocation candidate = new InvocationBuilder().args("arg1", "arg2").toInvocation();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when candidate is already verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Invocation candidate = new InvocationBuilder().mock(invocation.getMock()).args("arg1", "arg2").toInvocation();
        candidate.markVerified();
        assertFalse(invocationMatcher.hasSimilarMethod(candidate));
    }

    // Tests getLocation returns location of invocation
    @Test
    public void testGetLocation_returnsInvocationLocation() {
        assertEquals(invocation.getLocation(), invocationMatcher.getLocation());
    }

    // Tests captureArgumentsFrom with non-varargs method
    @Test
    public void testCaptureArgumentsFrom_nonVarArgs_capturesArguments() {
        CapturingMatcher capturingMatcher1 = new CapturingMatcher();
        CapturingMatcher capturingMatcher2 = new CapturingMatcher();
        List<Matcher> matchers = Arrays.<Matcher>asList(capturingMatcher1, capturingMatcher2);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);

        assertEquals(1, capturingMatcher1.captured.size());
        assertEquals("arg1", capturingMatcher1.captured.get(0));
        assertEquals(1, capturingMatcher2.captured.size());
        assertEquals("arg2", capturingMatcher2.captured.get(0));
    }

    // Tests captureArgumentsFrom with varargs method
    @Test
    public void testCaptureArgumentsFrom_varArgs_capturesArguments() {
        Invocation varArgsInvocation = new InvocationBuilder().method("varargs").args("a", "b", "c").toInvocation();
        CapturingMatcher capturingMatcher1 = new CapturingMatcher();
        CapturingMatcher capturingMatcher2 = new CapturingMatcher();
        CapturingMatcher capturingMatcher3 = new CapturingMatcher();
        List<Matcher> matchers = Arrays.<Matcher>asList(capturingMatcher1, capturingMatcher2, capturingMatcher3);

        InvocationMatcher matcher = new InvocationMatcher(varArgsInvocation, matchers);
        matcher.captureArgumentsFrom(varArgsInvocation);

        assertEquals(1, capturingMatcher1.captured.size());
        assertEquals(1, capturingMatcher2.captured.size());
        assertEquals(1, capturingMatcher3.captured.size());
    }

    // Tests captureArgumentsFrom when matchers list exceeds number of raw arguments
    @Test
    public void testCaptureArgumentsFrom_whenMatchersCountExceedsArguments_doesNotThrow() {
        CapturingMatcher capturingMatcher1 = new CapturingMatcher();
        CapturingMatcher capturingMatcher2 = new CapturingMatcher();
        CapturingMatcher capturingMatcher3 = new CapturingMatcher();
        List<Matcher> matchers = Arrays.<Matcher>asList(capturingMatcher1, capturingMatcher2, capturingMatcher3);

        Invocation oneArgInvocation = new InvocationBuilder().args("onlyOne").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(oneArgInvocation, matchers);
        matcher.captureArgumentsFrom(oneArgInvocation);

        assertEquals(1, capturingMatcher1.captured.size());
        assertEquals("onlyOne", capturingMatcher1.captured.get(0));
        assertEquals(0, capturingMatcher2.captured.size());
        assertEquals(0, capturingMatcher3.captured.size());
    }

    // Tests captureArgumentsFrom with matcher not implementing CapturesArguments
    @Test
    public void testCaptureArgumentsFrom_nonCapturingMatcher_doesNotThrow() {
        Matcher regularMatcher = new CustomMatcher();
        List<Matcher> matchers = Arrays.asList(regularMatcher, regularMatcher);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        assertNotNull(matcher);
    }

    // Tests createFrom converts list of Invocations to list of InvocationMatchers
    @Test
    public void testCreateFrom_invocationsList_returnsInvocationMatchers() {
        Invocation inv1 = new InvocationBuilder().args("a").toInvocation();
        Invocation inv2 = new InvocationBuilder().args("b").toInvocation();
        List<Invocation> invocations = Arrays.asList(inv1, inv2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

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

    private static class CustomMatcher extends BaseMatcher<Object> {
        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("custom");
        }
    }

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        final List<Object> captured = new ArrayList<Object>();

        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("capturing");
        }

        public void captureFrom(Object argument) {
            captured.add(argument);
        }
    }
}