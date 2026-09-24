package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonDeserializer;

public class JdkDeserializersTest {

    // Tests instantiation of the container class
    @Test
    public void testConstructor_default_instanceCreated() {
        JdkDeserializers deserializers = new JdkDeserializers();
        assertNotNull(deserializers);
    }

    // Tests finding deserializer for UUID
    @Test
    public void testFind_uuidType_returnsUuidDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, UUID.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof UUIDDeserializer || deser instanceof FromStringDeserializer);
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

    // Tests finding deserializer for File (FromStringDeserializer type)
    @Test
    public void testFind_fileType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(File.class, File.class.getName());
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

    // Tests finding deserializer for InetAddress (FromStringDeserializer type)
    @Test
    public void testFind_inetAddressType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(InetAddress.class, InetAddress.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for InetSocketAddress (FromStringDeserializer type)
    @Test
    public void testFind_inetSocketAddressType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(InetSocketAddress.class, InetSocketAddress.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for TimeZone (FromStringDeserializer type)
    @Test
    public void testFind_timeZoneType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(TimeZone.class, TimeZone.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests finding deserializer for Charset (FromStringDeserializer type)
    @Test
    public void testFind_charsetType_returnsFromStringDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Charset.class, Charset.class.getName());
        assertNotNull(deser);
        assertTrue(deser instanceof FromStringDeserializer);
    }

    // Tests non-JDK custom/unsupported class returns null
    @Test
    public void testFind_unsupportedClass_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, String.class.getName());
        assertNull(deser);
    }

    // Tests supported class name with unmatched rawType returns null
    @Test
    public void testFind_supportedNameWithMismatchedRawType_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, UUID.class.getName());
        assertNull(deser);
    }

    // Tests unknown class name returns null
    @Test
    public void testFind_unknownClassName_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, "com.example.UnknownClass");
        assertNull(deser);
    }

    // Tests null class name returns null
    @Test
    public void testFind_nullClassName_returnsNull() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, null);
        assertNull(deser);
    }
}