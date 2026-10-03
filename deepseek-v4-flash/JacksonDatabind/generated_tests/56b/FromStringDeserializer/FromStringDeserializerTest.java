package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class FromStringDeserializerTest {

    // Helper: deserializes a JSON string value into the target type
    private <T> T deserialize(String json, Class<T> clazz) throws Exception {
        return new ObjectMapper().readValue(json, clazz);
    }

    // Helper: deserializes with UNWRAP_SINGLE_VALUE_ARRAYS enabled
    private <T> T deserializeWithUnwrap(String json, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        return mapper.readValue(json, clazz);
    }

    @Test
    // Normal case for File
    public void testDeserialize_file_absolutePath_returnsFile() throws Exception {
        File f = deserialize("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), f);
    }

    @Test
    // Normal case for URL
    public void testDeserialize_url_valid_returnsURL() throws Exception {
        URL url = deserialize("\"http://example.com\"", URL.class);
        assertEquals(new URL("http://example.com"), url);
    }

    @Test
    // Normal case for URI
    public void testDeserialize_uri_valid_returnsURI() throws Exception {
        URI uri = deserialize("\"http://example.com/path\"", URI.class);
        assertEquals(URI.create("http://example.com/path"), uri);
    }

    @Test
    // Normal case for Class
    public void testDeserialize_class_valid_returnsClass() throws Exception {
        Class<?> cls = deserialize("\"java.lang.String\"", Class.class);
        assertEquals(String.class, cls);
    }

    @Test
    // Normal case for Currency
    public void testDeserialize_currency_valid_returnsCurrency() throws Exception {
        Currency curr = deserialize("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), curr);
    }

    @Test
    // Normal case for Pattern
    public void testDeserialize_pattern_valid_returnsPattern() throws Exception {
        Pattern p = deserialize("\"\\\\d+\"", Pattern.class);
        assertEquals(Pattern.compile("\\d+").pattern(), p.pattern());
    }

    @Test
    // Normal case for Locale with simple language
    public void testDeserialize_locale_simple_returnsLocale() throws Exception {
        Locale loc = deserialize("\"en\"", Locale.class);
        assertEquals(new Locale("en"), loc);
    }

    @Test
    // Normal case for Locale with language and country
    public void testDeserialize_locale_underscoreFormat_returnsLocale() throws Exception {
        Locale loc = deserialize("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), loc);
    }

    @Test
    // Normal case for Charset
    public void testDeserialize_charset_valid_returnsCharset() throws Exception {
        Charset cs = deserialize("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), cs);
    }

    @Test
    // Normal case for TimeZone
    public void testDeserialize_timezone_valid_returnsTimeZone() throws Exception {
        TimeZone tz = deserialize("\"America/New_York\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("America/New_York"), tz);
    }

    @Test
    // Normal case for InetAddress (IPv4)
    public void testDeserialize_inetAddress_ipv4_returnsInetAddress() throws Exception {
        InetAddress addr = deserialize("\"192.168.1.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("192.168.1.1"), addr);
    }

    @Test
    // Normal case for InetSocketAddress with host:port
    public void testDeserialize_inetSocketAddress_hostPort_returnsInetSocketAddress() throws Exception {
        InetSocketAddress sock = deserialize("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress("localhost", 8080), sock);
    }

    @Test
    // Normal case for InetSocketAddress with bracketed IPv6 and no port
    public void testDeserialize_inetSocketAddress_bracketedIPv6NoPort_returnsInetSocketAddress() throws Exception {
        InetSocketAddress sock = deserialize("\"[::1]\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress(InetAddress.getByName("::1"), 0), sock);
    }

    @Test
    // Normal case for InetSocketAddress with bracketed IPv6 and port
    public void testDeserialize_inetSocketAddress_bracketedIPv6WithPort_returnsInetSocketAddress() throws Exception {
        InetSocketAddress sock = deserialize("\"[::1]:9000\"", InetSocketAddress.class);
        assertEquals(new InetSocketAddress(InetAddress.getByName("::1"), 9000), sock);
    }

    @Test
    // Empty string for URI returns empty URI (special handling)
    public void testDeserialize_uri_emptyString_returnsEmptyURI() throws Exception {
        URI uri = deserialize("\"\"", URI.class);
        assertEquals(URI.create(""), uri);
    }

    @Test
    // Empty string for Locale returns Locale.ROOT (special handling)
    public void testDeserialize_locale_emptyString_returnsLocaleRoot() throws Exception {
        Locale loc = deserialize("\"\"", Locale.class);
        assertEquals(Locale.ROOT, loc);
    }

    @Test
    // Empty string for File returns null (default handling)
    public void testDeserialize_file_emptyString_returnsNull() throws Exception {
        File f = deserialize("\"\"", File.class);
        assertNull(f);
    }

    @Test(expected = JsonMappingException.class)
    // Invalid URL throws exception
    public void testDeserialize_invalidURL_throwsJsonMappingException() throws Exception {
        deserialize("\"not a url\"", URL.class);
    }

    @Test(expected = JsonMappingException.class)
    // Invalid currency throws exception
    public void testDeserialize_invalidCurrency_throwsJsonMappingException() throws Exception {
        deserialize("\"XYZ\"", Currency.class);
    }

    @Test(expected = InvalidFormatException.class)
    // Bracketed IPv6 without closing bracket throws InvalidFormatException
    public void testDeserialize_invalidInetSocketAddress_missingBracket_throwsInvalidFormatException() throws Exception {
        deserialize("\"[::1\"", InetSocketAddress.class);
    }

    @Test
    // UNWRAP_SINGLE_VALUE_ARRAYS: single element array returns the deserialized value
    public void testDeserialize_unwrapSingleValueArray_valid_returnsDeserialized() throws Exception {
        URL url = deserializeWithUnwrap("[\"http://example.com\"]", URL.class);
        assertEquals(new URL("http://example.com"), url);
    }

    @Test(expected = JsonMappingException.class)
    // UNWRAP_SINGLE_VALUE_ARRAYS: array with multiple values throws exception
    public void testDeserialize_unwrapSingleValueArray_multipleValues_throwsJsonMappingException() throws Exception {
        deserializeWithUnwrap("[\"a\",\"b\"]", String.class);
    }

    @Test(expected = JsonMappingException.class)
    // Non-string token (e.g., number) throws mapping exception
    public void testDeserialize_nonStringToken_throwsMappingException() throws Exception {
        deserialize("123", File.class);
    }

    @Test
    // findDeserializer returns Std for known types
    public void testFindDeserializer_knownTypes_returnsStd() {
        assertNotNull(FromStringDeserializer.findDeserializer(File.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URL.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URI.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Class.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Currency.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Pattern.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Locale.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Charset.class));
        assertNotNull(FromStringDeserializer.findDeserializer(TimeZone.class));
        assertNotNull(FromStringDeserializer.findDeserializer(InetAddress.class));
        assertNotNull(FromStringDeserializer.findDeserializer(InetSocketAddress.class));
    }

    @Test
    // findDeserializer returns null for unknown type
    public void testFindDeserializer_unknownType_returnsNull() {
        assertNull(FromStringDeserializer.findDeserializer(Integer.class));
    }
}