package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    // Test constructor with null name -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullName_throwsIllegalArgumentException() {
        new DocumentType(null, "pub", "sys", "http://base");
    }

    // Test constructor with empty name -> IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_emptyName_throwsIllegalArgumentException() {
        new DocumentType("", "pub", "sys", "http://base");
    }

    // Test constructor with valid inputs sets attributes correctly
    @Test
    public void testConstructor_validInputs_attributesSetCorrectly() {
        DocumentType doctype = new DocumentType("html", "pub", "sys", "http://base");
        assertEquals("html", doctype.attr("name"));
        assertEquals("pub", doctype.attr("publicId"));
        assertEquals("sys", doctype.attr("systemId"));
    }

    // Test nodeName returns "#doctype"
    @Test
    public void testNodeName_returnsDoctype() {
        DocumentType doctype = new DocumentType("html", "", "", "http://base");
        assertEquals("#doctype", doctype.nodeName());
    }

    // Test outerHtmlHead with only name (publicId and systemId blank)
    @Test
    public void testOuterHtmlHead_nameOnly_returnsDoctypeWithName() {
        DocumentType doctype = new DocumentType("html", "", "", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html>", sb.toString());
    }

    // Test outerHtmlHead with name and publicId only (systemId blank)
    @Test
    public void testOuterHtmlHead_nameAndPublicIdOnly_returnsDoctypeWithPublicId() {
        DocumentType doctype = new DocumentType("html", "public-id", "", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\">", sb.toString());
    }

    // Test outerHtmlHead with name and systemId only (publicId blank)
    // Expects SYSTEM keyword – detects Defects4J bug 40b (missing SYSTEM)
    @Test
    public void testOuterHtmlHead_nameAndSystemIdOnly_returnsDoctypeWithSystem() {
        DocumentType doctype = new DocumentType("html", "", "system-id", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html SYSTEM \"system-id\">", sb.toString());
    }

    // Test outerHtmlHead with name, publicId, and systemId
    @Test
    public void testOuterHtmlHead_namePublicIdSystemId_returnsFullDoctype() {
        DocumentType doctype = new DocumentType("html", "public-id", "system-id", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\" \"system-id\">", sb.toString());
    }

    // Test outerHtmlHead when name is set to blank after construction (branch name blank)
    @Test
    public void testOuterHtmlHead_nameBlankAfterSet_returnsDoctypeOnly() {
        DocumentType doctype = new DocumentType("html", "", "", "http://base");
        doctype.attr("name", "");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE>", sb.toString());
    }

    // Test outerHtmlHead when publicId is blank (space) – treated as blank
    @Test
    public void testOuterHtmlHead_publicIdBlank_skipsPublic() {
        DocumentType doctype = new DocumentType("html", " ", "system-id", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html SYSTEM \"system-id\">", sb.toString());
    }

    // Test outerHtmlHead when systemId is blank (space) – treated as blank
    @Test
    public void testOuterHtmlHead_systemIdBlank_skipsSystem() {
        DocumentType doctype = new DocumentType("html", "public-id", " ", "http://base");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\">", sb.toString());
    }

    // Test outerHtmlHead when publicId is set to null after construction – treated as blank
    @Test
    public void testOuterHtmlHead_publicIdSetToNull_skipsPublic() {
        DocumentType doctype = new DocumentType("html", "dummy", "system-id", "http://base");
        doctype.attr("publicId", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html SYSTEM \"system-id\">", sb.toString());
    }

    // Test outerHtmlHead when systemId is set to null after construction – treated as blank
    @Test
    public void testOuterHtmlHead_systemIdSetToNull_skipsSystem() {
        DocumentType doctype = new DocumentType("html", "public-id", "dummy", "http://base");
        doctype.attr("systemId", null);
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlHead(sb, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\">", sb.toString());
    }
}