package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Connection;
import org.jsoup.parser.Tag;
import java.util.List;

public class FormElementTest {

    private FormElement newForm(String baseUri) {
        return new FormElement(Tag.valueOf("form"), baseUri, new Attributes());
    }

    private Element namedInput(String name, String value) {
        return new Element(Tag.valueOf("input"), "")
                .attr("name", name)
                .attr("value", value);
    }

    // Tests initial elements list is empty
    @Test
    public void testElements_initialList_returnsEmpty() {
        FormElement form = newForm("");
        assertTrue(form.elements().isEmpty());
    }

    // Tests addElement returns this and adds the element
    @Test
    public void testAddElement_returnsThisAndAddsToElements() {
        FormElement form = newForm("");
        Element input = namedInput("a", "b");

        FormElement result = form.addElement(input);

        assertSame(form, result);
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    // Tests normal text input is submitted
    @Test
    public void testFormData_textInput_returnsKeyValue() {
        FormElement form = newForm("");
        form.addElement(namedInput("q", "jsoup").attr("type", "text"));

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
        assertEquals("jsoup", data.get(0).value());
    }

    // Tests non-form-submittable element is skipped
    @Test
    public void testFormData_nonSubmittableElement_skipped() {
        FormElement form = newForm("");
        form.addElement(new Element(Tag.valueOf("div"), "")
                .attr("name", "x")
                .attr("value", "y"));

        assertTrue(form.formData().isEmpty());
    }

    // Tests disabled input is skipped
    @Test
    public void testFormData_disabledInput_skipped() {
        FormElement form = newForm("");
        form.addElement(namedInput("a", "b").attr("disabled", "disabled"));

        assertTrue(form.formData().isEmpty());
    }

    // Tests input without name is skipped
    @Test
    public void testFormData_inputWithoutName_skipped() {
        FormElement form = newForm("");
        form.addElement(namedInput("", "b"));

        assertTrue(form.formData().isEmpty());
    }

    // Tests checked checkbox without value uses "on"
    @Test
    public void testFormData_checkedCheckboxWithoutValue_returnsOn() {
        FormElement form = newForm("");
        form.addElement(namedInput("agree", "").attr("type", "checkbox").attr("checked", "checked"));

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    // Tests checked checkbox with value uses its value
    @Test
    public void testFormData_checkedCheckboxWithValue_returnsValue() {
        FormElement form = newForm("");
        form.addElement(namedInput("agree", "yes").attr("type", "checkbox").attr("checked", "checked"));

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("yes", data.get(0).value());
    }

    // Tests unchecked checkbox is skipped
    @Test
    public void testFormData_uncheckedCheckbox_skipped() {
        FormElement form = newForm("");
        form.addElement(namedInput("agree", "yes").attr("type", "checkbox"));

        assertTrue(form.formData().isEmpty());
    }

    // Tests checked radio is included and unchecked radio is skipped
    @Test
    public void testFormData_checkedRadio_returnsValue() {
        FormElement form = newForm("");
        form.addElement(namedInput("r", "a").attr("type", "radio").attr("checked", "checked"));
        form.addElement(namedInput("r", "b").attr("type", "radio"));

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("a", data.get(0).value());
    }

    // Tests select with a selected option
    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedValue() {
        FormElement form = newForm("");
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "pet");
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "cat").attr("selected", "selected"));
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "dog"));
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("pet", data.get(0).key());
        assertEquals("cat", data.get(0).value());
    }

    // Tests single select with no selected option uses first option
    @Test
    public void testFormData_selectWithoutSelectedOption_usesFirstOption() {
        FormElement form = newForm("");
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "pet");
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "dog"));
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "cat"));
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("dog", data.get(0).value());
    }

    // Tests select with multiple selected options returns all selected values
    @Test
    public void testFormData_selectWithMultipleSelected_returnsAll() {
        FormElement form = newForm("");
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "pet");
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "cat").attr("selected", "selected"));
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "dog").attr("selected", "selected"));
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(2, data.size());
        assertEquals("cat", data.get(0).value());
        assertEquals("dog", data.get(1).value());
    }

    // Tests select with no options produces no form data
    @Test
    public void testFormData_selectWithoutOptions_returnsEmpty() {
        FormElement form = newForm("");
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "empty");
        form.addElement(select);

        assertTrue(form.formData().isEmpty());
    }

    // Tests multiple select with no selected option should submit nothing
    @Test
    public void testFormData_multipleSelectWithoutSelectedOption_skipped() {
        FormElement form = newForm("");
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "items").attr("multiple", "multiple");
        select.appendChild(new Element(Tag.valueOf("option"), "").attr("value", "a"));
        form.addElement(select);

        assertTrue(form.formData().isEmpty());
    }

    // Tests textarea uses its text content as value
    @Test
    public void testFormData_textarea_returnsTextContent() {
        FormElement form = newForm("");
        Element textarea = new Element(Tag.valueOf("textarea"), "").attr("name", "comment");
        textarea.text("hello");
        form.addElement(textarea);

        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("comment", data.get(0).key());
        assertEquals("hello", data.get(0).value());
    }

    // Tests submit with no action uses the base URI
    @Test
    public void testSubmit_noAction_usesBaseUri() {
        FormElement form = newForm("http://example.com");
        Connection conn = form.submit();

        assertEquals("http://example.com", conn.request().url().toExternalForm());
    }

    // Tests GET method is selected
    @Test
    public void testSubmit_getMethod_returnsGetConnection() {
        FormElement form = newForm("http://example.com");
        form.attr("action", "/submit");
        form.attr("method", "get");

        Connection conn = form.submit();

        assertEquals(Connection.Method.GET, conn.request().method());
        assertEquals("http://example.com/submit", conn.request().url().toExternalForm());
    }

    // Tests POST method is selected
    @Test
    public void testSubmit_postMethod_returnsPostConnection() {
        FormElement form = newForm("http://example.com");
        form.attr("action", "/submit");
        form.attr("method", "POST");

        Connection conn = form.submit();

        assertEquals(Connection.Method.POST, conn.request().method());
        assertEquals("http://example.com/submit", conn.request().url().toExternalForm());
    }

    // Tests submit with empty base URI and no action throws
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_emptyAction_throwsException() {
        FormElement form = newForm("");
        form.submit();
    }
}