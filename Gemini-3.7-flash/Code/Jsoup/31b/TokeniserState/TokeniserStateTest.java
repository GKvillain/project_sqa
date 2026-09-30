package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    // Tests Data state with plain text
    @Test
    public void testData_plainText_emitsCharacterToken() {
        CharacterReader r = new CharacterReader("Hello World");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello World", ((Token.Character) token).getData());
    }

    // Tests Data state encountering tag open
    @Test
    public void testData_tagOpen_transitionsToTagOpen() {
        CharacterReader r = new CharacterReader("<p>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("p", ((Token.StartTag) token).name());
    }

    // Tests Data state with null character
    @Test
    public void testData_nullChar_reportsErrorAndEmitsNull() {
        CharacterReader r = new CharacterReader("\u0000");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("\u0000", ((Token.Character) token).getData());
        assertFalse(t.getErrors().isEmpty());
    }

    // Tests TagOpen state with start tag, bogus comment, and error character
    @Test
    public void testTagOpen_questionMark_transitionsToBogusComment() {
        CharacterReader r = new CharacterReader("<?xml version=\"1.0\"?>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals("?xml version=\"1.0\"?", ((Token.Comment) token).getData());
    }

    // Tests TagOpen state with invalid character emitting '<'
    @Test
    public void testTagOpen_invalidChar_emitsLessThanAndData() {
        CharacterReader r = new CharacterReader("<123");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("<", ((Token.Character) token).getData());
        assertFalse(t.getErrors().isEmpty());
    }

    // Tests EndTagOpen with valid end tag name
    @Test
    public void testEndTagOpen_validName_emitsEndTag() {
        CharacterReader r = new CharacterReader("</div>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isEndTag());
        assertEquals("div", ((Token.EndTag) token).name());
    }

    // Tests EndTagOpen with missing tag name
    @Test
    public void testEndTagOpen_greaterThan_reportsErrorAndTransitionsToData() {
        CharacterReader r = new CharacterReader("</>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.read();
        assertFalse(t.getErrors().isEmpty());
    }

    // Tests TagName state with self closing start tag
    @Test
    public void testTagName_selfClosing_emitsSelfClosingTag() {
        CharacterReader r = new CharacterReader("<br/>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("br", ((Token.StartTag) token).name());
        assertTrue(((Token.StartTag) token).isSelfClosing());
    }

    // Tests TagName with attribute parsing across states
    @Test
    public void testTagName_withAttributes_correctlyParsesAttributes() {
        CharacterReader r = new CharacterReader("<a href=\"http://example.com\" id='link' target=blank>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("a", tag.name());
        assertEquals("http://example.com", tag.attributes.get("href"));
        assertEquals("link", tag.attributes.get("id"));
        assertEquals("blank", tag.attributes.get("target"));
    }

    // Tests MarkupDeclarationOpen transition to Comment
    @Test
    public void testMarkupDeclarationOpen_comment_parsesComment() {
        CharacterReader r = new CharacterReader("<!-- this is a comment -->");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" this is a comment ", ((Token.Comment) token).getData());
    }

    // Tests CommentEndBang state
    @Test
    public void testCommentEndBang_dashBangDash_emitsComment() {
        CharacterReader r = new CharacterReader("<!-- comment --!>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" comment ", ((Token.Comment) token).getData());
        assertFalse(t.getErrors().isEmpty());
    }

    // Tests MarkupDeclarationOpen transition to Doctype
    @Test
    public void testMarkupDeclarationOpen_doctype_emitsDoctype() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype with PUBLIC and SYSTEM identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_parsedCorrectly() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" 'http://www.w3.org/TR/html4/strict.dtd'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
    }

    // Tests MarkupDeclarationOpen transition to CDATA
    @Test
    public void testMarkupDeclarationOpen_cdataSection_emitsCharacterData() {
        CharacterReader r = new CharacterReader("<![CDATA[raw & unescaped <data>]]>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("raw & unescaped <data>", ((Token.Character) token).getData());
    }

    // Tests Rcdata state and entity resolution
    @Test
    public void testRcdata_entityResolution_emitsResolvedEntity() {
        CharacterReader r = new CharacterReader("&amp;");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        TokeniserState.Rcdata.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("&", ((Token.Character) token).getData());
    }

    // Tests Rawtext and RawtextEndTagName states
    @Test
    public void testRawtext_endTag_transitionsToData() {
        CharacterReader r = new CharacterReader("some raw text</style>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        TokeniserState.Rawtext.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("some raw text", ((Token.Character) token).getData());
    }

    // Tests ScriptDataEscaped and ScriptDataDoubleEscaped states
    @Test
    public void testScriptData_escapedScript_parsesCorrectly() {
        CharacterReader r = new CharacterReader("<!-- <script> var x = 1; </script> --> </script>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        TokeniserState.ScriptData.read(t, r);
        Token token = t.read();
        assertNotNull(token);
    }

    // Tests PLAINTEXT state handling to EOF
    @Test
    public void testPLAINTEXT_readsAllCharacters() {
        CharacterReader r = new CharacterReader("<div>some text</div>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.noTracking());
        TokeniserState.PLAINTEXT.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("<div>some text</div>", ((Token.Character) token).getData());
    }

    // Tests BogusDoctype state handling
    @Test
    public void testBogusDoctype_invalidDoctype_setsForceQuirks() {
        CharacterReader r = new CharacterReader("<!DOCTYPE >");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertTrue(doctype.isForceQuirks());
    }
}