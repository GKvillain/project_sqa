package org.jsoup.parser;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TokeniserStateTest {

    private List<Token> tokenize(String input) {
        return tokenize(input, ParseErrorList.tracking(20));
    }

    private List<Token> tokenize(String input, ParseErrorList errors) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        List<Token> tokens = new ArrayList<Token>();
        Token token;
        do {
            token = tokeniser.read();
            tokens.add(token);
        } while (token.type() != Token.TokenType.EOF);
        return tokens;
    }

    // Tests normal tag and data parsing
    @Test
    public void testRead_normalHtml_emitsExpectedTokens() {
        List<Token> tokens = tokenize("<p>Hello &amp; World</p>");
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("p", tokens.get(0).asStartTag().name());
        assertTrue(tokens.get(1).isCharacter());
        assertEquals("Hello & World", tokens.get(1).asCharacter().getData());
        assertTrue(tokens.get(2).isEndTag());
        assertEquals("p", tokens.get(2).asEndTag().name());
    }

    // Tests self-closing start tag with and without whitespace
    @Test
    public void testSelfClosingStartTag_validAndAttributes_setsSelfClosingFlag() {
        List<Token> tokens = tokenize("<br/><img src=\"pic.jpg\" alt='img' class=pic / >");
        Token.StartTag br = tokens.get(0).asStartTag();
        assertTrue(br.isSelfClosing());
        assertEquals("br", br.name());

        Token.StartTag img = tokens.get(1).asStartTag();
        assertTrue(img.isSelfClosing());
        assertEquals("pic.jpg", img.attributes.get("src"));
        assertEquals("img", img.attributes.get("alt"));
        assertEquals("pic", img.attributes.get("class"));
    }

    // Tests self-closing tag followed immediately by attribute (bug 55 regression area)
    @Test
    public void testSelfClosingStartTag_followedByAttribute_parsesAttributeCorrectly() {
        List<Token> tokens = tokenize("<img /src=\"test.png\">");
        Token.StartTag img = tokens.get(0).asStartTag();
        assertEquals("img", img.name());
        assertEquals("test.png", img.attributes.get("src"));
    }

    // Tests attribute values: double-quoted, single-quoted, and unquoted with character references
    @Test
    public void testAttributeValue_variousQuotesAndEntities_resolvesValues() {
        List<Token> tokens = tokenize("<a href=\"?a=1&amp;b=2\" target='_blank' data-val=unquoted&lt;1&gt;>");
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("?a=1&b=2", tag.attributes.get("href"));
        assertEquals("_blank", tag.attributes.get("target"));
        assertEquals("unquoted<1>", tag.attributes.get("data-val"));
    }

    // Tests end tag variations and malformed end tags
    @Test
    public void testEndTagOpen_variousInputs_handlesCorrectly() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        List<Token> tokens = tokenize("</></div></123></>");
        assertFalse(errors.isEmpty());
        assertTrue(tokens.get(0).isEndTag());
        assertEquals("div", tokens.get(0).asEndTag().name());
    }

    // Tests bogus comment from invalid tag open like <? or <!
    @Test
    public void testTagOpen_invalidCharacters_emitsBogusComment() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        List<Token> tokens = tokenize("<?xml version=\"1.0\"?><!something>");
        assertTrue(tokens.get(0).isComment());
        assertEquals("?xml version=\"1.0\"?", tokens.get(0).asComment().getData());
        assertTrue(tokens.get(0).asComment().bogus);
        assertTrue(tokens.get(1).isComment());
        assertFalse(errors.isEmpty());
    }

    // Tests comment parsing with normal, dashes, bang, and quirks
    @Test
    public void testComment_variousFormats_capturesCommentContent() {
        List<Token> tokens = tokenize("<!-- normal comment --><!---dash-><!-- --!> <!-- -->");
        assertTrue(tokens.get(0).isComment());
        assertEquals(" normal comment ", tokens.get(0).asComment().getData());
        assertTrue(tokens.get(1).isComment());
        assertTrue(tokens.get(2).isComment());
        assertTrue(tokens.get(4).isComment());
    }

    // Tests Doctype parsing: standard, PUBLIC, SYSTEM, and malformed
    @Test
    public void testDoctype_variousKeywords_parsesDoctypeCorrectly() {
        List<Token> tokens = tokenize("<!DOCTYPE html><!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        assertTrue(tokens.get(0).isDoctype());
        Token.Doctype d1 = tokens.get(0).asDoctype();
        assertEquals("html", d1.getName());
        assertEquals("", d1.getPublicIdentifier());
        assertEquals("", d1.getSystemIdentifier());
        assertFalse(d1.isForceQuirks());

        Token.Doctype d2 = tokens.get(1).asDoctype();
        assertEquals("html", d2.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", d2.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", d2.getSystemIdentifier());

        Token.Doctype d3 = tokens.get(2).asDoctype();
        assertEquals("html", d3.getName());
        assertEquals("about:legacy-compat", d3.getSystemIdentifier());
    }

    // Tests malformed Doctype triggers forceQuirks
    @Test
    public void testDoctype_malformed_setsForceQuirks() {
        List<Token> tokens = tokenize("<!DOCTYPE>");
        assertTrue(tokens.get(0).isDoctype());
        assertTrue(tokens.get(0).asDoctype().isForceQuirks());
    }

    // Tests RCDATA state with title element and character entities
    @Test
    public void testRcdata_titleElement_escapesEntitiesAndFindsEndTag() {
        CharacterReader r = new CharacterReader("Some &lt;title&gt; text</title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rcdata);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("Some <title> text", token.asCharacter().getData());
    }

    // Tests RCDATA when encountering a non-matching end tag
    @Test
    public void testRcdata_nonMatchingEndTag_treatedAsCharacterData() {
        CharacterReader r = new CharacterReader("Text </style></title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.tagPending = t.createTagPending(true).name("title");
        t.transition(TokeniserState.Rcdata);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertTrue(token.asCharacter().getData().contains("</style>"));
    }

    // Tests Rawtext state (style tag content)
    @Test
    public void testRawtext_styleTag_readsRawDataUntilEndTag() {
        CharacterReader r = new CharacterReader("div > p { color: red; &amp; }</style>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.tagPending = t.createTagPending(true).name("style");
        t.transition(TokeniserState.Rawtext);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("div > p { color: red; &amp; }", token.asCharacter().getData());
    }

    // Tests ScriptData state and ScriptData escape start / dash / double escaped states
    @Test
    public void testScriptData_escapedAndDoubleEscaped_readsCorrectly() {
        CharacterReader r = new CharacterReader("<!-- <script>var x = 1;</script> --> </script>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.tagPending = t.createTagPending(true).name("script");
        t.transition(TokeniserState.ScriptData);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertTrue(token.asCharacter().getData().contains("var x = 1;"));
    }

    // Tests CDATA section parsing
    @Test
    public void testCdataSection_validData_emitsLiteralContent() {
        CharacterReader r = new CharacterReader("raw <data> & text]]>after");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.CdataSection);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("raw <data> & text", token.asCharacter().getData());
    }

    // Tests PLAINTEXT state handling
    @Test
    public void testPlaintext_readsAllContentUntilEof() {
        CharacterReader r = new CharacterReader("line1\n<p>line2</p>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.PLAINTEXT);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("line1\n<p>line2</p>", token.asCharacter().getData());
    }

    // Tests null character handling across Data, TagName, and AttributeValue states
    @Test
    public void testNullCharacter_inVariousStates_replacesAndRecordsError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        List<Token> tokens = tokenize("<\u0000foo a='\u0000'>data\u0000", errors);
        assertFalse(errors.isEmpty());
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertTrue(tag.name().contains("\uFFFD"));
    }

    // Tests EOF edge cases in unclosed tags and attributes
    @Test
    public void testEofInTagAndAttribute_emitsEofErrorAndRecovers() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        List<Token> tokens = tokenize("<tag attr=\"val", errors);
        assertFalse(errors.isEmpty());
        Token last = tokens.get(tokens.size() - 1);
        assertEquals(Token.TokenType.EOF, last.type());
    }
}