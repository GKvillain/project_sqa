package org.mockito.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.reporting.Discrepancy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReporterTest {

    private Reporter reporter;

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    private PrintableInvocation createDummyPrintableInvocation(final String text) {
        return new PrintableInvocation() {
            public String toString() {
                return text;
            }

            public Location getLocation() {
                return new Location();
            }
        };
    }

    // Tests checkedExceptionInvalid throws MockitoException with cause details
    @Test
    public void testCheckedExceptionInvalid_withThrowable_throwsMockitoException() {
        try {
            reporter.checkedExceptionInvalid(new Exception("test checked"));
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("test checked"));
        }
    }

    // Tests cannotStubWithNullThrowable exception throwing
    @Test(expected = MockitoException.class)
    public void testCannotStubWithNullThrowable_throwsMockitoException() {
        reporter.cannotStubWithNullThrowable();
    }

    // Tests unfinishedStubbing throws UnfinishedStubbingException
    @Test
    public void testUnfinishedStubbing_withLocation_throwsUnfinishedStubbingException() {
        try {
            reporter.unfinishedStubbing(new Location());
            fail("Expected UnfinishedStubbingException to be thrown");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
        }
    }

    // Tests missingMethodInvocation exception throwing
    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation_throwsMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    // Tests unfinishedVerificationException throws UnfinishedVerificationException
    @Test
    public void testUnfinishedVerificationException_withLocation_throwsUnfinishedVerificationException() {
        try {
            reporter.unfinishedVerificationException(new Location());
            fail("Expected UnfinishedVerificationException to be thrown");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
        }
    }

    // Tests notAMockPassedToVerify throws NotAMockException
    @Test
    public void testNotAMockPassedToVerify_withClass_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException to be thrown");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    // Tests nullPassedToVerify throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerify_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerify();
    }

    // Tests notAMockPassedToWhenMethod throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToWhenMethod_throwsNotAMockException() {
        reporter.notAMockPassedToWhenMethod();
    }

    // Tests nullPassedToWhenMethod throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod_throwsNullInsteadOfMockException() {
        reporter.nullPassedToWhenMethod();
    }

    // Tests mocksHaveToBePassedToVerifyNoMoreInteractions throws MockitoException
    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions_throwsMockitoException() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    // Tests notAMockPassedToVerifyNoMoreInteractions throws NotAMockException
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToVerifyNoMoreInteractions_throwsNotAMockException() {
        reporter.notAMockPassedToVerifyNoMoreInteractions();
    }

    // Tests nullPassedToVerifyNoMoreInteractions throws NullInsteadOfMockException
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerifyNoMoreInteractions_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerifyNoMoreInteractions();
    }

    // Tests invalidUseOfMatchers throws InvalidUseOfMatchersException
    @Test
    public void testInvalidUseOfMatchers_withCounts_throwsInvalidUseOfMatchersException() {
        try {
            reporter.invalidUseOfMatchers(2, 1);
            fail("Expected InvalidUseOfMatchersException to be thrown");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("2 matchers expected, 1 recorded."));
        }
    }

    // Tests argumentsAreDifferent exception throwing
    @Test
    public void testArgumentsAreDifferent_withWantedAndActual_throwsAssertionError() {
        try {
            reporter.argumentsAreDifferent("wanted()", "actual()", new Location());
            fail("Expected AssertionError or ArgumentsAreDifferent to be thrown");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("Argument(s) are different!"));
        }
    }

    // Tests wantedButNotInvoked throws WantedButNotInvoked
    @Test
    public void testWantedButNotInvoked_withPrintableInvocation_throwsWantedButNotInvoked() {
        PrintableInvocation wanted = new PrintableInvocation() {
            public String toString() {
                return "mock.foo()";
            }
            public Location getLocation() {
                return new Location();
            }
        };

        try {
            reporter.wantedButNotInvoked(wanted);
            fail("Expected WantedButNotInvoked to be thrown");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("mock.foo()"));
        }
    }

    // Tests wantedButNotInvoked with empty invocations list
    @Test
    public void testWantedButNotInvoked_withEmptyInvocationsList_throwsWantedButNotInvoked() {
        PrintableInvocation wanted = new PrintableInvocation() {
            public String toString() {
                return "mock.bar()";
            }
            public Location getLocation() {
                return new Location();
            }
        };

        try {
            reporter.wantedButNotInvoked(wanted, Collections.<PrintableInvocation>emptyList());
            fail("Expected WantedButNotInvoked to be thrown");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    // Tests wantedButNotInvoked with non-empty invocations list
    @Test
    public void testWantedButNotInvoked_withNonEmptyInvocationsList_throwsWantedButNotInvoked() {
        PrintableInvocation wanted = new PrintableInvocation() {
            public String toString() {
                return "mock.bar()";
            }
            public Location getLocation() {
                return new Location();
            }
        };

        ArrayList<PrintableInvocation> invocations = new ArrayList<PrintableInvocation>();
        invocations.add(wanted);

        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked to be thrown");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
        }
    }

    // Tests smartNullPointerException throws SmartNullPointerException
    @Test
    public void testSmartNullPointerException_withLocation_throwsSmartNullPointerException() {
        try {
            reporter.smartNullPointerException(new Location());
            fail("Expected SmartNullPointerException to be thrown");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
            assertTrue(e.getMessage().contains("Because this method was *not* stubbed correctly:"));
        }
    }

    // Tests cannotMockFinalClass throws MockitoException with class details
    @Test
    public void testCannotMockFinalClass_withFinalClass_throwsMockitoException() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
        }
    }

    // Tests cannotStubVoidMethodWithAReturnValue throws MockitoException
    @Test
    public void testCannotStubVoidMethodWithAReturnValue_withMethodName_throwsMockitoException() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("voidMethod");
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("'voidMethod' is a *void method* and it *cannot* be stubbed with a *return value*!"));
        }
    }

    // Tests wrongTypeOfReturnValue throws WrongTypeOfReturnValue
    @Test
    public void testWrongTypeOfReturnValue_withTypes_throwsWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "getName");
            fail("Expected WrongTypeOfReturnValue to be thrown");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("Integer cannot be returned by getName()"));
            assertTrue(e.getMessage().contains("getName() should return String"));
        }
    }

    // Tests wantedAtMostX throws MockitoAssertionError
    @Test
    public void testWantedAtMostX_withCounts_throwsMockitoAssertionError() {
        try {
            reporter.wantedAtMostX(2, 3);
            fail("Expected MockitoAssertionError to be thrown");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 3"));
        }
    }

    // Tests cannotInitializeForSpyAnnotation with exception cause
    @Test
    public void testCannotInitializeForSpyAnnotation_withException_throwsMockitoException() {
        Exception cause = new RuntimeException("Constructor failed");
        try {
            reporter.cannotInitializeForSpyAnnotation("testField", cause);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instianate a @Spy for 'testField' field."));
            assertTrue(e.getMessage().contains("Constructor failed"));
            assertNotNull(e.getCause());
        }
    }

    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedWhenCreatingInOrder_throwsMockitoException() {
        reporter.mocksHaveToBePassedWhenCreatingInOrder();
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedWhenCreatingInOrder_throwsNotAMockException() {
        reporter.notAMockPassedWhenCreatingInOrder();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedWhenCreatingInOrder_throwsNullInsteadOfMockException() {
        reporter.nullPassedWhenCreatingInOrder();
    }

    @Test
    public void testTooLittleActualInvocations_throwsTooLittleActualInvocations() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.tooLittleActualInvocations(new Discrepancy(2, 1), wanted, new Location());
            fail("Expected TooLittleActualInvocations to be thrown");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("wanted()"));
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 1 time:"));
        }
    }

    @Test
    public void testTooLittleActualInvocationsInOrder_throwsVerificationInOrderFailure() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.tooLittleActualInvocationsInOrder(new Discrepancy(2, 1), wanted, new Location());
            fail("Expected VerificationInOrderFailure to be thrown");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("wanted()"));
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
        }
    }

    @Test
    public void testTooManyActualInvocations_throwsTooManyActualInvocations() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.tooManyActualInvocations(1, 2, wanted, new Location());
            fail("Expected TooManyActualInvocations to be thrown");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("wanted()"));
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
            assertTrue(e.getMessage().contains("But was 2 times."));
        }
    }

    @Test
    public void testTooManyActualInvocationsInOrder_throwsVerificationInOrderFailure() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.tooManyActualInvocationsInOrder(1, 2, wanted, new Location());
            fail("Expected VerificationInOrderFailure to be thrown");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("wanted()"));
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
        }
    }

    @Test
    public void testNeverWantedButInvoked_throwsNeverWantedButInvoked() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.neverWantedButInvoked(wanted, new Location());
            fail("Expected NeverWantedButInvoked to be thrown");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("But invoked here:"));
        }
    }

    @Test
    public void testNeverWantedButInvokedInOrder_throwsVerificationInOrderFailure() {
        PrintableInvocation wanted = createDummyPrintableInvocation("wanted()");
        try {
            reporter.neverWantedButInvokedInOrder(wanted, new Location());
            fail("Expected VerificationInOrderFailure to be thrown");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
            assertTrue(e.getMessage().contains("Never wanted here:"));
        }
    }

    @Test
    public void testNoMoreInteractionsWanted_throwsNoInteractionsWanted() {
        PrintableInvocation undesired = createDummyPrintableInvocation("undesired()");
        try {
            reporter.noMoreInteractionsWanted(undesired, Collections.<VerificationAwareInvocation>emptyList());
            fail("Expected NoInteractionsWanted to be thrown");
        } catch (NoInteractionsWanted e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction:"));
        }
    }

    @Test
    public void testNoMoreInteractionsWantedInOrder_throwsVerificationInOrderFailure() {
        PrintableInvocation undesired = createDummyPrintableInvocation("undesired()");
        try {
            reporter.noMoreInteractionsWantedInOrder(undesired);
            fail("Expected VerificationInOrderFailure to be thrown");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction:"));
        }
    }

    @Test(expected = MockitoException.class)
    public void testCannotVerifyToString_throwsMockitoException() {
        reporter.cannotVerifyToString();
    }

    @Test
    public void testMoreThanOneAnnotationNotAllowed_throwsMockitoException() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("mockField");
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("mockField"));
            assertTrue(e.getMessage().contains("You cannot have more than one Mockito annotation"));
        }
    }

    @Test
    public void testUnsupportedCombinationOfAnnotations_throwsMockitoException() {
        try {
            reporter.unsupportedCombinationOfAnnotations("InjectMocks", "Mock");
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InjectMocks"));
            assertTrue(e.getMessage().contains("Mock"));
        }
    }

    @Test
    public void testCannotInitializeForInjectMocksAnnotation_throwsMockitoException() {
        Exception cause = new RuntimeException("Injection error");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("injectField", cause);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("injectField"));
            assertTrue(e.getMessage().contains("Injection error"));
            assertNotNull(e.getCause());
        }
    }

    @Test(expected = MockitoException.class)
    public void testAtMostShouldNotBeUsedWithTimeout_throwsMockitoException() {
        reporter.atMostShouldNotBeUsedWithTimeout();
    }

    @Test(expected = MockitoException.class)
    public void testOnlyVoidMethodsCanBeSetToDoNothing_throwsMockitoException() {
        reporter.onlyVoidMethodsCanBeSetToDoNothing();
    }

    @Test
    public void testWrongTypeOfException_throwsMockitoException() {
        try {
            reporter.wrongTypeOfException(new Exception("wrong type"));
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("wrong type"));
        }
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesDoesNotAcceptNullParameters_throwsMockitoException() {
        reporter.extraInterfacesDoesNotAcceptNullParameters();
    }

    @Test
    public void testExtraInterfacesAcceptsOnlyInterfaces_throwsMockitoException() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("java.lang.String"));
            assertTrue(e.getMessage().contains("You cannot specify class which is not an interface"));
        }
    }

    @Test
    public void testExtraInterfacesCannotContainMockedType_throwsMockitoException() {
        try {
            reporter.extraInterfacesCannotContainMockedType(List.class);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("java.util.List"));
            assertTrue(e.getMessage().contains("Extra interfaces cannot contain the same type as the mocked type"));
        }
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesRequiresAtLeastOneInterface_throwsMockitoException() {
        reporter.extraInterfacesRequiresAtLeastOneInterface();
    }

    @Test
    public void testMockedTypeIsInconsistentWithSpiedInstanceType_throwsMockitoException() {
        try {
            reporter.mockedTypeIsInconsistentWithSpiedInstanceType(List.class, "someString");
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be exactly the same as the target class."));
        }
    }

    @Test(expected = MockitoException.class)
    public void testCannotCallRealMethodOnInterface_throwsMockitoException() {
        reporter.cannotCallRealMethodOnInterface();
    }

    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToDoAnswer_throwsNotAMockException() {
        reporter.notAMockPassedToDoAnswer();
    }

    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToDoAnswer_throwsNullInsteadOfMockException() {
        reporter.nullPassedToDoAnswer();
    }
}