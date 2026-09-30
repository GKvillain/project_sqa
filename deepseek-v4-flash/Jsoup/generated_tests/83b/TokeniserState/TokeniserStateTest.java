package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokeniserStateTest {

    private ParseErrorList createErrorList() {
        return new ParseErrorList(16, 16);
    }

    @Test
    // Tests plain text token in Data state
    public void testData_plainText_emitsTextToken() {
        Tokeniser t = new Tokeniser(new CharacterReader("Hello"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("Hello", ((Token.Character) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests null character replacement in Data state (potential defect)
    public void testData_nullChar_emitsReplacementChar() {
        Tokeniser t = new Tokeniser(new CharacterReader("He\u0000llo"), createErrorList());
        Token t1 = t.read();
        assertEquals("He", ((Token.Character) t1).getData());
        Token t2 = t.read();
        assertTrue(t2 instanceof Token.Character);
        // Expect replacement character, not nullChar
        assertEquals("\uFFFD", ((Token.Character) t2).getData());
        Token t3 = t.read();
        assertEquals("llo", ((Token.Character) t3).getData());
        Token t4 = t.read();
        assertTrue(t4 instanceof Token.EOF);
    }

    @Test
    // Tests valid character reference in Data state
    public void testData_validCharRef_emitsChar() {
        Tokeniser t = new Tokeniser(new CharacterReader("&amp;"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("&", ((Token.Character) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests invalid character reference in Data state
    public void testData_invalidCharRef_emitsAmpersandAndText() {
        Tokeniser t = new Tokeniser(new CharacterReader("&invalid;"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("&invalid;", ((Token.Character) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests empty input in Data state
    public void testData_emptyString_emitsEOF() {
        Tokeniser t = new Tokeniser(new CharacterReader(""), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests letter after '<' creates start tag
    public void testTagOpen_letter_createsStartTag() {
        Tokeniser t = new Tokeniser(new CharacterReader("<p>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertEquals("p", ((Token.StartTag) token).name());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests non-letter after '<' emits '<' and returns to Data
    public void testTagOpen_notLetter_emitsLessThan() {
        Tokeniser t = new Tokeniser(new CharacterReader("<1"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Character);
        assertEquals("<", ((Token.Character) token).getData());
        token = t.read();
        assertEquals("1", ((Token.Character) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests comment from "<!--" sequence
    public void testTagOpen_exclamationComment_emitsComment() {
        Tokeniser t = new Tokeniser(new CharacterReader("<!-- comment -->"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Comment);
        assertEquals(" comment ", ((Token.Comment) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests end tag from "</" sequence
    public void testTagOpen_slashEndTag_emitsEndTag() {
        Tokeniser t = new Tokeniser(new CharacterReader("</p>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.EndTag);
        assertEquals("p", ((Token.EndTag) token).name());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests self-closing tag
    public void testTagName_selfClosing_emitsSelfClosing() {
        Tokeniser t = new Tokeniser(new CharacterReader("<br/>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertEquals("br", ((Token.StartTag) token).name());
        assertTrue(((Token.StartTag) token).isSelfClosing());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests tag with double-quoted attribute
    public void testTagName_attribute_emitsTagWithAttribute() {
        Tokeniser t = new Tokeniser(new CharacterReader("<p class=\"a\">"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertEquals("p", ((Token.StartTag) token).name());
        assertEquals("a", ((Token.StartTag) token).attributes().get("class"));
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests null character in tag name replaced
    public void testTagName_nullChar_usesReplacement() {
        Tokeniser t = new Tokeniser(new CharacterReader("<p\u0000>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertEquals("p\uFFFD", ((Token.StartTag) token).name());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests EOF before completing start tag
    public void testTagName_eof_noStartTagEmitted() {
        Tokeniser t = new Tokeniser(new CharacterReader("<p"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests DOCTYPE token
    public void testDoctype_simple_emitsDoctype() {
        Tokeniser t = new Tokeniser(new CharacterReader("<!DOCTYPE html>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.Doctype);
        assertEquals("html", ((Token.Doctype) token).getName());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests CData section token
    public void testCdataSection_emitsCdata() {
        Tokeniser t = new Tokeniser(new CharacterReader("<![CDATA[data]]>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.CData);
        assertEquals("data", ((Token.CData) token).getData());
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }

    @Test
    // Tests unquoted attribute value
    public void testAttributeName_unquotedValue() {
        Tokeniser t = new Tokeniser(new CharacterReader("<p foo=bar>"), createErrorList());
        Token token = t.read();
        assertTrue(token instanceof Token.StartTag);
        assertEquals("p", ((Token.StartTag) token).name());
        assertEquals("bar", ((Token.StartTag) token).attributes().get("foo"));
        token = t.read();
        assertTrue(token instanceof Token.EOF);
    }
}