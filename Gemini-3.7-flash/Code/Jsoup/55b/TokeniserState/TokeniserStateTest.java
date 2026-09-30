package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    // Tests reading normal plain character data in Data state
    @Test
    public void testData_normalText_emitsCharacterToken() {
        CharacterReader r = new CharacterReader("Hello world");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("Hello world", token.asCharacter().getData());
    }

    // Tests TagOpen state transitioning to TagName on matching letter
    @Test
    public void testTagOpen_startTag_createsStartTag() {
        CharacterReader r = new CharacterReader("<div class='test'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("div", token.asStartTag().name());
        assertEquals("test", token.asStartTag().attributes.get("class"));
    }

    // Tests EndTagOpen state transitioning to TagName for closing tag
    @Test
    public void testEndTagOpen_validEndTag_createsEndTag() {
        CharacterReader r = new CharacterReader("</div>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isEndTag());
        assertEquals("div", token.asEndTag().name());
    }

    // Tests self closing start tag correctly sets selfClosing flag
    @Test
    public void testSelfClosingStartTag_validSlash_setsSelfClosingFlag() {
        CharacterReader r = new CharacterReader("<img src='foo.jpg' />");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertTrue(token.asStartTag().isSelfClosing());
    }

    // Tests handling of attribute after slash in start tag
    @Test
    public void testSelfClosingStartTag_slashFollowedByAttribute_parsesTagNameAndAttribute() {
        CharacterReader r = new CharacterReader("<a / href='bar'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("a", token.asStartTag().name());
    }

    // Tests standard comment parsing through CommentStart and CommentEnd
    @Test
    public void testComment_fullComment_emitsCommentToken() {
        CharacterReader r = new CharacterReader("<!-- this is a comment -->");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" this is a comment ", token.asComment().getData());
        assertFalse(token.asComment().bogus);
    }

    // Tests BogusComment state when TagOpen encounters question mark
    @Test
    public void testBogusComment_questionMarkTag_emitsBogusComment() {
        CharacterReader r = new CharacterReader("<?xml version=\"1.0\"?>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
    }

    // Tests HTML5 DOCTYPE declaration parsing
    @Test
    public void testDoctype_html5Doctype_emitsDoctypeToken() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
        assertFalse(token.asDoctype().isForceQuirks());
    }

    // Tests DOCTYPE declaration with PUBLIC and SYSTEM identifiers
    @Test
    public void testDoctype_publicAndSystem_emitsFullDoctype() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", token.asDoctype().getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", token.asDoctype().getSystemIdentifier());
        assertFalse(token.asDoctype().isForceQuirks());
    }

    // Tests invalid DOCTYPE without name forcing quirks mode
    @Test
    public void testDoctype_emptyDoctype_setsQuirksMode() {
        CharacterReader r = new CharacterReader("<!DOCTYPE>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertTrue(token.asDoctype().isForceQuirks());
    }

    // Tests CDATA section parsing
    @Test
    public void testCdataSection_inMarkup_emitsCharacterToken() {
        CharacterReader r = new CharacterReader("<![CDATA[raw cdata content]]>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("raw cdata content", token.asCharacter().getData());
    }

    // Tests Rawtext state until matching end tag
    @Test
    public void testRawtext_transitionAndRead_emitsRawtextUntilEndTag() {
        CharacterReader r = new CharacterReader("some <raw> content</style>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rawtext);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("some <raw> content", token1.asCharacter().getData());
        Token token2 = t.read();
        assertTrue(token2.isEndTag());
        assertEquals("style", token2.asEndTag().name());
    }

    // Tests Rcdata state entity decoding
    @Test
    public void testRcdata_characterReference_decodesEntity() {
        CharacterReader r = new CharacterReader("Hello &amp; world</title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rcdata);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("Hello & world", token1.asCharacter().getData());
    }

    // Tests PLAINTEXT state reading all content as character tokens
    @Test
    public void testPLAINTEXT_state_readsUntilEof() {
        CharacterReader r = new CharacterReader("plain text <with> <b>tags</b>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.PLAINTEXT);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("plain text <with> <b>tags</b>", token.asCharacter().getData());
    }

    // Tests unquoted attribute value parsing with entity reference
    @Test
    public void testAttributeValue_unquotedAndEntities_parsesValue() {
        CharacterReader r = new CharacterReader("<span id=main&amp;content data-val=123>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("main&content", token.asStartTag().attributes.get("id"));
        assertEquals("123", token.asStartTag().attributes.get("data-val"));
    }

    // Tests single quoted attribute value
    @Test
    public void testAttributeValue_singleQuoted_parsesValue() {
        CharacterReader r = new CharacterReader("<div title='hello world'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("hello world", token.asStartTag().attributes.get("title"));
    }

    // Tests TagOpen state when encountering non-letter character and falling back to Data
    @Test
    public void testTagOpen_invalidChar_recoversToData() {
        CharacterReader r = new CharacterReader("<123");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("<", token.asCharacter().getData());
    }

    // Tests comment ending with bang dash dash delimiter
    @Test
    public void testCommentEnd_bang_transitionsToCommentEndBang() {
        CharacterReader r = new CharacterReader("<!-- comment --!>next");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" comment ", token.asComment().getData());
    }

    // Tests EndTagOpen with invalid empty end tag
    @Test
    public void testEndTagOpen_emptyEndTag_recordsError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("</>");
        Tokeniser t = new Tokeniser(r, errors);
        t.read();
        assertFalse(errors.isEmpty());
    }

    // Tests ScriptData state and normal script content reading
    @Test
    public void testScriptData_basicScript_emitsDataAndEndTag() {
        CharacterReader r = new CharacterReader("var x = 1;</script>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.ScriptData);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("var x = 1;", token1.asCharacter().getData());
        Token token2 = t.read();
        assertTrue(token2.isEndTag());
        assertEquals("script", token2.asEndTag().name());
    }

    // Tests ScriptData escaped transitions with comments and escaped script end tags
    @Test
    public void testScriptData_escapedWithComment_handlesEscapedScriptTags() {
        CharacterReader r = new CharacterReader("<!-- <script>alert(1)</script> -->var y = 2;</script>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.ScriptData);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("<!-- <script>alert(1)</script> -->var y = 2;", token1.asCharacter().getData());
        Token token2 = t.read();
        assertTrue(token2.isEndTag());
        assertEquals("script", token2.asEndTag().name());
    }

    // Tests ScriptData double escaped transitions
    @Test
    public void testScriptData_doubleEscaped_handlesNestedScriptTags() {
        CharacterReader r = new CharacterReader("<!--<script>nested</script>-->after</script>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.ScriptData);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("<!--<script>nested</script>-->after", token1.asCharacter().getData());
        Token token2 = t.read();
        assertTrue(token2.isEndTag());
        assertEquals("script", token2.asEndTag().name());
    }

    // Tests Doctype with single-quoted identifiers and SYSTEM keyword
    @Test
    public void testDoctype_singleQuotedIdentifiers_parsesCorrectly() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertEquals("html", token.asDoctype().getName());
        assertEquals("about:legacy-compat", token.asDoctype().getSystemIdentifier());
        assertFalse(token.asDoctype().isForceQuirks());
    }

    // Tests BogusDoctype recovery
    @Test
    public void testDoctype_bogusDoctype_setsForceQuirks() {
        CharacterReader r = new CharacterReader("<!DOCTYPE html INVALID 'something'>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertTrue(token.asDoctype().isForceQuirks());
    }

    // Tests Comment short variants such as <!--> and <!--->
    @Test
    public void testComment_shortComments_parsesEmptyComment() {
        CharacterReader r1 = new CharacterReader("<!-->");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(10));
        Token token1 = t1.read();
        assertTrue(token1.isComment());
        assertEquals("", token1.asComment().getData());

        CharacterReader r2 = new CharacterReader("<!--->");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(10));
        Token token2 = t2.read();
        assertTrue(token2.isComment());
        assertEquals("", token2.asComment().getData());
    }

    // Tests Comment with internal dashes and unexpected closures
    @Test
    public void testComment_internalDashes_preservesContent() {
        CharacterReader r = new CharacterReader("<!-- a - b -- c -->");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertEquals(" a - b -- c ", token.asComment().getData());
    }

    // Tests null replacement character handling in Data state
    @Test
    public void testData_nullChar_replacesWithReplacementChar() {
        CharacterReader r = new CharacterReader("a\u0000b");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("a\uFFFDb", token.asCharacter().getData());
    }

    // Tests attribute name without value followed immediately by another attribute
    @Test
    public void testAttributeName_booleanAttributes_parsedCorrectly() {
        CharacterReader r = new CharacterReader("<input disabled checked=\"checked\" autofocus />");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertTrue(token.asStartTag().attributes.hasKey("disabled"));
        assertTrue(token.asStartTag().attributes.hasKey("checked"));
        assertTrue(token.asStartTag().attributes.hasKey("autofocus"));
        assertTrue(token.asStartTag().isSelfClosing());
    }

    // Tests missing whitespace between quoted attribute values
    @Test
    public void testAttributeValue_quotedWithoutWhitespace_recordsErrorAndParsesBoth() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("<div class=\"one\"id=\"two\">");
        Tokeniser t = new Tokeniser(r, errors);
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertEquals("one", token.asStartTag().attributes.get("class"));
        assertEquals("two", token.asStartTag().attributes.get("id"));
        assertFalse(errors.isEmpty());
    }

    // Tests EndTag with invalid self closing slash
    @Test
    public void testEndTag_selfClosingSlash_recordsError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("</div/>");
        Tokeniser t = new Tokeniser(r, errors);
        Token token = t.read();
        assertTrue(token.isEndTag());
        assertEquals("div", token.asEndTag().name());
        assertFalse(errors.isEmpty());
    }

    // Tests EndTag with attributes which is invalid HTML
    @Test
    public void testEndTag_withAttributes_recordsError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        CharacterReader r = new CharacterReader("</div class=\"foo\">");
        Tokeniser t = new Tokeniser(r, errors);
        Token token = t.read();
        assertTrue(token.isEndTag());
        assertEquals("div", token.asEndTag().name());
        assertFalse(errors.isEmpty());
    }

    // Tests MarkupDeclarationOpen fallback to bogus comment on unknown declaration
    @Test
    public void testMarkupDeclarationOpen_unknownDeclaration_parsesAsBogusComment() {
        CharacterReader r = new CharacterReader("<!FOOBAR baz>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
        assertEquals("FOOBAR baz", token.asComment().getData());
    }

    // Tests numeric character reference in Data state
    @Test
    public void testCharacterReference_numericDecimalAndHex_decodesCorrectly() {
        CharacterReader r = new CharacterReader("&#65;&#x42;");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("AB", token.asCharacter().getData());
    }

    // Tests unclosed character reference at EOF
    @Test
    public void testCharacterReference_atEof_handlesGracefully() {
        CharacterReader r = new CharacterReader("&amp");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("&", token.asCharacter().getData());
    }

    // Tests Rcdata non-matching end tag emitted as characters
    @Test
    public void testRcdata_nonMatchingEndTag_emitsCharacters() {
        CharacterReader r = new CharacterReader("text</other>more</title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.transition(TokeniserState.Rcdata);
        Token token1 = t.read();
        assertTrue(token1.isCharacter());
        assertEquals("text</other>more", token1.asCharacter().getData());
        Token token2 = t.read();
        assertTrue(token2.isEndTag());
        assertEquals("title", token2.asEndTag().name());
    }
}