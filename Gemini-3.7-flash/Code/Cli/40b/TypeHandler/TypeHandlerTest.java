package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.junit.Test;

public class TypeHandlerTest {

    // Tests createValue with String type
    @Test
    public void testCreateValue_stringValue_returnsString() throws Exception {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
    }

    // Tests createValue with Object type
    @Test
    public void testCreateValue_objectValue_returnsObject() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    // Tests createValue with Number type
    @Test
    public void testCreateValue_numberValue_returnsNumber() throws Exception {
        Number result = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Long.valueOf(123), result);
    }

    // Tests createValue with Class type
    @Test
    public void testCreateValue_classValue_returnsClass() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
    }

    // Tests createValue with File type
    @Test
    public void testCreateValue_fileValue_returnsFile() throws Exception {
        File result = TypeHandler.createValue("somefile.txt", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertEquals("somefile.txt", result.getName());
    }

    // Tests createValue with URL type
    @Test
    public void testCreateValue_urlValue_returnsUrl() throws Exception {
        URL result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertNotNull(result);
        assertEquals("http://commons.apache.org", result.toExternalForm());
    }

    // Tests createValue with unsupported/unmatched class returns null
    @Test
    public void testCreateValue_unknownClass_returnsNull() throws Exception {
        assertNull(TypeHandler.createValue("test", Integer.class));
    }

    // Tests createValue with Object parameter delegating to Class overload
    @Test
    public void testCreateValue_withObjectParam_delegatesToClass() throws Exception {
        Object result = TypeHandler.createValue("test", (Object) PatternOptionBuilder.STRING_VALUE);
        assertEquals("test", result);
    }

    // Tests createValue with Date type throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_dateValue_throwsUnsupportedOperationException() throws Exception {
        TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    // Tests createValue with Files type throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_filesValue_throwsUnsupportedOperationException() throws Exception {
        TypeHandler.createValue("*.txt", PatternOptionBuilder.FILES_VALUE);
    }

    // Tests createObject with valid class name
    @Test
    public void testCreateObject_validClassName_returnsNewInstance() throws Exception {
        Object result = TypeHandler.createObject("java.lang.String");
        assertNotNull(result);
        assertEquals("", result);
    }

    // Tests createObject with unknown class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_unknownClass_throwsParseException() throws Exception {
        TypeHandler.createObject("org.apache.commons.cli.NonExistingClass");
    }

    // Tests createObject with class lacking empty constructor throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_classWithoutEmptyConstructor_throwsParseException() throws Exception {
        TypeHandler.createObject("java.lang.Integer");
    }

    // Tests createNumber with integer string returns Long
    @Test
    public void testCreateNumber_integerString_returnsLong() throws Exception {
        Number result = TypeHandler.createNumber("42");
        assertEquals(Long.valueOf(42), result);
    }

    // Tests createNumber with floating point string returns Double
    @Test
    public void testCreateNumber_floatingPointString_returnsDouble() throws Exception {
        Number result = TypeHandler.createNumber("42.5");
        assertEquals(Double.valueOf(42.5), result);
    }

    // Tests createNumber with invalid input throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateNumber_invalidString_throwsParseException() throws Exception {
        TypeHandler.createNumber("not-a-number");
    }

    // Tests createClass with valid class name
    @Test
    public void testCreateClass_validClassName_returnsClass() throws Exception {
        Class<?> clazz = TypeHandler.createClass("java.lang.String");
        assertEquals(String.class, clazz);
    }

    // Tests createClass with invalid class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateClass_unknownClass_throwsParseException() throws Exception {
        TypeHandler.createClass("org.apache.commons.cli.UnknownClass");
    }

    // Tests createDate throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateDate_anyString_throwsUnsupportedOperationException() {
        TypeHandler.createDate("2023-01-01");
    }

    // Tests createURL with valid URL string
    @Test
    public void testCreateURL_validUrl_returnsUrl() throws Exception {
        URL url = TypeHandler.createURL("http://commons.apache.org");
        assertNotNull(url);
        assertEquals("commons.apache.org", url.getHost());
    }

    // Tests createURL with malformed URL string throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateURL_malformedUrl_throwsParseException() throws Exception {
        TypeHandler.createURL("invalid://url:with:invalid:port");
    }

    // Tests createFile with valid path
    @Test
    public void testCreateFile_validPath_returnsFile() {
        File file = TypeHandler.createFile("test.txt");
        assertNotNull(file);
        assertEquals("test.txt", file.getName());
    }

    // Tests openFile with existing file returns FileInputStream
    @Test
    public void testOpenFile_existingFile_returnsInputStream() throws Exception {
        File temp = File.createTempFile("typehandler_test", ".tmp");
        temp.deleteOnExit();
        try {
            FileInputStream fis = TypeHandler.openFile(temp.getAbsolutePath());
            assertNotNull(fis);
            fis.close();
        } finally {
            temp.delete();
        }
    }

    // Tests openFile with non existing file throws ParseException
    @Test(expected = ParseException.class)
    public void testOpenFile_nonExistingFile_throwsParseException() throws Exception {
        TypeHandler.openFile("non_existing_file_for_cli_test.txt");
    }

    // Tests createFiles throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiles_anyString_throwsUnsupportedOperationException() {
        TypeHandler.createFiles("pattern");
    }
}