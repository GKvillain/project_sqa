package org.apache.commons.lang.time;

import static org.junit.Assert.*;
import org.junit.Test;

public class StopWatchTest {

    private static final long TOLERANCE = 30L; // ms tolerance for timing tests

    @Test
    public void testGetTime_unstarted_returnsZero() {
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
    }

    @Test
    public void testStart_stop_returnsPositiveTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);
        watch.stop();
        assertTrue("Time should be positive", watch.getTime() > 0);
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_twice_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStop_withoutStart_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testStop_alreadyStopped_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspend_withoutStart_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspend_whenSuspended_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testResume_withoutSuspend_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void testResume_whenRunning_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void testSplit_withoutStart_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.split();
    }

    // Defect detection: split twice should throw but does not (defect)
    @Test(expected = IllegalStateException.class)
    public void testSplit_twice_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.split();
        watch.split();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnsplit_withoutSplit_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.unsplit();
    }

    @Test
    public void testStart_stop_reset_start_works() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(30);
        watch.stop();
        watch.reset();
        assertEquals(0, watch.getTime());
        watch.start();
        Thread.sleep(30);
        watch.stop();
        assertTrue("Time after reset and restart should be positive", watch.getTime() > 0);
    }

    // Defect detection: stop after suspend should not include suspended time
    @Test
    public void testStart_suspend_stop_shouldNotIncludeSuspendedTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(70); // run for ~70ms
        watch.suspend();
        long timeAtSuspend = watch.getTime();
        Thread.sleep(200); // suspend for 200ms
        watch.stop(); // defect: this sets stopTime to current time
        long timeAfterStop = watch.getTime();
        assertTrue("Time after stop should not include suspended time",
                   timeAfterStop <= timeAtSuspend + TOLERANCE);
    }

    @Test
    public void testStart_split_unsplit_stop_works() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(40);
        watch.split();
        long splitTime = watch.getSplitTime();
        assertTrue("Split time should be positive", splitTime > 0);
        Thread.sleep(20);
        watch.unsplit();
        Thread.sleep(30);
        watch.stop();
        assertTrue("Total time should be greater than split time", watch.getTime() > splitTime);
    }

    @Test
    public void testStart_split_getSplitTime_returnsPositive() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);
        watch.split();
        assertTrue("Split time should be >= 20", watch.getSplitTime() >= 20);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSplitTime_withoutSplit_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.getSplitTime();
    }

    @Test
    public void testStart_suspend_resume_stop_returnsCorrectTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(70); // first run
        watch.suspend();
        Thread.sleep(200); // suspended
        watch.resume();
        Thread.sleep(70); // second run
        watch.stop();
        long totalTime = watch.getTime();
        // Expected: ~140ms (70+70), not including suspended 200ms
        assertTrue("Total time should be approximately 140ms, but was " + totalTime,
                   totalTime >= 100 && totalTime <= 180);
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_afterStop_withoutReset_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.start();
    }

    @Test
    public void testReset_clearsState() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.reset();
        assertEquals(0, watch.getTime());
        // Should be able to start again without exception
        watch.start();
    }
}