package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    // Tests adding an element to form and retrieving elements list
    @Test
    public void testElements_addElement_returnsElementsList() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.attr("name", "user");
        input.attr("value", "john");

        form.addElement(input);

        assertEquals(1, form.elements().size());
        assertEquals(input, form.elements().get(0));
    }

    // Tests standard text inputs in formData
    @Test
    public void testFormData_textInput_returnsKeyVal() {
        String html = "<form action='/submit'><input name='username' value='testuser'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("testuser", data.get(0).value());
    }

    // Tests that elements with disabled attribute are skipped
    @Test
    public void testFormData_disabledInput_skipped() {
        String html = "<form action='/submit'>" +
                "<input name='username' value='testuser' disabled/>" +
                "<input name='email' value='test@example.com'/>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("email", data.get(0).key());
        assertEquals("test@example.com", data.get(0).value());
    }

    // Tests that inputs without a name attribute or empty name are skipped
    @Test
    public void testFormData_emptyNameInput_skipped() {
        String html = "<form action='/submit'>" +
                "<input name='' value='noname'/>" +
                "<input value='unnamed'/>" +
                "<input name='valid' value='present'/>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
        assertEquals("present", data.get(0).value());
    }

    // Tests select element with selected option
    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedValue() {
        String html = "<form action='/submit'>" +
                "<select name='city'>" +
                "<option value='ny'>New York</option>" +
                "<option value='la' selected>Los Angeles</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("la", data.get(0).value());
    }

    // Tests select element without explicit selected option (defaults to first option)
    @Test
    public void testFormData_selectWithoutSelectedOption_returnsFirstOptionValue() {
        String html = "<form action='/submit'>" +
                "<select name='city'>" +
                "<option value='ny'>New York</option>" +
                "<option value='la'>Los Angeles</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("ny", data.get(0).value());
    }

    // Tests select element with multiple selected options
    @Test
    public void testFormData_selectMultipleSelectedOptions_returnsAllSelectedValues() {
        String html = "<form action='/submit'>" +
                "<select name='color' multiple>" +
                "<option value='red' selected>Red</option>" +
                "<option value='green'>Green</option>" +
                "<option value='blue' selected>Blue</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(2, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value());
        assertEquals("color", data.get(1).key());
        assertEquals("blue", data.get(1).value());
    }

    // Tests checkboxes and radios when checked with and without custom values
    @Test
    public void testFormData_checkboxAndRadioChecked_returnsValuesOrOn() {
        String html = "<form action='/submit'>" +
                "<input type='checkbox' name='agree' checked/>" +
                "<input type='checkbox' name='subscribe' value='yes' checked/>" +
                "<input type='radio' name='gender' value='M' checked/>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(3, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
        assertEquals("subscribe", data.get(1).key());
        assertEquals("yes", data.get(1).value());
        assertEquals("gender", data.get(2).key());
        assertEquals("M", data.get(2).value());
    }

    // Tests checkboxes and radios when not checked (skipped)
    @Test
    public void testFormData_checkboxAndRadioUnchecked_skipped() {
        String html = "<form action='/submit'>" +
                "<input type='checkbox' name='agree'/>" +
                "<input type='radio' name='gender' value='M'/>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertTrue(data.isEmpty());
    }

    // Tests non-submittable elements inside form are ignored in formData
    @Test
    public void testFormData_nonSubmittableElement_ignored() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("name", "divName");
        div.val("divValue");

        form.addElement(div);

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // Tests submit() creating a GET connection by default with parsed absolute action URL
    @Test
    public void testSubmit_defaultMethodGet_returnsConnection() {
        String html = "<form action='/submit.php'><input name='q' value='jsoup'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();

        assertEquals(Connection.Method.GET, con.request().method());
        assertEquals("http://example.com/submit.php", con.request().url().toExternalForm());
        assertEquals(1, con.request().data().size());
        assertEquals("q", con.request().data().iterator().next().key());
        assertEquals("jsoup", con.request().data().iterator().next().value());
    }

    // Tests submit() with POST method
    @Test
    public void testSubmit_postMethod_returnsPostConnection() {
        String html = "<form action='/login' method='POST'><input name='user' value='admin'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();

        assertEquals(Connection.Method.POST, con.request().method());
        assertEquals("http://example.com/login", con.request().url().toExternalForm());
    }

    // Tests submit() when form action is missing, falls back to baseUri
    @Test
    public void testSubmit_missingAction_usesBaseUri() {
        String html = "<form><input name='q' value='test'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com/search");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();

        assertEquals("http://example.com/search", con.request().url().toExternalForm());
    }

    // Tests submit() exception when action URL cannot be determined
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_noActionAndEmptyBaseUri_throwsException() {
        String html = "<form><input name='q' value='test'/></form>";
        Document doc = Jsoup.parse(html, "");
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }

    // Tests removal of form element from DOM to verify form data updates accordingly (Defects4J bug 69)
    @Test
    public void testFormData_removedElementFromDom_notPresentInFormData() {
        String html = "<form action='/submit'>" +
                "<input name='one' value='1'/>" +
                "<input name='two' value='2'/>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Element inputTwo = doc.select("input[name=two]").first();
        inputTwo.remove();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("one", data.get(0).key());
        assertEquals("1", data.get(0).value());
    }
}