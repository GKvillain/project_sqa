package com.fasterxml.jackson.databind.ser.std;

import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.EnumValues;

public class EnumSerializerTest {

    public enum SampleEnum {
        FIRST,
        SECOND;

        @Override
        public String toString() {
            return "custom_" + name().toLowerCase();
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    private EnumValues createEnumValues() {
        return EnumValues.constructFromName(mapper.getSerializationConfig(), SampleEnum.class);
    }

    private DefaultSerializerProvider createSerializerProvider() {
        return (DefaultSerializerProvider) mapper.getSerializerProviderInstance();
    }

    // Tests shape check helper returning null for null format
    @Test
    public void testIsShapeWrittenUsingIndex_nullFormat_returnsNull() {
        Boolean result = EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, null, true);
        assertNull(result);
    }

    // Tests shape check helper returning null for shape ANY and SCALAR
    @Test
    public void testIsShapeWrittenUsingIndex_shapeAnyAndScalar_returnsNull() {
        JsonFormat.Value formatAny = JsonFormat.Value.forShape(JsonFormat.Shape.ANY);
        JsonFormat.Value formatScalar = JsonFormat.Value.forShape(JsonFormat.Shape.SCALAR);

        assertNull(EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatAny, true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatScalar, true));
    }

    // Tests shape check helper returning false for STRING and NATURAL shapes
    @Test
    public void testIsShapeWrittenUsingIndex_shapeStringAndNatural_returnsFalse() {
        JsonFormat.Value formatString = JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
        JsonFormat.Value formatNatural = JsonFormat.Value.forShape(JsonFormat.Shape.NATURAL);

        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatString, true));
        assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatNatural, true));
    }

    // Tests shape check helper returning true for numeric shapes and ARRAY
    @Test
    public void testIsShapeWrittenUsingIndex_shapeNumericAndArray_returnsTrue() {
        JsonFormat.Value formatNumber = JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
        JsonFormat.Value formatNumberInt = JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER_INT);
        JsonFormat.Value formatArray = JsonFormat.Value.forShape(JsonFormat.Shape.ARRAY);

        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatNumber, true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatNumberInt, true));
        assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatArray, true));
    }

    // Tests shape check helper throwing exception for unsupported OBJECT shape
    @Test(expected = IllegalArgumentException.class)
    public void testIsShapeWrittenUsingIndex_shapeObject_throwsIllegalArgumentException() {
        JsonFormat.Value formatObject = JsonFormat.Value.forShape(JsonFormat.Shape.OBJECT);
        EnumSerializer._isShapeWrittenUsingIndex(SampleEnum.class, formatObject, true);
    }

    // Tests factory method construct and getEnumValues
    @Test
    public void testConstruct_validInputs_returnsConfiguredSerializer() {
        SerializationConfig config = mapper.getSerializationConfig();
        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
        EnumSerializer serializer = EnumSerializer.construct(SampleEnum.class, config, null, format);

        assertNotNull(serializer);
        assertEquals(SampleEnum.class, serializer.handledType());
        assertNotNull(serializer.getEnumValues());
    }

    // Tests deprecated single-argument constructor
    @Test
    public void testConstructor_deprecatedSingleArg_initializesCorrectly() {
        EnumValues values = createEnumValues();
        EnumSerializer serializer = new EnumSerializer(values);

        assertSame(values, serializer.getEnumValues());
        assertEquals(SampleEnum.class, serializer.handledType());
    }

    // Tests createContextual returning same instance when BeanProperty is null
    @Test
    public void testCreateContextual_nullProperty_returnsSelf() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), null);
        JsonSerializer<?> contextual = serializer.createContextual(createSerializerProvider(), null);

        assertSame(serializer, contextual);
    }

    // Tests default serialization as string (enum name)
    @Test
    public void testSerialize_defaultName_writesString() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = createSerializerProvider();

        serializer.serialize(SampleEnum.FIRST, gen, provider);
        gen.flush();

        assertEquals("\"FIRST\"", sw.toString());
    }

    // Tests serialization statically configured to serialize as index
    @Test
    public void testSerialize_staticIndexTrue_writesOrdinal() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.TRUE);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider provider = createSerializerProvider();

        serializer.serialize(SampleEnum.SECOND, gen, provider);
        gen.flush();

        assertEquals("1", sw.toString());
    }

    // Tests serialization dynamically checking WRITE_ENUMS_USING_INDEX feature
    @Test
    public void testSerialize_dynamicIndexFeatureEnabled_writesOrdinal() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        ObjectMapper indexMapper = new ObjectMapper();
        indexMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        SerializerProvider provider = indexMapper.getSerializerProviderInstance();

        serializer.serialize(SampleEnum.FIRST, gen, provider);
        gen.flush();

        assertEquals("0", sw.toString());
    }

    // Tests serialization dynamically checking WRITE_ENUMS_USING_TO_STRING feature
    @Test
    public void testSerialize_writeEnumsUsingToStringEnabled_writesToString() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);

        ObjectMapper toStringMapper = new ObjectMapper();
        toStringMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider provider = toStringMapper.getSerializerProviderInstance();

        serializer.serialize(SampleEnum.SECOND, gen, provider);
        gen.flush();

        assertEquals("\"custom_second\"", sw.toString());
    }

    // Tests schema generation when serializeAsIndex is true
    @Test
    public void testGetSchema_serializeAsIndex_returnsIntegerSchema() {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.TRUE);
        SerializerProvider provider = createSerializerProvider();

        JsonNode schema = serializer.getSchema(provider, (Type) SampleEnum.class);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    // Tests schema generation for string enum with enum values list
    @Test
    public void testGetSchema_serializeAsString_returnsStringSchemaWithEnumValues() {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.FALSE);
        SerializerProvider provider = createSerializerProvider();

        JsonNode schema = serializer.getSchema(provider, (Type) SampleEnum.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
        ArrayNode enumNode = (ArrayNode) schema.get("enum");
        assertNotNull(enumNode);
        assertEquals(2, enumNode.size());
        assertEquals("FIRST", enumNode.get(0).asText());
        assertEquals("SECOND", enumNode.get(1).asText());
    }

    // Tests schema generation when typeHint is null or non-enum
    @Test
    public void testGetSchema_nullOrNonEnumTypeHint_returnsStringSchemaWithoutEnumArray() {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.FALSE);
        SerializerProvider provider = createSerializerProvider();

        JsonNode schemaNullHint = serializer.getSchema(provider, null);
        assertNotNull(schemaNullHint);
        assertEquals("string", schemaNullHint.get("type").asText());
        assertNull(schemaNullHint.get("enum"));

        JsonNode schemaNonEnum = serializer.getSchema(provider, (Type) String.class);
        assertNotNull(schemaNonEnum);
        assertEquals("string", schemaNonEnum.get("type").asText());
        assertNull(schemaNonEnum.get("enum"));
    }

    // Tests visitor accepting index format
    @Test
    public void testAcceptJsonFormatVisitor_serializeAsIndex_visitsIntFormat() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.TRUE);
        final boolean[] intVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(createSerializerProvider()) {
            @Override
            public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                intVisited[0] = true;
                return null;
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(SampleEnum.class));
        assertTrue(intVisited[0]);
    }

    // Tests visitor accepting string format with enum values
    @Test
    public void testAcceptJsonFormatVisitor_serializeAsString_visitsStringFormatWithValues() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), Boolean.FALSE);
        final Set<String> capturedEnums = new LinkedHashSet<String>();

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(createSerializerProvider()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        capturedEnums.addAll(enums);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(SampleEnum.class));
        assertEquals(2, capturedEnums.size());
        assertTrue(capturedEnums.contains("FIRST"));
        assertTrue(capturedEnums.contains("SECOND"));
    }

    // Tests visitor accepting string format with WRITE_ENUMS_USING_TO_STRING
    @Test
    public void testAcceptJsonFormatVisitor_withToStringFeature_visitsStringFormatWithToStringValues() throws Exception {
        EnumSerializer serializer = new EnumSerializer(createEnumValues(), null);
        ObjectMapper toStringMapper = new ObjectMapper();
        toStringMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider provider = toStringMapper.getSerializerProviderInstance();

        final Set<String> capturedEnums = new LinkedHashSet<String>();
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        capturedEnums.addAll(enums);
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, mapper.constructType(SampleEnum.class));
        assertEquals(2, capturedEnums.size());
        assertTrue(capturedEnums.contains("custom_first"));
        assertTrue(capturedEnums.contains("custom_second"));
    }
}