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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class FromStringDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    static class CustomType {
        final String value;
        CustomType(String value) {
            this.value = value;
        }
    }

    static class CustomFromStringDeserializer extends FromStringDeserializer<CustomType> {
        public CustomFromStringDeserializer() {
            super(CustomType.class);
        }

        @Override
        protected CustomType _deserialize(String value, DeserializationContext ctxt) throws IOException {
            if ("return-null".equals(value)) {
                return null;
            }
            return new CustomType(value);
        }
    }

    // Tests that types() returns all registered supported classes
    @Test
    public void testTypes_all_returnsRegisteredTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        assertNotNull(types);
        assertEquals(13, types.length);
    }

    // Tests finding deserializer for supported standard types
    @Test
    public void testFindDeserializer_supportedTypes_returnsNonNull() {
        for (Class<?> cls : FromStringDeserializer.types()) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(cls);
            assertNotNull(deser);
            assertEquals(cls, deser.handledType());
        }
    }

    // Tests finding deserializer for unsupported type returns null
    @Test
    public void testFindDeserializer_unsupportedType_returnsNull() {
        assertNull(FromStringDeserializer.findDeserializer(Object.class));
        assertNull(FromStringDeserializer.findDeserializer(String.class));
    }

    // Tests deserializing File from String
    @Test
    public void testDeserialize_file_returnsFile() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        assertEquals(new File("/tmp/test.txt"), file);
    }

    // Tests deserializing URL from String
    @Test
    public void testDeserialize_url_returnsUrl() throws Exception {
        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        assertEquals(new URL("http://localhost:8080/test"), url);
    }

    // Tests deserializing malformed URL throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidUrl_throwsException() throws Exception {
        mapper.readValue("\"invalid_url\"", URL.class);
    }

    // Tests deserializing URI and empty URI
    @Test
    public void testDeserialize_uri_returnsUri() throws Exception {
        URI uri = mapper.readValue("\"http://localhost:8080\"", URI.class);
        assertEquals(URI.create("http://localhost:8080"), uri);

        URI emptyUri = mapper.readValue("\"\"", URI.class);
        assertEquals(URI.create(""), emptyUri);
    }

    // Tests deserializing Class from String
    @Test
    public void testDeserialize_class_returnsClass() throws Exception {
        Class<?> cls = mapper.readValue("\"java.lang.String\"", Class.class);
        assertEquals(String.class, cls);
    }

    // Tests deserializing JavaType from String
    @Test
    public void testDeserialize_javaType_returnsJavaType() throws Exception {
        JavaType javaType = mapper.readValue("\"java.lang.String\"", JavaType.class);
        assertNotNull(javaType);
        assertEquals(String.class, javaType.getRawClass());
    }

    // Tests deserializing Currency from String
    @Test
    public void testDeserialize_currency_returnsCurrency() throws Exception {
        Currency curr = mapper.readValue("\"USD\"", Currency.class);
        assertEquals(Currency.getInstance("USD"), curr);
    }

    // Tests deserializing invalid Currency throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidCurrency_throwsException() throws Exception {
        mapper.readValue("\"UNKNOWN_CURR\"", Currency.class);
    }

    // Tests deserializing Pattern from String
    @Test
    public void testDeserialize_pattern_returnsPattern() throws Exception {
        Pattern pattern = mapper.readValue("\"a*b\"", Pattern.class);
        assertEquals("a*b", pattern.pattern());
    }

    // Tests deserializing Locale with 1, 2, 3 segments and empty String
    @Test
    public void testDeserialize_locale_returnsLocale() throws Exception {
        Locale loc1 = mapper.readValue("\"en\"", Locale.class);
        assertEquals(new Locale("en"), loc1);

        Locale loc2 = mapper.readValue("\"en_US\"", Locale.class);
        assertEquals(Locale.US, loc2);

        Locale loc3 = mapper.readValue("\"en-US-WIN\"", Locale.class);
        assertEquals(new Locale("en", "US", "WIN"), loc3);

        Locale emptyLoc = mapper.readValue("\"\"", Locale.class);
        assertEquals(Locale.ROOT, emptyLoc);
    }

    // Tests deserializing Charset from String
    @Test
    public void testDeserialize_charset_returnsCharset() throws Exception {
        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        assertEquals(Charset.forName("UTF-8"), charset);
    }

    // Tests deserializing TimeZone from String
    @Test
    public void testDeserialize_timeZone_returnsTimeZone() throws Exception {
        TimeZone tz = mapper.readValue("\"UTC\"", TimeZone.class);
        assertEquals(TimeZone.getTimeZone("UTC"), tz);
    }

    // Tests deserializing InetAddress from String
    @Test
    public void testDeserialize_inetAddress_returnsInetAddress() throws Exception {
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    // Tests deserializing InetSocketAddress with host, host:port, and bracketed IPv6
    @Test
    public void testDeserialize_inetSocketAddress_returnsInetSocketAddress() throws Exception {
        InetSocketAddress hostOnly = mapper.readValue("\"localhost\"", InetSocketAddress.class);
        assertEquals("localhost", hostOnly.getHostName());
        assertEquals(0, hostOnly.getPort());

        InetSocketAddress hostPort = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        assertEquals("localhost", hostPort.getHostName());
        assertEquals(8080, hostPort.getPort());

        InetSocketAddress ipv6 = mapper.readValue("\"[::1]:8080\"", InetSocketAddress.class);
        assertEquals("[::1]", ipv6.getHostString());
        assertEquals(8080, ipv6.getPort());
    }

    // Tests deserializing invalid bracketed IPv6 throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testDeserialize_invalidBracketedIpv6_throwsException() throws Exception {
        mapper.readValue("\"[::1\"", InetSocketAddress.class);
    }

    // Tests deserializing StringBuilder from String and empty String
    @Test
    public void testDeserialize_stringBuilder_returnsStringBuilder() throws Exception {
        StringBuilder sb = mapper.readValue("\"test\"", StringBuilder.class);
        assertEquals("test", sb.toString());

        StringBuilder emptySb = mapper.readValue("\"\"", StringBuilder.class);
        assertEquals("", emptySb.toString());
    }

    // Tests custom FromStringDeserializer returning null from _deserialize
    @Test
    public void testDeserialize_customReturningNull_returnsNull() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CustomType.class, new CustomFromStringDeserializer());
        customMapper.registerModule(module);

        CustomType result = customMapper.readValue("\"return-null\"", CustomType.class);
        assertNull(result);

        CustomType nonNullResult = customMapper.readValue("\"hello\"", CustomType.class);
        assertNotNull(nonNullResult);
        assertEquals("hello", nonNullResult.value);
    }
}