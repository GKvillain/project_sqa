package com.fasterxml.jackson.databind;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonMappingExceptionTest {

    static class OuterClass {
        static class InnerClass {
        }
    }

    // Tests Reference constructor throwing NullPointerException when fieldName is null
    @Test(expected = NullPointerException.class)
    public void testReferenceConstructor_nullFieldName_throwsNullPointerException() {
        new Reference("source", null);
    }

    // Tests Reference.getDescription() when from is null
    @Test
    public void testReferenceGetDescription_nullFrom_returnsUnknown() {
        Reference ref = new Reference();
        ref.setFieldName("foo");
        assertEquals("UNKNOWN[\"foo\"]", ref.getDescription());
        assertEquals("UNKNOWN[\"foo\"]", ref.toString());
        assertNull(ref.getFrom());
        assertEquals("foo", ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    // Tests Reference.getDescription() with class and field name
    @Test
    public void testReferenceGetDescription_withClassAndFieldName_returnsCorrectDesc() {
        Reference ref = new Reference(String.class, "length");
        assertEquals("java.lang.String[\"length\"]", ref.getDescription());
        assertEquals("java.lang.String[\"length\"]", ref.toString());
        assertEquals(String.class, ref.getFrom());
        assertEquals("length", ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    // Tests Reference.getDescription() with instance object and index
    @Test
    public void testReferenceGetDescription_withInstanceAndIndex_returnsCorrectDesc() {
        Reference ref = new Reference("sampleString", 3);
        assertEquals("java.lang.String[3]", ref.getDescription());
        assertEquals("sampleString", ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(3, ref.getIndex());
    }

    // Tests Reference.getDescription() without fieldName or index
    @Test
    public void testReferenceGetDescription_withoutFieldOrIndex_returnsQuestionMark() {
        Reference ref = new Reference(Integer.class);
        assertEquals("java.lang.Integer[?]", ref.getDescription());
    }

    // Tests Reference.getDescription() for nested inner class
    @Test
    public void testReferenceGetDescription_nestedInnerClass_includesEnclosingClass() {
        Reference ref = new Reference(OuterClass.InnerClass.class, "nestedField");
        String desc = ref.getDescription();
        assertTrue("Description should contain enclosing class name: " + desc,
                desc.startsWith("com.fasterxml.jackson.databind.JsonMappingExceptionTest$OuterClass$InnerClass") ||
                desc.startsWith("com.fasterxml.jackson.databind.JsonMappingExceptionTest.OuterClass$InnerClass") ||
                desc.contains("OuterClass$InnerClass") ||
                desc.contains("OuterClass.InnerClass"));
        assertTrue(desc.endsWith("[\"nestedField\"]"));
    }

    // Tests Reference setters and writeReplace
    @Test
    public void testReference_settersAndWriteReplace_updatesValuesAndEnsuresDesc() {
        Reference ref = new Reference();
        ref.setFieldName("field1");
        ref.setIndex(5);
        ref.setDescription("customDesc");

        assertEquals("field1", ref.getFieldName());
        assertEquals(5, ref.getIndex());
        assertEquals("customDesc", ref.getDescription());

        Object replaced = ref.writeReplace();
        assertSame(ref, replaced);
    }

    // Tests wrapWithPath when source is already a JsonMappingException
    @Test
    public void testWrapWithPath_alreadyJsonMappingException_prependsReference() {
        JsonMappingException orig = new JsonMappingException("Original error");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(orig, "hostBean", "propA");

        assertSame(orig, wrapped);
        List<Reference> path = wrapped.getPath();
        assertEquals(1, path.size());
        assertEquals("propA", path.get(0).getFieldName());
        assertEquals("hostBean", path.get(0).getFrom());
        assertTrue(wrapped.getMessage().contains("Original error (through reference chain: java.lang.String[\"propA\"])"));
    }

    // Tests wrapWithPath when source is a generic Throwable
    @Test
    public void testWrapWithPath_genericThrowable_wrapsAndPrependsPath() {
        NullPointerException npe = new NullPointerException("NPE occurred");
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(npe, "hostBean", 2);

        assertNotNull(wrapped);
        assertSame(npe, wrapped.getCause());
        List<Reference> path = wrapped.getPath();
        assertEquals(1, path.size());
        assertEquals(2, path.get(0).getIndex());
        assertTrue(wrapped.getMessage().contains("NPE occurred (through reference chain: java.lang.String[2])"));
    }

    // Tests wrapWithPath when source throwable has null/empty message
    @Test
    public void testWrapWithPath_throwableWithNullMessage_usesPlaceholder() {
        RuntimeException ex = new RuntimeException((String) null);
        JsonMappingException wrapped = JsonMappingException.wrapWithPath(ex, new Reference("srcObj", "foo"));

        assertNotNull(wrapped);
        assertTrue(wrapped.getMessage().startsWith("(was java.lang.RuntimeException)"));
        assertTrue(wrapped.getMessage().contains("(through reference chain: java.lang.String[\"foo\"])"));
    }

    // Tests prependPath ordering and building chained message
    @Test
    public void testPrependPath_multipleReferences_orderedCorrectly() {
        JsonMappingException ex = new JsonMappingException("Mapping failed");
        ex.prependPath("first", "field1");
        ex.prependPath("second", 0);
        ex.prependPath(new Reference("third", "field3"));

        List<Reference> path = ex.getPath();
        assertEquals(3, path.size());
        assertEquals("field3", path.get(0).getFieldName());
        assertEquals(0, path.get(1).getIndex());
        assertEquals("field1", path.get(2).getFieldName());

        String pathRef = ex.getPathReference();
        assertEquals("java.lang.String[\"field3\"]->java.lang.String[0]->java.lang.String[\"field1\"]", pathRef);

        String fullMsg = ex.getMessage();
        assertEquals("Mapping failed (through reference chain: " + pathRef + ")", fullMsg);
        assertEquals(fullMsg, ex.getLocalizedMessage());
        assertTrue(ex.toString().contains(fullMsg));
    }

    // Tests boundary for MAX_REFS_TO_LIST when prepending paths
    @Test
    public void testPrependPath_exceedsMaxLimit_limitsSizeToMax() {
        JsonMappingException ex = new JsonMappingException("Recursion limit test");
        for (int i = 0; i < JsonMappingException.MAX_REFS_TO_LIST + 10; i++) {
            ex.prependPath("obj", i);
        }
        assertEquals(JsonMappingException.MAX_REFS_TO_LIST, ex.getPath().size());
    }

    // Tests getPath when no references added
    @Test
    public void testGetPath_emptyPath_returnsEmptyListAndOriginalMessage() {
        JsonMappingException ex = new JsonMappingException("Basic error");
        assertNotNull(ex.getPath());
        assertTrue(ex.getPath().isEmpty());
        assertEquals("", ex.getPathReference());
        assertEquals("Basic error", ex.getMessage());
    }

    // Tests fromUnexpectedIOE factory method
    @Test
    public void testFromUnexpectedIOE_formatsErrorMessage() {
        IOException ioe = new IOException("Disk error");
        JsonMappingException ex = JsonMappingException.fromUnexpectedIOE(ioe);
        assertNotNull(ex);
        assertEquals("Unexpected IOException (of type java.io.IOException): Disk error", ex.getMessage());
    }

    // Tests constructors with processor, location, and cause
    @Test
    public void testConstructors_withProcessorLocationAndCause_setsProperties() {
        Closeable processor = new Closeable() {
            @Override
            public void close() throws IOException { }
        };
        JsonLocation loc = new JsonLocation("src", 100L, 1, 1);
        Throwable cause = new IllegalStateException("bad state");

        JsonMappingException ex1 = new JsonMappingException(processor, "msg1");
        assertSame(processor, ex1.getProcessor());
        assertNull(ex1.getCause());

        JsonMappingException ex2 = new JsonMappingException(processor, "msg2", cause);
        assertSame(processor, ex2.getProcessor());
        assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = new JsonMappingException(processor, "msg3", loc);
        assertSame(processor, ex3.getProcessor());
        assertEquals(loc, ex3.getLocation());

        JsonMappingException ex4 = new JsonMappingException("msg4", loc);
        assertEquals(loc, ex4.getLocation());

        JsonMappingException ex5 = new JsonMappingException("msg5", loc, cause);
        assertEquals(loc, ex5.getLocation());
        assertSame(cause, ex5.getCause());
    }
}