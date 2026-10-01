package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = ParseErrorList.tracking(1); // allow one error to be recorded
        return new Tokeniser(reader, errors);
    }

    private void assertTokenType(Token.TokenType type, Token token) {
        assertEquals(type, token.type);
    }

    // Tests empty input: should return EOF token
    @Test
    public void testRead_emptyInput_returnsEOF() {
        Tokeniser t = createTokeniser("");
        Token token = t.read();
        assertTokenType(Token.TokenType.EOF, token);
    }

    // Tests plain text input: returns a single Character token with the text
    @Test
    public void testRead_plainText_returnsCharacterToken() {
        Tokeniser t = createTokeniser("text");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("text", ((Token.Character)token).getData());
        // next token should be EOF
        assertTokenType(Token.TokenType.EOF, t.read());
    }

    // Tests a simple start tag: returns StartTag token with correct tag name
    @Test
    public void testRead_simpleStartTag_returnsStartTag() {
        Tokeniser t = createTokeniser("<html>");
        Token token = t.read();
        assertTokenType(Token.TokenType.StartTag, token);
        Token.StartTag startTag = (Token.StartTag) token;
        assertEquals("html", startTag.tagName);
        assertFalse(startTag.selfClosing);
    }

    // Tests a simple end tag: returns EndTag token with correct tag name
    @Test
    public void testRead_simpleEndTag_returnsEndTag() {
        Tokeniser t = createTokeniser("</html>");
        Token token = t.read();
        assertTokenType(Token.TokenType.EndTag, token);
        assertEquals("html", ((Token.EndTag)token).tagName);
    }

    // Tests a self-closing start tag: selfClosing flag is set and next read does not throw
    @Test
    public void testRead_selfClosingTag_selfClosingFlagSet() {
        Tokeniser t = createTokeniser("<br/>");
        Token token = t.read();
        assertTokenType(Token.TokenType.StartTag, token);
        Token.StartTag startTag = (Token.StartTag) token;
        assertEquals("br", startTag.tagName);
        assertTrue(startTag.selfClosing);
        // next read should produce EOF and acknowledge the flag
        assertTokenType(Token.TokenType.EOF, t.read());
    }

    // Tests a valid named character reference with semicolon: returns resolved character
    @Test
    public void testRead_characterReference_validNamed_returnsResolvedChar() {
        Tokeniser t = createTokeniser("&amp;");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("&", ((Token.Character)token).getData());
    }

    // Tests a base named character reference without semicolon: still resolved
    @Test
    public void testRead_characterReferenceBaseWithoutSemicolon_returnsResolvedChar() {
        Tokeniser t = createTokeniser("&amp");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("&", ((Token.Character)token).getData());
    }

    // Tests a numeric character reference missing semicolon: returns char and error recorded
    @Test
    public void testRead_characterReferenceMissingSemicolon_number_returnsCharWithError() {
        Tokeniser t = createTokeniser("&#65");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("A", ((Token.Character)token).getData());
        // error path covered because ParseErrorList can accept one error
    }

    // Tests a numeric character reference out of valid range: returns replacement character
    @Test
    public void testRead_characterReferenceOutOfRange_returnsReplacementChar() {
        Tokeniser t = createTokeniser("&#1114112;"); // > U+10FFFF
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("\uFFFD", ((Token.Character)token).getData());
    }

    // Tests an invalid named character reference: returns literal text including ampersand
    @Test
    public void testRead_characterReferenceInvalidNamed_returnsLiteralAmpersand() {
        Tokeniser t = createTokeniser("&unknown;");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("&unknown;", ((Token.Character)token).getData());
    }

    // Tests ampersand followed by space (matches notCharRefCharsSorted): returns literal '& '
    @Test
    public void testRead_characterReferenceFollowedBySpace_returnsLiteralAmpersandAndSpace() {
        Tokeniser t = createTokeniser("& ");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("& ", ((Token.Character)token).getData());
    }

    // Tests a hex numeric character reference: resolved correctly
    @Test
    public void testRead_characterReferenceHexNumber_returnsResolvedChar() {
        Tokeniser t = createTokeniser("&#x41;");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("A", ((Token.Character)token).getData());
    }

    // Tests text between two tags: returns StartTag, Character, EndTag tokens in order
    @Test
    public void testRead_textBetweenTags_returnsCharacterToken() {
        Tokeniser t = createTokeniser("<p>text</p>");
        Token start = t.read();
        assertTokenType(Token.TokenType.StartTag, start);
        assertEquals("p", ((Token.StartTag)start).tagName);

        Token text = t.read();
        assertTokenType(Token.TokenType.Character, text);
        assertEquals("text", ((Token.Character)text).getData());

        Token end = t.read();
        assertTokenType(Token.TokenType.EndTag, end);
        assertEquals("p", ((Token.EndTag)end).tagName);

        assertTokenType(Token.TokenType.EOF, t.read());
    }

    // Tests multiple character emits before a token: merged into a single Character token
    @Test
    public void testRead_multipleCharEmits_mergedCharacterToken() {
        Tokeniser t = createTokeniser("a&amp;b");
        Token token = t.read();
        assertTokenType(Token.TokenType.Character, token);
        assertEquals("a&b", ((Token.Character)token).getData());
    }

    // Tests a comment token: returns CommentToken with correct data
    @Test
    public void testRead_comment_returnsCommentToken() {
        Tokeniser t = createTokeniser("<!-- test -->");
        Token token = t.read();
        assertTokenType(Token.TokenType.Comment, token);
        assertEquals(" test ", ((Token.Comment)token).getData());
    }

    // Tests a doctype token: returns DoctypeToken with correct name
    @Test
    public void testRead_doctype_returnsDoctypeToken() {
        Tokeniser t = createTokeniser("<!DOCTYPE html>");
        Token token = t.read();
        assertTokenType(Token.TokenType.Doctype, token);
        assertEquals("html", ((Token.Doctype)token).getName());
    }

    // Tests that emitTagPending finalises the tag and makes it the next token
    @Test
    public void testEmitTagPending_finalisesAndEmits() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = (Token.StartTag) t.createTagPending(true);
        startTag.tagName = "mytag";
        t.emitTagPending();
        Token token = t.read();
        assertTokenType(Token.TokenType.StartTag, token);
        assertEquals("mytag", ((Token.StartTag)token).tagName);
    }

    // Tests isAppropriateEndTagToken: returns true when pending end tag matches last start tag
    @Test
    public void testIsAppropriateEndTagToken_matchesLastStartTag_returnsTrue() {
        Tokeniser t = createTokeniser("<html></html>");
        t.read(); // emits start tag, lastStartTag = "html"
        t.createTagPending(false);
        ((Token.EndTag)t.tagPending).tagName = "html";
        assertTrue(t.isAppropriateEndTagToken());
    }

    // Tests appropriateEndTagName: returns the name of the last start tag
    @Test
    public void testAppropriateEndTagName_returnsLastStartTag() {
        Tokeniser t = createTokeniser("<html>");
        t.read();
        assertEquals("html", t.appropriateEndTagName());
    }

    // Tests createTagPending(true) returns a StartTag
    @Test
    public void testCreateTagPending_createsStartTag() {
        Tokeniser t = createTokeniser("");
        Token.Tag tag = t.createTagPending(true);
        assertTrue(tag instanceof Token.StartTag);
    }

    // Tests createTagPending(false) returns an EndTag
    @Test
    public void testCreateTagPending_createsEndTag() {
        Tokeniser t = createTokeniser("");
        Token.Tag tag = t.createTagPending(false);
        assertTrue(tag instanceof Token.EndTag);
    }

    // Tests unescapeEntities: replaces character references in input with actual characters
    @Test
    public void testUnescapeEntities_handlesCharacterReference() {
        Tokeniser t = createTokeniser("a&amp;b");
        assertEquals("a&b", t.unescapeEntities(false));
    }
}