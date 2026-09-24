package org.jsoup.nodes;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Tests nodeName returns "#doctype"
    @Test
    public void testNodeName_default_returnsDoctypeNodeName() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }

    // Tests public static constants
    @Test
    public void testConstants_valid_matchExpectedKeys() {
        assertEquals("PUBLIC", DocumentType.PUBLIC_KEY);
        assertEquals("SYSTEM", DocumentType.SYSTEM_KEY);
    }

    // Tests HTML5 doctype output formatting (lowercase <!doctype html>)
    @Test
    public void testOuterHtml_html5DocType_returnsLowercaseDoctype() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("<!doctype html>", documentType.outerHtml());
    }

    // Tests HTML syntax with public and system identifiers
    @Test
    public void testOuterHtml_publicAndSystemIds_returnsFormattedDoctype() {
        DocumentType documentType = new DocumentType(
            "html",
            "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd",
            ""
        );
        assertEquals(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">",
            documentType.outerHtml()
        );
    }

    // Tests doctype with public ID only
    @Test
    public void testOuterHtml_publicIdOnly_returnsDoctypeWithPublicId() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", documentType.outerHtml());
    }

    // Tests doctype with system ID only
    @Test
    public void testOuterHtml_systemIdOnly_returnsDoctypeWithSystemId() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "");
        StringBuilder accum = new StringBuilder();
        documentType.outerHtmlHead(accum, 0, new Document.OutputSettings());
        assertTrue(accum.toString().contains("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"));
        assertTrue(accum.toString().startsWith("<!DOCTYPE"));
    }

    // Tests XML syntax produces uppercase <!DOCTYPE> even without public/system IDs
    @Test
    public void testOuterHtmlHead_xmlSyntax_returnsUppercaseDoctype() throws IOException {
        DocumentType documentType = new DocumentType("html", "", "", "");
        Document.OutputSettings settings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder accum = new StringBuilder();
        documentType.outerHtmlHead(accum, 0, settings);
        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    // Tests doctype with empty name
    @Test
    public void testOuterHtml_emptyName_returnsDoctypeWithoutName() {
        DocumentType documentType = new DocumentType("", "", "", "");
        assertEquals("<!doctype>", documentType.outerHtml());
    }

    // Tests doctype with whitespace-only attributes treated as blank
    @Test
    public void testOuterHtml_whitespaceAttributes_treatedAsBlank() {
        DocumentType documentType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!doctype html>", documentType.outerHtml());
    }

    // Tests doctype with null attributes
    @Test
    public void testOuterHtml_nullAttributes_treatedAsBlank() {
        DocumentType documentType = new DocumentType("html", null, null, "");
        assertEquals("<!doctype html>", documentType.outerHtml());
    }

    // Tests outerHtmlTail does not append anything
    @Test
    public void testOuterHtmlTail_default_doesNothing() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        documentType.outerHtmlTail(accum, 0, new Document.OutputSettings());
        assertEquals(0, accum.length());
    }

    // Tests three-argument constructor
    @Test
    public void testConstructor_threeArgs_initializesFields() {
        DocumentType documentType = new DocumentType(
            "html",
            "-//W3C//DTD HTML 4.01//EN",
            "http://www.w3.org/TR/html4/strict.dtd"
        );
        assertEquals("html", documentType.name());
        assertEquals("-//W3C//DTD HTML 4.01//EN", documentType.publicId());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", documentType.systemId());
    }

    // Tests name(), publicId(), and systemId() getters
    @Test
    public void testGetters_validValues_returnCorrectStrings() {
        DocumentType documentType = new DocumentType("html", "pubId", "sysId", "");
        assertEquals("html", documentType.name());
        assertEquals("pubId", documentType.publicId());
        assertEquals("sysId", documentType.systemId());
    }

    // Tests exact outerHtml formatting for systemId-only doctype
    @Test
    public void testOuterHtml_systemIdOnlyExactFormat_returnsSystemDoctype() {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", "");
        assertEquals(
            "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
            documentType.outerHtml()
        );
    }

    // Tests setPubSysKey with valid and null values
    @Test
    public void testSetPubSysKey_customAndNullValue_updatesOrIgnoresKey() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        documentType.setPubSysKey("SYSTEM");
        assertEquals("SYSTEM", documentType.attr("pubSysKey"));

        documentType.setPubSysKey(null);
        assertEquals("SYSTEM", documentType.attr("pubSysKey"));
    }
}