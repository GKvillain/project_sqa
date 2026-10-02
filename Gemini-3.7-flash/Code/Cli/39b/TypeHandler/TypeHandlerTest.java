package org.apache.commons.cli;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeHandlerTest
{
    // Tests createValue with String type
    @Test
    public void testCreateValue_stringValue_returnsString() throws Exception
    {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
    }

    // Tests createValue with Object type
    @Test
    public void testCreateValue_objectValue_returnsInstance() throws Exception
    {
        Object result = TypeHandler.createValue("java.util.ArrayList", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
    }

    // Tests createValue with Number type
    @Test
    public void testCreateValue_numberValue_returnsNumber() throws Exception
    {
        Object result = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertEquals(Long.valueOf(123), result);
    }

    // Tests createValue with Class type
    @Test
    public void testCreateValue_classValue_returnsClass() throws Exception
    {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertEquals(String.class, result);
    }

    // Tests createValue with File type
    @Test
    public void testCreateValue_fileValue_returnsFile() throws Exception
    {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        assertEquals(new File("test.txt"), result);
    }

    // Tests createValue with Existing File type
    @Test
    public void testCreateValue_existingFileValue_returnsFile() throws Exception
    {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertEquals(new File("test.txt"), result);
    }

    // Tests createValue with URL type
    @Test
    public void testCreateValue_urlValue_returnsURL() throws Exception
    {
        Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertEquals(new URL("http://commons.apache.org"), result);
    }

    // Tests createValue with unknown class type returning null
    @Test
    public void testCreateValue_unknownType_returnsNull() throws Exception
    {
        Object result = TypeHandler.createValue("value", Void.class);
        assertNull(result);
    }

    // Tests createValue delegating from Object parameter to Class parameter
    @Test
    public void testCreateValue_objectParam_delegatesToClassParam() throws Exception
    {
        Object result = TypeHandler.createValue("testString", (Object) PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
    }

    // Tests createObject with valid class name
    @Test
    public void testCreateObject_validClass_returnsInstance() throws Exception
    {
        Object result = TypeHandler.createObject("java.util.ArrayList");
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);
    }

    // Tests createObject with non-existent class throwing ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_classNotFound_throwsParseException() throws Exception
    {
        TypeHandler.createObject("non.existing.ClassName");
    }

    // Tests createObject with class lacking empty constructor throwing ParseException
    @Test(expected = ParseException.class)
    public void testCreateObject_noDefaultConstructor_throwsParseException() throws Exception
    {
        TypeHandler.createObject("java.lang.Integer");
    }

    // Tests createNumber with integer string returning Long
    @Test
    public void testCreateNumber_integerString_returnsLong() throws Exception
    {
        Number result = TypeHandler.createNumber("42");
        assertEquals(Long.valueOf(42), result);
    }

    // Tests createNumber with decimal string returning Double
    @Test
    public void testCreateNumber_decimalString_returnsDouble() throws Exception
    {
        Number result = TypeHandler.createNumber("42.5");
        assertEquals(Double.valueOf(42.5), result);
    }

    // Tests createNumber with invalid string throwing ParseException
    @Test(expected = ParseException.class)
    public void testCreateNumber_invalidString_throwsParseException() throws Exception
    {
        TypeHandler.createNumber("invalidNumber");
    }

    // Tests createClass with valid class name
    @Test
    public void testCreateClass_validClass_returnsClass() throws Exception
    {
        Class<?> result = TypeHandler.createClass("java.lang.String");
        assertEquals(String.class, result);
    }

    // Tests createClass with non-existent class throwing ParseException
    @Test(expected = ParseException.class)
    public void testCreateClass_classNotFound_throwsParseException() throws Exception
    {
        TypeHandler.createClass("non.existing.ClassName");
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

    // Tests createURL with malformed URL throwing ParseException
    @Test(expected = ParseException.class)
    public void testCreateURL_malformedURL_throwsParseException() throws Exception
    {
        TypeHandler.createURL("malformed url");
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