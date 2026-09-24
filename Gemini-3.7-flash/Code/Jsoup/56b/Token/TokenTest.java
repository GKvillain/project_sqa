package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTest {

    // Tests Doctype token initialization, getters, and type cast
    @Test
    public void testDoctype_initialAndGetters_returnsCorrectValues() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.publicIdentifier.append("public-id");
        doctype.systemIdentifier.append("system-id");
        doctype.forceQuirks = true;

        assertTrue(doctype.isDoctype());
        assertEquals("html", doctype.getName());
        assertEquals("public-id", doctype.getPublicIdentifier());
        assertEquals("system-id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
        assertEquals(doctype, doctype.asDoctype());
        assertEquals("Doctype", doctype.tokenType());
    }

    // Tests Doctype reset behavior
    @Test
    public void testDoctype_reset_clearsState() {
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

    // Tests StartTag creation, name, attributes, and toString
    @Test
    public void testStartTag_nameAndAttributes_formatsCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("DIV");
        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertFalse(startTag.isSelfClosing());
        assertEquals("<div>", startTag.toString());

        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.finaliseTag();

        assertEquals("<DIV id=\"main\">", startTag.toString());
        assertTrue(startTag.isStartTag());
        assertEquals(startTag, startTag.asStartTag());
    }

    // Tests StartTag reset clears attributes and state
    @Test
    public void testStartTag_reset_clearsState() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("span");
        startTag.selfClosing = true;
        startTag.appendAttributeName("class");
        startTag.appendAttributeValue("highlight");
        startTag.finaliseTag();

        startTag.reset();

        assertNull(startTag.normalName());
        assertFalse(startTag.isSelfClosing());
        assertNotNull(startTag.getAttributes());
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests StartTag nameAttr factory-like method
    @Test
    public void testStartTag_nameAttr_setsNameAndAttributes() {
        Token.StartTag startTag = new Token.StartTag();
        org.jsoup.nodes.Attributes attrs = new org.jsoup.nodes.Attributes();
        attrs.put("key", "val");
        startTag.nameAttr("P", attrs);

        assertEquals("P", startTag.name());
        assertEquals("p", startTag.normalName());
        assertEquals(attrs, startTag.getAttributes());
    }

    // Tests EndTag creation and toString format
    @Test
    public void testEndTag_nameAndToString_formatsCorrectly() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("P");

        assertTrue(endTag.isEndTag());
        assertEquals(endTag, endTag.asEndTag());
        assertEquals("P", endTag.name());
        assertEquals("p", endTag.normalName());
        assertEquals("</P>", endTag.toString());
    }

    // Tests Tag appendTagName with String and char
    @Test
    public void testTag_appendTagName_accumulatesAndNormalizes() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("di");
        startTag.appendTagName('v');

        assertEquals("div", startTag.name());
        assertEquals("div", startTag.normalName());
    }

    // Tests Tag attribute accumulation and multiple attribute value appends
    @Test
    public void testTag_appendAttributeValueVariants_constructsExpectedAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.appendAttributeName('h');
        startTag.appendAttributeName("ref");
        startTag.appendAttributeValue("http://");
        startTag.appendAttributeValue("example.com/");
        startTag.appendAttributeValue(new char[]{'t', 'e', 's', 't'});
        startTag.appendAttributeValue(new int[]{0x3F, 0x61}); // '?' and 'a'
        startTag.finaliseTag();

        assertEquals("http://example.com/test?a", startTag.getAttributes().get("href"));
    }

    // Tests Tag with empty attribute value
    @Test
    public void testTag_setEmptyAttributeValue_createsAttributeWithEmptyString() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("value");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        assertEquals("", startTag.getAttributes().get("value"));
        assertTrue(startTag.getAttributes().hasKey("value"));
    }

    // Tests Tag with boolean attribute
    @Test
    public void testTag_booleanAttribute_createsBooleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("disabled");
        startTag.newAttribute();

        assertTrue(startTag.getAttributes().hasKey("disabled"));
    }

    // Tests Tag name validation exception on empty name
    @Test(expected = IllegalArgumentException.class)
    public void testTag_nameWhenNull_throwsException() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name();
    }

    // Tests Comment token creation, data append, toString, and reset
    @Test
    public void testComment_dataToStringAndReset_worksCorrectly() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("This is a comment");
        comment.bogus = true;

        assertTrue(comment.isComment());
        assertEquals(comment, comment.asComment());
        assertEquals("This is a comment", comment.getData());
        assertEquals("<!--This is a comment-->", comment.toString());
        assertTrue(comment.bogus);

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    // Tests Character token data manipulation and reset
    @Test
    public void testCharacter_dataAndReset_worksCorrectly() {
        Token.Character character = new Token.Character();
        character.data("sample text");

        assertTrue(character.isCharacter());
        assertEquals(character, character.asCharacter());
        assertEquals("sample text", character.getData());
        assertEquals("sample text", character.toString());

        character.reset();
        assertNull(character.getData());
    }

    // Tests EOF token behavior and reset
    @Test
    public void testEOF_isEOFAndReset_returnsSelf() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertFalse(eof.isStartTag());
        assertFalse(eof.isEndTag());
        assertFalse(eof.isComment());
        assertFalse(eof.isCharacter());
        assertFalse(eof.isDoctype());

        Token resetResult = eof.reset();
        assertSame(eof, resetResult);
    }

    // Tests Token.reset(StringBuilder) helper method
    @Test
    public void testResetStringBuilder_nullAndNonNull_clearsSafely() {
        Token.reset((StringBuilder) null);

        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());
    }

    // Tests CData token initialization and representation
    @Test
    public void testCData_tokenBehavior() {
        Token.CData cdata = new Token.CData("data content");
        assertTrue(cdata.isCharacter());
        assertEquals("data content", cdata.getData());
        assertEquals("<![CDATA[data content]]>", cdata.toString());
    }

    // Tests TokenType names and tokenType() on remaining token types
    @Test
    public void testTokenType_forAllTokens() {
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    // Tests EndTag reset
    @Test
    public void testEndTag_reset_clearsState() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.reset();

        assertNull(endTag.normalName());
    }
}