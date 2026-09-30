package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class DocumentTypeTest {

    private String render(DocumentType doctype, Document.OutputSettings.Syntax syntax) throws Exception {
        Document.OutputSettings out = new Document.OutputSettings();
        out.syntax(syntax);
        StringBuilder accum = new StringBuilder();
        doctype.outerHtmlHead(accum, 0, out);
        return accum.toString();
    }

    // === existing tests ===

    @Test
    public void testNodeName_returnsHashDoctype() {
        DocumentType doctype = new DocumentType("html", "", "", "");
        assertEquals("#doctype", doctype.nodeName());
    }

    @Test
    public void testOuterHtmlHead_html5WithoutPublicOrSystem_writesLowercaseDoctype() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "", "");
        assertEquals("<!doctype html>", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_htmlWithPublicId_writesUppercaseDoctype() throws Exception {
        DocumentType doctype = new DocumentType("html", "public-id", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\">", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_htmlWithSystemIdOnly_writesSystemKeyword() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "system-id", "");
        assertEquals("<!DOCTYPE html SYSTEM \"system-id\">", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_htmlWithPublicAndSystemId_writesBothIdentifiers() throws Exception {
        DocumentType doctype = new DocumentType("html", "public-id", "system-id", "");
        assertEquals("<!DOCTYPE html PUBLIC \"public-id\" \"system-id\">", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_xmlWithoutPublicOrSystem_writesUppercaseDoctype() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", render(doctype, Document.OutputSettings.Syntax.xml));
    }

    @Test
    public void testOuterHtmlHead_xmlWithSystemIdOnly_writesSystemKeyword() throws Exception {
        DocumentType doctype = new DocumentType("svg", "", "system-id", "");
        assertEquals("<!DOCTYPE svg SYSTEM \"system-id\">", render(doctype, Document.OutputSettings.Syntax.xml));
    }

    @Test
    public void testOuterHtmlHead_xmlWithPublicAndSystemId_writesBothIdentifiers() throws Exception {
        DocumentType doctype = new DocumentType("svg", "public-id", "system-id", "");
        assertEquals("<!DOCTYPE svg PUBLIC \"public-id\" \"system-id\">", render(doctype, Document.OutputSettings.Syntax.xml));
    }

    @Test
    public void testOuterHtmlHead_blankName_omitsName() throws Exception {
        DocumentType doctype = new DocumentType("", "", "", "");
        assertEquals("<!doctype>", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_blankPublicAndSystemIds_treatedAsHtml5() throws Exception {
        DocumentType doctype = new DocumentType("html", "  ", " ", "");
        assertEquals("<!doctype html>", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_blankNameWithPublicId_writesPublicIdOnly() throws Exception {
        DocumentType doctype = new DocumentType("", "public-id", "", "");
        assertEquals("<!DOCTYPE PUBLIC \"public-id\">", render(doctype, Document.OutputSettings.Syntax.html));
    }

    // === new tests for uncovered code ===

    @Test
    public void testGetName_returnsCorrectName() {
        DocumentType doctype = new DocumentType("html", "pub", "sys", "internal");
        assertEquals("html", doctype.getName());
    }

    @Test
    public void testGetPublicId_returnsCorrectPublicId() {
        DocumentType doctype = new DocumentType("html", "public-id", "", "");
        assertEquals("public-id", doctype.getPublicId());
    }

    @Test
    public void testGetSystemId_returnsCorrectSystemId() {
        DocumentType doctype = new DocumentType("html", "", "system-id", "");
        assertEquals("system-id", doctype.getSystemId());
    }

    @Test
    public void testGetInternalSubset_returnsCorrectInternalSubset() {
        DocumentType doctype = new DocumentType("html", "", "", "internal-subset");
        assertEquals("internal-subset", doctype.getInternalSubset());
    }

    @Test
    public void testClone_returnsEqualObject() throws CloneNotSupportedException {
        DocumentType original = new DocumentType("html", "public-id", "system-id", "internal");
        DocumentType cloned = original.clone();
        assertEquals(original.getName(), cloned.getName());
        assertEquals(original.getPublicId(), cloned.getPublicId());
        assertEquals(original.getSystemId(), cloned.getSystemId());
        assertEquals(original.getInternalSubset(), cloned.getInternalSubset());
        assertEquals(original.nodeName(), cloned.nodeName());
    }

    @Test
    public void testOuterHtmlTail_doesNothing() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        doctype.outerHtmlTail(accum, 0, out);
        assertEquals("", accum.toString());
    }

    @Test
    public void testOuterHtmlHead_xmlWithPublicIdOnly_writesPublicId() throws Exception {
        DocumentType doctype = new DocumentType("svg", "public-id", "", "");
        assertEquals("<!DOCTYPE svg PUBLIC \"public-id\">", render(doctype, Document.OutputSettings.Syntax.xml));
    }

    @Test
    public void testOuterHtmlHead_htmlWithInternalSubset_ignoresInternalSubset() throws Exception {
        DocumentType doctype = new DocumentType("html", "", "", "internal");
        assertEquals("<!doctype html>", render(doctype, Document.OutputSettings.Syntax.html));
    }

    @Test
    public void testOuterHtmlHead_xmlBlankName_omitsName() throws Exception {
        DocumentType doctype = new DocumentType("", "", "", "");
        assertEquals("<!DOCTYPE>", render(doctype, Document.OutputSettings.Syntax.xml));
    }

    @Test
    public void testConstructor_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new DocumentType(null, "", "", ""));
    }

    @Test
    public void testOuterHtmlHead_htmlWithSvgName_writesUppercaseDoctype() throws Exception {
        DocumentType doctype = new DocumentType("svg", "", "", "");
        assertEquals("<!DOCTYPE svg>", render(doctype, Document.OutputSettings.Syntax.html));
    }
}