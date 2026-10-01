package org.apache.commons.lang.text;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test class for StrBuilder, focused on Defects4J bug 61b and key behaviors.
 */
public class StrBuilderTest {

    private StrBuilder builder;

    @Before
    public void setUp() {
        builder = new StrBuilder("HelloWorld");
    }

    // Test constructor with null string
    @Test
    public void testConstructor_nullInput_createsEmptyBuilder() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    // Test constructor with initial capacity zero (should become 32)
    @Test
    public void testConstructor_zeroCapacity_usesDefaultCapacity() {
        StrBuilder sb = new StrBuilder(0);
        assertEquals(32, sb.capacity());
    }

    // Test append(String) with normal string
    @Test
    public void testAppend_normalString_appendsCorrectly() {
        builder.append("Test");
        assertEquals("HelloWorldTest", builder.toString());
    }

    // Test append(String) with null
    @Test
    public void testAppend_nullInput_doesNothing() {
        StrBuilder sb = new StrBuilder("abc");
        sb.append((String) null);
        assertEquals("abc", sb.toString());
    }

    // Test append(boolean) both paths
    @Test
    public void testAppend_booleanTrue_appendsTrueString() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());
    }

    @Test
    public void testAppend_booleanFalse_appendsFalseString() {
        StrBuilder sb = new StrBuilder();
        sb.append(false);
        assertEquals("false", sb.toString());
    }

    // Test insert(int, String) normal
    @Test
    public void testInsert_middleIndex_insertsStringCorrectly() {
        builder.insert(5, "Beautiful");
        assertEquals("HelloBeautifulWorld", builder.toString());
    }

    // Test insert(int, String) with null (should use nullText which defaults to null, resulting no insertion)
    @Test
    public void testInsert_nullString_doesNothingWhenNullTextNull() {
        builder.insert(5, (String) null);
        assertEquals("HelloWorld", builder.toString());
    }

    // Test insert with invalid index throws exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsert_negativeIndex_throwsException() {
        builder.insert(-1, "test");
    }

    // Test delete(int,int) normal
    @Test
    public void testDelete_validRange_removesCharacters() {
        builder.delete(5, 10);
        assertEquals("Hello", builder.toString());
    }

    // Test delete with invalid startIndex throws exception
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDelete_negativeStartIndex_throwsException() {
        builder.delete(-1, 5);
    }

    // Test deleteAll(char) removes all occurrences
    @Test
    public void testDeleteAll_char_removesAllInstances() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteAll('a');
        assertEquals("bcd", sb.toString());
    }

    // Test replaceAll(char,char) with different chars
    @Test
    public void testReplaceAll_char_replacesAllMatches() {
        builder.replaceAll('l', 'X');
        assertEquals("HeXXoWorXd", builder.toString());
    }

    // Test replaceAll(String,String) normal
    @Test
    public void testReplaceAll_string_replacesAllMatches() {
        builder.replaceAll("World", "Java");
        assertEquals("HelloJava", builder.toString());
    }

    // Test replaceAll with empty replacement (defect test for infinite loop)
    @Test(timeout = 1000)
    public void testReplaceAll_emptyReplacement_doesNotLoopInfinitely() {
        StrBuilder sb = new StrBuilder("aaabbb");
        sb.replaceAll("a", "");
        assertEquals("bbb", sb.toString());
    }

    // Test replaceFirst with empty replacement
    @Test(timeout = 1000)
    public void testReplaceFirst_emptyReplacement_removesFirstOccurrence() {
        StrBuilder sb = new StrBuilder("aaabbb");
        sb.replaceFirst("a", "");
        assertEquals("aabbb", sb.toString());
    }

    // Test substring normal
    @Test
    public void testSubstring_validRange_returnsCorrectSubstring() {
        assertEquals("elloWorl", builder.substring(1, 9));
    }

    // Test substring with endIndex larger than size (should clamp)
    @Test
    public void testSubstring_endIndexGreaterThanSize_returnsRemainingString() {
        assertEquals("World", builder.substring(5, 20));
    }

    // Test indexOf char found
    @Test
    public void testIndexOf_charFound_returnsFirstIndex() {
        assertEquals(4, builder.indexOf('o'));
    }

    // Test indexOf char not found
    @Test
    public void testIndexOf_charNotFound_returnsMinusOne() {
        assertEquals(-1, builder.indexOf('z'));
    }

    // Test lastIndexOf string
    @Test
    public void testLastIndexOf_stringFound_returnsLastIndex() {
        assertEquals(4, builder.lastIndexOf("o"));
    }

    // Test trim removes leading and trailing spaces
    @Test
    public void testTrim_removesSpaces() {
        StrBuilder sb = new StrBuilder("  Hello  ");
        sb.trim();
        assertEquals("Hello", sb.toString());
    }

    // Test reverse
    @Test
    public void testReverse_reversesContents() {
        builder.reverse();
        assertEquals("dlroWolleH", builder.toString());
    }

    // Test equals with same content
    @Test
    public void testEquals_sameContent_returnsTrue() {
        StrBuilder other = new StrBuilder("HelloWorld");
        assertTrue(builder.equals(other));
    }

    // Test equals with different content
    @Test
    public void testEquals_differentContent_returnsFalse() {
        StrBuilder other = new StrBuilder("Hello");
        assertFalse(builder.equals(other));
    }

    // Test isEmpty on empty builder
    @Test
    public void testIsEmpty_emptyBuilder_returnsTrue() {
        StrBuilder sb = new StrBuilder();
        assertTrue(sb.isEmpty());
    }

    // Test capacity and ensureCapacity
    @Test
    public void testEnsureCapacity_increasesCapacity() {
        StrBuilder sb = new StrBuilder(10);
        sb.ensureCapacity(20);
        assertTrue(sb.capacity() >= 20);
    }

    // ========== Additional tests for uncovered areas ==========

    // Test append(char)
    @Test
    public void testAppend_char_appendsChar() {
        StrBuilder sb = new StrBuilder("He");
        sb.append('l');
        sb.append('l');
        sb.append('o');
        assertEquals("Hello", sb.toString());
    }

    // Test append(int)
    @Test
    public void testAppend_int_appendsInt() {
        StrBuilder sb = new StrBuilder();
        sb.append(123);
        assertEquals("123", sb.toString());
    }

    // Test append(long)
    @Test
    public void testAppend_long_appendsLong() {
        StrBuilder sb = new StrBuilder();
        sb.append(9876543210L);
        assertEquals("9876543210", sb.toString());
    }

    // Test append(float)
    @Test
    public void testAppend_float_appendsFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(3.14f);
        assertTrue(sb.toString().startsWith("3.14"));
    }

    // Test append(double)
    @Test
    public void testAppend_double_appendsDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.71828);
        assertTrue(sb.toString().startsWith("2.71828"));
    }

    // Test append(Object) with null (should call nullText or do nothing)
    @Test
    public void testAppend_objectNull_appendsNull() {
        StrBuilder sb = new StrBuilder("abc");
        sb.append((Object) null);
        // nullText defaults to null, so no insertion
        assertEquals("abc", sb.toString());
    }

    // Test append(char[]) 
    @Test
    public void testAppend_charArray_appendsCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'J', 'a', 'v', 'a'});
        assertEquals("Java", sb.toString());
    }

    // Test append(char[], int, int)
    @Test
    public void testAppend_charArraySubset_appendsSubset() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c', 'd', 'e'}, 1, 3);
        assertEquals("bcd", sb.toString());
    }

    // Test insert(int, char)
    @Test
    public void testInsert_char_insertsChar() {
        builder.insert(5, ' ');
        assertEquals("Hello World", builder.toString());
    }

    // Test insert(int, int)
    @Test
    public void testInsert_int_insertsInt() {
        StrBuilder sb = new StrBuilder("Value: ");
        sb.insert(7, 42);
        assertEquals("Value: 42", sb.toString());
    }

    // Test deleteAll(String) removes all substrings
    @Test
    public void testDeleteAll_string_removesAllSubstrings() {
        StrBuilder sb = new StrBuilder("ababa");
        sb.deleteAll("ab");
        assertEquals("a", sb.toString());
    }

    // Test deleteFirst(String) removes first substring
    @Test
    public void testDeleteFirst_string_removesFirstSubstring() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteFirst("a");
        assertEquals("bacad", sb.toString());
    }

    // Test deleteFirst(char) removes first char occurrence
    @Test
    public void testDeleteFirst_char_removesFirstChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteFirst('a');
        assertEquals("bacad", sb.toString());
    }

    // Test replace(int,int,String) 
    @Test
    public void testReplace_range_replacesRange() {
        builder.replace(5, 10, "Java");
        assertEquals("HelloJava", builder.toString());
    }

    // Test replaceFirst(String,String) normal
    @Test
    public void testReplaceFirst_normalString_replacesFirst() {
        StrBuilder sb = new StrBuilder("aaabbb");
        sb.replaceFirst("a", "X");
        assertEquals("Xaabbb", sb.toString());
    }

    // Test indexOf(String) found
    @Test
    public void testIndexOf_stringFound_returnsFirstIndex() {
        assertEquals(4, builder.indexOf("o"));
    }

    // Test indexOf(String) not found
    @Test
    public void testIndexOf_stringNotFound_returnsMinusOne() {
        assertEquals(-1, builder.indexOf("xyz"));
    }

    // Test indexOf(String, int) with start index
    @Test
    public void testIndexOf_stringWithStartIndex_findsLater() {
        assertEquals(7, builder.indexOf("o", 5));
    }

    // Test lastIndexOf(char) found
    @Test
    public void testLastIndexOf_charFound_returnsLastIndex() {
        assertEquals(7, builder.lastIndexOf('o'));
    }

    // Test lastIndexOf(char) not found
    @Test
    public void testLastIndexOf_charNotFound_returnsMinusOne() {
        assertEquals(-1, builder.lastIndexOf('z'));
    }

    // Test lastIndexOf(String, int) with end index
    @Test
    public void testLastIndexOf_stringWithEndIndex_findsEarlier() {
        assertEquals(4, builder.lastIndexOf("o", 6));
    }

    // Test contains(char) true
    @Test
    public void testContains_charTrue_returnsTrue() {
        assertTrue(builder.contains('W'));
    }

    // Test contains(char) false
    @Test
    public void testContains_charFalse_returnsFalse() {
        assertFalse(builder.contains('z'));
    }

    // Test contains(String) true
    @Test
    public void testContains_stringTrue_returnsTrue() {
        assertTrue(builder.contains("World"));
    }

    // Test contains(String) false
    @Test
    public void testContains_stringFalse_returnsFalse() {
        assertFalse(builder.contains("xyz"));
    }

    // Test startsWith
    @Test
    public void testStartsWith_validPrefix_returnsTrue() {
        assertTrue(builder.startsWith("Hello"));
    }

    // Test startsWith invalid
    @Test
    public void testStartsWith_invalidPrefix_returnsFalse() {
        assertFalse(builder.startsWith("World"));
    }

    // Test endsWith
    @Test
    public void testEndsWith_validSuffix_returnsTrue() {
        assertTrue(builder.endsWith("World"));
    }

    // Test endsWith invalid
    @Test
    public void testEndsWith_invalidSuffix_returnsFalse() {
        assertFalse(builder.endsWith("Hello"));
    }

    // Test toCharArray
    @Test
    public void testToCharArray_returnsCharArray() {
        char[] arr = builder.toCharArray();
        assertEquals(10, arr.length);
        assertArrayEquals(new char[]{'H','e','l','l','o','W','o','r','l','d'}, arr);
    }

    // Test hashCode consistent with String
    @Test
    public void testHashCode_consistentWithString() {
        String str = builder.toString();
        assertEquals(str.hashCode(), builder.hashCode());
    }

    // Test append(StringBuffer)
    @Test
    public void testAppend_StringBuffer_appendsContent() {
        StrBuilder sb = new StrBuilder("Hello");
        StringBuffer buf = new StringBuffer(" World");
        sb.append(buf);
        assertEquals("Hello World", sb.toString());
    }

    // Test append(StrBuilder)
    @Test
    public void testAppend_StrBuilder_appendsContent() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder(" World");
        sb1.append(sb2);
        assertEquals("Hello World", sb1.toString());
    }

    // Test insert at end index
    @Test
    public void testInsert_endIndex_insertsAtEnd() {
        builder.insert(builder.length(), "!");
        assertEquals("HelloWorld!", builder.toString());
    }
}