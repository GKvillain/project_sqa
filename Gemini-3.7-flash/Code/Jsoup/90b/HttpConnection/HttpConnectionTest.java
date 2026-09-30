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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class HttpConnectionTest {

    // Tests fixHeaderEncoding and looksLikeUtf8 boundary check for high byte character (Defects4J 90b)
    @Test
    public void testAddHeader_trailingHighByteIso88591_doesNotThrowException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("X-Header", "value \u00e9");
        assertEquals("value \u00e9", con.request().header("X-Header"));
    }

    // Tests fixHeaderEncoding with single high-order byte to verify array bounds handling
    @Test
    public void testAddHeader_singleHighByteCharacter_doesNotThrowException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("X-Header", "\u00e9");
        assertEquals("\u00e9", con.request().header("X-Header"));
    }

    // Tests normal connect factory method with URL string
    @Test
    public void testConnect_validUrlString_initializesProperly() {
        Connection con = HttpConnection.connect("http://example.com/path");
        assertNotNull(con);
        assertEquals("http://example.com/path", con.request().url().toExternalForm());
        assertEquals(Connection.Method.GET, con.request().method());
    }

    // Tests URL encoding when string contains spaces
    @Test
    public void testUrl_stringWithSpaces_encodesSpaces() {
        Connection con = HttpConnection.connect("http://example.com/test space/index.html");
        assertEquals("http://example.com/test%20space/index.html", con.request().url().toExternalForm());
    }

    // Tests invalid URL input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUrl_invalidUrlString_throwsIllegalArgumentException() {
        HttpConnection.connect("invalid_url_protocol://bad");
    }

    // Tests empty URL string validation
    @Test(expected = IllegalArgumentException.class)
    public void testUrl_emptyString_throwsIllegalArgumentException() {
        HttpConnection.connect("");
    }

    // Tests case-insensitive header lookup and retrieval
    @Test
    public void testHeader_caseInsensitiveLookup_returnsHeaderValue() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Content-Type", "application/json");

        assertEquals("application/json", con.request().header("content-type"));
        assertTrue(con.request().hasHeader("CONTENT-TYPE"));
        assertTrue(con.request().hasHeaderWithValue("content-type", "application/json"));
        assertFalse(con.request().hasHeaderWithValue("content-type", "text/html"));
    }

    // Tests adding multiple values for the same header
    @Test
    public void testAddHeader_multipleValues_returnsCombinedAndIndividualValues() {
        Connection con = HttpConnection.connect("http://example.com");
        con.request().addHeader("Accept", "text/html");
        con.request().addHeader("Accept", "application/xhtml+xml");

        assertEquals("text/html, application/xhtml+xml", con.request().header("Accept"));
        List<String> values = con.request().headers("Accept");
        assertEquals(2, values.size());
        assertEquals("text/html", values.get(0));
        assertEquals("application/xhtml+xml", values.get(1));
    }

    // Tests removing header case-insensitively
    @Test
    public void testRemoveHeader_existingHeader_removesSuccessfully() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Custom-Header", "value1");
        assertTrue(con.request().hasHeader("Custom-Header"));

        con.request().removeHeader("custom-header");
        assertFalse(con.request().hasHeader("Custom-Header"));
        assertNull(con.request().header("Custom-Header"));
    }

    // Tests setting headers via Map
    @Test
    public void testHeaders_mapOfHeaders_setsAllHeaders() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-1", "val1");
        headers.put("X-Custom-2", "val2");
        con.headers(headers);

        assertEquals("val1", con.request().header("X-Custom-1"));
        assertEquals("val2", con.request().header("X-Custom-2"));
    }

    // Tests cookie storage, retrieval, and removal
    @Test
    public void testCookie_setAndRemove_operatesCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.cookie("session_id", "xyz123");

        assertTrue(con.request().hasCookie("session_id"));
        assertEquals("xyz123", con.request().cookie("session_id"));

        con.request().removeCookie("session_id");
        assertFalse(con.request().hasCookie("session_id"));
        assertNull(con.request().cookie("session_id"));
    }

    // Tests data key-value pair additions using varargs
    @Test
    public void testData_varargsKeyVal_addsDataEntries() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("key1", "val1", "key2", "val2");

        assertEquals(2, con.request().data().size());
        Connection.KeyVal kv1 = con.data("key1");
        assertNotNull(kv1);
        assertEquals("val1", kv1.value());

        Connection.KeyVal kv2 = con.data("key2");
        assertNotNull(kv2);
        assertEquals("val2", kv2.value());
    }

    // Tests data varargs with odd number of parameters throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testData_oddNumberOfKeyvals_throwsIllegalArgumentException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("key1", "val1", "key2");
    }

    // Tests setting request timeout with valid and boundary values
    @Test
    public void testTimeout_validValue_setsTimeout() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(5000);
        assertEquals(5000, con.request().timeout());

        con.timeout(0);
        assertEquals(0, con.request().timeout());
    }

    // Tests negative timeout throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testTimeout_negativeValue_throwsIllegalArgumentException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(-1);
    }

    // Tests setting maxBodySize with valid and boundary values
    @Test
    public void testMaxBodySize_validValue_setsMaxBodySize() {
        Connection con = HttpConnection.connect("http://example.com");
        con.maxBodySize(2048);
        assertEquals(2048, con.request().maxBodySize());

        con.maxBodySize(0);
        assertEquals(0, con.request().maxBodySize());
    }

    // Tests negative maxBodySize throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testMaxBodySize_negativeValue_throwsIllegalArgumentException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.maxBodySize(-1);
    }

    // Tests null userAgent throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testUserAgent_nullValue_throwsIllegalArgumentException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.userAgent(null);
    }

    // Tests unsupported charset for postDataCharset throws IllegalCharsetNameException
    @Test(expected = IllegalCharsetNameException.class)
    public void testPostDataCharset_unsupportedCharset_throwsIllegalCharsetNameException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.postDataCharset("unsupported-charset-name-1234");
    }

    // Tests KeyVal creation with InputStream
    @Test
    public void testKeyVal_inputStreamData_initializesCorrectly() {
        InputStream stream = new ByteArrayInputStream("test content".getBytes());
        Connection.KeyVal kv = HttpConnection.KeyVal.create("file", "test.txt", stream);
        kv.contentType("text/plain");

        assertEquals("file", kv.key());
        assertEquals("test.txt", kv.value());
        assertTrue(kv.hasInputStream());
        assertEquals(stream, kv.inputStream());
        assertEquals("text/plain", kv.contentType());
        assertEquals("file=test.txt", kv.toString());
    }

    @Test
    public void testConnect_withUrlObject_initializesProperly() throws MalformedURLException {
        URL url = new URL("http://example.com/test");
        Connection con = HttpConnection.connect(url);
        assertNotNull(con);
        assertEquals(url, con.request().url());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnect_nullUrl_throwsIllegalArgumentException() {
        HttpConnection.connect((URL) null);
    }

    @Test
    public void testUrl_setterMethods_updatesRequestUrl() throws MalformedURLException {
        Connection con = HttpConnection.connect("http://example.com");
        URL newUrl = new URL("http://example.org/path");
        con.url(newUrl);
        assertEquals(newUrl, con.request().url());

        con.url("http://example.net/other");
        assertEquals("http://example.net/other", con.request().url().toExternalForm());
    }

    @Test
    public void testProxy_settingProxyInstanceAndHostPort_configuresProxy() {
        Connection con = HttpConnection.connect("http://example.com");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
        con.proxy(proxy);
        assertEquals(proxy, con.request().proxy());

        con.proxy("localhost", 8888);
        assertNotNull(con.request().proxy());
        assertEquals(Proxy.Type.HTTP, con.request().proxy().type());
    }

    @Test
    public void testFollowRedirects_flagSettings_updatesRequest() {
        Connection con = HttpConnection.connect("http://example.com");
        con.followRedirects(false);
        assertFalse(con.request().followRedirects());

        con.followRedirects(true);
        assertTrue(con.request().followRedirects());
    }

    @Test
    public void testReferrer_setter_setsReferrerHeader() {
        Connection con = HttpConnection.connect("http://example.com");
        con.referrer("http://google.com");
        assertEquals("http://google.com", con.request().header("Referer"));
    }

    @Test
    public void testMethod_setter_setsRequestMethod() {
        Connection con = HttpConnection.connect("http://example.com");
        con.method(Connection.Method.POST);
        assertEquals(Connection.Method.POST, con.request().method());
    }

    @Test
    public void testIgnoreHttpErrors_flagSettings_updatesRequest() {
        Connection con = HttpConnection.connect("http://example.com");
        con.ignoreHttpErrors(true);
        assertTrue(con.request().ignoreHttpErrors());

        con.ignoreHttpErrors(false);
        assertFalse(con.request().ignoreHttpErrors());
    }

    @Test
    public void testIgnoreContentType_flagSettings_updatesRequest() {
        Connection con = HttpConnection.connect("http://example.com");
        con.ignoreContentType(true);
        assertTrue(con.request().ignoreContentType());

        con.ignoreContentType(false);
        assertFalse(con.request().ignoreContentType());
    }

    @Test
    public void testData_fromMapAndCollection_populatesDataList() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("k1", "v1");
        con.data(dataMap);

        assertEquals(1, con.request().data().size());
        assertEquals("v1", con.data("k1").value());

        Collection<Connection.KeyVal> coll = new ArrayList<>();
        coll.add(HttpConnection.KeyVal.create("k2", "v2"));
        con.data(coll);

        assertEquals(2, con.request().data().size());
        assertEquals("v2", con.data("k2").value());
    }

    @Test
    public void testData_streamOverloads_addsStreamData() {
        Connection con = HttpConnection.connect("http://example.com");
        InputStream in = new ByteArrayInputStream("data".getBytes());
        con.data("upload", "file.bin", in);

        Connection.KeyVal kv = con.data("upload");
        assertNotNull(kv);
        assertEquals("file.bin", kv.value());
        assertTrue(kv.hasInputStream());

        InputStream in2 = new ByteArrayInputStream("data2".getBytes());
        con.data("upload2", "file2.txt", in2, "text/plain");
        Connection.KeyVal kv2 = con.data("upload2");
        assertNotNull(kv2);
        assertEquals("text/plain", kv2.contentType());
    }

    @Test
    public void testRequestBody_setterAndGetter_worksCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.requestBody("raw payload string");
        assertEquals("raw payload string", con.request().requestBody());
    }

    @Test
    public void testParser_setterAndGetter_configuresParser() {
        Connection con = HttpConnection.connect("http://example.com");
        Parser xmlParser = Parser.xmlParser();
        con.parser(xmlParser);
        assertSame(xmlParser, con.request().parser());
    }

    @Test
    public void testCookies_mapSetter_setsMultipleCookies() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> cookies = new HashMap<>();
        cookies.put("c1", "v1");
        cookies.put("c2", "v2");
        con.cookies(cookies);

        assertEquals("v1", con.request().cookie("c1"));
        assertEquals("v2", con.request().cookie("c2"));
        assertEquals(2, con.request().cookies().size());
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
    public void testSslSocketFactory_setter_configuresSslSocketFactory() {
        Connection con = HttpConnection.connect("https://example.com");
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        con.sslSocketFactory(factory);
        assertSame(factory, con.request().sslSocketFactory());
    }

    @Test
    public void testResponse_newResponseProperties_initializedProperly() {
        HttpConnection.Response res = new HttpConnection.Response();
        assertEquals(Connection.Method.GET, res.method());
        assertNull(res.contentType());
        assertNull(res.charset());
        assertEquals(0, res.statusCode());
        assertNull(res.statusMessage());
    }

    @Test
    public void testKeyVal_createSimple_initializesProperly() {
        Connection.KeyVal kv = HttpConnection.KeyVal.create("username", "admin");
        assertEquals("username", kv.key());
        assertEquals("admin", kv.value());
        assertFalse(kv.hasInputStream());
        assertNull(kv.inputStream());
        assertNull(kv.contentType());

        kv.key("newKey");
        kv.value("newValue");
        assertEquals("newKey", kv.key());
        assertEquals("newValue", kv.value());
    }

    @Test
    public void testHeaders_headerWithMultipleValuesValidation() {
        Connection con = HttpConnection.connect("http://example.com");
        assertFalse(con.request().hasHeader("non-existent"));
        assertNull(con.request().header("non-existent"));
        assertEquals(Collections.emptyList(), con.request().headers("non-existent"));

        con.request().addHeader("Multi", "val1");
        con.request().addHeader("Multi", "val2");
        assertTrue(con.request().hasHeader("Multi"));
        assertTrue(con.request().hasHeaderWithValue("multi", "val1"));
        assertTrue(con.request().hasHeaderWithValue("multi", "val2"));
        assertFalse(con.request().hasHeaderWithValue("multi", "val3"));
    }
}