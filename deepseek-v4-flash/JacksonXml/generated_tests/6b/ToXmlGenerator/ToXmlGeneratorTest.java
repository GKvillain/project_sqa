package com.fasterxml.jackson.dataformat.xml.ser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ToXmlGeneratorTest {

    private ByteArrayOutputStream out;
    private XMLStreamWriter xmlWriter;
    private ToXmlGenerator gen;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
        gen = null;
    }

    @After
    public void tearDown() throws Exception {
        if (gen != null) {
            gen.close();
        }
        xmlWriter.close();
    }

    private ToXmlGenerator createGenerator() {
        return createGenerator(0, 0, null);
    }

    private ToXmlGenerator createGenerator(int stdFeatures, int xmlFeatures, PrettyPrinter pp) {
        IOContext ioContext = new IOContext(null, new Object(), false);
        gen = new ToXmlGenerator(ioContext, stdFeatures, xmlFeatures, null, xmlWriter);
        if (pp != null) {
            gen.setPrettyPrinter(pp);
        }
        return gen;
    }

    private ToXmlGenerator createGeneratorWithPretty() {
        return createGenerator(0, 0, new DefaultXmlPrettyPrinter());
    }

    private String getOutput() {
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    // Tests

    // Tests initGenerator with no feature set -> no declaration
    @Test
    public void testInitGenerator_FeatureOff_doesNotWriteDeclaration() throws Exception {
        gen = createGenerator(0, 0, null);
        gen.initGenerator();
        gen.close();
        assertTrue("Expected no XML declaration", getOutput().isEmpty());
    }

    // Tests initGenerator with WRITE_XML_DECLARATION enabled
    @Test
    public void testInitGenerator_WRITE_XML_DECLARATION_writesDeclaration() throws Exception {
        gen = createGenerator(0, ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), null);
        gen.initGenerator();
        gen.close();
        String output = getOutput();
        assertTrue("Should contain XML declaration", output.contains("<?xml version='1.0' encoding='UTF-8'?>"));
    }

    // Tests initGenerator with WRITE_XML_1_1 enabled
    @Test
    public void testInitGenerator_WRITE_XML_1_1_writesDeclarationVersion1_1() throws Exception {
        gen = createGenerator(0, ToXmlGenerator.Feature.WRITE_XML_1_1.getMask(), null);
        gen.initGenerator();
        gen.close();
        String output = getOutput();
        assertTrue("Should contain version 1.1", output.contains("1.1"));
    }

    // Tests writeString normal element path
    @Test
    public void testWriteString_normalElement_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("elem"));
        gen.writeString("hello");
        gen.close();
        assertEquals("<elem>hello</elem>", getOutput());
    }

    // Tests writeString with attribute flag
    @Test
    public void testWriteString_attributePath_writesAttribute() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("container"));
        gen.writeStartObject();
        gen.setNextName(new QName("attr"));
        gen.setNextIsAttribute(true);
        gen.writeString("value");
        gen.writeEndObject();
        gen.close();
        String output = getOutput();
        assertTrue("Should contain attribute", output.contains("attr=\"value\""));
        assertTrue("Should contain container start/end", output.contains("<container") && output.contains("</container>"));
    }

    // Tests writeString with unwrapped flag
    @Test
    public void testWriteString_unwrappedPath_writesCharacters() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("wrapper"));
        gen.writeStartObject();
        gen.setNextName(new QName("content"));
        gen.setNextIsUnwrapped(true);
        gen.writeString("text");
        gen.writeEndObject();
        gen.close();
        assertEquals("<wrapper>text</wrapper>", getOutput());
    }

    // Tests writeString with CData flag
    @Test
    public void testWriteString_CData_writesCData() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("elem"));
        gen.setNextIsCData(true);
        gen.writeString("<greeting>");
        gen.close();
        String output = getOutput();
        assertTrue("Should contain CDATA section", output.contains("<![CDATA[<greeting>]]>"));
    }

    // Tests writeString with pretty printer
    @Test
    public void testWriteString_prettyPrinter_writesPrettyPrinted() throws Exception {
        gen = createGeneratorWithPretty();
        gen.setNextName(new QName("elem"));
        gen.writeString("value");
        gen.close();
        String output = getOutput();
        assertTrue(output.contains("<elem>value</elem>"));
    }

    // Tests writeNull normal element -> empty element
    @Test
    public void testWriteNull_normalElement_writesEmptyElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("empty"));
        gen.writeNull();
        gen.close();
        assertEquals("<empty/>", getOutput());
    }

    // Tests writeNull with attribute flag -> nothing written
    @Test
    public void testWriteNull_attributePath_doesNothing() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("container"));
        gen.writeStartObject();
        gen.setNextName(new QName("attr"));
        gen.setNextIsAttribute(true);
        gen.writeNull();
        gen.writeEndObject();
        gen.close();
        assertEquals("<container></container>", getOutput());
    }

    // Tests writeBoolean true
    @Test
    public void testWriteBoolean_true_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("flag"));
        gen.writeBoolean(true);
        gen.close();
        assertEquals("<flag>true</flag>", getOutput());
    }

    // Tests writeNumber with int
    @Test
    public void testWriteNumber_int_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("num"));
        gen.writeNumber(42);
        gen.close();
        assertEquals("<num>42</num>", getOutput());
    }

    // Tests writeNumber with long
    @Test
    public void testWriteNumber_long_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("longval"));
        gen.writeNumber(123456789012345L);
        gen.close();
        assertEquals("<longval>123456789012345</longval>", getOutput());
    }

    // Tests writeNumber with BigDecimal
    @Test
    public void testWriteNumber_BigDecimal_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("bd"));
        gen.writeNumber(new BigDecimal("123.456"));
        gen.close();
        assertEquals("<bd>123.456</bd>", getOutput());
    }

    // Tests writeNumber with BigDecimal null -> writeNull
    @Test
    public void testWriteNumber_BigDecimalNull_writesEmptyElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("nullbd"));
        gen.writeNumber((BigDecimal) null);
        gen.close();
        assertEquals("<nullbd/>", getOutput());
    }

    // Tests writeStartObject and writeEndObject
    @Test
    public void testWriteStartObjectAndEndObject_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("obj"));
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        assertEquals("<obj></obj>", getOutput());
    }

    // Tests startWrappedValue and finishWrappedValue
    @Test
    public void testStartWrappedValue_withWrapper_createsWrappedContent() throws Exception {
        gen = createGenerator();
        gen.startWrappedValue(new QName("wrap"), new QName("item"));
        gen.writeString("content");
        gen.finishWrappedValue(new QName("wrap"), new QName("item"));
        gen.close();
        assertEquals("<wrap><item>content</item></wrap>", getOutput());
    }

    // Tests writeFieldName sets nextName and writes element
    @Test
    public void testWriteFieldName_setsNextNameAndWritesElement() throws Exception {
        gen = createGenerator();
        gen.writeFieldName("myField");
        gen.writeString("val");
        gen.close();
        assertEquals("<myField>val</myField>", getOutput());
    }

    // Tests writeRawValue with Stax2 emulation -> throws exception
    @Test(expected = JsonGenerationException.class)
    public void testWriteRawValue_whenStax2Emulation_throwsException() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("x"));
        gen.writeRawValue("raw");
    }

    // Tests close with open object auto-closes element
    @Test
    public void testClose_withOpenObject_autoClosesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("obj"));
        gen.writeStartObject();
        gen.close();
        String output = getOutput();
        assertTrue("Should close open element", output.contains("</obj>"));
    }

    // Tests writeString without setting nextName -> IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testWriteString_withoutSettingNextName_throwsIllegalStateException() throws Exception {
        gen = createGenerator();
        gen.writeString("test");
    }

    // Tests setNextNameIfMissing when name missing
    @Test
    public void testSetNextNameIfMissing_whenNameMissing_setsName() throws Exception {
        gen = createGenerator();
        QName name = new QName("testName");
        assertTrue(gen.setNextNameIfMissing(name));
        gen.writeString("value");
        gen.close();
        assertEquals("<testName>value</testName>", getOutput());
    }

    // Tests setNextNameIfMissing when name already set
    @Test
    public void testSetNextNameIfMissing_whenNameAlreadySet_returnsFalse() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("existing"));
        QName name = new QName("newName");
        assertFalse(gen.setNextNameIfMissing(name));
    }

    // ==================== New tests for uncovered methods ====================

    // Tests writeNumber with double
    @Test
    public void testWriteNumber_double_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("dbl"));
        gen.writeNumber(3.14);
        gen.close();
        assertEquals("<dbl>3.14</dbl>", getOutput());
    }

    // Tests writeNumber with float
    @Test
    public void testWriteNumber_float_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("flt"));
        gen.writeNumber(2.5f);
        gen.close();
        assertEquals("<flt>2.5</flt>", getOutput());
    }

    // Tests writeNumber with BigInteger
    @Test
    public void testWriteNumber_BigInteger_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("big"));
        gen.writeNumber(new BigInteger("12345678901234567890"));
        gen.close();
        assertEquals("<big>12345678901234567890</big>", getOutput());
    }

    // Tests writeNumber with String
    @Test
    public void testWriteNumber_String_writesElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("strnum"));
        gen.writeNumber("123.45");
        gen.close();
        assertEquals("<strnum>123.45</strnum>", getOutput());
    }

    // Tests writeNumber with null String -> empty element
    @Test
    public void testWriteNumber_StringNull_writesEmptyElement() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("nullstr"));
        gen.writeNumber((String) null);
        gen.close();
        assertEquals("<nullstr/>", getOutput());
    }

    // Tests writeBinary with base64
    @Test
    public void testWriteBinary_writesBase64() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("bin"));
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);
        gen.close();
        assertEquals("<bin>SGVsbG8=</bin>", getOutput());
    }

    // Tests writeRaw writes raw string directly
    @Test
    public void testWriteRaw_writesRawString() throws Exception {
        gen = createGenerator(0, 0, null);
        gen.initGenerator();
        gen.writeRaw("raw text");
        gen.close();
        assertEquals("raw text", getOutput());
    }

    // Tests flush does not throw
    @Test
    public void testFlush_doesNotThrow() throws Exception {
        gen = createGenerator();
        gen.setNextName(new QName("x"));
        gen.writeString("value");
        gen.flush();
        String output = getOutput();
        assertTrue(output.contains("<x>value</x>"));
    }
}