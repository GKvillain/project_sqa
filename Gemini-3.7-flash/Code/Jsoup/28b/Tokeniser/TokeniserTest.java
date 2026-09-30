package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests consumeCharacterReference with a valid decimal numeric entity
    @Test
    public void testConsumeCharacterReference_decimalEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#65;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('A'), result);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference with a valid hexadecimal numeric entity
    @Test
    public void testConsumeCharacterReference_hexEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#x41;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('A'), result);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference with numeric entity missing semicolon
    @Test
    public void testConsumeCharacterReference_missingSemicolon_recordsErrorAndReturnsCharacter() {
        CharacterReader reader = new CharacterReader("#65");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('A'), result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with empty numeric reference
    @Test
    public void testConsumeCharacterReference_emptyNumeric_returnsNull() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with numeric entity outside valid range
    @Test
    public void testConsumeCharacterReference_outOfRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#x110000;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with surrogate range numeric entity
    @Test
    public void testConsumeCharacterReference_surrogateRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with a valid named entity
    @Test
    public void testConsumeCharacterReference_namedEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("lt;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('<'), result);
        assertTrue(errors.isEmpty());
    }

    // Tests consumeCharacterReference when reader is empty
    @Test
    public void testConsumeCharacterReference_emptyReader_returnsNull() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
    }

    // Tests consumeCharacterReference when next char matches additionalAllowedCharacter
    @Test
    public void testConsumeCharacterReference_additionalAllowedChar_returnsNull() {
        CharacterReader reader = new CharacterReader("\"rest");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference('\"', true);
        assertNull(result);
    }

    // Tests consumeCharacterReference when next char is invalid leading char
    @Test
    public void testConsumeCharacterReference_invalidLeadingChar_returnsNull() {
        CharacterReader reader = new CharacterReader(" text");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
    }

    // Tests consumeCharacterReference with invalid named entity
    @Test
    public void testConsumeCharacterReference_invalidNamedEntity_returnsNull() {
        CharacterReader reader = new CharacterReader("notanentity;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference in attribute with trailing letter/digit/equal
    @Test
    public void testConsumeCharacterReference_inAttributeWithTrailingEqual_returnsNull() {
        CharacterReader reader = new CharacterReader("lt=value");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, true);
        assertNull(result);
    }

    // Tests emit string and character buffering
    @Test
    public void testEmit_stringAndCharBuffer_buffersProperly() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.emit("Hello");
        tokeniser.emit(' ');
        tokeniser.emit("World");

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("Hello World", ((Token.Character) token).getData());
    }

    // Tests state transitions
    @Test
    public void testTransition_stateChange_updatesState() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(TokeniserState.Data, tokeniser.getState());

        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    // Tests tag creation, emit and end tag checking
    @Test
    public void testTagPending_createAndEmit_tracksAppropriateEndTag() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.tagName = "div";
        tokeniser.emitTagPending();

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.tagName = "div";

        assertTrue(tokeniser.isAppropriateEndTagToken());
        assertEquals("div", tokeniser.appropriateEndTagName());

        endTag.tagName = "span";
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests comment creation and emission
    @Test
    public void testCommentPending_createAndEmit_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("test comment");
        tokeniser.emitCommentPending();

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Comment);
        assertEquals("test comment", ((Token.Comment) token).getData());
    }

    // Tests doctype creation and emission
    @Test
    public void testDoctypePending_createAndEmit_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Doctype);
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests unacknowledged self closing tag flag creates error
    @Test
    public void testSelfClosingFlag_unacknowledged_createsErrorOnNextRead() {
        CharacterReader reader = new CharacterReader("text");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.Tag startTag = tokeniser.createTagPending(true);
        startTag.selfClosing = true;
        startTag.tagName = "img";
        tokeniser.emitTagPending();

        Token firstToken = tokeniser.read();
        assertTrue(firstToken instanceof Token.StartTag);

        tokeniser.read();
        assertEquals(1, errors.size());
    }

    // Tests error logging methods and currentNodeInHtmlNS
    @Test
    public void testErrorAndNamespaceMethods() {
        CharacterReader reader = new CharacterReader("a");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        assertEquals(2, errors.size());

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);
        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Tests consumeCharacterReference with null character reference (#0;)
    @Test
    public void testConsumeCharacterReference_nullChar_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#0;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with Win1252 mapped entity (#x80;)
    @Test
    public void testConsumeCharacterReference_win1252Entity_returnsMappedChar() {
        CharacterReader reader = new CharacterReader("#x80;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('\u20AC'), result);
        assertEquals(1, errors.size());
    }

    // Tests consumeCharacterReference with named entity missing semicolon in normal text
    @Test
    public void testConsumeCharacterReference_namedEntityMissingSemicolon_recordsErrorAndReturnsChar() {
        CharacterReader reader = new CharacterReader("amp ");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Character result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Character.valueOf('&'), result);
        assertEquals(1, errors.size());
    }

    // Tests emit with char array
    @Test
    public void testEmit_charArray_buffersProperly() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.emit(new char[]{'f', 'o', 'o'});
        Token token = tokeniser.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("foo", ((Token.Character) token).getData());
    }

    // Tests error with custom message
    @Test
    public void testError_withCustomMessage_recordsError() {
        CharacterReader reader = new CharacterReader("a");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error("custom error message");
        assertEquals(1, errors.size());
        assertEquals("custom error message", errors.get(0).getErrorMessage());
    }

    // Tests isAppropriateEndTagToken when no start tag has been emitted
    @Test
    public void testAppropriateEndTagToken_noStartTag_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.Tag endTag = tokeniser.createTagPending(false);
        endTag.tagName = "div";

        assertFalse(tokeniser.isAppropriateEndTagToken());
        assertNull(tokeniser.appropriateEndTagName());
    }

    // Tests reading until EOF returns EOF token
    @Test
    public void testRead_atEof_returnsEofToken() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertTrue(token instanceof Token.EOF);
    }
}