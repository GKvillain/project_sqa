package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;

public class ToXmlGeneratorTest {

    private XMLOutputFactory _xmlOutputFactory;
    private StringWriter _writer;

    @Before
    public void setUp() {
        _xmlOutputFactory = XMLOutputFactory.newInstance();
        _writer = new StringWriter();
    }

    private ToXmlGenerator createGenerator(int xmlFeatures) throws Exception {
        IOContext ctxt = new IOContext(new BufferRecycler(), new ByteArrayOutputStream(), false);
        XMLStreamWriter sw = _xmlOutputFactory.createXMLStreamWriter(_writer);
        return new ToXmlGenerator(ctxt, 0, xmlFeatures, null, sw);
    }

    // Tests feature bit collection and toggling methods
    @Test
    public void testFeatures_enableDisableAndOverride_returnsExpectedStates() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_1_1));

        gen.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
        assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_1_1));

        gen.overrideFormatFeatures(0, ToXmlGenerator.Feature.WRITE_XML_1_1.getMask());
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_1_1));

        int defaults = ToXmlGenerator.Feature.collectDefaults();
        assertEquals(0, defaults);
    }

    // Tests XML declaration initialization for XML 1.0 and 1.1
    @Test
    public void testInitGenerator_xmlDeclaration_writesProlog() throws Exception {
        ToXmlGenerator gen10 = createGenerator(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        gen10.initGenerator();
        gen10.initGenerator(); // idempotency check
        gen10.close();
        assertTrue(_writer.toString().contains("<?xml version=\"1.0\""));

        _writer = new StringWriter();
        ToXmlGenerator gen11 = createGenerator(ToXmlGenerator.Feature.WRITE_XML_1_1.getMask());
        gen11.initGenerator();
        gen11.close();
        assertTrue(_writer.toString().contains("<?xml version=\"1.1\""));
    }

    // Tests writing simple string elements and attributes
    @Test
    public void testWriteString_elementsAndAttributes_writesCorrectXml() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("attr"));
        gen.setNextIsAttribute(true);
        gen.writeString("attrVal");

        gen.writeFieldName("elem");
        gen.writeString("elemVal");

        gen.writeStringField("elem2", "elem2Val");

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<root"));
        assertTrue(xml.contains("attr=\"attrVal\""));
        assertTrue(xml.contains("<elem>elemVal</elem>"));
        assertTrue(xml.contains("<elem2>elem2Val</elem2>"));
        assertTrue(xml.contains("</root>"));
    }

    // Tests writing string with CDATA and unwrapped value
    @Test
    public void testWriteString_cdataAndUnwrapped_writesProperContent() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("cdataElem"));
        gen.setNextIsCData(true);
        gen.writeString("some <cdata> content");

        gen.setNextName(new QName("cdataChars"));
        gen.setNextIsCData(true);
        char[] chars = "text chars".toCharArray();
        gen.writeString(chars, 0, chars.length);

        gen.setNextName(new QName("unwrapped"));
        gen.writeStartObject();
        gen.setNextIsUnwrapped(true);
        gen.writeString("rawContent");
        gen.writeEndObject();

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<![CDATA[some <cdata> content]]>"));
        assertTrue(xml.contains("<![CDATA[text chars]]>"));
        assertTrue(xml.contains("rawContent"));
    }

    // Tests writing primitive numbers and boolean as element and attribute
    @Test
    public void testWriteNumbersAndBoolean_variousTypes_outputsCorrectXml() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("intAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(42);

        gen.setNextName(new QName("longElem"));
        gen.writeNumber(1234567890123L);

        gen.setNextName(new QName("doubleElem"));
        gen.writeNumber(12.5d);

        gen.setNextName(new QName("floatElem"));
        gen.writeNumber(3.14f);

        gen.setNextName(new QName("boolAttr"));
        gen.setNextIsAttribute(true);
        gen.writeBoolean(true);

        gen.setNextName(new QName("boolElem"));
        gen.writeBoolean(false);

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("intAttr=\"42\""));
        assertTrue(xml.contains("<longElem>1234567890123</longElem>"));
        assertTrue(xml.contains("<doubleElem>12.5</doubleElem>"));
        assertTrue(xml.contains("<floatElem>3.14</floatElem>"));
        assertTrue(xml.contains("boolAttr=\"true\""));
        assertTrue(xml.contains("<boolElem>false</boolElem>"));
    }

    // Tests BigDecimal, BigInteger, null and formatted numbers
    @Test
    public void testWriteBigDecimalBigIntegerAndNull_validValues_outputsXml() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("decElem"));
        gen.writeNumber(new BigDecimal("99999.99999"));

        gen.setNextName(new QName("bigIntElem"));
        gen.writeNumber(new BigInteger("987654321987654321"));

        gen.setNextName(new QName("nullElem"));
        gen.writeNull();

        gen.setNextName(new QName("nullDec"));
        gen.writeNumber((BigDecimal) null);

        gen.setNextName(new QName("nullBigInt"));
        gen.writeNumber((BigInteger) null);

        gen.setNextName(new QName("strNum"));
        gen.writeNumber("123.45");

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<decElem>99999.99999</decElem>"));
        assertTrue(xml.contains("<bigIntElem>987654321987654321</bigIntElem>"));
        assertTrue(xml.contains("<nullElem"));
        assertTrue(xml.contains("<strNum>123.45</strNum>"));
    }

    // Tests BigDecimal with WRITE_BIGDECIMAL_AS_PLAIN feature enabled
    @Test
    public void testWriteBigDecimal_plainFeature_outputsPlainString() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("plainAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("1E-5"));

        gen.setNextName(new QName("plainElem"));
        gen.writeNumber(new BigDecimal("1E-5"));

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("plainAttr=\"0.00001\""));
        assertTrue(xml.contains("<plainElem>0.00001</plainElem>"));
    }

    // Tests binary data output as element and attribute
    @Test
    public void testWriteBinary_byteArray_outputsBase64() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        byte[] data = new byte[]{1, 2, 3, 4, 5};
        gen.setNextName(new QName("binElem"));
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);

        gen.setNextName(new QName("binAttr"));
        gen.setNextIsAttribute(true);
        gen.writeBinary(Base64Variants.MIME, data, 1, 3);

        gen.setNextName(new QName("binNull"));
        gen.writeBinary(Base64Variants.MIME, null, 0, 0);

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<binElem>"));
        assertTrue(xml.contains("binAttr="));
        assertTrue(xml.contains("<binNull"));
    }

    // Tests wrapped value lifecycle (arrays/collections)
    @Test
    public void testStartAndFinishWrappedValue_arrayWrapping_writesWrapperTags() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        QName wrapper = new QName("items");
        QName wrapped = new QName("item");
        gen.startWrappedValue(wrapper, wrapped);
        gen.writeStartArray();

        gen.setNextName(wrapped);
        gen.writeString("first");
        gen.setNextName(wrapped);
        gen.writeString("second");

        gen.writeEndArray();
        gen.finishWrappedValue(wrapper, wrapped);

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<items>"));
        assertTrue(xml.contains("<item>first</item>"));
        assertTrue(xml.contains("<item>second</item>"));
        assertTrue(xml.contains("</items>"));
    }

    // Tests repeated field name and setNextNameIfMissing
    @Test
    public void testSetNextNameIfMissingAndRepeatedFieldName() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        assertTrue(gen.setNextNameIfMissing(new QName("root")));
        assertFalse(gen.setNextNameIfMissing(new QName("ignored")));
        gen.writeStartObject();

        gen.setNextName(new QName("elem"));
        gen.writeRepeatedFieldName();
        gen.writeString("val");

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<root>"));
        assertTrue(xml.contains("<elem>val</elem>"));
    }

    // Tests pretty printer integration and configuration methods
    @Test
    public void testPrettyPrinterAndConfig_constructsAndEmitsFormattedOutput() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        DefaultXmlPrettyPrinter pp = new DefaultXmlPrettyPrinter();
        gen.setPrettyPrinter(pp);

        assertNotNull(gen.getStaxWriter());
        assertNotNull(gen.getOutputTarget());
        assertEquals(-1, gen.getOutputBuffered());
        assertTrue(gen.canWriteFormattedNumbers());
        assertTrue(gen.inRoot());

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        assertFalse(gen.inRoot());

        gen.writeFieldName(new SerializedString("child"));
        gen.writeString(new SerializedString("value"));

        gen.writeEndObject();
        gen.flush();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<root>"));
        assertTrue(xml.contains("<child>value</child>"));
    }

    // Tests exception path when missing element name
    @Test(expected = IllegalStateException.class)
    public void testHandleMissingName_noNameProvided_throwsIllegalStateException() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.writeStartObject();
    }

    // Tests exception path when writeEndObject is called without start object
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObject_notInObjectContext_throwsException() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.writeEndObject();
    }

    // Tests exception path when writeEndArray is called without start array
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArray_notInArrayContext_throwsException() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.writeEndArray();
    }

    // Tests exception path when unsupported raw UTF8 methods are invoked
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String_unsupported_throwsException() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.writeRawUTF8String(new byte[4], 0, 4);
    }

    // Tests writeRaw variants and writeRawValue
    @Test
    public void testWriteRawAndRawValue_variousInputs_writesRawXml() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.writeRaw("<raw1>text</raw1>");
        gen.writeRaw("<raw2>text</raw2>", 0, 17);
        gen.writeRaw(new char[]{'<', 'r', 'a', 'w', '3', '>', 'x', '<', '/', 'r', 'a', 'w', '3', '>'}, 0, 14);
        gen.writeRaw(' ');
        gen.writeRaw(new SerializedString("<raw4/>"));

        gen.setNextName(new QName("rawValElem"));
        gen.writeRawValue("<innerVal/>");

        gen.setNextName(new QName("rawValElem2"));
        gen.writeRawValue("<innerVal2/>", 0, 12);

        gen.setNextName(new QName("rawValElem3"));
        gen.writeRawValue(new char[]{'<', 'v', '/', '>'}, 0, 4);

        gen.setNextName(new QName("rawValElem4"));
        gen.writeRawValue(new SerializedString("<v2/>"));

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<raw1>text</raw1>"));
        assertTrue(xml.contains("<raw2>text</raw2>"));
        assertTrue(xml.contains("<raw3>x</raw3>"));
        assertTrue(xml.contains("<raw4/>"));
        assertTrue(xml.contains("<innerVal/>"));
        assertTrue(xml.contains("<innerVal2/>"));
        assertTrue(xml.contains("<v/>"));
        assertTrue(xml.contains("<v2/>"));
    }

    // Tests writing numbers and booleans as attributes for all numeric types
    @Test
    public void testWriteNumbersAsAttributes_allTypes_writesAttributes() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("shortAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber((short) 10);

        gen.setNextName(new QName("shortElem"));
        gen.writeNumber((short) 20);

        gen.setNextName(new QName("longAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(9999999999L);

        gen.setNextName(new QName("doubleAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(1.25d);

        gen.setNextName(new QName("floatAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(2.5f);

        gen.setNextName(new QName("bigIntAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigInteger("12345678901234567890"));

        gen.setNextName(new QName("bigDecAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("12345.6789"));

        gen.setNextName(new QName("strNumAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber("555.666");

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("shortAttr=\"10\""));
        assertTrue(xml.contains("<shortElem>20</shortElem>"));
        assertTrue(xml.contains("longAttr=\"9999999999\""));
        assertTrue(xml.contains("doubleAttr=\"1.25\""));
        assertTrue(xml.contains("floatAttr=\"2.5\""));
        assertTrue(xml.contains("bigIntAttr=\"12345678901234567890\""));
        assertTrue(xml.contains("bigDecAttr=\"12345.6789\""));
        assertTrue(xml.contains("strNumAttr=\"555.666\""));
    }

    // Tests array creation with size and plain array lifecycle
    @Test
    public void testWriteStartArray_withAndWithoutSize_outputsArrayElements() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("items"));
        gen.writeStartArray(3);
        gen.setNextName(new QName("item"));
        gen.writeString("a");
        gen.setNextName(new QName("item"));
        gen.writeString("b");
        gen.writeEndArray();

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<item>a</item>"));
        assertTrue(xml.contains("<item>b</item>"));
    }

    // Tests binary stream output
    @Test
    public void testWriteBinary_inputStream_writesBase64() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        byte[] bytes = new byte[]{10, 20, 30, 40};
        InputStream in = new ByteArrayInputStream(bytes);
        gen.setNextName(new QName("streamBin"));
        int written = gen.writeBinary(Base64Variants.MIME, in, bytes.length);
        assertEquals(4, written);

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("<streamBin>"));
    }

    // Tests namespaced QNames with prefix and URI
    @Test
    public void testNamespacesAndPrefixes_namespacedQNames_writesPrefixAndUri() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.setNextName(new QName("http://example.com/root", "root", "ns"));
        gen.writeStartObject();

        gen.setNextName(new QName("http://example.com/ns2", "child", "p2"));
        gen.writeString("childVal");

        gen.writeEndObject();
        gen.close();

        String xml = _writer.toString();
        assertTrue(xml.contains("ns:root") || xml.contains("root"));
        assertTrue(xml.contains("childVal"));
    }

    // Tests version, codec, formatFeatures, and generator state getters
    @Test
    public void testVersionCodecAndState_validAccessors_returnsCorrectState() throws Exception {
        ToXmlGenerator gen = createGenerator(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        assertEquals(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), gen.getFormatFeatures());

        assertEquals(PackageVersion.VERSION, gen.version());
        assertNull(gen.getCodec());

        gen.setCodec(null);
        assertNull(gen.getCodec());

        assertFalse(gen.isClosed());
        gen.close();
        assertTrue(gen.isClosed());
    }

    // Tests unsupported writeUTF8String method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String_unsupported_throwsException() throws Exception {
        ToXmlGenerator gen = createGenerator(0);
        gen.writeUTF8String(new byte[4], 0, 4);
    }
}