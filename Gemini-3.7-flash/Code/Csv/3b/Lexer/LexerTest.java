package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class LexerTest {

    private static class TestLexer extends Lexer {
        TestLexer(final CSVFormat format, final ExtendedBufferedReader in) {
            super(format, in);
        }

        @Override
        Token nextToken(final Token reusableToken) throws IOException {
            return null;
        }
    }

    private TestLexer createLexer(final String input, final CSVFormat format) {
        return new TestLexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    // Tests reading escaped 'r' character mapping to CR
    @Test
    public void testReadEscape_escapedR_returnsCR() throws IOException {
        final TestLexer lexer = createLexer("r", CSVFormat.DEFAULT);
        assertEquals(Constants.CR, lexer.readEscape());
    }

    // Tests reading escaped 'n' character mapping to LF
    @Test
    public void testReadEscape_escapedN_returnsLF() throws IOException {
        final TestLexer lexer = createLexer("n", CSVFormat.DEFAULT);
        assertEquals(Constants.LF, lexer.readEscape());
    }

    // Tests reading escaped 't' character mapping to TAB
    @Test
    public void testReadEscape_escapedT_returnsTAB() throws IOException {
        final TestLexer lexer = createLexer("t", CSVFormat.DEFAULT);
        assertEquals(Constants.TAB, lexer.readEscape());
    }

    // Tests reading escaped 'b' character mapping to BACKSPACE
    @Test
    public void testReadEscape_escapedB_returnsBackspace() throws IOException {
        final TestLexer lexer = createLexer("b", CSVFormat.DEFAULT);
        assertEquals(Constants.BACKSPACE, lexer.readEscape());
    }

    // Tests reading escaped 'f' character mapping to FF
    @Test
    public void testReadEscape_escapedF_returnsFF() throws IOException {
        final TestLexer lexer = createLexer("f", CSVFormat.DEFAULT);
        assertEquals(Constants.FF, lexer.readEscape());
    }

    // Tests reading literal escaped character
    @Test
    public void testReadEscape_literalCharacter_returnsSameChar() throws IOException {
        final TestLexer lexer = createLexer("a", CSVFormat.DEFAULT);
        assertEquals('a', lexer.readEscape());
    }

    // Tests reading escape at end of stream throws IOException
    @Test(expected = IOException.class)
    public void testReadEscape_endOfStream_throwsIOException() throws IOException {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        lexer.readEscape();
    }

    // Tests readEndOfLine with CR followed by LF consumes both
    @Test
    public void testReadEndOfLine_crLfSequence_returnsTrue() throws IOException {
        final TestLexer lexer = createLexer("\n", CSVFormat.DEFAULT);
        assertTrue(lexer.readEndOfLine(Constants.CR));
    }

    // Tests readEndOfLine with standalone LF
    @Test
    public void testReadEndOfLine_singleLF_returnsTrue() throws IOException {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.readEndOfLine(Constants.LF));
    }

    // Tests readEndOfLine with standalone CR not followed by LF
    @Test
    public void testReadEndOfLine_singleCR_returnsTrue() throws IOException {
        final TestLexer lexer = createLexer("a", CSVFormat.DEFAULT);
        assertTrue(lexer.readEndOfLine(Constants.CR));
    }

    // Tests readEndOfLine with non-EOL character
    @Test
    public void testReadEndOfLine_nonEolChar_returnsFalse() throws IOException {
        final TestLexer lexer = createLexer("b", CSVFormat.DEFAULT);
        assertFalse(lexer.readEndOfLine('a'));
    }

    // Tests trimTrailingSpaces removes whitespace at the end
    @Test
    public void testTrimTrailingSpaces_trailingWhitespace_trimsSpaces() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("hello   \t ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("hello", sb.toString());
    }

    // Tests trimTrailingSpaces with empty buffer
    @Test
    public void testTrimTrailingSpaces_emptyBuffer_remainsEmpty() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("");
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());
    }

    // Tests isWhitespace returns true for space and false for delimiter
    @Test
    public void testIsWhitespace_whitespaceAndDelimiter_returnsExpected() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.isWhitespace(' '));
        assertTrue(lexer.isWhitespace('\t'));
        assertFalse(lexer.isWhitespace(','));
        assertFalse(lexer.isWhitespace('a'));
    }

    // Tests isStartOfLine for start-of-file, CR, LF, and normal chars
    @Test
    public void testIsStartOfLine_variousCharacters_returnsExpected() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.isStartOfLine(Constants.UNDEFINED));
        assertTrue(lexer.isStartOfLine(Constants.CR));
        assertTrue(lexer.isStartOfLine(Constants.LF));
        assertFalse(lexer.isStartOfLine('x'));
    }

    // Tests isEndOfFile check
    @Test
    public void testIsEndOfFile_eofChar_returnsTrue() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.isEndOfFile(Constants.END_OF_STREAM));
        assertFalse(lexer.isEndOfFile('a'));
    }

    // Tests delimiter recognition
    @Test
    public void testIsDelimiter_delimiterChar_returnsTrue() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.isDelimiter(','));
        assertFalse(lexer.isDelimiter(';'));
    }

    // Tests escape character recognition when enabled and disabled
    @Test
    public void testIsEscape_escapeConfigured_returnsExpected() {
        final CSVFormat formatWithEscape = CSVFormat.DEFAULT.withEscape('\\');
        final TestLexer lexerWithEscape = createLexer("", formatWithEscape);
        assertTrue(lexerWithEscape.isEscape('\\'));
        assertFalse(lexerWithEscape.isEscape('/'));

        final TestLexer lexerWithoutEscape = createLexer("", CSVFormat.DEFAULT);
        assertFalse(lexerWithoutEscape.isEscape('\\'));
    }

    // Tests quote character recognition
    @Test
    public void testIsQuoteChar_quoteConfigured_returnsExpected() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.isQuoteChar('"'));
        assertFalse(lexer.isQuoteChar('\''));
    }

    // Tests comment character recognition when enabled and disabled
    @Test
    public void testIsCommentStart_commentConfigured_returnsExpected() {
        final CSVFormat formatWithComment = CSVFormat.DEFAULT.withCommentMarker('#');
        final TestLexer lexerWithComment = createLexer("", formatWithComment);
        assertTrue(lexerWithComment.isCommentStart('#'));
        assertFalse(lexerWithComment.isCommentStart('!'));

        final TestLexer lexerWithoutComment = createLexer("", CSVFormat.DEFAULT);
        assertFalse(lexerWithoutComment.isCommentStart('#'));
    }

    // Tests getCurrentLineNumber delegates to ExtendedBufferedReader
    @Test
    public void testGetCurrentLineNumber_initialState_returnsZero() {
        final TestLexer lexer = createLexer("test", CSVFormat.DEFAULT);
        assertEquals(0L, lexer.getCurrentLineNumber());
    }

    // Additional tests for complete coverage

    @Test
    public void testIsQuoteChar_disabledQuote_returnsFalse() {
        final CSVFormat formatWithoutQuote = CSVFormat.DEFAULT.withQuote((Character) null);
        final TestLexer lexer = createLexer("", formatWithoutQuote);
        assertFalse(lexer.isQuoteChar('"'));
    }

    @Test
    public void testIsWhitespace_delimiterIsWhitespace_returnsFalse() {
        final CSVFormat formatSpaceDelimiter = CSVFormat.DEFAULT.withDelimiter(' ');
        final TestLexer lexer = createLexer("", formatSpaceDelimiter);
        assertFalse(lexer.isWhitespace(' '));
    }

    @Test
    public void testTrimTrailingSpaces_allWhitespace_trimsToEmpty() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("    \t \t ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());
    }

    @Test
    public void testTrimTrailingSpaces_spacesInMiddle_preservesMiddleSpaces() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder(" a  b ");
        lexer.trimTrailingSpaces(sb);
        assertEquals(" a  b", sb.toString());
    }

    @Test
    public void testReadEscape_escapedCR_returnsCR() throws IOException {
        final TestLexer lexer = createLexer("\r", CSVFormat.DEFAULT);
        assertEquals(Constants.CR, lexer.readEscape());
    }

    @Test
    public void testReadEscape_escapedLF_returnsLF() throws IOException {
        final TestLexer lexer = createLexer("\n", CSVFormat.DEFAULT);
        assertEquals(Constants.LF, lexer.readEscape());
    }

    @Test
    public void testReadEndOfLine_crAtEndOfStream_returnsTrue() throws IOException {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(lexer.readEndOfLine(Constants.CR));
    }
}