package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;

import static org.junit.Assert.*;

public class HttpConnectionTest {

    // Tests connect factory method with URL instance
    @Test
    public void testConnect_validURL_returnsConnectionWithConfiguredUrl() throws MalformedURLException {
        URL url = new URL("http://example.com/test");
        Connection con = HttpConnection.connect(url);
        assertNotNull(con);
        assertEquals(url, con.request().url());
    }

    // Tests URL encoding when url string contains spaces
    @Test
    public void testConnect_urlStringWithSpaces_encodesSpacesProperly() {
        Connection con = HttpConnection.connect("http://example.com/test path/page.html");
        assertEquals("http://example.com/test%20path/page.html", con.request().url().toExternalForm());
    }

    // Tests malformed URL string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConnect_malformedUrl_throwsIllegalArgumentException() {
        HttpConnection.connect("invalid://url:bad:format");
    }

    // Tests empty URL string throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConnect_emptyUrl_throwsIllegalArgumentException() {
        HttpConnection.connect("");
    }

    // Tests negative timeout value throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTimeout_negativeValue_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").timeout(-1);
    }

    // Tests negative maxBodySize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMaxBodySize_negativeValue_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").maxBodySize(-1);
    }

    // Tests adding data key-value pairs using varargs
    @Test
    public void testData_varargsKeyValues_addsDataToRequest() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("key1", "val1", "key2", "val2");
        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(2, data.size());
        Iterator<Connection.KeyVal> it = data.iterator();
        assertEquals("key1=val1", it.next().toString());
        assertEquals("key2=val2", it.next().toString());
    }

    // Tests odd number of varargs in data() throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testData_oddNumberOfVarargs_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").data("key1", "val1", "orphanKey");
    }

    // Tests header operations with case-insensitivity and removal
    @Test
    public void testHeader_caseInsensitiveOperations_updatesAndRemovesCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Content-Type", "text/html");
        assertTrue(con.request().hasHeader("content-type"));
        assertTrue(con.request().hasHeaderWithValue("CONTENT-TYPE", "text/html"));
        assertEquals("text/html", con.request().header("CONTENT-TYPE"));

        con.header("content-type", "application/json");
        assertEquals("application/json", con.request().header("Content-Type"));
        assertEquals(2, con.request().headers().size()); // Accept-Encoding default + Content-Type

        con.request().removeHeader("CONTENT-TYPE");
        assertFalse(con.request().hasHeader("Content-Type"));
        assertNull(con.request().header("Content-Type"));
    }

    // Tests cookie addition, query, and removal
    @Test
    public void testCookie_addAndRemove_maintainsCookieMap() {
        Connection con = HttpConnection.connect("http://example.com");
        con.cookie("session", "abc123");
        assertTrue(con.request().hasCookie("session"));
        assertEquals("abc123", con.request().cookie("session"));

        Map<String, String> cookieMap = new HashMap<String, String>();
        cookieMap.put("theme", "dark");
        con.cookies(cookieMap);
        assertEquals(2, con.request().cookies().size());

        con.request().removeCookie("session");
        assertFalse(con.request().hasCookie("session"));
        assertEquals(1, con.request().cookies().size());
    }

    // Tests unsupported charset for post data throws IllegalCharsetNameException
    @Test(expected = IllegalCharsetNameException.class)
    public void testPostDataCharset_unsupportedCharset_throwsIllegalCharsetNameException() {
        HttpConnection.connect("http://example.com").postDataCharset("INVALID_CHARSET_NAME_123");
    }

    // Tests combining multiple response header values with comma (Defects4J Bug 48)
    @Test
    public void testProcessResponseHeaders_multipleValuesForSameHeader_combinesWithComma() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        List<String> values = new ArrayList<String>();
        values.add("no-cache");
        values.add("no-store");
        headers.put("Cache-Control", values);

        res.processResponseHeaders(headers);
        assertEquals("no-cache, no-store", res.header("Cache-Control"));
    }

    // Tests Set-Cookie header parsing in response
    @Test
    public void testProcessResponseHeaders_setCookieHeaders_parsesCookiesProperly() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        headers.put("Set-Cookie", Arrays.asList("SID=12345; Path=/; Secure", "UID=67890; HttpOnly"));

        res.processResponseHeaders(headers);
        assertTrue(res.hasCookie("SID"));
        assertEquals("12345", res.cookie("SID"));
        assertTrue(res.hasCookie("UID"));
        assertEquals("67890", res.cookie("UID"));
    }

    // Tests KeyVal creation with input stream
    @Test
    public void testKeyVal_streamCreation_setsPropertiesCorrectly() {
        ByteArrayInputStream stream = new ByteArrayInputStream("data".getBytes());
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("upload", "file.txt", stream);
        assertEquals("upload", kv.key());
        assertEquals("file.txt", kv.value());
        assertTrue(kv.hasInputStream());
        assertSame(stream, kv.inputStream());
    }

    // Tests configuring fluent request properties
    @Test
    public void testRequest_fluentConfiguration_updatesRequestState() {
        Connection con = HttpConnection.connect("http://example.com");
        con.userAgent("Mozilla/5.0")
           .referrer("http://google.com")
           .method(Connection.Method.POST)
           .followRedirects(false)
           .ignoreHttpErrors(true)
           .ignoreContentType(true)
           .validateTLSCertificates(false)
           .parser(Parser.xmlParser());

        Connection.Request req = con.request();
        assertEquals("Mozilla/5.0", req.header("User-Agent"));
        assertEquals("http://google.com", req.header("Referer"));
        assertEquals(Connection.Method.POST, req.method());
        assertFalse(req.followRedirects());
        assertTrue(req.ignoreHttpErrors());
        assertTrue(req.ignoreContentType());
        assertFalse(req.validateTLSCertificates());
        assertNotNull(req.parser());
    }

    @Test
    public void testData_fromMap_addsAllEntries() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> dataMap = new LinkedHashMap<String, String>();
        dataMap.put("param1", "val1");
        dataMap.put("param2", "val2");

        con.data(dataMap);
        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(2, data.size());

        Iterator<Connection.KeyVal> it = data.iterator();
        Connection.KeyVal kv1 = it.next();
        assertEquals("param1", kv1.key());
        assertEquals("val1", kv1.value());

        Connection.KeyVal kv2 = it.next();
        assertEquals("param2", kv2.key());
        assertEquals("val2", kv2.value());
    }

    @Test
    public void testData_singleKeyValInstance_addsKeyVal() {
        Connection con = HttpConnection.connect("http://example.com");
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("name", "value");
        con.data(kv);

        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        assertTrue(data.contains(kv));
    }

    @Test
    public void testData_withInputStream_addsInputStreamData() {
        Connection con = HttpConnection.connect("http://example.com");
        InputStream is = new ByteArrayInputStream("content".getBytes());
        con.data("fileField", "filename.png", is);

        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        Connection.KeyVal kv = data.iterator().next();
        assertEquals("fileField", kv.key());
        assertEquals("filename.png", kv.value());
        assertSame(is, kv.inputStream());
        assertTrue(kv.hasInputStream());
    }

    @Test
    public void testKeyVal_mutators_updateValuesProperly() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("k1", "v1");
        kv.key("k2");
        assertEquals("k2", kv.key());
        kv.value("v2");
        assertEquals("v2", kv.value());

        InputStream is = new ByteArrayInputStream("test".getBytes());
        kv.inputStream(is);
        assertSame(is, kv.inputStream());
        assertTrue(kv.hasInputStream());
    }

    @Test
    public void testHeaders_fromMap_addsAllHeaders() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Custom-1", "Val1");
        headers.put("X-Custom-2", "Val2");

        con.headers(headers);
        assertTrue(con.request().hasHeader("X-Custom-1"));
        assertEquals("Val1", con.request().header("X-Custom-1"));
        assertTrue(con.request().hasHeader("X-Custom-2"));
        assertEquals("Val2", con.request().header("X-Custom-2"));
    }

    @Test
    public void testHasHeaderWithValue_returnsFalseWhenValueDiffersOrMissing() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("X-Test", "expectedValue");

        assertTrue(con.request().hasHeaderWithValue("X-Test", "expectedValue"));
        assertFalse(con.request().hasHeaderWithValue("X-Test", "differentValue"));
        assertFalse(con.request().hasHeaderWithValue("X-NonExistent", "expectedValue"));
    }

    @Test
    public void testProxy_configuration_setsProxyCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8080));
        con.proxy(proxy);
        assertEquals(proxy, con.request().proxy());

        con.proxy("127.0.0.1", 9090);
        Proxy setProxy = con.request().proxy();
        assertNotNull(setProxy);
        assertEquals(Proxy.Type.HTTP, setProxy.type());
        assertTrue(setProxy.address().toString().contains("127.0.0.1:9090"));
    }

    @Test
    public void testRequestAndResponse_replaceInstances_updatesCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request newReq = new HttpConnection.Request();
        Connection.Response newRes = new HttpConnection.Response();

        con.request(newReq);
        con.response(newRes);

        assertSame(newReq, con.request());
        assertSame(newRes, con.response());
    }

    @Test
    public void testPostDataCharset_validCharset_updatesCharset() {
        Connection con = HttpConnection.connect("http://example.com");
        con.postDataCharset("ISO-8859-1");
        assertEquals("ISO-8859-1", con.request().postDataCharset());

        con.postDataCharset("UTF-8");
        assertEquals("UTF-8", con.request().postDataCharset());
    }

    @Test
    public void testProcessResponseHeaders_handlesNullHeaderNameAndEmptyCookies() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
        // HTTP status line often has null key
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        // Malformed cookie entries (empty name or blank string)
        headers.put("Set-Cookie", Arrays.asList("", "=emptyName", "validName="));

        res.processResponseHeaders(headers);
        assertFalse(res.hasHeader(""));
        assertTrue(res.hasCookie("validName"));
        assertEquals("", res.cookie("validName"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeader_nullKey_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").header(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHeader_emptyKey_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").header("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookie_nullKey_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").cookie(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCookie_emptyKey_throwsIllegalArgumentException() {
        HttpConnection.connect("http://example.com").cookie("", "value");
    }
}