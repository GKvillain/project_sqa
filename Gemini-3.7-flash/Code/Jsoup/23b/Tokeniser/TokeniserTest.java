package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests consumeCharacterReference with standard named entity
    @Test
    public void testConsumeCharacterReference_namedEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("lt;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('<'), c);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference with decimal numeric entity
    @Test
    public void testConsumeCharacterReference_decimalNumericEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#65;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference with hex numeric entity
    @Test
    public void testConsumeCharacterReference_hexNumericEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#x41;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference with numeric entity missing semicolon
    @Test
    public void testConsumeCharacterReference_numericMissingSemicolon_recordsError() {
        CharacterReader reader = new CharacterReader("#65");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with invalid numeric reference having no numerals
    @Test
    public void testConsumeCharacterReference_numericWithNoNumerals_returnsNullAndRecordsError() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with character outside valid unicode range
    @Test
    public void testConsumeCharacterReference_characterOutOfRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x110000;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with surrogate range character
    @Test
    public void testConsumeCharacterReference_surrogateRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference when reader is empty or matches disallowed leading chars
    @Test
    public void testConsumeCharacterReference_disallowedLeadingChar_returnsNull() {
        CharacterReader reader = new CharacterReader("<foo");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);

        CharacterReader reader2 = new CharacterReader("=foo");
        Tokeniser tokeniser2 = new Tokeniser(reader2, ParseErrorList.noTracking());
        assertNull(tokeniser2.consumeCharacterReference('=', false));
    }

    // Tests consumeCharacterReference in attribute when followed by invalid char
    @Test
    public void testConsumeCharacterReference_inAttributeFollowedByLetter_returnsNull() {
        CharacterReader reader = new CharacterReader("lt=value");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Character c = tokeniser.consumeCharacterReference(null, true);
        assertNull(c);
    }

    // Tests consumeCharacterReference with invalid named reference
    @Test
    public void testConsumeCharacterReference_invalidNamedEntityWithSemicolon_returnsNullAndRecordsError() {
        CharacterReader reader = new CharacterReader("notanentity;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
        assertEquals(1, errors.size());
    }

    // Tests read method tokenizing simple HTML characters and tags
    @Test
    public void testRead_simpleData_returnsTokens() {
        CharacterReader reader = new CharacterReader("<p>Hello</p>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token startTag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, startTag.type);
        assertEquals("p", ((Token.StartTag) startTag).name());

        Token charToken = tokeniser.read();
        assertEquals(Token.TokenType.Character, charToken.type);
        assertEquals("Hello", ((Token.Character) charToken).getData());

        Token endTag = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, endTag.type);
        assertEquals("p", ((Token.EndTag) endTag).name());

        Token eofToken = tokeniser.read();
        assertEquals(Token.TokenType.EOF, eofToken.type);
    }

    // Tests emitPending validation exception when emitting twice without read
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_duplicatePendingToken_throwsException() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token.StartTag tag1 = new Token.StartTag();
        tag1.name("div");
        Token.StartTag tag2 = new Token.StartTag();
        tag2.name("span");

        tokeniser.emit(tag1);
        tokeniser.emit(tag2);
    }

    // Tests self-closing flag acknowledgment warning
    @Test
    public void testRead_selfClosingStartTagNotAcknowledged_recordsError() {
        CharacterReader reader = new CharacterReader("<img/>");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token tag = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, tag.type);
        assertTrue(((Token.StartTag) tag).isSelfClosing());

        // Read next token without acknowledging self closing flag
        tokeniser.read();
        assertTrue(errors.size() > 0);
    }

    // Tests state transition and advanceTransition
    @Test
    public void testStateTransition_advanceTransition_updatesStateAndPosition() {
        CharacterReader reader = new CharacterReader("abc");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.advanceTransition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());
        assertEquals('b', reader.current());

        tokeniser.transition(TokeniserState.Data);
        assertEquals(TokeniserState.Data, tokeniser.getState());
    }

    // Tests comment creation and emitting
    @Test
    public void testCommentPending_createAndEmit_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("test comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token.type);
        assertEquals("test comment", ((Token.Comment) token).getData());
    }

    // Tests doctype creation and emitting
    @Test
    public void testDoctypePending_createAndEmit_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, token.type);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests isAppropriateEndTagToken and appropriateEndTagName
    @Test
    public void testIsAppropriateEndTagToken_matchesStartTag_returnsTrue() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token.StartTag startTag = (Token.StartTag) tokeniser.createTagPending(true);
        startTag.name("script");
        tokeniser.emitTagPending();

        // Read to consume pending start tag and set lastStartTag
        tokeniser.read();

        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.name("script");
        assertEquals("script", tokeniser.appropriateEndTagName());
        assertTrue(tokeniser.isAppropriateEndTagToken());

        endTag.name("div");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests error and eofError methods
    @Test
    public void testErrorMethods_withTracking_recordsParseErrors() {
        CharacterReader reader = new CharacterReader("a");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        assertEquals(2, errors.size());
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Tests createTempBuffer and emit single char / string
    @Test
    public void testEmitCharAndString_buffersCorrectly() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);

        tokeniser.emit('H');
        tokeniser.emit("ello");

        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.name("p");
        tokeniser.emitTagPending();

        Token token1 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token1.type);
        assertEquals("Hello", ((Token.Character) token1).getData());

        Token token2 = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, token2.type);
    }
}