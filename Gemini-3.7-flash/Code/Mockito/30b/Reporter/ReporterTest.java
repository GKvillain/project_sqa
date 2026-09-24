package org.mockito.exceptions;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.Location;

public class ReporterTest {

    private Reporter reporter;

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    // Tests checkedExceptionInvalid exception and message
    @Test
    public void testCheckedExceptionInvalid_throwableProvided_throwsMockitoException() {
        try {
            reporter.checkedExceptionInvalid(new Exception("test checked exception"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("test checked exception"));
        }
    }

    // Tests unfinishedStubbing exception path
    @Test(expected = UnfinishedStubbingException.class)
    public void testUnfinishedStubbing_validLocation_throwsUnfinishedStubbingException() {
        reporter.unfinishedStubbing(new Location());
    }

    // Tests missingMethodInvocation exception path
    @Test(expected = MissingMethodInvocationException.class)
    public void testMissingMethodInvocation_throwsMissingMethodInvocationException() {
        reporter.missingMethodInvocation();
    }

    // Tests unfinishedVerificationException exception path
    @Test(expected = UnfinishedVerificationException.class)
    public void testUnfinishedVerificationException_validLocation_throwsUnfinishedVerificationException() {
        reporter.unfinishedVerificationException(new Location());
    }

    // Tests notAMockPassedToVerify exception path
    @Test
    public void testNotAMockPassedToVerify_classTypeProvided_throwsNotAMockException() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    // Tests nullPassedToVerify exception path
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToVerify_throwsNullInsteadOfMockException() {
        reporter.nullPassedToVerify();
    }

    // Tests notAMockPassedToWhenMethod exception path
    @Test(expected = NotAMockException.class)
    public void testNotAMockPassedToWhenMethod_throwsNotAMockException() {
        reporter.notAMockPassedToWhenMethod();
    }

    // Tests nullPassedToWhenMethod exception path
    @Test(expected = NullInsteadOfMockException.class)
    public void testNullPassedToWhenMethod_throwsNullInsteadOfMockException() {
        reporter.nullPassedToWhenMethod();
    }

    // Tests invalidUseOfMatchers exception path
    @Test
    public void testInvalidUseOfMatchers_countsProvided_throwsInvalidUseOfMatchersException() {
        try {
            reporter.invalidUseOfMatchers(2, 1);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("2 matchers expected, 1 recorded."));
        }
    }

    // Tests wantedButNotInvoked with empty invocations list
    @Test
    public void testWantedButNotInvoked_emptyInvocationsList_indicatesZeroInteractions() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        try {
            reporter.wantedButNotInvoked(wanted, Collections.<PrintableInvocation>emptyList());
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    // Tests wantedButNotInvoked with non-empty invocations list
    @Test
    public void testWantedButNotInvoked_nonEmptyInvocationsList_showsOtherInteractions() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        List<PrintableInvocation> actual = new ArrayList<PrintableInvocation>();
        actual.add(new DummyPrintableInvocation("actualMethod()"));

        try {
            reporter.wantedButNotInvoked(wanted, actual);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
        }
    }

    // Tests wantedButNotInvokedInOrder exception path
    @Test(expected = VerificationInOrderFailure.class)
    public void testWantedButNotInvokedInOrder_invocationsProvided_throwsVerificationInOrderFailure() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        PrintableInvocation previous = new DummyPrintableInvocation("previousMethod()");
        reporter.wantedButNotInvokedInOrder(wanted, previous);
    }

    // Tests tooManyActualInvocations exception path
    @Test(expected = TooManyActualInvocations.class)
    public void testTooManyActualInvocations_countsAndInvocationProvided_throwsTooManyActualInvocations() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        reporter.tooManyActualInvocations(1, 2, wanted, new Location());
    }

    // Tests neverWantedButInvoked exception path
    @Test(expected = NeverWantedButInvoked.class)
    public void testNeverWantedButInvoked_invocationAndLocationProvided_throwsNeverWantedButInvoked() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        reporter.neverWantedButInvoked(wanted, new Location());
    }

    // Tests tooLittleActualInvocations with null lastActualLocation
    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocations_nullLastLocation_throwsTooLittleActualInvocations() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocations(discrepancy, wanted, null);
    }

    // Tests tooLittleActualInvocations with non-null lastActualLocation
    @Test(expected = TooLittleActualInvocations.class)
    public void testTooLittleActualInvocations_withLastLocation_throwsTooLittleActualInvocations() {
        PrintableInvocation wanted = new DummyPrintableInvocation("wantedMethod()");
        Discrepancy discrepancy = new Discrepancy(2, 1);
        reporter.tooLittleActualInvocations(discrepancy, wanted, new Location());
    }

    // Tests wrongTypeOfReturnValue exception path
    @Test
    public void testWrongTypeOfReturnValue_typesProvided_throwsWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "getName");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("Integer cannot be returned by getName()"));
            assertTrue(e.getMessage().contains("getName() should return String"));
        }
    }

    // Tests wantedAtMostX exception path
    @Test
    public void testWantedAtMostX_countsProvided_throwsMockitoAssertionError() {
        try {
            reporter.wantedAtMostX(2, 4);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 4"));
        }
    }

    // Tests smartNullPointerException exception path
    @Test(expected = SmartNullPointerException.class)
    public void testSmartNullPointerException_locationProvided_throwsSmartNullPointerException() {
        reporter.smartNullPointerException(new Location());
    }

    // Tests cannotMockFinalClass exception path
    @Test
    public void testCannotMockFinalClass_classProvided_throwsMockitoException() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
        }
    }

    private static class DummyPrintableInvocation implements PrintableInvocation {
        private final String representation;
        private final Location location;

        DummyPrintableInvocation(String representation) {
            this.representation = representation;
            this.location = new Location();
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return representation;
        }
    }
}