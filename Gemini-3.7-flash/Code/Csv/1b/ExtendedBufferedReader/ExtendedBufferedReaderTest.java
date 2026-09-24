package org.apache.commons.csv;

import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;
import static org.junit.Assert.*;

public class ExtendedBufferedReaderTest {

    private ExtendedBufferedReader create(String input) {
        return new ExtendedBufferedReader(new StringReader(input));
    }

    // Tests initial state of readAgain and getLineNumber
    @Test
    public void testReadAgain_initialState_returnsUndefined() {
        ExtendedBufferedReader br = create("abc");
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        assertEquals(0, br.getLineNumber());
    }

    // Tests reading single characters sequentially and tracking line count on LF
    @Test
    public void testRead_singleCharacters_returnsCorrectCharsAndLineCount() throws IOException {
        ExtendedBufferedReader br = create("a\nb");
        assertEquals('a', br.read());
        assertEquals('a', br.readAgain());
        assertEquals(0, br.getLineNumber());

        assertEquals('\n', br.read());
        assertEquals('\n', br.readAgain());
        assertEquals(1, br.getLineNumber());

        assertEquals('b', br.read());
        assertEquals('b', br.readAgain());
        assertEquals(1, br.getLineNumber());
    }

    // Tests read() reaching end of stream
    @Test
    public void testRead_endOfStream_returnsEndOfStream() throws IOException {
        ExtendedBufferedReader br = create("");
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
    }

    // Tests lookAhead inspecting next character without advancing reader
    @Test
    public void testLookAhead_peeksNextChar_doesNotConsumeChar() throws IOException {
        ExtendedBufferedReader br = create("ab");
        assertEquals('a', br.lookAhead());
        assertEquals('a', br.lookAhead());
        assertEquals('a', br.read());
        assertEquals('b', br.lookAhead());
        assertEquals('b', br.read());
    }

    // Tests lookAhead at end of stream
    @Test
    public void testLookAhead_atEndOfStream_returnsEndOfStream() throws IOException {
        ExtendedBufferedReader br = create("");
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
    }

    // Tests read(char[], int, int) with length zero boundary
    @Test
    public void testReadCharArray_zeroLength_returnsZero() throws IOException {
        ExtendedBufferedReader br = create("abc");
        char[] buf = new char[5];
        int read = br.read(buf, 0, 0);
        assertEquals(0, read);
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
    }

    // Tests read(char[], int, int) with multiple LF newlines
    @Test
    public void testReadCharArray_multipleLinesWithLF_countsLinesCorrectly() throws IOException {
        ExtendedBufferedReader br = create("a\nb\n");
        char[] buf = new char[10];
        int read = br.read(buf, 0, 10);
        assertEquals(4, read);
        assertEquals('\n', br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    // Tests read(char[], int, int) with CRLF newlines without double-counting
    @Test
    public void testReadCharArray_multipleLinesWithCRLF_countsLinesCorrectly() throws IOException {
        ExtendedBufferedReader br = create("a\r\nb\r\n");
        char[] buf = new char[10];
        int read = br.read(buf, 0, 10);
        assertEquals(6, read);
        assertEquals('\n', br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    // Tests read(char[], int, int) with CR-only newlines
    @Test
    public void testReadCharArray_multipleLinesWithCR_countsLinesCorrectly() throws IOException {
        ExtendedBufferedReader br = create("a\rb\rc\r");
        char[] buf = new char[10];
        int read = br.read(buf, 0, 10);
        assertEquals(6, read);
        assertEquals('\r', br.readAgain());
        assertEquals(3, br.getLineNumber());
    }

    // Tests read(char[], int, int) when CR and LF are split across consecutive read calls
    @Test
    public void testReadCharArray_splitCRLFBetweenReads_countsLinesCorrectly() throws IOException {
        ExtendedBufferedReader br = create("a\r\nb");
        char[] buf1 = new char[2];
        int read1 = br.read(buf1, 0, 2);
        assertEquals(2, read1);
        assertEquals('\r', br.readAgain());
        assertEquals(1, br.getLineNumber());

        char[] buf2 = new char[2];
        int read2 = br.read(buf2, 0, 2);
        assertEquals(2, read2);
        assertEquals('b', br.readAgain());
        assertEquals(1, br.getLineNumber());
    }

    // Tests read(char[], int, int) reaching end of stream
    @Test
    public void testReadCharArray_endOfStream_returnsNegativeOneAndSetsEndOfStream() throws IOException {
        ExtendedBufferedReader br = create("");
        char[] buf = new char[5];
        int read = br.read(buf, 0, 5);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, read);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
    }

    // Tests readLine reading non-empty lines, updating lastChar and incrementing lineCounter
    @Test
    public void testReadLine_nonEmptyLines_incrementsLineCounterAndSetsLastChar() throws IOException {
        ExtendedBufferedReader br = create("first\r\nsecond");
        assertEquals("first", br.readLine());
        assertEquals('t', br.readAgain());
        assertEquals(1, br.getLineNumber());

        assertEquals("second", br.readLine());
        assertEquals('d', br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    // Tests readLine on an empty line without modifying lastChar
    @Test
    public void testReadLine_emptyLine_incrementsLineCounterWithoutUpdatingLastChar() throws IOException {
        ExtendedBufferedReader br = create("\nsecond");
        assertEquals("", br.readLine());
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        assertEquals(1, br.getLineNumber());

        assertEquals("second", br.readLine());
        assertEquals('d', br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    // Tests readLine at end of stream returning null and setting lastChar to END_OF_STREAM
    @Test
    public void testReadLine_atEndOfStream_returnsNullAndSetsEndOfStream() throws IOException {
        ExtendedBufferedReader br = create("");
        assertNull(br.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertEquals(0, br.getLineNumber());
    }
}