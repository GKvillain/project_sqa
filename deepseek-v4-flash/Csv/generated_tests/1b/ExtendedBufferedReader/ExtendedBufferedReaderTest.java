package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class ExtendedBufferedReaderTest {

    // Tests initial state: no char read yet -> readAgain returns UNDEFINED
    @Test
    public void testReadAgain_initialState_returnsUndefined() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(""));
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    // Tests reading a single char and lastChar update
    @Test
    public void testRead_singleChar_updatesLastChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        int ch = reader.read();
        assertEquals('a', ch);
        assertEquals('a', reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    // Tests that reading '\n' increments the line counter
    @Test
    public void testRead_newline_incrementsLineCounter() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\nb"));
        reader.read(); // 'a'
        reader.read(); // '\n'
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        reader.read(); // 'b'
        assertEquals(1, reader.getLineNumber());
    }

    // Tests read(char[],...) with length==0 returns 0 and does not change state
    @Test
    public void testReadArray_emptyLength_returnsZeroAndPreservesState() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 0);
        assertEquals(0, len);
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    // Tests a normal multi-char read: updates lastChar and line counter for LF
    @Test
    public void testReadArray_normalRead_updatesLastCharAndLineCounter() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\nb"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        assertEquals("a\nb", new String(buf, 0, len));
    }

    // Tests CRLF counts as a single line increment in read(char[],...)
    @Test
    public void testReadArray_crlf_lineCounterCountsOnce() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        char[] buf = new char[4];
        int len = reader.read(buf, 0, 4);
        assertEquals(4, len);
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
    }

    // Tests CR-only line terminator is counted once
    @Test
    public void testReadArray_crOnly_lineCounterIncrements() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
    }

    // Tests CRLF split across two read() calls: line counter must remain correct
    @Test
    public void testReadArray_splitCrlf_lineCounterCountsOnce() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb\r\nc"));
        char[] buf = new char[1];

        assertEquals(1, reader.read(buf, 0, 1)); // 'a'
        assertEquals(1, reader.read(buf, 0, 1)); // '\r' -> line 1
        assertEquals(1, reader.read(buf, 0, 1)); // '\n' -> should not increment
        assertEquals(1, reader.read(buf, 0, 1)); // 'b'
        assertEquals(1, reader.read(buf, 0, 1)); // '\r' -> line 2
        assertEquals(1, reader.read(buf, 0, 1)); // '\n' -> should not increment
        assertEquals(1, reader.read(buf, 0, 1)); // 'c'

        assertEquals(2, reader.getLineNumber());
        assertEquals('c', reader.readAgain());
    }

    // Tests reaching EOF: read char[] returns -1 and lastChar becomes END_OF_STREAM
    @Test
    public void testReadArray_atEOF_setsLastCharToEndOfStream() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(""));
        char[] buf = new char[1];
        int len = reader.read(buf, 0, 1);
        assertEquals(-1, len);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    // Tests read(char[],...) with a non-zero offset: lastChar should reflect the last read character
    @Test
    public void testReadArray_withOffsetAndLength_updatesLastCharFromLastReadPosition() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abcdef"));
        char[] buf = new char[6];
        int len = reader.read(buf, 1, 3); // reads 'abc' into buf[1], buf[2], buf[3]
        assertEquals(3, len);
        assertEquals('c', reader.readAgain());
        assertEquals(0, reader.getLineNumber());
        assertEquals('a', buf[1]);
        assertEquals('b', buf[2]);
        assertEquals('c', buf[3]);
    }

    // Tests readLine() on a normal line
    @Test
    public void testReadLine_normalLine_incrementsAndSetsLastChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("hello\nworld"));
        assertEquals("hello", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('o', reader.readAgain());

        assertEquals("world", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
    }

    // Tests readLine() on an empty line
    @Test
    public void testReadLine_emptyLine_incrementsLineCounter() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\nnext"));
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain()); // unchanged for empty line

        assertEquals("next", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('t', reader.readAgain());
    }

    // Tests readLine() at end-of-stream
    @Test
    public void testReadLine_atEOF_setsLastCharEndOfStream() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals("abc", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('c', reader.readAgain());

        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    // Tests readLine() with CR-only line terminators
    @Test
    public void testReadLine_crOnly_incrementsLineCounter() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb\rc"));
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());

        assertEquals("b", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('b', reader.readAgain());

        assertEquals("c", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        assertEquals('c', reader.readAgain());
    }

    // Tests lookAhead() does not consume the character or alter state
    @Test
    public void testLookAhead_returnsNextCharWithoutConsuming() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        int next = reader.lookAhead();
        assertEquals('a', next);
        assertEquals(0, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());

        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
    }

    // Tests lookAhead() at end-of-stream
    @Test
    public void testLookAhead_atEnd_returnsEndOfStream() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(""));
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals(0, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
    }
}