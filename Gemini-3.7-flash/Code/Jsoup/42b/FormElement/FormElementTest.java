package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    // Tests that a new FormElement initially has empty elements
    @Test
    public void testElements_emptyForm_returnsEmptyElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertNotNull(form.elements());
        assertTrue(form.elements().isEmpty());
    }

    // Tests adding an element to the form controls list and method chaining
    @Test
    public void testAddElement_validElement_addsAndReturnsForm() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        FormElement returnedForm = form.addElement(input);

        assertSame(form, returnedForm);
        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    // Tests submit with action URL and POST method
    @Test
    public void testSubmit_postMethodWithAction_returnsConfiguredConnection() {
        String html = "<form action='/submit' method='POST'><input name='q' value='jsoup'/></form>";
        FormElement form = (FormElement) Jsoup.parse(html, "http://example.com").select("form").first();

        Connection con = form.submit();
        assertEquals("http://example.com/submit", con.request().url().toExternalForm());
        assertEquals(Connection.Method.POST, con.request().method());
        assertEquals(1, con.request().data().size());
        assertEquals("q", con.request().data().iterator().next().key());
        assertEquals("jsoup", con.request().data().iterator().next().value());
    }

    // Tests submit fallback to base URI and default GET method
    @Test
    public void testSubmit_noActionUsesBaseUriAndDefaultGet_returnsConnection() {
        String html = "<form><input name='test' value='val'/></form>";
        FormElement form = (FormElement) Jsoup.parse(html, "http://example.com/search").select("form").first();

        Connection con = form.submit();
        assertEquals("http://example.com/search", con.request().url().toExternalForm());
        assertEquals(Connection.Method.GET, con.request().method());
    }

    // Tests submit throwing IllegalArgumentException when action and baseUri are empty
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_emptyActionAndNoBaseUri_throwsException() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.submit();
    }

    // Tests formData collection with standard text input and textarea controls
    @Test
    public void testFormData_textAndTextareaInputs_returnsCorrectKeyVals() {
        String html = "<form>" +
                "<input name='username' value='john'/>" +
                "<textarea name='bio'>hello world</textarea>" +
                "</form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("john", data.get(0).value());
        assertEquals("bio", data.get(1).key());
        assertEquals("hello world", data.get(1).value());
    }

    // Tests elements with empty name or non-submittable tags are ignored
    @Test
    public void testFormData_emptyNameOrNonSubmittable_ignored() {
        String html = "<form>" +
                "<input name='' value='skip'/>" +
                "<input value='no-name'/>" +
                "<div><input name='valid' value='ok'/></div>" +
                "</form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();
        Element div = form.select("div").first();
        form.addElement(div); // Add non-submittable element explicitly

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
        assertEquals("ok", data.get(0).value());
    }

    // Tests select element with explicitly selected options
    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedValue() {
        String html = "<form><select name='city'>" +
                "<option value='ny'>New York</option>" +
                "<option value='lon' selected>London</option>" +
                "</form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("lon", data.get(0).value());
    }

    // Tests select element without explicit selected attribute defaults to first option
    @Test
    public void testFormData_selectWithoutSelected_defaultsToFirstOption() {
        String html = "<form><select name='fruit'>" +
                "<option value='apple'>Apple</option>" +
                "<option value='banana'>Banana</option>" +
                "</select></form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("fruit", data.get(0).key());
        assertEquals("apple", data.get(0).value());
    }

    // Tests select element with no option elements at all
    @Test
    public void testFormData_selectWithNoOptions_returnsNoData() {
        String html = "<form><select name='empty'></select></form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // Tests checked and unchecked checkboxes and radio buttons
    @Test
    public void testFormData_checkboxAndRadio_onlyCheckedIncluded() {
        String html = "<form>" +
                "<input type='checkbox' name='cb_checked' value='1' checked/>" +
                "<input type='checkbox' name='cb_unchecked' value='2'/>" +
                "<input type='radio' name='r_checked' value='a' checked/>" +
                "<input type='radio' name='r_unchecked' value='b'/>" +
                "</form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("cb_checked", data.get(0).key());
        assertEquals("1", data.get(0).value());
        assertEquals("r_checked", data.get(1).key());
        assertEquals("a", data.get(1).value());
    }

    // Tests checked checkbox without explicit value attribute defaults to "on"
    @Test
    public void testFormData_checkedCheckboxWithoutValue_defaultsToOn() {
        String html = "<form><input type='checkbox' name='agree' checked/></form>";
        FormElement form = (FormElement) Jsoup.parse(html).select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    // Tests equals method on FormElement
    @Test
    public void testEquals_sameObject_returnsTrue() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertTrue(form.equals(form));
        assertFalse(form.equals(null));
    }
}