package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonDeserializer;

public class JdkDeserializersTest {

    // Tests default constructor instantiation
    @Test
    public void testConstructor_default_instanceCreated() {
        JdkDeserializers deserializers = new JdkDeserializers();
        assertNotNull(deserializers);
    }

    // Tests finding deserializer for UUID
    @Test
    public void testFind_uuidType_returnsUUIDDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, UUID.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof UUIDDeserializer);
    }

    // Tests finding deserializer for StackTraceElement
    @Test
    public void testFind_stackTraceElementType_returnsStackTraceElementDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(StackTraceElement.class, StackTraceElement.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof StackTraceElementDeserializer);
    }

    // Tests finding deserializer for AtomicBoolean
    @Test
    public void testFind_atomicBooleanType_returnsAtomicBooleanDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(AtomicBoolean.class, AtomicBoolean.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof AtomicBooleanDeserializer);
    }

    // Tests finding deserializer for ByteBuffer
    @Test
    public void testFind_byteBufferType_returnsByteBufferDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(ByteBuffer.class, ByteBuffer.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof ByteBufferDeserializer);
    }

    // Tests finding deserializer for File (FromStringDeserializer type)
    @Test
    public void testFind_fileType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(File.class, File.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for URL (FromStringDeserializer type)
    @Test
    public void testFind_urlType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(URL.class, URL.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for URI (FromStringDeserializer type)
    @Test
    public void testFind_uriType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(URI.class, URI.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for Currency (FromStringDeserializer type)
    @Test
    public void testFind_currencyType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Currency.class, Currency.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for Pattern (FromStringDeserializer type)
    @Test
    public void testFind_patternType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Pattern.class, Pattern.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for Locale (FromStringDeserializer type)
    @Test
    public void testFind_localeType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Locale.class, Locale.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for unsupported JDK type
    @Test
    public void testFind_unsupportedType_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, String.class.getName());
        assertNull(deser);
    }

    // Tests finding deserializer for arbitrary non-JDK class
    @Test
    public void testFind_objectType_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Object.class, Object.class.getName());
        assertNull(deser);
    }

    // Tests when class name is not in supported class names set
    @Test
    public void testFind_unknownClassName_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, "com.unknown.ClassName");
        assertNull(deser);
    }

    // Tests when class name is in supported set but rawType is mismatched
    @Test
    public void testFind_mismatchedRawTypeAndClassName_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, UUID.class.getName());
        assertNull(deser);
    }

    // Tests null class name handling
    @Test
    public void testFind_nullClassName_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, null);
        assertNull(deser);
    }
}