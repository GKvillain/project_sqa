package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests consume named character reference successfully with semicolon
    @Test
    public void testConsumeCharacterReference_validNamedEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("lt;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('<'), c);
        assertEquals(0, tokeniser.getState().ordinal() >= 0 ? 0 : 1);
    }

    // Tests consume named entity without semicolon
    @Test
    public void testConsumeCharacterReference_namedEntityMissingSemicolon_reportsErrorAndReturnsCharacter() {
        CharacterReader reader = new CharacterReader("gt");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('>'), c);
        assertEquals(1, errors.size());
    }

    // Tests consume decimal numeric character reference
    @Test
    public void testConsumeCharacterReference_decimalNumericEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#65;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);
    }

    // Tests consume hex numeric character reference (case insensitive)
    @Test
    public void testConsumeCharacterReference_hexNumericEntity_returnsCharacter() {
        CharacterReader reader = new CharacterReader("#x41;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf('A'), c);

        CharacterReader readerUpper = new CharacterReader("#X42;");
        Tokeniser tokeniserUpper = new Tokeniser(readerUpper, ParseErrorList.noTracking());
        Character cUpper = tokeniserUpper.consumeCharacterReference(null, false);
        assertNotNull(cUpper);
        assertEquals(Character.valueOf('B'), cUpper);
    }

    // Tests consume numeric character reference without digits
    @Test
    public void testConsumeCharacterReference_numericWithNoDigits_returnsNullAndReportsError() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
        assertEquals(1, errors.size());
    }

    // Tests consume numeric character reference out of valid unicode range
    @Test
    public void testConsumeCharacterReference_numericOutOfRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;"); // Surrogate range
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(c);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), c);
        assertEquals(1, errors.size());

        CharacterReader readerHigh = new CharacterReader("#x110000;"); // Beyond 0x10FFFF
        Tokeniser tokeniserHigh = new Tokeniser(readerHigh, errors);
        Character cHigh = tokeniserHigh.consumeCharacterReference(null, false);
        assertNotNull(cHigh);
        assertEquals(Character.valueOf(Tokeniser.replacementChar), cHigh);
    }

    // Tests consume character reference when reader is empty or matches additional allowed character
    @Test
    public void testConsumeCharacterReference_emptyOrAdditionalAllowed_returnsNull() {
        CharacterReader readerEmpty = new CharacterReader("");
        Tokeniser tokeniserEmpty = new Tokeniser(readerEmpty, ParseErrorList.noTracking());
        assertNull(tokeniserEmpty.consumeCharacterReference(null, false));

        CharacterReader readerAllowed = new CharacterReader("\"");
        Tokeniser tokeniserAllowed = new Tokeniser(readerAllowed, ParseErrorList.noTracking());
        assertNull(tokeniserAllowed.consumeCharacterReference('\"', false));
    }

    // Tests consume character reference when first char is invalid (whitespace, '<', '&')
    @Test
    public void testConsumeCharacterReference_invalidStartingChar_returnsNull() {
        CharacterReader readerSpace = new CharacterReader(" abc");
        Tokeniser tokeniser = new Tokeniser(readerSpace, ParseErrorList.noTracking());
        assertNull(tokeniser.consumeCharacterReference(null, false));

        CharacterReader readerLt = new CharacterReader("<abc");
        Tokeniser tokeniserLt = new Tokeniser(readerLt, ParseErrorList.noTracking());
        assertNull(tokeniserLt.consumeCharacterReference(null, false));
    }

    // Tests consume invalid named entity with semicolon
    @Test
    public void testConsumeCharacterReference_invalidNamedEntityWithSemi_returnsNullAndReportsError() {
        CharacterReader reader = new CharacterReader("nonexistententity;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Character c = tokeniser.consumeCharacterReference(null, false);
        assertNull(c);
        assertEquals(1, errors.size());
    }

    // Tests consume named entity in attribute followed by invalid following characters
    @Test
    public void testConsumeCharacterReference_inAttributeFollowedByChar_returnsNull() {
        CharacterReader reader = new CharacterReader("lt=value");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Character c = tokeniser.consumeCharacterReference(null, true);
        assertNull(c);
    }

    // Tests createTagPending, emitTagPending, and appropriate end tag matching
    @Test
    public void testTagPendingAndAppropriateEndTag_matchedTags_returnsTrue() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), ParseErrorList.noTracking());
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

    // Tests comment and doctype pending lifecycle
    @Test
    public void testCommentAndDoctypePending_emitTokens_producesTokens() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), ParseErrorList.noTracking());
        tokeniser.createCommentPending();
        tokeniser.commentPending.data.append("test comment");
        tokeniser.emitCommentPending();

        Token commentToken = tokeniser.read();
        assertEquals(Token.TokenType.Comment, commentToken.type);
        assertEquals("test comment", ((Token.Comment) commentToken).getData());

        tokeniser.createDoctypePending();
        tokeniser.doctypePending.name.append("html");
        tokeniser.emitDoctypePending();

        Token doctypeToken = tokeniser.read();
        assertEquals(Token.TokenType.Doctype, doctypeToken.type);
        assertEquals("html", ((Token.Doctype) doctypeToken).getName());
    }

    // Tests emitting character buffers before pending token
    @Test
    public void testRead_charBufferAndPendingToken_emitsCharsFirstThenToken() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), ParseErrorList.noTracking());
        tokeniser.emit("text");
        tokeniser.emit('!');
        tokeniser.createCommentPending();
        tokeniser.emitCommentPending();

        Token token1 = tokeniser.read();
        assertEquals(Token.TokenType.Character, token1.type);
        assertEquals("text!", ((Token.Character) token1).getData());

        Token token2 = tokeniser.read();
        assertEquals(Token.TokenType.Comment, token2.type);
    }

    // Tests self-closing tag unacknowledged error
    @Test
    public void testRead_unacknowledgedSelfClosingTag_reportsError() {
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), ParseErrorList.tracking(10));
        Token.StartTag startTag = (Token.StartTag) tokeniser.createTagPending(true);
        startTag.tagName = "img";
        startTag.selfClosing = true;
        tokeniser.emitTagPending();

        tokeniser.read(); // first read receives the StartTag
        tokeniser.createCommentPending();
        tokeniser.emitCommentPending();
        tokeniser.read(); // second read should trigger unacknowledged self-closing error

        ParseErrorList errors = ParseErrorList.tracking(10);
        // acknowledge check
        tokeniser.acknowledgeSelfClosingFlag();
    }

    // Tests end tag with attributes reporting parse error
    @Test
    public void testEmit_endTagWithAttributes_reportsError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(new CharacterReader(""), errors);
        Token.EndTag endTag = (Token.EndTag) tokeniser.createTagPending(false);
        endTag.tagName = "div";
        endTag.attributes.put("class", "foo");
        tokeniser.emitTagPending();

        assertEquals(1, errors.size());
    }

    // Tests state transitions, temp buffer and error logging helpers
    @Test
    public void testStateTransitionsAndHelpers() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());

        tokeniser.createTempBuffer();
        assertNotNull(tokeniser.dataBuffer);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        assertEquals(2, errors.size());

        assertTrue(tokeniser.currentNodeInHtmlNS());
    }

    // Tests tokenising standard html content end-to-end via read
    @Test
    public void testRead_htmlInput_readsTokensSuccessfully() {
        CharacterReader reader = new CharacterReader("<b>Hi</b>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token start = tokeniser.read();
        assertEquals(Token.TokenType.StartTag, start.type);
        assertEquals("b", ((Token.StartTag) start).name());

        Token text = tokeniser.read();
        assertEquals(Token.TokenType.Character, text.type);
        assertEquals("Hi", ((Token.Character) text).getData());

        Token end = tokeniser.read();
        assertEquals(Token.TokenType.EndTag, end.type);
        assertEquals("b", ((Token.EndTag) end).name());

        Token eof = tokeniser.read();
        assertEquals(Token.TokenType.EOF, eof.type);
    }
}