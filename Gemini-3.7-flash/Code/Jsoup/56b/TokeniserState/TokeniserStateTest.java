package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TokeniserStateTest {

    private ParseErrorList errors;

    @Before
    public void setUp() {
        errors = ParseErrorList.tracking(100);
    }

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, errors);
    }

    // Tests Data state with plain text and tag open transition
    @Test
    public void testData_plainTextAndTagOpen_emitsDataAndTransitions() {
        CharacterReader r = new CharacterReader("text<");
        Tokeniser t = new Tokeniser(r, errors);

        TokeniserState.Data.read(t, r);
        assertEquals(TokeniserState.TagOpen, t.getState());
        Token.Character charToken = (Token.Character) t.read();
        assertEquals("text", charToken.getData());
    }

    // Tests Data state with null character error and emit
    @Test
    public void testData_nullChar_recordsErrorAndEmits() {
        CharacterReader r = new CharacterReader("\u0000");
        Tokeniser t = new Tokeniser(r, errors);

        TokeniserState.Data.read(t, r);
        assertFalse(errors.isEmpty());
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("\u0000", ((Token.Character) token).getData());
    }

    // Tests Data state with EOF
    @Test
    public void testData_eof_emitsEOFToken() {
        CharacterReader r = new CharacterReader("");
        Tokeniser t = new Tokeniser(r, errors);

        TokeniserState.Data.read(t, r);
        Token token = t.read();
        assertTrue(token.isEOF());
    }

    // Tests TagOpen state with start tag, end tag, comment, and bogus comment
    @Test
    public void testTagOpen_variousInputs_transitionsCorrectly() {
        // start tag
        CharacterReader r1 = new CharacterReader("div");
        Tokeniser t1 = new Tokeniser(r1, errors);
        TokeniserState.TagOpen.read(t1, r1);
        assertEquals(TokeniserState.TagName, t1.getState());
        assertTrue(t1.tagPending.isStartTag());

        // end tag
        CharacterReader r2 = new CharacterReader("/div");
        Tokeniser t2 = new Tokeniser(r2, errors);
        TokeniserState.TagOpen.read(t2, r2);
        assertEquals(TokeniserState.EndTagOpen, t2.getState());

        // markup declaration
        CharacterReader r3 = new CharacterReader("!DOCTYPE");
        Tokeniser t3 = new Tokeniser(r3, errors);
        TokeniserState.TagOpen.read(t3, r3);
        assertEquals(TokeniserState.MarkupDeclarationOpen, t3.getState());

        // bogus comment
        CharacterReader r4 = new CharacterReader("?xml");
        Tokeniser t4 = new Tokeniser(r4, errors);
        TokeniserState.TagOpen.read(t4, r4);
        assertEquals(TokeniserState.BogusComment, t4.getState());
    }

    // Tests EndTagOpen state with empty input, invalid char, and valid name
    @Test
    public void testEndTagOpen_branches_handledCorrectly() {
        // empty input
        CharacterReader r1 = new CharacterReader("");
        Tokeniser t1 = new Tokeniser(r1, errors);
        TokeniserState.EndTagOpen.read(t1, r1);
        assertEquals(TokeniserState.Data, t1.getState());
        assertFalse(errors.isEmpty());

        // invalid closing >
        CharacterReader r2 = new CharacterReader(">");
        Tokeniser t2 = new Tokeniser(r2, errors);
        TokeniserState.EndTagOpen.read(t2, r2);
        assertEquals(TokeniserState.Data, t2.getState());

        // bogus comment branch on special char
        CharacterReader r3 = new CharacterReader("123");
        Tokeniser t3 = new Tokeniser(r3, errors);
        TokeniserState.EndTagOpen.read(t3, r3);
        assertEquals(TokeniserState.BogusComment, t3.getState());
    }

    // Tests TagName state parsing and transitions on whitespace, slash, and closing bracket
    @Test
    public void testTagName_delimiters_transitionsCorrectly() {
        CharacterReader r = new CharacterReader("b attr=val");
        Tokeniser t = new Tokeniser(r, errors);
        t.createTagPending(true);

        TokeniserState.TagName.read(t, r);
        assertEquals("b", t.tagPending.name());
        assertEquals(TokeniserState.BeforeAttributeName, t.getState());
    }

    // Tests Attribute parsing with double quoted, single quoted, and unquoted values
    @Test
    public void testAttributeValues_quotesAndUnquoted_extractsCorrectValues() {
        // double quoted
        Tokeniser t1 = createTokeniser("<div id=\"test-id\">");
        Token token1 = t1.read();
        assertTrue(token1.isStartTag());
        assertEquals("test-id", token1.asStartTag().attributes.get("id"));

        // single quoted
        Tokeniser t2 = createTokeniser("<div class='item-class'>");
        Token token2 = t2.read();
        assertTrue(token2.isStartTag());
        assertEquals("item-class", token2.asStartTag().attributes.get("class"));

        // unquoted
        Tokeniser t3 = createTokeniser("<div data-val=12345>");
        Token token3 = t3.read();
        assertTrue(token3.isStartTag());
        assertEquals("12345", token3.asStartTag().attributes.get("data-val"));
    }

    // Tests SelfClosingStartTag state
    @Test
    public void testSelfClosingStartTag_valid_marksSelfClosing() {
        Tokeniser t = createTokeniser("<img src=\"test.png\" />");
        Token token = t.read();
        assertTrue(token.isStartTag());
        assertTrue(token.asStartTag().isSelfClosing());
    }

    // Tests MarkupDeclarationOpen for DOCTYPE, Comments, and CDATA
    @Test
    public void testMarkupDeclarationOpen_cdataAndComment_transitionsCorrectly() {
        // comment start
        CharacterReader r1 = new CharacterReader("-- comment -->");
        Tokeniser t1 = new Tokeniser(r1, errors);
        TokeniserState.MarkupDeclarationOpen.read(t1, r1);
        assertEquals(TokeniserState.CommentStart, t1.getState());

        // CDATA section
        CharacterReader r2 = new CharacterReader("[CDATA[some data]]>");
        Tokeniser t2 = new Tokeniser(r2, errors);
        TokeniserState.MarkupDeclarationOpen.read(t2, r2);
        assertEquals(TokeniserState.CdataSection, t2.getState());
    }

    // Tests Doctype parsing with PUBLIC and SYSTEM identifiers
    @Test
    public void testDoctype_publicAndSystemIdentifiers_parsedCorrectly() {
        String doctypeHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        Tokeniser t = createTokeniser(doctypeHtml);
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("-//W3C//DTD HTML 4.01//EN", doctype.getPublicIdentifier());
        assertEquals("http://www.w3.org/TR/html4/strict.dtd", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype parsing with system identifier only
    @Test
    public void testDoctype_systemOnlyIdentifier_parsedCorrectly() {
        String doctypeHtml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\">";
        Tokeniser t = createTokeniser(doctypeHtml);
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("about:legacy-compat", doctype.getSystemIdentifier());
        assertFalse(doctype.isForceQuirks());
    }

    // Tests Doctype with single quoted identifiers
    @Test
    public void testDoctype_singleQuotedIdentifiers_parsedCorrectly() {
        String doctypeHtml = "<!DOCTYPE html PUBLIC 'pub-id' 'sys-id'>";
        Tokeniser t = createTokeniser(doctypeHtml);
        Token token = t.read();
        assertTrue(token.isDoctype());
        Token.Doctype doctype = token.asDoctype();
        assertEquals("html", doctype.getName());
        assertEquals("pub-id", doctype.getPublicIdentifier());
        assertEquals("sys-id", doctype.getSystemIdentifier());
    }

    // Tests Doctype force quirks on malformed input
    @Test
    public void testDoctype_malformed_setsForceQuirks() {
        String doctypeHtml = "<!DOCTYPE>";
        Tokeniser t = createTokeniser(doctypeHtml);
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertTrue(token.asDoctype().isForceQuirks());
    }

    // Tests Comment state handling dashes, bang, and end
    @Test
    public void testComment_variousEndings_parsedProperly() {
        Tokeniser t1 = createTokeniser("<!-- simple comment -->");
        Token token1 = t1.read();
        assertTrue(token1.isComment());
        assertEquals(" simple comment ", token1.asComment().getData());

        Tokeniser t2 = createTokeniser("<!-- comment with --!>");
        Token token2 = t2.read();
        assertTrue(token2.isComment());
    }

    // Tests ScriptData escaping and double escaping
    @Test
    public void testScriptData_escapedContent_emitsCorrectTokens() {
        CharacterReader r = new CharacterReader("var x = '<!-- <script>';</script>");
        Tokeniser t = new Tokeniser(r, errors);
        t.transition(TokeniserState.ScriptData);

        Token token = t.read();
        assertNotNull(token);
    }

    // Tests Rcdata and Rawtext states
    @Test
    public void testRcdataAndRawtext_characterReferencesAndEndTags_handledCorrectly() {
        // Rcdata with entity
        CharacterReader r1 = new CharacterReader("&amp;</title>");
        Tokeniser t1 = new Tokeniser(r1, errors);
        t1.transition(TokeniserState.Rcdata);
        Token token1 = t1.read();
        assertTrue(token1.isCharacter());

        // Rawtext
        CharacterReader r2 = new CharacterReader("raw content</style>");
        Tokeniser t2 = new Tokeniser(r2, errors);
        t2.transition(TokeniserState.Rawtext);
        Token token2 = t2.read();
        assertTrue(token2.isCharacter());
        assertEquals("raw content", ((Token.Character) token2).getData());
    }

    // Tests PLAINTEXT state reading until EOF
    @Test
    public void testPLAINTEXT_readsAllContent_emitsCorrectToken() {
        CharacterReader r = new CharacterReader("some plain text <tag> &amp;");
        Tokeniser t = new Tokeniser(r, errors);
        TokeniserState.PLAINTEXT.read(t, r);

        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("some plain text <tag> &amp;", ((Token.Character) token).getData());
    }

    // Tests BogusComment state parsing
    @Test
    public void testBogusComment_malformedDeclaration_emitsBogusComment() {
        CharacterReader r = new CharacterReader("!invalid comment>rest");
        Tokeniser t = new Tokeniser(r, errors);
        r.advance(); // simulate leading character consumed before bogus comment

        TokeniserState.BogusComment.read(t, r);
        Token token = t.read();
        assertTrue(token.isComment());
        assertTrue(token.asComment().bogus);
    }

    // Tests CdataSection state reading and emitting character tokens until CDATA end
    @Test
    public void testCdataSection_readsUntilClosingBracket() {
        CharacterReader r = new CharacterReader("CDATA text content]]>after");
        Tokeniser t = new Tokeniser(r, errors);
        t.transition(TokeniserState.CdataSection);

        TokeniserState.CdataSection.read(t, r);
        Token token = t.read();
        assertTrue(token.isCharacter());
        assertEquals("CDATA text content", ((Token.Character) token).getData());
        assertEquals(TokeniserState.Data, t.getState());
    }

    // Tests AfterDoctypePublicIdentifier transitions
    @Test
    public void testAfterDoctypePublicIdentifier_transitions() {
        // transition to System Identifier (single quoted)
        CharacterReader r1 = new CharacterReader(" 'sys-id'");
        Tokeniser t1 = new Tokeniser(r1, errors);
        t1.createDoctypePending();
        t1.transition(TokeniserState.AfterDoctypePublicIdentifier);

        TokeniserState.AfterDoctypePublicIdentifier.read(t1, r1);
        assertEquals(TokeniserState.DoctypeSystemIdentifier_singleQuoted, t1.getState());

        // transition with closing >
        CharacterReader r2 = new CharacterReader(">");
        Tokeniser t2 = new Tokeniser(r2, errors);
        t2.createDoctypePending();
        t2.transition(TokeniserState.AfterDoctypePublicIdentifier);

        TokeniserState.AfterDoctypePublicIdentifier.read(t2, r2);
        assertEquals(TokeniserState.Data, t2.getState());
    }

    // Tests AfterDoctypeSystemIdentifier transitions
    @Test
    public void testAfterDoctypeSystemIdentifier_transitions() {
        CharacterReader r = new CharacterReader(">");
        Tokeniser t = new Tokeniser(r, errors);
        t.createDoctypePending();
        t.transition(TokeniserState.AfterDoctypeSystemIdentifier);

        TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);
        assertEquals(TokeniserState.Data, t.getState());
    }

    // Tests BeforeDoctypeName handling EOF and invalid transitions
    @Test
    public void testBeforeDoctypeName_eof_setsQuirksAndEmits() {
        CharacterReader r = new CharacterReader("");
        Tokeniser t = new Tokeniser(r, errors);
        t.createDoctypePending();
        t.transition(TokeniserState.BeforeDoctypeName);

        TokeniserState.BeforeDoctypeName.read(t, r);
        Token token = t.read();
        assertTrue(token.isDoctype());
        assertTrue(token.asDoctype().isForceQuirks());
    }

    // Tests DoctypeSystemIdentifier single and double quoted parsing
    @Test
    public void testDoctypeSystemIdentifier_parsing() {
        CharacterReader r = new CharacterReader("system-literal'>");
        Tokeniser t = new Tokeniser(r, errors);
        t.createDoctypePending();
        t.transition(TokeniserState.DoctypeSystemIdentifier_singleQuoted);

        TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, r);
        assertEquals("system-literal", t.doctypePending.getSystemIdentifier());
        assertEquals(TokeniserState.AfterDoctypeSystemIdentifier, t.getState());
    }
}