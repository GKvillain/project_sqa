package org.jsoup;

import org.junit.Test;
import java.io.IOException;
import java.io.FileNotFoundException;
import static org.junit.Assert.*;

public class UncheckedIOExceptionTest {

    // Tests normal case where IOException is provided
    @Test
    public void testConstructor_withIOException_storesCauseCorrectly() {
        IOException cause = new IOException("Test error message");
        UncheckedIOException exception = new UncheckedIOException(cause);
        assertEquals("Test error message", exception.getCause().getMessage());
    }

    // Tests ioException method returns exact IOException instance
    @Test
    public void testIoException_withIOException_returnsSameInstance() {
        IOException cause = new IOException("Custom IO failure");
        UncheckedIOException exception = new UncheckedIOException(cause);
        assertSame(cause, exception.ioException());
    }

    // Tests null input edge case
    @Test
    public void testIoException_nullCause_returnsNull() {
        UncheckedIOException exception = new UncheckedIOException((IOException) null);
        assertNull(exception.ioException());
        assertNull(exception.getCause());
    }

    // Tests IOException subclass handling
    @Test
    public void testIoException_subclassOfIOException_returnsSameSubclass() {
        FileNotFoundException fnfException = new FileNotFoundException("file not found");
        UncheckedIOException exception = new UncheckedIOException(fnfException);
        assertTrue(exception.ioException() instanceof FileNotFoundException);
        assertSame(fnfException, exception.ioException());
    }

    // Tests that UncheckedIOException is an instance of RuntimeException
    @Test
    public void testInheritance_instanceOfRuntimeException_isTrue() {
        IOException cause = new IOException();
        UncheckedIOException exception = new UncheckedIOException(cause);
        assertTrue(exception instanceof RuntimeException);
    }

    // Tests string message constructor
    @Test
    public void testConstructor_withStringMessage_createsIOExceptionCause() {
        String errorMessage = "Custom error string";
        UncheckedIOException exception = new UncheckedIOException(errorMessage);
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
        assertEquals(errorMessage, exception.getCause().getMessage());
        assertEquals(errorMessage, exception.ioException().getMessage());
    }
}