package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TokeniserStateTest {

    private List<Token> tokenize(String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = tokeniser.read();
            tokens.add(token);
        } while (!token.isEOF());
        return tokens;
    }

    // Tests normal Data state emitting characters and EOF
    @Test
    public void testData_plainText_emitsDataAndEof() {
        CharacterReader reader = new CharacterReader("Hello World");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello World", token.asCharacter().getData());
    }

    // Tests Data state with character references
    @Test
    public void testData_characterReference_decodesEntity() {
        CharacterReader reader = new CharacterReader("&amp;&lt;&gt;&quot;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("&<>\"", token.asCharacter().getData());
    }

    // Tests TagOpen and TagName state with start tag
    @Test
    public void testTagName_startTag_emitsStartTagToken() {
        CharacterReader reader = new CharacterReader("<div>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        assertEquals("div", token.asStartTag().name());
    }

    // Tests EndTagOpen and TagName state with end tag
    @Test
    public void testTagName_endTag_emitsEndTagToken() {
        CharacterReader reader = new CharacterReader("</div>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isEndTag());
        assertEquals("div", token.asEndTag().name());
    }

    // Tests SelfClosingStartTag state
    @Test
    public void testSelfClosingStartTag_validSelfClosing_setsFlag() {
        CharacterReader reader = new CharacterReader("<img src=\"test.png\" />");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        assertTrue(token.asStartTag().selfClosing);
        assertEquals("img", token.asStartTag().name());
        assertEquals("test.png", token.asStartTag().attributes.get("src"));
    }

    // Tests Attribute states: double quoted, single quoted, unquoted
    @Test
    public void testAttributes_variousQuotings_parsesAttributesCorrectly() {
        CharacterReader reader = new CharacterReader("<a href=\"http://example.com\" title='Single' data=unquoted>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        assertEquals("http://example.com", token.asStartTag().attributes.get("href"));
        assertEquals("Single", token.asStartTag().attributes.get("title"));
        assertEquals("unquoted", token.asStartTag().attributes.get("data"));
    }

    // Tests RCDATA handling and unclosed tag defect detection
    @Test
    public void testRcdata_titleTag_parsesTitleAndSubsequentContent() {
        Document doc = Jsoup.parse("<title>One</title><p>Two</p>");
        assertEquals("One", doc.title());
        assertEquals("Two", doc.select("p").first().text());
    }

    // Tests unclosed RCDATA handling
    @Test
    public void testRcdata_unclosedTitle_parsesRemainingAsTitle() {
        Document doc = Jsoup.parse("<title>One <b>Two</b> Three");
        assertEquals("One <b>Two</b> Three", doc.title());
    }

    // Tests RCDATA end tag mismatch fallback
    @Test
    public void testRcdata_mismatchedEndTag_emitsDataAndStaysInRcdata() {
        Document doc = Jsoup.parse("<title>Hello </p> World</title>");
        assertEquals("Hello </p> World", doc.title());
    }

    // Tests Rawtext state with style tag
    @Test
    public void testRawtext_styleTag_parsesRawContent() {
        Document doc = Jsoup.parse("<style>body > div { color: red; }</style><p>Text</p>");
        assertEquals("body > div { color: red; }", doc.select("style").first().data());
        assertEquals("Text", doc.select("p").first().text());
    }

    // Tests ScriptData state and escaped transitions
    @Test
    public void testScriptData_escapedScript_parsesScriptCorrectly() {
        Document doc = Jsoup.parse("<script><!-- var a = \"<script>alert(1);</script>\"; --></script><p>Text</p>");
        assertEquals("<!-- var a = \"<script>alert(1);</script>\"; -->", doc.select("script").first().data());
        assertEquals("Text", doc.select("p").first().text());
    }

    // Tests ScriptData double escaped transition
    @Test
    public void testScriptDataDoubleEscaped_nestedScriptTags_handlesDoubleEscape() {
        Document doc = Jsoup.parse("<script><!-- <script> var x = 1; </script> --></script>");
        assertNotNull(doc.select("script").first());
    }

    // Tests PLAINTEXT state
    @Test
    public void testPlaintext_plainContent_consumesAllRemaining() {
        Document doc = Jsoup.parse("<plaintext><div>Hello <b>World</b>");
        assertEquals("<div>Hello <b>World</b>", doc.text());
    }

    // Tests Comment states: normal comments, dashes, bang
    @Test
    public void testComment_validComments_emitsCommentTokens() {
        CharacterReader reader = new CharacterReader("<!-- this is a comment -->");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals(" this is a comment ", token.asComment().getData());
    }

    // Tests BogusComment state
    @Test
    public void testBogusComment_invalidTagOpen_parsesAsComment() {
        CharacterReader reader = new CharacterReader("<?bogus comment>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals("?bogus comment", token.asComment().getData());
    }

    // Tests MarkupDeclarationOpen and CDATA section
    @Test
    public void testCdataSection_cdataData_emitsCdataContent() {
        CharacterReader reader = new CharacterReader("[CDATA[Some <raw> cdata]]>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.CdataSection);
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("Some <raw> cdata", token.asCharacter().getData());
    }

    // Tests Doctype states: standard doctype
    @Test
    public void testDoctype_standardHtml5_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
        assertFalse(token.asDoctype().isForceQuirks());
    }

    // Tests Doctype with Public and System identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_parsesCorrectly() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" 'http://www.w3.org/TR/html4/strict.dtd'>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", token.asDoctype().getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", token.asDoctype().getSystemIdentifier());
    }

    // Tests null characters handling in Data and Attribute states
    @Test
    public void testNullCharacter_inData_replacesOrEmits() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("a\u0000b");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertTrue(errorList.size() > 0);
    }

    // Tests EOF handling across incomplete states
    @Test
    public void testEofHandling_incompleteTag_emitsEofError() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<div class=");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);
        while (!tokeniser.read().isEOF()) {
            // consume all
        }
        assertTrue(errorList.size() > 0);
    }

    // Tests CommentEndDash and CommentEnd states
    @Test
    public void testComment_variousDashEndings() {
        List<Token> tokens1 = tokenize("<!-- comment --!>");
        assertTrue(tokens1.get(0).isComment());
        assertEquals(" comment ", tokens1.get(0).asComment().getData());

        List<Token> tokens2 = tokenize("<!-- comment --->");
        assertTrue(tokens2.get(0).isComment());
        assertEquals(" comment -", tokens2.get(0).asComment().getData());

        List<Token> tokens3 = tokenize("<!--->");
        assertTrue(tokens3.get(0).isComment());

        List<Token> tokens4 = tokenize("<!-->");
        assertTrue(tokens4.get(0).isComment());
    }

    // Tests Doctype System identifier only and BogusDoctype
    @Test
    public void testDoctype_systemOnlyAndBogus() {
        List<Token> tokens1 = tokenize("<!DOCTYPE html SYSTEM \"about:legacy-compat\">");
        assertTrue(tokens1.get(0).isDoctype());
        assertEquals("html", tokens1.get(0).asDoctype().getName());
        assertEquals("about:legacy-compat", tokens1.get(0).asDoctype().getSystemIdentifier());
        assertNull(tokens1.get(0).asDoctype().getPublicIdentifier());

        List<Token> tokens2 = tokenize("<!DOCTYPE>");
        assertTrue(tokens2.get(0).isDoctype());
        assertTrue(tokens2.get(0).asDoctype().isForceQuirks());

        List<Token> tokens3 = tokenize("<!DOCTYPE html BOGUS>");
        assertTrue(tokens3.get(0).isDoctype());
        assertTrue(tokens3.get(0).asDoctype().isForceQuirks());
    }

    // Tests Attribute value with character reference
    @Test
    public void testAttributeValue_characterReferenceDecoded() {
        List<Token> tokens = tokenize("<a href=\"foo&amp;bar\" title='foo&lt;bar' alt=foo&quot;bar>");
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("foo&bar", tag.attributes.get("href"));
        assertEquals("foo<bar", tag.attributes.get("title"));
        assertEquals("foo\"bar", tag.attributes.get("alt"));
    }

    // Tests EndTag with attributes and whitespace
    @Test
    public void testEndTag_withAttributesAndWhitespace() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("</div class='test' id=foo >");
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token token = tokeniser.read();
        assertTrue(token.isEndTag());
        assertEquals("div", token.asEndTag().name());
        assertTrue(errors.size() > 0);
    }

    // Tests Tag with empty or boolean attributes
    @Test
    public void testAttributes_emptyAndBoolean() {
        List<Token> tokens = tokenize("<input disabled required='' checked=\"\">");
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertTrue(tag.attributes.hasKey("disabled"));
        assertEquals("", tag.attributes.get("required"));
        assertEquals("", tag.attributes.get("checked"));
    }

    // Tests ScriptData escaped transitions with various dashes and less-than signs
    @Test
    public void testScriptData_escapedTransitions() {
        Document doc = Jsoup.parse("<script><!-- - < -- </script> --></script>");
        assertNotNull(doc.select("script").first());

        Document doc2 = Jsoup.parse("<script><!--<script>--</script>--></script>");
        assertNotNull(doc2.select("script").first());
    }

    // Tests Rawtext with character references not expanding
    @Test
    public void testRawtext_characterReferencesNotExpanded() {
        Document doc = Jsoup.parse("<noembed>&amp;&lt;</noembed><style>&amp;&lt;</style>");
        assertEquals("&amp;&lt;", doc.select("noembed").first().data());
        assertEquals("&amp;&lt;", doc.select("style").first().data());
    }

    // Tests TagOpen state with solidus (self-closing without name)
    @Test
    public void testTagOpen_solidusOnly_bogusComment() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("</>");
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token token = tokeniser.read();
        assertTrue(token.isComment() || token.isEOF());
        assertTrue(errors.size() > 0);
    }
}