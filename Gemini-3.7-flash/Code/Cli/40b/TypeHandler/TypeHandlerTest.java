package org.apache.commons.cli;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeHandlerTest {

    // Tests createValue with PatternOptionBuilder.STRING_VALUE
    @Test
    public void testCreateValue_stringValue_returnsString() throws Exception {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
    }

    // Tests createValue with PatternOptionBuilder.OBJECT_VALUE
    @Test
    public void testCreateValue_objectValue_returnsInstance() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    // Tests createValue with PatternOptionBuilder.NUMBER_VALUE for integer
    @Test
    public void testCreateValue_numberValueInteger_returnsLong() throws Exception {
        Object result = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Long.valueOf(123), result);
    }

    // Tests createValue with PatternOptionBuilder.NUMBER_VALUE for double
    @Test
    public void testCreateValue_numberValueDouble_returnsDouble() throws Exception {
        Object result = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Double.valueOf(123.45), result);
    }

    // Tests createValue with PatternOptionBuilder.CLASS_VALUE
    @Test
    public void testCreateValue_classValue_returnsClass() throws Exception {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
    }

    // Tests createValue with PatternOptionBuilder.FILE_VALUE
    @Test
    public void testCreateValue_fileValue_returnsFile() throws Exception {
        Object result = TypeHandler.createValue("somefile.txt", PatternOptionBuilder.FILE_VALUE);
        assertEquals(new File("somefile.txt"), result);
    }

    // Tests createValue with PatternOptionBuilder.URL_VALUE
    @Test
    public void testCreateValue_urlValue_returnsURL() throws Exception {
        Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertEquals(new URL("http://commons.apache.org"), result);
    }

    // Tests createValue with unhandled class type returns null
    @Test
    public void testCreateValue_unhandledType_returnsNull() throws Exception {
        Object result = TypeHandler.createValue("test", Integer.class);
        assertNull(result);
    }

    // Tests createValue with Object overload
    @Test
    public void testCreateValue_objectTypeOverload_returnsCorrectValue() throws Exception {
        Object result = TypeHandler.createValue("123", (Object) PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Long.valueOf(123), result);
    }

    // Tests createObject with valid class name
    @Test
    public void testCreateObject_validClassName_returnsNewInstance() throws Exception {
        Object result = TypeHandler.createObject("java.lang.String");
        assertNotNull(result);
        assertEquals("", result);
    }

    // Tests createObject with non-existing class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_unknownClass_throwsParseException() throws Exception {
        TypeHandler.createObject("org.apache.commons.cli.NonExistingClass");
    }

    // Tests createNumber with integer string returns Long
    @Test
    public void testCreateNumber_integerString_returnsLong() throws Exception {
        Number number = TypeHandler.createNumber("42");
        assertEquals(Long.valueOf(42), number);
    }

    // Tests createNumber with decimal string returns Double
    @Test
    public void testCreateNumber_decimalString_returnsDouble() throws Exception {
        Number number = TypeHandler.createNumber("42.5");
        assertEquals(Double.valueOf(42.5), number);
    }

    // Tests createNumber with invalid string throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateNumber_invalidFormat_throwsParseException() throws Exception {
        TypeHandler.createNumber("not_a_number");
    }

    // Tests createClass with valid class name
    @Test
    public void testCreateClass_validClassName_returnsClass() throws Exception {
        Class<?> clazz = TypeHandler.createClass("java.lang.Integer");
        assertEquals(Integer.class, clazz);
    }

    // Tests createClass with unknown class name throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateClass_unknownClass_throwsParseException() throws Exception {
        TypeHandler.createClass("org.apache.commons.cli.UnknownClass");
    }

    // Tests createDate always throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateDate_anyString_throwsUnsupportedOperationException() {
        TypeHandler.createDate("2023-01-01");
    }

    // Tests createURL with valid URL string
    @Test
    public void testCreateURL_validURL_returnsURL() throws Exception {
        URL url = TypeHandler.createURL("https://apache.org");
        assertEquals("https://apache.org", url.toString());
    }

    // Tests createURL with malformed URL throws ParseException
    @Test(expected = ParseException.class)
    public void testCreateURL_malformedURL_throwsParseException() throws Exception {
        TypeHandler.createURL("invalid://url:port");
    }

    // Tests createFile with string path
    @Test
    public void testCreateFile_validPath_returnsFile() {
        File file = TypeHandler.createFile("test.txt");
        assertEquals("test.txt", file.getPath());
    }

    // Tests openFile with non-existing file throws ParseException
    @Test(expected = ParseException.class)
    public void testOpenFile_nonExistingFile_throwsParseException() throws Exception {
        TypeHandler.openFile("non_existing_file_for_cli_test_12345.txt");
    }

    // Tests createFiles always throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiles_anyString_throwsUnsupportedOperationException() {
        TypeHandler.createFiles("*.txt");
    }
}