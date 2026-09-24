package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokenTest {

    // Tests Doctype token getters, flags, and reset functionality
    @Test
    public void testDoctype_stateAndReset_correctValues() {
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

    // Tests StartTag name handling with case preservation and normalisation
    @Test
    public void testStartTag_nameAndNormalName_preservesCaseAndLowercases() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("DIV");

        assertEquals("DIV", startTag.name());
        assertEquals("div", startTag.normalName());
        assertFalse(startTag.isSelfClosing());
    }

    // Tests exception thrown when name() is invoked with null or empty tagName
    @Test(expected = IllegalArgumentException.class)
    public void testStartTag_nameWhenEmpty_throwsException() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name();
    }

    // Tests tag name incremental appending with string and character
    @Test
    public void testStartTag_appendTagName_accumulatesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("sp");
        startTag.appendTagName('a');
        startTag.appendTagName("n");

        assertEquals("span", startTag.name());
        assertEquals("span", startTag.normalName());
    }

    // Tests attribute name and value appending through multiple types (string, char, char[], codepoints)
    @Test
    public void testStartTag_attributeAccumulation_createsAttributesCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");

        startTag.appendAttributeName("hr");
        startTag.appendAttributeName('e');
        startTag.appendAttributeName("f");

        startTag.appendAttributeValue("http://");
        startTag.appendAttributeValue('e');
        startTag.appendAttributeValue(new char[]{'x', 'a', 'm'});
        startTag.appendAttributeValue(new int[]{0x70, 0x6C, 0x65}); // "ple"

        startTag.finaliseTag();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertEquals("http://example", attrs.get("href"));
    }

    // Tests empty attribute value vs null (boolean) attribute value
    @Test
    public void testStartTag_emptyAndBooleanAttributeValues_setsExpectedValues() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("input");

        startTag.appendAttributeName("disabled");
        startTag.newAttribute();

        startTag.appendAttributeName("value");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        Attributes attrs = startTag.getAttributes();
        assertNotNull(attrs);
        assertEquals("", attrs.get("disabled"));
        assertEquals("", attrs.get("value"));
    }

    // Tests whitespace-only pending attribute name is trimmed and ignored
    @Test
    public void testStartTag_whitespaceAttributeName_isIgnored() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("   ");
        startTag.appendAttributeValue("val");
        startTag.finaliseTag();

        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests StartTag nameAttr factory-style initialization and toString
    @Test
    public void testStartTag_nameAttrAndToString_formatsHtmlTag() {
        Token.StartTag startTag = new Token.StartTag();
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        startTag.nameAttr("div", attrs);

        assertEquals("<div id=\"main\">", startTag.toString());

        Token.StartTag emptyAttrTag = new Token.StartTag();
        emptyAttrTag.name("p");
        assertEquals("<p>", emptyAttrTag.toString());
    }

    // Tests StartTag reset clears attributes, name, and pending states
    @Test
    public void testStartTag_reset_clearsAllState() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();
        startTag.selfClosing = true;

        startTag.reset();

        assertNull(startTag.tagName);
        assertNull(startTag.normalName());
        assertFalse(startTag.isSelfClosing());
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests EndTag toString, name, and reset
    @Test
    public void testEndTag_toStringAndReset_outputsClosingTag() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");

        assertEquals("</div>", endTag.toString());
        assertTrue(endTag.isEndTag());
        assertEquals(endTag, endTag.asEndTag());

        endTag.reset();
        assertNull(endTag.tagName);
    }

    // Tests Comment token creation, data accumulation, toString, and reset
    @Test
    public void testComment_dataToStringAndReset_behavesCorrectly() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("This is a comment");
        comment.bogus = true;

        assertEquals("This is a comment", comment.getData());
        assertEquals("<!--This is a comment-->", comment.toString());
        assertTrue(comment.isComment());
        assertEquals(comment, comment.asComment());

        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);
    }

    // Tests Character and CData token creation, data, toString, and reset
    @Test
    public void testCharacterAndCData_dataAndToString_returnsCorrectStrings() {
        Token.Character character = new Token.Character();
        character.data("text content");

        assertEquals("text content", character.getData());
        assertEquals("text content", character.toString());
        assertTrue(character.isCharacter());
        assertFalse(character.isCData());
        assertEquals(character, character.asCharacter());

        character.reset();
        assertNull(character.getData());

        Token.CData cdata = new Token.CData("cdata content");
        assertEquals("cdata content", cdata.getData());
        assertEquals("<![CDATA[cdata content]]>", cdata.toString());
        assertTrue(cdata.isCharacter());
        assertTrue(cdata.isCData());
    }

    // Tests EOF token type check and reset
    @Test
    public void testEOF_typeAndReset_behavesCorrectly() {
        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertEquals(Token.TokenType.EOF, eof.type);
        assertSame(eof, eof.reset());
    }

    // Tests type checking methods on various Token subclasses
    @Test
    public void testToken_typeChecksAndCasting_identifiesCorrectSubclass() {
        Token doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertEquals(doctype, doctype.asDoctype());

        Token startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertFalse(startTag.isEndTag());
        assertEquals(startTag, startTag.asStartTag());

        Token endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertFalse(endTag.isComment());
        assertEquals(endTag, endTag.asEndTag());

        Token comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertFalse(comment.isCharacter());
        assertEquals(comment, comment.asComment());
    }

    // Tests static reset helper with null and non-null StringBuilder
    @Test
    public void testToken_staticResetStringBuilder_clearsBuilderOrIgnoresNull() {
        StringBuilder sb = new StringBuilder("content");
        Token.reset(sb);
        assertEquals(0, sb.length());

        Token.reset((StringBuilder) null); // should not throw NPE
    }

    // Tests tokenType() on all token subclasses
    @Test
    public void testTokenType_allTokenClasses_returnExpectedNames() {
        assertEquals("StartTag", new Token.StartTag().tokenType());
        assertEquals("EndTag", new Token.EndTag().tokenType());
        assertEquals("Comment", new Token.Comment().tokenType());
        assertEquals("Character", new Token.Character().tokenType());
        assertEquals("EOF", new Token.EOF().tokenType());
    }

    // Tests newAttribute without a pending attribute name does nothing
    @Test
    public void testStartTag_newAttributeWithoutPendingName_doesNotCreateAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.newAttribute();
        startTag.finaliseTag();
        assertEquals(0, startTag.getAttributes().size());
    }

    // Tests transition when appending string multiple times to attribute value
    @Test
    public void testStartTag_multipleStringAppendsToAttributeValue_combinesValues() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        startTag.appendAttributeName("class");
        startTag.appendAttributeValue("foo ");
        startTag.appendAttributeValue("bar");
        startTag.finaliseTag();

        assertEquals("foo bar", startTag.getAttributes().get("class"));
    }

    // Tests EndTag attribute appending and finalisation
    @Test
    public void testEndTag_attributeHandling_populatesAttributes() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        endTag.appendAttributeName("id");
        endTag.appendAttributeValue("test");
        endTag.finaliseTag();

        assertEquals("test", endTag.getAttributes().get("id"));
        assertEquals("</div id=\"test\">", endTag.toString());
    }
}