package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

public class StopWatchTest {

    // Tests simple start and stop behavior
    @Test
    public void testStopWatch_simpleTiming_measuresTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        assertEquals(0, watch.getTime());
        
        watch.start();
        Thread.sleep(50);
        assertTrue(watch.getTime() >= 40);
        
        watch.stop();
        long time = watch.getTime();
        Thread.sleep(20);
        assertEquals(time, watch.getTime());
        assertTrue(watch.toString().length() > 0);
    }

    // Tests reset functionality allowing restart
    @Test
    public void testReset_afterStop_allowsRestart() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(30);
        watch.stop();
        
        watch.reset();
        assertEquals(0, watch.getTime());
        
        watch.start();
        Thread.sleep(30);
        watch.stop();
        assertTrue(watch.getTime() >= 20);
    }

    // Tests split and unsplit functionality
    @Test
    public void testSplitAndUnsplit_runningWatch_recordsSplitTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);
        watch.split();
        
        long splitTime = watch.getSplitTime();
        String splitStr = watch.toSplitString();
        assertNotNull(splitStr);
        
        Thread.sleep(50);
        assertEquals(splitTime, watch.getSplitTime());
        assertTrue(watch.getTime() > splitTime);
        
        watch.unsplit();
        Thread.sleep(20);
        watch.stop();
        assertTrue(watch.getTime() > splitTime);
    }

    // Tests suspend and resume functionality
    @Test
    public void testSuspendAndResume_pausesTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);
        watch.suspend();
        
        long suspendTime = watch.getTime();
        Thread.sleep(50);
        assertEquals(suspendTime, watch.getTime());
        
        watch.resume();
        Thread.sleep(50);
        watch.stop();
        
        long totalTime = watch.getTime();
        assertTrue(totalTime < 140);
        assertTrue(totalTime >= 90);
    }

    // Tests stopping a suspended watch (Defects4J Lang-55 regression)
    @Test
    public void testStop_whileSuspended_doesNotCountSuspendedTime() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);
        watch.suspend();
        
        long suspendTime = watch.getTime();
        Thread.sleep(50);
        watch.stop();
        
        long totalTime = watch.getTime();
        assertEquals(suspendTime, totalTime);
    }

    // Tests starting an already started watch throws exception
    @Test(expected = IllegalStateException.class)
    public void testStart_alreadyStarted_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.start();
    }

    // Tests restarting after stop without reset throws exception
    @Test(expected = IllegalStateException.class)
    public void testStart_afterStopWithoutReset_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.start();
    }

    // Tests stopping unstarted watch throws exception
    @Test(expected = IllegalStateException.class)
    public void testStop_unstarted_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.stop();
    }

    // Tests split when watch is not running throws exception
    @Test(expected = IllegalStateException.class)
    public void testSplit_unstarted_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.split();
    }

    // Tests unsplit when watch has not been split throws exception
    @Test(expected = IllegalStateException.class)
    public void testUnsplit_notSplit_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.unsplit();
    }

    // Tests suspend when watch is not running throws exception
    @Test(expected = IllegalStateException.class)
    public void testSuspend_unstarted_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.suspend();
    }

    // Tests resume when watch is not suspended throws exception
    @Test(expected = IllegalStateException.class)
    public void testResume_notSuspended_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.resume();
    }

    // Tests getSplitTime when not split throws exception
    @Test(expected = IllegalStateException.class)
    public void testGetSplitTime_notSplit_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.getSplitTime();
    }

    // Tests toSplitString when not split throws exception
    @Test(expected = IllegalStateException.class)
    public void testToSplitString_notSplit_throwsIllegalStateException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.toSplitString();
    }
}