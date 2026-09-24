package org.apache.commons.lang.text;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrBuilderTest {

    // Tests Defects4J Lang-47: appendFixedWidthPadLeft with null object
    @Test
    public void testAppendFixedWidthPadLeft_nullObject_handlesNull() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 5, '-');
        assertEquals("-----", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 5, '-');
        assertEquals("-null", sb.toString());
    }

    // Tests Defects4J Lang-47: appendFixedWidthPadRight with null object
    @Test
    public void testAppendFixedWidthPadRight_nullObject_handlesNull() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 5, '-');
        assertEquals("-----", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 5, '-');
        assertEquals("null-", sb.toString());
    }

    // Tests appendFixedWidthPadLeft and right with normal values
    @Test
    public void testAppendFixedWidthPad_variousInputs_formatsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, ' ');
        assertEquals("  abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 4, ' ');
        assertEquals("cdef", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 5, '0');
        assertEquals("00123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, ' ');
        assertEquals("abc  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 4, ' ');
        assertEquals("abcd", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 5, '0');
        assertEquals("12300", sb.toString());
    }

    // Tests constructor variations and capacity management
    @Test
    public void testConstructorsAndCapacity_variousInputs_configuresCapacity() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(32, sb1.capacity());
        assertEquals(0, sb1.length());
        assertTrue(sb1.isEmpty());

        StrBuilder sb2 = new StrBuilder(10);
        assertEquals(10, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(-5);
        assertEquals(32, sb3.capacity());

        StrBuilder sb4 = new StrBuilder("Hello");
        assertEquals(5 + 32, sb4.capacity());
        assertEquals("Hello", sb4.toString());

        StrBuilder sbNull = new StrBuilder((String) null);
        assertEquals(32, sbNull.capacity());

        sb4.ensureCapacity(100);
        assertTrue(sb4.capacity() >= 100);
        sb4.minimizeCapacity();
        assertEquals(5, sb4.capacity());
    }

    // Tests length adjustments and clear
    @Test
    public void testSetLengthAndClear_variousLengths_modifiesSize() {
        StrBuilder sb = new StrBuilder("Hello World");
        sb.setLength(5);
        assertEquals("Hello", sb.toString());

        sb.setLength(7);
        assertEquals(7, sb.length());
        assertEquals("Hello\0\0", sb.toString());

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    // Tests exception on negative length
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLength_negativeLength_throwsException() {
        StrBuilder sb = new StrBuilder();
        sb.setLength(-1);
    }

    // Tests character retrieval and mutation
    @Test
    public void testCharAtAndSetCharAt_validAndInvalidIndices() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals('e', sb.charAt(1));

        sb.setCharAt(1, 'a');
        assertEquals("tast", sb.toString());

        sb.deleteCharAt(1);
        assertEquals("tst", sb.toString());

        char[] chars = sb.toCharArray();
        assertArrayEquals(new char[]{'t', 's', 't'}, chars);

        char[] subChars = sb.toCharArray(1, 3);
        assertArrayEquals(new char[]{'s', 't'}, subChars);
    }

    // Tests append methods for primitive types and objects
    @Test
    public void testAppend_variousTypes_appendsExpectedStrings() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        sb.append('!');
        sb.append(10).append(20L);
        sb.append(1.5f).append(2.5d);
        sb.append(new StringBuffer("SB"));
        sb.append(new StrBuilder("StrB"));
        sb.append(new char[]{'a', 'b', 'c'});
        sb.append(new char[]{'d', 'e', 'f'}, 1, 2);
        sb.append("substring", 3, 6);
        assertEquals("truefalse!10201.52.5SBStrBabcdefstri", sb.toString());

        sb.clear();
        sb.setNewLineText("\n");
        sb.appendNewLine();
        assertEquals("\n", sb.toString());

        sb.clear();
        sb.appendln("line1");
        sb.appendln(123);
        assertEquals("line1\n123\n", sb.toString());
    }

    // Tests append with separators and collections
    @Test
    public void testAppendWithSeparatorsAndCollections_validInputs_appendsFormatted() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z"), "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("1", "2").iterator(), ":");
        assertEquals("1:2", sb.toString());

        sb.clear();
        sb.appendAll(new Object[]{"a", "b"});
        sb.appendAll(Collections.singletonList("c"));
        sb.appendAll(Collections.singletonList("d").iterator());
        assertEquals("abcd", sb.toString());
    }

    // Tests appendSeparator conditional behavior
    @Test
    public void testAppendSeparator_variousConditions_appendsWhenNeeded() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());

        sb.append("first");
        sb.appendSeparator(",");
        sb.append("second");
        assertEquals("first,second", sb.toString());

        sb.clear();
        for (int i = 0; i < 3; i++) {
            sb.appendSeparator(",", i);
            sb.append(i);
        }
        assertEquals("0,1,2", sb.toString());
    }

    // Tests insert operations
    @Test
    public void testInsert_variousTypes_insertsAtCorrectIndices() {
        StrBuilder sb = new StrBuilder("world");
        sb.insert(0, "hello ");
        assertEquals("hello world", sb.toString());

        sb.insert(5, '!');
        assertEquals("hello! world", sb.toString());

        sb.insert(0, true);
        assertEquals("truehello! world", sb.toString());

        sb.insert(0, 42);
        assertEquals("42truehello! world", sb.toString());

        sb.insert(0, new char[]{'X', 'Y'}, 0, 2);
        assertEquals("XY42truehello! world", sb.toString());
    }

    // Tests delete methods
    @Test
    public void testDelete_variousPatterns_removesExpectedContent() {
        StrBuilder sb = new StrBuilder("foo bar foo baz foo");
        sb.deleteFirst("foo ");
        assertEquals("bar foo baz foo", sb.toString());

        sb.deleteAll("foo");
        assertEquals("bar  baz ", sb.toString());

        sb.setLength(0);
        sb.append("banana");
        sb.deleteFirst('a');
        assertEquals("bnana", sb.toString());

        sb.deleteAll('a');
        assertEquals("bnn", sb.toString());

        sb.delete(1, 2);
        assertEquals("bn", sb.toString());
    }

    // Tests replace operations
    @Test
    public void testReplace_variousInputs_replacesCorrectly() {
        StrBuilder sb = new StrBuilder("one two one three");
        sb.replaceFirst("one", "1");
        assertEquals("1 two one three", sb.toString());

        sb.replaceAll("one", "1");
        assertEquals("1 two 1 three", sb.toString());

        sb.replaceAll('1', 'X');
        assertEquals("X two X three", sb.toString());

        sb.replaceFirst('X', 'Y');
        assertEquals("Y two X three", sb.toString());

        sb.replace(0, 1, "Z");
        assertEquals("Z two X three", sb.toString());

        sb.replace(StrMatcher.stringMatcher("two"), "2", 0, sb.length(), -1);
        assertEquals("Z 2 X three", sb.toString());
    }

    // Tests substring operations (left, right, mid, substring)
    @Test
    public void testSubstrings_validAndBoundaryInputs_extractsCorrectly() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("hello", sb.leftString(5));
        assertEquals("world", sb.rightString(5));
        assertEquals("lo wo", sb.midString(3, 5));
        assertEquals("hello world", sb.leftString(50));
        assertEquals("hello world", sb.rightString(50));
        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.midString(3, -1));
        assertEquals("world", sb.substring(6));
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6, 20));
    }

    // Tests indexOf and lastIndexOf operations
    @Test
    public void testIndexOfAndLastIndexOf_variousSearches_returnsIndices() {
        StrBuilder sb = new StrBuilder("abracadabra");
        assertEquals(0, sb.indexOf('a'));
        assertEquals(3, sb.indexOf('a', 1));
        assertEquals(10, sb.lastIndexOf('a'));
        assertEquals(7, sb.lastIndexOf('a', 9));

        assertEquals(0, sb.indexOf("abra"));
        assertEquals(7, sb.indexOf("abra", 1));
        assertEquals(7, sb.lastIndexOf("abra"));
        assertEquals(0, sb.lastIndexOf("abra", 6));

        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(-1, sb.lastIndexOf("xyz"));
        assertEquals(-1, sb.lastIndexOf((String) null));

        assertTrue(sb.contains('c'));
        assertFalse(sb.contains('z'));
        assertTrue(sb.contains("cad"));
        assertFalse(sb.contains("xyz"));
    }

    // Tests startsWith, endsWith, trim, and reverse
    @Test
    public void testTransformationsAndPredicates_variousInputs_operatesCorrectly() {
        StrBuilder sb = new StrBuilder("  hello world  ");
        assertTrue(sb.startsWith("  hello"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world  "));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));

        sb.trim();
        assertEquals("hello world", sb.toString());

        sb.reverse();
        assertEquals("dlrow olleh", sb.toString());
    }

    // Tests equals, equalsIgnoreCase, and hashCode
    @Test
    public void testEqualsAndHashCode_comparisons_returnsExpectedResult() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("ABC");
        StrBuilder sb4 = new StrBuilder("abcd");

        assertEquals(sb1, sb2);
        assertEquals(sb1, (Object) sb2);
        assertNotEquals(sb1, sb3);
        assertNotEquals(sb1, sb4);
        assertNotEquals(sb1, null);
        assertNotEquals(sb1, "abc");

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(null));

        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertEquals("abc", sb1.toStringBuffer().toString());
    }

    // Tests Reader, Writer, and Tokenizer views
    @Test
    public void testViews_readerWriterTokenizer_interactsProperly() throws Exception {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tokenizer = sb.asTokenizer();
        assertArrayEquals(new String[]{"a", "b", "c"}, tokenizer.getTokenArray());

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('a', (char) reader.read());
        char[] buf = new char[3];
        int read = reader.read(buf, 0, 3);
        assertEquals(3, read);
        assertEquals(" b ", new String(buf));

        Writer writer = sb.asWriter();
        writer.write(" d");
        writer.flush();
        writer.close();
        assertEquals("a b c d", sb.toString());
    }

    // Tests exceptions for range validation
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateRange_invalidIndices_throwsException() {
        StrBuilder sb = new StrBuilder("test");
        sb.substring(3, 1);
    }
}