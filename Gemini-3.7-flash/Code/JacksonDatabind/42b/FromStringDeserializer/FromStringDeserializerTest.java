package com.fasterxml.jackson.databind.deser.std;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class FromStringDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests types() array contains expected supported types
    @Test
    public void testTypes_returnsAllSupportedTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertTrue(types.length >= 12);
    }

    // Tests findDeserializer for valid types and unsupported type
    @Test
    public void testFindDeserializer_supportedAndUnsupportedTypes() {
        assertNotNull(FromStringDeserializer.findDeserializer(File.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URL.class));
        assertNotNull(FromStringDeserializer.findDeserializer(URI.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Class.class));
        assertNotNull(FromStringDeserializer.findDeserializer(JavaType.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Currency.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Pattern.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Locale.class));
        assertNotNull(FromStringDeserializer.findDeserializer(Charset.class));
        assertNotNull(FromStringDeserializer.findDeserializer(TimeZone.class));
        assertNotNull(FromStringDeserializer.findDeserializer(InetAddress.class));
        assertNotNull(FromStringDeserializer.findDeserializer(InetSocketAddress.class));
        assertNull(FromStringDeserializer.findDeserializer(String.class));
    }

    // Tests deserializing File, URL, URI, Class, JavaType, Currency, Pattern, Charset, TimeZone
    @Test
    public void testDeserialize_standardTypes() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);

        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);

        URI uri = mapper.readValue("\"http://localhost:8080/test\"", URI.class);
        assertEquals(URI.create("http://localhost:8080/test"), uri);

        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);

        JavaType javaType = mapper.readValue("\"java.lang.String\"", JavaType.class);
        assertEquals(mapper.constructType(String.class), javaType);

        Currency currency = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);

        Pattern pattern = mapper.readValue("\"a*b\"", Pattern.class);
        assertEquals("a*b", pattern.pattern());

        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);

        TimeZone timeZone = mapper.readValue("\"UTC\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("UTC"), timeZone);

        InetAddress inetAddress = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), inetAddress);
    }

    // Tests deserializing Locale with different segment lengths
    @Test
    public void testDeserialize_localeVariants() throws Exception {
        Locale l1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), l1);

        Locale l2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), l2);

        Locale l3 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), l3);
    }

    // Tests deserializing empty string for URI special handling
    @Test
    public void testDeserialize_emptyStringForURI() throws Exception {
        URI result = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), result);
    }

    // Tests deserializing empty string for Locale (Defects4J 42b regression target)
    @Test
    public void testDeserialize_emptyStringForLocale() throws Exception {
        Locale result = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, result);
    }

    // Tests deserializing empty string for other types returns null
    @Test
    public void testDeserialize_emptyStringReturnsNullForOtherTypes() throws Exception {
        assertNull(mapper.readValue("\"\"", Pattern.class));
        assertNull(mapper.readValue("\"   \"", Currency.class));
    }

    // Tests deserializing InetSocketAddress with host and port
    @Test
    public void testDeserialize_inetSocketAddressHostAndPort() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostName());
        assertEquals(8080, addr.getPort());
    }

    // Tests deserializing InetSocketAddress with host only
    @Test
    public void testDeserialize_inetSocketAddressHostOnly() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostName());
        assertEquals(0, addr.getPort());
    }

    // Tests deserializing InetSocketAddress with bracketed IPv6 and port
    @Test
    public void testDeserialize_inetSocketAddressBracketedIPv6WithPort() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"[::1]:8080\"", InetSocketAddress.class);
        assertEquals("[::1]", addr.getHostName());
        assertEquals(8080, addr.getPort());
    }

    // Tests deserializing InetSocketAddress with bracketed IPv6 without port
    @Test
    public void testDeserialize_inetSocketAddressBracketedIPv6WithoutPort() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"[::1]\"", InetSocketAddress.class);
        assertEquals("[::1]", addr.getHostName());
        assertEquals(0, addr.getPort());
    }

    // Tests invalid bracketed IPv6 missing closing bracket throws exception
    @Test(expected = InvalidFormatException.class)
    public void testDeserialize_inetSocketAddressInvalidBracket_throwsException() throws Exception {
        mapper.readValue("\"[::1:8080\"", InetSocketAddress.class);
    }

    // Tests unwrapping single value array feature
    @Test
    public void testDeserialize_unwrapSingleValueArray() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Currency currency = unwrapMapper.readValue("[\"USD\"]", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);
    }

    // Tests unwrapping array with multiple elements throws exception
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unwrapArrayWithMultipleElements_throwsException() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        unwrapMapper.readValue("[\"USD\", \"EUR\"]", Currency.class);
    }

    // Tests invalid textual input throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidCurrencyString_throwsException() throws Exception {
        mapper.readValue("\"INVALID_CURRENCY\"", Currency.class);
    }

    // Tests invalid class name throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidClassName_throwsException() throws Exception {
        mapper.readValue("\"com.nonexistent.NoSuchClass\"", Class.class);
    }

    // Tests non-string token throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_nonStringToken_throwsException() throws Exception {
        mapper.readValue("12345", Pattern.class);
    }
}