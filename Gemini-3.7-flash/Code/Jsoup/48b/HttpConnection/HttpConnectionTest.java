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

    // Tests that multiple response headers with the same name are combined with a comma
    @Test
    public void testProcessResponseHeaders_multipleValuesForSameHeader_combinesWithComma() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Cache-Control", Arrays.asList("no-cache", "no-store", "must-revalidate"));

        res.processResponseHeaders(headers);

        assertEquals("no-cache, no-store, must-revalidate", res.header("Cache-Control"));
    }

    // Tests parsing of Set-Cookie response headers into response cookies
    @Test
    public void testProcessResponseHeaders_setCookieHeaders_parsesCookies() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Set-Cookie", Arrays.asList("session_id=abc1234; Path=/; Secure", "theme=dark; Path=/"));

        res.processResponseHeaders(headers);

        assertTrue(res.hasCookie("session_id"));
        assertEquals("abc1234", res.cookie("session_id"));
        assertTrue(res.hasCookie("theme"));
        assertEquals("dark", res.cookie("theme"));
    }

    // Tests handling of null header key in response headers map
    @Test
    public void testProcessResponseHeaders_nullHeaderName_ignored() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        headers.put("Content-Type", Collections.singletonList("text/html"));

        res.processResponseHeaders(headers);

        assertEquals("text/html", res.header("Content-Type"));
    }

    // Tests connect with valid URL string containing spaces that need encoding
    @Test
    public void testConnect_stringUrlWithSpaces_encodesSpaces() {
        Connection con = HttpConnection.connect("http://example.com/test path");
        assertEquals("http://example.com/test%20path", con.request().url().toExternalForm());
    }

    // Tests connect with URL object
    @Test
    public void testConnect_validUrlObject_setsUrl() throws MalformedURLException {
        URL url = new URL("http://example.com");
        Connection con = HttpConnection.connect(url);
        assertEquals(url, con.request().url());
    }

    // Tests connect with invalid URL string throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConnect_malformedUrlString_throwsException() {
        HttpConnection.connect("not_a_valid_url");
    }

    // Tests case-insensitive header management
    @Test
    public void testHeader_caseInsensitiveOperations_worksCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Accept-Encoding", "gzip, deflate");

        assertTrue(con.request().hasHeader("accept-encoding"));
        assertTrue(con.request().hasHeader("ACCEPT-ENCODING"));
        assertEquals("gzip, deflate", con.request().header("accept-encoding"));
        assertTrue(con.request().hasHeaderWithValue("accept-encoding", "gzip, deflate"));
        assertFalse(con.request().hasHeaderWithValue("accept-encoding", "identity"));

        con.request().removeHeader("ACCEPT-ENCODING");
        assertFalse(con.request().hasHeader("Accept-Encoding"));
    }

    // Tests cookie management in Base/Request
    @Test
    public void testCookie_addAndRemove_worksCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.cookie("token", "xyz");

        assertTrue(con.request().hasCookie("token"));
        assertEquals("xyz", con.request().cookie("token"));

        con.request().removeCookie("token");
        assertFalse(con.request().hasCookie("token"));
        assertNull(con.request().cookie("token"));
    }

    // Tests adding multiple cookies using a map
    @Test
    public void testCookies_mapInput_addsAllCookies() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> cookies = new LinkedHashMap<String, String>();
        cookies.put("c1", "v1");
        cookies.put("c2", "v2");
        con.cookies(cookies);

        assertEquals("v1", con.request().cookie("c1"));
        assertEquals("v2", con.request().cookie("c2"));
    }

    // Tests user agent and referrer helper methods
    @Test
    public void testUserAgentAndReferrer_setsCorrectHeaders() {
        Connection con = HttpConnection.connect("http://example.com");
        con.userAgent("Mozilla/5.0");
        con.referrer("http://google.com");

        assertEquals("Mozilla/5.0", con.request().header("User-Agent"));
        assertEquals("http://google.com", con.request().header("Referer"));
    }

    // Tests data key-value pairs varargs
    @Test
    public void testData_varargs_populatesDataCollection() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("k1", "v1", "k2", "v2");

        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(2, data.size());
    }

    // Tests data varargs with odd number of arguments
    @Test(expected = IllegalArgumentException.class)
    public void testData_oddNumberOfVarargs_throwsException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("k1", "v1", "k2");
    }

    // Tests data key-value with input stream
    @Test
    public void testData_withInputStream_createsStreamKeyVal() {
        Connection con = HttpConnection.connect("http://example.com");
        InputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        con.data("uploadFile", "test.txt", in);

        Collection<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        Connection.KeyVal kv = data.iterator().next();
        assertEquals("uploadFile", kv.key());
        assertEquals("test.txt", kv.value());
        assertTrue(kv.hasInputStream());
        assertEquals(in, kv.inputStream());
    }

    // Tests KeyVal create and toString
    @Test
    public void testKeyVal_basicOperations_returnsExpectedValues() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("username", "admin");
        assertEquals("username", kv.key());
        assertEquals("admin", kv.value());
        assertFalse(kv.hasInputStream());
        assertNull(kv.inputStream());
        assertEquals("username=admin", kv.toString());
    }

    // Tests boundary values for timeout and maxBodySize
    @Test
    public void testTimeoutAndMaxBodySize_zeroValue_allowed() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(0);
        con.maxBodySize(0);

        assertEquals(0, con.request().timeout());
        assertEquals(0, con.request().maxBodySize());
    }

    // Tests negative timeout validation
    @Test(expected = IllegalArgumentException.class)
    public void testTimeout_negativeValue_throwsException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(-1);
    }

    // Tests negative maxBodySize validation
    @Test(expected = IllegalArgumentException.class)
    public void testMaxBodySize_negativeValue_throwsException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.maxBodySize(-1);
    }

    // Tests invalid charset for postDataCharset
    @Test(expected = IllegalCharsetNameException.class)
    public void testPostDataCharset_invalidCharset_throwsException() {
        Connection con = HttpConnection.connect("http://example.com");
        con.postDataCharset("INVALID_CHARSET_NAME_123");
    }

    // Tests valid charset for postDataCharset
    @Test
    public void testPostDataCharset_validCharset_setsCharset() {
        Connection con = HttpConnection.connect("http://example.com");
        con.postDataCharset("UTF-8");
        assertEquals("UTF-8", con.request().postDataCharset());
    }

    // Tests method, followRedirects, ignoreHttpErrors, and ignoreContentType configuration
    @Test
    public void testRequest_configurationFlags_setCorrectly() {
        Connection con = HttpConnection.connect("http://example.com");
        con.method(Connection.Method.POST);
        con.followRedirects(false);
        con.ignoreHttpErrors(true);
        con.ignoreContentType(true);
        con.validateTLSCertificates(false);

        assertEquals(Connection.Method.POST, con.request().method());
        assertFalse(con.request().followRedirects());
        assertTrue(con.request().ignoreHttpErrors());
        assertTrue(con.request().ignoreContentType());
        assertFalse(con.request().validateTLSCertificates());
    }
}