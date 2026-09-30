package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTest {

    // Tests Doctype token creation, field accessors, and reset
    @Test
    public void testDoctype_stateAndReset_resetsAllFields() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.publicIdentifier.append("public-id");
        doctype.systemIdentifier.append("system-id");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("public-id", doctype.getPublicIdentifier());
        assertEquals("system-id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
        assertTrue(doctype.isDoctype());
        assertSame(doctype, doctype.asDoctype());

        doctype.reset();
        assertEquals("", doctype.getName());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests StartTag token naming and normalName lowercase conversion
    @Test
    public void testStartTag_name_preservesCaseAndLowercasesNormalName() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("DIV");

        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertTrue(startTag.isStartTag());
        assertSame(startTag, startTag.asStartTag());
    }

    // Tests StartTag appending tag name chars and strings
    @Test
    public void testStartTag_appendTagName_accumulatesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("sp");
        startTag.appendTagName('a');
        startTag.appendTagName("n");

        assertEquals("span", startTag.name());
        assertEquals("span", startTag.normalName());
    }

    // Tests StartTag name() throws exception when tagName is unset
    @Test(expected = IllegalArgumentException.class)
    public void testStartTag_emptyTagName_throwsException() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name();
    }

    // Tests tag attribute accumulation with various appendAttributeValue overloads
    @Test
    public void testStartTag_attributeValueAppenders_accumulatesCorrectValue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.appendAttributeName("href");
        startTag.appendAttributeValue("http://");
        startTag.appendAttributeValue('e');
        startTag.appendAttributeValue(new char[]{'x', 'a'});
        startTag.appendAttributeValue(new int[]{0x6D, 0x70, 0x6C, 0x65}); // "mple"
        startTag.newAttribute();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertEquals("http://example", attrs.get("href"));
    }

    // Tests tag creation with boolean attribute (no value appended)
    @Test
    public void testStartTag_booleanAttribute_createsBooleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName('r');
        startTag.appendAttributeName("equired");
        startTag.newAttribute();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertTrue(attrs.hasKey("required"));
        assertEquals("", attrs.get("required"));
    }

    // Tests tag creation with empty string attribute value
    @Test
    public void testStartTag_emptyAttributeValue_createsEmptyValuedAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("value");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertTrue(attrs.hasKey("value"));
        assertEquals("", attrs.get("value"));
    }

    // Tests finaliseTag when an attribute is pending without explicit newAttribute call
    @Test
    public void testStartTag_finaliseTag_addsPendingAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("class");
        startTag.appendAttributeValue("container");
        startTag.finaliseTag();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertEquals("container", attrs.get("class"));
    }

    // Tests StartTag toString formatting with and without attributes
    @Test
    public void testStartTag_toString_returnsFormattedHtml() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");
        assertEquals("<p>", startTag.toString());

        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("para1");
        startTag.newAttribute();
        assertEquals("<p id=\"para1\">", startTag.toString());
    }

    // Tests StartTag nameAttr helper method
    @Test
    public void testStartTag_nameAttr_initializesTagAndAttributes() {
        Attributes attributes = new Attributes();
        attributes.put("class", "active");

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("SPAN", attributes);

        assertEquals("SPAN", startTag.name());
        assertEquals("span", startTag.normalName());
        assertEquals(attributes, startTag.getAttributes());
    }

    // Tests StartTag reset clears attributes and state
    @Test
    public void testStartTag_reset_clearsAllState() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.selfClosing = true;
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();

        startTag.reset();

        assertFalse(startTag.isSelfClosing());
        assertNull(startTag.normalName());
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests EndTag methods and toString
    @Test
    public void testEndTag_toString_returnsClosingTag() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");

        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());
        assertEquals("div", endTag.name());
        assertEquals("div", endTag.normalName());
        assertEquals("</div>", endTag.toString());
    }

    // Tests Comment token data, reset, and toString
    @Test
    public void testComment_dataAndReset_resetsState() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("This is a comment");
        comment.bogus = true;

        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());
        assertEquals("This is a comment", comment.getData());
        assertEquals("<!--This is a comment-->", comment.toString());

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    // Tests Character token data, reset, and toString
    @Test
    public void testCharacter_dataAndReset_managesDataState() {
        Token.Character character = new Token.Character();
        character.data("Hello & World");

        assertTrue(character.isCharacter());
        assertSame(character, character.asCharacter());
        assertEquals("Hello & World", character.getData());
        assertEquals("Hello & World", character.toString());

        character.reset();
        assertNull(character.getData());
    }

    // Tests EOF token properties and reset
    @Test
    public void testEOF_propertiesAndReset_maintainsType() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertSame(eof, eof.reset());
    }

    // Tests tokenType method returning class simple name
    @Test
    public void testTokenType_returnsSimpleClassName() {
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character().tokenType());
        assertEquals("Doctype", new Token.Doctype().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    // Tests static reset helper method on StringBuilder
    @Test
    public void testReset_stringBuilder_clearsContent() {
        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());

        Token.reset((StringBuilder) null); // Null-safe
    }

    // Tests CData token creation, type check, and toString representation
    @Test
    public void testCData_creationAndToString() {
        Token.CData cdata = new Token.CData("raw cdata content");

        assertTrue(cdata.isCharacter());
        assertTrue(cdata.isCData());
        assertEquals("raw cdata content", cdata.getData());
        assertEquals("<![CDATA[raw cdata content]]>", cdata.toString());
    }

    // Tests Character token isCData returns false
    @Test
    public void testCharacter_isCData_returnsFalse() {
        Token.Character character = new Token.Character();
        character.data("text");
        assertFalse(character.isCData());
    }

    // Tests Doctype pubSysKey accessor and reset
    @Test
    public void testDoctype_pubSysKey_getAndReset() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.pubSysKey = "SYSTEM";
        assertEquals("SYSTEM", doctype.getPubSysKey());

        doctype.reset();
        assertNull(doctype.getPubSysKey());
    }

    // Tests Tag attribute query methods (hasAttributes, hasAttribute, hasAttributeWithValue)
    @Test
    public void testStartTag_attributeQueries() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("input");

        assertFalse(tag.hasAttributes());
        assertFalse(tag.hasAttribute("type"));
        assertFalse(tag.hasAttributeWithValue("type", "text"));

        tag.appendAttributeName("type");
        tag.appendAttributeValue("text");
        tag.newAttribute();

        assertTrue(tag.hasAttributes());
        assertTrue(tag.hasAttribute("type"));
        assertFalse(tag.hasAttribute("other"));
        assertTrue(tag.hasAttributeWithValue("type", "text"));
        assertFalse(tag.hasAttributeWithValue("type", "password"));
        assertFalse(tag.hasAttributeWithValue("other", "text"));
    }

    // Tests newAttribute does nothing when pendingAttributeName is null
    @Test
    public void testTag_newAttribute_withoutPendingAttributeName_doesNothing() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.newAttribute();
        assertFalse(tag.hasAttributes());
    }

    // Tests finaliseTag when no pending attribute name is set
    @Test
    public void testTag_finaliseTag_withoutPendingAttribute_leavesAttributesUnchanged() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.finaliseTag();
        assertFalse(tag.hasAttributes());
    }

    // Tests type inquiry methods returning false on mismatched token types
    @Test
    public void testToken_typeInquiryMethods_returnFalseForOtherTypes() {
        Token.StartTag startTag = new Token.StartTag();
        assertFalse(startTag.isDoctype());
        assertFalse(startTag.isEndTag());
        assertFalse(startTag.isComment());
        assertFalse(startTag.isCharacter());
        assertFalse(startTag.isCData());
        assertFalse(startTag.isEOF());

        Token.EOF eof = new Token.EOF();
        assertFalse(eof.isDoctype());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertFalse(eof.isCData());
    }

    // Tests EndTag reset resets tag state
    @Test
    public void testEndTag_reset_clearsState() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("span");
        endTag.reset();

        assertNull(endTag.normalName());
    }
}