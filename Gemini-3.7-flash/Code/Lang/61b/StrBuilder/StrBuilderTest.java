package org.apache.commons.lang.text;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrBuilderTest {

    // Tests Lang-61 bug: indexOf and deleteAll when buffer capacity exceeds size
    @Test
    public void testIndexOf_stringNotFoundWithBufferLargerThanSize_returnsMinusOne() {
        StrBuilder sb = new StrBuilder("123456789012345678901234567890");
        sb.ensureCapacity(100);
        assertEquals(-1, sb.indexOf("notFound", 0));
    }

    // Tests deleteAll String with capacity larger than size
    @Test
    public void testDeleteAll_stringWithLargeCapacity_deletesCorrectly() {
        StrBuilder sb = new StrBuilder("12345");
        sb.ensureCapacity(50);
        sb.deleteAll("34");
        assertEquals("125", sb.toString());
        assertEquals(3, sb.length());
    }

    // Tests constructors and basic properties
    @Test
    public void testConstructors_validInputs_createsExpectedBuilder() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(0, sb1.length());
        assertTrue(sb1.isEmpty());
        assertTrue(sb1.capacity() >= StrBuilder.CAPACITY);

        StrBuilder sb2 = new StrBuilder(10);
        assertEquals(0, sb2.length());
        assertEquals(10, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(-5);
        assertEquals(StrBuilder.CAPACITY, sb3.capacity());

        StrBuilder sb4 = new StrBuilder("hello");
        assertEquals("hello", sb4.toString());
        assertEquals(5, sb4.length());
    }

    // Tests append null and null text behavior
    @Test
    public void testAppendNull_withNullText_appendsNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
        sb.append((String) null);
        sb.append((Object) null);
        assertEquals("NULLNULL", sb.toString());

        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    // Tests append primitives and various types
    @Test
    public void testAppend_variousTypes_appendsCorrectString() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        sb.append('!');
        sb.append(123);
        sb.append(456L);
        sb.append(1.5f);
        sb.append(2.5d);
        assertEquals("truefalse!1234561.52.5", sb.toString());
    }

    // Tests append with separators for arrays and collections
    @Test
    public void testAppendWithSeparators_arrayAndCollection_appendsProperlySeparated() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("x", "y"), "-");
        assertEquals("x-y", sb.toString());

        sb.appendWithSeparators(Arrays.asList("z").iterator(), null);
        assertEquals("x-yz", sb.toString());
    }

    // Tests padding and fixed width appending
    @Test
    public void testAppendFixedWidth_leftAndRightPad_formatsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, '0');
        assertEquals("00abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, '0');
        assertEquals("abc00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, '0');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 3, '0');
        assertEquals("abc", sb.toString());
    }

    // Tests insert operations
    @Test
    public void testInsert_variousTypes_insertsAtSpecifiedIndex() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, "prefix-");
        assertEquals("prefix-abc", sb.toString());

        sb.insert(sb.length(), true);
        assertEquals("prefix-abctrue", sb.toString());

        sb.insert(0, new char[]{'1', '2', '3'}, 1, 2);
        assertEquals("23prefix-abctrue", sb.toString());
    }

    // Tests insert index out of bounds exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_invalidIndex_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(10, "invalid");
    }

    // Tests delete operations
    @Test
    public void testDelete_variousMethods_removesExpectedContent() {
        StrBuilder sb = new StrBuilder("a-b-c-b-a");
        sb.deleteFirst('b');
        assertEquals("a--c-b-a", sb.toString());

        sb.deleteAll('-');
        assertEquals("acba", sb.toString());

        sb.deleteCharAt(0);
        assertEquals("cba", sb.toString());

        sb.delete(1, 2);
        assertEquals("ca", sb.toString());
    }

    // Tests replace operations
    @Test
    public void testReplace_variousMethods_replacesExpectedContent() {
        StrBuilder sb = new StrBuilder("foo bar foo baz");
        sb.replaceFirst("foo", "qux");
        assertEquals("qux bar foo baz", sb.toString());

        sb.replaceAll("foo", "qux");
        assertEquals("qux bar qux baz", sb.toString());

        sb.replaceAll('q', 'z');
        assertEquals("zux bar zux baz", sb.toString());

        sb.replace(0, 3, "abc");
        assertEquals("abc bar zux baz", sb.toString());
    }

    // Tests reverse and trim
    @Test
    public void testReverseAndTrim_validBuilder_reversesAndTrims() {
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

    // Tests startsWith and endsWith
    @Test
    public void testStartsWithAndEndsWith_validAndNullInputs_returnsCorrectBoolean() {
        StrBuilder sb = new StrBuilder("hello world");
        assertTrue(sb.startsWith("hello"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
    }

    // Tests substring and sliced strings
    @Test
    public void testSubstrings_validAndEdgeIndices_returnsExpectedStrings() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("cdef", sb.substring(2));
        assertEquals("cd", sb.substring(2, 4));
        assertEquals("ab", sb.leftString(2));
        assertEquals("ef", sb.rightString(2));
        assertEquals("cd", sb.midString(2, 2));

        assertEquals("", sb.leftString(-1));
        assertEquals("abcdef", sb.leftString(10));
        assertEquals("", sb.rightString(-1));
        assertEquals("abcdef", sb.rightString(10));
        assertEquals("", sb.midString(-1, 0));
        assertEquals("cdef", sb.midString(2, 10));
    }

    // Tests indexOf and lastIndexOf for char and String
    @Test
    public void testIndexOfAndLastIndexOf_variousInputs_returnsExpectedIndices() {
        StrBuilder sb = new StrBuilder("banana");
        assertEquals(1, sb.indexOf('a'));
        assertEquals(3, sb.indexOf('a', 2));
        assertEquals(-1, sb.indexOf('z'));

        assertEquals(1, sb.indexOf("an"));
        assertEquals(3, sb.indexOf("an", 2));
        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf((String) null));

        assertEquals(5, sb.lastIndexOf('a'));
        assertEquals(3, sb.lastIndexOf('a', 4));
        assertEquals(-1, sb.lastIndexOf('z'));

        assertEquals(3, sb.lastIndexOf("an"));
        assertEquals(1, sb.lastIndexOf("an", 2));
        assertEquals(-1, sb.lastIndexOf("xyz"));
        assertEquals(-1, sb.lastIndexOf((String) null));
    }

    // Tests charArray conversion and getChars
    @Test
    public void testToCharArrayAndGetChars_validRange_returnsChars() {
        StrBuilder sb = new StrBuilder("abcdef");
        char[] arr = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, arr);

        char[] subArr = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'b', 'c', 'd'}, subArr);

        char[] dest = new char[4];
        sb.getChars(1, 4, dest, 1);
        assertEquals('\0', dest[0]);
        assertEquals('b', dest[1]);
        assertEquals('c', dest[2]);
        assertEquals('d', dest[3]);
    }

    // Tests equals, equalsIgnoreCase, and hashCode
    @Test
    public void testEqualsAndHashCode_sameAndDifferentBuilders_returnsExpected() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("hello");

        assertTrue(sb1.equals(sb2));
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals("Hello"));
        assertFalse(sb1.equals(null));

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    // Tests reader and writer views
    @Test
    public void testAsReaderAndAsWriter_validUsage_readsAndWritesCorrectly() throws Exception {
        StrBuilder sb = new StrBuilder("initial");
        Writer writer = sb.asWriter();
        writer.write("-appended");
        assertEquals("initial-appended", sb.toString());

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('i', reader.read());

        char[] buf = new char[6];
        int readCount = reader.read(buf, 0, 6);
        assertEquals(6, readCount);
        assertEquals("nitial", new String(buf));
    }

    // Tests tokenizer view
    @Test
    public void testAsTokenizer_validInput_tokenizesContent() {
        StrBuilder sb = new StrBuilder("one two three");
        StrTokenizer tok = sb.asTokenizer();
        String[] tokens = tok.getTokenArray();
        assertArrayEquals(new String[]{"one", "two", "three"}, tokens);
    }
}