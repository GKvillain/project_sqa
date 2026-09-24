package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTest {

    // Tests tokenType returns the simple class name
    @Test
    public void testTokenType_startTag_returnsSimpleClassName() {
        Token.StartTag startTag = new Token.StartTag("p");
        assertEquals("StartTag", startTag.tokenType());
    }

    // Tests Doctype token initialization and field accessors
    @Test
    public void testDoctype_defaultsAndSetters_returnsExpectedValues() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());

        doctype.name.append("html");
        doctype.publicIdentifier.append("public-id");
        doctype.systemIdentifier.append("system-id");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("public-id", doctype.getPublicIdentifier());
        assertEquals("system-id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
        assertSame(doctype, doctype.asDoctype());
    }

    // Tests StartTag constructors and toString representation
    @Test
    public void testStartTag_constructorsAndToString_formattedCorrectly() {
        Token.StartTag tag1 = new Token.StartTag();
        tag1.name("div");
        assertEquals("<div>", tag1.toString());
        assertTrue(tag1.isStartTag());
        assertSame(tag1, tag1.asStartTag());

        Token.StartTag tag2 = new Token.StartTag("span");
        assertEquals("<span>", tag2.toString());
        assertNotNull(tag2.getAttributes());

        Attributes attrs = new Attributes();
        attrs.put("class", "main");
        Token.StartTag tag3 = new Token.StartTag("div", attrs);
        assertEquals("<div class=\"main\">", tag3.toString());
        assertEquals("div", tag3.name());
    }

    // Tests EndTag constructors and toString representation
    @Test
    public void testEndTag_constructorsAndToString_formattedCorrectly() {
        Token.EndTag tag = new Token.EndTag();
        tag.name("p");
        assertTrue(tag.isEndTag());
        assertSame(tag, tag.asEndTag());
        assertEquals("</p>", tag.toString());

        Token.EndTag tag2 = new Token.EndTag("div");
        assertEquals("</div>", tag2.toString());
        assertEquals("div", tag2.name());
    }

    // Tests Comment token creation, data append, and toString
    @Test
    public void testComment_dataAndToString_returnsFormattedComment() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());

        comment.data.append("This is a comment");
        assertEquals("This is a comment", comment.getData());
        assertEquals("<!--This is a comment-->", comment.toString());
    }

    // Tests Character token creation and toString
    @Test
    public void testCharacter_getDataAndToString_returnsExactData() {
        Token.Character character = new Token.Character("hello world");
        assertTrue(character.isCharacter());
        assertSame(character, character.asCharacter());
        assertEquals("hello world", character.getData());
        assertEquals("hello world", character.toString());
    }

    // Tests EOF token type verification
    @Test
    public void testEOF_typeCheck_returnsTrue() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertFalse(eof.isDoctype());
    }

    // Tests Tag appendTagName with String and char
    @Test
    public void testTag_appendTagName_accumulatesCorrectly() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("d");
        tag.appendTagName('i');
        tag.appendTagName("v");
        assertEquals("div", tag.name());
    }

    // Tests Tag appendAttributeName and appendAttributeValue with value
    @Test
    public void testTag_appendAttributeNameAndValue_createsValidAttribute() {
        Token.StartTag tag = new Token.StartTag("a");
        tag.appendAttributeName("hr");
        tag.appendAttributeName('e');
        tag.appendAttributeName("f");

        tag.appendAttributeValue("http://ex");
        tag.appendAttributeValue('a');
        tag.appendAttributeValue("mple.com");

        tag.newAttribute();

        Attributes attributes = tag.getAttributes();
        assertNotNull(attributes);
        assertEquals("http://example.com", attributes.get("href"));
    }

    // Tests Tag newAttribute when value is null (boolean/empty attribute)
    @Test
    public void testTag_newAttributeWithoutValue_createsEmptyValueAttribute() {
        Token.StartTag tag = new Token.StartTag("input");
        tag.appendAttributeName("disabled");
        tag.newAttribute();

        Attributes attributes = tag.getAttributes();
        assertNotNull(attributes);
        assertTrue(attributes.hasKey("disabled"));
        assertEquals("", attributes.get("disabled"));
    }

    // Tests Tag finaliseTag when pendingAttributeName is present
    @Test
    public void testTag_finaliseTag_commitsPendingAttribute() {
        Token.StartTag tag = new Token.StartTag("input");
        tag.appendAttributeName("required");
        tag.finaliseTag();

        assertTrue(tag.getAttributes().hasKey("required"));
    }

    // Tests Tag finaliseTag when pendingAttributeName is null
    @Test
    public void testTag_finaliseTag_doesNothingWhenNoPendingAttribute() {
        Token.StartTag tag = new Token.StartTag("input");
        tag.finaliseTag();
        assertEquals(0, tag.getAttributes().size());
    }

    // Tests Tag name validation throws exception on empty string
    @Test(expected = IllegalArgumentException.class)
    public void testTag_nameEmpty_throwsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("");
        tag.name();
    }

    // Tests Tag isSelfClosing default state
    @Test
    public void testTag_isSelfClosing_defaultsToFalse() {
        Token.StartTag tag = new Token.StartTag("img");
        assertFalse(tag.isSelfClosing());
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
    }

    // Tests EndTag newAttribute initializes attributes when previously null
    @Test
    public void testEndTag_newAttribute_initializesAttributes() {
        Token.EndTag tag = new Token.EndTag("div");
        assertNull(tag.getAttributes());

        tag.appendAttributeName("id");
        tag.appendAttributeValue("test");
        tag.newAttribute();

        assertNotNull(tag.getAttributes());
        assertEquals("test", tag.getAttributes().get("id"));
    }

    // Tests tokenType returns expected names across various token types
    @Test
    public void testTokenType_allTokenTypes_returnsSimpleClassName() {
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character("data").tokenType());
        assertEquals("Doctype", new Token.Doctype().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    // Tests type check methods return false for non-matching token types
    @Test
    public void testTokenTypeChecks_nonMatchingTypes_returnFalse() {
        Token.StartTag startTag = new Token.StartTag("p");
        assertFalse(startTag.isDoctype());
        assertFalse(startTag.isEndTag());
        assertFalse(startTag.isComment());
        assertFalse(startTag.isCharacter());
        assertFalse(startTag.isEOF());

        Token.EndTag endTag = new Token.EndTag("p");
        assertFalse(endTag.isDoctype());
        assertFalse(endTag.isStartTag());
        assertFalse(endTag.isComment());
        assertFalse(endTag.isCharacter());
        assertFalse(endTag.isEOF());

        Token.Comment comment = new Token.Comment();
        assertFalse(comment.isDoctype());
        assertFalse(comment.isStartTag());
        assertFalse(comment.isEndTag());
        assertFalse(comment.isCharacter());
        assertFalse(comment.isEOF());

        Token.Character character = new Token.Character("text");
        assertFalse(character.isDoctype());
        assertFalse(character.isStartTag());
        assertFalse(character.isEndTag());
        assertFalse(character.isComment());
        assertFalse(character.isEOF());

        Token.Doctype doctype = new Token.Doctype();
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());
    }

    // Tests Comment bogus flag default and setter
    @Test
    public void testComment_bogusProperty_defaultsToFalseAndCanBeSet() {
        Token.Comment comment = new Token.Comment();
        assertFalse(comment.bogus);
        comment.bogus = true;
        assertTrue(comment.bogus);
    }

    // Tests StartTag toString with attributes and self-closing
    @Test
    public void testStartTag_toStringWithSelfClosingAndAttributes() {
        Token.StartTag tag = new Token.StartTag("img");
        tag.selfClosing = true;
        tag.appendAttributeName("src");
        tag.appendAttributeValue("test.png");
        tag.newAttribute();
        assertEquals("<img src=\"test.png\">", tag.toString());
    }

    // Tests EndTag toString with attributes
    @Test
    public void testEndTag_toStringWithAttributes() {
        Token.EndTag tag = new Token.EndTag("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("container");
        tag.newAttribute();
        assertEquals("</div class=\"container\">", tag.toString());
    }
}