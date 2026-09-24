package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FormElementTest {

    // Tests constructor and elements getter
    @Test
    public void testElements_initialState_returnsEmptyElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertNotNull(form.elements());
        assertEquals(0, form.elements().size());
    }

    // Tests addElement chaining and element retention
    @Test
    public void testAddElement_validElement_addsToElementsList() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        FormElement result = form.addElement(input);

        assertEquals(form, result);
        assertEquals(1, form.elements().size());
        assertEquals(input, form.elements().get(0));
    }

    // Tests submit with action URL and POST method
    @Test
    public void testSubmit_actionAndPostMethod_createsCorrectConnection() {
        String html = "<form action='/submit' method='POST'><input name='user' value='john'></form>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals("http://example.com/submit", conn.request().url().toExternalForm());
        assertEquals(Connection.Method.POST, conn.request().method());
        assertEquals(1, conn.request().data().size());
        assertEquals("user", conn.request().data().iterator().next().key());
        assertEquals("john", conn.request().data().iterator().next().value());
    }

    // Tests submit with default GET method and fallback to baseUri
    @Test
    public void testSubmit_noActionFallbackToBaseUri_createsGetConnection() {
        String html = "<form><input name='q' value='jsoup'></form>";
        Document doc = Jsoup.parse(html, "http://example.com/search");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals("http://example.com/search", conn.request().url().toExternalForm());
        assertEquals(Connection.Method.GET, conn.request().method());
    }

    // Tests submit when action URL cannot be determined
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_missingActionAndBaseUri_throwsIllegalArgumentException() {
        String html = "<form><input name='q' value='test'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        form.submit();
    }

    // Tests disabled input and missing name attribute are skipped
    @Test
    public void testFormData_disabledOrNamelessInputs_skippedInFormData() {
        String html = "<form>" +
                "<input name='' value='no-name'>" +
                "<input name='disabled-input' value='val' disabled>" +
                "<input name='valid-input' value='valid-val'>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid-input", data.get(0).key());
        assertEquals("valid-val", data.get(0).value());
    }

    // Tests non-submittable tags are skipped
    @Test
    public void testFormData_nonSubmittableTags_skippedInFormData() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("name", "divName");
        div.val("divVal");
        form.addElement(div);

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // Tests select element with explicitly selected option
    @Test
    public void testFormData_selectWithSelectedOption_populatesSelectedValue() {
        String html = "<form><select name='city'>" +
                "<option value='ny'>New York</option>" +
                "<option value='lon' selected>London</option>" +
                "<option value='tok'>Tokyo</option>" +
                "</select></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("lon", data.get(0).value());
    }

    // Tests select element without selected attribute defaults to first option
    @Test
    public void testFormData_selectWithoutSelectedOption_defaultsToFirstOption() {
        String html = "<form><select name='color'>" +
                "<option value='red'>Red</option>" +
                "<option value='blue'>Blue</option>" +
                "</select></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value());
    }

    // Tests select element with no options
    @Test
    public void testFormData_selectWithNoOptions_producesNoData() {
        String html = "<form><select name='emptySelect'></select></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // Tests checkbox and radio button handling for checked vs unchecked
    @Test
    public void testFormData_checkboxAndRadio_onlyCheckedIncluded() {
        String html = "<form>" +
                "<input type='checkbox' name='cb_checked' value='1' checked>" +
                "<input type='checkbox' name='cb_unchecked' value='2'>" +
                "<input type='radio' name='r' value='r1'>" +
                "<input type='radio' name='r' value='r2' checked>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("cb_checked", data.get(0).key());
        assertEquals("1", data.get(0).value());
        assertEquals("r", data.get(1).key());
        assertEquals("r2", data.get(1).value());
    }

    // Tests checkbox without value attribute defaults to "on"
    @Test
    public void testFormData_checkboxWithoutValue_defaultsToOn() {
        String html = "<form><input type='checkbox' name='agree' checked></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    // Tests textarea and generic input controls
    @Test
    public void testFormData_textareaAndTextInputs_populatesCorrectly() {
        String html = "<form>" +
                "<input type='text' name='username' value='admin'>" +
                "<textarea name='bio'>Hello World</textarea>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("admin", data.get(0).value());
        assertEquals("bio", data.get(1).key());
        assertEquals("Hello World", data.get(1).value());
    }

    // Tests removal of form child element from DOM removes it from formData
    @Test
    public void testFormData_removedControlFromDom_notIncludedInFormData() {
        String html = "<form><input name='foo' value='bar'><input name='qux' value='baz'></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Element fooInput = form.select("input[name=foo]").first();
        fooInput.remove();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("qux", data.get(0).key());
        assertEquals("baz", data.get(0).value());
    }
}