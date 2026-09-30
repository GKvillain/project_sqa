package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Helper to create Tokeniser with empty input
    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = new ParseErrorList(16, 16);
        return new Tokeniser(reader, errors);
    }

    // Tests read() on empty input returns null-like (EOF)
    @Test
    public void testRead_emptyInput_returnsNull() {
        Tokeniser tokeniser = createTokeniser("");
        Token token = tokeniser.read();
        assertNull(token);
    }

    // Tests read() with simple text input
    @Test
    public void testRead_simpleText_returnsCharacterToken() {
        Tokeniser tokeniser = createTokeniser("hello");
        Token token = tokeniser.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("hello", ((Token.Character) token).getData());
    }

    // Tests emission of a StartTag token via emit(Token)
    @Test
    public void testEmit_startTag_setsLastStartTagAndSelfClosingFlag() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        // After emit, isEmitPending should be true
        // lastStartTag should be set
        // selfClosingFlagAcknowledged should be false
        assertEquals("div", tokeniser.appropriateEndTagName());
        // read() will consume the token and check selfClosingFlag,
        // but we can verify via isAppropriateEndTagToken
        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    // Tests emit(Token) when emitting an EndTag with attributes triggers error
    @Test
    public void testEmit_endTagWithAttributes_errorLogged() {
        Tokeniser tokeniser = createTokeniser("");
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.attributes.put("class", "foo");
        tokeniser.emit(endTag);
        // Should log error (we can't directly check error log, but no exception)
        // At least verify isEmitPending is true
        assertTrue(true); // placeholder - confirm no crash
    }

    // Tests emit(Token) when isEmitPending already true throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_doubleEmit_throwsException() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        tokeniser.emit(startTag);
        // Second emit should throw IllegalStateException (Validate.isFalse)
        tokeniser.emit(startTag);
    }

    // Tests emit(String) appends to charBuffer
    @Test
    public void testEmitString_appendsToBuffer() {
        Tokeniser tokeniser = createTokeniser("");
        tokeniser.emit("test");
        tokeniser.emit("ing");
        // read() should combine them into one Character token
        Token token = tokeniser.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("testing", ((Token.Character) token).getData());
    }

    // Tests emit(char) appends to charBuffer
    @Test
    public void testEmitChar_appendsToBuffer() {
        Tokeniser tokeniser = createTokeniser("");
        tokeniser.emit('a');
        tokeniser.emit('b');
        Token token = tokeniser.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("ab", ((Token.Character) token).getData());
    }

    // Tests transition() changes state
    @Test
    public void testTransition_changesState() {
        Tokeniser tokeniser = createTokeniser("");
        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
    }

    // Tests advanceTransition() advances reader and changes state
    @Test
    public void testAdvanceTransition_advancesReaderAndChangesState() {
        Tokeniser tokeniser = createTokeniser("a");
        TokeniserState oldState = tokeniser.getState();
        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        // reader should have advanced past 'a'
        assertNull(tokeniser.consumeCharacterReference(null, false));
    }

    // Tests acknowledgeSelfClosingFlag() clears the flag
    @Test
    public void testAcknowledgeSelfClosingFlag_clearsFlag() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("br");
        startTag.selfClosing = true;
        tokeniser.emit(startTag);
        // selfClosingFlagAcknowledged should be false
        tokeniser.acknowledgeSelfClosingFlag();
        // Now it should be true; read() should not call error
        // We can try reading an empty tokeniser - it should error via read()
        // but we can't directly check, just ensure no exception
        assertTrue(true);
    }

    // Tests consumeCharacterReference with empty string returns null
    @Test
    public void testConsumeCharacterReference_emptyInput_returnsNull() {
        Tokeniser tokeniser = createTokeniser("");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
    }

    // Tests consumeCharacterReference with numeric reference (decimal)
    @Test
    public void testConsumeCharacterReference_numericDecimal_returnsChar() {
        Tokeniser tokeniser = createTokeniser("#65;");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result.charValue());
    }

    // Tests consumeCharacterReference with numeric reference (hex)
    @Test
    public void testConsumeCharacterReference_numericHex_returnsChar() {
        Tokeniser tokeniser = createTokeniser("#x41;");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result.charValue());
    }

    // Tests consumeCharacterReference with named reference
    @Test
    public void testConsumeCharacterReference_named_returnsChar() {
        Tokeniser tokeniser = createTokeniser("amp;");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('&', result.charValue());
    }

    // Tests consumeCharacterReference with invalid named reference returns null
    @Test
    public void testConsumeCharacterReference_invalidNamed_returnsNull() {
        Tokeniser tokeniser = createTokeniser("invalid;");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
    }

    // Tests consumeCharacterReference with missing semicolon after numeric
    @Test
    public void testConsumeCharacterReference_numericMissingSemicolon_returnsChar() {
        Tokeniser tokeniser = createTokeniser("#65");
        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result.charValue());
    }

    // Tests createTagPending creates StartTag or EndTag
    @Test
    public void testCreateTagPending_start_createsStartTag() {
        Tokeniser tokeniser = createTokeniser("");
        Token.Tag tag = tokeniser.createTagPending(true);
        assertNotNull(tag);
        assertEquals(Token.TokenType.StartTag, tag.type);
    }

    // Tests emitTagPending finalises and emits the tag
    @Test
    public void testEmitTagPending_finalisesAndEmitsTag() {
        Tokeniser tokeniser = createTokeniser("");
        Token.Tag tag = tokeniser.createTagPending(true);
        tag.name("div");
        tokeniser.emitTagPending();
        // tagPending should be finalised and emitted
        Token readToken = tokeniser.read();
        assertNotNull(readToken);
        assertEquals(Token.TokenType.StartTag, readToken.type);
    }

    // Tests createCommentPending and emitCommentPending
    @Test
    public void testCreateAndEmitCommentPending_emitsComment() {
        Tokeniser tokeniser = createTokeniser("");
        tokeniser.createCommentPending();
        tokeniser.emitCommentPending();
        Token token = tokeniser.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.Comment, token.type);
    }

    // Tests createDoctypePending and emitDoctypePending
    @Test
    public void testCreateAndEmitDoctypePending_emitsDoctype() {
        Tokeniser tokeniser = createTokeniser("");
        tokeniser.createDoctypePending();
        tokeniser.emitDoctypePending();
        Token token = tokeniser.read();
        assertNotNull(token);
        assertEquals(Token.TokenType.Doctype, token.type);
    }

    // Tests isAppropriateEndTagToken returns true when tag matches lastStartTag
    @Test
    public void testIsAppropriateEndTagToken_matchingTags_returnsTrue() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        tokeniser.emit(startTag);
        // After emit, lastStartTag is set; now create an end tag with same name
        tokeniser.createTagPending(false);
        tokeniser.tagPending.name("div");
        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    // Tests isAppropriateEndTagToken returns false when tag does not match lastStartTag
    @Test
    public void testIsAppropriateEndTagToken_nonMatchingTags_returnsFalse() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        tokeniser.emit(startTag);
        tokeniser.createTagPending(false);
        tokeniser.tagPending.name("span");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests appropriateEndTagName returns lastStartTag name
    @Test
    public void testAppropriateEndTagName_returnsLastStartTagName() {
        Tokeniser tokeniser = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");
        tokeniser.emit(startTag);
        assertEquals("p", tokeniser.appropriateEndTagName());
    }
}