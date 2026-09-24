package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.EnumValues;

public class EnumSerializerTest {

    private enum TestEnum {
        FIRST,
        SECOND;

        @Override
        public String toString() {
            return "custom_" + name().toLowerCase();
        }
    }

    static class FormattedBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public TestEnum numEnum = TestEnum.SECOND;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public TestEnum strEnum = TestEnum.FIRST;
    }

    private ObjectMapper mapper;
    private SerializationConfig config;
    private SerializerProvider provider;
    private EnumValues enumValues;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getSerializationConfig();
        provider = mapper.getSerializerProviderInstance();
        enumValues = EnumValues.constructFromName(config, TestEnum.class);
    }

    // Tests deprecated single-argument constructor
    @Test
    public void testConstructor_singleArgument_initializesWithNullIndexFlag() {
        @SuppressWarnings("deprecation")
        EnumSerializer serializer = new EnumSerializer(enumValues);
        assertNotNull(serializer.getEnumValues());
        assertSame(enumValues, serializer.getEnumValues());
        assertFalse(serializer._serializeAsIndex(provider));
    }

    // Tests two-argument constructor with explicit index flag
    @Test
    public void testConstructor_withExplicitIndexFlag_setsFlagCorrectly() {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.TRUE);
        assertTrue(serializer._serializeAsIndex(provider));

        EnumSerializer serializerFalse = new EnumSerializer(enumValues, Boolean.FALSE);
        assertFalse(serializerFalse._serializeAsIndex(provider));
    }

    // Tests construct factory method with null format
    @Test
    public void testConstruct_nullFormat_returnsSerializerWithNullIndexFlag() {
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, config, null, null);
        assertNotNull(serializer);
        assertFalse(serializer._serializeAsIndex(provider));
    }

    // Tests construct factory method with number format shape
    @Test
    public void testConstruct_numberShape_returnsSerializerWithTrueIndexFlag() {
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.NUMBER);
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, config, null, format);
        assertTrue(serializer._serializeAsIndex(provider));
    }

    // Tests construct factory method with string format shape
    @Test
    public void testConstruct_stringShape_returnsSerializerWithFalseIndexFlag() {
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.STRING);
        EnumSerializer serializer = EnumSerializer.construct(TestEnum.class, config, null, format);
        assertFalse(serializer._serializeAsIndex(provider));
    }

    // Tests construct factory method with unsupported shape throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_unsupportedShapeObject_throwsIllegalArgumentException() {
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.OBJECT);
        EnumSerializer.construct(TestEnum.class, config, null, format);
    }

    // Tests _isShapeWrittenUsingIndex helper method branches
    @Test
    public void testIsShapeWrittenUsingIndex_variousShapes_returnsExpectedFlags() {
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, null, true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.ANY), true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.SCALAR), true));
        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.STRING), true));
        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.NATURAL), true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.NUMBER), true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.NUMBER_INT), true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.forShape(Shape.ARRAY), true));
    }

    // Tests serialization as index flag explicitly enabled
    @Test
    public void testSerialize_serializeAsIndexTrue_writesNumber() throws IOException {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        serializer.serialize(TestEnum.SECOND, gen, provider);
        gen.flush();

        assertEquals("1", sw.toString());
    }

    // Tests serialization using SerializationFeature.WRITE_ENUMS_USING_INDEX
    @Test
    public void testSerialize_featureWriteEnumsUsingIndex_writesNumber() throws IOException {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        SerializerProvider prov = customMapper.getSerializerProviderInstance();

        EnumSerializer serializer = new EnumSerializer(enumValues, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = customMapper.getFactory().createGenerator(sw);

        serializer.serialize(TestEnum.FIRST, gen, prov);
        gen.flush();

        assertEquals("0", sw.toString());
    }

    // Tests serialization using SerializationFeature.WRITE_ENUMS_USING_TO_STRING
    @Test
    public void testSerialize_featureWriteEnumsUsingToString_writesCustomToString() throws IOException {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider prov = customMapper.getSerializerProviderInstance();

        EnumSerializer serializer = new EnumSerializer(enumValues, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = customMapper.getFactory().createGenerator(sw);

        serializer.serialize(TestEnum.FIRST, gen, prov);
        gen.flush();

        assertEquals("\"custom_first\"", sw.toString());
    }

    // Tests standard default serialization using enum name
    @Test
    public void testSerialize_defaultSettings_writesEnumName() throws IOException {
        EnumSerializer serializer = new EnumSerializer(enumValues, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        serializer.serialize(TestEnum.FIRST, gen, provider);
        gen.flush();

        assertEquals("\"FIRST\"", sw.toString());
    }

    // Tests schema generation when serialized as index
    @Test
    public void testGetSchema_asIndex_returnsIntegerSchema() {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.TRUE);
        JsonNode schemaNode = serializer.getSchema(provider, TestEnum.class);

        assertNotNull(schemaNode);
        assertEquals("integer", schemaNode.get("type").asText());
    }

    // Tests schema generation when serialized as string enum
    @Test
    public void testGetSchema_asString_returnsStringSchemaWithEnumValues() {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.FALSE);
        JsonNode schemaNode = serializer.getSchema(provider, TestEnum.class);

        assertNotNull(schemaNode);
        assertEquals("string", schemaNode.get("type").asText());
        JsonNode enumArray = schemaNode.get("enum");
        assertNotNull(enumArray);
        assertTrue(enumArray.isArray());
        assertEquals(2, enumArray.size());
        assertEquals("FIRST", enumArray.get(0).asText());
        assertEquals("SECOND", enumArray.get(1).asText());
    }

    // Tests schema generation with non-enum type hint
    @Test
    public void testGetSchema_nonEnumTypeHint_returnsStringSchemaWithoutEnumArray() {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.FALSE);
        JsonNode schemaNode = serializer.getSchema(provider, String.class);

        assertNotNull(schemaNode);
        assertEquals("string", schemaNode.get("type").asText());
        assertNull(schemaNode.get("enum"));
    }

    // Tests schema generation with null type hint
    @Test
    public void testGetSchema_nullTypeHint_returnsStringSchemaWithoutEnumArray() {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.FALSE);
        JsonNode schemaNode = serializer.getSchema(provider, null);

        assertNotNull(schemaNode);
        assertEquals("string", schemaNode.get("type").asText());
        assertNull(schemaNode.get("enum"));
    }

    // Tests visitor pattern when serialized as index
    @Test
    public void testAcceptJsonFormatVisitor_asIndex_visitsIntFormat() throws Exception {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.TRUE);
        final boolean[] intVisited = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                intVisited[0] = true;
                return null;
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(TestEnum.class);
        serializer.acceptJsonFormatVisitor(visitor, type);
        assertTrue(intVisited[0]);
    }

    // Tests visitor pattern when serialized as string enum
    @Test
    public void testAcceptJsonFormatVisitor_asString_visitsStringFormat() throws Exception {
        EnumSerializer serializer = new EnumSerializer(enumValues, Boolean.FALSE);
        final Set<String> collectedEnums = new LinkedHashSet<String>();

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        collectedEnums.addAll(enums);
                    }
                };
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(TestEnum.class);
        serializer.acceptJsonFormatVisitor(visitor, type);
        assertEquals(2, collectedEnums.size());
        assertTrue(collectedEnums.contains("FIRST"));
        assertTrue(collectedEnums.contains("SECOND"));
    }

    // Tests visitor pattern when WRITE_ENUMS_USING_TO_STRING is enabled
    @Test
    public void testAcceptJsonFormatVisitor_usingToString_visitsStringFormatWithCustomStrings() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider prov = customMapper.getSerializerProviderInstance();

        EnumSerializer serializer = new EnumSerializer(enumValues, null);
        final Set<String> collectedEnums = new LinkedHashSet<String>();

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(prov) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        collectedEnums.addAll(enums);
                    }
                };
            }
        };

        JavaType type = TypeFactory.defaultInstance().constructType(TestEnum.class);
        serializer.acceptJsonFormatVisitor(visitor, type);
        assertEquals(2, collectedEnums.size());
        assertTrue(collectedEnums.contains("custom_first"));
        assertTrue(collectedEnums.contains("custom_second"));
    }

    // Tests createContextual with null property returns current serializer
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        EnumSerializer serializer = new EnumSerializer(enumValues, null);
        assertSame(serializer, serializer.createContextual(provider, null));
    }

    // Tests createContextual via contextual serialization of bean properties with @JsonFormat
    @Test
    public void testCreateContextual_withPropertyFormat_serializesCorrectly() throws Exception {
        FormattedBean bean = new FormattedBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"numEnum\":1"));
        assertTrue(json.contains("\"strEnum\":\"FIRST\""));
    }
}