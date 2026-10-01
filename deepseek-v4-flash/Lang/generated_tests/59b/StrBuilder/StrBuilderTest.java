package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

public class StrBuilderTest {

    // ===== Existing tests =====

    @Test
    public void testDefaultConstructor_createsEmptyBuilder() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testConstructorWithString_initializesContent() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.toString());
    }

    @Test
    public void testAppendString_addsContent() {
        StrBuilder sb = new StrBuilder();
        sb.append("World");
        assertEquals("World", sb.toString());
    }

    @Test
    public void testAppendNull_nullTextSet_appendsNullText() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setNullText("NULL");
        sb.append((String) null);
        assertEquals("abcNULL", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_stringLongerThanWidth_truncatesLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("Hello", 3, '_');
        assertEquals("llo", sb.toString());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testAppendFixedWidthPadRight_stringLongerThanWidth_smallBuffer_throwsException() {
        StrBuilder sb = new StrBuilder(3);
        sb.appendFixedWidthPadRight("Hello", 3, '*');
    }

    @Test
    public void testAppendFixedWidthPadRight_stringShorterThanWidth_padsRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("Hi", 5, '*');
        assertEquals("Hi***", sb.toString());
    }

    @Test
    public void testAppendWithSeparators_arrayWithSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test
    public void testInsertString_validIndex_insertsCorrectly() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        sb.insert(5, " ");
        assertEquals("Hello World", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertString_indexTooLarge_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(5, "d");
    }

    @Test
    public void testDeleteRange_removesCorrectRange() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(2, 4);
        assertEquals("abef", sb.toString());
    }

    @Test
    public void testDeleteAllChar_removesAllOccurrences() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteAll('a');
        assertEquals("bcd", sb.toString());
    }

    @Test
    public void testReplaceAllString_replacesAll() {
        StrBuilder sb = new StrBuilder("aabaab");
        sb.replaceAll("aa", "b");
        assertEquals("bbbb", sb.toString());
    }

    @Test
    public void testTrim_removesLeadingAndTrailingSpaces() {
        StrBuilder sb = new StrBuilder("  Hello World  ");
        sb.trim();
        assertEquals("Hello World", sb.toString());
    }

    @Test
    public void testReverse_reversesContent() {
        StrBuilder sb = new StrBuilder("abc");
        sb.reverse();
        assertEquals("cba", sb.toString());
    }

    @Test
    public void testSubstring_validRange_returnsSubstring() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("ell", sb.substring(1, 4));
    }

    @Test
    public void testIndexOfString_findsFirstOccurrence() {
        StrBuilder sb = new StrBuilder("abac");
        assertEquals(1, sb.indexOf("ba"));
    }

    @Test
    public void testSetLength_increaseLength_padsWithNullChars() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setLength(6);
        assertEquals(6, sb.length());
        assertEquals("abc", sb.substring(0, 3));
        assertEquals('\0', sb.charAt(3));
    }

    @Test
    public void testStartsWith_matchingPrefix_returnsTrue() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.startsWith("Hello"));
    }

    @Test
    public void testEquals_sameContent_returnsTrue() {
        StrBuilder sb1 = new StrBuilder("test");
        StrBuilder sb2 = new StrBuilder("test");
        assertTrue(sb1.equals(sb2));
    }

    // ===== New tests to improve coverage =====

    @Test
    public void testConstructorWithCapacity_initializesEmpty() {
        StrBuilder sb = new StrBuilder(10);
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testAppendBoolean_true() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());
    }

    @Test
    public void testAppendBoolean_false() {
        StrBuilder sb = new StrBuilder();
        sb.append(false);
        assertEquals("false", sb.toString());
    }

    @Test
    public void testAppendChar() {
        StrBuilder sb = new StrBuilder();
        sb.append('A');
        assertEquals("A", sb.toString());
    }

    @Test
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'H', 'e', 'l', 'l', 'o'});
        assertEquals("Hello", sb.toString());
    }

    @Test
    public void testAppendCharArraySubRange() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'H', 'e', 'l', 'l', 'o'}, 1, 3);
        assertEquals("ell", sb.toString());
    }

    @Test
    public void testAppendDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(3.14);
        assertEquals("3.14", sb.toString());
    }

    @Test
    public void testAppendFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.5f);
        assertEquals("2.5", sb.toString());
    }

    @Test
    public void testAppendInt() {
        StrBuilder sb = new StrBuilder();
        sb.append(42);
        assertEquals("42", sb.toString());
    }

    @Test
    public void testAppendLong() {
        StrBuilder sb = new StrBuilder();
        sb.append(1234567890123L);
        assertEquals("1234567890123", sb.toString());
    }

    @Test
    public void testAppendObject() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) "Object");
        assertEquals("Object", sb.toString());
    }

    @Test
    public void testAppendObject_null() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        assertEquals("null", sb.toString());
    }

    @Test
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("Buffer"));
        assertEquals("Buffer", sb.toString());
    }

    @Test
    public void testAppendStrBuilder() {
        StrBuilder sb = new StrBuilder();
        StrBuilder other = new StrBuilder("Other");
        sb.append(other);
        assertEquals("Other", sb.toString());
    }

    @Test
    public void testAppendNewLine() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendNewLine();
        assertEquals("a" + System.lineSeparator(), sb.toString());
    }

    @Test
    public void testAppendNull_noNullText_appendsNull() {
        StrBuilder sb = new StrBuilder("abc");
        sb.append((String) null);
        assertEquals("abcnull", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_withObject() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft((Object) "Hi", 5, '0');
        assertEquals("000Hi", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight_withObject() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight((Object) "Hi", 5, '0');
        assertEquals("Hi000", sb.toString());
    }

    @Test
    public void testAppendWithSeparators_collection() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(java.util.Arrays.asList("a", "b", "c"), "-");
        assertEquals("a-b-c", sb.toString());
    }

    @Test
    public void testAppendWithSeparators_iterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(java.util.Arrays.asList("x", "y").iterator(), ",");
        assertEquals("x,y", sb.toString());
    }

    @Test
    public void testDeleteString_allOccurrences() {
        StrBuilder sb = new StrBuilder("aaaaaa");
        sb.deleteAll("aa");
        assertEquals("aa", sb.toString());
    }

    @Test
    public void testDeleteString_firstOccurrence() {
        StrBuilder sb = new StrBuilder("abac");
        sb.deleteFirst("ba");
        assertEquals("ac", sb.toString());
    }

    @Test
    public void testDeleteChar_firstOccurrence() {
        StrBuilder sb = new StrBuilder("abac");
        sb.deleteFirst('a');
        assertEquals("bac", sb.toString());
    }

    @Test
    public void testReplaceSubstring_validRange() {
        StrBuilder sb = new StrBuilder("Hello World");
        sb.replace(6, 11, "Java");
        assertEquals("Hello Java", sb.toString());
    }

    @Test
    public void testReplaceFirstString() {
        StrBuilder sb = new StrBuilder("aabaab");
        sb.replaceFirst("aa", "c");
        assertEquals("cbaab", sb.toString());
    }

    @Test
    public void testIndexOfStringFromIndex() {
        StrBuilder sb = new StrBuilder("abacaba");
        assertEquals(4, sb.indexOf("aba", 2));
    }

    @Test
    public void testLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("abacada");
        assertEquals(6, sb.lastIndexOf('a'));
    }

    @Test
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("abacaba");
        assertEquals(4, sb.lastIndexOf("aba"));
    }

    @Test
    public void testLastIndexOfStringFromIndex() {
        StrBuilder sb = new StrBuilder("abacaba");
        assertEquals(0, sb.lastIndexOf("aba", 3));
    }

    @Test
    public void testSubstringFromStart() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("ello", sb.substring(1));
    }

    @Test
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("He", sb.leftString(2));
    }

    @Test
    public void testRightString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("llo", sb.rightString(3));
    }

    @Test
    public void testMidString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("ell", sb.midString(1, 3));
    }

    @Test
    public void testContains_char() {
        StrBuilder sb = new StrBuilder("Hello");
        assertTrue(sb.contains('l'));
        assertFalse(sb.contains('z'));
    }

    @Test
    public void testContains_string() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.contains("World"));
        assertFalse(sb.contains("Java"));
    }

    @Test
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.endsWith("World"));
        assertFalse(sb.endsWith("Hello"));
    }

    @Test
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("HELLO");
        assertTrue(sb1.equalsIgnoreCase(sb2));
    }

    @Test
    public void testCompareTo_equal() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        assertEquals(0, sb1.compareTo(sb2));
    }

    @Test
    public void testCompareTo_less() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abcd");
        assertTrue(sb1.compareTo(sb2) < 0);
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        StrBuilder sb1 = new StrBuilder("test");
        StrBuilder sb2 = new StrBuilder("test");
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test
    public void testGetChars() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] dest = new char[5];
        sb.getChars(0, 5, dest, 0);
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, dest);
    }

    @Test
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder("Hello");
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, sb.toCharArray());
    }

    @Test
    public void testCapacity() {
        StrBuilder sb = new StrBuilder(10);
        assertTrue(sb.capacity() >= 10);
    }

    @Test
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder(5);
        sb.ensureCapacity(20);
        assertTrue(sb.capacity() >= 20);
    }

    @Test
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder("Hello");
        int oldCap = sb.capacity();
        sb.minimizeCapacity();
        assertTrue(sb.capacity() <= oldCap);
        assertEquals(5, sb.capacity());
    }

    @Test
    public void testCharAt() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals('H', sb.charAt(0));
        assertEquals('o', sb.charAt(4));
    }

    @Test
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.setCharAt(0, 'h');
        assertEquals("hello", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAt_indexTooLarge_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(3);
    }

    @Test
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteCharAt(2);
        assertEquals("Helo", sb.toString());
    }

    @Test
    public void testInsertBoolean() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());
    }

    @Test
    public void testInsertChar() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, 'Z');
        assertEquals("aZbc", sb.toString());
    }

    @Test
    public void testInsertCharArray() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(3, new char[]{'X', 'Y'});
        assertEquals("abcXY", sb.toString());
    }

    @Test
    public void testInsertDouble() {
        StrBuilder sb = new StrBuilder("Value: ");
        sb.insert(7, 3.14);
        assertEquals("Value: 3.14", sb.toString());
    }

    @Test
    public void testInsertFloat() {
        StrBuilder sb = new StrBuilder("Value: ");
        sb.insert(7, 2.5f);
        assertEquals("Value: 2.5", sb.toString());
    }

    @Test
    public void testInsertInt() {
        StrBuilder sb = new StrBuilder("Number: ");
        sb.insert(7, 123);
        assertEquals("Number: 123", sb.toString());
    }

    @Test
    public void testInsertLong() {
        StrBuilder sb = new StrBuilder("Long: ");
        sb.insert(5, 9876543210L);
        assertEquals("Long: 9876543210", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertBoolean_indexNegative_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(-1, true);
    }

    @Test
    public void testSetLength_decreaseLength_truncates() {
        StrBuilder sb = new StrBuilder("Hello World");
        sb.setLength(5);
        assertEquals("Hello", sb.toString());
    }

    @Test
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder("Hi");
        sb.appendPadding(5, '*');
        assertEquals("Hi*****", sb.toString());
    }

    @Test
    public void testAppendSeparator_char() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(',');
        assertEquals("a,", sb.toString());
    }

    @Test
    public void testAppendSeparator_string() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(", ");
        assertEquals("a, ", sb.toString());
    }

    @Test
    public void testAppendSeparator_withCondition() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(',', 1);
        assertEquals("a,", sb.toString());

        StrBuilder sb2 = new StrBuilder();
        sb2.appendSeparator(',', 0);
        assertEquals("", sb2.toString());
    }

    @Test
    public void testSetNewLineText_andGet() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\\n");
        assertEquals("\\n", sb.getNewLineText());
    }

    @Test
    public void testSetNullText_andGet() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
    }

    @Test
    public void testClear() {
        StrBuilder sb = new StrBuilder("Some content");
        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testLength_zero() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
    }

    @Test
    public void testIsEmpty_true() {
        StrBuilder sb = new StrBuilder();
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testIsEmpty_false() {
        StrBuilder sb = new StrBuilder("a");
        assertFalse(sb.isEmpty());
    }

    @Test
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.toStringBuffer().toString());
    }
}