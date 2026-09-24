package org.jfree.chart.imagemap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StandardToolTipTagFragmentGeneratorTest {

    private StandardToolTipTagFragmentGenerator generator;

    @Before
    public void setUp() {
        this.generator = new StandardToolTipTagFragmentGenerator();
    }

    // Tests instance creation
    @Test
    public void testConstructor_default_createsInstance() {
        assertNotNull(this.generator);
    }

    // Tests normal text fragment generation without special characters
    @Test
    public void testGenerateToolTipFragment_plainText_returnsCorrectTag() {
        String result = this.generator.generateToolTipFragment("Series 1");
        assertEquals(" title=\"Series 1\" alt=\"\"", result);
    }

    // Tests empty string fragment generation
    @Test
    public void testGenerateToolTipFragment_emptyString_returnsEmptyTitleTag() {
        String result = this.generator.generateToolTipFragment("");
        assertEquals(" title=\"\" alt=\"\"", result);
    }

    // Tests string containing double quotes to ensure HTML escaping (Defects4J Chart-10)
    @Test
    public void testGenerateToolTipFragment_withDoubleQuotes_escapesQuotes() {
        String result = this.generator.generateToolTipFragment("Series \"A\"");
        assertEquals(" title=\"Series &quot;A&quot;\" alt=\"\"", result);
    }

    // Tests string containing ampersand to ensure HTML escaping
    @Test
    public void testGenerateToolTipFragment_withAmpersand_escapesAmpersand() {
        String result = this.generator.generateToolTipFragment("Income & Expenses");
        assertEquals(" title=\"Income &amp; Expenses\" alt=\"\"", result);
    }

    // Tests string containing less-than and greater-than characters
    @Test
    public void testGenerateToolTipFragment_withTags_escapesAngleBrackets() {
        String result = this.generator.generateToolTipFragment("<Category 1>");
        assertEquals(" title=\"&lt;Category 1&gt;\" alt=\"\"", result);
    }

    // Tests string containing single quote / apostrophe
    @Test
    public void testGenerateToolTipFragment_withSingleQuote_escapesApostrophe() {
        String result = this.generator.generateToolTipFragment("User's Data");
        assertEquals(" title=\"User&#39;s Data\" alt=\"\"", result);
    }

    // Tests string containing multiple special characters mixed together
    @Test
    public void testGenerateToolTipFragment_mixedSpecialChars_escapesAllSpecialChars() {
        String result = this.generator.generateToolTipFragment("\"A\" & 'B' <C>");
        assertEquals(" title=\"&quot;A&quot; &amp; &#39;B&#39; &lt;C&gt;\" alt=\"\"", result);
    }

    // Tests string containing only a double quote
    @Test
    public void testGenerateToolTipFragment_onlyDoubleQuote_escapesProperly() {
        String result = this.generator.generateToolTipFragment("\"");
        assertEquals(" title=\"&quot;\" alt=\"\"", result);
    }
}