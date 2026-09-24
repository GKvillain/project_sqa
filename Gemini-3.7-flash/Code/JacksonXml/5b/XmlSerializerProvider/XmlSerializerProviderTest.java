package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper _xmlMapper;
    private XmlSerializerProvider _provider;
    private XmlRootNameLookup _rootNameLookup;

    @JacksonXmlRootElement(localName = "customRoot", namespace = "http://example.com/ns")
    static class CustomRootBean {
        public String name = "test";
    }

    static class SimpleBean {
        public int id = 123;
    }

    @Before
    public void setUp() {
        _xmlMapper = new XmlMapper();
        _rootNameLookup = new XmlRootNameLookup();
        _provider = new XmlSerializerProvider(_rootNameLookup);
    }

    // Tests XmlSerializerProvider constructor with XmlRootNameLookup
    @Test
    public void testConstructor_withRootNames_initializesCorrectly() {
        XmlSerializerProvider provider = new XmlSerializerProvider(_rootNameLookup);
        assertNotNull(provider);
        assertSame(_rootNameLookup, provider._rootNameLookup);
    }

    // Tests copy() method and copy constructor (Defects4J JacksonXml-5 / dataformat-xml#282 issue)
    @Test
    public void testCopy_returnsNewInstanceWithSameLookup() {
        XmlSerializerProvider copy = (XmlSerializerProvider) _provider.copy();
        assertNotNull(copy);
        assertNotSame(_provider, copy);
        assertSame(_provider._rootNameLookup, copy._rootNameLookup);
    }

    // Tests createInstance() with config and SerializerFactory
    @Test
    public void testCreateInstance_withConfigAndFactory_returnsConfiguredInstance() {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        SerializerFactory factory = _xmlMapper.getSerializerFactory();
        XmlSerializerProvider instance = (XmlSerializerProvider) _provider.createInstance(config, factory);
        
        assertNotNull(instance);
        assertSame(_provider._rootNameLookup, instance._rootNameLookup);
        assertSame(config, instance.getConfig());
    }

    // Tests serializeValue(gen, null) serializing null value to ToXmlGenerator
    @Test
    public void testSerializeValue_nullValue_writesXmlNull() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        
        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null/>") || xml.contains("<null"));
    }

    // Tests serializeValue(gen, null, rootType, ser) serializing null with type and serializer
    @Test
    public void testSerializeValue_nullValueWithType_writesXmlNull() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        JavaType type = _xmlMapper.constructType(SimpleBean.class);

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, null, type, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null/>") || xml.contains("<null"));
    }

    // Tests serializeValue(gen, value) for simple bean object
    @Test
    public void testSerializeValue_simpleObject_serializesCorrectly() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        SimpleBean bean = new SimpleBean();

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, bean);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<id>123</id>"));
        assertTrue(xml.contains("</SimpleBean>"));
    }

    // Tests serializeValue with list/array (isIndexedType = true)
    @Test
    public void testSerializeValue_indexedType_writesRootArray() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        List<String> list = new ArrayList<String>();
        list.add("abc");
        list.add("def");

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, list);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<ArrayList>"));
        assertTrue(xml.contains("<item>abc</item>"));
        assertTrue(xml.contains("<item>def</item>"));
        assertTrue(xml.contains("</ArrayList>"));
    }

    // Tests serializeValue with explicit JavaType and null serializer
    @Test
    public void testSerializeValue_withJavaTypeAndNullSerializer_findsAndSerializes() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        JavaType type = _xmlMapper.constructType(SimpleBean.class);
        SimpleBean bean = new SimpleBean();

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, bean, type, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<id>123</id>"));
    }

    // Tests serializeValue with root namespace defined
    @Test
    public void testSerializeValue_customRootWithNamespace_serializesNamespace() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        CustomRootBean bean = new CustomRootBean();

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, bean);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("customRoot"));
        assertTrue(xml.contains("http://example.com/ns"));
    }

    // Tests serializeValue with TokenBuffer generator (convertValue scenario)
    @Test
    public void testSerializeValue_tokenBuffer_handlesNonXmlGenerator() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectMapper) null, false);
        SimpleBean bean = new SimpleBean();

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(tb, bean);
        tb.close();

        assertNotNull(tb.firstToken());
    }

    // Tests _asXmlGenerator throwing JsonMappingException for unsupported non-XML generator
    @Test(expected = JsonMappingException.class)
    public void testAsXmlGenerator_unsupportedGenerator_throwsJsonMappingException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator nonXmlGen = new ObjectMapper().getFactory().createGenerator(out);

        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov._asXmlGenerator(nonXmlGen);
    }

    // Tests _rootNameFromConfig when full root name with namespace is configured
    @Test
    public void testRootNameFromConfig_withNamespace_returnsQName() {
        XmlMapper mapper = new XmlMapper();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(new PropertyName("myRoot", "http://my.ns.org")));
        XmlSerializerProvider prov = (XmlSerializerProvider) mapper.getSerializerProviderInstance();

        QName rootName = prov._rootNameFromConfig();
        assertNotNull(rootName);
        assertEquals("myRoot", rootName.getLocalPart());
        assertEquals("http://my.ns.org", rootName.getNamespaceURI());
    }

    // Tests _rootNameFromConfig when full root name without namespace is configured
    @Test
    public void testRootNameFromConfig_withoutNamespace_returnsSimpleQName() {
        XmlMapper mapper = new XmlMapper();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(new PropertyName("simpleRoot")));
        XmlSerializerProvider prov = (XmlSerializerProvider) mapper.getSerializerProviderInstance();

        QName rootName = prov._rootNameFromConfig();
        assertNotNull(rootName);
        assertEquals("simpleRoot", rootName.getLocalPart());
        assertEquals("", rootName.getNamespaceURI());
    }

    // Tests _rootNameFromConfig when no full root name is set
    @Test
    public void testRootNameFromConfig_noConfig_returnsNull() {
        XmlSerializerProvider prov = (XmlSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        QName rootName = prov._rootNameFromConfig();
        assertNull(rootName);
    }

    // Tests _wrapAsIOE with IOException and RuntimeException
    @Test
    public void testWrapAsIOE_wrappingDifferentExceptions() {
        IOException ioEx = new IOException("io failure");
        IOException wrappedIo = _provider._wrapAsIOE(null, ioEx);
        assertSame(ioEx, wrappedIo);

        RuntimeException runtimeEx = new RuntimeException("runtime failure");
        IOException wrappedRuntime = _provider._wrapAsIOE(null, runtimeEx);
        assertTrue(wrappedRuntime instanceof JsonMappingException);
        assertEquals("runtime failure", wrappedRuntime.getMessage());

        NullPointerException npe = new NullPointerException();
        IOException wrappedNpe = _provider._wrapAsIOE(null, npe);
        assertTrue(wrappedNpe instanceof JsonMappingException);
        assertTrue(wrappedNpe.getMessage().contains("[no message for java.lang.NullPointerException]"));
    }
}