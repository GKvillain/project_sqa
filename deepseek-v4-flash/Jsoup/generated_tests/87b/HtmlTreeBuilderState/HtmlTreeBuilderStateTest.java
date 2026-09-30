package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.DocumentType;
import org.jsoup.select.Elements;

public class HtmlTreeBuilderStateTest {

    // Tests Initial state: doctype triggers creation of DocumentType and transition to BeforeHtml
    @Test
    public void testInitial_doctype_createsDocumentTypeAndHtml() {
        String html = "<!DOCTYPE html>";
        Document doc = Jsoup.parse(html);
        // Should have a DocumentType child and an html element
        assertEquals(2, doc.children().size());
        assertTrue(doc.childNode(0) instanceof DocumentType);
        assertEquals("html", doc.child(1).tagName());
        // Check doctype details
        DocumentType doctype = (DocumentType) doc.childNode(0);
        assertEquals("html", doctype.nodeName());
    }

    // Tests BeforeHtml: a start tag "html" inserts html element and transitions to BeforeHead
    @Test
    public void testBeforeHtml_startHtml_createsHtmlAndHead() {
        String html = "<html></html>";
        Document doc = Jsoup.parse(html);
        // parser should add head and body implicitly
        Element htmlEl = doc.child(0);
        assertEquals("html", htmlEl.tagName());
        assertEquals(2, htmlEl.children().size()); // head and body
        assertEquals("head", htmlEl.child(0).tagName());
        assertEquals("body", htmlEl.child(1).tagName());
    }

    // Tests BeforeHead: start tag "head" inserts head and transitions to InHead
    @Test
    public void testBeforeHead_startHead_setsHeadElement() {
        String html = "<html><head><title>T</title></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
        assertEquals(1, head.children().size());
        assertEquals("title", head.child(0).tagName());
    }

    // Tests InHead: various start tags like base, link, meta, title, style, script, noscript
    @Test
    public void testInHead_metaAndLink_insertElements() {
        String html = "<html><head><meta charset='utf-8'><link rel='stylesheet' href='style.css'><title>X</title></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Element head = doc.head();
        Elements metas = head.getElementsByTag("meta");
        assertEquals(1, metas.size());
        assertEquals("utf-8", metas.first().attr("charset"));
        Elements links = head.getElementsByTag("link");
        assertEquals(1, links.size());
        assertEquals("style.css", links.first().attr("href"));
        Elements titles = head.getElementsByTag("title");
        assertEquals(1, titles.size());
        assertEquals("X", titles.first().text());
    }

    // Tests AfterHead: start tag "body" inserts body and transitions to InBody
    @Test
    public void testAfterHead_startBody_createsBody() {
        String html = "<html><head></head><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        assertNotNull(body);
        assertEquals(1, body.children().size());
        assertEquals("p", body.child(0).tagName());
        assertEquals("Text", body.child(0).text());
    }

