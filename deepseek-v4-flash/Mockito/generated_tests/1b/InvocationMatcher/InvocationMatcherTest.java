package org.mockito.internal.invocation;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.mockito.internal.matchers.*;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;

public class InvocationMatcherTest {

    private Object mock;
    private Method toStringMethod;
    private Method equalsMethod;
    private Method printfMethod;
    private Invocation invocationToString;
    private Invocation invocationEquals;
    private Invocation invocationVarargs;

    @Before
    public void setUp() throws Exception {
        mock = new Object();
        toStringMethod = Object.class.getMethod("toString");
        equalsMethod = Object.class.getMethod("equals", Object.class);
        printfMethod = PrintStream.class.getMethod("printf", String.class, Object[].class);

        invocationToString = new InvocationBuilder()
                .mock(mock)
                .method(toStringMethod)
                .args()
                .toInvocation();

        invocationEquals = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("hello")
                .toInvocation();

        invocationVarargs = new InvocationBuilder()
                .mock(System.out)
                .method(printfMethod)
                .args("format", new Object[]{"arg1", "arg2"})
                .toInvocation();
    }

    private InvocationMatcher createMatcher(Invocation invocation, List<org.hamcrest.Matcher> matchers) {
        return new InvocationMatcher(invocation, matchers);
    }

    private InvocationMatcher createMatcher(Invocation invocation) {
        return new InvocationMatcher(invocation);
    }

    // Tests constructor with empty matchers -> uses ArgumentsProcessor to create matchers from arguments
    @Test
    public void testConstructor_emptyMatchers_createsMatchersFromArguments() {
        InvocationMatcher matcher = createMatcher(invocationEquals);
        assertEquals(1, matcher.getMatchers().size());
    }

    // Tests constructor with non-empty matchers -> uses provided matchers
    @Test
    public void testConstructor_nonEmptyMatchers_usesGivenMatchers() {
        List<org.hamcrest.Matcher> matcherList = Arrays.<org.hamcrest.Matcher>asList(new Equals("hello"));
        InvocationMatcher matcher = createMatcher(invocationEquals, matcherList);
        assertSame(matcherList, matcher.getMatchers());
    }

    // Tests matches when all conditions (mock, method, arguments) are met
    @Test
    public void testMatches_allConditionsMet_returnsTrue() {
        List<org.hamcrest.Matcher> matchers = Arrays.<org.hamcrest.Matcher>asList(new Equals("hello"));
        InvocationMatcher matcher = createMatcher(invocationEquals, matchers);
        Invocation actual = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("hello")
                .toInvocation();
        assertTrue(matcher.matches(actual));
    }

