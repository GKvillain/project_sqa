package org.apache.commons.lang.text;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.junit.Assert.*;

public class StrBuilderTest {

    // Tests defect Lang-60: contains(char) when buffer contains char past size
    @Test
    public void testContains_charPastSizeInBuffer_returnsFalse() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(0, 5);
        assertFalse(sb.contains('h'));
        assertFalse(sb.contains('e'));
        assertFalse(sb.contains('l'));
        assertFalse(sb.contains('o'));
    }

    // Tests defect Lang-60: contains(char) when empty buffer has null chars
    @Test
    public void testContains_nullCharInEmptyBuffer_returnsFalse() {
        StrBuilder sb = new StrBuilder(32);
        assertFalse(sb.contains('\0'));
    }

    // Tests defect Lang-60: indexOf(char, int) when buffer contains char past size
    @Test
    public void testIndexOf_charPastSizeInBuffer_returnsNegativeOne() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.setLength(5);
        assertEquals(-1, sb.indexOf('w', 0));
        assertEquals(-1, sb.indexOf('o', 5));
    }

    // Tests defect Lang-60: indexOf(char, int) with null character on unused buffer
    @Test
    public void testIndexOf_nullCharInUnusedBuffer_returnsNegativeOne() {
        StrBuilder sb = new StrBuilder(32);
        sb.append("test");
        assertEquals(-1, sb.indexOf('\0', 0));
    }

    // Tests constructors and capacity management
    @Test
    public void testConstructorsAndCapacity_variousInputs_maintainsCapacityAndLength() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(0, sb1.length());
        assertEquals(StrBuilder.CAPACITY, sb1.capacity());

        StrBuilder sb2 = new StrBuilder(-5);
        assertEquals(StrBuilder.CAPACITY, sb2.capacity());

        StrBuilder sb3 = new StrBuilder("abc");
        assertEquals(3, sb3.length());
        assertEquals(3 + StrBuilder.CAPACITY, sb3.capacity());
        assertEquals("abc", sb3.toString());

        sb3.minimizeCapacity();
        assertEquals(3, sb3.capacity());

        sb3.ensureCapacity(10);
        assertTrue(sb3.capacity() >= 10);
    }

    // Tests append methods for primitives, null values, and objects
    @Test
    public void testAppend_primitivesAndNullHandling_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.append((String) null);
        sb.append(true);
        sb.append(false);
        sb.append('!');
        sb.append(123);
        sb.append(456L);
        sb.append(1.5f);
        sb.append(2.5d);

        assertEquals("<null>truefalse!1234561.52.5", sb.toString());

        sb.clear();
        assertTrue(sb.isEmpty());
        sb.appendNull();
        assertEquals("<null>", sb.toString());
    }

    // Tests appendWithSeparators for arrays and collections
    @Test
    public void testAppendWithSeparators_arraysAndCollections_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y"), "-");
        assertEquals("X-Y", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.singletonList("Single").iterator(), ",");
        assertEquals("Single", sb.toString());

        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("Single", sb.toString());
    }

    // Tests padding and fixed width appending
    @Test
    public void testAppendPaddingAndFixedWidth_variousAlignments_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 5, ' ');
        assertEquals("  foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("longstring", 4, ' ');
        assertEquals("ring", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("bar", 5, '0');
        assertEquals("bar00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 4, '0');
        assertEquals("4200", sb.toString());
    }

    // Tests insert operations
    @Test
    public void testInsert_variousTypes_insertsAtCorrectIndices() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, "start-");
        assertEquals("start-abc", sb.toString());

        sb.insert(sb.length(), true);
        assertEquals("start-abctrue", sb.toString());

        char[] chars = new char[]{'1', '2', '3'};
        sb.insert(0, chars, 1, 2);
        assertEquals("23start-abctrue", sb.toString());
    }

    // Tests delete operations
    @Test
    public void testDelete_rangesAndPatterns_deletesCorrectly() {
        StrBuilder sb = new StrBuilder("hello world hello");
        sb.deleteFirst("hello ");
        assertEquals("world hello", sb.toString());

        sb.deleteAll('o');
        assertEquals("wrld hell", sb.toString());

        sb.deleteCharAt(0);
        assertEquals("rld hell", sb.toString());

        sb.delete(3, 100);
        assertEquals("rld", sb.toString());

        sb.deleteAll(StrMatcher.charMatcher('l'));
        assertEquals("rd", sb.toString());
    }

    // Tests replace operations with chars, strings, and StrMatcher
    @Test
    public void testReplace_charsStringsAndMatchers_replacesCorrectly() {
        StrBuilder sb = new StrBuilder("foo bar foo baz");
        sb.replaceFirst('o', 'a');
        assertEquals("fao bar foo baz", sb.toString());

        sb.replaceAll('o', 'e');
        assertEquals("fae bar fee baz", sb.toString());

        sb.replaceFirst("fae", "foo");
        assertEquals("foo bar fee baz", sb.toString());

        sb.replaceAll("fee", "foo");
        assertEquals("foo bar foo baz", sb.toString());

        sb.replaceAll(StrMatcher.stringMatcher("foo"), "qux");
        assertEquals("qux bar qux baz", sb.toString());

        sb.replace(0, 3, "start");
        assertEquals("start bar qux baz", sb.toString());
    }

    // Tests substring and mid/left/right extractions
    @Test
    public void testSubstrings_validAndOutOfBoundsIndices_returnsExpected() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("ab", sb.leftString(2));
        assertEquals("abcdef", sb.leftString(10));
        assertEquals("", sb.leftString(-1));

        assertEquals("ef", sb.rightString(2));
        assertEquals("abcdef", sb.rightString(10));
        assertEquals("", sb.rightString(0));

        assertEquals("cde", sb.midString(2, 3));
        assertEquals("def", sb.midString(3, 10));
        assertEquals("", sb.midString(-1, 0));

        assertEquals("bc", sb.substring(1, 3));
        assertEquals("cdef", sb.substring(2));
    }

    // Tests startsWith and endsWith
    @Test
    public void testStartsAndEndsWith_variousInputs_returnsCorrectBoolean() {
        StrBuilder sb = new StrBuilder("hello world");
        assertTrue(sb.startsWith("hello"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
    }

    // Tests indexOf and lastIndexOf with Strings and StrMatchers
    @Test
    public void testIndexOfAndLastIndexOf_variousInputs_returnsExpectedIndices() {
        StrBuilder sb = new StrBuilder("banana");
        assertEquals(1, sb.indexOf('a'));
        assertEquals(3, sb.indexOf('a', 2));
        assertEquals(5, sb.lastIndexOf('a'));
        assertEquals(3, sb.lastIndexOf('a', 4));

        assertEquals(1, sb.indexOf("an"));
        assertEquals(3, sb.lastIndexOf("an"));
        assertEquals(-1, sb.indexOf("orange"));
        assertEquals(-1, sb.lastIndexOf("orange"));

        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('a')));
        assertEquals(5, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        assertEquals(-1, sb.indexOf((StrMatcher) null));
    }

    // Tests asReader and asWriter functionality
    @Test
    public void testAsReaderAndWriter_readAndWriteOperations_synchronizesCorrectly() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write("Hello");
        writer.write(' ');
        writer.write("World".toCharArray());
        assertEquals("Hello World", sb.toString());

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('H', reader.read());
        char[] buf = new char[4];
        int readCount = reader.read(buf, 0, 4);
        assertEquals(4, readCount);
        assertEquals("ello", new String(buf));

        reader.skip(1);
        assertEquals('W', reader.read());
    }

    // Tests asTokenizer functionality
    @Test
    public void testAsTokenizer_spaceSeparated_tokenizesCorrectly() {
        StrBuilder sb = new StrBuilder("one two three");
        StrTokenizer tokenizer = sb.asTokenizer();
        String[] tokens = tokenizer.getTokenArray();
        assertArrayEquals(new String[]{"one", "two", "three"}, tokens);
        assertEquals("one two three", tokenizer.getContent());
    }

    // Tests reverse and trim operations
    @Test
    public void testTrimAndReverse_validContent_modifiesContentProperly() {
        StrBuilder sb = new StrBuilder("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());

        sb.reverse();
        assertEquals("olleh", sb.toString());

        StrBuilder empty = new StrBuilder();
        empty.trim();
        empty.reverse();
        assertEquals("", empty.toString());
    }

    // Tests equals, equalsIgnoreCase, and hashCode
    @Test
    public void testEqualsAndHashCode_variousComparisons_returnsExpected() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("hello");

        assertTrue(sb1.equals(sb2));
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertFalse(sb1.equals("Hello"));
        assertFalse(sb1.equals(null));
    }

    // Tests exception path for charAt with negative index
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_negativeIndex_throwsException() {
        StrBuilder sb = new StrBuilder("test");
        sb.charAt(-1);
    }

    // Tests exception path for setLength with negative length
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLength_negativeLength_throwsException() {
        StrBuilder sb = new StrBuilder("test");
        sb.setLength(-1);
    }
}