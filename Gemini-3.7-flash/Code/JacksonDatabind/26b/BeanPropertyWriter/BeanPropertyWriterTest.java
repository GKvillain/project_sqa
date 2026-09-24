package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    static class SimpleBean {
        public String name = "test";
        private int count = 42;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    static class SelfRefBean {
        public SelfRefBean self;
    }

    private static class DummyPropertyDefinition extends BeanPropertyDefinition {
        private final String _name;
        private final PropertyMetadata _metadata;
        private final PropertyName _wrapperName;

        public DummyPropertyDefinition(String name) {
            this(name, PropertyMetadata.STD_OPTIONAL, null);
        }

        public DummyPropertyDefinition(String name, PropertyMetadata metadata, PropertyName wrapperName) {
            _name = name;
            _metadata = metadata;
            _wrapperName = wrapperName;
        }

        @Override public String getName() { return _name; }
        @Override public PropertyName getFullName() { return new PropertyName(_name); }
        @Override public PropertyName getWrapperName() { return _wrapperName; }
        @Override public PropertyMetadata getMetadata() { return _metadata; }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return false; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getGetter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getSetter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedField getField() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedParameter getConstructorParameter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getAccessor() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getMutator() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getPrimaryMember() { return null; }
        @Override public BeanPropertyDefinition withSimpleName(String newName) { return new DummyPropertyDefinition(newName, _metadata, _wrapperName); }
        @Override public BeanPropertyDefinition withName(PropertyName newName) { return new DummyPropertyDefinition(newName.getSimpleName(), _metadata, _wrapperName); }
    }

    private static class DummyAnnotations implements Annotations {
        @Override
        public <A extends Annotation> A get(Class<A> cls) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private BeanPropertyWriter createFieldWriter(String propName, String fieldName, JsonSerializer<?> ser) throws Exception {
        Field field = SimpleBean.class.getField(fieldName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleBean.class, null, null);
        AnnotatedField af = new AnnotatedField(ac, field, new AnnotationMap());
        JavaType type = TypeFactory.defaultInstance().constructType(field.getGenericType());
        BeanPropertyDefinition propDef = new DummyPropertyDefinition(propName);
        return new BeanPropertyWriter(propDef, af, new DummyAnnotations(), type, ser, null, type, false, null);
    }

    private BeanPropertyWriter createMethodWriter(String propName, String methodName, JsonSerializer<?> ser) throws Exception {
        Method method = SimpleBean.class.getMethod(methodName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleBean.class, null, null);
        AnnotatedMethod am = new AnnotatedMethod(ac, method, new AnnotationMap(), null);
        JavaType type = TypeFactory.defaultInstance().constructType(method.getGenericReturnType());
        BeanPropertyDefinition propDef = new DummyPropertyDefinition(propName);
        return new BeanPropertyWriter(propDef, am, new DummyAnnotations(), type, ser, null, type, false, null);
    }

    // Tests getter access via field writer
    @Test
    public void testGet_fieldAccess_returnsValue() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        SimpleBean bean = new SimpleBean();
        bean.name = "custom";

        Object val = writer.get(bean);
        assertEquals("custom", val);
        assertEquals("name", writer.getName());
        assertEquals("name", writer.getFullName().getSimpleName());
        assertFalse(writer.isVirtual());
        assertFalse(writer.isUnwrapping());
    }

    // Tests getter access via method writer
    @Test
    public void testGet_methodAccess_returnsValue() throws Exception {
        BeanPropertyWriter writer = createMethodWriter("count", "getCount", null);
        SimpleBean bean = new SimpleBean();
        bean.setCount(99);

        Object val = writer.get(bean);
        assertEquals(99, val);
        assertEquals(int.class, writer.getPropertyType());
        assertNotNull(writer.getGenericPropertyType());
    }

    // Tests internal settings management (set, get, remove)
    @Test
    public void testInternalSettings_setGetRemove_behavesCorrectly() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        assertNull(writer.getInternalSetting("key1"));

        writer.setInternalSetting("key1", "val1");
        assertEquals("val1", writer.getInternalSetting("key1"));

        Object removed = writer.removeInternalSetting("key1");
        assertEquals("val1", removed);
        assertNull(writer.getInternalSetting("key1"));
        assertNull(writer.removeInternalSetting("key1"));
    }

    // Tests rename with unchanged name returning same instance
    @Test
    public void testRename_sameName_returnsSameInstance() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        BeanPropertyWriter renamed = writer.rename(NameTransformer.NOP);
        assertSame(writer, renamed);
    }

    // Tests rename with changed name returning new renamed instance
    @Test
    public void testRename_differentName_returnsNewRenamedInstance() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "prefix_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed;
            }
        };

        BeanPropertyWriter renamed = writer.rename(transformer);
        assertNotSame(writer, renamed);
        assertEquals("prefix_name", renamed.getName());
    }

    // Tests unwrapping writer creation
    @Test
    public void testUnwrappingWriter_validTransformer_returnsUnwrappingInstance() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        BeanPropertyWriter unwrapping = writer.unwrappingWriter(NameTransformer.NOP);
        assertNotNull(unwrapping);
        assertTrue(unwrapping.isUnwrapping());
    }

    // Tests assigning serializers successfully and detecting invalid overrides
    @Test
    public void testAssignSerializer_validAndIllegalOverride() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        assertFalse(writer.hasSerializer());

        JsonSerializer<Object> ser1 = ToStringSerializer.instance;
        writer.assignSerializer(ser1);
        assertTrue(writer.hasSerializer());
        assertSame(ser1, writer.getSerializer());

        // Re-assigning the same serializer instance should succeed
        writer.assignSerializer(ser1);

        // Assigning a different serializer should throw IllegalStateException
        try {
            writer.assignSerializer(NullSerializer.instance);
            fail("Expected IllegalStateException on serializer override");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override serializer"));
        }
    }

    // Tests assigning null serializer and detecting invalid overrides
    @Test
    public void testAssignNullSerializer_validAndIllegalOverride() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        assertFalse(writer.hasNullSerializer());

        JsonSerializer<Object> nullSer = NullSerializer.instance;
        writer.assignNullSerializer(nullSer);
        assertTrue(writer.hasNullSerializer());

        // Re-assigning the same instance should succeed
        writer.assignNullSerializer(nullSer);

        // Assigning different instance should throw IllegalStateException
        try {
            writer.assignNullSerializer(ToStringSerializer.instance);
            fail("Expected IllegalStateException on null serializer override");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Can not override null serializer"));
        }
    }

    // Tests type serializer and non-trivial base type assignment
    @Test
    public void testTypeSerializerAndNonTrivialBaseType_accessors_match() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        assertNull(writer.getTypeSerializer());

        writer.assignTypeSerializer(null);
        assertNull(writer.getTypeSerializer());

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        writer.setNonTrivialBaseType(stringType);
        assertEquals(String.class, writer.getRawSerializationType());
    }

    // Tests wouldConflictWithName matching rules
    @Test
    public void testWouldConflictWithName_variousInputs_returnsExpected() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        assertTrue(writer.wouldConflictWithName(new PropertyName("name")));
        assertFalse(writer.wouldConflictWithName(new PropertyName("other")));
        assertFalse(writer.wouldConflictWithName(new PropertyName("name", "http://ns")));
    }

    // Tests wouldConflictWithName with explicit wrapper name
    @Test
    public void testWouldConflictWithName_withWrapperName_checksWrapper() throws Exception {
        Field field = SimpleBean.class.getField("name");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleBean.class, null, null);
        AnnotatedField af = new AnnotatedField(ac, field, new AnnotationMap());
        JavaType type = TypeFactory.defaultInstance().constructType(field.getGenericType());
        PropertyName wrapperName = new PropertyName("wrapper");
        BeanPropertyDefinition propDef = new DummyPropertyDefinition("name", PropertyMetadata.STD_OPTIONAL, wrapperName);

        BeanPropertyWriter writer = new BeanPropertyWriter(propDef, af, new DummyAnnotations(), type, null, null, type, false, null);
        assertTrue(writer.wouldConflictWithName(new PropertyName("wrapper")));
        assertFalse(writer.wouldConflictWithName(new PropertyName("name")));
    }

    // Tests findFormatOverrides caching behavior
    @Test
    public void testFindFormatOverrides_nullIntrospector_cachesNoFormat() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        JsonFormat.Value format = writer.findFormatOverrides(null);
        assertNull(format);
        // Second call should return cached NO_FORMAT (which maps to null)
        assertNull(writer.findFormatOverrides(null));
    }

    // Tests serialization as JSON Object field
    @Test
    public void testSerializeAsField_normalValue_writesOutput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean();
        bean.name = "jackson";
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"jackson\""));
        assertTrue(json.contains("\"count\":42"));
    }

    // Tests serializeAsPlaceholder writing null or serializer output
    @Test
    public void testSerializeAsPlaceholder_withoutNullSerializer_writesNull() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        gen.writeStartArray();
        writer.serializeAsPlaceholder(new SimpleBean(), gen, prov);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null]", sw.toString());
    }

    // Tests serializeAsOmittedField
    @Test
    public void testSerializeAsOmittedField_normalGenerator_doesNotThrow() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        gen.writeStartObject();
        writer.serializeAsOmittedField(new SimpleBean(), gen, prov);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    // Tests direct self reference throwing exception when FAIL_ON_SELF_REFERENCES is enabled
    @Test(expected = JsonMappingException.class)
    public void testHandleSelfReference_cycleDetected_throwsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        SelfRefBean bean = new SelfRefBean();
        bean.self = bean;
        mapper.writeValueAsString(bean);
    }

    // Tests schema deposition with visitor
    @Test
    public void testDepositSchemaProperty_withVisitor_invokesVisitorMethod() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        final boolean[] called = new boolean[1];
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void optionalProperty(BeanProperty prop) {
                called[0] = true;
                assertEquals("name", prop.getName());
            }
        };

        writer.depositSchemaProperty(visitor);
        assertTrue(called[0]);
    }

    // Tests deprecated depositSchemaProperty with ObjectNode
    @Test
    public void testDepositSchemaProperty_withObjectNode_populatesNode() throws Exception {
        BeanPropertyWriter writer = createFieldWriter("name", "name", null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        ObjectNode propertiesNode = JsonNodeFactory.instance.objectNode();

        writer.depositSchemaProperty(propertiesNode, prov);
        assertNotNull(propertiesNode.get("name"));
    }

    // Tests toString format representation
    @Test
    public void testToString_fieldAndMethodWriters_containsMetadata() throws Exception {
        BeanPropertyWriter fieldWriter = createFieldWriter("name", "name", null);
        String fieldStr = fieldWriter.toString();
        assertTrue(fieldStr.contains("property 'name'"));
        assertTrue(fieldStr.contains("field"));

        BeanPropertyWriter methodWriter = createMethodWriter("count", "getCount", ToStringSerializer.instance);
        String methodStr = methodWriter.toString();
        assertTrue(methodStr.contains("property 'count'"));
        assertTrue(methodStr.contains("via method"));
        assertTrue(methodStr.contains("static serializer"));
    }
}