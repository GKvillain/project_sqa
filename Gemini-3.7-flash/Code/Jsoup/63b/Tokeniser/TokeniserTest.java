package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests reading simple text tokens and verifying output data
    @Test
    public void testRead_plainText_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("Hello");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Character, token.type);
        assertEquals("Hello", ((Token.Character) token).getData());
    }

    // Tests self-closing flag acknowledgment prevents unacknowledged error
    @Test
    public void testRead_selfClosingFlagAcknowledged_noErrorEmitted() {
        CharacterReader reader = new CharacterReader("<img />");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token startTag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertTrue(((Token.StartTag) startTag).isSelfClosing());

        tokeniser.acknowledgeSelfClosingFlag();
        tokeniser.read(); // EOF token

        assertTrue(errors.isEmpty());
    }

    // Tests unacknowledged self-closing flag records an error on subsequent read
    @Test
    public void testRead_selfClosingFlagNotAcknowledged_recordsError() {
        CharacterReader reader = new CharacterReader("<img />");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token startTag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertTrue(((Token.StartTag) startTag).isSelfClosing());

        // Next read without acknowledgeSelfClosingFlag() should trigger error
        tokeniser.read();

        assertFalse(errors.isEmpty());
        assertEquals("Self closing flag not acknowledged", errors.get(0).getErrorMessage());
    }

    // Tests emit when a token is already pending throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_unreadTokenPending_throwsException() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag tag1 = new Token.StartTag();
        tag1.name("div");
        Token.StartTag tag2 = new Token.StartTag();
        tag2.name("p");

        tokeniser.emit(tag1);
        tokeniser.emit(tag2);
    }

    // Tests emit end tag with attributes records an error
    @Test
    public void testEmit_endTagWithAttributes_recordsError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("p");
        endTag.attributes.put("class", "error");

        tokeniser.emit(endTag);

        assertEquals(1, errors.size());
        assertEquals("Attributes incorrectly present on end tag", errors.get(0).getErrorMessage());
    }

    // Tests consumeCharacterReference with valid named entity
    @Test
    public void testConsumeCharacterReference_validNamedEntity_returnsCodePoint() {
        CharacterReader reader = new CharacterReader("lt;rest");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNotNull(codepoints);
        assertEquals(1, codepoints.length);
        assertEquals('<', codepoints[0]);
        assertEquals("rest", reader.current() + reader.consumeToEnd().substring(1));
    }

    // Tests consumeCharacterReference with decimal numeric entity
    @Test
    public void testConsumeCharacterReference_decimalNumericEntity_returnsCodePoint() {
        CharacterReader reader = new CharacterReader("#65;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNotNull(codepoints);
        assertEquals(1, codepoints.length);
        assertEquals(65, codepoints[0]);
    }

    // Tests consumeCharacterReference with hex numeric entity
    @Test
    public void testConsumeCharacterReference_hexNumericEntity_returnsCodePoint() {
        CharacterReader reader = new CharacterReader("#x41;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNotNull(codepoints);
        assertEquals(1, codepoints.length);
        assertEquals(0x41, codepoints[0]);
    }

    // Tests consumeCharacterReference missing numerals in numeric reference
    @Test
    public void testConsumeCharacterReference_numericWithoutNumerals_returnsNullAndRecordsError() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNull(codepoints);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
    }

    // Tests consumeCharacterReference with out of range numeric entity
    @Test
    public void testConsumeCharacterReference_outOfRangeNumeric_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x110000;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNotNull(codepoints);
        assertEquals(Tokeniser.replacementChar, (char) codepoints[0]);
        assertFalse(errors.isEmpty());
    }

    // Tests consumeCharacterReference in attribute context matching invalid characters
    @Test
    public void testConsumeCharacterReference_inAttributeWithSuffix_returnsNull() {
        CharacterReader reader = new CharacterReader("notanentity=value");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, true);

        assertNull(codepoints);
    }

    // Tests consumeCharacterReference with invalid named reference with semicolon
    @Test
    public void testConsumeCharacterReference_invalidNamedEntityWithSemi_recordsError() {
        CharacterReader reader = new CharacterReader("fakeentity;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] codepoints = tokeniser.consumeCharacterReference(null, false);

        assertNull(codepoints);
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).getErrorMessage().contains("invalid named"));
    }

    // Tests unescapeEntities unescapes named and numeric entities
    @Test
    public void testUnescapeEntities_stringWithEntities_returnsUnescapedString() {
        CharacterReader reader = new CharacterReader("One &amp; Two &#60; Three");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        String result = tokeniser.unescapeEntities(false);

        assertEquals("One & Two < Three", result);
    }

    // Tests state transitions and getState
    @Test
    public void testTransition_changesCurrentState() {
        CharacterReader reader = new CharacterReader("test");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
    }

    // Tests advanceTransition consumes character and changes state
    @Test
    public void testAdvanceTransition_consumesCharAndChangesState() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals('a', reader.current());
        tokeniser.advanceTransition(TokeniserState.Rawtext);
        assertEquals(TokeniserState.Rawtext, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    // Tests isAppropriateEndTagToken and appropriateEndTagName
    @Test
    public void testIsAppropriateEndTagToken_matchesLastStartTag() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.appropriateEndTagName());
        assertFalse(tokeniser.isAppropriateEndTagToken());

        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        tokeniser.emit(startTag);

        assertEquals("div", tokeniser.appropriateEndTagName());

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.name("div");
        assertTrue(tokeniser.isAppropriateEndTagToken());

        endTag.name("span");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests error logging methods
    @Test
    public void testError_recordsParseErrorsCorrectly() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.TagOpen);
        tokeniser.error("Custom error");

        assertEquals(3, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Unexpected character"));
        assertTrue(errors.get(1).getErrorMessage().contains("Unexpectedly reached end of file"));
        assertEquals("Custom error", errors.get(2).getErrorMessage());
    }

    // Tests doctype pending creation and emit
    @Test
    public void testDoctypePending_creationAndEmit() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html>");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests comment pending creation and emit
    @Test
    public void testCommentPending_creationAndEmit() {
        CharacterReader reader = new CharacterReader("<!-- a comment -->");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals(" a comment ", ((Token.Comment) token).getData());
    }

    // Tests currentNodeInHtmlNS returns true by default
    @Test
    public void testCurrentNodeInHtmlNS_returnsTrue() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertTrue(tokeniser.currentNodeInHtmlNS());
    }
}