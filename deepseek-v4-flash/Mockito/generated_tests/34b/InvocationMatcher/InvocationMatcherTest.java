package org.mockito.internal.invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.internal.matchers.CapturesArguments;

public class InvocationMatcherTest {

    // Helper to create a fully stubbed Invocation
    private Invocation createStubInvocation(Method method, Object mockObj, Object[] args,
                                            boolean verified, Location location) {
        Invocation inv = mock(Invocation.class);
        when(inv.getMethod()).thenReturn(method);
        when(inv.getMock()).thenReturn(mockObj);
        when(inv.getArguments()).thenReturn(args);
        when(inv.isVerified()).thenReturn(verified);
        when(inv.getLocation()).thenReturn(location);
        return inv;
    }

    // Helper to create a simple Method mock with given name
    private Method createMethodMock(String name) {
        Method m = mock(Method.class);
        when(m.getName()).thenReturn(name);
        return m;
    }

    @Test
    // Tests the first constructor: matchers list empty -> delegates to invocation.argumentsToMatchers()
    public void testConstructor_emptyMatchers_createsMatchersFromInvocation() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> matchersFromInvocation = new ArrayList<>();
        when(inv.argumentsToMatchers()).thenReturn(matchersFromInvocation);

        InvocationMatcher invocationMatcher = new InvocationMatcher(inv, Collections.<Matcher>emptyList());
        assertSame(matchersFromInvocation, invocationMatcher.getMatchers());
    }

    @Test
    // Tests the second constructor: uses provided matchers
    public void testConstructor_withMatchers_usesProvidedMatchers() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> providedMatchers = new ArrayList<>();
        providedMatchers.add(mock(Matcher.class));

        InvocationMatcher invocationMatcher = new InvocationMatcher(inv, providedMatchers);
        assertSame(providedMatchers, invocationMatcher.getMatchers());
    }

    @Test
    // Tests the first constructor (no matchers argument) – uses argumentsToMatchers()
    public void testConstructor_noMatchers_usesArgumentsToMatchers() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> matchersFromInvocation = new ArrayList<>();
        matchersFromInvocation.add(mock(Matcher.class));
        when(inv.argumentsToMatchers()).thenReturn(matchersFromInvocation);

        InvocationMatcher invocationMatcher = new InvocationMatcher(inv);
        assertSame(matchersFromInvocation, invocationMatcher.getMatchers());
    }

    @Test
    // Tests getMethod returns invocation's method
    public void testGetMethod_returnsInvocationMethod() {
        Method method = createMethodMock("someMethod");
        Invocation inv = createStubInvocation(method, new Object(), new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(inv);

        assertSame(method, im.getMethod());
    }

    @Test
    // Tests getInvocation returns the stored invocation
    public void testGetInvocation_returnsStoredInvocation() {
        Invocation inv = mock(Invocation.class);
        InvocationMatcher im = new InvocationMatcher(inv);
        assertSame(inv, im.getInvocation());
    }

    @Test
    // Tests getMatchers returns the matchers list
    public void testGetMatchers_returnsMatchers() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> matchers = new ArrayList<>();
        InvocationMatcher im = new InvocationMatcher(inv, matchers);
        assertSame(matchers, im.getMatchers());
    }

    @Test
    // Tests toString delegates to invocation with default PrintSettings
    public void testToString_delegatesToInvocation() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> matchers = new ArrayList<>();
        String expectedString = "myInvocation";
        when(inv.toString(matchers, new PrintSettings())).thenReturn(expectedString);

        InvocationMatcher im = new InvocationMatcher(inv, matchers);
        assertEquals(expectedString, im.toString());
    }

    @Test
    // Tests matches returns true when mock, method, and arguments all match
    public void testMatches_allConditionsTrue_returnsTrue() {
        Object mockObj = new Object();
        Method method = createMethodMock("foo");
        Object[] args = {"arg1", 2};
        Invocation actual = createStubInvocation(method, mockObj, args, false, null);

        Matcher matcher1 = mock(Matcher.class);
        when(matcher1.matches("arg1")).thenReturn(true);
        Matcher matcher2 = mock(Matcher.class);
        when(matcher2.matches(2)).thenReturn(true);
        List<Matcher> matchers = Arrays.asList(matcher1, matcher2);

        InvocationMatcher im = new InvocationMatcher(actual, matchers);
        assertTrue(im.matches(actual));
    }

    @Test
    // Tests matches returns false when mock is different
    public void testMatches_mockNotEqual_returnsFalse() {
        Object mockObj1 = new Object();
        Object mockObj2 = new Object();
        Method method = createMethodMock("foo");
        Object[] args = {"arg"};
        Invocation wanted = createStubInvocation(method, mockObj1, args, false, null);
        Invocation actual = createStubInvocation(method, mockObj2, args, false, null);

        InvocationMatcher im = new InvocationMatcher(wanted);
        assertFalse(im.matches(actual));
    }

    @Test
    // Tests matches returns false when method is different
    public void testMatches_methodNotEqual_returnsFalse() {
        Object mockObj = new Object();
        Method method1 = createMethodMock("foo");
        Method method2 = createMethodMock("bar");
        Object[] args = {"arg"};
        Invocation wanted = createStubInvocation(method1, mockObj, args, false, null);
        Invocation actual = createStubInvocation(method2, mockObj, args, false, null);

        InvocationMatcher im = new InvocationMatcher(wanted);
        assertFalse(im.matches(actual));
    }

    @Test
    // Tests matches returns false when arguments do not match
    public void testMatches_argumentsNotMatch_returnsFalse() {
        Object mockObj = new Object();
        Method method = createMethodMock("foo");
        Object[] wantedArgs = {"expected"};
        Object[] actualArgs = {"different"};
        Invocation wanted = createStubInvocation(method, mockObj, wantedArgs, false, null);
        Invocation actual = createStubInvocation(method, mockObj, actualArgs, false, null);

        Matcher matcher = mock(Matcher.class);
        when(matcher.matches("different")).thenReturn(false);
        List<Matcher> matchers = Collections.singletonList(matcher);

        InvocationMatcher im = new InvocationMatcher(wanted, matchers);
        assertFalse(im.matches(actual));
    }

    @Test
    // Tests matches returns false when argument count differs from matcher count
    public void testMatches_argumentsLengthMismatch_returnsFalse() {
        Object mockObj = new Object();
        Method method = createMethodMock("foo");
        Object[] args = {"singleArg"}; // only one argument
        Invocation actual = createStubInvocation(method, mockObj, args, false, null);

        // Two matchers for one argument -> mismatch
        Matcher matcher1 = mock(Matcher.class);
        Matcher matcher2 = mock(Matcher.class);
        List<Matcher> matchers = Arrays.asList(matcher1, matcher2);

        InvocationMatcher im = new InvocationMatcher(actual, matchers);
        // The underlying ArgumentsComparator should detect size mismatch and return false
        assertFalse(im.matches(actual));
    }

    @Test
    // Tests hasSameMethod returns true when method objects are equal
    public void testHasSameMethod_sameMethod_returnsTrue() {
        Method method = createMethodMock("foo");
        Invocation inv1 = createStubInvocation(method, new Object(), new Object[0], false, null);
        Invocation inv2 = createStubInvocation(method, new Object(), new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(inv1);
        assertTrue(im.hasSameMethod(inv2));
    }

    @Test
    // Tests hasSameMethod returns false when method objects are different
    public void testHasSameMethod_differentMethod_returnsFalse() {
        Method method1 = createMethodMock("foo");
        Method method2 = createMethodMock("foo");
        // Different mock objects -> not equal
        Invocation inv1 = createStubInvocation(method1, new Object(), new Object[0], false, null);
        Invocation inv2 = createStubInvocation(method2, new Object(), new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(inv1);
        assertFalse(im.hasSameMethod(inv2));
    }

    @Test
    // Tests hasSimilarMethod returns true when all conditions are met and method equals
    public void testHasSimilarMethod_allConditionsTrue_methodEqual_returnsTrue() {
        Object mockObj = new Object();
        Method method = createMethodMock("foo");
        Invocation wanted = createStubInvocation(method, mockObj, new Object[0], false, null);
        Invocation candidate = createStubInvocation(method, mockObj, new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(wanted);
        assertTrue(im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests hasSimilarMethod returns false when method names differ
    public void testHasSimilarMethod_methodNameNotEqual_returnsFalse() {
        Object mockObj = new Object();
        Method method1 = createMethodMock("foo");
        Method method2 = createMethodMock("bar");
        Invocation wanted = createStubInvocation(method1, mockObj, new Object[0], false, null);
        Invocation candidate = createStubInvocation(method2, mockObj, new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(wanted);
        assertFalse(im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests hasSimilarMethod returns false when candidate is verified
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Object mockObj = new Object();
        Method method = createMethodMock("foo");
        Invocation wanted = createStubInvocation(method, mockObj, new Object[0], false, null);
        Invocation candidate = createStubInvocation(method, mockObj, new Object[0], true, null); // verified
        InvocationMatcher im = new InvocationMatcher(wanted);
        assertFalse(im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests hasSimilarMethod returns false when mock objects differ
    public void testHasSimilarMethod_mockNotSame_returnsFalse() {
        Object mockObj1 = new Object();
        Object mockObj2 = new Object();
        Method method = createMethodMock("foo");
        Invocation wanted = createStubInvocation(method, mockObj1, new Object[0], false, null);
        Invocation candidate = createStubInvocation(method, mockObj2, new Object[0], false, null);
        InvocationMatcher im = new InvocationMatcher(wanted);
        assertFalse(im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests hasSimilarMethod with overloaded method and same arguments:
    // Expected correct behavior: should return true (candidate is still similar).
    // Buggy version returns false. This test detects the defect.
    public void testHasSimilarMethod_overloadedSameArgs_returnsTrue() {
        Object mockObj = new Object();

        // Two different Method objects with same name
        Method method1 = createMethodMock("overloaded");
        Method method2 = createMethodMock("overloaded");
        // Ensure they are not equal (different mock objects already)

        Object[] args = {"commonArg"};
        Invocation wanted = createStubInvocation(method1, mockObj, args, false, null);
        Invocation candidate = createStubInvocation(method2, mockObj, args, false, null);

        // Provide matcher that matches the argument
        Matcher matcher = mock(Matcher.class);
        when(matcher.matches("commonArg")).thenReturn(true);
        List<Matcher> matchers = Collections.singletonList(matcher);

        InvocationMatcher im = new InvocationMatcher(wanted, matchers);
        // The correct implementation should return true because method name, mock, unverified are same.
        // The buggy implementation returns false because !methodEquals && safelyArgumentsMatch => overloadedButSameArgs=true.
        assertTrue("hasSimilarMethod should return true for overloaded method with same args", im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests hasSimilarMethod with overloaded method and different args: should return true
    public void testHasSimilarMethod_overloadedDifferentArgs_returnsTrue() {
        Object mockObj = new Object();
        Method method1 = createMethodMock("overloaded");
        Method method2 = createMethodMock("overloaded");
        Object[] wantedArgs = {"A"};
        Object[] candArgs = {"B"};
        Invocation wanted = createStubInvocation(method1, mockObj, wantedArgs, false, null);
        Invocation candidate = createStubInvocation(method2, mockObj, candArgs, false, null);

        // Matcher that won't match "B" (since we want arguments to not match)
        Matcher matcher = mock(Matcher.class);
        when(matcher.matches("B")).thenReturn(false);
        List<Matcher> matchers = Collections.singletonList(matcher);

        InvocationMatcher im = new InvocationMatcher(wanted, matchers);
        // overloadedButSameArgs false because args don't match -> returns true
        assertTrue(im.hasSimilarMethod(candidate));
    }

    @Test
    // Tests getLocation returns the location from invocation
    public void testGetLocation_returnsInvocationLocation() {
        Location location = mock(Location.class);
        Invocation inv = createStubInvocation(mock(Method.class), new Object(), new Object[0], false, location);
        InvocationMatcher im = new InvocationMatcher(inv);
        assertSame(location, im.getLocation());
    }

    @Test
    // Tests toString with custom PrintSettings
    public void testToString_withPrintSettings_returnsInvocationToString() {
        Invocation inv = mock(Invocation.class);
        List<Matcher> matchers = new ArrayList<>();
        PrintSettings settings = new PrintSettings();
        String expected = "custom string";
        when(inv.toString(matchers, settings)).thenReturn(expected);

        InvocationMatcher im = new InvocationMatcher(inv, matchers);
        assertEquals(expected, im.toString(settings));
    }

    @Test
    // Tests captureArgumentsFrom iterates over matchers and calls captureFrom for those implementing CapturesArguments
    public void testCaptureArgumentsFrom_callsCaptureOnMatchingMatchers() {
        // We create a matcher that implements CapturesArguments
        Matcher capturingMatcher = mock(Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        // Also ensure the matcher is in the matchers list
        List<Matcher> matchers = Collections.singletonList(capturingMatcher);

        Object[] args = {"capturedArg"};
        Invocation inv = createStubInvocation(mock(Method.class), new Object(), args, false, null);
        InvocationMatcher im = new InvocationMatcher(inv, matchers);

        im.captureArgumentsFrom(inv);
        // Verify the captureFrom method was called with the first argument
        verify((CapturesArguments) capturingMatcher).captureFrom("capturedArg");
    }

    @Test
    // Tests captureArgumentsFrom skips matchers not implementing CapturesArguments
    public void testCaptureArgumentsFrom_skipsNonCapturingMatchers() {
        Matcher nonCapturingMatcher = mock(Matcher.class);
        List<Matcher> matchers = Collections.singletonList(nonCapturingMatcher);

        Object[] args = {"ignored"};
        Invocation inv = createStubInvocation(mock(Method.class), new Object(), args, false, null);
        InvocationMatcher im = new InvocationMatcher(inv, matchers);

        im.captureArgumentsFrom(inv);
        // No interaction on nonCapturingMatcher, nothing to verify
    }

    @Test
    // Tests captureArgumentsFrom when invocation arguments are null – should handle gracefully without exception
    public void testCaptureArgumentsFrom_argumentsNull_doesNotThrow() {
        Matcher capturingMatcher = mock(Matcher.class, withSettings().extraInterfaces(CapturesArguments.class));
        List<Matcher> matchers = Collections.singletonList(capturingMatcher);

        // arguments are null
        Invocation inv = createStubInvocation(mock(Method.class), new Object(), null, false, null);
        InvocationMatcher im = new InvocationMatcher(inv, matchers);

        try {
            im.captureArgumentsFrom(inv);
        } catch (Exception e) {
            fail("captureArgumentsFrom should not throw when arguments are null, but threw: " + e);
        }
        // captureFrom should not be called because arguments are null (loop may check for null)
        verify((CapturesArguments) capturingMatcher, never()).captureFrom(any());
    }
}