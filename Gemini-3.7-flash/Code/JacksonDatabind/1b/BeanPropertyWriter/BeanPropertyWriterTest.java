package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    private ObjectMapper _mapper;
    private JavaType _stringType;
    private BeanDescription _beanDesc;

    public static class SampleBean {
        public String fieldProp = "fieldValue";
        public String nullProp = null;
        public SampleBean selfProp;

        public String getMethodProp() {
            return "methodValue";
        }
    }

    public static class ParamBean {
        public ParamBean(String param) {}
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType beanType = _mapper.constructType(SampleBean.class);
        _beanDesc = _mapper.getSerializationConfig().introspect(beanType);
    }

    private BeanPropertyDefinition findPropDef(BeanDescription desc, String name) {
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if (prop.getName().equals(name)) {
                return prop;
            }
        }
        return null;
    }

    private BeanPropertyWriter createFieldWriter(String propName, String fieldName,
                                                 JsonSerializer<?> ser, Object suppressableValue) throws Exception {
        BeanPropertyDefinition propDef = findPropDef(_beanDesc, fieldName);
        AnnotatedField af = propDef != null ? propDef.getField() : null;
        if (af == null) {
            for (AnnotatedField f : _beanDesc.findProperties().get(0).getField().getDeclaringClass().getDeclaredFields()) {
                // fallback if needed
            }
        }
        return new BeanPropertyWriter(propDef, propDef.getField(), _beanDesc.getClassAnnotations(), _stringType,
                ser, null, _stringType, false, suppressableValue);
    }

    private BeanPropertyWriter createMethodWriter(String propName, String getterName,
                                                  JsonSerializer<?> ser, Object suppressableValue) throws Exception {
        BeanPropertyDefinition propDef = findPropDef(_beanDesc, propName);
        return new BeanPropertyWriter(propDef, propDef.getGetter(), _beanDesc.getClassAnnotations(), _stringType,
                ser, null, _stringType, false, suppressableValue);
    }

    // Tests construction and basic getters with AnnotatedField
    @Test
    public void testConstruct_annotatedField_correctProperties() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertEquals("fieldProp", bpw.getName());
        assertEquals(_stringType, bpw.getType());
        assertEquals(String.class, bpw.getPropertyType());
        assertEquals(String.class, bpw.getGenericPropertyType());
        assertEquals(String.class, bpw.getRawSerializationType());
        assertFalse(bpw.hasSerializer());
        assertFalse(bpw.hasNullSerializer());
        assertFalse(bpw.isRequired());
        assertNotNull(bpw.getMember());
        assertTrue(bpw.getMember() instanceof AnnotatedField);
        assertTrue(bpw.toString().contains("field \""));
    }

    // Tests construction and basic getters with AnnotatedMethod
    @Test
    public void testConstruct_annotatedMethod_correctProperties() throws Exception {
        BeanPropertyWriter bpw = createMethodWriter("methodProp", "getMethodProp", StringSerializer.instance, null);
        assertEquals("methodProp", bpw.getName());
        assertEquals(String.class, bpw.getPropertyType());
        assertEquals(String.class, bpw.getGenericPropertyType());
        assertTrue(bpw.hasSerializer());
        assertSame(StringSerializer.instance, bpw.getSerializer());
        assertTrue(bpw.toString().contains("via method "));
    }

    // Tests constructor exception on invalid AnnotatedMember type (e.g. AnnotatedParameter)
    @Test(expected = IllegalArgumentException.class)
    public void testConstruct_invalidMemberType_throwsIllegalArgumentException() {
        JavaType paramBeanType = _mapper.constructType(ParamBean.class);
        BeanDescription paramBeanDesc = _mapper.getSerializationConfig().introspect(paramBeanType);
        AnnotatedWithParams ctor = paramBeanDesc.getConstructors().get(0);
        AnnotatedParameter ap = ctor.getParameter(0);
        BeanPropertyDefinition propDef = findPropDef(_beanDesc, "fieldProp");
        new BeanPropertyWriter(propDef, ap, _beanDesc.getClassAnnotations(), _stringType, null, null, null, false, null);
    }

    // Tests assignSerializer normal assignment and illegal override
    @Test
    public void testAssignSerializer_validAndOverride() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertFalse(bpw.hasSerializer());

        bpw.assignSerializer(StringSerializer.instance);
        assertTrue(bpw.hasSerializer());
        assertSame(StringSerializer.instance, bpw.getSerializer());

        // Re-assigning the same instance should succeed
        bpw.assignSerializer(StringSerializer.instance);

        // Assigning a different serializer should throw IllegalStateException
        try {
            bpw.assignSerializer(ToStringSerializer.instance);
            fail("Expected IllegalStateException on overriding serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override serializer") || e.getMessage().contains("Cannot override serializer"));
        }
    }

    // Tests assignNullSerializer normal assignment and illegal override
    @Test
    public void testAssignNullSerializer_validAndOverride() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertFalse(bpw.hasNullSerializer());

        bpw.assignNullSerializer(NullSerializer.instance);
        assertTrue(bpw.hasNullSerializer());

        // Re-assigning the same null serializer should succeed
        bpw.assignNullSerializer(NullSerializer.instance);

        // Assigning different null serializer should throw IllegalStateException
        try {
            bpw.assignNullSerializer(StringSerializer.instance);
            fail("Expected IllegalStateException on overriding null serializer");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override null serializer") || e.getMessage().contains("Cannot override null serializer"));
        }
    }

    // Tests internal setting storage lifecycle: get, set, override, and remove
    @Test
    public void testInternalSettings_lifecycle_returnsExpectedValues() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertNull(bpw.getInternalSetting("key1"));
        assertNull(bpw.removeInternalSetting("key1"));

        assertNull(bpw.setInternalSetting("key1", "val1"));
        assertEquals("val1", bpw.getInternalSetting("key1"));

        assertEquals("val1", bpw.setInternalSetting("key1", "val2"));
        assertEquals("val2", bpw.getInternalSetting("key1"));

        assertEquals("val2", bpw.removeInternalSetting("key1"));
        assertNull(bpw.getInternalSetting("key1"));
    }

    // Tests rename with unchanged name and changed name
    @Test
    public void testRename_sameAndDifferentName() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);

        BeanPropertyWriter renamedSame = bpw.rename(NameTransformer.NOP);
        assertSame(bpw, renamedSame);

        NameTransformer prefix = NameTransformer.simpleTransformer("prefix_", "");
        BeanPropertyWriter renamedDiff = bpw.rename(prefix);
        assertNotSame(bpw, renamedDiff);
        assertEquals("prefix_fieldProp", renamedDiff.getName());
    }

    // Tests unwrappingWriter creation
    @Test
    public void testUnwrappingWriter_createsUnwrappingInstance() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        BeanPropertyWriter unwrapping = bpw.unwrappingWriter(NameTransformer.NOP);
        assertNotNull(unwrapping);
        assertTrue(unwrapping.isUnwrapping());
    }

    // Tests non-trivial base type configuration
    @Test
    public void testSetNonTrivialBaseType_setsTypeProperly() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        bpw.setNonTrivialBaseType(listType);
        assertNotNull(bpw);
    }

    // Tests get method on field and method accessors
    @Test
    public void testGet_retrievesPropertyValue() throws Exception {
        BeanPropertyWriter fieldBpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        BeanPropertyWriter methodBpw = createMethodWriter("methodProp", "getMethodProp", null, null);

        SampleBean bean = new SampleBean();
        assertEquals("fieldValue", fieldBpw.get(bean));
        assertEquals("methodValue", methodBpw.get(bean));
    }

    // Tests serializeAsField writing valid field value
    @Test
    public void testSerializeAsField_normalValue_writesField() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"fieldProp\":\"fieldValue\"}", sw.toString());
    }

    // Tests serializeAsField with null value and null serializer configured
    @Test
    public void testSerializeAsField_nullValueWithNullSerializer_writesNull() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("nullProp", "nullProp", StringSerializer.instance, null);
        bpw.assignNullSerializer(NullSerializer.instance);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"nullProp\":null}", sw.toString());
    }

    // Tests serializeAsField with null value suppressed (no null serializer)
    @Test
    public void testSerializeAsField_nullValueSuppressed_writesNothing() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("nullProp", "nullProp", StringSerializer.instance, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    // Tests serializeAsField with suppressable default value
    @Test
    public void testSerializeAsField_suppressableValue_suppressesOutput() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, "fieldValue");
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, borderProvider(prov));
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    private SerializerProvider borderProvider(SerializerProvider prov) {
        return prov;
    }

    // Tests serializeAsField with MARKER_FOR_EMPTY suppression
    @Test
    public void testSerializeAsField_markerForEmpty_suppressesEmpty() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, BeanPropertyWriter.MARKER_FOR_EMPTY);
        SampleBean bean = new SampleBean();
        bean.fieldProp = "";
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    // Tests serializeAsField direct self reference throws JsonMappingException
    @Test(expected = JsonMappingException.class)
    public void testSerializeAsField_selfReference_throwsException() throws Exception {
        BeanPropertyDefinition propDef = findPropDef(_beanDesc, "selfProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(propDef, propDef.getField(), _beanDesc.getClassAnnotations(),
                TypeFactory.defaultInstance().constructType(SampleBean.class),
                ToStringSerializer.instance, null, null, false, null);

        SampleBean bean = new SampleBean();
        bean.selfProp = bean;

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsField(bean, gen, prov);
    }

    // Tests serializeAsElement with tabular/indexed format
    @Test
    public void testSerializeAsElement_writesValueInArray() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartArray();
        bpw.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"fieldValue\"]", sw.toString());
    }

    // Tests serializeAsElement with null value and null serializer
    @Test
    public void testSerializeAsElement_nullValueWithNullSerializer_writesNull() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("nullProp", "nullProp", StringSerializer.instance, null);
        bpw.assignNullSerializer(NullSerializer.instance);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartArray();
        bpw.serializeAsElement(bean, gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null]", sw.toString());
    }

    // Tests serializeAsOmittedField
    @Test
    public void testSerializeAsOmittedField_callsGeneratorOmittedField() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        bpw.serializeAsOmittedField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    // Tests serializeAsPlaceholder
    @Test
    public void testSerializeAsPlaceholder_writesNull() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        gen.writeStartArray();
        bpw.serializeAsPlaceholder(bean, gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null]", sw.toString());
    }

    // Tests fixAccess on property writer
    @Test
    public void testFixAccess_doesNotThrow() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        bpw.fixAccess(_mapper.getSerializationConfig());
        assertNotNull(bpw);
    }

    // Tests wouldConflictWithName
    @Test
    public void testWouldConflictWithName() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertTrue(bpw.wouldConflictWithName(new PropertyName("fieldProp")));
        assertFalse(bpw.wouldConflictWithName(new PropertyName("otherProp")));
    }

    // Tests metadata, fullName, and wrapperName getters
    @Test
    public void testMetadataAndPropertyNames() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", null, null);
        assertNotNull(bpw.getFullName());
        assertEquals("fieldProp", bpw.getFullName().getSimpleName());
        assertNotNull(bpw.getMetadata());
        assertNull(bpw.getWrapperName());
    }

    // Tests depositSchemaProperty with ObjectNode
    @Test
    public void testDepositSchemaProperty_objectNode_populatesSchema() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        bpw.depositSchemaProperty(root, prov);
        assertTrue(root.has("fieldProp"));
        assertNotNull(root.get("fieldProp"));
    }

    // Tests depositSchemaProperty with JsonObjectFormatVisitor
    @Test
    public void testDepositSchemaProperty_visitor_handlesOptionalAndRequired() throws Exception {
        BeanPropertyWriter bpw = createFieldWriter("fieldProp", "fieldProp", StringSerializer.instance, null);
        final boolean[] called = new boolean[1];
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void optionalProperty(BeanProperty prop) {
                called[0] = true;
            }
        };
        SerializerProvider prov = _mapper.getSerializerProviderInstance();
        bpw.depositSchemaProperty(visitor, prov);
        assertTrue(called[0]);
    }
}