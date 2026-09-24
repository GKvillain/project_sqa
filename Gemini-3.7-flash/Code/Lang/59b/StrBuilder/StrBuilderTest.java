package org.apache.commons.lang.text;

import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class StrBuilderTest {

    private StrBuilder sb;

    @Before
    public void setUp() {
        sb = new StrBuilder();
    }

    // Tests Lang-59 defect: appendFixedWidthPadRight with object length > width
    @Test
    public void testAppendFixedWidthPadRight_longerInput_truncatesRightSide() {
        sb.appendFixedWidthPadRight("foo bar", 3, '-');
        assertEquals("foo", sb.toString());
        assertEquals(3, sb.length());
    }

    // Tests Lang-59 defect on small initial buffer
    @Test
    public void testAppendFixedWidthPadRight_longerInputSmallBuffer_noException() {
        StrBuilder builder = new StrBuilder(4);
        builder.appendFixedWidthPadRight("1234567890", 5, ' ');
        assertEquals("12345", builder.toString());
        assertEquals(5, builder.length());
    }

    // Tests appendFixedWidthPadRight with shorter input padding
    @Test
    public void testAppendFixedWidthPadRight_shorterInput_padsRight() {
        sb.appendFixedWidthPadRight("foo", 6, '-');
        assertEquals("foo---", sb.toString());
    }

    // Tests appendFixedWidthPadRight with int and null
    @Test
    public void testAppendFixedWidthPadRight_intAndNullInput_correctOutput() {
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 6, '-');
        assertEquals("null--", sb.toString());
        sb.clear();
        sb.appendFixedWidthPadRight(123, 5, '0');
        assertEquals("12300", sb.toString());
    }

    // Tests appendFixedWidthPadLeft with longer, shorter and null input
    @Test
    public void testAppendFixedWidthPadLeft_variousInputs_padsOrTruncatesCorrectly() {
        sb.appendFixedWidthPadLeft("foobar", 3, '-');
        assertEquals("bar", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 6, '-');
        assertEquals("---foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 5, '0');
        assertEquals("00042", sb.toString());
    }

    // Tests constructors and capacity management
    @Test
    public void testConstructorsAndCapacity_variousParameters_correctCapacityAndLength() {
        StrBuilder b1 = new StrBuilder();
        assertEquals(32, b1.capacity());
        assertEquals(0, b1.length());
        assertTrue(b1.isEmpty());

        StrBuilder b2 = new StrBuilder(-5);
        assertEquals(32, b2.capacity());

        StrBuilder b3 = new StrBuilder("initial");
        assertEquals("initial", b3.toString());
        assertEquals(7, b3.length());
        assertEquals(7 + 32, b3.capacity());

        b3.minimizeCapacity();
        assertEquals(7, b3.capacity());

        b3.ensureCapacity(100);
        assertTrue(b3.capacity() >= 100);
    }

    // Tests setLength, charAt, setCharAt, and deleteCharAt
    @Test
    public void testLengthAndCharacterOperations_validInputs_modifiesBuilder() {
        sb.append("Hello World");
        assertEquals('H', sb.charAt(0));
        assertEquals('d', sb.charAt(10));

        sb.setCharAt(0, 'h');
        assertEquals("hello World", sb.toString());

        sb.deleteCharAt(5);
        assertEquals("helloWorld", sb.toString());

        sb.setLength(5);
        assertEquals("hello", sb.toString());

        sb.setLength(7);
        assertEquals("hello\0\0", sb.toString());
    }

    // Tests exception on charAt out of bounds
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_negativeIndex_throwsException() {
        sb.charAt(-1);
    }

    // Tests toCharArray and getChars
    @Test
    public void testToCharArrayAndGetChars_validRanges_returnsExpectedArrays() {
        sb.append("abcdef");
        char[] array = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, array);

        char[] subArray = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'b', 'c', 'd'}, subArray);

        char[] dest = new char[4];
        sb.getChars(1, 4, dest, 1);
        assertEquals('b', dest[1]);
        assertEquals('c', dest[2]);
        assertEquals('d', dest[3]);
    }

    // Tests appending various primitive types and objects
    @Test
    public void testAppend_primitivesAndObjects_appendsCorrectString() {
        sb.append(true)
          .append(false)
          .append('!')
          .append(123)
          .append(456L)
          .append(1.5f)
          .append(2.5d);
        assertEquals("truefalse!1234561.52.5", sb.toString());

        sb.clear();
        sb.setNewLineText("\n");
        sb.append("line1").appendNewLine().append("line2");
        assertEquals("line1\nline2", sb.toString());
    }

    // Tests appendWithSeparators for array, collection, and iterator
    @Test
    public void testAppendWithSeparators_arrayAndCollections_appendsFormatted() {
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        List<String> list = Arrays.asList("1", "2", "3");
        sb.appendWithSeparators(list, "-");
        assertEquals("1-2-3", sb.toString());

        sb.clear();
        sb.appendWithSeparators(list.iterator(), ":");
        assertEquals("1:2:3", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators((List) null, ",");
        assertEquals("", sb.toString());
    }

    // Tests substring, leftString, rightString, and midString
    @Test
    public void testSubstrings_validAndEdgeIndices_returnsCorrectSubstrings() {
        sb.append("HelloWorld");
        assertEquals("World", sb.substring(5));
        assertEquals("Hello", sb.substring(0, 5));
        assertEquals("Hello", sb.leftString(5));
        assertEquals("World", sb.rightString(5));
        assertEquals("loWo", sb.midString(3, 4));

        assertEquals("", sb.leftString(-1));
        assertEquals("HelloWorld", sb.leftString(50));
        assertEquals("", sb.rightString(0));
        assertEquals("HelloWorld", sb.rightString(50));
        assertEquals("", sb.midString(50, 2));
    }

    // Tests insert operations
    @Test
    public void testInsert_variousTypes_insertsAtSpecifiedIndex() {
        sb.append("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());

        sb.insert(4, 123);
        assertEquals("true123abc", sb.toString());

        sb.insert(0, new char[]{'X', 'Y'}, 0, 2);
        assertEquals("XYtrue123abc", sb.toString());
    }

    // Tests delete and replace operations with char, string, and matchers
    @Test
    public void testDeleteAndReplace_validInputs_modifiesBuffer() {
        sb.append("foo bar baz bar foo");
        sb.deleteFirst("bar ");
        assertEquals("foo baz bar foo", sb.toString());

        sb.deleteAll("foo");
        assertEquals(" baz bar ", sb.toString());

        sb.trim();
        assertEquals("baz bar", sb.toString());

        sb.replaceAll('a', 'o');
        assertEquals("boz bor", sb.toString());

        sb.replaceFirst('o', 'a');
        assertEquals("baz bor", sb.toString());

        sb.replace(0, 3, "foo");
        assertEquals("foo bor", sb.toString());

        sb.replaceAll(StrMatcher.charMatcher('o'), "x");
        assertEquals("fxx bxr", sb.toString());
    }

    // Tests search methods: contains, indexOf, lastIndexOf, startsWith, endsWith
    @Test
    public void testSearchMethods_variousMatches_returnsExpectedIndices() {
        sb.append("the quick brown fox jumps over the lazy dog");
        assertTrue(sb.contains('q'));
        assertTrue(sb.contains("brown"));
        assertFalse(sb.contains("cat"));
        assertTrue(sb.contains(StrMatcher.stringMatcher("fox")));

        assertTrue(sb.startsWith("the"));
        assertFalse(sb.startsWith("quick"));
        assertTrue(sb.endsWith("dog"));
        assertFalse(sb.endsWith("cat"));

        assertEquals(4, sb.indexOf('q'));
        assertEquals(10, sb.indexOf("brown"));
        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(42, sb.lastIndexOf('g'));

        assertEquals(-1, sb.indexOf("notfound"));
        assertEquals(-1, sb.lastIndexOf("notfound"));
    }

    // Tests reverse, equals, equalsIgnoreCase, and hashCode
    @Test
    public void testReverseAndEqualsAndHashCode_validInputs_returnsCorrectResults() {
        sb.append("abcdef");
        sb.reverse();
        assertEquals("fedcba", sb.toString());

        StrBuilder other1 = new StrBuilder("fedcba");
        StrBuilder other2 = new StrBuilder("FEDCBA");
        StrBuilder other3 = new StrBuilder("different");

        assertTrue(sb.equals(other1));
        assertTrue(sb.equals((Object) other1));
        assertFalse(sb.equals(other2));
        assertFalse(sb.equals(other3));
        assertFalse(sb.equals("fedcba"));
        assertFalse(sb.equals(null));

        assertTrue(sb.equalsIgnoreCase(other2));
        assertFalse(sb.equalsIgnoreCase(other3));
        assertFalse(sb.equalsIgnoreCase(null));

        assertEquals(sb.hashCode(), other1.hashCode());
    }

    // Tests asReader, asWriter, and asTokenizer views
    @Test
    public void testViews_readerWriterTokenizer_behavesCorrectly() throws Exception {
        sb.append("word1 word2 word3");

        StrTokenizer tok = sb.asTokenizer();
        String[] tokens = tok.getTokenArray();
        assertArrayEquals(new String[]{"word1", "word2", "word3"}, tokens);

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        char[] buf = new char[5];
        int readCount = reader.read(buf, 0, 5);
        assertEquals(5, readCount);
        assertEquals("word1", new String(buf));

        Writer writer = sb.asWriter();
        writer.write(" word4");
        assertEquals("word1 word2 word3 word4", sb.toString());
    }
}