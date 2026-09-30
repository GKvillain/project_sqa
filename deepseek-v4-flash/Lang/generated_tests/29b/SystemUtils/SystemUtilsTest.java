package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

public class SystemUtilsTest {

    // Tests toJavaVersionIntArray with null input
    @Test
    public void testToJavaVersionIntArray_nullInput_returnsEmptyArray() {
        int[] result = SystemUtils.toJavaVersionIntArray(null);
        assertArrayEquals(new int[0], result);
    }

    // Tests toJavaVersionIntArray with empty string
    @Test
    public void testToJavaVersionIntArray_emptyString_returnsEmptyArray() {
        int[] result = SystemUtils.toJavaVersionIntArray("");
        assertArrayEquals(new int[0], result);
    }

    // Tests toJavaVersionIntArray with standard version string
    @Test
    public void testToJavaVersionIntArray_normalVersion_returnsCorrectArray() {
        int[] result = SystemUtils.toJavaVersionIntArray("1.6.0");
        assertArrayEquals(new int[]{1, 6, 0}, result);
    }

    // Tests toJavaVersionIntArray with version that includes update number
    @Test
    public void testToJavaVersionIntArray_versionWithUpdateNumber_returnsArrayIncludingUpdate() {
        int[] result = SystemUtils.toJavaVersionIntArray("1.6.0_12");
        assertArrayEquals(new int[]{1, 6, 0, 12}, result);
    }

    // Tests toJavaVersionIntArray with only digits, no dots
    @Test
    public void testToJavaVersionIntArray_versionOnlyDigits_returnsSingleElementArray() {
        int[] result = SystemUtils.toJavaVersionIntArray("5");
        assertArrayEquals(new int[]{5}, result);
    }

    // Tests toJavaVersionIntArray with version containing alpha suffix – should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testToJavaVersionIntArray_versionWithAlphaSuffix_throwsNumberFormatException() {
        SystemUtils.toJavaVersionIntArray("1.6.0_12-ea");
    }

    // Tests toJavaVersionIntArray with version containing dash suffix – should throw NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testToJavaVersionIntArray_versionWithDashSuffix_throwsNumberFormatException() {
        SystemUtils.toJavaVersionIntArray("1.6.0_12-b04");
    }

    // Tests toJavaVersionFloat with standard three-part version
    @Test
    public void testToJavaVersionFloat_normalVersion_returnsCorrectFloat() {
        float result = SystemUtils.toJavaVersionFloat("1.6.0");
        assertEquals(1.6f, result, 0.0001f);
    }

    // Tests toJavaVersionFloat with two-part version
    @Test
    public void testToJavaVersionFloat_twoPartVersion_returnsCorrectFloat() {
        float result = SystemUtils.toJavaVersionFloat("1.2");
        assertEquals(1.2f, result, 0.0001f);
    }

    // Tests toJavaVersionFloat with single number
    @Test
    public void testToJavaVersionFloat_singleDigitVersion_returnsFloat() {
        float result = SystemUtils.toJavaVersionFloat("5");
        assertEquals(5.0f, result, 0.0001f);
    }

    // Tests toJavaVersionInt with standard three-part version
    @Test
    public void testToJavaVersionInt_normalVersion_returnsCorrectInt() {
        float result = SystemUtils.toJavaVersionInt("1.3.1");
        assertEquals(131.0f, result, 0.0001f);
    }

    // Tests toJavaVersionInt with two-part version
    @Test
    public void testToJavaVersionInt_twoPartVersion_returnsCorrectInt() {
        float result = SystemUtils.toJavaVersionInt("1.2");
        assertEquals(120.0f, result, 0.0001f);
    }

    // Tests toJavaVersionInt with single number
    @Test
    public void testToJavaVersionInt_singleDigitVersion_returnsInt() {
        float result = SystemUtils.toJavaVersionInt("5");
        assertEquals(500.0f, result, 0.0001f);
    }

    // Tests isJavaVersionMatch with null version
    @Test
    public void testIsJavaVersionMatch_nullVersion_returnsFalse() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.5"));
    }

    // Tests isJavaVersionMatch with matching prefix
    @Test
    public void testIsJavaVersionMatch_matchingPrefix_returnsTrue() {
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0_12", "1.6"));
    }

    // Tests isJavaVersionMatch with non-matching prefix
    @Test
    public void testIsJavaVersionMatch_nonMatchingPrefix_returnsFalse() {
        assertFalse(SystemUtils.isJavaVersionMatch("1.7.0", "1.6"));
    }

    // Tests isOSMatch with null osName
    @Test
    public void testIsOSMatch_nullOsName_returnsFalse() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
    }

    // Tests isOSMatch with null osVersion
    @Test
    public void testIsOSMatch_nullOsVersion_returnsFalse() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
    }

    // Tests isOSMatch with matching name and version
    @Test
    public void testIsOSMatch_matchingNameAndVersion_returnsTrue() {
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
    }

    // Tests isOSMatch with non-matching version
    @Test
    public void testIsOSMatch_nonMatchingVersion_returnsFalse() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", "6.0", "Windows", "5.1"));
    }

    // Tests isOSNameMatch with null osName
    @Test
    public void testIsOSNameMatch_nullOsName_returnsFalse() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
    }

    // Tests isOSNameMatch with matching prefix
    @Test
    public void testIsOSNameMatch_matchingPrefix_returnsTrue() {
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
    }

    // Tests isOSNameMatch with non-matching prefix
    @Test
    public void testIsOSNameMatch_nonMatchingPrefix_returnsFalse() {
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Windows"));
    }
}