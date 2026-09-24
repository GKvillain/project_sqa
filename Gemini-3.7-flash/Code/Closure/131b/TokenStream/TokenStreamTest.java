package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenStreamTest {

    // Tests empty string for isJSIdentifier
    @Test
    public void testIsJSIdentifier_emptyString_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier(""));
    }

    // Tests valid single character identifier
    @Test
    public void testIsJSIdentifier_validSingleChar_returnsTrue() {
        assertTrue(TokenStream.isJSIdentifier("a"));
        assertTrue(TokenStream.isJSIdentifier("_"));
        assertTrue(TokenStream.isJSIdentifier("$"));
    }

    // Tests invalid start character (digit)
    @Test
    public void testIsJSIdentifier_digitStart_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier("0abc"));
        assertFalse(TokenStream.isJSIdentifier("9"));
    }

    // Tests valid multi-character identifier
    @Test
    public void testIsJSIdentifier_validIdentifier_returnsTrue() {
        assertTrue(TokenStream.isJSIdentifier("myVariable123"));
        assertTrue(TokenStream.isJSIdentifier("_$foo_bar"));
    }

    // Tests invalid character inside identifier
    @Test
    public void testIsJSIdentifier_invalidCharInBody_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier("a-b"));
        assertFalse(TokenStream.isJSIdentifier("a.b"));
        assertFalse(TokenStream.isJSIdentifier("a b"));
        assertFalse(TokenStream.isJSIdentifier("a#b"));
    }

    // Tests 2-character keywords and non-keywords
    @Test
    public void testIsKeyword_twoCharKeywords() {
        assertTrue(TokenStream.isKeyword("if"));
        assertTrue(TokenStream.isKeyword("in"));
        assertTrue(TokenStream.isKeyword("do"));
        assertFalse(TokenStream.isKeyword("is"));
        assertFalse(TokenStream.isKeyword("to"));
    }

    // Tests 3-character keywords and non-keywords
    @Test
    public void testIsKeyword_threeCharKeywords() {
        assertTrue(TokenStream.isKeyword("for"));
        assertTrue(TokenStream.isKeyword("int"));
        assertTrue(TokenStream.isKeyword("new"));
        assertTrue(TokenStream.isKeyword("try"));
        assertTrue(TokenStream.isKeyword("var"));
        assertFalse(TokenStream.isKeyword("foo"));
        assertFalse(TokenStream.isKeyword("bar"));
    }

    // Tests 4-character keywords and non-keywords
    @Test
    public void testIsKeyword_fourCharKeywords() {
        assertTrue(TokenStream.isKeyword("byte"));
        assertTrue(TokenStream.isKeyword("case"));
        assertTrue(TokenStream.isKeyword("char"));
        assertTrue(TokenStream.isKeyword("else"));
        assertTrue(TokenStream.isKeyword("enum"));
        assertTrue(TokenStream.isKeyword("goto"));
        assertTrue(TokenStream.isKeyword("long"));
        assertTrue(TokenStream.isKeyword("null"));
        assertTrue(TokenStream.isKeyword("true"));
        assertTrue(TokenStream.isKeyword("this"));
        assertTrue(TokenStream.isKeyword("void"));
        assertTrue(TokenStream.isKeyword("with"));
        assertFalse(TokenStream.isKeyword("test"));
        assertFalse(TokenStream.isKeyword("cars"));
    }

    // Tests 5-character keywords and non-keywords
    @Test
    public void testIsKeyword_fiveCharKeywords() {
        assertTrue(TokenStream.isKeyword("class"));
        assertTrue(TokenStream.isKeyword("break"));
        assertTrue(TokenStream.isKeyword("while"));
        assertTrue(TokenStream.isKeyword("false"));
        assertTrue(TokenStream.isKeyword("const"));
        assertTrue(TokenStream.isKeyword("final"));
        assertTrue(TokenStream.isKeyword("float"));
        assertTrue(TokenStream.isKeyword("short"));
        assertTrue(TokenStream.isKeyword("super"));
        assertTrue(TokenStream.isKeyword("throw"));
        assertTrue(TokenStream.isKeyword("catch"));
        assertFalse(TokenStream.isKeyword("clock"));
        assertFalse(TokenStream.isKeyword("fruit"));
    }

    // Tests 6-character keywords and non-keywords
    @Test
    public void testIsKeyword_sixCharKeywords() {
        assertTrue(TokenStream.isKeyword("native"));
        assertTrue(TokenStream.isKeyword("delete"));
        assertTrue(TokenStream.isKeyword("return"));
        assertTrue(TokenStream.isKeyword("throws"));
        assertTrue(TokenStream.isKeyword("import"));
        assertTrue(TokenStream.isKeyword("double"));
        assertTrue(TokenStream.isKeyword("static"));
        assertTrue(TokenStream.isKeyword("public"));
        assertTrue(TokenStream.isKeyword("switch"));
        assertTrue(TokenStream.isKeyword("export"));
        assertTrue(TokenStream.isKeyword("typeof"));
        assertFalse(TokenStream.isKeyword("custom"));
    }

    // Tests 7-character keywords and non-keywords
    @Test
    public void testIsKeyword_sevenCharKeywords() {
        assertTrue(TokenStream.isKeyword("package"));
        assertTrue(TokenStream.isKeyword("default"));
        assertTrue(TokenStream.isKeyword("finally"));
        assertTrue(TokenStream.isKeyword("boolean"));
        assertTrue(TokenStream.isKeyword("private"));
        assertTrue(TokenStream.isKeyword("extends"));
        assertFalse(TokenStream.isKeyword("program"));
    }

    // Tests 8-character keywords and non-keywords
    @Test
    public void testIsKeyword_eightCharKeywords() {
        assertTrue(TokenStream.isKeyword("abstract"));
        assertTrue(TokenStream.isKeyword("continue"));
        assertTrue(TokenStream.isKeyword("debugger"));
        assertTrue(TokenStream.isKeyword("function"));
        assertTrue(TokenStream.isKeyword("volatile"));
        assertFalse(TokenStream.isKeyword("absolute"));
    }

    // Tests 9-character keywords and non-keywords
    @Test
    public void testIsKeyword_nineCharKeywords() {
        assertTrue(TokenStream.isKeyword("interface"));
        assertTrue(TokenStream.isKeyword("protected"));
        assertTrue(TokenStream.isKeyword("transient"));
        assertFalse(TokenStream.isKeyword("important"));
    }

    // Tests 10-character and 12-character keywords and non-keywords
    @Test
    public void testIsKeyword_tenAndTwelveCharKeywords() {
        assertTrue(TokenStream.isKeyword("implements"));
        assertTrue(TokenStream.isKeyword("instanceof"));
        assertTrue(TokenStream.isKeyword("synchronized"));
        assertFalse(TokenStream.isKeyword("implementor"));
        assertFalse(TokenStream.isKeyword("synchronizer"));
    }

    // Tests default / non-keyword length lengths
    @Test
    public void testIsKeyword_unsupportedLength_returnsFalse() {
        assertFalse(TokenStream.isKeyword(""));
        assertFalse(TokenStream.isKeyword("a"));
        assertFalse(TokenStream.isKeyword("unsupportedlengthname"));
    }
}