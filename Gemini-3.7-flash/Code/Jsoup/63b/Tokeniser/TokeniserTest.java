package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserTest {

    // Tests reading simple text characters
    @Test
    public void testRead_plainText_emitsCharacterTokens() {
        CharacterReader reader = new CharacterReader("Hello");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello", token.asCharacter().getData());
    }

    // Tests tag parsing and self-closing flag acknowledgment
    @Test
    public void testRead_selfClosingStartTag_tracksSelfClosingFlag() {
        CharacterReader reader = new CharacterReader("<img />");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token1 = tokeniser.read();
        assertTrue(token1.isStartTag());
        Token.StartTag startTag = token1.asStartTag();
        assertEquals("img", startTag.name());
        assertTrue(startTag.isSelfClosing());

        // Read next token without acknowledging self-closing flag generates error
        Token token2 = tokeniser.read();
        assertTrue(token2.isEOF());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Self closing flag not acknowledged"));
    }

    // Tests acknowledging self-closing flag suppresses error
    @Test
    public void testAcknowledgeSelfClosingFlag_selfClosingTag_noErrorOnSubsequentRead() {
        CharacterReader reader = new CharacterReader("<img />");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token1 = tokeniser.read();
        assertTrue(token1.isStartTag());
        tokeniser.acknowledgeSelfClosingFlag();

        Token token2 = tokeniser.read();
        assertTrue(token2.isEOF());
        assertEquals(0, errors.size());
    }

    // Tests appropriate end tag identification
    @Test
    public void testIsAppropriateEndTagToken_matchingTag_returnsTrue() {
        CharacterReader reader = new CharacterReader("<title>Test</title>");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        // Read <title>
        Token start = tokeniser.read();
        assertEquals("title", start.asStartTag().name());
        assertEquals("title", tokeniser.appropriateEndTagName());

        tokeniser.createTagPending(false);
        tokeniser.tagPending.name("title");
        assertTrue(tokeniser.isAppropriateEndTagToken());

        tokeniser.tagPending.name("other");
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests appropriateEndTagName when no start tag has been emitted
    @Test
    public void testAppropriateEndTagName_noStartTag_returnsNull() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertNull(tokeniser.appropriateEndTagName());
        assertFalse(tokeniser.isAppropriateEndTagToken());
    }

    // Tests unescaping named character references
    @Test
    public void testUnescapeEntities_namedEntity_unescapesCorrectly() {
        CharacterReader reader = new CharacterReader("&lt;&gt;&amp;&quot;&apos;");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        String unescaped = tokeniser.unescapeEntities(false);
        assertEquals("<>&\"'", unescaped);
    }

    // Tests unescaping decimal and hex numeric character references
    @Test
    public void testUnescapeEntities_numericEntities_unescapesCorrectly() {
        CharacterReader reader = new CharacterReader("&#65;&#x42;&#X43;");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        String unescaped = tokeniser.unescapeEntities(false);
        assertEquals("ABC", unescaped);
    }

    // Tests consuming character reference with invalid numeric format
    @Test
    public void testConsumeCharacterReference_emptyNumericEntity_returnsNullAndLogsError() {
        CharacterReader reader = new CharacterReader("#;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("numeric reference with no numerals"));
    }

    // Tests numeric character reference out of valid range
    @Test
    public void testConsumeCharacterReference_outOfRangeNumericEntity_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xFFFFFF;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, (char) result[0]);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("character outside of valid range"));
    }

    // Tests numeric character reference in surrogate pair range
    @Test
    public void testConsumeCharacterReference_surrogateRange_returnsReplacementChar() {
        CharacterReader reader = new CharacterReader("#xD800;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, (char) result[0]);
    }

    // Tests numeric character reference missing terminating semicolon
    @Test
    public void testConsumeCharacterReference_numericMissingSemicolon_logsError() {
        CharacterReader reader = new CharacterReader("#65");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(65, result[0]);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    // Tests consuming invalid named character reference
    @Test
    public void testConsumeCharacterReference_invalidNamedEntity_returnsNullAndLogsError() {
        CharacterReader reader = new CharacterReader("notanentity;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("invalid named referenece"));
    }

    // Tests named character reference in attribute context without match
    @Test
    public void testConsumeCharacterReference_inAttributeWithEqual_doesNotConsume() {
        CharacterReader reader = new CharacterReader("lt=foo");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, true);
        assertNull(result);
    }

    // Tests character reference with additional allowed character
    @Test
    public void testConsumeCharacterReference_additionalAllowedChar_returnsNull() {
        CharacterReader reader = new CharacterReader("\"");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference('\"', false);
        assertNull(result);
    }

    // Tests empty character reader
    @Test
    public void testConsumeCharacterReference_emptyReader_returnsNull() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNull(result);
    }

    // Tests emitting comment token
    @Test
    public void testEmitCommentPending_validComment_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("<!-- comment -->");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals(" comment ", token.asComment().getData());
    }

    // Tests emitting doctype token
    @Test
    public void testEmitDoctypePending_validDoctype_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html>");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
    }

    // Tests emit methods with char, char array, and string
    @Test
    public void testEmit_characterBuffers_emitsAccumulatedString() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.emit("a");
        tokeniser.emit('b');
        tokeniser.emit(new char[]{'c', 'd'});
        tokeniser.emit(new int[]{0x65}); // 'e'

        // Force emit of character buffer via read when pending token is set
        tokeniser.emit(new Token.EOF());
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("abcde", token.asCharacter().getData());
    }

    // Tests state transition helpers
    @Test
    public void testTransition_stateManagement_updatesStateCorrectly() {
        CharacterReader reader = new CharacterReader("abc");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertEquals(TokeniserState.Data, tokeniser.getState());
        tokeniser.transition(TokeniserState.TagOpen);
        assertEquals(TokeniserState.TagOpen, tokeniser.getState());

        tokeniser.advanceTransition(TokeniserState.TagName);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        assertEquals('b', reader.current());
    }

    // Tests error logging methods
    @Test
    public void testError_loggingMethods_addErrorsToList() {
        CharacterReader reader = new CharacterReader("test");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        tokeniser.error(TokeniserState.Data);
        tokeniser.eofError(TokeniserState.Data);
        tokeniser.error("Custom error");

        assertEquals(3, errors.size());
    }

    // Tests end tag with attributes triggers error
    @Test
    public void testEmit_endTagWithAttributes_logsError() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.attributes.put("class", "foo");

        tokeniser.emit(endTag);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("Attributes incorrectly present on end tag"));
    }

    // Tests double emit without read throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testEmit_doubleEmitWithoutRead_throwsException() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        Token.StartTag tag1 = new Token.StartTag();
        tag1.name("div");
        Token.StartTag tag2 = new Token.StartTag();
        tag2.name("span");

        tokeniser.emit(tag1);
        tokeniser.emit(tag2);
    }

    // Tests win1252 character reference mapping and error tracking
    @Test
    public void testConsumeCharacterReference_win1252Entity_mapsToCorrectCharAndLogsError() {
        CharacterReader reader = new CharacterReader("#x80;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(0x20AC, result[0]); // Euro symbol
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("character reference in win1252 range"));
    }

    // Tests zero character reference replacement and error tracking
    @Test
    public void testConsumeCharacterReference_zeroCharRef_mapsToReplacementCharAndLogsError() {
        CharacterReader reader = new CharacterReader("#0;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(Tokeniser.replacementChar, (char) result[0]);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("character reference was zero"));
    }

    // Tests control characters and noncharacter code points in numeric character reference
    @Test
    public void testConsumeCharacterReference_controlAndNonCharacterCodePoints_logsError() {
        CharacterReader reader = new CharacterReader("#x1F;");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(0x1F, result[0]);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("character reference in range of invalid characters"));

        CharacterReader nonCharReader = new CharacterReader("#xFDD0;");
        Tokeniser nonCharTokeniser = new Tokeniser(nonCharReader, errors);
        int[] nonCharResult = nonCharTokeniser.consumeCharacterReference(null, false);
        assertNotNull(nonCharResult);
        assertEquals(0xFDD0, nonCharResult[0]);
        assertEquals(2, errors.size());
    }

    // Tests consuming supplementary character reference (surrogate pair)
    @Test
    public void testConsumeCharacterReference_supplementaryCodePoint_returnsSurrogates() {
        CharacterReader reader = new CharacterReader("#x10000;");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Character.toCodePoint((char) result[0], (char) result[1]), 0x10000);
    }

    // Tests named character reference missing semicolon logs error
    @Test
    public void testConsumeCharacterReference_namedEntityMissingSemicolon_logsError() {
        CharacterReader reader = new CharacterReader("amp ");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        int[] result = tokeniser.consumeCharacterReference(null, false);
        assertNotNull(result);
        assertEquals('&', (char) result[0]);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getErrorMessage().contains("missing semicolon"));
    }

    // Tests characters that immediately cause consumeCharacterReference to return null
    @Test
    public void testConsumeCharacterReference_ignoredLeadingChars_returnsNull() {
        ParseErrorList errors = ParseErrorList.noTracking();

        Tokeniser tokeniserSpace = new Tokeniser(new CharacterReader(" "), errors);
        assertNull(tokeniserSpace.consumeCharacterReference(null, false));

        Tokeniser tokeniserLt = new Tokeniser(new CharacterReader("<"), errors);
        assertNull(tokeniserLt.consumeCharacterReference(null, false));

        Tokeniser tokeniserAmp = new Tokeniser(new CharacterReader("&"), errors);
        assertNull(tokeniserAmp.consumeCharacterReference(null, false));
    }

    // Tests manual creation of pending tokens and html namespace check
    @Test
    public void testPendingTokenHelpers_andHtmlNamespaceCheck() {
        CharacterReader reader = new CharacterReader("");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        assertTrue(tokeniser.currentNodeInHtmlNS());

        tokeniser.createDoctypePending();
        assertNotNull(tokeniser.doctypePending);

        tokeniser.createCommentPending();
        assertNotNull(tokeniser.commentPending);

        tokeniser.createTagPending(true);
        assertNotNull(tokeniser.tagPending);
        assertTrue(tokeniser.tagPending instanceof Token.StartTag);

        tokeniser.createTagPending(false);
        assertNotNull(tokeniser.tagPending);
        assertTrue(tokeniser.tagPending instanceof Token.EndTag);
    }

    // Tests unescapeEntities in attribute mode
    @Test
    public void testUnescapeEntities_inAttributeMode_handlesEntitiesCorrectly() {
        CharacterReader reader = new CharacterReader("&amp;test&amp=123");
        ParseErrorList errors = ParseErrorList.noTracking();
        Tokeniser tokeniser = new Tokeniser(reader, errors);

        String unescaped = tokeniser.unescapeEntities(true);
        assertEquals("&test&amp=123", unescaped);
    }
}