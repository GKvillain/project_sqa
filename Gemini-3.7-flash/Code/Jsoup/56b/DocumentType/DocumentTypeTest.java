package org.jsoup.nodes;

import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests nodeName return value
    @Test
    public void testNodeName_default_returnsDoctypeNodeName() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }

    // Tests constructor setting initial attributes
    @Test
    public void testConstructor_validArguments_setsAttributesCorrectly() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "http://example.com");
        assertEquals("html", documentType.attr("name"));
        assertEquals("-//W3C//DTD HTML 4.01//EN", documentType.attr("publicId"));
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", documentType.attr("systemId"));
        assertEquals("http://example.com", documentType.baseUri());
    }

    // Tests 3-argument constructor
    @Test
    public void testConstructor_threeArgs_setsAttributesCorrectly() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd");
        assertEquals("html", documentType.name());
        assertEquals("-//W3C//DTD HTML 4.01//EN", documentType.publicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", documentType.systemId());
    }

    // Tests getter methods (name, publicId, systemId)
    @Test
    public void testGetters_validValues_returnCorrectStrings() {
        DocumentType documentType = new DocumentType("html", "pubId", "sysId");
        assertEquals("html", documentType.name());
        assertEquals("pubId", documentType.publicId());
        assertEquals("sysId", documentType.systemId());
    }

    // Tests HTML5 doctype rendering in lowercase for HTML syntax
    @Test
    public void testOuterHtmlHead_html5Doctype_rendersLowercaseDoctype() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!doctype html>", accum.toString());
    }

    // Tests HTML5 doctype rendering in uppercase for XML syntax
    @Test
    public void testOuterHtmlHead_xmlSyntaxWithoutIds_rendersUppercaseDoctype() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.xml);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    // Tests XML syntax with public and system IDs
    @Test
    public void testOuterHtmlHead_xmlSyntaxWithIds_rendersUppercaseDoctypeWithIds() throws IOException {
        DocumentType documentType = new DocumentType("html", "pub", "sys");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.xml);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", accum.toString());
    }

    // Tests doctype with both publicId and systemId
    @Test
    public void testOuterHtmlHead_publicAndSystemIds_rendersFullDoctype() throws IOException {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", accum.toString());
    }

    // Tests doctype with publicId only
    @Test
    public void testOuterHtmlHead_publicIdOnly_rendersDoctypeWithPublicId() throws IOException {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", accum.toString());
    }

    // Tests doctype with systemId only
    @Test
    public void testOuterHtmlHead_systemIdOnly_rendersDoctypeWithSystemId() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "about:legacy-compat", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html \"about:legacy-compat\">", accum.toString());
    }

    // Tests doctype with setPubSysKey explicitly set to SYSTEM
    @Test
    public void testSetPubSysKey_system_rendersSystemDoctype() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "http://example.com/dtd");
        documentType.setPubSysKey("SYSTEM");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html SYSTEM \"http://example.com/dtd\">", accum.toString());
    }

    // Tests doctype with setPubSysKey explicitly set to custom value
    @Test
    public void testSetPubSysKey_customValue_rendersCustomKey() throws IOException {
        DocumentType documentType = new DocumentType("html", "pub", "sys");
        documentType.setPubSysKey("CUSTOM");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE html CUSTOM \"pub\" \"sys\">", accum.toString());
    }

    // Tests doctype without name
    @Test
    public void testOuterHtmlHead_blankName_rendersDoctypeWithoutName() throws IOException {
        DocumentType documentType = new DocumentType("", "", "", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.html);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!doctype>", accum.toString());
    }

    // Tests XML syntax with blank name
    @Test
    public void testOuterHtmlHead_xmlSyntaxBlankName_rendersUppercaseDoctype() throws IOException {
        DocumentType documentType = new DocumentType("", "", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings().syntax(Syntax.xml);

        documentType.outerHtmlHead(accum, 0, settings);

        assertEquals("<!DOCTYPE>", accum.toString());
    }

    // Tests outerHtmlTail has no effect
    @Test
    public void testOuterHtmlTail_anyState_appendsNothing() {
        DocumentType documentType = new DocumentType("html", "pub", "sys", "");
        StringBuilder accum = new StringBuilder();
        OutputSettings settings = new OutputSettings();

        documentType.outerHtmlTail(accum, 0, settings);

        assertEquals("", accum.toString());
    }

    // Tests outerHtml helper method on Node
    @Test
    public void testOuterHtml_standardHtml5_returnsCorrectString() {
        DocumentType documentType = new DocumentType("html", null, null, "");
        assertEquals("<!doctype html>", documentType.outerHtml());
    }
}