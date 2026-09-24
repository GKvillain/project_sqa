package com.fasterxml.jackson.databind;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonMappingExceptionTest {

    // Tests Reference constructor throwing NullPointerException when fieldName is null
    @Test(expected = NullPointerException.class)
    public void testReference_nullFieldName_throwsNullPointerException() {
        new JsonMappingException.Reference("fromObject", null);
    }

    // Tests Reference properties with field name
    @Test
    public void testReference_withFieldName_returnsCorrectValues() {
        String from = "testObject";
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, "myField");

        assertEquals(from, ref.getFrom());
        assertEquals("myField", ref.getFieldName());
        assertEquals(-1, ref.getIndex());
        assertEquals("java.lang.String[\"myField\"]", ref.getDescription());
        assertEquals("java.lang.String[\"myField\"]", ref.toString());
    }

    // Tests Reference properties with index
    @Test
    public void testReference_withIndex_returnsCorrectValues() {
        Integer from = 123;
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, 5);

        assertEquals(from, ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(5, ref.getIndex());
        assertEquals("java.lang.Integer[5]", ref.getDescription());
    }

    // Tests Reference formatting with Class instance and array class
    @Test
    public void testReference_withArrayClass_formatsCorrectDescription() {
        String[][] array = new String[1][1];
        JsonMappingException.Reference ref = new JsonMappingException.Reference(array, 0);

        assertEquals("java.lang.String[][0]", ref.getDescription());

        JsonMappingException.Reference classRef = new JsonMappingException.Reference(String[].class, "length");
        assertEquals("java.lang.String[][\"length\"]", classRef.getDescription());
    }

    // Tests Reference with null from and unknown field/index
    @Test
    public void testReference_withNullFromAndUnknownIndex_returnsUnknownDescription() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        ref.setIndex(-1);

        assertNull(ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
        assertEquals("UNKNOWN[?]", ref.getDescription());
    }

    // Tests Reference setters and writeReplace
    @Test
    public void testReference_settersAndWriteReplace_preservesDescription() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        ref.setFieldName("customField");
        ref.setIndex(2);
        ref.setDescription("customDescription");

        assertEquals("customDescription", ref.getDescription());
        assertSame(ref, ref.writeReplace());
    }

    // Tests deprecated constructors and basic exception message without path
    @Test
    public void testJsonMappingException_deprecatedConstructors_setsMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        JsonMappingException ex1 = new JsonMappingException("msg1");
        assertEquals("msg1", ex1.getMessage());
        assertNull(ex1.getCause());
        assertTrue(ex1.getPath().isEmpty());

        JsonMappingException ex2 = new JsonMappingException("msg2", cause);
        assertEquals("msg2", ex2.getMessage());
        assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = new JsonMappingException("msg3", null, cause);
        assertEquals("msg3", ex3.getMessage());
        assertSame(cause, ex3.getCause());
    }

    // Tests constructors with processor Closeable
    @Test
    public void testJsonMappingException_withProcessor_setsProcessor() {
        Closeable processor = new Closeable() {
            @Override
            public void close() throws IOException {}
        };
        Throwable cause = new IOException("io err");

        JsonMappingException ex = new JsonMappingException(processor, "msg", cause);
        assertSame(processor, ex.getProcessor());
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    // Tests factory method fromUnexpectedIOE
    @Test
    public void testFromUnexpectedIOE_validIOException_createsExpectedException() {
        IOException ioe = new IOException("network failure");
        JsonMappingException jme = JsonMappingException.fromUnexpectedIOE(ioe);

        assertNotNull(jme);
        assertTrue(jme.getMessage().contains("Unexpected IOException (of type java.io.IOException): network failure"));
    }

    // Tests wrapWithPath with non-JsonMappingException Throwable
    @Test
    public void testWrapWithPath_genericThrowable_wrapsAndAppendsPath() {
        Exception src = new Exception("original error");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(src, "sourceObject", "propA");

        assertNotNull(wrapped);
        assertSame(src, wrapped.getCause());
        List<JsonMappingException.Reference> path = wrapped.getPath();
        assertEquals(1, path.size());
        assertEquals("propA", path.get(0).getFieldName());
        assertTrue(wrapped.getMessage().contains("original error (through reference chain: java.lang.String[\"propA\"])"));
        assertTrue(wrapped.getLocalizedMessage().contains("original error"));
        assertTrue(wrapped.toString().startsWith("com.fasterxml.jackson.databind.JsonMappingException:"));
    }

    // Tests wrapWithPath with Throwable having null or empty message
    @Test
    public void testWrapWithPath_emptyMessageThrowable_usesPlaceholderMessage() {
        Exception src = new Exception("");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(src, "sourceObject", 0);

        assertTrue(wrapped.getMessage().contains("(was java.lang.Exception)"));
        assertEquals(1, wrapped.getPath().size());
        assertEquals(0, wrapped.getPath().get(0).getIndex());
    }

    // Tests wrapWithPath with existing JsonMappingException and multiple path prepends
    @Test
    public void testWrapWithPath_existingJsonMappingException_prependsPathsCorrectly() {
        JsonMappingException jme = new JsonMappingException("error");
        JsonMappingException.wrapWithPath(jme, "childObject", 3);
        JsonMappingException.wrapWithPath(jme, "parentObject", "items");

        List<JsonMappingException.Reference> path = jme.getPath();
        assertEquals(2, path.size());
        assertEquals("items", path.get(0).getFieldName());
        assertEquals(3, path.get(1).getIndex());

        String pathRef = jme.getPathReference();
        assertEquals("java.lang.String[\"items\"]->java.lang.String[3]", pathRef);
        assertTrue(jme.getMessage().contains("error (through reference chain: java.lang.String[\"items\"]->java.lang.String[3])"));
    }

    // Tests prependPath methods with referrer and field/index directly
    @Test
    public void testPrependPath_directMethods_addsReferences() {
        JsonMappingException jme = new JsonMappingException("error");
        jme.prependPath("ref1", 10);
        jme.prependPath("ref2", "field2");

        assertEquals(2, jme.getPath().size());
        assertEquals("field2", jme.getPath().get(0).getFieldName());
        assertEquals(10, jme.getPath().get(1).getIndex());
    }

    // Tests prependPath limit boundary at MAX_REFS_TO_LIST
    @Test
    public void testPrependPath_exceedingMaxRefsToList_stopsAdding() {
        JsonMappingException jme = new JsonMappingException("error");
        for (int i = 0; i < JsonMappingException.MAX_REFS_TO_LIST + 10; i++) {
            jme.prependPath("ref", i);
        }

        assertEquals(JsonMappingException.MAX_REFS_TO_LIST, jme.getPath().size());
    }

    // Tests getPathReference on empty path
    @Test
    public void testGetPathReference_emptyPath_returnsEmptyString() {
        JsonMappingException jme = new JsonMappingException("error");
        assertEquals("", jme.getPathReference());
        assertEquals(0, jme.getPath().size());
    }
}