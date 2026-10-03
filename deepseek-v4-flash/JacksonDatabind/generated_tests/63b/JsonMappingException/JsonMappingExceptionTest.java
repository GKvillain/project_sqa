package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

/**
 * JUnit 4 test class for JsonMappingException (Defects4J Bug 63b)
 */
public class JsonMappingExceptionTest {

    // Tests default constructor with message only (deprecated but present)
    @Test
    public void testConstructor_deprecatedMsg_setsMessage() {
        JsonMappingException e = new JsonMappingException("test msg");
        assertEquals("test msg", e.getMessage());
    }

    // Tests constructor with closeable processor (JsonParser is Closeable) and message
    @Test
    public void testConstructor_withProcessor_setsProcessor() {
        // Cannot easily instantiate real JsonParser here; use null processor
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "test");
        assertNull(e.getProcessor());
        assertEquals("test", e.getMessage());
    }

    // Tests from(JsonParser, String) factory method
    @Test
    public void testFrom_withParser_setsMessage() {
        // Use null parser since we cannot instantiate real one in test
        JsonMappingException e = JsonMappingException.from((JsonParser) null, "from parser");
        assertNotNull(e);
        assertEquals("from parser", e.getMessage());
    }

    // Tests from(JsonGenerator, String) factory method
    @Test
    public void testFrom_withGenerator_setsMessage() {
        JsonMappingException e = JsonMappingException.from((JsonGenerator) null, "from gen");
        assertNotNull(e);
        assertEquals("from gen", e.getMessage());
    }

    // Tests from(DeserializationContext, String) factory method
    @Test
    public void testFrom_withDeserializationContext_setsMessage() {
        // Use null context; internally ctxt.getParser() will return null, but that's acceptable
        JsonMappingException e = JsonMappingException.from((DeserializationContext) null, "from ctxt");
        assertNotNull(e);
        assertEquals("from ctxt", e.getMessage());
    }

    // Tests getPath() returns empty list when no path set
    @Test
    public void testGetPath_noPath_returnsEmptyList() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "no path");
        List<JsonMappingException.Reference> path = e.getPath();
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    // Tests prependPath with Reference and then getPath returns the reference
    @Test
    public void testPrependPath_andGetPath_returnsPath() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "path test");
        e.prependPath(new JsonMappingException.Reference("fromObj", "fieldName"));
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals("fieldName", path.get(0).getFieldName());
        assertEquals(-1, path.get(0).getIndex());
    }

    // Tests prependPath with (Object, String) overload
    @Test
    public void testPrependPath_withObjectField_addsReference() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "obj ref");
        e.prependPath("referrer", "field1");
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals("field1", path.get(0).getFieldName());
    }

    // Tests prependPath with (Object, int) overload
    @Test
    public void testPrependPath_withObjectIndex_addsReference() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "index ref");
        e.prependPath("referrer", 42);
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals(42, path.get(0).getIndex());
        assertNull(path.get(0).getFieldName());
    }

    // Tests getPathReference returns string representation of path
    @Test
    public void testGetPathReference_withPath_returnsFormattedString() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "path ref");
        e.prependPath("obj1", "fieldA");
        e.prependPath("obj2", "fieldB");
        String pathRef = e.getPathReference();
        // Since prependPath adds to front, first added is last in chain
        // Order: obj2[fieldB]->obj1[fieldA]
        assertTrue(pathRef.contains("fieldA"));
        assertTrue(pathRef.contains("fieldB"));
        assertTrue(pathRef.contains("->"));
    }

    // Tests wrapWithPath when source is not JsonMappingException (creates new one)
    @Test
    public void testWrapWithPath_nonMappingException_createsMappingException() {
        IOException original = new IOException("original IO error");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(original, "fromObj", "fieldX");
        assertNotNull(wrapped);
        // Message should contain original message
        assertTrue(wrapped.getMessage().contains("original IO error"));
        // Should have path reference
        List<JsonMappingException.Reference> path = wrapped.getPath();
        assertEquals(1, path.size());
        assertEquals("fieldX", path.get(0).getFieldName());
    }

    // Tests wrapWithPath when source is already JsonMappingException (augments it)
    @Test
    public void testWrapWithPath_existingMappingException_addsReference() {
        JsonMappingException original = new JsonMappingException((java.io.Closeable) null, "original mapping");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(original, "obj", "fieldY");
        assertSame(original, wrapped);
        List<JsonMappingException.Reference> path = wrapped.getPath();
        assertEquals(1, path.size());
        assertEquals("fieldY", path.get(0).getFieldName());
    }

    // Tests wrapWithPath using index variant
    @Test
    public void testWrapWithPath_withIndex_addsReference() {
        JsonMappingException result = JsonMappingException.wrapWithPath(new IOException("ioe"), "arrObj", 5);
        assertNotNull(result);
        List<JsonMappingException.Reference> path = result.getPath();
        assertEquals(1, path.size());
        assertEquals(5, path.get(0).getIndex());
    }

    // Tests fromUnexpectedIOE factory method
    @Test
    public void testFromUnexpectedIOE_createsMappingException() {
        IOException ioe = new IOException("unexpected");
        JsonMappingException e = JsonMappingException.fromUnexpectedIOE(ioe);
        assertNotNull(e);
        assertTrue(e.getMessage().contains("unexpected"));
        assertTrue(e.getMessage().contains("IOException"));
    }

    // Tests getPathReference via StringBuilder variant
    @Test
    public void testGetPathReference_stringBuilder_appendsPath() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "sb test");
        e.prependPath("o1", "f1");
        StringBuilder sb = new StringBuilder("prefix");
        e.getPathReference(sb);
        String result = sb.toString();
        assertTrue(result.startsWith("prefix"));
        assertTrue(result.contains("f1"));
    }

    // Tests Reference constructor with Object only
    @Test
    public void testReference_constructorWithFrom_setsFrom() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("myObj");
        assertEquals("myObj", ref.getFrom());
        assertEquals(-1, ref.getIndex());
        assertNull(ref.getFieldName());
    }

    // Tests Reference constructor with from and fieldName
    @Test
    public void testReference_constructorWithField_setsField() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("obj", "field");
        assertEquals("field", ref.getFieldName());
    }

    // Tests Reference getDescription for non-null from object
    @Test
    public void testReference_getDescription_returnsFormattedString() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("testObj", "myField");
        String desc = ref.getDescription();
        assertTrue(desc.contains("myField"));
        assertTrue(desc.contains("testObj"));
        assertTrue(desc.contains("["));
        assertTrue(desc.contains("]"));
    }

    // Tests Reference getDescription when _from is a Class (unusual but possible)
    @Test
    public void testReference_getDescription_withClassFrom_returnsClassName() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference(String.class, "value");
        String desc = ref.getDescription();
        assertTrue(desc.contains("String"));
        assertTrue(desc.contains("value"));
    }

    // Tests Reference getIndex returns -1 when not set
    @Test
    public void testReference_getIndex_defaultIsMinusOne() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("obj", "f");
        assertEquals(-1, ref.getIndex());
    }

    // Tests Reference getFieldName returns null when not set
    @Test
    public void testReference_getFieldName_defaultIsNull() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("obj", 3);
        assertNull(ref.getFieldName());
    }

    // Tests Reference toString returns description
    @Test
    public void testReference_toString_returnsDescription() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("x", "y");
        assertEquals(ref.getDescription(), ref.toString());
    }

    // Tests _buildMessage includes path reference when path exists
    @Test
    public void testMessage_withPath_includesReference() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "base msg");
        e.prependPath("refObj", "refField");
        String msg = e.getMessage();
        assertTrue(msg.contains("base msg"));
        assertTrue(msg.contains("through reference chain"));
        assertTrue(msg.contains("refField"));
    }

    // Tests getPath when path is present returns unmodifiable list
    @Test
    public void testGetPath_withPath_returnsUnmodifiableList() {
        JsonMappingException e = new JsonMappingException((java.io.Closeable) null, "unmod");
        e.prependPath("o", "f");
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        try {
            path.add(new JsonMappingException.Reference("extra"));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    // Tests Reference writeReplace triggers description generation (no assertion failure)
    @Test
    public void testReference_writeReplace_generatesDescription() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("obj", "field");
        Object result = ref.writeReplace();
        assertNotNull(result);
        // writeReplace should return this after calling getDescription
        assertSame(ref, result);
        assertNotNull(ref.getDescription()); // ensure description is now set
    }
}