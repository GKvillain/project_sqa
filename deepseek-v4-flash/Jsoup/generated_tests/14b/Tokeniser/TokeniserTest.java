package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    private Tokeniser tokeniser(String input) {
        return new Tokeniser(new CharacterReader(input));
    }

    // Tests empty input
    @Test
    public void testRead_emptyInput_returnsEof() {
        Tokeniser t = tokeniser("");
        Token token = t.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.EOF, token.type);
    }

    // Tests plain text accumulates into a character token
    @Test
    public void testRead_plainText_returnsCharacterToken() {
        Tokeniser t = tokeniser("Hello");
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("Hello", ((Token.Character) token).getData());
        assertEquals(Token.TokenType.EOF, t.read().type);
    }

    // Tests character reference handling in data state
    @Test
    public void testRead_characterReference_returnsDecodedCharacter() {
        Tokeniser t = tokeniser("&amp;");
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("&", ((Token.Character) token).getData());
    }

    // Tests start tag token
    @Test
    public void testRead_startTag_returnsStartTag() {
        Tokeniser t = tokeniser("<p>Text");
        Token token = t.read();
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("p", ((Token.StartTag) token).tagName);
        assertEquals(Token.TokenType.Character, t.read().type);
    }

    // Tests end tag with attributes is tokenized
    @Test
    public void testRead_endTagWithAttributes_returnsEndTagWithAttributes() {
        Tokeniser t = tokeniser("</p id=\"x\">");
        Token token = t.read();
        assertEquals(Token.TokenType.EndTag, token.type);
        assertEquals("p", ((Token.EndTag) token).tagName);
        assertEquals(1, ((Token.EndTag) token).attributes.size());
    }

    // Tests comment token
    @Test
    public void testRead_comment_returnsCommentData() {
        Tokeniser t = tokeniser("<!-- comment -->");
        Token token = t.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals(" comment ", ((Token.Comment) token).getData());
    }

    // Tests doctype token
    @Test
    public void testRead_doctype_returnsDoctypeName() {
        Tokeniser t = tokeniser("<!DOCTYPE html>");
        Token token = t.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests self-closing start tag and unacknowledged self-closing flag path
    @Test
    public void testRead_selfClosingTag_returnsSelfClosingAndEof() {
        Tokeniser t = tokeniser("<br/>");
        Token first = t.read();
        assertEquals(Token.TokenType.StartTag, first.type);
        assertTrue(((Token.StartTag) first).selfClosing);
        assertEquals(Token.TokenType.EOF, t.read().type);
    }

    // Tests start tag attributes are retained
    @Test
    public void testRead_startTagWithAttributes_returnsAttributes() {
        Tokeniser t = tokeniser("<div id=\"42\" class=\"c\">");
        Token token = t.read();
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("div", tag.tagName);
        assertEquals(2, tag.attributes.size());
        assertEquals("42", tag.attributes.get("id"));
        assertEquals("c", tag.attributes.get("class"));
    }

    // Tests string and character emission are buffered into one character token
    @Test
    public void testEmitStringAndChar_buffersCharacters() {
        Tokeniser t = tokeniser("");
        t.emit("ab");
        t.emit('c');
        Token token = t.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("abc", ((Token.Character) token).getData());
    }

    // Tests creating and emitting a pending start tag
    @Test
    public void testCreateTagPending_start_emitsStartTag() {
        Tokeniser t = tokeniser("");
        t.createTagPending(true);
        t.tagPending.tagName = "br";
        t.tagPending.selfClosing = true;
        t.emitTagPending();
        Token token = t.read();
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("br", ((Token.StartTag) token).tagName);
        assertTrue(((Token.StartTag) token).selfClosing);
    }

    // Tests creating and emitting a pending end tag
    @Test
    public void testCreateTagPending_end_emitsEndTag() {
        Tokeniser t = tokeniser("");
        t.createTagPending(false);
        t.tagPending.tagName = "p";
        t.emitTagPending();
        Token token = t.read();
        assertEquals(Token.TokenType.EndTag, token.type);
        assertEquals("p", ((Token.EndTag) token).tagName);
    }

    // Tests appropriate end tag detection, including missing last start tag
    @Test
    public void testIsAppropriateEndTagToken_comparesToLastStartTag() {
        Tokeniser matching = tokeniser("<div>");
        matching.read();
        matching.createTagPending(false);
        matching.tagPending.tagName = "div";
        assertTrue(matching.isAppropriateEndTagToken());

        Tokeniser nonMatching = tokeniser("<div>");
        nonMatching.read();
        nonMatching.createTagPending(false);
        nonMatching.tagPending.tagName = "span";
        assertFalse(nonMatching.isAppropriateEndTagToken());

        Tokeniser noStart = tokeniser("");
        noStart.createTagPending(false);
        noStart.tagPending.tagName = "div";
        assertFalse(noStart.isAppropriateEndTagToken());
    }

    // Tests null-result guards in character reference consumption
    @Test
    public void testConsumeCharacterReference_nullConditions_returnsNull() {
        assertNull(tokeniser("").consumeCharacterReference(null, false));
        assertNull(tokeniser("=").consumeCharacterReference('=', false));
        assertNull(tokeniser("&").consumeCharacterReference(null, false));
        assertNull(tokeniser("<").consumeCharacterReference(null, false));
        assertNull(tokeniser("\t").consumeCharacterReference(null, false));
    }

    // Tests decimal and hexadecimal numeric character references
    @Test
    public void testConsumeCharacterReference_numericReferences_returnsCharacter() {
        Character decimal = tokeniser("#65;").consumeCharacterReference(null, false);
        assertNotNull(decimal);
        assertEquals('A', decimal.charValue());

        Character hexLower = tokeniser("x41;").consumeCharacterReference(null, false);
        assertNotNull(hexLower);
        assertEquals('A', hexLower.charValue());

        Character hexUpper = tokeniser("X41;").consumeCharacterReference(null, false);
        assertNotNull(hexUpper);
        assertEquals('A', hexUpper.charValue());

        Character missingSemi = tokeniser("#65").consumeCharacterReference(null, false);
        assertNotNull(missingSemi);
        assertEquals('A', missingSemi.charValue());
    }

    // Tests invalid numeric character references
    @Test
    public void testConsumeCharacterReference_invalidNumericReference_returnsReplacementChar() {
        assertNull(tokeniser("#;").consumeCharacterReference(null, false));

        Character surrogate = tokeniser("#xD800;").consumeCharacterReference(null, false);
        assertNotNull(surrogate);
        assertEquals('\uFFFD', surrogate.charValue());

        Character outOfRange = tokeniser("#x110000;").consumeCharacterReference(null, false);
        assertNotNull(outOfRange);
        assertEquals('\uFFFD', outOfRange.charValue());
    }

    // Tests named character references
    @Test
    public void testConsumeCharacterReference_namedReference_returnsCharacter() {
        Character withSemi = tokeniser("amp;").consumeCharacterReference(null, false);
        assertNotNull(withSemi);
        assertEquals('&', withSemi.charValue());

        Character withoutSemi = tokeniser("amp").consumeCharacterReference(null, false);
        assertNotNull(withoutSemi);
        assertEquals('&', withoutSemi.charValue());
    }

    // Tests attribute-specific guard on named character references
    @Test
    public void testConsumeCharacterReference_namedReferenceInAttribute_guardsAgainstFollowingCharacters() {
        assertNull(tokeniser("ampx").consumeCharacterReference(null, true));
        assertNull(tokeniser("amp=").consumeCharacterReference(null, true));

        Character withSemi = tokeniser("amp;x").consumeCharacterReference(null, true);
        assertNotNull(withSemi);
        assertEquals('&', withSemi.charValue());
    }

    // Tests unknown named character references return null
    @Test
    public void testConsumeCharacterReference_unknownNamedReference_returnsNull() {
        assertNull(tokeniser("zzzz;").consumeCharacterReference(null, false));
        assertNull(tokeniser("zzzz").consumeCharacterReference(null, false));
    }

    // Tests state transitions, error tracking, namespace check, and temp buffer
    @Test
    public void testStateAndErrorTrackingAccessors_updateValues() {
        Tokeniser t = tokeniser("x");
        assertEquals(TokeniserState.Data, t.getState());
        t.transition(TokeniserState.Rawtext);
        assertEquals(TokeniserState.Rawtext, t.getState());
        t.advanceTransition(TokeniserState.RCDATA);
        assertEquals(TokeniserState.RCDATA, t.getState());

        assertTrue(t.isTrackErrors());
        t.setTrackErrors(false);
        assertFalse(t.isTrackErrors());

        assertTrue(t.currentNodeInHtmlNS());

        t.createTempBuffer();
        assertNotNull(t.dataBuffer);
        assertEquals(0, t.dataBuffer.length());
    }
}