package com.fasterxml.jackson.databind.deser.std;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.junit.Test;
import static org.junit.Assert.*;

public class FromStringDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests types() method returns all supported types
    @Test
    public void testTypes_returnsSupportedClassArray() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(12, types.length);
    }

    // Tests findDeserializer() for supported types
    @Test
    public void testFindDeserializer_supportedTypes_returnsDeserializer() {
        for (Class<?> type : FromStringDeserializer.types()) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(type);
            assertNotNull(deser);
        }
    }

    // Tests findDeserializer() for unsupported type returns null
    @Test
    public void testFindDeserializer_unsupportedType_returnsNull() {
        assertNull(FromStringDeserializer.findDeserializer(String.class));
        assertNull(FromStringDeserializer.findDeserializer(Integer.class));
    }

    // Tests deserializing empty string for URI
    @Test
    public void testDeserialize_emptyStringForURI_returnsEmptyURI() throws Exception {
        URI result = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), result);
    }

    // Tests deserializing empty string for Locale (Defects4J 42b regression)
    @Test
    public void testDeserialize_emptyStringForLocale_returnsRootLocale() throws Exception {
        Locale result = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, result);
    }

    // Tests deserializing Locale with 1, 2, and 3 parts
    @Test
    public void testDeserialize_localeFormats_returnsLocale() throws Exception {
        Locale loc1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), loc1);

        Locale loc2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), loc2);

        Locale loc3 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), loc3);
    }

    // Tests deserializing File, URL, Pattern, Charset, TimeZone, Currency
    @Test
    public void testDeserialize_standardTypes_returnsParsedObjects() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);

        URL url = mapper.readValue("\"http://localhost:8080\"", URL.class);
        assertEquals(new URL("http://localhost:8080"), url);

        Pattern pattern = mapper.readValue("\"[a-z]+\"", Pattern.class);
        assertEquals("[a-z]+", pattern.pattern());

        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);

        TimeZone tz = mapper.readValue("\"UTC\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("UTC"), tz);

        Currency curr = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), curr);
    }

    // Tests deserializing Class and JavaType
    @Test
    public void testDeserialize_classAndJavaType_returnsTypes() throws Exception {
        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);

        JavaType type = mapper.readValue("\"java.util.List<java.lang.String>\"", JavaType.class);
        assertNotNull(type);
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    // Tests deserializing InetAddress and InetSocketAddress formats
    @Test
    public void testDeserialize_inetSocketAddress_returnsCorrectAddress() throws Exception {
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);

        InetSocketAddress hostPort = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals("localhost", hostPort.getHostName());
        assertEquals(8080, hostPort.getPort());

        InetSocketAddress hostOnly = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", hostOnly.getHostName());
        assertEquals(0, hostOnly.getPort());

        InetSocketAddress ipv6 = mapper.readValue("\"[::1]:9090\"", InetSocketAddress.class);
        assertEquals("[::1]", ipv6.getHostName());
        assertEquals(9090, ipv6.getPort());
    }

    // Tests exception on malformed bracketed IPv6 address
    @Test(expected = InvalidFormatException.class)
    public void testDeserialize_invalidBracketedIpv6_throwsInvalidFormatException() throws Exception {
        mapper.readValue("\"[::1:9090\"", InetSocketAddress.class);
    }

    // Tests exception on invalid Currency code
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidCurrency_throwsJsonMappingException() throws Exception {
        mapper.readValue("\"INVALID_CURR_CODE\"", Currency.class);
    }

    // Tests unwrapping single value arrays
    @Test
    public void testDeserialize_unwrapSingleValueArray_returnsValue() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        URI uri = unwrapMapper.readValue("[\"http://localhost\"]", URI.class);
        assertEquals(URI.create("http://localhost"), uri);
    }

    // Tests array unwrapping failure when array has multiple values
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unwrapMultipleValueArray_throwsException() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        unwrapMapper.readValue("[\"http://localhost\", \"http://other\"]", URI.class);
    }

    // Tests non-string token throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_numericToken_throwsJsonMappingException() throws Exception {
        mapper.readValue("12345", Pattern.class);
    }
}