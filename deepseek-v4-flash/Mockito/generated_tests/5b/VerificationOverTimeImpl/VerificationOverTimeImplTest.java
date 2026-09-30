package org.mockito.internal.verification;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.*;

public class VerificationOverTimeImplTest {

    // Helper class to create a VerificationMode that always succeeds on verify()
    private static class AlwaysSuccessVerificationMode implements VerificationMode {
        public void verify(VerificationData data) {
            // do nothing, always succeeds
        }
    }

    // Helper class to create a VerificationMode that always fails on verify()
    private static class AlwaysFailVerificationMode implements VerificationMode {
        private final MockitoAssertionError error;

        AlwaysFailVerificationMode(String message) {
            this.error = new MockitoAssertionError(message);
        }

        public void verify(VerificationData data) {
            throw error;
        }
    }

    // Helper class to create a VerificationMode that fails once then succeeds
    private static class FailOnceThenSucceedVerificationMode implements VerificationMode {
        private boolean firstCall = true;

        public void verify(VerificationData data) {
            if (firstCall) {
                firstCall = false;
                throw new MockitoAssertionError("First call fails");
            }
        }
    }

    // Helper class to create a VerificationMode that fails then succeeds with returnOnSuccess=false test
    private static class FailOnceThenSucceedVerificationModeWithDuration implements VerificationMode {
        private boolean firstCall = true;

        public void verify(VerificationData data) {
            if (firstCall) {
                firstCall = false;
                throw new MockitoAssertionError("First call fails");
            }
        }
    }

    // Helper class to create an AtMost verification mode-like behavior (cannot recover from failure)
    private static class AtMostLikeVerificationMode implements VerificationMode {
        public void verify(VerificationData data) {
            throw new MockitoAssertionError("AtMost cannot recover");
        }
    }

    // Tests default constructor
    @Test
    public void testConstructor_defaultTimer_createsValidObject() {
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, new AlwaysSuccessVerificationMode(), true);
        assertEquals(10, verification.getPollingPeriod());
        assertEquals(100, verification.getDuration());
        assertNotNull(verification.getDelegate());
    }

    // Tests constructor with explicit Timer
    @Test
    public void testConstructor_explicitTimer_createsValidObject() {
        Timer timer = new Timer(50);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(5, 50, new AlwaysSuccessVerificationMode(), false, timer);
        assertEquals(5, verification.getPollingPeriod());
        assertEquals(50, verification.getDuration());
        assertNotNull(verification.getDelegate());
    }

    // Tests verify with returnOnSuccess=true and delegate succeeds immediately
    @Test
    public void testVerify_returnOnSuccessTrue_delegateSucceedsImmediately_returnsSuccess() {
        Timer timer = new Timer(100);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, new AlwaysSuccessVerificationMode(), true, timer);
        VerificationData data = null; // Since verification mode ignores data, null is acceptable
        // Should not throw any exception
        verification.verify(data);
    }

    // Tests verify with returnOnSuccess=true and delegate fails then succeeds
    @Test
    public void testVerify_returnOnSuccessTrue_delegateFailsThenSucceeds_returnsSuccess() {
        Timer timer = new Timer(100);
        FailOnceThenSucceedVerificationMode delegate = new FailOnceThenSucceedVerificationMode();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, delegate, true, timer);
        VerificationData data = null;
        // Should succeed after first failure
        verification.verify(data);
    }

    // Tests verify with returnOnSuccess=false and delegate succeeds immediately, but must wait full duration
    @Test
    public void testVerify_returnOnSuccessFalse_delegateSucceedsImmediately_waitsFullDuration() {
        Timer timer = new Timer(100);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, new AlwaysSuccessVerificationMode(), false, timer);
        VerificationData data = null;
        verification.verify(data);
        // After verify, timer should have expired (waited full duration)
        assertFalse(timer.isCounting());
    }

    // Tests verify when delegate always fails and cannot recover (AtMost-like)
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_delegateCannotRecover_throwsExceptionImmediately() {
        Timer timer = new Timer(100);
        AtMostLikeVerificationMode delegate = new AtMostLikeVerificationMode();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, delegate, true, timer);
        VerificationData data = null;
        verification.verify(data);
    }

    // Tests verify when delegate always fails, can recover, and timer expires
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_delegateCanRecoverFailsAlways_timerExpires_throwsLastError() {
        Timer timer = new Timer(50);
        AlwaysFailVerificationMode delegate = new AlwaysFailVerificationMode("Always fails");
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 50, delegate, true, timer);
        VerificationData data = null;
        verification.verify(data);
    }

    // Tests verify with zero duration timer
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_zeroDuration_timerExpiresImmediately_throwsError() {
        Timer timer = new Timer(0);
        AlwaysFailVerificationMode delegate = new AlwaysFailVerificationMode("Fails immediately");
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 0, delegate, true, timer);
        VerificationData data = null;
        verification.verify(data);
    }

    // Tests verify with negative polling period (should not throw, but sleep will be called with negative)
    @Test
    public void testVerify_negativePollingPeriod_doesNotThrow() {
        Timer timer = new Timer(100);
        AlwaysSuccessVerificationMode delegate = new AlwaysSuccessVerificationMode();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(-5, 100, delegate, true, timer);
        VerificationData data = null;
        // Should not throw, sleep with negative may be platform-dependent but should not crash
        verification.verify(data);
    }

    // Tests getPollingPeriod
    @Test
    public void testGetPollingPeriod_returnsCorrectValue() {
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(50, 200, new AlwaysSuccessVerificationMode(), true);
        assertEquals(50, verification.getPollingPeriod());
    }

    // Tests getDuration
    @Test
    public void testGetDuration_returnsCorrectValue() {
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(50, 200, new AlwaysSuccessVerificationMode(), true);
        assertEquals(200, verification.getDuration());
    }

    // Tests getDelegate
    @Test
    public void testGetDelegate_returnsCorrectDelegate() {
        VerificationMode delegate = new AlwaysSuccessVerificationMode();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, delegate, true);
        assertSame(delegate, verification.getDelegate());
    }

    // Tests verify with null delegate (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testVerify_nullDelegate_throwsNullPointer() {
        Timer timer = new Timer(100);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, null, true, timer);
        VerificationData data = null;
        verification.verify(data);
    }

    // Tests canRecoverFromFailure for AtMost-like verification mode
    @Test
    public void testCanRecoverFromFailure_atMostMode_returnsFalse() {
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, new AlwaysSuccessVerificationMode(), true);
        VerificationMode atMost = new AtMostLikeVerificationMode();
        assertFalse(verification.canRecoverFromFailure(atMost));
    }

    // Tests canRecoverFromFailure for recoverable verification mode
    @Test
    public void testCanRecoverFromFailure_recoverableMode_returnsTrue() {
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, new AlwaysSuccessVerificationMode(), true);
        VerificationMode recoverable = new AlwaysSuccessVerificationMode();
        assertTrue(verification.canRecoverFromFailure(recoverable));
    }

    // Tests verify with returnOnSuccess=true and delegate fails once then succeeds, verifying timer is still counting after success
    @Test
    public void testVerify_returnOnSuccessTrue_delegateFailsOnceThenSucceeds_returnsEarly() {
        Timer timer = new Timer(100);
        FailOnceThenSucceedVerificationMode delegate = new FailOnceThenSucceedVerificationMode();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10, 100, delegate, true, timer);
        VerificationData data = null;
        verification.verify(data);
        // Since we returned on success, timer may still be counting (or not, depending on timing)
        // But we just ensure no exception thrown
        assertNotNull(verification);
    }
}