package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TokenTest {

    // Tests Doctype token creation, getter methods, and reset
    @Test
    public void testDoctype_stateAndReset_resetsFieldsCorrectly() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("pubId");
        doctype.systemIdentifier.append("sysId");
        doctype.forceQuirks = true;

        assertEquals("html", doctype.getName());
        assertEquals("PUBLIC", doctype.getPubSysKey());
        assertEquals("pubId", doctype.getPublicIdentifier());
        assertEquals("sysId", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
        assertEquals("Doctype", doctype.tokenType());

        doctype.reset();
        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests StartTag toString with and without attributes, and reset functionality
    @Test
    public void testStartTag_toStringAndReset_behavesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        assertEquals("<div>", startTag.toString());

        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();
        assertEquals("<div id=\"main\">", startTag.toString());

        startTag.reset();
        assertNull(startTag.normalName());
        assertNotNull(startTag.getAttributes());
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests StartTag nameAttr helper method
    @Test
    public void testStartTag_nameAttr_setsNameAndAttributes() {
        Token.StartTag startTag = new Token.StartTag();
        Attributes attributes = new Attributes();
        attributes.put("class", "container");
        startTag.nameAttr("SPAN", attributes);

        assertEquals("SPAN", startTag.name());
        assertEquals("span", startTag.normalName());
        assertEquals(attributes, startTag.getAttributes());
        assertEquals("<SPAN class=\"container\">", startTag.toString());
    }

    // Tests EndTag toString output
    @Test
    public void testEndTag_toString_returnsClosingTag() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("P");
        assertEquals("</P>", endTag.toString());
        assertEquals("EndTag", endTag.tokenType());
    }

    // Tests appending tag name via String and char methods
    @Test
    public void testTag_appendTagName_accumulatesAndLowercasesNormalName() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("h");
        startTag.appendTagName('1');

        assertEquals("h1", startTag.name());
        assertEquals("h1", startTag.normalName());
        assertFalse(startTag.isSelfClosing());
    }

    // Tests appending attribute names via String and char methods
    @Test
    public void testTag_appendAttributeName_accumulatesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.appendAttributeName("data-");
        startTag.appendAttributeName('v');
        startTag.appendAttributeValue("test");
        startTag.newAttribute();

        assertEquals("test", startTag.getAttributes().get("data-v"));
    }

    // Tests appending attribute values across multiple types: char, char array, codepoints
    @Test
    public void testTag_appendAttributeValue_multipleTypes_accumulatesValue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.appendAttributeName("href");
        startTag.appendAttributeValue("http");
        startTag.appendAttributeValue(':');
        startTag.appendAttributeValue(new char[]{'/', '/'});
        startTag.appendAttributeValue(new int[]{0x61, 0x62}); // "ab"
        startTag.newAttribute();

        assertEquals("http://ab", startTag.getAttributes().get("href"));
    }

    // Tests boolean attribute creation when no value or empty value is specified
    @Test
    public void testTag_newAttribute_booleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("disabled");
        startTag.newAttribute();

        assertTrue(startTag.getAttributes().hasKey("disabled"));
        assertEquals("", startTag.getAttributes().get("disabled"));
    }

    // Tests empty attribute value distinction from boolean attribute
    @Test
    public void testTag_newAttribute_emptyAttributeValue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");
        startTag.appendAttributeName("value");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        assertTrue(startTag.getAttributes().hasKey("value"));
        assertEquals("", startTag.getAttributes().get("value"));
    }

    // Tests regression defect jsoup-59: whitespace attribute name trimmed to empty string
    @Test
    public void testTag_newAttribute_whitespaceOnlyAttributeName_handledSafely() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("   ");
        startTag.appendAttributeValue("value");
        startTag.newAttribute();

        // Attribute name with only whitespace should not cause IllegalArgumentException
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests finaliseTag invokes newAttribute when pending attribute exists
    @Test
    public void testTag_finaliseTag_persistsPendingAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("meta");
        startTag.appendAttributeName("charset");
        startTag.appendAttributeValue("utf-8");
        startTag.finaliseTag();

        assertEquals("utf-8", startTag.getAttributes().get("charset"));
    }

    // Tests exception path when name() is called with null or empty tagName
    @Test(expected = IllegalArgumentException.class)
    public void testTag_name_emptyTagName_throwsException() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name();
    }

    // Tests Comment token methods and reset
    @Test
    public void testComment_dataAndReset_resetsData() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("sample comment");
        comment.bogus = true;

        assertEquals("sample comment", comment.getData());
        assertEquals("<!--sample comment-->", comment.toString());
        assertTrue(comment.bogus);

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
        assertEquals("<!---->", comment.toString());
    }

    // Tests Character token methods and reset
    @Test
    public void testCharacter_dataAndReset_resetsData() {
        Token.Character character = new Token.Character();
        character.data("text content");

        assertEquals("text content", character.getData());
        assertEquals("text content", character.toString());

        character.reset();
        assertNull(character.getData());
    }

    // Tests type checking and casting methods on Token
    @Test
    public void testToken_typeCheckingAndCasting_identifiesCorrectly() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertEquals(doctype, doctype.asDoctype());

        Token startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertEquals(startTag, startTag.asStartTag());

        Token endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertEquals(endTag, endTag.asEndTag());

        Token comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertEquals(comment, comment.asComment());

        Token character = new Token.Character();
        assertTrue(character.isCharacter());
        assertEquals(character, character.asCharacter());

        Token eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals(eof, eof.reset());
    }

    // Tests static reset helper method for StringBuilder
    @Test
    public void testResetStringBuilder_nullAndNonNull_clearsCorrectly() {
        Token.reset(null);

        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());
    }
}