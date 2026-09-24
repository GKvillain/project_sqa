package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.parser.Parser;
import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HttpConnectionTest {

    // Tests connect factory method with string URL
    @Test
    public void testConnect_validStringUrl_createsConnectionWithUrl() {
        Connection con = HttpConnection.connect("http://example.com/test");
        assertEquals("http://example.com/test", con.request().url().toExternalForm());
    }

    // Tests connect factory method with URL object
    @Test
    public void testConnect_validUrlObject_createsConnectionWithUrl() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        Connection con = HttpConnection.connect(url);
        assertEquals(url, con.request().url());
    }

    // Tests url validation with invalid/empty string
    @Test(expected = IllegalArgumentException.class)
    public void testUrl_emptyString_throwsException() {
        HttpConnection.connect("");
    }

    // Tests url encoding with spaces in URL string
    @Test
    public void testUrl_urlWithSpaces_encodesCorrectly() {
        Connection con = HttpConnection.connect("http://example.com/test path/index.html");
        assertEquals("http://example.com/test%20path/index.html", con.request().url().toExternalForm());
    }

    // Tests header operations and case-insensitivity
    @Test
    public void testHeader_setAndGet_caseInsensitive() {
        HttpConnection con = new HttpConnection();
        con.header("Accept-Encoding", "gzip, deflate");
        
        assertTrue(con.request().hasHeader("accept-encoding"));
        assertTrue(con.request().hasHeaderWithValue("ACCEPT-ENCODING", "gzip, deflate"));
        assertEquals("gzip, deflate", con.request().header("ACCEPT-ENCODING"));

        con.request().removeHeader("accept-encoding");
        assertFalse(con.request().hasHeader("Accept-Encoding"));
    }

    // Tests adding multiple header values and UTF-8 / non-ASCII header fixing
    @Test
    public void testAddHeader_nonAsciiHeaderValue_handlesEncoding() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.addHeader("X-Header", "Hello");
        req.addHeader("X-Header", "World");
        
        List<String> values = req.headers("X-Header");
        assertEquals(2, values.size());
        assertEquals("Hello", values.get(0));
        assertEquals("World", values.get(1));
        assertEquals("Hello, World", req.header("X-Header"));

        // Tests fixHeaderEncoding and looksLikeUtf8 with non-ASCII ISO-8859-1 strings
        req.addHeader("ISO-Header", "Montr\u00e9al");
        assertNotNull(req.header("ISO-Header"));
    }

    // Tests headers map manipulation
    @Test
    public void testHeaders_mapInput_setsAllHeaders() {
        HttpConnection con = new HttpConnection();
        Map<String, String> headers = new HashMap<>();
        headers.put("Header1", "Value1");
        headers.put("Header2", "Value2");
        con.headers(headers);

        assertEquals("Value1", con.request().header("Header1"));
        assertEquals("Value2", con.request().header("Header2"));
        assertEquals(4, con.request().headers().size()); // 2 added + 2 defaults (Accept-Encoding, User-Agent)
    }

    // Tests cookie operations
    @Test
    public void testCookie_setGetAndRemove_maintainsCookies() {
        HttpConnection con = new HttpConnection();
        con.cookie("session_id", "12345");
        
        assertTrue(con.request().hasCookie("session_id"));
        assertEquals("12345", con.request().cookie("session_id"));

        Map<String, String> cookies = new HashMap<>();
        cookies.put("pref", "dark");
        con.cookies(cookies);
        assertEquals("dark", con.request().cookie("pref"));

        con.request().removeCookie("session_id");
        assertFalse(con.request().hasCookie("session_id"));
        assertEquals(1, con.request().cookies().size());
    }

    // Tests data key-value pair additions with string varargs
    @Test
    public void testData_varargs_populatesKeyVals() {
        HttpConnection con = new HttpConnection();
        con.data("key1", "val1", "key2", "val2");

        assertEquals("val1", con.data("key1").value());
        assertEquals("val2", con.data("key2").value());
        assertNull(con.data("key3"));
    }

    // Tests data key-value with odd number of varargs
    @Test(expected = IllegalArgumentException.class)
    public void testData_oddVarargs_throwsException() {
        HttpConnection con = new HttpConnection();
        con.data("key1", "val1", "key2");
    }

    // Tests data collection and map input
    @Test
    public void testData_collectionAndMap_populatesKeyVals() {
        HttpConnection con = new HttpConnection();
        Map<String, String> map = new HashMap<>();
        map.put("k1", "v1");
        con.data(map);

        Collection<Connection.KeyVal> dataList = new ArrayList<>();
        dataList.add(HttpConnection.KeyVal.create("k2", "v2"));
        con.data(dataList);

        assertEquals(2, con.request().data().size());
        assertEquals("v1", con.data("k1").value());
        assertEquals("v2", con.data("k2").value());
    }

    // Tests KeyVal create with inputStream and contentType
    @Test
    public void testKeyVal_streamAndContentType_setsCorrectly() {
        InputStream stream = new ByteArrayInputStream("data".getBytes());
        HttpConnection.KeyVal keyVal = HttpConnection.KeyVal.create("fileKey", "file.txt", stream);
        keyVal.contentType("text/plain");

        assertTrue(keyVal.hasInputStream());
        assertEquals("fileKey", keyVal.key());
        assertEquals("file.txt", keyVal.value());
        assertEquals(stream, keyVal.inputStream());
        assertEquals("text/plain", keyVal.contentType());
        assertEquals("fileKey=file.txt", keyVal.toString());
    }

    // Tests configuration setter methods on Connection
    @Test
    public void testConnection_configMethods_updatesRequestProperly() {
        HttpConnection con = new HttpConnection();
        con.userAgent("CustomUA")
           .timeout(5000)
           .maxBodySize(2048)
           .followRedirects(false)
           .referrer("http://referrer.com")
           .method(Connection.Method.POST)
           .ignoreHttpErrors(true)
           .ignoreContentType(true)
           .requestBody("raw body")
           .postDataCharset("UTF-8")
           .parser(Parser.xmlParser());

        assertEquals("CustomUA", con.request().header("User-Agent"));
        assertEquals(5000, con.request().timeout());
        assertEquals(2048, con.request().maxBodySize());
        assertFalse(con.request().followRedirects());
        assertEquals("http://referrer.com", con.request().header("Referer"));
        assertEquals(Connection.Method.POST, con.request().method());
        assertTrue(con.request().ignoreHttpErrors());
        assertTrue(con.request().ignoreContentType());
        assertEquals("raw body", con.request().requestBody());
        assertEquals("UTF-8", con.request().postDataCharset());
        assertEquals(Parser.xmlParser().getClass(), con.request().parser().getClass());
    }

    // Tests timeout boundary validation
    @Test(expected = IllegalArgumentException.class)
    public void testTimeout_negativeValue_throwsException() {
        HttpConnection.connect("http://example.com").timeout(-1);
    }

    // Tests maxBodySize boundary validation
    @Test(expected = IllegalArgumentException.class)
    public void testMaxBodySize_negativeValue_throwsException() {
        HttpConnection.connect("http://example.com").maxBodySize(-1);
    }

    // Tests postDataCharset invalid charset exception
    @Test(expected = IllegalCharsetNameException.class)
    public void testPostDataCharset_unsupportedCharset_throwsException() {
        HttpConnection.connect("http://example.com").postDataCharset("unsupported-charset-name");
    }

    // Tests Response default constructor and property setters/getters
    @Test
    public void testResponse_propertySettersAndGetters() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.charset("UTF-8");
        res.header("Content-Type", "text/html; charset=UTF-8");
        res.cookie("auth", "token");

        assertEquals("UTF-8", res.charset());
        assertEquals("text/html; charset=UTF-8", res.header("Content-Type"));
        assertEquals("token", res.cookie("auth"));
        assertEquals(0, res.statusCode());
        assertNull(res.statusMessage());
    }

    // Tests parse before execution throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testResponse_parseUnexecuted_throwsException() throws Exception {
        HttpConnection.Response res = new HttpConnection.Response();
        res.parse();
    }

    // Tests proxy configuration methods
    @Test
    public void testProxy_proxyInstanceAndHostPort_setsProxyOnRequest() {
        HttpConnection con = new HttpConnection();
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
        con.proxy(proxy);
        assertEquals(proxy, con.request().proxy());

        con.proxy("localhost", 3128);
        assertNotNull(con.request().proxy());
        assertEquals(Proxy.Type.HTTP, con.request().proxy().type());
        assertEquals(new InetSocketAddress("localhost", 3128), con.request().proxy().address());
    }

    // Tests SSLSocketFactory setter and getter
    @Test
    public void testSslSocketFactory_setsFactoryCorrectly() {
        HttpConnection con = new HttpConnection();
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        con.sslSocketFactory(factory);
        assertEquals(factory, con.request().sslSocketFactory());
    }

    // Tests validateTLSCertificates flag
    @Test
    public void testValidateTLSCertificates_updatesRequestFlag() {
        HttpConnection con = new HttpConnection();
        assertTrue(con.request().validateTLSCertificates());
        con.validateTLSCertificates(false);
        assertFalse(con.request().validateTLSCertificates());
    }

    // Tests newRequest creates a new connection instance
    @Test
    public void testNewRequest_createsIndependentInstance() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection newCon = con.newRequest();
        assertNotSame(con, newCon);
        assertEquals("http://example.com", newCon.request().url().toExternalForm());
    }

    // Tests setting custom Request and Response instances on Connection
    @Test
    public void testRequestAndResponse_setCustomInstances() {
        HttpConnection con = new HttpConnection();
        Connection.Request req = new HttpConnection.Request();
        Connection.Response res = new HttpConnection.Response();

        con.request(req);
        con.response(res);

        assertSame(req, con.request());
        assertSame(res, con.response());
    }

    // Tests data helper overloads (stream, stream with contentType, KeyVal object)
    @Test
    public void testData_helperVariants_addsKeyVal() {
        HttpConnection con = new HttpConnection();
        InputStream stream1 = new ByteArrayInputStream("content1".getBytes());
        InputStream stream2 = new ByteArrayInputStream("content2".getBytes());

        con.data("upload1", "file1.txt", stream1);
        con.data("upload2", "file2.png", stream2, "image/png");
        con.data(HttpConnection.KeyVal.create("k3", "v3"));

        Connection.KeyVal kv1 = con.data("upload1");
        assertNotNull(kv1);
        assertEquals("file1.txt", kv1.value());
        assertTrue(kv1.hasInputStream());
        assertNull(kv1.contentType());

        Connection.KeyVal kv2 = con.data("upload2");
        assertNotNull(kv2);
        assertEquals("file2.png", kv2.value());
        assertEquals("image/png", kv2.contentType());

        Connection.KeyVal kv3 = con.data("k3");
        assertNotNull(kv3);
        assertEquals("v3", kv3.value());
    }

    // Tests KeyVal setters modifying internal state
    @Test
    public void testKeyVal_setters_mutateFields() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("key", "val");
        kv.key("newKey");
        kv.value("newVal");
        InputStream stream = new ByteArrayInputStream("data".getBytes());
        kv.inputStream(stream);

        assertEquals("newKey", kv.key());
        assertEquals("newVal", kv.value());
        assertSame(stream, kv.inputStream());
        assertTrue(kv.hasInputStream());
    }

    // Tests body() called on unexecuted response throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testResponse_bodyUnexecuted_throwsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.body();
    }

    // Tests bodyAsBytes() called on unexecuted response throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testResponse_bodyAsBytesUnexecuted_throwsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bodyAsBytes();
    }

    // Tests bufferUp() called on unexecuted response throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testResponse_bufferUpUnexecuted_throwsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bufferUp();
    }

    // Tests contentType() getter on Response
    @Test
    public void testResponse_contentType_returnsHeaderValue() {
        HttpConnection.Response res = new HttpConnection.Response();
        assertNull(res.contentType());

        res.header("Content-Type", "application/json");
        assertEquals("application/json", res.contentType());
    }

    // Tests hasHeaderWithValue returns false when absent or differing
    @Test
    public void testHeader_hasHeaderWithValue_returnsFalseWhenMismatchOrMissing() {
        HttpConnection con = new HttpConnection();
        assertFalse(con.request().hasHeaderWithValue("Non-Existent", "val"));

        con.header("X-Custom", "actual");
        assertFalse(con.request().hasHeaderWithValue("X-Custom", "different"));
        assertTrue(con.request().hasHeaderWithValue("X-Custom", "actual"));
    }

    // Tests URL encoding with non-ascii / query parameters
    @Test
    public void testUrl_nonAsciiAndQueryParams_encodesCorrectly() {
        Connection con = HttpConnection.connect("http://example.com/search?q=\u00fcber");
        assertEquals("http://example.com/search?q=%C3%BCber", con.request().url().toExternalForm());
    }
}