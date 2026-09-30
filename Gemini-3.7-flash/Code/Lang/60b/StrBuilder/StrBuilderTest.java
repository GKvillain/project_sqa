package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class StrBuilderTest {

    // Tests Lang-60 bug: contains(char) checking buffer capacity instead of size
    @Test
    public void testContains_charNotInSizeRange_returnsFalse() {
        StrBuilder sb = new StrBuilder("hello");
        assertFalse(sb.contains('\0'));
        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('z'));
    }

    // Tests Lang-60 bug: indexOf(char, int) checking buffer.length instead of size
    @Test
    public void testIndexOf_charNullByteNotInSizeRange_returnsNotFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf('\0', 0));
        assertEquals(1, sb.indexOf('e', 0));
        assertEquals(-1, sb.indexOf('z', 0));
    }

    // Tests constructors with different capacities and initial string
    @Test
    public void testConstructor_variousInputs_initializesProperly() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(0, sb1.length());
        assertTrue(sb1.capacity() >= StrBuilder.CAPACITY);

        StrBuilder sb2 = new StrBuilder(0);
        assertEquals(0, sb2.length());
        assertEquals(StrBuilder.CAPACITY, sb2.capacity());

        StrBuilder sb3 = new StrBuilder("test");
        assertEquals(4, sb3.length());
        assertEquals("test", sb3.toString());

        StrBuilder sb4 = new StrBuilder((String) null);
        assertEquals(0, sb4.length());
    }

    // Tests append methods for primitives, Objects, and null handling
    @Test
    public void testAppend_variousDataTypes_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.append(true)
          .append(' ')
          .append(123)
          .append(' ')
          .append(456L)
          .append(' ')
          .append(1.5f)
          .append(' ')
          .append(2.5d)
          .append(' ')
          .append((Object) null);

        assertEquals("true 123 456 1.5 2.5 <null>", sb.toString());
    }

    // Tests append with separators for arrays and collections
    @Test
    public void testAppendWithSeparators_arrayAndCollection_appendsProperly() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z"), "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals(0, sb.length());
    }

    // Tests append padding and fixed width formatting
    @Test
    public void testAppendPaddingAndFixedWidth_variousWidths_padsCorrectly() {
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

    // Tests insert operations at various positions
    @Test
    public void testInsert_variousTypes_insertsCorrectly() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());

        sb.insert(7, "123");
        assertEquals("trueabc123", sb.toString());

        sb.insert(0, new char[]{'X', 'Y'}, 0, 2);
        assertEquals("XYtrueabc123", sb.toString());
    }

    // Tests delete methods for char, string, and ranges
    @Test
    public void testDelete_charAndStringAndRange_removesExpectedContent() {
        StrBuilder sb = new StrBuilder("banana");
        sb.deleteFirst('a');
        assertEquals("bnana", sb.toString());

        sb.deleteAll('a');
        assertEquals("bnn", sb.toString());

        sb.clear();
        sb.append("foo bar foo bar");
        sb.deleteFirst("foo ");
        assertEquals("bar foo bar", sb.toString());

        sb.deleteAll("bar");
        assertEquals(" foo ", sb.toString());

        sb.delete(0, 1);
        assertEquals("foo ", sb.toString());

        sb.deleteCharAt(3);
        assertEquals("foo", sb.toString());
    }

    // Tests replace operations for chars, strings, and matcher
    @Test
    public void testReplace_variousInputs_replacesCorrectly() {
        StrBuilder sb = new StrBuilder("hello world hello");
        sb.replaceFirst('o', 'a');
        assertEquals("hella world hello", sb.toString());

        sb.replaceAll('l', 'x');
        assertEquals("hexxa worxd hexxo", sb.toString());

        sb.clear();
        sb.append("one two one three");
        sb.replaceFirst("one", "1");
        assertEquals("1 two one three", sb.toString());

        sb.replaceAll("one", "1");
        assertEquals("1 two 1 three", sb.toString());

        sb.replace(0, 1, "ONE");
        assertEquals("ONE two 1 three", sb.toString());

        sb.clear();
        sb.append("a b c");
        sb.replaceAll(StrMatcher.charSetMatcher(" "), "-");
        assertEquals("a-b-c", sb.toString());
    }

    // Tests reverse and trim functionality
    @Test
    public void testReverseAndTrim_validInput_transformsCorrectly() {
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

    // Tests prefix and suffix matching
    @Test
    public void testStartsWithAndEndsWith_variousStrings_returnsExpected() {
        StrBuilder sb = new StrBuilder("helloworld");
        assertTrue(sb.startsWith("hello"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
    }

    // Tests substring and slice methods
    @Test
    public void testSubstrings_validIndices_extractsSubstrings() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("cd", sb.substring(2, 4));
        assertEquals("cdef", sb.substring(2));
        assertEquals("ab", sb.leftString(2));
        assertEquals("ef", sb.rightString(2));
        assertEquals("bcd", sb.midString(1, 3));

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.midString(10, 2));
    }

    // Tests search methods (indexOf, lastIndexOf, contains)
    @Test
    public void testSearchMethods_variousInputs_findsIndices() {
        StrBuilder sb = new StrBuilder("foo bar foo");
        assertEquals(0, sb.indexOf("foo"));
        assertEquals(8, sb.indexOf("foo", 4));
        assertEquals(8, sb.lastIndexOf("foo"));
        assertEquals(0, sb.lastIndexOf("foo", 7));
        assertEquals(-1, sb.indexOf("baz"));
        assertEquals(-1, sb.lastIndexOf("baz"));

        assertTrue(sb.contains("bar"));
        assertFalse(sb.contains("baz"));
        assertTrue(sb.contains(StrMatcher.stringMatcher("bar")));

        assertEquals(4, sb.indexOf(StrMatcher.stringMatcher("bar")));
        assertEquals(4, sb.lastIndexOf(StrMatcher.stringMatcher("bar")));
    }

    // Tests character array conversions and copying
    @Test
    public void testToCharArrayAndGetChars_validRange_copiesCorrectly() {
        StrBuilder sb = new StrBuilder("abcdef");
        char[] array = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, array);

        char[] subArray = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'b', 'c', 'd'}, subArray);

        char[] dest = new char[3];
        sb.getChars(1, 4, dest, 0);
        assertArrayEquals(new char[]{'b', 'c', 'd'}, dest);
    }

    // Tests equality and hashCode implementation
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_worksCorrectly() {
        StrBuilder sb1 = new StrBuilder("test");
        StrBuilder sb2 = new StrBuilder("test");
        StrBuilder sb3 = new StrBuilder("TEST");

        assertTrue(sb1.equals(sb2));
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(null));
        assertFalse(sb1.equals("test"));

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    // Tests Reader view of StrBuilder
    @Test
    public void testAsReader_readOperations_readsCorrectData() throws Exception {
        StrBuilder sb = new StrBuilder("abc");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertTrue(reader.markSupported());
        assertEquals('a', reader.read());

        reader.mark(10);
        char[] buf = new char[2];
        int count = reader.read(buf, 0, 2);
        assertEquals(2, count);
        assertArrayEquals(new char[]{'b', 'c'}, buf);

        reader.reset();
        assertEquals('b', reader.read());
        reader.close();
    }

    // Tests Writer and Tokenizer views
    @Test
    public void testAsWriterAndTokenizer_operations_modifiesAndTokenizes() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write("Hello ");
        writer.write(new char[]{'W', 'o', 'r', 'l', 'd'}, 0, 5);
        writer.flush();
        writer.close();
        assertEquals("Hello World", sb.toString());

        StrTokenizer tokenizer = sb.asTokenizer();
        String[] tokens = tokenizer.getTokenArray();
        assertEquals(2, tokens.length);
        assertEquals("Hello", tokens[0]);
        assertEquals("World", tokens[1]);
    }

    // Tests capacity management and setLength
    @Test
    public void testCapacityAndSetLength_growthAndShrink_updatesState() {
        StrBuilder sb = new StrBuilder(10);
        sb.append("hello");
        assertEquals(5, sb.length());
        sb.ensureCapacity(50);
        assertTrue(sb.capacity() >= 50);

        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());

        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("hel", sb.toString());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals('h', sb.charAt(0));
        assertEquals('\0', sb.charAt(4));
    }

    // Tests exception path for invalid charAt index
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_negativeIndex_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(-1);
    }

    // Tests exception path for setLength negative
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLength_negativeLength_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setLength(-1);
    }

    // Additional tests for complete coverage

    @Test
    public void testAppendln_allTypes_appendsNewline() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");
        assertEquals("\n", sb.getNewLineText());

        sb.appendNewLine()
          .appendln("line1")
          .appendln(new StringBuffer("line2"))
          .appendln(new StrBuilder("line3"))
          .appendln(new char[]{'l', 'i', 'n', 'e', '4'})
          .appendln(new char[]{'a', 'l', 'i', 'n', 'e', '5', 'z'}, 1, 5)
          .appendln(true)
          .appendln('x')
          .appendln(10)
          .appendln(20L)
          .appendln(3.14f)
          .appendln(4.56d)
          .appendln((Object) "lineObj");

        String expected = "\nline1\nline2\nline3\nline4\nline5\ntrue\nx\n10\n20\n3.14\n4.56\nlineObj\n";
        assertEquals(expected, sb.toString());

        sb.setNewLineText(null);
        sb.clear();
        sb.appendln("test");
        assertEquals("test" + System.getProperty("line.separator"), sb.toString());
    }

    @Test
    public void testAppend_subSequenceAndBuffersAndArrays() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("buff"), 1, 2);
        sb.append((StringBuffer) null, 0, 0);
        sb.append(new StrBuilder("builder"), 1, 3);
        sb.append((StrBuilder) null, 0, 0);
        sb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        sb.append((char[]) null, 0, 0);
        sb.append("string", 1, 3);
        sb.append((String) null, 0, 0);
        assertEquals("ufuiabtr", sb.toString());

        sb.clear();
        sb.append(new StringBuffer("hello"));
        sb.append((StringBuffer) null);
        sb.append(new StrBuilder("world"));
        sb.append((StrBuilder) null);
        sb.append(new char[]{'!'});
        sb.append((char[]) null);
        assertEquals("helloworld!", sb.toString());
    }

    @Test
    public void testAppendAll_collectionsAndIteratorsAndArrays() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new Object[]{"1", "2"});
        sb.appendAll((Object[]) null);
        sb.appendAll(Arrays.asList("3", "4"));
        sb.appendAll((Iterable<?>) null);
        sb.appendAll(Arrays.asList("5", "6").iterator());
        sb.appendAll((Iterator<?>) null);
        assertEquals("123456", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("a", "b").iterator(), ",");
        assertEquals("a,b", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators(Collections.emptyList(), ",");
        assertEquals(0, sb.length());
    }

    @Test
    public void testAppendSeparator_variousOverloads() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());
        sb.append("A");
        sb.appendSeparator(",");
        assertEquals("A,", sb.toString());

        sb.clear();
        sb.appendSeparator(',');
        assertEquals("", sb.toString());
        sb.append("A");
        sb.appendSeparator(',');
        assertEquals("A,", sb.toString());

        sb.clear();
        sb.appendSeparator(",", "default");
        assertEquals("default", sb.toString());
        sb.appendSeparator(",", "default");
        assertEquals("default,", sb.toString());

        sb.clear();
        sb.appendSeparator(',', 'd');
        assertEquals("d", sb.toString());
        sb.appendSeparator(',', 'd');
        assertEquals("d,", sb.toString());

        sb.clear();
        sb.appendSeparator(",", 0);
        assertEquals("", sb.toString());
        sb.appendSeparator(",", 1);
        assertEquals(",", sb.toString());

        sb.clear();
        sb.appendSeparator(',', 0);
        assertEquals("", sb.toString());
        sb.appendSeparator(',', 1);
        assertEquals(",", sb.toString());
    }

    @Test
    public void testInsert_allPrimitiveAndObjectTypes() {
        StrBuilder sb = new StrBuilder();
        sb.insert(0, (Object) "obj")
          .insert(0, (String) null)
          .insert(0, (char[]) null)
          .insert(0, new char[]{'c', 'h', 'a', 'r'})
          .insert(0, (StringBuffer) null)
          .insert(0, new StringBuffer("buf"))
          .insert(0, (StrBuilder) null)
          .insert(0, new StrBuilder("bld"))
          .insert(0, (byte) 1)
          .insert(0, (short) 2)
          .insert(0, (int) 3)
          .insert(0, (long) 4)
          .insert(0, 5.0f)
          .insert(0, 6.0d);

        assertNotNull(sb.toString());
    }

    @Test
    public void testDeleteAndReplace_matcherAndIndexMethods() {
        StrBuilder sb = new StrBuilder("A-B-C-D-E");
        sb.deleteFirst(StrMatcher.charMatcher('-'));
        assertEquals("AB-C-D-E", sb.toString());

        sb.deleteAll(StrMatcher.charMatcher('-'));
        assertEquals("ABCDE", sb.toString());

        sb.replace(StrMatcher.charMatcher('B'), "X", 0, sb.length(), 1);
        assertEquals("AXCDE", sb.toString());

        sb.replaceFirst(StrMatcher.charMatcher('C'), "Y");
        assertEquals("AXYDE", sb.toString());

        sb.replaceAll(StrMatcher.charMatcher('D'), "Z");
        assertEquals("AXYZE", sb.toString());

        sb.replace(0, 100, "RESET");
        assertEquals("RESET", sb.toString());
    }

    @Test
    public void testGetCharsAndSetCharAt() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setCharAt(0, 'H');
        assertEquals("Hello", sb.toString());

        char[] dest = new char[5];
        sb.getChars(dest);
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, dest);
    }

    @Test
    public void testSearchMethods_edgeCases() {
        StrBuilder sb = new StrBuilder("hello world hello");
        assertEquals(6, sb.indexOf('w'));
        assertEquals(6, sb.indexOf('w', 0));
        assertEquals(-1, sb.indexOf('w', 10));
        assertEquals(-1, sb.indexOf('z'));

        assertEquals(6, sb.lastIndexOf('w'));
        assertEquals(6, sb.lastIndexOf('w', 10));
        assertEquals(-1, sb.lastIndexOf('w', 4));
        assertEquals(-1, sb.lastIndexOf('z'));

        assertEquals(0, sb.lastIndexOf("hello"));
        assertEquals(0, sb.lastIndexOf("hello", 5));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(-1, sb.indexOf((String) null));

        assertEquals(0, sb.indexOf(StrMatcher.stringMatcher("hello"), 0));
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        assertEquals(12, sb.lastIndexOf(StrMatcher.stringMatcher("hello"), 15));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        assertFalse(sb.contains((StrMatcher) null));
    }

    @Test
    public void testCharSequenceAndMiscMethods() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ell", sb.subSequence(1, 4).toString());
        assertEquals("hello", sb.toStringBuffer().toString());

        assertTrue(sb.size() == 5);
        assertFalse(sb.isEmpty());
        sb.clear();
        assertTrue(sb.isEmpty());
        assertEquals(0, sb.size());

        sb.setNullText("null");
        assertEquals("null", sb.getNullText());
    }

    @Test
    public void testReadFrom_readerInput() throws Exception {
        StrBuilder sb = new StrBuilder();
        int read = sb.readFrom(new StringReader("sample data"));
        assertEquals(11, read);
        assertEquals("sample data", sb.toString());
    }

    @Test
    public void testAsWriter_allMethods() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write('A');
        writer.write("BCDE", 1, 3);
        writer.write(new char[]{'F', 'G'});
        assertEquals("ACDEFG", sb.toString());
    }

    @Test
    public void testAsReader_skipAndBounds() throws Exception {
        StrBuilder sb = new StrBuilder("0123456789");
        Reader reader = sb.asReader();
        assertEquals(4, reader.skip(4));
        assertEquals('4', reader.read());
        assertEquals(-1, reader.skip(-1));

        char[] buf = new char[10];
        int count = reader.read(buf, 0, 10);
        assertEquals(5, count);
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read(buf, 0, 5));
        assertEquals(0, reader.read(buf, 0, 0));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAt_outOfBounds_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setCharAt(5, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAt_outOfBounds_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_outOfBounds_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(5, "x");
    }
}