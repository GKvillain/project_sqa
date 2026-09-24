package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;

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

import static org.junit.Assert.*;

public class FromStringDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    // Tests types() method returns all supported standard types
    @Test
    public void testTypes_returnsExpectedSupportedTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(12, types.length);
    }

    // Tests findDeserializer with supported and unsupported types
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
        assertNull(FromStringDeserializer.findDeserializer(StringBuilder.class));
    }

    // Tests standard deserialization for File, URL, and URI
    @Test
    public void testDeserialize_fileUrlUri_success() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);

        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);

        URI uri = mapper.readValue("\"http://localhost:8080/test\"", URI.class);
        assertEquals(URI.create("http://localhost:8080/test"), uri);
    }

    // Tests standard deserialization for Class and JavaType
    @Test
    public void testDeserialize_classAndJavaType_success() throws Exception {
        Class<?> clazz = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, clazz);

        JavaType type = mapper.readValue("\"java.util.List<java.lang.String>\"", JavaType.class);
        assertNotNull(type);
        assertEquals(java.util.List.class, type.getRawClass());
    }

    // Tests standard deserialization for Currency, Pattern, Charset, and TimeZone
    @Test
    public void testDeserialize_miscTypes_success() throws Exception {
        Currency currency = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), currency);

        Pattern pattern = mapper.readValue("\"a*b\"", Pattern.class);
        assertEquals("a*b", pattern.pattern());

        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);

        TimeZone timeZone = mapper.readValue("\"GMT+8\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("GMT+8"), timeZone);
    }

    // Tests standard deserialization for InetAddress
    @Test
    public void testDeserialize_inetAddress_success() throws Exception {
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    // Tests Locale deserialization with 1, 2, and 3 parts
    @Test
    public void testDeserialize_localeVariations_success() throws Exception {
        Locale loc1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), loc1);

        Locale loc2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(new Locale("en", "US"), loc2);

        Locale loc3 = mapper.readValue("\"en_US_WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), loc3);
    }

    // Tests empty string handling for URI and Locale
    @Test
    public void testDeserialize_emptyStringSpecialCases_returnsSpecialValues() throws Exception {
        URI uri = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), uri);

        Locale loc = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, loc);
    }

    // Tests empty string handling for default types returning null
    @Test
    public void testDeserialize_emptyStringDefault_returnsNull() throws Exception {
        File file = mapper.readValue("\"\"", File.class);
        assertNull(file);

        Currency currency = mapper.readValue("\"   \"", Currency.class);
        assertNull(currency);
    }

    // Tests InetSocketAddress with host and port
    @Test
    public void testDeserialize_inetSocketAddressHostPort_success() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostName());
        assertEquals(8080, addr.getPort());
    }

    // Tests InetSocketAddress with host only
    @Test
    public void testDeserialize_inetSocketAddressHostOnly_success() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", addr.getHostName());
        assertEquals(0, addr.getPort());
    }

    // Tests InetSocketAddress with bracketed IPv6 and port
    @Test
    public void testDeserialize_inetSocketAddressBracketedIpv6WithPort_success() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"[::1]:8080\"", InetSocketAddress.class);
        assertEquals("[::1]", addr.getHostString());
        assertEquals(8080, addr.getPort());
    }

    // Tests InetSocketAddress with bracketed IPv6 without port
    @Test
    public void testDeserialize_inetSocketAddressBracketedIpv6NoPort_success() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"[::1]\"", InetSocketAddress.class);
        assertEquals("[::1]", addr.getHostString());
        assertEquals(0, addr.getPort());
    }

    // Tests InetSocketAddress with unbracketed IPv6
    @Test
    public void testDeserialize_inetSocketAddressUnbracketedIpv6_success() throws Exception {
        InetSocketAddress addr = mapper.readValue("\"2001:db8::1\"", InetSocketAddress.class);
        assertEquals("2001:db8::1", addr.getHostName());
        assertEquals(0, addr.getPort());
    }

    // Tests InetSocketAddress with malformed bracketed IPv6 missing closing bracket
    @Test(expected = InvalidFormatException.class)
    public void testDeserialize_inetSocketAddressMalformedBracket_throwsException() throws Exception {
        mapper.readValue("\"[::1:8080\"", InetSocketAddress.class);
    }

    // Tests invalid input throwing JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidCurrency_throwsJsonMappingException() throws Exception {
        mapper.readValue("\"INVALID_CURRENCY_CODE\"", Currency.class);
    }

    // Tests unwrapping single value array feature
    @Test
    public void testDeserialize_unwrappedSingleValueArray_success() throws Exception {
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        URI uri = mapper.readValue("[\"http://localhost:8080\"]", URI.class);
        assertEquals(URI.create("http://localhost:8080"), uri);
    }

    // Tests unwrapping single value array failing on multiple elements
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_unwrappedMultipleValuesArray_throwsException() throws Exception {
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        mapper.readValue("[\"http://localhost:8080\", \"http://localhost:8081\"]", URI.class);
    }
}