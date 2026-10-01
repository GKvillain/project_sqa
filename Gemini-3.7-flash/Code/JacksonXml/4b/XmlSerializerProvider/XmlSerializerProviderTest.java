package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper xmlMapper;
    private XmlSerializerProvider provider;

    public static class SimpleBean {
        public int x = 1;
        public String y = "test";
    }

    public static class FailingBean {
        public String getValue() {
            throw new RuntimeException("Simulated failure");
        }
    }

    @Before
    public void setUp() {
        xmlMapper = new XmlMapper();
        provider = new XmlSerializerProvider(new XmlRootNameLookup());
    }

    // Tests constructor and createInstance copy behavior
    @Test
    public void testCreateInstance_validConfig_returnsNewInstance() {
        SerializationConfig config = xmlMapper.getSerializationConfig();
        DefaultSerializerProvider instance = provider.createInstance(config, xmlMapper.getSerializerFactory());
        assertNotNull(instance);
        assertTrue(instance instanceof XmlSerializerProvider);
    }

    // Tests serialization of null value using ToXmlGenerator
    @Test
    public void testSerializeValue_nullValue_serializesXmlNull() throws IOException {
        String xml = xmlMapper.writeValueAsString(null);
        assertNotNull(xml);
        assertTrue(xml.contains("<null") || xml.contains("<null/>"));
    }

    // Tests serialization of simple Object
    @Test
    public void testSerializeValue_simpleBean_serializesCorrectly() throws IOException {
        SimpleBean bean = new SimpleBean();
        String xml = xmlMapper.writeValueAsString(bean);
        assertNotNull(xml);
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<x>1</x>"));
        assertTrue(xml.contains("<y>test</y>"));
    }

    // Tests serialization of indexed type (List) triggering root array handling
    @Test
    public void testSerializeValue_listType_serializesAsArray() throws IOException {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        String xml = xmlMapper.writeValueAsString(list);
        assertNotNull(xml);
        assertTrue(xml.contains("<item>a</item>"));
        assertTrue(xml.contains("<item>b</item>"));
    }

    // Tests serializeValue with explicit JavaType
    @Test
    public void testSerializeValue_withJavaType_serializesCorrectly() throws IOException {
        SimpleBean bean = new SimpleBean();
        JavaType javaType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = xmlMapper.getFactory().createGenerator(sw);
        
        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(xgen, bean, javaType);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<SimpleBean>"));
        assertTrue(xml.contains("<x>1</x>"));
    }

    // Tests serializeValue with explicit JavaType and null value
    @Test
    public void testSerializeValue_withJavaTypeAndNullValue_serializesXmlNull() throws IOException {
        JavaType javaType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = xmlMapper.getFactory().createGenerator(sw);
        
        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(xgen, null, javaType);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null"));
    }

    // Tests serializeValue with explicit JavaType and custom JsonSerializer
    @Test
    public void testSerializeValue_withJavaTypeAndCustomSerializer_usesProvidedSerializer() throws IOException {
        SimpleBean bean = new SimpleBean();
        JavaType javaType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        JsonSerializer<Object> ser = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("custom-output");
            }
        };

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = xmlMapper.getFactory().createGenerator(sw);
        
        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(xgen, bean, javaType, ser);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("custom-output"));
    }

    // Tests serializeValue with null JsonSerializer falling back to findTypedValueSerializer
    @Test
    public void testSerializeValue_withJavaTypeAndNullSerializer_findsSerializer() throws IOException {
        SimpleBean bean = new SimpleBean();
        JavaType javaType = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = xmlMapper.getFactory().createGenerator(sw);
        
        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(xgen, bean, javaType, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<SimpleBean>"));
    }

    // Tests serializeValue with TokenBuffer where _asXmlGenerator returns null
    @Test
    public void testSerializeValue_withTokenBuffer_succeedsWithoutXmlGenerator() throws IOException {
        TokenBuffer buffer = new TokenBuffer(xmlMapper, false);
        SimpleBean bean = new SimpleBean();

        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(buffer, bean);
        assertNotNull(buffer.firstToken());
        buffer.close();
    }

    // Tests _asXmlGenerator throwing JsonMappingException for unsupported JsonGenerator
    @Test(expected = JsonMappingException.class)
    public void testSerializeValue_withNonXmlGenerator_throwsJsonMappingException() throws IOException {
        JsonFactory standardFactory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator standardGen = standardFactory.createGenerator(sw);

        DefaultSerializerProvider prov = provider.createInstance(xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov.serializeValue(standardGen, new SimpleBean());
    }

    // Tests root name resolution from config with namespace
    @Test
    public void testRootNameFromConfig_withNamespace_configuresCorrectly() throws IOException {
        PropertyName propName = new PropertyName("customRoot", "http://example.com/ns");
        XmlMapper mapperWithRoot = new XmlMapper();
        String xml = mapperWithRoot.writer().withRootName(propName).writeValueAsString(new SimpleBean());
        assertNotNull(xml);
        assertTrue(xml.contains("customRoot"));
        assertTrue(xml.contains("http://example.com/ns"));
    }

    // Tests root name resolution from config without namespace
    @Test
    public void testRootNameFromConfig_withoutNamespace_configuresCorrectly() throws IOException {
        PropertyName propName = new PropertyName("simpleRoot");
        String xml = xmlMapper.writer().withRootName(propName).writeValueAsString(new SimpleBean());
        assertNotNull(xml);
        assertTrue(xml.contains("<simpleRoot>"));
        assertTrue(xml.contains("</simpleRoot>"));
    }

    // Tests root name resolution when root name is not configured
    @Test
    public void testRootNameFromConfig_nullConfiguredRootName_returnsNull() {
        SerializationConfig config = xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = new XmlSerializerProvider(provider, config, xmlMapper.getSerializerFactory());
        QName qname = prov._rootNameFromConfig();
        assertNull(qname);
    }

    // Tests exception handling when POJO getter throws RuntimeException
    @Test(expected = JsonMappingException.class)
    public void testSerializeValue_runtimeExceptionInGetter_wrapsInJsonMappingException() throws IOException {
        xmlMapper.writeValueAsString(new FailingBean());
    }

    // Tests _serializeXmlNull with explicit ToXmlGenerator
    @Test
    public void testSerializeXmlNull_directCall_writesNullElement() throws IOException {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = xmlMapper.getFactory().createGenerator(sw);
        
        XmlSerializerProvider prov = new XmlSerializerProvider(provider, xmlMapper.getSerializationConfig(), xmlMapper.getSerializerFactory());
        prov._serializeXmlNull(xgen);
        xgen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<null"));
    }
}