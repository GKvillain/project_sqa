package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenStreamTest {

    // Tests empty string as JS identifier
    @Test
    public void testIsJSIdentifier_emptyString_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier(""));
    }

    // Tests valid simple JS identifier
    @Test
    public void testIsJSIdentifier_validIdentifier_returnsTrue() {
        assertTrue(TokenStream.isJSIdentifier("validVar"));
        assertTrue(TokenStream.isJSIdentifier("_privateVar"));
        assertTrue(TokenStream.isJSIdentifier("$jquery"));
        assertTrue(TokenStream.isJSIdentifier("var123"));
    }

    // Tests JS identifier starting with digit
    @Test
    public void testIsJSIdentifier_startingWithDigit_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier("123var"));
    }

    // Tests JS identifier containing special characters or spaces
    @Test
    public void testIsJSIdentifier_containingSpecialChars_returnsFalse() {
        assertFalse(TokenStream.isJSIdentifier("foo-bar"));
        assertFalse(TokenStream.isJSIdentifier("foo bar"));
        assertFalse(TokenStream.isJSIdentifier("foo.bar"));
        assertFalse(TokenStream.isJSIdentifier("foo@bar"));
    }

    // Tests keywords of length 2
    @Test
    public void testIsKeyword_lengthTwoKeywords_returnsTrue() {
        assertTrue(TokenStream.isKeyword("if"));
        assertTrue(TokenStream.isKeyword("in"));
        assertTrue(TokenStream.isKeyword("do"));
        assertFalse(TokenStream.isKeyword("to"));
        assertFalse(TokenStream.isKeyword("it"));
    }

    // Tests keywords of length 3
    @Test
    public void testIsKeyword_lengthThreeKeywords_returnsTrue() {
        assertTrue(TokenStream.isKeyword("for"));
        assertTrue(TokenStream.isKeyword("int"));
        assertTrue(TokenStream.isKeyword("new"));
        assertTrue(TokenStream.isKeyword("try"));
        assertTrue(TokenStream.isKeyword("var"));
        assertFalse(TokenStream.isKeyword("foo"));
        assertFalse(TokenStream.isKeyword("bar"));
    }

    // Tests keywords of length 4
    @Test
    public void testIsKeyword_lengthFourKeywords_returnsTrue() {
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
        assertFalse(TokenStream.isKeyword("care"));
    }

    // Tests keywords of length 5
    @Test
    public void testIsKeyword_lengthFiveKeywords_returnsTrue() {
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
        assertFalse(TokenStream.isKeyword("hello"));
        assertFalse(TokenStream.isKeyword("flock"));
    }

    // Tests keywords of length 6
    @Test
    public void testIsKeyword_lengthSixKeywords_returnsTrue() {
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
        assertFalse(TokenStream.isKeyword("revert"));
    }

    // Tests keywords of length 7
    @Test
    public void testIsKeyword_lengthSevenKeywords_returnsTrue() {
        assertTrue(TokenStream.isKeyword("package"));
        assertTrue(TokenStream.isKeyword("default"));
        assertTrue(TokenStream.isKeyword("finally"));
        assertTrue(TokenStream.isKeyword("boolean"));
        assertTrue(TokenStream.isKeyword("private"));
        assertTrue(TokenStream.isKeyword("extends"));
        assertFalse(TokenStream.isKeyword("example"));
    }

    // Tests keywords of length 8
    @Test
    public void testIsKeyword_lengthEightKeywords_returnsTrue() {
        assertTrue(TokenStream.isKeyword("abstract"));
        assertTrue(TokenStream.isKeyword("continue"));
        assertTrue(TokenStream.isKeyword("debugger"));
        assertTrue(TokenStream.isKeyword("function"));
        assertTrue(TokenStream.isKeyword("volatile"));
        assertFalse(TokenStream.isKeyword("argument"));
    }

    // Tests keywords of length 9, 10, and 12
    @Test
    public void testIsKeyword_lengthNineTenTwelveKeywords_returnsTrue() {
        assertTrue(TokenStream.isKeyword("interface"));
        assertTrue(TokenStream.isKeyword("protected"));
        assertTrue(TokenStream.isKeyword("transient"));
        assertTrue(TokenStream.isKeyword("implements"));
        assertTrue(TokenStream.isKeyword("instanceof"));
        assertTrue(TokenStream.isKeyword("synchronized"));
        assertFalse(TokenStream.isKeyword("interfaces"));
        assertFalse(TokenStream.isKeyword("implementor"));
    }

    // Tests strings of non-keyword length or non-matching partial strings
    @Test
    public void testIsKeyword_invalidLengthsAndNonKeywords_returnsFalse() {
        assertFalse(TokenStream.isKeyword(""));
        assertFalse(TokenStream.isKeyword("a"));
        assertFalse(TokenStream.isKeyword("supercalifragilistic"));
        assertFalse(TokenStream.isKeyword("synchro"));
    }
}