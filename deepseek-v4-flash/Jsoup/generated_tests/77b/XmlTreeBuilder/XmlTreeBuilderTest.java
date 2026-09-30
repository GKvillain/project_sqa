package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.nodes.*;
import java.util.List;
import java.io.StringReader;

public class XmlTreeBuilderTest {

    // Tests parsing simple XML with root and child
    @Test
    public void testParse_simpleXml_returnsDocumentWithRootElement() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child/></root>", "");
        assertNotNull(doc);
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
    }

    // Tests that self-closing unknown tag is marked as self-closing
    @Test
    public void testInsert_startTagSelfClosingUnknown_setsSelfClosing() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<unknown/>", "");
        Element el = doc.child(0);
        assertEquals("unknown", el.tagName());
        assertTrue(el.tag().isSelfClosing());
    }

    // Tests that self-closing known tag (e.g., br) does not set self-closing (branch: known tag)
    @Test
    public void testInsert_startTagSelfClosingKnown_doesNotSetSelfClosing() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<br/>", "");
        Element el = doc.child(0);
        assertEquals("br", el.tagName());
        // In XML mode, br is considered a known tag; self-closing should not be set
        assertFalse(el.tag().isSelfClosing());
    }

    // Tests parsing a normal comment (bogus = false)
    @Test
    public void testInsert_commentNormal_createsCommentNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!-- test -->", "");
        List<Node> nodes = doc.childNodes();
        assertEquals(1, nodes.size());
        Comment comment = (Comment) nodes.get(0);
        assertEquals(" test ", comment.getData());
    }

    // Tests parsing a bogus comment that becomes an XML declaration (bogus = true, starts with '?')
    @Test
    public void testInsert_bogusCommentDeclaration_createsXmlDeclaration() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<?xml version=\"1.0\"?>", "");
        List<Node> nodes = doc.childNodes();
        assertEquals(1, nodes.size());
        assertTrue(nodes.get(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) nodes.get(0);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
    }

    // Tests parsing a DOCTYPE token
    @Test
    public void testInsert_doctype_createsDocumentType() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<!DOCTYPE root>", "");
        List<Node> nodes = doc.childNodes();
        assertEquals(1, nodes.size());
        DocumentType doctype = (DocumentType) nodes.get(0);
        assertEquals("root", doctype.name());
    }

    // Tests parsing CDATA inside an element
    @Test
    public void testInsert_characterCData_createsCDataNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><![CDATA[data]]></root>", "");
        Element root = doc.child(0);
        List<Node> children = root.childNodes();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof CDataNode);
        CDataNode cdata = (CDataNode) children.get(0);
        assertEquals("data", cdata.getWholeText());
    }

    // Tests parsing a text node inside an element
    @Test
    public void testInsert_characterText_createsTextNode() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root>text</root>", "");
        Element root = doc.child(0);
        List<Node> children = root.childNodes();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof TextNode);
        TextNode text = (TextNode) children.get(0);
        assertEquals("text", text.getWholeText());
    }

    // Tests that a matching end tag correctly removes elements from stack
    @Test
    public void testPopStackToClose_found_removesUpToFirstFound() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child></child></root>", "");
        Element root = doc.child(0);
        assertEquals(1, root.childNodes().size());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests that a non‑matching end tag does not remove any element
    @Test
    public void testPopStackToClose_notFound_skips() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("<root><child></other></root>", "");
        Element root = doc.child(0);
        // child element should still be present (not closed)
        assertEquals(1, root.childNodes().size());
        assertEquals("child", root.child(0).tagName());
    }

    // Tests parsing an empty input produces a document with no children
    @Test
    public void testParse_emptyInput_returnsEmptyDocument() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        Document doc = builder.parse("", "");
        assertEquals(0, doc.childNodes().size());
    }

    // Tests that null string input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParse_nullStringInput_throwsNullPointerException() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.parse((String) null, "");
    }

    // Tests that null Reader input throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testParse_nullReaderInput_throwsNullPointerException() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        builder.parse((Reader) null, "");
    }

    // Tests parseFragment returns a list of nodes
    @Test
    public void testParseFragment_simpleXml_returnsListWithElement() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        List<Node> nodes = builder.parseFragment("<root/>", "",
                ParseErrorList.noTracking(), ParseSettings.preserveCase);
        assertEquals(1, nodes.size());
        Element el = (Element) nodes.get(0);
        assertEquals("root", el.tagName());
    }

    // Tests the default settings method
    @Test
    public void testDefaultSettings_returnsPreserveCase() {
        XmlTreeBuilder builder = new XmlTreeBuilder();
        assertSame(ParseSettings.preserveCase, builder.defaultSettings());
    }
}