    // Tests matches when mock is not equal
    @Test
    public void testMatches_mockNotEqual_returnsFalse() {
        List<org.hamcrest.Matcher> matchers = Arrays.<org.hamcrest.Matcher>asList(new Equals("hello"));
        InvocationMatcher matcher = createMatcher(invocationEquals, matchers);
        Invocation actual = new InvocationBuilder()
                .mock(new Object())
                .method(equalsMethod)
                .args("hello")
                .toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests matches when method is different
    @Test
    public void testMatches_methodNotEqual_returnsFalse() throws Exception {
        List<org.hamcrest.Matcher> matchers = Arrays.<org.hamcrest.Matcher>asList(new Equals("hello"));
        InvocationMatcher matcher = createMatcher(invocationEquals, matchers);
        Method diffMethod = Object.class.getMethod("hashCode");
        Invocation actual = new InvocationBuilder()
                .mock(mock)
                .method(diffMethod)
                .args()
                .toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests matches when arguments do not match
    @Test
    public void testMatches_argumentsNotMatch_returnsFalse() {
        List<org.hamcrest.Matcher> matchers = Arrays.<org.hamcrest.Matcher>asList(new Equals("world"));
        InvocationMatcher matcher = createMatcher(invocationEquals, matchers);
        Invocation actual = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("hello")
                .toInvocation();
        assertFalse(matcher.matches(actual));
    }

    // Tests hasSimilarMethod when method name differs
    @Test
    public void testHasSimilarMethod_nameDifferent_returnsFalse() throws Exception {
        InvocationMatcher matcher = createMatcher(invocationToString);
        Invocation candidate = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("x")
                .toInvocation();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when mock is not the same
    @Test
    public void testHasSimilarMethod_mockNotSame_returnsFalse() {
        InvocationMatcher matcher = createMatcher(invocationToString);
        Invocation candidate = new InvocationBuilder()
                .mock(new Object())
                .method(toStringMethod)
                .args()
                .toInvocation();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when method equals and unverified -> returns true
    @Test
    public void testHasSimilarMethod_methodEqualsUnverified_returnsTrue() {
        InvocationMatcher matcher = createMatcher(invocationToString);
        Invocation candidate = new InvocationBuilder()
                .mock(mock)
                .method(toStringMethod)
                .args()
                .toInvocation();
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod when overloaded method but arguments match (safelyArgumentsMatch true) -> returns false
    @Test
    public void testHasSimilarMethod_overloadedSameArgs_returnsFalse() throws Exception {
        Method valueOfObject = String.class.getMethod("valueOf", Object.class);
        Method valueOfCharArray = String.class.getMethod("valueOf", char[].class);
        Object mock = new Object();
        Invocation wantedInvocation = new InvocationBuilder()
                .mock(mock)
                .method(valueOfObject)
                .args(new Object[]{null})
                .toInvocation();
        InvocationMatcher matcher = createMatcher(wantedInvocation);
        Invocation candidate = new InvocationBuilder()
                .mock(mock)
                .method(valueOfCharArray)
                .args(new Object[]{null})
                .toInvocation();
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSameMethod when method name equal but parameter types differ
    @Test
    public void testHasSameMethod_nameEqualParamsDifferent_returnsFalse() throws Exception {
        Method valueOfInt = String.class.getMethod("valueOf", int.class);
        Method valueOfBool = String.class.getMethod("valueOf", boolean.class);
        InvocationMatcher matcher = createMatcher(new InvocationBuilder()
                .mock("")
                .method(valueOfInt)
                .args(1)
                .toInvocation());
        Invocation candidate = new InvocationBuilder()
                .mock("")
                .method(valueOfBool)
                .args(true)
                .toInvocation();
        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod when method name equal and parameter types equal
    @Test
    public void testHasSameMethod_nameEqualParamsEqual_returnsTrue() {
        InvocationMatcher matcher = createMatcher(invocationEquals);
        Invocation candidate = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("world")
                .toInvocation();
        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod when method name equal but parameter count differs
    @Test
    public void testHasSameMethod_paramsLengthDifferent_returnsFalse() throws Exception {
        Method format1 = String.class.getMethod("format", String.class, Object[].class);
        Method format2 = String.class.getMethod("format", Locale.class, String.class, Object[].class);
        InvocationMatcher matcher = createMatcher(new InvocationBuilder()
                .mock("")
                .method(format1)
                .args("x", new Object[]{})
                .toInvocation());
        Invocation candidate = new InvocationBuilder()
                .mock("")
                .method(format2)
                .args(Locale.US, "y", new Object[]{})
                .toInvocation();
        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests captureArgumentsFrom for non-varargs invocation captures arguments correctly
    @Test
    public void testCaptureArgumentsFrom_nonVarargs_capturesArguments() {
        class CapturingMatcher extends org.hamcrest.BaseMatcher<Object> implements CapturesArguments {
            private Object captured;
            @Override
            public boolean matches(Object item) { return true; }
            @Override
            public void describeTo(org.hamcrest.Description description) { }
            @Override
            public void captureFrom(Object argument) { this.captured = argument; }
            public Object getCaptured() { return captured; }
        }
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        List<org.hamcrest.Matcher> matcherList = Arrays.<org.hamcrest.Matcher>asList(capturingMatcher);
        InvocationMatcher matcher = createMatcher(invocationEquals, matcherList);
        Invocation actualInvocation = new InvocationBuilder()
                .mock(mock)
                .method(equalsMethod)
                .args("hello")
                .toInvocation();
        matcher.captureArgumentsFrom(actualInvocation);
        assertEquals("hello", capturingMatcher.getCaptured());
    }

    // Tests captureArgumentsFrom for varargs invocation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCaptureArgumentsFrom_varargs_throwsUnsupportedOperationException() {
        List<org.hamcrest.Matcher> matcherList = Collections.emptyList();
        InvocationMatcher matcher = createMatcher(invocationVarargs, matcherList);
        matcher.captureArgumentsFrom(invocationVarargs);
    }

    // Tests static createFrom method
    @Test
    public void testCreateFrom_listOfInvocations_returnsList() {
        List<Invocation> invocations = Arrays.asList(invocationToString, invocationEquals);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);
        assertEquals(2, result.size());
        assertSame(invocationToString, result.get(0).getInvocation());
        assertSame(invocationEquals, result.get(1).getInvocation());
    }

    // Tests getMethod returns correct method
    @Test
    public void testGetMethod_returnsMethod() {
        InvocationMatcher matcher = createMatcher(invocationEquals);
        assertSame(equalsMethod, matcher.getMethod());
    }

    // Tests getInvocation returns the invocation
    @Test
    public void testGetInvocation_returnsInvocation() {
        InvocationMatcher matcher = createMatcher(invocationToString);
        assertSame(invocationToString, matcher.getInvocation());
    }

    // Tests getLocation returns non-null location
    @Test
    public void testGetLocation_returnsLocation() {
        InvocationMatcher matcher = createMatcher(invocationToString);
        Location location = matcher.getLocation();
        assertNotNull(location);
    }

    // Tests toString returns non-null string
    @Test
    public void testToString_returnsString() {
        InvocationMatcher matcher = createMatcher(invocationToString);
        assertNotNull(matcher.toString());
    }
}