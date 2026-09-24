package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    // Tests Data state with plain text, tag open, and character reference
    @Test
    public void testData_variousInputs_transitionsAndEmitsCorrectly() {
        CharacterReader reader = new CharacterReader("text&amp;<p>\0");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("text", ((Token.Character) token).getData());

        Token refToken = tokeniser.read();
        assertTrue(refToken.isCharacter());
        assertEquals("&", ((Token.Character) refToken).getData());
    }

    // Tests Data state EOF emission
    @Test
    public void testData_eof_emitsEofToken() {
        CharacterReader reader = new CharacterReader("");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        TokeniserState.Data.read(tokeniser, reader);
        Token token = tokeniser.read();
        assertTrue(token.isEOF());
    }

    // Tests TagOpen state with start tag, end tag, comment, and bogus comment
    @Test
    public void testTagOpen_transitions_createsExpectedTokens() {
        CharacterReader reader = new CharacterReader("div>/div>?bogus>!doctype");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.TagName, tokeniser.getState());
        
        reader.consumeTo('>');
        reader.consume();
        
        TokeniserState.TagOpen.read(tokeniser, reader);
        assertEquals(TokeniserState.EndTagOpen, tokeniser.getState());
        
        CharacterReader readerBogus = new CharacterReader("?comment>");
        Tokeniser tokeniserBogus = new Tokeniser(readerBogus, ParseErrorList.noTracking());
        TokeniserState.TagOpen.read(tokeniserBogus, readerBogus);
        assertEquals(TokeniserState.BogusComment, tokeniserBogus.getState());

        CharacterReader readerInvalid = new CharacterReader("123");
        Tokeniser tokeniserInvalid = new Tokeniser(readerInvalid, ParseErrorList.tracking(5));
        TokeniserState.TagOpen.read(tokeniserInvalid, readerInvalid);
        assertEquals(TokeniserState.Data, tokeniserInvalid.getState());
        assertFalse(tokeniserInvalid.getErrors().isEmpty());
    }

    // Tests EndTagOpen state branches: letter, '>', empty, and invalid
    @Test
    public void testEndTagOpen_variousInputs_handlesProperly() {
        CharacterReader reader1 = new CharacterReader("div>");
        Tokeniser tokeniser1 = new Tokeniser(reader1, ParseErrorList.noTracking());
        TokeniserState.EndTagOpen.read(tokeniser1, reader1);
        assertEquals(TokeniserState.TagName, tokeniser1.getState());

        CharacterReader reader2 = new CharacterReader(">");
        Tokeniser tokeniser2 = new Tokeniser(reader2, ParseErrorList.tracking(5));
        TokeniserState.EndTagOpen.read(tokeniser2, reader2);
        assertEquals(TokeniserState.Data, tokeniser2.getState());

        CharacterReader reader3 = new CharacterReader("");
        Tokeniser tokeniser3 = new Tokeniser(reader3, ParseErrorList.tracking(5));
        TokeniserState.EndTagOpen.read(tokeniser3, reader3);
        assertEquals(TokeniserState.Data, tokeniser3.getState());

        CharacterReader reader4 = new CharacterReader("!bogus");
        Tokeniser tokeniser4 = new Tokeniser(reader4, ParseErrorList.tracking(5));
        TokeniserState.EndTagOpen.read(tokeniser4, reader4);
        assertEquals(TokeniserState.BogusComment, tokeniser4.getState());
    }

    // Tests TagName state parsing attributes, self-closing, and completion
    @Test
    public void testTagName_delimiters_transitionsCorrectly() {
        CharacterReader reader = new CharacterReader("img src='foo'/>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("img", tag.name());
        assertEquals("foo", tag.attributes.get("src"));
        assertTrue(tag.isSelfClosing());
    }

    // Tests RCDATA state transitions and end tag matching
    @Test
    public void testRcdata_matchingAndUnmatchingEndTag_tokenisesCorrectly() {
        CharacterReader reader = new CharacterReader("sample <notEnd </title>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rcdata);
        tokeniser.createTagPending(true);
        tokeniser.tagPending.appendTagName("title");
        tokeniser.emitTagPending();
        
        Token textToken = tokeniser.read();
        assertTrue(textToken.isCharacter());
        
        Token endTag = tokeniser.read();
        while (endTag != null && !endTag.isEndTag()) {
            endTag = tokeniser.read();
        }
        assertNotNull(endTag);
        assertTrue(endTag.isEndTag());
        assertEquals("title", ((Token.EndTag) endTag).name());
    }

    // Tests RCDATA state with null character and character reference
    @Test
    public void testRcdata_specialCharacters_emitsReplacementAndReferences() {
        CharacterReader reader = new CharacterReader("\0&amp;");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(5));
        tokeniser.transition(TokeniserState.Rcdata);

        Token token1 = tokeniser.read();
        assertTrue(token1.isCharacter());
        assertEquals(String.valueOf(Tokeniser.replacementChar), ((Token.Character) token1).getData());
    }

    // Tests Rawtext state transitions
    @Test
    public void testRawtext_endTagHandling_processesProperly() {
        CharacterReader reader = new CharacterReader("raw text </style>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.Rawtext);
        tokeniser.createTagPending(true);
        tokeniser.tagPending.appendTagName("style");
        tokeniser.emitTagPending();

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("raw text ", ((Token.Character) token).getData());

        Token endToken = tokeniser.read();
        assertTrue(endToken.isEndTag());
        assertEquals("style", ((Token.EndTag) endToken).name());
    }

    // Tests ScriptData state and script escape transitions
    @Test
    public void testScriptData_escapedScript_parsesCorrectly() {
        CharacterReader reader = new CharacterReader("<!-- <script> var x = 1; </script> --> </script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptData);
        tokeniser.createTagPending(true);
        tokeniser.tagPending.appendTagName("script");
        tokeniser.emitTagPending();

        Token token;
        boolean foundEnd = false;
        while ((token = tokeniser.read()) != null && !token.isEOF()) {
            if (token.isEndTag() && ((Token.EndTag) token).name().equals("script")) {
                foundEnd = true;
                break;
            }
        }
        assertTrue(foundEnd);
    }

    // Tests ScriptData double escaped state transitions
    @Test
    public void testScriptDataDoubleEscaped_transitions_switchesStateProperly() {
        CharacterReader reader = new CharacterReader("-<script>text</script>--></script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.ScriptDataEscaped);
        
        Token token;
        boolean foundEnd = false;
        while ((token = tokeniser.read()) != null && !token.isEOF()) {
            if (token.isEndTag() && ((Token.EndTag) token).name().equals("script")) {
                foundEnd = true;
                break;
            }
        }
        assertTrue(foundEnd);
    }

    // Tests Attribute states with unquoted, single-quoted, and double-quoted values
    @Test
    public void testAttributeValues_differentQuotes_parsesValuesCorrectly() {
        CharacterReader reader = new CharacterReader("input a=\"val1\" b='val2' c=val3 >");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        Token.StartTag tag = (Token.StartTag) token;
        assertEquals("val1", tag.attributes.get("a"));
        assertEquals("val2", tag.attributes.get("b"));
        assertEquals("val3", tag.attributes.get("c"));
    }

    // Tests AttributeName edge cases like nullChar and invalid characters
    @Test
    public void testAttributeName_invalidChars_recordsErrors() {
        CharacterReader reader = new CharacterReader("<tag \0=\"test\" 'bad'>", ParseErrorList.tracking(10));
        Tokeniser tokeniser = new Tokeniser(reader, reader.getErrors());
        
        Token token = tokeniser.read();
        assertTrue(token.isStartTag());
        assertFalse(reader.getErrors().isEmpty());
    }

    // Tests Comment tokenisation including dashes, bangs, and null characters
    @Test
    public void testComment_variousFormats_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("<!-- a normal comment --><!---dash-dash---><!--!bang-->");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token1 = tokeniser.read();
        assertTrue(token1.isComment());
        assertEquals(" a normal comment ", ((Token.Comment) token1).getData());

        Token token2 = tokeniser.read();
        assertTrue(token2.isComment());
        assertEquals("-dash-dash-", ((Token.Comment) token2).getData());

        Token token3 = tokeniser.read();
        assertTrue(token3.isComment());
        assertEquals("bang", ((Token.Comment) token3).getData());
    }

    // Tests Doctype parsing including public and system identifiers
    @Test
    public void testDoctype_fullDeclaration_emitsDoctypeToken() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype with SYSTEM keyword and quirks mode trigger
    @Test
    public void testDoctype_systemKeywordAndQuirks_setsPropertiesCorrectly() {
        CharacterReader reader = new CharacterReader("<!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token = tokeniser.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = (Token.Doctype) token;
        assertEquals("html", doctype.getName());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());

        CharacterReader bogusReader = new CharacterReader("<!DOCTYPE>");
        Tokeniser bogusTokeniser = new Tokeniser(bogusReader, ParseErrorList.tracking(5));
        Token bogusToken = bogusTokeniser.read();
        assertTrue(bogusToken.isDoctype());
        assertTrue(((Token.Doctype) bogusToken).isForceQuirks());
    }

    // Tests CDATA section parsing
    @Test
    public void testCdataSection_validCdata_emitsCharacterToken() {
        CharacterReader reader = new CharacterReader("<![CDATA[raw <data> & characters]]>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        
        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("raw <data> & characters", ((Token.Character) token).getData());
    }

    // Tests PLAINTEXT state handling
    @Test
    public void testPlainText_content_emitsAllAsCharacterToken() {
        CharacterReader reader = new CharacterReader("some text <p>not a tag</p>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.noTracking());
        tokeniser.transition(TokeniserState.PLAINTEXT);

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("some text <p>not a tag</p>", ((Token.Character) token).getData());
    }

    // Tests BogusComment state handling from invalid markup declaration
    @Test
    public void testBogusComment_invalidMarkup_emitsCommentToken() {
        CharacterReader reader = new CharacterReader("<!bogus comment>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(5));
        
        Token token = tokeniser.read();
        assertTrue(token.isComment());
        assertEquals("bogus comment", ((Token.Comment) token).getData());
        assertFalse(tokeniser.getErrors().isEmpty());
    }
}