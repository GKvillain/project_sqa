package org.apache.commons.lang.text;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StrBuilderTest {

    private StrBuilder sb;

    @Before
    public void setUp() {
        sb = new StrBuilder();
    }

    // Tests default constructor and initial capacity/state
    @Test
    public void testConstructor_default_initializesCorrectly() {
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());
        assertEquals(32, sb.capacity());
        assertEquals("", sb.toString());
    }

    // Tests constructor with string input including null handling
    @Test
    public void testConstructor_stringInput_initializesCorrectly() {
        StrBuilder builderNull = new StrBuilder((String) null);
        assertEquals(0, builderNull.length());
        assertEquals(32, builderNull.capacity());

        StrBuilder builderStr = new StrBuilder("hello");
        assertEquals(5, builderStr.length());
        assertEquals("hello", builderStr.toString());
        assertEquals(37, builderStr.capacity());
    }

    // Tests appendFixedWidthPadRight when input length exceeds width (Defects4J Lang-59 target)
    @Test
    public void testAppendFixedWidthPadRight_strLongerThanWidth_truncatesRightSide() {
        sb = new StrBuilder(4);
        sb.appendFixedWidthPadRight("abcdef", 4, '-');
        assertEquals("abcd", sb.toString());
        assertEquals(4, sb.length());
    }

    // Tests appendFixedWidthPadRight when input length is shorter than width
    @Test
    public void testAppendFixedWidthPadRight_strShorterThanWidth_padsRight() {
        sb.appendFixedWidthPadRight("abc", 5, '-');
        assertEquals("abc--", sb.toString());
        assertEquals(5, sb.length());
    }

    // Tests appendFixedWidthPadRight with null object and custom null text
    @Test
    public void testAppendFixedWidthPadRight_nullObject_usesNullText() {
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 6, ' ');
        assertEquals("null  ", sb.toString());
    }

    // Tests appendFixedWidthPadLeft when input length exceeds and is less than width
    @Test
    public void testAppendFixedWidthPadLeft_variousInputs_padsOrTruncatesCorrectly() {
        sb.appendFixedWidthPadLeft("abcdef", 4, '-');
        assertEquals("cdef", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, '-');
        assertEquals("--abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 5, '0');
        assertEquals("00123", sb.toString());
    }

    // Tests append primitive types and chaining
    @Test
    public void testAppend_primitiveTypes_appendsExpectedStrings() {
        sb.append(true)
          .append(' ')
          .append(100)
          .append(' ')
          .append(1000L)
          .append(' ')
          .append(2.5f)
          .append(' ')
          .append(3.1415d);
        assertEquals("true 100 1000 2.5 3.1415", sb.toString());
    }

    // Tests append with separators for arrays and collections
    @Test
    public void testAppendWithSeparators_arrayAndCollection_appendsFormatted() {
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z"), "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.emptyList(), ",");
        assertEquals("", sb.toString());
    }

    // Tests insert operations at various positions
    @Test
    public void testInsert_variousPositionsAndTypes_insertsCorrectly() {
        sb.append("ac");
        sb.insert(1, "b");
        assertEquals("abc", sb.toString());

        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());

        sb.insert(sb.length(), '!');
        assertEquals("trueabc!", sb.toString());
    }

    // Tests insert with invalid index throws IndexOutOfBoundsException
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_invalidNegativeIndex_throwsException() {
        sb.insert(-1, "test");
    }

    // Tests delete and deleteCharAt methods
    @Test
    public void testDelete_rangesAndChars_deletesExpectedPortions() {
        sb.append("hello world");
        sb.delete(5, 11);
        assertEquals("hello", sb.toString());

        sb.deleteCharAt(0);
        assertEquals("ello", sb.toString());

        sb.append("llll");
        sb.deleteAll('l');
        assertEquals("eo", sb.toString());
    }

    // Tests replace operations with Strings and chars
    @Test
    public void testReplace_stringAndChar_replacesCorrectly() {
        sb.append("foo bar foo baz");
        sb.replaceAll("foo", "qux");
        assertEquals("qux bar qux baz", sb.toString());

        sb.replaceFirst("qux", "foo");
        assertEquals("foo bar qux baz", sb.toString());

        sb.replaceAll('a', 'o');
        assertEquals("foo bor qux boz", sb.toString());
    }

    // Tests substring and left/mid/rightString methods
    @Test
    public void testSubstrings_validAndEdgeCases_returnsExpectedSubstrings() {
        sb.append("quick brown fox");
        assertEquals("quick", sb.substring(0, 5));
        assertEquals("brown fox", sb.substring(6));
        assertEquals("quick", sb.leftString(5));
        assertEquals("fox", sb.rightString(3));
        assertEquals("brown", sb.midString(6, 5));

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.midString(100, 5));
    }

    // Tests search methods: contains, indexOf, lastIndexOf
    @Test
    public void testSearching_indexOfAndContains_findsExpectedIndices() {
        sb.append("the quick brown fox jumps over the lazy dog");
        assertTrue(sb.contains("fox"));
        assertFalse(sb.contains("cat"));
        assertTrue(sb.contains('q'));
        assertFalse(sb.contains('z') == false);

        assertEquals(4, sb.indexOf("quick"));
        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(0, sb.indexOf("the"));
        assertEquals(-1, sb.indexOf("missing"));
        assertEquals(-1, sb.indexOf((String) null));
    }

    // Tests reverse, trim, and setLength operations
    @Test
    public void testTransformations_reverseTrimAndSetLength_modifiesBuffer() {
        sb.append("  abc  ");
        sb.trim();
        assertEquals("abc", sb.toString());

        sb.reverse();
        assertEquals("cba", sb.toString());

        sb.setLength(2);
        assertEquals("cb", sb.toString());

        sb.setLength(4);
        assertEquals(4, sb.length());
        assertEquals('c', sb.charAt(0));
        assertEquals('b', sb.charAt(1));
        assertEquals('\0', sb.charAt(2));
        assertEquals('\0', sb.charAt(3));
    }

    // Tests Reader view from StrBuilder
    @Test
    public void testAsReader_readOperations_readsCharactersCorrectly() throws Exception {
        sb.append("hello reader");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertTrue(reader.markSupported());

        char[] chars = new char[5];
        int numRead = reader.read(chars, 0, 5);
        assertEquals(5, numRead);
        assertEquals("hello", new String(chars));

        assertEquals(' ', (char) reader.read());
        reader.close();
    }

    // Tests Writer view to StrBuilder
    @Test
    public void testAsWriter_writeOperations_populatesStrBuilder() throws Exception {
        Writer writer = sb.asWriter();
        writer.write("hello");
        writer.write(' ');
        writer.write("world!".toCharArray());
        writer.flush();
        writer.close();
        assertEquals("hello world!", sb.toString());
    }

    // Tests equals, equalsIgnoreCase, and hashCode contracts
    @Test
    public void testEqualsAndHashCode_variousBuilders_correctContract() {
        StrBuilder sb1 = new StrBuilder("Apache");
        StrBuilder sb2 = new StrBuilder("Apache");
        StrBuilder sb3 = new StrBuilder("apache");

        assertTrue(sb1.equals(sb2));
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertFalse(sb1.equals("Apache"));
    }

    // Additional tests for constructor with capacity and capacity adjustments
    @Test
    public void testCapacityAndEnsureMinimizeCapacity() {
        StrBuilder builder = new StrBuilder(0);
        assertEquals(32, builder.capacity());

        StrBuilder builderPos = new StrBuilder(64);
        assertEquals(64, builderPos.capacity());

        builderPos.append("test");
        builderPos.minimizeCapacity();
        assertEquals(4, builderPos.capacity());

        builderPos.ensureCapacity(100);
        assertTrue(builderPos.capacity() >= 100);
    }

    // Tests get/set newline and appendNewLine
    @Test
    public void testNewLineHandling() {
        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
        sb.append("line1").appendNewLine().append("line2");
        assertEquals("line1\r\nline2", sb.toString());

        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());
    }

    // Tests appendln overloads
    @Test
    public void testAppendln_variousTypes() {
        sb.setNewLineText("\n");
        sb.appendln("text")
          .appendln(new StringBuffer("buf"))
          .appendln(new StrBuilder("builder"))
          .appendln(new char[]{'a', 'b'})
          .appendln(new char[]{'a', 'b', 'c'}, 1, 2)
          .appendln(true)
          .appendln('x')
          .appendln(12)
          .appendln(34L)
          .appendln(5.6f)
          .appendln(7.8d);
        String expected = "text\nbuf\nbuilder\nab\nbc\ntrue\nx\n12\n34\n5.6\n7.8\n";
        assertEquals(expected, sb.toString());
    }

    // Tests append with substrings and CharSequence/StringBuffer/StrBuilder
    @Test
    public void testAppend_subsequencesAndBuffers() {
        sb.append((String) null, 0, 0);
        sb.append("abcdef", 1, 3);
        sb.append(new StringBuffer("ghijkl"), 1, 3);
        sb.append(new StrBuilder("mnopqr"), 1, 3);
        sb.append(new char[]{'s', 't', 'u', 'v'}, 1, 2);
        assertEquals("bcdehitu", sb.toString());

        sb.appendNull();
        assertEquals("bcdehitu", sb.toString());
        sb.setNullText("<null>");
        assertEquals("<null>", sb.getNullText());
        sb.appendNull();
        assertEquals("bcdehitu<null>", sb.toString());
    }

    // Tests appendAll with Array, Collection, and Iterator
    @Test
    public void testAppendAll_collectionsAndIterators() {
        sb.appendAll((Object[]) null);
        sb.appendAll(new String[]{"1", "2"});
        sb.appendAll((Iterable<?>) null);
        sb.appendAll(Arrays.asList("3", "4"));
        sb.appendAll((Iterator<?>) null);
        sb.appendAll(Arrays.asList("5", "6").iterator());
        assertEquals("123456", sb.toString());
    }

    // Tests appendSeparator variations
    @Test
    public void testAppendSeparator_variousOverloads() {
        sb.appendSeparator(",");
        assertEquals("", sb.toString());
        sb.append("A");
        sb.appendSeparator(",");
        sb.append("B");
        assertEquals("A,B", sb.toString());

        sb.appendSeparator(';');
        sb.append("C");
        assertEquals("A,BC", sb.toString().replace(";", ";"));
        assertEquals("A,B;C", sb.toString());

        sb.clear();
        sb.appendSeparator(",", "DEFAULT");
        assertEquals("DEFAULT", sb.toString());

        sb.clear();
        sb.appendSeparator(',', '|');
        assertEquals("|", sb.toString());

        sb.clear();
        sb.append("A");
        sb.appendSeparator(",", 1);
        sb.append("B");
        assertEquals("A,B", sb.toString());
    }

    // Tests appendPadding and appendFixedWidthPad overloads
    @Test
    public void testAppendPaddingAndFixedPrimitives() {
        sb.appendPadding(3, '*');
        assertEquals("***", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 5, '0');
        assertEquals("00042", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 5, '0');
        assertEquals("42000", sb.toString());
    }

    // Tests character arrays and char indexing
    @Test
    public void testCharConversionsAndIndexing() {
        sb.append("abcdef");
        assertEquals('c', sb.charAt(2));
        sb.setCharAt(2, 'Z');
        assertEquals("abZdef", sb.toString());

        char[] chars = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'Z', 'd', 'e', 'f'}, chars);

        char[] subChars = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'b', 'Z', 'd'}, subChars);

        char[] dst = new char[8];
        sb.getChars(0, 3, dst, 2);
        assertEquals("abZ", new String(dst, 2, 3));

        char[] fullDst = new char[6];
        sb.getChars(fullDst);
        assertEquals("abZdef", new String(fullDst));
    }

    // Tests startsWith and endsWith
    @Test
    public void testStartsWithAndEndsWith() {
        sb.append("hello world");
        assertTrue(sb.startsWith("hello"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
    }

    // Tests matcher-based operations: delete, replace, indexOf, lastIndexOf, contains
    @Test
    public void testStrMatcherOperations() {
        sb.append("a,b,c,d,e");
        StrMatcher commaMatcher = StrMatcher.charMatcher(',');

        assertTrue(sb.contains(commaMatcher));
        assertEquals(1, sb.indexOf(commaMatcher));
        assertEquals(7, sb.lastIndexOf(commaMatcher));

        sb.deleteFirst(commaMatcher);
        assertEquals("ab,c,d,e", sb.toString());

        sb.deleteAll(commaMatcher);
        assertEquals("abcde", sb.toString());

        sb.clear();
        sb.append("a1b2c3");
        sb.replaceAll(StrMatcher.charSetMatcher("123"), "_");
        assertEquals("a_b_c_", sb.toString());

        sb.replaceFirst(StrMatcher.charSetMatcher("_"), "-");
        assertEquals("a-b_c_", sb.toString());
    }

    // Tests subSequence and toStringBuffer
    @Test
    public void testSubSequenceAndToStringBuffer() {
        sb.append("Apache Commons");
        CharSequence cs = sb.subSequence(0, 6);
        assertEquals("Apache", cs.toString());

        StringBuffer buf = sb.toStringBuffer();
        assertEquals("Apache Commons", buf.toString());
    }

    // Tests asTokenizer
    @Test
    public void testAsTokenizer() {
        sb.append("word1 word2 word3");
        StrTokenizer tok = sb.asTokenizer();
        assertEquals("word1", tok.next());
        assertEquals("word2", tok.next());
        assertEquals("word3", tok.next());
        assertFalse(tok.hasNext());
    }

    // Tests edge cases and exceptions
    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAt_outOfBounds_throwsException() {
        sb.append("abc");
        sb.charAt(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetCharAt_outOfBounds_throwsException() {
        sb.append("abc");
        sb.setCharAt(-1, 'x');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetLength_negative_throwsException() {
        sb.setLength(-1);
    }

    @Test
    public void testDeleteFirst_charAndString() {
        sb.append("banana");
        sb.deleteFirst('a');
        assertEquals("bnana", sb.toString());

        sb.deleteFirst("an");
        assertEquals("bna", sb.toString());
    }

    @Test
    public void testReader_markResetSkip() throws Exception {
        sb.append("abcdefghij");
        Reader reader = sb.asReader();

        assertEquals('a', (char) reader.read());
        reader.mark(5);
        assertEquals('b', (char) reader.read());
        assertEquals('c', (char) reader.read());
        reader.reset();
        assertEquals('b', (char) reader.read());

        long skipped = reader.skip(2);
        assertEquals(2, skipped);
        assertEquals('e', (char) reader.read());
        reader.close();
    }
}