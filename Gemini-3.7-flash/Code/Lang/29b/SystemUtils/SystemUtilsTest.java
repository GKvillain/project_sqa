package org.apache.commons.lang3;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SystemUtilsTest {

    // Tests public constructor instantiation
    @Test
    public void testConstructor_defaultInstantiation_isPublic() {
        SystemUtils utils = new SystemUtils();
        assertNotNull(utils);
        Constructor<?>[] constructors = SystemUtils.class.getConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPublic(constructors[0].getModifiers()));
    }

    // Tests getJavaHome method returns an existing directory File
    @Test
    public void testGetJavaHome_existingProperty_returnsFile() {
        File dir = SystemUtils.getJavaHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    // Tests getJavaIoTmpDir method returns an existing directory File
    @Test
    public void testGetJavaIoTmpDir_existingProperty_returnsFile() {
        File dir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    // Tests getUserDir method returns an existing directory File
    @Test
    public void testGetUserDir_existingProperty_returnsFile() {
        File dir = SystemUtils.getUserDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    // Tests getUserHome method returns an existing directory File
    @Test
    public void testGetUserHome_existingProperty_returnsFile() {
        File dir = SystemUtils.getUserHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    // Tests isJavaAwtHeadless returns consistent boolean value
    @Test
    public void testIsJavaAwtHeadless_systemState_returnsBoolean() {
        boolean headless = SystemUtils.isJavaAwtHeadless();
        String headlessProp = System.getProperty("java.awt.headless");
        if (headlessProp != null) {
            assertEquals(Boolean.parseBoolean(headlessProp), headless);
        } else {
            assertFalse(headless);
        }
    }

    // Tests isJavaVersionAtLeast with float argument
    @Test
    public void testIsJavaVersionAtLeast_floatVersion_returnsCorrectResult() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.0f));
        assertTrue(SystemUtils.isJavaVersionAtLeast(1.1f));
        assertFalse(SystemUtils.isJavaVersionAtLeast(99.0f));
    }

    // Tests isJavaVersionAtLeast with int argument
    @Test
    public void testIsJavaVersionAtLeast_intVersion_returnsCorrectResult() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0));
        assertTrue(SystemUtils.isJavaVersionAtLeast(110));
        assertFalse(SystemUtils.isJavaVersionAtLeast(9900));
    }

    // Tests isJavaVersionMatch with valid and invalid prefixes
    @Test
    public void testIsJavaVersionMatch_validPrefix_returnsTrue() {
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0_20", "1.6"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.7.0", "1.7"));
    }

    // Tests isJavaVersionMatch with non-matching and null inputs
    @Test
    public void testIsJavaVersionMatch_nonMatchingAndNull_returnsFalse() {
        assertFalse(SystemUtils.isJavaVersionMatch("1.6.0_20", "1.5"));
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.6"));
    }

    // Tests isOSMatch with matching OS name and version prefix
    @Test
    public void testIsOSMatch_matchingPrefix_returnsTrue() {
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
        assertTrue(SystemUtils.isOSMatch("Mac OS X", "10.6.8", "Mac", "10.6"));
    }

    // Tests isOSMatch with non-matching and null inputs
    @Test
    public void testIsOSMatch_nonMatchingAndNull_returnsFalse() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "6.0"));
        assertFalse(SystemUtils.isOSMatch("Linux", "2.6", "Windows", "2.6"));
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
    }

    // Tests isOSNameMatch with matching prefix
    @Test
    public void testIsOSNameMatch_matchingPrefix_returnsTrue() {
        assertTrue(SystemUtils.isOSNameMatch("Linux", "Lin"));
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
    }

    // Tests isOSNameMatch with non-matching and null inputs
    @Test
    public void testIsOSNameMatch_nonMatchingAndNull_returnsFalse() {
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Win"));
        assertFalse(SystemUtils.isOSNameMatch(null, "Linux"));
    }

    // Tests toJavaVersionFloat with standard version strings
    @Test
    public void testToJavaVersionFloat_validVersions_returnsFloat() {
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.00001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.00001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.00001f);
        assertEquals(2.0f, SystemUtils.toJavaVersionFloat("2.0"), 0.00001f);
    }

    // Tests toJavaVersionFloat with null, empty, and single number inputs
    @Test
    public void testToJavaVersionFloat_nullAndEmptyAndSingle_returnsExpected() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(""), 0.00001f);
        assertEquals(2.0f, SystemUtils.toJavaVersionFloat("2"), 0.00001f);
    }

    // Tests toJavaVersionInt with standard version strings
    @Test
    public void testToJavaVersionInt_validVersions_returnsInt() {
        assertEquals(120.0f, SystemUtils.toJavaVersionInt("1.2"), 0.00001f);
        assertEquals(131.0f, SystemUtils.toJavaVersionInt("1.3.1"), 0.00001f);
        assertEquals(160.0f, SystemUtils.toJavaVersionInt("1.6.0_20"), 0.00001f);
        assertEquals(200.0f, SystemUtils.toJavaVersionInt("2.0"), 0.00001f);
    }

    // Tests toJavaVersionInt with null, empty, and single number inputs
    @Test
    public void testToJavaVersionInt_nullAndEmptyAndSingle_returnsExpected() {
        assertEquals(0.0f, SystemUtils.toJavaVersionInt(null), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionInt(""), 0.00001f);
        assertEquals(200.0f, SystemUtils.toJavaVersionInt("2"), 0.00001f);
    }

    // Tests toJavaVersionIntArray with various version strings
    @Test
    public void testToJavaVersionIntArray_validVersions_returnsIntArray() {
        assertArrayEquals(new int[]{1, 2}, SystemUtils.toJavaVersionIntArray("1.2"));
        assertArrayEquals(new int[]{1, 3, 1}, SystemUtils.toJavaVersionIntArray("1.3.1"));
        assertArrayEquals(new int[]{1, 5, 0, 21}, SystemUtils.toJavaVersionIntArray("1.5.0_21"));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(null));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(""));
    }

    // Tests system constants are initialized
    @Test
    public void testConstants_initialization_notNullOrValid() {
        assertNotNull(SystemUtils.FILE_SEPARATOR);
        assertNotNull(SystemUtils.LINE_SEPARATOR);
        assertNotNull(SystemUtils.PATH_SEPARATOR);
        assertNotNull(SystemUtils.JAVA_VERSION);
        assertNotNull(SystemUtils.JAVA_VERSION_TRIMMED);
        assertTrue(SystemUtils.JAVA_VERSION_FLOAT > 0f);
        assertTrue(SystemUtils.JAVA_VERSION_INT > 0);
    }
}