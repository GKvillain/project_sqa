package org.mockito.internal.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class TimerTest {

    @Test(expected = AssertionError.class)
    public void testIsCounting_beforeStart_throwsAssertionError() {
        Timer timer = new Timer(100);
        timer.isCounting();
    }

    @Test
    public void testIsCounting_afterStart_returnsTrue() {
        Timer timer = new Timer(500);
        timer.start();
        assertTrue(timer.isCounting());
    }

    @Test
    public void testIsCounting_afterDurationExpired_returnsFalse() throws InterruptedException {
        Timer timer = new Timer(50);
        timer.start();
        Thread.sleep(200);
        assertFalse(timer.isCounting());
    }

    @Test
    public void testIsCounting_exactDuration_returnsTrue() throws InterruptedException {
        Timer timer = new Timer(500);
        timer.start();
        Thread.sleep(400);
        assertTrue(timer.isCounting());
    }

    @Test
    public void testIsCounting_justOverDuration_returnsFalse() throws InterruptedException {
        Timer timer = new Timer(500);
        timer.start();
        Thread.sleep(600);
        assertFalse(timer.isCounting());
    }

    @Test
    public void testStart_resetsStartTime_restartsCounting() throws InterruptedException {
        Timer timer = new Timer(10);
        timer.start();
        Thread.sleep(100);
        assertFalse(timer.isCounting());
        timer.start();
        assertTrue(timer.isCounting());
    }

    @Test
    public void testConstructor_negativeDuration_isCountingReturnsFalse() {
        Timer timer = new Timer(-100);
        timer.start();
        assertFalse(timer.isCounting());
    }

    @Test
    public void testConstructor_zeroDuration_returnsTrueOnlyForInstant() throws InterruptedException {
        Timer timer = new Timer(0);
        timer.start();
        assertTrue(timer.isCounting());
        Thread.sleep(20);
        assertFalse(timer.isCounting());
    }

    @Test
    public void testIsCounting_multipleCalls_consistentWhileNotExpired() throws InterruptedException {
        Timer timer = new Timer(500);
        timer.start();
        for (int i = 0; i < 5; i++) {
            assertTrue(timer.isCounting());
            Thread.sleep(10);
        }
        Thread.sleep(200);
        assertFalse(timer.isCounting());
    }
}