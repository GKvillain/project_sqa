package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;

public class TypeHandlerTest {

    // Tests createValue with STRING_VALUE returns the input string
    @Test
    public void testCreateValue_stringValue_returnsString() throws ParseException {
        assertEquals("test", TypeHandler.createValue("test", PatternOptionBuilder.STRING_VALUE));
    }

    // Tests createValue with OBJECT_VALUE returns an object created from class name
    @Test
    public void testCreateValue_objectValue_returnsObject() throws ParseException {
        Object obj = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertTrue(obj instanceof String);
    }

    // Tests createValue with NUMBER_VALUE returns a Long when no decimal point
    @Test
    public void testCreateValue_numberValue_noDecimal_returnsLong() throws ParseException {
        assertEquals(42L, TypeHandler.createValue("42", PatternOptionBuilder.NUMBER_VALUE));
    }

    // Tests createValue with NUMBER_VALUE returns a Double when decimal point present
    @Test
    public void testCreateValue_numberValue_withDecimal_returnsDouble() throws ParseException {
        assertEquals(4.2, TypeHandler.createValue("4.2", PatternOptionBuilder.NUMBER_VALUE));
    }

    // Tests createValue with DATE_VALUE throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_dateValue_throwsUnsupportedOperationException() throws ParseException {
        TypeHandler.createValue("2024-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    // Tests createValue with CLASS_VALUE returns the class object
    @Test
    public void testCreateValue_classValue_returnsClass() throws ParseException {
        assertEquals(String.class, TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE));
    }

    // Tests createValue with FILE_VALUE returns a File object
    @Test
    public void testCreateValue_fileValue_returnsFile() throws ParseException {
        assertEquals(new File("test.txt"), TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE));
    }

    // Tests createValue with EXISTING_FILE_VALUE returns a FileInputStream for an existing file
    @Test
    public void testCreateValue_existingFileValue_returnsFileInputStream() throws ParseException {
        FileInputStream fis = TypeHandler.createValue("pom.xml", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(fis);
    }

    // Tests createValue with FILES_VALUE throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_filesValue_throwsUnsupportedOperationException() throws ParseException {
        TypeHandler.createValue("file1.txt,file2.txt", PatternOptionBuilder.FILES_VALUE);
    }

    // Tests createValue with URL_VALUE returns a URL object
    @Test
    public void testCreateValue_urlValue_returnsURL() throws ParseException {
        assertEquals(java.net.URL.class, TypeHandler.createValue("http://example.com", PatternOptionBuilder.URL_VALUE).getClass());
    }

    // Tests createValue with unknown class returns null
    @Test
    public void testCreateValue_unknownClass_returnsNull() throws ParseException {
        assertNull(TypeHandler.createValue("test", Object.class));
    }

    // Tests createObject with valid class name creates an instance
    @Test
    public void testCreateObject_validClassName_returnsInstance() throws ParseException {
        Object obj = TypeHandler.createObject("java.lang.String");
        assertTrue(obj instanceof String);
    }

    // Tests createObject with non-existent class throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_nonExistentClass_throwsParseException() throws ParseException {
        TypeHandler.createObject("no.such.Class");
    }

    // Tests createObject with class that cannot be instantiated (abstract class) throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_abstractClass_throwsParseException() throws ParseException {
        TypeHandler.createObject("java.lang.AbstractStringBuilder");
    }

    // Tests createNumber with valid integer string returns Long
    @Test
    public void testCreateNumber_validInteger_returnsLong() throws ParseException {
        assertEquals(123L, TypeHandler.createNumber("123"));
    }

    // Tests createNumber with valid double string returns Double
    @Test
    public void testCreateNumber_validDouble_returnsDouble() throws ParseException {
        assertEquals(1.5, TypeHandler.createNumber("1.5"));
    }

    // Tests createNumber with invalid numeric string throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateNumber_invalidString_throwsParseException() throws ParseException {
        TypeHandler.createNumber("abc");
    }

    // Tests createNumber with null input throws ParseException (NullPointerException wrapped)
    @Test(expected = ParseException.class)
    public void testCreateNumber_nullInput_throwsParseException() throws ParseException {
        TypeHandler.createNumber(null);
    }

    // Tests createClass with valid class name returns the class
    @Test
    public void testCreateClass_validClassName_returnsClass() throws ParseException {
        assertEquals(Integer.class, TypeHandler.createClass("java.lang.Integer"));
    }

    // Tests createClass with non-existent class throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateClass_nonExistentClass_throwsParseException() throws ParseException {
        TypeHandler.createClass("no.such.Class");
    }

    // Tests createURL with valid URL string returns URL
    @Test
    public void testCreateURL_validURL_returnsURL() throws ParseException {
        assertEquals("http://example.com", TypeHandler.createURL("http://example.com").toString());
    }

    // Tests createURL with malformed URL string throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateURL_invalidURL_throwsParseException() throws ParseException {
        TypeHandler.createURL("not a url");
    }

    // Tests createFile returns a File object with the given path
    @Test
    public void testCreateFile_validPath_returnsFile() {
        assertEquals(new File("test.txt"), TypeHandler.createFile("test.txt"));
    }

    // Tests openFile with existing file returns FileInputStream
    @Test
    public void testOpenFile_existingFile_returnsInputStream() throws ParseException {
        FileInputStream fis = TypeHandler.openFile("pom.xml");
        assertNotNull(fis);
    }

    // Tests openFile with non-existent file throws ParseException
    @Test(expected = ParseException.class)
    public void testOpenFile_nonExistentFile_throwsParseException() throws ParseException {
        TypeHandler.openFile("no_such_file_here.txt");
    }

    // Tests createFiles throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiles_always_throwsUnsupportedOperationException() {
        TypeHandler.createFiles("test.txt");
    }
}