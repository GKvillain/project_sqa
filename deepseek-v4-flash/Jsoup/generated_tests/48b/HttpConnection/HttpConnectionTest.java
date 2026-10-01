package org.jsoup.helper;

import org.jsoup.Connection;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;

import static org.junit.Assert.*;

public class HttpConnectionTest {

    // ===== HttpConnection static methods =====

    @Test
    public void testConnect_urlString_spacesEncoded() {
        HttpConnection conn = (HttpConnection) HttpConnection.connect("http://example.com/a b");
        assertEquals("http://example.com/a%20b", conn.request().url().toExternalForm());
    }

    @Test
    public void testConnect_urlObject_spacesEncoded() throws Exception {
        URL url = new URL("http://example.com/c d");
        HttpConnection conn = (HttpConnection) HttpConnection.connect(url);
        assertEquals("http://example.com/c%20d", conn.request().url().toExternalForm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_nullString_throwsException() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_emptyString_throwsException() {
        HttpConnection.connect("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_malformedString_throwsException() {
        HttpConnection.connect("invalid url");
    }

    // ===== HttpConnection instance methods =====

    @Test
    public void testData_keyValue_addsToRequest() {
        Connection conn = HttpConnection.connect("http://example.com");
        conn.data("key1", "val1");
        Collection<Connection.KeyVal> data = conn.request().data();
        assertEquals(1, data.size());
        assertEquals("key1", data.iterator().next().key());
        assertEquals("val1", data.iterator().next().value());
    }

    @Test
    public void testData_map_addsAll() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        Connection conn = HttpConnection.connect("http://example.com");
        conn.data(map);
        assertEquals(2, conn.request().data().size());
    }

    @Test
    public void testData_collection_addsAll() {
        List<Connection.KeyVal> list = new ArrayList<>();
        list.add(HttpConnection.KeyVal.create("x", "y"));
        list.add(HttpConnection.KeyVal.create("z", "w"));
        Connection conn = HttpConnection.connect("http://example.com");
        conn.data(list);
        assertEquals(2, conn.request().data().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testData_nullMap_throwsException() {
        HttpConnection.connect("http://example.com").data((Map<String, String>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testData_nullCollection_throwsException() {
        HttpConnection.connect("http://example.com").data((Collection<Connection.KeyVal>) null);
    }

    @Test
    public void testHeader_addAndGet() {
        Connection conn = HttpConnection.connect("http://example.com");
        conn.header("X-Test", "value");
        assertEquals("value", conn.request().header("X-Test"));
    }

    @Test
    public void testCookie_addAndGet() {
        Connection conn = HttpConnection.connect("http://example.com");
        conn.cookie("session", "abc123");
        assertEquals("abc123", conn.request().cookie("session"));
    }

    // ===== Request inner class =====

    @Test
    public void testRequest_defaults() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertEquals(3000, req.timeout());
        assertEquals(1024 * 1024, req.maxBodySize());
        assertTrue(req.followRedirects());
        assertEquals(Connection.Method.GET, req.method());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequest_timeout_negative_throwsException() {
        new HttpConnection.Request().timeout(-1);
    }

    @Test
    public void testRequest_timeout_zeroAllowed() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.timeout(0);
        assertEquals(0, req.timeout());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequest_maxBodySize_negative_throwsException() {
        new HttpConnection.Request().maxBodySize(-1);
    }

    @Test
    public void testRequest_maxBodySize_zeroAllowed() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.maxBodySize(0);
        assertEquals(0, req.maxBodySize());
    }

    @Test
    public void testRequest_postDataCharset_valid() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.postDataCharset("UTF-8");
        assertEquals("UTF-8", req.postDataCharset());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testRequest_postDataCharset_invalid_throwsException() {
        new HttpConnection.Request().postDataCharset("invalid-charset");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequest_postDataCharset_null_throwsException() {
        new HttpConnection.Request().postDataCharset(null);
    }

    // ===== KeyVal inner class =====

    @Test
    public void testKeyVal_createKeyValue() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        assertEquals("key", kv.key());
        assertEquals("value", kv.value());
        assertFalse(kv.hasInputStream());
    }

    @Test
    public void testKeyVal_createWithStream() {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "name.txt", stream);
        assertEquals("file", kv.key());
        assertEquals("name.txt", kv.value());
        assertTrue(kv.hasInputStream());
        assertSame(stream, kv.inputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyVal_key_empty_throwsException() {
        HttpConnection.KeyVal.create("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyVal_value_null_throwsException() {
        HttpConnection.KeyVal.create("key", null);
    }

    // ===== Response inner class (processResponseHeaders) =====

    @Test
    public void testResponse_processResponseHeaders_setCookie() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Set-Cookie", Arrays.asList("name=value; Path=/"));
        res.processResponseHeaders(headers);
        assertTrue(res.hasCookie("name"));
        assertEquals("value", res.cookie("name"));
    }

    @Test
    public void testResponse_processResponseHeaders_multipleCookies() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Set-Cookie", Arrays.asList("a=1", "b=2"));
        res.processResponseHeaders(headers);
        assertEquals("1", res.cookie("a"));
        assertEquals("2", res.cookie("b"));
    }

    @Test
    public void testResponse_processResponseHeaders_normalHeader() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("Content-Type", Arrays.asList("text/html"));
        res.processResponseHeaders(headers);
        assertEquals("text/html", res.header("Content-Type"));
    }

    @Test
    public void testResponse_processResponseHeaders_nullKey_skipped() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put(null, Arrays.asList("HTTP/1.1 200 OK"));
        res.processResponseHeaders(headers);
        // should not throw and no header added
        assertNull(res.header(null));
    }

    @Test
    public void testResponse_processResponseHeaders_multipleValuesForSameHeader() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put("X-Custom", Arrays.asList("first", "second"));
        res.processResponseHeaders(headers);
        // only first value is taken
        assertEquals("first", res.header("X-Custom"));
    }
}