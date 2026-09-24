package com.fasterxml.jackson.core.util;

import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;

public class DefaultPrettyPrinterTest
{
    private final JsonFactory JSON_F = new JsonFactory();

    // Tests default constructor initialization and default instance creation
    @Test
    public void testConstructors_defaultState_createsProperDefaults() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter copy = pp.createInstance();
        assertNotNull(copy);
        assertNotSame(pp, copy);
    }

    // Tests constructor with String root separator (non-null and null)
    @Test
    public void testConstructor_stringRootSeparator_setsSeparatorCorrectly() throws Exception {
        DefaultPrettyPrinter pp1 = new DefaultPrettyPrinter("\n");
        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = JSON_F.createGenerator(sw1);
        pp1.writeRootValueSeparator(g1);
        g1.flush();
        assertEquals("\n", sw1.toString());

        DefaultPrettyPrinter pp2 = new DefaultPrettyPrinter((String) null);
        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = JSON_F.createGenerator(sw2);
        pp2.writeRootValueSeparator(g2);
        g2.flush();
        assertEquals("", sw2.toString());
    }

    // Tests withRootSeparator method with same and different values
    @Test
    public void testWithRootSeparator_sameAndDifferentValues_returnsExpectedInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(" ");
        assertSame(pp, pp.withRootSeparator(" "));
        assertSame(pp, pp.withRootSeparator(new SerializedString(" ")));

        DefaultPrettyPrinter pp2 = pp.withRootSeparator("\n\n");
        assertNotSame(pp, pp2);

        DefaultPrettyPrinter pp3 = pp.withRootSeparator((String) null);
        assertNotSame(pp, pp3);
        assertSame(pp3, pp3.withRootSeparator((SerializableString) null));
    }

    // Tests indentArraysWith and withArrayIndenter including null handling
    @Test
    public void testWithArrayIndenter_variousIndenters_updatesOrReturnsNewInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultPrettyPrinter.FixedSpaceIndenter();

        DefaultPrettyPrinter pp2 = pp.withArrayIndenter(indenter);
        assertSame(pp2, pp2.withArrayIndenter(indenter));

        DefaultPrettyPrinter ppNull = pp.withArrayIndenter(null);
        assertNotSame(pp, ppNull);

        pp.indentArraysWith(null);
        pp.indentArraysWith(indenter);
    }

    // Tests indentObjectsWith and withObjectIndenter including null handling
    @Test
    public void testWithObjectIndenter_variousIndenters_updatesOrReturnsNewInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", "\n");

        DefaultPrettyPrinter pp2 = pp.withObjectIndenter(indenter);
        assertSame(pp2, pp2.withObjectIndenter(indenter));

        DefaultPrettyPrinter ppNull = pp.withObjectIndenter(null);
        assertNotSame(pp, ppNull);

        pp.indentObjectsWith(null);
        pp.indentObjectsWith(indenter);
    }

    // Tests withSpacesInObjectEntries and withoutSpacesInObjectEntries toggle
    @Test
    public void testSpacesInObjectEntries_toggleState_returnsCorrectInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withSpacesInObjectEntries());

        DefaultPrettyPrinter withoutSpaces = pp.withoutSpacesInObjectEntries();
        assertNotSame(pp, withoutSpaces);
        assertSame(withoutSpaces, withoutSpaces.withoutSpacesInObjectEntries());

        DefaultPrettyPrinter withSpaces = withoutSpaces.withSpacesInObjectEntries();
        assertNotSame(withoutSpaces, withSpaces);
    }

    // Tests withSeparators updates separator configuration
    @Test
    public void testWithSeparators_customSeparators_appliesCustomSeparators() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        Separators seps = new Separators().withObjectFieldValueSeparator('=');
        DefaultPrettyPrinter pp2 = pp.withSeparators(seps);
        assertSame(pp, pp2);

        StringWriter sw = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(sw);
        pp2.writeObjectFieldValueSeparator(g);
        g.flush();
        assertEquals(" = ", sw.toString());
    }

    // Tests empty object and populated object generation with and without spaces
    @Test
    public void testObjectFormatting_emptyAndPopulated_formatsCorrectly() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(new DefaultIndenter("  ", "\n"));

        // Empty object
        StringWriter swEmpty = new StringWriter();
        JsonGenerator gEmpty = JSON_F.createGenerator(swEmpty);
        pp.writeStartObject(gEmpty);
        pp.writeEndObject(gEmpty, 0);
        gEmpty.flush();
        assertEquals("{ }", swEmpty.toString());

        // Populated object with spaces
        StringWriter swPop = new StringWriter();
        JsonGenerator gPop = JSON_F.createGenerator(swPop);
        pp.writeStartObject(gPop);
        pp.beforeObjectEntries(gPop);
        gPop.writeRaw("\"key1\"");
        pp.writeObjectFieldValueSeparator(gPop);
        gPop.writeRaw("\"value1\"");
        pp.writeObjectEntrySeparator(gPop);
        gPop.writeRaw("\"key2\"");
        pp.writeObjectFieldValueSeparator(gPop);
        gPop.writeRaw("\"value2\"");
        pp.writeEndObject(gPop, 2);
        gPop.flush();
        assertEquals("{\n  \"key1\" : \"value1\",\n  \"key2\" : \"value2\"\n}", swPop.toString());

        // Populated object without spaces
        DefaultPrettyPrinter ppNoSpaces = pp.withoutSpacesInObjectEntries();
        StringWriter swNoSpaces = new StringWriter();
        JsonGenerator gNoSpaces = JSON_F.createGenerator(swNoSpaces);
        ppNoSpaces.writeStartObject(gNoSpaces);
        ppNoSpaces.beforeObjectEntries(gNoSpaces);
        gNoSpaces.writeRaw("\"k\"");
        ppNoSpaces.writeObjectFieldValueSeparator(gNoSpaces);
        gNoSpaces.writeRaw("\"v\"");
        ppNoSpaces.writeEndObject(gNoSpaces, 1);
        gNoSpaces.flush();
        assertEquals("{\n  \"k\": \"v\"\n}", swNoSpaces.toString());
    }

    // Tests empty array and populated array generation
    @Test
    public void testArrayFormatting_emptyAndPopulated_formatsCorrectly() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(new DefaultIndenter("  ", "\n"));

        // Empty array
        StringWriter swEmpty = new StringWriter();
        JsonGenerator gEmpty = JSON_F.createGenerator(swEmpty);
        pp.writeStartArray(gEmpty);
        pp.writeEndArray(gEmpty, 0);
        gEmpty.flush();
        assertEquals("[ ]", swEmpty.toString());

        // Populated array
        StringWriter swPop = new StringWriter();
        JsonGenerator gPop = JSON_F.createGenerator(swPop);
        pp.writeStartArray(gPop);
        pp.beforeArrayValues(gPop);
        gPop.writeRaw("1");
        pp.writeArrayValueSeparator(gPop);
        gPop.writeRaw("2");
        pp.writeEndArray(gPop, 2);
        gPop.flush();
        assertEquals("[\n  1,\n  2\n]", swPop.toString());
    }

    // Tests NopIndenter and FixedSpaceIndenter helper classes
    @Test
    public void testHelperIndenters_behaviorAndInlineStatus_behaveCorrectly() throws Exception {
        DefaultPrettyPrinter.NopIndenter nop = DefaultPrettyPrinter.NopIndenter.instance;
        assertTrue(nop.isInline());
        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = JSON_F.createGenerator(sw1);
        nop.writeIndentation(g1, 5);
        g1.flush();
        assertEquals("", sw1.toString());

        DefaultPrettyPrinter.FixedSpaceIndenter fixed = DefaultPrettyPrinter.FixedSpaceIndenter.instance;
        assertTrue(fixed.isInline());
        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = JSON_F.createGenerator(sw2);
        fixed.writeIndentation(g2, 5);
        g2.flush();
        assertEquals(" ", sw2.toString());
    }

    // Tests createInstance behavior with subclass of DefaultPrettyPrinter
    @Test
    public void testSubclassCreateInstance_customSubclass_createsOrFailsProperly() {
        class CustomPrettyPrinter extends DefaultPrettyPrinter {
            public CustomPrettyPrinter() {
                super();
            }
        }
        CustomPrettyPrinter custom = new CustomPrettyPrinter();
        try {
            DefaultPrettyPrinter result = custom.createInstance();
            assertNotNull(result);
        } catch (IllegalStateException e) {
            // In fixed versions, creating instance without overriding throws IllegalStateException
            assertTrue(e.getMessage().contains("must override createInstance()"));
        }
    }

    // Tests copy constructors and SerializableString constructor
    @Test
    public void testCopyConstructors_andSerializableStringConstructor() {
        SerializableString rootSep = new SerializedString("::");
        DefaultPrettyPrinter pp1 = new DefaultPrettyPrinter(rootSep);
        DefaultPrettyPrinter ppCopy = new DefaultPrettyPrinter(pp1);
        assertNotNull(ppCopy);

        DefaultPrettyPrinter ppCopyRoot = new DefaultPrettyPrinter(pp1, new SerializedString(","));
        assertNotNull(ppCopyRoot);
    }

    // Tests deprecated spacesInObjectEntries method
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedSpacesInObjectEntries() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.spacesInObjectEntries(false);
        DefaultPrettyPrinter noSpaces = pp.withoutSpacesInObjectEntries();
        assertSame(pp, noSpaces);

        pp.spacesInObjectEntries(true);
        DefaultPrettyPrinter withSpaces = pp.withSpacesInObjectEntries();
        assertSame(pp, withSpaces);
    }

    // Tests nested structures formatting
    @Test
    public void testNestedStructureFormatting() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(new DefaultIndenter("  ", "\n"));
        pp.indentArraysWith(new DefaultIndenter("  ", "\n"));

        StringWriter sw = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(sw);

        pp.writeStartObject(g);
        pp.beforeObjectEntries(g);
        g.writeRaw("\"arr\"");
        pp.writeObjectFieldValueSeparator(g);

        pp.writeStartArray(g);
        pp.beforeArrayValues(g);
        g.writeRaw("1");
        pp.writeEndArray(g, 1);

        pp.writeEndObject(g, 1);
        g.flush();

        String expected = "{\n  \"arr\" : [\n    1\n  ]\n}";
        assertEquals(expected, sw.toString());
    }
}