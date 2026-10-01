package org.jsoup.parser;

import static org.junit.Assert.*;
import org.junit.Test;
import org.jsoup.nodes.Attributes;

/**
 * JUnit 4 test class for Token and its inner classes.
 * Focuses on reset, attribute handling, type checking, and basic construction.
 */
public class TokenTest {

    // 1. Doctype reset and getters
    // Tests normal case: set values, reset, verify initial state
    @Test
    public void testDoctypeReset_afterSet_resetToInitialState() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.pubSysKey = "SYSTEM";
        doctype.publicIdentifier.append("publicID");
        doctype.systemIdentifier.append("systemID");
        doctype.forceQuirks = true;

        doctype.reset();

        assertEquals("", doctype.getName());
        assertNull(doctype.getPubSysKey());
        assertEquals("", doctype.getPublicIdentifier());
        assertEquals("", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // 2. StartTag reset and nameAttr
    @Test
    public void testStartTagResetAndNameAttr() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("div");
        tag.newAttribute();
        tag.reset();
        assertNull(tag.tagName);
        assertNull(tag.normalName);
        assertNotNull(tag.attributes);

        Attributes attrs = new Attributes();
        attrs.put("class", "test");
        Token.StartTag named = new Token.StartTag().nameAttr("span", attrs);
        assertEquals("span", named.name());
        assertTrue(named.toString().contains("class=\"test\""));
    }

    // 3. EndTag toString
    // Tests normal case
    @Test
    public void testEndTagToString() {
        Token.EndTag tag = new Token.EndTag();
        tag.name("div");
        assertEquals("</div>", tag.toString());
    }

