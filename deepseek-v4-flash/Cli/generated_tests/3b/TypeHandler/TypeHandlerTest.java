package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.net.URL;
import java.util.Date;

public class TypeHandlerTest {

    // Tests createValue with STRING_VALUE class returns the input string
    @Test
    public void testCreateValue_stringValue_returnsString() {
        assertEquals("test", TypeHandler.createValue("test", PatternOptionBuilder.STRING_VALUE));
    }

    // Tests createValue with OBJECT_VALUE class delegates to createObject
    @Test
    public void testCreateValue_objectValue_classFound_returnsInstance() {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    // Tests createValue with OBJECT_VALUE class when class not found returns null
    @Test
    public void testCreateValue_objectValue_classNotFound_returnsNull() {
        assertNull(TypeHandler.createValue("NonExistentClass", PatternOptionBuilder.OBJECT_VALUE));
    }

    // Tests createValue with NUMBER_VALUE class returns a Number
    @Test
    public void testCreateValue_numberValue_validNumber_returnsNumber() {
        Number result = (Number) TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(result);
        assertEquals(123L, result.longValue());
    }

    // Tests createValue with NUMBER_VALUE class for invalid number returns null
    @Test
    public void testCreateValue_numberValue_invalidNumber_returnsNull() {
        assertNull(TypeHandler.createValue("abc", PatternOptionBuilder.NUMBER_VALUE));
    }

    // Tests createValue with DATE_VALUE class always returns null due to bug
    @Test
    public void testCreateValue_dateValue_anyInput_returnsNull() {
        assertNull(TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE));
    }

    // Tests createValue with CLASS_VALUE class returns the Class object
    @Test
    public void testCreateValue_classValue_validClass_returnsClass() {
        Class result = (Class) TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertNotNull(result);
        assertEquals(String.class, result);
    }

    // Tests createValue with CLASS_VALUE class when class not found returns null
    @Test
    public void testCreateValue_classValue_invalidClass_returnsNull() {
        assertNull(TypeHandler.createValue("NonExistentClass", PatternOptionBuilder.CLASS_VALUE));
    }

    // Tests createValue with FILE_VALUE class returns a File object
    @Test
    public void testCreateValue_fileValue_validPath_returnsFile() {
        File result = (File) TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertEquals("/tmp/test.txt", result.getPath());
    }

    // Tests createValue with EXISTING_FILE_VALUE class returns a File object (same as FILE_VALUE)
    @Test
    public void testCreateValue_existingFileValue_validPath_returnsFile() {
        File result = (File) TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(result);
        assertEquals("/tmp/test.txt", result.getPath());
    }

    // Tests createValue with FILES_VALUE class returns null (not implemented)
    @Test
    public void testCreateValue_filesValue_anyInput_returnsNull() {
        assertNull(TypeHandler.createValue("/tmp", PatternOptionBuilder.FILES_VALUE));
    }

    // Tests createValue with URL_VALUE class returns a URL object
    @Test
    public void testCreateValue_urlValue_validUrl_returnsURL() throws Exception {
        URL result = (URL) TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
        assertNotNull(result);
        assertEquals(new URL("http://example.com"), result);
    }

    // Tests createValue with URL_VALUE class when invalid URL returns null
    @Test
    public void testCreateValue_urlValue_invalidUrl_returnsNull() {
        assertNull(TypeHandler.createValue("invalid-url", PatternOptionBuilder.URL_VALUE));
    }

    // Tests createValue with unknown class returns null
    @Test
    public void testCreateValue_unknownClass_returnsNull() {
        assertNull(TypeHandler.createValue("test", Integer.class));
    }

    // Tests createValue with null str for STRING_VALUE returns null (str is null)
    @Test
    public void testCreateValue_nullString_returnsNull() {
        assertNull(TypeHandler.createValue(null, PatternOptionBuilder.STRING_VALUE));
    }

    // Tests createNumber with a double value returns a Double
    @Test
    public void testCreateNumber_doubleString_returnsDouble() {
        Number result = TypeHandler.createNumber("3.14");
        assertNotNull(result);
        assertEquals(3.14, result.doubleValue(), 0.0001);
    }

    // Tests createNumber with invalid input returns null
    @Test
    public void testCreateNumber_invalidString_returnsNull() {
        assertNull(TypeHandler.createNumber("notanumber"));
    }

    // Tests createClass with valid class name returns the class
    @Test
    public void testCreateClass_validClassName_returnsClass() {
        Class result = TypeHandler.createClass("java.lang.String");
        assertNotNull(result);
        assertEquals(String.class, result);
    }

    // Tests createClass with invalid class name returns null
    @Test
    public void testCreateClass_invalidClassName_returnsNull() {
        assertNull(TypeHandler.createClass("NonExistentClass"));
    }

