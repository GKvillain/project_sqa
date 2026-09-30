package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParseSettingsTest {

    // Tests construction with both case preservation flags set to false (HTML default)
    @Test
    public void testConstructor_htmlDefault_preserveTagAndAttributeFalse() {
        ParseSettings settings = new ParseSettings(false, false);
        assertFalse(settings.preserveTagCase());
        // attribute case preservation is not directly exposed, but indirectly tested via normalizeAttribute
        assertEquals("test", settings.normalizeAttribute("Test"));
    }

    // Tests construction with both case preservation flags set to true (preserveCase)
    @Test
    public void testConstructor_preserveCase_preserveTagAndAttributeTrue() {
        ParseSettings settings = new ParseSettings(true, true);
        assertTrue(settings.preserveTagCase());
        assertEquals("Test", settings.normalizeAttribute("Test"));
    }

    // Tests normal case for preserveTagCase() when true
    @Test
    public void testPreserveTagCase_true_returnsTrue() {
        assertTrue(ParseSettings.preserveCase.preserveTagCase());
    }

    // Tests normal case for preserveTagCase() when false
    @Test
    public void testPreserveTagCase_false_returnsFalse() {
        assertFalse(ParseSettings.htmlDefault.preserveTagCase());
    }

    // Tests normalizeTag with tag case preservation true (preserveCase)
    @Test
    public void testNormalizeTag_preserveTagCaseTrue_returnsSameCase() {
        String result = ParseSettings.preserveCase.normalizeTag("DIV");
        assertEquals("DIV", result);
    }

    // Tests normalizeTag with tag case preservation false (htmlDefault)
    @Test
    public void testNormalizeTag_preserveTagCaseFalse_returnsLowerCase() {
        String result = ParseSettings.htmlDefault.normalizeTag("DIV");
        assertEquals("div", result);
    }

    // Tests normalizeTag with already lowercased input and false preservation
    @Test
    public void testNormalizeTag_lowercaseInput_false_returnsSame() {
        String result = ParseSettings.htmlDefault.normalizeTag("div");
        assertEquals("div", result);
    }

    // Tests normalizeTag with whitespace trimming (boundary: leading/trailing spaces)
    @Test
    public void testNormalizeTag_inputWithSpaces_trimmedAndLowerCased() {
        String result = ParseSettings.htmlDefault.normalizeTag("  DIV  ");
        assertEquals("div", result);
    }

    // Tests normalizeTag with empty string input (edge case)
    @Test
    public void testNormalizeTag_emptyInput_returnsEmpty() {
        String result = ParseSettings.htmlDefault.normalizeTag("");
        assertEquals("", result);
    }

    // Tests normalizeTag with mixed case and preservation false
    @Test
    public void testNormalizeTag_mixedCasePreserveFalse_returnsLowerCase() {
        String result = ParseSettings.htmlDefault.normalizeTag("DiV");
        assertEquals("div", result);
    }

    // Tests normalizeAttribute with attribute case preservation true (preserveCase)
    @Test
    public void testNormalizeAttribute_preserveAttributeCaseTrue_returnsSameCase() {
        String result = ParseSettings.preserveCase.normalizeAttribute("CLASS");
        assertEquals("CLASS", result);
    }

    // Tests normalizeAttribute with attribute case preservation false (htmlDefault)
    @Test
    public void testNormalizeAttribute_preserveAttributeCaseFalse_returnsLowerCase() {
        String result = ParseSettings.htmlDefault.normalizeAttribute("CLASS");
        assertEquals("class", result);
    }

    // Tests normalizeAttribute with whitespace trimming (boundary: leading/trailing spaces)
    @Test
    public void testNormalizeAttribute_inputWithSpaces_trimmedAndLowerCased() {
        String result = ParseSettings.htmlDefault.normalizeAttribute("  ID  ");
        assertEquals("id", result);
    }

    // Tests normalizeAttribute with empty string input (edge case)
    @Test
    public void testNormalizeAttribute_emptyInput_returnsEmpty() {
        String result = ParseSettings.htmlDefault.normalizeAttribute("");
        assertEquals("", result);
    }

    // Tests normalizeAttributes with preserveAttributeCase false (htmlDefault): should call normalize on attributes
    @Test
    public void testNormalizeAttributes_preserveFalse_callsNormalize() {
        ParseSettings settings = new ParseSettings(true, false);
        Attributes attrs = new Attributes();
        attrs.add("Class", "test");
        attrs.add("class", "other");
        Attributes result = settings.normalizeAttributes(attrs);
        // After normalization, only 'class' should remain (due to lowercasing/deduplication)
        assertEquals("other", result.get("class"));
        assertFalse(result.hasKey("Class"));
    }

    // Tests normalizeAttributes with preserveAttributeCase true (preserveCase): should not modify attributes
    @Test
    public void testNormalizeAttributes_preserveTrue_doesNotModify() {
        ParseSettings settings = new ParseSettings(true, true);
        Attributes attrs = new Attributes();
        attrs.add("Class", "test");
        Attributes result = settings.normalizeAttributes(attrs);
        assertTrue(result.hasKey("Class"));
        assertEquals("test", result.get("Class"));
    }

    // Tests normalizeAttributes with null or empty attributes (regression)
    @Test
    public void testNormalizeAttributes_nullInput_doesNotThrow() {
        ParseSettings settings = ParseSettings.htmlDefault;
        Attributes result = settings.normalizeAttributes(null);
        assertNull(result);
    }
}