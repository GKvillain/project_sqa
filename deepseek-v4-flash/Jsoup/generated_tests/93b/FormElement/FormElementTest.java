package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class FormElementTest {

    private FormElement parseForm(String html) {
        return (FormElement) Jsoup.parse(html).select("form").first();
    }

    private Element element(String tagName) {
        return new Element(Tag.valueOf(tagName), "");
    }

    // Tests simple input field value
    @Test
    public void testFormData_singleInput_returnsCorrectKeyVal() {
        FormElement form = parseForm("<form><input name='q' value='jsoup'></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
        assertEquals("jsoup", data.get(0).value());
    }

    // Tests that elements with missing or empty names are skipped
    @Test
    public void testFormData_missingOrEmptyName_skipped() {
        FormElement form = parseForm(
            "<form><input value='no-name'><input name='' value='empty'><input name='ok' value='yes'></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("ok", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    // Tests that disabled controls are skipped
    @Test
    public void testFormData_disabledControl_skipped() {
        FormElement form = parseForm(
            "<form><input name='a' value='a' disabled><input name='b' value='b'></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("b", data.get(0).key());
        assertEquals("b", data.get(0).value());
    }

    // Tests that non-form-submittable elements are skipped
    @Test
    public void testFormData_nonSubmittableElement_skipped() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.addElement(element("div").attr("name", "x"));

        assertEquals(0, form.formData().size());
    }

    // Tests select with an explicitly selected option
    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedValue() {
        FormElement form = parseForm(
            "<form><select name='s'><option value='a'>A</option><option value='b' selected>B</option></select></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("s", data.get(0).key());
        assertEquals("b", data.get(0).value());
    }

    // Tests select without a selected option falls back to the first option
    @Test
    public void testFormData_selectWithoutSelectedOption_returnsFirstOption() {
        FormElement form = parseForm(
            "<form><select name='s'><option value='a'>A</option><option value='b'>B</option></select></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("a", data.get(0).value());
    }

    // Tests select with no options produces no data
    @Test
    public void testFormData_selectWithNoOptions_returnsEmptyList() {
        FormElement form = parseForm("<form><select name='s'></select></form>");

        assertEquals(0, form.formData().size());
    }

    // Tests checked checkbox with a value
    @Test
    public void testFormData_checkedCheckboxWithValue_returnsValue() {
        FormElement form = parseForm(
            "<form><input type='checkbox' name='cb' value='yes' checked></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("cb", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    // Tests unchecked checkbox is skipped
    @Test
    public void testFormData_uncheckedCheckbox_skipped() {
        FormElement form = parseForm(
            "<form><input type='checkbox' name='cb' value='yes'></form>");

        assertEquals(0, form.formData().size());
    }

    // Tests checked checkbox with no value attribute defaults to "on"
    @Test
    public void testFormData_checkedCheckboxWithoutValue_returnsOn() {
        FormElement form = parseForm("<form><input type='checkbox' name='cb' checked></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("cb", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    // Tests checked radio button with a value
    @Test
    public void testFormData_checkedRadioWithValue_returnsValue() {
        FormElement form = parseForm(
            "<form><input type='radio' name='r' value='m' checked></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("m", data.get(0).value());
    }

    // Tests textarea value is returned as form data
    @Test
    public void testFormData_textarea_returnsText() {
        FormElement form = parseForm("<form><textarea name='ta'>hello</textarea></form>");
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("ta", data.get(0).key());
        assertEquals("hello", data.get(0).value());
    }

    // Tests that formData returns a new independent list each time
    @Test
    public void testFormData_clearReturnedList_doesNotAffectNextCall() {
        FormElement form = parseForm("<form><input name='a' value='b'></form>");

        List<Connection.KeyVal> data = form.formData();
        data.clear();

        assertEquals(1, form.formData().size());
    }

    // Tests addElement chaining and storage
    @Test
    public void testAddElement_returnsThisAndAddsElement() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.org/", new Attributes());
        Element input = element("input").attr("name", "a");

        assertSame(form, form.addElement(input));
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    // Tests removing a direct child also removes it from the form's elements list
    @Test
    public void testRemoveChild_removesControlFromFormElements() {
        FormElement form = parseForm("<form><input name='a' value='1'><input name='b' value='2'></form>");

        assertEquals(2, form.elements().size());
        form.select("input").first().remove();

        assertEquals(1, form.elements().size());
        assertEquals("b", form.elements().get(0).attr("name"));
    }

    // Tests submit when no action or base URI is available
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_formWithoutActionOrBaseUri_throwsException() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.addElement(element("input").attr("name", "a").attr("value", "b"));

        form.submit();
    }

    // Tests submit default method is GET
    @Test
    public void testSubmit_formWithActionDefaultMethod_isGet() {
        FormElement form = parseForm("<form action='http://example.org/submit'><input name='a' value='b'></form>");

        Connection connection = form.submit();

        assertNotNull(connection);
        assertSame(Connection.Method.GET, connection.request().method());
    }

    // Tests submit with POST method
    @Test
    public void testSubmit_formWithPostMethod_isPost() {
        FormElement form = parseForm(
            "<form action='http://example.org/submit' method='POST'><input name='a' value='b'></form>");

        Connection connection = form.submit();

        assertNotNull(connection);
        assertSame(Connection.Method.POST, connection.request().method());
    }
}