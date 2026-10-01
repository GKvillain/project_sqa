package org.jsoup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class UncheckedIOExceptionTest {

    // Tests that UncheckedIOException is a RuntimeException and stores the IOException cause.
    @Test
    public void testConstructor_withIOExceptionCause_isUncheckedAndPreservesCause() {
        IOException cause = new IOException("boom");

        UncheckedIOException ex = new UncheckedIOException(cause);

        assertTrue(ex instanceof RuntimeException);
        assertSame(cause, ex.getCause());
    }

    // Tests that ioException() returns the exact IOException instance supplied.
    @Test
    public void testIoException_withIOExceptionCause_returnsSameInstance() {
        IOException cause = new IOException("connection failed");

        UncheckedIOException ex = new UncheckedIOException(cause);

        assertSame(cause, ex.ioException());
    }

    // Tests that the message is initialized from the wrapped IOException by the constructor.
    @Test
    public void testConstructor_withIOExceptionCause_getMessageDescribesCause() {
        IOException cause = new IOException("boom");

        UncheckedIOException ex = new UncheckedIOException(cause);

        assertEquals(cause.toString(), ex.getMessage());
    }

    // Tests null cause path: the constructor succeeds and no cause or message is stored.
    @Test
    public void testConstructor_withNullCause_hasNoCauseAndIoExceptionIsNull() {
        UncheckedIOException ex = new UncheckedIOException(null);

        assertNull(ex.getCause());
        assertNull(ex.getMessage());
        assertNull(ex.ioException());
    }

    // Tests that an IOException subclass is preserved by ioException().
    @Test
    public void testIoException_withIOExceptionSubclassCause_returnsSameSubclassInstance() {
        FileNotFoundException cause = new FileNotFoundException("not found");

        UncheckedIOException ex = new UncheckedIOException(cause);

        assertSame(cause, ex.ioException());
    }

    // New tests for uncovered parts (toString override)

    // Tests that toString returns the wrapped IOException's toString when the cause is present.
    @Test
    public void testToString_withIOExceptionCause_returnsCauseToString() {
        IOException cause = new IOException("boom");

        UncheckedIOException ex = new UncheckedIOException(cause);

        assertEquals(cause.toString(), ex.toString());
    }

    // Tests that toString returns a default representation when the cause is null.
    @Test
    public void testToString_withNullCause_returnsDefaultToString() {
        UncheckedIOException ex = new UncheckedIOException(null);

        String str = ex.toString();
        assertNotNull(str);
        assertTrue(str.contains("UncheckedIOException"));
    }
}