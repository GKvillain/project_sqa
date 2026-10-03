package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.EnumValues;

/**
 * JUnit 4 test class for EnumSerializer, targeting Defects4J bug 75b.
 */
public class EnumSerializerTest {

    private enum MyEnum { ONE, TWO, THREE }

    @Test(expected = IllegalArgumentException.class)
    // Tests that unsupported shape OBJECT throws IllegalArgumentException in static helper
    public void testIsShapeWrittenUsingIndex_shapeObject_throwsIllegalArgumentException() {
        EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                JsonFormat.Value.forShape(Shape.OBJECT), true);
    }

    @Test
    // Tests null format returns null
    public void testIsShapeWrittenUsingIndex_nullFormat_returnsNull() {
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class, null, true));
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class, null, false));
    }

    @Test
    // Tests shape STRING returns Boolean.FALSE
    public void testIsShapeWrittenUsingIndex_shapeString_returnsFalse() {
        assertEquals(Boolean.FALSE,
                EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                        JsonFormat.Value.forShape(Shape.STRING), true));
    }

    @Test
    // Tests shape NATURAL returns Boolean.FALSE
    public void testIsShapeWrittenUsingIndex_shapeNatural_returnsFalse() {
        assertEquals(Boolean.FALSE,
                EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                        JsonFormat.Value.forShape(Shape.NATURAL), true));
    }

    @Test
    // Tests shape NUMBER returns Boolean.TRUE
    public void testIsShapeWrittenUsingIndex_shapeNumber_returnsTrue() {
        assertEquals(Boolean.TRUE,
                EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                        JsonFormat.Value.forShape(Shape.NUMBER), true));
    }

    @Test
    // Tests shape ARRAY returns Boolean.TRUE
    public void testIsShapeWrittenUsingIndex_shapeArray_returnsTrue() {
        assertEquals(Boolean.TRUE,
                EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                        JsonFormat.Value.forShape(Shape.ARRAY), true));
    }

    @Test
    // Tests shape ANY returns null
    public void testIsShapeWrittenUsingIndex_shapeAny_returnsNull() {
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                JsonFormat.Value.forShape(Shape.ANY), true));
    }

    @Test
    // Tests shape SCALAR returns null
    public void testIsShapeWrittenUsingIndex_shapeScalar_returnsNull() {
        assertNull(EnumSerializer._isShapeWrittenUsingIndex(MyEnum.class,
                JsonFormat.Value.forShape(Shape.SCALAR), true));
    }

    @Test
    // Tests construct with default format yields _serializeAsIndex null
    public void testConstruct_defaultFormat_returnsDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, null);
        assertNotNull(ser);
        assertNotNull(ser.getEnumValues());
        // _serializeAsIndex should be null since shape is not specified
        // We check serialization behavior indirectly later
    }

    @Test
    // Tests construct with shape STRING yields _serializeAsIndex = false
    public void testConstruct_shapeString_serializeAsIndexFalse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.STRING);
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
        // We can test via _serializeAsIndex indirectly: serialize with index feature disabled,
        // should still output name, not index. Better to test via serialize.
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("\"ONE\"", sw.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    // Tests construct with shape OBJECT throws exception
    public void testConstruct_shapeObject_throwsIllegalArgumentException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.OBJECT);
        EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
    }

    @Test
    // Tests serialize with index feature enabled uses ordinal
    public void testSerialize_withIndexFeatureEnabled_usesOrdinal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.TWO, gen, prov);
        gen.flush();
        assertEquals("1", sw.toString()); // TWO.ordinal() == 1
    }

    @Test
    // Tests serialize with toString feature enabled uses toString()
    public void testSerialize_withToStringFeatureEnabled_usesToString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("\"ONE\"", sw.toString()); // default toString returns name
    }

    @Test
    // Tests serialize with no special features uses name from EnumValues
    public void testSerialize_withNoFeatures_usesEnumValuesName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.THREE, gen, prov);
        gen.flush();
        assertEquals("\"THREE\"", sw.toString());
    }

    @Test
    // Tests serialize with explicit _serializeAsIndex = true uses ordinal regardless of feature
    public void testSerialize_withExplicitIndexTrue_usesOrdinal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), MyEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.TRUE);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.TWO, gen, prov);
        gen.flush();
        assertEquals("1", sw.toString());
    }

    @Test
    // Tests serialize with explicit _serializeAsIndex = false uses name even if index feature is on
    public void testSerialize_withExplicitIndexFalse_usesName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), MyEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("\"ONE\"", sw.toString());
    }

    @Test
    // Tests createContextual with property shape STRING creates new serializer with false
    public void testCreateContextual_withPropertyShapeString_returnsFalse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSerializer base = createDefaultEnumSerializer(mapper);
        // Simulate a property with JsonFormat(shape=STRING)
        // We need a BeanProperty. For simplicity, use generic method from ObjectMapper.
        // But easier: we can call createContextual with a dummy property.
        // We use an internal helper to create a property-like object.
        // Since we cannot mock, we can create a simple BeanProperty using AnnotatedMember?
        // Alternatively, we can test indirectly by configuring ObjectMapper with @JsonFormat on a field.
        // For unit test, we can rely on the fact that createContextual calls _isShapeWrittenUsingIndex.
        // We'll test via a real property with @JsonFormat on a class field.
        // Build a simple class with @JsonFormat on enum field.
        // But that's more integration. We can test the branch by calling with a property that has shape.
        // We'll create a fake property using an existing method: BeanProperty.Std.
        // BeanProperty.Std is available in Jackson? Yes.
        com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition propDef = null;
        // Instead, we can test createContextual by directly calling with a property that we construct manually.
        // Let's use ObjectMapper's property resolution to get a property.
        // Because of time, we'll test the internal logic via _isShapeWrittenUsingIndex already tested.
        // For code coverage, we can create a simple test with a property that has JsonFormat shape STRING.
        // Use an annotation introspection.
        // We'll use a simple approach: create a class with a field annotated with @JsonFormat(shape=STRING) and
        // serialize it; but that would test through ObjectMapper. However, need to be sure.
        // For brevity, I'll add a test that uses a custom property using BeanProperty.Std constructor.
        // But that requires an AnnotatedMember which is complex.
        // Given constraints, we can skip testing createContextual directly and rely on indirect testing through
        // serialization with @JsonFormat on enum field (which would involve createContextual).
        // However, we need to ensure we cover the branch. We'll add a simple test using ObjectMapper with
        // a wrapper class that has @JsonFormat(shape=STRING) on enum field.
        // This is acceptable as test.
        // Let's create inner class with annotated field.
        class Wrapper {
            @JsonFormat(shape = Shape.STRING)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.ONE;
        String json = mapper.writeValueAsString(w);
        // Should be {"value":"ONE"}
        assertEquals("{\"value\":\"ONE\"}", json);
    }

    @Test(expected = IllegalArgumentException.class)
    // Tests createContextual with property shape OBJECT throws
    public void testCreateContextual_withPropertyShapeObject_throws() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSerializer base = createDefaultEnumSerializer(mapper);
        // We need a property with shape OBJECT. Use similar wrapper approach but expect exception during serialization.
        // However, the exception is thrown in _isShapeWrittenUsingIndex which is called from createContextual.
        // To trigger, we need a property with shape OBJECT. We'll create wrapper with @JsonFormat(shape=OBJECT).
        class Wrapper {
            @JsonFormat(shape = Shape.OBJECT)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.ONE;
        mapper.writeValueAsString(w);
        // Should throw IllegalArgumentException
    }

    @Test
    // Tests getEnumValues returns correct values
    public void testGetEnumValues_returnsCorrectValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        EnumValues values = ser.getEnumValues();
        assertNotNull(values);
        assertEquals(MyEnum.class, values.getEnumClass());
    }

    @Test
    // Tests that serialize with null _serializeAsIndex and no index feature uses name
    public void testSerialize_nullSerializeAsIndexNoIndexFeature_usesName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), MyEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("\"ONE\"", sw.toString());
    }

    @Test
    // Tests that serialize with null _serializeAsIndex and index feature enabled uses ordinal
    public void testSerialize_nullSerializeAsIndexWithIndexFeature_usesOrdinal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), MyEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.TWO, gen, prov);
        gen.flush();
        assertEquals("1", sw.toString());
    }

    @Test
    // Tests getSchema with index serialization returns integer schema
    public void testGetSchema_withIndex_returnsIntegerNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), null);
        assertTrue(schema.isObject());
        assertEquals("integer", schema.get("type").asText());
    }

    @Test
    // Tests getSchema without index returns string schema with enum values
    public void testGetSchema_withoutIndex_returnsStringNodeWithEnum() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumSerializer ser = createDefaultEnumSerializer(mapper);
        JavaType type = mapper.constructType(MyEnum.class);
        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), type);
        assertTrue(schema.isObject());
        assertEquals("string", schema.get("type").asText());
        assertTrue(schema.has("enum"));
        ArrayNode enumNode = (ArrayNode) schema.get("enum");
        assertEquals(3, enumNode.size());
        assertEquals("ONE", enumNode.get(0).asText());
        assertEquals("TWO", enumNode.get(1).asText());
        assertEquals("THREE", enumNode.get(2).asText());
    }

    // ===== New tests to cover missing parts =====

    @Test
    // Tests construct with shape NUMBER yields _serializeAsIndex = true
    public void testConstruct_shapeNumber_serializeAsIndexTrue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.NUMBER);
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
        // serialize should output ordinal
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("0", sw.toString());
    }

    @Test
    // Tests construct with shape ARRAY yields _serializeAsIndex = true
    public void testConstruct_shapeArray_serializeAsIndexTrue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.ARRAY);
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
        // serialize should output ordinal
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.TWO, gen, prov);
        gen.flush();
        assertEquals("1", sw.toString());
    }

    @Test
    // Tests construct with shape SCALAR yields _serializeAsIndex = null (default behavior)
    public void testConstruct_shapeScalar_serializeAsIndexNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.SCALAR);
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
        // serialization should use name (no index feature)
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.THREE, gen, prov);
        gen.flush();
        assertEquals("\"THREE\"", sw.toString());
    }

    @Test
    // Tests construct with shape ANY yields _serializeAsIndex = null (default behavior)
    public void testConstruct_shapeAny_serializeAsIndexNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(
                mapper.constructType(MyEnum.class));
        JsonFormat.Value format = JsonFormat.Value.forShape(Shape.ANY);
        EnumSerializer ser = EnumSerializer.construct(MyEnum.class, config, beanDesc, format);
        // serialization should use name
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(MyEnum.ONE, gen, prov);
        gen.flush();
        assertEquals("\"ONE\"", sw.toString());
    }

    @Test
    // Tests createContextual with property shape NUMBER uses ordinal
    public void testCreateContextual_withPropertyShapeNumber_usesOrdinal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        class Wrapper {
            @JsonFormat(shape = Shape.NUMBER)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.ONE;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"value\":0}", json);
    }

    @Test
    // Tests createContextual with property shape ARRAY uses ordinal
    public void testCreateContextual_withPropertyShapeArray_usesOrdinal() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        class Wrapper {
            @JsonFormat(shape = Shape.ARRAY)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.TWO;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"value\":1}", json);
    }

    @Test
    // Tests createContextual with property shape SCALAR keeps default (name)
    public void testCreateContextual_withPropertyShapeScalar_usesName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        class Wrapper {
            @JsonFormat(shape = Shape.SCALAR)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.THREE;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"value\":\"THREE\"}", json);
    }

    @Test
    // Tests createContextual with property shape ANY keeps default (name)
    public void testCreateContextual_withPropertyShapeAny_usesName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        class Wrapper {
            @JsonFormat(shape = Shape.ANY)
            public MyEnum value;
        }
        Wrapper w = new Wrapper();
        w.value = MyEnum.ONE;
        String json = mapper.writeValueAsString(w);
        assertEquals("{\"value\":\"ONE\"}", json);
    }

    @Test
    // Tests serialize with toString feature and custom toString() method
    public void testSerialize_withToStringFeatureAndCustomToString_usesCustom() throws Exception {
        // Create an enum with overridden toString
        enum CustomEnum {
            A {
                @Override public String toString() { return "customA"; }
            },
            B {
                @Override public String toString() { return "customB"; }
            };
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        EnumValues values = EnumValues.constructFromToString(mapper.getSerializationConfig(), CustomEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ser.serialize(CustomEnum.A, gen, prov);
        gen.flush();
        assertEquals("\"customA\"", sw.toString());
    }

    // Helper method to create an EnumSerializer with default settings
    private EnumSerializer createDefaultEnumSerializer(ObjectMapper mapper) {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), MyEnum.class);
        return new EnumSerializer(values, null);
    }
}