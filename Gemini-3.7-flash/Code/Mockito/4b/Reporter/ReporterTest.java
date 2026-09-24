package org.mockito.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.CannotStubVoidMethodWithReturnValue;
import org.mockito.exceptions.misusing.CannotVerifyStubOnlyMock;
import org.mockito.exceptions.misusing.FriendlyReminderException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.matchers.LocalizedMatcher;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Location;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReporterTest {

    private Reporter reporter;
    public String dummyField;

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    // Tests defect where details exception has no cause (details.getCause() == null)
    @Test
    public void testCannotInjectDependency_exceptionWithoutCause_throwsMockitoException() throws Exception {
        Field field = getClass().getField("dummyField");
        Exception details = new Exception("Direct message without cause");
        try {
            reporter.cannotInjectDependency(field, "mockObject", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("dummyField"));
        }
    }

    // Tests cannotInjectDependency with cause present
    @Test
    public void testCannotInjectDependency_exceptionWithCause_throwsMockitoException() throws Exception {
        Field field = getClass().getField("dummyField");
        Exception cause = new Exception("Root cause error");
        Exception details = new Exception("Wrapper error", cause);
        try {
            reporter.cannotInjectDependency(field, "mockObject", details);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Root cause error"));
        }
    }

    // Tests checkedExceptionInvalid exception path and message
    @Test(expected = MockitoException.class)
    public void testCheckedExceptionInvalid_throwableProvided_throwsMockitoException() {
        reporter.checkedExceptionInvalid(new Exception("Invalid checked"));
    }

    // Tests cannotStubWithNullThrowable exception path
    @Test(expected = MockitoException.class)
    public void testCannotStubWithNullThrowable_throwsMockitoException() {
        reporter.cannotStubWithNullThrowable();
    }

    // Tests unfinishedStubbing exception message
    @Test
    public void testUnfinishedStubbing_locationProvided_throwsUnfinishedStubbingException() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedStubbing(location);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
        }
    }

    // Tests incorrectUseOfApi exception path
    @Test(expected = MockitoException.class)
    public void testIncorrectUseOfApi_throwsMockitoException() {
        reporter.incorrectUseOfApi();
    }

    // Tests missingMethodInvocation exception path
    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation_throwsMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    // Tests unfinishedVerificationException message
    @Test
    public void testUnfinishedVerificationException_locationProvided_throwsUnfinishedVerificationException() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedVerificationException(location);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
        }
    }

    // Tests notAMockPassedToVerify message contains type name
    @Test
    public void testNotAMockPassedToVerify_typeProvided_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("String"));
        }
    }

    // Tests nullPassedToVerify exception path
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerify_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerify();
    }

    // Tests nullPassedToWhenMethod exception path
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod_throwsNullInsteadOfMockException() {
        reporter.nullPassedToWhenMethod();
    }

    // Tests mocksHaveToBePassedToVerifyNoMoreInteractions exception path
    @Test(expected = MockitoException.class)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions_throwsMockitoException() {
        reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
    }

    // Tests stubPassedToVerify exception path
    @Test(expected = CannotVerifyStubOnlyMock.class)
    public void testStubPassedToVerify_throwsCannotVerifyStubOnlyMock() {
        reporter.stubPassedToVerify();
    }

    // Tests reportNoSubMatchersFound message contains matcher name
    @Test
    public void testReportNoSubMatchersFound_nameProvided_throwsInvalidUseOfMatchersException() {
        try {
            reporter.reportNoSubMatchersFound("AdditionalMatcher");
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("AdditionalMatcher"));
        }
    }

    // Tests wantedButNotInvoked with empty invocations list branch
    @Test
    public void testWantedButNotInvoked_emptyInvocationsList_throwsWantedButNotInvoked() {
        DescribedInvocation wanted = new DescribedInvocation() {
            public String toString() {
                return "mock.method()";
            }
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.wantedButNotInvoked(wanted, Collections.<DescribedInvocation>emptyList());
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    // Tests cannotMockFinalClass message contains class name
    @Test
    public void testCannotMockFinalClass_finalClass_throwsMockitoException() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("String"));
        }
    }

    // Tests cannotStubVoidMethodWithAReturnValue message contains method name
    @Test
    public void testCannotStubVoidMethodWithAReturnValue_methodNameProvided_throwsCannotStubVoidMethodWithReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("voidMethod");
            fail("Expected CannotStubVoidMethodWithReturnValue");
        } catch (CannotStubVoidMethodWithReturnValue e) {
            assertTrue(e.getMessage().contains("voidMethod"));
        }
    }

    // Tests wantedAtMostX message with count pluralization
    @Test
    public void testWantedAtMostX_countsProvided_throwsMockitoAssertionError() {
        try {
            reporter.wantedAtMostX(2, 5);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 5"));
        }
    }

    // Tests extraInterfacesDoesNotAcceptNullParameters exception path
    @Test(expected = MockitoException.class)
    public void testExtraInterfacesDoesNotAcceptNullParameters_throwsMockitoException() {
        reporter.extraInterfacesDoesNotAcceptNullParameters();
    }

    // Tests usingConstructorWithFancySerializable message contains mode
    @Test
    public void testUsingConstructorWithFancySerializable_modeProvided_throwsMockitoException() {
        try {
            reporter.usingConstructorWithFancySerializable(SerializableMode.ACROSS_CLASSLOADERS);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("ACROSS_CLASSLOADERS"));
        }
    }
}