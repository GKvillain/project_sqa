package org.apache.commons.lang.text;

import static org.junit.Assert.*;
import org.junit.Test;

public class StrBuilderTest {

    @Test
    public void testStrBuilder_nullString_createsEmptyBuilder() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals(0, sb.length());
        assertEquals(32, sb.capacity());
    }

    @Test
    public void testStrBuilder_initialCapacityLessThanOne_usesDefault() {
        StrBuilder sb = new StrBuilder(0);
        assertTrue(sb.capacity() >= 32);
    }

    @Test
    public void testSetNullText_emptyString_setsToNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    @Test
    public void testAppendNull_nullTextNull_doesNothing() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
        sb.appendNull();
        assertEquals(0, sb.length());
    }

    @Test
    public void testAppendNull_nullTextSet_appendsText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.appendNull();
        assertEquals("NULL", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight_nullObjectNullTextNull_shouldPadWithNullString() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 5, '*');
        assertEquals("null*", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_nullObjectNullTextNull_shouldPadWithNullString() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 5, '*');
        assertEquals("*null", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight_normalObject_padsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 5, '-');
        assertEquals("abc--", sb.toString());
    }

    @Test
    public void testSetLength_negative_throwsException() {
        StrBuilder sb = new StrBuilder();
        try {
            sb.setLength(-1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testSetLength_greaterThanSize_fillsWithZero() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setLength(5);
        assertEquals(5, sb.length());
        char[] chars = sb.toCharArray();
        assertEquals('a', chars[0]);
        assertEquals('b', chars[1]);
        assertEquals('c', chars[2]);
        assertEquals('\0', chars[3]);
        assertEquals('\0', chars[4]);
    }

    @Test
    public void testDeleteCharAt_validIndex_deletesChar() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(1);
        assertEquals("ac", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAt_invalidIndex_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(3);
    }

    @Test
    public void testReplace_validRange_replacesCorrectly() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.replace(1, 4, "XYZ");
        assertEquals("aXYZef", sb.toString());
    }

    @Test
    public void testReplaceAll_char_replacesAll() {
        StrBuilder sb = new StrBuilder("aabbcc");
        sb.replaceAll('b', 'B');
        assertEquals("aaBBcc", sb.toString());
    }

    @Test
    public void testReplaceFirst_string_replacesFirst() {
        StrBuilder sb = new StrBuilder("abab");
        sb.replaceFirst("ab", "XY");
        assertEquals("XYab", sb.toString());
    }

    @Test
    public void testIndexOf_stringNotFound_returnsMinusOne() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.indexOf("def"));
    }

    @Test
    public void testLastIndexOf_stringFound_returnsIndex() {
        StrBuilder sb = new StrBuilder("abacad");
        assertEquals(4, sb.lastIndexOf("a"));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSubstring_invalidStart_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.substring(-1);
    }

    @Test
    public void testLeftString_negativeLength_returnsEmpty() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("", sb.leftString(-1));
    }

    @Test
    public void testRightString_exceedsLength_returnsFullString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.rightString(5));
    }

    @Test
    public void testStartsWith_null_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.startsWith(null));
    }

    @Test
    public void testEndsWith_nonMatching_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.endsWith("b"));
    }

    @Test
    public void testTrim_leadingAndTrailingSpaces_trims() {
        StrBuilder sb = new StrBuilder("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testReverse_evenLength_reversesCorrectly() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.reverse();
        assertEquals("dcba", sb.toString());
    }

    @Test
    public void testEquals_sameContent_returnsTrue() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        assertTrue(sb1.equals(sb2));
    }

    // ===== New test cases for uncovered code =====

    @Test
    public void testStrBuilder_defaultConstructor() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        assertTrue(sb.capacity() >= 32);
    }

    @Test
    public void testStrBuilder_stringConstructor() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.toString());
        assertEquals(5, sb.length());
    }

    @Test
    public void testStrBuilder_initialCapacityExact() {
        StrBuilder sb = new StrBuilder(100);
        assertTrue(sb.capacity() >= 100);
    }

    @Test
    public void testSetNullText_nonEmptyString_setsProperly() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("N/A");
        assertEquals("N/A", sb.getNullText());
    }

    @Test
    public void testSetNullText_null_setsNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText(null);
        assertNull(sb.getNullText());
    }

    @Test
    public void testAppendFixedWidthPadLeft_normalObject_padsCorrectly() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, '-');
        assertEquals("--abc", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight_objectTooLong_noPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abcdef", 3, '*');
        assertEquals("abcdef", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft_objectTooLong_noPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abcdef", 3, '*');
        assertEquals("abcdef", sb.toString());
    }

    @Test
    public void testSetLength_lessThanSize_truncates() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testSetLength_equalToSize_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testDelete_validRange_deletesCorrectly() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(1, 4);
        assertEquals("aef", sb.toString());
    }

    @Test
    public void testDelete_startEqualsEnd_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(1, 1);
        assertEquals("abc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDelete_invalidStart_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDelete_invalidEnd_throwsException() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(1, 10);
    }

    @Test
    public void testDeleteAll_string_found_deletesAll() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.deleteAll("ab");
        assertEquals("", sb.toString());
    }

    @Test
    public void testDeleteAll_string_notFound_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteAll("xyz");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testDeleteFirst_string_found_deletesFirst() {
        StrBuilder sb = new StrBuilder("abab");
        sb.deleteFirst("ab");
        assertEquals("ab", sb.toString());
    }

    @Test
    public void testDeleteFirst_string_notFound_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteFirst("xyz");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testDeleteAll_char_found_deletesAll() {
        StrBuilder sb = new StrBuilder("aabbcc");
        sb.deleteAll('b');
        assertEquals("aacc", sb.toString());
    }

    @Test
    public void testDeleteFirst_char_found_deletesFirst() {
        StrBuilder sb = new StrBuilder("abac");
        sb.deleteFirst('a');
        assertEquals("bac", sb.toString());
    }

    @Test
    public void testReplaceAll_string_found_replacesAll() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceAll("ab", "XY");
        assertEquals("XYXYXY", sb.toString());
    }

    @Test
    public void testReplaceAll_string_notFound_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replaceAll("xyz", "123");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testReplaceFirst_string_notFound_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replaceFirst("xyz", "123");
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testReplaceFirst_char_found_replacesFirst() {
        StrBuilder sb = new StrBuilder("abac");
        sb.replaceFirst('a', 'X');
        assertEquals("Xbac", sb.toString());
    }

    @Test
    public void testReplaceFirst_char_notFound_noChange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replaceFirst('x', 'X');
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testIndexOf_stringFound_returnsIndex() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals(1, sb.indexOf("bc"));
    }

    @Test
    public void testIndexOf_charFound_returnsIndex() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals(2, sb.indexOf('c'));
    }

    @Test
    public void testIndexOf_charNotFound_returnsMinusOne() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.indexOf('x'));
    }

    @Test
    public void testLastIndexOf_charFound_returnsIndex() {
        StrBuilder sb = new StrBuilder("abacad");
        assertEquals(4, sb.lastIndexOf('a'));
    }

    @Test
    public void testLastIndexOf_charNotFound_returnsMinusOne() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(-1, sb.lastIndexOf('x'));
    }

    @Test
    public void testSubstring_validRange_returnsSubstring() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("cde", sb.substring(2, 5));
    }

    @Test
    public void testLeftString_zeroLength_returnsEmpty() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("", sb.leftString(0));
    }

    @Test
    public void testLeftString_normal_returnsCorrect() {
        StrBuilder sb = new StrBuilder("abcde");
        assertEquals("abc", sb.leftString(3));
    }

    @Test
    public void testRightString_zeroLength_returnsEmpty() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("", sb.rightString(0));
    }

    @Test
    public void testRightString_normal_returnsCorrect() {
        StrBuilder sb = new StrBuilder("abcde");
        assertEquals("cde", sb.rightString(3));
    }

    @Test
    public void testStartsWith_emptyString_returnsTrue() {
        StrBuilder sb = new StrBuilder("abc");
        assertTrue(sb.startsWith(""));
    }

    @Test
    public void testStartsWith_matchingPrefix_returnsTrue() {
        StrBuilder sb = new StrBuilder("abcde");
        assertTrue(sb.startsWith("abc"));
    }

    @Test
    public void testStartsWith_nonMatching_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.startsWith("abd"));
    }

    @Test
    public void testEndsWith_emptyString_returnsTrue() {
        StrBuilder sb = new StrBuilder("abc");
        assertTrue(sb.endsWith(""));
    }

    @Test
    public void testEndsWith_matchingSuffix_returnsTrue() {
        StrBuilder sb = new StrBuilder("abcde");
        assertTrue(sb.endsWith("cde"));
    }

    @Test
    public void testEndsWith_null_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.endsWith(null));
    }

    @Test
    public void testTrim_noSpaces_noChange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test
    public void testTrim_onlySpaces_emptiesString() {
        StrBuilder sb = new StrBuilder("   ");
        sb.trim();
        assertEquals("", sb.toString());
    }

    @Test
    public void testReverse_oddLength_reversesCorrectly() {
        StrBuilder sb = new StrBuilder("abcde");
        sb.reverse();
        assertEquals("edcba", sb.toString());
    }

    @Test
    public void testReverse_emptyString_noChange() {
        StrBuilder sb = new StrBuilder("");
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test
    public void testEquals_differentContent_returnsFalse() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("def");
        assertFalse(sb1.equals(sb2));
    }

    @Test
    public void testEquals_nonStrBuilderObject_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.equals("abc"));
    }

    @Test
    public void testEquals_null_returnsFalse() {
        StrBuilder sb = new StrBuilder("abc");
        assertFalse(sb.equals(null));
    }

    @Test
    public void testEquals_sameReference_returnsTrue() {
        StrBuilder sb = new StrBuilder("abc");
        assertTrue(sb.equals(sb));
    }

    @Test
    public void testHashCode_consistentWithEquals() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test
    public void testToString_returnsCorrectString() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.toString());
    }

    @Test
    public void testToString_emptyBuilder_returnsEmptyString() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.toString());
    }
}