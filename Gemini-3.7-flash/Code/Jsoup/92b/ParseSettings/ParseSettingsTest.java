package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParseSettingsTest {

    // Tests static htmlDefault configuration flags
    @Test
    public void testHtmlDefault_staticField_bothFlagsAreFalse() {
        assertNotNull(ParseSettings.htmlDefault);
        assertFalse(ParseSettings.htmlDefault.preserveTagCase());
    }

    // Tests static preserveCase configuration flags
    @Test
    public void testPreserveCase_staticField_bothFlagsAreTrue() {
        assertNotNull(ParseSettings.preserveCase);
        assertTrue(ParseSettings.preserveCase.preserveTagCase());
    }

    // Tests custom constructor and preserveTagCase getter with true
    @Test
    public void testPreserveTagCase_tagCaseTrue_returnsTrue() {
        ParseSettings settings = new ParseSettings(true, false);
        assertTrue(settings.preserveTagCase());
    }

    // Tests custom constructor and preserveTagCase getter with false
    @Test
    public void testPreserveTagCase_tagCaseFalse_returnsFalse() {
        ParseSettings settings = new ParseSettings(false, true);
        assertFalse(settings.preserveTagCase());
    }

    // Tests normalizeTag when preserveTagCase is false (should trim and lower-case)
    @Test
    public void testNormalizeTag_preserveTagCaseFalse_returnsTrimmedLowerCase() {
        ParseSettings settings = new ParseSettings(false, false);
        assertEquals("div", settings.normalizeTag("  DIV  "));
        assertEquals("p", settings.normalizeTag("P"));
        assertEquals("custom-tag", settings.normalizeTag("Custom-Tag"));
    }

    // Tests normalizeTag when preserveTagCase is true (should trim but preserve case)
    @Test
    public void testNormalizeTag_preserveTagCaseTrue_returnsTrimmedOriginalCase() {
        ParseSettings settings = new ParseSettings(true, false);
        assertEquals("DIV", settings.normalizeTag("  DIV  "));
        assertEquals("Custom-Tag", settings.normalizeTag("  Custom-Tag  "));
        assertEquals("p", settings.normalizeTag("p"));
    }

    // Tests normalizeTag with empty and already normalized strings
    @Test
    public void testNormalizeTag_emptyString_returnsEmptyString() {
        ParseSettings settings = new ParseSettings(false, false);
        assertEquals("", settings.normalizeTag("   "));
        assertEquals("", settings.normalizeTag(""));
    }

    // Tests normalizeAttribute when preserveAttributeCase is false (should trim and lower-case)
    @Test
    public void testNormalizeAttribute_preserveAttributeCaseFalse_returnsTrimmedLowerCase() {
        ParseSettings settings = new ParseSettings(false, false);
        assertEquals("id", settings.normalizeAttribute("  ID  "));
        assertEquals("href", settings.normalizeAttribute("HREF"));
        assertEquals("data-name", settings.normalizeAttribute("Data-Name"));
    }

    // Tests normalizeAttribute when preserveAttributeCase is true (should trim but preserve case)
    @Test
    public void testNormalizeAttribute_preserveAttributeCaseTrue_returnsTrimmedOriginalCase() {
        ParseSettings settings = new ParseSettings(false, true);
        assertEquals("ID", settings.normalizeAttribute("  ID  "));
        assertEquals("Data-Name", settings.normalizeAttribute("  Data-Name  "));
        assertEquals("class", settings.normalizeAttribute("class"));
    }

    // Tests normalizeAttribute with empty string
    @Test
    public void testNormalizeAttribute_emptyString_returnsEmptyString() {
        ParseSettings settings = new ParseSettings(false, false);
        assertEquals("", settings.normalizeAttribute("   "));
        assertEquals("", settings.normalizeAttribute(""));
    }

    // Tests normalizeAttributes when preserveAttributeCase is false (should normalize attribute keys)
    @Test
    public void testNormalizeAttributes_preserveAttributeCaseFalse_normalizesKeysToLowerCase() {
        ParseSettings settings = new ParseSettings(false, false);
        Attributes attributes = new Attributes();
        attributes.put("HREF", "http://example.com");
        attributes.put("TITLE", "Example");

        Attributes result = settings.normalizeAttributes(attributes);

        assertNotNull(result);
        assertTrue(result.hasKey("href"));
        assertTrue(result.hasKey("title"));
        assertFalse(result.hasKey("HREF"));
        assertFalse(result.hasKey("TITLE"));
        assertEquals("http://example.com", result.get("href"));
        assertEquals("Example", result.get("title"));
    }

    // Tests normalizeAttributes when preserveAttributeCase is true (should preserve attribute keys)
    @Test
    public void testNormalizeAttributes_preserveAttributeCaseTrue_preservesOriginalKeys() {
        ParseSettings settings = new ParseSettings(false, true);
        Attributes attributes = new Attributes();
        attributes.put("HREF", "http://example.com");
        attributes.put("TITLE", "Example");

        Attributes result = settings.normalizeAttributes(attributes);

        assertNotNull(result);
        assertTrue(result.hasKey("HREF"));
        assertTrue(result.hasKey("TITLE"));
        assertEquals("http://example.com", result.get("HREF"));
        assertEquals("Example", result.get("TITLE"));
    }

    // Tests normalizeAttributes with an empty Attributes object
    @Test
    public void testNormalizeAttributes_emptyAttributes_returnsEmptyAttributes() {
        ParseSettings settings = new ParseSettings(false, false);
        Attributes attributes = new Attributes();

        Attributes result = settings.normalizeAttributes(attributes);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // Tests preserveAttributeCase on static htmlDefault
    @Test
    public void testHtmlDefault_preserveAttributeCase_returnsFalse() {
        assertFalse(ParseSettings.htmlDefault.preserveAttributeCase());
    }

    // Tests preserveAttributeCase on static preserveCase
    @Test
    public void testPreserveCase_preserveAttributeCase_returnsTrue() {
        assertTrue(ParseSettings.preserveCase.preserveAttributeCase());
    }

    // Tests preserveAttributeCase getter when initialized with true
    @Test
    public void testPreserveAttributeCase_attributeCaseTrue_returnsTrue() {
        ParseSettings settings = new ParseSettings(false, true);
        assertTrue(settings.preserveAttributeCase());
    }

    // Tests preserveAttributeCase getter when initialized with false
    @Test
    public void testPreserveAttributeCase_attributeCaseFalse_returnsFalse() {
        ParseSettings settings = new ParseSettings(true, false);
        assertFalse(settings.preserveAttributeCase());
    }

    // Tests normalizeAttributes with null input
    @Test
    public void testNormalizeAttributes_nullInput_returnsNull() {
        ParseSettings settingsFalse = new ParseSettings(false, false);
        assertNull(settingsFalse.normalizeAttributes(null));

        ParseSettings settingsTrue = new ParseSettings(false, true);
        assertNull(settingsTrue.normalizeAttributes(null));
    }
}