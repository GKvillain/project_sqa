package org.apache.commons.lang.text;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit 4 test class for StrBuilder targeting Defects4J bug 60b and core functionality.
 */
public class StrBuilderTest {

    // ========== Constructor tests ==========

    @Test
    public void testConstructor_default_capacity32() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
    }

    @Test
    public void testConstructor_negativeCapacity_usesDefault() {
        StrBuilder sb = new StrBuilder(-10);
        assertEquals(32, sb.capacity());
    }

    @Test
    public void testConstructor_stringNull_createsEmpty() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals(0, sb.length());
        assertEquals(32, sb.capacity());
    }

    @Test
    public void testConstructor_stringValid_copiesContent() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.toString());
        assertEquals(3, sb.length());
    }

    // ========== contains(char) tests (targeting Defects4J bug 60) ==========

    @Test
    public void testContains_char_present_returnsTrue() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.contains('e'));
    }

    @Test
    public void testContains_char_absent_returnsFalse() {
        StrBuilder sb = new StrBuilder("hello");
        assertFalse(sb.contains('z'));
    }

    @Test
    public void testContains_char_emptyBuilder_returnsFalse() {
        StrBuilder sb = new StrBuilder();
        assertFalse(sb.contains('a'));
    }

    // Bug trigger: buffer larger than size, null chars in unused buffer positions
    @Test
    public void testContains_char_bug60_unusedBufferContainsNull_returnsFalse() {
        StrBuilder sb = new StrBuilder(10);
        sb.append("hi");          // size = 2, buffer length = 10 (or more)
        // buffer[2..9] are '\0'
        // contains(char) should not search beyond size
        assertFalse("Bug: contains('\\0') should be false", sb.contains('\0'));
    }

    @Test
    public void testContains_char_afterAppendAndDelete_returnsCorrect() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(2, 4);          // now "abef", size=4
        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('c'));
    }

    // ========== append tests ==========

    @Test
    public void testAppend_stringNull_usesNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.append((String) null);
        assertEquals("NULL", sb.toString());
    }

    @Test
    public void testAppend_stringEmpty_noChange() {
        StrBuilder sb = new StrBuilder("a");
        sb.append("");
        assertEquals("a", sb.toString());
    }

    @Test
    public void testAppend_booleanTrue_appendsTrue() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());
    }

    @Test
    public void testAppend_booleanFalse_appendsFalse() {
        StrBuilder sb = new StrBuilder();
        sb.append(false);
        assertEquals("false", sb.toString());
    }

    @Test
    public void testAppend_charArray_appendsContent() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c'});
        assertEquals("abc", sb.toString());
    }

    // ========== insert tests ==========

    @Test
    public void testInsert_stringAtBeginning() {
        StrBuilder sb = new StrBuilder("bc");
        sb.insert(0, "a");
        assertEquals("abc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_negativeIndex_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_indexGreaterThanSize_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(4, "x");
    }

    // ========== delete tests ==========

    @Test
    public void testDelete_range_valid() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(2, 4);
        assertEquals("abef", sb.toString());
    }

    @Test
    public void testDeleteAll_char_multipleOccurrences() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteAll('a');
        assertEquals("bcd", sb.toString());
    }

    @Test
    public void testDeleteFirst_char_oneRemoved() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteFirst('a');
        assertEquals("bacad", sb.toString());
    }

    @Test
    public void testDeleteAll_string_multiple() {
        StrBuilder sb = new StrBuilder("xabxxabxx");
        sb.deleteAll("ab");
        assertEquals("xxxxx", sb.toString());
    }

    // ========== replace tests ==========

    @Test
    public void testReplace_range_replacesContent() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.replace(2, 4, "XYZ");   // replace "cd" with "XYZ"
        assertEquals("abXYZef", sb.toString());
    }

    @Test
    public void testReplaceAll_char_toDifferentChar() {
        StrBuilder sb = new StrBuilder("aabbaa");
        sb.replaceAll('a', 'b');
        assertEquals("bbbbbb", sb.toString());
    }

    @Test
    public void testReplaceFirst_char() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.replaceFirst('a', 'z');
        assertEquals("zbcabc", sb.toString());
    }

    // ========== startsWith / endsWith tests ==========

    @Test
    public void testStartsWith_null_returnsFalse() {
        StrBuilder sb = new StrBuilder("test");
        assertFalse(sb.startsWith(null));
    }

    @Test
    public void testStartsWith_matchingPrefix_returnsTrue() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.startsWith("he"));
    }

    @Test
    public void testEndsWith_matchingSuffix_returnsTrue() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.endsWith("lo"));
    }

    // ========== substring / leftString / rightString / midString tests ==========

    @Test
    public void testSubstring_range_returnsSubstring() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("ello", sb.substring(1, 5));
    }

    @Test
    public void testLeftString_negative_returnsEmpty() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("", sb.leftString(-1));
    }

    @Test
    public void testRightString_negative_returnsEmpty() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("", sb.rightString(-1));
    }

    @Test
    public void testMidString_indexNegative_usesZero() {
        StrBuilder sb = new StrBuilder("abcde");
        assertEquals("abc", sb.midString(-2, 3));
    }

    // ========== reverse / trim tests ==========

    @Test
    public void testReverse_empty_doesNothing() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test
    public void testReverse_nonEmpty_reverses() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.reverse();
        assertEquals("dcba", sb.toString());
    }

    @Test
    public void testTrim_spacesOnBothEnds_trims() {
        StrBuilder sb = new StrBuilder("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    // ========== equals / hashCode / toString ==========

    @Test
    public void testEquals_sameContent_returnsTrue() {
        StrBuilder a = new StrBuilder("abc");
        StrBuilder b = new StrBuilder("abc");
        assertTrue(a.equals(b));
    }

    @Test
    public void testEquals_differentContent_returnsFalse() {
        StrBuilder a = new StrBuilder("abc");
        StrBuilder b = new StrBuilder("xyz");
        assertFalse(a.equals(b));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        StrBuilder a = new StrBuilder("abc");
        StrBuilder b = new StrBuilder("abc");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString_returnsContent() {
        StrBuilder sb = new StrBuilder("content");
        assertEquals("content", sb.toString());
    }

    // ========== setNullText edge case ==========
    @Test
    public void testSetNullText_emptyString_setsToNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("");
        assertNull(sb.getNullText());  // empty string is converted to null
    }

    // ========== appendFixedWidthPadRight (potential bug area) ==========
    @Test
    public void testAppendFixedWidthPadRight_shorterString_pads() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("ab", 5, '-');
        assertEquals("ab---", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight_longerString_truncatesRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abcdef", 3, '-');
        assertEquals("abc", sb.toString());
    }

    // ========== ensureCapacity and minimizeCapacity ==========
    @Test
    public void testEnsureCapacity_expandsBuffer() {
        StrBuilder sb = new StrBuilder(2);
        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);
    }

    @Test
    public void testMinimizeCapacity_reducesToSize() {
        StrBuilder sb = new StrBuilder(100);
        sb.append("hello");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    // ========== Additional tests to improve coverage ==========

    // Tests for contains(String)
    @Test
    public void testContainsString_null_returnsFalse() {
        StrBuilder sb = new StrBuilder("test");
        assertFalse(sb.contains((String) null));
    }

    @Test
    public void testContainsString_empty_returnsTrue() {
        StrBuilder sb = new StrBuilder("test");
        assertTrue(sb.contains(""));
    }

    @Test
    public void testContainsString_present_returnsTrue() {
        StrBuilder sb = new StrBuilder("hello world");
        assertTrue(sb.contains("world"));
    }

    @Test
    public void testContainsString_absent_returnsFalse() {
        StrBuilder sb = new StrBuilder("hello");
        assertFalse(sb.contains("world"));
    }

    // Tests for append(String, int, int)
    @Test
    public void testAppendStringSubstring_validRange() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello world", 6, 5);
        assertEquals("world", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubstring_invalidStart() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello", -1, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubstring_invalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello", 0, 10);
    }

    // Tests for append(StringBuffer)
    @Test
    public void testAppendStringBuffer_nonNull() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("test"));
        assertEquals("test", sb.toString());
    }

    @Test
    public void testAppendStringBuffer_null() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.append((StringBuffer) null);
        assertEquals("NULL", sb.toString());
    }

    // Tests for append(char[], int, int)
    @Test
    public void testAppendCharArraySubset_valid() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals("bc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubset_invalidOffset() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubset_invalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b'}, 0, 5);
    }

    // Tests for append(char) and append(int)
    @Test
    public void testAppendChar_single() {
        StrBuilder sb = new StrBuilder();
        sb.append('x');
        assertEquals("x", sb.toString());
    }

    @Test
    public void testAppendInt_positive() {
        StrBuilder sb = new StrBuilder();
        sb.append(123);
        assertEquals("123", sb.toString());
    }

    @Test
    public void testAppendInt_negative() {
        StrBuilder sb = new StrBuilder();
        sb.append(-456);
        assertEquals("-456", sb.toString());
    }

    // Tests for append(long)
    @Test
    public void testAppendLong_positive() {
        StrBuilder sb = new StrBuilder();
        sb.append(123456789L);
        assertEquals("123456789", sb.toString());
    }

    @Test
    public void testAppendLong_negative() {
        StrBuilder sb = new StrBuilder();
        sb.append(-987654321L);
        assertEquals("-987654321", sb.toString());
    }

    // Tests for append(float) and append(double)
    @Test
    public void testAppendFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(3.14f);
        assertEquals("3.14", sb.toString());
    }

    @Test
    public void testAppendDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.71828);
        assertEquals("2.71828", sb.toString());
    }

    // Tests for appendln methods
    @Test
    public void testAppendlnString() {
        StrBuilder sb = new StrBuilder();
        sb.appendln("hello");
        assertEquals("hello" + System.lineSeparator(), sb.toString());
    }

    @Test
    public void testAppendlnBoolean() {
        StrBuilder sb = new StrBuilder();
        sb.appendln(true);
        assertEquals("true" + System.lineSeparator(), sb.toString());
    }

    // Tests for appendFixedWidthPadLeft
    @Test
    public void testAppendFixedWidthPadLeft_shorterString_pads() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("ab", 5, '-');
        assertEquals("---ab", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_longerString_truncatesLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abcdef", 3, '-');
        assertEquals("def", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_nullUsesNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.appendFixedWidthPadLeft(null, 6, '-');
        assertEquals("--NULL", sb.toString());
    }

    // Tests for insert with various types
    @Test
    public void testInsertBoolean_true() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, true);
        assertEquals("atruebc", sb.toString());
    }

    @Test
    public void testInsertChar() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(2, 'Z');
        assertEquals("abZc", sb.toString());
    }

    @Test
    public void testInsertCharArray() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, new char[]{'X', 'Y'});
        assertEquals("aXYbc", sb.toString());
    }

    @Test
    public void testInsertCharArraySubset() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, new char[]{'X', 'Y', 'Z'}, 1, 2);
        assertEquals("aYZbc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubset_invalidOffset() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, new char[]{'X', 'Y'}, -1, 1);
    }

    // Tests for deleteCharAt
    @Test
    public void testDeleteCharAt_valid() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(1);
        assertEquals("ac", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAt_negative() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAt_tooLarge() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(3);
    }

    // Tests for deleteFirst(String)
    @Test
    public void testDeleteFirstString_present() {
        StrBuilder sb = new StrBuilder("xabxabx");
        sb.deleteFirst("ab");
        assertEquals("xxabx", sb.toString());
    }

    @Test
    public void testDeleteFirstString_absent() {
        StrBuilder sb = new StrBuilder("hello");
        sb.deleteFirst("xyz");
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testDeleteFirstString_null() {
        StrBuilder sb = new StrBuilder("test");
        sb.deleteFirst((String) null);
        assertEquals("test", sb.toString());
    }

    // Tests for replaceAll(String, String)
    @Test
    public void testReplaceAllString_present() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceAll("ab", "X");
        assertEquals("XXX", sb.toString());
    }

    @Test
    public void testReplaceAllString_nullSearch() {
        StrBuilder sb = new StrBuilder("test");
        sb.replaceAll(null, "X");
        assertEquals("test", sb.toString());
    }

    @Test
    public void testReplaceAllString_emptySearch() {
        StrBuilder sb = new StrBuilder("test");
        sb.replaceAll("", "X");
        assertEquals("test", sb.toString());
    }

    // Tests for replaceFirst(String, String)
    @Test
    public void testReplaceFirstString_present() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceFirst("ab", "X");
        assertEquals("Xabab", sb.toString());
    }

    @Test
    public void testReplaceFirstString_absent() {
        StrBuilder sb = new StrBuilder("test");
        sb.replaceFirst("xyz", "X");
        assertEquals("test", sb.toString());
    }

    // Tests for indexOf / lastIndexOf
    @Test
    public void testIndexOfChar_found() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(2, sb.indexOf('l'));
    }

    @Test
    public void testIndexOfChar_notFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf('z'));
    }

    @Test
    public void testIndexOfChar_startIndex() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(3, sb.indexOf('l', 3));
    }

    @Test
    public void testLastIndexOfChar_found() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(3, sb.lastIndexOf('l'));
    }

    @Test
    public void testLastIndexOfChar_notFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.lastIndexOf('z'));
    }

    @Test
    public void testIndexOfString_found() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals(6, sb.indexOf("world"));
    }

    @Test
    public void testIndexOfString_notFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf("xyz"));
    }

    @Test
    public void testLastIndexOfString_found() {
        StrBuilder sb = new StrBuilder("ababab");
        assertEquals(4, sb.lastIndexOf("ab"));
    }

    @Test
    public void testLastIndexOfString_notFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.lastIndexOf("xyz"));
    }

    // Tests for substring(int)
    @Test
    public void testSubstring_startIndex() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ello", sb.substring(1));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstring_startIndexNegative() {
        StrBuilder sb = new StrBuilder("hello");
        sb.substring(-1);
    }

    // Tests for leftString, rightString, midString edge cases
    @Test
    public void testLeftString_zero() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("", sb.leftString(0));
    }

    @Test
    public void testLeftString_exceedsLength() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.leftString(10));
    }

    @Test
    public void testRightString_zero() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("", sb.rightString(0));
    }

    @Test
    public void testRightString_exceedsLength() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.rightString(10));
    }

    @Test
    public void testMidString_lengthExceeds() {
        StrBuilder sb = new StrBuilder("abcde");
        assertEquals("cde", sb.midString(2, 10));
    }

    // Tests for asReader and asWriter
    @Test
    public void testAsReader_read() throws Exception {
        StrBuilder sb = new StrBuilder("hello");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[5];
        assertEquals(5, reader.read(buf));
        assertEquals("hello", new String(buf));
    }

    @Test
    public void testAsWriter_write() {
        StrBuilder sb = new StrBuilder();
        java.io.Writer writer = sb.asWriter();
        try {
            writer.write("test");
        } catch (java.io.IOException e) {
            fail("IOException unexpected");
        }
        assertEquals("test", sb.toString());
    }

    // Tests for asTokenizer
    @Test
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tokenizer = sb.asTokenizer();
        assertArrayEquals(new String[]{"a", "b", "c"}, tokenizer.getTokenArray());
    }

    // Tests for getNullText
    @Test
    public void testGetNullText_default() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
    }

    @Test
    public void testGetNullText_set() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
    }

    // Tests for length and capacity
    @Test
    public void testLength_afterAppend() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(5, sb.length());
    }

    @Test
    public void testCapacity_initial() {
        StrBuilder sb = new StrBuilder(50);
        assertEquals(50, sb.capacity());
    }

    // Tests for isEmpty
    @Test
    public void testIsEmpty_empty() {
        StrBuilder sb = new StrBuilder();
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testIsEmpty_notEmpty() {
        StrBuilder sb = new StrBuilder("test");
        assertFalse(sb.isEmpty());
    }

    // Tests for charAt
    @Test
    public void testCharAt_valid() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals('e', sb.charAt(1));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_negative() {
        StrBuilder sb = new StrBuilder("hello");
        sb.charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_tooLarge() {
        StrBuilder sb = new StrBuilder("hello");
        sb.charAt(5);
    }

    // Tests for setCharAt
    @Test
    public void testSetCharAt_valid() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setCharAt(1, 'a');
        assertEquals("hallo", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAt_negative() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setCharAt(-1, 'x');
    }

    // Tests for getChars
    @Test
    public void testGetChars_valid() {
        StrBuilder sb = new StrBuilder("hello");
        char[] dst = new char[5];
        sb.getChars(0, 5, dst, 0);
        assertArrayEquals(new char[]{'h', 'e', 'l', 'l', 'o'}, dst);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetChars_invalidRange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.getChars(0, 10, new char[10], 0);
    }

    // Tests for deleteAll(String) with null
    @Test
    public void testDeleteAllString_null() {
        StrBuilder sb = new StrBuilder("test");
        sb.deleteAll((String) null);
        assertEquals("test", sb.toString());
    }

    // Tests for replace(int, int, String) edge cases
    @Test
    public void testReplace_sameLength() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.replace(1, 3, "XY");
        assertEquals("aXYd", sb.toString());
    }

    @Test
    public void testReplace_shorterReplacement() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.replace(1, 3, "Z");
        assertEquals("aZd", sb.toString());
    }

    @Test
    public void testReplace_longerReplacement() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.replace(1, 2, "XYZ");
        assertEquals("aXYZcd", sb.toString());
    }

    // Tests for replace with StrMatcher
    @Test
    public void testReplaceAllStrMatcher() {
        StrBuilder sb = new StrBuilder("a1b2c3");
        sb.replaceAll(StrMatcher.charSetMatcher("0123456789"), "X");
        assertEquals("aXbXcX", sb.toString());
    }

    @Test
    public void testReplaceFirstStrMatcher() {
        StrBuilder sb = new StrBuilder("a1b2c3");
        sb.replaceFirst(StrMatcher.charSetMatcher("0123456789"), "X");
        assertEquals("aXb2c3", sb.toString());
    }

    // Tests for reverse edge cases
    @Test
    public void testReverse_singleChar() {
        StrBuilder sb = new StrBuilder("a");
        sb.reverse();
        assertEquals("a", sb.toString());
    }

    // Tests for trim edge cases
    @Test
    public void testTrim_noSpaces() {
        StrBuilder sb = new StrBuilder("hello");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testTrim_allSpaces() {
        StrBuilder sb = new StrBuilder("   ");
        sb.trim();
        assertEquals("", sb.toString());
    }

    // Tests for ensureCapacity with smaller value
    @Test
    public void testEnsureCapacity_noChangeNeeded() {
        StrBuilder sb = new StrBuilder(50);
        sb.ensureCapacity(10);
        assertEquals(50, sb.capacity());
    }

    // Tests for minimizeCapacity with empty
    @Test
    public void testMinimizeCapacity_empty() {
        StrBuilder sb = new StrBuilder(100);
        sb.minimizeCapacity();
        assertTrue(sb.capacity() >= 32); // default minimum
    }

    // Additional equals tests
    @Test
    public void testEquals_sameObject() {
        StrBuilder sb = new StrBuilder("test");
        assertTrue(sb.equals(sb));
    }

    @Test
    public void testEquals_differentType() {
        StrBuilder sb = new StrBuilder("test");
        assertFalse(sb.equals("test"));
    }

    @Test
    public void testEquals_null() {
        StrBuilder sb = new StrBuilder("test");
        assertFalse(sb.equals(null));
    }

    // Tests for appendAll with Iterable
    @Test
    public void testAppendAll_Iterable() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = java.util.Arrays.asList("a", "b", "c");
        sb.appendAll(list);
        assertEquals("abc", sb.toString());
    }

    // Tests for appendWithSeparators
    @Test
    public void testAppendWithSeparators_array() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test
    public void testAppendWithSeparators_Iterable() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = java.util.Arrays.asList("x", "y");
        sb.appendWithSeparators(list, "-");
        assertEquals("x-y", sb.toString());
    }

    @Test
    public void testAppendWithSeparators_nullArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());
    }

    // Tests for appendPadding
    @Test
    public void testAppendPadding_positive() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(5, '*');
        assertEquals("*****", sb.toString());
    }

    @Test
    public void testAppendPadding_zero() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(0, '*');
        assertEquals("", sb.toString());
    }

    // Tests for hashCode with empty builder
    @Test
    public void testHashCode_empty() {
        StrBuilder a = new StrBuilder();
        StrBuilder b = new StrBuilder();
        assertEquals(a.hashCode(), b.hashCode());
    }
}