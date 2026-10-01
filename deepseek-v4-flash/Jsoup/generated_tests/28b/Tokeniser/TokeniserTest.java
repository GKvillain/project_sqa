package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        ParseErrorList errors = ParseErrorList.tracking(1);
        return new Tokeniser(reader, errors);
    }

    @Test
    public void testRead_simpleCharacter_returnsCharacter() {
        Tokeniser t = createTokeniser("a");
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        assertEquals("a", ((Token.Character) tok).getData());
    }

    @Test
    public void testRead_withPendingStartTag_returnsStartTag() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "div";
        t.emit(startTag);
        Token tok = t.read();
        assertSame(startTag, tok);
    }

    @Test
    public void testRead_emitStringThenStartTag_returnsCharacterThenStartTag() {
        Tokeniser t = createTokeniser("");
        t.emit("hello");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "p";
        t.emit(startTag);
        Token tok1 = t.read();
        assertTrue(tok1 instanceof Token.Character);
        assertEquals("hello", ((Token.Character) tok1).getData());
        Token tok2 = t.read();
        assertSame(startTag, tok2);
    }

    @Test
    public void testEmitChar_appendsToCharBuffer() {
        Tokeniser t = createTokeniser("");
        t.emit('x');
        Token tok = t.read();
        assertTrue(tok instanceof Token.Character);
        assertEquals("x", ((Token.Character) tok).getData());
    }

    @Test
    public void testConsumeCharacterReference_numeric_returnsChar() {
        CharacterReader reader = new CharacterReader("#65;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result.charValue());
    }

    @Test
    public void testConsumeCharacterReference_hex_returnsChar() {
        CharacterReader reader = new CharacterReader("#x41;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('A', result.charValue());
    }

    @Test
    public void testConsumeCharacterReference_numericOutOfRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#110000;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, result.charValue());
    }

    @Test
    public void testConsumeCharacterReference_namedEntity_returnsChar() {
        CharacterReader reader = new CharacterReader("amp;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('&', result.charValue());
    }

    @Test
    public void testConsumeCharacterReference_invalidNamedRef_returnsNull() {
        CharacterReader reader = new CharacterReader("xyz;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, false);
        assertNull(result);
    }

    @Test
    public void testConsumeCharacterReference_inAttributeWithTrailingLetter_returnsNull() {
        CharacterReader reader = new CharacterReader("ampx;");
        ParseErrorList errors = ParseErrorList.tracking(1);
        Tokeniser t = new Tokeniser(reader, errors);
        Character result = t.consumeCharacterReference(null, true);
        assertNull(result);
    }

    @Test
    public void testTransition_changesState() {
        Tokeniser t = createTokeniser("");
        assertEquals(TokeniserState.Data, t.getState());
        t.transition(TokeniserState.Data);
        assertEquals(TokeniserState.Data, t.getState());
    }

    @Test
    public void testAdvanceTransition_advancesReaderAndChangesState() {
        Tokeniser t = createTokeniser("ab");
        assertEquals(TokeniserState.Data, t.getState());
        t.advanceTransition(TokeniserState.Data);
        assertEquals(TokeniserState.Data, t.getState());
    }

    @Test
    public void testIsAppropriateEndTagToken_noLastStartTag_returnsFalse() {
        Tokeniser t = createTokeniser("");
        assertFalse(t.isAppropriateEndTagToken());
    }

    @Test
    public void testIsAppropriateEndTagToken_withMatchingTag_returnsTrue() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "div";
        t.emit(startTag);
        Token.EndTag endTag = new Token.EndTag();
        endTag.tagName = "div";
        t.tagPending = endTag;
        assertTrue(t.isAppropriateEndTagToken());
    }

    @Test
    public void testAppropriateEndTagName_returnsLastStartTagName() {
        Tokeniser t = createTokeniser("");
        Token.StartTag startTag = new Token.StartTag();
        startTag.tagName = "span";
        t.emit(startTag);
        assertEquals("span", t.appropriateEndTagName());
    }

    @Test
    public void testCreateTagPending_returnsCorrectTagType() {
        Tokeniser t = createTokeniser("");
        Token.Tag startTag = t.createTagPending(true);
        assertTrue(startTag instanceof Token.StartTag);
        assertSame(t.tagPending, startTag);
        Token.Tag endTag = t.createTagPending(false);
        assertTrue(endTag instanceof Token.EndTag);
        assertSame(t.tagPending, endTag);
    }

    @Test
    public void testEmitTagPending_emitsTag() {
        Tokeniser t = createTokeniser("");
        t.createTagPending(true);
        t.tagPending.tagName = "a";
        t.emitTagPending();
        Token tok = t.read();
        assertTrue(tok instanceof Token.StartTag);
        assertEquals("a", ((Token.StartTag) tok).tagName);
    }

    @Test
    public void testCreateTempBuffer_initialized() {
        Tokeniser t = createTokeniser("");
        assertNull(t.dataBuffer);
        t.createTempBuffer();
        assertNotNull(t.dataBuffer);
    }
}