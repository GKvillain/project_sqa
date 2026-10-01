package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for HttpConnection, targeting Defects4J bug 90b.
 * Tests are constructed without network calls, focusing on request configuration,
 * URL encoding, validation, and internal helper methods.
 */
public class HttpConnectionTest {

    // ===== Basic construction and URL handling =====

    @Test
    public void testConnectString_validUrl_returnsConnection() {
        Connection con = HttpConnection.connect("http://example.com");
        assertNotNull(con);
        assertEquals("http://example.com", con.request().url().toExternalForm());
    }

    @Test
    public void testConnectURL_validUrl_returnsConnection() throws Exception {
        URL url = new URL("http://example.com");
        Connection con = HttpConnection.connect(url);
        assertNotNull(con);
        assertEquals(url, con.request().url());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_nullString_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.url((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_emptyString_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.url("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_malformedString_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.url("not a url");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrl_nullURL_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.url((URL) null);
    }

    @Test
    public void testUrlWithSpaces_encodesSpaces() throws Exception {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com/a b");
        assertEquals("http://example.com/a%20b", con.request().url().toExternalForm());
    }

    // ===== Proxy =====

    @Test
    public void testProxyStringPort_setsProxy() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.proxy("myproxy.com", 8080);
        Proxy proxy = con.request().proxy();
        assertNotNull(proxy);
        assertEquals(Proxy.Type.HTTP, proxy.type());
    }

    // ===== Request configuration =====

    @Test(expected = IllegalArgumentException.class)
    public void testUserAgent_null_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.userAgent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeout_negative_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.timeout(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxBodySize_negative_throwsIllegalArgumentException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.maxBodySize(-1);
    }

    @Test
    public void testFollowRedirects_setsFlag() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.followRedirects(false);
        assertFalse(con.request().followRedirects());
    }

    @Test
    public void testIgnoreHttpErrors_setsFlag() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.ignoreHttpErrors(true);
        assertTrue(con.request().ignoreHttpErrors());
    }

