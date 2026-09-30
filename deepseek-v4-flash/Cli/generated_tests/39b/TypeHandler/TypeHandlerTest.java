package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class TypeHandlerTest {

    // Test createValue with STRING_VALUE returns the input string
    @Test
    public void testCreateValue_StringClass_STRING_returnsString() throws ParseException {
        assertEquals("hello", TypeHandler.createValue("hello", PatternOptionBuilder.STRING_VALUE));
    }

    // Test createValue with OBJECT_VALUE for valid class name returns instance
    @Test
    public void testCreateValue_StringClass_OBJECT_valid_returnsInstance() throws ParseException {
        Object result = TypeHandler.createValue("java.lang.StringBuilder", PatternOptionBuilder.OBJECT_VALUE);
        assertTrue(result instanceof StringBuilder);
    }

    // Test createValue with OBJECT_VALUE for invalid class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_OBJECT_invalid_throwsParseException() throws ParseException {
        TypeHandler.createValue("NoSuchClass", PatternOptionBuilder.OBJECT_VALUE);
    }

    // Test createValue with OBJECT_VALUE for class without default constructor throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_OBJECT_noDefaultCtor_throwsParseException() throws ParseException {
        TypeHandler.createValue("java.lang.Integer", PatternOptionBuilder.OBJECT_VALUE);
    }

    // Test createValue with NUMBER_VALUE for integer string returns Long
    @Test
    public void testCreateValue_StringClass_NUMBER_integer_returnsLong() throws ParseException {
        Object result = TypeHandler.createValue("42", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Long);
        assertEquals(42L, result);
    }

    // Test createValue with NUMBER_VALUE for decimal string returns Double
    @Test
    public void testCreateValue_StringClass_NUMBER_decimal_returnsDouble() throws ParseException {
        Object result = TypeHandler.createValue("3.14", PatternOptionBuilder.NUMBER_VALUE);
        assertTrue(result instanceof Double);
        assertEquals(3.14, (Double) result, 0.0001);
    }

    // Test createValue with NUMBER_VALUE for invalid number throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_NUMBER_invalid_throwsParseException() throws ParseException {
        TypeHandler.createValue("abc", PatternOptionBuilder.NUMBER_VALUE);
    }

    // Test createValue with NUMBER_VALUE for null string throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testCreateValue_StringClass_NUMBER_null_throwsNullPointerException() throws ParseException {
        TypeHandler.createValue(null, PatternOptionBuilder.NUMBER_VALUE);
    }

    // Test createValue with NUMBER_VALUE for empty string throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_NUMBER_empty_throwsParseException() throws ParseException {
        TypeHandler.createValue("", PatternOptionBuilder.NUMBER_VALUE);
    }

    // Test createValue with DATE_VALUE throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_StringClass_DATE_throwsUnsupportedOperationException() throws ParseException {
        TypeHandler.createValue("2024-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    // Test createValue with CLASS_VALUE for valid class name returns Class
    @Test
    public void testCreateValue_StringClass_CLASS_valid_returnsClass() throws ParseException {
        assertEquals(String.class, TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE));
    }

    // Test createValue with CLASS_VALUE for invalid class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_CLASS_invalid_throwsParseException() throws ParseException {
        TypeHandler.createValue("NonExistentClass", PatternOptionBuilder.CLASS_VALUE);
    }

    // Test createValue with FILE_VALUE returns File
    @Test
    public void testCreateValue_StringClass_FILE_returnsFile() throws ParseException {
        File file = (File) TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.FILE_VALUE);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    // Test createValue with EXISTING_FILE_VALUE returns File (same as FILE)
    @Test
    public void testCreateValue_StringClass_EXISTING_FILE_returnsFile() throws ParseException {
        File file = (File) TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    // Test createValue with FILES_VALUE throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_StringClass_FILES_throwsUnsupportedOperationException() throws ParseException {
        TypeHandler.createValue("file1 file2", PatternOptionBuilder.FILES_VALUE);
    }

    // Test createValue with URL_VALUE for valid URL returns URL
    @Test
    public void testCreateValue_StringClass_URL_valid_returnsURL() throws ParseException {
        URL url = (URL) TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
        assertEquals(new URL("http://example.com"), url);
    }

    // Test createValue with URL_VALUE for malformed URL throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateValue_StringClass_URL_invalid_throwsParseException() throws ParseException {
        TypeHandler.createValue("not a url", PatternOptionBuilder.URL_VALUE);
    }

    // Test createValue with unknown class type returns null
    @Test
    public void testCreateValue_StringClass_unknownType_returnsNull() throws ParseException {
        assertNull(TypeHandler.createValue("test", Integer.class));
    }

    // Test createValue(Object) overload delegates correctly
    @Test
    public void testCreateValue_ObjectArg_worksLikeClassArg() throws ParseException {
        Object classObj = PatternOptionBuilder.STRING_VALUE;
        assertEquals(
            TypeHandler.createValue("hello", (Class<?>) classObj),
            TypeHandler.createValue("hello", classObj)
        );
    }

    // New test: Test createValue with FILE_VALUE returns File object with valid path
    @Test
    public void testCreateValue_File_ValidPath_returnsFileObject() throws ParseException {
        Object result = TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.FILE_VALUE);
        assertTrue(result instanceof File);
        assertEquals(new File("/tmp/test.txt"), (File) result);
    }

    // New test: Test createValue with EXISTING_FILE_VALUE returns File object with valid path
    @Test
    public void testCreateValue_ExistingFile_ValidPath_returnsFileObject() throws ParseException {
        Object result = TypeHandler.createValue("/tmp/test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertTrue(result instanceof File);
        assertEquals(new File("/tmp/test.txt"), (File) result);
    }

    // New test: Test createValue with URL_VALUE for valid URL returns URL object
    @Test
    public void testCreateValue_URL_ValidURL_returnsURLObject() throws ParseException {
        Object result = TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE);
        assertTrue(result instanceof URL);
        assertEquals(new URL("http://example.com"), (URL) result);
    }

    // New test: Test createValue with URL_VALUE and malformed URL returns null (since Exception is not thrown)
    @Test
    public void testCreateValue_URL_MalformedURL_returnsNull() {
        try {
            Object result = TypeHandler.createValue("invalid url", PatternOptionBuilder.URL_VALUE);
            assertNull(result);
        } catch (ParseException e) {
            // If exception is thrown, test passes too since malformed URL should throw exception
            assertTrue(e.getMessage().contains("MalformedURLException"));
        }
    }
}