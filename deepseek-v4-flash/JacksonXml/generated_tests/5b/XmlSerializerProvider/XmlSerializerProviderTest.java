package com.fasterxml.jackson.dataformat.xml.ser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.namespace.QName;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

/**
 * JUnit 4 test class for XmlSerializerProvider.
 * Focuses on defect detection, branch coverage, and regression testing.
 */
public class XmlSerializerProviderTest {

    // Helper: create a basic provider with default XmlRootNameLookup
    private XmlSerializerProvider createBasicProvider() {
        return new XmlSerializerProvider(new XmlRootNameLookup());
    }

    // Tests that constructor with XmlRootNameLookup works
    @Test
    public void testConstructor_withRootNameLookup_createsInstance() {
        XmlSerializerProvider provider = createBasicProvider();
        assertNotNull(provider);
    }

    // Tests copy() returns a new instance (not same object)
    @Test
    public void testCopy_returnsNewInstance() {
        XmlSerializerProvider provider = createBasicProvider();
        DefaultSerializerProvider copy = provider.copy();
        assertNotNull(copy);
        assertTrue(copy instanceof XmlSerializerProvider);
        assertNotSame(provider, copy);
    }

    // Tests createInstance() returns a new instance
    @Test
    public void testCreateInstance_returnsNewInstance() {
        XmlSerializerProvider provider = createBasicProvider();
        // Use default config and factory from provider itself (not important for identity)
        DefaultSerializerProvider instance = provider.createInstance(
                provider.getConfig(), provider.getFactory());
        assertNotNull(instance);
        assertTrue(instance instanceof XmlSerializerProvider);
        assertNotSame(provider, instance);
    }

    // Tests _asXmlGenerator with TokenBuffer returns null (convertValue path)
    @Test
    public void testAsXmlGenerator_withTokenBuffer_returnsNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer buf = new TokenBuffer(mapper);
        // Simulate a parser-less buffer (just close to avoid resource leak)
        buf.close();
        ToXmlGenerator result = provider._asXmlGenerator(buf);
        assertNull(result);
    }

    // Tests _asXmlGenerator with invalid generator (not ToXmlGenerator, not TokenBuffer) throws exception
    @Test(expected = JsonMappingException.class)
    public void testAsXmlGenerator_withInvalidGenerator_throwsException() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        try {
            provider._asXmlGenerator(gen);
        } finally {
            gen.close();
        }
    }

    // Tests _serializeXmlNull with non-ToXmlGenerator writes "null"
    @Test
    public void testSerializeXmlNull_withNonToXmlGenerator_writesNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        try {
            provider._serializeXmlNull(gen);
            gen.flush();
            String output = sw.toString();
            assertTrue(output.contains("null"));
        } finally {
            gen.close();
        }
    }

    // Tests _wrapAsIOE with IOException returns same exception
    @Test
    public void testWrapAsIOE_withIOException_returnsSame() {
        XmlSerializerProvider provider = createBasicProvider();
        IOException orig = new IOException("test");
        IOException wrapped = provider._wrapAsIOE(null, orig);
        assertSame(orig, wrapped);
    }

    // Tests _wrapAsIOE with non-IOException returns JsonMappingException
    @Test
    public void testWrapAsIOE_withOtherException_returnsJsonMappingException() {
        XmlSerializerProvider provider = createBasicProvider();
        Exception other = new RuntimeException("other");
        IOException wrapped = provider._wrapAsIOE(null, other);
        assertTrue(wrapped instanceof JsonMappingException);
        assertEquals("other", wrapped.getMessage());
    }

    // Tests _wrapAsIOE with non-IOException and null message uses default
    @Test
    public void testWrapAsIOE_withOtherExceptionNullMessage_usesDefault() {
        XmlSerializerProvider provider = createBasicProvider();
        Exception other = new RuntimeException();
        IOException wrapped = provider._wrapAsIOE(null, other);
        assertTrue(wrapped instanceof JsonMappingException);
        assertNotNull(wrapped.getMessage());
        assertTrue(wrapped.getMessage().contains("no message"));
    }

    // Tests serializeValue with null value (calls _serializeXmlNull)
    @Test
    public void testSerializeValue_null_serializesNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        try {
            provider.serializeValue(gen, null);
            gen.flush();
            String output = sw.toString();
            // Should contain "null" (JSON literal)
            assertTrue(output.contains("null"));
        } finally {
            gen.close();
        }
    }

    // Tests serializeValue with null value and a JsonGenerator that is TokenBuffer (convertValue)
    @Test
    public void testSerializeValue_nullWithTokenBuffer_serializesNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer buf = new TokenBuffer(mapper);
        try {
            provider.serializeValue(buf, null);
            // TokenBuffer should contain a null token
            JsonParser p = buf.asParser();
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
            p.close();
        } finally {
            buf.close();
        }
    }

    // ========== New tests for uncovered branches ==========

    // Tests _asXmlGenerator when provided with a ToXmlGenerator returns the same generator
    @Test
    public void testAsXmlGenerator_withToXmlGenerator_returnsSame() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        XmlFactory xmlFactory = new XmlFactory();
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = xmlFactory.createGenerator(sw);
        try {
            ToXmlGenerator result = provider._asXmlGenerator(gen);
            assertSame(gen, result);
        } finally {
            gen.close();
        }
    }

    // Tests _serializeXmlNull when provided with a ToXmlGenerator writes XML null element
    @Test
    public void testSerializeXmlNull_withToXmlGenerator_writesXmlNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        XmlFactory xmlFactory = new XmlFactory();
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = xmlFactory.createGenerator(sw);
        try {
            provider._serializeXmlNull(gen);
            gen.flush();
            String output = sw.toString();
            // Expect XML null element (e.g., <null/>)
            assertTrue("Expected XML null element, but got: " + output, output.contains("<null"));
        } finally {
            gen.close();
        }
    }

    // Tests serializeValue with a String value on a ToXmlGenerator writes XML element with value
    @Test
    public void testSerializeValue_stringWithToXmlGenerator_writesXml() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        XmlFactory xmlFactory = new XmlFactory();
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = xmlFactory.createGenerator(sw);
        try {
            provider.serializeValue(gen, "hello");
            gen.flush();
            String output = sw.toString();
            // Should contain the string value
            assertTrue("Output should contain 'hello'", output.contains("hello"));
            // Should contain start tag (root name likely "String")
            assertTrue("Output should contain <String> or similar root element",
                       output.contains("<String>") || output.contains("<string>"));
        } finally {
            gen.close();
        }
    }

    // Tests serializeValue with a String value on a TokenBuffer (non-XML generator) writes token
    @Test
    public void testSerializeValue_stringWithTokenBuffer_writesToken() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer buf = new TokenBuffer(mapper);
        try {
            provider.serializeValue(buf, "hello");
            JsonParser p = buf.asParser();
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("hello", p.getText());
            p.close();
        } finally {
            buf.close();
        }
    }

    // Tests serializeValue with null value on a ToXmlGenerator writes XML null element
    @Test
    public void testSerializeValue_nullWithToXmlGenerator_writesXmlNull() throws IOException {
        XmlSerializerProvider provider = createBasicProvider();
        XmlFactory xmlFactory = new XmlFactory();
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = xmlFactory.createGenerator(sw);
        try {
            provider.serializeValue(gen, null);
            gen.flush();
            String output = sw.toString();
            // Should contain XML null element
            assertTrue("Expected XML null element", output.contains("<null"));
        } finally {
            gen.close();
        }
    }
}