package org.mockito.internal.verification;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VerificationOverTimeImplTest {

    private VerificationData dummyData;

    @Before
    public void setUp() {
        dummyData = new VerificationData() {
            public org.mockito.internal.invocation.InvocationMatcher getWanted() {
                return null;
            }
            public java.util.List<org.mockito.invocation.Invocation> getAllInvocations() {
                return null;
            }
        };
    }

    // Tests getter methods for pollingPeriod, duration, and delegate
    @Test
    public void testGetters_returnsConfiguredValues() {
        VerificationMode delegate = new DummyVerificationMode();
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 50L, delegate, true);

        assertEquals(10L, overTime.getPollingPeriod());
        assertEquals(50L, overTime.getDuration());
        assertSame(delegate, overTime.getDelegate());
    }

    // Tests immediate return when delegate succeeds and returnOnSuccess is true
    @Test
    public void testVerify_successImmediate_returnOnSuccessTrue() {
        CountingVerificationMode delegate = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 100L, delegate, true);

        overTime.verify(dummyData);

        assertEquals(1, delegate.callCount);
    }

    // Tests polling until timer expires when delegate succeeds and returnOnSuccess is false
    @Test
    public void testVerify_success_returnOnSuccessFalse() {
        CountingVerificationMode delegate = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 20L, delegate, false);

        overTime.verify(dummyData);

        assertTrue(delegate.callCount >= 1);
    }

    // Tests recovery when delegate initially fails then succeeds (returnOnSuccess = true)
    @Test
    public void testVerify_delegateFailsThenSucceeds_returnOnSuccessTrue() {
        CountingVerificationMode delegate = new CountingVerificationMode(2);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 100L, delegate, true);

        overTime.verify(dummyData);

        assertEquals(3, delegate.callCount);
    }

    // Tests exception thrown after timeout when delegate always throws MockitoAssertionError
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_delegateAlwaysFails_throwsMockitoAssertionError() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new MockitoAssertionError("verification failed");
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 20L, delegate, true);

        overTime.verify(dummyData);
    }

    // Tests exception thrown after timeout when delegate always throws ArgumentsAreDifferent
    @Test(expected = ArgumentsAreDifferent.class)
    public void testVerify_delegateThrowsArgumentsAreDifferent_throwsArgumentsAreDifferent() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new ArgumentsAreDifferent("diff", "wanted", "actual");
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 20L, delegate, true);

        overTime.verify(dummyData);
    }

    // Tests immediate failure without retrying when delegate is AtMost mode
    @Test
    public void testVerify_delegateAtMostFails_throwsImmediatelyWithoutPolling() {
        final int[] callCount = new int[]{0};
        AtMost atMostDelegate = new AtMost(1) {
            @Override
            public void verify(VerificationData data) {
                callCount[0]++;
                throw new MockitoAssertionError("AtMost failed");
            }
        };

        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 100L, atMostDelegate, true);

        try {
            overTime.verify(dummyData);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertEquals(1, callCount[0]);
        }
    }

    // Tests immediate failure without retrying when delegate is NoMoreInteractions mode
    @Test
    public void testVerify_delegateNoMoreInteractionsFails_throwsImmediatelyWithoutPolling() {
        final int[] callCount = new int[]{0};
        NoMoreInteractions noMoreInteractionsDelegate = new NoMoreInteractions() {
            @Override
            public void verify(VerificationData data) {
                callCount[0]++;
                throw new MockitoAssertionError("NoMoreInteractions failed");
            }
        };

        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 100L, noMoreInteractionsDelegate, true);

        try {
            overTime.verify(dummyData);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertEquals(1, callCount[0]);
        }
    }

    // Tests canRecoverFromFailure method logic for different verification modes
    @Test
    public void testCanRecoverFromFailure_variousModes() {
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 50L, new DummyVerificationMode(), true);

        assertTrue(overTime.canRecoverFromFailure(new DummyVerificationMode()));
        assertFalse(overTime.canRecoverFromFailure(new AtMost(1)));
        assertFalse(overTime.canRecoverFromFailure(new NoMoreInteractions()));
    }

    // Tests verify method with custom Timer constructor
    @Test
    public void testVerify_customTimerExpired_throwsException() {
        Timer customTimer = new Timer(0L) {
            private int count = 0;
            @Override
            public boolean isCounting() {
                return count++ < 1;
            }
        };

        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new MockitoAssertionError("custom timer error");
            }
        };

        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(1L, 0L, delegate, true, customTimer);

        try {
            overTime.verify(dummyData);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertEquals("custom timer error", e.getMessage());
        }
    }

    // Tests handling of interrupted thread during sleep
    @Test
    public void testVerify_threadInterruptedDuringSleep_continuesAndThrowsError() {
        final int[] calls = new int[]{0};
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                calls[0]++;
                if (calls[0] == 1) {
                    Thread.currentThread().interrupt();
                }
                throw new MockitoAssertionError("still failing");
            }
        };

        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 20L, delegate, true);

        try {
            overTime.verify(dummyData);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(calls[0] >= 1);
        } finally {
            // Clean up interrupt status
            Thread.interrupted();
        }
    }

    // Tests copyWithVerificationMode creates a new instance with the updated delegate and preserved properties
    @Test
    public void testCopyWithVerificationMode() {
        VerificationMode originalDelegate = new DummyVerificationMode();
        VerificationOverTimeImpl original = new VerificationOverTimeImpl(10L, 50L, originalDelegate, true);
        VerificationMode newDelegate = new DummyVerificationMode();

        VerificationOverTimeImpl copy = original.copyWithVerificationMode(newDelegate);

        assertEquals(10L, copy.getPollingPeriod());
        assertEquals(50L, copy.getDuration());
        assertSame(newDelegate, copy.getDelegate());
        assertTrue(copy.isReturnOnSuccess());
        assertSame(original.getTimer(), copy.getTimer());
    }

    // Tests isReturnOnSuccess and getTimer getter methods
    @Test
    public void testGetTimer_and_isReturnOnSuccess() {
        Timer customTimer = new Timer(50L);
        VerificationMode delegate = new DummyVerificationMode();
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 50L, delegate, true, customTimer);

        assertSame(customTimer, overTime.getTimer());
        assertTrue(overTime.isReturnOnSuccess());

        VerificationOverTimeImpl overTimeFalse = new VerificationOverTimeImpl(10L, 50L, delegate, false);
        assertFalse(overTimeFalse.isReturnOnSuccess());
        assertNotNull(overTimeFalse.getTimer());
    }

    // Tests recovery and completion when returnOnSuccess is false and delegate fails then succeeds
    @Test
    public void testVerify_delegateFailsThenSucceeds_returnOnSuccessFalse() {
        CountingVerificationMode delegate = new CountingVerificationMode(1);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 30L, delegate, false);

        overTime.verify(dummyData);

        assertTrue(delegate.callCount > 1);
    }

    // Tests exception thrown when returnOnSuccess is false and delegate succeeds initially but fails on later attempt
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_delegateSucceedsThenFails_returnOnSuccessFalse_throwsAssertionError() {
        VerificationMode delegate = new VerificationMode() {
            int calls = 0;
            public void verify(VerificationData data) {
                calls++;
                if (calls > 1) {
                    throw new MockitoAssertionError("failed on later call");
                }
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(5L, 30L, delegate, false);

        overTime.verify(dummyData);
    }

    // Helper dummy verification mode
    private static class DummyVerificationMode implements VerificationMode {
        public void verify(VerificationData data) {
            // no-op
        }
    }

    // Helper verification mode that fails a given number of times before succeeding
    private static class CountingVerificationMode implements VerificationMode {
        int callCount = 0;
        final int failTimes;

        CountingVerificationMode(int failTimes) {
            this.failTimes = failTimes;
        }

        public void verify(VerificationData data) {
            callCount++;
            if (callCount <= failTimes) {
                throw new MockitoAssertionError("Fail attempt " + callCount);
            }
        }
    }
}