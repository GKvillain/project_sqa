package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class StrBuilderTest {

    // Tests indexOf when target string exists only in residual buffer after deletion (Defects4J Lang-61)
    @Test
    public void testIndexOf_residualBufferAfterDelete_returnsNotFound() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.delete(5, 11);
        assertEquals(-1, sb.indexOf("world"));
        assertEquals(-1, sb.indexOf("world", 0));
    }

    // Tests deleteAll when called repeatedly on residual buffer (Defects4J Lang-61)
    @Test
    public void testDeleteAll_repeatedDelete_doesNotThrowException() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.deleteAll("world");
        assertEquals("hello ", sb.toString());
        sb.deleteAll("world");
        assertEquals("hello ", sb.toString());
    }

    // Tests constructors and capacity management
    @Test
    public void testConstructorsAndCapacity_variousInputs_maintainsExpectedCapacityAndSize() {
        StrBuilder sbDefault = new StrBuilder();
        assertEquals(0, sbDefault.length());
        assertEquals(StrBuilder.CAPACITY, sbDefault.capacity());
        assertTrue(sbDefault.isEmpty());

        StrBuilder sbCapacity = new StrBuilder(-10);
        assertEquals(StrBuilder.CAPACITY, sbCapacity.capacity());

        StrBuilder sbString = new StrBuilder("test");
        assertEquals(4, sbString.length());
        assertEquals(4 + StrBuilder.CAPACITY, sbString.capacity());

        StrBuilder sbNull = new StrBuilder((String) null);
        assertEquals(0, sbNull.length());

        sbString.ensureCapacity(100);
        assertTrue(sbString.capacity() >= 100);

        sbString.minimizeCapacity();
        assertEquals(4, sbString.capacity());

        sbString.clear();
        assertEquals(0, sbString.size());
        assertTrue(sbString.isEmpty());
    }

    // Tests setLength with positive expansion and truncation
    @Test
    public void testSetLength_expandAndTruncate_handlesCorrectly() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setLength(2);
        assertEquals("he", sb.toString());
        assertEquals(2, sb.length());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals('h', sb.charAt(0));
        assertEquals('e', sb.charAt(1));
        assertEquals('\0', sb.charAt(2));
    }

    // Tests setLength with negative index expecting exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLength_negativeLength_throwsException() {
        StrBuilder sb = new StrBuilder();
        sb.setLength(-1);
    }

    // Tests append methods for various data types and null handling
    @Test
    public void testAppend_variousTypes_appendsExpectedStrings() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.setNewLineText("\n");

        sb.append((Object) null)
          .append("abc")
          .append((String) null)
          .append(new StringBuffer("def"))
          .append(new StrBuilder("ghi"))
          .append(new char[]{'j', 'k'})
          .append(true)
          .append(false)
          .append('!')
          .append(123)
          .append(456L)
          .append(1.5f)
          .append(2.5d)
          .appendNewLine();

        String expected = "<null>abc<null>defghijkltruefalse!1234561.52.5\n";
        assertEquals(expected, sb.toString());
    }

    // Tests partial append methods with offsets and lengths
    @Test
    public void testAppend_substringAndArrayRanges_appendsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.append("abcdef", 1, 3);
        sb.append(new StringBuffer("ghijkl"), 1, 3);
        sb.append(new StrBuilder("mnopqr"), 1, 3);
        sb.append(new char[]{'s', 't', 'u', 'v'}, 1, 2);

        assertEquals("bchijktu", sb.toString());
    }

    // Tests appendWithSeparators for arrays, collections, and iterators
    @Test
    public void testAppendWithSeparators_collectionAndArray_addsSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y"), "-");
        assertEquals("X-Y", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.singletonList("Z").iterator(), ":");
        assertEquals("Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());
    }

    // Tests fixed width padding on left and right
    @Test
    public void testAppendFixedWidthPad_leftAndRight_padsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 5, ' ');
        assertEquals("  foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("toolongstring", 4, ' ');
        assertEquals("ring", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("bar", 5, ' ');
        assertEquals("bar  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 5, '0');
        assertEquals("42000", sb.toString());
    }

    // Tests insert methods at specific index
    @Test
    public void testInsert_variousTypes_insertsAtCorrectIndex() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, "start-");
        assertEquals("start-abc", sb.toString());

        sb.insert(sb.length(), true);
        assertEquals("start-abctrue", sb.toString());

        sb.insert(0, new char[]{'1', '2', '3'}, 0, 2);
        assertEquals("12start-abctrue", sb.toString());
    }

    // Tests deletion methods by range, char, and string
    @Test
    public void testDelete_rangeAndChars_removesExpectedContent() {
        StrBuilder sb = new StrBuilder("a-b-c-d-b-e");
        sb.deleteCharAt(1);
        assertEquals("ab-c-d-b-e", sb.toString());

        sb.deleteFirst('b');
        assertEquals("a-c-d-b-e", sb.toString());

        sb.deleteAll('-');
        assertEquals("acdbe", sb.toString());

        sb.deleteFirst("cd");
        assertEquals("abe", sb.toString());

        sb.delete(1, 2);
        assertEquals("ae", sb.toString());
    }

    // Tests replace methods by char, string, and ranges
    @Test
    public void testReplace_stringAndCharAndRange_replacesContentCorrectly() {
        StrBuilder sb = new StrBuilder("banana");
        sb.replaceFirst('a', 'o');
        assertEquals("bonana", sb.toString());

        sb.replaceAll('a', 'o');
        assertEquals("bonono", sb.toString());

        sb.replaceFirst("on", "xx");
        assertEquals("bxxono", sb.toString());

        sb.replaceAll("o", "a");
        assertEquals("bxxana", sb.toString());

        sb.replace(0, 3, "c");
        assertEquals("cana", sb.toString());
    }

    // Tests search and replace using StrMatcher
    @Test
    public void testReplaceAndSearch_withStrMatcher_matchesAndReplacesCorrectly() {
        StrBuilder sb = new StrBuilder("a1b2c3");
        StrMatcher digitMatcher = new StrMatcher() {
            public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
                return Character.isDigit(buffer[pos]) ? 1 : 0;
            }
        };

        assertTrue(sb.contains(digitMatcher));
        assertEquals(1, sb.indexOf(digitMatcher));
        assertEquals(5, sb.lastIndexOf(digitMatcher));

        sb.replaceFirst(digitMatcher, "-");
        assertEquals("a-b2c3", sb.toString());

        sb.replaceAll(digitMatcher, "_");
        assertEquals("a-b_c_", sb.toString());

        sb.deleteAll(digitMatcher);
        assertEquals("a-b_c_", sb.toString());
    }

    // Tests reverse, trim, startsWith, and endsWith
    @Test
    public void testReverseAndTrimAndPrefixSuffix_validContent_behavesExpectedly() {
        StrBuilder sb = new StrBuilder("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());

        assertTrue(sb.startsWith("he"));
        assertFalse(sb.startsWith("world"));
        assertTrue(sb.endsWith("lo"));
        assertFalse(sb.endsWith("he"));

        sb.reverse();
        assertEquals("olleh", sb.toString());
    }

    // Tests substring extractions
    @Test
    public void testSubstrings_leftRightMidAndRange_returnsExpectedStrings() {
        StrBuilder sb = new StrBuilder("abcdefgh");

        assertEquals("abcd", sb.substring(0, 4));
        assertEquals("efgh", sb.substring(4));
        assertEquals("abc", sb.leftString(3));
        assertEquals("fgh", sb.rightString(3));
        assertEquals("cde", sb.midString(2, 3));

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.midString(20, 3));
    }

    // Tests contains, indexOf, and lastIndexOf for chars and strings
    @Test
    public void testContainsAndIndexOf_charAndString_locatesPositions() {
        StrBuilder sb = new StrBuilder("foobarbazfoo");

        assertTrue(sb.contains('b'));
        assertFalse(sb.contains((char) ('z' + 1)));
        assertTrue(sb.contains("bar"));
        assertFalse(sb.contains("xyz"));

        assertEquals(3, sb.indexOf('b'));
        assertEquals(6, sb.indexOf('b', 4));
        assertEquals(-1, sb.indexOf('q'));

        assertEquals(0, sb.indexOf("foo"));
        assertEquals(9, sb.indexOf("foo", 1));
        assertEquals(-1, sb.indexOf("notfound"));

        assertEquals(9, sb.lastIndexOf("foo"));
        assertEquals(0, sb.lastIndexOf("foo", 8));
        assertEquals(6, sb.lastIndexOf('b'));
        assertEquals(3, sb.lastIndexOf('b', 5));
    }

    // Tests toCharArray and getChars operations
    @Test
    public void testToCharArrayAndGetChars_validRanges_copiesContent() {
        StrBuilder sb = new StrBuilder("hello");
        char[] chars = sb.toCharArray();
        assertTrue(Arrays.equals(new char[]{'h', 'e', 'l', 'l', 'o'}, chars));

        char[] subChars = sb.toCharArray(1, 4);
        assertTrue(Arrays.equals(new char[]{'e', 'l', 'l'}, subChars));

        char[] dest = new char[5];
        sb.getChars(0, 5, dest, 0);
        assertTrue(Arrays.equals(new char[]{'h', 'e', 'l', 'l', 'o'}, dest));
    }

    // Tests Reader and Writer views
    @Test
    public void testAsReaderAndAsWriter_streamOperations_readsAndWritesBuffer() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write("Hello");
        writer.write(' ');
        writer.write("World".toCharArray());
        assertEquals("Hello World", sb.toString());

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('H', reader.read());

        char[] cbuf = new char[4];
        int count = reader.read(cbuf, 0, 4);
        assertEquals(4, count);
        assertEquals("ello", new String(cbuf));

        reader.close();
        writer.close();
    }

    // Tests StrTokenizer view
    @Test
    public void testAsTokenizer_spaceDelimited_tokensExtracted() {
        StrBuilder sb = new StrBuilder("one two three");
        StrTokenizer tok = sb.asTokenizer();
        assertEquals("one", tok.next());
        assertEquals("two", tok.next());
        assertEquals("three", tok.next());
        assertFalse(tok.hasNext());
    }

    // Tests equals, equalsIgnoreCase, and hashCode
    @Test
    public void testEqualsAndHashCode_sameAndDifferentBuilders_behavesCorrectly() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("ABC");

        assertEquals(sb1, sb2);
        assertFalse(sb1.equals(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertFalse(sb1.equals("abc"));
        assertEquals("abc", sb1.toStringBuffer().toString());
    }

    // Tests charAt with invalid index expecting exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_outOfBounds_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(5);
    }

    // Tests insert with invalid index expecting exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_invalidIndex_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(10, "invalid");
    }

    // Tests appendAll and appendln methods
    @Test
    public void testAppendAllAndAppendln_variousInputs_appendsExpectedValues() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new Object[]{"a", "b"});
        sb.appendAll(Arrays.asList("c", "d"));
        sb.appendAll((Iterator) Arrays.asList("e", "f").iterator());
        assertEquals("abcdef", sb.toString());

        sb.clear();
        sb.setNewLineText("\n");
        sb.appendln("line1");
        sb.appendln(new StringBuffer("line2"));
        sb.appendln(new StrBuilder("line3"));
        sb.appendln(new char[]{'l', '4'});
        sb.appendln(true);
        sb.appendln('X');
        sb.appendln(10);
        sb.appendln(20L);
        sb.appendln(1.5f);
        sb.appendln(2.5d);
        sb.appendln((Object) "obj");

        String result = sb.toString();
        assertTrue(result.startsWith("line1\nline2\nline3\nl4\ntrue\nX\n10\n20\n1.5\n2.5\nobj\n"));
    }

    // Tests appendSeparator variations
    @Test
    public void testAppendSeparator_variousSeparators_addsSeparatorsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());

        sb.append("A");
        sb.appendSeparator(",");
        sb.append("B");
        assertEquals("A,B", sb.toString());

        sb.appendSeparator('-', "default");
        sb.append("C");
        assertEquals("A,B-C", sb.toString());

        sb.clear();
        sb.appendSeparator('-', "default");
        assertEquals("default", sb.toString());

        sb.clear();
        sb.appendSeparator(',', 0);
        assertEquals("", sb.toString());
        sb.appendSeparator(',', 1);
        assertEquals(",", sb.toString());
    }

    // Tests setCharAt and getChars destinations
    @Test
    public void testSetCharAtAndGetChars_validOperations_modifiesAndCopiesBuffer() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setCharAt(0, 'H');
        assertEquals("Hello", sb.toString());

        char[] dest = new char[5];
        sb.getChars(dest);
        assertTrue(Arrays.equals(new char[]{'H', 'e', 'l', 'l', 'o'}, dest));
    }

    // Tests clone and equals edge cases
    @Test
    public void testCloneAndEquals_edgeCases_clonesAndComparesCorrectly() throws Exception {
        StrBuilder sb1 = new StrBuilder("test");
        StrBuilder sb2 = (StrBuilder) sb1.clone();

        assertEquals(sb1, sb2);
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals((Object) null));
        assertFalse(sb1.equalsIgnoreCase(null));
        assertTrue(sb1.equalsIgnoreCase(sb2));

        sb2.append("more");
        assertFalse(sb1.equals(sb2));
        assertFalse(sb1.equalsIgnoreCase(sb2));
    }
}