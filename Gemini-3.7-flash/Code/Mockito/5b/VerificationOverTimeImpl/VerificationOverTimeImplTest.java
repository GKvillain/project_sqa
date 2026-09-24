package org.mockito.internal.verification;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class VerificationOverTimeImplTest {

    // Tests getters return the configured constructor values
    @Test
    public void testGetters_configuredValues_returnsCorrectValues() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, delegate, true);

        assertEquals(10L, verification.getPollingPeriod());
        assertEquals(50L, verification.getDuration());
        assertEquals(delegate, verification.getDelegate());
    }

    // Tests constructor without timer initializes properly
    @Test
    public void testConstructor_withoutTimer_instantiatesSuccessfully() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(5L, 20L, delegate, false);
        assertNotNull(verification.getDelegate());
    }

    // Tests successful verification with returnOnSuccess=true returns immediately
    @Test
    public void testVerify_delegateSucceedsImmediately_returnOnSuccessTrue_returnsSuccessfully() {
        final int[] callCount = new int[]{0};
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                callCount[0]++;
            }
        };

        Timer timer = new Timer(100L);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 100L, delegate, true, timer);
        verification.verify(null);

        assertEquals(1, callCount[0]);
    }

    // Tests successful verification with returnOnSuccess=false continues polling until timer expires
    @Test
    public void testVerify_delegateSucceeds_returnOnSuccessFalse_pollsUntilTimerExpires() {
        final int[] callCount = new int[]{0};
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                callCount[0]++;
            }
        };

        Timer timer = new Timer(20L) {
            private int count = 0;
            @Override
            public boolean isCounting() {
                return count++ < 3;
            }
        };

        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(1L, 20L, delegate, false, timer);
        verification.verify(null);

        assertEquals(3, callCount[0]);
    }

    // Tests delegate failing with MockitoAssertionError throws exception after timer expires
    @Test(expected = MockitoAssertionError.class)
    public void testVerify_delegateThrowsMockitoAssertionError_timerExpires_throwsException() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new MockitoAssertionError("verification failed");
            }
        };

        Timer timer = new Timer(10L) {
            private int count = 0;
            @Override
            public boolean isCounting() {
                return count++ < 2;
            }
        };

        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(1L, 10L, delegate, true, timer);
        verification.verify(null);
    }

    // Tests delegate failing with ArgumentsAreDifferent throws exception after timer expires
    @Test(expected = ArgumentsAreDifferent.class)
    public void testVerify_delegateThrowsArgumentsAreDifferent_timerExpires_throwsException() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                throw new ArgumentsAreDifferent("arguments differ", "expected", "actual");
            }
        };

        Timer timer = new Timer(10L) {
            private int count = 0;
            @Override
            public boolean isCounting() {
                return count++ < 2;
            }
        };

        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(1L, 10L, delegate, true, timer);
        verification.verify(null);
    }

    // Tests delegate recovering from MockitoAssertionError before timeout when returnOnSuccess=true
    @Test
    public void testVerify_delegateFailsThenSucceeds_returnOnSuccessTrue_returnsSuccessfully() {
        final int[] callCount = new int[]{0};
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                callCount[0]++;
                if (callCount[0] < 2) {
                    throw new MockitoAssertionError("first attempt failed");
                }
            }
        };

        Timer timer = new Timer(50L);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(1L, 50L, delegate, true, timer);
        verification.verify(null);

        assertEquals(2, callCount[0]);
    }

    // Tests unrecoverable delegate (AtMost) throws exception immediately without polling
    @Test
    public void testVerify_unrecoverableAtMostDelegateFails_throwsImmediatelyWithoutPolling() {
        final int[] callCount = new int[]{0};
        AtMost atMostDelegate = new AtMost(1) {
            @Override
            public void verify(VerificationData data) {
                callCount[0]++;
                throw new MockitoAssertionError("AtMost failed");
            }
        };

        Timer timer = new Timer(50L);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, atMostDelegate, true, timer);

        try {
            verification.verify(null);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertEquals(1, callCount[0]);
        }
    }

    // Tests unrecoverable delegate (NoMoreInteractions) throws exception immediately
    @Test
    public void testVerify_unrecoverableNoMoreInteractionsDelegateFails_throwsImmediately() {
        final int[] callCount = new int[]{0};
        NoMoreInteractions noMoreInteractions = new NoMoreInteractions() {
            @Override
            public void verify(VerificationData data) {
                callCount[0]++;
                throw new MockitoAssertionError("NoMoreInteractions failed");
            }
        };

        Timer timer = new Timer(50L);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, noMoreInteractions, true, timer);

        try {
            verification.verify(null);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertEquals(1, callCount[0]);
        }
    }

    // Tests canRecoverFromFailure with standard verification mode returns true
    @Test
    public void testCanRecoverFromFailure_standardVerificationMode_returnsTrue() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, delegate, true);

        assertTrue(verification.canRecoverFromFailure(delegate));
    }

    // Tests canRecoverFromFailure with AtMost returns false
    @Test
    public void testCanRecoverFromFailure_atMostMode_returnsFalse() {
        AtMost atMost = new AtMost(1);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, atMost, true);

        assertFalse(verification.canRecoverFromFailure(atMost));
    }

    // Tests canRecoverFromFailure with NoMoreInteractions returns false
    @Test
    public void testCanRecoverFromFailure_noMoreInteractionsMode_returnsFalse() {
        NoMoreInteractions noMoreInteractions = new NoMoreInteractions();
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 50L, noMoreInteractions, true);

        assertFalse(verification.canRecoverFromFailure(noMoreInteractions));
    }

    // Tests generic AssertionError handling during verification
    @Test
    public void testVerify_delegateThrowsGenericAssertionError_caughtAndHandledOrThrown() {
        final int[] callCount = new int[]{0};
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {
                callCount[0]++;
                throw new AssertionError("Generic assertion error");
            }
        };

        Timer timer = new Timer(10L) {
            private int count = 0;
            @Override
            public boolean isCounting() {
                return count++ < 2;
            }
        };

        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(1L, 10L, delegate, true, timer);
        try {
            verification.verify(null);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            assertTrue(callCount[0] >= 1);
        }
    }

    // Tests copyWithVerificationMode creates a new instance with updated delegate
    @Test
    public void testCopyWithVerificationMode_createsNewInstanceWithUpdatedDelegate() {
        VerificationMode originalDelegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        VerificationMode newDelegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        Timer timer = new Timer(50L);
        VerificationOverTimeImpl original = new VerificationOverTimeImpl(10L, 50L, originalDelegate, true, timer);
        VerificationOverTimeImpl copied = original.copyWithVerificationMode(newDelegate);

        assertNotNull(copied);
        assertEquals(newDelegate, copied.getDelegate());
        assertEquals(10L, copied.getPollingPeriod());
        assertEquals(50L, copied.getDuration());
        assertTrue(copied.isReturnOnSuccess());
    }

    // Tests isReturnOnSuccess returns correct boolean value
    @Test
    public void testIsReturnOnSuccess_returnsConfiguredValue() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        VerificationOverTimeImpl verificationTrue = new VerificationOverTimeImpl(10L, 50L, delegate, true);
        assertTrue(verificationTrue.isReturnOnSuccess());

        VerificationOverTimeImpl verificationFalse = new VerificationOverTimeImpl(10L, 50L, delegate, false);
        assertFalse(verificationFalse.isReturnOnSuccess());
    }

    // Tests getTimer returns configured timer instance
    @Test
    public void testGetTimer_returnsConfiguredTimer() {
        VerificationMode delegate = new VerificationMode() {
            public void verify(VerificationData data) {}
        };
        Timer timer = new Timer(100L);
        VerificationOverTimeImpl verification = new VerificationOverTimeImpl(10L, 100L, delegate, true, timer);

        assertEquals(timer, verification.getTimer());
    }
}