    @Test
    public void testIgnoreContentType_setsFlag() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.ignoreContentType(true);
        assertTrue(con.request().ignoreContentType());
    }

    @Test
    public void testMethod_setsMethod() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, con.request().method());
    }

    // ===== Data handling =====

    @Test
    public void testDataKeyValue_addsKeyVal() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data("key1", "value1");
        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        Connection.KeyVal kv = data.iterator().next();
        assertEquals("key1", kv.key());
        assertEquals("value1", kv.value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataKeyValuePairs_oddCount_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data("key1", "value1", "key2"); // three elements -> odd
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataKeyValuePairs_emptyKey_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataKeyValuePairs_nullValue_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data("key", null);
    }

    @Test
    public void testDataMap_addsMultipleKeyVals() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        con.data(map);
        assertEquals(2, con.request().data().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataMap_null_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data((Map<String, String>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataCollection_null_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data((Collection<Connection.KeyVal>) null);
    }

    @Test
    public void testDataString_existingKey_returnsKeyVal() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data("foo", "bar");
        Connection.KeyVal kv = con.data("foo");
        assertNotNull(kv);
        assertEquals("bar", kv.value());
        // non-existing key returns null
        assertNull(con.data("nonexistent"));
    }

    @Test
    public void testDataKeyFilenameInputStream_addsKeyValWithStream() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        InputStream stream = new ByteArrayInputStream(new byte[]{1,2,3});
        con.data("file", "test.txt", stream);
        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        Connection.KeyVal kv = data.iterator().next();
        assertTrue(kv.hasInputStream());
        assertEquals("test.txt", kv.value());
    }

    // ===== Headers =====

    @Test(expected = IllegalArgumentException.class)
    public void testHeader_nullName_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.header(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeader_emptyName_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.header("", "value");
    }

    @Test
    public void testHeader_addAndGet() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.header("Accept", "text/html");
        assertEquals("text/html", con.request().header("Accept"));
        // case insensitive retrieval
        assertEquals("text/html", con.request().header("accept"));
    }

    @Test
    public void testAddHeader_multipleValues() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.request().addHeader("X-Custom", "value1");
        con.request().addHeader("X-Custom", "value2");
        List<String> values = con.request().headers("X-Custom");
        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    @Test
    public void testRemoveHeader() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.header("X-Test", "val");
        assertTrue(con.request().hasHeader("X-Test"));
        con.request().removeHeader("x-test");
        assertFalse(con.request().hasHeader("X-Test"));
    }

    @Test
    public void testHasHeader() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        assertFalse(con.request().hasHeader("X"));
        con.header("X", "y");
        assertTrue(con.request().hasHeader("x"));
    }

    @Test
    public void testHasHeaderWithValue() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.header("X-Cache", "HIT");
        assertTrue(con.request().hasHeaderWithValue("x-cache", "hit"));
        assertFalse(con.request().hasHeaderWithValue("x-cache", "miss"));
    }

    // ===== Cookies =====

    @Test(expected = IllegalArgumentException.class)
    public void testCookie_nullName_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.cookie(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookie_emptyName_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.cookie("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookie_nullValue_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.cookie("name", null);
    }

    @Test
    public void testCookie_setAndGet() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.cookie("session", "abc123");
        assertEquals("abc123", con.request().cookie("session"));
    }

    @Test
    public void testRemoveCookie() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.cookie("test", "val");
        assertTrue(con.request().hasCookie("test"));
        con.request().removeCookie("test");
        assertFalse(con.request().hasCookie("test"));
    }

    // ===== Request body / charset =====

    @Test(expected = IllegalCharsetNameException.class)
    public void testPostDataCharset_invalidCharset_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.postDataCharset("invalid-charset-name");
    }

    @Test
    public void testRequestDefaultValues() {
        HttpConnection.Request req = new HttpConnection.Request();
        assertEquals(30000, req.timeout());
        assertEquals(1024 * 1024, req.maxBodySize());
        assertTrue(req.followRedirects());
        assertFalse(req.ignoreHttpErrors());
        assertFalse(req.ignoreContentType());
        assertEquals(Connection.Method.GET, req.method());
        assertNotNull(req.parser());
        // default user-agent header should be present
        assertTrue(req.hasHeader("User-Agent"));
        assertEquals(HttpConnection.DEFAULT_UA, req.header("User-Agent"));
    }

    // ===== KeyVal tests =====

    @Test
    public void testKeyValCreate_keyValue() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        assertEquals("key", kv.key());
        assertEquals("value", kv.value());
        assertFalse(kv.hasInputStream());
    }

    @Test
    public void testKeyValCreate_keyFilenameStream() {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "test.txt", stream);
        assertEquals("file", kv.key());
        assertEquals("test.txt", kv.value());
        assertTrue(kv.hasInputStream());
        assertSame(stream, kv.inputStream());
    }

    @Test
    public void testKeyValContentType() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        kv.contentType("text/plain");
        assertEquals("text/plain", kv.contentType());
    }

    // ===== Execution validation (without network) =====

    @Test(expected = MalformedURLException.class)
    public void testExecute_invalidProtocol_throwsMalformedURLException() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("ftp://example.com"));
        HttpConnection.Response.execute(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecute_requestBodyOnGet_throwsIllegalArgumentException() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com"));
        req.method(Connection.Method.GET);
        req.requestBody("should fail");
        HttpConnection.Response.execute(req);
    }

    // ===== Parser =====

    @Test
    public void testParser_setsParser() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        Parser xmlParser = Parser.xmlParser();
        con.parser(xmlParser);
        assertSame(xmlParser, con.request().parser());
    }

    // ===== Request body and method hasBody validation (via request) =====

    @Test
    public void testRequestMethodHasBody() {
        assertTrue(Connection.Method.POST.hasBody());
        assertFalse(Connection.Method.GET.hasBody());
    }

    // ===== Static helper encodeUrl is private, but we can test effect via URL setting =====

    @Test
    public void testEncodeUrl_effectOnSpecialCharacters() throws Exception {
        // url with space and other characters
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com/path with spaces?q=hello world");
        URL encodedUrl = con.request().url();
        // The original URL contains spaces, they should be encoded as %20
        assertTrue(encodedUrl.toExternalForm().contains("%20"));
        // Also the query part should be encoded
        assertTrue(encodedUrl.toExternalForm().contains("hello%20world"));
    }

    // ===== New tests for uncovered code =====

    @Test
    public void testConnectString_withQueryParams() {
        Connection con = HttpConnection.connect("http://example.com?param=value&other=1");
        assertNotNull(con);
        assertEquals("http://example.com?param=value&other=1", con.request().url().toExternalForm());
    }

    @Test
    public void testConnectURL_withQueryParams() throws Exception {
        URL url = new URL("http://example.com?param=value");
        Connection con = HttpConnection.connect(url);
        assertNotNull(con);
        assertEquals(url, con.request().url());
    }

    @Test
    public void testConnectString_withPortNumber() {
        Connection con = HttpConnection.connect("http://example.com:8080/path");
        assertNotNull(con);
        assertEquals(8080, con.request().url().getPort());
        assertEquals("/path", con.request().url().getPath());
    }

    @Test
    public void testConnectString_withHttps() {
        Connection con = HttpConnection.connect("https://secure.example.com");
        assertNotNull(con);
        assertEquals("https", con.request().url().getProtocol());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataKeyValuePairs_nullKey_throwsException() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data((String) null, "value");
    }

    @Test
    public void testDataArray_withNullElements() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        // This should not throw because null values are handled by the data method
        con.data("key1", null, "key2", null);
        assertEquals(2, con.request().data().size());
    }

    @Test
    public void testDataMap_withEmptyMap() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.data(new HashMap<String, String>());
        assertEquals(0, con.request().data().size());
    }

    @Test(expected = NullPointerException.class)
    public void testDataCollection_withNullKeyVal() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        List<Connection.KeyVal> list = new ArrayList<>();
        list.add(null);
        con.data(list);
    }

    @Test
    public void testDataCollection_withKeyVals() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        List<Connection.KeyVal> list = new ArrayList<>();
        Connection.KeyVal kv1 = HttpConnection.KeyVal.create("key1", "value1");
        Connection.KeyVal kv2 = HttpConnection.KeyVal.create("key2", "value2");
        list.add(kv1);
        list.add(kv2);
        con.data(list);
        assertEquals(2, con.request().data().size());
    }

    @Test
    public void testAddHeader_withNullValue_removesHeader() {
        HttpConnection con = (HttpConnection) HttpConnection.connect("http://example.com");
        con.request().addHeader("X-Test", "val");
        assertTrue(con.request().hasHeader("X-Test"));
        con.request().addHeader("X-Test", null);
        assertFalse(con.request().hasHeader("X-Test"));
    }

    @Test
    public void testResponse_headerNames() {
        HttpConnection.Response resp = new HttpConnection.Response();
        resp.header("Content-Type", "text/html");
        assertTrue(resp.hasHeader("Content-Type"));
        assertEquals("text/html", resp.header("content-type"));
    }

    @Test
    public void testResponse_headerCaseInsensitive() {
        HttpConnection.Response resp = new HttpConnection.Response();
        resp.header("Content-Type", "text/html");
        resp.header("content-type", "application/json");
        assertEquals("application/json", resp.header("CONTENT-TYPE"));
    }

    @Test
    public void testResponse_hasHeaderWithValue_caseInsensitive() {
        HttpConnection.Response resp = new HttpConnection.Response();
        resp.header("X-Cache", "MISS");
        assertTrue(resp.hasHeaderWithValue("x-cache", "miss"));
        assertFalse(resp.hasHeaderWithValue("x-cache", "hit"));
    }

    @Test
    public void testResponse_removeHeader_caseInsensitive() {
        HttpConnection.Response resp = new HttpConnection.Response();
        resp.header("X-Test", "val");
        resp.removeHeader("x-test");
        assertFalse(resp.hasHeader("X-Test"));
    }

    @Test
    public void testRequest_proxy_withProxyObject() {
        HttpConnection.Request req = new HttpConnection.Request();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("proxy.com", 8080));
        req.proxy(proxy);
        assertSame(proxy, req.proxy());
    }

    @Test
    public void testKeyVal_keyValue_nullValue() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", null);
        assertEquals("key", kv.key());
        assertNull(kv.value());
        assertFalse(kv.hasInputStream());
    }

    @Test
    public void testKeyVal_setValue() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        kv.value("newvalue");
        assertEquals("newvalue", kv.value());
    }

    @Test
    public void testKeyVal_setKey() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("key", "value");
        kv.key("newkey");
        assertEquals("newkey", kv.key());
    }

    @Test
    public void testKeyVal_inputStream() {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "test.bin", stream);
        assertSame(stream, kv.inputStream());
    }

    @Test
    public void testKeyVal_setInputStream() {
        InputStream stream1 = new ByteArrayInputStream(new byte[]{1});
        InputStream stream2 = new ByteArrayInputStream(new byte[]{2});
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "test.bin", stream1);
        kv.inputStream(stream2);
        assertSame(stream2, kv.inputStream());
    }

    @Test
    public void testRequest_data_withMultipleEntries() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.data(HttpConnection.KeyVal.create("key1", "value1"));
        req.data(HttpConnection.KeyVal.create("key2", "value2"));
        assertEquals(2, req.data().size());
    }

    @Test
    public void testRequest_getData_byKey() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.data(HttpConnection.KeyVal.create("key1", "value1"));
        Connection.KeyVal kv = req.data("key1");
        assertNotNull(kv);
        assertEquals("value1", kv.value());
        assertNull(req.data("nonexistent"));
    }

    @Test
    public void testRequest_headers_getMultipleValues() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.addHeader("X-Test", "val1");
        req.addHeader("X-Test", "val2");
        List<String> values = req.headers("x-test");
        assertEquals(2, values.size());
        assertTrue(values.contains("val1"));
        assertTrue(values.contains("val2"));
    }

    @Test
    public void testRequest_cookies_multipleCookies() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.cookie("a", "1");
        req.cookie("b", "2");
        assertEquals(2, req.cookies().size());
        assertEquals("1", req.cookie("a"));
        assertEquals("2", req.cookie("b"));
    }

    @Test
    public void testRequest_removeCookie_caseSensitive() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.cookie("Test", "val");
        assertTrue(req.hasCookie("Test"));
        req.removeCookie("Test");
        assertFalse(req.hasCookie("Test"));
    }

    @Test
    public void testRequest_hasCookie_withDifferentCase() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.cookie("Test", "val");
        assertTrue(req.hasCookie("test"));
        assertEquals("val", req.cookie("tesT"));
    }

    @Test
    public void testRequest_setProxy_withStringPortAndType() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.proxy("proxy.com", 8080);
        assertNotNull(req.proxy());
        assertEquals(Proxy.Type.HTTP, req.proxy().type());
    }

    @Test
    public void testRequest_url_withEncodedCharacters() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com/path%20with%20spaces"));
        assertEquals("/path%20with%20spaces", req.url().getPath());
        assertEquals("http://example.com/path%20with%20spaces", req.url().toExternalForm());
    }

    @Test
    public void testRequest_method_withNewValue() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.method(Connection.Method.PUT);
        assertEquals(Connection.Method.PUT, req.method());
    }
}