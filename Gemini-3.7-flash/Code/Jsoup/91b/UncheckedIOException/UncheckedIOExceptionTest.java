package org.jsoup;

import org.junit.Test;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

import static org.junit.Assert.*;

public class UncheckedIOExceptionTest {

    // Tests constructor and ioException retrieval with standard IOException
    @Test
    public void testConstructor_standardIOException_storesCauseCorrectly() {
        IOException cause = new IOException("Disk read failure");
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertSame(cause, exception.getCause());
        assertSame(cause, exception.ioException());
        assertEquals("java.io.IOException: Disk read failure", exception.getMessage());
    }

    // Tests ioException retrieval with FileNotFoundException subclass
    @Test
    public void testIoException_fileNotFoundExceptionCause_returnsExactInstance() {
        FileNotFoundException cause = new FileNotFoundException("file.txt not found");
        UncheckedIOException exception = new UncheckedIOException(cause);

        IOException returned = exception.ioException();
        assertNotNull(returned);
        assertSame(cause, returned);
        assertTrue(returned instanceof FileNotFoundException);
        assertEquals("file.txt not found", returned.getMessage());
    }

    // Tests ioException retrieval with EOFException subclass
    @Test
    public void testIoException_eofExceptionCause_returnsExactInstance() {
        EOFException cause = new EOFException("Unexpected end of stream");
        UncheckedIOException exception = new UncheckedIOException(cause);

        IOException returned = exception.ioException();
        assertSame(cause, returned);
        assertTrue(returned instanceof EOFException);
    }

    // Tests ioException retrieval with SocketTimeoutException subclass
    @Test
    public void testIoException_socketTimeoutExceptionCause_returnsExactInstance() {
        SocketTimeoutException cause = new SocketTimeoutException("Connection timed out");
        UncheckedIOException exception = new UncheckedIOException(cause);

        IOException returned = exception.ioException();
        assertSame(cause, returned);
        assertEquals("Connection timed out", returned.getMessage());
    }

    // Tests ioException retrieval with InterruptedIOException subclass
    @Test
    public void testIoException_interruptedIOExceptionCause_returnsExactInstance() {
        InterruptedIOException cause = new InterruptedIOException("Operation interrupted");
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertSame(cause, exception.ioException());
    }

    // Tests null input cause handling
    @Test
    public void testConstructor_nullCause_returnsNullIOException() {
        UncheckedIOException exception = new UncheckedIOException((IOException) null);

        assertNull(exception.getCause());
        assertNull(exception.ioException());
    }

    // Tests hierarchy ensuring UncheckedIOException is an unchecked RuntimeException
    @Test
    public void testHierarchy_instanceCheck_isRuntimeException() {
        IOException cause = new IOException("Error");
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertTrue(exception instanceof RuntimeException);
        assertFalse(IOException.class.isAssignableFrom(UncheckedIOException.class));
    }

    // Tests IOException with empty message
    @Test
    public void testConstructor_emptyMessageIOException_preservesMessage() {
        IOException cause = new IOException("");
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertEquals("", cause.getMessage());
        assertSame(cause, exception.ioException());
    }

    // Tests IOException with null message
    @Test
    public void testConstructor_nullMessageIOException_preservesNullMessage() {
        IOException cause = new IOException((String) null);
        UncheckedIOException exception = new UncheckedIOException(cause);

        assertNull(cause.getMessage());
        assertSame(cause, exception.ioException());
    }

    // Tests that throwing and catching as RuntimeException preserves ioException
    @Test
    public void testThrowCatch_asRuntimeException_retainsIOException() {
        IOException cause = new IOException("Nested error");
        try {
            throw new UncheckedIOException(cause);
        } catch (RuntimeException e) {
            assertTrue(e instanceof UncheckedIOException);
            UncheckedIOException unchecked = (UncheckedIOException) e;
            assertSame(cause, unchecked.ioException());
            assertEquals("Nested error", unchecked.ioException().getMessage());
        }
    }

    // Tests constructor with String message creates wrapped IOException with the given message
    @Test
    public void testConstructor_stringMessage_createsWrappedIOException() {
        String msg = "Custom IO error occurred";
        UncheckedIOException exception = new UncheckedIOException(msg);

        assertNotNull(exception.getCause());
        assertNotNull(exception.ioException());
        assertEquals(msg, exception.ioException().getMessage());
    }

    // Tests constructor with null String message creates wrapped IOException with null message
    @Test
    public void testConstructor_nullStringMessage_createsWrappedIOExceptionWithNullMessage() {
        UncheckedIOException exception = new UncheckedIOException((String) null);

        assertNotNull(exception.getCause());
        assertNotNull(exception.ioException());
        assertNull(exception.ioException().getMessage());
    }

    // Tests constructor with empty String message creates wrapped IOException with empty message
    @Test
    public void testConstructor_emptyStringMessage_createsWrappedIOExceptionWithEmptyMessage() {
        UncheckedIOException exception = new UncheckedIOException("");

        assertNotNull(exception.getCause());
        assertNotNull(exception.ioException());
        assertEquals("", exception.ioException().getMessage());
    }
}