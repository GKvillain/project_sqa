package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    // Tests reading standard character data in Data state
    @Test
    public void testData_standardCharacters_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("Hello world");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello world", token.asCharacter().getData());
    }

    // Tests character reference transition from Data state
    @Test
    public void testData_characterReference_decodesAndEmitsCharacter() {
        CharacterReader reader = new CharacterReader("&amp;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("&", token.asCharacter().getData());
    }

    // Tests TagOpen state branching to start tag creation on letter
    @Test
    public void testTagOpen_letter_createsStartTagToken() {
        CharacterReader reader = new CharacterReader("<div");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        TokeniserState.Data.read(tokeniser, reader);
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertTrue(tokeniser.isAppropriateEndTagToken() || tokeniser.tagPending != null);
        assertEquals("div", tokeniser.tagPending.name());
    }

    // Tests TagOpen state branching to EndTagOpen on slash
    @Test
    public void testTagOpen_slash_transitionsToEndTagOpen() {
        CharacterReader reader = new CharacterReader("</div");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        TokeniserState.Data.read(tokeniser, reader);
        TokeniserState.TagOpen.read(tokeniser, reader);
        TokeniserState.EndTagOpen.read(tokeniser, reader);
        assertFalse(tokeniser.tagPending.isStartTag());
    }

    // Tests TagOpen state branching to BogusComment on question mark
    @Test
    public void testTagOpen_questionMark_transitionsToBogusComment() {
        CharacterReader reader = new CharacterReader("<?xml version=\"1.0\"?>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals("?xml version=\"1.0\"?", token.asComment().getData());
    }

    // Tests TagOpen state on non-matching character falling back to Data
    @Test
    public void testTagOpen_nonLetterNonSpecial_emitsLessThanChar() {
        CharacterReader reader = new CharacterReader("< 123");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("<", token.asCharacter().getData());
    }

    // Tests MarkupDeclarationOpen with standard HTML comment
    @Test
    public void testMarkupDeclarationOpen_commentDashes_parsesComment() {
        CharacterReader reader = new CharacterReader("<!-- a comment -->");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals(" a comment ", token.asComment().getData());
    }

    // Tests MarkupDeclarationOpen with DOCTYPE
    @Test
    public void testMarkupDeclarationOpen_doctype_parsesDoctype() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests CDATA section parsing
    @Test
    public void testCdataSection_validCdata_emitsDataContent() {
        CharacterReader reader = new CharacterReader("<![CDATA[raw <data> & text]]>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        TokeniserState.Data.read(tokeniser, reader);
        TokeniserState.TagOpen.read(tokeniser, reader);
        TokeniserState.MarkupDeclarationOpen.read(tokeniser, reader);
        TokeniserState.CdataSection.read(tokeniser, reader);
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("raw <data> & text", token.asCharacter().getData());
    }

    // Tests parsing attributes with double, single, and unquoted values
    @Test
    public void testAttributes_variousQuoteStyles_parsesAllAttributes() {
        CharacterReader reader = new CharacterReader("<div id=\"main\" class='active' data-count=123>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = token.asStartTag();
        assertEquals("div", tag.name());
        assertEquals("main", tag.attributes.get("id"));
        assertEquals("active", tag.attributes.get("class"));
        assertEquals("123", tag.attributes.get("data-count"));
    }

    // Tests self-closing start tag transition and attribute parsing
    @Test
    public void testSelfClosingStartTag_validSlash_setsSelfClosingTrue() {
        CharacterReader reader = new CharacterReader("<img src=\"img.png\" />");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = token.asStartTag();
        assertEquals("img", tag.name());
        assertTrue(tag.isSelfClosing());
    }

    // Tests RCDATA state with character reference and appropriate end tag
    @Test
    public void testRcdata_withCharacterReferenceAndEndTag_parsesCorrectly() {
        CharacterReader reader = new CharacterReader("Some &lt;title&gt;</title>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rcdata);
        tokeniser.appropriateEndTagName = "title";

        Token token1 = tokeniser.read();
        assertTrue(token1.isCharacter());
        assertEquals("Some <title>", token1.asCharacter().getData());

        Token token2 = tokeniser.read();
        assertTrue(token2.isEndTag());
        assertEquals("title", token2.asEndTag().name());
    }

    // Tests RAWTEXT state parsing until appropriate end tag
    @Test
    public void testRawtext_withAppropriateEndTag_emitsRawtextAndEndTag() {
        CharacterReader reader = new CharacterReader("line1\nline2</style>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rawtext);
        tokeniser.appropriateEndTagName = "style";

        Token token1 = tokeniser.read();
        assertTrue(token1.isCharacter());
        assertEquals("line1\nline2", token1.asCharacter().getData());

        Token token2 = tokeniser.read();
        assertTrue(token2.isEndTag());
        assertEquals("style", token2.asEndTag().name());
    }

    // Tests PLAINTEXT state consuming to EOF
    @Test
    public void testPLAINTEXT_anyContent_readsEntireBufferAsCharacter() {
        CharacterReader reader = new CharacterReader("everything <is> <b>plain</b>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.PLAINTEXT);

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("everything <is> <b>plain</b>", token.asCharacter().getData());
    }

    // Tests DOCTYPE with PUBLIC and SYSTEM identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_parsesCorrectly() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests ScriptData double escaped transition and content parsing
    @Test
    public void testScriptData_escapedScriptTag_parsesNestedScriptCorrectly() {
        CharacterReader reader = new CharacterReader("<!-- <script>var x = 1;</script> --></script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptData);
        tokeniser.appropriateEndTagName = "script";

        Token token1 = tokeniser.read();
        assertTrue(token1.isCharacter());
        assertEquals("<!-- <script>var x = 1;</script> -->", token1.asCharacter().getData());

        Token token2 = tokeniser.read();
        assertTrue(token2.isEndTag());
        assertEquals("script", token2.asEndTag().name());
    }

    // Tests null character replacement in Data state
    @Test
    public void testData_nullCharacter_emitsNullCharAndLogsError() {
        CharacterReader reader = new CharacterReader("\u0000");
        ParseErrorList errors = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("\u0000", token.asCharacter().getData());
        assertFalse(errors.isEmpty());
    }

    // Tests comment with bang ending syntax <!--- comment --!>
    @Test
    public void testComment_bangEnding_emitsComment() {
        CharacterReader reader = new CharacterReader("<!-- comment --!>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals(" comment ", token.asComment().getData());
    }
}