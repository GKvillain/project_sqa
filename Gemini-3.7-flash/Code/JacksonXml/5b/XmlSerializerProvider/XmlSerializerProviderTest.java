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
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper _xmlMapper;
    private XmlSerializerProvider _provider;

    @JacksonXmlRootElement(localName = "customRoot", namespace = "http://example.com")
    static class AnnotatedBean {
        public String name = "test";
    }

    static class SimpleBean {
        public int id = 123;
    }

    @Before
    public void setUp() {
        _xmlMapper = new XmlMapper();
        _provider = new XmlSerializerProvider(new XmlRootNameLookup());
    }

    // Tests XmlSerializerProvider constructors and field copying
    @Test
    public void testCopy_createsNewInstanceWithSameLookup() {
        DefaultSerializerProvider copy = _provider.copy();
        assertNotNull(copy);
        assertTrue(copy instanceof XmlSerializerProvider);
        XmlSerializerProvider xmlCopy = (XmlSerializerProvider) copy;
        assertNotNull(xmlCopy._rootNameLookup);
    }

    // Tests createInstance method
    @Test
    public void testCreateInstance_withConfigAndFactory_createsValidInstance() {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        SerializerFactory factory = _xmlMapper.getSerializerFactory();
        DefaultSerializerProvider instance = _provider.createInstance(config, factory);
        assertNotNull(instance);
        assertTrue(instance instanceof XmlSerializerProvider);
    }

    // Tests serializing a null value with ToXmlGenerator
    @Test
    public void testSerializeValue_nullValue_serializesNullXml() throws Exception {
        String xml = _xmlMapper.writeValueAsString(null);
        assertNotNull(xml);
        assertTrue(xml.contains("<null/>") || xml.contains("<null"));
    }

    // Tests serializing a simple object value
    @Test
    public void testSerializeValue_simpleBean_serializesSuccessfully() throws Exception {
        SimpleBean bean = new SimpleBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<id>123</id>"));
        assertTrue(xml.contains("</SimpleBean>"));
    }

    // Tests serializing an annotated object with custom root name and namespace
    @Test
    public void testSerializeValue_annotatedBeanWithNamespace_writesCorrectNamespace() throws Exception {
        AnnotatedBean bean = new AnnotatedBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<customRoot xmlns=\"http://example.com\">"));
        assertTrue(xml.contains("<name>test</name>"));
        assertTrue(xml.contains("</customRoot>"));
    }

    // Tests serializing an indexed/array type (branch asArray == true)
    @Test
    public void testSerializeValue_arrayType_writesArrayWrapping() throws Exception {
        String[] items = new String[] { "a", "b" };
        String xml = _xmlMapper.writeValueAsString(items);
        assertNotNull(xml);
        assertTrue(xml.contains("<item>a</item>"));
        assertTrue(xml.contains("<item>b</item>"));
    }

    // Tests serializing a List type
    @Test
    public void testSerializeValue_listType_writesListWrapping() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        String xml = _xmlMapper.writeValueAsString(list);
        assertNotNull(xml);
        assertTrue(xml.contains("<item>first</item>"));
        assertTrue(xml.contains("<item>second</item>"));
    }

    // Tests serializeValue with explicit JavaType and null serializer
    @Test
    public void testSerializeValueWithType_nullValue_serializesNullXml() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        ((XmlSerializerProvider) prov).serializeValue(xgen, null, type, null);
        xgen.close();
        String xml = sw.toString();
        assertNotNull(xml);
        assertTrue(xml.contains("<null/>") || xml.contains("<null"));
    }

    // Tests serializeValue with explicit JavaType and valid bean
    @Test
    public void testSerializeValueWithType_simpleBean_serializesSuccessfully() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        SimpleBean bean = new SimpleBean();
        ((XmlSerializerProvider) prov).serializeValue(xgen, bean, type, null);
        xgen.close();
        String xml = sw.toString();
        assertNotNull(xml);
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<id>123</id>"));
    }

    // Tests serializeValue with explicit JavaType for indexed type
    @Test
    public void testSerializeValueWithType_arrayType_writesArrayWrapping() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String[].class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        String[] items = new String[] { "hello" };
        ((XmlSerializerProvider) prov).serializeValue(xgen, items, type, null);
        xgen.close();
        String xml = sw.toString();
        assertNotNull(xml);
        assertTrue(xml.contains("<item>hello</item>"));
    }

    // Tests serializing using TokenBuffer which causes _asXmlGenerator to return null
    @Test
    public void testSerializeValue_withTokenBuffer_returnsNullForXmlGenerator() throws Exception {
        TokenBuffer tb = new TokenBuffer(new ObjectMapper(), false);
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        SimpleBean bean = new SimpleBean();
        prov.serializeValue(tb, bean);
        tb.close();
        assertNotNull(tb.asParser());
    }

    // Tests serializeValue with explicit JavaType and TokenBuffer
    @Test
    public void testSerializeValueWithType_withTokenBuffer_succeeds() throws Exception {
        TokenBuffer tb = new TokenBuffer(new ObjectMapper(), false);
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        SimpleBean bean = new SimpleBean();
        ((XmlSerializerProvider) prov).serializeValue(tb, bean, type, null);
        tb.close();
        assertNotNull(tb.asParser());
    }

    // Tests serializeValue with an invalid generator type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testAsXmlGenerator_invalidGeneratorType_throwsJsonMappingException() throws Exception {
        JsonGenerator fakeGen = new ObjectMapper().getFactory().createGenerator(new StringWriter());
        SerializerProvider prov = _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(fakeGen, new SimpleBean());
    }

    // Tests root name lookup configured with full root name
    @Test
    public void testRootNameFromConfig_withExplicitFullRootName() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(PropertyName.construct("customRootName", "http://ns.example.com")));
        SimpleBean bean = new SimpleBean();
        String xml = mapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<customRootName xmlns=\"http://ns.example.com\">"));
    }

    // Tests root name lookup configured with simple root name (empty namespace)
    @Test
    public void testRootNameFromConfig_withSimpleRootNameNoNamespace() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(PropertyName.construct("simpleRoot", "")));
        SimpleBean bean = new SimpleBean();
        String xml = mapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<simpleRoot>"));
    }

    // Tests root name lookup configured with null namespace in PropertyName
    @Test
    public void testRootNameFromConfig_withNullNamespace() throws Exception {
        XmlMapper mapper = new XmlMapper();
        mapper.setConfig(mapper.getSerializationConfig().withRootName(PropertyName.construct("nullNsRoot", null)));
        SimpleBean bean = new SimpleBean();
        String xml = mapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<nullNsRoot>"));
    }

    // Tests _wrapAsIOE method with IOException
    @Test
    public void testWrapAsIOE_withIOException_returnsOriginalIOException() {
        IOException original = new IOException("original error");
        IOException wrapped = _provider._wrapAsIOE(null, original);
        assertSame(original, wrapped);
    }

    // Tests _wrapAsIOE method with RuntimeException
    @Test
    public void testWrapAsIOE_withRuntimeException_returnsJsonMappingException() {
        RuntimeException original = new RuntimeException("runtime error");
        IOException wrapped = _provider._wrapAsIOE(null, original);
        assertTrue(wrapped instanceof JsonMappingException);
        assertEquals("runtime error", wrapped.getMessage());
        assertSame(original, wrapped.getCause());
    }

    // Tests _wrapAsIOE method with Exception having null message
    @Test
    public void testWrapAsIOE_withNullMessageException_returnsDefaultMessage() {
        NullPointerException original = new NullPointerException();
        IOException wrapped = _provider._wrapAsIOE(null, original);
        assertTrue(wrapped instanceof JsonMappingException);
        assertTrue(wrapped.getMessage().contains("[no message for java.lang.NullPointerException]"));
    }
}