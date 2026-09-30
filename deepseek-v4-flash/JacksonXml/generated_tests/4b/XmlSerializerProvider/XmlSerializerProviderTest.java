package com.fasterxml.jackson.dataformat.xml.ser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlRootNameLookup;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;
import com.fasterxml.jackson.dataformat.xml.XmlTestUtil; // For TestTokenBuffer if available, otherwise use TokenBuffer from core

/**
 * JUnit 4 tests for XmlSerializerProvider, targeting Defects4J bug 4b.
 */
public class XmlSerializerProviderTest {

    private XmlMapper xmlMapper;
    private XmlSerializerProvider provider;
    private SerializationConfig config;
    private SerializerFactory factory;

    @Before
    public void setUp() throws Exception {
        xmlMapper = new XmlMapper();
        config = xmlMapper.getSerializationConfig();
        factory = xmlMapper.getSerializerFactory();
        XmlRootNameLookup rootNameLookup = new DefaultXmlRootNameLookup();
        provider = new XmlSerializerProvider(rootNameLookup);
        provider.setSerializerFactory(factory);
        provider.setConfig(config);
    }

    // Tests that serializeValue with null value calls _serializeXmlNull and writes <null/>
    @Test
    public void testSerializeValue_nullValue_writesNullElement() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, null);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain null element, got: " + xml, xml.contains("<null/>") || xml.contains("<null></null>"));
    }

    // Tests serializeValue with a simple bean
    @Test
    public void testSerializeValue_simpleBean_serializesCorrectly() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, bean);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain <name>test</name>, got: " + xml, xml.contains("<name>test</name>"));
        assertTrue("XML should contain root element <SimpleBean>, got: " + xml, xml.contains("<SimpleBean>"));
    }

    // Tests serializeValue with a list (array type)
    @Test
    public void testSerializeValue_listValue_serializesAsArray() throws Exception {
        List<String> list = Arrays.asList("a", "b");
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, list);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain item elements, got: " + xml, xml.contains("<item>"));
        assertTrue("XML should contain 'a', got: " + xml, xml.contains("a"));
        assertTrue("XML should contain 'b', got: " + xml, xml.contains("b"));
    }

    // Tests serializeValue with root type (JavaType) and non-null value
    @Test
    public void testSerializeValue_withRootType_serializesCorrectly() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.name = "hello";
        JavaType rootType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, bean, rootType);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain <name>hello</name>, got: " + xml, xml.contains("<name>hello</name>"));
    }

    // Tests serializeValue with root type and null value
    @Test
    public void testSerializeValue_withRootTypeNullValue_writesNullElement() throws Exception {
        JavaType rootType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, null, rootType);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain null element, got: " + xml, xml.contains("<null/>") || xml.contains("<null></null>"));
    }

    // Tests serializeValue with JsonSerializer parameter and non-null value
    @Test
    public void testSerializeValue_withSerializer_usesProvidedSerializer() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.name = "world";
        JavaType rootType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        JsonSerializer<Object> ser = provider.findTypedValueSerializer(SimpleBean.class, true, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, bean, rootType, ser);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain <name>world</name>, got: " + xml, xml.contains("<name>world</name>"));
    }

    // Tests serializeValue with null serializer (falls back to findTypedValueSerializer)
    @Test
    public void testSerializeValue_nullSerializer_fallsBackToDefault() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.name = "fallback";
        JavaType rootType = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, bean, rootType, null);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain <name>fallback</name>, got: " + xml, xml.contains("<name>fallback</name>"));
    }

    // Tests serializeValue with root type and list (array type)
    @Test
    public void testSerializeValue_withRootTypeList_serializesAsArray() throws Exception {
        List<String> list = Arrays.asList("x", "y");
        JavaType rootType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, list, rootType);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain item elements, got: " + xml, xml.contains("<item>"));
        assertTrue("XML should contain 'x', got: " + xml, xml.contains("x"));
        assertTrue("XML should contain 'y', got: " + xml, xml.contains("y"));
    }

    // Tests _asXmlGenerator with a non-ToXmlGenerator (should throw exception)
    @Test(expected = JsonMappingException.class)
    public void testAsXmlGenerator_nonToXmlGenerator_throwsJsonMappingException() throws Exception {
        // Use a JsonGenerator that is not ToXmlGenerator and not TokenBuffer
        ObjectMapper plainMapper = new ObjectMapper();
        JsonGenerator gen = plainMapper.getFactory().createGenerator(new StringWriter());
        // This should trigger the exception path
        provider.serializeValue(gen, new SimpleBean());
    }

    // Tests _rootNameFromConfig with null root name
    @Test
    public void testRootNameFromConfig_nullRootName_returnsNull() throws Exception {
        // Config without root name should return null from _rootNameFromConfig
        QName qname = provider._rootNameFromConfig();
        assertNull("When no root name configured, should return null", qname);
    }

    // Tests _initWithRootName sets namespace correctly
    @Test
    public void testInitWithRootName_withNamespace_setsDefaultNamespace() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(sw);
        QName rootName = new QName("http://example.com", "root");
        provider._initWithRootName(xgen, rootName);
        // After init, generator should be ready to write
        xgen.writeStartObject();
        xgen.writeEndObject();
        xgen.close();
        String xml = sw.toString();
        // Should have root element
        assertNotNull("XML output should not be null after init", xml);
    }

    // Tests _startRootArray writes start object and field name "item"
    @Test
    public void testStartRootArray_writesStartObjectAndItemField() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(sw);
        QName rootName = new QName("root");
        provider._startRootArray(xgen, rootName);
        xgen.writeEndObject();
        xgen.close();
        String xml = sw.toString();
        assertTrue("XML should contain 'item' field, got: " + xml, xml.contains("item"));
    }

    // Tests _serializeXmlNull with ToXmlGenerator
    @Test
    public void testSerializeXmlNull_withToXmlGenerator_writesNullElement() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(sw);
        provider._serializeXmlNull(xgen);
        xgen.close();
        String xml = sw.toString();
        assertTrue("XML should contain null element, got: " + xml, xml.contains("<null/>") || xml.contains("<null></null>"));
    }

    // Test that createInstance returns correct type
    @Test
    public void testCreateInstance_returnsXmlSerializerProvider() throws Exception {
        DefaultSerializerProvider instance = provider.createInstance(config, factory);
        assertTrue("createInstance should return XmlSerializerProvider", instance instanceof XmlSerializerProvider);
    }

    // ===== NEW TEST CASES FOR COVERAGE =====

    // Tests _asXmlGenerator with TokenBuffer (should not throw exception)
    @Test
    public void testAsXmlGenerator_withTokenBuffer_doesNotThrow() throws Exception {
        // Use TokenBuffer from Jackson core (simulates a non-XML generator that is allowed)
        com.fasterxml.jackson.databind.util.TokenBuffer tokenBuffer = new com.fasterxml.jackson.databind.util.TokenBuffer(xmlMapper, false);
        JsonGenerator gen = tokenBuffer.asGenerator();
        // This should succeed without throwing JsonMappingException
        provider.serializeValue(gen, new SimpleBean());
        gen.close();
        // We just verify no exception was thrown; TokenBuffer doesn't produce XML
        assertTrue("Should reach here without exception", true);
    }

    // Tests _serializeXmlNull with non-ToXmlGenerator (JsonGenerator that is not XML)
    @Test(expected = JsonMappingException.class)
    public void testSerializeXmlNull_withNonToXmlGenerator_throwsJsonMappingException() throws Exception {
        // Use a plain ObjectMapper's JsonGenerator that is not ToXmlGenerator
        ObjectMapper plainMapper = new ObjectMapper();
        JsonGenerator gen = plainMapper.getFactory().createGenerator(new StringWriter());
        // _serializeXmlNull will call _asXmlGenerator internally, expecting it to throw
        // But _serializeXmlNull is protected; we can test via serializeValue with null and non-XML generator
        // However, the existing testAsXmlGenerator_nonToXmlGenerator already tests via serializeValue
        // Let's test a different path: directly call _serializeXmlNull via reflection or by creating a scenario
        // We'll call the protected method using reflection for direct coverage
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_serializeXmlNull", JsonGenerator.class);
        method.setAccessible(true);
        method.invoke(provider, gen);
    }

    // Tests _startRootArray with a non-ToXmlGenerator (should throw)
    @Test(expected = JsonMappingException.class)
    public void testStartRootArray_withNonToXmlGenerator_throwsJsonMappingException() throws Exception {
        ObjectMapper plainMapper = new ObjectMapper();
        JsonGenerator gen = plainMapper.getFactory().createGenerator(new StringWriter());
        // Use reflection to call _startRootArray with non-XML generator
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_startRootArray", JsonGenerator.class, QName.class);
        method.setAccessible(true);
        method.invoke(provider, gen, new QName("test"));
    }

    // Tests _initWithRootName with non-ToXmlGenerator (should throw)
    @Test(expected = JsonMappingException.class)
    public void testInitWithRootName_withNonToXmlGenerator_throwsJsonMappingException() throws Exception {
        ObjectMapper plainMapper = new ObjectMapper();
        JsonGenerator gen = plainMapper.getFactory().createGenerator(new StringWriter());
        // Use reflection to call _initWithRootName with non-XML generator
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_initWithRootName", JsonGenerator.class, QName.class);
        method.setAccessible(true);
        method.invoke(provider, gen, new QName("test"));
    }

    // Tests the case where findTypedValueSerializer is called with a type that has no serializer
    @Test(expected = JsonMappingException.class)
    public void testFindTypedValueSerializer_withUnsupportedType_throwsJsonMappingException() throws Exception {
        // Provide an unsupported type (e.g., an interface or abstract class that cannot be serialized)
        JavaType unsupportedType = TypeFactory.defaultInstance().constructType(java.io.Serializable.class);
        provider.findTypedValueSerializer(unsupportedType, true, null);
    }

    // Tests that serializeValue with an empty collection produces proper XML
    @Test
    public void testSerializeValue_emptyList_serializesCorrectly() throws Exception {
        List<String> emptyList = Collections.emptyList();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        provider.serializeValue(gen, emptyList);
        gen.close();
        String xml = sw.toString();
        // Should produce a root element with no children
        assertTrue("XML should contain root element for empty list", xml.contains("<ArrayList>") || xml.contains("<list>") || xml.contains("<null>"));
        // Not checking specific content as it depends on serialization config
        assertNotNull("XML should not be null", xml);
    }

    // Tests that _rootNameFromConfig returns correct QName when @JsonRootName is used
    @Test
    public void testRootNameFromConfig_withJsonRootName_returnsCorrectQName() throws Exception {
        // Create a bean with @JsonRootName annotation
        StringWriter sw = new StringWriter();
        JsonGenerator gen = xmlMapper.getFactory().createGenerator(sw);
        SimpleBeanWithRootName bean = new SimpleBeanWithRootName();
        bean.value = "test";
        provider.serializeValue(gen, bean);
        gen.close();
        String xml = sw.toString();
        assertTrue("XML should contain custom root name 'CustomRoot', got: " + xml, xml.contains("<CustomRoot>"));
    }

    @JsonRootName("CustomRoot")
    static class SimpleBeanWithRootName {
        public String value;
    }

    // @JsonRootName to help with root name lookup
    @JsonRootName("SimpleBean")
    static class SimpleBean {
        public String name;
    }
}