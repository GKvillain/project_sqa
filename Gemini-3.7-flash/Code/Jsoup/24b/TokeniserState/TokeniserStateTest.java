package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokeniserStateTest {

    private ParseErrorList errors;

    @Before
    public void setUp() {
        errors = ParseErrorList.noTracking();
    }

    // Tests Data state with plain text
    @Test
    public void testData_plainText_emitsCharacters() {
        CharacterReader r = new CharacterReader("Hello World");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.Data.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello World", ((Token.Character) token).getData());
    }

    // Tests Data state with tag open transition
    @Test
    public void testData_tagOpen_transitionsToTagOpen() {
        CharacterReader r = new CharacterReader("<div");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.Data.read(t, r);
        assertEquals(TokeniserState.TagOpen, t.getState());
    }

    // Tests Data state with character reference
    @Test
    public void testData_ampersand_transitionsToCharacterReference() {
        CharacterReader r = new CharacterReader("&amp;");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.Data.read(t, r);
        assertEquals(TokeniserState.CharacterReferenceInData, t.getState());
    }

    // Tests TagOpen state with start tag name
    @Test
    public void testTagOpen_letter_transitionsToTagName() {
        CharacterReader r = new CharacterReader("div>");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.TagOpen.read(t, r);
        assertEquals(TokeniserState.TagName, t.getState());
        assertTrue(t.isAppropriateEndTagToken() == false);
    }

    // Tests TagOpen state with end tag slash
    @Test
    public void testTagOpen_slash_transitionsToEndTagOpen() {
        CharacterReader r = new CharacterReader("/div>");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.TagOpen.read(t, r);
        assertEquals(TokeniserState.EndTagOpen, t.getState());
    }

    // Tests TagOpen state with exclamation mark
    @Test
    public void testTagOpen_exclamation_transitionsToMarkupDeclarationOpen() {
        CharacterReader r = new CharacterReader("!--comment-->");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.TagOpen.read(t, r);
        assertEquals(TokeniserState.MarkupDeclarationOpen, t.getState());
    }

    // Tests TagOpen state with question mark (bogus comment)
    @Test
    public void testTagOpen_questionMark_transitionsToBogusComment() {
        CharacterReader r = new CharacterReader("?xml version='1.0'>");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.TagOpen.read(t, r);
        assertEquals(TokeniserState.BogusComment, t.getState());
    }

    // Tests TagName state reading tag attributes transition
    @Test
    public void testTagName_whitespace_transitionsToBeforeAttributeName() {
        CharacterReader r = new CharacterReader("div class='test'>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTagPending(true);
        TokeniserState.TagName.read(t, r);
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());
    }

    // Tests TagName state closing tag immediately
    @Test
    public void testTagName_greaterThan_emitsTag() {
        CharacterReader r = new CharacterReader("b>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTagPending(true);
        TokeniserState.TagName.read(t, r);
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("b", ((Token.StartTag) token).name());
        assertEquals(TokeniserState.Data, t.getState());
    }

    // Tests Attribute parsing with quoted values
    @Test
    public void testAttribute_doubleQuotedValue_parsesCorrectly() {
        CharacterReader r = new CharacterReader("id=\"header\">");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTagPending(true);
        TokeniserState.AttributeName.read(t, r);
        assertEquals(TokeniserState.BeforeAttributeValue, t.getState());

        TokeniserState.BeforeAttributeValue.read(t, r);
        assertEquals(TokeniserState.AttributeValue_doubleQuoted, t.getState());

        TokeniserState.AttributeValue_doubleQuoted.read(t, r);
        assertEquals(TokeniserState.AfterAttributeValue_quoted, t.getState());
    }

    // Tests SelfClosingStartTag state
    @Test
    public void testSelfClosingStartTag_closing_setsSelfClosingFlag() {
        CharacterReader r = new CharacterReader("/>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTagPending(true);
        TokeniserState.TagName.read(t, new CharacterReader("img/"));
        TokeniserState.SelfClosingStartTag.read(t, new CharacterReader(">"));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertTrue(((Token.StartTag) token).isSelfClosing());
    }

    // Tests Comment parsing
    @Test
    public void testMarkupDeclarationOpen_comment_parsesComment() {
        CharacterReader r = new CharacterReader("-- a comment -->");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.MarkupDeclarationOpen.read(t, r);
        assertEquals(TokeniserState.CommentStart, t.getState());

        TokeniserState.CommentStart.read(t, r);
        assertEquals(TokeniserState.Comment, t.getState());
    }

    // Tests Doctype parsing
    @Test
    public void testMarkupDeclarationOpen_doctype_parsesDoctype() {
        CharacterReader r = new CharacterReader("DOCTYPE html>");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.MarkupDeclarationOpen.read(t, r);
        assertEquals(TokeniserState.Doctype, t.getState());

        TokeniserState.Doctype.read(t, r);
        assertEquals(TokeniserState.BeforeDoctypeName, t.getState());

        TokeniserState.BeforeDoctypeName.read(t, r);
        assertEquals(TokeniserState.DoctypeName, t.getState());

        TokeniserState.DoctypeName.read(t, r);
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertEquals("html", ((Token.Doctype) token).getName());
    }

    // Tests Rawtext state and end tag parsing
    @Test
    public void testRawtext_endTag_emitsAppropriateEndTag() {
        CharacterReader r = new CharacterReader("style data</style>");
        Tokeniser t = new Tokeniser(r, errors);
        t.transition(TokeniserState.Rawtext);

        TokeniserState.Rawtext.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("style data", ((Token.Character) token).getData());
    }

    // Tests Rcdata state with entity resolution
    @Test
    public void testRcdata_ampersand_transitionsToCharacterReference() {
        CharacterReader r = new CharacterReader("&lt;");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.Rcdata.read(t, r);
        assertEquals(TokeniserState.CharacterReferenceInRcdata, t.getState());
    }

    // Tests ScriptData escaped end tag name handling
    @Test
    public void testScriptDataEscapedEndTagName_letter_accumulatesName() {
        CharacterReader r = new CharacterReader("script>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTempBuffer();
        t.createTagPending(false);

        TokeniserState.ScriptDataEscapedEndTagName.read(t, r);
        assertEquals("script", t.dataBuffer.toString());
    }

    // Tests ScriptData double escape sequence
    @Test
    public void testScriptDataDoubleEscapeStart_script_transitionsToDoubleEscaped() {
        CharacterReader r = new CharacterReader("script>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTempBuffer();

        TokeniserState.ScriptDataDoubleEscapeStart.read(t, r);
        TokeniserState.ScriptDataDoubleEscapeStart.read(t, r);
        assertEquals(TokeniserState.ScriptDataDoubleEscaped, t.getState());
    }

    // Tests CdataSection state
    @Test
    public void testCdataSection_validCdata_emitsDataAndTransitionsToData() {
        CharacterReader r = new CharacterReader("some <cdata> content]]>trailing");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.CdataSection.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("some <cdata> content", ((Token.Character) token).getData());
        assertEquals(TokeniserState.Data, t.getState());
    }

    // Tests PLAINTEXT state
    @Test
    public void testPLAINTEXT_consumesUntilEof() {
        CharacterReader r = new CharacterReader("plain content without tags <b>test</b>");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.PLAINTEXT.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("plain content without tags <b>test</b>", ((Token.Character) token).getData());
    }
}