    // 4. newAttribute with null pending attribute name (no-op)
    // Tests false branch of if (pendingAttributeName != null)
    @Test
    public void testTagNewAttribute_nullPendingName_noEffect() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.newAttribute(); // should not throw, attributes remain unchanged
        assertNotNull(tag.getAttributes());
    }

    // 5. newAttribute with pendingAttributeName and value
    // Tests normal path, value from pendingAttributeValueS
    @Test
    public void testTagNewAttribute_withValue_createsAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("id");
        tag.appendAttributeValue("main");
        tag.newAttribute();
        assertTrue(tag.toString().contains("id=\"main\""));
    }

    // 6. newAttribute with empty attribute value (setEmptyAttributeValue)
    // Tests hasEmptyAttributeValue path
    @Test
    public void testTagNewAttribute_emptyValue_createsEmptyAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("data-empty");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertTrue(tag.toString().contains("data-empty=\"\""));
    }

    // 7. newAttribute with no value (boolean attribute)
    // Tests value = null path
    @Test
    public void testTagNewAttribute_noValue_createsBooleanAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("disabled");
        tag.newAttribute(); // no value set, so value = null
        String result = tag.toString();
        assertTrue(result.contains("disabled"));
        assertFalse(result.contains("disabled="));
    }

    // 8. finaliseTag
    // Tests that pending attribute is added when finaliseTag called
    @Test
    public void testTagFinaliseTag_addsPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("foo");
        tag.finaliseTag();
        assertTrue(tag.toString().contains("class=\"foo\""));
    }

    // 9. tagName() normal case
    @Test
    public void testTagName_normalCase_returnsExactName() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("myTag");
        assertEquals("myTag", tag.name());
    }

    // 10. tagName() with null tagName throws
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_nullTagName_throwsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset(); // sets tagName to null
        tag.name();
    }

    // 11. tagName() with empty tagName throws
    @Test(expected = IllegalArgumentException.class)
    public void testTagName_emptyTagName_throwsException() {
        Token.StartTag tag = new Token.StartTag();
        tag.tagName = "";
        tag.name();
    }

    // 12. normalName() after name()
    @Test
    public void testNormalName_afterName_lowerCased() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("Test");
        assertEquals("test", tag.normalName());
    }

    // 13. appendTagName
    @Test
    public void testAppendTagName_appendsCorrectly() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("ab");
        tag.appendTagName("cd");
        assertEquals("abcd", tag.tagName);
        assertEquals("abcd", tag.name());
        assertEquals("abcd", tag.normalName());
    }

    // 14. appendAttributeName
    @Test
    public void testAppendAttributeName_concatAndCreatesAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("a");
        tag.appendAttributeName("data");
        tag.appendAttributeName("-name");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertTrue(tag.toString().contains("data-name=\"\""));
    }

    // 15. setEmptyAttributeValue
    @Test
    public void testSetEmptyAttributeValue_emptyValueOnNextNewAttr() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("a");
        tag.appendAttributeName("empty");
        tag.setEmptyAttributeValue();
        tag.newAttribute();
        assertTrue(tag.toString().contains("empty=\"\""));
    }

    // 16. appendAttributeValue single String
    @Test
    public void testAppendAttributeValue_singleString_createsAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("test");
        tag.newAttribute();
        assertTrue(tag.toString().contains("class=\"test\""));
    }

    // 17. appendAttributeValue multiple Strings (concatenation)
    @Test
    public void testAppendAttributeValue_multipleStrings_concatenated() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("p");
        tag.appendAttributeName("id");
        tag.appendAttributeValue("first");
        tag.appendAttributeValue("second");
        tag.newAttribute();
        assertTrue(tag.toString().contains("id=\"firstsecond\""));
    }

    // 18. appendAttributeValue with char, char[], int[]
    @Test
    public void testAppendAttributeValue_charAndArrays_characterAppended() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("data-val");
        tag.appendAttributeValue('x');
        tag.appendAttributeValue(new char[]{'y', 'z'});
        tag.appendAttributeValue(new int[]{0x41}); // 'A'
        tag.newAttribute();
        assertTrue(tag.toString().contains("data-val=\"xyzA\""));
    }

    // 19. Comment, Character, CData reset and toString
    @Test
    public void testCommentAndCharacterAndCData() {
        // Comment
        Token.Comment comment = new Token.Comment();
        comment.data.append("text");
        comment.bogus = true;
        comment.reset();
        assertEquals("", comment.getData());
        assertFalse(comment.bogus);

        // Character
        Token.Character character = new Token.Character();
        character.data("data");
        character.reset();
        assertNull(character.getData());

        // CData
        Token.CData cdata = new Token.CData("content");
        assertEquals("<![CDATA[content]]>", cdata.toString());

        // Character on CData
        assertTrue(cdata.isCharacter());
        assertTrue(cdata.isCData());
        assertNotNull(cdata.asCharacter());
    }

    // 20. Type checking and casting (Doctype, StartTag, EndTag, Comment, EOF)
    @Test
    public void testTypeCheckingAndCasting() {
        Token.Doctype doctype = new Token.Doctype();
        assertTrue(doctype.isDoctype());
        assertFalse(doctype.isStartTag());
        assertFalse(doctype.isEndTag());
        assertFalse(doctype.isComment());
        assertFalse(doctype.isCharacter());
        assertFalse(doctype.isCData());
        assertFalse(doctype.isEOF());
        assertSame(doctype, doctype.asDoctype());

        Token.StartTag startTag = new Token.StartTag();
        assertTrue(startTag.isStartTag());
        assertSame(startTag, startTag.asStartTag());

        Token.EndTag endTag = new Token.EndTag();
        assertTrue(endTag.isEndTag());
        assertSame(endTag, endTag.asEndTag());

        Token.Comment comment = new Token.Comment();
        assertTrue(comment.isComment());
        assertSame(comment, comment.asComment());

        Token.CData cdata = new Token.CData("x");
        assertTrue(cdata.isCData());
        assertTrue(cdata.isCharacter());
        assertTrue(cdata.asCharacter() instanceof Token.CData);

        Token.EOF eof = new Token.EOF();
        assertTrue(eof.isEOF());
        assertSame(eof, eof.reset());
    }

    // ----- Additional test cases to improve coverage -----

    // 21. Self-closing StartTag
    @Test
    public void testStartTagSelfClosing() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("br");
        tag.selfClosing = true;
        assertTrue(tag.isSelfClosing());
        String str = tag.toString();
        assertTrue("Expected self-closing tag", str.contains("/>"));
    }

    // 22. Doctype toString
    @Test
    public void testDoctypeToString() {
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.pubSysKey = "SYSTEM";
        doctype.systemIdentifier.append("about:legacy");
        String str = doctype.toString();
        assertTrue(str.contains("<!DOCTYPE html SYSTEM"));
        assertTrue(str.contains("about:legacy"));
    }

    // 23. Comment toString
    @Test
    public void testCommentToString() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("hello");
        assertEquals("<!--hello-->", comment.toString());
    }

    // 24. Character toString
    @Test
    public void testCharacterToString() {
        Token.Character character = new Token.Character();
        character.data("test");
        assertEquals("test", character.toString());
    }

    // 25. EOF toString
    @Test
    public void testEOFToString() {
        Token.EOF eof = new Token.EOF();
        assertEquals("#eof", eof.toString());
    }

    // 26. Character isCharacter, isCData, asCharacter
    @Test
    public void testCharacterIsCharacterAndIsCData() {
        Token.Character c = new Token.Character();
        assertTrue(c.isCharacter());
        assertFalse(c.isCData());
        assertSame(c, c.asCharacter());
    }

    // 27. normalName caching
    @Test
    public void testNormalNameCaching() {
        Token.StartTag tag = new Token.StartTag();
        tag.name("Test");
        String first = tag.normalName();
        String second = tag.normalName();
        assertEquals("test", first);
        assertEquals("test", second);
        // after modifying tagName, cache is cleared
        tag.appendTagName("2");
        assertEquals("test2", tag.normalName());
    }

    // 28. normalName throws on null tagName
    @Test(expected = IllegalArgumentException.class)
    public void testNormalNameThrowsOnNull() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset(); // sets tagName to null
        tag.normalName();
    }

    // 29. normalName throws on empty tagName
    @Test(expected = IllegalArgumentException.class)
    public void testNormalNameThrowsOnEmpty() {
        Token.StartTag tag = new Token.StartTag();
        tag.tagName = "";
        tag.normalName();
    }

    // 30. finaliseTag with no pending attribute
    @Test
    public void testFinaliseTagNoPendingAttribute() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("span");
        tag.finaliseTag();
        assertEquals("<span>", tag.toString());
    }

    // 31. newAttribute with only char[] value
    @Test
    public void testNewAttributeWithCharArrayOnly() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("a");
        tag.appendAttributeName("href");
        tag.appendAttributeValue(new char[]{'h', 't', 'm', 'l'});
        tag.newAttribute();
        assertTrue(tag.toString().contains("href=\"html\""));
    }

    // 32. newAttribute with only int[] value
    @Test
    public void testNewAttributeWithIntArrayOnly() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("a");
        tag.appendAttributeName("data");
        tag.appendAttributeValue(new int[]{0x48, 0x49}); // HI
        tag.newAttribute();
        assertTrue(tag.toString().contains("data=\"HI\""));
    }

    // 33. setEmptyAttributeValue then appendAttributeValue (empty should win)
    @Test
    public void testSetEmptyAttributeValueThenAppendValue() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("class");
        tag.setEmptyAttributeValue();
        tag.appendAttributeValue("shouldBeIgnored");
        tag.newAttribute();
        assertTrue("Expected empty value", tag.toString().contains("class=\"\""));
        assertFalse("Appended value should be ignored", tag.toString().contains("shouldBeIgnored"));
    }

    // 34. appendAttributeValue char multiple times
    @Test
    public void testAppendAttributeValueCharMultipleTimes() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("p");
        tag.appendAttributeName("id");
        tag.appendAttributeValue('a');
        tag.appendAttributeValue('b');
        tag.newAttribute();
        assertTrue(tag.toString().contains("id=\"ab\""));
    }

    // 35. Multiple attributes
    @Test
    public void testMultipleAttributes() {
        Token.StartTag tag = new Token.StartTag();
        tag.reset();
        tag.name("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("foo");
        tag.newAttribute();
        tag.appendAttributeName("id");
        tag.appendAttributeValue("bar");
        tag.newAttribute();
        assertTrue(tag.toString().contains("class=\"foo\""));
        assertTrue(tag.toString().contains("id=\"bar\""));
    }

    // 36. Incorrect type casting returns null
    @Test
    public void testIncorrectTypeCastingReturnsNull() {
        Token.EOF eof = new Token.EOF();
        assertNull(eof.asStartTag());
        assertNull(eof.asEndTag());
        assertNull(eof.asComment());
        assertNull(eof.asDoctype());
        assertNull(eof.asCharacter());

        Token.StartTag start = new Token.StartTag();
        assertNull(start.asEndTag());
        assertNull(start.asComment());
        assertNull(start.asDoctype());
        assertNull(start.asCharacter());
        assertNull(start.asEOF()); // if asEOF() exists, otherwise omit
    }

    // 37. Comment getData before reset
    @Test
    public void testCommentGetDataBeforeReset() {
        Token.Comment comment = new Token.Comment();
        comment.data.append("test");
        assertEquals("test", comment.getData());
    }

    // 38. Character getData before reset
    @Test
    public void testCharacterGetDataBeforeReset() {
        Token.Character c = new Token.Character();
        c.data("value");
        assertEquals("value", c.getData());
    }

    // 39. EndTag with attribute (should not affect toString)
    @Test
    public void testEndTagNewAttribute() {
        Token.EndTag tag = new Token.EndTag();
        tag.name("div");
        tag.appendAttributeName("class");
        tag.appendAttributeValue("foo");
        tag.newAttribute();
        // EndTag ignores attributes in toString
        assertEquals("</div>", tag.toString());
    }
}