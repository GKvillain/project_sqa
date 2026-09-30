package org.apache.commons.lang3;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SystemUtils}.
 */
public class SystemUtilsTest {

    // Tests public constructor instantiation
    @Test
    public void testConstructor_default_instanceCreated() {
        SystemUtils utils = new SystemUtils();
        assertNotNull(utils);
    }

    // Tests getJavaHome directory retrieval
    @Test
    public void testGetJavaHome_existingSystem_returnsDirectory() {
        File dir = SystemUtils.getJavaHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    // Tests getJavaIoTmpDir directory retrieval
    @Test
    public void testGetJavaIoTmpDir_existingSystem_returnsDirectory() {
        File dir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    // Tests getUserDir directory retrieval
    @Test
    public void testGetUserDir_existingSystem_returnsDirectory() {
        File dir = SystemUtils.getUserDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    // Tests getUserHome directory retrieval
    @Test
    public void testGetUserHome_existingSystem_returnsDirectory() {
        File dir = SystemUtils.getUserHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    // Tests isJavaAwtHeadless boolean helper
    @Test
    public void testIsJavaAwtHeadless_returnsExpectedValue() {
        boolean expected = "true".equals(System.getProperty("java.awt.headless"));
        assertEquals(expected, SystemUtils.isJavaAwtHeadless());
    }

    // Tests isJavaVersionAtLeast with float
    @Test
    public void testIsJavaVersionAtLeast_floatThreshold_returnsCorrectBoolean() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.0f));
        assertFalse(SystemUtils.isJavaVersionAtLeast(100.0f));
    }

    // Tests isJavaVersionAtLeast with int
    @Test
    public void testIsJavaVersionAtLeast_intThreshold_returnsCorrectBoolean() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0));
        assertFalse(SystemUtils.isJavaVersionAtLeast(10000));
    }

    // Tests isJavaVersionMatch with valid and invalid inputs
    @Test
    public void testIsJavaVersionMatch_nullAndValidInputs_returnsExpectedResult() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.5"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.5.0_22", "1.5"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.6.0", "1.5"));
    }

    // Tests isOSMatch with valid and null inputs
    @Test
    public void testIsOSMatch_nullAndValidInputs_returnsExpectedResult() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "5.1"));
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Linux", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "6.0"));
    }

    // Tests isOSNameMatch with valid and null inputs
    @Test
    public void testIsOSNameMatch_nullAndValidInputs_returnsExpectedResult() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
        assertFalse(SystemUtils.isOSNameMatch("Mac OS X", "Windows"));
    }

    // Tests toJavaVersionFloat with various string representations
    @Test
    public void testToJavaVersionFloat_variousVersions_returnsCorrectFloat() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(""), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat("0"), 0.00001f);
        assertEquals(1.1f, SystemUtils.toJavaVersionFloat("1.1"), 0.00001f);
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.00001f);
        assertEquals(1.3f, SystemUtils.toJavaVersionFloat("1.3.0"), 0.00001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.00001f);
        assertEquals(1.42f, SystemUtils.toJavaVersionFloat("1.4.2_05"), 0.00001f);
        assertEquals(1.5f, SystemUtils.toJavaVersionFloat("1.5.0_21"), 0.00001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.00001f);
    }

    // Tests toJavaVersionInt with various string representations
    @Test
    public void testToJavaVersionInt_variousVersions_returnsCorrectInt() {
        assertEquals(0.0f, SystemUtils.toJavaVersionInt(null), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionInt(""), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionInt("0"), 0.00001f);
        assertEquals(110.0f, SystemUtils.toJavaVersionInt("1.1"), 0.00001f);
        assertEquals(120.0f, SystemUtils.toJavaVersionInt("1.2"), 0.00001f);
        assertEquals(130.0f, SystemUtils.toJavaVersionInt("1.3.0"), 0.00001f);
        assertEquals(131.0f, SystemUtils.toJavaVersionInt("1.3.1"), 0.00001f);
        assertEquals(142.0f, SystemUtils.toJavaVersionInt("1.4.2_05"), 0.00001f);
        assertEquals(150.0f, SystemUtils.toJavaVersionInt("1.5.0_21"), 0.00001f);
        assertEquals(160.0f, SystemUtils.toJavaVersionInt("1.6.0_20"), 0.00001f);
    }

    // Tests toJavaVersionIntArray with null, empty, and multiple components
    @Test
    public void testToJavaVersionIntArray_variousInputs_returnsIntArray() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(null));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(""));
        assertArrayEquals(new int[]{1, 2}, SystemUtils.toJavaVersionIntArray("1.2"));
        assertArrayEquals(new int[]{1, 3, 1}, SystemUtils.toJavaVersionIntArray("1.3.1"));
        assertArrayEquals(new int[]{1, 5, 0, 21}, SystemUtils.toJavaVersionIntArray("1.5.0_21"));
    }
}