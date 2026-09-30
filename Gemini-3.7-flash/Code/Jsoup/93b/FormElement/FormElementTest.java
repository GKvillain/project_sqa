package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FormElementTest {

    // Tests adding an element to form control elements
    @Test
    public void testAddElement_addsControlElement_increasesElementListSize() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "http://example.com/");
        form.addElement(input);

        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    // Tests removing a child node removes it from elements list as well
    @Test
    public void testRemoveChild_removesAssociatedControlElement_elementsListUpdated() {
        Document doc = Jsoup.parse("<form><input name='q' value='test'/><input name='r' value='val'/></form>");
        FormElement form = (FormElement) doc.select("form").first();
        Element input = form.select("input[name=q]").first();

        assertEquals(2, form.elements().size());
        form.removeChild(input);
        assertEquals(1, form.elements().size());
        assertFalse(form.elements().contains(input));
    }

    // Tests basic text input and textarea form data extraction
    @Test
    public void testFormData_textAndTextareaInputs_returnsCorrectKeyValList() {
        Document doc = Jsoup.parse("<form><input name='username' value='john'/><textarea name='comment'>hello</textarea></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("john", data.get(0).value());
        assertEquals("comment", data.get(1).key());
        assertEquals("hello", data.get(1).value());
    }

    // Tests skipping disabled input elements
    @Test
    public void testFormData_disabledInputs_skippedInData() {
        Document doc = Jsoup.parse("<form><input name='active' value='1'/><input name='inactive' value='0' disabled/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("active", data.get(0).key());
    }

    // Tests skipping inputs with empty or missing name attribute
    @Test
    public void testFormData_emptyOrMissingName_skippedInData() {
        Document doc = Jsoup.parse("<form><input value='noname'/><input name='' value='emptyname'/><input name='valid' value='ok'/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
    }

    // Tests skipping non-submittable elements (e.g., div, p)
    @Test
    public void testFormData_nonSubmittableTags_skippedInData() {
        Document doc = Jsoup.parse("<form><div><p>text</p></div><input name='valid' value='1'/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
    }

    // Tests select element with explicitly selected option
    @Test
    public void testFormData_selectWithSelectedOption_returnsSelectedOptionValue() {
        Document doc = Jsoup.parse("<form><select name='city'><option value='ny'>NY</option><option value='la' selected>LA</option></select></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("la", data.get(0).value());
    }

    // Tests select element without selected attribute defaults to first option
    @Test
    public void testFormData_selectWithoutExplicitSelection_defaultsToFirstOption() {
        Document doc = Jsoup.parse("<form><select name='city'><option value='ny'>NY</option><option value='la'>LA</option></select></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("city", data.get(0).key());
        assertEquals("ny", data.get(0).value());
    }

    // Tests select element with multiple selected options
    @Test
    public void testFormData_selectMultipleOptions_returnsAllSelectedValues() {
        Document doc = Jsoup.parse("<form><select name='color' multiple><option value='red' selected>Red</option><option value='green'>Green</option><option value='blue' selected>Blue</option></select></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value());
        assertEquals("color", data.get(1).key());
        assertEquals("blue", data.get(1).value());
    }

    // Tests select element with no option children
    @Test
    public void testFormData_selectWithNoOptions_returnsEmptyList() {
        Document doc = Jsoup.parse("<form><select name='empty'></select></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // Tests checkbox with and without explicit value when checked
    @Test
    public void testFormData_checkedCheckbox_returnsValueOrDefaultOn() {
        Document doc = Jsoup.parse("<form><input type='checkbox' name='agree' checked/><input type='checkbox' name='opt' value='yes' checked/><input type='checkbox' name='uncheck'/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
        assertEquals("opt", data.get(1).key());
        assertEquals("yes", data.get(1).value());
    }

    // Tests radio button behavior when checked and unchecked
    @Test
    public void testFormData_radioButton_onlyCheckedIncluded() {
        Document doc = Jsoup.parse("<form><input type='radio' name='gender' value='m'/><input type='radio' name='gender' value='f' checked/><input type='radio' name='defaultRadio' checked/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("f", data.get(0).value());
        assertEquals("defaultRadio", data.get(1).key());
        assertEquals("on", data.get(1).value());
    }

    // Tests button and reset type inputs should be excluded or handled correctly
    @Test
    public void testFormData_buttonAndResetTypes_handling() {
        Document doc = Jsoup.parse("<form><input type='button' name='btn' value='Button'/><input type='reset' name='rst' value='Reset'/><input type='text' name='txt' value='Text'/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        boolean hasText = false;
        for (Connection.KeyVal kv : data) {
            if ("txt".equals(kv.key())) {
                hasText = true;
                assertEquals("Text", kv.value());
            }
        }
        assertTrue(hasText);
    }

    // Tests submit with POST method and action URL
    @Test
    public void testSubmit_postMethodWithActionUrl_preparesPostConnection() {
        Document doc = Jsoup.parse("<form action='/process' method='post'><input name='field' value='data'/></form>", "http://example.com/");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.POST, con.request().method());
        assertEquals("http://example.com/process", con.request().url().toExternalForm());
        assertEquals(1, con.request().data().size());
    }

    // Tests submit defaulting to GET method and base URI when no action attribute is present
    @Test
    public void testSubmit_noActionAttribute_usesBaseUriAndGetMethod() {
        Document doc = Jsoup.parse("<form method='get'><input name='q' value='jsoup'/></form>", "http://example.com/search");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();
        assertNotNull(con);
        assertEquals(Connection.Method.GET, con.request().method());
        assertEquals("http://example.com/search", con.request().url().toExternalForm());
    }

    // Tests submit throwing IllegalArgumentException when action URL cannot be determined
    @Test(expected = IllegalArgumentException.class)
    public void testSubmit_emptyActionAndNoBaseUri_throwsIllegalArgumentException() {
        Document doc = Jsoup.parse("<form><input name='q' value='test'/></form>");
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }
}