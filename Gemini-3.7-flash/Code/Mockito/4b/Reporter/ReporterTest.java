package org.mockito.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.CannotStubVoidMethodWithReturnValue;
import org.mockito.exceptions.misusing.CannotVerifyStubOnlyMock;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Location;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReporterTest {

    private Reporter reporter;

    private static class DummyTarget {
        private String dummyField;
    }

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    private DescribedInvocation createDescribedInvocation(final String description) {
        return new DescribedInvocation() {
            @Override
            public String toString() {
                return description;
            }

            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
    }

    // Tests checkedExceptionInvalid exception message
    @Test
    public void testCheckedExceptionInvalid_withThrowable_throwsMockitoException() {
        Exception cause = new Exception("custom checked exception");
        try {
            reporter.checkedExceptionInvalid(cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("custom checked exception"));
        }
    }

    // Tests cannotStubWithNullThrowable exception
    @Test(expected = MockitoException.class)
    public void testCannotStubWithNullThrowable_throwsMockitoException() {
        reporter.cannotStubWithNullThrowable();
    }

    // Tests unfinishedStubbing with location information
    @Test
    public void testUnfinishedStubbing_withLocation_throwsUnfinishedStubbingException() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedStubbing(location);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
        }
    }

    // Tests missingMethodInvocation exception message
    @Test
    public void testMissingMethodInvocation_throwsMissingMethodInvocationException() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument"));
        }
    }

    // Tests notAMockPassedToVerify with class parameter
    @Test
    public void testNotAMockPassedToVerify_nonMockClass_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    // Tests nullPassedToVerify exception message
    @Test
    public void testNullPassedToVerify_throwsNullInsteadOfMockException() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() should be a mock but is null!"));
        }
    }

    // Tests wantedButNotInvoked with single invocation
    @Test
    public void testWantedButNotInvoked_singleInvocation_throwsWantedButNotInvoked() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        try {
            reporter.wantedButNotInvoked(wanted);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("foo.bar()"));
        }
    }

    // Tests wantedButNotInvoked branch with empty invocations list
    @Test
    public void testWantedButNotInvoked_emptyInvocationsList_throwsWantedButNotInvoked() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        try {
            reporter.wantedButNotInvoked(wanted, Collections.<DescribedInvocation>emptyList());
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    // Tests wantedButNotInvoked branch with non-empty invocations list
    @Test
    public void testWantedButNotInvoked_nonEmptyInvocationsList_throwsWantedButNotInvoked() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        List<DescribedInvocation> invocations = new ArrayList<DescribedInvocation>();
        invocations.add(createDescribedInvocation("foo.baz()"));

        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
            assertTrue(e.getMessage().contains("foo.baz()"));
        }
    }

    // Tests wantedButNotInvokedInOrder exception
    @Test
    public void testWantedButNotInvokedInOrder_throwsVerificationInOrderFailure() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        DescribedInvocation previous = createDescribedInvocation("foo.previous()");
        try {
            reporter.wantedButNotInvokedInOrder(wanted, previous);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
            assertTrue(e.getMessage().contains("Wanted anywhere AFTER following interaction:"));
        }
    }

    // Tests tooManyActualInvocations exception message
    @Test
    public void testTooManyActualInvocations_throwsTooManyActualInvocations() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        Location location = new LocationImpl();
        try {
            reporter.tooManyActualInvocations(1, 2, wanted, location);
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
            assertTrue(e.getMessage().contains("But was 2 times."));
        }
    }

    // Tests neverWantedButInvoked exception message
    @Test
    public void testNeverWantedButInvoked_throwsNeverWantedButInvoked() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        Location location = new LocationImpl();
        try {
            reporter.neverWantedButInvoked(wanted, location);
            fail("Expected NeverWantedButInvoked");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("foo.bar()"));
        }
    }

    // Tests tooLittleActualInvocations exception
    @Test
    public void testTooLittleActualInvocations_withDiscrepancy_throwsTooLittleActualInvocations() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()");
        Location location = new LocationImpl();
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, location);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 1 time:"));
        }
    }

    // Tests cannotMockFinalClass exception message
    @Test
    public void testCannotMockFinalClass_withFinalClass_throwsMockitoException() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
            assertTrue(e.getMessage().contains("final classes"));
        }
    }

    // Tests cannotStubVoidMethodWithAReturnValue exception
    @Test
    public void testCannotStubVoidMethodWithAReturnValue_throwsCannotStubVoidMethodWithReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("doSomething");
            fail("Expected CannotStubVoidMethodWithReturnValue");
        } catch (CannotStubVoidMethodWithReturnValue e) {
            assertTrue(e.getMessage().contains("'doSomething' is a *void method*"));
        }
    }

    // Tests wantedAtMostX assertion error
    @Test
    public void testWantedAtMostX_maxInvocationsExceeded_throwsMockitoAssertionError() {
        try {
            reporter.wantedAtMostX(2, 4);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 4"));
        }
    }

    // Tests cannotInjectDependency when exception details has a cause
    @Test
    public void testCannotInjectDependency_exceptionWithCause_throwsMockitoException() throws Exception {
        Field field = DummyTarget.class.getDeclaredField("dummyField");
        Exception cause = new Exception("underlying root cause");
        Exception details = new RuntimeException("outer failure", cause);

        try {
            reporter.cannotInjectDependency(field, new Object(), details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("dummyField"));
            assertTrue(e.getMessage().contains("underlying root cause"));
        }
    }

    // Tests cannotInjectDependency when exception details does not have a cause (defect reproduction)
    @Test
    public void testCannotInjectDependency_exceptionWithoutCause_throwsMockitoException() throws Exception {
        Field field = DummyTarget.class.getDeclaredField("dummyField");
        Exception details = new RuntimeException("failure without cause");

        try {
            reporter.cannotInjectDependency(field, new Object(), details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("dummyField"));
            assertNotNull(e.getCause());
        }
    }

    // Tests fieldInitialisationThrewException exception message
    @Test
    public void testFieldInitialisationThrewException_throwsMockitoException() throws Exception {
        Field field = DummyTarget.class.getDeclaredField("dummyField");
        Throwable details = new IllegalArgumentException("init failed");

        try {
            reporter.fieldInitialisationThrewException(field, details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'dummyField'"));
            assertTrue(e.getMessage().contains("init failed"));
            assertEquals(details, e.getCause());
        }
    }

    // Tests usingConstructorWithFancySerializable exception
    @Test
    public void testUsingConstructorWithFancySerializable_throwsMockitoException() {
        try {
            reporter.usingConstructorWithFancySerializable(SerializableMode.ACROSS_CLASSLOADERS);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocks instantiated with constructor cannot be combined with ACROSS_CLASSLOADERS serialization mode."));
        }
    }

    // Tests notAMockPassedToWhenMethod exception
    @Test
    public void testNotAMockPassedToWhenMethod_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToWhenMethod();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is not a mock!"));
        }
    }

    // Tests nullPassedToWhenMethod exception
    @Test
    public void testNullPassedToWhenMethod_throwsNullInsteadOfMockException() {
        try {
            reporter.nullPassedToWhenMethod();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is null!"));
        }
    }

    // Tests mocksHaveToBePassedToVerifyNoMoreInteractions exception
    @Test
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions_throwsMockitoException() {
        try {
            reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)"));
        }
    }

    // Tests notAMockPassedToVerifyNoMoreInteractions exception
    @Test
    public void testNotAMockPassedToVerifyNoMoreInteractions_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToVerifyNoMoreInteractions();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    // Tests nullPassedToVerifyNoMoreInteractions exception
    @Test
    public void testNullPassedToVerifyNoMoreInteractions_throwsNullInsteadOfMockException() {
        try {
            reporter.nullPassedToVerifyNoMoreInteractions();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    // Tests notAMockPassedWhenCreatingInOrder exception
    @Test
    public void testNotAMockPassedWhenCreatingInOrder_throwsNotAMockException() {
        try {
            reporter.notAMockPassedWhenCreatingInOrder();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    // Tests nullPassedWhenCreatingInOrder exception
    @Test
    public void testNullPassedWhenCreatingInOrder_throwsNullInsteadOfMockException() {
        try {
            reporter.nullPassedWhenCreatingInOrder();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    // Tests mocksHaveToBePassedWhenCreatingInOrder exception
    @Test
    public void testMocksHaveToBePassedWhenCreatingInOrder_throwsMockitoException() {
        try {
            reporter.mocksHaveToBePassedWhenCreatingInOrder();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)"));
        }
    }

    // Tests inOrderRequiresFamiliarMock exception
    @Test
    public void testInOrderRequiresFamiliarMock_throwsMockitoException() {
        try {
            reporter.inOrderRequiresFamiliarMock();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InOrder can only verify mocked objects"));
        }
    }

    // Tests stubPassedToVerify exception
    @Test
    public void testStubPassedToVerify_throwsCannotVerifyStubOnlyMock() {
        try {
            reporter.stubPassedToVerify();
            fail("Expected CannotVerifyStubOnlyMock");
        } catch (CannotVerifyStubOnlyMock e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is a stubOnly() mock."));
        }
    }

    // Tests extraInterfacesDoesNotAcceptNull exception
    @Test
    public void testExtraInterfacesDoesNotAcceptNull_throwsMockitoException() {
        try {
            reporter.extraInterfacesDoesNotAcceptNull();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept null parameters."));
        }
    }

    // Tests extraInterfacesAcceptsOnlyInterfaces exception
    @Test
    public void testExtraInterfacesAcceptsOnlyInterfaces_throwsMockitoException() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("getClass() is not an interface"));
        }
    }

    // Tests extraInterfacesCannotContainMockedType exception
    @Test
    public void testExtraInterfacesCannotContainMockedType_throwsMockitoException() {
        try {
            reporter.extraInterfacesCannotContainMockedType(List.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept the same type as the mocked type."));
        }
    }

    // Tests extraInterfacesRequiresAtLeastOneInterface exception
    @Test
    public void testExtraInterfacesRequiresAtLeastOneInterface_throwsMockitoException() {
        try {
            reporter.extraInterfacesRequiresAtLeastOneInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() requires at least interface class to be passed."));
        }
    }

    // Tests cannotCallRealMethodOnInterface exception
    @Test
    public void testCannotCallRealMethodOnInterface_throwsMockitoException() {
        try {
            reporter.cannotCallRealMethodOnInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot call real method on java interface."));
        }
    }

    // Tests cannotVerifyToString exception
    @Test
    public void testCannotVerifyToString_throwsMockitoException() {
        try {
            reporter.cannotVerifyToString();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito cannot verify toString()"));
        }
    }

    // Tests moreThanOneAnnotationNotAllowed exception
    @Test
    public void testMoreThanOneAnnotationNotAllowed_throwsMockitoException() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("myField");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("You cannot have more than one Mockito annotation on a field!"));
            assertTrue(e.getMessage().contains("myField"));
        }
    }

    // Tests unsupportedCombinationOfAnnotations exception
    @Test
    public void testUnsupportedCombinationOfAnnotations_throwsMockitoException() {
        try {
            reporter.unsupportedCombinationOfAnnotations("InjectMocks", "Mock");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("This combination of Mockito annotations is not supported: @InjectMocks and @Mock"));
        }
    }

    // Tests cannotInitializeForSpyAnnotation exception
    @Test
    public void testCannotInitializeForSpyAnnotation_throwsMockitoException() {
        Exception cause = new RuntimeException("spy failure");
        try {
            reporter.cannotInitializeForSpyAnnotation("spyField", cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate a @Spy for 'spyField'"));
            assertEquals(cause, e.getCause());
        }
    }

    // Tests cannotInitializeForInjectMocksAnnotation exception
    @Test
    public void testCannotInitializeForInjectMocksAnnotation_throwsMockitoException() {
        Exception cause = new RuntimeException("inject failure");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("injectField", cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'injectField'"));
            assertEquals(cause, e.getCause());
        }
    }

    // Tests atLeastMethodRequiresZeroOrPositiveNumberOfInvocations exception
    @Test
    public void testAtLeastMethodRequiresZeroOrPositiveNumberOfInvocations_negativeCount_throwsMockitoException() {
        try {
            reporter.atLeastMethodRequiresZeroOrPositiveNumberOfInvocations(-1);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Negative value is not allowed here"));
        }
    }

    // Tests noArgumentValueWasCaptured exception
    @Test
    public void testNoArgumentValueWasCaptured_throwsMockitoException() {
        try {
            reporter.noArgumentValueWasCaptured();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("No argument value was captured!"));
        }
    }

    // Tests smartNullPointerException exception
    @Test
    public void testSmartNullPointerException_throwsSmartNullPointerException() {
        Location location = new LocationImpl();
        try {
            reporter.smartNullPointerException("someMethod", location);
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
        }
    }

    // Tests wrongTypeOfReturnValue exception
    @Test
    public void testWrongTypeOfReturnValue_throwsWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "getName");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("Integer cannot be returned by getName()"));
            assertTrue(e.getMessage().contains("getName() should return String"));
        }
    }

    // Tests spyAndDelegateAreNotAllowed exception
    @Test
    public void testSpyAndDelegateAreNotAllowed_throwsMockitoException() {
        try {
            reporter.spyAndDelegateAreNotAllowed();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Both spy(Object) and toDelegate(1) have been used!"));
        }
    }
}