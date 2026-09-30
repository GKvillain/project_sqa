package org.apache.commons.lang.time;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StopWatchTest {

    private StopWatch watch;

    @Before
    public void setUp() {
        watch = new StopWatch();
    }

    // Tests initial state getTime returns zero
    @Test
    public void testGetTime_unstarted_returnsZero() {
        assertEquals(0, watch.getTime());
    }

    // Tests simple start and stop timing
    @Test
    public void testStartAndStop_normalOperation_returnsPositiveTime() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.stop();
        long time = watch.getTime();
        assertTrue("Time should be >= 40ms", time >= 40);
        assertTrue("Time should be <= 500ms", time <= 500);
    }

    // Tests getting time while running
    @Test
    public void testGetTime_whileRunning_returnsElapsed() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        long time = watch.getTime();
        assertTrue("Time should be >= 40ms", time >= 40);
    }

    // Tests resetting stopwatch after stop allows restart
    @Test
    public void testReset_afterStop_allowsRestart() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.stop();
        watch.reset();
        assertEquals(0, watch.getTime());
        watch.start();
        Thread.sleep(20);
        watch.stop();
        assertTrue(watch.getTime() >= 15);
    }

    // Tests split and unsplit behavior
    @Test
    public void testSplitAndUnsplit_normalOperation_returnsCorrectSplitTime() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.split();
        long splitTime = watch.getSplitTime();
        assertEquals(splitTime, watch.getTime());
        Thread.sleep(50);
        watch.unsplit();
        Thread.sleep(50);
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue("Total time should be greater than split time", totalTime > splitTime);
    }

    // Tests suspend and resume behavior
    @Test
    public void testSuspendAndResume_normalOperation_excludesSuspendedDuration() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.suspend();
        long suspendTime = watch.getTime();
        Thread.sleep(100);
        watch.resume();
        Thread.sleep(50);
        watch.stop();
        long totalTime = watch.getTime();
        assertTrue("Total time should exclude suspended interval", totalTime < suspendTime + 120);
        assertTrue("Total time should be >= 80ms", totalTime >= 80);
    }

    // Tests stopping while in suspended state does not include suspended time (Lang-55 defect)
    @Test
    public void testStop_afterSuspend_doesNotIncludeSuspendedTime() throws InterruptedException {
        watch.start();
        Thread.sleep(50);
        watch.suspend();
        long suspendTime = watch.getTime();
        Thread.sleep(100);
        watch.stop();
        long totalTime = watch.getTime();
        assertEquals(suspendTime, totalTime);
    }

    // Tests toString formatting
    @Test
    public void testToString_validTiming_returnsFormattedString() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.stop();
        assertNotNull(watch.toString());
        assertTrue(watch.toString().matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    // Tests toSplitString formatting
    @Test
    public void testToSplitString_validSplit_returnsFormattedString() throws InterruptedException {
        watch.start();
        Thread.sleep(20);
        watch.split();
        assertNotNull(watch.toSplitString());
        assertTrue(watch.toSplitString().matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    // Tests starting already running watch throws exception
    @Test(expected = IllegalStateException.class)
    public void testStart_alreadyStarted_throwsException() {
        watch.start();
        watch.start();
    }

    // Tests starting stopped watch without reset throws exception
    @Test(expected = IllegalStateException.class)
    public void testStart_stoppedWithoutReset_throwsException() {
        watch.start();
        watch.stop();
        watch.start();
    }

    // Tests stopping unstarted watch throws exception
    @Test(expected = IllegalStateException.class)
    public void testStop_unstarted_throwsException() {
        watch.stop();
    }

    // Tests split when unstarted throws exception
    @Test(expected = IllegalStateException.class)
    public void testSplit_unstarted_throwsException() {
        watch.split();
    }

    // Tests unsplit without split throws exception
    @Test(expected = IllegalStateException.class)
    public void testUnsplit_notSplit_throwsException() {
        watch.start();
        watch.unsplit();
    }

    // Tests getSplitTime without split throws exception
    @Test(expected = IllegalStateException.class)
    public void testGetSplitTime_notSplit_throwsException() {
        watch.start();
        watch.getSplitTime();
    }

    // Tests suspend when unstarted throws exception
    @Test(expected = IllegalStateException.class)
    public void testSuspend_unstarted_throwsException() {
        watch.suspend();
    }

    // Tests resume when not suspended throws exception
    @Test(expected = IllegalStateException.class)
    public void testResume_notSuspended_throwsException() {
        watch.start();
        watch.resume();
    }
}