    // Tests InBody: basic block elements like p, div, h1, and inline elements like span, a
    @Test
    public void testInBody_blockAndInlineElements() {
        String html = "<body><div><p>Para <a href='#'>link</a></p><span>sp</span></div></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        assertEquals(1, body.children().size());
        Element div = body.child(0);
        assertEquals("div", div.tagName());
        assertEquals(2, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
        Element p = div.child(0);
        assertEquals("Para ", p.text().substring(0,5));
        assertEquals("link", p.child(0).text());
    }

    // Tests InBody: table insertion causes transition to InTable and foster parenting
    @Test
    public void testInBody_table_createsTableStructure() {
        String html = "<body><table><tr><td>Cell</td></tr></table></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Element table = body.children().first();
        assertEquals("table", table.tagName());
        assertEquals(1, table.children().size());
        Element tbody = table.child(0);
        assertEquals("tbody", tbody.tagName()); // implicit tbody
        assertEquals(1, tbody.children().size());
        Element tr = tbody.child(0);
        assertEquals("tr", tr.tagName());
        assertEquals(1, tr.children().size());
        Element td = tr.child(0);
        assertEquals("td", td.tagName());
        assertEquals("Cell", td.text());
    }

    // Tests InTable: caption, colgroup, col, and end tag "table"
    @Test
    public void testInTable_caption_colgroup() {
        String html = "<body><table><caption>Cap</caption><colgroup><col span='2'></colgroup><tr><td>X</td></tr></table></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element table = doc.body().child(0);
        assertEquals("table", table.tagName());
        Elements captions = table.getElementsByTag("caption");
        assertEquals(1, captions.size());
        assertEquals("Cap", captions.first().text());
        Elements colgroups = table.getElementsByTag("colgroup");
        assertEquals(1, colgroups.size());
        Elements cols = colgroups.first().getElementsByTag("col");
        assertEquals(1, cols.size());
        assertEquals("2", cols.first().attr("span"));
    }

    // Tests InBody: form element handling
    @Test
    public void testInBody_form_createsForm() {
        String html = "<body><form action='/submit'><input type='text' name='q'></form></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Element form = body.children().first();
        assertEquals("form", form.tagName());
        assertEquals("/submit", form.attr("action"));
        assertEquals(1, form.children().size());
        assertEquals("input", form.child(0).tagName());
        assertEquals("text", form.child(0).attr("type"));
    }

    // Tests InBody: adoption agency for nested formatting elements (e.g., <b><i>text</i></b>)
    @Test
    public void testInBody_adoptionAgency_nestedFormatting() {
        String html = "<body><b><i>italic</i> bold</b></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Element b = body.child(0);
        assertEquals("b", b.tagName());
        assertEquals(2, b.children().size()); // i and text node
        assertEquals("i", b.child(0).tagName());
        assertEquals("italic", b.child(0).text());
        assertEquals(" bold", b.text().substring(6));
    }

    // Tests InBody: handling of <li> with list
    @Test
    public void testInBody_li_createsListItems() {
        String html = "<body><ul><li>Item1<li>Item2</ul></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Element ul = body.child(0);
        assertEquals("ul", ul.tagName());
        assertEquals(2, ul.children().size());
        assertEquals("li", ul.child(0).tagName());
        assertEquals("Item1", ul.child(0).text());
        assertEquals("li", ul.child(1).tagName());
        assertEquals("Item2", ul.child(1).text());
    }

    // Tests InBody: <img> and <br> as void elements
    @Test
    public void testInBody_voidElements_br_img() {
        String html = "<body><br><img src='pic.png' alt='x'></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Elements brs = body.getElementsByTag("br");
        assertEquals(1, brs.size());
        Elements imgs = body.getElementsByTag("img");
        assertEquals(1, imgs.size());
        assertEquals("pic.png", imgs.first().attr("src"));
    }

    // Tests InBody: <image> should be converted to <img>
    @Test
    public void testInBody_imageTag_mappedToImg() {
        String html = "<body><image src='a.jpg'></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        // <image> should become <img>
        assertEquals(0, body.getElementsByTag("image").size());
        Elements imgs = body.getElementsByTag("img");
        assertEquals(1, imgs.size());
        assertEquals("a.jpg", imgs.first().attr("src"));
    }

    // Tests InBody: <plaintext> transitions to PLAINTEXT tokenizer state
    @Test
    public void testInBody_plaintext_parsesAsText() {
        String html = "<body><plaintext>This is plain text <b>not bold</b></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        // The remainder after <plaintext> should be treated as raw text
        Element body = doc.body();
        // The <plaintext> element is inserted, then all subsequent characters become text
        Element plaintext = body.children().first();
        assertEquals("plaintext", plaintext.tagName());
        // The text node after plaintext should contain the rest of the input
        String wholeText = body.text();
        assertTrue(wholeText.startsWith("This is plain text <b>not bold</b>"));
    }

    // Tests InTable: foster parenting when character data appears inside table
    @Test
    public void testInTable_fosterParenting_characterData() {
        String html = "<body><table>Some text<tr><td>Cell</td></tr></table></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        // The text "Some text" should be placed before the table (foster parenting)
        Element table = body.getElementsByTag("table").first();
        // The first child of body should be the text node, then the table
        assertEquals(2, body.children().size());
        // Cannot easily check text node via children() because it returns Elements only; check body.text()
        assertTrue(body.text().startsWith("Some text"));
    }

    // Tests InSelect: <option> and <optgroup>
    @Test
    public void testInSelect_optionGroup() {
        String html = "<body><select><optgroup label='Group'><option>Opt1</option></optgroup></select></body>";
        Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();
        Element select = body.children().first();
        assertEquals("select", select.tagName());
        Element optgroup = select.children().first();
        assertEquals("optgroup", optgroup.tagName());
        assertEquals("Group", optgroup.attr("label"));
        Element option = optgroup.children().first();
        assertEquals("option", option.tagName());
        assertEquals("Opt1", option.text());
    }

    // Tests AfterBody: whitespace is processed, start tag html, end tag html transitions to AfterAfterBody
    @Test
    public void testAfterBody_endHtml_transitions() {
        String html = "<html><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        // After parsing, the document should be complete
        assertEquals("html", doc.children().get(0).tagName());
        // No extra nodes
        assertEquals(1, doc.children().size());
    }
}