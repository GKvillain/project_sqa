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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FromStringDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests types() returns supported types array
    @Test
    public void testTypes_returnsNonEmptyArray() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertTrue(types.length > 0);
    }

    // Tests findDeserializer for supported and unsupported types
    @Test
    public void testFindDeserializer_supportedAndUnsupportedTypes() {
        for (Class<?> type : FromStringDeserializer.types()) {
            assertNotNull(FromStringDeserializer.findDeserializer(type));
        }
        assertNull(FromStringDeserializer.findDeserializer(String.class));
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
    }

    // Tests deserialization of File
    @Test
    public void testDeserialize_file_returnsFile() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    // Tests deserialization of URL
    @Test
    public void testDeserialize_url_returnsURL() throws Exception {
        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);
    }

    // Tests deserialization of URI (normal and empty string handling)
    @Test
    public void testDeserialize_uri_returnsURI() throws Exception {
        URI uri = mapper.readValue("\"http://localhost:8080/test\"", URI.class);
        assertEquals(URI.create("http://localhost:8080/test"), uri);

        URI emptyUri = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), emptyUri);
    }

    // Tests deserialization of Locale (1, 2, 3 parts and empty string)
    @Test
    public void testDeserialize_locale_returnsLocale() throws Exception {
        Locale l1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), l1);

        Locale l2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), l2);

        Locale l3 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), l3);

        Locale emptyLocale = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, emptyLocale);
    }

    // Tests deserialization of Currency
    @Test
    public void testDeserialize_currency_returnsCurrency() throws Exception {
        Currency currency = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);
    }

    // Tests deserialization of Pattern
    @Test
    public void testDeserialize_pattern_returnsPattern() throws Exception {
        Pattern pattern = mapper.readValue("\"a*b\"", Pattern.class);
        assertEquals("a*b", pattern.pattern());
    }

    // Tests deserialization of Charset
    @Test
    public void testDeserialize_charset_returnsCharset() throws Exception {
        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);
    }

    // Tests deserialization of TimeZone
    @Test
    public void testDeserialize_timeZone_returnsTimeZone() throws Exception {
        TimeZone tz = mapper.readValue("\"UTC\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("UTC"), tz);
    }

    // Tests deserialization of InetAddress
    @Test
    public void testDeserialize_inetAddress_returnsInetAddress() throws Exception {
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    // Tests deserialization of InetSocketAddress (IPv4 with and without port)
    @Test
    public void testDeserialize_inetSocketAddress_ipv4() throws Exception {
        InetSocketAddress isaWithPort = mapper.readValue("\"127.0.0.1:8080\"", InetSocketAddress.class);
        assertEquals("127.0.0.1", isaWithPort.getHostName());
        assertEquals(8080, isaWithPort.getPort());

        InetSocketAddress isaWithoutPort = mapper.readValue("\"127.0.0.1\"", InetSocketAddress.class);
        assertEquals("127.0.0.1", isaWithoutPort.getHostName());
        assertEquals(0, isaWithoutPort.getPort());
    }

    // Tests deserialization of InetSocketAddress (IPv6 bracketed format)
    @Test
    public void testDeserialize_inetSocketAddress_bracketedIpv6() throws Exception {
        InetSocketAddress isa = mapper.readValue("\"[::1]:8080\"", InetSocketAddress.class);
        assertEquals("[::1]", isa.getHostName());
        assertEquals(8080, isa.getPort());

        InetSocketAddress isaNoPort = mapper.readValue("\"[::1]\"", InetSocketAddress.class);
        assertEquals("[::1]", isaNoPort.getHostName());
        assertEquals(0, isaNoPort.getPort());
    }

    // Tests exception on malformed bracketed IPv6 address
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_inetSocketAddress_malformedIpv6_throwsException() throws Exception {
        mapper.readValue("\"[::1\"", InetSocketAddress.class);
    }

    // Tests exception on invalid textual representation
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidCurrency_throwsException() throws Exception {
        mapper.readValue("\"INVALID_CURRENCY\"", Currency.class);
    }

    // Tests unwrapping single value array feature
    @Test
    public void testDeserialize_unwrapSingleValueArray_returnsValue() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        Currency currency = unwrapMapper.readValue("[\"USD\"]", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);
    }

    // Tests exception when array has multiple values under unwrapping feature
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unwrapSingleValueArray_multipleElements_throwsException() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper();
        unwrapMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        unwrapMapper.readValue("[\"USD\", \"EUR\"]", Currency.class);
    }

    // Tests empty string deserialization returning null for standard types
    @Test
    public void testDeserialize_emptyString_returnsNull() throws Exception {
        assertNull(mapper.readValue("\"\"", Currency.class));
        assertNull(mapper.readValue("\"   \"", Currency.class));
        assertNull(mapper.readValue("\"\"", Pattern.class));
    }
}