package org.apache.commons.lang.text;

import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class StrBuilderTest {

    // Tests defect Lang-47: appendFixedWidthPadLeft with null object when nullText is null
    @Test
    public void testAppendFixedWidthPadLeft_nullObjectNullText_padsSpaces() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 5, ' ');
        assertEquals("     ", sb.toString());
    }

    // Tests defect Lang-47: appendFixedWidthPadRight with null object when nullText is null
    @Test
    public void testAppendFixedWidthPadRight_nullObjectNullText_padsSpaces() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 5, ' ');
        assertEquals("     ", sb.toString());
    }

    // Tests constructors and capacity management
    @Test
    public void testConstructorsAndCapacity_variousInitializations_expectedCapacityAndSize() {
        StrBuilder sbDefault = new StrBuilder();
        assertEquals(32, sbDefault.capacity());
        assertEquals(0, sbDefault.length());
        assertTrue(sbDefault.isEmpty());

        StrBuilder sbCapacity = new StrBuilder(-5);
        assertEquals(32, sbCapacity.capacity());

        StrBuilder sbStr = new StrBuilder("hello");
        assertEquals(37, sbStr.capacity());
        assertEquals(5, sbStr.length());
        assertEquals("hello", sbStr.toString());

        sbStr.ensureCapacity(100);
        assertTrue(sbStr.capacity() >= 100);

        sbStr.minimizeCapacity();
        assertEquals(5, sbStr.capacity());
    }

    // Tests setLength, clear, and isEmpty
    @Test
    public void testSetLengthAndClear_variousLengths_modifiesBufferAndSize() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.setLength(5);
        assertEquals("hello", sb.toString());
        assertEquals(5, sb.length());

        sb.setLength(8);
        assertEquals(8, sb.length());
        assertEquals("hello\0\0\0", sb.toString());

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    // Tests setLength with negative length expecting exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLength_negativeLength_throwsException() {
        StrBuilder sb = new StrBuilder();
        sb.setLength(-1);
    }

    // Tests char access and manipulation
    @Test
    public void testCharAtAndSetCharAt_validInputs_modifiesCharacter() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals('a', sb.charAt(0));
        assertEquals('f', sb.charAt(5));

        sb.setCharAt(0, 'z');
        assertEquals('z', sb.charAt(0));

        sb.deleteCharAt(1);
        assertEquals("zcdef", sb.toString());
    }

    // Tests charAt with invalid index expecting exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_indexOutOfBounds_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(5);
    }

    // Tests toCharArray and getChars methods
    @Test
    public void testToCharArrayAndGetChars_validRanges_copiesArrayCorrectly() {
        StrBuilder sbEmpty = new StrBuilder();
        assertArrayEquals(new char[0], sbEmpty.toCharArray());
        assertArrayEquals(new char[0], sbEmpty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("abcdef");
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, sb.toCharArray());
        assertArrayEquals(new char[]{'b', 'c', 'd'}, sb.toCharArray(1, 4));

        char[] dest = new char[6];
        sb.getChars(1, 4, dest, 2);
        assertEquals('b', dest[2]);
        assertEquals('c', dest[3]);
        assertEquals('d', dest[4]);

        char[] allChars = sb.getChars(null);
        assertEquals(6, allChars.length);
        assertEquals('a', allChars[0]);
    }

    // Tests various append and appendln methods
    @Test
    public void testAppendAndAppendln_variousTypes_appendsExpectedValues() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.setNewLineText("\n");

        sb.append((Object) null);
        sb.append("test");
        sb.append(new StringBuffer("buf"));
        sb.append(new StrBuilder("bld"));
        sb.append(new char[]{'c', 'h'});
        sb.append(true);
        sb.append(false);
        sb.append('!');
        sb.append(123);
        sb.append(456L);
        sb.append(1.5f);
        sb.append(2.5d);
        sb.appendNewLine();

        String expected = "<null>testbufbldchtruefalse!1234561.52.5\n";
        assertEquals(expected, sb.toString());

        StrBuilder sbLn = new StrBuilder();
        sbLn.setNewLineText("\n");
        sbLn.appendln("line1");
        sbLn.appendln(100);
        assertEquals("line1\n100\n", sbLn.toString());
    }

    // Tests appendAll and appendWithSeparators with arrays, collections, and iterators
    @Test
    public void testAppendAllAndWithSeparators_arraysAndCollections_formattedProperly() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new String[]{"a", "b", "c"});
        sb.appendAll(Arrays.asList("d", "e"));
        sb.appendAll(Collections.singletonList("f").iterator());
        assertEquals("abcdef", sb.toString());

        StrBuilder sbSep = new StrBuilder();
        sbSep.appendWithSeparators(new Object[]{"1", "2", "3"}, ",");
        assertEquals("1,2,3", sbSep.toString());

        StrBuilder sbColl = new StrBuilder();
        sbColl.appendWithSeparators(Arrays.asList("x", "y", "z"), "-");
        assertEquals("x-y-z", sbColl.toString());

        StrBuilder sbIter = new StrBuilder();
        sbIter.appendWithSeparators(Arrays.asList("a", "b").iterator(), ":");
        assertEquals("a:b", sbIter.toString());
    }

    // Tests appendSeparator and appendPadding methods
    @Test
    public void testAppendSeparatorAndPadding_variousConditions_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());

        sb.append("a");
        sb.appendSeparator(",");
        sb.append("b");
        sb.appendSeparator(';');
        sb.append("c");
        assertEquals("a,b;c", sb.toString());

        StrBuilder sbLoop = new StrBuilder();
        for (int i = 0; i < 3; i++) {
            sbLoop.appendSeparator(",", i);
            sbLoop.append(i);
        }
        assertEquals("0,1,2", sbLoop.toString());

        StrBuilder sbPad = new StrBuilder("x");
        sbPad.appendPadding(3, '-');
        assertEquals("x---", sbPad.toString());
    }

    // Tests appendFixedWidthPadLeft and appendFixedWidthPadRight with non-null values
    @Test
    public void testAppendFixedWidth_stringsOfDifferentLengths_padsOrTruncatesCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, '0');
        sb.appendFixedWidthPadLeft("abcdef", 4, '0');
        sb.appendFixedWidthPadLeft(42, 4, ' ');
        assertEquals("00abccdef  42", sb.toString());

        StrBuilder sbRight = new StrBuilder();
        sbRight.appendFixedWidthPadRight("abc", 5, '-');
        sbRight.appendFixedWidthPadRight("abcdef", 4, '-');
        sbRight.appendFixedWidthPadRight(42, 4, '-');
        assertEquals("abc--abcd42--", sbRight.toString());
    }

    // Tests insert methods for various data types
    @Test
    public void testInsert_variousTypesAndIndices_insertsAtSpecifiedPositions() {
        StrBuilder sb = new StrBuilder("world");
        sb.insert(0, "hello ");
        assertEquals("hello world", sb.toString());

        sb.insert(5, '!');
        assertEquals("hello! world", sb.toString());

        sb.insert(0, true);
        assertEquals("truehello! world", sb.toString());

        sb.insert(0, new char[]{'1', '2'});
        assertEquals("12truehello! world", sb.toString());

        sb.insert(2, new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals("12bctruehello! world", sb.toString());
    }

    // Tests delete and replace methods with char, string, and matcher
    @Test
    public void testDeleteAndReplace_charsAndStrings_modifiesContentsCorrectly() {
        StrBuilder sb = new StrBuilder("banana");
        sb.deleteFirst('a');
        assertEquals("bnana", sb.toString());

        sb.deleteAll('a');
        assertEquals("bnn", sb.toString());

        StrBuilder sb2 = new StrBuilder("foo bar foo baz");
        sb2.deleteFirst("foo ");
        assertEquals("bar foo baz", sb2.toString());

        sb2.deleteAll("foo ");
        assertEquals("bar baz", sb2.toString());

        StrBuilder sb3 = new StrBuilder("hello world");
        sb3.replace(6, 11, "there");
        assertEquals("hello there", sb3.toString());

        sb3.replaceAll('e', 'a');
        assertEquals("hallo thara", sb3.toString());

        sb3.replaceAll("ha", "ho");
        assertEquals("hollo thara", sb3.toString());

        sb3.replaceFirst("thara", "world");
        assertEquals("hollo world", sb3.toString());
    }

    // Tests reverse and trim operations
    @Test
    public void testTrimAndReverse_validBuilder_trimsSpacesAndReversesContent() {
        StrBuilder sb = new StrBuilder("  \t hello world  \n ");
        sb.trim();
        assertEquals("hello world", sb.toString());

        sb.reverse();
        assertEquals("dlrow olleh", sb.toString());

        StrBuilder empty = new StrBuilder();
        empty.trim();
        empty.reverse();
        assertEquals("", empty.toString());
    }

    // Tests startsWith, endsWith, and substring extraction methods
    @Test
    public void testStartsEndsWithAndSubstrings_variousInputs_expectedResults() {
        StrBuilder sb = new StrBuilder("hello world");

        assertTrue(sb.startsWith("hello"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));

        assertEquals("world", sb.substring(6));
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("hel", sb.leftString(3));
        assertEquals("rld", sb.rightString(3));
        assertEquals("lo wo", sb.midString(3, 5));
        assertEquals("", sb.midString(-1, 0));
    }

    // Tests indexOf, lastIndexOf, and contains methods
    @Test
    public void testIndexOfAndLastIndexOf_charAndString_findsCorrectIndices() {
        StrBuilder sb = new StrBuilder("banana");

        assertTrue(sb.contains('a'));
        assertTrue(sb.contains("nan"));
        assertFalse(sb.contains('z'));
        assertFalse(sb.contains("apple"));

        assertEquals(1, sb.indexOf('a'));
        assertEquals(3, sb.indexOf('a', 2));
        assertEquals(-1, sb.indexOf('z'));

        assertEquals(1, sb.indexOf("an"));
        assertEquals(3, sb.indexOf("an", 2));
        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf((String) null));

        assertEquals(5, sb.lastIndexOf('a'));
        assertEquals(3, sb.lastIndexOf('a', 4));

        assertEquals(3, sb.lastIndexOf("an"));
        assertEquals(1, sb.lastIndexOf("an", 2));
    }

    // Tests equals, equalsIgnoreCase, hashCode, toString, and toStringBuffer
    @Test
    public void testEqualsAndHashCode_sameAndDifferentObjects_consistentResults() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("hello");

        assertTrue(sb1.equals(sb1));
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals("Hello"));
        assertFalse(sb1.equals(null));

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(new StrBuilder("Hell")));

        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertEquals("Hello", sb1.toString());
        assertEquals("Hello", sb1.toStringBuffer().toString());
    }

    // Tests asReader and asWriter functionality
    @Test
    public void testAsReaderAndAsWriter_readingAndWriting_sharesInternalBuffer() throws Exception {
        StrBuilder sb = new StrBuilder("ABCDEF");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('A', reader.read());

        char[] cbuf = new char[3];
        int readCount = reader.read(cbuf, 0, 3);
        assertEquals(3, readCount);
        assertArrayEquals(new char[]{'B', 'C', 'D'}, cbuf);

        assertEquals(2, reader.skip(2));
        assertEquals(-1, reader.read());
        reader.close();

        StrBuilder sbWriter = new StrBuilder();
        Writer writer = sbWriter.asWriter();
        writer.write("Hello");
        writer.write(' ');
        writer.write(new char[]{'W', 'o', 'r', 'l', 'd'});
        writer.flush();
        writer.close();
        assertEquals("Hello World", sbWriter.toString());
    }

    // Tests validation methods with negative and invalid range
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstring_negativeIndex_throwsException() {
        StrBuilder sb = new StrBuilder("test");
        sb.substring(-1, 2);
    }
}