    // Tests createURL with valid URL returns URL
    @Test
    public void testCreateURL_validUrl_returnsURL() throws Exception {
        URL result = TypeHandler.createURL("http://example.com");
        assertNotNull(result);
        assertEquals(new URL("http://example.com"), result);
    }

    // Tests createURL with invalid URL returns null
    @Test
    public void testCreateURL_invalidUrl_returnsNull() {
        assertNull(TypeHandler.createURL("invalid-url"));
    }

    // Tests createFile with a path returns File
    @Test
    public void testCreateFile_validPath_returnsFile() {
        File result = TypeHandler.createFile("/tmp/test.txt");
        assertNotNull(result);
        assertEquals("/tmp/test.txt", result.getPath());
    }

    // Tests createFiles returns null (not implemented)
    @Test
    public void testCreateFiles_anyInput_returnsNull() {
        assertNull(TypeHandler.createFiles("/tmp"));
    }

    // Tests createDate returns null (buggy implementation)
    @Test
    public void testCreateDate_anyInput_returnsNull() {
        assertNull(TypeHandler.createDate("2023-01-01"));
    }

    // Tests createObject with valid class name returns instance
    @Test
    public void testCreateObject_validClassName_returnsInstance() {
        Object result = TypeHandler.createObject("java.lang.String");
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    // Tests createObject with invalid class name returns null
    @Test
    public void testCreateObject_invalidClassName_returnsNull() {
        assertNull(TypeHandler.createObject("NonExistentClass"));
    }

    // === NEW TEST CASES FOR IMPROVED COVERAGE ===

    // Test createNumber with null input
    @Test
    public void testCreateNumber_nullInput_returnsNull() {
        assertNull(TypeHandler.createNumber(null));
    }

    // Test createNumber with empty string
    @Test
    public void testCreateNumber_emptyString_returnsNull() {
        assertNull(TypeHandler.createNumber(""));
    }

    // Test createNumber with integer string (e.g., "42") returns an integer Number (likely Long)
    @Test
    public void testCreateNumber_integerString_returnsNumber() {
        Number result = TypeHandler.createNumber("42");
        assertNotNull(result);
        assertEquals(42L, result.longValue());
    }

    // Test createClass with null input
    @Test
    public void testCreateClass_nullInput_returnsNull() {
        assertNull(TypeHandler.createClass(null));
    }

    // Test createURL with null input
    @Test
    public void testCreateURL_nullInput_returnsNull() {
        assertNull(TypeHandler.createURL(null));
    }

    // Test createFile with null input
    @Test
    public void testCreateFile_nullInput_returnsNull() {
        assertNull(TypeHandler.createFile(null));
    }

    // Test createFiles with null input (should return null as not implemented)
    @Test
    public void testCreateFiles_nullInput_returnsNull() {
        assertNull(TypeHandler.createFiles(null));
    }

    // Test createDate with null input
    @Test
    public void testCreateDate_nullInput_returnsNull() {
        assertNull(TypeHandler.createDate(null));
    }

    // Test createObject with null input
    @Test
    public void testCreateObject_nullInput_returnsNull() {
        assertNull(TypeHandler.createObject(null));
    }

    // Test createValue with an object (not a Class) as the second parameter - should return null
    @Test
    public void testCreateValue_nonClassObject_returnsNull() {
        assertNull(TypeHandler.createValue("test", "notAClass"));
    }

    // Test createValue with empty string for STRING_VALUE returns empty string
    @Test
    public void testCreateValue_emptyStringForString_returnsEmptyString() {
        assertEquals("", TypeHandler.createValue("", PatternOptionBuilder.STRING_VALUE));
    }

    // Test createValue with empty string for NUMBER_VALUE returns null
    @Test
    public void testCreateValue_emptyStringForNumber_returnsNull() {
        assertNull(TypeHandler.createValue("", PatternOptionBuilder.NUMBER_VALUE));
    }

    // Test createValue with empty string for CLASS_VALUE returns null
    @Test
    public void testCreateValue_emptyStringForClass_returnsNull() {
        assertNull(TypeHandler.createValue("", PatternOptionBuilder.CLASS_VALUE));
    }

    // Test createValue with empty string for FILE_VALUE returns File with empty path
    @Test
    public void testCreateValue_emptyStringForFile_returnsFile() {
        File result = (File) TypeHandler.createValue("", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertEquals("", result.getPath());
    }

    // Test createValue with empty string for URL_VALUE returns null
    @Test
    public void testCreateValue_emptyStringForURL_returnsNull() {
        assertNull(TypeHandler.createValue("", PatternOptionBuilder.URL_VALUE));
    }

    // Test createValue with empty string for OBJECT_VALUE returns null
    @Test
    public void testCreateValue_emptyStringForObject_returnsNull() {
        assertNull(TypeHandler.createValue("", PatternOptionBuilder.OBJECT_VALUE));
    }
}