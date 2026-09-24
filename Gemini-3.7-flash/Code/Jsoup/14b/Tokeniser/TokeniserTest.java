package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests reading simple text tokens
    @Test
    public void testRead_plainText_returnsCharacterTokens() {
        CharacterReader reader = new CharacterReader("Hello world");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("Hello world", ((Token.Character) token).getData());
    }

    // Tests consuming hex character references
    @Test
    public void testConsumeCharacterReference_hexNumber_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("#x41;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consuming decimal character references
    @Test
    public void testConsumeCharacterReference_decNumber_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("#65;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consuming hex character references without semi-colon
    @Test
    public void testConsumeCharacterReference_hexNumberWithoutSemi_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("#x41");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consuming character reference with empty input
    @Test
    public void testConsumeCharacterReference_emptyReader_returnsNull() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests consuming character reference when next char is additionalAllowedCharacter
    @Test
    public void testConsumeCharacterReference_additionalAllowedCharMatch_returnsNull() {
        CharacterReader reader = new CharacterReader("=");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference('=', false);
        assertNull(c);
    }

    // Tests consuming character reference with invalid prefix characters
    @Test
    public void testConsumeCharacterReference_invalidPrefix_returnsNull() {
        CharacterReader reader = new CharacterReader("\t");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests consuming named character reference like &lt;
    @Test
    public void testConsumeCharacterReference_namedEntity_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("lt;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('<'), c);
    }

    // Tests consuming named character reference without semicolon
    @Test
    public void testConsumeCharacterReference_namedEntityWithoutSemi_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("gt");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('>'), c);
    }

    // Tests consuming invalid named entity
    @Test
    public void testConsumeCharacterReference_invalidNamedEntity_returnsNull() {
        CharacterReader reader = new CharacterReader("invalidentityname;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests numeric character reference out of valid unicode range
    @Test
    public void testConsumeCharacterReference_invalidCodePoint_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
    }

    // Tests numeric character reference with no digits
    @Test
    public void testConsumeCharacterReference_noDigits_returnsNull() {
        CharacterReader reader = new CharacterReader("#;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests creating and emitting start tag pending
    @Test
    public void testEmitTagPending_startTag_emitsCorrectToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token.Tag tag = tokeniser.createTagPending(true);
        tag.name("div");
        tokeniser.emitTagPending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type);
        assertEquals("div", ((Token.StartTag) token).name());
    }

    // Tests appropriate end tag comparison
    @Test
    public void testIsAppropriateEndTagToken_matchingTag_returnsTrue() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("script");
        tokeniser.emitTagPending();

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.name("script");

        assertTrue(tokeniser.isAppropriateEndTagToken());
    }

    // Tests appropriate end tag comparison when mismatched
    @Test
    public void testIsAppropriateEndTagToken_mismatchedTag_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("title");
        tokeniser.emitTagPending();

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.name("style");

        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests creating and emitting comment token
    @Test
    public void testEmitCommentPending_validComment_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("test comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals("test comment", ((Token.Comment) token).getData());
    }

    // Tests creating and emitting doctype token
    @Test
    public void testEmitDoctypePending_validDoctype_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests state transition
    @Test
    public void testTransition_validState_changesState() {
        CharacterReader reader = new CharacterReader("test");
        Tokeniser tokeniser = new Tokeniser(reader);
        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    // Tests advance transition
    @Test
    public void testAdvanceTransition_validState_advancesReaderAndChangesState() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.advanceTransition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    // Tests exception when emitting multiple pending tokens without reading
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_duplicatePendingToken_throwsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token.StartTag tag1 = new Token.StartTag();
        tag1.name("div");
        Token.StartTag tag2 = new Token.StartTag();
        tag2.name("p");

        tokeniser.emit(tag1);
        tokeniser.emit(tag2);
    }

    // Tests consuming uppercase hex character references
    @Test
    public void testConsumeCharacterReference_uppercaseHex_returnsDecodedChar() {
        CharacterReader reader = new CharacterReader("#X41;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consuming windows-1252 specific character reference mapping
    @Test
    public void testConsumeCharacterReference_win1252Entity_returnsMappedChar() {
        CharacterReader reader = new CharacterReader("#x80;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('\u20AC'), c);
    }

    // Tests consuming null character reference (0x00) maps to replacement character
    @Test
    public void testConsumeCharacterReference_zeroCodePoint_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x00;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
    }

    // Tests consuming character reference beyond 0x10FFFF
    @Test
    public void testConsumeCharacterReference_aboveMaxCodePoint_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x110000;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
    }

    // Tests named entity in attribute when followed by '=' or alphanumeric without semicolon
    @Test
    public void testConsumeCharacterReference_inAttributeFollowedByEquals_returnsNullAndRewinds() {
        CharacterReader reader = new CharacterReader("amp=123");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, true);
        assertNull(c);
        assertEquals('a', reader.current());
    }

    // Tests emitting string tokens directly
    @Test
    public void testEmit_string_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.emit("sample string");
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("sample string", ((Token.Character) token).getData());
    }

    // Tests emitting single character
    @Test
    public void testEmit_char_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.emit('z');
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("z", ((Token.Character) token).getData());
    }

    // Tests emitting char array
    @Test
    public void testEmit_charArray_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.emit(new char[]{'f', 'o', 'o'});
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("foo", ((Token.Character) token).getData());
    }

    // Tests reading until EOF token
    @Test
    public void testRead_emptyReader_returnsEOFToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.EOF, token.type);
    }

    // Tests appropriateEndTagName method
    @Test
    public void testAppropriateEndTagName_afterStartTag_returnsTagName() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("textarea");
        tokeniser.emitTagPending();

        assertEquals("textarea", tokeniser.appropriateEndTagName());
    }

    // Tests isAppropriateEndTagToken when lastStartTag is null
    @Test
    public void testIsAppropriateEndTagToken_nullLastStartTag_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.createTagPending(false).name("div");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests createTempBuffer and currentNodeInHtmlNS
    @Test
    public void testCreateTempBufferAndCurrentNodeInHtmlNS() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Tests error logging methods execution without exceptions
    @Test
    public void testErrorMethods_executeWithoutException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.error(TokeniserState.Data);
        tokeniser.error("custom error message");
        tokeniser.eofError(TokeniserState.TagOpen);
    }
}