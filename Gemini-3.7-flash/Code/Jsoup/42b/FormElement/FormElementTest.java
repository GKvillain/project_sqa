package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class FormElementTest {

    // Tests elements collection and addElement method
    @Test
    public void testElements_addElement_returnsAddedElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        form.addElement(input);

        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    // Tests formData with basic text inputs and non-submittable/unnamed elements
    @Test
    public void testFormData_basicInputs_extractsExpectedKeyVals() {
        String html = "<form action='/submit' method='POST'>" +
                "<input type='text' name='username' value='john_doe' />" +
                "<input type='password' name='pass' value='secret' />" +
                "<input type='text' value='noname' />" +
                "<input type='text' name='' value='emptyname' />" +
                "<div><p>Not submittable</p></div>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(2, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("john_doe", data.get(0).value());
        assertEquals("pass", data.get(1).key());
        assertEquals("secret", data.get(1).value());
    }

    // Tests formData with select element having a selected option
    @Test
    public void testFormData_selectWithSelectedOption_extractsSelectedValue() {
        String html = "<form>" +
                "<select name='choice'>" +
                "<option value='one'>One</option>" +
                "<option value='two' selected>Two</option>" +
                "<option value='three'>Three</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("choice", data.get(0).key());
        assertEquals("two", data.get(0).value());
    }

    // Tests formData with select element having no explicit selected option (defaults to first option)
    @Test
    public void testFormData_selectWithoutSelectedOption_defaultsToFirstOption() {
        String html = "<form>" +
                "<select name='choice'>" +
                "<option value='first'>First</option>" +
                "<option value='second'>Second</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("choice", data.get(0).key());
        assertEquals("first", data.get(0).value());
    }

    // Tests formData with empty select element
    @Test
    public void testFormData_emptySelect_noDataAdded() {
        String html = "<form><select name='empty'></select></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(0, data.size());
    }

    // Tests formData with checkbox and radio buttons (checked vs unchecked)
    @Test
    public void testFormData_checkboxAndRadio_onlyCheckedIncluded() {
        String html = "<form>" +
                "<input type='checkbox' name='cb_checked' value='cb1' checked />" +
                "<input type='checkbox' name='cb_unchecked' value='cb2' />" +
                "<input type='radio' name='r_group' value='r1' />" +
                "<input type='radio' name='r_group' value='r2' checked />" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(2, data.size());
        assertEquals("cb_checked", data.get(0).key());
        assertEquals("cb1", data.get(0).value());
        assertEquals("r_group", data.get(1).key());
        assertEquals("r2", data.get(1).value());
    }

    // Tests formData with checkbox that has no value attribute (should default to "on")
    @Test
    public void testFormData_checkboxWithoutValue_usesDefaultValueOn() {
        String html = "<form>" +
                "<input type='checkbox' name='check_default' checked />" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("check_default", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    // Tests formData with textarea
    @Test
    public void testFormData_textarea_extractsText() {
        String html = "<form>" +
                "<textarea name='comments'>Sample text here</textarea>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("comments", data.get(0).key());
        assertEquals("Sample text here", data.get(0).value());
    }

    // Tests submit with relative action and POST method
    @Test
    public void testSubmit_postMethodWithRelativeAction_createsCorrectConnection() {
        String html = "<form action='/post-target' method='post'>" +
                "<input type='text' name='q' value='jsoup' />" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com/dir/");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();

        assertEquals(Connection.Method.POST, con.request().method());
        assertEquals("http://example.com/post-target", con.request().url().toExternalForm());
        assertEquals(1, con.request().data().size());
        assertEquals("q", con.request().data().iterator().next().key());
    }

    // Tests submit without action attribute falling back to baseUri with default GET method
    @Test
    public void testSubmit_noActionAttribute_usesBaseUriAndGetMethod() {
        String html = "<form>" +
                "<input type='hidden' name='token' value='12345' />" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com/test");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();

        assertEquals(Connection.Method.GET, con.request().method());
        assertEquals("http://example.com/test", con.request().url().toExternalForm());
    }

    // Tests submit when action/baseUri is empty throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_emptyActionAndBaseUri_throwsIllegalArgumentException() {
        String html = "<form></form>";
        Document doc = Jsoup.parse(html); // baseUri is empty
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }

    // Tests equals method consistency with Element
    @Test
    public void testEquals_sameAndDifferentObjects_behavesCorrectly() {
        FormElement form1 = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        FormElement form2 = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        assertTrue(form1.equals(form1));
        assertFalse(form1.equals(form2));
        assertFalse(form1.equals(null));
        assertFalse(form1.equals("string"));
    }
}