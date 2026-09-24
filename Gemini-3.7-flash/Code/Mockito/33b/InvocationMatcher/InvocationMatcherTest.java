package org.mockito.internal.invocation;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.Mockito;

public class InvocationMatcherTest {

    private interface SuperInterface {
        void execute(String param);
        void simpleMethod();
    }

    private interface SubInterface extends SuperInterface {
        @Override
        void execute(String param);
    }

    private static class ImplementingClass implements SuperInterface {
        @Override
        public void execute(String param) {}
        @Override
        public void simpleMethod() {}
        public void differentMethod() {}
        public void overloadedMethod(String s) {}
        public void overloadedMethod(int i) {}
    }

    private static class DummyCapturingMatcher implements Matcher<Object>, CapturesArguments {
        private Object captured;

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(org.hamcrest.Description description) {}

        @Override
        public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}

        @Override
        public void captureFrom(Object argument) {
            this.captured = argument;
        }

        public Object getCaptured() {
            return captured;
        }
    }

    private Invocation invocation;
    private Invocation candidate;
    private Method simpleMethod;
    private Method superExecuteMethod;
    private Method subExecuteMethod;

    @Before
    public void setUp() throws Exception {
        invocation = Mockito.mock(Invocation.class);
        candidate = Mockito.mock(Invocation.class);

        simpleMethod = ImplementingClass.class.getMethod("simpleMethod");
        superExecuteMethod = SuperInterface.class.getMethod("execute", String.class);
        subExecuteMethod = SubInterface.class.getMethod("execute", String.class);
    }

    // Tests constructor with empty matcher list falling back to invocation.argumentsToMatchers()
    @Test
    public void testConstructor_emptyMatchers_usesArgumentsToMatchers() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new IsEqual<String>("test"));
        Mockito.when(invocation.argumentsToMatchers()).thenReturn(matchers);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(matchers, matcher.getMatchers());
        assertEquals(invocation, matcher.getInvocation());
    }

    // Tests constructor with explicitly provided matchers
    @Test
    public void testConstructor_withExplicitMatchers_usesProvidedMatchers() {
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new IsEqual<String>("custom"));

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertEquals(matchers, matcher.getMatchers());
        assertEquals(invocation, matcher.getInvocation());
    }

    // Tests getMethod delegates to underlying invocation
    @Test
    public void testGetMethod_returnsInvocationMethod() {
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertEquals(simpleMethod, matcher.getMethod());
    }

    // Tests getLocation delegates to underlying invocation
    @Test
    public void testGetLocation_returnsInvocationLocation() {
        Location location = Mockito.mock(Location.class);
        Mockito.when(invocation.getLocation()).thenReturn(location);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertSame(location, matcher.getLocation());
    }

    // Tests hasSameMethod with exact same Method reflection instance
    @Test
    public void testHasSameMethod_identicalMethod_returnsTrue() {
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(simpleMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod with different methods
    @Test
    public void testHasSameMethod_differentMethod_returnsFalse() throws Exception {
        Method diffMethod = ImplementingClass.class.getMethod("differentMethod");
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(diffMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(matcher.hasSameMethod(candidate));
    }

    // Tests hasSameMethod when method is declared in super interface vs sub interface (Defects4J 33b defect)
    @Test
    public void testHasSameMethod_declaredInSuperInterfaceAndSubInterface_returnsTrue() {
        Mockito.when(invocation.getMethod()).thenReturn(superExecuteMethod);
        Mockito.when(candidate.getMethod()).thenReturn(subExecuteMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertTrue(matcher.hasSameMethod(candidate));
    }

    // Tests matches returns true when mock, method, and arguments match
    @Test
    public void testMatches_allConditionsMatch_returnsTrue() {
        Object mock = new Object();
        Mockito.when(invocation.getMock()).thenReturn(mock);
        Mockito.when(candidate.getMock()).thenReturn(mock);
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(simpleMethod);
        Mockito.when(invocation.getArguments()).thenReturn(new Object[0]);
        Mockito.when(candidate.getArguments()).thenReturn(new Object[0]);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertTrue(matcher.matches(candidate));
    }

    // Tests matches returns false when mocks are different
    @Test
    public void testMatches_differentMock_returnsFalse() {
        Mockito.when(invocation.getMock()).thenReturn(new Object());
        Mockito.when(candidate.getMock()).thenReturn(new Object());

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(matcher.matches(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate method name differs
    @Test
    public void testHasSimilarMethod_differentMethodName_returnsFalse() throws Exception {
        Method diffMethod = ImplementingClass.class.getMethod("differentMethod");
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(diffMethod);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when candidate is already verified
    @Test
    public void testHasSimilarMethod_verifiedCandidate_returnsFalse() {
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.isVerified()).thenReturn(true);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false when mocks are different instances
    @Test
    public void testHasSimilarMethod_differentMockInstances_returnsFalse() {
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.isVerified()).thenReturn(false);
        Mockito.when(invocation.getMock()).thenReturn(new Object());
        Mockito.when(candidate.getMock()).thenReturn(new Object());

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns true for unverified candidate with same mock and same method
    @Test
    public void testHasSimilarMethod_sameMethodAndUnverifiedAndSameMock_returnsTrue() {
        Object mock = new Object();
        Mockito.when(invocation.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.getMethod()).thenReturn(simpleMethod);
        Mockito.when(candidate.isVerified()).thenReturn(false);
        Mockito.when(invocation.getMock()).thenReturn(mock);
        Mockito.when(candidate.getMock()).thenReturn(mock);

        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    // Tests hasSimilarMethod returns false for overloaded method with same matching argument types
    @Test
    public void testHasSimilarMethod_overloadedMethodSameArgs_returnsFalse() throws Exception {
        Object mock = new Object();
        Method m1 = ImplementingClass.class.getMethod("overloadedMethod", String.class);
        Method m2 = ImplementingClass.class.getMethod("overloadedMethod", int.class);

        Mockito.when(invocation.getMethod()).thenReturn(m1);
        Mockito.when(candidate.getMethod()).thenReturn(m2);
        Mockito.when(candidate.isVerified()).thenReturn(false);
        Mockito.when(invocation.getMock()).thenReturn(mock);
        Mockito.when(candidate.getMock()).thenReturn(mock);

        Matcher stringMatcher = new IsEqual<String>("val");
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(stringMatcher);

        Mockito.when(candidate.getArguments()).thenReturn(new Object[]{"val"});

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    // Tests captureArgumentsFrom capturing value when matcher implements CapturesArguments
    @Test
    public void testCaptureArgumentsFrom_matcherImplementsCapturesArguments_capturesValue() {
        DummyCapturingMatcher capturingMatcher = new DummyCapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);

        Mockito.when(candidate.getArguments()).thenReturn(new Object[]{"capturedValue"});

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(candidate);

        assertEquals("capturedValue", capturingMatcher.getCaptured());
    }

    // Tests captureArgumentsFrom with fewer arguments than matchers to test boundary branch
    @Test
    public void testCaptureArgumentsFrom_fewerArgumentsThanMatchers_doesNotThrow() {
        DummyCapturingMatcher capturingMatcher = new DummyCapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);

        Mockito.when(candidate.getArguments()).thenReturn(new Object[0]);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(candidate);

        assertNull(capturingMatcher.getCaptured());
    }

    // Tests createFrom static factory method creating list of InvocationMatcher
    @Test
    public void testCreateFrom_listOfInvocations_returnsListOfInvocationMatchers() {
        Invocation i1 = Mockito.mock(Invocation.class);
        Invocation i2 = Mockito.mock(Invocation.class);
        Mockito.when(i1.argumentsToMatchers()).thenReturn(Collections.<Matcher>emptyList());
        Mockito.when(i2.argumentsToMatchers()).thenReturn(Collections.<Matcher>emptyList());

        List<Invocation> invocations = Arrays.asList(i1, i2);
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(i1, result.get(0).getInvocation());
        assertSame(i2, result.get(1).getInvocation());
    }

    // Tests toString with and without PrintSettings
    @Test
    public void testToString_delegatesToInvocationToString() {
        List<Matcher> matchers = Collections.emptyList();
        Mockito.when(invocation.toString(Mockito.eq(matchers), Mockito.any(PrintSettings.class)))
                .thenReturn("mockInvocation.toString()");

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertEquals("mockInvocation.toString()", matcher.toString());

        PrintSettings settings = new PrintSettings();
        Mockito.when(invocation.toString(matchers, settings)).thenReturn("mockInvocationWithSettings.toString()");
        assertEquals("mockInvocationWithSettings.toString()", matcher.toString(settings));
    }
}