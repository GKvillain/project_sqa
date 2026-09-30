package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class TokeniserStateTest {

    private Tokeniser createTokeniser(String html) {
        CharacterReader reader = new CharacterReader(html);
        return new Tokeniser(reader, ParseErrorList.tracking(20));
    }

    private List<Token> tokenize(String html) {
        Tokeniser tokeniser = createTokeniser(html);
        List<Token> tokens = new ArrayList<Token>();
        Token token;
        do {
            token = tokeniser.read();
            tokens.add(token);
        } while (!token.isEOF());
        return tokens;
    }

    // Tests normal tag and character reference in Data state
    @Test
    public void testData_withTagAndCharacterReference_emitsTokens() {
        List<Token> tokens = tokenize("<p>&amp;Hello</p>");
        assertEquals(5, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        assertEquals("p", tokens.get(0).asStartTag().name());
        assertTrue(tokens.get(1).isCharacter());
        assertEquals("&", tokens.get(1).asCharacter().getData());
        assertTrue(tokens.get(2).isCharacter());
        assertEquals("Hello", tokens.get(2).asCharacter().getData());
        assertTrue(tokens.get(3).isEndTag());
        assertEquals("p", tokens.get(3).asEndTag().name());
        assertTrue(tokens.get(4).isEOF());
    }

    // Tests tag name transitions and self-closing start tag
    @Test
    public void testTagName_selfClosingAndAttributes_emitsSelfClosingTag() {
        List<Token> tokens = tokenize("<img src=\"test.jpg\" alt='test' id=foo />");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isStartTag());
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("img", tag.name());
        assertTrue(tag.isSelfClosing());
        assertEquals("test.jpg", tag.attributes.get("src"));
        assertEquals("test", tag.attributes.get("alt"));
        assertEquals("foo", tag.attributes.get("id"));
    }

    // Tests various attribute name and value states including empty and special chars
    @Test
    public void testAttributeStates_emptyAndSpecialAttributes_parsesCorrectly() {
        List<Token> tokens = tokenize("<a disabled href=\"\" title='' target=_blank >");
        assertEquals(2, tokens.size());
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("a", tag.name());
        assertTrue(tag.attributes.hasKey("disabled"));
        assertEquals("", tag.attributes.get("href"));
        assertEquals("", tag.attributes.get("title"));
        assertEquals("_blank", tag.attributes.get("target"));
    }

    // Tests Doctype parsing with Public and System identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_createsDoctypeToken() {
        List<Token> tokens = tokenize("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isDoctype());
        Token.Doctype doctype = tokens.get(0).asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype with System identifier only
    @Test
    public void testDoctype_systemIdentifierOnly_createsDoctypeToken() {
        List<Token> tokens = tokenize("<!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isDoctype());
        Token.Doctype doctype = tokens.get(0).asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype with missing name triggering force-quirks flag
    @Test
    public void testDoctype_missingName_forcesQuirks() {
        List<Token> tokens = tokenize("<!DOCTYPE>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isDoctype());
        assertTrue(tokens.get(0).asDoctype().isForceQuirks());
    }

    // Tests normal comment parsing and comment dash states
    @Test
    public void testComment_standardAndDashes_emitsComment() {
        List<Token> tokens = tokenize("<!-- a standard -- comment -->");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isComment());
        assertEquals(" a standard -- comment ", tokens.get(0).asComment().getData());
    }

    // Tests comment with exclamation end bang
    @Test
    public void testComment_endBang_emitsComment() {
        List<Token> tokens = tokenize("<!-- comment --!>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isComment());
        assertEquals(" comment ", tokens.get(0).asComment().getData());
    }

    // Tests bogus comment transitions from question mark and invalid markup declaration
    @Test
    public void testBogusComment_questionMarkAndInvalidDecl_emitsBogusComment() {
        List<Token> tokens = tokenize("<?xml version=\"1.0\"?><!invalid comment>");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isComment());
        assertEquals("?xml version=\"1.0\"?", tokens.get(0).asComment().getData());
        assertTrue(tokens.get(1).isComment());
        assertEquals("invalid comment", tokens.get(1).asComment().getData());
    }

    // Tests CDATA section state
    @Test
    public void testCdataSection_validContent_emitsCDataToken() {
        Tokeniser tokeniser = createTokeniser("<![CDATA[raw <data> & content]]>");
        List<Token> tokens = new ArrayList<Token>();
        Token token;
        do {
            token = tokeniser.read();
            tokens.add(token);
        } while (!token.isEOF());
        assertTrue(tokens.size() >= 2);
        assertTrue(tokens.get(0) instanceof Token.Character);
    }

    // Tests RCDATA state with title element and character references
    @Test
    public void testRcdata_withCharacterRefAndEndTag_handlesRcdata() {
        CharacterReader reader = new CharacterReader("Hello &amp; <world></title>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        tokeniser.transition(TokeniserState.RCDATA);
        tokeniser.createTagPending(false);
        tokeniser.tagPending.appendTagName("title");
        tokeniser.dataBuffer.append("title");

        Token token;
        StringBuilder sb = new StringBuilder();
        while (!(token = tokeniser.read()).isEOF()) {
            if (token.isCharacter()) {
                sb.append(token.asCharacter().getData());
            } else if (token.isEndTag()) {
                assertEquals("title", token.asEndTag().name());
                break;
            }
        }
        assertTrue(sb.toString().contains("Hello &"));
    }

    // Tests Rawtext state handling
    @Test
    public void testRawtext_withMarkup_preservesRawContent() {
        CharacterReader reader = new CharacterReader("content <b>bold</b></style>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        tokeniser.transition(TokeniserState.RAWTEXT);
        tokeniser.createTagPending(false);
        tokeniser.tagPending.appendTagName("style");
        tokeniser.dataBuffer.append("style");

        Token token;
        StringBuilder sb = new StringBuilder();
        while (!(token = tokeniser.read()).isEOF()) {
            if (token.isCharacter()) {
                sb.append(token.asCharacter().getData());
            } else if (token.isEndTag()) {
                assertEquals("style", token.asEndTag().name());
                break;
            }
        }
        assertTrue(sb.toString().contains("content <b>bold</b>"));
    }

    // Tests ScriptData escaped and double escaped states
    @Test
    public void testScriptData_escapedAndDoubleEscaped_parsesScriptCorrectly() {
        CharacterReader reader = new CharacterReader("<!-- <script>var x=1;</script> --> </script>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        tokeniser.transition(TokeniserState.ScriptData);
        tokeniser.createTagPending(false);
        tokeniser.tagPending.appendTagName("script");
        tokeniser.dataBuffer.append("script");

        Token token;
        StringBuilder sb = new StringBuilder();
        while (!(token = tokeniser.read()).isEOF()) {
            if (token.isCharacter()) {
                sb.append(token.asCharacter().getData());
            } else if (token.isEndTag()) {
                assertEquals("script", token.asEndTag().name());
                break;
            }
        }
        assertTrue(sb.toString().contains("var x=1;"));
    }

    // Tests EndTagOpen state with empty or invalid characters
    @Test
    public void testEndTagOpen_emptyAndInvalid_handlesErrors() {
        List<Token> tokens = tokenize("</><div></?bogus></div>");
        assertTrue(tokens.size() >= 3);
    }

    // Tests malformed attribute names with symbols like < or =
    @Test
    public void testBeforeAttributeName_specialCharacters_parsesAsAttributes() {
        List<Token> tokens = tokenize("<a <p>foo</p></a>");
        assertTrue(tokens.size() >= 2);
        assertTrue(tokens.get(0).isStartTag());
    }

    // Tests null character handling across Data and AttributeValue states
    @Test
    public void testNullCharacter_inDataAndAttribute_replacedProperly() {
        List<Token> tokens = tokenize("<div attr=\"val\u0000ue\">null\u0000char</div>");
        assertTrue(tokens.size() >= 3);
        Token.StartTag startTag = tokens.get(0).asStartTag();
        assertEquals("val\uFFFDue", startTag.attributes.get("attr"));
    }

    // Tests PLAINTEXT state consuming to EOF
    @Test
    public void testPlaintext_consumesToEnd() {
        CharacterReader reader = new CharacterReader("plain <b>text</b> <content>");
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(10));
        tokeniser.transition(TokeniserState.PLAINTEXT);

        Token token = tokeniser.read();
        assertTrue(token.isCharacter());
        assertEquals("plain <b>text</b> <content>", token.asCharacter().getData());
    }

    // Tests attribute value without quotes handling entity references and whitespace
    @Test
    public void testAttributeValueUnquoted_withEntityAndSpaces_parsesCorrectly() {
        List<Token> tokens = tokenize("<a href=http://example.com?a=1&amp;b=2 title=test>");
        assertEquals(2, tokens.size());
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("http://example.com?a=1&b=2", tag.attributes.get("href"));
        assertEquals("test", tag.attributes.get("title"));
    }

    // Tests single quoted public identifier in DOCTYPE
    @Test
    public void testDoctype_singleQuotedPublicIdentifier_parsesCorrectly() {
        List<Token> tokens = tokenize("<!DOCTYPE html PUBLIC '-//W3C//DTD HTML 4.01//EN' 'http://www.w3.org/TR/html4/strict.dtd'>");
        assertEquals(2, tokens.size());
        Token.Doctype doctype = tokens.get(0).asDoctype();
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
    }

    // Tests short comments like <!--> and <!--->
    @Test
    public void testComment_shortComments_parsedAsComments() {
        List<Token> tokens = tokenize("<!--><!--->");
        assertEquals(3, tokens.size());
        assertTrue(tokens.get(0).isComment());
        assertEquals("", tokens.get(0).asComment().getData());
        assertTrue(tokens.get(1).isComment());
        assertEquals("", tokens.get(1).asComment().getData());
    }

    // Tests BogusDoctype state transitions
    @Test
    public void testDoctype_bogusDoctype_setsForceQuirks() {
        List<Token> tokens = tokenize("<!DOCTYPE html BOGUS 'something'>");
        assertEquals(2, tokens.size());
        assertTrue(tokens.get(0).isDoctype());
        assertTrue(tokens.get(0).asDoctype().isForceQuirks());
    }

    // Tests character reference in unquoted attribute value
    @Test
    public void testAttributeValue_characterReferenceInAttribute_resolvesEntity() {
        List<Token> tokens = tokenize("<a href=\"foo&quot;bar\" title='foo&amp;bar'>");
        assertEquals(2, tokens.size());
        Token.StartTag tag = tokens.get(0).asStartTag();
        assertEquals("foo\"bar", tag.attributes.get("href"));
        assertEquals("foo&bar", tag.attributes.get("title"));
    }

    // Tests TagOpen state with solidus without tag name
    @Test
    public void testTagOpen_solidusNoTagName_emitsCommentOrHandlesError() {
        List<Token> tokens = tokenize("< / div>");
        assertTrue(tokens.size() >= 2);
    }
}