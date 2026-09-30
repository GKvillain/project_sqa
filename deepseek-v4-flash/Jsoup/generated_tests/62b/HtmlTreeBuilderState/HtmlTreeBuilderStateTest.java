package org.jsoup.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for HtmlTreeBuilderState (Jsoup Defects4J Bug 62b)
 * Focuses on table-related states which are affected by the defect.
 */
public class HtmlTreeBuilderStateTest {

    // Helper method to build a parser
    private HtmlTreeBuilder createTreeBuilder() {
        return new HtmlTreeBuilder();
    }

    // Helper method to process a token through a specific state
    private HtmlTreeBuilder processInState(HtmlTreeBuilderState state, String html) {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(html, "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        return tb;
    }

    // Tests the InTable state with character token that's not whitespace
    @Test
    public void testInTable_characterNotWhitespace_processesThroughFosterInsertion() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td>text</td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        // Move to InTable state
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        // Process non-whitespace character in table context
        org.jsoup.parser.Token.Character charToken = new org.jsoup.parser.Token.Character();
        charToken.data("a");
        boolean result = HtmlTreeBuilderState.InTable.process(charToken, tb);
        assertTrue(result);
    }

    // Tests the InTable state with whitespace character token
    @Test
    public void testInTable_whitespaceCharacter_transitionsToInTableText() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table> </table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        // Move to InTable state
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        org.jsoup.parser.Token.Character spaceToken = new org.jsoup.parser.Token.Character();
        spaceToken.data(" ");
        boolean result = HtmlTreeBuilderState.InTable.process(spaceToken, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTableText, tb.state());
    }

    // Tests the InTable state with start tag for caption
    @Test
    public void testInTable_startTagCaption_transitionsToInCaption() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><caption>test</caption></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);

        org.jsoup.parser.Token.StartTag captionTag = new org.jsoup.parser.Token.StartTag();
        captionTag.name("caption");
        boolean result = HtmlTreeBuilderState.InTable.process(captionTag, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InCaption, tb.state());
    }

    // Tests the InTable state with start tag for colgroup
    @Test
    public void testInTable_startTagColgroup_transitionsToInColumnGroup() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><colgroup><col></colgroup></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);

        org.jsoup.parser.Token.StartTag colgroupTag = new org.jsoup.parser.Token.StartTag();
        colgroupTag.name("colgroup");
        boolean result = HtmlTreeBuilderState.InTable.process(colgroupTag, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InColumnGroup, tb.state());
    }

    // Tests the InTable state with start tag for tbody
    @Test
    public void testInTable_startTagTbody_transitionsToInTableBody() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td></td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);

        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        boolean result = HtmlTreeBuilderState.InTable.process(tbodyTag, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());
    }

    // Tests the InTable state with start tag for td/th/tr (should process tbody first)
    @Test
    public void testInTable_startTagTdOrTh_processesTbodyFirst() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tr><td></td></tr></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);

        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        boolean result = HtmlTreeBuilderState.InTable.process(trTag, tb);
        assertTrue(result);
        // Should transition through InTableBody then to InRow
        assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());
    }

    // Tests the InTable state with end tag for table
    @Test
    public void testInTable_endTagTable_closesTable() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        org.jsoup.parser.Token.EndTag endTable = new org.jsoup.parser.Token.EndTag();
        endTable.name("table");
        boolean result = HtmlTreeBuilderState.InTable.process(endTable, tb);
        assertTrue(result);
        assertNotEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // Tests the InTableBody state with start tag for tr
    @Test
    public void testInTableBody_startTagTr_transitionsToInRow() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td></td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());

        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        boolean result = HtmlTreeBuilderState.InTableBody.process(trTag, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());
    }

    // Tests the InTableBody state with end tag for tbody
    @Test
    public void testInTableBody_endTagTbody_returnsToInTable() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());

        // Add a tr to the stack
        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        tb.process(trTag);

        org.jsoup.parser.Token.EndTag endTbody = new org.jsoup.parser.Token.EndTag();
        endTbody.name("tbody");
        boolean result = HtmlTreeBuilderState.InTableBody.process(endTbody, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // Tests the InRow state with start tag for td/th
    @Test
    public void testInRow_startTagTdOrTh_transitionsToInCell() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td></td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        tb.process(trTag);
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());

        org.jsoup.parser.Token.StartTag tdTag = new org.jsoup.parser.Token.StartTag();
        tdTag.name("td");
        boolean result = HtmlTreeBuilderState.InRow.process(tdTag, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());
    }

    // Tests the InRow state with end tag for tr
    @Test
    public void testInRow_endTagTr_returnsToInTableBody() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        tb.process(trTag);
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());

        org.jsoup.parser.Token.EndTag endTr = new org.jsoup.parser.Token.EndTag();
        endTr.name("tr");
        boolean result = HtmlTreeBuilderState.InRow.process(endTr, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());
    }

    // Tests the InCell state with end tag for td/th
    @Test
    public void testInCell_endTagTdOrTh_returnsToInRow() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td></td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        tb.process(trTag);
        org.jsoup.parser.Token.StartTag tdTag = new org.jsoup.parser.Token.StartTag();
        tdTag.name("td");
        tb.process(tdTag);
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        org.jsoup.parser.Token.EndTag endTd = new org.jsoup.parser.Token.EndTag();
        endTd.name("td");
        boolean result = HtmlTreeBuilderState.InCell.process(endTd, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InRow, tb.state());
    }

    // Tests the InCaption state with end tag for caption
    @Test
    public void testInCaption_endTagCaption_returnsToInTable() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><caption></caption></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag captionTag = new org.jsoup.parser.Token.StartTag();
        captionTag.name("caption");
        tb.process(captionTag);
        assertEquals(HtmlTreeBuilderState.InCaption, tb.state());

        org.jsoup.parser.Token.EndTag endCaption = new org.jsoup.parser.Token.EndTag();
        endCaption.name("caption");
        boolean result = HtmlTreeBuilderState.InCaption.process(endCaption, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // Tests the InColumnGroup state with end tag for colgroup
    @Test
    public void testInColumnGroup_endTagColgroup_returnsToInTable() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><colgroup><col></colgroup></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag colgroupTag = new org.jsoup.parser.Token.StartTag();
        colgroupTag.name("colgroup");
        tb.process(colgroupTag);
        assertEquals(HtmlTreeBuilderState.InColumnGroup, tb.state());

        org.jsoup.parser.Token.EndTag endColgroup = new org.jsoup.parser.Token.EndTag();
        endColgroup.name("colgroup");
        boolean result = HtmlTreeBuilderState.InColumnGroup.process(endColgroup, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
    }

    // Tests the InTableText state with non-whitespace character (pending table characters)
    @Test
    public void testInTableText_nonWhitespaceCharacter_processesThroughInBody() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table>a</table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        org.jsoup.parser.Token.Character charToken = new org.jsoup.parser.Token.Character();
        charToken.data("a");
        boolean result = HtmlTreeBuilderState.InTable.process(charToken, tb);
        assertTrue(result);
        assertEquals(HtmlTreeBuilderState.InTableText, tb.state());
    }

    // Tests the InTable state with EOF token
    @Test
    public void testInTable_eofToken_returnsTrue() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);

        org.jsoup.parser.Token.EOF eofToken = new org.jsoup.parser.Token.EOF();
        boolean result = HtmlTreeBuilderState.InTable.process(eofToken, tb);
        assertTrue(result);
    }

    // Integration test to verify parsing of a simple table
    @Test
    public void testParseFullTable_htmlParsesCorrectly() {
        String html = "<table><thead><tr><th>Header</th></tr></thead><tbody><tr><td>Cell</td></tr></tbody></table>";
        Document doc = org.jsoup.Jsoup.parse(html);
        Elements table = doc.select("table");
        assertEquals(1, table.size());
        Elements thead = table.select("thead");
        assertEquals(1, thead.size());
        Elements tbody = table.select("tbody");
        assertEquals(1, tbody.size());
        Elements th = table.select("th");
        assertEquals("Header", th.text());
        Elements td = table.select("td");
        assertEquals("Cell", td.text());
    }

    // Test InCell with end tag for table (should close cell first)
    @Test
    public void testInCell_endTagTable_closesCellFirst() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<table><tbody><tr><td></td></tr></tbody></table>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag tableTag = new org.jsoup.parser.Token.StartTag();
        tableTag.name("table");
        tb.process(tableTag);
        org.jsoup.parser.Token.StartTag tbodyTag = new org.jsoup.parser.Token.StartTag();
        tbodyTag.name("tbody");
        tb.process(tbodyTag);
        org.jsoup.parser.Token.StartTag trTag = new org.jsoup.parser.Token.StartTag();
        trTag.name("tr");
        tb.process(trTag);
        org.jsoup.parser.Token.StartTag tdTag = new org.jsoup.parser.Token.StartTag();
        tdTag.name("td");
        tb.process(tdTag);
        assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        org.jsoup.parser.Token.EndTag endTable = new org.jsoup.parser.Token.EndTag();
        endTable.name("table");
        boolean result = HtmlTreeBuilderState.InCell.process(endTable, tb);
        assertTrue(result);
        // Should have moved from InRow to InTableBody then to InTable
        assertNotEquals(HtmlTreeBuilderState.InCell, tb.state());
    }

    // Test InBody with start tag for "a" when one is already active
    @Test
    public void testInBody_startTagA_whenActiveFormattingElementExists_closesExisting() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<a href='one'>one</a><a href='two'>two</a>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag aTag = new org.jsoup.parser.Token.StartTag();
        aTag.name("a");
        aTag.attributes.put("href", "one");
        HtmlTreeBuilderState.InBody.process(aTag, tb);

        org.jsoup.parser.Token.Character charToken = new org.jsoup.parser.Token.Character();
        charToken.data("one");
        HtmlTreeBuilderState.InBody.process(charToken, tb);

        // Process another "a" tag - should close the first one
        org.jsoup.parser.Token.StartTag aTag2 = new org.jsoup.parser.Token.StartTag();
        aTag2.name("a");
        aTag2.attributes.put("href", "two");
        boolean result = HtmlTreeBuilderState.InBody.process(aTag2, tb);
        assertTrue(result);
    }

    // Test InBody with end tag for "li"
    @Test
    public void testInBody_endTagLi_closesListItem() {
        HtmlTreeBuilder tb = createTreeBuilder();
        tb.initialiseParse("<ul><li>item</li></ul>", "http://example.com", ParseErrorList.tracking(10), new ParseSettings(false, false));
        org.jsoup.parser.Token.StartTag ulTag = new org.jsoup.parser.Token.StartTag();
        ulTag.name("ul");
        tb.process(ulTag);
        org.jsoup.parser.Token.StartTag liTag = new org.jsoup.parser.Token.StartTag();
        liTag.name("li");
        tb.process(liTag);

        org.jsoup.parser.Token.EndTag endLi = new org.jsoup.parser.Token.EndTag();
        endLi.name("li");
        boolean result = HtmlTreeBuilderState.InBody.process(endLi, tb);
        assertTrue(result);
    }
}