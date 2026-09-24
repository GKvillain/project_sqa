package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.mockito.exceptions.base.MockitoException;

public class TimerTest {

    // Tests that timer is counting immediately after start with a positive duration
    @Test
    public void testIsCounting_immediatelyAfterStart_returnsTrue() {
        Timer timer = new Timer(1000L);
        timer.start();
        assertTrue(timer.isCounting());
    }

    // Tests that timer stops counting after duration has elapsed
    @Test
    public void testIsCounting_afterDurationExpires_returnsFalse() throws InterruptedException {
        Timer timer = new Timer(10L);
        timer.start();
        Thread.sleep(30L);
        assertFalse(timer.isCounting());
    }

    // Tests that calling start resets the countdown
    @Test
    public void testStart_calledMultipleTimes_resetsTimer() throws InterruptedException {
        Timer timer = new Timer(50L);
        timer.start();
        Thread.sleep(60L);
        assertFalse(timer.isCounting());

        timer.start();
        assertTrue(timer.isCounting());
    }

    // Tests timer behavior with zero duration
    @Test
    public void testIsCounting_zeroDurationAfterSleep_returnsFalse() throws InterruptedException {
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

    // Tests that negative duration throws MockitoException
    @Test(expected = MockitoException.class)
    public void testTimer_negativeDuration_throwsException() {
        new Timer(-1L);
    }

    // Tests that calling isCounting before start throws MockitoException
    @Test(expected = MockitoException.class)
    public void testIsCounting_beforeStart_throwsException() {
        Timer timer = new Timer(1000L);
        timer.isCounting();
    }

    // Tests that duration returns the configured duration in milliseconds
    @Test
    public void testDuration_returnsConfiguredDuration() {
        Timer timer = new Timer(1000L);
        assertEquals(1000L, timer.duration());
    }
}