package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokeniserStateTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = ParseErrorList.tracking(10);
        return new Tokeniser(reader, errors);
    }

    // Tests reading simple text in Data state
    @Test
    public void testRead_dataStateText_emitsCharacterTokens() {
        Tokeniser t = createTokeniser("Hello World");
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello World", token.asCharacter().getData());
    }

    // Tests reading a start tag and transitions through TagOpen and TagName
    @Test
    public void testRead_startTag_emitsStartTagToken() {
        Tokeniser t = createTokeniser("<div>");
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("div", token.asStartTag().name());
        assertFalse(token.asStartTag().isSelfClosing());
    }

    // Tests self-closing tag transition and flag
    @Test
    public void testRead_selfClosingTag_emitsSelfClosingStartTag() {
        Tokeniser t = createTokeniser("<img src=\"test.png\" />");
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("img", token.asStartTag().name());
        assertTrue(token.asStartTag().isSelfClosing());
        assertEquals("test.png", token.asStartTag().getAttributes().get("src"));
    }

    // Tests end tag transition and attributes parsing
    @Test
    public void testRead_endTag_emitsEndTagToken() {
        Tokeniser t = createTokeniser("</span>");
        Token token = t.read();
        assertTrue(token.isEndTag());
        assertEquals("span", token.asEndTag().name());
    }

    // Tests attribute parsing with double quotes, single quotes, and unquoted values
    @Test
    public void testRead_attributesWithDifferentQuotes_parsesValuesCorrectly() {
        Tokeniser t = createTokeniser("<a href=\"http://example.com\" target='_blank' id=link1>");
        Token token = t.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = token.asStartTag();
        assertEquals("http://example.com", tag.getAttributes().get("href"));
        assertEquals("_blank", tag.getAttributes().get("target"));
        assertEquals("link1", tag.getAttributes().get("id"));
    }

    // Tests comment state handling normal comment syntax
    @Test
    public void testRead_standardComment_emitsCommentToken() {
        Tokeniser t = createTokeniser("<!-- This is a comment -->");
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" This is a comment ", token.asComment().getData());
        assertFalse(token.asComment().bogus);
    }

    // Tests bogus comment handling invalid tag syntax
    @Test
    public void testRead_bogusComment_emitsBogusCommentToken() {
        Tokeniser t = createTokeniser("<?xml version=\"1.0\"?>");
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
    }

    // Tests basic doctype declaration without identifiers
    @Test
    public void testRead_doctypeBasic_emitsDoctypeToken() {
        Tokeniser t = createTokeniser("<!DOCTYPE html>");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests doctype with PUBLIC identifier
    @Test
    public void testRead_doctypePublic_emitsDoctypeWithPublicId() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests doctype with both PUBLIC and SYSTEM identifiers
    @Test
    public void testRead_doctypePublicAndSystem_emitsDoctypeWithBothIds() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests doctype with SYSTEM identifier only using single quotes
    @Test
    public void testRead_doctypeSystemSingleQuoted_emitsDoctypeWithSystemId() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests malformed doctype triggering force quirks
    @Test
    public void testRead_malformedDoctype_forcesQuirks() {
        Tokeniser t = createTokeniser("<!DOCTYPE>");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertTrue(doctype.isForceQuirks());
    }

    // Tests CDATA section parsing
    @Test
    public void testRead_cdataSection_emitsCharacterTokenWithCdataContent() {
        CharacterReader reader = new CharacterReader("<![CDATA[raw & unescaped <data>]]>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("raw & unescaped <data>", token.asCharacter().getData());
    }

    // Tests character references in Data state
    @Test
    public void testRead_characterReferenceInData_resolvesEntity() {
        Tokeniser t = createTokeniser("&amp;&lt;&gt;&quot;");
        Token token1 = t.read();
        assertEquals("&", token1.asCharacter().getData());
        Token token2 = t.read();
        assertEquals("<", token2.asCharacter().getData());
        Token token3 = t.read();
        assertEquals(">", token3.asCharacter().getData());
        Token token4 = t.read();
        assertEquals("\"", token4.asCharacter().getData());
    }

    // Tests RCDATA state transition and character reference resolution
    @Test
    public void testRead_rcdataState_resolvesEntitiesUntilEndTag() {
        CharacterReader reader = new CharacterReader("AT&amp;T</title>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rcdata);
        
        Token token1 = t.read();
        assertEquals("AT", token1.asCharacter().getData());
        Token token2 = t.read();
        assertEquals("&", token2.asCharacter().getData());
        Token token3 = t.read();
        assertEquals("T", token3.asCharacter().getData());
    }

    // Tests Rawtext state handling
    @Test
    public void testRead_rawtextState_readsContentUntilEndTag() {
        CharacterReader reader = new CharacterReader("var x = 1 < 2;</style>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rawtext);

        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("var x = 1 < 2;", token.asCharacter().getData());
    }

    // Tests ScriptData state with escaped scripts
    @Test
    public void testRead_scriptDataState_handlesEscapedScript() {
        CharacterReader reader = new CharacterReader("<!-- <script>alert('test');</script> -->");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.ScriptData);

        Token token = t.read();
        assertTrue(token.isCharacter());
    }

    // Tests PLAINTEXT state consuming to EOF
    @Test
    public void testRead_plaintextState_readsAllAsCharacters() {
        CharacterReader reader = new CharacterReader("<b>plain text content</b>");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        t.transition(TokeniserState.PLAINTEXT);

        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("<b>plain text content</b>", token.asCharacter().getData());
    }

    // Tests EOF token generation at end of input
    @Test
    public void testRead_eofReached_emitsEOFToken() {
        Tokeniser t = createTokeniser("");
        Token token = t.read();
        assertTrue(token.isEOF());
    }

    // Tests comment ending with exclamation mark <!-- comment --!>
    @Test
    public void testRead_commentEndBang_emitsComment() {
        Tokeniser t = createTokeniser("<!-- comment --!>");
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" comment ", token.asComment().getData());
    }

    // Tests comment with dash right after opening <!--- text -->
    @Test
    public void testRead_commentStartDash_emitsComment() {
        Tokeniser t = createTokeniser("<!-- - text -->");
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" - text ", token.asComment().getData());
    }

    // Tests invalid markup declaration transitions to bogus comment
    @Test
    public void testRead_markupDeclarationOpenInvalid_emitsBogusComment() {
        Tokeniser t = createTokeniser("<!INVALID>");
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
        assertEquals("INVALID", token.asComment().getData());
    }

    // Tests end tag open with invalid character transitions to bogus comment
    @Test
    public void testRead_endTagOpenWithNonLetter_emitsBogusComment() {
        Tokeniser t = createTokeniser("</123>");
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
        assertEquals("123", token.asComment().getData());
    }

    // Tests empty end tag </ >
    @Test
    public void testRead_emptyEndTag_emitsNothingOrHandlesGracefully() {
        Tokeniser t = createTokeniser("</>");
        Token token = t.read();
        assertTrue(token.isEOF());
    }

    // Tests attribute name without value followed by slash and closing
    @Test
    public void testRead_booleanAttributeAndSlash_parsesCorrectly() {
        Tokeniser t = createTokeniser("<input disabled />");
        Token token = t.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = token.asStartTag();
        assertEquals("input", tag.name());
        assertTrue(tag.isSelfClosing());
        assertTrue(tag.getAttributes().hasKey("disabled"));
    }

    // Tests doctype with system identifier in double quotes
    @Test
    public void testRead_doctypeSystemDoubleQuoted_emitsDoctypeWithSystemId() {
        Tokeniser t = createTokeniser("<!DOCTYPE html SYSTEM \"about:legacy-compat\">");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests doctype missing closing quote or malformed identifier transitions to bogus doctype
    @Test
    public void testRead_doctypeBogusPublicIdentifier_forcesQuirks() {
        Tokeniser t = createTokeniser("<!DOCTYPE html PUBLIC missing-quotes>");
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertTrue(doctype.isForceQuirks());
    }

    // Tests script data double escaped transitions
    @Test
    public void testRead_scriptDataDoubleEscaped_handlesNestedScriptTag() {
        CharacterReader reader = new CharacterReader("<script><!-- <script>var a = 1;</script> -->");
        Tokeniser t = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token token1 = t.read();
        assertTrue(token1.isStartTag());
        assertEquals("script", token1.asStartTag().name());

        Token token2 = t.read();
        assertTrue(token2.isCharacter());
    }

    // Tests tag name with null character replacement
    @Test
    public void testRead_nullCharacterInTagName_replacesWithReplacementChar() {
        Tokeniser t = createTokeniser("<\u0000div>");
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("\uFFFDdiv", token.asStartTag().name());
    }
}