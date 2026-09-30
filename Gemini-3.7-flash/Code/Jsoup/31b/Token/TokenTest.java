package org.jsoup.parser;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTest {

    // Tests Doctype token creation, getters, and type checks
    @Test
    public void testDoctype_initialValues_returnsExpectedProperties() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());
        assertEquals("Doctype", doctype.tokenType());
        assertSame(doctype, doctype.asDoctype());

        doctype.name.append("html");
        doctype.publicIdentifier.append("public-id");
        doctype.systemIdentifier.append("system-id");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("public-id", doctype.getPublicIdentifier());
        assertEquals("system-id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
    }

    // Tests StartTag constructors and toString representation without attributes
    @Test
    public void testStartTag_noAttributes_toStringFormatsCorrectly() {
        Token.StartTag startTag = new Token.StartTag("div");
        assertTrue(startTag.isStartTag());
        assertSame(startTag, startTag.asStartTag());
        assertEquals("div", startTag.name());
        assertFalse(startTag.isSelfClosing());
        assertEquals("<div>", startTag.toString());
    }

    // Tests StartTag constructor with attributes and toString representation
    @Test
    public void testStartTag_withAttributes_toStringFormatsCorrectly() {
        Attributes attributes = new Attributes();
        attributes.put("id", "main");
        Token.StartTag startTag = new Token.StartTag("div", attributes);
        assertEquals("<div id=\"main\">", startTag.toString());
        assertEquals(attributes, startTag.getAttributes());
    }

    // Tests StartTag attribute accumulation with name and value
    @Test
    public void testTag_appendAttributeNameAndValue_createsAttributesProperly() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("a");
        tag.appendAttributeName("hr");
        tag.appendAttributeName('e');
        tag.appendAttributeName("f");

        tag.appendAttributeValue("http://");
        tag.appendAttributeValue('e');
        tag.appendAttributeValue("xample.com");

        tag.finaliseTag();

        Attributes attrs = tag.getAttributes();
        assertNotNull(attrs);
        assertTrue(attrs.hasKey("href"));
        assertEquals("http://example.com", attrs.get("href"));
    }

    // Tests Tag attribute accumulation when value is null (boolean attribute)
    @Test
    public void testTag_appendAttributeNameWithoutValue_createsEmptyValueAttribute() {
        Token.StartTag tag = new Token.StartTag("input");
        tag.appendAttributeName("disabled");
        tag.newAttribute();

        Attributes attrs = tag.getAttributes();
        assertTrue(attrs.hasKey("disabled"));
        assertEquals("", attrs.get("disabled"));
    }

    // Tests appending tag name via String and char
    @Test
    public void testTag_appendTagName_accumulatesCorrectly() {
        Token.StartTag tag = new Token.StartTag();
        tag.appendTagName("sp");
        tag.appendTagName('a');
        tag.appendTagName("n");

        assertEquals("span", tag.name());
    }

    // Tests Tag name validation when empty
    @Test(expected = IllegalArgumentException.class)
    public void testTag_emptyName_throwsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("");
        tag.name();
    }

    // Tests EndTag constructors and toString representation
    @Test
    public void testEndTag_validName_toStringFormatsCorrectly() {
        Token.EndTag endTag = new Token.EndTag("span");
        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());
        assertEquals("span", endTag.name());
        assertEquals("</span>", endTag.toString());
    }

    // Tests EndTag attribute creation when attributes is initially null
    @Test
    public void testEndTag_newAttributeWhenAttributesNull_initializesAttributes() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        assertNull(endTag.getAttributes());

        endTag.appendAttributeName("class");
        endTag.appendAttributeValue("test");
        endTag.newAttribute();

        assertNotNull(endTag.getAttributes());
        assertEquals("test", endTag.getAttributes().get("class"));
    }

    // Tests Comment token creation, data appending, and toString
    @Test
    public void testComment_validData_toStringFormatsCorrectly() {
        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());
        assertEquals("Comment", comment.tokenType());

        comment.data.append("sample comment");
        assertEquals("sample comment", comment.getData());
        assertEquals("<!--sample comment-->", comment.toString());
    }

    // Tests Character token creation, data retrieval, and toString
    @Test
    public void testCharacter_validData_returnsExpectedDataAndString() {
        Token.Character character = new Token.Character("hello world");
        assertTrue(character.isCharacter());
        assertSame(character, character.asCharacter());
        assertEquals("Character", character.tokenType());
        assertEquals("hello world", character.getData());
        assertEquals("hello world", character.toString());
    }

    // Tests EOF token creation and type identification
    @Test
    public void testEOF_instance_identifiesCorrectly() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertFalse(eof.isCharacter());
        assertFalse(eof.isComment());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isDoctype());
        assertEquals("EOF", eof.tokenType());
    }

    // Tests finaliseTag when no pending attribute exists
    @Test
    public void testTag_finaliseTagWithoutPendingAttribute_doesNotThrow() {
        Token.StartTag tag = new Token.StartTag("p");
        tag.finaliseTag();
        assertEquals(0, tag.getAttributes().size());
    }

    // Tests Doctype reset resets all fields
    @Test
    public void testDoctype_reset_clearsFields() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.publicIdentifier.append("pub");
        doctype.systemIdentifier.append("sys");
        doctype.forceQuirks = true;

        doctype.reset();

        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests StartTag self-closing property and toString formatting
    @Test
    public void testStartTag_selfClosing_toStringFormatsCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("img");
        startTag.selfClosing = true;
        assertTrue(startTag.isSelfClosing());
        assertEquals("<img/>", startTag.toString());

        Attributes attrs = new Attributes();
        attrs.put("src", "foo.jpg");
        startTag.nameAttr("img", attrs);
        startTag.selfClosing = true;
        assertEquals("<img src=\"foo.jpg\"/>", startTag.toString());
    }

    // Tests StartTag reset clears state
    @Test
    public void testStartTag_reset_clearsState() {
        Token.StartTag startTag = new Token.StartTag("div");
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("test");
        startTag.newAttribute();
        startTag.selfClosing = true;

        startTag.reset();

        assertNull(startTag.name());
        assertFalse(startTag.isSelfClosing());
        assertNull(startTag.getAttributes());
    }

    // Tests Tag normalName returns lowercased tag name
    @Test
    public void testTag_normalName_returnsLowercased() {
        Token.StartTag startTag = new Token.StartTag("DIV");
        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
    }

    // Tests Tag appendAttributeValue with char array and codepoints array
    @Test
    public void testTag_appendAttributeValueVariants_appendsCorrectly() {
        Token.StartTag tag = new Token.StartTag("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue(new char[]{'h', 't', 't', 'p'});
        tag.appendAttributeValue(new int[]{58, 47, 47}); // "://"

        tag.finaliseTag();
        assertEquals("http://", tag.getAttributes().get("href"));
    }

    // Tests Comment reset clears data and bogus flag
    @Test
    public void testComment_resetAndBogus_operatesCorrectly() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("sample");
        comment.bogus = true;
        assertTrue(comment.bogus);

        comment.reset();

        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    // Tests Character reset and data setter
    @Test
    public void testCharacter_resetAndDataSetter_operatesCorrectly() {
        Token.Character character = new Token.Character();
        character.data("first");
        assertEquals("first", character.getData());

        character.reset();
        assertEquals("", character.getData());

        character.data("second");
        assertEquals("second", character.getData());
    }

    // Tests EOF reset does not throw
    @Test
    public void testEOF_reset_returnsSameInstance() {
        Token.EOF eof = new Token.EOF();
        Token resetToken = eof.reset();
        assertSame(eof, resetToken);
        assertTrue(resetToken.isEOF());
    }

    // Tests EndTag reset clears state
    @Test
    public void testEndTag_reset_clearsState() {
        Token.EndTag endTag = new Token.EndTag("span");
        endTag.reset();
        assertNull(endTag.name());
    }

    // Tests Tag duplicate attributes handling
    @Test
    public void testTag_duplicateAttributes_overwritesOrIgnoresCorrectly() {
        Token.StartTag tag = new Token.StartTag("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("one");
        tag.newAttribute();

        tag.appendAttributeName("class");
        tag.appendAttributeValue("two");
        tag.newAttribute();

        tag.finaliseTag();
        assertEquals("one", tag.getAttributes().get("class"));
    }

    // Tests asTokenType methods type casting behavior
    @Test(expected = ClassCastException.class)
    public void testToken_asStartTagOnEndTag_throwsClassCastException() {
        Token token = new Token.EndTag("div");
        token.asStartTag();
    }
}