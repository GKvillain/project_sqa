package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for FormElement (Defects4J bug 42b).
 */
public class FormElementTest {

    // --- Constructor and elements() / addElement() ---

    @Test
    public void testConstructorAndAddElement_elementsEmpty_returnsList() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertNotNull(form.elements());
        assertTrue(form.elements().isEmpty());

        Element input = new Element(Tag.valueOf("input"), "");
        FormElement same = form.addElement(input);
        assertSame(form, same);
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    // --- formData() ---

    @Test
    public void testFormData_inputText_returnsKeyVal() {
        String html = "<form><input name='a' value='1'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
        assertEquals("1", data.get(0).value());
    }

    @Test
    public void testFormData_inputWithoutName_skipped() {
        String html = "<form><input name='' value='1'><input value='2'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormData_notFormSubmittableElement_skipped() {
        // div is not form submittable
        String html = "<form><div name='x'>text</div></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedValue() {
        String html = "<form><select name='s'><option value='a' selected>A</option><option value='b'>B</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("s", data.get(0).key());
        assertEquals("a", data.get(0).value());
    }

    @Test
    public void testFormData_selectWithoutSelectedOption_returnsFirstOption() {
        String html = "<form><select name='s'><option value='a'>A</option><option value='b'>B</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("s", data.get(0).key());
        assertEquals("a", data.get(0).value());
    }

    @Test
    public void testFormData_selectWithMultipleSelectedOptions_returnsAllSelected() {
        String html = "<form><select name='s' multiple><option value='a' selected>A</option><option value='b'>B</option><option value='c' selected>C</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("s", data.get(0).key());
        assertEquals("a", data.get(0).value());
        assertEquals("s", data.get(1).key());
        assertEquals("c", data.get(1).value());
    }

    @Test
    public void testFormData_checkboxChecked_returnsValue() {
        String html = "<form><input type='checkbox' name='c' value='yes' checked></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("c", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    @Test
    public void testFormData_checkboxNotChecked_notReturned() {
        String html = "<form><input type='checkbox' name='c' value='yes'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormData_radioChecked_returnsValue() {
        String html = "<form><input type='radio' name='r' value='on' checked></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("r", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    @Test
    public void testFormData_radioNotChecked_notReturned() {
        String html = "<form><input type='radio' name='r' value='on'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test
    public void testFormData_inputTypeImage_returnsKeyVal() {
        // input type="image" is form submittable and should be included
        String html = "<form><input type='image' name='img' value='submit'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("img", data.get(0).key());
        assertEquals("submit", data.get(0).value());
    }

    @Test
    public void testFormData_mixedControls_returnsAllSubmittableWithName() {
        String html = "<form>" +
                "<input name='a' value='1'>" +
                "<input type='hidden' name='b' value='2'>" +
                "<select name='c'><option value='x' selected>X</option></select>" +
                "<input type='checkbox' name='d' value='yes' checked>" +
                "<input type='radio' name='e' value='no'>" + // not checked, should be omitted
                "<input type='text' name='f' value='3'>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(4, data.size()); // a, b, c, d, f (5? no e omitted) Actually a,b,c,d,f = 5? 1,2,3,4,5? Let's count: a,b,c,d,f = 5. But c is select with one selected => 1, so a,b,c,d,f = 5.
        // Let's check: a=1, b=2, c=x, d=yes, f=3 => 5.
        assertEquals(5, data.size());
        // Verify order (depends on iteration order of elements)
        assertEquals("a", data.get(0).key());
        assertEquals("b", data.get(1).key());
        assertEquals("c", data.get(2).key());
        assertEquals("d", data.get(3).key());
        assertEquals("f", data.get(4).key());
    }

    // --- submit() ---

    @Test
    public void testSubmit_actionPresentMethodPost_returnsConnectionWithPostMethod() {
        String html = "<form action='/submit' method='post'><input name='a' value='1'></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();
        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.POST, con.method());
        // verify data
        List<Connection.KeyVal> data = con.data();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
        assertEquals("1", data.get(0).value());
    }

    @Test
    public void testSubmit_actionPresentMethodGet_returnsConnectionWithGetMethod() {
        String html = "<form action='/search' method='get'><input name='q' value='test'></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();
        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.GET, con.method());
        List<Connection.KeyVal> data = con.data();
        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
        assertEquals("test", data.get(0).value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_noActionAndNoBaseUri_throwsIllegalArgumentException() {
        // form has no action attribute and document has empty base URI
        String html = "<form><input name='a' value='1'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        form.submit();
    }

    @Test
    public void testSubmit_actionPresentWithoutBaseUri_usesActionAbsolute() {
        // action already absolute, baseUri not needed
        String html = "<form action='http://example.com/submit' method='post'><input name='a' value='1'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.POST, con.method());
        List<Connection.KeyVal> data = con.data();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
        assertEquals("1", data.get(0).value());
    }
}