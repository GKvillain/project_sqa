package org.apache.commons.cli;

import java.io.File;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeHandlerTest
{
    // Tests createValue with String type
    @Test
    public void testCreateValue_stringType_returnsString() throws Exception
    {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
    }

    // Tests createValue with Object type
    @Test
    public void testCreateValue_objectType_returnsObject() throws Exception
    {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
    }

    // Tests createValue with Number type
    @Test
    public void testCreateValue_numberType_returnsNumber() throws Exception
    {
        Object result = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Long.valueOf(123), result);
    }

    // Tests createValue with Date type
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_dateType_throwsUnsupportedOperationException() throws Exception
    {
        TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    // Tests createValue with Class type
    @Test
    public void testCreateValue_classType_returnsClass() throws Exception
    {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
    }

    // Tests createValue with File type
    @Test
    public void testCreateValue_fileType_returnsFile() throws Exception
    {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        assertEquals(new File("test.txt"), result);
    }

    // Tests createValue with Existing File type
    @Test
    public void testCreateValue_existingFileType_returnsFile() throws Exception
    {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertEquals(new File("test.txt"), result);
    }

    // Tests createValue with Files type
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValue_filesType_throwsUnsupportedOperationException() throws Exception
    {
        TypeHandler.createValue("test.txt", PatternOptionBuilder.FILES_VALUE);
    }

    // Tests createValue with URL type
    @Test
    public void testCreateValue_urlType_returnsURL() throws Exception
    {
        Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertEquals(new URL("http://commons.apache.org"), result);
    }

    // Tests createValue with unhandled/unknown class type
    @Test
    public void testCreateValue_unknownType_returnsNull() throws Exception
    {
        Object result = TypeHandler.createValue("test", Integer.class);
        assertNull(result);
    }

    // Tests createValue with Object parameter overload
    @Test
    public void testCreateValue_objectParamOverload_returnsValue() throws Exception
    {
        Object result = TypeHandler.createValue("test", (Object) PatternOptionBuilder.STRING_VALUE);
        assertEquals("test", result);
    }

    // Tests createObject with valid class name having a default constructor
    @Test
    public void testCreateObject_validClass_instantiatesObject() throws Exception
    {
        Object result = TypeHandler.createObject("java.lang.String");
        assertNotNull(result);
        assertEquals("", result);
    }

    // Tests createObject with non-existent class name
    @Test(expected = ParseException.class)
    public void testCreateObject_invalidClass_throwsParseException() throws Exception
    {
        TypeHandler.createObject("non.existent.ClassName");
    }

    // Tests createObject with class lacking accessible default constructor
    @Test(expected = ParseException.class)
    public void testCreateObject_noDefaultConstructor_throwsParseException() throws Exception
    {
        TypeHandler.createObject("java.lang.System");
    }

    // Tests createNumber with integer string returning Long
    @Test
    public void testCreateNumber_integerString_returnsLong() throws Exception
    {
        Number result = TypeHandler.createNumber("12345");
        assertEquals(Long.valueOf(12345), result);
    }

    // Tests createNumber with decimal string returning Double
    @Test
    public void testCreateNumber_decimalString_returnsDouble() throws Exception
    {
        Number result = TypeHandler.createNumber("123.45");
        assertEquals(Double.valueOf(123.45), result);
    }

    // Tests createNumber with invalid number string
    @Test(expected = ParseException.class)
    public void testCreateNumber_invalidString_throwsParseException() throws Exception
    {
        TypeHandler.createNumber("not-a-number");
    }

    // Tests createClass with valid class name
    @Test
    public void testCreateClass_validClassName_returnsClass() throws Exception
    {
        Class<?> result = TypeHandler.createClass("java.lang.String");
        assertEquals(String.class, result);
    }

    // Tests createClass with invalid class name
    @Test(expected = ParseException.class)
    public void testCreateClass_invalidClassName_throwsParseException() throws Exception
    {
        TypeHandler.createClass("invalid.class.Name");
    }

    // Tests createDate throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateDate_anyString_throwsUnsupportedOperationException()
    {
        TypeHandler.createDate("2023-01-01");
    }

    // Tests createURL with valid URL string
    @Test
    public void testCreateURL_validURL_returnsURL() throws Exception
    {
        URL result = TypeHandler.createURL("http://commons.apache.org");
        assertEquals(new URL("http://commons.apache.org"), result);
    }

    // Tests createURL with invalid URL string
    @Test(expected = ParseException.class)
    public void testCreateURL_invalidURL_throwsParseException() throws Exception
    {
        TypeHandler.createURL("invalid://url test");
    }

    // Tests createFile with string path
    @Test
    public void testCreateFile_validPath_returnsFile()
    {
        File result = TypeHandler.createFile("path/to/file.txt");
        assertEquals(new File("path/to/file.txt"), result);
    }

    // Tests createFiles throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiles_anyString_throwsUnsupportedOperationException()
    {
        TypeHandler.createFiles("path/to/files");
    }
}