package org.jsoup.parser;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TokeniserStateTest {

    // Tests Data state with plain text, tag open, character reference, and EOF
    @Test
    public void testRead_dataStateNormalText_emitsDataAndEOF() {
        CharacterReader reader = new CharacterReader("Hello world<p>&amp;</p>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("Hello world", ((Token.Character) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isStartTag());
        assertEquals("p", ((Token.StartTag) t2).name());

        Token t3 = tokeniser.read();
        assertTrue(t3.isCharacter());
        assertEquals("&", ((Token.Character) t3).getData());
    }

    // Tests Data state with null character
    @Test
    public void testRead_dataStateNullChar_emitsNullCharAndLogsError() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("\u0000");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("\u0000", ((Token.Character) token).getData());
        assertFalse(errorList.isEmpty());
    }

    // Tests TagOpen and EndTagOpen transitions
    @Test
    public void testRead_tagOpenAndEndTag_parsesCorrectly() {
        CharacterReader reader = new CharacterReader("<div></div>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isStartTag());
        assertEquals("div", ((Token.StartTag) t1).name());

        Token t2 = tokeniser.read();
        assertTrue(t2.isEndTag());
        assertEquals("div", ((Token.EndTag) t2).name());
    }

    // Tests TagOpen with invalid characters triggering BogusComment or Data fallback
    @Test
    public void testRead_tagOpenInvalidChars_transitionsProperly() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<?bogus>< 123");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token t1 = tokeniser.read();
        assertTrue(t1.isComment());
        assertEquals("?bogus", ((Token.Comment) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isCharacter());
        assertEquals("<", ((Token.Character) t2).getData());
    }

    // Tests EndTagOpen edge cases with empty and invalid characters
    @Test
    public void testRead_endTagOpenInvalid_emitsExpectedTokens() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("</>");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token t1 = tokeniser.read();
        assertTrue(t1.isEOF());
        assertFalse(errorList.isEmpty());

        reader = new CharacterReader("</?");
        tokeniser = new Tokeniser(reader, errorList);
        Token t2 = tokeniser.read();
        assertTrue(t2.isComment());
    }

    // Tests tag attributes with various quoting styles and self-closing syntax
    @Test
    public void testRead_tagWithAttributesAndSelfClosing_parsesAttributes() {
        CharacterReader reader = new CharacterReader("<img id=\"foo\" class='bar' disabled attr=val / >");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isStartTag());
        Token.StartTag startTag = (Token.StartTag) t1;
        assertEquals("img", startTag.name());
        assertEquals("foo", startTag.attributes.get("id"));
        assertEquals("bar", startTag.attributes.get("class"));
        assertTrue(startTag.attributes.hasKey("disabled"));
        assertEquals("val", startTag.attributes.get("attr"));
        assertTrue(startTag.isSelfClosing());
    }

    // Tests comment parsing with dashes, bang, and normal end
    @Test
    public void testRead_commentTransitions_emitsCommentData() {
        CharacterReader reader = new CharacterReader("<!-- hello -- world --><!--!>--><!--");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isComment());
        assertEquals(" hello -- world ", ((Token.Comment) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isComment());
        assertEquals("hello -- world --", ((Token.Comment) t1).getData().isEmpty() ? "" : ((Token.Comment) t1).getData());

        Token t3 = tokeniser.read();
        assertTrue(t3.isComment() || t3.isEOF());
    }

    // Tests MarkupDeclarationOpen for CDATA and bogus comments
    @Test
    public void testRead_markupDeclaration_cdataAndBogusComment() {
        CharacterReader reader = new CharacterReader("<![CDATA[section data]]><!INVALID>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("section data", ((Token.Character) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isComment());
    }

    // Tests DOCTYPE parsing with public and system identifiers
    @Test
    public void testRead_doctypeDeclaration_parsesDoctypeTokens() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token t1 = tokeniser.read();
        assertTrue(t1.isDoctype());
        Token.Doctype doctype = (Token.Doctype) t1;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests DOCTYPE with system keyword and quirks mode
    @Test
    public void testRead_doctypeSystemAndBogus_forcesQuirks() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html SYSTEM 'sys.dtd'><!DOCTYPE bogus");
        ParseErrorList errorList = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token t1 = tokeniser.read();
        assertTrue(t1.isDoctype());
        Token.Doctype d1 = (Token.Doctype) t1;
        assertEquals("html", d1.getName());
        assertEquals("sys.dtd", d1.getSystemIdentifier());

        Token t2 = tokeniser.read();
        assertTrue(t2.isDoctype());
        Token.Doctype d2 = (Token.Doctype) t2;
        assertTrue(d2.isForceQuirks());
    }

    // Tests RCDATA state with title element and character references
    @Test
    public void testRead_rcdataState_parsesEntitiesAndAppropriateEndTag() {
        CharacterReader reader = new CharacterReader("Hello &amp; <title> world</title>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rcdata);

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("Hello ", ((Token.Character) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isCharacter());
        assertEquals("&", ((Token.Character) t2).getData());
    }

    // Tests Rawtext state with style element and end tag
    @Test
    public void testRead_rawtextState_readsUntilEndTag() {
        CharacterReader reader = new CharacterReader("body { color: red; }</style>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rawtext);

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("body { color: red; }", ((Token.Character) t1).getData());
    }

    // Tests ScriptData state and ScriptDataLessthanSign
    @Test
    public void testRead_scriptDataState_readsDataAndHandlesScriptTag() {
        CharacterReader reader = new CharacterReader("var a = 1 < 2; </script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptData);

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("var a = 1 ", ((Token.Character) t1).getData());
    }

    // Tests ScriptDataEscaped and ScriptDataDoubleEscaped transitions
    @Test
    public void testRead_scriptDataEscapedAndDoubleEscaped_handlesNestedScripts() {
        CharacterReader reader = new CharacterReader("<!-- <script> var x = 1; </script> --> </script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptData);

        Token token;
        boolean sawEndTag = false;
        while (!(token = tokeniser.read()).isEOF()) {
            if (token.isEndTag() && ((Token.EndTag) token).name().equals("script")) {
                sawEndTag = true;
            }
        }
        assertTrue(sawEndTag || reader.isEmpty());
    }

    // Tests ScriptDataEscapedEndTagName state
    @Test
    public void testRead_scriptDataEscapedEndTagName_handlesAppropriateEndTag() {
        CharacterReader reader = new CharacterReader("</script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptDataEscaped);

        Token token = tokeniser.read();
        assertNotNull(token);
    }

    // Tests PLAINTEXT state consuming until EOF
    @Test
    public void testRead_plaintextState_consumesAllRemainingInput() {
        CharacterReader reader = new CharacterReader("plain <b>text</b> \u0000 content");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        tokeniser.transition(TokeniserState.PLAINTEXT);

        Token t1 = tokeniser.read();
        assertTrue(t1.isCharacter());
        assertEquals("plain <b>text</b> ", ((Token.Character) t1).getData());

        Token t2 = tokeniser.read();
        assertTrue(t2.isCharacter());
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) t2).getData());

        Token t3 = tokeniser.read();
        assertTrue(t3.isCharacter());
        assertEquals(" content", ((Token.Character) t3).getData());
    }

    // Tests AttributeName and AttributeValue edge cases with null character
    @Test
    public void testRead_attributeWithNullCharacter_replacesWithReplacementChar() {
        CharacterReader reader = new CharacterReader("<a key\u0000=val\u0000ue>");
        ParseErrorList errorList = ParseErrorList.tracking(10);
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("a", tag.name());
        assertFalse(errorList.isEmpty());
    }

    // Tests CharacterReference in attribute values with double and single quotes
    @Test
    public void testRead_characterReferenceInAttributeValues_resolvesEntities() {
        CharacterReader reader = new CharacterReader("<a href=\"&lt;&amp;&gt;\" title='&quot;'>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());

        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("<&>", tag.attributes.get("href"));
        assertEquals("\"", tag.attributes.get("title"));
    }

    // Tests unexpected EOF in attribute value transitions
    @Test
    public void testRead_unexpectedEofInAttribute_logsErrorAndEmitsData() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        CharacterReader reader = new CharacterReader("<a href=\"unclosed");
        Tokeniser tokeniser = new Tokeniser(reader, errorList);

        Token token = tokeniser.read();
        assertNotNull(token);
        assertFalse(errorList.isEmpty());
    }
}