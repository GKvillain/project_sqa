package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests reading simple character data
    @Test
    public void testRead_simpleCharacterData_emitsCharacterTokens() {
        CharacterReader reader = new CharacterReader("hello");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("hello", ((Token.Character) token).getData());
    }

    // Tests reading tag tokens
    @Test
    public void testRead_startAndEndTag_emitsTagsCorrectly() {
        CharacterReader reader = new CharacterReader("<p>text</p>");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token t1 = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, t1.type);
        assertEquals("p", ((Token.StartTag) t1).name());

        Token t2 = tokeniser.read();
        assertEquals(Token.TokenType.Character, t2.type);
        assertEquals("text", ((Token.Character) t2).getData());

        Token t3 = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, t3.type);
        assertEquals("p", ((Token.EndTag) t3).name());

        Token t4 = tokeniser.read();
        assertEquals(Token.TokenType.EOF, t4.type);
    }

    // Tests reading a comment token
    @Test
    public void testRead_comment_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("<!-- a comment -->");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals(" a comment ", ((Token.Comment) token).getData());
    }

    // Tests reading a doctype token
    @Test
    public void testRead_doctype_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html>");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests state transitions and getters
    @Test
    public void testTransition_stateChange_updatesState() {
        CharacterReader reader = new CharacterReader("test");
        Tokeniser tokeniser = new Tokeniser(reader);
        assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('e', reader.current());
    }

    // Tests consume named character reference
    @Test
    public void testConsumeCharacterReference_namedEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("amp;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('&'), c);
    }

    // Tests consume decimal character reference
    @Test
    public void testConsumeCharacterReference_decimalEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#65;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consume hex character reference
    @Test
    public void testConsumeCharacterReference_hexEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#x42;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('B'), c);
    }

    // Tests consume character reference with invalid prefix
    @Test
    public void testConsumeCharacterReference_invalidEntity_returnsNull() {
        CharacterReader reader = new CharacterReader("#xyz;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests consume character reference on empty reader
    @Test
    public void testConsumeCharacterReference_emptyReader_returnsNull() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
    }

    // Tests consume character reference matching additional allowed character
    @Test
    public void testConsumeCharacterReference_additionalAllowedChar_returnsNull() {
        CharacterReader reader = new CharacterReader("\"");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference('\"', true);
        assertNull(c);
    }

    // Tests consume character reference in attribute followed by equal sign
    @Test
    public void testConsumeCharacterReference_inAttributeWithEquals_returnsNull() {
        CharacterReader reader = new CharacterReader("notanentity=123");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, true);
        assertNull(c);
    }

    // Tests consume out-of-range character reference returning replacement character
    @Test
    public void testConsumeCharacterReference_outOfRangeCodePoint_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
    }

    // Tests tag pending creation and appropriate end tag check
    @Test
    public void testIsAppropriateEndTagToken_matchingAndMismatchedTags_returnsExpected() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("div");
        tokeniser.emitTagPending();

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.name("div");
        assertTrue(tokeniser.isAppropriateEndTagToken());

        endTag.name("span");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests acknowledge self closing flag
    @Test
    public void testAcknowledgeSelfClosingFlag_unacknowledgedFlag_resetsState() {
        CharacterReader reader = new CharacterReader("<img />");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token token = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, token.type);
        assertTrue(((Token.StartTag) token).isSelfClosing());

        tokeniser.acknowledgeSelfClosingFlag();
        Token nextToken = tokeniser.read();
        assertEquals(Token.TokenType.EOF, nextToken.type);
    }

    // Tests error tracking flag
    @Test
    public void testTrackErrors_toggleFlag_updatesTrackErrors() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        assertTrue(tokeniser.isTrackErrors());

        tokeniser.setTrackErrors(false);
        assertFalse(tokeniser.isTrackErrors());

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
    }

    // Tests temp data buffer creation and html namespace check
    @Test
    public void testCreateTempBufferAndCurrentNodeInHtmlNS_basic_success() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Tests emitPending conflict throwing exception
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_duplicatePendingToken_throwsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.emit(new Token.Character("a"));
        tokeniser.emit(new Token.Character("b"));
    }

    // Tests emit overloads (String, char[], char, int[])
    @Test
    public void testEmit_variousTypes_emitsCharactersCorrectly() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.emit("sample");
        Token token1 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token1.type);
        assertEquals("sample", ((Token.Character) token1).getData());

        tokeniser.emit(new char[]{'a', 'b', 'c'});
        Token token2 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token2.type);
        assertEquals("abc", ((Token.Character) token2).getData());

        tokeniser.emit('z');
        Token token3 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token3.type);
        assertEquals("z", ((Token.Character) token3).getData());

        tokeniser.emit(new int[]{0x1F600});
        Token token4 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token4.type);
        assertEquals(new String(Character.toChars(0x1F600)), ((Token.Character) token4).getData());
    }

    // Tests createCommentPending and emitCommentPending
    @Test
    public void testCommentPending_lifecycle_createsAndEmitsComment() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.createCommentPending();
        tokeniser.commentPending.append("manual comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals("manual comment", ((Token.Comment) token).getData());
    }

    // Tests createDoctypePending and emitDoctypePending
    @Test
    public void testDoctypePending_lifecycle_createsAndEmitsDoctype() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.doctypePending.forceQuirks = true;
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertTrue(doctype.isForceQuirks());
    }

    // Tests appropriateEndTagName method
    @Test
    public void testAppropriateEndTagName_withAndWithoutLastStartTag_returnsCorrectName() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);

        assertNull(tokeniser.appropriateEndTagName());

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.name("textarea");
        tokeniser.emitTagPending();

        assertEquals("textarea", tokeniser.appropriateEndTagName());
    }

    // Tests consume windows-1252 character reference mapping
    @Test
    public void testConsumeCharacterReference_windows1252Entity_mapsCorrectly() {
        CharacterReader reader = new CharacterReader("#x80;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('\u20AC'), c);
    }

    // Tests consume numeric character reference without closing semicolon
    @Test
    public void testConsumeCharacterReference_missingSemicolon_tracksErrorAndConsumes() {
        CharacterReader reader = new CharacterReader("#65 ");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consume numeric character reference with code point > 0x10FFFF
    @Test
    public void testConsumeCharacterReference_excessiveCodePoint_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x110000;");
        Tokeniser tokeniser = new Tokeniser(reader);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
    }

    // Tests unacknowledged self-closing tag emits error on next read
    @Test
    public void testRead_unacknowledgedSelfClosingFlag_emitsErrorOnNextRead() {
        CharacterReader reader = new CharacterReader("<div/><span></span>");
        Tokeniser tokeniser = new Tokeniser(reader);
        Token t1 = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, t1.type);
        assertTrue(((Token.StartTag) t1).isSelfClosing());

        // Read next token without acknowledging self closing flag
        Token t2 = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, t2.type);
        assertEquals("span", ((Token.StartTag) t2).name());
    }

    // Tests custom error and character reference error methods
    @Test
    public void testCustomErrors_explicitCalls_invokesWithoutException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        tokeniser.error("custom error message");
        tokeniser.characterReferenceError("character ref error message");
    }

    // Tests appropriateEndTagToken check when tagPending is null
    @Test
    public void testIsAppropriateEndTagToken_nullTagPending_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader);
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }
}