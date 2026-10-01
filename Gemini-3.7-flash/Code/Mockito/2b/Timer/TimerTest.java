package org.mockito.internal.util;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerTest {

    // Tests that a newly started timer with positive duration is counting
    @Test
    public void testIsCounting_afterStartWithinDuration_returnsTrue() {
        Timer timer = new Timer(1000L);
        timer.start();
        assertTrue(timer.isCounting());
    }

    // Tests that timer stops counting after duration has elapsed
    @Test
    public void testIsCounting_afterDurationElapsed_returnsFalse() throws InterruptedException {
        Timer timer = new Timer(10L);
        timer.start();
        Thread.sleep(30L);
        assertFalse(timer.isCounting());
    }

    // Tests timer with zero duration after elapsed time
    @Test
    public void testIsCounting_zeroDurationAfterSmallDelay_returnsFalse() throws InterruptedException {
        Timer timer = new Timer(0L);
        timer.start();
        Thread.sleep(5L);
        assertFalse(timer.isCounting());
    }

    // Tests timer with a large duration remains counting
    @Test
    public void testIsCounting_largeDuration_returnsTrue() {
        Timer timer = new Timer(100000L);
        timer.start();
        assertTrue(timer.isCounting());
    }

    // Tests restarting the timer resets the countdown
    @Test
    public void testStart_restartTime_resetsTimer() throws InterruptedException {
        Timer timer = new Timer(20L);
        timer.start();
        Thread.sleep(30L);
        assertFalse(timer.isCounting());

        timer.start();
        assertTrue(timer.isCounting());
    }

    // Tests negative duration throws MockitoException
    @Test(expected = MockitoException.class)
    public void testTimer_negativeDuration_throwsException() {
        new Timer(-1L);
    }

    // Tests large negative duration throws MockitoException
    @Test(expected = MockitoException.class)
    public void testTimer_largeNegativeDuration_throwsException() {
        new Timer(-1000L);
    }

    // Tests that checking isCounting before calling start throws MockitoException
    @Test(expected = MockitoException.class)
    public void testIsCounting_withoutStarting_throwsException() {
        Timer timer = new Timer(1000L);
        timer.isCounting();
    }
}