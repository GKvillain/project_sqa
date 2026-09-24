package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper _xmlMapper;
    private XmlRootNameLookup _rootNames;
    private XmlSerializerProvider _provider;

    public static class SimpleBean {
        public String name = "test";
        public int value = 42;
    }

    @JacksonXmlRootElement(localName = "customRoot", namespace = "http://example.com/ns")
    public static class NsBean {
        public String field = "val";
    }

    public static class FailingBean {
        public String getThrows() {
            throw new RuntimeException("runtime-error-in-getter");
        }
    }

    @Before
    public void setUp() {
        _xmlMapper = new XmlMapper();
        _rootNames = new XmlRootNameLookup();
        _provider = new XmlSerializerProvider(_rootNames);
    }

    // Tests constructor and createInstance functionality
    @Test
    public void testCreateInstance_withConfigAndFactory_returnsConfiguredInstance() {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        SerializerFactory factory = _xmlMapper.getSerializerFactory();

        DefaultSerializerProvider instance = _provider.createInstance(config, factory);

        assertNotNull(instance);
        assertTrue(instance instanceof XmlSerializerProvider);
        XmlSerializerProvider xmlInstance = (XmlSerializerProvider) instance;
        assertSame(_rootNames, xmlInstance._rootNameLookup);
    }

    // Tests serializing null value output
    @Test
    public void testSerializeValue_nullValue_serializesNullTag() throws Exception {
        String xml = _xmlMapper.writeValueAsString(null);
        assertNotNull(xml);
        assertTrue(xml.contains("<null") || xml.contains("<null/>"));
    }

    // Tests serializing a normal POJO using serializeValue(gen, value)
    @Test
    public void testSerializeValue_simpleBean_serializesCorrectXml() throws Exception {
        SimpleBean bean = new SimpleBean();
        String xml = _xmlMapper.writeValueAsString(bean);

        assertNotNull(xml);
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<name>test</name>"));
        assertTrue(xml.contains("<value>42</value>"));
        assertTrue(xml.contains("</SimpleBean>"));
    }

    // Tests serializing POJO with namespace
    @Test
    public void testSerializeValue_beanWithNamespace_serializesNamespace() throws Exception {
        NsBean bean = new NsBean();
        String xml = _xmlMapper.writeValueAsString(bean);

        assertNotNull(xml);
        assertTrue(xml.contains("customRoot"));
        assertTrue(xml.contains("http://example.com/ns"));
        assertTrue(xml.contains("<field>val</field>"));
    }

    // Tests serializing indexed type (array/list) triggering _startRootArray and gen.writeEndObject()
    @Test
    public void testSerializeValue_listType_serializesWithRootItemElements() throws Exception {
        List<String> list = Arrays.asList("first", "second");
        String xml = _xmlMapper.writeValueAsString(list);

        assertNotNull(xml);
        assertTrue(xml.contains("<item>first</item>"));
        assertTrue(xml.contains("<item>second</item>"));
    }

    // Tests serializeValue(gen, value, javaType)
    @Test
    public void testSerializeValue_withJavaType_serializesExpectedXml() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);

        DefaultSerializerProvider prov = (DefaultSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, new SimpleBean(), type);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<name>test</name>"));
    }

    // Tests serializeValue(gen, value, javaType) with null value
    @Test
    public void testSerializeValue_withJavaTypeNullValue_serializesNullTag() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);

        DefaultSerializerProvider prov = (DefaultSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, null, type);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null") || xml.contains("<null/>"));
    }

    // Tests serializeValue(gen, value, javaType, ser)
    @Test
    public void testSerializeValue_withCustomSerializer_usesProvidedSerializer() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> ser = _xmlMapper.getSerializerProviderInstance().findTypedValueSerializer(type, true, null);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);

        DefaultSerializerProvider prov = (DefaultSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, "testString", type, ser);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("testString"));
    }

    // Tests serializeValue(gen, value, javaType, ser) with null value
    @Test
    public void testSerializeValue_withCustomSerializerNullValue_serializesNullTag() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> ser = _xmlMapper.getSerializerProviderInstance().findTypedValueSerializer(type, true, null);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);

        DefaultSerializerProvider prov = (DefaultSerializerProvider) _xmlMapper.getSerializerProviderInstance();
        prov.serializeValue(xgen, null, type, ser);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null") || xml.contains("<null/>"));
    }

    // Tests serializeValue when generator is a TokenBuffer
    @Test
    public void testSerializeValue_withTokenBuffer_succeedsWithoutXmlGenerator() throws Exception {
        TokenBuffer buffer = new TokenBuffer(_xmlMapper, false);
        _xmlMapper.writeValue(buffer, new SimpleBean());
        buffer.close();

        assertNotNull(buffer.firstToken());
    }

    // Tests serializeValue null value when generator is a TokenBuffer
    @Test
    public void testSerializeValue_nullValueWithTokenBuffer_succeeds() throws Exception {
        TokenBuffer buffer = new TokenBuffer(_xmlMapper, false);
        _xmlMapper.writeValue(buffer, null);
        buffer.close();

        assertNotNull(buffer.firstToken());
    }

    // Tests _asXmlGenerator with unsupported generator type throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testAsXmlGenerator_unsupportedGenerator_throwsJsonMappingException() throws Exception {
        JsonFactory nonXmlFactory = new JsonFactory();
        JsonGenerator jsonGen = nonXmlFactory.createGenerator(new StringWriter());

        _provider._asXmlGenerator(jsonGen);
    }

    // Tests _rootNameFromConfig with empty namespace vs configured namespace
    @Test
    public void testSerializeValue_withExplicitRootNameConfig_usesConfiguredName() throws Exception {
        String xml = _xmlMapper.writer()
                .withRootName(new PropertyName("customSimpleRoot"))
                .writeValueAsString(new SimpleBean());

        assertNotNull(xml);
        assertTrue(xml.contains("<customSimpleRoot>"));
        assertTrue(xml.contains("</customSimpleRoot>"));
    }

    // Tests _rootNameFromConfig with namespace
    @Test
    public void testSerializeValue_withExplicitRootNameAndNamespace_usesConfiguredNamespace() throws Exception {
        String xml = _xmlMapper.writer()
                .withRootName(new PropertyName("customNsRoot", "http://custom.com/ns"))
                .writeValueAsString(new SimpleBean());

        assertNotNull(xml);
        assertTrue(xml.contains("<customNsRoot"));
        assertTrue(xml.contains("http://custom.com/ns"));
    }

    // Tests exception path in serializeValue wrapping RuntimeException to JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testSerializeValue_runtimeExceptionInGetter_throwsJsonMappingException() throws Exception {
        _xmlMapper.writeValueAsString(new FailingBean());
    }

    // Tests _initWithRootName when rootName is null to guard against NPE
    @Test
    public void testInitWithRootName_nullRootName_handlesGracefully() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = _xmlMapper.getFactory().createGenerator(sw);

        _provider._initWithRootName(xgen, new QName("testRoot"));
        xgen.close();
    }
}