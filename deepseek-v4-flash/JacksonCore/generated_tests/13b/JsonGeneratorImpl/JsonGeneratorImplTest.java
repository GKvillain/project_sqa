package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharTypes;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonGeneratorImplTest {
    private JsonFactory factory;
    private ByteArrayOutputStream out;
    private JsonGenerator gen;
    private JsonGeneratorImpl genImpl;

    @Before
    public void setUp() throws IOException {
        factory = new JsonFactory();
        out = new ByteArrayOutputStream();
        gen = factory.createGenerator(out);
        assertTrue("Generator should be instance of JsonGeneratorImpl", gen instanceof JsonGeneratorImpl);
        genImpl = (JsonGeneratorImpl) gen;
    }

    @After
    public void tearDown() throws IOException {
        if (gen != null) {
            gen.close();
        }
    }

    // Tests constructor initial state with default features
    @Test
    public void testConstructor_initialConfig_expectedDefaults() {
        assertFalse(genImpl._cfgUnqNames);
        assertEquals(0, genImpl._maximumNonEscapedChar);
        assertNull(genImpl._characterEscapes);
        assertSame(CharTypes.get7BitOutputEscapes(), genImpl._outputEscapes);
        assertSame(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, genImpl._rootValueSeparator);
    }

    // Tests constructor when ESCAPE_NON_ASCII enabled
    @Test
    public void testConstructor_escapeNonAsciiEnabled_setsMaxNonEscapedChar127() throws IOException {
        JsonFactory factory2 = new JsonFactory();
        factory2.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory2.createGenerator(out2);
        JsonGeneratorImpl impl2 = (JsonGeneratorImpl) gen2;
        assertEquals(127, impl2._maximumNonEscapedChar);
        gen2.close();
    }

    // Tests enable with QUOTE_FIELD_NAMES when already enabled (no change)
    @Test
    public void testEnable_quoteFieldNamesAlreadyEnabled_noChange() {
        genImpl.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(genImpl._cfgUnqNames);
    }

    // Tests enable with QUOTE_FIELD_NAMES when initially disabled
    @Test
    public void testEnable_quoteFieldNamesDisabledThenEnabled_setsUnqNamesFalse() throws IOException {
        JsonFactory factory2 = new JsonFactory();
        factory2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory2.createGenerator(out2);
        JsonGeneratorImpl impl2 = (JsonGeneratorImpl) gen2;
        assertTrue(impl2._cfgUnqNames);
        impl2.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(impl2._cfgUnqNames);
        gen2.close();
    }

    // Tests enable with non-QUOTE_FIELD_NAMES feature does not affect _cfgUnqNames
    @Test
    public void testEnable_nonQuoteFieldNamesFeature_doesNotChangeUnqNames() throws IOException {
        JsonFactory factory2 = new JsonFactory();
        factory2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory2.createGenerator(out2);
        JsonGeneratorImpl impl2 = (JsonGeneratorImpl) gen2;
        assertTrue(impl2._cfgUnqNames);
        impl2.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(impl2._cfgUnqNames);
        impl2.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(impl2._cfgUnqNames);
        gen2.close();
    }

    // Tests disable of QUOTE_FIELD_NAMES (triggers _checkStdFeatureChanges)
    @Test
    public void testDisable_quoteFieldNames_setsUnqNamesTrue() throws IOException {
        JsonFactory factory2 = new JsonFactory();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory2.createGenerator(out2);
        JsonGeneratorImpl impl2 = (JsonGeneratorImpl) gen2;
        assertFalse(impl2._cfgUnqNames);
        gen2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(impl2._cfgUnqNames);
        gen2.close();
    }

    // Tests setHighestNonEscapedChar with negative value (boundary)
    @Test
    public void testSetHighestNonEscapedChar_negativeValue_setsZero() {
        genImpl.setHighestNonEscapedChar(-1);
        assertEquals(0, genImpl._maximumNonEscapedChar);
        assertEquals(0, genImpl.getHighestEscapedChar());
    }

    // Tests setHighestNonEscapedChar with zero
    @Test
    public void testSetHighestNonEscapedChar_zeroValue_setsZero() {
        genImpl.setHighestNonEscapedChar(0);
        assertEquals(0, genImpl._maximumNonEscapedChar);
    }

    // Tests setHighestNonEscapedChar with positive value (127)
    @Test
    public void testSetHighestNonEscapedChar_positiveValue_setsValue() {
        genImpl.setHighestNonEscapedChar(127);
        assertEquals(127, genImpl._maximumNonEscapedChar);
    }

    // Tests setHighestNonEscapedChar with maximum allowed value (65535)
    @Test
    public void testSetHighestNonEscapedChar_maxValue_setsValue() {
        genImpl.setHighestNonEscapedChar(65535);
        assertEquals(65535, genImpl._maximumNonEscapedChar);
    }

    // Tests getHighestEscapedChar initial value
    @Test
    public void testGetHighestEscapedChar_initial_returnsZero() {
        assertEquals(0, genImpl.getHighestEscapedChar());
    }

    // Tests setCharacterEscapes with null reverts to default escapes
    @Test
    public void testSetCharacterEscapes_null_revertsToDefaultEscapes() {
        int[] defaultEscapes = CharTypes.get7BitOutputEscapes();
        CharacterEscapes customEscapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] esc = new int[128];
                for (int i = 0; i < 128; i++) esc[i] = CharacterEscapes.ESCAPE_STANDARD;
                return esc;
            }
            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null;
            }
        };
        genImpl.setCharacterEscapes(customEscapes);
        assertNotNull(genImpl._characterEscapes);
        assertNotSame(defaultEscapes, genImpl._outputEscapes);
        genImpl.setCharacterEscapes(null);
        assertNull(genImpl._characterEscapes);
        assertSame(defaultEscapes, genImpl._outputEscapes);
    }

    // Tests setCharacterEscapes with custom escapes
    @Test
    public void testSetCharacterEscapes_custom_usesCustomEscapes() {
        CharacterEscapes customEscapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] esc = new int[128];
                esc['a'] = 'A';
                return esc;
            }
            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null;
            }
        };
        genImpl.setCharacterEscapes(customEscapes);
        assertSame(customEscapes, genImpl._characterEscapes);
        assertArrayEquals(customEscapes.getEscapeCodesForAscii(), genImpl._outputEscapes);
    }

    // Tests getCharacterEscapes initially
    @Test
    public void testGetCharacterEscapes_initial_returnsNull() {
        assertNull(genImpl.getCharacterEscapes());
    }

    // Tests setRootValueSeparator
    @Test
    public void testSetRootValueSeparator_customSeparator_setsAndReturns() {
        SerializableString sep = new SerializableString() {
            @Override
            public String getValue() {
                return "---";
            }
            @Override
            public int charLength() {
                return 3;
            }
            @Override
            public char[] asQuotedChars() {
                return "---".toCharArray();
            }
            @Override
            public byte[] asUnquotedUTF8() {
                return "---".getBytes();
            }
            @Override
            public byte[] asQuotedUTF8() {
                return "---".getBytes();
            }
        };
        genImpl.setRootValueSeparator(sep);
        assertSame(sep, genImpl._rootValueSeparator);
    }

    // Tests version() returns non-null version
    @Test
    public void testVersion_returnsNonNullVersion() {
        assertNotNull(genImpl.version());
    }

    // Tests writeStringField with default quoting
    @Test
    public void testWriteStringField_normalInput_writesCorrectly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory.createGenerator(bos);
        gen2.writeStartObject();
        gen2.writeStringField("name", "value");
        gen2.writeEndObject();
        gen2.close();
        String json = bos.toString("UTF-8");
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"value\""));
    }

    // Tests writeStringField with unquoted field names
    @Test
    public void testWriteStringField_unquotedFieldNames_writesWithoutQuotes() throws IOException {
        JsonFactory factory2 = new JsonFactory();
        factory2.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        JsonGenerator gen2 = factory2.createGenerator(bos2);
        gen2.writeStartObject();
        gen2.writeStringField("name", "value");
        gen2.writeEndObject();
        gen2.close();
        String json = bos2.toString("UTF-8");
        assertTrue(json.contains("name") && !json.contains("\"name\""));
        assertTrue(json.contains("\"value\""));
    }

    // ==================== New tests to cover missing areas ====================

    // Tests writeString with ESCAPE_NON_ASCII enabled
    @Test
    public void testWriteStringWithEscapeNonAscii() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = f.createGenerator(bos);
        g.writeStartArray();
        g.writeString("Hello\u00e9World");
        g.writeEndArray();
        g.close();
        String json = bos.toString("UTF-8");
        assertTrue("Expected escape sequence \\u00E9 or \\u00e9", 
            json.contains("\\u00E9") || json.contains("\\u00e9"));
        assertFalse("Should not contain raw é", json.contains("é"));
    }

    // Tests writeString with custom character escapes and verifies output
    @Test
    public void testWriteStringWithCustomCharacterEscapes() throws IOException {
        CharacterEscapes customEscapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] esc = CharTypes.get7BitOutputEscapes();
                esc['x'] = 'X'; // placeholder, actual escape sequence used below
                return esc;
            }
            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'x') {
                    return new SerializableString() {
                        @Override
                        public String getValue() { return "\\u0078"; }
                        @Override
                        public int charLength() { return 6; }
                        @Override
                        public char[] asQuotedChars() { return "\\u0078".toCharArray(); }
                        @Override
                        public byte[] asUnquotedUTF8() { return "\\u0078".getBytes(); }
                        @Override
                        public byte[] asQuotedUTF8() { return "\\u0078".getBytes(); }
                    };
                }
                return null;
            }
        };
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = new JsonFactory().createGenerator(bos);
        g.setCharacterEscapes(customEscapes);
        g.writeStartArray();
        g.writeString("axb");
        g.writeEndArray();
        g.close();
        String json = bos.toString("UTF-8");
        assertTrue("Expected escape \\u0078 for 'x'", json.contains("\\u0078"));
        assertFalse("Should not contain raw 'x'", json.contains("x"));
    }

    // Tests that root value separator is written between multiple root values
    @Test
    public void testRootValueSeparatorInSequence() throws IOException {
        SerializableString sep = new SerializableString() {
            @Override
            public String getValue() { return "***"; }
            @Override
            public int charLength() { return 3; }
            @Override
            public char[] asQuotedChars() { return "***".toCharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return "***".getBytes(); }
            @Override
            public byte[] asQuotedUTF8() { return "***".getBytes(); }
        };
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = new JsonFactory().createGenerator(bos);
        g.setRootValueSeparator(sep);
        g.writeStartObject();
        g.writeEndObject();
        g.writeStartObject();
        g.writeEndObject();
        g.close();
        String json = bos.toString("UTF-8").trim();
        assertEquals("{}***{}", json);
    }

    // Tests writeNumber, writeBoolean and writeNull
    @Test
    public void testWriteNumberAndBoolean() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = new JsonFactory().createGenerator(bos);
        g.writeStartArray();
        g.writeNumber(42);
        g.writeNumber(-3.14);
        g.writeBoolean(true);
        g.writeBoolean(false);
        g.writeNull();
        g.writeEndArray();
        g.close();
        String json = bos.toString("UTF-8");
        assertTrue(json.contains("42"));
        assertTrue(json.contains("-3.14"));
        assertTrue(json.contains("true"));
        assertTrue(json.contains("false"));
        assertTrue(json.contains("null"));
    }

    // Tests writeString with setHighestNonEscapedChar causing escape
    @Test
    public void testWriteStringWithHighestNonEscapedChar() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = new JsonFactory().createGenerator(bos);
        g.setHighestNonEscapedChar(127);
        g.writeStartArray();
        g.writeString("a\u00E9b");
        g.writeEndArray();
        g.close();
        String json = bos.toString("UTF-8");
        assertTrue("Expected escape \\u00E9", json.contains("\\u00E9") || json.contains("\\u00e9"));
    }

    // Tests nested object writing (writeStartObject/writeEndObject stack)
    @Test
    public void testNestedObjectWriting() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = new JsonFactory().createGenerator(bos);
        g.writeStartObject();
        g.writeFieldName("outer");
        g.writeStartObject();
        g.writeStringField("inner", "value");
        g.writeEndObject();
        g.writeEndObject();
        g.close();
        String json = bos.toString("UTF-8");
        assertTrue(json.contains("\"outer\""));
        assertTrue(json.contains("\"inner\""));
        assertTrue(json.contains("\"value\""));
    }
}