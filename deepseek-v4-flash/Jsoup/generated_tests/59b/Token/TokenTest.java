package org.jsoup.parser;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * JUnit 4 test class for org.jsoup.parser.Token.
 */
public class TokenTest {

    // Tests reset of Doctype token
    @Test
    public void testDoctypeReset_resetToken_fieldsAreCleared() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.pubSysKey = "PUBLIC";
        doctype.publicIdentifier.append("-//W3C//DTD HTML 4.01//EN");
        doctype.systemIdentifier.append("http://www.w3.org/TR/html4/strict.dtd");
        doctype.forceQuirks = true;

        doctype.reset();

        assertEquals(0, doctype.name.length());
        assertNull(doctype.pubSysKey);
        assertEquals(0, doctype.publicIdentifier.length());
        assertEquals(0, doctype.systemIdentifier.length());
        assertFalse(doctype.forceQuirks);
    }

    // Tests getter methods of Doctype after reset
    @Test
    public void testDoctypeGetters_doctypeAfterReset_returnsEmptyStrings() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.reset();

        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests that Doctype fields hold set values
    @Test
    public void testDoctypeGetters_doctypeWithValues_returnsCorrectValues() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("HTML");
        doctype.pubSysKey = "SYSTEM";
        doctype.publicIdentifier.append("identifier");
        doctype.systemIdentifier.append("systemId");
        doctype.forceQuirks = true;

        assertEquals("HTML", doctype.getName());
        assertEquals("SYSTEM", doctype.getPubSysKey());
        assertEquals("identifier", doctype.getPublicIdentifier());
        assertEquals("systemId", doctype.getSystemIdentifier());
        assertTrue(doctype.isForceQuirks());
    }

    // Tests reset of Comment token
    @Test
    public void testCommentReset_resetToken_fieldsAreCleared() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("some data");
        comment.bogus = true;

        comment.reset();

        assertEquals(0, comment.data.length());
        assertFalse(comment.bogus);
    }

    // Tests Comment getData after reset
    @Test
    public void testCommentGetData_commentAfterReset_returnsEmptyString() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("test");
        comment.reset();

        assertEquals("", comment.getData());
    }

    // Tests Comment getData with value set
    @Test
    public void testCommentGetData_commentWithData_returnsData() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("test data");

        assertEquals("test data", comment.getData());
    }

    // Tests Comment toString
    @Test
    public void testCommentToString_commentWithData_returnsExpectedString() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("test");

        assertEquals("<!--test-->", comment.toString());
    }

    // Tests reset of Character token
    @Test
    public void testCharacterReset_resetToken_dataIsNull() {
        Token.Character character = new Token.Character();
        character.data("dataValue");

        character.reset();

        assertNull(character.getData());
    }

    // Tests Character data setter and getter
    @Test
    public void testCharacterData_dataSetter_returnsSameData() {
        Token.Character character = new Token.Character();
        character.data("hello");

        assertEquals("hello", character.getData());
    }

    // Tests Character toString
    @Test
    public void testCharacterToString_characterWithData_returnsData() {
        Token.Character character = new Token.Character();
        character.data("text");

        assertEquals("text", character.toString());
    }

    // Tests reset of EOF token
    @Test
    public void testEOFReset_resetToken_returnsThis() {
        Token.EOF eof = new Token.EOF();
        Token result = eof.reset();

        assertSame(eof, result);
    }

    // Tests StartTag toString with no attributes
    @Test
    public void testStartTagToString_noAttributes_returnsSimpleTag() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");

        assertEquals("<div>", startTag.toString());
    }

    // Tests StartTag toString with attributes
    @Test
    public void testStartTagToString_withAttributes_returnsTagWithAttributes() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("a");
        startTag.attributes.put("href", "http://example.com");

        assertEquals("<a href=\"http://example.com\">", startTag.toString());
    }

    // Tests EndTag toString
    @Test
    public void testEndTagToString_endTag_returnsClosingTag() {
        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");

        assertEquals("</div>", endTag.toString());
    }

    // Tests Tag name setter and getter
    @Test
    public void testTagName_nameSet_returnsSetName() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("Test");

        assertEquals("Test", startTag.name());
        assertEquals("test", startTag.normalName());
    }

    // Tests Tag isSelfClosing
    @Test
    public void testTagSelfClosing_selfClosingSet_returnsTrue() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.selfClosing = true;

        assertTrue(startTag.isSelfClosing());
    }

    // Tests Tag finaliseTag when pendingAttributeName is null
    @Test
    public void testTagFinaliseTag_noPendingAttributeName_doesNotAddAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.finaliseTag();

        assertNull(startTag.getAttributes().get("any"));
    }

    // Tests Tag finaliseTag when pendingAttributeName is not null
    @Test
    public void testTagFinaliseTag_withPendingAttributeName_addsBooleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("disabled");
        startTag.finaliseTag();

        assertNotNull(startTag.getAttributes());
        assertTrue(startTag.getAttributes().hasKey("disabled"));
    }

    // Tests newAttribute with empty attribute value (BooleanAttribute)
    @Test
    public void testTagNewAttribute_withEmptyValue_createsBooleanAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("checked");
        startTag.setEmptyAttributeValue();
        startTag.newAttribute();

        assertTrue(startTag.getAttributes().hasKey("checked"));
    }

    // Tests newAttribute with pending attribute value via StringBuilder
    @Test
    public void testTagNewAttribute_withValueViaBuilder_createsAttribute() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("class");
        startTag.appendAttributeValue("main");
        startTag.newAttribute();

        assertEquals("main", startTag.getAttributes().get("class"));
    }

    // Tests appendTagName with null tagName
    @Test
    public void testTagAppendTagName_nullTagName_appendsSuccessfully() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendTagName("div");

        assertEquals("div", startTag.name());
    }

    // Tests appendTagName with existing tagName
    @Test
    public void testTagAppendTagName_existingTagName_appendsSuccessfully() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("di");
        startTag.appendTagName("v");

        assertEquals("div", startTag.name());
    }

    // Tests appendAttributeValue with char[] array
    @Test
    public void testTagAppendAttributeValue_charArray_appendsCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("id");
        startTag.appendAttributeValue(new char[] {'m', 'a', 'i', 'n'});
        startTag.newAttribute();

        assertEquals("main", startTag.getAttributes().get("id"));
    }

    // Tests appendAttributeValue with int[] codepoints
    @Test
    public void testTagAppendAttributeValue_intArrayCodepoints_appendsCorrectly() {
        Token.StartTag startTag = new Token.StartTag();
        startTag.appendAttributeName("data-val");
        startTag.appendAttributeValue(new int[] {65, 66, 67}); // A, B, C
        startTag.newAttribute();

        assertEquals("ABC", startTag.getAttributes().get("data-val"));
    }

    // Tests tokenType for each token subtype
    @Test
    public void testTokenType_doctype_returnsDoctype() {
        Token.Doctype doctype = new Token.Doctype();
        assertEquals("Doctype", doctype.tokenType());
    }

    @Test
    public void testTokenType_comment_returnsComment() {
        Token.Comment comment = new Token.Comment();
        assertEquals("Comment", comment.tokenType());
    }

    @Test
    public void testTokenType_character_returnsCharacter() {
        Token.Character character = new Token.Character();
        assertEquals("Character", character.tokenType());
    }

    @Test
    public void testTokenType_eof_returnsEOF() {
        Token.EOF eof = new Token.EOF();
        assertEquals("EOF", eof.tokenType());
    }
}