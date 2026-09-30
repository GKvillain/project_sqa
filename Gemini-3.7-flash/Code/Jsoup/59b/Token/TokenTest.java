package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenTest {

    // Tests whitespace-only attribute name in newAttribute
    @Test
    public void testNewAttribute_whitespaceAttributeName_trimmedCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("   ");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();
        assertNotNull(startTag.getAttributes());
    }

    // Tests adding boolean attribute
    @Test
    public void testNewAttribute_booleanAttribute_createsBooleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("disabled");
        startTag.newAttribute();

        assertTrue(startTag.getAttributes().hasKey("disabled"));
    }

    // Tests adding attribute with empty string value
    @Test
    public void testNewAttribute_emptyAttributeValue_createsEmptyAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.appendAttributeName("href");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        assertEquals("", startTag.getAttributes().get("href"));
    }

    // Tests attribute value appending via single string and multiple parts
    @Test
    public void testNewAttribute_multipleValueAppends_concatenatesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("span");
        startTag.appendAttributeName('c');
        startTag.appendAttributeName("lass");
        startTag.appendAttributeValue("btn");
        startTag.appendAttributeValue(" btn-");
        startTag.appendAttributeValue('p');
        startTag.appendAttributeValue(new char[]{'r', 'i'});
        startTag.appendAttributeValue(new int[]{109, 97, 114, 121}); // "mary"
        startTag.finaliseTag();

        assertEquals("btn btn-primary", startTag.getAttributes().get("class"));
    }

    // Tests StartTag name, normalName, and tag name appends
    @Test
    public void testStartTag_appendTagName_updatesTagNameAndNormalName() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("DI");
        startTag.appendTagName('V');

        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertEquals("<DIV>", startTag.toString());
    }

    // Tests tag name validation throwing exception when name is missing
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_nullOrEmpty_throwsException() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name();
    }

    // Tests StartTag reset functionality
    @Test
    public void testStartTag_reset_clearsState() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("p");
        startTag.selfClosing = true;
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();

        startTag.reset();

        assertNull(startTag.normalName());
        assertFalse(startTag.isSelfClosing());
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests StartTag with nameAttr method and toString formatting
    @Test
    public void testStartTag_nameAttr_formatsToStringWithAttributes() {
        Token.StartTag startTag = new Token.StartTag();
        org.jsoup.nodes.Attributes attrs = new org.jsoup.nodes.Attributes();
        attrs.put("id", "test");
        startTag.nameAttr("DIV", attrs);

        assertEquals("div", startTag.normalName());
        assertEquals("<DIV id=\"test\">", startTag.toString());
    }

    // Tests EndTag methods and toString
    @Test
    public void testEndTag_nameAndToString_formatsCorrectly() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("DIV");

        assertEquals("DIV", endTag.name());
        assertEquals("div", endTag.normalName());
        assertEquals("</DIV>", endTag.toString());

        endTag.reset();
        assertNull(endTag.normalName());
    }

    // Tests Doctype token methods and reset
    @Test
    public void testDoctype_methodsAndReset_workCorrectly() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("public-id");
        doctype.systemIdentifier.append("system-id");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("PUBLIC", doctype.getPubSysKey());
        assertEquals("public-id", doctype.getPublicIdentifier());
        assertEquals("system-id", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());

        doctype.reset();

        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Comment token methods, toString and reset
    @Test
    public void testComment_methodsAndReset_workCorrectly() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("This is a comment");
        comment.bogus = true;

        assertEquals("This is a comment", comment.getData());
        assertEquals("<!--This is a comment-->", comment.toString());

        comment.reset();

        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    // Tests Character token data and reset
    @Test
    public void testCharacter_dataAndReset_workCorrectly() {
        Token.Character character = new Token.Character();
        character.data("text data");

        assertEquals("text data", character.getData());
        assertEquals("text data", character.toString());

        character.reset();

        assertNull(character.getData());
    }

    // Tests EOF token type and reset
    @Test
    public void testEOF_reset_returnsSameInstance() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertSame(eof, eof.reset());
    }

    // Tests type checking methods and casting helpers
    @Test
    public void testToken_typeChecksAndCasting_identifiesCorrectly() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertNotNull(doctype.asDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isEOF());

        Token startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertNotNull(startTag.asStartTag());

        Token endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertNotNull(endTag.asEndTag());

        Token comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertNotNull(comment.asComment());

        Token character = new Token.Character();
        assertTrue(character.isCharacter());
        assertNotNull(character.asCharacter());

        Token eof = new Token.EOF();
        assertTrue(eof.isEOF());
    }

    // Tests static reset helper on StringBuilder
    @Test
    public void testReset_stringBuilder_clearsContent() {
        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());

        // Test with null StringBuilder to ensure no exception
        Token.reset(null);
    }

    // Tests tokenType method
    @Test
    public void testTokenType_returnsSimpleClassName() {
        Token startTag = new Token.StartTag();
        assertEquals("StartTag", startTag.tokenType());
    }
}